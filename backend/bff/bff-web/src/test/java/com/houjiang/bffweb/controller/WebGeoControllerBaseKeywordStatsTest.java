package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GeoServiceFeign;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebGeoControllerBaseKeywordStatsTest {

    @Test
    void forwardsBaseKeywordStatsWithProjectContext() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> params = Map.of("projectId", 3L);

        controller.baseKeywordStats(params);

        verify(feign).baseKeywordStats(params);
    }
}
