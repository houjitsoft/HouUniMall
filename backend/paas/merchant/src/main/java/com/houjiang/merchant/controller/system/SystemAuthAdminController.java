package com.houjiang.merchant.controller.system;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.admin.IAdminService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.system.SystemAdminCreateValidate;
import com.houjiang.merchant.validate.system.SystemAdminSearchValidate;
import com.houjiang.merchant.validate.system.SystemAdminUpInfoValidate;
import com.houjiang.merchant.validate.system.SystemAdminUpdateValidate;
import com.houjiang.merchant.vo.system.SystemAuthAdminDetailVo;
import com.houjiang.merchant.vo.system.SystemAuthAdminListedVo;
import com.houjiang.merchant.vo.system.SystemAuthAdminSelvesVo;
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
@RequestMapping("/adminapi/auth.admin")
public class SystemAuthAdminController {

    @Resource
    IAdminService iSystemAuthAdminService;

    @GetMapping("/lists")
    @Operation(summary="管理员列表")
    public AjaxResult<Object> list(@Validated PageValidate pageValidate,
                                                                @Validated SystemAdminSearchValidate searchValidate) {
        if (StringUtils.isNotNull(searchValidate.getExport()) && searchValidate.getExport().equals(1)) {
            JSONObject ret = iSystemAuthAdminService.getExportData(pageValidate, searchValidate);
            return AjaxResult.success(ret);
        } else if (StringUtils.isNotNull(searchValidate.getExport()) && searchValidate.getExport().equals(2)) {
            String path = iSystemAuthAdminService.export(searchValidate);
            return AjaxResult.success(2, new JSONObject() {{
                put("url", path);
            }}, ErrorEnum.SHOW_MSG.getCode());
        } else {
            PageResult<SystemAuthAdminListedVo> list = iSystemAuthAdminService.list(pageValidate, searchValidate);
            return AjaxResult.success(list);
        }
    }

    @GetMapping("/self")
    @Operation(summary="管理员信息")
    public AjaxResult<SystemAuthAdminSelvesVo> self() {
        Integer adminId = MerchantThreadLocal.getAdminId();
        SystemAuthAdminSelvesVo vo = iSystemAuthAdminService.self(adminId);
        return AjaxResult.success(vo);
    }

    @GetMapping("/detail")
    @Operation(summary="管理员详情")
    public AjaxResult<SystemAuthAdminDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        SystemAuthAdminDetailVo vo = iSystemAuthAdminService.detail(id);
        return AjaxResult.success(vo);
    }

    @Log(title = "管理员新增")
    @PostMapping("/add")
    @Operation(summary="管理员新增")
    public AjaxResult<Object> add(@Validated @RequestBody SystemAdminCreateValidate createValidate) {
        iSystemAuthAdminService.add(createValidate);
        return AjaxResult.success();
    }

    @Log(title = "管理员编辑")
    @PostMapping("/edit")
    @Operation(summary="管理员编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody SystemAdminUpdateValidate updateValidate) {
        Integer adminId = MerchantThreadLocal.getAdminId();
        iSystemAuthAdminService.edit(updateValidate, adminId);
        return AjaxResult.success();
    }

    @Log(title = "管理员更新")
    @PostMapping("/upInfo")
    @Operation(summary="当前管理员更新")
    public AjaxResult<Object> upInfo(@Validated @RequestBody SystemAdminUpInfoValidate upInfoValidate) {
        Integer adminId = MerchantThreadLocal.getAdminId();
        iSystemAuthAdminService.upInfo(upInfoValidate, adminId);
        return AjaxResult.success();
    }

    @Log(title = "管理员删除")
    @PostMapping("/delete")
    @Operation(summary="管理员删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        Integer adminId = MerchantThreadLocal.getAdminId();
        iSystemAuthAdminService.del(idValidate.getId(), adminId);
        return AjaxResult.success();
    }

    @Log(title = "管理员状态")
    @PostMapping("/disable")
    @Operation(summary="管理员状态切换")
    public AjaxResult<Object> disable(@Validated @RequestBody IdValidate idValidate) {
        Integer adminId = MerchantThreadLocal.getAdminId();
        iSystemAuthAdminService.disable(idValidate.getId(), adminId);
        return AjaxResult.success();
    }

}
