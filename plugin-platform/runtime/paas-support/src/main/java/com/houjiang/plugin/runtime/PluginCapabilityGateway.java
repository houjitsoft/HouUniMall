package com.houjiang.plugin.runtime;

import com.houjiang.plugin.api.host.PluginHostApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 插件能力发现与降级网关（最终版 §3.2）。
 *
 * <p>Core 调用插件能力的唯一推荐入口。契约本身由 {@link PluginHostApi} 定义，本类在其之上补两件事：
 * <ol>
 *     <li><b>统一降级</b>：插件未安装/未启用/未暴露能力时，返回空或调用方给定的兜底值，
 *         而不是抛异常向上冒泡。定稿 §3.2 要求「不因某个收费插件未安装而阻断登录、
 *         商品浏览和基础下单」，调用方不再各自写 {@code if (isActive)} 判断。</li>
 *     <li><b>可观测</b>：每次未命中都按 WARN 记录插件码与能力类型，便于线上区分
 *         「插件确实没装」与「插件装了但能力没注册」。</li>
 * </ol>
 *
 * <p><b>不替代 Feign 的领域服务调用</b>：Core 调 Core（如 order → goods）仍走
 * {@code @FeignClient}（§3.1）；本网关只用于 Core 调<b>插件</b>能力（§3.2）。
 *
 * @author HouCloud
 */
public class PluginCapabilityGateway {

    private static final Logger log = LoggerFactory.getLogger(PluginCapabilityGateway.class);

    private final PluginHostApi pluginHostApi;

    public PluginCapabilityGateway(PluginHostApi pluginHostApi) {
        this.pluginHostApi = Objects.requireNonNull(pluginHostApi, "pluginHostApi");
    }

    /** 插件是否已安装且处于启用状态。 */
    public boolean isActive(String pluginCode) {
        return pluginHostApi.isActive(pluginCode);
    }

    /**
     * 发现插件能力；未安装、未启用或未暴露该能力时返回空。
     *
     * @param pluginCode     插件编码，如 {@code erp}、{@code activity}
     * @param capabilityType 能力接口类型
     */
    public <T> Optional<T> capability(String pluginCode, Class<T> capabilityType) {
        Optional<T> found = pluginHostApi.capability(pluginCode, capabilityType);
        if (found.isEmpty()) {
            log.warn("[plugin-runtime] 能力未命中：plugin={}, capability={}（插件未安装/未启用或未暴露该能力，走降级）",
                    pluginCode, capabilityType.getName());
        }
        return found;
    }

    /**
     * 发现插件能力，缺失时返回兜底值。
     *
     * <p>用于「有插件用插件、没插件用默认行为」的场景，例如营销插件未安装时按原价成交。
     */
    public <T> T capabilityOrElse(String pluginCode, Class<T> capabilityType, T fallback) {
        return capability(pluginCode, capabilityType).orElse(fallback);
    }

    /**
     * 发现插件能力，缺失时用 {@code fallback} 惰性兜底。
     */
    public <T> T capabilityOrGet(String pluginCode, Class<T> capabilityType, Supplier<? extends T> fallback) {
        return capability(pluginCode, capabilityType).orElseGet(fallback);
    }

    /**
     * 插件可用时执行 {@code action}，否则执行 {@code onAbsent}。
     *
     * <p>用于「插件在时要产生副作用（如扣券）、不在时要跳过」的场景，避免把副作用写进默认值。
     */
    public <T> void ifPresent(String pluginCode, Class<T> capabilityType,
                              java.util.function.Consumer<T> action,
                              Runnable onAbsent) {
        Optional<T> found = capability(pluginCode, capabilityType);
        if (found.isPresent()) {
            action.accept(found.get());
        } else {
            onAbsent.run();
        }
    }
}
