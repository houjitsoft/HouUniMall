package com.houjiang.logging.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.logging.entity.LogSettingEntity;
import com.houjiang.logging.service.LogSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 日志设置 Controller
 *
 * <p>提供日志模块配置项的查询与更新接口。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Slf4j
@RestController
@RequestMapping("/logging/setting")
@RequiredArgsConstructor
@Tag(name = "日志设置", description = "日志模块配置项管理接口")
public class LogSettingController {

    private final LogSettingService settingService;

    /**
     * 获取所有日志配置项
     *
     * @return 配置项列表
     */
    @Operation(summary = "获取所有日志配置项")
    @PostMapping("/list")
    public ResponseResult<List<LogSettingEntity>> list() {
        return ResponseResult.success(settingService.getAllSettings());
    }

    /**
     * 更新日志配置项
     *
     * <p>接收包含 key 和 value 的 Map 请求体，更新对应的配置项值。</p>
     *
     * @param params 请求体，必须包含 key（配置键名）和 value（新配置值）
     * @return 操作结果
     */
    @Operation(summary = "更新日志配置项")
    @PostMapping("/update")
    public ResponseResult<String> update(@RequestBody Map<String, String> params) {
        String key = params.get("key");
        String value = params.get("value");
        if (key == null || key.isBlank()) {
            return ResponseResult.fail("配置键名 key 不能为空");
        }
        boolean result = settingService.updateSetting(key, value);
        return result ? ResponseResult.success("更新成功") : ResponseResult.fail("更新失败，配置项不存在");
    }
}
