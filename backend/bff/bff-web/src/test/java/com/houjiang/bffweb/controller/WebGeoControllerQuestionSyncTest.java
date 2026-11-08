package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GeoServiceFeign;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebGeoControllerQuestionSyncTest {

    @Test
    void forwardsQuestionScopePreviewAndSyncWithoutChangingTheProjectPayload() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> query = Map.of("projectId", 3L);
        Map<String, Object> body = Map.of(
            "projectId", 3L,
            "keywords", List.of("S2B2B商城系统")
        );

        controller.monitorQuestionScopeSyncPreview(query);
        controller.monitorQuestionsFromScope(body);

        verify(feign).monitorQuestionScopeSyncPreview(query);
        verify(feign).monitorQuestionsFromScope(body);
    }
}
