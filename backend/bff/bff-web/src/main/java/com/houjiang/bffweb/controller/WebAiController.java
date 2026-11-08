package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.AiServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI服务BFF适配器
/**
 * 只做请求转发，不包含业务逻辑
 * @author Amos
 */
@RestController
@RequestMapping("/web/ai")
public class WebAiController {

    @Autowired
    private AiServiceFeign aiServiceFeign;

    /** ================= AI管理接口 ================= */

    @PostMapping("/manage/statistics")
    public ResponseResult getStatistics() {
        return aiServiceFeign.getAiStatistics(null);
    }

    @PostMapping("/manage/list")
    public ResponseResult getAiList(@RequestBody Map<String, Object> queryParams) {
        return aiServiceFeign.queryAiList(queryParams);
    }

    @PostMapping("/manage/detail")
    public ResponseResult getAiDetail(@RequestBody Map<String, Object> param) {
        return aiServiceFeign.getAiDetail(param);
    }

    @PostMapping("/manage/create")
    public ResponseResult createAi(@RequestBody Map<String, Object> aiData) {
        return aiServiceFeign.createAi(aiData);
    }

    @PostMapping("/manage/update")
    public ResponseResult updateAi(@RequestBody Map<String, Object> aiData) {
        return aiServiceFeign.updateAi(aiData);
    }

    @PostMapping("/manage/delete")
    public ResponseResult deleteAi(@RequestBody Map<String, Object> param) {
        return aiServiceFeign.deleteAi(param);
    }

    @PostMapping("/manage/pause")
    public ResponseResult pauseAi(@RequestBody Map<String, Object> param) {
        return aiServiceFeign.pauseAi(param);
    }

    @PostMapping("/manage/resume")
    public ResponseResult resumeAi(@RequestBody Map<String, Object> param) {
        return aiServiceFeign.resumeAi(param);
    }

    @PostMapping("/manage/copy")
    public ResponseResult copyAi(@RequestBody Map<String, Object> param) {
        return aiServiceFeign.copyAi(param);
    }

    @GetMapping("/manage/types")
    public ResponseResult getAiTypes() {
        return aiServiceFeign.getAiTypes();
    }

    @PostMapping("/manage/batch/status")
    public ResponseResult batchUpdateStatus(@RequestBody Map<String, Object> param) {
        return aiServiceFeign.batchUpdateStatus(param);
    }

    @PostMapping("/manage/generation/list")
    public ResponseResult listGenerationRecords(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.listGenerationRecords(params);
    }

    /** ================= AI应用接口 ================= */

    @PostMapping("/chat")
    public ResponseResult chat(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.chat(params);
    }

    @PostMapping("/chat/history")
    public ResponseResult getChatHistory(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.getChatHistory(params);
    }

    @PostMapping("/chat/clear")
    public ResponseResult clearChatHistory(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.clearChatHistory(params);
    }

    @PostMapping("/text/generate")
    public ResponseResult generateText(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.generateText(params);
    }

    @PostMapping("/text/summary")
    public ResponseResult textSummary(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.textSummary(params);
    }

    @PostMapping("/text/translate")
    public ResponseResult textTranslate(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.textTranslate(params);
    }

    @PostMapping("/text/correct")
    public ResponseResult textCorrect(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.textCorrect(params);
    }

    @PostMapping("/image/generate")
    public ResponseResult generateImage(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.generateImage(params);
    }

    @PostMapping("/image/recognize")
    public ResponseResult recognizeImage(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.recognizeImage(params);
    }

    @PostMapping("/image/process")
    public ResponseResult processImage(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.processImage(params);
    }

    /** ================= 数据分析接口 ================= */

    @PostMapping("/data/analyze")
    public ResponseResult dataAnalyze(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.dataAnalyze(params);
    }

    @PostMapping("/data/predict")
    public ResponseResult dataPredict(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.dataPredict(params);
    }

    @PostMapping("/data/recommend")
    public ResponseResult dataRecommend(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.dataRecommend(params);
    }

    /** ================= 模型与提示词 ================= */

    @GetMapping("/model/enabled")
    public ResponseResult getEnabledModels() {
        return aiServiceFeign.getEnabledModels();
    }

    @PostMapping("/prompt/list")
    public ResponseResult listPrompts(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.listPrompts(params);
    }

    @PostMapping("/prompt/detail")
    public ResponseResult getPromptDetail(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.getPromptDetail(params);
    }

    @PostMapping("/prompt/create")
    public ResponseResult createPrompt(@RequestBody Map<String, Object> data) {
        return aiServiceFeign.createPrompt(data);
    }

    @PostMapping("/prompt/update")
    public ResponseResult updatePrompt(@RequestBody Map<String, Object> data) {
        return aiServiceFeign.updatePrompt(data);
    }

    @PostMapping("/prompt/delete")
    public ResponseResult deletePrompt(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.deletePrompt(params);
    }

    @GetMapping("/prompt/category/list")
    public ResponseResult listPromptCategories() {
        return aiServiceFeign.listPromptCategories();
    }

    @PostMapping("/prompt/category/create")
    public ResponseResult createPromptCategory(@RequestBody Map<String, Object> data) {
        return aiServiceFeign.createPromptCategory(data);
    }

    @PostMapping("/prompt/category/update")
    public ResponseResult updatePromptCategory(@RequestBody Map<String, Object> data) {
        return aiServiceFeign.updatePromptCategory(data);
    }

    @PostMapping("/prompt/category/delete")
    public ResponseResult deletePromptCategory(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.deletePromptCategory(params);
    }

    /** ================= AI配置接口 ================= */

    @GetMapping("/config/all")
    public ResponseResult getConfigAll() {
        return aiServiceFeign.getConfigAll();
    }

    @PostMapping("/config/list")
    public ResponseResult listConfig() {
        return aiServiceFeign.listConfig();
    }

    @PostMapping("/config/save")
    public ResponseResult saveConfig(@RequestBody Map<String, Object> data) {
        return aiServiceFeign.saveConfig(data);
    }

    @PostMapping("/config/batch")
    public ResponseResult batchSaveConfig(@RequestBody Map<String, Object> data) {
        return aiServiceFeign.batchSaveConfig(data);
    }

    /** ================= AI 中台：调用计量与余额配额 ================= */

    @GetMapping("/usage/summary")
    public ResponseResult getUsageSummary() {
        return aiServiceFeign.getUsageSummary();
    }

    @PostMapping("/usage/records")
    public ResponseResult listUsageRecords(@RequestBody(required = false) Map<String, Object> params) {
        return aiServiceFeign.listUsageRecords(params);
    }

    @PostMapping("/balance/list")
    public ResponseResult listBalance(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.listBalance(params);
    }

    @GetMapping("/balance/detail")
    public ResponseResult getBalanceDetail(@RequestParam("subjectType") String subjectType,
                                           @RequestParam("subjectKey") String subjectKey) {
        return aiServiceFeign.getBalanceDetail(subjectType, subjectKey);
    }

    @PostMapping("/balance/recharge")
    public ResponseResult rechargeBalance(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.rechargeBalance(params);
    }

    @PostMapping("/balance/consume")
    public ResponseResult consumeBalance(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.consumeBalance(params);
    }

    /** ================= AI 中台：智能体（Agent）统一管理 ================= */

    @PostMapping("/agent/list")
    public ResponseResult listAgents(@RequestBody(required = false) Map<String, Object> params) {
        return aiServiceFeign.listAgents(params);
    }

    @GetMapping("/agent/detail")
    public ResponseResult getAgentDetail(@RequestParam("code") String code) {
        return aiServiceFeign.getAgentDetail(code);
    }

    @PostMapping("/agent/save")
    public ResponseResult saveAgent(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.saveAgent(params);
    }

    @PostMapping("/agent/status")
    public ResponseResult updateAgentStatus(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.updateAgentStatus(params);
    }

    @PostMapping("/agent/delete")
    public ResponseResult deleteAgent(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.deleteAgent(params);
    }

    @GetMapping("/agent/scenes")
    public ResponseResult getAgentScenes() {
        return aiServiceFeign.getAgentScenes();
    }

    @PostMapping("/agent/invoke/logs")
    public ResponseResult listAgentInvokeLogs(@RequestBody(required = false) Map<String, Object> params) {
        return aiServiceFeign.listAgentInvokeLogs(params);
    }

    /** 前台按编码取智能体可执行配置 */
    @GetMapping("/agent/config")
    public ResponseResult getAgentConfig(@RequestParam("code") String code) {
        return aiServiceFeign.getAgentConfig(code);
    }

    /** 前台上报一次智能体调用结果（用于后台「调用记录」留痕） */
    @PostMapping("/agent/invoke/log")
    public ResponseResult recordAgentInvoke(@RequestBody(required = false) Map<String, Object> params) {
        return aiServiceFeign.recordAgentInvoke(params == null ? Map.of() : params);
    }

    /** ================= AI 工作流（可视化编排） ================= */

    @PostMapping("/workflow/list")
    public ResponseResult listWorkflows(@RequestBody(required = false) Map<String, Object> params) {
        return aiServiceFeign.listWorkflows(params == null ? Map.of() : params);
    }

    @GetMapping("/workflow/detail")
    public ResponseResult getWorkflowDetail(@RequestParam("code") String code) {
        return aiServiceFeign.getWorkflowDetail(code);
    }

    @PostMapping("/workflow/save")
    public ResponseResult saveWorkflow(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.saveWorkflow(params);
    }

    @PostMapping("/workflow/copy")
    public ResponseResult copyWorkflow(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.copyWorkflow(params);
    }

    @PostMapping("/workflow/delete")
    public ResponseResult deleteWorkflow(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.deleteWorkflow(params);
    }

    @PostMapping("/workflow/status")
    public ResponseResult updateWorkflowStatus(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.updateWorkflowStatus(params);
    }

    @PostMapping("/workflow/validate")
    public ResponseResult validateWorkflow(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.validateWorkflow(params);
    }

    @PostMapping("/workflow/run")
    public ResponseResult runWorkflow(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.runWorkflow(params);
    }

    @PostMapping("/workflow/runs")
    public ResponseResult listWorkflowRuns(@RequestBody(required = false) Map<String, Object> params) {
        return aiServiceFeign.listWorkflowRuns(params == null ? Map.of() : params);
    }

    @GetMapping("/workflow/run/detail")
    public ResponseResult getWorkflowRunDetail(@RequestParam("runNo") String runNo) {
        return aiServiceFeign.getWorkflowRunDetail(runNo);
    }

    /** ================= 知识库（外部 WeKnora 服务） ================= */

    @GetMapping("/knowledge/status")
    public ResponseResult getKnowledgeStatus() {
        return aiServiceFeign.getKnowledgeStatus();
    }

    @PostMapping("/knowledge/bases")
    public ResponseResult listKnowledgeBases() {
        return aiServiceFeign.listKnowledgeBases();
    }

    @PostMapping("/knowledge/documents")
    public ResponseResult listKnowledgeDocuments(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.listKnowledgeDocuments(params);
    }

    @PostMapping("/knowledge/search")
    public ResponseResult searchKnowledge(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.searchKnowledge(params);
    }

    @PostMapping("/knowledge/chat")
    public ResponseResult chatWithKnowledge(@RequestBody Map<String, Object> params) {
        return aiServiceFeign.chatWithKnowledge(params);
    }
}
