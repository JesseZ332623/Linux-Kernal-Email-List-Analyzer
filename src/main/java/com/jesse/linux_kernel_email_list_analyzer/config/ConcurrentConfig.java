package com.jesse.linux_kernel_email_list_analyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 并发组件配置类。*/
@Configuration
public class ConcurrentConfig
{
    /** 邮件服务专用虚拟线程执行器。*/
    @Bean(name = "email-service-executor")
    public ExecutorService emailServiceExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}