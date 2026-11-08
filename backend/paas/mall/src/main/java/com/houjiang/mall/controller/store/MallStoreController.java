package com.houjiang.mall.controller.store;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.store.MallStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 平台门店管理；门店始终隶属于一个商家。 */
@RestController
@RequestMapping("/mall/store")
@RequiredArgsConstructor
public class MallStoreController {

    private final MallStoreService mallStoreService;

    @PostMapping("/list")
    public ResponseResult list(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(mallStoreService.page(params == null ? Map.of() : params));
    }

    @PostMapping("/merchant-options")
    public ResponseResult merchantOptions() {
        return ResponseResult.success(mallStoreService.merchantOptions());
    }

    /** 新增门店时可授权的已有统一账号。 */
    @PostMapping("/account-options")
    public ResponseResult accountOptions(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(mallStoreService.accountOptions(params == null ? Map.of() : params));
    }

    @PostMapping("/create")
    public ResponseResult create(@RequestBody Map<String, Object> params) {
        try {
            return ResponseResult.success(mallStoreService.create(params));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(e.getMessage());
        }
    }

    @PostMapping("/toggle")
    public ResponseResult toggle(@RequestBody Map<String, Object> params) {
        try {
            return ResponseResult.success(mallStoreService.updateToggle(params));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(e.getMessage());
        }
    }
}
