package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI服务Feign客户端
/**
 * 
 * @description 调用paas中台AI服务(ai)的接口
 * @author Amos
 * @date 2024-12-20
 */
@FeignClient(name = "ai", path = "/", fallback = AiServiceFeignFallback.class, contextId = "ai")
public interface AiServiceFeign {

    // ================= AI管理接口 =================

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/ai/manage/statistics")
    ResponseResult getAiStatistics(@RequestParam(required = false) Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/manage/list")
    ResponseResult queryAiList(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/manage/detail")
    ResponseResult getAiDetail(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param aiData TODO
     * @return TODO
     */
    @PostMapping("/ai/manage/create")
    ResponseResult createAi(@RequestBody Map<String, Object> aiData);

    /**
     * TODO 补充方法说明
/**
     * @param aiData TODO
     * @return TODO
     */
    @PostMapping("/ai/manage/update")
    ResponseResult updateAi(@RequestBody Map<String, Object> aiData);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/manage/delete")
    ResponseResult deleteAi(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/manage/pause")
    ResponseResult pauseAi(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/manage/resume")
    ResponseResult resumeAi(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/manage/copy")
    ResponseResult copyAi(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/ai/manage/types")
    ResponseResult getAiTypes();

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/manage/batch/status")
    ResponseResult batchUpdateStatus(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/manage/generation/list")
    ResponseResult listGenerationRecords(@RequestBody Map<String, Object> param);

    // ================= 文本生成接口 =================

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/text/generate")
    ResponseResult generateText(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/text/summary")
    ResponseResult textSummary(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/text/translate")
    ResponseResult textTranslate(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/text/correct")
    ResponseResult textCorrect(@RequestBody Map<String, Object> param);

    // ================= 图像生成接口 =================

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/image/generate")
    ResponseResult generateImage(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/image/recognize")
    ResponseResult recognizeImage(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/image/process")
    ResponseResult processImage(@RequestBody Map<String, Object> param);

    // ================= 智能客服接口 =================

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/chat/chat")
    ResponseResult chat(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/chat/history")
    ResponseResult getChatHistory(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/chat/clear")
    ResponseResult clearChatHistory(@RequestBody Map<String, Object> param);

    // ================= 数据分析接口 =================

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/data/analyze")
    ResponseResult dataAnalyze(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/data/predict")
    ResponseResult dataPredict(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/data/recommend")
    ResponseResult dataRecommend(@RequestBody Map<String, Object> param);

    // ================= 模型与提示词 =================

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/ai/model/enabled")
    ResponseResult getEnabledModels();

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/prompt/list")
    ResponseResult listPrompts(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/prompt/detail")
    ResponseResult getPromptDetail(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param data TODO
     * @return TODO
     */
    @PostMapping("/ai/prompt/create")
    ResponseResult createPrompt(@RequestBody Map<String, Object> data);

    /**
     * TODO 补充方法说明
/**
     * @param data TODO
     * @return TODO
     */
    @PostMapping("/ai/prompt/update")
    ResponseResult updatePrompt(@RequestBody Map<String, Object> data);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/prompt/delete")
    ResponseResult deletePrompt(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/ai/prompt/category/list")
    ResponseResult listPromptCategories();

    /**
     * TODO 补充方法说明
/**
     * @param data TODO
     * @return TODO
     */
    @PostMapping("/ai/prompt/category/create")
    ResponseResult createPromptCategory(@RequestBody Map<String, Object> data);

    /**
     * TODO 补充方法说明
/**
     * @param data TODO
     * @return TODO
     */
    @PostMapping("/ai/prompt/category/update")
    ResponseResult updatePromptCategory(@RequestBody Map<String, Object> data);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/ai/prompt/category/delete")
    ResponseResult deletePromptCategory(@RequestBody Map<String, Object> param);

    // ================= AI配置接口 =================

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/ai/config/all")
    ResponseResult getConfigAll();

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/ai/config/list")
    ResponseResult listConfig();

    /**
     * TODO 补充方法说明
/**
     * @param data TODO
     * @return TODO
     */
    @PostMapping("/ai/config/save")
    ResponseResult saveConfig(@RequestBody Map<String, Object> data);

    /**
     * TODO 补充方法说明
/**
     * @param data TODO
     * @return TODO
     */
    @PostMapping("/ai/config/batch")
    ResponseResult batchSaveConfig(@RequestBody Map<String, Object> data);

    // ================= AI 中台：调用计量与余额配额 =================

    @GetMapping("/ai/usage/summary")
    ResponseResult getUsageSummary();

    @PostMapping("/ai/usage/records")
    ResponseResult listUsageRecords(@RequestBody(required = false) Map<String, Object> param);

    @PostMapping("/ai/balance/list")
    ResponseResult listBalance(@RequestBody Map<String, Object> param);

    @GetMapping("/ai/balance/detail")
    ResponseResult getBalanceDetail(@RequestParam("subjectType") String subjectType,
                                    @RequestParam("subjectKey") String subjectKey);

    @PostMapping("/ai/balance/recharge")
    ResponseResult rechargeBalance(@RequestBody Map<String, Object> param);

    @PostMapping("/ai/balance/consume")
    ResponseResult consumeBalance(@RequestBody Map<String, Object> param);

    // ================= AI 中台：智能体（Agent）统一管理 =================

    @PostMapping("/ai/agent/list")
    ResponseResult listAgents(@RequestBody(required = false) Map<String, Object> param);

    @GetMapping("/ai/agent/detail")
    ResponseResult getAgentDetail(@RequestParam("code") String code);

    @PostMapping("/ai/agent/save")
    ResponseResult saveAgent(@RequestBody Map<String, Object> param);

    @PostMapping("/ai/agent/status")
    ResponseResult updateAgentStatus(@RequestBody Map<String, Object> param);

    @PostMapping("/ai/agent/delete")
    ResponseResult deleteAgent(@RequestBody Map<String, Object> param);

    @GetMapping("/ai/agent/scenes")
    ResponseResult getAgentScenes();

    @PostMapping("/ai/agent/invoke/logs")
    ResponseResult listAgentInvokeLogs(@RequestBody(required = false) Map<String, Object> param);

    @GetMapping("/ai/agent/config")
    ResponseResult getAgentConfig(@RequestParam("code") String code);

    @PostMapping("/ai/agent/invoke/log")
    ResponseResult recordAgentInvoke(@RequestBody Map<String, Object> param);

    // ================= AI 工作流（可视化编排） =================

    @PostMapping("/ai/workflow/list")
    ResponseResult listWorkflows(@RequestBody(required = false) Map<String, Object> param);

    @GetMapping("/ai/workflow/detail")
    ResponseResult getWorkflowDetail(@RequestParam("code") String code);

    @PostMapping("/ai/workflow/save")
    ResponseResult saveWorkflow(@RequestBody Map<String, Object> param);

    @PostMapping("/ai/workflow/copy")
    ResponseResult copyWorkflow(@RequestBody Map<String, Object> param);

    @PostMapping("/ai/workflow/delete")
    ResponseResult deleteWorkflow(@RequestBody Map<String, Object> param);

    @PostMapping("/ai/workflow/status")
    ResponseResult updateWorkflowStatus(@RequestBody Map<String, Object> param);

    @PostMapping("/ai/workflow/validate")
    ResponseResult validateWorkflow(@RequestBody Map<String, Object> param);

    @PostMapping("/ai/workflow/run")
    ResponseResult runWorkflow(@RequestBody Map<String, Object> param);

    @PostMapping("/ai/workflow/runs")
    ResponseResult listWorkflowRuns(@RequestBody(required = false) Map<String, Object> param);

    @GetMapping("/ai/workflow/run/detail")
    ResponseResult getWorkflowRunDetail(@RequestParam("runNo") String runNo);

    // ================= AI 中台：知识库（外部 WeKnora） =================

    @GetMapping("/ai/knowledge/status")
    ResponseResult getKnowledgeStatus();

    @PostMapping("/ai/knowledge/bases")
    ResponseResult listKnowledgeBases();

    @PostMapping("/ai/knowledge/documents")
    ResponseResult listKnowledgeDocuments(@RequestBody Map<String, Object> param);

    @PostMapping("/ai/knowledge/search")
    ResponseResult searchKnowledge(@RequestBody Map<String, Object> param);

    @PostMapping("/ai/knowledge/chat")
    ResponseResult chatWithKnowledge(@RequestBody Map<String, Object> param);
}
