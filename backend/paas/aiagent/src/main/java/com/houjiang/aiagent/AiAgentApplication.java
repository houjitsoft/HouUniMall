package com.houjiang.aiagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * AI 智能体服务。
 *
 * <p>注册到 Nacos 的服务名是 {@code ai}（见 bootstrap.yml），对应 BFF 的
 * {@code @FeignClient(name = "ai")}，承接全部 {@code /web/ai/**} 接口。
 */
@EnableFeignClients
@SpringBootApplication(scanBasePackages = {"com.houjiang.*"})
@EnableDiscoveryClient
@MapperScan({"com.houjiang.aiagent.mapper"})
public class AiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAgentApplication.class, args);
    }
}
