package com.houjiang.plugin.engine.host;

import com.houjiang.plugin.api.host.PluginHostApi;
import com.houjiang.plugin.api.host.PluginCapability;
import com.houjiang.plugin.engine.core.PluginState;
import com.houjiang.plugin.engine.registry.PluginRegistry;
import com.houjiang.plugin.sdk.model.PluginManifest;
import com.houjiang.plugin.sdk.model.PluginServiceOwnership;
import com.houjiang.plugin.engine.spi.PluginServiceEndpointResolver;
import org.pf4j.DefaultPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@link PluginHostApi} 的默认实现：可选能力发现 + 降级。
 *
 * <p>这是总判断第 7 条与 §1.4.4 的落地：Core 调插件能力必须按 {@code pluginCode} 走本实现发现，
 * 插件未安装 / 未启用 / 未暴露该能力 / 反射失败时一律返回 {@link Optional#empty()}，
 * 由调用方走降级分支；<b>任何情况下都不抛异常</b>——降级路径抛异常等于把「插件缺失」变成「Core 不可用」，
 * 正是闭坑清单 P0「Core 失去独立运行」要防的事故。
 *
 * <p>典型用法（替代 {@code auth} 里硬编码的 {@code @FeignClient("webpos")}）：
 * <pre>{@code
 * pluginHost.capability("webpos", WebPosLoginCapability.class)
 *          .ifPresentOrElse(c -> c.login(req), () -> fallbackAsNormalUser(req));
 * }</pre>
 *
 * @author HouCloud
 */
public class DefaultPluginHostApi implements PluginHostApi {

    private static final Logger log = LoggerFactory.getLogger(DefaultPluginHostApi.class);

    private final PluginRegistry registry;
    private final DefaultPluginManager pluginManager;
    private final StandaloneCapabilityClient standaloneClient;
    private final PluginServiceEndpointResolver endpointResolver;

    public DefaultPluginHostApi(PluginRegistry registry, DefaultPluginManager pluginManager) {
        this(registry, pluginManager, new StandaloneCapabilityClient());
    }

    public DefaultPluginHostApi(PluginRegistry registry,
                                DefaultPluginManager pluginManager,
                                StandaloneCapabilityClient standaloneClient) {
        this(registry, pluginManager, standaloneClient, service -> null);
    }

    public DefaultPluginHostApi(PluginRegistry registry,
                                DefaultPluginManager pluginManager,
                                StandaloneCapabilityClient standaloneClient,
                                PluginServiceEndpointResolver endpointResolver) {
        this.registry = registry;
        this.pluginManager = pluginManager;
        this.standaloneClient = standaloneClient;
        this.endpointResolver = endpointResolver;
    }

    @Override
    public boolean isActive(String pluginCode) {
        if (pluginCode == null || pluginCode.isBlank()) {
            return false;
        }
        return registry.isActive(pluginCode);
    }

    @Override
    public Optional<PluginCapability> discover(String pluginCode) {
        if (pluginCode == null || pluginCode.isBlank()) {
            return Optional.empty();
        }
        PluginManifest manifest = registry.getManifest(pluginCode);
        PluginState state = registry.getState(pluginCode);
        if (manifest == null && state == null) {
            return Optional.empty();
        }
        return Optional.of(toCapability(pluginCode, manifest, state));
    }

    @Override
    public <T> Optional<T> capability(String pluginCode, Class<T> capabilityType) {
        if (pluginCode == null || pluginCode.isBlank() || capabilityType == null) {
            return Optional.empty();
        }
        if (!isActive(pluginCode)) {
            return Optional.empty();
        }
        // standalone 插件不在本进程，无法通过 PF4J 取实现类，改为跨进程能力代理。
        // 两者对调用方完全一致：拿到的都是 capabilityType 的实例。
        PluginManifest manifest = registry.getManifest(pluginCode);
        if (isStandalone(manifest)) {
            try {
                String service = PluginServiceOwnership.resolve(manifest, false);
                String endpoint = endpointResolver.resolve(service);
                if (endpoint == null || endpoint.isBlank()) {
                    return Optional.empty();
                }
                return Optional.ofNullable(standaloneClient.createProxy(endpoint, capabilityType));
            } catch (Exception e) {
                log.warn("构造 standalone 能力代理失败，按缺失降级处理: plugin={}, capability={}, reason={}",
                        pluginCode, capabilityType.getName(), e.getMessage());
                return Optional.empty();
            }
        }
        try {
            List<T> extensions = pluginManager.getExtensions(capabilityType, pluginCode);
            if (extensions == null || extensions.isEmpty()) {
                return Optional.empty();
            }
            return Optional.ofNullable(extensions.get(0));
        } catch (Exception e) {
            // 降级永不抛异常：插件缺失/类装载失败只记录告警
            log.warn("发现插件能力失败，按缺失降级处理: plugin={}, capability={}, reason={}",
                    pluginCode, capabilityType.getName(), e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<PluginCapability> activeCapabilities() {
        List<PluginCapability> result = new ArrayList<>();
        for (Map.Entry<String, PluginRegistry.PluginRecord> entry : registry.snapshot().entrySet()) {
            PluginRegistry.PluginRecord record = entry.getValue();
            if (record == null) {
                continue;
            }
            result.add(toCapability(entry.getKey(), record.getManifest(), record.getState()));
        }
        return List.copyOf(result);
    }

    /** Standalone products always remain remote, including when discovery is unavailable. */
    private boolean isStandalone(PluginManifest manifest) {
        return manifest != null && "standalone".equalsIgnoreCase(manifest.getRuntime());
    }

    private PluginCapability toCapability(String pluginCode, PluginManifest manifest, PluginState state) {
        String version = manifest != null && manifest.getVersion() != null
                ? manifest.getVersion()
                : "unknown";
        boolean active = state == PluginState.ACTIVE;
        Boolean licenseValid = licenseValidOf(manifest);
        return new PluginCapability(pluginCode, version, active, licenseValid, isStandalone(manifest) ? List.of() : capabilityNames(pluginCode));
    }

    /**
     * License 判定：免费插件视为有效；收费/试用插件本实现不重复校验，返回 null（未校验）。
     *
     * <p>依据 §6：授权校验发生在安装、启用、后端请求、定时任务与升级各环节，
     * 插件能处于 ACTIVE 说明启用时已通过授权；能力发现不再重复判定，避免把授权状态缓存进快照导致失效延迟。
     */
    private Boolean licenseValidOf(PluginManifest manifest) {
        if (manifest == null || manifest.getLicense() == null) {
            return null;
        }
        return "FREE".equalsIgnoreCase(manifest.getLicense()) ? Boolean.TRUE : null;
    }

    private List<String> capabilityNames(String pluginCode) {
        try {
            var names = pluginManager.getExtensionClassNames(pluginCode);
            return names == null ? List.of() : List.copyOf(names);
        } catch (Exception e) {
            return List.of();
        }
    }
}
