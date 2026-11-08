package com.houjiang.merchant.controller.setting;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.ISettingNoticeService;
import com.houjiang.merchant.vo.setting.SettingNoticeDetailVo;
import com.houjiang.merchant.vo.setting.SettingNoticeListedVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @author Amos
 */
@RestController
@RequestMapping("/adminapi/notice.notice")
public class SettingNoticeController {

    @Resource
    ISettingNoticeService iSettingNoticeService;

    @GetMapping("/settingLists")
    @Operation(summary="通知设置列表")
    public AjaxResult<JSONObject> list(@RequestParam Integer recipient) {
        List<SettingNoticeListedVo> list = iSettingNoticeService.list(recipient);
        JSONObject result = new JSONObject();
        result.put("lists", list);
        return AjaxResult.success(result);
    }

    @GetMapping("/detail")
    @Operation(summary="通知设置详情")
    public AjaxResult<SettingNoticeDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        SettingNoticeDetailVo vo = iSettingNoticeService.detail(id);
        return AjaxResult.success(vo);
    }

    @Log(title = "通知设置编辑")
    @PostMapping("/set")
    @Operation(summary="通知设置编辑")
    public AjaxResult<Object> save(@RequestBody JSONObject params) {
        iSettingNoticeService.save(params);
        return AjaxResult.success();
    }

}
