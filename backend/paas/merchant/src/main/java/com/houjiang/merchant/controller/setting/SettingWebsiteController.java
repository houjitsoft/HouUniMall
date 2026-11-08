package com.houjiang.merchant.controller.setting;

import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.ISettingCopyrightService;
import com.houjiang.merchant.service.ISettingProtocolService;
import com.houjiang.merchant.service.ISettingWebsiteService;
import com.houjiang.merchant.validate.setting.SettingCopyrightValidate;
import com.houjiang.merchant.validate.setting.SettingAgreementValidate;
import com.houjiang.merchant.validate.setting.SettingSiteStatisticsValidate;
import com.houjiang.merchant.validate.setting.SettingWebsiteValidate;
import com.houjiang.merchant.vo.setting.SettingCopyrightVo;
import com.houjiang.merchant.vo.setting.SettingAgreementVo;
import com.houjiang.merchant.vo.setting.SettingSiteStatisticsVo;
import com.houjiang.merchant.vo.setting.SettingWebsiteVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.util.ConfigUtils;
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
@RequestMapping("adminapi/setting.web.web_setting")
public class SettingWebsiteController {

    @Resource
    ISettingWebsiteService iSettingWebsiteService;

    @Resource
    ISettingCopyrightService iSettingCopyrightService;

    @Resource
    ISettingProtocolService iSettingProtocolService;

    @GetMapping("/getWebsite")
    @Operation(summary="网站配置信息")
    public AjaxResult<SettingWebsiteVo> getWebsite() {
        SettingWebsiteVo detail = iSettingWebsiteService.getWebsite();
        return AjaxResult.success(detail);
    }

    @Log(title = "网站配置编辑")
    @PostMapping("/setWebsite")
    @Operation(summary="网站配置编辑")
    public AjaxResult<Object> setWebsite(@Validated @RequestBody SettingWebsiteValidate websiteValidate) {
        iSettingWebsiteService.setWebsite(websiteValidate);
        return AjaxResult.success();
    }



    @GetMapping("/getCopyright")
    @Operation(summary="网站版权信息")
    public AjaxResult<List<SettingCopyrightVo>> getCopyright() {
        List<SettingCopyrightVo> list = iSettingCopyrightService.getCopyright();
        return AjaxResult.success(list);
    }

    @Log(title = "网站版权编辑")
    @PostMapping("/setCopyright")
    @Operation(summary="网站版权编辑")
    public AjaxResult<Object> setCopyright(@Validated @RequestBody SettingCopyrightValidate copyrightValidate) {
        iSettingCopyrightService.setCopyright(copyrightValidate);
        return AjaxResult.success();
    }

    @GetMapping("/getAgreement")
    @Operation(summary="政策协议信息")
    public AjaxResult<SettingAgreementVo> getAgreement() {
        SettingAgreementVo detail = iSettingProtocolService.getAgreement();
        return AjaxResult.success(detail);
    }

    @Log(title = "政策协议编辑")
    @PostMapping("/setAgreement")
    @Operation(summary="政策协议编辑")
    public AjaxResult<Object> setAgreement(@Validated @RequestBody SettingAgreementValidate protocolValidate) {
        iSettingProtocolService.setAgreement(protocolValidate);
        return AjaxResult.success();
    }


    @GetMapping("/getSiteStatistics")
    @Operation(summary="获取站点统计配置")
    public AjaxResult<SettingSiteStatisticsVo> getSiteStatistics() {
        SettingSiteStatisticsVo vo = new SettingSiteStatisticsVo();
        vo.setClarityCode(ConfigUtils.get("siteStatistics", "clarity_code", ""));
        return AjaxResult.success(vo);
    }

    @Log(title = "站点统计配置")
    @PostMapping("/setSiteStatistics")
    @Operation(summary="站点统计配置")
    public AjaxResult<Object> setSiteStatistics(@Validated @RequestBody SettingSiteStatisticsValidate settingSiteStatisticsValidate) {
        ConfigUtils.set("siteStatistics", "clarity_code", settingSiteStatisticsValidate.getClarityCode());
        return AjaxResult.success();
    }

}
