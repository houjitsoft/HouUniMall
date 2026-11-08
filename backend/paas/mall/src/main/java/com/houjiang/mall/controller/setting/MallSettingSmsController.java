package com.houjiang.mall.controller.setting;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.setting.SettingSmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商城后台 - 短信配置 Controller。
 * 对应 like-admin SettingSmsController（/adminapi/notice.sms_config）。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/setting/sms")
@RequiredArgsConstructor
public class MallSettingSmsController {

    private final SettingSmsService smsService;

    /**
     * 短信引擎列表。
     * GET /mall/setting/sms/lists
     */
    @GetMapping("/lists")
    public ResponseResult<List<Map<String, Object>>> lists() {
        return ResponseResult.success(smsService.list());
    }

    /**
     * 短信引擎详情。
     * GET /mall/setting/sms/detail?type=ali
     */
    @GetMapping("/detail")
    public ResponseResult<Map<String, Object>> detail(@RequestParam("type") String type) {
        return ResponseResult.success(smsService.detail(type));
    }

    /**
     * 短信引擎编辑（保存配置 + 状态）。
     * POST /mall/setting/sms/edit
     * body: {type, status, name, sign, app_key/secret_key 或 app_id/secret_id/secret_key}
     */
    @PostMapping("/edit")
    public ResponseResult<Void> edit(@RequestBody Map<String, Object> params) {
        smsService.edit(params);
        return ResponseResult.success();
    }
}
