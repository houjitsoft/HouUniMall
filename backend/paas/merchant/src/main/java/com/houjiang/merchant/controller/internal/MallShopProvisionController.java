package com.houjiang.merchant.controller.internal;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.service.impl.mall.MallShopProvisionService;
import com.houjiang.merchant.validate.mall.MallShopProvisionCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopProvisionUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopProvisionListedVo;
import com.houjiang.merchant.vo.mall.MallShopProvisionResultVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 仅供 BFF 调用的平台商家开通接口。 */
@RestController
@RequestMapping("/merchant/internal/shop-provision")
@RequiredArgsConstructor
public class MallShopProvisionController {

    private final MallShopProvisionService provisionService;

    @PostMapping("/create")
    public AjaxResult<MallShopProvisionResultVo> create(
            @Valid @RequestBody MallShopProvisionCreateValidate request) {
        return AjaxResult.success("商家开通成功", provisionService.create(request));
    }

    @PostMapping("/list")
    public AjaxResult<?> list(@RequestBody(required = false) Map<String, Object> params) {
        return AjaxResult.success(provisionService.list(params == null ? Map.of() : params));
    }

    @PostMapping("/detail")
    public AjaxResult<MallShopProvisionListedVo> detail(@RequestBody Map<String, Object> params) {
        Integer id = params.get("id") == null ? null : Integer.valueOf(params.get("id").toString());
        return AjaxResult.success(provisionService.detail(id));
    }

    @PostMapping("/options")
    public AjaxResult<List<Map<String, Object>>> options() {
        return AjaxResult.success(provisionService.options());
    }

    @PostMapping("/account-options")
    public AjaxResult<List<Map<String, Object>>> accountOptions(
            @RequestBody(required = false) Map<String, Object> params) {
        return AjaxResult.success(provisionService.accountOptions(params == null ? Map.of() : params));
    }

    @PostMapping("/status")
    public AjaxResult<Object> status(@RequestBody Map<String, Object> params) {
        Integer id = params.get("id") == null ? null : Integer.valueOf(params.get("id").toString());
        Integer status = params.get("status") == null ? null : Integer.valueOf(params.get("status").toString());
        provisionService.updateStatus(id, status);
        return AjaxResult.success("商家状态已更新");
    }

    @PostMapping("/update")
    public AjaxResult<Object> update(@Valid @RequestBody MallShopProvisionUpdateValidate request) {
        provisionService.update(request);
        return AjaxResult.success("店铺资料已更新");
    }
}
