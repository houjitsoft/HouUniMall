package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * GEO 服务 Feign 客户端（路径与 paas-geo Controller 一致）
/**
 * @author Amos
 */
@FeignClient(value = "geo", path = "/plugin/geo", fallback = GeoServiceFeignFallback.class, contextId = "geoServiceFeign", url = "${feign.geo.url:}")
public interface GeoServiceFeign {

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/content/creation-tasks")
    ResponseResult creationTasks(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/creation-tasks")
    ResponseResult saveCreationTask(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/creation-tasks/batch-delete")
    ResponseResult deleteCreationTasks(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/assets/image-categories")
    ResponseResult imageCategories(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/assets/image-categories")
    ResponseResult createImageCategory(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PutMapping("/assets/image-categories/rename")
    ResponseResult renameImageCategory(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @DeleteMapping("/assets/image-categories")
    ResponseResult deleteImageCategory(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/brand/products")
    ResponseResult brandProducts(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/brand/products")
    ResponseResult createBrandProduct(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/brand/products/{id}")
    ResponseResult updateBrandProduct(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/brand/products/{id}")
    ResponseResult deleteBrandProduct(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/brand/competitors")
    ResponseResult brandCompetitors(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/brand/competitors")
    ResponseResult createBrandCompetitor(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/brand/competitors/{id}")
    ResponseResult updateBrandCompetitor(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/brand/competitors/{id}")
    ResponseResult deleteBrandCompetitor(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/brand/catalog-analysis")
    ResponseResult triggerBrandCatalogAnalysis(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/brand/catalog-analysis/status")
    ResponseResult brandCatalogAnalysisStatus(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/experiments/attribution/analyze")
    ResponseResult analyzeAttribution(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/technical/crawl-index/audit-run")
    ResponseResult technicalAuditRun(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/technical/site-health/check-run")
    ResponseResult technicalSiteHealthCheckRun(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/technical/site-health/cwv-check-run")
    ResponseResult technicalSiteHealthCwvCheckRun(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/runs")
    ResponseResult geoRuns(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/runs/{id}")
    ResponseResult geoRunDetail(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/runs/{id}/events")
    ResponseResult geoRunEvents(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/runs/{id}/cancel")
    ResponseResult cancelGeoRun(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/runs/items/{itemId}/retry")
    ResponseResult retryGeoRunItem(@PathVariable("itemId") Long itemId, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/content/video-capabilities")
    ResponseResult videoCapabilities();

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/content/digital-humans/api-services")
    ResponseResult digitalHumanApiServices();

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/content/video-assets/aliyun/templates")
    ResponseResult aliyunVideoTemplates(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/content/video-assets/aliyun/public-scene-templates")
    ResponseResult aliyunPublicSceneTemplates(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/video-assets/aliyun/public-scenes")
    ResponseResult copyAliyunPublicScene(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/content/video-assets/aliyun/materials")
    ResponseResult aliyunVideoMaterials(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/video-assets/aliyun/audio-sync")
    ResponseResult syncAliyunVideoAudio(@RequestBody Map<String, Object> body);

    @GetMapping("/content/video-assets/aliyun/templates/{templateId}") ResponseResult aliyunVideoTemplate(@PathVariable("templateId") String templateId);
    @GetMapping("/content/video-assets/aliyun/resources") ResponseResult aliyunVideoResources(@RequestParam(required = false) Map<String, Object> params);
    @PostMapping("/content/video-assets/aliyun/background-sync") ResponseResult syncAliyunBackground(@RequestBody Map<String, Object> body);
    @PostMapping("/content/video-assets/aliyun/sticker-sync") ResponseResult syncAliyunSticker(@RequestBody Map<String, Object> body);
    @DeleteMapping("/content/video-assets/aliyun/stickers/{stickerId}") ResponseResult deleteAliyunSticker(@PathVariable("stickerId") String stickerId, @RequestBody(required = false) Map<String, Object> body);
    @PostMapping("/content/video-assets/aliyun/materials/batch-delete") ResponseResult batchDeleteAliyunMaterials(@RequestBody Map<String, Object> body);
    @PostMapping("/content/video-assets/aliyun/avatars/no-train") ResponseResult createAliyunNoTrainAvatar(@RequestBody Map<String, Object> body);
    @PostMapping("/content/video-assets/aliyun/avatars/train") ResponseResult createAliyunTrainAvatar(@RequestBody Map<String, Object> body);
    @GetMapping("/content/video-assets/aliyun/avatars/{avatarId}/status") ResponseResult aliyunTrainAvatarStatus(@PathVariable("avatarId") String avatarId);
    @PostMapping("/content/video-assets/aliyun/avatars/{avatarId}/confirm") ResponseResult confirmAliyunTrainAvatar(@PathVariable("avatarId") String avatarId, @RequestBody Map<String, Object> body);
    @GetMapping("/content/video-assets/aliyun/voices") ResponseResult aliyunVoices(@RequestParam(required = false) Map<String, Object> params);
    @GetMapping("/content/video-assets/aliyun/voices/public") ResponseResult aliyunPublicVoices();
    @PostMapping("/content/video-assets/aliyun/voices/public/use") ResponseResult useAliyunPublicVoice(@RequestBody Map<String, Object> body);
    @GetMapping("/content/video-assets/aliyun/voices/{voiceId}") ResponseResult aliyunVoice(@PathVariable("voiceId") String voiceId);
    @PostMapping("/content/video-assets/aliyun/voices") ResponseResult createAliyunVoice(@RequestBody Map<String, Object> body);
    @PostMapping("/content/video-assets/aliyun/chat-configs") ResponseResult createAliyunChatConfig(@RequestBody Map<String, Object> body);
    @PostMapping("/content/video-assets/aliyun/chat-sessions") ResponseResult createAliyunChatSession(@RequestBody Map<String, Object> body);
    @PostMapping("/content/video-assets/aliyun/chat-sessions/query") ResponseResult queryAliyunChatSessions(@RequestBody Map<String, Object> body);
    @PostMapping("/content/video-assets/aliyun/chat-sessions/close") ResponseResult closeAliyunChatSessions(@RequestBody Map<String, Object> body);
    @PostMapping("/content/digital-humans/live/instances") ResponseResult startAliyunAvatarLive(@RequestBody Map<String, Object> body);
    @DeleteMapping("/content/digital-humans/live/instances/{sessionId}") ResponseResult stopAliyunAvatarLive(@PathVariable("sessionId") String sessionId, @RequestBody(required = false) Map<String, Object> body);
    @GetMapping("/content/digital-humans/live/instances") ResponseResult queryAliyunAvatarLiveInstances(@RequestParam(required = false) Map<String, Object> params);
    @PostMapping("/content/digital-humans/live/instances/{sessionId}/text") ResponseResult sendAliyunAvatarLiveText(@PathVariable("sessionId") String sessionId, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/video-ideation/titles")
    ResponseResult generateVideoTitles(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/video-ideation/brief")
    ResponseResult generateVideoBrief(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/video-ideation/storyboard")
    ResponseResult generateVideoStoryboard(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/video-projects")
    ResponseResult createVideoProject(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/content/video-projects")
    ResponseResult videoProjects(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/content/video-projects/{projectId}")
    ResponseResult videoProject(@PathVariable("projectId") Long projectId, @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/content/video-projects/{projectId}")
    ResponseResult saveVideoProject(@PathVariable("projectId") Long projectId, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/content/video-projects/{projectId}/reviews")
    ResponseResult reviewVideoProject(@PathVariable("projectId") Long projectId, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/content/video-projects/{projectId}/preflight")
    ResponseResult preflightVideoProject(@PathVariable("projectId") Long projectId, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/content/video-projects/{projectId}/generate")
    ResponseResult generateVideoProject(@PathVariable("projectId") Long projectId, @RequestParam(required = false) Map<String, Object> params, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/content/video-jobs")
    ResponseResult videoJobs(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/content/video-jobs/{id}")
    ResponseResult videoJob(@PathVariable("id") Long id, @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/content/video-jobs/{id}/detail")
    ResponseResult videoJobDetail(@PathVariable("id") Long id, @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/content/video-jobs/{id}/cancel")
    ResponseResult cancelVideoJob(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/content/video-jobs/{id}/retry")
    ResponseResult retryVideoJob(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/content/video-jobs/{id}/regenerate")
    ResponseResult regenerateVideoJob(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/content/video-jobs/{id}/compose")
    ResponseResult composeVideoJob(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/content/video-jobs/{id}/validate")
    ResponseResult validateVideoJob(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/content/video-jobs/{id}/review")
    ResponseResult reviewVideoJob(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/content/video-jobs/{id}/handoff")
    ResponseResult handoffVideoJob(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> body);

    // --- 监测 ---
    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/monitor/visibility")
    ResponseResult monitorVisibility(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/monitor/visibility/refresh")
    ResponseResult monitorVisibilityRefresh(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/monitor/citations")
    ResponseResult monitorCitations(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/monitor/citations/sync")
    ResponseResult monitorCitationsSync(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/monitor/questions")
    ResponseResult monitorQuestions(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/monitor/questions/sync-scope-preview")
    ResponseResult monitorQuestionScopeSyncPreview(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/monitor/questions/sync-scope")
    ResponseResult monitorQuestionsFromScope(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PatchMapping("/monitor/questions/{id}")
    ResponseResult monitorToggleQuestion(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/monitor/questions/batch-monitor")
    ResponseResult monitorBatchMonitor(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/monitor/questions/import")
    ResponseResult monitorImportQuestions(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/monitor/questions/to-task")
    ResponseResult monitorQuestionsToTask(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/monitor/competitor")
    ResponseResult monitorCompetitor(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/monitor/competitor")
    ResponseResult createMonitorCompetitor(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/monitor/competitor/{id}")
    ResponseResult deleteMonitorCompetitor(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/monitor/competitor/gaps/task")
    ResponseResult monitorCompetitorGapToTask(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/monitor/sentiment")
    ResponseResult monitorSentiment(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PatchMapping("/monitor/sentiment/alerts/{id}")
    ResponseResult monitorCloseAlert(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PatchMapping("/monitor/sentiment/hallucinations/{id}")
    ResponseResult monitorResolveHallucination(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/dashboard/geo-kpis")
    ResponseResult dashboardGeoKpis(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/dashboard/overview")
    ResponseResult dashboardOverview(@RequestParam(required = false) Map<String, Object> params,
                                     @RequestParam(defaultValue = "7") int days);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/media/resources")
    ResponseResult mediaResources(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/media/resources/{id}/favorite")
    ResponseResult mediaResourceFavorite(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/media/resources/submit")
    ResponseResult mediaResourceSubmit(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/media/resources/import")
    ResponseResult mediaResourceImport(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/media/resources/sync-timus")
    ResponseResult mediaResourceSyncTimus(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/media/resources/sync-timus-full")
    ResponseResult mediaResourceSyncTimusFull(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/media/resources/sync-timus-full/{jobId}")
    ResponseResult mediaResourceSyncTimusFullStatus(@PathVariable("jobId") String jobId,
                                                    @RequestParam(required = false) Map<String, Object> params);

    // --- Schema ---
    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/dashboard")
    ResponseResult dashboard(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/templates")
    ResponseResult templateList(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/pages")
    ResponseResult pageList(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/validate")
    ResponseResult validateJsonLd(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/templates/{id}")
    ResponseResult templateDetail(@PathVariable("id") Long id,
                                  @RequestParam(value = "projectId", required = false) Long projectId);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/templates")
    ResponseResult templateCreate(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/templates/{id}")
    ResponseResult templateUpdate(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/templates/{id}")
    ResponseResult templateDelete(@PathVariable("id") Long id,
                                  @RequestParam(value = "projectId", required = false) Long projectId);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/templates/{id}/copy")
    ResponseResult templateCopy(@PathVariable("id") Long id,
                                @RequestParam(value = "projectId", required = false) Long projectId);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/pages/{id}/schema")
    ResponseResult pageSchema(@PathVariable("id") Long id,
                              @RequestParam(value = "projectId", required = false) Long projectId);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/pages/{id}/schema")
    ResponseResult savePageSchema(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    // --- 研究 / 分析 / 内容 ---
    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/research/plate-a/overview")
    ResponseResult researchPlateAOverview(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/research/auto-complete")
    ResponseResult researchAutoComplete(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/research/auto-complete")
    ResponseResult researchAutoCompleteStatus(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/research/keywords")
    ResponseResult researchKeywords(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/research/keywords/discover")
    ResponseResult researchDiscover(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/research/keywords/competitor-gap")
    ResponseResult researchCompetitorGap(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/research/keywords/{id}/trend")
    ResponseResult researchKeywordTrend(@PathVariable("id") Long id, @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/research/keywords/i18n-map")
    ResponseResult researchI18nMap(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/research/keywords/export")
    ResponseResult researchExportKeywords(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/research/topic-clusters")
    ResponseResult researchTopicClusters(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/research/intent")
    ResponseResult researchIntent(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/analysis/models")
    ResponseResult analysisModels(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/analysis/models/{id}/diagnosis")
    ResponseResult analysisModelDiagnosis(@PathVariable("id") String id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/analysis/models/{id}/subscription")
    ResponseResult updateAnalysisModelSubscription(@PathVariable("id") String id, @RequestBody Map<String, Object> body);

    // --- 前端路径别名 ---
    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/analysis/models")
    ResponseResult modelsAlias(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/analysis/models/{id}/diagnosis")
    ResponseResult modelsDiagnosisAlias(@PathVariable("id") String id,
                                        @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/analysis/models/{id}/subscription")
    ResponseResult updateModelsSubscriptionAlias(@PathVariable("id") String id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/analysis/models/{id}/probe")
    ResponseResult probeModelIntegration(@PathVariable("id") String id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/analysis/models/{id}/registration")
    ResponseResult saveModelRegistration(@PathVariable("id") String id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/analysis/models/{id}/registration/status")
    ResponseResult updateModelRegistrationStatus(@PathVariable("id") String id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/reports")
    ResponseResult reportsAlias(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/reports")
    ResponseResult generateReportAlias(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/intent/coverage")
    ResponseResult intentCoverageAlias(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/local")
    ResponseResult localAlias(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/links/internal")
    ResponseResult linksInternalAlias(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/monitor/questions/import-hot")
    ResponseResult monitorImportHot(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/templates/{id}/preview")
    ResponseResult templatePreview(@PathVariable("id") Long id,
                                   @RequestParam(value = "projectId", required = false) Long projectId);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/research/keywords/import")
    ResponseResult researchImportKeywords(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/research/conversions/preview")
    ResponseResult researchConversionPreview(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/research/conversions/commit")
    ResponseResult researchConversionCommit(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/research/topic-clusters/import")
    ResponseResult researchImportClusters();

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/research/topic-clusters")
    ResponseResult researchCreateCluster(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/research/topic-clusters/{id}")
    ResponseResult researchUpdateCluster(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/research/topic-clusters/{id}/move-keywords")
    ResponseResult researchMoveClusterKeywords(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/research/topic-clusters/merge")
    ResponseResult researchMergeClusters(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/research/topic-clusters/{id}")
    ResponseResult researchDeleteCluster(@PathVariable("id") Long id,
                                         @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/research/topic-clusters/pillar-plan")
    ResponseResult researchTopicPillarPlan(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/research/topic-clusters/subscribe")
    ResponseResult researchTopicSubscribe(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/research/intent/scan")
    ResponseResult researchIntentScan(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/research/intent/import-questions")
    ResponseResult researchImportIntentQuestions();

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/strategy/authority/scan")
    ResponseResult strategyAuthorityScan(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PatchMapping("/strategy/authority/citations/{id}/adopt")
    ResponseResult strategyAuthorityCitationAdopt(@PathVariable("id") Long id,
                                                  @RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/strategy/model-adapt/sandbox")
    ResponseResult strategySandbox(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/strategy/model-adapt/format")
    ResponseResult strategyFormat(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/strategy/howto/generate")
    ResponseResult strategyHowToGenerate(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/ai-studio/generate")
    ResponseResult contentGenerate(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/ai-studio/generate-by-keyword")
    ResponseResult contentGenerateByKeyword(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/ai-studio/generate-batch")
    ResponseResult contentGenerateBatch(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/content/ai-studio/generate-batch/active")
    ResponseResult contentGenerateBatchActive(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/content/ai-studio/generate-batch/{jobId}")
    ResponseResult contentGenerateBatchDetail(@PathVariable("jobId") String jobId);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/ai-studio/platform-preview")
    ResponseResult contentPlatformPreview(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/ai-studio/advance-to-schedule-pool")
    ResponseResult contentAdvanceToSchedulePool(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/ai-studio/keywords/expand")
    ResponseResult contentExpandKeywords(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/ai-studio/seed")
    ResponseResult contentSaveAiStudioSeed(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/ai-studio/video-script")
    ResponseResult contentVideoScript(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/multimodal/visual")
    ResponseResult contentVisual(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/hot-trend/{platform}")
    ResponseResult hotTrendPlatform(@PathVariable("platform") String platform, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/hot-trend/sync-logs")
    ResponseResult hotTrendSyncLogs(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/hot-trend/to-questions")
    ResponseResult hotTrendToQuestions(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/integrations/api")
    ResponseResult integrationsApi(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/integrations/mcp/servers")
    ResponseResult mcpServers(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/integrations/mcp/servers")
    ResponseResult saveMcpServer(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/integrations/mcp/servers/{id}")
    ResponseResult deleteMcpServer(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/integrations/mcp/servers/{id}/test")
    ResponseResult testMcpServer(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/pages/batch")
    ResponseResult batchPages(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/pages/import-sitemap")
    ResponseResult importSitemap(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/ai-studio/style")
    ResponseResult contentStyle(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/ai-studio/i18n")
    ResponseResult contentI18n(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/content/multimodal/visual/{id}/complete")
    ResponseResult contentVisualComplete(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/multimodal/tts")
    ResponseResult contentTts(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/multimodal/avatar")
    ResponseResult contentAvatar(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/multimodal/chart")
    ResponseResult contentChart(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/content/viral-replica")
    ResponseResult contentViralReplica(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/viral-replica/prompt")
    ResponseResult contentViralReplicaPrompt(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/viral-replica/replicate")
    ResponseResult contentViralReplicaReplicate(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/content/viral-replica/{id}")
    ResponseResult contentViralReplicaDelete(
        @PathVariable("id") Long id,
        @RequestParam(required = false) Map<String, Object> params
    );

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/assets")
    ResponseResult contentCreateAsset(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/content/assets/{id}")
    ResponseResult contentUpdateAsset(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/content/assets/{id}")
    ResponseResult contentDeleteAsset(@PathVariable("id") Long id, @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping(value = "/content/assets/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseResult uploadContentAsset(@RequestPart("file") MultipartFile file,
                                      @RequestParam("projectId") Long projectId,
                                      @RequestParam(value = "name", required = false) String name,
                                      @RequestParam(value = "tags", required = false) String tags);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/content/assets/generation-candidates")
    ResponseResult assetGenerationCandidates(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/assets/import-generated")
    ResponseResult importGeneratedAssets(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/content/assets/{assetId}/versions")
    ResponseResult assetVersions(@PathVariable("assetId") Long assetId, @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/content/assets/{assetId}/versions")
    ResponseResult createExplicitAssetVersion(@PathVariable("assetId") Long assetId, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/content/assets/{assetId}/versions/{versionId}/rollback")
    ResponseResult rollbackAssetVersion(@PathVariable("assetId") Long assetId, @PathVariable("versionId") Long versionId, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/content/assets/freshness-alerts/{id}/ignore")
    ResponseResult ignoreAssetFreshnessAlert(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/assets/versions")
    ResponseResult contentAssetVersion(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/content/assets/freshness-scan")
    ResponseResult contentFreshnessScan(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/ranking/search-ai-features")
    ResponseResult rankingSearchAiFeatures(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/experiments/attribution")
    ResponseResult experimentsAttribution(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/local/gbp")
    ResponseResult localGbp(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/local/china-maps")
    ResponseResult localChinaMaps(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/local/nap")
    ResponseResult localNap(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/local/ai-summary")
    ResponseResult localAiSummary(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/technical/site-health")
    ResponseResult technicalSiteHealth(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/technical/ai-discoverability")
    ResponseResult technicalAiDiscoverability(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/technical/crawlability")
    ResponseResult technicalCrawlability(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/technical/semantic-html")
    ResponseResult technicalSemanticHtml(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/technical/performance")
    ResponseResult technicalPerformance(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/technical/crawlability/js-check")
    ResponseResult technicalJsCheck(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/technical/semantic-html/check")
    ResponseResult technicalSemanticCheck(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/technical/performance/mobile")
    ResponseResult technicalMobileCheck(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/technical/performance/lighthouse")
    ResponseResult technicalLighthouseCheck(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/intelligence/industry")
    ResponseResult intelligenceIndustry(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/hot-trend/sync")
    ResponseResult hotTrendSync(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/hot-trend/batch-question-bank")
    ResponseResult hotTrendBatchQuestionBank(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/analysis/source")
    ResponseResult analysisSource(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/analysis/health")
    ResponseResult analysisHealth(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/analysis/reports")
    ResponseResult analysisReports(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/strategy/authority")
    ResponseResult strategyAuthority(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/strategy/model-adapt")
    ResponseResult strategyModelAdapt(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/content/ai-studio")
    ResponseResult contentAiStudio(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/content/expand-modes")
    ResponseResult contentExpandModes(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/content/expand-modes/refresh")
    ResponseResult contentRefreshExpandModes(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/content/multimodal")
    ResponseResult contentMultimodal(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/content/assets")
    ResponseResult contentAssets(@RequestParam(required = false) Map<String, Object> params);

    // --- 链接 / 排名 / 实验 / 本地化 / 技术 / 情报 ---
    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/links/backlinks")
    ResponseResult linksBacklinks(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/links/outreach")
    ResponseResult linksOutreach(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/links/internal-links")
    ResponseResult linksInternal(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/links/backlinks")
    ResponseResult addBacklink(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/links/outreach")
    ResponseResult createLinkOutreach(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/links/internal-links")
    ResponseResult addInternalLinkAudit(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/links/backlinks/export")
    ResponseResult exportBacklinks(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/links/outreach/export")
    ResponseResult exportLinkOutreach(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/links/internal-links/export")
    ResponseResult exportInternalLinks(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/ranking/ai-engines")
    ResponseResult rankingAiEngines(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/ranking/refresh")
    ResponseResult refreshRanking(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/experiments/ab-tests")
    ResponseResult experimentsAbTests(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/experiments/ab-tests")
    ResponseResult createExperiment(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/experiments/ab-tests/run")
    ResponseResult runExperiment(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/experiments/ab-tests/{id}")
    ResponseResult deleteExperiment(@PathVariable("id") Long id, @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/local/overview")
    ResponseResult localOverview(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/local/nap/report")
    ResponseResult localNapReport(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/local/gbp/refresh")
    ResponseResult refreshLocalGbp(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/local/china-maps/import")
    ResponseResult importLocalChinaMaps(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/local/nap/check")
    ResponseResult checkLocalNap(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/local/ai-summary/refresh")
    ResponseResult refreshLocalAiSummary(@RequestParam(required = false) Map<String, Object> params);

    @GetMapping("/local/stores") ResponseResult localStores(@RequestParam Map<String, Object> params);
    @GetMapping("/local/stores/export") ResponseEntity<byte[]> exportLocalStores(@RequestParam Map<String, Object> params);
    @PostMapping("/local/stores") ResponseResult createLocalStore(@RequestBody Map<String, Object> body);
    @PutMapping("/local/stores/{id}") ResponseResult updateLocalStore(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);
    @DeleteMapping("/local/stores/{id}") ResponseResult deleteLocalStore(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);
    @PostMapping("/local/stores/import") ResponseResult importLocalStores(@RequestBody Map<String, Object> body);
    @GetMapping("/local/bindings") ResponseResult localBindings(@RequestParam Map<String, Object> params);
    @GetMapping("/local/bindings/candidates") ResponseResult searchLocalBindingCandidates(@RequestParam Map<String, Object> params);
    @PostMapping("/local/bindings") ResponseResult saveLocalBinding(@RequestBody Map<String, Object> body);
    @PutMapping("/local/bindings/{id}") ResponseResult updateLocalBinding(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);
    @DeleteMapping("/local/bindings/{id}") ResponseResult deleteLocalBinding(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);
    @GetMapping("/local/providers/status") ResponseResult localProviderStatus(@RequestParam Map<String, Object> params);
    @PostMapping("/local/sync-jobs") ResponseResult createLocalSyncJob(@RequestBody Map<String, Object> body);
    @GetMapping("/local/sync-jobs") ResponseResult localSyncJobs(@RequestParam Map<String, Object> params);
    @GetMapping("/local/sync-jobs/{id}") ResponseResult localSyncJob(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);
    @PostMapping("/local/sync-jobs/{id}/retry") ResponseResult retryLocalSyncJob(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);
    @PostMapping("/local/gbp/import") ResponseResult importLocalGbp(@RequestBody Map<String, Object> body);
    @PostMapping("/local/nap/audits") ResponseResult createLocalNapAudit(@RequestBody Map<String, Object> body);
    @GetMapping("/local/nap/audits") ResponseResult localNapAudits(@RequestParam Map<String, Object> params);
    @GetMapping("/local/nap/issues") ResponseResult localNapIssues(@RequestParam Map<String, Object> params);
    @PostMapping("/local/nap/issues/approve") ResponseResult approveLocalNapIssues(@RequestBody Map<String, Object> body);
    @PostMapping("/local/nap/issues/reject") ResponseResult rejectLocalNapIssues(@RequestBody Map<String, Object> body);
    @PostMapping("/local/nap/issues/{id}/recheck") ResponseResult recheckLocalNapIssue(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);
    @GetMapping("/local/nap/issues/export") ResponseResult exportLocalNapIssues(@RequestParam Map<String, Object> params);
    @GetMapping("/local/monitor/queries") ResponseResult localMonitorQueries(@RequestParam Map<String, Object> params);
    @PostMapping("/local/monitor/queries") ResponseResult createLocalMonitorQuery(@RequestBody Map<String, Object> body);
    @PutMapping("/local/monitor/queries/{id}") ResponseResult updateLocalMonitorQuery(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);
    @DeleteMapping("/local/monitor/queries/{id}") ResponseResult deleteLocalMonitorQuery(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);
    @PostMapping("/local/monitor/queries/import") ResponseResult importLocalMonitorQueries(@RequestBody Map<String, Object> body);
    @GetMapping("/local/monitor/models") ResponseResult localMonitorModels(@RequestParam Map<String, Object> params);
    @GetMapping("/local/monitor/plan") ResponseResult localMonitorPlan(@RequestParam Map<String, Object> params);
    @PutMapping("/local/monitor/plan") ResponseResult saveLocalMonitorPlan(@RequestBody Map<String, Object> body);
    @GetMapping("/local/monitor/runs") ResponseResult localMonitorRuns(@RequestParam Map<String, Object> params);
    @PostMapping("/local/monitor/runs") ResponseResult runLocalMonitor(@RequestBody Map<String, Object> body);
    @GetMapping("/local/monitor/runs/{id}") ResponseResult localMonitorRun(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);
    @PostMapping("/local/monitor/runs/{id}/retry") ResponseResult retryLocalMonitorRun(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);
    @PostMapping("/local/monitor/results/import") ResponseResult importLocalMonitorResults(@RequestBody Map<String, Object> body);
    @GetMapping("/local/monitor/results") ResponseResult localMonitorResults(@RequestParam Map<String, Object> params);
    @GetMapping("/local/monitor/results/{id}") ResponseResult localMonitorResult(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);
    @PostMapping("/local/monitor/results/{id}/workflow-task") ResponseResult createLocalMonitorWorkflowTask(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);
    @GetMapping("/local/monitor/trends") ResponseResult localMonitorTrends(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/technical/crawl-index")
    ResponseResult technicalCrawlIndex(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/intelligence/crawler-policy")
    ResponseResult intelligenceCrawlerPolicy(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/intelligence/policy/changelog")
    ResponseResult intelligencePolicyChangelog(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/intelligence/policy/subscribe")
    ResponseResult subscribeIntelligencePolicy(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/intelligence/industry/report/create")
    ResponseResult createIntelligenceIndustryReport(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/workflow/tasks")
    ResponseResult workflowTasks(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/workflow/tasks")
    ResponseResult createWorkflowTask(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/workflow/tasks/{id}")
    ResponseResult updateWorkflowTask(@PathVariable("id") Long id,
                                      @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/workflow/tasks/{id}")
    ResponseResult deleteWorkflowTask(@PathVariable("id") Long id,
                                      @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/workflow/team")
    ResponseResult workflowTeam(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/workflow/team")
    ResponseResult workflowAddTeamMember(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/workflow/team/{memberId}")
    ResponseResult workflowRemoveTeamMember(@PathVariable("memberId") Long memberId,
                                            @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/workflow/team/{memberId}/role")
    ResponseResult updateTeamMemberRole(@PathVariable("memberId") Long memberId,
                                        @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/hot-trend/list")
    ResponseResult hotTrendList(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/open-api/keys")
    ResponseResult openApiKeys(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/open-api/keys")
    ResponseResult openApiKeysCreate(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/open-api/keys/{id}")
    ResponseResult openApiKeyRevoke(@PathVariable("id") Long id,
                                    @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/settings/monitor")
    ResponseResult settingsMonitor(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PutMapping("/settings/monitor")
    ResponseResult settingsMonitorSave(@RequestBody Map<String, Object> body);

    // --- 分发 P0-C ---
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/distribute/{platform}")
    ResponseResult distributeList(@PathVariable("platform") String platform, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/distribute/{platform}/{id}")
    ResponseResult distributeDetail(@PathVariable("platform") String platform, @PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/distribute/{platform}")
    ResponseResult distributeCreate(@PathVariable("platform") String platform, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/distribute/{platform}/{id}")
    ResponseResult distributeUpdate(@PathVariable("platform") String platform, @PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/distribute/{platform}/{id}")
    ResponseResult distributeDelete(@PathVariable("platform") String platform, @PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/distribute/{platform}/publish")
    ResponseResult distributePublish(@PathVariable("platform") String platform, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/distribute/{platform}/geo-detect")
    ResponseResult distributeGeoDetect(@PathVariable("platform") String platform, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/distribute/{platform}/schedule-pool")
    ResponseResult distributeSchedulePool(@PathVariable("platform") String platform, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/distribute/{platform}/from-article")
    ResponseResult distributeImportFromArticle(@PathVariable("platform") String platform, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/distribute/{platform}/ai/topics")
    ResponseResult distributeAiTopics(@PathVariable("platform") String platform, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/distribute/{platform}/ai/generate")
    ResponseResult distributeAiGenerate(@PathVariable("platform") String platform, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/distribute/{platform}/ai/adapt-tone")
    ResponseResult distributeAiAdaptTone(@PathVariable("platform") String platform, @RequestBody Map<String, Object> body);

    // --- 平台授权与配置 ---
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/platform-auth")
    ResponseResult platformAuthList(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/platform-auth/accounts")
    ResponseResult platformAuthAccounts(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/settings/platform-auth")
    ResponseResult settingsPlatformAuthAlias(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/settings/platform-auth")
    ResponseResult settingsPlatformAuthCreateAlias(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/platform-auth")
    ResponseResult platformAuthCreate(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/platform-auth/{id}")
    ResponseResult platformAuthDelete(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/platform-auth/{id}/check")
    ResponseResult platformAuthCheck(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/platform-auth/{id}/default")
    ResponseResult platformAuthSetDefault(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/platform-auth/{id}/refresh")
    ResponseResult platformAuthRefresh(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/platform-auth/{platform}/oauth-url")
    ResponseResult platformAuthOauthUrl(@PathVariable("platform") String platform, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/platform-auth/{platform}/oauth-callback")
    ResponseResult platformAuthOauthCallback(@PathVariable("platform") String platform, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/platform-auth/{platform}/cookie-login-info")
    ResponseResult platformAuthCookieLoginInfo(@PathVariable("platform") String platform, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/platform-auth/{platform}/cookie-verify")
    ResponseResult platformAuthCookieVerify(@PathVariable("platform") String platform, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/platform-auth/{platform}/cookie-bind")
    ResponseResult platformAuthCookieBind(@PathVariable("platform") String platform, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/platform-auth/{id}/permissions")
    ResponseResult platformAuthPermissions(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/platform-auth/{id}/auth-logs")
    ResponseResult platformAuthAuthLogs(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);

    /** 保存平台登录凭证（密码加密存储） */
    @PostMapping("/platform-auth/credentials")
    ResponseResult platformAuthCredentialSave(@RequestBody Map<String, Object> body);

    /** 平台登录凭证列表 */
    @GetMapping("/platform-auth/credentials")
    ResponseResult platformAuthCredentials(@RequestParam(required = false) Map<String, Object> params);

    /** 删除平台登录凭证 */
    @DeleteMapping("/platform-auth/credentials/{id}")
    ResponseResult platformAuthCredentialDelete(@PathVariable("id") Long id, @RequestParam(required = false) Map<String, Object> params);

    /** 发起自动重新登录（账号密码登录，需扫码时返回二维码） */
    @PostMapping("/platform-auth/relogin")
    ResponseResult platformAuthRelogin(@RequestBody Map<String, Object> body);

    /** 查询自动重新登录会话状态 */
    @GetMapping("/platform-auth/relogin/{sessionId}")
    ResponseResult platformAuthReloginStatus(@PathVariable("sessionId") String sessionId,
                                             @RequestParam(required = false) Map<String, Object> params);

    /** 取消自动重新登录会话 */
    @DeleteMapping("/platform-auth/relogin/{sessionId}")
    ResponseResult platformAuthReloginCancel(@PathVariable("sessionId") String sessionId,
                                             @RequestParam(required = false) Map<String, Object> params);

    /** 提交手机号并请求短信验证码（小红书等仅验证码登录的平台） */
    @PostMapping("/platform-auth/relogin/{sessionId}/sms/phone")
    ResponseResult platformAuthReloginSmsPhone(@PathVariable("sessionId") String sessionId,
                                               @RequestBody Map<String, Object> body);

    /** 提交短信验证码完成登录 */
    @PostMapping("/platform-auth/relogin/{sessionId}/sms/code")
    ResponseResult platformAuthReloginSmsCode(@PathVariable("sessionId") String sessionId,
                                              @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/platform-config/{platform}")
    ResponseResult platformConfigGet(@PathVariable("platform") String platform, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/platform-config/{platform}")
    ResponseResult platformConfigSave(@PathVariable("platform") String platform, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/platform-config/{platform}/test")
    ResponseResult platformConfigTest(@PathVariable("platform") String platform, @RequestBody(required = false) Map<String, Object> body);

    // --- Omni ---
    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/distribute/omni/preview")
    ResponseResult omniPreview(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/distribute/omni/validate")
    ResponseResult omniValidate(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/distribute/omni/publish")
    ResponseResult omniPublish(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/distribute/omni/jobs")
    ResponseResult omniJobs(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/distribute/omni/jobs/{id}")
    ResponseResult omniJobDetail(@PathVariable("id") String id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/distribute/omni/jobs/{id}/retry")
    ResponseResult omniJobRetry(@PathVariable("id") String id, @RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/distribute/omni/context")
    ResponseResult omniContext(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/distribute/omni/drafts")
    ResponseResult omniDrafts(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/distribute/omni/source/import-url")
    ResponseResult omniImportSource(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/distribute/omni/jobs/{id}/logs")
    ResponseResult omniJobLogs(@PathVariable("id") String id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/distribute/omni/fail-logs")
    ResponseResult omniFailLogs(@RequestParam(required = false) Map<String, Object> params);

    @GetMapping("/risk/events")
    ResponseResult riskEvents(@RequestParam(required = false) Map<String, Object> params);

    @PutMapping("/risk/events/{id}")
    ResponseResult updateRiskEvent(@PathVariable("id") String id, @RequestBody Map<String, Object> body);

    // --- 月排期计划 ---
    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/distribute/publish-plan/list")
    ResponseResult publishPlanList(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/distribute/publish-plan/calendar")
    ResponseResult publishPlanCalendar(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/distribute/publish-plan/schedule-pool")
    ResponseResult publishPlanSchedulePool(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/distribute/publish-plan/platform-quotas")
    ResponseResult publishPlanPlatformQuotas(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/distribute/publish-plan/{id:\\d+}")
    ResponseResult publishPlanDetail(@PathVariable("id") Long id, @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/distribute/publish-plan/get-or-create")
    ResponseResult publishPlanGetOrCreate(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/distribute/publish-plan/{id:\\d+}/items")
    ResponseResult publishPlanSaveItems(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/distribute/publish-plan/{id:\\d+}/auto-fill")
    ResponseResult publishPlanAutoFill(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/distribute/publish-plan/auto-fill-year")
    ResponseResult publishPlanAutoFillYear(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/distribute/publish-plan/auto-fill-preview")
    ResponseResult publishPlanAutoFillPreview(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/distribute/publish-plan/{id:\\d+}/activate")
    ResponseResult publishPlanActivate(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/distribute/publish-plan/activate-year")
    ResponseResult publishPlanActivateYear(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/distribute/publish-plan/{id:\\d+}/generate-scheduled-send")
    ResponseResult publishPlanGenerateScheduledSend(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/distribute/publish-plan/{id:\\d+}/pause")
    ResponseResult publishPlanPause(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/distribute/publish-plan/tasks/{taskId:\\d+}/retry")
    ResponseResult publishPlanRetryTask(@PathVariable("taskId") Long taskId, @RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PutMapping("/distribute/publish-plan/platform-quotas")
    ResponseResult publishPlanSavePlatformQuotas(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/distribute/jobs")
    ResponseResult distributeJobsAlias(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/distribute/jobs/{id}")
    ResponseResult distributeJobDetailAlias(@PathVariable("id") String id, @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/distribute/jobs/{id}/retry")
    ResponseResult distributeJobRetryAlias(@PathVariable("id") String id, @RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/account/profile")
    ResponseResult accountProfile(@RequestParam(required = false) Map<String, Object> params);

    // --- 通用 Tab ---
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/page-tabs/{key}")
    ResponseResult pageTab(@PathVariable("key") String key,
                           @RequestParam(value = "projectId", required = false) Long projectId,
                           @RequestHeader(value = "X-Project-Id", required = false) Long projectIdHeader);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/page-tabs/{key}/refresh")
    ResponseResult pageTabRefresh(@PathVariable("key") String key,
                                  @RequestParam(value = "projectId", required = false) Long projectId,
                                  @RequestHeader(value = "X-Project-Id", required = false) Long projectIdHeader,
                                  @RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/page-tabs/{key}/execute")
    ResponseResult pageTabExecute(@PathVariable("key") String key,
                                  @RequestParam(value = "projectId", required = false) Long projectId,
                                  @RequestHeader(value = "X-Project-Id", required = false) Long projectIdHeader,
                                  @RequestBody Map<String, Object> body);

    // --- 工作流 ---
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/workflow/projects")
    ResponseResult workflowProjects(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/workflow/projects")
    ResponseResult workflowCreateProject(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/workflow/projects/{id}")
    ResponseResult workflowUpdateProject(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/workflow/projects/{id}")
    ResponseResult deleteWorkflowProject(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/workflow/projects/{id}/brand-settings")
    ResponseResult workflowBrandSettings(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/workflow/projects/{id}/brand-settings")
    ResponseResult workflowSaveBrandSettings(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/workflow/projects/{id}/monitor-scope")
    ResponseResult workflowMonitorScope(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/workflow/projects/{id}/monitor-scope")
    ResponseResult workflowSaveMonitorScope(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/workflow/projects/monitor-keywords/suggest")
    ResponseResult suggestMonitorKeywords(@RequestBody Map<String, Object> body);

    // --- Webhook ---
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/open-api/webhooks")
    ResponseResult webhookList(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/open-api/webhooks")
    ResponseResult webhookSave(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/open-api/webhooks/{id}")
    ResponseResult webhookDelete(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/open-api/webhooks/test")
    ResponseResult webhookTest(@RequestBody(required = false) Map<String, Object> body);

    // --- v6.3 目标 / 收录 / 资产 / 蒸馏 ---
    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/goals/conversion")
    ResponseResult goalsConversion(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/goals/conversion")
    ResponseResult goalsSaveConversion(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/goals/conversion-events")
    ResponseResult conversionEvents(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/goals/conversion-events/summary")
    ResponseResult conversionEventSummary(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/goals/conversion-events/{id}")
    ResponseResult conversionEventDetail(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/goals/conversion-events/{id}/contact/reveal")
    ResponseResult revealConversionEventContact(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/goals/conversion-events/{id}")
    ResponseResult deleteConversionEvent(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping(value = "/goals/conversion-events/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseResult importConversionEvents(@RequestPart("file") MultipartFile file,
                                          @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/goals/conversion-events/export")
    ResponseEntity<byte[]> exportConversionEvents(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/media/generated/{fileName}")
    ResponseEntity<byte[]> generatedMedia(@PathVariable("fileName") String fileName);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/goals/conversion-tracking/config")
    ResponseResult conversionTrackingConfig(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PutMapping("/goals/conversion-tracking/config")
    ResponseResult saveConversionTrackingConfig(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/goals/conversion-tracking/config/token/rotate")
    ResponseResult rotateConversionTrackingToken(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/goals/conversion-tracking/config/webhook-secret/rotate")
    ResponseResult rotateConversionWebhookSecret(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/indexing/reports")
    ResponseResult indexingReports(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/indexing/sync")
    ResponseResult indexingSync(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/indexing/reports/export")
    ResponseResult indexingReportsExport(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/assets/images")
    ResponseResult assetsImages(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/assets/images")
    ResponseResult assetsCreateImage(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/assets/images/batch")
    ResponseResult assetsBatchCreateImages(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/assets/images/{id}")
    ResponseResult assetsUpdateImage(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/assets/images/{id}")
    ResponseResult assetsDeleteImage(@PathVariable("id") Long id,
                                     @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/assets/knowledge")
    ResponseResult assetsKnowledge(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/assets/knowledge")
    ResponseResult assetsCreateKnowledge(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/assets/knowledge/{id}")
    ResponseResult assetsUpdateKnowledge(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/assets/knowledge/{id}")
    ResponseResult assetsDeleteKnowledge(@PathVariable("id") Long id,
                                         @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/assets/knowledge/{id}/default-context")
    ResponseResult assetsSetKnowledgeDefault(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping(value = "/assets/knowledge/import", consumes = "multipart/form-data")
    ResponseResult assetsPreviewKnowledgeImport(@RequestPart("files") MultipartFile file,
                                                @RequestParam(value = "defaultCategory", required = false) String defaultCategory,
                                                @RequestParam(value = "projectId", required = false) Long projectId);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/assets/knowledge/import/commit")
    ResponseResult assetsCommitKnowledgeImport(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/assets/knowledge/{id}/versions")
    ResponseResult assetsKnowledgeVersions(@PathVariable("id") Long id, @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/assets/knowledge/{id}/versions/{versionNo}/restore")
    ResponseResult assetsRestoreKnowledgeVersion(@PathVariable("id") Long id, @PathVariable("versionNo") Integer versionNo,
                                                 @RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/assets/knowledge/tags")
    ResponseResult assetsKnowledgeTags(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/assets/knowledge/tags")
    ResponseResult assetsSaveKnowledgeTag(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/assets/knowledge/tags/{id}")
    ResponseResult assetsDeleteKnowledgeTag(@PathVariable("id") Long id, @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/assets/knowledge/{id}/sharing")
    ResponseResult assetsKnowledgeSharing(@PathVariable("id") Long id, @RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/assets/knowledge/{id}/sharing")
    ResponseResult assetsSaveKnowledgeSharing(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/assets/knowledge/batch")
    ResponseResult assetsBatchKnowledge(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/assets/knowledge/dify")
    ResponseResult assetsDifyKnowledgeInfo();

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/assets/knowledge/dify/documents")
    ResponseResult assetsDifyDocuments(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/assets/knowledge/dify/documents/{documentId}")
    ResponseResult assetsDifyDocument(@PathVariable("documentId") String documentId, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/assets/knowledge/dify/sync")
    ResponseResult assetsSyncDifyKnowledge(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/assets/knowledge/dify/push-manuals")
    ResponseResult assetsPushBundledManualsToDify(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/content/article-settings")
    ResponseResult contentArticleSettings(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/article-settings")
    ResponseResult contentSaveArticleSettings(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/content/article-settings/{id}")
    ResponseResult contentDeleteArticleSettings(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/content/articles")
    ResponseResult contentArticles(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/content/articles/{id}")
    ResponseResult contentGetArticle(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/articles")
    ResponseResult contentCreateArticle(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/content/articles/{id}")
    ResponseResult contentUpdateArticle(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PatchMapping("/content/articles/{id}/status")
    ResponseResult contentUpdateArticleStatus(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/content/articles/{id}")
    ResponseResult contentDeleteArticle(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/articles/export")
    ResponseResult contentExportArticles(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/articles/import")
    ResponseResult contentImportArticles(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/research/base-keywords/stats")
    ResponseResult baseKeywordStats(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/research/distill")
    ResponseResult researchDistill(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/research/distill")
    ResponseResult researchCreateDistill(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/research/distill/expand")
    ResponseResult researchDistillExpand(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/research/distill/{id}")
    ResponseResult researchUpdateDistill(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/research/distill/batch-delete")
    ResponseResult researchBatchDeleteDistill(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/research/distill/{id}")
    ResponseResult researchDeleteDistill(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/research/distill/{id}/rerun")
    ResponseResult researchRerunDistill(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/research/distill/results")
    ResponseResult researchDistillResults(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/research/distill/results/{id}")
    ResponseResult researchDeleteDistillResult(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/research/distill/results/merge")
    ResponseResult researchMergeDistillResults(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/research/distill/results/export")
    ResponseResult researchExportDistillResults(@RequestParam(required = false) Map<String, Object> params);

    // --- 商业化 / 工具 / 设置 ---
    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/commerce/products")
    ResponseResult commerceProducts(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/commerce/products")
    ResponseResult commerceSaveProduct(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/commerce/products/{id}")
    ResponseResult commerceDeleteProduct(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/commerce/orders")
    ResponseResult commerceOrders(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/commerce/orders")
    ResponseResult commerceCreateOrder(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PatchMapping("/commerce/orders/{id}")
    ResponseResult commerceUpdateOrder(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/commerce/plans")
    ResponseResult commercePlans(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/commerce/upgrade")
    ResponseResult commerceUpgrade(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/tools/keyword-index")
    ResponseResult toolsKeywordIndex(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/tools/keyword-index/refresh")
    ResponseResult toolsRefreshKeywordIndex(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/tools/landing-page")
    ResponseResult toolsLandingPages(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/tools/landing-page/{id}")
    ResponseResult toolsGetLandingPage(@PathVariable("id") Long id, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/tools/landing-page")
    ResponseResult toolsGenerateLandingPage(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/tools/landing-page/{id}")
    ResponseResult toolsDeleteLandingPage(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/tools/landing-page/{id}/publish")
    ResponseResult toolsPublishLandingPage(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PutMapping("/tools/landing-page/{id}")
    ResponseResult toolsUpdateLandingPage(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/tools/landing-page/{id}/share")
    ResponseResult toolsGetLandingPageShareInfo(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/tools/landing-page/{id}/preview")
    ResponseResult toolsPreviewLandingPage(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/tools/landing-page/batch-generate")
    ResponseResult toolsBatchGenerateLandingPages(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/public/landing-page/{id}")
    ResponseResult publicLandingPage(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/settings/security")
    ResponseResult settingsSecurity(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PutMapping("/settings/security")
    ResponseResult settingsSaveSecurity(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/settings/system")
    ResponseResult settingsSystem(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PutMapping("/settings/system")
    ResponseResult settingsSaveSystem(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/settings/audit-log")
    ResponseResult settingsAuditLog(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/help/manual")
    ResponseResult helpManual(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PutMapping("/help/manual")
    ResponseResult saveHelpManual(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/settings/support-chat")
    ResponseResult settingsSupportChat(@RequestParam(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PutMapping("/settings/support-chat")
    ResponseResult settingsSaveSupportChat(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/content/ai-studio/rewrite")
    ResponseResult contentRewrite(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/goals/conversion/{id}")
    ResponseResult goalsDeleteConversion(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PatchMapping("/goals/conversion/{id}")
    ResponseResult goalsToggleConversion(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> body);

    // --- GEO 独立权限（hou_geo_*）---
    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/permission/user/menus")
    ResponseResult geoPermissionUserMenus(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/permission/user/permissions")
    ResponseResult geoPermissionUserPermissions(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/permission/user/check")
    ResponseResult geoPermissionUserCheck(@RequestBody Map<String, Object> body);

    // --- GEO 权限管理（hou_geo_* · 对标 mall /mall/perms）---
    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/perms/menu/list")
    ResponseResult geoPermMenuList(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/perms/menu/all")
    ResponseResult geoPermMenuAll(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/perms/menu/tree")
    ResponseResult geoPermMenuTree(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/perms/menu/detail")
    ResponseResult geoPermMenuDetail(@RequestParam("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/perms/menu/add")
    ResponseResult geoPermMenuAdd(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/perms/menu/edit")
    ResponseResult geoPermMenuEdit(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/perms/menu/delete")
    ResponseResult geoPermMenuDelete(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/perms/role/all")
    ResponseResult geoPermRoleAll(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/perms/role/lists")
    ResponseResult geoPermRoleLists(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/perms/role/detail")
    ResponseResult geoPermRoleDetail(@RequestParam("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/perms/role/add")
    ResponseResult geoPermRoleAdd(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/perms/role/edit")
    ResponseResult geoPermRoleEdit(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/perms/role/delete")
    ResponseResult geoPermRoleDelete(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/perms/user/lists")
    ResponseResult geoPermUserLists(@RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/perms/user/roles")
    ResponseResult geoPermUserRoles(@RequestParam("user_id") Long userId);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/perms/user/bind")
    ResponseResult geoPermUserBind(@RequestBody Map<String, Object> body);

    // --- AI Token 使用统计 ---
    /**
     * TODO 补充方法说明
/**
     * @param "tenantId" TODO
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/aiTokenUsage/stats")
    ResponseResult geoAiTokenUsageStats(@RequestParam(value = "tenantId", required = false) Long tenantId);

    /**
     * TODO 补充方法说明
/**
     * @param "tenantId" TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/aiTokenUsage/details")
    ResponseResult geoAiTokenUsageDetails(@RequestParam(value = "tenantId", required = false) Long tenantId,
                                          @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/aiTokenUsage/stats-query")
    ResponseResult geoAiTokenUsageStatsQuery(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/aiTokenUsage/admin-stats")
    ResponseResult geoAiTokenUsageAdminStats(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/aiTokenUsage/admin-details")
    ResponseResult geoAiTokenUsageAdminDetails(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/aiTokenUsage/admin-quotas")
    ResponseResult geoAiTokenUsageAdminQuotas(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/aiTokenUsage/admin-quota/set")
    ResponseResult geoAiTokenUsageAdminQuotaSet(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/support/chat")
    ResponseResult supportChat(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/support/status")
    ResponseResult supportStatus();

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/support/quota")
    ResponseResult supportQuota(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/support/usage")
    ResponseResult supportUsage(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/support/recharge/packages")
    ResponseResult supportRechargePackages();

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/support/recharge/create")
    ResponseResult supportRechargeCreate(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/support/recharge/attach-h5")
    ResponseResult supportRechargeAttachH5(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/support/recharge/confirm")
    ResponseResult supportRechargeConfirm(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/support/recharge/status")
    ResponseResult supportRechargeStatus(@RequestParam("orderNo") String orderNo);
}
