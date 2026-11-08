package com.houjiang.bffweb.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** Resolves plugin ownership from the trusted release manifest bundled with this BFF. */
@Component
public class PluginServiceResolver {
    /**
     * 非业务服务型的合法插件宿主。
     *
     * <p>{@code mall} 不在 24 个业务服务清单里（清单里出现 mall 即为统一业务宿主回归，
     * 仍由上方的校验拦住），但它是合法插件宿主：商城域插件（优惠券/秒杀/拼团/满赠/
     * 积分商城/抽奖/红包/商品折扣）集中挂在 mall 下装载。见 AGENTS.md §零 与
     * public/docs/系统架构.md §1。路由仍只走 {@code feign.mall.url} 或 Nacos 服务发现。
     */
    private static final Set<String> NON_SERVICE_PLUGIN_HOSTS = Set.of("mall");

    private final Map<String, String> owners;
    private final Set<String> standalonePlugins;
    private final Environment environment;
    private final DiscoveryClient discovery;

    @Autowired
    public PluginServiceResolver(Environment environment, ObjectProvider<DiscoveryClient> discovery) {
        this(environment, discovery.getIfAvailable());
    }

    public PluginServiceResolver(Environment environment, DiscoveryClient discovery) {
        this.environment = environment;
        this.discovery = discovery;
        Manifest manifest = loadManifest();
        this.owners = manifest.owners;
        this.standalonePlugins = manifest.standalone;
    }

    public Resolved resolve(String pluginCode) {
        if (pluginCode == null || !pluginCode.matches("[a-z0-9][a-z0-9-]{0,63}")) {
            throw new ResolutionException(400, "Invalid plugin code");
        }
        String service = owners.get(pluginCode);
        if (service == null) throw new ResolutionException(404, "Unknown plugin");
        // Explicit, removable bridge until PM has taken over the delivered AICode service.
        // Ownership remains PM; no implicit fallback or client-supplied destination is allowed.
        String transition = environment.getProperty("houcloud.plugin.migration.aicode-url", "").trim();
        if ("aicode".equals(pluginCode) && !transition.isEmpty()) {
            return new Resolved(service, validateBaseUrl(transition), "aicode-transition");
        }
        // 独立服务型插件不跑在归属服务进程里：真实运行方是它自己的独立容器。
        // 归属服务只是平台集成边界（例如 aigateway 归属 thirdparty，但运行在 unimall-aigateway 容器），
        // 按归属服务解析会把请求打到不承载该能力的进程上。独立服务按自身编码解析，
        // 地址仍只来自服务端配置或服务发现，不接受浏览器传入的目标。
        if (standalonePlugins.contains(pluginCode)) {
            return resolveStandalone(pluginCode, service);
        }
        String configured = environment.getProperty("feign." + service + ".url", "").trim();
        if (!configured.isEmpty()) {
            return new Resolved(service, validateBaseUrl(configured), "config");
        }
        try {
            List<ServiceInstance> instances = discovery == null ? List.of() : discovery.getInstances(service);
            if (instances != null && !instances.isEmpty()) {
                ServiceInstance instance = instances.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(instances.size()));
                return new Resolved(service, validateBaseUrl(instance.getUri().toString()), "nacos");
            }
        } catch (RuntimeException ex) {
            throw new ResolutionException(503, "Plugin service discovery is unavailable");
        }
        throw new ResolutionException(503, "Plugin service is unavailable");
    }

    /**
     * 解析独立服务型插件的运行地址。
     *
     * <p>独立服务（{@code runtime: standalone}）跑在自己的容器里，真实运行方是它自身，
     * 而不是 {@code hostService} 指向的归属服务。归属只是平台集成边界：
     * 例如 AI 网关归属 thirdparty，但它跑在 {@code unimall-aigateway} 容器里，
     * 按归属服务解析会把请求打到不承载该能力的进程上。
     *
     * <p>地址来源与内嵌插件保持同一条既有路径（{@code feign.<code>.url} → 服务发现），
     * 只是服务名换成插件自身编码。现有独立服务的编码与归属同名，因此行为完全不变；
     * 拿不到地址就报 503，绝不回落去归属服务，也不接受浏览器传入的目标地址。
     */
    private Resolved resolveStandalone(String pluginCode, String ownerService) {
        String configured = environment.getProperty("feign." + pluginCode + ".url", "").trim();
        if (!configured.isEmpty()) {
            return new Resolved(ownerService, validateBaseUrl(configured), "config");
        }
        try {
            List<ServiceInstance> instances = discovery == null ? List.of() : discovery.getInstances(pluginCode);
            if (instances != null && !instances.isEmpty()) {
                ServiceInstance instance = instances.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(instances.size()));
                return new Resolved(ownerService, validateBaseUrl(instance.getUri().toString()), "nacos");
            }
        } catch (RuntimeException ex) {
            throw new ResolutionException(503, "Standalone plugin service discovery is unavailable");
        }
        throw new ResolutionException(503, "Standalone plugin service is unavailable");
    }

    private static String validateBaseUrl(String value) {
        try {
            URI uri = URI.create(value);
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null || uri.getUserInfo() != null || uri.getRawQuery() != null
                    || uri.getRawFragment() != null || uri.getPort() == 0 || uri.getPort() > 65535
                    || !(uri.getRawPath().isEmpty() || uri.getRawPath().equals("/"))) {
                throw new IllegalArgumentException("Expected an HTTP(S) origin");
            }
            return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
        } catch (IllegalArgumentException ex) {
            throw new ResolutionException(503, "Invalid plugin service address configuration");
        }
    }

    private static Manifest loadManifest() {
        try (InputStream input = PluginServiceResolver.class.getResourceAsStream("/architecture/services.json")) {
            if (input == null) throw new IllegalStateException("Missing release service ownership manifest");
            JsonNode root = new ObjectMapper().readTree(input);
            Set<String> services = new HashSet<>();
            for (JsonNode item : root.path("services")) services.add(item.path("name").asText());
            // 服务数量是下界而非等式：新增独立服务时 BFF 不应因硬编码数字未同步而启动失败。
            // 但mall 作为统一业务宿主已被基线取消，任何情况下都不得出现在服务清单里。
            if (services.size() < 24 || services.contains("mall")) {
                throw new IllegalStateException("Invalid release service list");
            }
            Map<String, String> owners = new HashMap<>();
            Set<String> standalone = new HashSet<>();
            for (JsonNode item : root.path("plugins")) {
                String code = item.path("code").asText();
                String host = item.path("hostService").asText();
                if (!code.matches("[a-z0-9][a-z0-9-]{0,63}")
                        || !(services.contains(host) || NON_SERVICE_PLUGIN_HOSTS.contains(host))
                        || owners.putIfAbsent(code, host) != null) {
                    throw new IllegalStateException("Invalid or duplicate plugin ownership: " + code);
                }
                // runtime=standalone 表示该插件跑在自己的独立容器里，BFF 不能按 hostService 解析。
                if ("standalone".equals(item.path("runtime").asText())) {
                    standalone.add(code);
                }
            }
            if (owners.isEmpty()) throw new IllegalStateException("Empty plugin ownership manifest");
            return new Manifest(Map.copyOf(owners), Set.copyOf(standalone));
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("Cannot read release service ownership manifest", ex);
        }
    }

    /** 发布清单解析结果：业务归属映射与独立服务编码集合。 */
    private record Manifest(Map<String, String> owners, Set<String> standalone) { }

    public record Resolved(String hostService, String url, String source) { }

    public static class ResolutionException extends RuntimeException {
        private final int status;
        public ResolutionException(int status, String message) {
            super(message);
            this.status = status;
        }
        public int status() { return status; }
    }
}
