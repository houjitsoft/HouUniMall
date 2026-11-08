package com.houjiang.merchant.controller.pt;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.ms.IMsService;
import com.houjiang.merchant.service.pt.IPtService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.ms.MsCreateValidate;
import com.houjiang.merchant.validate.ms.MsSearchValidate;
import com.houjiang.merchant.validate.ms.MsUpdateValidate;
import com.houjiang.merchant.validate.pt.PtCreateValidate;
import com.houjiang.merchant.validate.pt.PtSearchValidate;
import com.houjiang.merchant.validate.pt.PtUpdateValidate;
import com.houjiang.merchant.vo.ms.MsDetailVo;
import com.houjiang.merchant.vo.ms.MsListedVo;
import com.houjiang.merchant.vo.pt.PtDetailVo;
import com.houjiang.merchant.vo.pt.PtListedVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/mall.pt")
public class PtController {
    @Resource
    IPtService iPtService;
    @GetMapping("/lists")
    @Operation(summary = "秒杀列表")
    public AjaxResult<PageResult<PtListedVo>> lists(@Validated PageValidate pageValidate,
                                                    @Validated PtSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<PtListedVo> lists = iPtService.list(pageValidate, searchValidate);
        return AjaxResult.success(lists);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取拼团详情")
    public AjaxResult<PtDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        PtDetailVo vo = iPtService.detail(id);
        return AjaxResult.success(vo);
    }

    @PostMapping("/delete")
    @Operation(summary = "拼团删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iPtService.del(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/close")
    @Operation(summary = "拼团关闭")
    public AjaxResult<Object> close(@Validated @RequestBody IdValidate idValidate) {
        iPtService.close(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/submit")
    @Operation(summary = "拼团提审")
    public AjaxResult<Object> submit(@Validated @RequestBody IdValidate idValidate) {
        iPtService.submit(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/open")
    @Operation(summary = "拼团开始")
    public AjaxResult<Object> open(@Validated @RequestBody IdValidate idValidate) {
        iPtService.open(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/add")
    @Operation(summary = "添加秒杀")
    public AjaxResult<Object> add(@Validated @RequestBody PtCreateValidate createValidate) {
        createValidate.setShopId(MerchantThreadLocal.getShopId());
        iPtService.add(createValidate);
        return AjaxResult.success();
    }

    @PostMapping("/edit")
    @Operation(summary = "编辑秒杀")
    public AjaxResult<Object> edit(@Validated @RequestBody PtUpdateValidate updateValidate) {
        updateValidate.setShopId(MerchantThreadLocal.getShopId());
        iPtService.edit(updateValidate);
        return AjaxResult.success();
    }
}
