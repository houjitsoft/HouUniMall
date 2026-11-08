package com.houjiang.merchant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Merchant服务启动类
 * 已从LikeShopApplication迁移为MerchantApplication，集成Nacos服务发现和Feign远程调用
 * 
 * @author Amos
 * @since 2026-04-28
 */
@Configuration
@EnableFeignClients(defaultConfiguration = com.houjiang.merchant.config.FeignConfig.class)
@SpringBootApplication(scanBasePackages = {"com.houjiang.*"}, exclude = {RedisRepositoriesAutoConfiguration.class})
@MapperScan(basePackages = {"com.houjiang.merchant.common.mapper"})
@EnableDiscoveryClient
@EnableTransactionManagement
public class MerchantApplication {

    /**
     * 应用程序入口
     * 
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MerchantApplication.class, args);
    }

}
