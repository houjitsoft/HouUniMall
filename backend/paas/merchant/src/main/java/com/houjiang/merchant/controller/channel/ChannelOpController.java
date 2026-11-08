package com.houjiang.merchant.controller.channel;

import com.houjiang.merchant.service.IChannelOpService;
import com.houjiang.merchant.validate.channel.ChannelOpValidate;
import com.houjiang.merchant.vo.channel.ChannelOpVo;
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
@RequestMapping("/adminapi/channel.open_setting/")
public class ChannelOpController {

    @Resource
    IChannelOpService iChannelOpService;

    @GetMapping("/getConfig")
    @Operation(summary="开放平台设置详情")
    public AjaxResult<Object> getConfig() {
        ChannelOpVo vo = iChannelOpService.getConfig();
        return AjaxResult.success(vo);
    }

    @PostMapping("/setConfig")
    @Operation(summary="开放平台设置保存")
    public AjaxResult<Object> save(@Validated @RequestBody ChannelOpValidate opValidate) {
        iChannelOpService.setConfig(opValidate);
        return AjaxResult.success();
    }

}
