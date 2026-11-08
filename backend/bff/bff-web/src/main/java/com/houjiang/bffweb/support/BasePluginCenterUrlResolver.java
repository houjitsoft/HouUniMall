package com.houjiang.bffweb.support;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

/** Resolves the base plugin-center origin from deployment configuration or discovery. */
@Component
public class BasePluginCenterUrlResolver {

    /**
     * 兜底地址：BFF 与 base 同机部署时的默认端口。
     */
    public static final String DEFAULT_BASE_URL = "http://127.0.0.1:8214";

    /**
     * 来源：配置（{@code feign.base.url}）。
     */
    public static final String SOURCE_CONFIG = "config";

    /**
     * 来源：注册中心。
     */
    public static final String SOURCE_NACOS = "nacos";

    /**
     * 来源：本机兜底。
     */
    public static final String SOURCE_DEFAULT = "default";

    /**
     * 注册中心里的服务名。
     */
    public static final String SERVICE_ID = "base";

    private final String configuredBaseUrl;

    private final DiscoveryClient discoveryClient;

    private final ForwardDiagnostics diagnostics;

    /**
     * Spring 装配：配置值 + 可选注册中心 + 诊断记录。
     *
     * @param configuredBaseUrl 配置的 base 直连地址，可为空
     * @param discoveryClient   注册中心（未接入时为 {@code null}）
     * @param diagnostics       诊断记录
     */
    @Autowired
    public BasePluginCenterUrlResolver(@Value("${feign.base.url:}") String configuredBaseUrl,
                               ObjectProvider<DiscoveryClient> discoveryClient,
                               ForwardDiagnostics diagnostics) {
        this(configuredBaseUrl, discoveryClient == null ? null : discoveryClient.getIfAvailable(),
                diagnostics);
    }

    /**
     * 无诊断记录（单测或不关心诊断的场景）。
     *
     * @param configuredBaseUrl 配置的 base 直连地址，可为空
     * @param discoveryClient   注册中心，可为 null
     */
    public BasePluginCenterUrlResolver(String configuredBaseUrl, DiscoveryClient discoveryClient) {
        this(configuredBaseUrl, discoveryClient, null);
    }

    /**
     * 完整构造。
     *
     * @param configuredBaseUrl 配置的 base 直连地址，可为空
     * @param discoveryClient   注册中心，可为 null
     * @param diagnostics       诊断记录，可为 null（为 null 时不记录）
     */
    public BasePluginCenterUrlResolver(String configuredBaseUrl, DiscoveryClient discoveryClient,
                               ForwardDiagnostics diagnostics) {
        this.configuredBaseUrl = normalize(configuredBaseUrl);
        this.discoveryClient = discoveryClient;
        this.diagnostics = diagnostics;
    }

    /**
     * 解析基址（只返回地址，不关心来源时用）。
     *
     * @return 基址，末尾无斜杠
     */
    public String resolveUrl() {
        return resolve().url();
    }

    /**
     * 解析基址，并把这个结果（含来源）记进诊断队列。
     *
     * <p>成功也记：否则排障时无法回答「它到底走了哪条路」——
     * 而「走错路」正是本次 502 的本质。
     *
     * @return 地址与来源
     */
    public Resolved resolve() {
        if (!configuredBaseUrl.isBlank()) {
            return record(configuredBaseUrl, SOURCE_CONFIG);
        }
        if (discoveryClient != null) {
            try {
                List<ServiceInstance> instances = discoveryClient.getInstances(SERVICE_ID);
                if (instances != null && !instances.isEmpty()) {
                    ServiceInstance instance = instances.get(0);
                    String host = instance.getHost();
                    int port = instance.getPort();
                    if (host != null && !host.isBlank() && port > 0) {
                        return record("http://" + host + ":" + port, SOURCE_NACOS);
                    }
                }
            } catch (RuntimeException e) {
                // 注册中心不可用不是致命错误：还有兜底地址，直接降级
                recordFailure("discovery:" + SERVICE_ID, SOURCE_NACOS, e);
            }
        }
        return record(DEFAULT_BASE_URL, SOURCE_DEFAULT);
    }

    /**
     * 记录一次转发失败（转发器在 catch 里调用）。
     *
     * <p>放在这里而不是让转发器自己拿 {@link ForwardDiagnostics}，是为了让转发器
     * 只依赖一个对象——多一个依赖就多一处「忘了注入于是没记录」的可能。
     *
     * @param url    实际请求的地址（会被打码）
     * @param source 地址来源
     * @param error  异常
     */
    public void recordFailure(String url, String source, Throwable error) {
        if (diagnostics != null) {
            diagnostics.recordFailure(url, source, error);
        }
    }

    /**
     * 配置里登记的 base 地址（可能为空）。
     *
     * @return 配置值，已去空格
     */
    public String configuredBaseUrl() {
        return configuredBaseUrl;
    }

    private Resolved record(String url, String source) {
        if (diagnostics != null) {
            diagnostics.recordResolved(url, source);
        }
        return new Resolved(url, source);
    }

    private static String normalize(String url) {
        if (url == null) {
            return "";
        }
        return url.trim().replaceAll("/+$", "");
    }

    /**
     * 解析结果。
     *
     * @param url    实际选中的基址
     * @param source 来源（{@link #SOURCE_CONFIG} / {@link #SOURCE_NACOS} / {@link #SOURCE_DEFAULT}）
     */
    public record Resolved(String url, String source) {

        /**
         * 构造解析结果。
         *
         * @param url    地址
         * @param source 来源
         */
        public Resolved {
            Objects.requireNonNull(url, "地址不能为空");
            Objects.requireNonNull(source, "来源不能为空");
        }
    }
}
