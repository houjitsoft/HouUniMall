package com.houjiang.aiagent.service;

import com.houjiang.aiagent.client.ModelClient;
import com.houjiang.aiagent.entity.AiGenerationRecord;
import com.houjiang.aiagent.entity.AiPrompt;
import com.houjiang.aiagent.mapper.AiGenerationRecordMapper;
import com.houjiang.aiagent.mapper.AiPromptMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 生成类能力（文本 / 图像 / 数据分析）的统一入口。
 *
 * <p>每条调用都会落 {@code hou_ai_generation_record}（可追溯）与 {@code hou_ai_usage_daily}（可归因）。
 * 明确不做的能力（文生图、图像处理、销售预测、智能推荐）返回「未开放」提示而不是 500，
 * 避免前端把「功能未实现」显示成系统错误。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiGenerateService {

    /** 各场景的系统提示词，约束模型不要越界（价格/库存一律由工具取数，模型禁止自算） */
    private static final Map<String, String> SCENE_PROMPTS = Map.of(
            "text_generate", "你是电商内容助手，按用户要求生成文案。不得编造商品参数、价格与库存。",
            "summary", "你是电商内容助手，请对用户给出的内容做要点摘要，保留关键数据与结论。",
            "translate", "你是电商内容助手，请准确翻译，保留商品术语与规格单位，不要意译数字。",
            "correct", "你是电商内容助手，请修正错别字与语病，输出修正后的全文，不要改变原意。",
            "data_analyze", "你是经营分析助手，只解读给定的数据，不得编造数字；不确定的地方请明确说明。");

    private final ModelClient modelClient;
    private final AiGenerationRecordMapper recordMapper;
    private final AiPromptMapper promptMapper;
    private final AiUsageService usageService;
    private final AiModelService modelService;
    private final AiPromptService promptService;

    /**
     * @param scene 业务场景，如 text_generate / summary / translate / correct / data_analyze
     */
    public Map<String, Object> generate(String scene, Map<String, Object> params) {
        String userPrompt = resolvePrompt(scene, params);
        if (!StringUtils.hasText(userPrompt)) {
            return failed(scene, "请输入内容", null, null);
        }
        ModelClient.ModelChatResult result = modelClient.chat(new ModelClient.ModelChatRequest()
                .setSystemPrompt(SCENE_PROMPTS.getOrDefault(scene, ""))
                .setUserPrompt(userPrompt)
                .setModel(str(params, "model"))
                .setTemperature(doubleOr(params, "temperature", 0.7))
                .setMaxTokens(intOr(params, "maxTokens", 2048))
                .setScene(scene));

        Long promptId = longOr(params, "promptId");
        if (promptId == null) {
            promptId = longOr(params, "prompt_id");
        }
        if (promptId != null) {
            promptService.incrUsage(promptId);
        }
        modelService.incrUsage(result.getModel());
        usageService.record(scene, result.getModel(), result.getPromptTokens(), result.getCompletionTokens(),
                result.getTotalTokens());
        saveRecord(scene, result, userPrompt);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("success", result.isSuccess());
        data.put("content", result.getContent());
        data.put("result", result.getContent());
        data.put("model", result.getModel());
        data.put("tokens", result.getTotalTokens());
        if (!result.isSuccess()) {
            data.put("message", result.getErrorMessage());
            data.put("errorCode", result.getErrorCode());
        }
        return data;
    }

    /**
     * 未开放场景的统一应答。返回 success=false + 明确原因，前端展示为提示而非报错。
     */
    public Map<String, Object> notSupported(String scene, String reason) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("success", false);
        data.put("errorCode", "FEATURE_NOT_ENABLED");
        data.put("message", reason);
        data.put("content", reason);
        data.put("scene", scene);
        return data;
    }

    private String resolvePrompt(String scene, Map<String, Object> params) {
        Long promptId = longOr(params, "promptId");
        if (promptId == null) {
            promptId = longOr(params, "prompt_id");
        }
        if (promptId != null) {
            AiPrompt template = promptMapper.selectById(promptId);
            if (template != null && StringUtils.hasText(template.getContent())) {
                String input = firstText(params, "content", "text", "prompt", "input");
                return template.getContent().replace("{input}", input == null ? "" : input);
            }
        }
        return firstText(params, "prompt", "content", "text", "input");
    }

    private void saveRecord(String scene, ModelClient.ModelChatResult result, String prompt) {
        try {
            AiGenerationRecord record = new AiGenerationRecord();
            record.setScene(scene);
            record.setModelName(result.getModel());
            record.setPrompt(prompt.length() > 4000 ? prompt.substring(0, 4000) : prompt);
            record.setResult(result.getContent() == null ? null
                    : (result.getContent().length() > 8000 ? result.getContent().substring(0, 8000) : result.getContent()));
            record.setStatus(result.isSuccess() ? 1 : 0);
            record.setErrorMsg(result.isSuccess() ? null : result.getErrorMessage());
            record.setTokens(result.getTotalTokens() == null ? 0 : result.getTotalTokens());
            record.setUserId(0L);
            record.setCreateTime(LocalDateTime.now());
            recordMapper.insert(record);
        } catch (Exception e) {
            log.warn("[aiagent] 生成记录落库失败 scene={} msg={}", scene, e.getMessage());
        }
    }

    private Map<String, Object> failed(String scene, String message, String code, String model) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("success", false);
        data.put("message", message);
        data.put("errorCode", code);
        data.put("content", message);
        data.put("model", model);
        data.put("scene", scene);
        return data;
    }

    private String firstText(Map<String, Object> params, String... keys) {
        for (String key : keys) {
            String value = str(params, key);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private String str(Map<String, Object> params, String key) {
        Object value = params == null ? null : params.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private Long longOr(Map<String, Object> params, String key) {
        Object value = params == null ? null : params.get(key);
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer intOr(Map<String, Object> params, String key, int fallback) {
        Long value = longOr(params, key);
        return value == null ? fallback : value.intValue();
    }

    private Double doubleOr(Map<String, Object> params, String key, double fallback) {
        Object value = params == null ? null : params.get(key);
        if (value == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
