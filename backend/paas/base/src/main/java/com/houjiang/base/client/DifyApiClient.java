package com.houjiang.base.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.houjiang.base.vo.BaseAiConfigVO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dify API 客户端（文件上传 + 工作流/对话识别）
 * @author Amos
 */
@Slf4j
@Component
public class DifyApiClient {

    private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");
    private static final Pattern JSON_BLOCK = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    public boolean testConnection(BaseAiConfigVO config) {
        return false;
    }

    public JSONObject recognizeFile(BaseAiConfigVO config, MultipartFile file, String description, String userId) {
        throw new IllegalStateException("Dify 文档识别能力已移除，请使用规则识别或系统 AI 模型");
    }

    public boolean isDifyConfig(BaseAiConfigVO config) {
        if (config == null) {
            return false;
        }
        String provider = config.getProviderCode() != null ? config.getProviderCode().toLowerCase() : "";
        if (provider.contains("dify")) {
            return true;
        }
        String name = config.getProviderName() != null ? config.getProviderName().toLowerCase() : "";
        return name.contains("dify");
    }

    private String uploadFile(String baseUrl, String apiKey, MultipartFile file, String user, BaseAiConfigVO config)
        throws Exception {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.bin";
        String mime = resolveMimeType(fileName, file.getContentType());
        RequestBody fileBody = RequestBody.create(file.getBytes(), MediaType.parse(mime));
        MultipartBody body = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", fileName, fileBody)
            .addFormDataPart("user", user)
            .build();
        Request request = new Request.Builder()
            .url(baseUrl + "/files/upload")
            .addHeader("Authorization", "Bearer " + apiKey)
            .post(body)
            .build();
        try (Response response = buildClient(config).newCall(request).execute()) {
            String respBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.warn("Dify 文件上传失败 status={} body={}", response.code(), truncate(respBody, 500));
                throw new IllegalStateException("Dify 文件上传失败: HTTP " + response.code());
            }
            JSONObject json = JSON.parseObject(respBody);
            if (json == null || json.getString("id") == null) {
                throw new IllegalStateException("Dify 文件上传未返回文件 ID");
            }
            return json.getString("id");
        }
    }

    private String runWorkflow(
        String baseUrl,
        String apiKey,
        BaseAiConfigVO config,
        String uploadId,
        MultipartFile file,
        String description,
        String user
    ) throws Exception {
        JSONObject cfg = parseConfigJson(config.getConfigJson());
        String fileKey = cfg.getString("fileInputKey");
        if (fileKey == null || fileKey.isBlank()) {
            fileKey = "file";
        }
        String descKey = cfg.getString("descriptionInputKey");
        if (descKey == null || descKey.isBlank()) {
            descKey = "description";
        }
        Map<String, Object> inputs = new LinkedHashMap<>();
        Map<String, Object> fileInput = new LinkedHashMap<>();
        fileInput.put("transfer_method", "local_file");
        fileInput.put("upload_file_id", uploadId);
        fileInput.put("type", resolveDifyFileType(file.getOriginalFilename()));
        inputs.put(fileKey, fileInput);
        if (description != null && !description.isBlank()) {
            inputs.put(descKey, description.trim());
        }
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload";
        inputs.putIfAbsent("file_name", fileName);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inputs", inputs);
        payload.put("response_mode", "blocking");
        payload.put("user", user);

        return postJson(baseUrl + "/workflows/run", apiKey, payload, config, "workflow");
    }

    private String runChat(
        String baseUrl,
        String apiKey,
        BaseAiConfigVO config,
        String uploadId,
        MultipartFile file,
        String description,
        String user
    ) throws Exception {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload";
        String query = buildChatQuery(fileName, description);
        Map<String, Object> fileObj = new LinkedHashMap<>();
        fileObj.put("type", resolveDifyFileType(fileName));
        fileObj.put("transfer_method", "local_file");
        fileObj.put("upload_file_id", uploadId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inputs", Map.of());
        payload.put("query", query);
        payload.put("response_mode", "blocking");
        payload.put("conversation_id", "");
        payload.put("user", user);
        payload.put("files", java.util.List.of(fileObj));

        return postJson(baseUrl + "/chat-messages", apiKey, payload, config, "chat");
    }

    private String postJson(String url, String apiKey, Map<String, Object> payload, BaseAiConfigVO config, String mode)
        throws Exception {
        Request request = new Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(JSON.toJSONString(payload), JSON_MEDIA))
            .build();
        try (Response response = buildClient(config).newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.warn("Dify {} 调用失败 status={} body={}", mode, response.code(), truncate(body, 500));
                throw new IllegalStateException("Dify 识别失败: HTTP " + response.code());
            }
            return extractAnswer(body, mode);
        }
    }

    private String extractAnswer(String body, String mode) {
        JSONObject json = JSON.parseObject(body);
        if (json == null) {
            throw new IllegalStateException("Dify 返回为空");
        }
        if ("workflow".equals(mode)) {
            JSONObject data = json.getJSONObject("data");
            if (data != null) {
                JSONObject outputs = data.getJSONObject("outputs");
                if (outputs != null) {
                    String outputKey = "result";
                    if (outputs.containsKey(outputKey)) {
                        return String.valueOf(outputs.get(outputKey));
                    }
                    for (String key : outputs.keySet()) {
                        Object val = outputs.get(key);
                        if (val != null && !String.valueOf(val).isBlank()) {
                            return String.valueOf(val);
                        }
                    }
                }
                String text = data.getString("text");
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
        }
        String answer = json.getString("answer");
        if (answer != null && !answer.isBlank()) {
            return answer;
        }
        throw new IllegalStateException("Dify 未返回有效识别结果");
    }

    private JSONObject parseRecognizeJson(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("Dify 识别结果为空");
        }
        String text = raw.trim();
        Matcher matcher = JSON_BLOCK.matcher(text);
        if (matcher.find()) {
            text = matcher.group(1).trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            text = text.substring(start, end + 1);
        }
        JSONObject parsed = JSON.parseObject(text);
        if (parsed == null) {
            throw new IllegalStateException("Dify 返回内容不是有效 JSON");
        }
        return parsed;
    }

    private String buildChatQuery(String fileName, String description) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是企业文档智能识别助手。请分析用户上传的文件，判断属于哪类业务单据并提取关键信息。");
        sb.append("仅返回 JSON，不要包含其它文字或 markdown 代码块。");
        sb.append("格式：{\"type\":\"contract|receivable|payable|goods|document|invoice|unknown\",");
        sb.append("\"summary\":\"一句话摘要\",\"title\":\"文档标题\",\"fullText\":\"附件全部OCR文字原文\",");
        sb.append("\"fields\":{\"amount\":0,\"date\":\"YYYY-MM-DD\",\"desc\":\"说明\"}}");
        sb.append("\n文件名：").append(fileName);
        if (description != null && !description.isBlank()) {
            sb.append("\n补充描述：").append(description.trim());
        }
        return sb.toString();
    }

    private String resolveAppType(BaseAiConfigVO config) {
        JSONObject cfg = parseConfigJson(config.getConfigJson());
        String appType = cfg.getString("appType");
        if (appType != null && !appType.isBlank()) {
            return appType.trim();
        }
        String model = config.getModelName() != null ? config.getModelName().toLowerCase() : "";
        if (model.contains("workflow")) {
            return "workflow";
        }
        return "chat";
    }

    private JSONObject parseConfigJson(String configJson) {
        if (configJson == null || configJson.isBlank()) {
            return new JSONObject();
        }
        try {
            JSONObject obj = JSON.parseObject(configJson);
            return obj != null ? obj : new JSONObject();
        } catch (Exception ex) {
            return new JSONObject();
        }
    }

    private String resolveDifyFileType(String fileName) {
        if (fileName == null) {
            return "document";
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
            || lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".bmp")) {
            return "image";
        }
        return "document";
    }

    private String resolveMimeType(String fileName, String contentType) {
        if (contentType != null && !contentType.isBlank() && !"application/octet-stream".equals(contentType)) {
            return contentType;
        }
        if (fileName == null) {
            return "application/octet-stream";
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".doc")) {
            return "application/msword";
        }
        if (lower.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        if (lower.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        }
        if (lower.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        return "application/octet-stream";
    }

    private String normalizeBaseUrl(String endpoint) {
        String base = endpoint != null ? endpoint.trim() : "";
        if (base.isEmpty()) {
            throw new IllegalStateException("Dify API 地址未配置");
        }
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (!base.endsWith("/v1")) {
            base = base + "/v1";
        }
        return base;
    }

    private OkHttpClient buildClient(BaseAiConfigVO config) {
        int timeout = config.getTimeout() != null && config.getTimeout() > 0 ? config.getTimeout() : 120;
        return new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(timeout))
            .readTimeout(Duration.ofSeconds(timeout))
            .writeTimeout(Duration.ofSeconds(timeout))
            .callTimeout(Duration.ofSeconds(timeout))
            .build();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
