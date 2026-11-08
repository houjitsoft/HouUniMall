package com.houjiang.merchant.config;

import feign.Logger;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign客户端配置
 * 
 * @description 配置Feign客户端支持Spring MVC注解、日志、重试等参数
 * @author Amos
 * @since 2026-04-28
 */
@Configuration
public class FeignConfig {

    // 不要在这里声明 Contract bean。
    // 主容器一旦存在 Contract bean，FeignClientsConfiguration 里带 ConversionService 与
    // AnnotatedParameterProcessor 的标准 SpringMvcContract 就会被 @ConditionalOnMissingBean 判掉，
    // 需要 Spring MVC 注解的 FeignClient 可能退化成 feign 原生 Contract.Default，
    // 报 "Method ... not annotated with HTTP method type (ex. GET, POST)"。
    // Spring Boot 默认就提供 SpringMvcContract，无需在此覆盖。
    // （cms 2026-10-17 已按同样方式清理。）

    /**
     * Feign日志级别配置
     *
     * @return Logger.Level 日志级别
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * Feign重试配置
     * 
     * @return Retryer 重试器，最大重试次数3次，初始间隔100ms，最大间隔1s
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 1000, 3);
    }
}
