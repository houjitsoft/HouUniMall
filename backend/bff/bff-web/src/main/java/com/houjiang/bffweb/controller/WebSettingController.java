package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.BaseServiceFeign;
import com.houjiang.bffweb.feign.JobServiceFeign;
import com.houjiang.bffweb.feign.ThirdpartyConfigServiceFeign;
import com.houjiang.bffweb.dto.ThirdPartyConfigDTO;
import com.houjiang.bffweb.service.XxlJobAdminClient;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设置管理Web端BFF适配器
/**
 * 只做请求转发，不包含业务逻辑
 *
 * @author Amos
 * @since 2025-01-07
 */
@RestController
@RequestMapping("/web/setting")
public class WebSettingController {

    @Autowired
    private ThirdpartyConfigServiceFeign thirdpartyConfigServiceFeign;

    @Autowired
    private BaseServiceFeign baseServiceFeign;

    @Autowired
    private JobServiceFeign jobServiceFeign;

    @Autowired
    private XxlJobAdminClient xxlJobAdminClient;

    /** xxl-job houcloud 执行器 ID */
    @Value("${xxl.job.executor.job-group:12}")
    private int jobGroup;

    // ================= 第三方集成配置接口 =================

    /**
     * 批量获取第三方集成配置（设置页一次加载）
     */
    @GetMapping("/third-party/batch")
    public ResponseResult<Map<String, ThirdPartyConfigDTO>> getThirdPartyConfigsBatch() {
        return thirdpartyConfigServiceFeign.getAllConfigs();
    }

    /**
     * 获取第三方集成配置
     */
    @GetMapping("/third-party/{platform}")
    public ResponseResult<ThirdPartyConfigDTO> getThirdPartyConfig(@PathVariable("platform") String platform) {
        return thirdpartyConfigServiceFeign.getConfig(platform);
    }

    /**
     * 保存第三方集成配置
     */
    @PostMapping("/third-party/{platform}")
    public ResponseResult<String> saveThirdPartyConfig(@PathVariable("platform") String platform, 
                                                       @RequestBody ThirdPartyConfigDTO config) {
        return thirdpartyConfigServiceFeign.saveConfig(platform, config);
    }

    /**
     * 测试第三方集成连接
     */
    @PostMapping("/third-party/{platform}/test")
    public ResponseResult<String> testThirdPartyConnection(@PathVariable("platform") String platform, 
                                                           @RequestBody ThirdPartyConfigDTO config) {
        return thirdpartyConfigServiceFeign.testConnection(platform, config);
    }

    /**
     * 删除第三方集成配置
     */
    @DeleteMapping("/third-party/{platform}")
    public ResponseResult<String> deleteThirdPartyConfig(@PathVariable("platform") String platform) {
        return thirdpartyConfigServiceFeign.deleteConfig(platform);
    }

    /** ================= 数据库管理 / SQL 执行 ================= */

    @GetMapping("/database/patches")
    public ResponseResult databaseListPatches() {
        return baseServiceFeign.databaseListPatches();
    }

    @GetMapping("/database/patch/{patchId}/content")
    public ResponseResult databaseReadPatch(@PathVariable("patchId") String patchId) {
        return baseServiceFeign.databaseReadPatch(patchId);
    }

    @GetMapping("/database/list")
    public ResponseResult databaseList() {
        return baseServiceFeign.databaseList();
    }

    @GetMapping("/database/{dbName}/tables")
    public ResponseResult databaseListTables(@PathVariable("dbName") String dbName) {
        return baseServiceFeign.databaseListTables(dbName);
    }

    @GetMapping("/database/{dbName}/tables/{tableName}/columns")
    public ResponseResult databaseListColumns(@PathVariable("dbName") String dbName,
                                              @PathVariable("tableName") String tableName) {
        return baseServiceFeign.databaseListColumns(dbName, tableName);
    }

    @PostMapping("/database/execute")
    public ResponseResult databaseExecute(@RequestBody Map<String, Object> body) {
        return baseServiceFeign.databaseExecute(body);
    }

    @PostMapping("/database/patch/{patchId}/execute")
    public ResponseResult databaseExecutePatch(@PathVariable("patchId") String patchId,
                                               @RequestBody(required = false) Map<String, Object> body) {
        return baseServiceFeign.databaseExecutePatch(patchId, body == null ? Map.of() : body);
    }

    @PostMapping("/database/wecom/import")
    public ResponseResult databaseImportWecom(@RequestBody(required = false) Map<String, Object> body) {
        return baseServiceFeign.databaseImportWecom(body == null ? Map.of() : body);
    }

    /** ================= 队列管理 ================= */

    @GetMapping("/queue/stats")
    public ResponseResult queueStats(@RequestParam(required = false) Map<String, Object> params) {
        return jobServiceFeign.getQueueStats(params == null ? Map.of() : params);
    }

    @GetMapping("/queue/list")
    public ResponseResult queueList(@RequestParam Map<String, Object> params) {
        return jobServiceFeign.getQueueList(params);
    }

    @PostMapping("/queue/cancel/{id}")
    public ResponseResult queueCancel(@PathVariable("id") Long id) {
        return jobServiceFeign.cancelQueue(id);
    }

    @PostMapping("/queue/retry/{id}")
    public ResponseResult queueRetry(@PathVariable("id") Long id) {
        return jobServiceFeign.retryQueue(id);
    }

    @PostMapping("/queue/execute/{id}")
    public ResponseResult queueExecute(@PathVariable("id") Long id) {
        return jobServiceFeign.executeQueue(id);
    }

    @PostMapping("/queue/batchCancel")
    public ResponseResult queueBatchCancel(@RequestBody Map<String, Object> body) {
        return jobServiceFeign.batchCancelQueue(body);
    }

    // ================= 定时任务管理（xxl-job） =================

    /**
     * 任务统计数据
     */
    @GetMapping("/task/stats")
    public ResponseResult taskStats(@RequestParam(required = false) Map<String, Object> params) {
        return ResponseResult.success(xxlJobAdminClient.getJobStats(jobGroup));
    }

    /**
     * 任务列表（分页）
     */
    @GetMapping("/task/list")
    public ResponseResult taskList(@RequestParam Map<String, Object> params) {
        int page = parseIntParam(params.get("page"), 1);
        int limit = parseIntParam(params.get("limit"), 20);
        String keyword = params.get("keyword") != null ? String.valueOf(params.get("keyword")) : null;
        String module = params.get("module") != null ? String.valueOf(params.get("module")) : null;
        // 全部
        int triggerStatus = -1;
        if ("running".equals(params.get("status"))) triggerStatus = 1;
        else if ("paused".equals(params.get("status"))) triggerStatus = 0;

        Map<String, Object> data = xxlJobAdminClient.getJobList(jobGroup, (page - 1) * limit, limit, triggerStatus, keyword, null);

        // 按模块过滤
        if (module != null && !"全部".equals(module) && !"ALL".equalsIgnoreCase(module)) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
            list = list.stream().filter(vo -> module.equals(vo.get("module"))).collect(java.util.stream.Collectors.toList());
            data.put("list", list);
            data.put("totalCount", list.size());
        }
        return ResponseResult.success(data);
    }

    /**
     * 任务模块分组
     */
    @GetMapping("/task/modules")
    public ResponseResult taskModules() {
        return ResponseResult.success(xxlJobAdminClient.getJobModules(jobGroup));
    }

    /**
     * 新增任务
     */
    @PostMapping("/task/create")
    public ResponseResult taskCreate(@RequestBody Map<String, Object> body) {
        String name = String.valueOf(body.get("name"));
        String cron = String.valueOf(body.get("cron"));
        String invokeTarget = String.valueOf(body.get("invokeTarget"));
        if (xxlJobAdminClient.addJob(jobGroup, name, cron, invokeTarget)) {
            return ResponseResult.success();
        }
        return ResponseResult.fail("创建任务失败");
    }

    /**
     * 更新任务
     */
    @PostMapping("/task/update")
    public ResponseResult taskUpdate(@RequestBody Map<String, Object> body) {
        long id = Long.parseLong(String.valueOf(body.get("id")));
        String name = body.get("name") != null ? String.valueOf(body.get("name")) : "";
        String cron = body.get("cron") != null ? String.valueOf(body.get("cron")) : "";
        String invokeTarget = body.get("invokeTarget") != null ? String.valueOf(body.get("invokeTarget")) : "";
        if (xxlJobAdminClient.updateJob(id, name, cron, invokeTarget)) {
            return ResponseResult.success();
        }
        return ResponseResult.fail("更新任务失败");
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/task/delete/{id}")
    public ResponseResult taskDelete(@PathVariable("id") Long id) {
        if (xxlJobAdminClient.removeJob(id)) {
            return ResponseResult.success();
        }
        return ResponseResult.fail("删除任务失败");
    }

    /**
     * 暂停任务
     */
    @PostMapping("/task/pause/{id}")
    public ResponseResult taskPause(@PathVariable("id") Long id) {
        if (xxlJobAdminClient.stopJob(id)) {
            return ResponseResult.success();
        }
        return ResponseResult.fail("暂停任务失败");
    }

    /**
     * 恢复任务
     */
    @PostMapping("/task/resume/{id}")
    public ResponseResult taskResume(@PathVariable("id") Long id) {
        if (xxlJobAdminClient.startJob(id)) {
            return ResponseResult.success();
        }
        return ResponseResult.fail("恢复任务失败");
    }

    /**
     * 手动触发任务
     */
    @PostMapping("/task/trigger/{id}")
    public ResponseResult taskTrigger(@PathVariable("id") Long id) {
        if (xxlJobAdminClient.triggerJob(id)) {
            return ResponseResult.success();
        }
        return ResponseResult.fail("触发任务失败");
    }

    /**
     * 启用/禁用切换
     */
    @PostMapping("/task/toggleEnabled/{id}")
    public ResponseResult taskToggleEnabled(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        Boolean enabled = body.get("enabled") != null && Boolean.parseBoolean(String.valueOf(body.get("enabled")));
        boolean ok = enabled ? xxlJobAdminClient.startJob(id) : xxlJobAdminClient.stopJob(id);
        if (ok) {
            return ResponseResult.success();
        }
        return ResponseResult.fail(enabled ? "启用失败" : "禁用失败");
    }

    /**
     * 调度日志列表
     */
    @GetMapping("/task/log/list")
    public ResponseResult taskLogList(@RequestParam Map<String, Object> params) {
        int page = parseIntParam(params.get("page"), 1);
        int limit = parseIntParam(params.get("limit"), 20);
        Long jobId = params.get("jobId") != null ? Long.parseLong(String.valueOf(params.get("jobId"))) : null;
        int logStatus = -1;
        if ("success".equals(params.get("status"))) logStatus = 1;
        else if ("failed".equals(params.get("status"))) logStatus = 2;
        else if ("running".equals(params.get("status"))) logStatus = 0;

        Map<String, Object> data = xxlJobAdminClient.getJobLogList(jobGroup, jobId, (page - 1) * limit, limit, logStatus);
        return ResponseResult.success(data);
    }

    private int parseIntParam(Object val, int defaultVal) {
        if (val == null) return defaultVal;
        try { return Integer.parseInt(String.valueOf(val)); }
        catch (NumberFormatException e) { return defaultVal; }
    }
}