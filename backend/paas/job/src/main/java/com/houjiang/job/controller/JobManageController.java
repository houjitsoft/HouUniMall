package com.houjiang.job.controller;

import com.houjiang.commons.annnotation.SysLog;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.job.service.JobExecutionLogService;
import com.houjiang.job.service.JobManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 定时任务管理（系统设置）
 * @author Amos
 */
@RestController
@RequestMapping("/job")
public class JobManageController {

    @Autowired
    private JobManageService jobManageService;

    @Autowired
    private JobExecutionLogService jobExecutionLogService;

    /** ================= 任务管理 ================= */

    @GetMapping("/list")
    public ResponseResult list(@RequestParam Map<String, Object> params) {
        return jobManageService.list(params);
    }

    @GetMapping("/modules")
    public ResponseResult modules() {
        return jobManageService.modules();
    }

    @GetMapping("/detail")
    public ResponseResult detail(@RequestParam Map<String, Object> params) {
        return jobManageService.detail(params);
    }

    @PostMapping("/create")
    @SysLog(value = "创建定时任务", module = "job")
    public ResponseResult create(@RequestBody Map<String, Object> params) {
        if (params.get("invokeTarget") == null && params.get("name") != null) {
            params.put("invokeTarget", "com.houjiang.job.job." + params.get("name"));
        }
        return jobManageService.create(params);
    }

    @PostMapping("/update")
    @SysLog(value = "修改定时任务", module = "job")
    public ResponseResult update(@RequestBody Map<String, Object> params) {
        return jobManageService.update(params);
    }

    @PostMapping("/delete")
    @SysLog(value = "删除定时任务", module = "job")
    public ResponseResult delete(@RequestBody Map<String, Object> params) {
        Long jobId = Long.valueOf(String.valueOf(params.get("jobId") != null ? params.get("jobId") : params.get("id")));
        return jobManageService.delete(jobId);
    }

    @PostMapping("/pause")
    @SysLog(value = "暂停定时任务", module = "job")
    public ResponseResult pause(@RequestBody Map<String, Object> params) {
        Long jobId = Long.valueOf(String.valueOf(params.get("jobId") != null ? params.get("jobId") : params.get("id")));
        return jobManageService.pause(jobId);
    }

    @PostMapping("/resume")
    @SysLog(value = "恢复定时任务", module = "job")
    public ResponseResult resume(@RequestBody Map<String, Object> params) {
        Long jobId = Long.valueOf(String.valueOf(params.get("jobId") != null ? params.get("jobId") : params.get("id")));
        return jobManageService.resume(jobId);
    }

    @PostMapping("/trigger")
    @SysLog(value = "执行定时任务", module = "job")
    public ResponseResult trigger(@RequestBody Map<String, Object> params) {
        Long jobId = Long.valueOf(String.valueOf(params.get("jobId") != null ? params.get("jobId") : params.get("id")));
        return jobManageService.trigger(jobId);
    }

    @PostMapping("/enable")
    @SysLog(value = "启用定时任务", module = "job")
    public ResponseResult enable(@RequestBody Map<String, Object> params) {
        Long jobId = Long.valueOf(String.valueOf(params.get("jobId") != null ? params.get("jobId") : params.get("id")));
        return jobManageService.resume(jobId);
    }

    @PostMapping("/disable")
    @SysLog(value = "禁用定时任务", module = "job")
    public ResponseResult disable(@RequestBody Map<String, Object> params) {
        Long jobId = Long.valueOf(String.valueOf(params.get("jobId") != null ? params.get("jobId") : params.get("id")));
        return jobManageService.pause(jobId);
    }

    /** ================= 统计 ================= */

    @GetMapping("/statistics/overview")
    public ResponseResult statisticsOverview() {
        return jobManageService.stats();
    }

    @GetMapping("/statistics/execution")
    public ResponseResult statisticsExecution() {
        return jobManageService.stats();
    }

    @GetMapping("/statistics/success")
    public ResponseResult statisticsSuccess() {
        return jobManageService.stats();
    }

    @GetMapping("/statistics/fail")
    public ResponseResult statisticsFail() {
        return jobManageService.stats();
    }

    @GetMapping("/statistics/success-rate")
    public ResponseResult statisticsSuccessRate() {
        return jobManageService.stats();
    }

    /** ================= 执行日志 ================= */

    @GetMapping("/log/list")
    public ResponseResult logList(@RequestParam Map<String, Object> params) {
        return jobExecutionLogService.list(params);
    }

    @GetMapping("/log/detail")
    public ResponseResult logDetail(@RequestParam Map<String, Object> params) {
        Long id = Long.valueOf(String.valueOf(params.get("id")));
        return jobExecutionLogService.detail(id);
    }

    @PostMapping("/log/clear")
    @SysLog(value = "清空任务执行日志", module = "job")
    public ResponseResult logClear(@RequestBody Map<String, Object> params) {
        return jobExecutionLogService.clear(params);
    }

    /** ================= 监控 ================= */

    @GetMapping("/monitor/status")
    public ResponseResult monitorStatus() {
        return jobManageService.stats();
    }

    @GetMapping("/executor/running")
    public ResponseResult executorRunning(@RequestParam Map<String, Object> params) {
        params.put("status", "running");
        return jobExecutionLogService.list(params);
    }

    @GetMapping("/executor/queue")
    public ResponseResult executorQueue(@RequestParam Map<String, Object> params) {
        return jobManageService.list(params);
    }
}
