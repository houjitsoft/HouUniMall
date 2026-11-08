package com.houjiang.mall.controller.setting;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.setting.SettingWebsiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商城后台 - 网站设置 Controller。
 * 对应 like-admin SettingWebsiteController（前缀 /setting.web.web_setting）。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/setting/website")
@RequiredArgsConstructor
public class MallSettingWebsiteController {

    private final SettingWebsiteService websiteService;

    /** ===== website 网站基础信息 ===== */

    @GetMapping("/lists")
    public ResponseResult<Map<String, Object>> lists() {
        return ResponseResult.success(websiteService.getWebsite());
    }

    @PostMapping("/edit")
    public ResponseResult<Void> edit(@RequestBody Map<String, Object> params) {
        websiteService.setWebsite(params);
        return ResponseResult.success();
    }

    /** ===== copyright 备案信息 ===== */

    @GetMapping("/copyright/lists")
    public ResponseResult<List<Map<String, String>>> copyrightLists() {
        return ResponseResult.success(websiteService.getCopyright());
    }

    @PostMapping("/copyright/edit")
    public ResponseResult<Void> copyrightEdit(@RequestBody Map<String, Object> params) {
        websiteService.setCopyright(params);
        return ResponseResult.success();
    }

    /** ===== agreement 政策协议 ===== */

    @GetMapping("/agreement/lists")
    public ResponseResult<Map<String, Object>> agreementLists() {
        return ResponseResult.success(websiteService.getAgreement());
    }

    @PostMapping("/agreement/edit")
    public ResponseResult<Void> agreementEdit(@RequestBody Map<String, Object> params) {
        websiteService.setAgreement(params);
        return ResponseResult.success();
    }

    /** ===== statistics 站点统计 ===== */

    @GetMapping("/statistics/lists")
    public ResponseResult<Map<String, Object>> statisticsLists() {
        return ResponseResult.success(websiteService.getSiteStatistics());
    }

    @PostMapping("/statistics/edit")
    public ResponseResult<Void> statisticsEdit(@RequestBody Map<String, Object> params) {
        websiteService.setSiteStatistics(params);
        return ResponseResult.success();
    }
}
