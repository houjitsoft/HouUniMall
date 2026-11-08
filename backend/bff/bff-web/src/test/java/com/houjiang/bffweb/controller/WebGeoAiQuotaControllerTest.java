package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GeoServiceFeign;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebGeoAiQuotaControllerTest {

    @Test
    void forwardsQuotaListAndSetPayloadsUnchanged() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> listPayload = Map.of("keyword", "42", "page", 1, "limit", 20);
        Map<String, Object> setPayload = Map.of(
            "memberId", 42L,
            "freeQuotaTokens", 200000L,
            "expiresAt", "2026-09-01T00:00:00"
        );

        controller.getAiTokenUsageAdminQuotas(listPayload);
        controller.setAiTokenUsageAdminQuota(setPayload);

        verify(feign).geoAiTokenUsageAdminQuotas(listPayload);
        verify(feign).geoAiTokenUsageAdminQuotaSet(setPayload);
    }
}
