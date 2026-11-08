package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GatewayServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 网关服务Web控制器
/**
 * 
 * @author Amos
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/web/gateway")
public class WebGatewayController {

    @Autowired
    private GatewayServiceFeign gatewayServiceFeign;

    /**
     * 获取网关路由信息
     */
    @GetMapping("/routes")
    public ResponseResult getRoutes() {
        return gatewayServiceFeign.getRoutes();
    }

    /**
     * 刷新网关路由
     */
    @PostMapping("/routes/refresh")
    public ResponseResult refreshRoutes() {
        return gatewayServiceFeign.refreshRoutes();
    }

    /**
     * 获取网关健康状态
     */
    @GetMapping("/health")
    public ResponseResult healthCheck() {
        return gatewayServiceFeign.healthCheck();
    }

    /**
     * 获取服务实例列表
     */
    @GetMapping("/instances")
    public ResponseResult getInstances() {
        return gatewayServiceFeign.getInstances();
    }

    /**
     * 获取API统计信息
     */
    @GetMapping("/statistics")
    public ResponseResult getStatistics() {
        return gatewayServiceFeign.getStatistics();
    }
}