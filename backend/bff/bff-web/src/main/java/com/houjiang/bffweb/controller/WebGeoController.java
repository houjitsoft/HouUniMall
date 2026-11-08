package com.houjiang.bffweb.controller;



import com.houjiang.bffweb.feign.GeoServiceFeign;

import com.houjiang.bffweb.util.ResponseResult;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;



import java.util.LinkedHashMap;
import java.util.Map;



/**

 * GEO BFF 转发层（/web/geo → paas-geo /geo）

/**
 * @author Amos
 */

@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")

@RequestMapping("/web/geo")

@RequiredArgsConstructor

public class WebGeoController {



    private final GeoServiceFeign geoServiceFeign;

    @GetMapping("/content/creation-tasks")
    public ResponseResult creationTasks(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.creationTasks(params != null ? params : Map.of());
    }

    @PostMapping("/content/creation-tasks")
    public ResponseResult saveCreationTask(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.saveCreationTask(body);
    }

    @PostMapping("/content/creation-tasks/batch-delete")
    public ResponseResult deleteCreationTasks(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.deleteCreationTasks(body);
    }

    @GetMapping("/assets/image-categories")
    public ResponseResult imageCategories(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.imageCategories(params != null ? params : Map.of()); }

    @PostMapping("/assets/image-categories")
    public ResponseResult createImageCategory(@RequestBody Map<String, Object> body) { return geoServiceFeign.createImageCategory(body); }

    @PutMapping("/assets/image-categories/rename")
    public ResponseResult renameImageCategory(@RequestBody Map<String, Object> body) { return geoServiceFeign.renameImageCategory(body); }

    @DeleteMapping("/assets/image-categories")
    public ResponseResult deleteImageCategory(@RequestParam Map<String, Object> params) { return geoServiceFeign.deleteImageCategory(params); }

    @GetMapping("/brand/products")
    public ResponseResult brandProducts(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.brandProducts(params != null ? params : Map.of());
    }

    @PostMapping("/brand/products")
    public ResponseResult createBrandProduct(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.createBrandProduct(body);
    }

    @PutMapping("/brand/products/{id}")
    public ResponseResult updateBrandProduct(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.updateBrandProduct(id, body);
    }

    @DeleteMapping("/brand/products/{id}")
    public ResponseResult deleteBrandProduct(
            @PathVariable Long id,
            @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.deleteBrandProduct(id, params != null ? params : Map.of());
    }

    @GetMapping("/brand/competitors")
    public ResponseResult brandCompetitors(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.brandCompetitors(params != null ? params : Map.of());
    }

    @PostMapping("/brand/competitors")
    public ResponseResult createBrandCompetitor(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.createBrandCompetitor(body);
    }

    @PutMapping("/brand/competitors/{id}")
    public ResponseResult updateBrandCompetitor(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.updateBrandCompetitor(id, body);
    }

    @DeleteMapping("/brand/competitors/{id}")
    public ResponseResult deleteBrandCompetitor(
            @PathVariable Long id,
            @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.deleteBrandCompetitor(id, params != null ? params : Map.of());
    }

    @PostMapping("/brand/catalog-analysis")
    public ResponseResult triggerBrandCatalogAnalysis(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.triggerBrandCatalogAnalysis(body != null ? body : Map.of());
    }

    @GetMapping("/brand/catalog-analysis/status")
    public ResponseResult brandCatalogAnalysisStatus(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.brandCatalogAnalysisStatus(params != null ? params : Map.of());
    }

    @PostMapping("/experiments/attribution/analyze")
    public ResponseResult analyzeAttribution(@RequestBody Map<String, Object> body) { return geoServiceFeign.analyzeAttribution(body); }

    @PostMapping("/technical/crawl-index/audit-run")
    public ResponseResult technicalAuditRun(@RequestBody Map<String, Object> body) { return geoServiceFeign.technicalAuditRun(body); }

    @PostMapping("/technical/site-health/check-run")
    public ResponseResult technicalSiteHealthCheckRun(@RequestBody Map<String, Object> body) { return geoServiceFeign.technicalSiteHealthCheckRun(body); }

    @PostMapping("/technical/site-health/cwv-check-run")
    public ResponseResult technicalSiteHealthCwvCheckRun(@RequestBody Map<String, Object> body) { return geoServiceFeign.technicalSiteHealthCwvCheckRun(body); }

    @GetMapping("/runs")
    public ResponseResult geoRuns(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.geoRuns(params != null ? params : Map.of());
    }

    @GetMapping("/runs/{id}")
    public ResponseResult geoRunDetail(
            @PathVariable Long id,
            @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.geoRunDetail(id, params != null ? params : Map.of());
    }

    @GetMapping("/runs/{id}/events")
    public ResponseResult geoRunEvents(
            @PathVariable Long id,
            @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.geoRunEvents(id, params != null ? params : Map.of());
    }

    @PostMapping("/runs/{id}/cancel")
    public ResponseResult cancelGeoRun(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.cancelGeoRun(id, body != null ? body : Map.of());
    }

    @PostMapping("/runs/items/{itemId}/retry")
    public ResponseResult retryGeoRunItem(
            @PathVariable Long itemId,
            @RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.retryGeoRunItem(itemId, body != null ? body : Map.of());
    }

    @GetMapping("/content/video-capabilities")
    public ResponseResult videoCapabilities() { return geoServiceFeign.videoCapabilities(); }

    @GetMapping("/content/digital-humans/avatars/templates")
    public ResponseResult digitalHumanAvatarTemplates(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.aliyunVideoTemplates(params != null ? params : Map.of()); }
    @GetMapping("/content/digital-humans/avatars/templates/{templateId}")
    public ResponseResult digitalHumanAvatarTemplate(@PathVariable String templateId) { return geoServiceFeign.aliyunVideoTemplate(templateId); }
    @GetMapping("/content/digital-humans/avatars/template-materials")
    public ResponseResult digitalHumanAvatarTemplateMaterials(@RequestParam Map<String, Object> params) { return geoServiceFeign.aliyunVideoMaterials(params); }
    @GetMapping("/content/digital-humans/avatars/public-scene-templates")
    public ResponseResult digitalHumanPublicSceneTemplates(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.aliyunPublicSceneTemplates(params != null ? params : Map.of()); }
    @PostMapping("/content/digital-humans/avatars/public-scenes")
    public ResponseResult copyDigitalHumanPublicScene(@RequestBody Map<String, Object> body) { return geoServiceFeign.copyAliyunPublicScene(body); }
    @GetMapping("/content/digital-humans/avatars/resources")
    public ResponseResult digitalHumanAvatarResources(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.aliyunVideoResources(params != null ? params : Map.of()); }
    @PostMapping("/content/digital-humans/avatars/no-train")
    public ResponseResult createDigitalHumanNoTrainAvatar(@RequestBody Map<String, Object> body) { return geoServiceFeign.createAliyunNoTrainAvatar(body); }
    @PostMapping("/content/digital-humans/avatars/train")
    public ResponseResult createDigitalHumanTrainAvatar(@RequestBody Map<String, Object> body) { return geoServiceFeign.createAliyunTrainAvatar(body); }
    @GetMapping("/content/digital-humans/avatars/{avatarId}/status")
    public ResponseResult digitalHumanTrainAvatarStatus(@PathVariable String avatarId) { return geoServiceFeign.aliyunTrainAvatarStatus(avatarId); }
    @PostMapping("/content/digital-humans/avatars/{avatarId}/confirm")
    public ResponseResult confirmDigitalHumanTrainAvatar(@PathVariable String avatarId, @RequestBody Map<String, Object> body) { return geoServiceFeign.confirmAliyunTrainAvatar(avatarId, body); }

    @GetMapping("/content/digital-humans/voices")
    public ResponseResult digitalHumanVoices(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.aliyunVoices(params != null ? params : Map.of()); }
    @GetMapping("/content/digital-humans/voices/public")
    public ResponseResult digitalHumanPublicVoices() { return geoServiceFeign.aliyunPublicVoices(); }
    @PostMapping("/content/digital-humans/voices/public/use")
    public ResponseResult useDigitalHumanPublicVoice(@RequestBody Map<String, Object> body) { return geoServiceFeign.useAliyunPublicVoice(body); }
    @GetMapping("/content/digital-humans/voices/{voiceId}")
    public ResponseResult digitalHumanVoice(@PathVariable String voiceId) { return geoServiceFeign.aliyunVoice(voiceId); }
    @PostMapping("/content/digital-humans/voices")
    public ResponseResult createDigitalHumanVoice(@RequestBody Map<String, Object> body) { return geoServiceFeign.createAliyunVoice(body); }
    @GetMapping("/content/digital-humans/voices/audio")
    public ResponseResult digitalHumanAudioResources(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.aliyunVideoResources(params != null ? params : Map.of("resourceType", "audio")); }
    @PostMapping("/content/digital-humans/voices/audio")
    public ResponseResult syncDigitalHumanAudio(@RequestBody Map<String, Object> body) { return geoServiceFeign.syncAliyunVideoAudio(body); }

    @GetMapping("/content/digital-humans/materials")
    public ResponseResult digitalHumanMaterials(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.aliyunVideoResources(params != null ? params : Map.of()); }
    @PostMapping("/content/digital-humans/materials/backgrounds")
    public ResponseResult syncDigitalHumanBackground(@RequestBody Map<String, Object> body) { return geoServiceFeign.syncAliyunBackground(body); }
    @PostMapping("/content/digital-humans/materials/stickers")
    public ResponseResult syncDigitalHumanSticker(@RequestBody Map<String, Object> body) { return geoServiceFeign.syncAliyunSticker(body); }
    @DeleteMapping("/content/digital-humans/materials/stickers/{stickerId}")
    public ResponseResult deleteDigitalHumanSticker(@PathVariable String stickerId, @RequestBody(required = false) Map<String, Object> body) { return geoServiceFeign.deleteAliyunSticker(stickerId, body != null ? body : Map.of()); }
    @PostMapping("/content/digital-humans/materials/batch-delete")
    public ResponseResult batchDeleteDigitalHumanMaterials(@RequestBody Map<String, Object> body) { return geoServiceFeign.batchDeleteAliyunMaterials(body); }

    @GetMapping("/content/digital-humans/api-services")
    public ResponseResult digitalHumanApiServices() { return geoServiceFeign.digitalHumanApiServices(); }
    @GetMapping("/content/digital-humans/api-services/resources")
    public ResponseResult digitalHumanApiResources(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.aliyunVideoResources(params != null ? params : Map.of()); }
    @PostMapping("/content/digital-humans/api-services/chat-configs")
    public ResponseResult createDigitalHumanChatConfig(@RequestBody Map<String, Object> body) { return geoServiceFeign.createAliyunChatConfig(body); }
    @PostMapping("/content/digital-humans/api-services/chat-sessions")
    public ResponseResult createDigitalHumanChatSession(@RequestBody Map<String, Object> body) { return geoServiceFeign.createAliyunChatSession(body); }
    @PostMapping("/content/digital-humans/api-services/chat-sessions/query")
    public ResponseResult queryDigitalHumanChatSessions(@RequestBody Map<String, Object> body) { return geoServiceFeign.queryAliyunChatSessions(body); }
    @PostMapping("/content/digital-humans/api-services/chat-sessions/close")
    public ResponseResult closeDigitalHumanChatSessions(@RequestBody Map<String, Object> body) { return geoServiceFeign.closeAliyunChatSessions(body); }

    @PostMapping("/content/digital-humans/live/instances")
    public ResponseResult startAliyunAvatarLive(@RequestBody Map<String, Object> body) { return geoServiceFeign.startAliyunAvatarLive(body); }
    @DeleteMapping("/content/digital-humans/live/instances/{sessionId}")
    public ResponseResult stopAliyunAvatarLive(@PathVariable String sessionId, @RequestBody(required = false) Map<String, Object> body) { return geoServiceFeign.stopAliyunAvatarLive(sessionId, body != null ? body : Map.of()); }
    @GetMapping("/content/digital-humans/live/instances")
    public ResponseResult queryAliyunAvatarLiveInstances(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.queryAliyunAvatarLiveInstances(params != null ? params : Map.of()); }
    @PostMapping("/content/digital-humans/live/instances/{sessionId}/text")
    public ResponseResult sendAliyunAvatarLiveText(@PathVariable String sessionId, @RequestBody Map<String, Object> body) { return geoServiceFeign.sendAliyunAvatarLiveText(sessionId, body); }

    @GetMapping("/content/video-assets/aliyun/templates")
    public ResponseResult aliyunVideoTemplates(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.aliyunVideoTemplates(params != null ? params : Map.of()); }

    @GetMapping("/content/video-assets/aliyun/public-scene-templates")
    public ResponseResult aliyunPublicSceneTemplates(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.aliyunPublicSceneTemplates(params != null ? params : Map.of()); }

    @PostMapping("/content/video-assets/aliyun/public-scenes")
    public ResponseResult copyAliyunPublicScene(@RequestBody Map<String, Object> body) { return geoServiceFeign.copyAliyunPublicScene(body); }

    @GetMapping("/content/video-assets/aliyun/materials")
    public ResponseResult aliyunVideoMaterials(@RequestParam Map<String, Object> params) { return geoServiceFeign.aliyunVideoMaterials(params); }

    @PostMapping("/content/video-assets/aliyun/audio-sync")
    public ResponseResult syncAliyunVideoAudio(@RequestBody Map<String, Object> body) { return geoServiceFeign.syncAliyunVideoAudio(body); }

    @GetMapping("/content/video-assets/aliyun/templates/{templateId}") public ResponseResult aliyunVideoTemplate(@PathVariable String templateId) { return geoServiceFeign.aliyunVideoTemplate(templateId); }
    @GetMapping("/content/video-assets/aliyun/resources") public ResponseResult aliyunVideoResources(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.aliyunVideoResources(params != null ? params : Map.of()); }
    @PostMapping("/content/video-assets/aliyun/background-sync") public ResponseResult syncAliyunBackground(@RequestBody Map<String, Object> body) { return geoServiceFeign.syncAliyunBackground(body); }
    @PostMapping("/content/video-assets/aliyun/sticker-sync") public ResponseResult syncAliyunSticker(@RequestBody Map<String, Object> body) { return geoServiceFeign.syncAliyunSticker(body); }
    @DeleteMapping("/content/video-assets/aliyun/stickers/{stickerId}") public ResponseResult deleteAliyunSticker(@PathVariable String stickerId, @RequestBody(required = false) Map<String, Object> body) { return geoServiceFeign.deleteAliyunSticker(stickerId, body != null ? body : Map.of()); }
    @PostMapping("/content/video-assets/aliyun/materials/batch-delete") public ResponseResult batchDeleteAliyunMaterials(@RequestBody Map<String, Object> body) { return geoServiceFeign.batchDeleteAliyunMaterials(body); }
    @PostMapping("/content/video-assets/aliyun/avatars/no-train") public ResponseResult createAliyunNoTrainAvatar(@RequestBody Map<String, Object> body) { return geoServiceFeign.createAliyunNoTrainAvatar(body); }
    @PostMapping("/content/video-assets/aliyun/avatars/train") public ResponseResult createAliyunTrainAvatar(@RequestBody Map<String, Object> body) { return geoServiceFeign.createAliyunTrainAvatar(body); }
    @GetMapping("/content/video-assets/aliyun/avatars/{avatarId}/status") public ResponseResult aliyunTrainAvatarStatus(@PathVariable String avatarId) { return geoServiceFeign.aliyunTrainAvatarStatus(avatarId); }
    @PostMapping("/content/video-assets/aliyun/avatars/{avatarId}/confirm") public ResponseResult confirmAliyunTrainAvatar(@PathVariable String avatarId, @RequestBody Map<String, Object> body) { return geoServiceFeign.confirmAliyunTrainAvatar(avatarId, body); }
    @GetMapping("/content/video-assets/aliyun/voices") public ResponseResult aliyunVoices(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.aliyunVoices(params != null ? params : Map.of()); }
    @GetMapping("/content/video-assets/aliyun/voices/public") public ResponseResult aliyunPublicVoices() { return geoServiceFeign.aliyunPublicVoices(); }
    @PostMapping("/content/video-assets/aliyun/voices/public/use") public ResponseResult useAliyunPublicVoice(@RequestBody Map<String, Object> body) { return geoServiceFeign.useAliyunPublicVoice(body); }
    @GetMapping("/content/video-assets/aliyun/voices/{voiceId}") public ResponseResult aliyunVoice(@PathVariable String voiceId) { return geoServiceFeign.aliyunVoice(voiceId); }
    @PostMapping("/content/video-assets/aliyun/voices") public ResponseResult createAliyunVoice(@RequestBody Map<String, Object> body) { return geoServiceFeign.createAliyunVoice(body); }
    @PostMapping("/content/video-assets/aliyun/chat-configs") public ResponseResult createAliyunChatConfig(@RequestBody Map<String, Object> body) { return geoServiceFeign.createAliyunChatConfig(body); }
    @PostMapping("/content/video-assets/aliyun/chat-sessions") public ResponseResult createAliyunChatSession(@RequestBody Map<String, Object> body) { return geoServiceFeign.createAliyunChatSession(body); }
    @PostMapping("/content/video-assets/aliyun/chat-sessions/query") public ResponseResult queryAliyunChatSessions(@RequestBody Map<String, Object> body) { return geoServiceFeign.queryAliyunChatSessions(body); }
    @PostMapping("/content/video-assets/aliyun/chat-sessions/close") public ResponseResult closeAliyunChatSessions(@RequestBody Map<String, Object> body) { return geoServiceFeign.closeAliyunChatSessions(body); }

    @PostMapping("/content/video-ideation/titles")
    public ResponseResult generateVideoTitles(@RequestBody Map<String, Object> body) { return geoServiceFeign.generateVideoTitles(body); }

    @PostMapping("/content/video-ideation/brief")
    public ResponseResult generateVideoBrief(@RequestBody Map<String, Object> body) { return geoServiceFeign.generateVideoBrief(body); }

    @PostMapping("/content/video-ideation/storyboard")
    public ResponseResult generateVideoStoryboard(@RequestBody Map<String, Object> body) { return geoServiceFeign.generateVideoStoryboard(body); }

    @PostMapping("/content/video-projects")
    public ResponseResult createVideoProject(@RequestBody Map<String, Object> body) { return geoServiceFeign.createVideoProject(body); }

    @GetMapping("/content/video-projects")
    public ResponseResult videoProjects(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.videoProjects(params != null ? params : Map.of()); }

    @GetMapping("/content/video-projects/{projectId}")
    public ResponseResult videoProject(@PathVariable Long projectId, @RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.videoProject(projectId, params != null ? params : Map.of()); }

    @PutMapping("/content/video-projects/{projectId}")
    public ResponseResult saveVideoProject(@PathVariable Long projectId, @RequestBody Map<String, Object> body) { return geoServiceFeign.saveVideoProject(projectId, body); }

    @PostMapping("/content/video-projects/{projectId}/reviews")
    public ResponseResult reviewVideoProject(@PathVariable Long projectId, @RequestBody Map<String, Object> body) { return geoServiceFeign.reviewVideoProject(projectId, body); }

    @PostMapping("/content/video-projects/{projectId}/preflight")
    public ResponseResult preflightVideoProject(@PathVariable Long projectId, @RequestBody Map<String, Object> body) { return geoServiceFeign.preflightVideoProject(projectId, body); }

    @PostMapping("/content/video-projects/{projectId}/generate")
    public ResponseResult generateVideoProject(@PathVariable Long projectId, @RequestParam(required = false) Map<String, Object> params, @RequestBody Map<String, Object> body) { return geoServiceFeign.generateVideoProject(projectId, params != null ? params : Map.of(), body); }

    @GetMapping("/content/video-jobs")
    public ResponseResult videoJobs(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.videoJobs(params != null ? params : Map.of()); }

    @GetMapping("/content/video-jobs/{id}")
    public ResponseResult videoJob(@PathVariable Long id, @RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.videoJob(id, params != null ? params : Map.of()); }

    @GetMapping("/content/video-jobs/{id}/detail")
    public ResponseResult videoJobDetail(@PathVariable Long id, @RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.videoJobDetail(id, params != null ? params : Map.of()); }

    @PostMapping("/content/video-jobs/{id}/cancel")
    public ResponseResult cancelVideoJob(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) { return geoServiceFeign.cancelVideoJob(id, body != null ? body : Map.of()); }

    @PostMapping("/content/video-jobs/{id}/retry")
    public ResponseResult retryVideoJob(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) { return geoServiceFeign.retryVideoJob(id, body != null ? body : Map.of()); }

    @PostMapping("/content/video-jobs/{id}/regenerate")
    public ResponseResult regenerateVideoJob(@PathVariable Long id, @RequestBody Map<String, Object> body) { return geoServiceFeign.regenerateVideoJob(id, body); }

    @PostMapping("/content/video-jobs/{id}/compose")
    public ResponseResult composeVideoJob(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) { return geoServiceFeign.composeVideoJob(id, body != null ? body : Map.of()); }

    @PostMapping("/content/video-jobs/{id}/validate")
    public ResponseResult validateVideoJob(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) { return geoServiceFeign.validateVideoJob(id, body != null ? body : Map.of()); }

    @PostMapping("/content/video-jobs/{id}/review")
    public ResponseResult reviewVideoJob(@PathVariable Long id, @RequestBody Map<String, Object> body) { return geoServiceFeign.reviewVideoJob(id, body); }

    @PostMapping("/content/video-jobs/{id}/handoff")
    public ResponseResult handoffVideoJob(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) { return geoServiceFeign.handoffVideoJob(id, body != null ? body : Map.of()); }

    /** 单平台分发路径段，排除 omni 以免与 Omni 专用路由冲突 */
    private static final String DIST_PLATFORM = "{platform:^(?!omni$)[a-z0-9_-]+}";



    /** --- 监测 --- */

    @GetMapping("/monitor/visibility")

    public ResponseResult monitorVisibility(@RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.monitorVisibility(params != null ? params : Map.of());

    }



    @PostMapping("/monitor/visibility/refresh")

    public ResponseResult monitorVisibilityRefresh(@RequestBody(required = false) Map<String, Object> body) {

        return geoServiceFeign.monitorVisibilityRefresh(body != null ? body : Map.of());

    }



    @GetMapping("/monitor/citations")

    public ResponseResult monitorCitations(@RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.monitorCitations(params != null ? params : Map.of());

    }



    @PostMapping("/monitor/citations/sync")

    public ResponseResult monitorCitationsSync(@RequestBody(required = false) Map<String, Object> body) {

        return geoServiceFeign.monitorCitationsSync(body != null ? body : Map.of());

    }



    @GetMapping("/monitor/questions")

    public ResponseResult monitorQuestions(@RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.monitorQuestions(params != null ? params : Map.of());

    }

    @GetMapping("/monitor/questions/sync-scope-preview")
    public ResponseResult monitorQuestionScopeSyncPreview(
            @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.monitorQuestionScopeSyncPreview(params != null ? params : Map.of());
    }

    @PostMapping("/monitor/questions/sync-scope")
    public ResponseResult monitorQuestionsFromScope(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.monitorQuestionsFromScope(body != null ? body : Map.of());
    }



    @PatchMapping("/monitor/questions/{id}")

    public ResponseResult monitorToggleQuestion(@PathVariable Long id, @RequestBody Map<String, Object> body) {

        return geoServiceFeign.monitorToggleQuestion(id, body);

    }



    @PostMapping("/monitor/questions/batch-monitor")

    public ResponseResult monitorBatchMonitor(@RequestBody Map<String, Object> body) {

        return geoServiceFeign.monitorBatchMonitor(body);

    }



    @PostMapping("/monitor/questions/import")

    public ResponseResult monitorImportQuestions(@RequestBody Map<String, Object> body) {

        return geoServiceFeign.monitorImportQuestions(body);

    }

    @PostMapping("/monitor/questions/to-task")
    public ResponseResult monitorQuestionsToTask(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.monitorQuestionsToTask(body != null ? body : Map.of());
    }



    @GetMapping("/monitor/competitor")

    public ResponseResult monitorCompetitor(@RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.monitorCompetitor(params != null ? params : Map.of());

    }

    @PostMapping("/monitor/competitor")
    public ResponseResult createMonitorCompetitor(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.createMonitorCompetitor(body);
    }

    @DeleteMapping("/monitor/competitor/{id}")
    public ResponseResult deleteMonitorCompetitor(
        @PathVariable Long id,
        @RequestParam(required = false) Map<String, Object> params
    ) {
        return geoServiceFeign.deleteMonitorCompetitor(id, params != null ? params : Map.of());
    }

    @PostMapping("/monitor/competitor/gaps/task")
    public ResponseResult monitorCompetitorGapToTask(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.monitorCompetitorGapToTask(body != null ? body : Map.of());
    }



    @GetMapping("/monitor/sentiment")

    public ResponseResult monitorSentiment(@RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.monitorSentiment(params != null ? params : Map.of());

    }



    @PatchMapping("/monitor/sentiment/alerts/{id}")
    public ResponseResult monitorCloseAlert(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.monitorCloseAlert(id, body != null ? body : Map.of());
    }

    @PatchMapping("/monitor/sentiment/hallucinations/{id}")
    public ResponseResult monitorResolveHallucination(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.monitorResolveHallucination(id, body != null ? body : Map.of());
    }

    @PostMapping("/monitor/questions/import-hot")
    public ResponseResult monitorImportHot(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.monitorImportHot(body);
    }



    @GetMapping("/dashboard/geo-kpis")

    public ResponseResult dashboardGeoKpis(@RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.dashboardGeoKpis(params != null ? params : Map.of());

    }

    @GetMapping("/dashboard/overview")
    public ResponseResult dashboardOverview(
            @RequestParam(required = false) Map<String, Object> params,
            @RequestParam(defaultValue = "7") int days) {
        return geoServiceFeign.dashboardOverview(params != null ? params : Map.of(), days);
    }

    @GetMapping("/media/resources")
    public ResponseResult mediaResources(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.mediaResources(params != null ? params : Map.of());
    }

    @PutMapping("/media/resources/{id}/favorite")
    public ResponseResult mediaResourceFavorite(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.mediaResourceFavorite(id, body != null ? body : Map.of());
    }

    @PostMapping("/media/resources/submit")
    public ResponseResult mediaResourceSubmit(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.mediaResourceSubmit(body != null ? body : Map.of());
    }

    @PostMapping("/media/resources/import")
    public ResponseResult mediaResourceImport(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.mediaResourceImport(body != null ? body : Map.of());
    }

    @PostMapping("/media/resources/sync-timus")
    public ResponseResult mediaResourceSyncTimus(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.mediaResourceSyncTimus(body != null ? body : Map.of());
    }

    @PostMapping("/media/resources/sync-timus-full")
    public ResponseResult mediaResourceSyncTimusFull(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.mediaResourceSyncTimusFull(body != null ? body : Map.of());
    }

    @GetMapping("/media/resources/sync-timus-full/{jobId}")
    public ResponseResult mediaResourceSyncTimusFullStatus(
            @PathVariable String jobId,
            @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.mediaResourceSyncTimusFullStatus(jobId, params != null ? params : Map.of());
    }



    /** --- Schema --- */

    @GetMapping("/dashboard")

    public ResponseResult dashboard(@RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.dashboard(params != null ? params : Map.of());

    }



    @GetMapping("/templates")

    public ResponseResult templateList(@RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.templateList(params != null ? params : Map.of());

    }



    @GetMapping("/templates/{id}")

    public ResponseResult templateDetail(@PathVariable Long id,
                                         @RequestParam(value = "projectId", required = false) Long projectId) {

        return geoServiceFeign.templateDetail(id, projectId);

    }



    @PostMapping("/templates")

    public ResponseResult templateCreate(@RequestBody Map<String, Object> body) {

        return geoServiceFeign.templateCreate(body);

    }



    @PutMapping("/templates/{id}")

    public ResponseResult templateUpdate(@PathVariable Long id, @RequestBody Map<String, Object> body) {

        return geoServiceFeign.templateUpdate(id, body);

    }



    @DeleteMapping("/templates/{id}")
    public ResponseResult templateDelete(@PathVariable Long id,
                                         @RequestParam(value = "projectId", required = false) Long projectId) {
        return geoServiceFeign.templateDelete(id, projectId);
    }

    @PostMapping("/templates/{id}/copy")
    public ResponseResult templateCopy(@PathVariable Long id,
                                       @RequestParam(value = "projectId", required = false) Long projectId) {
        return geoServiceFeign.templateCopy(id, projectId);
    }

    @GetMapping("/templates/{id}/preview")
    public ResponseResult templatePreview(@PathVariable Long id,
                                          @RequestParam(value = "projectId", required = false) Long projectId) {
        return geoServiceFeign.templatePreview(id, projectId);
    }



    @GetMapping("/pages")

    public ResponseResult pageList(@RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.pageList(params != null ? params : Map.of());

    }



    @GetMapping("/pages/{id}/schema")

    public ResponseResult pageSchema(@PathVariable Long id,
                                     @RequestParam(value = "projectId", required = false) Long projectId) {

        return geoServiceFeign.pageSchema(id, projectId);

    }



    @PostMapping("/pages/{id}/schema")

    public ResponseResult savePageSchema(@PathVariable Long id, @RequestBody Map<String, Object> body) {

        return geoServiceFeign.savePageSchema(id, body);

    }



    @PostMapping("/validate")
    public ResponseResult validateJsonLd(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.validateJsonLd(body);
    }

    @PostMapping("/pages/batch")
    public ResponseResult batchPages(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.batchPages(body);
    }

    @PostMapping("/pages/import-sitemap")
    public ResponseResult importSitemap(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.importSitemap(body);
    }



    /** --- 研究 / 分析 / 内容 --- */

    @GetMapping("/research/plate-a/overview")
    public ResponseResult researchPlateAOverview(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.researchPlateAOverview(params != null ? params : Map.of());
    }

    @PostMapping("/research/auto-complete")
    public ResponseResult researchAutoComplete(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.researchAutoComplete(body != null ? body : Map.of());
    }

    @GetMapping("/research/auto-complete")
    public ResponseResult researchAutoCompleteStatus(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.researchAutoCompleteStatus(params != null ? params : Map.of());
    }

    @GetMapping("/research/keywords")
    public ResponseResult researchKeywords(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.researchKeywords(params != null ? params : Map.of());
    }

    @PostMapping("/research/keywords/discover")
    public ResponseResult researchDiscover(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.researchDiscover(body);
    }

    @PostMapping("/research/keywords/competitor-gap")
    public ResponseResult researchCompetitorGap(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.researchCompetitorGap(body != null ? body : Map.of());
    }

    @GetMapping("/research/keywords/{id}/trend")
    public ResponseResult researchKeywordTrend(@PathVariable Long id, @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.researchKeywordTrend(id, params != null ? params : Map.of());
    }

    @GetMapping("/research/keywords/i18n-map")
    public ResponseResult researchI18nMap(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.researchI18nMap(params != null ? params : Map.of());
    }

    @PostMapping("/research/keywords/export")
    public ResponseResult researchExportKeywords(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.researchExportKeywords(body != null ? body : Map.of());
    }

    @PostMapping("/research/keywords/import")
    public ResponseResult researchImportKeywords(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.researchImportKeywords(body);
    }

    @PostMapping("/research/conversions/preview")
    public ResponseResult researchConversionPreview(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.researchConversionPreview(body);
    }

    @PostMapping("/research/conversions/commit")
    public ResponseResult researchConversionCommit(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.researchConversionCommit(body);
    }



    @GetMapping("/research/topic-clusters")
    public ResponseResult researchTopicClusters(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.researchTopicClusters(params != null ? params : Map.of());
    }

    @PostMapping("/research/topic-clusters/import")
    public ResponseResult researchImportClusters() {
        return geoServiceFeign.researchImportClusters();
    }

    @PostMapping("/research/topic-clusters")
    public ResponseResult researchCreateCluster(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.researchCreateCluster(body);
    }

    @PutMapping("/research/topic-clusters/{id}")
    public ResponseResult researchUpdateCluster(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.researchUpdateCluster(id, body);
    }

    @PostMapping("/research/topic-clusters/{id}/move-keywords")
    public ResponseResult researchMoveClusterKeywords(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.researchMoveClusterKeywords(id, body);
    }

    @PostMapping("/research/topic-clusters/merge")
    public ResponseResult researchMergeClusters(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.researchMergeClusters(body);
    }

    @DeleteMapping("/research/topic-clusters/{id}")
    public ResponseResult researchDeleteCluster(@PathVariable Long id,
                                                @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.researchDeleteCluster(id, params != null ? params : Map.of());
    }

    @PostMapping("/research/topic-clusters/pillar-plan")
    public ResponseResult researchTopicPillarPlan(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.researchTopicPillarPlan(body != null ? body : Map.of());
    }

    @PostMapping("/research/topic-clusters/subscribe")
    public ResponseResult researchTopicSubscribe(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.researchTopicSubscribe(body);
    }

    @GetMapping("/research/intent")
    public ResponseResult researchIntent(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.researchIntent(params != null ? params : Map.of());
    }

    @PostMapping("/research/intent/scan")
    public ResponseResult researchIntentScan(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.researchIntentScan(body);
    }

    @PostMapping("/research/intent/import-questions")
    public ResponseResult researchImportIntentQuestions() {
        return geoServiceFeign.researchImportIntentQuestions();
    }



    @GetMapping("/analysis/models")

    public ResponseResult analysisModels(@RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.analysisModels(params != null ? params : Map.of());

    }



    @GetMapping("/analysis/models/{id}/diagnosis")
    public ResponseResult analysisModelDiagnosis(@PathVariable String id,
                                                  @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.analysisModelDiagnosis(id, params != null ? params : Map.of());
    }

    @PutMapping("/analysis/models/{id}/subscription")
    public ResponseResult updateAnalysisModelSubscription(@PathVariable String id,
                                                           @RequestBody Map<String, Object> body) {
        return geoServiceFeign.updateAnalysisModelSubscription(id, body);
    }

    @GetMapping("/models")
    public ResponseResult modelsAlias(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.modelsAlias(params != null ? params : Map.of());
    }

    @GetMapping("/models/{id}/diagnosis")
    public ResponseResult modelsDiagnosisAlias(@PathVariable String id,
                                               @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.modelsDiagnosisAlias(id, params != null ? params : Map.of());
    }

    @PutMapping("/models/{id}/subscription")
    public ResponseResult updateModelsSubscriptionAlias(@PathVariable String id,
                                                         @RequestBody Map<String, Object> body) {
        return geoServiceFeign.updateModelsSubscriptionAlias(id, body);
    }

    @PostMapping("/models/{id}/probe")
    public ResponseResult probeModelIntegration(@PathVariable String id,
                                                 @RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.probeModelIntegration(id, body != null ? body : Map.of());
    }

    @PutMapping("/models/{id}/registration")
    public ResponseResult saveModelRegistration(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.saveModelRegistration(id, body);
    }

    @PutMapping("/models/{id}/registration/status")
    public ResponseResult updateModelRegistrationStatus(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.updateModelRegistrationStatus(id, body);
    }

    @GetMapping("/analysis/source")
    public ResponseResult analysisSource(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.analysisSource(params != null ? params : Map.of());
    }

    @GetMapping("/analysis/health")
    public ResponseResult analysisHealth(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.analysisHealth(params != null ? params : Map.of());
    }

    @GetMapping("/reports")
    public ResponseResult reportsAlias(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.reportsAlias(params != null ? params : Map.of());
    }

    @PostMapping("/reports")
    public ResponseResult generateReportAlias(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.generateReportAlias(body != null ? body : Map.of());
    }

    @GetMapping("/intent/coverage")
    public ResponseResult intentCoverageAlias(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.intentCoverageAlias(params != null ? params : Map.of());
    }



    @GetMapping("/strategy/authority")
    public ResponseResult strategyAuthority(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.strategyAuthority(params != null ? params : Map.of());
    }

    @PostMapping("/strategy/authority/scan")
    public ResponseResult strategyAuthorityScan(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.strategyAuthorityScan(body);
    }

    @PatchMapping("/strategy/authority/citations/{id}/adopt")
    public ResponseResult strategyAuthorityCitationAdopt(@PathVariable Long id,
                                                         @RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.strategyAuthorityCitationAdopt(id, body != null ? body : Map.of());
    }



    @GetMapping("/strategy/model-adapt")
    public ResponseResult strategyModelAdapt(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.strategyModelAdapt(params != null ? params : Map.of());
    }

    @PostMapping("/strategy/model-adapt/sandbox")
    public ResponseResult strategySandbox(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.strategySandbox(body);
    }

    @PostMapping("/strategy/model-adapt/format")
    public ResponseResult strategyFormat(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.strategyFormat(body);
    }

    @PostMapping("/strategy/howto/generate")
    public ResponseResult strategyHowToGenerate(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.strategyHowToGenerate(body);
    }



    @GetMapping("/content/ai-studio")
    public ResponseResult contentAiStudio(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.contentAiStudio(params != null ? params : Map.of());
    }

    @PostMapping("/content/ai-studio/generate")
    public ResponseResult contentGenerate(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentGenerate(body);
    }

    @PostMapping("/content/ai-studio/generate-by-keyword")
    public ResponseResult contentGenerateByKeyword(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentGenerateByKeyword(body);
    }

    @PostMapping("/content/ai-studio/generate-batch")
    public ResponseResult contentGenerateBatch(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentGenerateBatch(body);
    }

    @GetMapping("/content/ai-studio/generate-batch/active")
    public ResponseResult contentGenerateBatchActive(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.contentGenerateBatchActive(params != null ? params : Map.of());
    }

    @GetMapping("/content/ai-studio/generate-batch/{jobId}")
    public ResponseResult contentGenerateBatchDetail(@PathVariable("jobId") String jobId) {
        return geoServiceFeign.contentGenerateBatchDetail(jobId);
    }

    @PostMapping("/content/ai-studio/platform-preview")
    public ResponseResult contentPlatformPreview(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentPlatformPreview(body);
    }

    @PostMapping("/content/ai-studio/advance-to-schedule-pool")
    public ResponseResult contentAdvanceToSchedulePool(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentAdvanceToSchedulePool(body);
    }

    @PostMapping("/content/ai-studio/keywords/expand")
    public ResponseResult contentExpandKeywords(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentExpandKeywords(body);
    }

    @PostMapping("/content/ai-studio/seed")
    public ResponseResult contentSaveAiStudioSeed(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentSaveAiStudioSeed(body);
    }

    @PostMapping("/content/ai-studio/video-script")
    public ResponseResult contentVideoScript(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentVideoScript(body);
    }

    @PostMapping("/content/ai-studio/style")
    public ResponseResult contentStyle(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentStyle(body);
    }

    @PostMapping("/content/ai-studio/i18n")
    public ResponseResult contentI18n(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentI18n(body);
    }




    @GetMapping("/content/expand-modes")
    public ResponseResult contentExpandModes(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.contentExpandModes(params != null ? params : Map.of());
    }

    @PostMapping("/content/expand-modes/refresh")
    public ResponseResult contentRefreshExpandModes(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.contentRefreshExpandModes(body != null ? body : Map.of());
    }
    @GetMapping("/content/multimodal")
    public ResponseResult contentMultimodal(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.contentMultimodal(params != null ? params : Map.of());
    }

    @PostMapping("/content/multimodal/visual")
    public ResponseResult contentVisual(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentVisual(body);
    }

    @PostMapping("/content/multimodal/visual/{id}/complete")
    public ResponseResult contentVisualComplete(@PathVariable Long id) {
        return geoServiceFeign.contentVisualComplete(id);
    }

    @PostMapping("/content/multimodal/tts")
    public ResponseResult contentTts(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentTts(body);
    }

    @PostMapping("/content/multimodal/avatar")
    public ResponseResult contentAvatar(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentAvatar(body);
    }

    @PostMapping("/content/multimodal/chart")
    public ResponseResult contentChart(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentChart(body);
    }

    @GetMapping("/content/viral-replica")
    public ResponseResult contentViralReplica(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.contentViralReplica(params != null ? params : Map.of());
    }

    @PostMapping("/content/viral-replica/prompt")
    public ResponseResult contentViralReplicaPrompt(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentViralReplicaPrompt(body);
    }

    @PostMapping("/content/viral-replica/replicate")
    public ResponseResult contentViralReplicaReplicate(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentViralReplicaReplicate(body);
    }

    @DeleteMapping("/content/viral-replica/{id}")
    public ResponseResult contentViralReplicaDelete(
        @PathVariable Long id,
        @RequestParam(required = false) Map<String, Object> params
    ) {
        return geoServiceFeign.contentViralReplicaDelete(id, params != null ? params : Map.of());
    }



    @GetMapping("/content/assets")
    public ResponseResult contentAssets(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.contentAssets(params != null ? params : Map.of());
    }

    @PostMapping("/content/assets")
    public ResponseResult contentCreateAsset(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentCreateAsset(body);
    }

    @PutMapping("/content/assets/{id}")
    public ResponseResult contentUpdateAsset(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentUpdateAsset(id, body);
    }

    @DeleteMapping("/content/assets/{id}")
    public ResponseResult contentDeleteAsset(@PathVariable Long id,
                                             @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.contentDeleteAsset(id, params != null ? params : Map.of());
    }

    @PostMapping(value = "/content/assets/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseResult uploadContentAsset(@RequestPart("file") MultipartFile file,
                                             @RequestParam("projectId") Long projectId,
                                             @RequestParam(value = "name", required = false) String name,
                                             @RequestParam(value = "tags", required = false) String tags) {
        return geoServiceFeign.uploadContentAsset(file, projectId, name, tags);
    }

    @GetMapping("/content/assets/generation-candidates")
    public ResponseResult assetGenerationCandidates(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.assetGenerationCandidates(params != null ? params : Map.of());
    }

    @PostMapping("/content/assets/import-generated")
    public ResponseResult importGeneratedAssets(@RequestBody Map<String, Object> body) { return geoServiceFeign.importGeneratedAssets(body); }

    @GetMapping("/content/assets/{assetId}/versions")
    public ResponseResult assetVersions(@PathVariable Long assetId, @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.assetVersions(assetId, params != null ? params : Map.of());
    }

    @PostMapping("/content/assets/{assetId}/versions")
    public ResponseResult createExplicitAssetVersion(@PathVariable Long assetId, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.createExplicitAssetVersion(assetId, body);
    }

    @PostMapping("/content/assets/{assetId}/versions/{versionId}/rollback")
    public ResponseResult rollbackAssetVersion(@PathVariable Long assetId, @PathVariable Long versionId, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.rollbackAssetVersion(assetId, versionId, body);
    }

    @PostMapping("/content/assets/freshness-alerts/{id}/ignore")
    public ResponseResult ignoreAssetFreshnessAlert(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.ignoreAssetFreshnessAlert(id, body);
    }

    @PostMapping("/content/assets/versions")
    public ResponseResult contentAssetVersion(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentAssetVersion(body);
    }

    @PostMapping("/content/assets/freshness-scan")
    public ResponseResult contentFreshnessScan(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.contentFreshnessScan(body != null ? body : Map.of());
    }



    @GetMapping("/links/backlinks")
    public ResponseResult linksBacklinks(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.linksBacklinks(params != null ? params : Map.of());
    }

    @GetMapping("/links/outreach")
    public ResponseResult linksOutreach(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.linksOutreach(params != null ? params : Map.of());
    }

    @GetMapping("/links/internal")
    public ResponseResult linksInternalAlias(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.linksInternalAlias(params != null ? params : Map.of());
    }

    @GetMapping("/links/internal-links")
    public ResponseResult linksInternal(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.linksInternal(params != null ? params : Map.of());
    }

    @PostMapping("/links/backlinks")
    public ResponseResult addBacklink(@RequestBody Map<String, Object> body) { return geoServiceFeign.addBacklink(body); }

    @PostMapping("/links/outreach")
    public ResponseResult createLinkOutreach(@RequestBody Map<String, Object> body) { return geoServiceFeign.createLinkOutreach(body); }

    @PostMapping("/links/internal-links")
    public ResponseResult addInternalLinkAudit(@RequestBody Map<String, Object> body) { return geoServiceFeign.addInternalLinkAudit(body); }

    @PostMapping("/links/backlinks/export")
    public ResponseResult exportBacklinks(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.exportBacklinks(params != null ? params : Map.of()); }

    @PostMapping("/links/outreach/export")
    public ResponseResult exportLinkOutreach(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.exportLinkOutreach(params != null ? params : Map.of()); }

    @PostMapping("/links/internal-links/export")
    public ResponseResult exportInternalLinks(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.exportInternalLinks(params != null ? params : Map.of()); }



    @GetMapping("/ranking/ai-engines")
    public ResponseResult rankingAiEngines(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.rankingAiEngines(params != null ? params : Map.of());
    }

    @GetMapping("/ranking/search-ai-features")
    public ResponseResult rankingSearchAiFeatures(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.rankingSearchAiFeatures(params != null ? params : Map.of());
    }

    @PostMapping("/ranking/refresh")
    public ResponseResult refreshRanking(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.refreshRanking(body);
    }



    @GetMapping("/experiments/ab-tests")
    public ResponseResult experimentsAbTests(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.experimentsAbTests(params != null ? params : Map.of());
    }

    @GetMapping("/experiments/attribution")
    public ResponseResult experimentsAttribution(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.experimentsAttribution(params != null ? params : Map.of());
    }

    @PostMapping("/experiments/ab-tests")
    public ResponseResult createExperiment(@RequestBody Map<String, Object> body) { return geoServiceFeign.createExperiment(body); }

    @PostMapping("/experiments/ab-tests/run")
    public ResponseResult runExperiment(@RequestBody Map<String, Object> body) { return geoServiceFeign.runExperiment(body); }

    @DeleteMapping("/experiments/ab-tests/{id}")
    public ResponseResult deleteExperiment(@PathVariable Long id, @RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.deleteExperiment(id, params != null ? params : Map.of()); }



    @GetMapping("/local")
    public ResponseResult localAlias(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.localAlias(params != null ? params : Map.of());
    }

    @GetMapping("/local/overview")
    public ResponseResult localOverview(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.localOverview(params != null ? params : Map.of());
    }

    @GetMapping("/local/gbp")
    public ResponseResult localGbp(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.localGbp(params != null ? params : Map.of());
    }

    @GetMapping("/local/china-maps")
    public ResponseResult localChinaMaps(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.localChinaMaps(params != null ? params : Map.of());
    }

    @GetMapping("/local/nap")
    public ResponseResult localNap(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.localNap(params != null ? params : Map.of());
    }

    @GetMapping("/local/nap/report")
    public ResponseResult localNapReport(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.localNapReport(params != null ? params : Map.of());
    }

    @GetMapping("/local/ai-summary")
    public ResponseResult localAiSummary(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.localAiSummary(params != null ? params : Map.of());
    }

    @PostMapping("/local/gbp/refresh")
    public ResponseResult refreshLocalGbp(@RequestBody Map<String, Object> body) { return geoServiceFeign.refreshLocalGbp(body); }

    @PostMapping("/local/china-maps/import")
    public ResponseResult importLocalChinaMaps(@RequestBody Map<String, Object> body) { return geoServiceFeign.importLocalChinaMaps(body); }

    @PostMapping("/local/nap/check")
    public ResponseResult checkLocalNap(@RequestBody Map<String, Object> body) { return geoServiceFeign.checkLocalNap(body); }

    @PostMapping("/local/ai-summary/refresh")
    public ResponseResult refreshLocalAiSummary(@RequestParam(required = false) Map<String, Object> params) { return geoServiceFeign.refreshLocalAiSummary(params != null ? params : Map.of()); }

    @GetMapping("/local/stores") public ResponseResult localStores(@RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.localStores(p != null ? p : Map.of()); }
    @GetMapping("/local/stores/export") public ResponseEntity<byte[]> exportLocalStores(@RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.exportLocalStores(p != null ? p : Map.of()); }
    @PostMapping("/local/stores") public ResponseResult createLocalStore(@RequestBody Map<String, Object> b) { return geoServiceFeign.createLocalStore(b); }
    @PutMapping("/local/stores/{id}") public ResponseResult updateLocalStore(@PathVariable Long id, @RequestBody Map<String, Object> b) { return geoServiceFeign.updateLocalStore(id, b); }
    @DeleteMapping("/local/stores/{id}") public ResponseResult deleteLocalStore(@PathVariable Long id, @RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.deleteLocalStore(id, p != null ? p : Map.of()); }
    @PostMapping("/local/stores/import") public ResponseResult importLocalStores(@RequestBody Map<String, Object> b) { return geoServiceFeign.importLocalStores(b); }
    @GetMapping("/local/bindings") public ResponseResult localBindings(@RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.localBindings(p != null ? p : Map.of()); }
    @GetMapping("/local/bindings/candidates") public ResponseResult searchLocalBindingCandidates(@RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.searchLocalBindingCandidates(p != null ? p : Map.of()); }
    @PostMapping("/local/bindings") public ResponseResult saveLocalBinding(@RequestBody Map<String, Object> b) { return geoServiceFeign.saveLocalBinding(b); }
    @PutMapping("/local/bindings/{id}") public ResponseResult updateLocalBinding(@PathVariable Long id, @RequestBody Map<String, Object> b) { return geoServiceFeign.updateLocalBinding(id, b); }
    @DeleteMapping("/local/bindings/{id}") public ResponseResult deleteLocalBinding(@PathVariable Long id, @RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.deleteLocalBinding(id, p != null ? p : Map.of()); }
    @GetMapping("/local/providers/status") public ResponseResult localProviderStatus(@RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.localProviderStatus(p != null ? p : Map.of()); }
    @PostMapping("/local/sync-jobs") public ResponseResult createLocalSyncJob(@RequestBody Map<String, Object> b) { return geoServiceFeign.createLocalSyncJob(b); }
    @GetMapping("/local/sync-jobs") public ResponseResult localSyncJobs(@RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.localSyncJobs(p != null ? p : Map.of()); }
    @GetMapping("/local/sync-jobs/{id}") public ResponseResult localSyncJob(@PathVariable Long id, @RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.localSyncJob(id, p != null ? p : Map.of()); }
    @PostMapping("/local/sync-jobs/{id}/retry") public ResponseResult retryLocalSyncJob(@PathVariable Long id, @RequestBody Map<String, Object> b) { return geoServiceFeign.retryLocalSyncJob(id, b); }
    @PostMapping("/local/gbp/import") public ResponseResult importLocalGbp(@RequestBody Map<String, Object> b) { return geoServiceFeign.importLocalGbp(b); }
    @PostMapping("/local/nap/audits") public ResponseResult createLocalNapAudit(@RequestBody Map<String, Object> b) { return geoServiceFeign.createLocalNapAudit(b); }
    @GetMapping("/local/nap/audits") public ResponseResult localNapAudits(@RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.localNapAudits(p != null ? p : Map.of()); }
    @GetMapping("/local/nap/issues") public ResponseResult localNapIssues(@RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.localNapIssues(p != null ? p : Map.of()); }
    @PostMapping("/local/nap/issues/approve") public ResponseResult approveLocalNapIssues(@RequestBody Map<String, Object> b) { return geoServiceFeign.approveLocalNapIssues(b); }
    @PostMapping("/local/nap/issues/reject") public ResponseResult rejectLocalNapIssues(@RequestBody Map<String, Object> b) { return geoServiceFeign.rejectLocalNapIssues(b); }
    @PostMapping("/local/nap/issues/{id}/recheck") public ResponseResult recheckLocalNapIssue(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> b) { return geoServiceFeign.recheckLocalNapIssue(id, b != null ? b : Map.of()); }
    @GetMapping("/local/nap/issues/export") public ResponseResult exportLocalNapIssues(@RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.exportLocalNapIssues(p != null ? p : Map.of()); }
    @GetMapping("/local/monitor/queries") public ResponseResult localMonitorQueries(@RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.localMonitorQueries(p != null ? p : Map.of()); }
    @PostMapping("/local/monitor/queries") public ResponseResult createLocalMonitorQuery(@RequestBody Map<String, Object> b) { return geoServiceFeign.createLocalMonitorQuery(b); }
    @PutMapping("/local/monitor/queries/{id}") public ResponseResult updateLocalMonitorQuery(@PathVariable Long id, @RequestBody Map<String, Object> b) { return geoServiceFeign.updateLocalMonitorQuery(id, b); }
    @DeleteMapping("/local/monitor/queries/{id}") public ResponseResult deleteLocalMonitorQuery(@PathVariable Long id, @RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.deleteLocalMonitorQuery(id, p != null ? p : Map.of()); }
    @PostMapping("/local/monitor/queries/import") public ResponseResult importLocalMonitorQueries(@RequestBody Map<String, Object> b) { return geoServiceFeign.importLocalMonitorQueries(b); }
    @GetMapping("/local/monitor/models") public ResponseResult localMonitorModels(@RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.localMonitorModels(p != null ? p : Map.of()); }
    @GetMapping("/local/monitor/plan") public ResponseResult localMonitorPlan(@RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.localMonitorPlan(p != null ? p : Map.of()); }
    @PutMapping("/local/monitor/plan") public ResponseResult saveLocalMonitorPlan(@RequestBody Map<String, Object> b) { return geoServiceFeign.saveLocalMonitorPlan(b); }
    @GetMapping("/local/monitor/runs") public ResponseResult localMonitorRuns(@RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.localMonitorRuns(p != null ? p : Map.of()); }
    @PostMapping("/local/monitor/runs") public ResponseResult runLocalMonitor(@RequestBody Map<String, Object> b) { return geoServiceFeign.runLocalMonitor(b); }
    @GetMapping("/local/monitor/runs/{id}") public ResponseResult localMonitorRun(@PathVariable Long id, @RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.localMonitorRun(id, p != null ? p : Map.of()); }
    @PostMapping("/local/monitor/runs/{id}/retry") public ResponseResult retryLocalMonitorRun(@PathVariable Long id, @RequestBody Map<String, Object> b) { return geoServiceFeign.retryLocalMonitorRun(id, b); }
    @PostMapping("/local/monitor/results/import") public ResponseResult importLocalMonitorResults(@RequestBody Map<String, Object> b) { return geoServiceFeign.importLocalMonitorResults(b); }
    @GetMapping("/local/monitor/results") public ResponseResult localMonitorResults(@RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.localMonitorResults(p != null ? p : Map.of()); }
    @GetMapping("/local/monitor/results/{id}") public ResponseResult localMonitorResult(@PathVariable Long id, @RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.localMonitorResult(id, p != null ? p : Map.of()); }
    @PostMapping("/local/monitor/results/{id}/workflow-task") public ResponseResult createLocalMonitorWorkflowTask(@PathVariable Long id, @RequestBody Map<String, Object> b) { return geoServiceFeign.createLocalMonitorWorkflowTask(id, b); }
    @GetMapping("/local/monitor/trends") public ResponseResult localMonitorTrends(@RequestParam(required = false) Map<String, Object> p) { return geoServiceFeign.localMonitorTrends(p != null ? p : Map.of()); }



    @GetMapping("/technical/crawl-index")
    public ResponseResult technicalCrawlIndex(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.technicalCrawlIndex(params != null ? params : Map.of());
    }

    @GetMapping("/technical/site-health")
    public ResponseResult technicalSiteHealth(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.technicalSiteHealth(params != null ? params : Map.of());
    }

    @GetMapping("/technical/ai-discoverability")
    public ResponseResult technicalAiDiscoverability(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.technicalAiDiscoverability(params != null ? params : Map.of());
    }

    @GetMapping("/technical/crawlability")
    public ResponseResult technicalCrawlability(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.technicalCrawlability(params != null ? params : Map.of());
    }

    @GetMapping("/technical/semantic-html")
    public ResponseResult technicalSemanticHtml(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.technicalSemanticHtml(params != null ? params : Map.of());
    }

    @GetMapping("/technical/performance")
    public ResponseResult technicalPerformance(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.technicalPerformance(params != null ? params : Map.of());
    }

    @PostMapping("/technical/crawlability/js-check")
    public ResponseResult technicalJsCheck(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.technicalJsCheck(body);
    }

    @PostMapping("/technical/semantic-html/check")
    public ResponseResult technicalSemanticCheck(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.technicalSemanticCheck(body);
    }

    @PostMapping("/technical/performance/mobile")
    public ResponseResult technicalMobileCheck(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.technicalMobileCheck(body);
    }

    @PostMapping("/technical/performance/lighthouse")
    public ResponseResult technicalLighthouseCheck(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.technicalLighthouseCheck(body);
    }



    @GetMapping("/intelligence/crawler-policy")
    public ResponseResult intelligenceCrawlerPolicy(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.intelligenceCrawlerPolicy(params != null ? params : Map.of());
    }

    @GetMapping("/intelligence/policy/changelog")
    public ResponseResult intelligencePolicyChangelog(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.intelligencePolicyChangelog(params != null ? params : Map.of());
    }

    @GetMapping("/intelligence/industry")
    public ResponseResult intelligenceIndustry(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.intelligenceIndustry(params != null ? params : Map.of());
    }

    @PostMapping("/intelligence/policy/subscribe")
    public ResponseResult subscribeIntelligencePolicy(@RequestBody Map<String, Object> body) { return geoServiceFeign.subscribeIntelligencePolicy(body); }

    @PostMapping("/intelligence/industry/report/create")
    public ResponseResult createIntelligenceIndustryReport(@RequestBody Map<String, Object> body) { return geoServiceFeign.createIntelligenceIndustryReport(body); }



    @GetMapping("/workflow/tasks")

    public ResponseResult workflowTasks(@RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.workflowTasks(params != null ? params : Map.of());

    }

    @PostMapping("/workflow/tasks")
    public ResponseResult createWorkflowTask(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.createWorkflowTask(body);
    }

    @PutMapping("/workflow/tasks/{id}")
    public ResponseResult updateWorkflowTask(@PathVariable Long id,
                                             @RequestBody Map<String, Object> body) {
        return geoServiceFeign.updateWorkflowTask(id, body);
    }

    @DeleteMapping("/workflow/tasks/{id}")
    public ResponseResult deleteWorkflowTask(@PathVariable Long id,
                                             @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.deleteWorkflowTask(id, params != null ? params : Map.of());
    }



    @GetMapping("/workflow/team")

    public ResponseResult workflowTeam(@RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.workflowTeam(params != null ? params : Map.of());

    }



    @PostMapping("/workflow/team")

    public ResponseResult workflowAddTeamMember(@RequestBody Map<String, Object> body) {

        return geoServiceFeign.workflowAddTeamMember(body);

    }



    @DeleteMapping("/workflow/team/{memberId}")
    public ResponseResult workflowRemoveTeamMember(@PathVariable Long memberId,
                                                   @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.workflowRemoveTeamMember(memberId, params != null ? params : Map.of());
    }

    @PutMapping("/workflow/team/{memberId}/role")
    public ResponseResult updateTeamMemberRole(@PathVariable Long memberId,
                                               @RequestBody Map<String, Object> body) {
        return geoServiceFeign.updateTeamMemberRole(memberId, body);
    }



    @GetMapping("/hot-trend/list")
    public ResponseResult hotTrendList(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.hotTrendList(params != null ? params : Map.of());
    }

    @GetMapping("/hot-trend/{platform}")
    public ResponseResult hotTrendPlatform(@PathVariable String platform,
                                           @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.hotTrendPlatform(platform, params != null ? params : Map.of());
    }

    @PostMapping("/hot-trend/sync")
    public ResponseResult hotTrendSync(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.hotTrendSync(body);
    }

    @GetMapping("/hot-trend/sync-logs")
    public ResponseResult hotTrendSyncLogs(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.hotTrendSyncLogs(params != null ? params : Map.of());
    }

    @PostMapping("/hot-trend/to-questions")
    public ResponseResult hotTrendToQuestions(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.hotTrendToQuestions(body);
    }

    @PostMapping("/hot-trend/batch-question-bank")
    public ResponseResult hotTrendBatchQuestionBank(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.hotTrendBatchQuestionBank(body);
    }

    @GetMapping("/integrations/api")
    public ResponseResult integrationsApi(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.integrationsApi(params != null ? params : Map.of());
    }

    @GetMapping("/integrations/mcp/servers")
    public ResponseResult mcpServers(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.mcpServers(params != null ? params : Map.of());
    }

    @PostMapping("/integrations/mcp/servers")
    public ResponseResult saveMcpServer(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.saveMcpServer(body);
    }

    @DeleteMapping("/integrations/mcp/servers/{id}")
    public ResponseResult deleteMcpServer(@PathVariable Long id, @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.deleteMcpServer(id, params != null ? params : Map.of());
    }

    @PostMapping("/integrations/mcp/servers/{id}/test")
    public ResponseResult testMcpServer(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.testMcpServer(id, body != null ? body : Map.of());
    }



    @GetMapping("/open-api/keys")

    public ResponseResult openApiKeys(@RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.openApiKeys(params != null ? params : Map.of());

    }

    @PostMapping("/open-api/keys")
    public ResponseResult openApiKeysCreate(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.openApiKeysCreate(body);
    }

    @DeleteMapping("/open-api/keys/{id}")
    public ResponseResult openApiKeyRevoke(@PathVariable Long id,
                                           @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.openApiKeyRevoke(id, params != null ? params : Map.of());
    }



    @GetMapping("/settings/monitor")

    public ResponseResult settingsMonitor(@RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.settingsMonitor(params != null ? params : Map.of());

    }



    @PutMapping("/settings/monitor")

    public ResponseResult settingsMonitorSave(@RequestBody Map<String, Object> body) {

        return geoServiceFeign.settingsMonitorSave(body);

    }



    /** --- 分发 --- */

    @GetMapping("/distribute/" + DIST_PLATFORM)

    public ResponseResult distributeList(@PathVariable String platform,

                                         @RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.distributeList(platform, params != null ? params : Map.of());

    }



    /** 与 geo 服务一致：platform 排除 omni，id 仅数字，避免 GET …/omni/publish 把 publish 当 Long 解析 */
    @GetMapping("/distribute/" + DIST_PLATFORM + "/{id:\\d+}")

    public ResponseResult distributeDetail(@PathVariable String platform, @PathVariable Long id) {

        return geoServiceFeign.distributeDetail(platform, id);

    }



    @PostMapping("/distribute/" + DIST_PLATFORM)

    public ResponseResult distributeCreate(@PathVariable String platform, @RequestBody Map<String, Object> body) {

        return geoServiceFeign.distributeCreate(platform, body);

    }



    @PutMapping("/distribute/" + DIST_PLATFORM + "/{id:\\d+}")

    public ResponseResult distributeUpdate(@PathVariable String platform,

                                           @PathVariable Long id,

                                           @RequestBody Map<String, Object> body) {

        return geoServiceFeign.distributeUpdate(platform, id, body);

    }



    @DeleteMapping("/distribute/" + DIST_PLATFORM + "/{id:\\d+}")

    public ResponseResult distributeDelete(@PathVariable String platform, @PathVariable Long id) {

        return geoServiceFeign.distributeDelete(platform, id);

    }



    @PostMapping("/distribute/" + DIST_PLATFORM + "/publish")

    public ResponseResult distributePublish(@PathVariable String platform, @RequestBody Map<String, Object> body) {

        return geoServiceFeign.distributePublish(platform, body);

    }



    @PostMapping("/distribute/" + DIST_PLATFORM + "/geo-detect")

    public ResponseResult distributeGeoDetect(@PathVariable String platform, @RequestBody Map<String, Object> body) {

        return geoServiceFeign.distributeGeoDetect(platform, body);

    }



    @GetMapping("/distribute/" + DIST_PLATFORM + "/schedule-pool")

    public ResponseResult distributeSchedulePool(@PathVariable String platform,

                                                 @RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.distributeSchedulePool(platform, params != null ? params : Map.of());

    }



    @PostMapping("/distribute/" + DIST_PLATFORM + "/from-article")

    public ResponseResult distributeImportFromArticle(@PathVariable String platform,

                                                      @RequestBody Map<String, Object> body) {

        return geoServiceFeign.distributeImportFromArticle(platform, body);

    }



    @PostMapping("/distribute/" + DIST_PLATFORM + "/ai/topics")

    public ResponseResult distributeAiTopics(@PathVariable String platform, @RequestBody Map<String, Object> body) {

        return geoServiceFeign.distributeAiTopics(platform, body);

    }



    @PostMapping("/distribute/" + DIST_PLATFORM + "/ai/generate")

    public ResponseResult distributeAiGenerate(@PathVariable String platform, @RequestBody Map<String, Object> body) {

        return geoServiceFeign.distributeAiGenerate(platform, body);

    }



    @PostMapping("/distribute/" + DIST_PLATFORM + "/ai/adapt-tone")

    public ResponseResult distributeAiAdaptTone(@PathVariable String platform, @RequestBody Map<String, Object> body) {

        return geoServiceFeign.distributeAiAdaptTone(platform, body);

    }



    /** --- 平台授权 --- */

    @GetMapping("/platform-auth")

    public ResponseResult platformAuthList(@RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.platformAuthList(params != null ? params : Map.of());

    }

    @GetMapping("/platform-auth/accounts")
    public ResponseResult platformAuthAccounts(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.platformAuthAccounts(params != null ? params : Map.of());
    }

    @GetMapping("/settings/platform-auth")
    public ResponseResult settingsPlatformAuth(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.settingsPlatformAuthAlias(params != null ? params : Map.of());
    }

    @PostMapping("/settings/platform-auth")
    public ResponseResult settingsPlatformAuthCreate(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.settingsPlatformAuthCreateAlias(body);
    }



    @PostMapping("/platform-auth")

    public ResponseResult platformAuthCreate(@RequestBody Map<String, Object> body) {

        return geoServiceFeign.platformAuthCreate(body);

    }



    @DeleteMapping("/platform-auth/{id}")

    public ResponseResult platformAuthDelete(@PathVariable Long id,
                                             @RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.platformAuthDelete(id, params != null ? params : Map.of());

    }



    @PostMapping("/platform-auth/{id}/check")

    public ResponseResult platformAuthCheck(@PathVariable Long id,
                                            @RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.platformAuthCheck(id, params != null ? params : Map.of());

    }



    @PostMapping("/platform-auth/{id}/default")

    public ResponseResult platformAuthSetDefault(@PathVariable Long id,
                                                 @RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.platformAuthSetDefault(id, params != null ? params : Map.of());

    }



    @PostMapping("/platform-auth/{id}/refresh")

    public ResponseResult platformAuthRefresh(@PathVariable Long id,
                                              @RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.platformAuthRefresh(id, params != null ? params : Map.of());

    }



    @GetMapping("/platform-auth/{platform}/oauth-url")

    public ResponseResult platformAuthOauthUrl(@PathVariable String platform,
                                               @RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.platformAuthOauthUrl(platform, params != null ? params : Map.of());

    }



    @PostMapping("/platform-auth/{platform}/oauth-callback")

    public ResponseResult platformAuthOauthCallback(@PathVariable String platform, @RequestBody Map<String, Object> body) {

        return geoServiceFeign.platformAuthOauthCallback(platform, body);

    }

    @GetMapping("/platform-auth/{platform}/cookie-login-info")
    public ResponseResult platformAuthCookieLoginInfo(@PathVariable String platform,
                                                      @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.platformAuthCookieLoginInfo(platform, params != null ? params : Map.of());
    }

    @PostMapping("/platform-auth/{platform}/cookie-verify")
    public ResponseResult platformAuthCookieVerify(@PathVariable String platform, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.platformAuthCookieVerify(platform, body);
    }

    @PostMapping("/platform-auth/{platform}/cookie-bind")
    public ResponseResult platformAuthCookieBind(@PathVariable String platform, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.platformAuthCookieBind(platform, body);
    }

    @GetMapping("/platform-auth/{id}/permissions")
    public ResponseResult platformAuthPermissions(@PathVariable Long id,
                                                  @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.platformAuthPermissions(id, params != null ? params : Map.of());
    }

    @GetMapping("/platform-auth/{id}/auth-logs")
    public ResponseResult platformAuthAuthLogs(@PathVariable Long id,
                                               @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.platformAuthAuthLogs(id, params != null ? params : Map.of());
    }

    @PostMapping("/platform-auth/credentials")
    public ResponseResult platformAuthCredentialSave(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.platformAuthCredentialSave(body);
    }

    @GetMapping("/platform-auth/credentials")
    public ResponseResult platformAuthCredentials(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.platformAuthCredentials(params != null ? params : Map.of());
    }

    @DeleteMapping("/platform-auth/credentials/{id}")
    public ResponseResult platformAuthCredentialDelete(@PathVariable Long id,
                                                       @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.platformAuthCredentialDelete(id, params != null ? params : Map.of());
    }

    @PostMapping("/platform-auth/relogin")
    public ResponseResult platformAuthRelogin(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.platformAuthRelogin(body);
    }

    @GetMapping("/platform-auth/relogin/{sessionId}")
    public ResponseResult platformAuthReloginStatus(@PathVariable String sessionId,
                                                    @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.platformAuthReloginStatus(sessionId, params != null ? params : Map.of());
    }

    @DeleteMapping("/platform-auth/relogin/{sessionId}")
    public ResponseResult platformAuthReloginCancel(@PathVariable String sessionId,
                                                    @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.platformAuthReloginCancel(sessionId, params != null ? params : Map.of());
    }

    @PostMapping("/platform-auth/relogin/{sessionId}/sms/phone")
    public ResponseResult platformAuthReloginSmsPhone(@PathVariable String sessionId,
                                                      @RequestBody Map<String, Object> body) {
        return geoServiceFeign.platformAuthReloginSmsPhone(sessionId, body != null ? body : Map.of());
    }

    @PostMapping("/platform-auth/relogin/{sessionId}/sms/code")
    public ResponseResult platformAuthReloginSmsCode(@PathVariable String sessionId,
                                                     @RequestBody Map<String, Object> body) {
        return geoServiceFeign.platformAuthReloginSmsCode(sessionId, body != null ? body : Map.of());
    }

    @GetMapping("/platform-config/{platform}")

    public ResponseResult platformConfigGet(@PathVariable String platform, @RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.platformConfigGet(platform, params != null ? params : java.util.Collections.emptyMap());

    }



    @PutMapping("/platform-config/{platform}")

    public ResponseResult platformConfigSave(@PathVariable String platform, @RequestBody Map<String, Object> body) {

        return geoServiceFeign.platformConfigSave(platform, body);

    }



    @PostMapping("/platform-config/{platform}/test")

    public ResponseResult platformConfigTest(@PathVariable String platform, @RequestBody(required = false) Map<String, Object> body) {

        return geoServiceFeign.platformConfigTest(platform, body != null ? body : Map.of());

    }



    /** --- Omni --- */

    @PostMapping("/distribute/omni/preview")

    public ResponseResult omniPreview(@RequestBody Map<String, Object> body) {

        return geoServiceFeign.omniPreview(body);

    }



    @PostMapping("/distribute/omni/validate")

    public ResponseResult omniValidate(@RequestBody Map<String, Object> body) {

        return geoServiceFeign.omniValidate(body);

    }



    @PostMapping("/distribute/omni/publish")

    public ResponseResult omniPublish(@RequestBody Map<String, Object> body) {

        return geoServiceFeign.omniPublish(body);

    }



    @GetMapping("/distribute/omni/jobs")

    public ResponseResult omniJobs(@RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.omniJobs(params != null ? params : Map.of());

    }



    @GetMapping("/distribute/omni/jobs/{id}")

    public ResponseResult omniJobDetail(@PathVariable String id,
                                         @RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.omniJobDetail(id, params != null ? params : Map.of());

    }



    @PostMapping("/distribute/omni/jobs/{id}/retry")

    public ResponseResult omniJobRetry(@PathVariable String id, @RequestBody(required = false) Map<String, Object> body) {

        return geoServiceFeign.omniJobRetry(id, body != null ? body : Map.of());

    }

    @GetMapping("/distribute/omni/context")
    public ResponseResult omniContext(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.omniContext(params != null ? params : Map.of());
    }

    @GetMapping("/distribute/omni/drafts")
    public ResponseResult omniDrafts(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.omniDrafts(params != null ? params : Map.of());
    }

    @PostMapping("/distribute/omni/source/import-url")
    public ResponseResult omniImportSource(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.omniImportSource(body);
    }

    @GetMapping("/distribute/omni/jobs/{id}/logs")
    public ResponseResult omniJobLogs(@PathVariable String id,
                                      @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.omniJobLogs(id, params != null ? params : Map.of());
    }

    @GetMapping("/distribute/omni/fail-logs")
    public ResponseResult omniFailLogs(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.omniFailLogs(params != null ? params : Map.of());
    }

    @GetMapping("/risk/events")
    public ResponseResult riskEvents(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.riskEvents(params != null ? params : Map.of());
    }

    @PutMapping("/risk/events/{id}")
    public ResponseResult updateRiskEvent(@PathVariable String id, @RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.updateRiskEvent(id, body != null ? body : Map.of());
    }

    @GetMapping("/distribute/publish-plan/list")
    public ResponseResult publishPlanList(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.publishPlanList(params != null ? params : Map.of());
    }

    @GetMapping("/distribute/publish-plan/calendar")
    public ResponseResult publishPlanCalendar(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.publishPlanCalendar(params != null ? params : Map.of());
    }

    @GetMapping("/distribute/publish-plan/schedule-pool")
    public ResponseResult publishPlanSchedulePool(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.publishPlanSchedulePool(params != null ? params : Map.of());
    }

    @GetMapping("/distribute/publish-plan/platform-quotas")
    public ResponseResult publishPlanPlatformQuotas(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.publishPlanPlatformQuotas(params != null ? params : Map.of());
    }

    @GetMapping("/distribute/publish-plan/{id:\\d+}")
    public ResponseResult publishPlanDetail(@PathVariable Long id, @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.publishPlanDetail(id, params != null ? params : Map.of());
    }

    @PostMapping("/distribute/publish-plan/get-or-create")
    public ResponseResult publishPlanGetOrCreate(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.publishPlanGetOrCreate(body);
    }

    @PutMapping("/distribute/publish-plan/{id:\\d+}/items")
    public ResponseResult publishPlanSaveItems(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.publishPlanSaveItems(id, body);
    }

    @PostMapping("/distribute/publish-plan/{id:\\d+}/auto-fill")
    public ResponseResult publishPlanAutoFill(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.publishPlanAutoFill(id, body);
    }

    @PostMapping("/distribute/publish-plan/auto-fill-year")
    public ResponseResult publishPlanAutoFillYear(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.publishPlanAutoFillYear(body);
    }

    @PostMapping("/distribute/publish-plan/auto-fill-preview")
    public ResponseResult publishPlanAutoFillPreview(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.publishPlanAutoFillPreview(body);
    }

    @PostMapping("/distribute/publish-plan/{id:\\d+}/activate")
    public ResponseResult publishPlanActivate(@PathVariable Long id,
                                              @RequestParam(required = false) Map<String, Object> params,
                                              @RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.publishPlanActivate(id, mergeGeoBody(params, body));
    }

    @PostMapping("/distribute/publish-plan/activate-year")
    public ResponseResult publishPlanActivateYear(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.publishPlanActivateYear(body);
    }

    @PostMapping("/distribute/publish-plan/{id:\\d+}/generate-scheduled-send")
    public ResponseResult publishPlanGenerateScheduledSend(@PathVariable Long id,
                                                           @RequestParam(required = false) Map<String, Object> params,
                                                           @RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.publishPlanGenerateScheduledSend(id, mergeGeoBody(params, body));
    }

    @PostMapping("/distribute/publish-plan/{id:\\d+}/pause")
    public ResponseResult publishPlanPause(@PathVariable Long id,
                                           @RequestParam(required = false) Map<String, Object> params,
                                           @RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.publishPlanPause(id, mergeGeoBody(params, body));
    }

    @PostMapping("/distribute/publish-plan/tasks/{taskId:\\d+}/retry")
    public ResponseResult publishPlanRetryTask(@PathVariable Long taskId,
                                               @RequestParam(required = false) Map<String, Object> params,
                                               @RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.publishPlanRetryTask(taskId, mergeGeoBody(params, body));
    }

    @PutMapping("/distribute/publish-plan/platform-quotas")
    public ResponseResult publishPlanSavePlatformQuotas(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.publishPlanSavePlatformQuotas(body);
    }

    @GetMapping("/distribute/jobs")
    public ResponseResult distributeJobs(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.distributeJobsAlias(params != null ? params : Map.of());
    }

    @GetMapping("/distribute/jobs/{id}")
    public ResponseResult distributeJobDetail(@PathVariable String id, @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.distributeJobDetailAlias(id, params != null ? params : Map.of());
    }

    @PostMapping("/distribute/jobs/{id}/retry")
    public ResponseResult distributeJobRetry(@PathVariable String id, @RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.distributeJobRetryAlias(id, body != null ? body : Map.of());
    }

    @GetMapping("/account/profile")
    public ResponseResult accountProfile(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.accountProfile(params != null ? params : Map.of());
    }



    /** --- Tab / 工作流 / Webhook --- */

    @GetMapping("/page-tabs/{key}")

    public ResponseResult pageTab(@PathVariable String key,
                                  @RequestParam(value = "projectId", required = false) Long projectId) {

        return geoServiceFeign.pageTab(key, projectId, projectId);

    }



    @PostMapping("/page-tabs/{key}/refresh")

    public ResponseResult pageTabRefresh(@PathVariable String key,
                                         @RequestParam(value = "projectId", required = false) Long projectId,
                                         @RequestBody(required = false) Map<String, Object> body) {

        return geoServiceFeign.pageTabRefresh(key, projectId, projectId, body != null ? body : Map.of());

    }

    @PostMapping("/page-tabs/{key}/execute")
    public ResponseResult pageTabExecute(@PathVariable String key,
                                         @RequestParam(value = "projectId", required = false) Long projectId,
                                         @RequestBody Map<String, Object> body) {
        return geoServiceFeign.pageTabExecute(key, projectId, projectId, body);
    }



    @GetMapping("/workflow/projects")

    public ResponseResult workflowProjects(@RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.workflowProjects(params != null ? params : Map.of());

    }



    @PostMapping("/workflow/projects")

    public ResponseResult workflowCreateProject(@RequestBody Map<String, Object> body) {

        return geoServiceFeign.workflowCreateProject(body);

    }



    @PutMapping("/workflow/projects/{id}")

    public ResponseResult workflowUpdateProject(@PathVariable Long id, @RequestBody Map<String, Object> body) {

        return geoServiceFeign.workflowUpdateProject(id, body);

    }

    @DeleteMapping("/workflow/projects/{id}")
    public ResponseResult deleteWorkflowProject(@PathVariable Long id) {
        return geoServiceFeign.deleteWorkflowProject(id);
    }



    @GetMapping("/workflow/projects/{id}/brand-settings")

    public ResponseResult workflowBrandSettings(@PathVariable Long id,
                                                 @RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.workflowBrandSettings(id, params != null ? params : Map.of());

    }



    @PutMapping("/workflow/projects/{id}/brand-settings")

    public ResponseResult workflowSaveBrandSettings(@PathVariable Long id, @RequestBody Map<String, Object> body) {

        return geoServiceFeign.workflowSaveBrandSettings(id, body);

    }



    @GetMapping("/workflow/projects/{id}/monitor-scope")

    public ResponseResult workflowMonitorScope(@PathVariable Long id,
                                                @RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.workflowMonitorScope(id, params != null ? params : Map.of());

    }



    @PutMapping("/workflow/projects/{id}/monitor-scope")

    public ResponseResult workflowSaveMonitorScope(@PathVariable Long id, @RequestBody Map<String, Object> body) {

        return geoServiceFeign.workflowSaveMonitorScope(id, body);

    }



    @PostMapping("/workflow/projects/monitor-keywords/suggest")

    public ResponseResult suggestMonitorKeywords(@RequestBody Map<String, Object> body) {

        return geoServiceFeign.suggestMonitorKeywords(body);

    }



    @GetMapping("/open-api/webhooks")

    public ResponseResult webhookList(@RequestParam(required = false) Map<String, Object> params) {

        return geoServiceFeign.webhookList(params != null ? params : Map.of());

    }



    @PostMapping("/open-api/webhooks")

    public ResponseResult webhookSave(@RequestBody Map<String, Object> body) {

        return geoServiceFeign.webhookSave(body);

    }



    @DeleteMapping("/open-api/webhooks/{id}")

    public ResponseResult webhookDelete(@PathVariable Long id) {

        return geoServiceFeign.webhookDelete(id);

    }



    @PostMapping("/open-api/webhooks/test")
    public ResponseResult webhookTest(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.webhookTest(body != null ? body : Map.of());
    }

    @GetMapping("/goals/conversion")
    public ResponseResult goalsConversion(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.goalsConversion(params != null ? params : Map.of());
    }

    @PostMapping("/goals/conversion")
    public ResponseResult goalsSaveConversion(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.goalsSaveConversion(body);
    }

    @GetMapping("/goals/conversion-events")
    public ResponseResult conversionEvents(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.conversionEvents(params != null ? params : Map.of());
    }

    @GetMapping("/goals/conversion-events/summary")
    public ResponseResult conversionEventSummary(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.conversionEventSummary(params != null ? params : Map.of());
    }

    @GetMapping("/goals/conversion-events/{id}")
    public ResponseResult conversionEventDetail(@PathVariable Long id,
                                                @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.conversionEventDetail(id, params != null ? params : Map.of());
    }

    @PostMapping("/goals/conversion-events/{id}/contact/reveal")
    public ResponseResult revealConversionEventContact(@PathVariable Long id,
                                                       @RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.revealConversionEventContact(id, body != null ? body : Map.of());
    }

    @DeleteMapping("/goals/conversion-events/{id}")
    public ResponseResult deleteConversionEvent(@PathVariable Long id,
                                                @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.deleteConversionEvent(id, params != null ? params : Map.of());
    }

    @PostMapping(value = "/goals/conversion-events/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseResult importConversionEvents(@RequestPart("file") MultipartFile file,
                                                 @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.importConversionEvents(file, params != null ? params : Map.of());
    }

    @GetMapping("/goals/conversion-events/export")
    public ResponseEntity<byte[]> exportConversionEvents(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.exportConversionEvents(params != null ? params : Map.of());
    }

    /** 生成媒体（成片/配音）预览与下载：透传 geo 服务磁盘产物 */
    // 同 WebCommonController#serveFile：PathPatternParser 不支持 {var:regex}，
    // 写成 {fileName:.+} 会静默匹配不上，成片/配音预览下载一律 404。
    @GetMapping("/media/generated/{fileName}")
    public ResponseEntity<byte[]> generatedMedia(@PathVariable String fileName) {
        return geoServiceFeign.generatedMedia(fileName);
    }

    @GetMapping("/goals/conversion-tracking/config")
    public ResponseResult conversionTrackingConfig(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.conversionTrackingConfig(params != null ? params : Map.of());
    }

    @PutMapping("/goals/conversion-tracking/config")
    public ResponseResult saveConversionTrackingConfig(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.saveConversionTrackingConfig(body);
    }

    @PostMapping("/goals/conversion-tracking/config/token/rotate")
    public ResponseResult rotateConversionTrackingToken(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.rotateConversionTrackingToken(body);
    }

    @PostMapping("/goals/conversion-tracking/config/webhook-secret/rotate")
    public ResponseResult rotateConversionWebhookSecret(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.rotateConversionWebhookSecret(body);
    }

    @GetMapping("/indexing/reports")
    public ResponseResult indexingReports(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.indexingReports(params != null ? params : Map.of());
    }

    @PostMapping("/indexing/sync")
    public ResponseResult indexingSync(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.indexingSync(body != null ? body : Map.of());
    }

    @GetMapping("/indexing/reports/export")
    public ResponseResult indexingReportsExport(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.indexingReportsExport(params != null ? params : Map.of());
    }

    @GetMapping("/assets/images")
    public ResponseResult assetsImages(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.assetsImages(params != null ? params : Map.of());
    }

    @PostMapping("/assets/images")
    public ResponseResult assetsCreateImage(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.assetsCreateImage(body);
    }

    @PostMapping("/assets/images/batch")
    public ResponseResult assetsBatchCreateImages(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.assetsBatchCreateImages(body);
    }

    @PutMapping("/assets/images/{id}")
    public ResponseResult assetsUpdateImage(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.assetsUpdateImage(id, body);
    }

    @DeleteMapping("/assets/images/{id}")
    public ResponseResult assetsDeleteImage(@PathVariable Long id,
                                            @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.assetsDeleteImage(id, params != null ? params : Map.of());
    }

    @GetMapping("/assets/knowledge")
    public ResponseResult assetsKnowledge(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.assetsKnowledge(params != null ? params : Map.of());
    }

    @PostMapping("/assets/knowledge")
    public ResponseResult assetsCreateKnowledge(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.assetsCreateKnowledge(body);
    }

    @PutMapping("/assets/knowledge/{id:\\d+}")
    public ResponseResult assetsUpdateKnowledge(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.assetsUpdateKnowledge(id, body);
    }

    @DeleteMapping("/assets/knowledge/{id:\\d+}")
    public ResponseResult assetsDeleteKnowledge(@PathVariable Long id,
                                                 @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.assetsDeleteKnowledge(id, params != null ? params : Map.of());
    }

    @PutMapping("/assets/knowledge/{id:\\d+}/default-context")
    public ResponseResult assetsSetKnowledgeDefault(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.assetsSetKnowledgeDefault(id, body != null ? body : Map.of());
    }

    @PostMapping(value = "/assets/knowledge/import", consumes = "multipart/form-data")
    public ResponseResult assetsPreviewKnowledgeImport(
        @RequestPart("files") MultipartFile file,
        @RequestParam(required = false) String defaultCategory,
        @RequestParam(required = false) Long projectId
    ) {
        return geoServiceFeign.assetsPreviewKnowledgeImport(file, defaultCategory, projectId);
    }

    @PostMapping("/assets/knowledge/import/commit")
    public ResponseResult assetsCommitKnowledgeImport(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.assetsCommitKnowledgeImport(body);
    }

    @GetMapping("/assets/knowledge/{id:\\d+}/versions")
    public ResponseResult assetsKnowledgeVersions(@PathVariable Long id,
                                                  @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.assetsKnowledgeVersions(id, params != null ? params : Map.of());
    }

    @PostMapping("/assets/knowledge/{id:\\d+}/versions/{versionNo}/restore")
    public ResponseResult assetsRestoreKnowledgeVersion(@PathVariable Long id, @PathVariable Integer versionNo,
                                                        @RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.assetsRestoreKnowledgeVersion(id, versionNo, body != null ? body : Map.of());
    }

    @GetMapping("/assets/knowledge/tags")
    public ResponseResult assetsKnowledgeTags(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.assetsKnowledgeTags(params != null ? params : Map.of());
    }

    @PostMapping("/assets/knowledge/tags")
    public ResponseResult assetsSaveKnowledgeTag(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.assetsSaveKnowledgeTag(body);
    }

    @DeleteMapping("/assets/knowledge/tags/{id:\\d+}")
    public ResponseResult assetsDeleteKnowledgeTag(@PathVariable Long id,
                                                   @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.assetsDeleteKnowledgeTag(id, params != null ? params : Map.of());
    }

    @GetMapping("/assets/knowledge/{id:\\d+}/sharing")
    public ResponseResult assetsKnowledgeSharing(@PathVariable Long id,
                                                 @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.assetsKnowledgeSharing(id, params != null ? params : Map.of());
    }

    @PutMapping("/assets/knowledge/{id:\\d+}/sharing")
    public ResponseResult assetsSaveKnowledgeSharing(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.assetsSaveKnowledgeSharing(id, body);
    }

    @PostMapping("/assets/knowledge/batch")
    public ResponseResult assetsBatchKnowledge(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.assetsBatchKnowledge(body);
    }

    @GetMapping("/assets/knowledge/dify")
    public ResponseResult assetsDifyKnowledgeInfo() {
        return geoServiceFeign.assetsDifyKnowledgeInfo();
    }

    @GetMapping("/assets/knowledge/dify/documents")
    public ResponseResult assetsDifyDocuments(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.assetsDifyDocuments(params != null ? params : Map.of());
    }

    @GetMapping("/assets/knowledge/dify/documents/{documentId}")
    public ResponseResult assetsDifyDocument(@PathVariable String documentId,
                                             @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.assetsDifyDocument(documentId, params != null ? params : Map.of());
    }

    @PostMapping("/assets/knowledge/dify/sync")
    public ResponseResult assetsSyncDifyKnowledge(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.assetsSyncDifyKnowledge(body != null ? body : Map.of());
    }

    @PostMapping("/assets/knowledge/dify/push-manuals")
    public ResponseResult assetsPushBundledManualsToDify(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.assetsPushBundledManualsToDify(body != null ? body : Map.of());
    }

    @GetMapping("/content/article-settings")
    public ResponseResult contentArticleSettings(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.contentArticleSettings(params != null ? params : Map.of());
    }

    @PostMapping("/content/article-settings")
    public ResponseResult contentSaveArticleSettings(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentSaveArticleSettings(body);
    }

    @DeleteMapping("/content/article-settings/{id}")
    public ResponseResult contentDeleteArticleSettings(@PathVariable Long id) {
        return geoServiceFeign.contentDeleteArticleSettings(id);
    }

    @GetMapping("/content/articles")
    public ResponseResult contentArticles(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.contentArticles(params != null ? params : Map.of());
    }

    @GetMapping("/content/articles/{id}")
    public ResponseResult contentGetArticle(@PathVariable Long id,
                                            @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.contentGetArticle(id, params != null ? params : Map.of());
    }

    @PostMapping("/content/articles")
    public ResponseResult contentCreateArticle(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentCreateArticle(body);
    }

    @PutMapping("/content/articles/{id}")
    public ResponseResult contentUpdateArticle(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentUpdateArticle(id, body);
    }

    @PatchMapping("/content/articles/{id}/status")
    public ResponseResult contentUpdateArticleStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentUpdateArticleStatus(id, body);
    }

    @DeleteMapping("/content/articles/{id}")
    public ResponseResult contentDeleteArticle(@PathVariable Long id) {
        return geoServiceFeign.contentDeleteArticle(id);
    }

    @PostMapping("/content/articles/export")
    public ResponseResult contentExportArticles(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.contentExportArticles(body != null ? body : Map.of());
    }

    @PostMapping("/content/articles/import")
    public ResponseResult contentImportArticles(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentImportArticles(body);
    }

    @GetMapping("/research/base-keywords/stats")
    public ResponseResult baseKeywordStats(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.baseKeywordStats(params != null ? params : Map.of());
    }

    @GetMapping("/research/distill")
    public ResponseResult researchDistill(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.researchDistill(params != null ? params : Map.of());
    }

    @PostMapping("/research/distill")
    public ResponseResult researchCreateDistill(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.researchCreateDistill(body);
    }

    @PostMapping("/research/distill/expand")
    public ResponseResult researchDistillExpand(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.researchDistillExpand(body);
    }

    @PutMapping("/research/distill/{id}")
    public ResponseResult researchUpdateDistill(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.researchUpdateDistill(id, body);
    }

    @PatchMapping("/research/distill/{id}")
    public ResponseResult researchUpdateDistillPatch(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.researchUpdateDistill(id, body);
    }

    @PostMapping("/research/distill/batch-delete")
    public ResponseResult researchBatchDeleteDistill(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.researchBatchDeleteDistill(body);
    }

    @DeleteMapping("/research/distill/{id}")
    public ResponseResult researchDeleteDistill(@PathVariable Long id) {
        return geoServiceFeign.researchDeleteDistill(id);
    }

    @PostMapping("/research/distill/{id}/rerun")
    public ResponseResult researchRerunDistill(@PathVariable Long id) {
        return geoServiceFeign.researchRerunDistill(id);
    }

    @GetMapping("/research/distill/results")
    public ResponseResult researchDistillResults(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.researchDistillResults(params != null ? params : Map.of());
    }

    @DeleteMapping("/research/distill/results/{id}")
    public ResponseResult researchDeleteDistillResult(@PathVariable Long id) {
        return geoServiceFeign.researchDeleteDistillResult(id);
    }

    @PostMapping("/research/distill/results/merge")
    public ResponseResult researchMergeDistillResults(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.researchMergeDistillResults(body);
    }

    @GetMapping("/research/distill/results/export")
    public ResponseResult researchExportDistillResults(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.researchExportDistillResults(params != null ? params : Map.of());
    }

    @GetMapping("/commerce/products")
    public ResponseResult commerceProducts(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.commerceProducts(params != null ? params : Map.of());
    }

    @PostMapping("/commerce/products")
    public ResponseResult commerceSaveProduct(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.commerceSaveProduct(body);
    }

    @DeleteMapping("/commerce/products/{id}")
    public ResponseResult commerceDeleteProduct(@PathVariable Long id) {
        return geoServiceFeign.commerceDeleteProduct(id);
    }

    @GetMapping("/commerce/orders")
    public ResponseResult commerceOrders(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.commerceOrders(params != null ? params : Map.of());
    }

    @PostMapping("/commerce/orders")
    public ResponseResult commerceCreateOrder(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.commerceCreateOrder(body);
    }

    @PatchMapping("/commerce/orders/{id}")
    public ResponseResult commerceUpdateOrder(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.commerceUpdateOrder(id, body);
    }

    @GetMapping("/commerce/plans")
    public ResponseResult commercePlans(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.commercePlans(params != null ? params : Map.of());
    }

    @GetMapping("/commerce/upgrade")
    public ResponseResult commerceUpgrade(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.commerceUpgrade(params != null ? params : Map.of());
    }

    @GetMapping("/tools/keyword-index")
    public ResponseResult toolsKeywordIndex(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.toolsKeywordIndex(params != null ? params : Map.of());
    }

    @PostMapping("/tools/keyword-index/refresh")
    public ResponseResult toolsRefreshKeywordIndex(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.toolsRefreshKeywordIndex(body != null ? body : Map.of());
    }

    @GetMapping("/tools/landing-page")
    public ResponseResult toolsLandingPages(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.toolsLandingPages(params != null ? params : Map.of());
    }

    @GetMapping("/tools/landing-page/{id}")
    public ResponseResult toolsGetLandingPage(@PathVariable Long id,
                                              @RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.toolsGetLandingPage(id, params != null ? params : Map.of());
    }

    @PostMapping("/tools/landing-page")
    public ResponseResult toolsGenerateLandingPage(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.toolsGenerateLandingPage(body);
    }

    @DeleteMapping("/tools/landing-page/{id}")
    public ResponseResult toolsDeleteLandingPage(@PathVariable Long id) {
        return geoServiceFeign.toolsDeleteLandingPage(id);
    }

    @PostMapping("/tools/landing-page/{id}/publish")
    public ResponseResult toolsPublishLandingPage(@PathVariable Long id) {
        return geoServiceFeign.toolsPublishLandingPage(id);
    }

    @PutMapping("/tools/landing-page/{id}")
    public ResponseResult toolsUpdateLandingPage(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return geoServiceFeign.toolsUpdateLandingPage(id, body);
    }

    @GetMapping("/tools/landing-page/{id}/share")
    public ResponseResult toolsGetLandingPageShareInfo(@PathVariable Long id) {
        return geoServiceFeign.toolsGetLandingPageShareInfo(id);
    }

    @GetMapping("/tools/landing-page/{id}/preview")
    public ResponseResult toolsPreviewLandingPage(@PathVariable Long id) {
        return geoServiceFeign.toolsPreviewLandingPage(id);
    }

    @PostMapping("/tools/landing-page/batch-generate")
    public ResponseResult toolsBatchGenerateLandingPages(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.toolsBatchGenerateLandingPages(body);
    }

    @GetMapping("/public/landing-page/{id}")
    public ResponseResult publicLandingPage(@PathVariable Long id) {
        return geoServiceFeign.publicLandingPage(id);
    }

    @GetMapping("/settings/security")
    public ResponseResult settingsSecurity(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.settingsSecurity(params != null ? params : Map.of());
    }

    @PutMapping("/settings/security")
    public ResponseResult settingsSaveSecurity(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.settingsSaveSecurity(body);
    }

    @GetMapping("/settings/system")
    public ResponseResult settingsSystem(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.settingsSystem(params != null ? params : Map.of());
    }

    @PutMapping("/settings/system")
    public ResponseResult settingsSaveSystem(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.settingsSaveSystem(body);
    }

    @GetMapping("/settings/audit-log")
    public ResponseResult settingsAuditLog(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.settingsAuditLog(params != null ? params : Map.of());
    }

    @GetMapping("/help/manual")
    public ResponseResult helpManual(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.helpManual(params != null ? params : Map.of());
    }

    @PutMapping("/help/manual")
    public ResponseResult saveHelpManual(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.saveHelpManual(body);
    }

    @GetMapping("/settings/support-chat")
    public ResponseResult settingsSupportChat(@RequestParam(required = false) Map<String, Object> params) {
        return geoServiceFeign.settingsSupportChat(params != null ? params : Map.of());
    }

    @PutMapping("/settings/support-chat")
    public ResponseResult settingsSaveSupportChat(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.settingsSaveSupportChat(body);
    }

    @PostMapping("/content/ai-studio/rewrite")
    public ResponseResult contentRewrite(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.contentRewrite(body);
    }

    @DeleteMapping("/goals/conversion/{id}")
    public ResponseResult goalsDeleteConversion(@PathVariable Long id) {
        return geoServiceFeign.goalsDeleteConversion(id);
    }

    @PatchMapping("/goals/conversion/{id}")
    public ResponseResult goalsToggleConversion(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.goalsToggleConversion(id, body != null ? body : Map.of());
    }

    @PostMapping("/permission/user/menus")
    public ResponseResult geoPermissionUserMenus(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.geoPermissionUserMenus(body != null ? body : Map.of());
    }

    @PostMapping("/permission/user/permissions")
    public ResponseResult geoPermissionUserPermissions(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.geoPermissionUserPermissions(body != null ? body : Map.of());
    }

    @PostMapping("/permission/user/check")
    public ResponseResult geoPermissionUserCheck(@RequestBody Map<String, Object> body) {
        return geoServiceFeign.geoPermissionUserCheck(body != null ? body : Map.of());
    }

    /** --- AI Token 使用统计 --- */
    @GetMapping("/ai/token-usage-stats")
    public ResponseResult getAiTokenUsageStats(@RequestParam(value = "tenantId", required = false) Long tenantId) {
        return geoServiceFeign.geoAiTokenUsageStats(tenantId);
    }

    @PostMapping("/ai/token-usage-details")
    public ResponseResult getAiTokenUsageDetails(
            @RequestParam(value = "tenantId", required = false) Long tenantId,
            @RequestBody Map<String, Object> params) {
        return geoServiceFeign.geoAiTokenUsageDetails(tenantId, params);
    }

    /**
     * POST 方式查询当前登录会员的 AI 用量统计（筛选条件走请求体透传）
/**
     *
     * @param params 筛选条件（moduleName / modelName / modelKeyword / startTime / endTime）
     * @return 统计数据
     */
    @PostMapping("/ai/token-usage-stats-query")
    public ResponseResult queryAiTokenUsageStats(@RequestBody(required = false) Map<String, Object> params) {
        return geoServiceFeign.geoAiTokenUsageStatsQuery(params != null ? params : Map.of());
    }

    /**
     * 查询全部会员的 AI 用量统计（geo 服务内校验管理员身份）
/**
     *
     * @param params 筛选条件
     * @return 全量统计
     */
    @PostMapping("/ai/token-usage-admin-stats")
    public ResponseResult getAiTokenUsageAdminStats(@RequestBody(required = false) Map<String, Object> params) {
        return geoServiceFeign.geoAiTokenUsageAdminStats(params != null ? params : Map.of());
    }

    /**
     * 分页查询全部会员的 AI 用量明细（geo 服务内校验管理员身份）
/**
     *
     * @param params page / limit + 筛选条件
     * @return 分页明细
     */
    @PostMapping("/ai/token-usage-admin-details")
    public ResponseResult getAiTokenUsageAdminDetails(@RequestBody(required = false) Map<String, Object> params) {
        return geoServiceFeign.geoAiTokenUsageAdminDetails(params != null ? params : Map.of());
    }

    @PostMapping("/ai/token-usage-admin-quotas")
    public ResponseResult getAiTokenUsageAdminQuotas(@RequestBody(required = false) Map<String, Object> params) {
        return geoServiceFeign.geoAiTokenUsageAdminQuotas(params != null ? params : Map.of());
    }

    @PostMapping("/ai/token-usage-admin-quota-set")
    public ResponseResult setAiTokenUsageAdminQuota(@RequestBody Map<String, Object> params) {
        return geoServiceFeign.geoAiTokenUsageAdminQuotaSet(params);
    }

    /** 客服浮窗（无需登录） */
    @GetMapping("/support/status")
    public ResponseResult supportStatus() {
        return geoServiceFeign.supportStatus();
    }

    @PostMapping("/support/chat")
    public ResponseResult supportChat(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.supportChat(body != null ? body : Map.of());
    }

    @PostMapping("/support/quota")
    public ResponseResult supportQuota(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.supportQuota(body != null ? body : Map.of());
    }

    @PostMapping("/support/usage")
    public ResponseResult supportUsage(@RequestBody(required = false) Map<String, Object> body) {
        return geoServiceFeign.supportUsage(body != null ? body : Map.of());
    }

    /**
     * 合并 query 与 body，确保 projectId/orgId 等上下文能经 Feign 传到 geo。
     */
    private static Map<String, Object> mergeGeoBody(Map<String, Object> params, Map<String, Object> body) {
        Map<String, Object> merged = new LinkedHashMap<>();
        if (params != null) {
            merged.putAll(params);
        }
        if (body != null) {
            merged.putAll(body);
        }
        return merged;
    }
}
