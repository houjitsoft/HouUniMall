package com.houjiang.merchant.controller.internal;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.service.AdvertisingMenuLifecycleService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

/** Trusted plugin lifecycle callbacks; guarded by the narrow internal-token filter. */
@RestController
@RequestMapping("/merchant/internal/advertising/menu")
@PreAuthorize("hasRole('ADVERTISING_LIFECYCLE')")
public class AdvertisingMenuLifecycleController {

    private final AdvertisingMenuLifecycleService lifecycle;

    public AdvertisingMenuLifecycleController(AdvertisingMenuLifecycleService lifecycle) {
        this.lifecycle = lifecycle;
    }

    @PostMapping("/sync")
    public AjaxResult<Map<String, Integer>> sync() {
        return AjaxResult.success(Map.of("menuId", lifecycle.sync()));
    }

    @PostMapping("/disable")
    public AjaxResult<Object> disable() {
        lifecycle.disable();
        return AjaxResult.success();
    }

    @PostMapping("/remove")
    public AjaxResult<Object> remove() {
        lifecycle.remove();
        return AjaxResult.success();
    }
}
