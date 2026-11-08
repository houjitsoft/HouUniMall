package com.houjiang.merchant.controller.ms;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.ms.IMsService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.ms.MsCreateValidate;
import com.houjiang.merchant.validate.ms.MsOpenValidate;
import com.houjiang.merchant.validate.ms.MsSearchValidate;
import com.houjiang.merchant.validate.ms.MsUpdateValidate;
import com.houjiang.merchant.vo.ms.MsDetailVo;
import com.houjiang.merchant.vo.ms.MsListedVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/mall.ms")
public class MsController {

    @Resource
    IMsService iMsService;
    @GetMapping("/lists")
    @Operation(summary = "秒杀列表")
    public AjaxResult<PageResult<MsListedVo>> lists(@Validated PageValidate pageValidate,
                                                       @Validated MsSearchValidate msSearchValidate) {
        msSearchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<MsListedVo> lists = iMsService.list(pageValidate, msSearchValidate);
        return AjaxResult.success(lists);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取秒杀详情")
    public AjaxResult<MsDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        MsDetailVo vo = iMsService.detail(id);
        return AjaxResult.success(vo);
    }

    @PostMapping("/delete")
    @Operation(summary = "秒杀删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iMsService.del(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/close")
    @Operation(summary = "秒杀关闭")
    public AjaxResult<Object> close(@Validated @RequestBody IdValidate idValidate) {
        iMsService.close(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/submit")
    @Operation(summary = "秒杀提审")
    public AjaxResult<Object> submit(@Validated @RequestBody IdValidate idValidate) {
        iMsService.submit(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/add")
    @Operation(summary = "添加秒杀")
    public AjaxResult<Object> add(@Validated @RequestBody MsCreateValidate msCreateValidate) {
        msCreateValidate.setShopId(MerchantThreadLocal.getShopId());
        iMsService.add(msCreateValidate);
        return AjaxResult.success();
    }

    @PostMapping("/edit")
    @Operation(summary = "编辑秒杀")
    public AjaxResult<Object> edit(@Validated @RequestBody MsUpdateValidate msUpdateValidate) {
        msUpdateValidate.setShopId(MerchantThreadLocal.getShopId());
        iMsService.edit(msUpdateValidate);
        return AjaxResult.success();
    }

    @PostMapping("/open")
    @Operation(summary = "开始活动")
    public AjaxResult<Object> open(@Validated @RequestBody MsOpenValidate validate) {
        validate.setShopId(MerchantThreadLocal.getShopId());
        iMsService.open(validate);
        return AjaxResult.success();
    }

}
