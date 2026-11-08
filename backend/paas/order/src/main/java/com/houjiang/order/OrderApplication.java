package com.houjiang.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;


@EnableFeignClients(basePackages = {"com.houjiang.order.feign", "com.houjiang.activity.feign"})
// 显式追加 com.houjiang.activity.feign：activity-shared 里的 @FeignClient 在 com.houjiang.activity 包下，
// 而本宿主类位于 com.houjiang.<host> 包，无参 @EnableFeignClients 只扫描宿主自身包，扫不到它们，
// 导致插件 @Autowired 这些 FeignClient 时 NoSuchBeanDefinitionException。
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.houjiang.*"})
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }


}
