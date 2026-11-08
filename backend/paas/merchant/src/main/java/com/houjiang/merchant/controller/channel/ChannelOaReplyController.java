package com.houjiang.merchant.controller.channel;

import com.houjiang.merchant.service.IChannelOaReplyService;
import com.houjiang.merchant.validate.channel.ChannelRpValidate;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.channel.ChannelRpVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("/adminapi/channel.official_account_reply")
public class ChannelOaReplyController {

    @Resource
    IChannelOaReplyService iChannelOaReplyService;

    @GetMapping("/lists")
    @Operation(summary="默认回复列表")
    public AjaxResult<PageResult<ChannelRpVo>> list(@Validated PageValidate pageValidate, @RequestParam("reply_type") Integer replyType) {
        PageResult<ChannelRpVo> list = iChannelOaReplyService.list(pageValidate, replyType);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary="默认回复详情")
    public AjaxResult<ChannelRpVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        ChannelRpVo vo = iChannelOaReplyService.detail(id);
        return AjaxResult.success(vo);
    }

    @PostMapping("/add")
    @Operation(summary="默认回复新增")
    public AjaxResult<Object> add(@Validated @RequestBody ChannelRpValidate defaultValidate) {
        iChannelOaReplyService.add(defaultValidate);
        return AjaxResult.success();
    }

    @PostMapping("/edit")
    @Operation(summary="默认回复编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody ChannelRpValidate defaultValidate) {
        iChannelOaReplyService.edit(defaultValidate);
        return AjaxResult.success();
    }

    @PostMapping("/delete")
    @Operation(summary="默认回复删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iChannelOaReplyService.del(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/status")
    @Operation(summary="默认回复状态")
    public AjaxResult<Object> status(@Validated @RequestBody IdValidate idValidate) {
        iChannelOaReplyService.status(idValidate.getId());
        return AjaxResult.success();
    }

}
