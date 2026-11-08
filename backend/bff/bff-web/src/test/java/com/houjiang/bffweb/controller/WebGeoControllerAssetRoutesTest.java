package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GeoServiceFeign;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebGeoControllerAssetRoutesTest {
    @Test
    void proxiesAssetLifecycleRoutes() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> body = Map.of("projectId", 31L);
        controller.assetGenerationCandidates(body);
        controller.importGeneratedAssets(Map.of("jobIds", List.of(1L)));
        controller.contentUpdateAsset(8L, body);
        controller.contentDeleteAsset(8L, body);
        controller.assetVersions(8L, body);
        controller.createExplicitAssetVersion(8L, body);
        controller.rollbackAssetVersion(8L, 2L, body);
        controller.ignoreAssetFreshnessAlert(5L, body);
        verify(feign).assetGenerationCandidates(body);
        verify(feign).importGeneratedAssets(Map.of("jobIds", List.of(1L)));
        verify(feign).contentUpdateAsset(8L, body);
        verify(feign).contentDeleteAsset(8L, body);
        verify(feign).assetVersions(8L, body);
        verify(feign).createExplicitAssetVersion(8L, body);
        verify(feign).rollbackAssetVersion(8L, 2L, body);
        verify(feign).ignoreAssetFreshnessAlert(5L, body);
    }

    @Test
    void uploadRouteConsumesMultipart() throws Exception {
        Method method = WebGeoController.class.getMethod("uploadContentAsset",
            org.springframework.web.multipart.MultipartFile.class, Long.class, String.class, String.class);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertEquals(MediaType.MULTIPART_FORM_DATA_VALUE, mapping.consumes()[0]);
    }

    @Test
    void forwardsProjectContextWhenDeletingImage() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> params = Map.of("projectId", 31L);

        controller.assetsDeleteImage(8L, params);

        verify(feign).assetsDeleteImage(8L, params);
    }

    @Test
    void forwardsProjectContextWhenDeletingKnowledge() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> params = Map.of("projectId", 31L);

        controller.assetsDeleteKnowledge(9L, params);

        verify(feign).assetsDeleteKnowledge(9L, params);
    }

    @Test
    void forwardsProjectContextWhenDeletingTeamMemberAndSupportsRoleUpdate() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> params = Map.of("projectId", 31L);
        Map<String, Object> body = Map.of("projectId", 31L, "role", "管理员");

        controller.workflowRemoveTeamMember(10L, params);
        controller.updateTeamMemberRole(10L, body);

        verify(feign).workflowRemoveTeamMember(10L, params);
        verify(feign).updateTeamMemberRole(10L, body);
    }

    @Test
    void proxiesWorkflowTaskMutationRoutesWithProjectContext() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> body = Map.of("projectId", 31L, "status", "done");
        Map<String, Object> params = Map.of("projectId", 31L);

        controller.updateWorkflowTask(12L, body);
        controller.deleteWorkflowTask(12L, params);

        verify(feign).updateWorkflowTask(12L, body);
        verify(feign).deleteWorkflowTask(12L, params);
    }
}
