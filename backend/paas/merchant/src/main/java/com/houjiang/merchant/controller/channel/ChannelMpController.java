package com.houjiang.merchant.controller.channel;

import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.IChannelMpConfigService;
import com.houjiang.merchant.validate.channel.ChannelMpValidate;
import com.houjiang.merchant.vo.channel.ChannelMpVo;
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
@RequestMapping("/adminapi/channel.mnp_settings")
public class ChannelMpController {

    @Resource
    IChannelMpConfigService iChannelMpConfigService;

    @GetMapping("/getConfig")
    @Operation(summary="微信程序渠道设置详情")
    public AjaxResult<ChannelMpVo> getConfig() {
        ChannelMpVo vo = iChannelMpConfigService.getConfig();
        return AjaxResult.success(vo);
    }

    @Log(title = "微信小程序渠道设置保存")
    @PostMapping("/setConfig")
    @Operation(summary="微信程序渠道设置保存")
    public AjaxResult<Object> setConfig(@Validated @RequestBody ChannelMpValidate channelMpValidate) {
        iChannelMpConfigService.setConfig(channelMpValidate);
        return AjaxResult.success();
    }

}
