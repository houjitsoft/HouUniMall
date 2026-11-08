package com.houjiang.mall.controller.setting;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.setting.SettingStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商城后台 - 存储引擎配置 Controller。
 * 对应 like-admin SettingStorageController（/adminapi/setting.storage）。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/setting/storage")
@RequiredArgsConstructor
public class MallSettingStorageController {

    private final SettingStorageService storageService;

    /**
     * 存储引擎列表（硬编码 4 类，状态来自 hou_merchant_config）。
     * GET /mall/setting/storage/lists
     */
    @GetMapping("/lists")
    public ResponseResult<List<Map<String, Object>>> lists() {
        return ResponseResult.success(storageService.list());
    }

    /**
     * 存储引擎详情。
     * GET /mall/setting/storage/detail?engine=aliyun
     */
    @GetMapping("/detail")
    public ResponseResult<Map<String, Object>> detail(@RequestParam("engine") String engine) {
        return ResponseResult.success(storageService.detail(engine));
    }

    /**
     * 存储引擎编辑（保存配置 + 状态）。
     * POST /mall/setting/storage/setup
     * body: {engine, status, bucket, secret_key, access_key, domain, region?, endpoint?}
     */
    @PostMapping("/setup")
    public ResponseResult<Void> setup(@RequestBody Map<String, String> params) {
        storageService.setup(params);
        return ResponseResult.success();
    }

    /**
     * 存储引擎切换（仅切换 default 别名）。
     * POST /mall/setting/storage/change
     * body: {alias, status}
     */
    @PostMapping("/change")
    public ResponseResult<Void> change(@RequestBody Map<String, String> params) {
        String alias = params == null ? null : params.get("alias");
        String statusStr = params == null ? null : params.get("status");
        if (alias == null || alias.isEmpty()) {
            throw new IllegalArgumentException("alias 参数缺失");
        }
        if (statusStr == null || statusStr.isEmpty()) {
            throw new IllegalArgumentException("status 参数缺失");
        }
        Integer status;
        try {
            status = Integer.parseInt(statusStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("status 必须是整数");
        }
        storageService.change(alias, status);
        return ResponseResult.success();
    }
}
