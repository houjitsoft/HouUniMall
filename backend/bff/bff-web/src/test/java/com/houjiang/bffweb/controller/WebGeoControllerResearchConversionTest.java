package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GeoServiceFeign;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebGeoControllerResearchConversionTest {

    @Test
    void forwardsResearchConversionPayloadWithoutChangingIt() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> body = Map.of("projectId", 31L, "sourceType", "keyword", "sourceId", 21L);

        controller.researchConversionPreview(body);
        controller.researchConversionCommit(body);

        verify(feign).researchConversionPreview(body);
        verify(feign).researchConversionCommit(body);
    }
}
