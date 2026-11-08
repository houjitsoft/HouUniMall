package com.houjiang.merchant.controller.pt;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.pt.PtOpen;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.ms.IMsService;
import com.houjiang.merchant.service.pt.IPtOpenService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.ms.MsSearchValidate;
import com.houjiang.merchant.validate.pt.PtOpenSearchValidate;
import com.houjiang.merchant.vo.ms.MsDetailVo;
import com.houjiang.merchant.vo.ms.MsListedVo;
import com.houjiang.merchant.vo.pt.PtOpenListedVo;
import com.houjiang.merchant.vo.pt.PtOpenVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/mall.ptOpen")
public class PtOpenController {

    @Resource
    IPtOpenService iPtOpenService;

    @GetMapping("/lists")
    @Operation(summary = "拼团列表")
    public AjaxResult<PageResult<PtOpenListedVo>> lists(@Validated PageValidate pageValidate,
                                                        @Validated PtOpenSearchValidate msSearchValidate) {
        msSearchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<PtOpenListedVo> lists = iPtOpenService.list(pageValidate, msSearchValidate);
        return AjaxResult.success(lists);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取拼团详情")
    public AjaxResult<PtOpenVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        PtOpenVo vo = iPtOpenService.detail(id);
        return AjaxResult.success(vo);
    }

    @PostMapping("/close")
    @Operation(summary = "拼团关闭")
    public AjaxResult<Object> close(@Validated @RequestBody IdValidate idValidate) {
        iPtOpenService.close(idValidate.getId());
        return AjaxResult.success();
    }
}
