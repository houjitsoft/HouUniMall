package com.houjiang.base.feign;

import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * IM Feign 专用短超时：客服配置为首页非关键依赖，避免 IM/DB 抖动拖垮商城首屏。
 * 注意：不要加 {@code @Configuration}，否则会被组件扫描成全局 Feign 配置。
 */
public class ImFeignConfig {

    @Bean
    public Request.Options imRequestOptions() {
        return new Request.Options(1, TimeUnit.SECONDS, 3, TimeUnit.SECONDS, true);
    }

    @Bean
    public Retryer imRetryer() {
        return Retryer.NEVER_RETRY;
    }
}
