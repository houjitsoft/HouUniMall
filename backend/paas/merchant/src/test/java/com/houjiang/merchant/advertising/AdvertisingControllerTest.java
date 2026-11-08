package com.houjiang.merchant.advertising;

import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.controller.AdvertisingController;
import com.houjiang.merchant.service.AdvertisingPluginClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdvertisingControllerTest {

    @AfterEach
    void clearMerchantIdentity() {
        MerchantThreadLocal.remove();
    }

    @Test
    void ignoresBrowserShopAndActorIdsWhenSavingApplication() {
        AdvertisingPluginClient client = mock(AdvertisingPluginClient.class);
        AdvertisingController controller = new AdvertisingController(client);
        MerchantThreadLocal.put("shopId", 12);
        MerchantThreadLocal.put("adminId", 34);
        Map<String, Object> browserBody = Map.of(
                "shopId", 999,
                "actorId", 888,
                "application", Map.of("title", "秋季上新"));
        when(client.save(org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn(Map.of("id", 71, "title", "秋季上新"));

        var result = controller.save(browserBody);

        assertEquals(1, result.getCode());
        assertEquals(Map.of("id", 71, "title", "秋季上新"), result.getData());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> sent = ArgumentCaptor.forClass(Map.class);
        verify(client).save(sent.capture());
        assertEquals(12, sent.getValue().get("shopId"));
        assertEquals(34, sent.getValue().get("actorId"));
        assertEquals(Map.of("title", "秋季上新"), sent.getValue().get("application"));
    }

    @Test
    void derivesShopIdentityForListSubmitAndWithdraw() {
        AdvertisingPluginClient client = mock(AdvertisingPluginClient.class);
        AdvertisingController controller = new AdvertisingController(client);
        MerchantThreadLocal.put("shopId", 12);
        MerchantThreadLocal.put("adminId", 34);
        when(client.list(Map.of("shopId", 12, "actorId", 34))).thenReturn(List.of());
        when(client.submit(Map.of("shopId", 12, "actorId", 34, "applicationId", 81L)))
                .thenReturn(Map.of("id", 81, "reviewStatus", "PENDING"));
        when(client.withdraw(Map.of("shopId", 12, "actorId", 34, "applicationId", 82L)))
                .thenReturn(Map.of("id", 82, "reviewStatus", "WITHDRAWN"));

        assertEquals(List.of(), controller.list(Map.of("shopId", 999)).getData());
        assertEquals("PENDING", ((Map<?, ?>) controller.submit(Map.of(
                "shopId", 999, "applicationId", 81)).getData()).get("reviewStatus"));
        assertEquals("WITHDRAWN", ((Map<?, ?>) controller.withdraw(Map.of(
                "shopId", 999, "applicationId", 82)).getData()).get("reviewStatus"));
    }

    @Test
    void exposesOnlyEnabledAdvertisingPositionsThroughTheNarrowPluginClient() {
        AdvertisingPluginClient client = mock(AdvertisingPluginClient.class);
        AdvertisingController controller = new AdvertisingController(client);
        when(client.positions(Map.of())).thenReturn(List.of(Map.of(
                "id", 1, "code", "pc-login-left", "enabled", true)));

        var result = controller.positions(Map.of("shopId", 999));

        assertEquals(1, result.getCode());
        assertEquals(List.of(Map.of("id", 1, "code", "pc-login-left", "enabled", true)), result.getData());
        verify(client).positions(Map.of());
    }
}
