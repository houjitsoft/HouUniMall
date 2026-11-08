package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.CommonsServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 公共服务Web控制器
/**
 * 
 * @author Amos
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/web/commons")
public class WebCommonsController {

    @Autowired
    private CommonsServiceFeign commonsServiceFeign;

    /**
     * 获取系统配置
     */
    @GetMapping("/config/{key}")
    public ResponseResult getConfig(@PathVariable("key") String key) {
        return commonsServiceFeign.getConfig(key);
    }

    /**
     * 获取所有配置
     */
    @GetMapping("/config")
    public ResponseResult getAllConfig() {
        return commonsServiceFeign.getAllConfig();
    }

    /**
     * 获取文件上传配置
     */
    @GetMapping("/file/config")
    public ResponseResult getFileConfig() {
        return commonsServiceFeign.getFileConfig();
    }

    /**
     * 获取系统信息
     */
    @GetMapping("/system/info")
    public ResponseResult getSystemInfo() {
        return commonsServiceFeign.getSystemInfo();
    }

    /**
     * 获取系统版本
     */
    @GetMapping("/system/version")
    public ResponseResult getSystemVersion() {
        return commonsServiceFeign.getSystemVersion();
    }

    /**
     * 获取字典数据
     */
    @GetMapping("/dict/{type}")
    public ResponseResult getDictByType(@PathVariable("type") String type) {
        return commonsServiceFeign.getDictByType(type);
    }

    /**
     * 获取地区数据
     */
    @GetMapping("/area")
    public ResponseResult getAreaData() {
        return commonsServiceFeign.getAreaData();
    }
}