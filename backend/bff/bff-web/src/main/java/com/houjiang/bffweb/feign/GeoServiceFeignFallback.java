package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Amos
 */
@Component
public class GeoServiceFeignFallback implements GeoServiceFeign {

    private static final String MSG = "GEO服务暂时不可用";

    private ResponseResult down() {
        return ResponseResult.fail(MSG);
    }

    @Override public ResponseResult geoRuns(Map<String, Object> params) { return down(); }
    @Override public ResponseResult geoRunDetail(Long id, Map<String, Object> params) { return down(); }
    @Override public ResponseResult geoRunEvents(Long id, Map<String, Object> params) { return down(); }
    @Override public ResponseResult cancelGeoRun(Long id, Map<String, Object> body) { return down(); }
    @Override public ResponseResult retryGeoRunItem(Long itemId, Map<String, Object> body) { return down(); }
    @Override public ResponseResult conversionEvents(Map<String, Object> params) { return down(); }
    @Override public ResponseResult conversionEventSummary(Map<String, Object> params) { return down(); }
    @Override public ResponseResult conversionEventDetail(Long id, Map<String, Object> params) { return down(); }
    @Override public ResponseResult revealConversionEventContact(Long id, Map<String, Object> body) { return down(); }
    @Override public ResponseResult deleteConversionEvent(Long id, Map<String, Object> params) { return down(); }
    @Override public ResponseResult importConversionEvents(org.springframework.web.multipart.MultipartFile file, Map<String, Object> params) { return down(); }
    @Override public org.springframework.http.ResponseEntity<byte[]> exportConversionEvents(Map<String, Object> params) {
        return org.springframework.http.ResponseEntity.status(503).body(new byte[0]);
    }
    @Override public org.springframework.http.ResponseEntity<byte[]> generatedMedia(String fileName) {
        return org.springframework.http.ResponseEntity.status(503).body(new byte[0]);
    }
    @Override public ResponseResult conversionTrackingConfig(Map<String, Object> params) { return down(); }
    @Override public ResponseResult saveConversionTrackingConfig(Map<String, Object> body) { return down(); }
    @Override public ResponseResult rotateConversionTrackingToken(Map<String, Object> body) { return down(); }
    @Override public ResponseResult rotateConversionWebhookSecret(Map<String, Object> body) { return down(); }

    @Override public ResponseResult creationTasks(Map<String, Object> params) { return down(); }
    @Override public ResponseResult saveCreationTask(Map<String, Object> body) { return down(); }
    @Override public ResponseResult deleteCreationTasks(Map<String, Object> body) { return down(); }
    @Override public ResponseResult imageCategories(Map<String, Object> params) { return down(); }
    @Override public ResponseResult createImageCategory(Map<String, Object> body) { return down(); }
    @Override public ResponseResult renameImageCategory(Map<String, Object> body) { return down(); }
    @Override public ResponseResult deleteImageCategory(Map<String, Object> params) { return down(); }
    @Override public ResponseResult brandProducts(Map<String, Object> params) { return down(); }
    @Override public ResponseResult createBrandProduct(Map<String, Object> body) { return down(); }
    @Override public ResponseResult updateBrandProduct(Long id, Map<String, Object> body) { return down(); }
    @Override public ResponseResult deleteBrandProduct(Long id, Map<String, Object> params) { return down(); }
    @Override public ResponseResult brandCompetitors(Map<String, Object> params) { return down(); }
    @Override public ResponseResult createBrandCompetitor(Map<String, Object> body) { return down(); }
    @Override public ResponseResult updateBrandCompetitor(Long id, Map<String, Object> body) { return down(); }
    @Override public ResponseResult deleteBrandCompetitor(Long id, Map<String, Object> params) { return down(); }
    @Override public ResponseResult triggerBrandCatalogAnalysis(Map<String, Object> body) { return down(); }
    @Override public ResponseResult brandCatalogAnalysisStatus(Map<String, Object> params) { return down(); }
    @Override public ResponseResult analyzeAttribution(Map<String, Object> body) { return down(); }
    @Override public ResponseResult technicalAuditRun(Map<String, Object> body) { return down(); }
    @Override public ResponseResult technicalSiteHealthCheckRun(Map<String, Object> body) { return down(); }
    @Override public ResponseResult technicalSiteHealthCwvCheckRun(Map<String, Object> body) { return down(); }
    @Override public ResponseResult researchConversionPreview(Map<String, Object> body) { return down(); }
    @Override public ResponseResult researchConversionCommit(Map<String, Object> body) { return down(); }
    @Override public ResponseResult createWorkflowTask(Map<String, Object> body) { return down(); }
    @Override public ResponseResult updateWorkflowTask(Long id, Map<String, Object> body) { return down(); }
    @Override public ResponseResult deleteWorkflowTask(Long id, Map<String, Object> params) { return down(); }
    @Override public ResponseResult videoCapabilities() { return down(); }
    @Override public ResponseResult digitalHumanApiServices() { return down(); }
    @Override public ResponseResult aliyunVideoTemplates(Map<String, Object> params) { return down(); }
    @Override public ResponseResult aliyunPublicSceneTemplates(Map<String, Object> params) { return down(); }
    @Override public ResponseResult copyAliyunPublicScene(Map<String, Object> body) { return down(); }
    @Override public ResponseResult aliyunVideoMaterials(Map<String, Object> params) { return down(); }
    @Override public ResponseResult syncAliyunVideoAudio(Map<String, Object> body) { return down(); }
    @Override public ResponseResult aliyunVideoTemplate(String templateId) { return down(); }
    @Override public ResponseResult aliyunVideoResources(Map<String, Object> params) { return down(); }
    @Override public ResponseResult syncAliyunBackground(Map<String, Object> body) { return down(); }
    @Override public ResponseResult syncAliyunSticker(Map<String, Object> body) { return down(); }
    @Override public ResponseResult deleteAliyunSticker(String stickerId, Map<String, Object> body) { return down(); }
    @Override public ResponseResult batchDeleteAliyunMaterials(Map<String, Object> body) { return down(); }
    @Override public ResponseResult createAliyunNoTrainAvatar(Map<String, Object> body) { return down(); }
    @Override public ResponseResult createAliyunTrainAvatar(Map<String, Object> body) { return down(); }
    @Override public ResponseResult aliyunTrainAvatarStatus(String avatarId) { return down(); }
    @Override public ResponseResult confirmAliyunTrainAvatar(String avatarId, Map<String, Object> body) { return down(); }
    @Override public ResponseResult aliyunVoices(Map<String, Object> params) { return down(); }
    @Override public ResponseResult aliyunPublicVoices() { return down(); }
    @Override public ResponseResult useAliyunPublicVoice(Map<String, Object> body) { return down(); }
    @Override public ResponseResult aliyunVoice(String voiceId) { return down(); }
    @Override public ResponseResult createAliyunVoice(Map<String, Object> body) { return down(); }
    @Override public ResponseResult createAliyunChatConfig(Map<String, Object> body) { return down(); }
    @Override public ResponseResult createAliyunChatSession(Map<String, Object> body) { return down(); }
    @Override public ResponseResult queryAliyunChatSessions(Map<String, Object> body) { return down(); }
    @Override public ResponseResult closeAliyunChatSessions(Map<String, Object> body) { return down(); }
    @Override public ResponseResult startAliyunAvatarLive(Map<String, Object> body) { return down(); }
    @Override public ResponseResult stopAliyunAvatarLive(String sessionId, Map<String, Object> body) { return down(); }
    @Override public ResponseResult queryAliyunAvatarLiveInstances(Map<String, Object> params) { return down(); }
    @Override public ResponseResult sendAliyunAvatarLiveText(String sessionId, Map<String, Object> body) { return down(); }
    @Override public ResponseResult generateVideoTitles(Map<String, Object> body) { return down(); }
    @Override public ResponseResult generateVideoBrief(Map<String, Object> body) { return down(); }
    @Override public ResponseResult generateVideoStoryboard(Map<String, Object> body) { return down(); }
    @Override public ResponseResult createVideoProject(Map<String, Object> body) { return down(); }
    @Override public ResponseResult videoProjects(Map<String, Object> params) { return down(); }
    @Override public ResponseResult videoProject(Long projectId, Map<String, Object> params) { return down(); }
    @Override public ResponseResult saveVideoProject(Long projectId, Map<String, Object> body) { return down(); }
    @Override public ResponseResult reviewVideoProject(Long projectId, Map<String, Object> body) { return down(); }
    @Override public ResponseResult preflightVideoProject(Long projectId, Map<String, Object> body) { return down(); }
    @Override public ResponseResult generateVideoProject(Long projectId, Map<String, Object> params, Map<String, Object> body) { return down(); }
    @Override public ResponseResult videoJobs(Map<String, Object> params) { return down(); }
    @Override public ResponseResult videoJob(Long id, Map<String, Object> params) { return down(); }
    @Override public ResponseResult videoJobDetail(Long id, Map<String, Object> params) { return down(); }
    @Override public ResponseResult cancelVideoJob(Long id, Map<String, Object> body) { return down(); }
    @Override public ResponseResult retryVideoJob(Long id, Map<String, Object> body) { return down(); }
    @Override public ResponseResult regenerateVideoJob(Long id, Map<String, Object> body) { return down(); }
    @Override public ResponseResult composeVideoJob(Long id, Map<String, Object> body) { return down(); }
    @Override public ResponseResult validateVideoJob(Long id, Map<String, Object> body) { return down(); }
    @Override public ResponseResult reviewVideoJob(Long id, Map<String, Object> body) { return down(); }
    @Override public ResponseResult handoffVideoJob(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult monitorVisibility(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult monitorVisibilityRefresh(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult monitorCitations(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult monitorCitationsSync(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult monitorQuestions(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult monitorQuestionScopeSyncPreview(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult monitorQuestionsFromScope(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult monitorToggleQuestion(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult monitorBatchMonitor(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult monitorImportQuestions(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult monitorQuestionsToTask(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult monitorCompetitor(Map<String, Object> params) { return down(); }
    @Override
    public ResponseResult createMonitorCompetitor(Map<String, Object> body) { return down(); }
    @Override
    public ResponseResult deleteMonitorCompetitor(Long id, Map<String, Object> params) { return down(); }
    @Override
    public ResponseResult monitorCompetitorGapToTask(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult monitorSentiment(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult monitorCloseAlert(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult monitorResolveHallucination(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult dashboardGeoKpis(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult dashboardOverview(Map<String, Object> params, int days) { return down(); }

    @Override
    public ResponseResult mediaResources(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult mediaResourceFavorite(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult mediaResourceSubmit(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult mediaResourceImport(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult mediaResourceSyncTimus(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult mediaResourceSyncTimusFull(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult mediaResourceSyncTimusFullStatus(String jobId, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult dashboard(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult templateList(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult pageList(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult validateJsonLd(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult templateDetail(Long id, Long projectId) { return down(); }

    @Override
    public ResponseResult templateCreate(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult templateUpdate(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult templateDelete(Long id, Long projectId) { return down(); }

    @Override
    public ResponseResult templateCopy(Long id, Long projectId) { return down(); }

    @Override
    public ResponseResult pageSchema(Long id, Long projectId) { return down(); }

    @Override
    public ResponseResult savePageSchema(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult researchKeywords(Map<String, Object> params) { return down(); }
    public ResponseResult researchPlateAOverview(Map<String, Object> params) { return down(); }
    @Override
    public ResponseResult researchAutoComplete(Map<String, Object> body) { return down(); }
    @Override
    public ResponseResult researchAutoCompleteStatus(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult researchDiscover(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult researchTopicClusters(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult researchIntent(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult analysisModels(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult analysisModelDiagnosis(String id, Map<String, Object> params) { return down(); }
    @Override
    public ResponseResult updateAnalysisModelSubscription(String id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult modelsAlias(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult modelsDiagnosisAlias(String id, Map<String, Object> params) { return down(); }
    @Override
    public ResponseResult updateModelsSubscriptionAlias(String id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult probeModelIntegration(String id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult saveModelRegistration(String id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult updateModelRegistrationStatus(String id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult reportsAlias(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult generateReportAlias(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult intentCoverageAlias(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult localAlias(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult linksInternalAlias(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult monitorImportHot(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult templatePreview(Long id, Long projectId) { return down(); }

    @Override
    public ResponseResult researchImportKeywords(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult researchImportClusters() { return down(); }

    @Override
    public ResponseResult researchCreateCluster(Map<String, Object> body) { return down(); }
    @Override
    public ResponseResult researchUpdateCluster(Long id, Map<String, Object> body) { return down(); }
    @Override
    public ResponseResult researchMoveClusterKeywords(Long id, Map<String, Object> body) { return down(); }
    @Override
    public ResponseResult researchMergeClusters(Map<String, Object> body) { return down(); }
    @Override
    public ResponseResult researchDeleteCluster(Long id, Map<String, Object> params) { return down(); }
    @Override
    public ResponseResult researchTopicPillarPlan(Map<String, Object> body) { return down(); }
    @Override
    public ResponseResult researchTopicSubscribe(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult researchIntentScan(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult researchImportIntentQuestions() { return down(); }

    @Override
    public ResponseResult strategyAuthorityScan(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult strategyAuthorityCitationAdopt(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult strategySandbox(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult strategyHowToGenerate(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult strategyFormat(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentGenerate(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentGenerateByKeyword(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentGenerateBatch(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentGenerateBatchActive(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult contentGenerateBatchDetail(String jobId) { return down(); }

    @Override
    public ResponseResult contentPlatformPreview(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentAdvanceToSchedulePool(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentExpandKeywords(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentSaveAiStudioSeed(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentVideoScript(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentVisual(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult hotTrendPlatform(String platform, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult hotTrendSyncLogs(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult hotTrendToQuestions(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult integrationsApi(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult mcpServers(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult saveMcpServer(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult deleteMcpServer(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult testMcpServer(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult batchPages(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult importSitemap(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentStyle(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentI18n(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentVisualComplete(Long id) { return down(); }

    @Override
    public ResponseResult contentTts(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentAvatar(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentChart(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentViralReplica(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult contentViralReplicaPrompt(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentViralReplicaReplicate(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentViralReplicaDelete(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult contentCreateAsset(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentUpdateAsset(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentDeleteAsset(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult contentAssetVersion(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult rankingSearchAiFeatures(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult experimentsAttribution(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult localGbp(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult localChinaMaps(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult localNap(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult localAiSummary(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult technicalSiteHealth(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult technicalAiDiscoverability(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult technicalCrawlability(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult technicalSemanticHtml(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult technicalPerformance(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult technicalJsCheck(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult technicalSemanticCheck(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult technicalMobileCheck(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult technicalLighthouseCheck(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult intelligenceIndustry(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult hotTrendSync(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult hotTrendBatchQuestionBank(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult analysisSource(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult analysisHealth(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult analysisReports(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult strategyAuthority(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult strategyModelAdapt(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult contentAiStudio(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult contentExpandModes(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult contentRefreshExpandModes(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentMultimodal(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult contentAssets(Map<String, Object> params) { return down(); }
    @Override public ResponseResult contentFreshnessScan(Map<String, Object> body) { return down(); }
    @Override public ResponseResult uploadContentAsset(org.springframework.web.multipart.MultipartFile file, Long projectId, String name, String tags) { return down(); }
    @Override public ResponseResult assetGenerationCandidates(Map<String, Object> params) { return down(); }
    @Override public ResponseResult importGeneratedAssets(Map<String, Object> body) { return down(); }
    @Override public ResponseResult assetVersions(Long assetId, Map<String, Object> params) { return down(); }
    @Override public ResponseResult createExplicitAssetVersion(Long assetId, Map<String, Object> body) { return down(); }
    @Override public ResponseResult rollbackAssetVersion(Long assetId, Long versionId, Map<String, Object> body) { return down(); }
    @Override public ResponseResult ignoreAssetFreshnessAlert(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult linksBacklinks(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult linksOutreach(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult linksInternal(Map<String, Object> params) { return down(); }

    @Override public ResponseResult addBacklink(Map<String, Object> body) { return down(); }
    @Override public ResponseResult createLinkOutreach(Map<String, Object> body) { return down(); }
    @Override public ResponseResult addInternalLinkAudit(Map<String, Object> body) { return down(); }
    @Override public ResponseResult exportBacklinks(Map<String, Object> params) { return down(); }
    @Override public ResponseResult exportLinkOutreach(Map<String, Object> params) { return down(); }
    @Override public ResponseResult exportInternalLinks(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult rankingAiEngines(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult refreshRanking(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult experimentsAbTests(Map<String, Object> params) { return down(); }

    @Override public ResponseResult createExperiment(Map<String, Object> body) { return down(); }
    @Override public ResponseResult runExperiment(Map<String, Object> body) { return down(); }
    @Override public ResponseResult deleteExperiment(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult localOverview(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult localNapReport(Map<String, Object> params) { return down(); }

    @Override public ResponseResult refreshLocalGbp(Map<String, Object> body) { return down(); }
    @Override public ResponseResult importLocalChinaMaps(Map<String, Object> body) { return down(); }
    @Override public ResponseResult checkLocalNap(Map<String, Object> body) { return down(); }
    @Override public ResponseResult refreshLocalAiSummary(Map<String, Object> params) { return down(); }
    @Override public ResponseResult localStores(Map<String, Object> p) { return down(); }
    @Override public org.springframework.http.ResponseEntity<byte[]> exportLocalStores(Map<String, Object> p) {
        return org.springframework.http.ResponseEntity.status(503)
            .contentType(new org.springframework.http.MediaType("text", "plain", java.nio.charset.StandardCharsets.UTF_8))
            .body(MSG.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    @Override public ResponseResult createLocalStore(Map<String, Object> b) { return down(); }
    @Override public ResponseResult updateLocalStore(Long id, Map<String, Object> b) { return down(); }
    @Override public ResponseResult deleteLocalStore(Long id, Map<String, Object> p) { return down(); }
    @Override public ResponseResult importLocalStores(Map<String, Object> b) { return down(); }
    @Override public ResponseResult localBindings(Map<String, Object> p) { return down(); }
    @Override public ResponseResult searchLocalBindingCandidates(Map<String, Object> p) { return down(); }
    @Override public ResponseResult saveLocalBinding(Map<String, Object> b) { return down(); }
    @Override public ResponseResult updateLocalBinding(Long id, Map<String, Object> b) { return down(); }
    @Override public ResponseResult deleteLocalBinding(Long id, Map<String, Object> p) { return down(); }
    @Override public ResponseResult localProviderStatus(Map<String, Object> p) { return down(); }
    @Override public ResponseResult createLocalSyncJob(Map<String, Object> b) { return down(); }
    @Override public ResponseResult localSyncJobs(Map<String, Object> p) { return down(); }
    @Override public ResponseResult localSyncJob(Long id, Map<String, Object> p) { return down(); }
    @Override public ResponseResult retryLocalSyncJob(Long id, Map<String, Object> b) { return down(); }
    @Override public ResponseResult importLocalGbp(Map<String, Object> b) { return down(); }
    @Override public ResponseResult createLocalNapAudit(Map<String, Object> b) { return down(); }
    @Override public ResponseResult localNapAudits(Map<String, Object> p) { return down(); }
    @Override public ResponseResult localNapIssues(Map<String, Object> p) { return down(); }
    @Override public ResponseResult approveLocalNapIssues(Map<String, Object> b) { return down(); }
    @Override public ResponseResult rejectLocalNapIssues(Map<String, Object> b) { return down(); }
    @Override public ResponseResult recheckLocalNapIssue(Long id, Map<String, Object> b) { return down(); }
    @Override public ResponseResult exportLocalNapIssues(Map<String, Object> p) { return down(); }
    @Override public ResponseResult localMonitorQueries(Map<String, Object> p) { return down(); }
    @Override public ResponseResult createLocalMonitorQuery(Map<String, Object> b) { return down(); }
    @Override public ResponseResult updateLocalMonitorQuery(Long id, Map<String, Object> b) { return down(); }
    @Override public ResponseResult deleteLocalMonitorQuery(Long id, Map<String, Object> p) { return down(); }
    @Override public ResponseResult importLocalMonitorQueries(Map<String, Object> b) { return down(); }
    @Override public ResponseResult localMonitorModels(Map<String, Object> p) { return down(); }
    @Override public ResponseResult localMonitorPlan(Map<String, Object> p) { return down(); }
    @Override public ResponseResult saveLocalMonitorPlan(Map<String, Object> b) { return down(); }
    @Override public ResponseResult localMonitorRuns(Map<String, Object> p) { return down(); }
    @Override public ResponseResult runLocalMonitor(Map<String, Object> b) { return down(); }
    @Override public ResponseResult localMonitorRun(Long id, Map<String, Object> p) { return down(); }
    @Override public ResponseResult retryLocalMonitorRun(Long id, Map<String, Object> b) { return down(); }
    @Override public ResponseResult importLocalMonitorResults(Map<String, Object> b) { return down(); }
    @Override public ResponseResult localMonitorResults(Map<String, Object> p) { return down(); }
    @Override public ResponseResult localMonitorResult(Long id, Map<String, Object> p) { return down(); }
    @Override public ResponseResult createLocalMonitorWorkflowTask(Long id, Map<String, Object> b) { return down(); }
    @Override public ResponseResult localMonitorTrends(Map<String, Object> p) { return down(); }

    @Override
    public ResponseResult technicalCrawlIndex(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult intelligenceCrawlerPolicy(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult intelligencePolicyChangelog(Map<String, Object> params) { return down(); }

    @Override public ResponseResult subscribeIntelligencePolicy(Map<String, Object> body) { return down(); }
    @Override public ResponseResult createIntelligenceIndustryReport(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult workflowTasks(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult workflowTeam(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult workflowAddTeamMember(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult workflowRemoveTeamMember(Long memberId, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult updateTeamMemberRole(Long memberId, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult hotTrendList(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult openApiKeys(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult settingsMonitor(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult settingsMonitorSave(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult distributeList(String platform, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult distributeDetail(String platform, Long id) { return down(); }

    @Override
    public ResponseResult distributeCreate(String platform, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult distributeUpdate(String platform, Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult distributeDelete(String platform, Long id) { return down(); }

    @Override
    public ResponseResult distributePublish(String platform, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult distributeGeoDetect(String platform, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult distributeSchedulePool(String platform, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult distributeImportFromArticle(String platform, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult distributeAiTopics(String platform, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult distributeAiGenerate(String platform, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult distributeAiAdaptTone(String platform, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult platformAuthList(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult platformAuthCreate(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult platformAuthDelete(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult platformAuthCheck(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult platformAuthSetDefault(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult platformAuthRefresh(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult platformAuthOauthUrl(String platform, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult platformAuthOauthCallback(String platform, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult platformAuthCookieLoginInfo(String platform, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult platformAuthCookieVerify(String platform, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult platformAuthCookieBind(String platform, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult platformAuthPermissions(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult platformAuthAuthLogs(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult platformAuthCredentialSave(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult platformAuthCredentials(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult platformAuthCredentialDelete(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult platformAuthRelogin(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult platformAuthReloginStatus(String sessionId, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult platformAuthReloginCancel(String sessionId, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult platformAuthReloginSmsPhone(String sessionId, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult platformAuthReloginSmsCode(String sessionId, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult platformConfigGet(String platform, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult platformConfigSave(String platform, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult platformConfigTest(String platform, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult omniPreview(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult omniValidate(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult omniPublish(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult omniJobs(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult omniJobDetail(String id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult omniJobRetry(String id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult pageTab(String key, Long projectId, Long projectIdHeader) { return down(); }

    @Override
    public ResponseResult pageTabRefresh(String key, Long projectId, Long projectIdHeader, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult pageTabExecute(String key, Long projectId, Long projectIdHeader, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult workflowProjects(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult workflowCreateProject(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult workflowUpdateProject(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult deleteWorkflowProject(Long id) { return down(); }

    @Override
    public ResponseResult workflowBrandSettings(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult workflowSaveBrandSettings(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult webhookList(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult webhookSave(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult webhookDelete(Long id) { return down(); }

    @Override
    public ResponseResult webhookTest(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult goalsConversion(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult goalsSaveConversion(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult indexingReports(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult indexingSync(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult indexingReportsExport(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult assetsImages(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult assetsCreateImage(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult assetsBatchCreateImages(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult assetsUpdateImage(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult assetsDeleteImage(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult assetsKnowledge(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult assetsCreateKnowledge(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult assetsUpdateKnowledge(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult assetsDeleteKnowledge(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult assetsSetKnowledgeDefault(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult assetsPreviewKnowledgeImport(org.springframework.web.multipart.MultipartFile file, String defaultCategory, Long projectId) { return down(); }

    @Override
    public ResponseResult assetsCommitKnowledgeImport(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult assetsKnowledgeVersions(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult assetsRestoreKnowledgeVersion(Long id, Integer versionNo, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult assetsKnowledgeTags(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult assetsSaveKnowledgeTag(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult assetsDeleteKnowledgeTag(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult assetsKnowledgeSharing(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult assetsSaveKnowledgeSharing(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult assetsBatchKnowledge(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult assetsDifyKnowledgeInfo() { return down(); }

    @Override
    public ResponseResult assetsDifyDocuments(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult assetsDifyDocument(String documentId, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult assetsSyncDifyKnowledge(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult assetsPushBundledManualsToDify(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentArticleSettings(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult contentSaveArticleSettings(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentDeleteArticleSettings(Long id) { return down(); }

    @Override
    public ResponseResult contentArticles(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult contentGetArticle(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult contentCreateArticle(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentUpdateArticle(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentUpdateArticleStatus(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentDeleteArticle(Long id) { return down(); }

    @Override
    public ResponseResult contentExportArticles(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentImportArticles(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult baseKeywordStats(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult researchDistill(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult researchCreateDistill(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult researchDistillExpand(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult researchUpdateDistill(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult researchBatchDeleteDistill(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult researchDeleteDistill(Long id) { return down(); }
    public ResponseResult researchRerunDistill(Long id) { return down(); }

    @Override
    public ResponseResult researchDistillResults(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult researchDeleteDistillResult(Long id) { return down(); }

    @Override
    public ResponseResult researchMergeDistillResults(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult researchExportDistillResults(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult commerceProducts(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult commerceSaveProduct(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult commerceDeleteProduct(Long id) { return down(); }

    @Override
    public ResponseResult commerceOrders(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult commerceCreateOrder(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult commerceUpdateOrder(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult commercePlans(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult commerceUpgrade(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult toolsKeywordIndex(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult toolsRefreshKeywordIndex(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult toolsLandingPages(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult toolsGetLandingPage(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult toolsGenerateLandingPage(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult toolsDeleteLandingPage(Long id) { return down(); }

    @Override
    public ResponseResult toolsPublishLandingPage(Long id) { return down(); }

    @Override
    public ResponseResult toolsUpdateLandingPage(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult toolsGetLandingPageShareInfo(Long id) { return down(); }

    @Override
    public ResponseResult toolsPreviewLandingPage(Long id) { return down(); }

    @Override
    public ResponseResult toolsBatchGenerateLandingPages(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult publicLandingPage(Long id) { return down(); }

    @Override
    public ResponseResult settingsSecurity(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult settingsSaveSecurity(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult settingsSystem(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult settingsSaveSystem(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult settingsAuditLog(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult helpManual(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult saveHelpManual(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult settingsSupportChat(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult settingsSaveSupportChat(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult contentRewrite(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult goalsDeleteConversion(Long id) { return down(); }

    @Override
    public ResponseResult goalsToggleConversion(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult geoPermissionUserMenus(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult geoPermissionUserPermissions(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult geoPermissionUserCheck(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult geoPermMenuList(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult geoPermMenuAll(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult geoPermMenuTree(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult geoPermMenuDetail(Integer id) { return down(); }

    @Override
    public ResponseResult geoPermMenuAdd(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult geoPermMenuEdit(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult geoPermMenuDelete(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult geoPermRoleAll(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult geoPermRoleLists(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult geoPermRoleDetail(Integer id) { return down(); }

    @Override
    public ResponseResult geoPermRoleAdd(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult geoPermRoleEdit(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult geoPermRoleDelete(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult geoPermUserLists(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult geoPermUserRoles(Long userId) { return down(); }

    @Override
    public ResponseResult geoPermUserBind(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult accountProfile(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult researchCompetitorGap(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult researchKeywordTrend(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult researchI18nMap(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult researchExportKeywords(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult openApiKeysCreate(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult openApiKeyRevoke(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult platformAuthAccounts(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult settingsPlatformAuthAlias(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult settingsPlatformAuthCreateAlias(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult omniContext(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult omniDrafts(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult omniImportSource(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult omniJobLogs(String id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult omniFailLogs(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult riskEvents(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult updateRiskEvent(String id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult publishPlanList(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult publishPlanCalendar(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult publishPlanSchedulePool(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult publishPlanPlatformQuotas(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult publishPlanDetail(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult publishPlanGetOrCreate(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult publishPlanSaveItems(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult publishPlanAutoFill(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult publishPlanAutoFillYear(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult publishPlanAutoFillPreview(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult publishPlanActivate(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult publishPlanActivateYear(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult publishPlanGenerateScheduledSend(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult publishPlanPause(Long id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult publishPlanRetryTask(Long taskId, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult publishPlanSavePlatformQuotas(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult distributeJobsAlias(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult distributeJobDetailAlias(String id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult distributeJobRetryAlias(String id, Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult geoAiTokenUsageStats(Long tenantId) { return down(); }

    @Override
    public ResponseResult geoAiTokenUsageDetails(Long tenantId, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult geoAiTokenUsageStatsQuery(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult geoAiTokenUsageAdminStats(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult geoAiTokenUsageAdminDetails(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult geoAiTokenUsageAdminQuotas(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult geoAiTokenUsageAdminQuotaSet(Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult supportChat(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult supportStatus() { return down(); }

    @Override
    public ResponseResult supportQuota(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult supportUsage(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult supportRechargePackages() { return down(); }

    @Override
    public ResponseResult supportRechargeCreate(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult supportRechargeAttachH5(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult supportRechargeConfirm(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult supportRechargeStatus(String orderNo) { return down(); }

    @Override
    public ResponseResult suggestMonitorKeywords(Map<String, Object> body) { return down(); }

    @Override
    public ResponseResult workflowMonitorScope(Long id, Map<String, Object> params) { return down(); }

    @Override
    public ResponseResult workflowSaveMonitorScope(Long id, Map<String, Object> body) { return down(); }
}
