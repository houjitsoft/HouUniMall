package com.houjiang.aiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.aiagent.entity.AiAgent;
import com.houjiang.aiagent.entity.AiAgentInvokeLog;
import com.houjiang.aiagent.mapper.AiAgentInvokeLogMapper;
import com.houjiang.aiagent.mapper.AiAgentMapper;
import com.houjiang.aiagent.support.AiPageSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AI 中台 - 智能体（Agent）统一管理。
 *
 * <p>后台在这里配置「用哪个模型 + 什么提示词 + 何时触发 + 对谁生效」，
 * 前台通过 {@link #config(String)} 按 code 取回配置后调用，
 * 避免提示词与模型选型散落在各业务代码里。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAgentService {

    /** 状态：启用 */
    public static final int STATUS_ENABLED = 1;
    /** 状态：停用 */
    public static final int STATUS_DISABLED = 0;

    private static final Map<String, String> SCENE_LABELS = sceneLabels();

    private final AiAgentMapper agentMapper;
    private final AiAgentInvokeLogMapper invokeLogMapper;

    // ---------------- 列表与详情 ----------------

    public Map<String, Object> list(Map<String, Object> params) {
        LambdaQueryWrapper<AiAgent> wrapper = new LambdaQueryWrapper<>();
        String terminal = AiPageSupport.str(params, "terminal");
        if (StringUtils.hasText(terminal)) {
            wrapper.eq(AiAgent::getTerminal, terminal);
        }
        String scene = AiPageSupport.str(params, "scene");
        if (StringUtils.hasText(scene)) {
            wrapper.eq(AiAgent::getScene, scene);
        }
        Integer status = AiPageSupport.toInt(params, "status");
        if (status != null) {
            wrapper.eq(AiAgent::getStatus, status);
        }
        String keyword = AiPageSupport.str(params, "keyword");
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(AiAgent::getName, keyword).or().like(AiAgent::getCode, keyword));
        }
        wrapper.orderByAsc(AiAgent::getSort).orderByDesc(AiAgent::getUpdateTime);
        IPage<AiAgent> page = agentMapper.selectPage(AiPageSupport.page(params), wrapper);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AiAgent agent : page.getRecords()) {
            rows.add(toView(agent));
        }
        return AiPageSupport.pageResult(page, rows);
    }

    public Map<String, Object> detail(String code) {
        AiAgent agent = findByCode(code);
        return agent == null ? new LinkedHashMap<>() : toView(agent);
    }

    public AiAgent findByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        return agentMapper.selectOne(new LambdaQueryWrapper<AiAgent>()
                .eq(AiAgent::getCode, code.trim())
                .last("limit 1"));
    }

    // ---------------- 编辑 ----------------

    /**
     * 新增或更新。{@code id} 或 {@code code} 命中即更新，否则新增。
     */
    public AiAgent save(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        AiAgent target = resolveTarget(data);
        if (target == null) {
            String code = AiPageSupport.str(data, "code");
            if (!StringUtils.hasText(code)) {
                return null;
            }
            target = new AiAgent();
            target.setCode(code.trim());
            target.setStatus(STATUS_DISABLED);
            target.setBuiltIn(0);
        }
        applyPatch(target, data);
        if (target.getId() == null) {
            target.setSort(target.getSort() == null ? 100 : target.getSort());
            agentMapper.insert(target);
        } else {
            agentMapper.updateById(target);
        }
        return target;
    }

    /**
     * 启用 / 停用。返回是否命中并更新。
     */
    public boolean updateStatus(String code, Integer status) {
        AiAgent agent = findByCode(code);
        if (agent == null) {
            return false;
        }
        agent.setStatus(status == null || status == STATUS_ENABLED ? STATUS_ENABLED : STATUS_DISABLED);
        agent.setUpdateTime(LocalDateTime.now());
        return agentMapper.updateById(agent) > 0;
    }

    /**
     * 删除。内置智能体（built_in=1）不允许删除。
     */
    public Map<String, Object> delete(Long id) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (id == null) {
            result.put("success", false);
            result.put("message", "缺少智能体 id");
            return result;
        }
        AiAgent agent = agentMapper.selectById(id);
        if (agent == null) {
            result.put("success", false);
            result.put("message", "智能体不存在");
            return result;
        }
        if (agent.getBuiltIn() != null && agent.getBuiltIn() == 1) {
            result.put("success", false);
            result.put("message", "内置智能体不可删除，可停用");
            return result;
        }
        result.put("success", agentMapper.deleteById(id) > 0);
        return result;
    }

    // ---------------- 场景字典 ----------------

    public List<Map<String, Object>> scenes() {
        List<Map<String, Object>> list = new ArrayList<>();
        SCENE_LABELS.forEach((value, label) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("value", value);
            item.put("label", label);
            list.add(item);
        });
        return list;
    }

    // ---------------- 前台调用配置 ----------------

    /**
     * 前台按 code 取回可执行的智能体配置。未启用或未配置模型时返回 enabled=false，
     * 让前台走既有兜底逻辑，而不是抛错。
     */
    public Map<String, Object> config(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        AiAgent agent = findByCode(code);
        if (agent == null) {
            result.put("enabled", false);
            result.put("reason", "智能体不存在：" + code);
            return result;
        }
        boolean active = agent.getStatus() != null && agent.getStatus() == STATUS_ENABLED;
        // 工作流模式由画布节点决定用哪个模型，不强制智能体自身绑模型；但必须关联一个工作流
        boolean workflowMode = "workflow".equals(normalizeMode(agent.getMode()));
        boolean enabled = active && (workflowMode
                ? StringUtils.hasText(agent.getWorkflowCode())
                : StringUtils.hasText(agent.getModelCode()));
        result.put("enabled", enabled);
        if (!enabled) {
            result.put("reason", !active ? "智能体已停用"
                    : workflowMode ? "智能体未关联工作流，请在后台「智能体管理」中选择"
                    : "智能体未绑定模型，请在后台「智能体管理」中配置");
        }
        result.put("mode", normalizeMode(agent.getMode()));
        result.put("workflowCode", agent.getWorkflowCode() == null ? "" : agent.getWorkflowCode());
        result.put("code", agent.getCode());
        result.put("name", agent.getName());
        result.put("scene", agent.getScene());
        result.put("modelCode", agent.getModelCode());
        result.put("usagePage", agent.getUsagePage() == null ? "" : agent.getUsagePage());
        result.put("prompt", agent.getPrompt());
        result.put("triggerType", agent.getTriggerType());
        result.put("triggerConfig", agent.getTriggerConfig());
        result.put("scopeType", agent.getScopeType());
        result.put("scopeConfig", agent.getScopeConfig());
        result.put("params", agent.getParams());
        return result;
    }

    // ---------------- 调用记录 ----------------

    public Map<String, Object> invokeLogs(Map<String, Object> params) {
        LambdaQueryWrapper<AiAgentInvokeLog> wrapper = new LambdaQueryWrapper<>();
        String agentCode = AiPageSupport.str(params, "agentCode");
        if (StringUtils.hasText(agentCode)) {
            wrapper.eq(AiAgentInvokeLog::getAgentCode, agentCode);
        }
        String scene = AiPageSupport.str(params, "scene");
        if (StringUtils.hasText(scene)) {
            wrapper.eq(AiAgentInvokeLog::getScene, scene);
        }
        Integer status = AiPageSupport.toInt(params, "status");
        if (status != null) {
            wrapper.eq(AiAgentInvokeLog::getStatus, status);
        }
        String bizId = AiPageSupport.str(params, "bizId");
        if (StringUtils.hasText(bizId)) {
            wrapper.eq(AiAgentInvokeLog::getBizId, bizId);
        }
        wrapper.orderByDesc(AiAgentInvokeLog::getCreateTime).orderByDesc(AiAgentInvokeLog::getId);
        IPage<AiAgentInvokeLog> page = invokeLogMapper.selectPage(AiPageSupport.page(params), wrapper);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AiAgentInvokeLog item : page.getRecords()) {
            rows.add(logToView(item));
        }
        return AiPageSupport.pageResult(page, rows);
    }

    /**
     * 记录一次调用。调用方（前台 / 内部服务）在拿到结果后上报。
     */
    public void recordInvoke(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        try {
            AiAgentInvokeLog log = new AiAgentInvokeLog();
            String code = AiPageSupport.str(data, "agentCode");
            AiAgent agent = findByCode(code);
            log.setAgentId(agent == null ? 0L : agent.getId());
            log.setAgentCode(code == null ? "" : code);
            log.setScene(agent == null ? "" : nz(agent.getScene()));
            log.setSubjectType(nz(AiPageSupport.str(data, "subjectType")));
            log.setSubjectKey(nz(AiPageSupport.str(data, "subjectKey")));
            log.setBizId(nz(AiPageSupport.str(data, "bizId")));
            log.setModelName(nz(AiPageSupport.str(data, "modelName")));
            log.setInputSummary(clip(AiPageSupport.str(data, "inputSummary"), 500));
            log.setOutputSummary(clip(AiPageSupport.str(data, "outputSummary"), 1000));
            int promptTokens = intOf(data, "promptTokens");
            int completionTokens = intOf(data, "completionTokens");
            log.setPromptTokens(promptTokens);
            log.setCompletionTokens(completionTokens);
            log.setTotalTokens(promptTokens + completionTokens);
            log.setLatencyMs(intOf(data, "latencyMs"));
            log.setStatus(intOf(data, "status", STATUS_ENABLED));
            log.setErrorMessage(clip(AiPageSupport.str(data, "errorMessage"), 500));
            invokeLogMapper.insert(log);
        } catch (Exception e) {
            log.warn("[aiagent] 记录智能体调用失败 code={} msg={}", data.get("agentCode"), e.getMessage());
        }
    }

    // ---------------- 内部方法 ----------------

    private AiAgent resolveTarget(Map<String, Object> data) {
        Long id = AiPageSupport.toLong(data, "id");
        if (id != null) {
            AiAgent byId = agentMapper.selectById(id);
            if (byId != null) {
                return byId;
            }
        }
        String code = AiPageSupport.str(data, "code");
        return findByCode(code);
    }

    private void applyPatch(AiAgent target, Map<String, Object> data) {
        put(target, data, "name", v -> target.setName(v));
        put(target, data, "scene", v -> target.setScene(v));
        put(target, data, "terminal", v -> target.setTerminal(normalizeTerminal(v)));
        put(target, data, "icon", v -> target.setIcon(v));
        put(target, data, "description", v -> target.setDescription(v));
        put(target, data, "usagePage", v -> target.setUsagePage(v));
        put(target, data, "pcPage", v -> target.setPcPage(v));
        put(target, data, "pcPath", v -> target.setPcPath(v));
        put(target, data, "h5Page", v -> target.setH5Page(v));
        put(target, data, "h5Path", v -> target.setH5Path(v));
        put(target, data, "modelCode", v -> target.setModelCode(v));
        put(target, data, "prompt", v -> target.setPrompt(v));
        put(target, data, "mode", v -> target.setMode(normalizeMode(v)));
        put(target, data, "workflowCode", v -> target.setWorkflowCode(v));
        put(target, data, "triggerType", v -> target.setTriggerType(v));
        put(target, data, "triggerConfig", v -> target.setTriggerConfig(v));
        put(target, data, "scopeType", v -> target.setScopeType(v));
        put(target, data, "scopeConfig", v -> target.setScopeConfig(v));
        put(target, data, "params", v -> target.setParams(v));
        Integer status = AiPageSupport.toInt(data, "status");
        if (status != null) {
            target.setStatus(status);
        }
        Integer sort = AiPageSupport.toInt(data, "sort");
        if (sort != null) {
            target.setSort(sort);
        }
        target.setUpdateTime(LocalDateTime.now());
    }

    private void put(AiAgent target, Map<String, Object> data, String key, java.util.function.Consumer<String> setter) {
        String value = AiPageSupport.str(data, key);
        if (StringUtils.hasText(value)) {
            setter.accept(value);
        }
    }

    private Map<String, Object> toView(AiAgent a) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", a.getId());
        item.put("code", a.getCode());
        item.put("name", a.getName());
        item.put("scene", a.getScene());
        item.put("sceneLabel", SCENE_LABELS.getOrDefault(a.getScene(), a.getScene()));
        item.put("icon", a.getIcon());
        item.put("description", a.getDescription());
        item.put("usagePage", a.getUsagePage() == null ? "" : a.getUsagePage());
        item.put("pcPage", a.getPcPage() == null ? "" : a.getPcPage());
        item.put("pcPath", a.getPcPath() == null ? "" : a.getPcPath());
        item.put("h5Page", a.getH5Page() == null ? "" : a.getH5Page());
        item.put("h5Path", a.getH5Path() == null ? "" : a.getH5Path());
        item.put("status", a.getStatus() == null ? STATUS_DISABLED : a.getStatus());
        item.put("statusText", a.getStatus() != null && a.getStatus() == STATUS_ENABLED ? "启用" : "停用");
        item.put("modelCode", a.getModelCode());
        item.put("modelReady", StringUtils.hasText(a.getModelCode()));
        item.put("prompt", a.getPrompt());
        item.put("mode", normalizeMode(a.getMode()));
        item.put("workflowCode", a.getWorkflowCode() == null ? "" : a.getWorkflowCode());
        item.put("triggerType", a.getTriggerType());
        item.put("triggerConfig", a.getTriggerConfig());
        item.put("terminal", a.getTerminal());
        item.put("scopeType", a.getScopeType());
        item.put("scopeConfig", a.getScopeConfig());
        item.put("params", a.getParams());
        item.put("builtIn", a.getBuiltIn());
        item.put("sort", a.getSort());
        item.put("createTime", a.getCreateTime());
        item.put("updateTime", a.getUpdateTime());
        return item;
    }

    private Map<String, Object> logToView(AiAgentInvokeLog l) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", l.getId());
        item.put("agentId", l.getAgentId());
        item.put("agentCode", l.getAgentCode());
        item.put("scene", l.getScene());
        item.put("sceneLabel", SCENE_LABELS.getOrDefault(l.getScene(), l.getScene()));
        item.put("subjectType", l.getSubjectType());
        item.put("subjectKey", l.getSubjectKey());
        item.put("bizId", l.getBizId());
        item.put("modelName", l.getModelName());
        item.put("inputSummary", l.getInputSummary());
        item.put("outputSummary", l.getOutputSummary());
        item.put("promptTokens", l.getPromptTokens());
        item.put("completionTokens", l.getCompletionTokens());
        item.put("totalTokens", l.getTotalTokens());
        item.put("latencyMs", l.getLatencyMs());
        item.put("status", l.getStatus());
        item.put("statusText", l.getStatus() != null && l.getStatus() == 1 ? "成功" : "失败");
        item.put("errorMessage", l.getErrorMessage());
        item.put("createTime", l.getCreateTime());
        return item;
    }

    private static int intOf(Map<String, Object> data, String key) {
        return intOf(data, key, 0);
    }

    private static int intOf(Map<String, Object> data, String key, int fallback) {
        Integer value = AiPageSupport.toInt(data, key);
        return value == null ? fallback : value;
    }

    private static String nz(String value) {
        return value == null ? "" : value;
    }

    private static String clip(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private static Map<String, String> sceneLabels() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("recommend", "推荐转化");
        map.put("service", "智能客服");
        map.put("order", "订单履约");
        map.put("content", "内容生成");
        map.put("marketing", "营销活动");
        map.put("insight", "洞察分析");
        map.put("supply", "供应链");
        map.put("aftersale", "售后服务");
        return map;
    }

    /** 执行模式收敛：只允许 prompt 提示词 / workflow 工作流，其余按 prompt 处理 */
    private String normalizeMode(String mode) {
        if (!StringUtils.hasText(mode)) {
            return "prompt";
        }
        String v = mode.trim().toLowerCase(Locale.ROOT);
        return "workflow".equals(v) ? "workflow" : "prompt";
    }

    /** 端取值收敛：只允许 merchant / frontend / admin / im，其余按 frontend 处理
     *  im = IM 客服与消息通知线（backend/paas/im + frontend/imchat + hou_message_* 消息中心） */
    private String normalizeTerminal(String terminal) {
        if (!StringUtils.hasText(terminal)) {
            return "frontend";
        }
        String v = terminal.trim().toLowerCase();
        return switch (v) {
            case "merchant", "frontend", "admin", "im" -> v;
            default -> "frontend";
        };
    }
}
