package com.houjiang.thirdparty.config;

import feign.Logger;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign客户端配置
 *
 * @description 配置Feign客户端的日志、重试等参数
 * @author Amos
 * @date 2024-01-01
 */
@Configuration
public class FeignConfig {

    /**
     * Feign日志级别配置
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * Feign重试配置
     */
    @Bean
    public Retryer feignRetryer() {
        // 最大重试次数3次，初始间隔100ms，最大间隔1s
        return new Retryer.Default(100, 1000, 3);
    }
}
