package com.houjiang.activity.feign;

import feign.Contract;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;

/**
 * activity 共享 FeignClient 的专属配置（只在 @FeignClient(configuration=...) 里被引用）。
 *
 * <p>刻意不标 @Configuration：宿主普遍 scanBasePackages=com.houjiang.*，
 * 一旦标了就会被注册成主容器的全局 Contract bean，反而把 FeignClientsConfiguration 里
 * 带 ConversionService 的标准 SpringMvcContract 挤掉。
 *
 * <p>为什么必须显式给一个 Contract：某些宿主（cms）里 FeignContext 子容器拿不到
 * SpringMvcContract，会退化成 feign 原生 Contract.Default，只认 @RequestLine，
 * 于是共享 client 上所有 @PostMapping 都报
 * "Method ... not annotated with HTTP method type (ex. GET, POST)"，
 * 直接导致依赖它的插件装配失败。
 *
 * <p>方法名保持 feignContract，与 FeignClientsConfiguration 同名，
 * 即便 default 配置也生效，也只是覆盖、不会重复注册两个 Contract。
 */
public class ActivityFeignConfig {

    @Bean
    public Contract feignContract() {
        return new SpringMvcContract();
    }
}
