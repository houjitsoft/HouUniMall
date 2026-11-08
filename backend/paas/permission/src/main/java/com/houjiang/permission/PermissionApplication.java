package com.houjiang.permission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 权限管理服务启动类
 * 
 * @author Amos
 * @since 2025-01-07
 */
@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {"com.houjiang.permission", "com.houjiang.commons"})
public class PermissionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PermissionApplication.class, args);
        System.out.println("权限管理服务启动成功！端口：8200");
    }
}
