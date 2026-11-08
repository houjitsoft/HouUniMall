package com.houjiang.commons.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;

/**
 * @author Amos
 */
@Configuration
public class AsyncConfig {

    @Bean("versionTaskExecutor")
    public ThreadPoolTaskExecutor versionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("VersionAsync-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("DcenterJob-");
        executor.initialize();
        return executor;
    }

    /** 定义任务调度器（用于延迟执行） */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        // 调度线程数
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("delay-scheduler-");
        scheduler.initialize();
        return scheduler;
    }

}
