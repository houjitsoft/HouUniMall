package com.houjiang.merchant.controller.gather;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallGatherGoodsService;
import com.houjiang.merchant.service.mall.IMallShopJobService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.gather.MallGatherEditGoodsValidate;
import com.houjiang.merchant.validate.gather.MallGatherSaveGoodsValidate;
import com.houjiang.merchant.validate.mall.MallShopJobCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopJobSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopJobUpdateValidate;
import com.houjiang.merchant.vo.gather.MallGatherGoodsDetailVo;
import com.houjiang.merchant.vo.mall.MallShopJobDetailVo;
import com.houjiang.merchant.vo.mall.MallShopJobListedVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/gather.gatherGoods")
public class GatherGoodsController {
    @Resource
    IMallGatherGoodsService iMallGatherGoodsService;

    @GetMapping("/detail")
    @Operation(summary = "获取采集商品详情")
    public AjaxResult<MallGatherGoodsDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        MallGatherGoodsDetailVo vo = iMallGatherGoodsService.detail(id);
        return AjaxResult.success(vo);
    }

    @PostMapping("/edit_goods")
    @Operation(summary = "编辑采集商品")
    public AjaxResult<Object> editGoods(@Validated @RequestBody MallGatherEditGoodsValidate editGoodsValidate) {
        iMallGatherGoodsService.editGoods(editGoodsValidate);
        return AjaxResult.success();
    }

    @PostMapping("/save_goods")
    @Operation(summary = "保存商品")
    public AjaxResult<Object> saveGoods(@Validated @RequestBody MallGatherSaveGoodsValidate saveValidate) throws IOException {
        saveValidate.setShopId(MerchantThreadLocal.getShopId());
        iMallGatherGoodsService.saveGoods(saveValidate);
        return AjaxResult.success();
    }

}
