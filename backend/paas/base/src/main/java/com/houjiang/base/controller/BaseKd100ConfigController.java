package com.houjiang.base.controller;

import com.houjiang.base.service.BaseKd100ConfigService;
import com.houjiang.commons.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 快递100配置Controller
 * @author Amos
 */
@RestController
@RequestMapping("base/kd100Config")
@RequiredArgsConstructor
public class BaseKd100ConfigController {

    private final BaseKd100ConfigService baseKd100ConfigService;

    /**
     * 查询快递100配置
     */
    @PostMapping("/queryConfig")
    public ResponseResult queryConfig(@RequestBody Map<String, Object> param) {
        return baseKd100ConfigService.queryConfig(param);
    }

    /**
     * 更新快递100配置
     */
    @PostMapping("/updateConfig")
    public ResponseResult updateConfig(@RequestBody Map<String, Object> param) {
        return baseKd100ConfigService.updateConfig(param);
    }
}
