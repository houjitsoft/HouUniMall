package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GeoServiceFeign;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebGeoControllerRunTest {

    @Test
    void forwardsProjectScopedRunOperationsWithoutChangingPayload() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> context = Map.of("projectId", 31L);

        controller.geoRuns(context);
        controller.geoRunDetail(41L, context);
        controller.geoRunEvents(41L, context);
        controller.cancelGeoRun(41L, context);
        controller.retryGeoRunItem(51L, context);

        verify(feign).geoRuns(context);
        verify(feign).geoRunDetail(41L, context);
        verify(feign).geoRunEvents(41L, context);
        verify(feign).cancelGeoRun(41L, context);
        verify(feign).retryGeoRunItem(51L, context);
    }
}
