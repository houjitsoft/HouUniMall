package com.houjiang.aiagent.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.houjiang.aiagent.entity.AiConfig;
import com.houjiang.aiagent.mapper.AiConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 Spring AI 的模型客户端，出口固定指向自建 aigateway。
 *
 * <p>两条硬纪律（方案 D.8）：
 * <ol>
 *   <li>base-url 只指向网关，apiKey 只填网关签发的档案密钥（{@code sk-cp-…}），
 *       绝不存放厂商密钥——厂商密钥只存在于 aigateway。</li>
 *   <li>不叠加 Spring AI 自身的重试（网关已有故障切换链），重试次数保持 1。</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAiModelClient implements ModelClient {

    /** 网关未配置时的错误码，前端据此提示「请先配置 AI 网关」而不是显示 500 */
    public static final String ERR_NOT_CONFIGURED = "AI_GATEWAY_NOT_CONFIGURED";

    private static final String CFG_BASE_URL = "ai_gateway_base_url";
    private static final String CFG_PROFILE_KEY = "ai_gateway_profile_key";
    private static final String CFG_DEFAULT_MODEL = "default_model";

    private final AiConfigMapper configMapper;
    private final Environment environment;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 流式专用客户端。
     *
     * <p>🔴 必须固定 {@code HTTP_1_1}（2026-10-29 实测，差 50 倍）：
     * 同一个网关、同一个请求体，直接用 curl/python 打首字 **1.5 秒**，
     * 经本客户端却要 **74 秒**。原因是 JDK HttpClient 默认协商 HTTP/2，
     * 而 HTTP/2 的流控窗口在长流场景下不会及时扩窗，网关侧把数据发不出来，
     * 表现就是「模型早就开始生成了，客户端却一直收不到首包」。
     * 固定 HTTP/1.1 + 复用同一个客户端（避免每次重新 DNS/TLS 握手）后恢复正常。
     */
    private final HttpClient streamClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();

    @Override
    public ModelChatResult chat(ModelChatRequest request) {
        ModelEndpoint endpoint = endpoint();
        if (!endpoint.isConfigured()) {
            return new ModelChatResult()
                    .setSuccess(false)
                    .setErrorCode(ERR_NOT_CONFIGURED)
                    .setErrorMessage("AI 网关尚未配置：请到「AI 设置」填写网关地址与档案密钥");
        }
        try {
            OpenAiChatModel model = buildModel(request);
            Prompt prompt = new Prompt(buildMessages(request),
                    OpenAiChatOptions.builder()
                            .model(resolveModel(request))
                            .temperature(request.getTemperature() == null ? 0.7 : request.getTemperature())
                            .maxTokens(request.getMaxTokens() == null ? 2048 : request.getMaxTokens())
                            .build());
            ChatResponse response = model.call(prompt);
            ModelChatResult result = new ModelChatResult()
                    .setSuccess(true)
                    .setModel(resolveModel(request))
                    .setContent(response.getResult() == null ? "" : response.getResult().getOutput().getText());
            applyUsage(result, response.getMetadata());
            return result;
        } catch (Exception e) {
            log.warn("[aiagent] 模型调用失败 scene={} model={} msg={}", request.getScene(), request.getModel(), e.getMessage());
            return new ModelChatResult()
                    .setSuccess(false)
                    .setErrorCode("AI_CALL_FAILED")
                    .setErrorMessage("模型调用失败：" + e.getMessage());
        }
    }

    /**
     * 流式对话。
     *
     * <p>🔴 这里**不用** Spring AI 的 {@code OpenAiChatModel#stream}（2026-10-29 实测不可用）：
     * 同样的网关地址与密钥，非流式 {@code chat()} 能正常返回整段内容，
     * 而 {@code stream()} 会先抛 {@code 404 Not Found from POST .../v1/chat/completions}、
     * 再变成 {@code The mapper [...] returned a null value}——它对流式请求的构造与解析
     * 和这家 OpenAI 兼容网关对不上，且日志里看不到实际发出的报文，排不动。
     *
     * <p>改用 JDK HttpClient 直连，逐行读 SSE，只取 {@code choices[0].delta.content}。
     * 好处是请求体、状态码、异常原文都拿得到；配合 {@code ModelClient} 这层门面，
     * 将来换回框架实现也不影响业务代码。
     */
    @Override
    public Flux<String> chatStream(ModelChatRequest request) {
        ModelEndpoint endpoint = endpoint();
        if (!endpoint.isConfigured()) {
            return Flux.just("AI 网关尚未配置，请到「AI 设置」填写网关地址与档案密钥。");
        }
        return Flux.<String>create(sink -> {
                    try {
                        doStream(endpoint, request, sink);
                    } catch (Exception e) {
                        log.warn("[aiagent] 流式调用失败 scene={} msg={}", request.getScene(), e.getMessage());
                        sink.error(e);
                    }
                })
                .onErrorResume(e -> Flux.just("生成失败：" + e.getMessage()));
    }

    /** 实际发起流式请求并按 SSE 逐行推增量，任何非 200 都带状态码抛出去，避免只剩一句「生成失败」 */
    private void doStream(ModelEndpoint endpoint, ModelChatRequest request,
                          reactor.core.publisher.FluxSink<String> sink) throws Exception {
        String base = endpoint.getBaseUrl() == null ? "" : endpoint.getBaseUrl().replaceAll("/+$", "");
        String payload = buildStreamBody(request, resolveModel(request));
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(base + "/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream")
                .header("Authorization", "Bearer " + config(CFG_PROFILE_KEY))
                .timeout(java.time.Duration.ofMinutes(10))
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<InputStream> response = streamClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            String detail;
            try (InputStream in = response.body()) {
                detail = new String(in.readNBytes(300), StandardCharsets.UTF_8);
            }
            throw new IllegalStateException("AI 网关返回 " + response.statusCode() + "：" + detail);
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data:")) {
                    continue;
                }
                String data = line.substring(5).trim();
                if (data.isEmpty()) {
                    continue;
                }
                if ("[DONE]".equals(data)) {
                    break;
                }
                String delta = extractDelta(data);
                if (delta != null && !delta.isEmpty()) {
                    sink.next(delta);
                }
            }
        }
        sink.complete();
    }

    /** 组装流式请求体；字段名必须用 OpenAI 的 snake_case（max_tokens），不能沿用 camelCase */
    private String buildStreamBody(ModelChatRequest request, String model) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", model);
        ArrayNode messages = root.putArray("messages");
        if (StringUtils.hasText(request.getSystemPrompt())) {
            ObjectNode system = messages.addObject();
            system.put("role", "system");
            system.put("content", request.getSystemPrompt());
        }
        ObjectNode user = messages.addObject();
        user.put("role", "user");
        user.put("content", request.getUserPrompt() == null ? "" : request.getUserPrompt());
        root.put("stream", true);
        if (request.getTemperature() != null) {
            root.put("temperature", request.getTemperature());
        }
        if (request.getMaxTokens() != null) {
            root.put("max_tokens", request.getMaxTokens());
        }
        return objectMapper.writeValueAsString(root);
    }

    /**
     * 从一条 SSE data 里取出增量文本。
     * 只认 {@code delta.content}：思考型模型还会下发 {@code delta.reasoning_content}，
     * 那是内部推理过程，不该出现在给用户的回复里。
     */
    private String extractDelta(String json) {
        try {
            JsonNode choices = objectMapper.readTree(json).path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return null;
            }
            JsonNode content = choices.get(0).path("delta").get("content");
            return content == null || content.isNull() ? null : content.asText();
        } catch (Exception e) {
            log.debug("[aiagent] 跳过一条无法解析的流式分片 msg={}", e.getMessage());
            return null;
        }
    }

    /**
     * 组装消息列表：智能体下发的提示词走 SystemMessage，用户输入走 UserMessage。
     *
     * <p>此前只把内容塞进 UserMessage，导致后台「智能体管理」配置的提示词被静默忽略。
     */
    private List<Message> buildMessages(ModelChatRequest request) {
        List<Message> messages = new ArrayList<>();
        if (StringUtils.hasText(request.getSystemPrompt())) {
            messages.add(new SystemMessage(request.getSystemPrompt()));
        }
        messages.add(new UserMessage(request.getUserPrompt() == null ? "" : request.getUserPrompt()));
        return messages;
    }

    @Override
    public ModelHealth health() {
        ModelEndpoint endpoint = endpoint();
        if (!endpoint.isConfigured()) {
            return new ModelHealth().setAvailable(false).setMessage("网关未配置");
        }
        ModelChatResult probe = chat(new ModelChatRequest()
                .setUserPrompt("ping")
                .setMaxTokens(16)
                .setScene("health"));
        return new ModelHealth()
                .setAvailable(probe.isSuccess())
                .setMessage(probe.isSuccess() ? "网关连通，模型 " + probe.getModel() : probe.getErrorMessage());
    }

    @Override
    public ModelEndpoint endpoint() {
        String baseUrl = config(CFG_BASE_URL);
        String apiKey = config(CFG_PROFILE_KEY);
        String model = config(CFG_DEFAULT_MODEL);
        return new ModelEndpoint()
                .setBaseUrl(baseUrl)
                .setModel(model == null || model.isEmpty() ? "mall-chat" : model)
                .setConfigured(baseUrl != null && !baseUrl.isEmpty() && apiKey != null && !apiKey.isEmpty());
    }

    private OpenAiChatModel buildModel(ModelChatRequest request) {
        ModelEndpoint endpoint = endpoint();
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(endpoint.getBaseUrl())
                .apiKey(config(CFG_PROFILE_KEY))
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(resolveModel(request))
                        .build())
                .build();
    }

    private String resolveModel(ModelChatRequest request) {
        if (request != null && request.getModel() != null && !request.getModel().isBlank()) {
            return request.getModel();
        }
        return endpoint().getModel();
    }

    private void applyUsage(ModelChatResult result, ChatResponseMetadata metadata) {
        if (metadata == null) {
            return;
        }
        Usage usage = metadata.getUsage();
        if (usage == null) {
            return;
        }
        result.setPromptTokens(usage.getPromptTokens() == null ? 0 : usage.getPromptTokens().intValue());
        result.setCompletionTokens(usage.getCompletionTokens() == null ? 0 : usage.getCompletionTokens().intValue());
        result.setTotalTokens(usage.getTotalTokens() == null ? 0 : usage.getTotalTokens().intValue());
    }

    /**
     * 配置优先取数据库（支持后台热改），取不到再回退到环境变量/默认值。
     */
    private String config(String key) {
        try {
            List<AiConfig> list = configMapper.selectList(
                    new LambdaQueryWrapper<AiConfig>().eq(AiConfig::getConfigKey, key).last("limit 1"));
            if (list != null && !list.isEmpty() && list.get(0).getConfigValue() != null
                    && !list.get(0).getConfigValue().isEmpty()) {
                return list.get(0).getConfigValue();
            }
        } catch (Exception e) {
            log.debug("[aiagent] 读取数据库配置失败，回退环境变量 key={} msg={}", key, e.getMessage());
        }
        return switch (key) {
            case CFG_BASE_URL -> environment.getProperty("ai.gateway.base-url", "");
            case CFG_PROFILE_KEY -> environment.getProperty("ai.gateway.profile-key", "");
            case CFG_DEFAULT_MODEL -> environment.getProperty("ai.gateway.default-model", "mall-chat");
            default -> "";
        };
    }
}
