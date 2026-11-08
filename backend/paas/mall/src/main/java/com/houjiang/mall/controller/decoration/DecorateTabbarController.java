package com.houjiang.mall.controller.decoration;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.decoration.LikeDecorateTabbarAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 底部导航 Controller
 * 路径：/mall/decoration/tabbar/*
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/decoration/tabbar")
@RequiredArgsConstructor
public class DecorateTabbarController {

    private final LikeDecorateTabbarAdminService likeDecorateTabbarAdminService;

    /**
     * 获取底部导航详情（likeshop：list + style）
     * POST /mall/decoration/tabbar/detail
     */
    @PostMapping("/detail")
    public ResponseResult<Map<String, Object>> getTabbarDetail(@RequestBody(required = false) Map<String, Object> params) {
        try {
            log.info("获取底部导航详情");
            return ResponseResult.success(likeDecorateTabbarAdminService.detail());
        } catch (Exception e) {
            log.error("获取底部导航详情失败", e);
            return ResponseResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 保存底部导航（likeshop：list + style）
     * POST /mall/decoration/tabbar/save
     */
    @PostMapping("/save")
    public ResponseResult<Boolean> saveTabbar(@RequestBody Map<String, Object> params) {
        try {
            log.info("保存底部导航配置");
            likeDecorateTabbarAdminService.save(params);
            return ResponseResult.success(true);
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("保存底部导航配置失败", e);
            return ResponseResult.error("保存失败：" + e.getMessage());
        }
    }
}
