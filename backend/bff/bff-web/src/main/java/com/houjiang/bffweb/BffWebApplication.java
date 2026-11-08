package com.houjiang.bffweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * BFF Web应用启动类
/**
 *
 * @description Web端BFF服务，负责前端请求的适配和转发
 * @author Amos
 * @date 2024-01-01
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class BffWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffWebApplication.class, args);
    }

}