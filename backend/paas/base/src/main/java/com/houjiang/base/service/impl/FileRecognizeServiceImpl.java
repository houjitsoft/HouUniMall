package com.houjiang.base.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.houjiang.base.client.DifyApiClient;
import com.houjiang.base.client.OpenAiCompatibleClient;
import com.houjiang.base.mapper.BaseAiConfigMapper;
import com.houjiang.base.dto.fileimport.FileRecognizeCatalogEntry;
import com.houjiang.base.service.FileRecognizeCatalog;
import com.houjiang.base.service.FileRecognizeService;
import com.houjiang.base.support.FileRecognizeRuleHelper;
import com.houjiang.base.vo.BaseAiConfigVO;
import com.houjiang.commons.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 文件智能识别：优先系统默认 AI 模型（OpenAI 兼容），失败时规则兜底。不再走 Dify。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileRecognizeServiceImpl implements FileRecognizeService {

    private static final Set<String> VALID_TYPES = Set.of(
        "contract", "receivable", "payable", "goods", "document", "invoice", "unknown"
    );

    private final BaseAiConfigMapper aiConfigMapper;
    private final OpenAiCompatibleClient openAiCompatibleClient;
    private final DifyApiClient difyApiClient;
    private final FileRecognizeCatalog importCatalog = new FileRecognizeCatalog();

    @Override
    public ResponseResult recognize(MultipartFile file, String description, String token) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseResult.fail("文件不能为空");
            }
            String fileName = file.getOriginalFilename();
            byte[] fileBytes = file.getBytes();

            Map<String, Object> result = trySystemAiRecognize(file, description);
            if (result == null) {
                result = FileRecognizeRuleHelper.recognize(fileName, fileBytes, description);
            }

            result.put("fileName", fileName);
            result.put("fileSize", fileBytes.length);
            result.put("contentType", file.getContentType());
            if ("rule".equals(result.get("source")) && isImageFile(file)
                    && !result.containsKey("recognitionWarning")) {
                result.put("recognitionWarning", "当前未配置可用的视觉/OCR模型，未完成图片文字识别，请先配置视觉模型");
            }
            normalizeType(result);
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("文件识别失败", e);
            return ResponseResult.fail("文件识别失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseResult recognizeForImport(MultipartFile file, String systemKey, String description, String token) {
        if (systemKey == null || systemKey.isBlank()) {
            return ResponseResult.fail(422, "请选择业务系统");
        }
        if (description == null || description.trim().isEmpty()) {
            return ResponseResult.fail(422, "描述不能为空");
        }
        if (description.trim().length() > 500) {
            return ResponseResult.fail(422, "描述不能超过500字");
        }
        if (importCatalog.entriesForSystem(systemKey.trim()).isEmpty()) {
            return ResponseResult.fail(422, "业务系统不在允许目录中");
        }
        if (file == null || file.isEmpty()) {
            return ResponseResult.fail(422, "文件不能为空");
        }
        try {
            return ResponseResult.success(recognizeImportWithAi(file, systemKey.trim(), description.trim()));
        } catch (Exception ex) {
            log.error("智能入库识别失败", ex);
            return ResponseResult.fail(500, "智能入库识别失败: " + safeErr(ex.getMessage()));
        }
    }

    private Map<String, Object> recognizeImportWithAi(MultipartFile file, String systemKey, String description) {
        List<BaseAiConfigVO> candidates;
        try {
            candidates = listRecognizeCandidates(isImageFile(file));
        } catch (Exception ex) {
            return manualTargetResult(file, systemKey, "读取 AI 模型配置失败：" + safeErr(ex.getMessage()));
        }
        if (candidates.isEmpty()) {
            return manualTargetResult(file, systemKey, "当前没有可用的 AI 识别模型");
        }

        String instruction = buildImportInstruction(systemKey);
        List<String> errors = new ArrayList<>();
        for (BaseAiConfigVO config : candidates) {
            try {
                JSONObject ai = openAiCompatibleClient.recognizeFile(config, file, description, instruction);
                return normalizeImportResult(file, systemKey, ai, config, errors);
            } catch (Exception ex) {
                errors.add(config.getModelName() + "：" + safeErr(ex.getMessage()));
            }
        }
        return manualTargetResult(file, systemKey, "AI 模型识别失败：" + String.join("；", errors));
    }

    private String buildImportInstruction(String systemKey) {
        return "你正在为业务系统执行受控数据提取。只能从以下 JSON 目录选择对象和字段：\n"
                + JSONObject.toJSONString(importCatalog.promptSchema(systemKey))
                + "\n仅返回 JSON：{\"systemKey\":\"" + systemKey
                + "\",\"functionKey\":\"目录中的功能\",\"objectKey\":\"目录中的对象\","
                + "\"fields\":{},\"summary\":\"识别摘要\"}。"
                + "不得输出 SQL、表名或目录外字段；无法判断对象时 objectKey 返回空字符串。";
    }

    private Map<String, Object> normalizeImportResult(MultipartFile file, String systemKey, JSONObject ai,
                                                       BaseAiConfigVO config, List<String> previousErrors) {
        String objectKey = ai == null ? null : ai.getString("objectKey");
        if (ai == null || !systemKey.equals(ai.getString("systemKey"))
                || objectKey == null || objectKey.isBlank()) {
            return manualTargetResult(file, systemKey, "AI 未能匹配受控业务对象");
        }
        var entry = importCatalog.find(systemKey, objectKey);
        if (entry.isEmpty()) {
            return manualTargetResult(file, systemKey, "AI 返回的业务对象不在允许目录中");
        }

        FileRecognizeCatalogEntry target = entry.get();
        Map<String, Object> aiFields = extractAiFields(ai.get("fields"));
        Set<String> allowed = target.fields().stream().map(FileRecognizeCatalogEntry.FieldDefinition::name)
                .collect(java.util.stream.Collectors.toSet());
        Map<String, Object> fields = new LinkedHashMap<>();
        List<String> validationErrors = new ArrayList<>();
        for (Map.Entry<String, Object> field : aiFields.entrySet()) {
            if (allowed.contains(field.getKey())) {
                fields.put(field.getKey(), field.getValue());
            } else {
                validationErrors.add("已忽略目录外字段：" + field.getKey());
            }
        }
        for (FileRecognizeCatalogEntry.FieldDefinition definition : target.fields()) {
            Object value = fields.get(definition.name());
            if (definition.required() && (value == null || value instanceof String text && text.isBlank())) {
                validationErrors.add("缺少必填字段：" + definition.name());
            }
        }

        Map<String, Object> result = fileMetadata(file, systemKey);
        result.put("functionKey", target.functionKey());
        result.put("functionName", target.functionName());
        result.put("objectKey", target.objectKey());
        result.put("objectName", target.objectName());
        result.put("targetTable", target.targetTable());
        result.put("fields", fields);
        result.put("fieldSchema", target.fields());
        result.put("summary", firstNonBlank(ai.getString("summary"), "AI 智能识别完成"));
        result.put("needsManualTarget", false);
        result.put("validationErrors", validationErrors);
        result.put("source", "system-ai");
        result.put("aiProvider", config.getProviderName());
        result.put("aiModel", config.getModelName());
        if (!previousErrors.isEmpty()) {
            result.put("recognitionNote", "主模型失败后已切换至 " + config.getModelName());
        }
        return result;
    }

    private Map<String, Object> manualTargetResult(MultipartFile file, String systemKey, String warning) {
        Map<String, Object> result = fileMetadata(file, systemKey);
        result.put("functionKey", null);
        result.put("objectKey", null);
        result.put("fields", new LinkedHashMap<>());
        result.put("needsManualTarget", true);
        result.put("validationErrors", List.of(warning));
        result.put("summary", "需要人工选择目标功能和业务对象");
        result.put("source", "manual-target");
        return result;
    }

    private Map<String, Object> fileMetadata(MultipartFile file, String systemKey) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("systemKey", systemKey);
        result.put("fileName", file.getOriginalFilename());
        result.put("fileSize", file.getSize());
        result.put("contentType", file.getContentType());
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractAiFields(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, fieldValue) -> result.put(String.valueOf(key), fieldValue));
            return result;
        }
        return Map.of();
    }

    @Override
    public ResponseResult confirm(Map<String, Object> params) {
        try {
            String fileName = params != null ? (String) params.get("fileName") : null;
            String recognizeType = params != null ? (String) params.get("recognizeType") : null;
            log.info("确认文件识别: {}, 类型: {}", fileName, recognizeType);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "处理成功");
            result.put("redirectUrl", FileRecognizeRuleHelper.getRedirectUrl(recognizeType));
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("确认处理失败", e);
            return ResponseResult.fail("处理失败: " + e.getMessage());
        }
    }

    /**
     * 使用系统 AI 模型识别：图片场景按视觉能力排序，主模型失败后自动切换备用视觉模型。
     */
    private Map<String, Object> trySystemAiRecognize(MultipartFile file, String description) {
        boolean imageFile = isImageFile(file);
        List<BaseAiConfigVO> candidates;
        try {
            candidates = listRecognizeCandidates(imageFile);
        } catch (Exception ex) {
            log.warn("读取 AI 配置失败，回退规则识别: {}", ex.getMessage(), ex);
            Map<String, Object> fallback = FileRecognizeRuleHelper.recognize(
                    file.getOriginalFilename(), safeBytes(file), description);
            if (imageFile) {
                fallback.put("recognitionWarning", "读取 AI 模型配置失败，未完成图片文字识别：" + safeErr(ex.getMessage()));
            }
            return fallback;
        }
        if (candidates.isEmpty()) {
            log.info("未找到可用的视觉/OCR模型，使用规则识别");
            return null;
        }

        List<String> errors = new ArrayList<>();
        for (BaseAiConfigVO config : candidates) {
            try {
                JSONObject aiResult = openAiCompatibleClient.recognizeFile(config, file, description);
                Map<String, Object> result = new HashMap<>();
                result.put("type", aiResult.getString("type"));
                result.put("summary", firstNonBlank(
                    aiResult.getString("summary"),
                    aiResult.getString("title"),
                    "AI 智能识别完成"
                ));
                result.put("title", aiResult.getString("title"));
                if (aiResult.containsKey("fullText")) {
                    result.put("fullText", aiResult.getString("fullText"));
                }
                if (aiResult.containsKey("fields")) {
                    result.put("fields", aiResult.get("fields"));
                }
                result.put("source", "system-ai");
                result.put("aiProvider", config.getProviderName());
                result.put("aiModel", config.getModelName());
                if (!errors.isEmpty()) {
                    result.put("recognitionNote", "主模型失败后已切换至 " + config.getModelName());
                }
                return result;
            } catch (Exception ex) {
                String detail = safeErr(ex.getMessage());
                errors.add(config.getModelName() + "：" + detail);
                log.warn("模型识别失败，尝试下一个: model={} err={}", config.getModelName(), detail);
            }
        }

        Map<String, Object> fallback = FileRecognizeRuleHelper.recognize(
                file.getOriginalFilename(), safeBytes(file), description);
        fallback.put("recognitionWarning", "已配置视觉模型但全部调用失败：" + String.join("；", errors)
                + "。请检查模型名称、接口地址及上游服务状态");
        BaseAiConfigVO first = candidates.get(0);
        fallback.put("aiProvider", first.getProviderName());
        fallback.put("aiModel", first.getModelName());
        return fallback;
    }

    private List<BaseAiConfigVO> listRecognizeCandidates(boolean imageFile) {
        List<BaseAiConfigVO> pool = new ArrayList<>();
        List<BaseAiConfigVO> defaults = aiConfigMapper.getDefaultConfigList();
        if (defaults != null) {
            pool.addAll(defaults);
        }
        if (imageFile) {
            List<BaseAiConfigVO> enabled = aiConfigMapper.queryEnabledConfigs();
            if (enabled != null) {
                pool.addAll(enabled);
            }
        }
        Map<Integer, BaseAiConfigVO> dedup = new LinkedHashMap<>();
        for (BaseAiConfigVO c : pool) {
            if (c == null || c.getId() == null) {
                continue;
            }
            dedup.putIfAbsent(c.getId(), c);
        }
        // 无 id 的兜底（测试 mock）按对象本身保留
        List<BaseAiConfigVO> withoutId = pool.stream()
                .filter(c -> c != null && c.getId() == null)
                .toList();
        List<BaseAiConfigVO> all = new ArrayList<>(dedup.values());
        all.addAll(withoutId);

        List<BaseAiConfigVO> usable = all.stream()
                .filter(c -> c.getIsEnabled() != null && c.getIsEnabled() == 1)
                .filter(c -> !difyApiClient.isDifyConfig(c))
                .filter(c -> c.getApiKey() != null && !c.getApiKey().isBlank() && !c.getApiKey().contains("***"))
                .filter(c -> c.getApiEndpoint() != null && !c.getApiEndpoint().isBlank())
                .sorted(Comparator
                        .comparingInt((BaseAiConfigVO c) -> visionScore(c)).reversed()
                        .thenComparing(Comparator.comparingInt((BaseAiConfigVO c) -> c.getPriority() == null ? 0 : c.getPriority()).reversed())
                        .thenComparing(Comparator.comparingInt((BaseAiConfigVO c) -> c.getSortOrder() == null ? 0 : c.getSortOrder()).reversed()))
                .toList();

        if (!imageFile) {
            return usable;
        }
        return usable.stream().filter(this::isVisionCapable).toList();
    }

    private boolean isVisionCapable(BaseAiConfigVO c) {
        String cat = BaseAiConfigServiceImpl.inferModelCategory(c.getModelName(), c.getDescription());
        if ("vision".equals(cat) || "ocr".equals(cat)) {
            return true;
        }
        return c.getSupportVision() != null && c.getSupportVision() == 1;
    }

    private int visionScore(BaseAiConfigVO c) {
        String cat = BaseAiConfigServiceImpl.inferModelCategory(c.getModelName(), c.getDescription());
        if ("vision".equals(cat) || "ocr".equals(cat)) {
            return 2;
        }
        if (c.getSupportVision() != null && c.getSupportVision() == 1) {
            return 1;
        }
        return 0;
    }

    private byte[] safeBytes(MultipartFile file) {
        try {
            return file == null ? new byte[0] : file.getBytes();
        } catch (Exception ignored) {
            return new byte[0];
        }
    }

    private boolean isImageFile(MultipartFile file) {
        if (file == null) return false;
        String contentType = file.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith("image/")) return true;
        String name = file.getOriginalFilename();
        if (name == null) return false;
        String lower = name.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".bmp");
    }

    private void normalizeType(Map<String, Object> result) {
        Object typeObj = result.get("type");
        String type = typeObj != null ? String.valueOf(typeObj).trim().toLowerCase() : "unknown";
        if (!VALID_TYPES.contains(type)) {
            type = "unknown";
        }
        result.put("type", type);
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return "";
    }

    private static String safeErr(String message) {
        if (message == null || message.isBlank()) {
            return "上游服务返回异常";
        }
        return message.length() > 240 ? message.substring(0, 240) + "..." : message;
    }
}
