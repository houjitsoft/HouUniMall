package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GeoServiceFeign;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebGeoControllerWorkflowTaskTest {

    @Test
    void forwardsWorkflowTaskCreationWithoutChangingProjectContext() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> body = Map.of(
            "projectId", 31L,
            "title", "技术整改：修复 noindex",
            "workflowRefId", 100L
        );

        controller.createWorkflowTask(body);

        verify(feign).createWorkflowTask(body);
    }
}
