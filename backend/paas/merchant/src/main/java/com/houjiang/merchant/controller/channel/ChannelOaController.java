package com.houjiang.merchant.controller.channel;

import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.IChannelOaConfigService;
import com.houjiang.merchant.validate.channel.ChannelOaValidate;
import com.houjiang.merchant.vo.channel.ChannelOaVo;
import com.houjiang.merchant.common.core.AjaxResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("/adminapi/channel.official_account_setting")
public class ChannelOaController {

    @Resource
    IChannelOaConfigService iChannelOaConfigService;

    @GetMapping("/getConfig")
    @Operation(summary="公众号渠道设置详情")
    public AjaxResult<ChannelOaVo> getConfig() {
        ChannelOaVo vo = iChannelOaConfigService.getConfig();
        return AjaxResult.success(vo);
    }

    @Log(title = "公众号渠道设置保存")
    @PostMapping("/setConfig")
    @Operation(summary="公众号渠道设置保存")
    public AjaxResult<Object> setConfig(@Validated @RequestBody ChannelOaValidate channelOaValidate) {
        iChannelOaConfigService.setConfig(channelOaValidate);
        return AjaxResult.success();
    }

}
