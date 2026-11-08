package com.houjiang.merchant.controller.finance;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.finance.IFinanceRechargerService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.finance.FinanceRechargeSearchValidate;
import com.houjiang.merchant.validate.finance.FinanceRefundValidate;
import com.houjiang.merchant.vo.finance.FinanceRechargeListVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.enums.ErrorEnum;
import com.houjiang.merchant.common.util.StringUtils;
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
@RequestMapping({"/merchant/recharge/recharge", "/merchant/recharge.recharge"})
public class FinanceRechargerController {

    @Resource
    IFinanceRechargerService iFinanceRechargerService;

    @GetMapping("/lists")
    @Operation(summary = "充值记录")
    public AjaxResult<Object> list(@Validated PageValidate pageValidate,
                                   @Validated FinanceRechargeSearchValidate searchValidate) {
        if (StringUtils.isNotNull(searchValidate.getExport()) && searchValidate.getExport().equals(1)) {
            JSONObject ret = iFinanceRechargerService.getExportData(pageValidate, searchValidate);
            return AjaxResult.success(ret);
        } else if (StringUtils.isNotNull(searchValidate.getExport()) && searchValidate.getExport().equals(2)) {
            String path = iFinanceRechargerService.export(searchValidate);
            return AjaxResult.success(2, new JSONObject() {{
                put("url", path);
            }}, ErrorEnum.SHOW_MSG.getCode());
        } else {
            PageResult<FinanceRechargeListVo> list = iFinanceRechargerService.list(pageValidate, searchValidate);
            return AjaxResult.success(list);
        }
    }

    @PostMapping("/refund")
    @Operation(summary = "发起退款")
    public AjaxResult<Object> refund(@Validated @RequestBody FinanceRefundValidate financeRefundValidate) {
        Integer adminId = MerchantThreadLocal.getAdminId();

        iFinanceRechargerService.refund(financeRefundValidate.getRecharge_id(), adminId);
        return AjaxResult.success();
    }

    @PostMapping("/refundAgain")
    @Schema(description = "重新退款")
    public AjaxResult<Object> refundAgain(@Validated @RequestBody IdValidate idValidate) {
        Integer adminId = MerchantThreadLocal.getAdminId();

        iFinanceRechargerService.refundAgain(idValidate.getId(), adminId);
        return AjaxResult.success();
    }

}
