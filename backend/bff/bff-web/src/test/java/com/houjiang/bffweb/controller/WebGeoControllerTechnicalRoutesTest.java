package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GeoServiceFeign;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebGeoControllerTechnicalRoutesTest {

    @Test
    void forwardsTechnicalRunRequestsWithoutChangingPayload() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> body = Map.of("projectId", 31L, "url", "https://example.com");

        controller.technicalAuditRun(body);
        controller.technicalSiteHealthCheckRun(body);
        controller.technicalSiteHealthCwvCheckRun(body);

        verify(feign).technicalAuditRun(body);
        verify(feign).technicalSiteHealthCheckRun(body);
        verify(feign).technicalSiteHealthCwvCheckRun(body);
    }
}
