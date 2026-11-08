package com.houjiang.base.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.houjiang.base.vo.BaseAiConfigVO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OpenAI 兼容 Chat Completions 客户端（系统默认模型，支持图文多模态）
 */
@Slf4j
@Component
public class OpenAiCompatibleClient {

    private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");
    private static final Pattern JSON_BLOCK = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 用默认模型识别文件：图片走 vision 多模态；非图片仅带文件名与提示词
     */
    public JSONObject recognizeFile(BaseAiConfigVO config, MultipartFile file, String description) {
        return recognizeFile(config, file, description, null);
    }

    /**
     * 使用服务端受控目录约束模型输出。instruction 只能由服务端生成，不能直接接收前端内容。
     */
    public JSONObject recognizeFile(BaseAiConfigVO config, MultipartFile file, String description,
                                    String instruction) {
        if (config == null) {
            throw new IllegalArgumentException("AI 配置不能为空");
        }
        String apiKey = config.getApiKey();
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("***")) {
            throw new IllegalStateException("默认模型未配置有效 API Key");
        }
        String prompt = (description != null && !description.isBlank())
                ? description.trim()
                : "请识别附件内容，提取关键信息，仅返回 JSON。";
        if (instruction != null && !instruction.isBlank()) {
            prompt += "\n\n" + instruction.trim();
        }
        String fileName = file != null && file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "upload.bin";
        boolean image = isImageFile(fileName, file != null ? file.getContentType() : null);

        JSONArray messages = new JSONArray();
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");

        if (image && file != null && !file.isEmpty()) {
            JSONArray content = new JSONArray();
            JSONObject textPart = new JSONObject();
            textPart.put("type", "text");
            textPart.put("text", prompt + "\n文件名：" + fileName);
            content.add(textPart);

            JSONObject imagePart = new JSONObject();
            imagePart.put("type", "image_url");
            JSONObject imageUrl = new JSONObject();
            try {
                String mime = resolveMime(fileName, file.getContentType());
                String b64 = Base64.getEncoder().encodeToString(file.getBytes());
                imageUrl.put("url", "data:" + mime + ";base64," + b64);
            } catch (Exception e) {
                throw new IllegalStateException("读取附件失败: " + e.getMessage(), e);
            }
            imagePart.put("image_url", imageUrl);
            content.add(imagePart);
            userMsg.put("content", content);
        } else {
            userMsg.put("content", prompt + "\n文件名：" + fileName
                    + "\n说明：当前附件非图片，请尽可能根据文件名与说明推断；无法识别时返回空 fields。");
        }
        messages.add(userMsg);

        String content = chat(config, messages);
        return parseRecognizeJson(content);
    }

    public String chat(BaseAiConfigVO config, JSONArray messages) {
        String url = normalizeChatUrl(config.getApiEndpoint());
        JSONObject body = new JSONObject();
        body.put("model", resolveModelName(config));
        if (config.getTemperature() != null) {
            body.put("temperature", config.getTemperature());
        }
        if (config.getMaxTokens() != null) {
            body.put("max_tokens", config.getMaxTokens());
        } else {
            body.put("max_tokens", 2048);
        }
        body.put("messages", messages);

        int timeoutSec = config.getTimeout() != null && config.getTimeout() > 0
                ? Math.min(config.getTimeout(), 180) : 120;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(timeoutSec))
                .writeTimeout(Duration.ofSeconds(60))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + config.getApiKey().trim())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toJSONString(), JSON_MEDIA))
                .build();
        try (Response response = client.newCall(request).execute()) {
            String respBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.warn("默认模型调用失败 HTTP {} body={}", response.code(), truncate(respBody, 500));
                String upstream = extractUpstreamMessage(respBody);
                throw new IllegalStateException("默认模型调用失败: HTTP " + response.code()
                        + (upstream.isBlank() ? "" : " " + upstream));
            }
            JSONObject json = JSON.parseObject(respBody);
            JSONArray choices = json != null ? json.getJSONArray("choices") : null;
            if (choices == null || choices.isEmpty()) {
                throw new IllegalStateException("默认模型返回为空");
            }
            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            String content = message != null ? message.getString("content") : null;
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("默认模型未返回内容");
            }
            return content;
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("默认模型调用异常: " + ex.getMessage(), ex);
        }
    }

    public JSONObject parseRecognizeJson(String content) {
        String raw = content == null ? "" : content.trim();
        Matcher m = JSON_BLOCK.matcher(raw);
        if (m.find()) {
            raw = m.group(1).trim();
        } else {
            int start = raw.indexOf('{');
            int end = raw.lastIndexOf('}');
            if (start >= 0 && end > start) {
                raw = raw.substring(start, end + 1);
            }
        }
        JSONObject parsed;
        try {
            parsed = JSON.parseObject(raw);
        } catch (Exception ignored) {
            parsed = null;
        }
        if (parsed == null) {
            JSONObject fallback = new JSONObject();
            fallback.put("type", "document");
            fallback.put("summary", "模型已返回文本，但未解析出 JSON");
            fallback.put("fullText", content);
            JSONObject fields = new JSONObject();
            extractTextFields(raw, fields);
            fallback.put("fields", fields);
            return fallback;
        }
        if (!parsed.containsKey("fullText") && parsed.containsKey("text")) {
            parsed.put("fullText", parsed.getString("text"));
        }
        if (!parsed.containsKey("fields") || !(parsed.get("fields") instanceof JSONObject)) {
            parsed.put("fields", new JSONObject());
        }
        normalizeAmountField(parsed.getJSONObject("fields"));
        extractTextFields(firstNonBlank(parsed.getString("fullText"), parsed.getString("summary")),
                parsed.getJSONObject("fields"));
        return parsed;
    }

    private void normalizeAmountField(JSONObject fields) {
        if (fields == null || !hasValue(fields, "amount", "money", "total")) {
            return;
        }
        for (String name : new String[]{"amount", "money", "total"}) {
            Object value = fields.get(name);
            if (value == null || value instanceof Number) {
                continue;
            }
            Matcher matcher = Pattern.compile(
                    "([0-9]{1,3}(?:[,，][0-9]{3})*(?:\\.[0-9]{1,2})?)(?:\\s*(万|万元|元))?")
                    .matcher(String.valueOf(value));
            if (!matcher.find()) {
                continue;
            }
            java.math.BigDecimal amount = new java.math.BigDecimal(
                    matcher.group(1).replace(",", "").replace("，", ""));
            if (matcher.group(2) != null && matcher.group(2).startsWith("万")) {
                amount = amount.multiply(java.math.BigDecimal.valueOf(10000));
            }
            fields.put(name, amount.toPlainString());
            return;
        }
    }

    private void extractTextFields(String text, JSONObject fields) {
        if (text == null || text.isBlank() || fields == null) {
            return;
        }
        if (!hasValue(fields, "amount", "money", "total")) {
            Matcher amount = Pattern.compile(
                    "(?:金额|合计|总额|应收|应付|实收|付款|收款|价税合计)[^0-9]{0,24}"
                            + "([0-9]{1,3}(?:[,，][0-9]{3})*(?:\\.[0-9]{1,2})?)(?:\\s*(万|万元|元))?",
                    Pattern.CASE_INSENSITIVE).matcher(text);
            if (amount.find()) {
                String number = amount.group(1).replace(",", "").replace("，", "");
                java.math.BigDecimal value = new java.math.BigDecimal(number);
                if (amount.group(2) != null && amount.group(2).startsWith("万")) {
                    value = value.multiply(java.math.BigDecimal.valueOf(10000));
                }
                fields.put("amount", value.toPlainString());
            }
        }
        if (!hasValue(fields, "date", "datatime", "occurDate")) {
            Matcher date = Pattern.compile(
                    "(?<![0-9])((?:19|20)[0-9]{2}(?:年|[-/.])[0-9]{1,2}(?:月|[-/.])[0-9]{1,2}(?:日)?)")
                    .matcher(text);
            if (date.find()) {
                fields.put("date", date.group(1));
            }
        }
    }

    private boolean hasValue(JSONObject fields, String... names) {
        for (String name : names) {
            Object value = fields.get(name);
            if (value != null && !String.valueOf(value).isBlank()) {
                return true;
            }
        }
        return false;
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : (second == null ? "" : second);
    }

    private String normalizeChatUrl(String apiEndpoint) {
        if (apiEndpoint == null || apiEndpoint.isBlank()) {
            return "https://api.openai.com/v1/chat/completions";
        }
        String url = apiEndpoint.trim();
        if (url.endsWith("/chat/completions")) {
            return url;
        }
        if (url.endsWith("/v1")) {
            // DashScope 的 OpenAI 兼容接口不在 /api/v1，而是在 /compatible-mode/v1。
            if (url.contains("dashscope.aliyuncs.com/api/v1")) {
                return url.substring(0, url.length() - "/api/v1".length())
                        + "/compatible-mode/v1/chat/completions";
            }
            return url + "/chat/completions";
        }
        if (url.contains("/compatible-mode/")) {
            return url.endsWith("/") ? url + "chat/completions" : url + "/chat/completions";
        }
        if (url.endsWith("/")) {
            return url + "chat/completions";
        }
        // 已是完整路径但未带 chat/completions
        if (url.contains("/v1/") && !url.contains("/chat/completions")) {
            return url.endsWith("/") ? url + "chat/completions" : url + "/chat/completions";
        }
        return url + "/chat/completions";
    }

    private String resolveModelName(BaseAiConfigVO config) {
        if (config.getModelName() != null && !config.getModelName().isBlank()) {
            return config.getModelName().trim();
        }
        return "gpt-4o-mini";
    }

    private boolean isImageFile(String fileName, String contentType) {
        if (contentType != null && contentType.toLowerCase().startsWith("image/")) {
            return true;
        }
        if (fileName == null) {
            return false;
        }
        String lower = fileName.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".bmp");
    }

    private String resolveMime(String fileName, String contentType) {
        if (contentType != null && !contentType.isBlank() && !"application/octet-stream".equalsIgnoreCase(contentType)) {
            return contentType;
        }
        String lower = fileName != null ? fileName.toLowerCase() : "";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".bmp")) return "image/bmp";
        return "image/jpeg";
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }

    private String extractUpstreamMessage(String respBody) {
        if (respBody == null || respBody.isBlank()) {
            return "";
        }
        try {
            JSONObject json = JSON.parseObject(respBody);
            if (json == null) {
                return truncate(respBody, 160);
            }
            Object err = json.get("error");
            if (err instanceof JSONObject errObj) {
                String msg = errObj.getString("message");
                if (msg != null && !msg.isBlank()) {
                    return truncate(msg, 160);
                }
            }
            String msg = json.getString("message");
            if (msg != null && !msg.isBlank()) {
                return truncate(msg, 160);
            }
        } catch (Exception ignored) {
            // ignore parse errors
        }
        return truncate(respBody, 160);
    }
}
