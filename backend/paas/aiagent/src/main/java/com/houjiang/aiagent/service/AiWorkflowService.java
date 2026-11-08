package com.houjiang.aiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.aiagent.client.ModelClient;
import com.houjiang.aiagent.entity.AiWorkflow;
import com.houjiang.aiagent.entity.AiWorkflowRun;
import com.houjiang.aiagent.mapper.AiWorkflowMapper;
import com.houjiang.aiagent.mapper.AiWorkflowRunMapper;
import com.houjiang.aiagent.support.AiPageSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * AI 中台 - 工作流：画布 CRUD + 图校验 + DAG 执行引擎。
 *
 * <p>设计取向（与 Dify 同思路，但规模收敛）：
 * <ol>
 *   <li><b>编排数据化</b>：画布结构（nodes/edges）整体存 JSON，引擎按数据执行，
 *       新增节点类型只需在 {@link #executeNode} 加一个分支，不动调用方。</li>
 *   <li><b>变量显式引用</b>：提示词与模板里用 {@code {{变量名}}} 引用上游产出，
 *       执行前统一渲染；引用不到的变量替换为空串并在轨迹里标 missingVars，绝不静默编造。</li>
 *   <li><b>全程留痕</b>：每个节点的入参、出参、耗时、错误都进 traces，一次运行可完整回放。</li>
 *   <li><b>失败即停</b>：节点报错立刻终止并记录，不留半截结果（避免下游拿着空变量继续跑）。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiWorkflowService {

    /** 节点类型 */
    private static final String TYPE_START = "start";
    private static final String TYPE_LLM = "llm";
    private static final String TYPE_CONDITION = "condition";
    private static final String TYPE_TRANSFORM = "transform";
    private static final String TYPE_END = "end";

    private static final Set<String> KNOWN_TYPES =
            Set.of(TYPE_START, TYPE_LLM, TYPE_CONDITION, TYPE_TRANSFORM, TYPE_END);

    /** 单次运行最多经过的节点数，防死循环（图校验已排除环，这里是兜底） */
    private static final int MAX_NODE_VISITS = 100;

    private final AiWorkflowMapper workflowMapper;
    private final AiWorkflowRunMapper runMapper;
    private final ModelClient modelClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ---------------- 列表 / 详情 ----------------

    public Map<String, Object> list(Map<String, Object> params) {
        LambdaQueryWrapper<AiWorkflow> wrapper = new LambdaQueryWrapper<>();
        String terminal = AiPageSupport.str(params, "terminal");
        if (StringUtils.hasText(terminal)) {
            wrapper.eq(AiWorkflow::getTerminal, terminal);
        }
        String scene = AiPageSupport.str(params, "scene");
        if (StringUtils.hasText(scene)) {
            wrapper.eq(AiWorkflow::getScene, scene);
        }
        Integer status = AiPageSupport.toInt(params, "status");
        if (status != null) {
            wrapper.eq(AiWorkflow::getStatus, status);
        }
        String keyword = AiPageSupport.str(params, "keyword");
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(AiWorkflow::getName, keyword).or().like(AiWorkflow::getCode, keyword));
        }
        wrapper.orderByDesc(AiWorkflow::getUpdateTime);
        IPage<AiWorkflow> page = workflowMapper.selectPage(AiPageSupport.page(params), wrapper);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AiWorkflow wf : page.getRecords()) {
            rows.add(toView(wf, false));
        }
        return AiPageSupport.pageResult(page, rows);
    }

    public Map<String, Object> detail(String code) {
        AiWorkflow wf = findByCode(code);
        return wf == null ? new LinkedHashMap<>() : toView(wf, true);
    }

    public AiWorkflow findByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        List<AiWorkflow> list = workflowMapper.selectList(
                new LambdaQueryWrapper<AiWorkflow>().eq(AiWorkflow::getCode, code).last("limit 1"));
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    // ---------------- 保存 / 删除 / 复制 ----------------

    public Map<String, Object> save(Map<String, Object> data) {
        String code = AiPageSupport.str(data, "code");
        if (!StringUtils.hasText(code)) {
            throw new IllegalArgumentException("工作流编码不能为空");
        }
        String graph = AiPageSupport.str(data, "graph");
        List<String> errors = validateGraph(graph);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("画布校验未通过：" + String.join("；", errors));
        }

        AiWorkflow existing = findByCode(code);
        boolean isNew = existing == null;
        final AiWorkflow target = isNew ? new AiWorkflow() : existing;
        if (isNew) {
            target.setCode(code);
            target.setVersion(1);
        } else {
            target.setVersion(target.getVersion() == null ? 2 : target.getVersion() + 1);
        }

        put(data, "name", v -> target.setName(v));
        put(data, "terminal", v -> target.setTerminal(normalizeTerminal(v)));
        put(data, "scene", v -> target.setScene(v));
        put(data, "icon", v -> target.setIcon(v));
        put(data, "description", v -> target.setDescription(v));
        put(data, "modelCode", v -> target.setModelCode(v));
        put(data, "graph", v -> target.setGraph(v));
        Integer status = AiPageSupport.toInt(data, "status");
        if (status != null) {
            target.setStatus(status);
        }
        if (target.getName() == null || target.getName().isEmpty()) {
            target.setName(code);
        }
        if (target.getStatus() == null) {
            target.setStatus(0);
        }
        if (target.getBuiltIn() == null) {
            target.setBuiltIn(0);
        }
        if (target.getModelCode() == null) {
            target.setModelCode("");
        }
        if (target.getScene() == null) {
            target.setScene("");
        }
        if (target.getIcon() == null) {
            target.setIcon("");
        }
        if (target.getDescription() == null) {
            target.setDescription("");
        }
        LocalDateTime now = LocalDateTime.now();
        target.setUpdateTime(now);
        if (isNew) {
            target.setCreateTime(now);
            workflowMapper.insert(target);
        } else {
            workflowMapper.updateById(target);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("id", target.getId());
        result.put("code", target.getCode());
        result.put("version", target.getVersion());
        return result;
    }

    /** 复制一份：编码加 _copy 后缀，状态置为停用，避免直接上线一份没验证过的图 */
    public Map<String, Object> copy(String code) {
        AiWorkflow source = findByCode(code);
        if (source == null) {
            throw new IllegalArgumentException("工作流不存在：" + code);
        }
        String newCode = code + "_copy";
        int seq = 1;
        while (findByCode(newCode) != null) {
            newCode = code + "_copy" + (++seq);
        }
        AiWorkflow target = new AiWorkflow();
        target.setCode(newCode);
        target.setName(source.getName() + " 副本");
        target.setTerminal(source.getTerminal());
        target.setScene(source.getScene());
        target.setIcon(source.getIcon());
        target.setDescription(source.getDescription());
        target.setStatus(0);
        target.setModelCode(source.getModelCode());
        target.setGraph(source.getGraph());
        target.setVersion(1);
        target.setBuiltIn(0);
        target.setCreateTime(LocalDateTime.now());
        target.setUpdateTime(LocalDateTime.now());
        workflowMapper.insert(target);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("code", newCode);
        return result;
    }

    public Map<String, Object> delete(Long id) {
        AiWorkflow wf = workflowMapper.selectById(id);
        Map<String, Object> result = new LinkedHashMap<>();
        if (wf == null) {
            result.put("success", false);
            result.put("message", "工作流不存在");
            return result;
        }
        if (wf.getBuiltIn() != null && wf.getBuiltIn() == 1) {
            result.put("success", false);
            result.put("message", "内置工作流不允许删除，请先复制再修改");
            return result;
        }
        workflowMapper.deleteById(id);
        result.put("success", true);
        result.put("message", "已删除");
        return result;
    }

    public Map<String, Object> updateStatus(String code, Integer status) {
        AiWorkflow wf = findByCode(code);
        Map<String, Object> result = new LinkedHashMap<>();
        if (wf == null) {
            result.put("success", false);
            result.put("message", "工作流不存在：" + code);
            return result;
        }
        wf.setStatus(status == null || status != 1 ? 0 : 1);
        wf.setUpdateTime(LocalDateTime.now());
        workflowMapper.updateById(wf);
        result.put("success", true);
        result.put("message", wf.getStatus() == 1 ? "已启用" : "已停用");
        return result;
    }

    // ---------------- 图校验 ----------------

    /**
     * 保存前校验画布：起点唯一、终点存在、类型已知、无环、必填项齐全。
     *
     * @return 错误列表；空列表代表通过
     */
    @SuppressWarnings("unchecked")
    public List<String> validateGraph(String graph) {
        List<String> errors = new ArrayList<>();
        if (!StringUtils.hasText(graph)) {
            errors.add("画布为空");
            return errors;
        }
        Map<String, Object> g;
        try {
            g = objectMapper.readValue(graph, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            errors.add("画布 JSON 解析失败：" + e.getMessage());
            return errors;
        }
        Object nodesObj = g.get("nodes");
        if (!(nodesObj instanceof List<?> nodes) || nodes.isEmpty()) {
            errors.add("画布没有节点");
            return errors;
        }
        long starts = nodes.stream()
                .filter(n -> n instanceof Map && TYPE_START.equals(str((Map<String, Object>) n, "type")))
                .count();
        long ends = nodes.stream()
                .filter(n -> n instanceof Map && TYPE_END.equals(str((Map<String, Object>) n, "type")))
                .count();
        if (starts != 1) {
            errors.add("必须有且仅有 1 个「开始」节点，当前 " + starts + " 个");
        }
        if (ends < 1) {
            errors.add("至少要有 1 个「结束」节点");
        }
        for (Object n : nodes) {
            if (!(n instanceof Map<?, ?> nm)) {
                continue;
            }
            Map<String, Object> node = (Map<String, Object>) nm;
            String type = str(node, "type");
            String nodeId = str(node, "id");
            if (!KNOWN_TYPES.contains(type)) {
                errors.add("节点 " + nodeId + " 类型未知：" + type);
                continue;
            }
            Map<String, Object> config = nodeConfig(node);
            if (TYPE_LLM.equals(type) && !StringUtils.hasText(str(config, "userPrompt"))) {
                errors.add("模型节点「" + str(node, "name") + "」必须填写用户提示词");
            }
            if (TYPE_CONDITION.equals(type) && !StringUtils.hasText(str(config, "varName"))) {
                errors.add("条件节点「" + str(node, "name") + "」必须选择判断变量");
            }
        }
        if (hasCycle(g)) {
            errors.add("画布存在环路，工作流必须是单向无环图");
        }
        return errors;
    }

    private boolean hasCycle(Map<String, Object> graph) {
        Map<String, List<String>> adjacency = adjacency(graph);
        Map<String, Integer> state = new LinkedHashMap<>();
        for (String nodeId : adjacency.keySet()) {
            if (dfs(nodeId, adjacency, state)) {
                return true;
            }
        }
        return false;
    }

    private boolean dfs(String nodeId, Map<String, List<String>> adjacency, Map<String, Integer> state) {
        Integer s = state.get(nodeId);
        if (s != null && s == 1) {
            return true;
        }
        if (s != null && s == 2) {
            return false;
        }
        state.put(nodeId, 1);
        for (String next : adjacency.getOrDefault(nodeId, List.of())) {
            if (dfs(next, adjacency, state)) {
                return true;
            }
        }
        state.put(nodeId, 2);
        return false;
    }

    // ---------------- 执行引擎 ----------------

    /**
     * 取回工作流「开始节点」声明的入参名（按声明顺序）。
     * 供外部（如智能体会话）把用户输入映射成画布变量，避免调用方重复解析 graph。
     */
    @SuppressWarnings("unchecked")
    public List<String> inputNames(String code) {
        AiWorkflow wf = findByCode(code);
        if (wf == null || !StringUtils.hasText(wf.getGraph())) {
            return List.of();
        }
        try {
            Map<String, Object> graph = objectMapper.readValue(wf.getGraph(), new TypeReference<Map<String, Object>>() {
            });
            for (Object n : (List<Object>) graph.getOrDefault("nodes", List.of())) {
                if (!(n instanceof Map<?, ?> nm)) {
                    continue;
                }
                Map<String, Object> node = (Map<String, Object>) nm;
                if (!TYPE_START.equals(str(node, "type"))) {
                    continue;
                }
                List<String> names = new ArrayList<>();
                Object declared = nodeConfig(node).get("inputs");
                if (declared instanceof List<?> list) {
                    for (Object item : list) {
                        if (!(item instanceof Map<?, ?> im)) {
                            continue;
                        }
                        String name = str((Map<String, Object>) im, "name");
                        if (StringUtils.hasText(name)) {
                            names.add(name);
                        }
                    }
                }
                return names;
            }
        } catch (Exception e) {
            log.warn("[aiagent] 解析工作流入参失败 code={} msg={}", code, e.getMessage());
        }
        return List.of();
    }

    /**
     * 执行一次工作流。
     *
     * @param code   工作流编码
     * @param inputs 入参（键为「开始」节点声明的变量名）
     * @param scene  场景，用于用量归因
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> run(String code, Map<String, Object> inputs, String scene) {
        Map<String, Object> result = new LinkedHashMap<>();
        AiWorkflow wf = findByCode(code);
        if (wf == null) {
            result.put("status", "failed");
            result.put("errorMsg", "工作流不存在：" + code);
            return result;
        }
        List<String> errors = validateGraph(wf.getGraph());
        if (!errors.isEmpty()) {
            result.put("status", "failed");
            result.put("errorMsg", "画布校验未通过：" + String.join("；", errors));
            return result;
        }

        Map<String, Object> graph;
        try {
            graph = objectMapper.readValue(wf.getGraph(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            result.put("status", "failed");
            result.put("errorMsg", "画布解析失败：" + e.getMessage());
            return result;
        }

        String runNo = newRunNo();
        AiWorkflowRun record = new AiWorkflowRun();
        record.setRunNo(runNo);
        record.setWorkflowCode(wf.getCode());
        record.setWorkflowName(wf.getName());
        record.setTerminal(wf.getTerminal());
        record.setStatus("running");
        record.setInputs(toJson(inputs == null ? Map.of() : inputs));
        record.setCreateTime(LocalDateTime.now());
        record.setCostMs(0);
        record.setTokenUsed(0);
        runMapper.insert(record);

        long began = System.currentTimeMillis();
        List<Map<String, Object>> traces = new ArrayList<>();
        Map<String, Object> ctx = new LinkedHashMap<>();
        String status = "success";
        String errorMsg = "";
        String finalOutput = "";
        int tokenUsed = 0;

        try {
            Map<String, Map<String, Object>> nodeIndex = new LinkedHashMap<>();
            for (Object n : (List<Object>) graph.getOrDefault("nodes", List.of())) {
                if (n instanceof Map<?, ?> nm) {
                    Map<String, Object> node = (Map<String, Object>) nm;
                    nodeIndex.put(str(node, "id"), node);
                }
            }
            Map<String, List<Map<String, Object>>> outgoing = new LinkedHashMap<>();
            for (Object e : (List<Object>) graph.getOrDefault("edges", List.of())) {
                if (e instanceof Map<?, ?> em) {
                    Map<String, Object> edge = (Map<String, Object>) em;
                    outgoing.computeIfAbsent(str(edge, "source"), k -> new ArrayList<>()).add(edge);
                }
            }

            String startId = nodeIndex.values().stream()
                    .filter(n -> TYPE_START.equals(str(n, "type")))
                    .map(n -> str(n, "id"))
                    .findFirst().orElse(null);
            if (startId == null) {
                throw new IllegalStateException("找不到开始节点");
            }

            Deque<String> queue = new ArrayDeque<>();
            Set<String> enqueued = new java.util.HashSet<>();
            queue.add(startId);
            enqueued.add(startId);
            int visits = 0;

            while (!queue.isEmpty()) {
                String nodeId = queue.poll();
                if (++visits > MAX_NODE_VISITS) {
                    throw new IllegalStateException("节点执行次数超过上限 " + MAX_NODE_VISITS + "，疑似环路");
                }
                Map<String, Object> node = nodeIndex.get(nodeId);
                if (node == null) {
                    continue;
                }
                String type = str(node, "type");
                Map<String, Object> config = nodeConfig(node);
                Map<String, Object> trace = new LinkedHashMap<>();
                trace.put("nodeId", nodeId);
                trace.put("name", str(node, "name"));
                trace.put("type", type);
                long nodeStart = System.currentTimeMillis();
                trace.put("input", renderBrief(config));

                if (TYPE_START.equals(type)) {
                    Object declared = config.get("inputs");
                    if (declared instanceof List<?> list) {
                        for (Object item : list) {
                            if (!(item instanceof Map<?, ?> im)) {
                                continue;
                            }
                            Map<String, Object> var = (Map<String, Object>) im;
                            String varName = str(var, "name");
                            if (!StringUtils.hasText(varName)) {
                                continue;
                            }
                            Object value = inputs == null ? null : inputs.get(varName);
                            if (value == null || String.valueOf(value).isEmpty()) {
                                value = var.get("default");
                            }
                            ctx.put(varName, value == null ? "" : value);
                        }
                    }
                    trace.put("output", new LinkedHashMap<>(ctx));
                } else if (TYPE_LLM.equals(type)) {
                    ModelClient.ModelChatRequest request = new ModelClient.ModelChatRequest()
                            .setModel(StringUtils.hasText(str(config, "modelCode"))
                                    ? str(config, "modelCode") : wf.getModelCode())
                            .setSystemPrompt(render(str(config, "systemPrompt"), ctx))
                            .setUserPrompt(render(str(config, "userPrompt"), ctx))
                            .setScene(scene == null || scene.isEmpty() ? "workflow:" + wf.getCode() : scene);
                    Double temperature = toDouble(config.get("temperature"));
                    if (temperature != null) {
                        request.setTemperature(temperature);
                    }
                    Integer maxTokens = toInt(config.get("maxTokens"));
                    if (maxTokens != null && maxTokens > 0) {
                        request.setMaxTokens(maxTokens);
                    }
                    ModelClient.ModelChatResult chat = chatWithTimeout(request, runNo);
                    if (!chat.isSuccess()) {
                        trace.put("status", "failed");
                        trace.put("error", chat.getErrorMessage());
                        trace.put("costMs", System.currentTimeMillis() - nodeStart);
                        traces.add(trace);
                        throw new IllegalStateException("模型节点「" + str(node, "name") + "」调用失败："
                                + chat.getErrorMessage());
                    }
                    String content = chat.getContent() == null ? "" : chat.getContent();
                    tokenUsed += (chat.getTotalTokens() == null ? 0 : chat.getTotalTokens());
                    String outputVar = str(config, "outputVar");
                    if (!StringUtils.hasText(outputVar)) {
                        outputVar = nodeId;
                    }
                    ctx.put(outputVar, content);
                    trace.put("output", content);
                } else if (TYPE_TRANSFORM.equals(type)) {
                    String value = render(str(config, "template"), ctx);
                    String outputVar = str(config, "outputVar");
                    if (!StringUtils.hasText(outputVar)) {
                        outputVar = nodeId;
                    }
                    ctx.put(outputVar, value);
                    trace.put("output", value);
                } else if (TYPE_CONDITION.equals(type)) {
                    boolean hit = evalCondition(str(config, "varName"), str(config, "operator"),
                            str(config, "value"), ctx);
                    trace.put("output", hit ? "true" : "false");
                    trace.put("status", "success");
                    trace.put("costMs", System.currentTimeMillis() - nodeStart);
                    traces.add(trace);
                    for (Map<String, Object> edge : outgoing.getOrDefault(nodeId, List.of())) {
                        String handle = str(edge, "sourceHandle");
                        boolean wanted = hit ? "true".equals(handle) : "false".equals(handle);
                        if (!wanted) {
                            continue;
                        }
                        String target = str(edge, "target");
                        if (enqueued.add(target)) {
                            queue.add(target);
                        }
                    }
                    continue;
                } else if (TYPE_END.equals(type)) {
                    finalOutput = render(str(config, "outputTemplate"), ctx);
                    if (!StringUtils.hasText(finalOutput)) {
                        // 结束节点没配模板时，回退到最后一个模型节点的输出，避免整条链路白跑
                        for (Object v : new ArrayList<>(ctx.values())) {
                            if (v != null && String.valueOf(v).length() > 0) {
                                finalOutput = String.valueOf(v);
                            }
                        }
                    }
                    trace.put("output", finalOutput);
                }

                trace.put("status", "success");
                trace.put("costMs", System.currentTimeMillis() - nodeStart);
                traces.add(trace);

                if (TYPE_END.equals(type)) {
                    continue;
                }
                for (Map<String, Object> edge : outgoing.getOrDefault(nodeId, List.of())) {
                    String target = str(edge, "target");
                    if (enqueued.add(target)) {
                        queue.add(target);
                    }
                }
            }
        } catch (Exception e) {
            status = "failed";
            errorMsg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            log.warn("[aiagent] 工作流执行失败 code={} runNo={} msg={}", code, runNo, errorMsg);
        } finally {
            // 🔴 无论成功、失败还是被中断，运行记录都必须收口。
            // 之前回写写在 try/catch 之后：一旦线程被中断或抛出 Error，记录会永远停在
            // running，运行记录页一直转圈，也拿不到失败原因（实测出现过两条 15 分钟不落定的 run）。
            if ("running".equals(status)) {
                status = "failed";
                if (!StringUtils.hasText(errorMsg)) {
                    errorMsg = "执行中断，未产出结果";
                }
            }
            int costMs = (int) (System.currentTimeMillis() - began);
            Map<String, Object> outputs = new LinkedHashMap<>();
            outputs.put("text", finalOutput);
            outputs.put("vars", ctx);
            try {
                record.setStatus(status);
                record.setOutputs(toJson(outputs));
                record.setTraces(toJson(traces));
                record.setErrorMsg(errorMsg == null ? "" : errorMsg);
                record.setCostMs(costMs);
                record.setTokenUsed(tokenUsed);
                record.setFinishTime(LocalDateTime.now());
                runMapper.updateById(record);
            } catch (Exception e) {
                log.warn("[aiagent] 工作流运行记录回写失败 runNo={} msg={}", runNo, e.getMessage());
            }

            result.put("runNo", runNo);
            result.put("status", status);
            result.put("errorMsg", errorMsg);
            result.put("costMs", costMs);
            result.put("tokenUsed", tokenUsed);
            result.put("outputs", outputs);
            result.put("traces", traces);
        }
        return result;
    }

    /**
     * 兜底收口：执行被中断（容器重启 / 客户端断开 / 抛出 Error）时运行记录会停在 running，
     * 查询列表时把超过 10 分钟仍未落定的置为失败，免得运行记录页永远转圈。
     */
    private void settleStaleRuns() {
        try {
            LambdaQueryWrapper<AiWorkflowRun> query = new LambdaQueryWrapper<>();
            query.eq(AiWorkflowRun::getStatus, "running")
                    .lt(AiWorkflowRun::getCreateTime, LocalDateTime.now().minusMinutes(10));
            List<AiWorkflowRun> stale = runMapper.selectList(query);
            for (AiWorkflowRun row : stale) {
                row.setStatus("failed");
                row.setErrorMsg("执行中断，未产出结果（超过 10 分钟未落定，系统自动收口）");
                row.setFinishTime(LocalDateTime.now());
                runMapper.updateById(row);
            }
            if (!stale.isEmpty()) {
                log.warn("[aiagent] 工作流运行记录自动收口 {} 条", stale.size());
            }
        } catch (Exception e) {
            log.warn("[aiagent] 工作流运行记录收口失败 msg={}", e.getMessage());
        }
    }

    /** 单节点模型调用超时：网关经 frp 暴露，实测会「连上但不返回」，没有上限会把整条流程吊死 */
    private static final long NODE_CHAT_TIMEOUT_MS = 180_000L;

    /**
     * 带超时的模型调用，超时按节点失败处理。
     *
     * <p>没有这层保护时，网关一挂起就会让运行记录永远停在 running、前端一直转圈、
     * 执行线程也放不掉；加超时后流程照常收口落库，用户看到的是「某节点超时」而不是无止尽的等待。
     */
    private ModelClient.ModelChatResult chatWithTimeout(ModelClient.ModelChatRequest request, String runNo) {
        try {
            return CompletableFuture.supplyAsync(() -> modelClient.chat(request))
                    .orTimeout(NODE_CHAT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .get();
        } catch (Exception e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            String msg = cause instanceof java.util.concurrent.TimeoutException
                    ? "模型调用超时（超过 " + (NODE_CHAT_TIMEOUT_MS / 1000) + " 秒未返回）"
                    : (cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage());
            log.warn("[aiagent] 工作流节点模型调用失败 runNo={} msg={}", runNo, msg);
            return new ModelClient.ModelChatResult()
                    .setSuccess(false)
                    .setErrorCode("AI_CALL_FAILED")
                    .setErrorMessage(msg);
        }
    }

    // ---------------- 运行记录 ----------------

    public Map<String, Object> runs(Map<String, Object> params) {
        settleStaleRuns();
        LambdaQueryWrapper<AiWorkflowRun> wrapper = new LambdaQueryWrapper<>();
        String workflowCode = AiPageSupport.str(params, "workflowCode");
        if (StringUtils.hasText(workflowCode)) {
            wrapper.eq(AiWorkflowRun::getWorkflowCode, workflowCode);
        }
        String status = AiPageSupport.str(params, "status");
        if (StringUtils.hasText(status)) {
            wrapper.eq(AiWorkflowRun::getStatus, status);
        }
        wrapper.orderByDesc(AiWorkflowRun::getId);
        IPage<AiWorkflowRun> page = runMapper.selectPage(AiPageSupport.page(params), wrapper);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AiWorkflowRun r : page.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.getId());
            item.put("runNo", r.getRunNo());
            item.put("workflowCode", r.getWorkflowCode());
            item.put("workflowName", r.getWorkflowName());
            item.put("terminal", r.getTerminal());
            item.put("status", r.getStatus());
            item.put("costMs", r.getCostMs());
            item.put("tokenUsed", r.getTokenUsed());
            item.put("errorMsg", r.getErrorMsg());
            item.put("createTime", r.getCreateTime() == null ? null : r.getCreateTime().toString());
            item.put("finishTime", r.getFinishTime() == null ? null : r.getFinishTime().toString());
            rows.add(item);
        }
        return AiPageSupport.pageResult(page, rows);
    }

    public Map<String, Object> runDetail(String runNo) {
        List<AiWorkflowRun> list = runMapper.selectList(
                new LambdaQueryWrapper<AiWorkflowRun>().eq(AiWorkflowRun::getRunNo, runNo).last("limit 1"));
        Map<String, Object> result = new LinkedHashMap<>();
        if (list == null || list.isEmpty()) {
            return result;
        }
        AiWorkflowRun r = list.get(0);
        result.put("runNo", r.getRunNo());
        result.put("workflowCode", r.getWorkflowCode());
        result.put("workflowName", r.getWorkflowName());
        result.put("status", r.getStatus());
        result.put("costMs", r.getCostMs());
        result.put("tokenUsed", r.getTokenUsed());
        result.put("errorMsg", r.getErrorMsg());
        result.put("inputs", parseAny(r.getInputs()));
        result.put("outputs", parseAny(r.getOutputs()));
        result.put("traces", parseAny(r.getTraces()));
        result.put("createTime", r.getCreateTime() == null ? null : r.getCreateTime().toString());
        result.put("finishTime", r.getFinishTime() == null ? null : r.getFinishTime().toString());
        return result;
    }

    // ---------------- 节点执行辅助 ----------------

    @SuppressWarnings("unchecked")
    private static Map<String, Object> nodeConfig(Map<String, Object> node) {
        Object cfg = node.get("config");
        return cfg instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
    }

    /** 渲染 {@code {{var}}}：取不到的变量替换为空串，并在返回体里不做任何编造 */
    private static String render(String template, Map<String, Object> ctx) {
        if (!StringUtils.hasText(template)) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < template.length()) {
            int open = template.indexOf("{{", i);
            if (open < 0) {
                out.append(template.substring(i));
                break;
            }
            out.append(template, i, open);
            int close = template.indexOf("}}", open + 2);
            if (close < 0) {
                out.append(template.substring(open));
                break;
            }
            String name = template.substring(open + 2, close).trim();
            Object value = ctx.get(name);
            out.append(value == null ? "" : String.valueOf(value));
            i = close + 2;
        }
        return out.toString();
    }

    /** 轨迹里记录节点配置的精简版，避免把整段提示词塞进运行记录 */
    private String renderBrief(Map<String, Object> config) {
        Map<String, Object> brief = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : config.entrySet()) {
            Object v = e.getValue();
            String s = v == null ? "" : String.valueOf(v);
            brief.put(e.getKey(), s.length() > 200 ? s.substring(0, 200) + "…" : s);
        }
        return toJson(brief);
    }

    private static boolean evalCondition(String varName, String operator, String expected,
                                         Map<String, Object> ctx) {
        Object actual = ctx.get(varName);
        String text = actual == null ? "" : String.valueOf(actual);
        String op = operator == null || operator.isEmpty() ? "contains" : operator.toLowerCase(Locale.ROOT);
        String want = expected == null ? "" : expected;
        switch (op) {
            case "eq":
                return text.trim().equalsIgnoreCase(want.trim());
            case "neq":
                return !text.trim().equalsIgnoreCase(want.trim());
            case "contains":
                return text.toLowerCase(Locale.ROOT).contains(want.trim().toLowerCase(Locale.ROOT));
            case "notcontains":
                return !text.toLowerCase(Locale.ROOT).contains(want.trim().toLowerCase(Locale.ROOT));
            case "isempty":
                return text.trim().isEmpty();
            case "notempty":
                return !text.trim().isEmpty();
            case "gt":
            case "gte":
            case "lt":
            case "lte": {
                Double a = toDouble(text);
                Double b = toDouble(want);
                if (a == null || b == null) {
                    return false;
                }
                return switch (op) {
                    case "gt" -> a > b;
                    case "gte" -> a >= b;
                    case "lt" -> a < b;
                    default -> a <= b;
                };
            }
            default:
                return text.toLowerCase(Locale.ROOT).contains(want.trim().toLowerCase(Locale.ROOT));
        }
    }

    private static Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(String.valueOf(value).trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return (int) Double.parseDouble(String.valueOf(value).trim());
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<String>> adjacency(Map<String, Object> graph) {
        Map<String, List<String>> adjacency = new LinkedHashMap<>();
        Object nodesObj = graph.get("nodes");
        if (nodesObj instanceof List<?> nodes) {
            for (Object n : nodes) {
                if (n instanceof Map<?, ?> nm) {
                    adjacency.putIfAbsent(str((Map<String, Object>) nm, "id"), new ArrayList<>());
                }
            }
        }
        Object edgesObj = graph.get("edges");
        if (edgesObj instanceof List<?> edges) {
            for (Object e : edges) {
                if (e instanceof Map<?, ?> em) {
                    Map<String, Object> edge = (Map<String, Object>) em;
                    adjacency.computeIfAbsent(str(edge, "source"), k -> new ArrayList<>())
                            .add(str(edge, "target"));
                }
            }
        }
        return adjacency;
    }

    // ---------------- 视图与小工具 ----------------

    private Map<String, Object> toView(AiWorkflow wf, boolean withGraph) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", wf.getId());
        item.put("code", wf.getCode());
        item.put("name", wf.getName());
        item.put("terminal", wf.getTerminal());
        item.put("scene", wf.getScene());
        item.put("icon", wf.getIcon());
        item.put("description", wf.getDescription());
        item.put("status", wf.getStatus());
        item.put("modelCode", wf.getModelCode());
        item.put("version", wf.getVersion());
        item.put("builtIn", wf.getBuiltIn());
        item.put("updateTime", wf.getUpdateTime() == null ? null : wf.getUpdateTime().toString());
        if (withGraph) {
            item.put("graph", parseJson(wf.getGraph()));
        } else {
            item.put("nodeCount", countNodes(wf.getGraph()));
        }
        return item;
    }

    private int countNodes(String graph) {
        Map<String, Object> parsed = parseJson(graph);
        Object nodes = parsed == null ? null : parsed.get("nodes");
        return nodes instanceof List<?> list ? list.size() : 0;
    }

    private Map<String, Object> parseJson(String json) {
        if (!StringUtils.hasText(json)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private Object parseAny(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Object>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    private static String str(Map<String, Object> map, String key) {
        Object v = map == null ? null : map.get(key);
        return v == null ? "" : String.valueOf(v);
    }

    private static void put(Map<String, Object> data, String key, java.util.function.Consumer<String> setter) {
        String value = AiPageSupport.str(data, key);
        if (StringUtils.hasText(value)) {
            setter.accept(value);
        }
    }

    private String normalizeTerminal(String terminal) {
        if (!StringUtils.hasText(terminal)) {
            return "merchant";
        }
        String v = terminal.trim().toLowerCase(Locale.ROOT);
        return switch (v) {
            case "merchant", "frontend", "admin", "im" -> v;
            default -> "merchant";
        };
    }

    private String newRunNo() {
        return "WFR" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
                + UUID.randomUUID().toString().substring(0, 6);
    }
}
