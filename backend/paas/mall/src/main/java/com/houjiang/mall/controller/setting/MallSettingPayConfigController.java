package com.houjiang.mall.controller.setting;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.setting.SettingPayConfigService;
import com.houjiang.mall.validator.BaseValidator;
import com.houjiang.mall.validator.setting.PayConfigUpdateParam;
import com.houjiang.mall.vo.setting.SettingMallPayVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商城后台 - 支付配置 Controller
 * 对应 like-admin SettingPayConfigController（/adminapi/setting.pay.pay_config 与 /adminapi/mall.pay）。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/setting/pay_config")
@RequiredArgsConstructor
public class MallSettingPayConfigController {

    private final SettingPayConfigService payConfigService;

    /**
     * 支付配置列表
     * GET /mall/setting/pay_config/lists
     * 返回结构：{"lists": [...]} （兼容 like-admin 前端的 res.lists 取值）
     */
    @GetMapping("/lists")
    public ResponseResult<Map<String, Object>> lists() {
        List<SettingMallPayVo> lists = payConfigService.list();
        Map<String, Object> data = new HashMap<>();
        data.put("lists", lists);
        return ResponseResult.success(data);
    }

    /**
     * 支付配置详情
     * GET /mall/setting/pay_config/detail?id=1
     */
    @GetMapping("/detail")
    public ResponseResult<SettingMallPayVo> detail(@RequestParam("id") Integer id) {
        BaseValidator.mustId(id, "id");
        return ResponseResult.success(payConfigService.detail(id));
    }

    /**
     * 支付配置编辑
     * POST /mall/setting/pay_config/edit
     */
    @PostMapping("/edit")
    public ResponseResult<Void> edit(@RequestBody PayConfigUpdateParam param) {
        BaseValidator.validate(param);
        payConfigService.edit(param);
        return ResponseResult.success();
    }
}
