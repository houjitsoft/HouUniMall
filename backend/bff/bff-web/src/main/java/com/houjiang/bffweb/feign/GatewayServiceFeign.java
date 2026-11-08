package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 网关服务Feign客户端
/**
 * 
 * @author Amos
 * @since 2024-01-01
 */
@FeignClient(name = "gateway-service", path = "/gateway", contextId = "gatewayServiceFeign")
public interface GatewayServiceFeign {

    /**
     * 获取网关路由信息
     */
    @GetMapping("/routes")
    ResponseResult getRoutes();

    /**
     * 刷新网关路由
     */
    @PostMapping("/routes/refresh")
    ResponseResult refreshRoutes();

    /**
     * 获取网关健康状态
     */
    @GetMapping("/health")
    ResponseResult healthCheck();

    /**
     * 获取服务实例列表
     */
    @GetMapping("/instances")
    ResponseResult getInstances();

    /**
     * 获取API统计信息
     */
    @GetMapping("/statistics")
    ResponseResult getStatistics();
}