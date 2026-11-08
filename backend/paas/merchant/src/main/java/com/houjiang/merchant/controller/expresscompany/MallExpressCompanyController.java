package com.houjiang.merchant.controller.expresscompany;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.service.mall.IMallExpressCompanyService;
import com.houjiang.merchant.vo.mall.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @author Amos
 */
@RestController
@RequestMapping(value = {"merchant/mall.expressCompany"})
public class MallExpressCompanyController {
    @Resource
    IMallExpressCompanyService iMallExpressCompanyService;
    @GetMapping("/lists")
    @Operation(summary="商品列表")
    public AjaxResult<List<MallExpressCompanyListedVo>> list() {
        List<MallExpressCompanyListedVo> list = iMallExpressCompanyService.getLists();
        return AjaxResult.success(list);
    }


}
