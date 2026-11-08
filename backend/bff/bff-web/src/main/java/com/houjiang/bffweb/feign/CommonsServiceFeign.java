package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 公共服务Feign客户端
/**
 * 
 * @author Amos
 * @since 2024-01-01
 */
@FeignClient(name = "commons-service", path = "/commons", contextId = "commonsServiceFeign")
public interface CommonsServiceFeign {

    /**
     * 获取系统配置
     */
    @GetMapping("/config/{key}")
    ResponseResult getConfig(@PathVariable("key") String key);

    /**
     * 获取所有配置
     */
    @GetMapping("/config")
    ResponseResult getAllConfig();

    /**
     * 获取文件上传配置
     */
    @GetMapping("/file/config")
    ResponseResult getFileConfig();

    /**
     * 获取系统信息
     */
    @GetMapping("/system/info")
    ResponseResult getSystemInfo();

    /**
     * 获取系统版本
     */
    @GetMapping("/system/version")
    ResponseResult getSystemVersion();

    /**
     * 获取字典数据
     */
    @GetMapping("/dict/{type}")
    ResponseResult getDictByType(@PathVariable("type") String type);

    /**
     * 获取地区数据
     */
    @GetMapping("/area")
    ResponseResult getAreaData();
}