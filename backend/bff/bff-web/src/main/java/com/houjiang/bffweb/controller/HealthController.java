package com.houjiang.bffweb.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
/**
 * 
 * @description 提供 BFF Web 服务的健康检查接口
 * @author Amos
 * @date 2024-01-01
 */
@RestController
@RequestMapping("/actuator")
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "bff-web");
        health.put("timestamp", System.currentTimeMillis());
        return health;
    }
}
