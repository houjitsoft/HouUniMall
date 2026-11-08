package com.houjiang.userms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = {"com.houjiang.userms.feign", "com.houjiang.activity.feign"})
// 追加 com.houjiang.activity.feign：activity-shared 里的 @FeignClient 在该包下，
// 插件 @Autowired 它们时宿主必须已注册对应 bean，否则 NoSuchBeanDefinitionException。

@SpringBootApplication(scanBasePackages={"com.houjiang.*"})
@EnableDiscoveryClient
public class UsermsApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsermsApplication.class, args);
    }
}
