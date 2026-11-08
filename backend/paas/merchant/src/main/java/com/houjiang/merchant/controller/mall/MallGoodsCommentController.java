package com.houjiang.merchant.controller.mall;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallGoodsCommentService;
import com.houjiang.merchant.service.mall.IMallGoodsService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallGoodsCommentSearchValidate;
import com.houjiang.merchant.validate.mall.MallGoodsCommentSetIsShowValidate;
import com.houjiang.merchant.validate.mall.MallGoodsCommentSetReplyValidate;
import com.houjiang.merchant.validate.mall.MallGoodsSearchValidate;
import com.houjiang.merchant.vo.mall.MallGoodsCommentListedVo;
import com.houjiang.merchant.vo.mall.MallGoodsListedVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/mall.goodsComment")
public class MallGoodsCommentController {

    @Resource
    IMallGoodsCommentService iMallGoodsCommentService;
    @Resource
    IMallGoodsService iMallGoodsService;

    @GetMapping("/lists")
    @Operation(summary="商品评论列表")
    public AjaxResult<PageResult<MallGoodsCommentListedVo>> list(@Validated PageValidate pageValidate,
                                                                 @Validated MallGoodsCommentSearchValidate searchValidate) {
        PageResult<MallGoodsCommentListedVo> list = iMallGoodsCommentService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @PostMapping("/set_reply")
    @Schema(description = "回复&编辑回复")
    public AjaxResult<Object> setReply(@Validated @RequestBody MallGoodsCommentSetReplyValidate setReplyValidate) {
        iMallGoodsCommentService.setReply(setReplyValidate);
        return AjaxResult.success();
    }

    @PostMapping("/set_is_show")
    @Schema(description = "回复&编辑回复")
    public AjaxResult<Object> setIsShow(@Validated @RequestBody MallGoodsCommentSetIsShowValidate setIsShowValidate) {
        iMallGoodsCommentService.setIsShow(setIsShowValidate);
        return AjaxResult.success();
    }
}
