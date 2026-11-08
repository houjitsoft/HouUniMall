package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.service.PluginApiClient;
import com.houjiang.bffweb.service.PluginApiClient.PluginCallResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AdvertisingSlotControllerTest {

    private final PluginApiClient pluginApiClient = mock(PluginApiClient.class);
    private final AdvertisingSlotController controller = new AdvertisingSlotController(pluginApiClient);

    @Test
    void returnsSanitizedActiveAdvertisements() {
        when(pluginApiClient.internalGet("mall-advertising", "/public/slots/pc-login-left"))
                .thenReturn(new PluginCallResult(200, List.of(Map.of(
                        "id", 7,
                        "title", "秋季上新",
                        "imageUrl", "https://cdn.example.com/ad.webp",
                        "targetUrl", "/goods/7",
                        "startTime", "2026-09-24T09:00:00",
                        "endTime", "2026-09-25T09:00:00",
                        "shopId", 9988,
                        "reviewNote", "private"))));

        var result = controller.slot("pc-login-left");

        assertEquals(1, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals(7L, result.getData().getFirst().id());
        assertEquals("/goods/7", result.getData().getFirst().targetUrl());
    }

    @Test
    void absentDisabledOrUnavailablePluginDegradesToEmptySuccess() {
        for (int status : List.of(401, 404, 423, 503, 504)) {
            when(pluginApiClient.internalGet("mall-advertising", "/public/slots/pc-login-left"))
                    .thenReturn(new PluginCallResult(status, Map.of("error", "hidden")));
            var result = controller.slot("pc-login-left");
            assertEquals(1, result.getCode());
            assertEquals(List.of(), result.getData());
        }
    }

    @Test
    void malformedOrUnsafeItemsAreRemovedOrHaveTheirTargetCleared() {
        when(pluginApiClient.internalGet("mall-advertising", "/public/slots/pc-login-left"))
                .thenReturn(new PluginCallResult(200, List.of(
                        Map.of("id", 1, "title", "bad image", "imageUrl", "javascript:alert(1)"),
                        Map.of("id", 2, "title", "safe image", "imageUrl", "/uploads/ad.webp",
                                "targetUrl", "javascript:alert(1)"))));

        var result = controller.slot("pc-login-left");

        assertEquals(1, result.getData().size());
        assertEquals(2L, result.getData().getFirst().id());
        assertEquals(null, result.getData().getFirst().targetUrl());
    }

    @Test
    void rejectsUnsafeSlotCodeWithoutCallingPluginHost() {
        var result = controller.slot("../../secret");

        assertEquals(400, result.getCode());
        assertEquals(List.of(), result.getData());
        verifyNoInteractions(pluginApiClient);
    }
}
