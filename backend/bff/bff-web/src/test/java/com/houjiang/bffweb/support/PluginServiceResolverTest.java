package com.houjiang.bffweb.support;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PluginServiceResolverTest {
    @Test
    void discoversOnlyTheTrustedOwnerForProductsAndExtensions() {
        DiscoveryClient discovery = mock(DiscoveryClient.class);
        when(discovery.getInstances("oa")).thenReturn(List.of(new DefaultServiceInstance("oa-1", "oa", "oa-node", 8393, false)));
        when(discovery.getInstances("payment")).thenReturn(List.of(new DefaultServiceInstance("pay-1", "payment", "pay-node", 8207, false)));
        PluginServiceResolver resolver = new PluginServiceResolver(new MockEnvironment(), discovery);
        assertEquals("http://oa-node:8393", resolver.resolve("oa").url());
        // 内嵌扩展按归属服务解析（unipay 归属 payment，随宿主运行）
        assertEquals("http://pay-node:8207", resolver.resolve("unipay").url());
        assertEquals("payment", resolver.resolve("unipay").hostService());
        verify(discovery, never()).getInstances("mall");
    }

    @Test
    void configuredOriginOverridesDiscoveryAndNormalizesTrailingSlash() {
        DiscoveryClient discovery = mock(DiscoveryClient.class);
        var resolver = new PluginServiceResolver(new MockEnvironment().withProperty("feign.erp.url", "https://erp.example:8443/"), discovery);
        assertEquals("https://erp.example:8443", resolver.resolve("erp").url());
        verifyNoInteractions(discovery);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ftp://erp.example", "http://erp.example/plugin/oa", "http://erp.example//", "http://erp.example/?x=1",
            "http://erp.example/#x", "http://user@erp.example", "http://erp.example/%2e%2e", "http://erp.example:99999", "erp.example"})
    void rejectsConfiguredUrlsThatCanInjectPathOrAuthority(String url) {
        var resolver = new PluginServiceResolver(new MockEnvironment().withProperty("feign.erp.url", url), (DiscoveryClient) null);
        assertEquals(503, assertThrows(PluginServiceResolver.ResolutionException.class, () -> resolver.resolve("erp")).status());
    }

    @Test
    void rejectsUnknownPluginEvenIfAClientNamedServiceExists() {
        DiscoveryClient discovery = mock(DiscoveryClient.class);
        var resolver = new PluginServiceResolver(new MockEnvironment().withProperty("feign.evil.url", "http://evil"), discovery);
        assertEquals(404, assertThrows(PluginServiceResolver.ResolutionException.class, () -> resolver.resolve("evil")).status());
        assertEquals(400, assertThrows(PluginServiceResolver.ResolutionException.class, () -> resolver.resolve("oa%2Ferp")).status());
        verifyNoInteractions(discovery);
    }

    @Test
    void unavailableDiscoveryNeverFallsBackToMall() {
        DiscoveryClient discovery = mock(DiscoveryClient.class);
        when(discovery.getInstances("mes")).thenReturn(List.of());
        when(discovery.getInstances("oa")).thenThrow(new IllegalStateException("offline"));
        var resolver = new PluginServiceResolver(new MockEnvironment().withProperty("feign.mall.url", "http://mall"), discovery);
        for (String code : List.of("mes", "oa")) {
            assertEquals(503, assertThrows(PluginServiceResolver.ResolutionException.class, () -> resolver.resolve(code)).status());
        }
        verify(discovery, never()).getInstances("mall");
    }
    /**
     * 打包默认值不得让<b>独立服务型插件</b>指向归属服务的地址。
     *
     * <p>独立服务跑在自己的容器里（AI 网关在 unimall-aigateway），按归属服务解析
     * 会把请求打到不承载该能力的进程上。默认配置里若出现独立服务编码，
     * 必须是它自己的地址，而不是 hostService 的地址。
     *
     * <p>内嵌扩展不在此断言范围：它们本来就随宿主服务运行，
     * {@code feign.<hostService>.url} 指向宿主是正确行为。
     */
    @Test
    void packagedDefaultsNeverPointStandalonePluginsAtTheirOwnerService() throws Exception {
        var yaml = new org.springframework.beans.factory.config.YamlPropertiesFactoryBean();
        yaml.setResources(new org.springframework.core.io.ClassPathResource("bootstrap.yml"));
        var properties = yaml.getObject();
        try (var stream = getClass().getResourceAsStream("/architecture/services.json")) {
            var manifest = new com.fasterxml.jackson.databind.ObjectMapper().readTree(stream);
            for (var plugin : manifest.path("plugins")) {
                if (!"standalone".equals(plugin.path("runtime").asText())) continue;
                String code = plugin.path("code").asText();
                String owner = plugin.path("hostService").asText();
                Object own = properties.get("feign." + code + ".url");
                Object ownerUrl = properties.get("feign." + owner + ".url");
                if (own == null) continue;
                // 配置了自身地址时，不得与归属服务地址相同（否则等于没按独立服务解析）
                if (ownerUrl != null && own.equals(ownerUrl) && !code.equals(owner)) {
                    fail("独立服务 " + code + " 的默认地址与归属服务 " + owner + " 相同，"
                            + "会把请求打到不承载该能力的进程上");
                }
            }
        }
    }

    @Test
    void explicitAicodeTransitionKeepsPmOwnershipWithoutChangingOtherPlugins() {
        var env = new MockEnvironment().withProperty("houcloud.plugin.migration.aicode-url", "http://127.0.0.1:8318/");
        var resolver = new PluginServiceResolver(env, (DiscoveryClient) null);
        assertEquals("pm", resolver.resolve("aicode").hostService());
        assertEquals("http://127.0.0.1:8318", resolver.resolve("aicode").url());
        assertEquals("aicode-transition", resolver.resolve("aicode").source());
        assertThrows(PluginServiceResolver.ResolutionException.class, () -> resolver.resolve("pm"));
    }

    /**
     * 独立服务型插件按自身编码解析，而不是按归属服务。
     *
     * <p>AI 网关归属 thirdparty（平台集成边界），但它跑在独立的 unimall-aigateway 容器里；
     * 按 thirdparty 解析会把请求打到不承载网关能力的进程上。
     */
    @Test
    void standalonePluginResolvesByItsOwnServiceNotByOwnership() {
        DiscoveryClient discovery = mock(DiscoveryClient.class);
        when(discovery.getInstances("aigateway"))
                .thenReturn(List.of(new DefaultServiceInstance("aigateway-1", "aigateway", "aigateway-node", 18301, false)));
        var resolver = new PluginServiceResolver(new MockEnvironment(), discovery);

        var resolved = resolver.resolve("aigateway");
        assertEquals("http://aigateway-node:18301", resolved.url());
        assertEquals("thirdparty", resolved.hostService(), "归属仍是平台集成边界，不因运行方式改变");
        verify(discovery, never()).getInstances("thirdparty");
    }

    /** 既有独立服务编码与归属同名，解析行为必须与改造前一致。 */
    @Test
    void existingStandaloneServicesKeepResolvingByTheirOwnName() {
        DiscoveryClient discovery = mock(DiscoveryClient.class);
        when(discovery.getInstances("erp"))
                .thenReturn(List.of(new DefaultServiceInstance("erp-1", "erp", "erp-node", 8393, false)));
        var resolver = new PluginServiceResolver(new MockEnvironment(), discovery);

        assertEquals("http://erp-node:8393", resolver.resolve("erp").url());
        verify(discovery).getInstances("erp");
    }

    /** 独立服务拿不到地址时报 503，绝不回落去归属服务。 */
    @Test
    void standalonePluginNeverFallsBackToOwnershipService() {
        DiscoveryClient discovery = mock(DiscoveryClient.class);
        when(discovery.getInstances("aigateway")).thenReturn(List.of());
        var resolver = new PluginServiceResolver(new MockEnvironment(), discovery);

        assertEquals(503, assertThrows(PluginServiceResolver.ResolutionException.class,
                () -> resolver.resolve("aigateway")).status());
        verify(discovery, never()).getInstances("thirdparty");
    }

}
