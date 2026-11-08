package com.houjiang.aiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.aiagent.client.ModelClient;
import com.houjiang.aiagent.entity.AiAgent;
import com.houjiang.aiagent.entity.AiChatMessage;
import com.houjiang.aiagent.entity.AiChatSession;
import com.houjiang.aiagent.mapper.AiAgentMapper;
import com.houjiang.aiagent.mapper.AiChatMessageMapper;
import com.houjiang.aiagent.mapper.AiChatSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 会话与消息。消息表同时是合规留痕的真相源，只软删不物理删。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;
    private final ModelClient modelClient;
    private final AiAgentMapper agentMapper;
    private final AiWorkflowService workflowService;

    public Map<String, Object> chat(Map<String, Object> params) {
        return chat(params, null);
    }

    /**
     * 对话主流程。
     *
     * <p>两种出口：
     * <ul>
     *   <li>{@code onDelta == null}：一次性算完再返回，供前台既有接口继续使用；</li>
     *   <li>{@code onDelta != null}：流式——模型每产出一段文本就回调一次，由 SSE 端点实时下发。
     *       这是后台「试用对话」的默认路径：实测长文案整体要 60 ～ 70 秒，
     *       但首字只要 2 ～ 3 秒，同步等待的表现就是「点了没反应然后超时」。</li>
     * </ul>
     *
     * @param onDelta 流式增量回调，可为 null
     */
    public Map<String, Object> chat(Map<String, Object> params, Consumer<String> onDelta) {
        String content = str(params, "content");
        if (content == null) {
            content = str(params, "message");
        }
        if (!StringUtils.hasText(content)) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("success", false);
            empty.put("message", "请输入内容");
            return empty;
        }
        String sessionNo = str(params, "sessionNo");
        if (!StringUtils.hasText(sessionNo)) {
            sessionNo = str(params, "session_no");
        }
        if (!StringUtils.hasText(sessionNo)) {
            sessionNo = UUID.randomUUID().toString().replace("-", "");
        }
        AiChatSession session = ensureSession(sessionNo, params);

        saveMessage(session.getId(), "user", content, null, 0);
        List<Map<String, String>> history = recentMessages(session.getId(), 10);

        StringBuilder context = new StringBuilder();
        for (Map<String, String> item : history) {
            context.append(item.get("role")).append(": ").append(item.get("content")).append("\n");
        }
        String scene = str(params, "scene");
        if (!StringUtils.hasText(scene)) {
            scene = "chat";
        }
        AiAgent agent = agentMapper.selectOne(new LambdaQueryWrapper<AiAgent>()
                .eq(AiAgent::getCode, scene).last("limit 1"));
        if (agent != null && "workflow".equalsIgnoreCase(agent.getMode())
                && StringUtils.hasText(agent.getWorkflowCode())) {
            return runWorkflow(agent, content, session, sessionNo, params, onDelta);
        }

        // 🔴 试用对话 / 前台调用只传 scene 时，提示词与模型必须回退到智能体自身配置。
        //    否则后台「改完提示词看效果」看到的是裸模型回复，验证结果和线上完全不是一回事。
        String systemPrompt = str(params, "systemPrompt");
        if (!StringUtils.hasText(systemPrompt) && agent != null) {
            systemPrompt = agent.getPrompt();
        }
        String model = str(params, "model");
        if (!StringUtils.hasText(model) && agent != null) {
            model = agent.getModelCode();
        }
        ModelClient.ModelChatRequest request = new ModelClient.ModelChatRequest()
                .setUserPrompt(context.toString())
                .setSystemPrompt(systemPrompt)
                .setModel(model)
                .setScene(scene);
        applyAgentParams(request, agent);

        if (onDelta != null) {
            return streamAnswer(session, sessionNo, model, scene, request, onDelta);
        }

        ModelClient.ModelChatResult result = modelClient.chat(request);
        String answer = result.isSuccess() ? result.getContent() : ("（未获得模型回复）" + result.getErrorMessage());
        saveMessage(session.getId(), "assistant", answer, result.getModel(),
                result.getTotalTokens() == null ? 0 : result.getTotalTokens());

        session.setMessageCount(session.getMessageCount() == null ? 2 : session.getMessageCount() + 2);
        sessionMapper.updateById(session);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("success", result.isSuccess());
        data.put("sessionNo", sessionNo);
        data.put("content", answer);
        data.put("model", result.getModel());
        if (!result.isSuccess()) {
            data.put("message", result.getErrorMessage());
            data.put("errorCode", result.getErrorCode());
        }
        return data;
    }

    /**
     * 流式收口：边收边回调，收完把完整回答一次性落库（留痕仍是一条完整消息，不做碎片化存储）。
     */
    private Map<String, Object> streamAnswer(AiChatSession session, String sessionNo, String model, String scene,
                                             ModelClient.ModelChatRequest request, Consumer<String> onDelta) {
        StringBuilder full = new StringBuilder();
        try {
            modelClient.chatStream(request)
                    .doOnNext(chunk -> {
                        if (chunk == null || chunk.isEmpty()) {
                            return;
                        }
                        full.append(chunk);
                        onDelta.accept(chunk);
                    })
                    .blockLast(java.time.Duration.ofMinutes(10));
        } catch (Exception e) {
            log.warn("[aiagent] 流式对话中断 scene={} msg={}", scene, e.getMessage());
        }
        String answer = full.toString();
        if (!StringUtils.hasText(answer)) {
            answer = "（模型没有返回内容）";
        }
        saveMessage(session.getId(), "assistant", answer, model, 0);
        session.setMessageCount(session.getMessageCount() == null ? 2 : session.getMessageCount() + 2);
        sessionMapper.updateById(session);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("success", StringUtils.hasText(full.toString()));
        data.put("sessionNo", sessionNo);
        data.put("content", answer);
        data.put("model", model);
        return data;
    }

    /**
     * 把智能体「其他参数」里的 temperature / maxTokens 落到本次模型调用，
     * 后台配置了采样参数就该生效，而不是永远走 0.7 / 2048 的硬编码默认值。
     */
    private void applyAgentParams(ModelClient.ModelChatRequest request, AiAgent agent) {
        if (agent == null || !StringUtils.hasText(agent.getParams())) {
            return;
        }
        try {
            com.fasterxml.jackson.databind.JsonNode node =
                    com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                            .readTree(agent.getParams());
            if (node == null || !node.isObject()) {
                return;
            }
            if (node.hasNonNull("temperature")) {
                request.setTemperature(node.get("temperature").asDouble());
            }
            if (node.hasNonNull("maxTokens")) {
                request.setMaxTokens(node.get("maxTokens").asInt());
            }
        } catch (Exception e) {
            log.debug("[aiagent] params 解析失败，按默认采样参数执行 agent={} msg={}", agent.getCode(), e.getMessage());
        }
    }

    /**
     * 智能体处于「工作流」执行模式时走这里：把用户输入映射成画布入参，跑完整条 DAG，
     * 最终输出作为回复返回。消息留痕与提示词模式保持一致，便于事后追溯。
     *
     * <p>注意：用户消息已在调用方写入，这里不能重复 ensureSession / saveMessage，
     * 否则每问一句会话里会多出一条重复的用户消息。
     */
    private Map<String, Object> runWorkflow(AiAgent agent, String content, AiChatSession session,
                                           String sessionNo, Map<String, Object> params,
                                           Consumer<String> onDelta) {
        Map<String, Object> inputs = new LinkedHashMap<>();
        Object passed = params.get("inputs");
        if (passed instanceof Map<?, ?> pm) {
            for (Map.Entry<?, ?> entry : pm.entrySet()) {
                inputs.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        if (inputs.isEmpty()) {
            List<String> names = workflowService.inputNames(agent.getWorkflowCode());
            String target = null;
            for (String candidate : new String[]{"content", "question", "input", "text", "query", "userInput"}) {
                if (names.contains(candidate)) {
                    target = candidate;
                    break;
                }
            }
            if (target == null && !names.isEmpty()) {
                target = names.get(0);
            }
            if (target != null) {
                inputs.put(target, content);
            }
        }

        Map<String, Object> run = workflowService.run(agent.getWorkflowCode(), inputs, agent.getCode());
        String status = String.valueOf(run.getOrDefault("status", "failed"));
        Map<String, Object> outputs = run.get("outputs") instanceof Map<?, ?> om
                ? (Map<String, Object>) om : Map.of();
        String answer = String.valueOf(outputs.getOrDefault("text", ""));
        if (!"success".equals(status)) {
            answer = "（工作流执行失败）" + run.getOrDefault("errorMsg", "未知错误");
        }
        if (!StringUtils.hasText(answer)) {
            answer = "（工作流未产出内容）";
        }
        saveMessage(session.getId(), "assistant", answer, "workflow:" + agent.getWorkflowCode(), 0);
        session.setMessageCount(session.getMessageCount() == null ? 2 : session.getMessageCount() + 2);
        sessionMapper.updateById(session);

        // 画布是整条 DAG 跑完才有最终输出，没有逐字增量；这里把结果一次性推给流式端点，
        // 让前端拿到的仍然是「流式协议 + 一次性内容」，不用为工作流单独写一套前端。
        if (onDelta != null && StringUtils.hasText(answer)) {
            onDelta.accept(answer);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("success", "success".equals(status));
        data.put("sessionNo", sessionNo);
        data.put("content", answer);
        data.put("model", "workflow:" + agent.getWorkflowCode());
        data.put("runNo", run.get("runNo"));
        data.put("status", status);
        data.put("workflowCode", agent.getWorkflowCode());
        data.put("traces", run.get("traces"));
        if (!"success".equals(status)) {
            data.put("message", run.get("errorMsg"));
        }
        return data;
    }

    public Map<String, Object> history(Map<String, Object> params) {
        String sessionNo = str(params, "sessionNo");
        if (!StringUtils.hasText(sessionNo)) {
            sessionNo = str(params, "session_no");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        if (!StringUtils.hasText(sessionNo)) {
            data.put("sessions", sessions());
            data.put("messages", List.of());
            return data;
        }
        AiChatSession session = sessionMapper.selectOne(new LambdaQueryWrapper<AiChatSession>()
                .eq(AiChatSession::getSessionNo, sessionNo).last("limit 1"));
        if (session == null) {
            data.put("sessions", List.of());
            data.put("messages", List.of());
            return data;
        }
        List<AiChatMessage> messages = messageMapper.selectList(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getSessionId, session.getId())
                .orderByAsc(AiChatMessage::getId));
        List<Map<String, Object>> list = new ArrayList<>();
        for (AiChatMessage message : messages) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", message.getId());
            item.put("role", message.getRole());
            item.put("content", message.getContent());
            item.put("time", message.getCreateTime());
            item.put("model", message.getModelName());
            list.add(item);
        }
        data.put("sessions", sessions());
        data.put("messages", list);
        return data;
    }

    public boolean clear(Map<String, Object> params) {
        String sessionNo = str(params, "sessionNo");
        if (!StringUtils.hasText(sessionNo)) {
            sessionNo = str(params, "session_no");
        }
        if (!StringUtils.hasText(sessionNo)) {
            return false;
        }
        AiChatSession session = sessionMapper.selectOne(new LambdaQueryWrapper<AiChatSession>()
                .eq(AiChatSession::getSessionNo, sessionNo).last("limit 1"));
        if (session == null) {
            return false;
        }
        messageMapper.delete(new LambdaQueryWrapper<AiChatMessage>().eq(AiChatMessage::getSessionId, session.getId()));
        session.setMessageCount(0);
        sessionMapper.updateById(session);
        return true;
    }

    /** 最近 N 条消息（含刚写入的 user 消息），正序返回 */
    private List<Map<String, String>> recentMessages(Long sessionId, int limit) {
        List<AiChatMessage> messages = messageMapper.selectList(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getSessionId, sessionId)
                .orderByDesc(AiChatMessage::getId)
                .last("limit " + limit));
        List<Map<String, String>> result = new ArrayList<>();
        for (int i = messages.size() - 1; i >= 0; i--) {
            AiChatMessage message = messages.get(i);
            Map<String, String> item = new LinkedHashMap<>();
            item.put("role", message.getRole());
            item.put("content", message.getContent() == null ? "" : message.getContent());
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> sessions() {
        List<AiChatSession> list = sessionMapper.selectList(new LambdaQueryWrapper<AiChatSession>()
                .orderByDesc(AiChatSession::getId).last("limit 50"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (AiChatSession session : list) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", session.getId());
            item.put("sessionNo", session.getSessionNo());
            item.put("title", StringUtils.hasText(session.getTitle()) ? session.getTitle() : "新对话");
            item.put("messageCount", session.getMessageCount());
            item.put("time", session.getUpdateTime() == null ? session.getCreateTime() : session.getUpdateTime());
            result.add(item);
        }
        return result;
    }

    private AiChatSession ensureSession(String sessionNo, Map<String, Object> params) {
        AiChatSession session = sessionMapper.selectOne(new LambdaQueryWrapper<AiChatSession>()
                .eq(AiChatSession::getSessionNo, sessionNo).last("limit 1"));
        if (session != null) {
            return session;
        }
        session = new AiChatSession();
        session.setSessionNo(sessionNo);
        String title = str(params, "title");
        session.setTitle(StringUtils.hasText(title) ? title : defaultTitle(params));
        session.setModelName(str(params, "model"));
        session.setUserId(0L);
        session.setMessageCount(0);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }

    private String defaultTitle(Map<String, Object> params) {
        String content = str(params, "content");
        if (!StringUtils.hasText(content)) {
            return "新对话";
        }
        return content.length() > 20 ? content.substring(0, 20) + "…" : content;
    }

    private void saveMessage(Long sessionId, String role, String content, String model, Integer tokens) {
        AiChatMessage message = new AiChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setModelName(model);
        message.setTokens(tokens == null ? 0 : tokens);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
    }

    private String str(Map<String, Object> params, String key) {
        Object value = params == null ? null : params.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
