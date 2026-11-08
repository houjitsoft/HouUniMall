package com.houjiang.bffweb.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * {@link MallBaseUrlResolver} 单测（P0 502 修复）。
 *
 * <p>核心断言：<b>地址解析必须与既有 Feign 走同一条路——配置优先</b>。
 * 本次 502 的根因就是转发器优先吃注册中心地址、Feign 吃配置地址，
 * 同一个进程里出现两条路。这条断言把「只有一条路」钉死。
 *
 * @author Amos
 * @since M4 补丁（502 排查）
 */
class MallBaseUrlResolverTest {

    /**
     * 配置里的地址（与生产 Nacos {@code bff-web.yaml} 的 {@code feign.mall.url} 一致）。
     */
    private static final String CONFIGURED = "http://127.0.0.1:8220";

    /**
     * 注册中心里的地址（生产 Nacos 上 mall 注册的就是这个）。
     */
    private static final String FROM_NACOS = "http://127.0.0.1:8220";

    private static DiscoveryClient discoveryReturning(String host, int port) {
        DiscoveryClient client = mock(DiscoveryClient.class);
        ServiceInstance instance = new DefaultServiceInstance("inst-1", "mall", host, port, false);
        when(client.getInstances("mall")).thenReturn(List.of(instance));
        return client;
    }

    @Test
    void 配置存在时优先用配置_不用注册中心() {
        MallBaseUrlResolver resolver =
                new MallBaseUrlResolver(CONFIGURED, discoveryReturning("127.0.0.1", 8220));

        MallBaseUrlResolver.Resolved resolved = resolver.resolve();

        assertEquals(CONFIGURED, resolved.url(), "必须走配置里的回环地址，不能被注册中心带偏");
        assertEquals(MallBaseUrlResolver.SOURCE_CONFIG, resolved.source());
        assertEquals(CONFIGURED, resolver.resolveUrl());
    }

    @Test
    void 配置为空时回退到注册中心() {
        MallBaseUrlResolver resolver =
                new MallBaseUrlResolver("", discoveryReturning("127.0.0.1", 8220));

        MallBaseUrlResolver.Resolved resolved = resolver.resolve();

        assertEquals(FROM_NACOS, resolved.url());
        assertEquals(MallBaseUrlResolver.SOURCE_NACOS, resolved.source());
    }

    @Test
    void 配置为空且注册中心无实例时用本机兜底() {
        DiscoveryClient empty = mock(DiscoveryClient.class);
        when(empty.getInstances("mall")).thenReturn(List.of());

        MallBaseUrlResolver resolver = new MallBaseUrlResolver("   ", empty);

        MallBaseUrlResolver.Resolved resolved = resolver.resolve();

        assertEquals(MallBaseUrlResolver.DEFAULT_BASE_URL, resolved.url());
        assertEquals(MallBaseUrlResolver.SOURCE_DEFAULT, resolved.source());
    }

    @Test
    void 没有注册中心时也不会报错() {
        MallBaseUrlResolver resolver = new MallBaseUrlResolver("", null);

        assertEquals(MallBaseUrlResolver.DEFAULT_BASE_URL, resolver.resolveUrl());
        assertEquals(MallBaseUrlResolver.SOURCE_DEFAULT, resolver.resolve().source());
    }

    @Test
    void 注册中心抛异常时降级到兜底地址而不是中断请求() {
        DiscoveryClient broken = mock(DiscoveryClient.class);
        when(broken.getInstances("mall")).thenThrow(new IllegalStateException("nacos 不可达"));

        MallBaseUrlResolver resolver = new MallBaseUrlResolver("", broken);

        assertEquals(MallBaseUrlResolver.DEFAULT_BASE_URL, resolver.resolveUrl());
    }

    @Test
    void 配置末尾的多余斜杠会被去掉() {
        MallBaseUrlResolver resolver = new MallBaseUrlResolver("http://127.0.0.1:8220///", null);

        assertEquals("http://127.0.0.1:8220", resolver.configuredBaseUrl());
    }

    @Test
    void 每次解析都往诊断队列记一条来源() {
        ForwardDiagnostics diagnostics = new ForwardDiagnostics();
        MallBaseUrlResolver resolver =
                new MallBaseUrlResolver(CONFIGURED, discoveryReturning("127.0.0.1", 8220), diagnostics);

        resolver.resolve();
        resolver.resolve();

        List<ForwardDiagnostics.Entry> entries = diagnostics.recentResolved(10);
        assertEquals(2, entries.size());
        assertTrue(entries.stream().allMatch(e -> "config".equals(e.source())),
                "每一条都要标出它走了哪条路，否则排障时仍然只能猜");
        assertEquals(CONFIGURED, entries.get(0).url());
        assertNull(entries.get(0).errType());
    }

    @Test
    void 转发失败会记录URL与异常类型() {
        ForwardDiagnostics diagnostics = new ForwardDiagnostics();
        MallBaseUrlResolver resolver = new MallBaseUrlResolver(CONFIGURED, (DiscoveryClient) null, diagnostics);

        resolver.recordFailure(CONFIGURED + "/mall/artifact/raw?token=abc",
                "config", new java.net.ConnectException("Connection refused"));

        List<ForwardDiagnostics.Entry> failures = diagnostics.recentFailures(10);
        assertEquals(1, failures.size());
        assertEquals("java.net.ConnectException", failures.get(0).errType());
        assertEquals("Connection refused", failures.get(0).errMsg());
        // 关键：令牌不能出现在诊断输出里
        assertEquals(CONFIGURED + "/mall/artifact/raw?token=***", failures.get(0).url());
    }

    @Test
    void 未装配诊断组件时记录失败不会空指针() {
        MallBaseUrlResolver resolver = new MallBaseUrlResolver(CONFIGURED, null);

        resolver.recordFailure(CONFIGURED, "config", new RuntimeException("x"));

        assertNotNull(resolver.resolveUrl());
    }
}
