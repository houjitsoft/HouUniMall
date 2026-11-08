package com.houjiang.commons.config;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置类，用于创建和管理全局线程池。
 * @author Amos
 */
@Configuration
public class ThreadPoolConfig {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolConfig.class);

    /** 使用全局线程池 */
    public static final ExecutorService executorService = ThreadPoolConfig.createThreadPool();

    /** 线程池核心线程数 */
    private static final int CORE_POOL_SIZE = 5;
    /** 线程池最大线程数 */
    private static final int MAX_POOL_SIZE = 10;
    /** 线程空闲时间 */
    private static final long KEEP_ALIVE_TIME = 60L;
    /** 任务队列容量 */
    private static final int QUEUE_CAPACITY = 100;

    /**
     * 创建并返回一个线程池实例。
     * 线程池的核心线程数、最大线程数、空闲时间以及队列容量均使用类中定义的常量。
     *
     * @return 配置好的线程池实例
     */
    public static ExecutorService createThreadPool() {
        logger.info("Creating thread pool with corePoolSize={}, maxPoolSize={}, keepAliveTime={}s, queueCapacity={}",
                CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, QUEUE_CAPACITY);
        return new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 关闭全局线程池。首先尝试优雅关闭，等待一段时间后如果仍未关闭，则强制关闭。
     * 如果线程池关闭过程中被中断，会记录错误日志并强制关闭线程池。
     */
    public static void shutdownExecutorService() {
        logger.info("Shutting down thread pool...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Thread pool shutdown interrupted", e);
            executorService.shutdownNow();
        }
        logger.info("Thread pool shutdown completed");
    }
}