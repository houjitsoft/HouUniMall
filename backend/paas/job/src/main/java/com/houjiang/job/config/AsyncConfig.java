//package com.houjiang.job.config;
//
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import java.util.concurrent.Executor;
//
///**
// * @ClassName AsyncConfig
// * @Description
// * @Author Amos
// * @Date 2025/1/21 10:39
// * @Version 1.0
// **/
//@Configuration
//@EnableAsync
//public class AsyncConfig {
//
//    @Bean
//    public Executor taskExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(10);
//        executor.setMaxPoolSize(20);
//        executor.setQueueCapacity(50);
//        executor.setThreadNamePrefix("DcenterJob-");
//        executor.initialize();
//        return executor;
//    }
//}
