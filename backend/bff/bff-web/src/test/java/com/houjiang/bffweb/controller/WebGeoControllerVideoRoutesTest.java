package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GeoServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebGeoControllerVideoRoutesTest {
    @Test
    void proxiesVideoCapabilitiesAndJobsToGeoService() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);

        controller.videoCapabilities();
        controller.videoJobs(Map.of("page", 1));

        verify(feign).videoCapabilities();
        verify(feign).videoJobs(Map.of("page", 1));
    }

    @Test
    void proxiesVideoGenerationWithProjectAndBody() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> body = Map.of("title", "demo");

        controller.videoProject(7L, Map.of());
        controller.generateVideoProject(7L, Map.of(), body);

        verify(feign).videoProject(7L, Map.of());
        verify(feign).generateVideoProject(7L, Map.of(), body);
    }

    @Test
    void proxiesCreationTaskCrudToGeoService() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> body = Map.of("title", "demo");

        controller.creationTasks(Map.of("projectId", 1));
        controller.saveCreationTask(body);
        controller.deleteCreationTasks(Map.of("ids", java.util.List.of(3L)));

        verify(feign).creationTasks(Map.of("projectId", 1));
        verify(feign).saveCreationTask(body);
        verify(feign).deleteCreationTasks(Map.of("ids", java.util.List.of(3L)));
    }

    @Test
    void proxiesAllOtherStaticRoutesFoundByFrontendAudit() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> payload = Map.of("projectId", 1);

        controller.imageCategories(payload);
        controller.createImageCategory(payload);
        controller.renameImageCategory(payload);
        controller.deleteImageCategory(payload);
        controller.analyzeAttribution(payload);
        controller.technicalAuditRun(payload);
        controller.technicalSiteHealthCheckRun(payload);

        verify(feign).imageCategories(payload);
        verify(feign).createImageCategory(payload);
        verify(feign).renameImageCategory(payload);
        verify(feign).deleteImageCategory(payload);
        verify(feign).analyzeAttribution(payload);
        verify(feign).technicalAuditRun(payload);
        verify(feign).technicalSiteHealthCheckRun(payload);
    }

    @Test
    void proxiesVideoIdeationRequests() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> body = Map.of("keywords", java.util.List.of("GEO"));

        controller.generateVideoTitles(body);
        controller.generateVideoBrief(body);
        controller.generateVideoStoryboard(body);

        verify(feign).generateVideoTitles(body);
        verify(feign).generateVideoBrief(body);
        verify(feign).generateVideoStoryboard(body);
    }

    @Test
    void proxiesAllVideoRecoveryAndHandoffOperations() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> regenerate = Map.of("stage", "avatar");
        Map<String, Object> validate = Map.of("targetPlatforms", java.util.List.of("douyin"));
        Map<String, Object> review = Map.of("action", "approve", "comment", "通过");

        controller.regenerateVideoJob(9L, regenerate);
        controller.composeVideoJob(9L, Map.of());
        controller.validateVideoJob(9L, validate);
        controller.reviewVideoJob(9L, review);
        controller.handoffVideoJob(9L, Map.of());

        verify(feign).regenerateVideoJob(9L, regenerate);
        verify(feign).composeVideoJob(9L, Map.of());
        verify(feign).validateVideoJob(9L, validate);
        verify(feign).reviewVideoJob(9L, review);
        verify(feign).handoffVideoJob(9L, Map.of());
    }

    @Test
    void proxiesModelSubscriptionWithoutDroppingProjectContext() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> body = Map.of("projectId", 31L, "subscribed", true);

        controller.updateAnalysisModelSubscription("deepseek", body);
        controller.updateModelsSubscriptionAlias("deepseek", body);

        verify(feign).updateAnalysisModelSubscription("deepseek", body);
        verify(feign).updateModelsSubscriptionAlias("deepseek", body);
    }

    @Test
    void proxiesMultimodalRequestsWithoutDroppingProjectContext() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> body = Map.of("projectId", 31L, "title", "真实生成任务");

        controller.contentMultimodal(body);
        controller.contentVisual(body);
        controller.contentTts(body);
        controller.contentAvatar(body);
        controller.contentChart(body);

        verify(feign).contentMultimodal(body);
        verify(feign).contentVisual(body);
        verify(feign).contentTts(body);
        verify(feign).contentAvatar(body);
        verify(feign).contentChart(body);
    }
}
