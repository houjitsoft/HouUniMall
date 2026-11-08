package com.houjiang.bffweb.config;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign配置类
/**
 *
 * 1. FeignRequestInterceptor 已通过 @Component 注册为 Bean，无需重复注册
 * 2. 注册 SpringFormEncoder 以支持 multipart/form-data 文件上传
 *    （否则 MultipartFile 类型参数会被默认 SpringEncoder 拒绝，触发 500）
 *
 * @author Amos
 * @since 2025-01-07
 */
@Configuration
public class FeignConfig {

    /**
     * 提供支持 multipart/form-data 的 Feign Encoder。
/**
     *
     * <p>包装 SpringEncoder 作为底层 fallback，使其同时支持 JSON 和 multipart 两种 Content-Type，
     * 保证文件上传接口（@RequestPart MultipartFile）能正确序列化。
     *
     * @param messageConverters Spring Boot 自动装配的消息转换器集合
     * @return Encoder 同时支持 JSON 与 multipart
     */
    @Bean
    public Encoder feignFormEncoder(ObjectProvider<HttpMessageConverters> messageConverters) {
        ObjectFactory<HttpMessageConverters> factory = messageConverters::getObject;
        return new SpringFormEncoder(new SpringEncoder(factory));
    }
}