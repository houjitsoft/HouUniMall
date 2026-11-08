package com.houjiang.plugin.service;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@AutoConfiguration
@ConditionalOnProperty(name="houcloud.product.code")
public class ProductServiceAutoConfiguration {
    @Bean
    public WebMvcRegistrations productWebMvcRegistrations(@Value("${houcloud.product.code}") String code,
            @Value("${houcloud.product.package}") String pkg) {
        return new WebMvcRegistrations() {
            @Override public RequestMappingHandlerMapping getRequestMappingHandlerMapping() { return new ProductRequestMapping(code,pkg); }
        };
    }
    @Bean @LoadBalanced
    public RestTemplate productControlPlaneRestTemplate(RestTemplateBuilder builder) {
        return builder.setConnectTimeout(Duration.ofSeconds(2)).setReadTimeout(Duration.ofSeconds(3)).build();
    }
    @Bean @ConditionalOnMissingBean(ProductAccessPolicy.class)
    public ProductAccessPolicy productAccessPolicy(
            @org.springframework.beans.factory.annotation.Qualifier("productControlPlaneRestTemplate") RestTemplate client) {
        return new RemoteProductAccessPolicy(client);
    }
    @Bean
    public WebMvcConfigurer productAccessWebMvcConfigurer(@Value("${houcloud.product.code}") String code,
            @Value("${houcloud.product.package}") String pkg, ProductAccessPolicy policy) {
        return new WebMvcConfigurer() {
            @Override public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new ProductAccessInterceptor(code,pkg,policy));
            }
        };
    }
}
