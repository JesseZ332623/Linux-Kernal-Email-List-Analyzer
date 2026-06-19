package com.jesse.linux_kernel_email_list_analyzer.components.impl;

import com.jesse.linux_kernel_email_list_analyzer.components.SingleImapConnection;
import com.jesse.linux_kernel_email_list_analyzer.properties.EmailReceiverProperties;
import com.jesse.linux_kernel_email_list_analyzer.repository.ApplicationApiKeysRepository;
import jakarta.annotation.PreDestroy;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/** 单邮件服务 IMAP 连接实例管理接口实现。*/
@Slf4j
@Component
@RequiredArgsConstructor
public class SingleImapConnectionImpl implements SingleImapConnection
{
    /** {@link Store} 是有状态的，每个线程调用 getStore() 的时候都要抢这把锁。*/
    private final ReentrantLock lock = new ReentrantLock();

    /** 邮箱服务属性配置类。*/
    private final EmailReceiverProperties properties;

    /** 第三方应用访问 API Keys 表仓库类。*/
    private final
    ApplicationApiKeysRepository applicationApiKeysRepository;

    /** store 执行 connect() 操作的次数。*/
    private final
    AtomicInteger connectCounts = new AtomicInteger(0);

    /** 邮箱服务会话类。*/
    @Qualifier(value = "gmail-session")
    private final Session session;

    /** 邮件服务 IMAP 连接。*/
    private Store store;

    private boolean isConnected() {
        return Objects.nonNull(this.store) && this.store.isConnected();
    }

    /** 开始连接邮箱服务。（懒加载模式，由 getStore() 触发） */
    @Retryable(
        retryFor    = MessagingException.class,
        maxAttempts = 2,
        backoff     = @Backoff(multiplier = 2.0)
    )
    public void connect()
    {
        final String username = this.properties.getUsername();

        try
        {
            this.store = this.session.getStore();

            store.connect(
                username,
                this.applicationApiKeysRepository
                    .findByAppName(username)
            );

            connectCounts.getAndIncrement();
        }
        catch (MessagingException exception)
        {
            log.error("Connecting to email service failed...", exception);

            // re-throw 异常触发重试
            throw new RuntimeException(exception);
        }
    }

    /** 服务关闭的时候断开与邮箱服务的连接。*/
    @PreDestroy
    public void close()
    {
        try
        {
            if (this.isConnected())
            {
                this.store.close();
                log.info("Closing email servive connection...");
            }
        }
        catch (MessagingException exception) {
            log.error("Closing email servive connection failed...", exception);
        }
    }

    @Override
    public Store getStore()
    {
        final long waitTimeout
            = this.properties.getStoreLockWaitTimeout().toSeconds();

        boolean isLocked = false;

        try
        {
            isLocked = this.lock.tryLock(waitTimeout, TimeUnit.SECONDS);

            if (!isLocked)
            {
                log.warn("Failed to acquire lock within {} seconds.", waitTimeout);
                return null;
            }

            if (!this.isConnected())
            {
                if (this.connectCounts.get() == 0) {
                    log.info("Initialization IMAP connection...");
                }
                else {
                    log.warn("Connection disconnected, restart...");
                }

                this.connect();

                // 重连后再次校验，如果还是不行，这次调用就算失败
                if (!this.isConnected())
                {
                    log.error("Reconnection attempt failed.");
                    return null;
                }
            }

            return this.store;
        }
        catch (InterruptedException exception)
        {
            log.warn("", exception);
            Thread.currentThread().interrupt();
            return null;
        }
        finally
        {
            if (isLocked) {
                lock.unlock();
            }
        }
    }
}