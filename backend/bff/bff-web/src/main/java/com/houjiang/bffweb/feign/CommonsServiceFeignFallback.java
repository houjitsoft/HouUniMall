package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

/**
 * 公共服务Feign客户端降级处理
/**
 * 
 * @author Amos
 * @since 2024-01-01
 */
@Component
public class CommonsServiceFeignFallback implements CommonsServiceFeign {

    @Override
    public ResponseResult getConfig(String key) {
        return ResponseResult.error("获取系统配置失败");
    }

    @Override
    public ResponseResult getAllConfig() {
        return ResponseResult.error("获取所有配置失败");
    }

    @Override
    public ResponseResult getFileConfig() {
        return ResponseResult.error("获取文件上传配置失败");
    }

    @Override
    public ResponseResult getSystemInfo() {
        return ResponseResult.error("获取系统信息失败");
    }

    @Override
    public ResponseResult getSystemVersion() {
        return ResponseResult.error("获取系统版本失败");
    }

    @Override
    public ResponseResult getDictByType(String type) {
        return ResponseResult.error("获取字典数据失败");
    }

    @Override
    public ResponseResult getAreaData() {
        return ResponseResult.error("获取地区数据失败");
    }
}