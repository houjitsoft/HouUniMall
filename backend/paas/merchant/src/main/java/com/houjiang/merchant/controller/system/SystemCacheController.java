package com.houjiang.merchant.controller.system;


import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.system.ISystemCacheService;
import com.houjiang.merchant.common.core.AjaxResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("/adminapi/setting.system.cache")
public class SystemCacheController {

    @Resource
    ISystemCacheService iSystemCacheService;

    @Log(title = "清除系统缓存")
    @PostMapping("/clear")
    @Operation(summary = "清除系统缓存")
    public AjaxResult<Object> clear() {
        iSystemCacheService.clear();
        return AjaxResult.success();
    }


}
