package com.houjiang.mall.controller.decoration;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.decoration.MallStyleAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商城风格配置 Controller（likeshop：{ current, list }）
 * 路径：/mall/config/*
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/config")
@RequiredArgsConstructor
public class MallConfigController {

    private final MallStyleAdminService mallStyleAdminService;

    /**
     * 获取商城风格主题
     * POST /mall/config/getMallStyle
     */
    @PostMapping("/getMallStyle")
    public ResponseResult<Map<String, Object>> getMallStyle(@RequestBody(required = false) Map<String, Object> params) {
        return getMallStyleConfig();
    }

    /**
     * likeshop 路径：GET /mall/config/get_mall_style
     */
    @GetMapping("/get_mall_style")
    public ResponseResult<Map<String, Object>> getMallStyleConfig() {
        try {
            log.info("获取商城风格主题（likeshop）");
            return ResponseResult.success(mallStyleAdminService.getMallStyle());
        } catch (Exception e) {
            log.error("获取商城风格主题失败", e);
            return ResponseResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 保存商城风格主题
     * POST /mall/config/setMallStyle
     */
    @PostMapping("/setMallStyle")
    public ResponseResult<Boolean> setMallStyle(@RequestBody Map<String, Object> params) {
        return setMallStyleConfig(params);
    }

    /**
     * likeshop 路径：POST /mall/config/set_mall_style
     */
    @PostMapping("/set_mall_style")
    public ResponseResult<Boolean> setMallStyleConfig(@RequestBody Map<String, Object> params) {
        try {
            log.info("保存商城风格主题（likeshop），参数：{}", params);
            mallStyleAdminService.setMallStyle(params);
            return ResponseResult.success(true);
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("保存商城风格主题失败", e);
            return ResponseResult.error("保存失败：" + e.getMessage());
        }
    }
}
