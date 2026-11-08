package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

/**
 * 网关服务Feign客户端降级处理
/**
 * 
 * @author Amos
 * @since 2024-01-01
 */
@Component
public class GatewayServiceFeignFallback implements GatewayServiceFeign {

    @Override
    public ResponseResult getRoutes() {
        return ResponseResult.error("获取网关路由信息失败");
    }

    @Override
    public ResponseResult refreshRoutes() {
        return ResponseResult.error("刷新网关路由失败");
    }

    @Override
    public ResponseResult healthCheck() {
        return ResponseResult.error("网关健康检查失败");
    }

    @Override
    public ResponseResult getInstances() {
        return ResponseResult.error("获取服务实例列表失败");
    }

    @Override
    public ResponseResult getStatistics() {
        return ResponseResult.error("获取API统计信息失败");
    }
}