package com.houjiang.merchant.controller.channel;

import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.IChannelH5ConfigService;
import com.houjiang.merchant.validate.channel.ChannelH5Validate;
import com.houjiang.merchant.vo.channel.ChannelH5Vo;
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
@RequestMapping("/adminapi/channel.web_page_setting")
public class ChannelH5Controller {

    @Resource
    IChannelH5ConfigService iChannelH5ConfigService;

    @GetMapping("/getConfig")
    @Operation(summary="H5渠道设置详情")
    public AjaxResult<ChannelH5Vo> getConfig() {
        ChannelH5Vo vo = iChannelH5ConfigService.getConfig();
        return AjaxResult.success(vo);
    }

    @Log(title = "H5渠道设置保存")
    @PostMapping("/setConfig")
    @Operation(summary="H5渠道设置保存")
    public AjaxResult<Object> setConfig(@Validated @RequestBody ChannelH5Validate channelH5Validate) {
        iChannelH5ConfigService.setConfig(channelH5Validate);
        return AjaxResult.success();
    }

}
