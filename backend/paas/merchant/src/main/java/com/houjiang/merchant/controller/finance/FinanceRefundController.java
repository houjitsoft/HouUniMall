package com.houjiang.merchant.controller.finance;

import com.houjiang.merchant.service.finance.IFinanceRefundService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.finance.FinanceRefundSearchValidate;
import com.houjiang.merchant.vo.finance.FinanceRefundListVo;
import com.houjiang.merchant.vo.finance.FinanceRefundLogVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @author Amos
 */
@RestController
@Tag(name = "退款记录管理")
@RequestMapping("/merchant/finance.refund")
public class FinanceRefundController {

    @Resource
    IFinanceRefundService iFinanceRefundService;

    @GetMapping("/stat")
    @Operation(summary = "退还统计")
    public AjaxResult<Object> stat() {
        return AjaxResult.success(iFinanceRefundService.stat());
    }

    @GetMapping("/record")
    @Operation(summary = "退款记录")
    public AjaxResult<Object> list(@Validated PageValidate pageValidate,
                                   @Validated FinanceRefundSearchValidate searchValidate) {
        PageResult<FinanceRefundListVo> list = iFinanceRefundService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/log")
    @Operation(summary = "退款日志")
    public AjaxResult<Object> log(@Validated @IDMust() @RequestParam("record_id") Integer id) {
        List<FinanceRefundLogVo> list = iFinanceRefundService.log(id);
        return AjaxResult.success(list);
    }

}
