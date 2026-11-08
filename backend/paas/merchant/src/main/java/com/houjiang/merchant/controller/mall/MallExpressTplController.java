package com.houjiang.merchant.controller.mall;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallExpressTplService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallExpressTplCreateValidate;
import com.houjiang.merchant.validate.mall.MallExpressTplSearchValidate;
import com.houjiang.merchant.validate.mall.MallExpressTplUpdateValidate;
import com.houjiang.merchant.vo.mall.MallExpressTplDetailVo;
import com.houjiang.merchant.vo.mall.MallExpressTplListedVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/mall.expressTpl")
public class MallExpressTplController {

    @Resource
    IMallExpressTplService iMallExpressTplService;

    @GetMapping("/lists")
    @Operation(summary="运费模板列表")
    public AjaxResult<PageResult<MallExpressTplListedVo>> list(@Validated PageValidate pageValidate, @Validated MallExpressTplSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<MallExpressTplListedVo> list = iMallExpressTplService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary="运费模板详情")
    public AjaxResult<MallExpressTplDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        MallExpressTplDetailVo detail = iMallExpressTplService.detail(id);
        return AjaxResult.success(detail);
    }

    @PostMapping("/add")
    @Operation(summary="运费模板新增")
    public AjaxResult<Object> add(@Validated @RequestBody MallExpressTplCreateValidate createValidate) {
        createValidate.setShopId(MerchantThreadLocal.getShopId());
        iMallExpressTplService.add(createValidate);
        return AjaxResult.success();
    }

    @PostMapping("/edit")
    @Operation(summary="运费模板编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody MallExpressTplUpdateValidate updateValidate) {
        iMallExpressTplService.edit(updateValidate);
        return AjaxResult.success();
    }

    @PostMapping("/delete")
    @Operation(summary="运费模板删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iMallExpressTplService.del(idValidate.getId());
        return AjaxResult.success();
    }
}
