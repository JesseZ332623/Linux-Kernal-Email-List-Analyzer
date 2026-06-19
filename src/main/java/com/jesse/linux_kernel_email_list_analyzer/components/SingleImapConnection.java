package com.jesse.linux_kernel_email_list_analyzer.components;

import jakarta.mail.Store;
import org.eclipse.angus.mail.imap.IMAPSSLStore;

import java.util.concurrent.locks.ReentrantLock;

/** 单邮件服务 IMAP 连接实例管理接口。*/
public interface SingleImapConnection
{
    /**
     * 获取邮件服务连接，
     * Store 具体的实现类是 {@link IMAPSSLStore}。
     * 目前的负载下单连接实足矣，
     * 所有的邮件收取操作抢一把 {@link ReentrantLock} 可重入锁，
     * 锁等待则返回 null， 上游需要注意判断。
     */
    Store getStore();
}