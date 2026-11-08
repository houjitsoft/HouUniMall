package com.houjiang.merchant.controller;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.user.IUserService;
import com.houjiang.merchant.validate.user.UserSearchValidate;
import com.houjiang.merchant.validate.user.UserUpdateValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.user.UserWalletValidate;
import com.houjiang.merchant.vo.user.UserVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.enums.ErrorEnum;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping({"merchant/user"})
public class UserController {

    @Resource
    IUserService iUserService;

    @GetMapping("/lists")
    @Operation(summary="用户列表")
    public AjaxResult<Object> list(@Validated PageValidate pageValidate,
                                               @Validated UserSearchValidate searchValidate) {
        if (StringUtils.isNotNull(searchValidate.getExport()) && searchValidate.getExport().equals(1)) {
            JSONObject ret = iUserService.getExportData(pageValidate, searchValidate);
            return AjaxResult.success(ret);
        } else if (StringUtils.isNotNull(searchValidate.getExport()) && searchValidate.getExport().equals(2)) {
            String path = iUserService.export(searchValidate);
            return AjaxResult.success(2, new JSONObject() {{
                put("url", path);
            }}, ErrorEnum.SHOW_MSG.getCode());
        } else {
            PageResult<UserVo> list = iUserService.list(pageValidate, searchValidate);
            return AjaxResult.success(list);
        }
    }

    @GetMapping("/detail")
    @Operation(summary="用户详情")
    public AjaxResult<UserVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        UserVo vo = iUserService.detail(id);
        return AjaxResult.success(vo);
    }

    @Log(title = "用户编辑")
    @PostMapping("/edit")
    @Operation(summary="用户编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody UserUpdateValidate updateValidate) {
        iUserService.edit(updateValidate);
        return AjaxResult.success();
    }

    @Log(title = "余额调整")
    @PostMapping("/adjustMoney")
    @Operation(summary="余额调整")
    public AjaxResult<Object> adjustWallet(@Validated @RequestBody UserWalletValidate walletValidate) {
        iUserService.adjustWallet(walletValidate);
        return AjaxResult.success();
    }

}
