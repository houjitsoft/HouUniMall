package com.houjiang.plugin.api.host;

import java.util.List;
import java.util.Optional;

/**
 * 插件宿主能力发现契约（原 core-api，已并入 plugin-api）。
 *
 * <p>这是总判断第 7 条「可选能力发现 + 降级」的落地接口：
 * Core 需要调用插件能力时，<b>必须</b>经本接口按 {@code pluginCode} 发现，
 * 插件未安装/未启用/无该能力时返回空，由调用方走降级分支；
 * <b>禁止</b>用 {@code @FeignClient} 或硬编码类名直接依赖插件实现类。
 *
 * <p>典型反例（须改造，见 §1.4.4）：{@code auth} 直接 {@code @FeignClient("webpos")} 调收银台登录，
 * 导致 webpos 做成「未安装即不存在」的收费插件后会打破开源版独立运行。
 * 正确写法：{@code pluginHost.capability("webpos", WebPosLoginCapability.class).ifPresent(...)}，
 * 缺失时按「非收银台用户」放行。
 *
 * <p>由 {@code plugin-engine} + {@code mall} 宿主提供实现。
 */
public interface PluginHostApi {

    /** 插件是否已安装且处于启用（ACTIVE）状态。 */
    boolean isActive(String pluginCode);

    /**
     * 查询插件能力描述。
     *
     * <p>插件<b>未安装</b>返回空；已安装但停用时仍返回描述（{@code active=false}），
     * 调用方据此走降级。是否可安全调用由 {@link PluginCapability#callable()} 判定。
     */
    Optional<PluginCapability> discover(String pluginCode);

    /**
     * 按能力类型发现插件暴露的能力实现。
     *
     * @param pluginCode     插件编码，如 {@code erp}、{@code webpos}
     * @param capabilityType 能力接口类型（须由插件与本模块共同约定的稳定接口）
     * @return 能力实现；插件未安装/未启用/未暴露该能力时返回空
     */
    <T> Optional<T> capability(String pluginCode, Class<T> capabilityType);

    /** 当前站点全部已启用插件的能力描述；无插件返回空列表（非 null）。 */
    List<PluginCapability> activeCapabilities();
}
