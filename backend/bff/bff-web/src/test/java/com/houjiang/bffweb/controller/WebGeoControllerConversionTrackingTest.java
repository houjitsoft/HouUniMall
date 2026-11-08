package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GeoServiceFeign;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebGeoControllerConversionTrackingTest {

    private final GeoServiceFeign feign = mock(GeoServiceFeign.class);
    private final WebGeoController controller = new WebGeoController(feign);

    @Test
    void forwardsConversionEventManagementCalls() {
        Map<String, Object> params = Map.of("projectId", 3L, "goalId", 12L);

        controller.conversionEvents(params);
        controller.conversionEventSummary(params);
        controller.conversionEventDetail(21L, params);
        controller.revealConversionEventContact(21L, params);
        controller.deleteConversionEvent(21L, params);
        controller.exportConversionEvents(params);

        verify(feign).conversionEvents(params);
        verify(feign).conversionEventSummary(params);
        verify(feign).conversionEventDetail(21L, params);
        verify(feign).revealConversionEventContact(21L, params);
        verify(feign).deleteConversionEvent(21L, params);
        verify(feign).exportConversionEvents(params);
    }

    @Test
    void forwardsConversionCsvImportWithTheSameFile() {
        MockMultipartFile file = new MockMultipartFile(
            "file", "events.csv", "text/csv", "eventCode\nlead".getBytes()
        );
        Map<String, Object> params = Map.of("projectId", 3L);

        controller.importConversionEvents(file, params);

        verify(feign).importConversionEvents(file, params);
    }

    @Test
    void forwardsTrackingConfigurationAndRotationBodiesWithoutMutation() {
        Map<String, Object> params = Map.of("projectId", 3L);
        Map<String, Object> config = new LinkedHashMap<>(Map.of(
            "projectId", 3L, "conversionContactRetentionDays", 180
        ));
        Map<String, Object> tokenRotation = new LinkedHashMap<>(Map.of("projectId", 3L));
        Map<String, Object> secretRotation = new LinkedHashMap<>(Map.of("projectId", 3L));

        controller.conversionTrackingConfig(params);
        controller.saveConversionTrackingConfig(config);
        controller.rotateConversionTrackingToken(tokenRotation);
        controller.rotateConversionWebhookSecret(secretRotation);

        verify(feign).conversionTrackingConfig(params);
        verify(feign).saveConversionTrackingConfig(config);
        verify(feign).rotateConversionTrackingToken(tokenRotation);
        verify(feign).rotateConversionWebhookSecret(secretRotation);
    }
}
