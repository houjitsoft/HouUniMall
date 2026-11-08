package com.houjiang.bffweb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * xxl-job Admin API 客户端
/**
 * 封装 xxl-job 管理后台的 HTTP API，供 BFF 层调用
 *
 * @author Amos
 */
@Slf4j
@Service
public class XxlJobAdminClient {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${xxl.job.admin.addresses:http://127.0.0.1:38082/xxl-job-admin}")
    private String adminAddresses;

    @Value("${xxl.job.admin.username:admin}")
    private String username;

    @Value("${xxl.job.admin.password:123456}")
    private String password;

    private final RestTemplate restTemplate = createRestTemplate();
    private volatile String cookie = null;
    private volatile long cookieExpireAt = 0;

    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        return new RestTemplate(factory);
    }

    /**
     * 获取登录 Cookie（带缓存，5 分钟过期）
     */
    private String getCookie() {
        long now = System.currentTimeMillis();
        if (cookie != null && cookieExpireAt > now) {
            return cookie;
        }
        synchronized (this) {
            if (cookie != null && cookieExpireAt > now) {
                return cookie;
            }
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
                body.add("userName", username);
                body.add("password", password);
                ResponseEntity<String> resp = restTemplate.postForEntity(
                        adminAddresses + "/login", new HttpEntity<>(body, headers), String.class);
                List<String> setCookies = resp.getHeaders().get("Set-Cookie");
                if (setCookies != null) {
                    StringBuilder sb = new StringBuilder();
                    for (String c : setCookies) {
                        String nameValue = c.split(";")[0];
                        if (sb.length() > 0) sb.append("; ");
                        sb.append(nameValue);
                    }
                    cookie = sb.toString();
                    cookieExpireAt = now + 5 * 60 * 1000L;
                    return cookie;
                }
            } catch (Exception e) {
                log.error("xxl-job 登录失败", e);
            }
            return null;
        }
    }

    /**
     * 调用 xxl-job admin API
     */
    private JsonNode callApi(String path, MultiValueMap<String, String> params) {
        String ck = getCookie();
        if (ck == null) {
            return null;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Cookie", ck);
            ResponseEntity<String> resp = restTemplate.postForEntity(
                    adminAddresses + path, new HttpEntity<>(params, headers), String.class);
            // 刷新 cookie
            List<String> setCookies = resp.getHeaders().get("Set-Cookie");
            if (setCookies != null && !setCookies.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String c : setCookies) {
                    String nameValue = c.split(";")[0];
                    if (sb.length() > 0) sb.append("; ");
                    sb.append(nameValue);
                }
                cookie = sb.toString();
                cookieExpireAt = System.currentTimeMillis() + 5 * 60 * 1000L;
            }
            return mapper.readTree(resp.getBody());
        } catch (Exception e) {
            log.error("xxl-job API 调用失败: {}", path, e);
            return null;
        }
    }

    // ================= 任务列表 =================

    /**
     * 查询任务列表（分页）
     */
    public Map<String, Object> getJobList(int jobGroup, int start, int length, int triggerStatus, String jobDesc, String executorHandler) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("jobGroup", String.valueOf(jobGroup));
        params.add("start", String.valueOf(start));
        params.add("length", String.valueOf(length));
        params.add("triggerStatus", String.valueOf(triggerStatus));
        if (jobDesc != null && !jobDesc.isEmpty()) params.add("jobDesc", jobDesc);
        if (executorHandler != null && !executorHandler.isEmpty()) params.add("executorHandler", executorHandler);

        JsonNode node = callApi("/jobinfo/pageList", params);
        if (node == null || node.path("code").asInt() != 200) {
            return buildErrorPage();
        }
        return convertJobList(node);
    }

    /**
     * 将 xxl-job 的任务列表转换为前端期望的格式
     */
    private Map<String, Object> convertJobList(JsonNode node) {
        JsonNode data = node.path("data");
        int total = node.path("recordsTotal").asInt(0);
        List<Map<String, Object>> list = new ArrayList<>();
        if (data.isArray()) {
            for (JsonNode item : data) {
                Map<String, Object> vo = new LinkedHashMap<>();
                vo.put("id", item.path("id").asLong());
                vo.put("name", item.path("jobDesc").asText(""));
                vo.put("module", deriveModule(item.path("executorParam").asText("")));
                vo.put("type", "cron");
                vo.put("status", item.path("triggerStatus").asInt() == 1 ? "running" : "paused");
                vo.put("cron", item.path("scheduleConf").asText("").trim());
                vo.put("invokeTarget", item.path("executorParam").asText(""));
                vo.put("description", item.path("executorParam").asText(""));
                vo.put("enabled", item.path("triggerStatus").asInt() == 1);
                vo.put("lastExecuteTime", formatTimestamp(item.path("triggerLastTime").asLong(0)));
                vo.put("nextExecuteTime", formatTimestamp(item.path("triggerNextTime").asLong(0)));
                vo.put("createdAt", item.path("addTime").asText(""));
                vo.put("updatedAt", item.path("updateTime").asText(""));
                list.add(vo);
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("totalCount", total);
        result.put("pageSize", data.isArray() ? data.size() : 0);
        result.put("currPage", 1);
        result.put("totalPage", total > 0 ? (int) Math.ceil((double) total / Math.max(data.isArray() ? data.size() : 10, 1)) : 0);
        return result;
    }

    // ================= 任务统计 =================

    /**
     * 获取任务统计数据
     */
    public Map<String, Object> getJobStats(int jobGroup) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("jobGroup", String.valueOf(jobGroup));
        params.add("start", "0");
        params.add("length", "1");
        params.add("triggerStatus", "-1");

        JsonNode allNode = callApi("/jobinfo/pageList", params);
        int total = 0;
        int running = 0;
        if (allNode != null && allNode.path("code").asInt() == 200) {
            total = allNode.path("recordsTotal").asInt(0);
            JsonNode data = allNode.path("data");
            if (data.isArray()) {
                for (JsonNode item : data) {
                    if (item.path("triggerStatus").asInt() == 1) running++;
                }
            }
        }
        // 获取运行中的任务数
        params.clear();
        params.add("jobGroup", String.valueOf(jobGroup));
        params.add("start", "0");
        params.add("length", "1");
        params.add("triggerStatus", "1");
        JsonNode runNode = callApi("/jobinfo/pageList", params);
        if (runNode != null && runNode.path("code").asInt() == 200) {
            running = runNode.path("recordsTotal").asInt(0);
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("running", running);
        stats.put("scheduled", total);
        stats.put("todayCompleted", 0);
        stats.put("todayFailed", 0);
        return stats;
    }

    // ================= 任务模块分组 =================

    /**
     * 获取任务模块分组
     */
    public List<Map<String, Object>> getJobModules(int jobGroup) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("jobGroup", String.valueOf(jobGroup));
        params.add("start", "0");
        params.add("length", "200");
        params.add("triggerStatus", "-1");

        JsonNode node = callApi("/jobinfo/pageList", params);
        Map<String, Integer> moduleCount = new LinkedHashMap<>();
        int total = 0;
        if (node != null && node.path("code").asInt() == 200) {
            total = node.path("recordsTotal").asInt(0);
            JsonNode data = node.path("data");
            if (data.isArray()) {
                for (JsonNode item : data) {
                    String param = item.path("executorParam").asText("");
                    String module = deriveModule(param);
                    moduleCount.merge(module, 1, Integer::sum);
                }
            }
        }

        List<Map<String, Object>> modules = new ArrayList<>();
        Map<String, Object> allTab = new LinkedHashMap<>();
        allTab.put("name", "全部");
        allTab.put("value", "ALL");
        allTab.put("count", total);
        modules.add(allTab);
        for (Map.Entry<String, Integer> entry : moduleCount.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getKey());
            item.put("count", entry.getValue());
            modules.add(item);
        }
        return modules;
    }

    // ================= 任务操作 =================

    /**
     * 新增任务
     */
    public boolean addJob(int jobGroup, String jobDesc, String cron, String executorParam) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("jobGroup", String.valueOf(jobGroup));
        params.add("jobDesc", jobDesc);
        params.add("author", "admin");
        params.add("scheduleType", "CRON");
        params.add("scheduleConf", cron);
        params.add("misfireStrategy", "DO_NOTHING");
        params.add("glueType", "BEAN");
        params.add("executorHandler", "quartzJobHandler");
        params.add("executorParam", executorParam);
        params.add("executorRouteStrategy", "FIRST");
        params.add("executorBlockStrategy", "SERIAL_EXECUTION");
        params.add("executorTimeout", "0");
        params.add("executorFailRetryCount", "0");
        params.add("triggerStatus", "0");

        JsonNode node = callApi("/jobinfo/add", params);
        return node != null && node.path("code").asInt() == 200;
    }

    /**
     * 更新任务
     */
    public boolean updateJob(long id, String jobDesc, String cron, String executorParam) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", String.valueOf(id));
        params.add("jobGroup", "12");
        params.add("jobDesc", jobDesc);
        params.add("author", "admin");
        params.add("scheduleType", "CRON");
        params.add("scheduleConf", cron);
        params.add("misfireStrategy", "DO_NOTHING");
        params.add("glueType", "BEAN");
        params.add("executorHandler", "quartzJobHandler");
        params.add("executorParam", executorParam);
        params.add("executorRouteStrategy", "FIRST");
        params.add("executorBlockStrategy", "SERIAL_EXECUTION");
        params.add("executorTimeout", "0");
        params.add("executorFailRetryCount", "0");

        JsonNode node = callApi("/jobinfo/update", params);
        return node != null && node.path("code").asInt() == 200;
    }

    /**
     * 删除任务
     */
    public boolean removeJob(long id) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", String.valueOf(id));
        JsonNode node = callApi("/jobinfo/remove", params);
        return node != null && node.path("code").asInt() == 200;
    }

    /**
     * 启动任务
     */
    public boolean startJob(long id) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", String.valueOf(id));
        JsonNode node = callApi("/jobinfo/start", params);
        return node != null && node.path("code").asInt() == 200;
    }

    /**
     * 停止任务
     */
    public boolean stopJob(long id) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", String.valueOf(id));
        JsonNode node = callApi("/jobinfo/stop", params);
        return node != null && node.path("code").asInt() == 200;
    }

    /**
     * 手动触发任务
     */
    public boolean triggerJob(long id) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", String.valueOf(id));
        JsonNode node = callApi("/jobinfo/trigger", params);
        return node != null && node.path("code").asInt() == 200;
    }

    // ================= 调度日志 =================

    /**
     * 查询调度日志
     */
    public Map<String, Object> getJobLogList(int jobGroup, Long jobId, int start, int length, int logStatus) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("jobGroup", String.valueOf(jobGroup));
        if (jobId != null && jobId > 0) params.add("jobId", String.valueOf(jobId));
        params.add("start", String.valueOf(start));
        params.add("length", String.valueOf(length));
        params.add("logStatus", String.valueOf(logStatus));

        JsonNode node = callApi("/joblog/pageList", params);
        if (node == null || node.path("code").asInt() != 200) {
            return buildErrorPage();
        }

        JsonNode data = node.path("data");
        int total = node.path("recordsTotal").asInt(0);
        List<Map<String, Object>> list = new ArrayList<>();
        if (data.isArray()) {
            for (JsonNode item : data) {
                Map<String, Object> vo = new LinkedHashMap<>();
                vo.put("id", item.path("id").asLong());
                vo.put("jobId", item.path("jobId").asLong());
                vo.put("executorAddress", item.path("executorAddress").asText(""));
                vo.put("executorHandler", item.path("executorHandler").asText(""));
                vo.put("executorParam", item.path("executorParam").asText(""));
                vo.put("executorShardingParam", item.path("executorShardingParam").asText(""));
                vo.put("triggerTime", formatTimestamp(item.path("triggerTime").asLong(0)));
                vo.put("triggerCode", item.path("triggerCode").asInt(0));
                vo.put("triggerMsg", item.path("triggerMsg").asText(""));
                vo.put("handleTime", formatTimestamp(item.path("handleTime").asLong(0)));
                vo.put("handleCode", item.path("handleCode").asInt(0));
                vo.put("handleMsg", item.path("handleMsg").asText(""));
                String status = "pending";
                int handleCode = item.path("handleCode").asInt(0);
                if (handleCode == 200) status = "success";
                else if (handleCode > 0) status = "failed";
                vo.put("status", status);
                list.add(vo);
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("totalCount", total);
        result.put("pageSize", data.isArray() ? data.size() : 0);
        result.put("currPage", 1);
        result.put("totalPage", total > 0 ? (int) Math.ceil((double) total / Math.max(data.isArray() ? data.size() : 10, 1)) : 0);
        return result;
    }

    /** ================= 工具方法 ================= */

    private String deriveModule(String executorParam) {
        if (executorParam == null || executorParam.isEmpty()) return "系统模块";
        String lower = executorParam.toLowerCase();
        if (lower.contains("order") || lower.contains("settle") || lower.contains("commission")) return "订单模块";
        if (lower.contains("pay") || lower.contains("ali") || lower.contains("wx") || lower.contains("zfb")) return "支付模块";
        if (lower.contains("goods") || lower.contains("es") || lower.contains("seckill") || lower.contains("combination") || lower.contains("shelf")) return "商品模块";
        if (lower.contains("user") || lower.contains("member") || lower.contains("plus") || lower.contains("promotion") || lower.contains("salary")) return "用户模块";
        if (lower.contains("im")) return "消息模块";
        if (lower.contains("geo")) return "系统模块";
        if (lower.contains("dcenter")) return "系统模块";
        if (lower.contains("spinwin") || lower.contains("virtual")) return "商品模块";
        return "系统模块";
    }

    private String formatTimestamp(long ts) {
        if (ts <= 0) return "-";
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(new Date(ts));
        } catch (Exception e) {
            return "-";
        }
    }

    private Map<String, Object> buildErrorPage() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", Collections.emptyList());
        result.put("totalCount", 0);
        result.put("pageSize", 0);
        result.put("currPage", 1);
        result.put("totalPage", 0);
        return result;
    }
}
