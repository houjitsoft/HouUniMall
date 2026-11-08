package com.houjiang.ext.goods;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/** 通过 AutoConfiguration.imports 发现，无需扩大宿主组件扫描范围。 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "houcloud.custom.goods.hello-world", name = "enabled", havingValue = "true")
public class CustomGoodsAutoConfiguration {
    @Bean
    public HelloWorldExtension helloWorldExtension() { return new HelloWorldExtension(); }
}
