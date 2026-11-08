package com.houjiang.merchant.controller.system;

import com.houjiang.merchant.service.system.ISystemLogsServer;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.system.SystemSearchLoginsValidate;
import com.houjiang.merchant.validate.system.SystemSearchOperateValidate;
import com.houjiang.merchant.vo.system.SystemLogLoginVo;
import com.houjiang.merchant.vo.system.SystemLogOperateVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@Tag(name = "系统日志管理")
@RequestMapping("/adminapi/setting.system.log")
public class SystemLogsController {

    @Resource
    ISystemLogsServer iSystemLogsServer;

    @GetMapping("/lists")
    @Operation(summary="系统操作日志")
    public AjaxResult<PageResult<SystemLogOperateVo>> lists(@Validated PageValidate pageValidate,
                                                            @Validated SystemSearchOperateValidate searchValidate) {
        PageResult<SystemLogOperateVo> list = iSystemLogsServer.operate(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/login")
    @Operation(summary="系统登录日志")
    public AjaxResult<PageResult<SystemLogLoginVo>> login(@Validated PageValidate pageValidate,
                                                          @Validated SystemSearchLoginsValidate searchValidate) {
        PageResult<SystemLogLoginVo> list = iSystemLogsServer.login(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

}
