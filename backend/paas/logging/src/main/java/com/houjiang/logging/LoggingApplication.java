package com.houjiang.logging;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 日志管理服务启动类
 *
 * <p>负责启动日志微服务，监听 RocketMQ 消息队列收集各服务产生的日志，
 * 并提供日志查询、分析、设置管理等 REST 接口。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@EnableFeignClients
@SpringBootApplication(scanBasePackages = {"com.houjiang.*"})
@EnableDiscoveryClient
@MapperScan({"com.houjiang.**.mapper"})
public class LoggingApplication {

    /**
     * 应用程序入口点
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(LoggingApplication.class, args);
    }
}
