package com.houjiang.base.plugin;

import com.houjiang.base.config.FeignRequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdvertisingMerchantMenuLifecycleContributorTest {

    @Test
    void mapsAdvertisingPluginLifecycleToTheTrustedMerchantEndpoints() {
        MerchantAdvertisingMenuFeign client = mock(MerchantAdvertisingMenuFeign.class);
        when(client.transition("sync")).thenReturn(Map.of("code", 1));
        when(client.transition("disable")).thenReturn(Map.of("code", 1));
        when(client.transition("remove")).thenReturn(Map.of("code", 1));
        AdvertisingMerchantMenuLifecycleContributor contributor =
                new AdvertisingMerchantMenuLifecycleContributor(client);

        contributor.sync("mall-advertising");
        contributor.enable("mall-advertising");
        contributor.disable("mall-advertising");
        contributor.remove("mall-advertising");

        verify(client, org.mockito.Mockito.times(2)).transition("sync");
        verify(client).transition("disable");
        verify(client).transition("remove");
    }

    @Test
    void ignoresPluginsThatDoNotOwnTheAdvertisingSellerMenu() {
        MerchantAdvertisingMenuFeign client = mock(MerchantAdvertisingMenuFeign.class);
        AdvertisingMerchantMenuLifecycleContributor contributor =
                new AdvertisingMerchantMenuLifecycleContributor(client);

        contributor.sync("activity");
        contributor.disable("activity");
        contributor.remove("activity");

        verify(client, never()).transition(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void rejectsMerchantBusinessFailureInsteadOfReportingInstallSuccess() {
        MerchantAdvertisingMenuFeign client = mock(MerchantAdvertisingMenuFeign.class);
        when(client.transition("sync")).thenReturn(Map.of("code", 0, "msg", "父菜单不存在"));
        AdvertisingMerchantMenuLifecycleContributor contributor =
                new AdvertisingMerchantMenuLifecycleContributor(client);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> contributor.sync("mall-advertising"));

        assertEquals("商家端广告菜单 sync 失败: 父菜单不存在", error.getMessage());
    }

    @Test
    void serviceCredentialReplacesAnyBrowserAuthorizationHeader() {
        AdvertisingMerchantMenuFeignConfiguration configuration =
                new AdvertisingMerchantMenuFeignConfiguration("service-secret");
        RequestTemplate template = new RequestTemplate();
        template.header("Authorization", "Bearer browser-token");

        configuration.advertisingLifecycleRequestInterceptor().apply(template);

        assertFalse(template.headers().containsKey("Authorization"));
        assertEquals(java.util.List.of("service-secret"),
                java.util.List.copyOf(template.headers().get("X-Internal-Job-Token")));
    }

    @Test
    void globalBrowserCredentialPropagationCannotReAddAuthorization() {
        AdvertisingMerchantMenuFeignConfiguration configuration =
                new AdvertisingMerchantMenuFeignConfiguration("service-secret");
        RequestTemplate template = new RequestTemplate();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer browser-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try {
            configuration.advertisingLifecycleRequestInterceptor().apply(template);
            new FeignRequestInterceptor().apply(template);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }

        assertFalse(template.headers().containsKey("Authorization"));
        assertEquals(java.util.List.of("service-secret"),
                java.util.List.copyOf(template.headers().get("X-Internal-Job-Token")));
    }

    @Test
    void missingServiceCredentialFailsClosed() {
        AdvertisingMerchantMenuFeignConfiguration configuration =
                new AdvertisingMerchantMenuFeignConfiguration(" ");

        assertThrows(IllegalStateException.class,
                () -> configuration.advertisingLifecycleRequestInterceptor().apply(new RequestTemplate()));
    }
}
