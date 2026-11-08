package com.houjiang.bffweb.controller.plugincenter;

import com.houjiang.bffweb.feign.PluginCenterServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 插件中心（Cloud 后台 /mall/addons）。
 *
 * <p>本控制器只做转发，不承担业务逻辑：全部请求经 Feign 打到 base 服务
 * 的 {@code /base/plugin-center/**}（端口 8220）。鉴权不在 BFF 进行，
 * Token 由 FeignRequestInterceptor 透传，JWT 校验在 base 服务侧完成。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/web/plugin-center")
@Tag(name = "插件中心", description = "平台插件的安装、启停、卸载与配置")
public class PluginCenterController {

    private final PluginCenterServiceFeign pluginCenterServiceFeign;

    @PostMapping("/list")
    @Operation(summary = "插件列表（含可选分类）")
    public ResponseResult list(@RequestBody(required = false) Map<String, Object> params) {
        return pluginCenterServiceFeign.pluginCenterList(params == null ? Map.of() : params);
    }

    @PostMapping("/categories")
    @Operation(summary = "应用分类字典")
    public ResponseResult categories() {
        return pluginCenterServiceFeign.pluginCenterCategories();
    }

    @PostMapping("/summary")
    @Operation(summary = "插件概览统计")
    public ResponseResult summary() {
        return pluginCenterServiceFeign.pluginCenterSummary();
    }

    @PostMapping("/detail")
    @Operation(summary = "插件详情")
    public ResponseResult detail(@RequestBody Map<String, Object> params) {
        return pluginCenterServiceFeign.pluginCenterDetail(params == null ? Map.of() : params);
    }

    @PostMapping("/install")
    @Operation(summary = "安装插件")
    public ResponseResult install(@RequestBody Map<String, Object> params) {
        return pluginCenterServiceFeign.pluginCenterInstall(params == null ? Map.of() : params);
    }

    @PostMapping("/uninstall")
    @Operation(summary = "卸载插件")
    public ResponseResult uninstall(@RequestBody Map<String, Object> params) {
        return pluginCenterServiceFeign.pluginCenterUninstall(params == null ? Map.of() : params);
    }

    @PostMapping("/toggle")
    @Operation(summary = "启用 / 停用插件")
    public ResponseResult toggle(@RequestBody Map<String, Object> params) {
        return pluginCenterServiceFeign.pluginCenterToggle(params == null ? Map.of() : params);
    }

    @PostMapping("/config/save")
    @Operation(summary = "保存插件配置")
    public ResponseResult saveConfig(@RequestBody Map<String, Object> params) {
        return pluginCenterServiceFeign.pluginCenterSaveConfig(params == null ? Map.of() : params);
    }

    @PostMapping("/create")
    @Operation(summary = "创建插件：真实生成 legacy/architecture-before-2026-09-20/plugin-services/{code}-service 骨架并写目录/市场库")
    public ResponseResult create(@RequestBody Map<String, Object> params) {
        return pluginCenterServiceFeign.pluginCenterCreate(params == null ? Map.of() : params);
    }
}
