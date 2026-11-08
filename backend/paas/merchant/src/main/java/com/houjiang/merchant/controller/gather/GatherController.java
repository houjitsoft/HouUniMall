package com.houjiang.merchant.controller.gather;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallGatherService;
import com.houjiang.merchant.service.mall.IMallShopJobService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.gather.MallGatherCreateValidate;
import com.houjiang.merchant.validate.gather.MallGatherSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopJobCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopJobSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopJobUpdateValidate;
import com.houjiang.merchant.vo.gather.MallGatherCreateVo;
import com.houjiang.merchant.vo.gather.MallGatherDetailVo;
import com.houjiang.merchant.vo.gather.MallGatherListedVo;
import com.houjiang.merchant.vo.mall.MallShopJobDetailVo;
import com.houjiang.merchant.vo.mall.MallShopJobListedVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/gather.gather")
public class GatherController {

    @Resource
    IMallGatherService iMallGatherService;
    @GetMapping("/lists")
    @Operation(summary = "采集控制列表")
    public AjaxResult<PageResult<MallGatherListedVo>> lists(@Validated PageValidate pageValidate,
                                                        @Validated MallGatherSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<MallGatherListedVo> list = iMallGatherService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取采集控制详情")
    public AjaxResult<MallGatherDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        MallGatherDetailVo vo = iMallGatherService.detail(id);
        return AjaxResult.success(vo);
    }

    @PostMapping("/add")
    @Operation(summary = "采集控制新增")
    public AjaxResult<MallGatherCreateVo> add(@Validated @RequestBody MallGatherCreateValidate createValidate) {
        createValidate.setShopId(MerchantThreadLocal.getShopId());
        MallGatherCreateVo vo = iMallGatherService.add(createValidate);
        return AjaxResult.success(vo);
    }

    @PostMapping("/delete")
    @Operation(summary = "采集控制删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iMallGatherService.del(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/deal")
    @Operation(summary = "执行")
    public AjaxResult<Object> deal(@Validated @RequestBody IdValidate idValidate) {
        iMallGatherService.deal(idValidate.getId());
        return AjaxResult.success("采集完成");
    }

}
