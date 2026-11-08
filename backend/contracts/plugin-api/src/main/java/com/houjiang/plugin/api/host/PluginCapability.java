package com.houjiang.plugin.api.host;

import java.util.List;
import java.util.Objects;

/**
 * 插件能力描述模型（不可变）。
 *
 * <p>用于 {@code PluginHostApi} 的「可选能力发现」：Core 先查插件是否 ACTIVE，
 * 再决定是否调用其能力，插件缺失时走降级（总判断第 7 条）。
 *
 * @param pluginCode    插件编码，如 {@code erp}、{@code webpos}（非空）
 * @param version       已安装版本（非空）
 * @param active        是否启用（ACTIVE）
 * @param licenseValid  License 是否有效（可空：未校验时为空）
 * @param capabilities  该插件暴露的能力接口全限定名（非空、不含 null）
 */
public record PluginCapability(
        String pluginCode,
        String version,
        boolean active,
        Boolean licenseValid,
        List<String> capabilities
) {
    public PluginCapability {
        Objects.requireNonNull(pluginCode, "pluginCode 不能为空");
        Objects.requireNonNull(version, "version 不能为空");
        capabilities = capabilities == null ? List.of() : List.copyOf(capabilities);
    }

    /** 是否可安全调用：已启用且（未校验 License 或 License 有效）。 */
    public boolean callable() {
        return active && (licenseValid == null || licenseValid);
    }
}
