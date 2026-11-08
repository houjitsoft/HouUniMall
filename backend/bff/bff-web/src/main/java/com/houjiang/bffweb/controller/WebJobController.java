package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.JobServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 任务调度Web控制器
/**
 * 
 * @description 提供任务调度相关功能的Web接口
 * @author Amos
 * @date 2024-12-20
 */
@RestController
@RequestMapping("/web/job")
public class WebJobController {

    @Autowired
    private JobServiceFeign jobServiceFeign;

    /** ================= 任务管理相关接口 ================= */
    
    @GetMapping("/list")
    public ResponseResult getJobList(@RequestParam Map<String, Object> param) {
        return jobServiceFeign.getJobList(param);
    }
    
    @GetMapping("/detail")
    public ResponseResult getJobDetail(@RequestParam Map<String, Object> param) {
        return jobServiceFeign.getJobDetail(param);
    }
    
    @PostMapping("/create")
    public ResponseResult createJob(@RequestBody Map<String, Object> param) {
        return jobServiceFeign.createJob(param);
    }
    
    @PostMapping("/update")
    public ResponseResult updateJob(@RequestBody Map<String, Object> param) {
        return jobServiceFeign.updateJob(param);
    }
    
    @PostMapping("/delete")
    public ResponseResult deleteJob(@RequestBody Map<String, Object> param) {
        return jobServiceFeign.deleteJob(param);
    }
    
    @PostMapping("/enable")
    public ResponseResult enableJob(@RequestBody Map<String, Object> param) {
        return jobServiceFeign.enableJob(param);
    }
    
    @PostMapping("/disable")
    public ResponseResult disableJob(@RequestBody Map<String, Object> param) {
        return jobServiceFeign.disableJob(param);
    }
    
    @PostMapping("/trigger")
    public ResponseResult triggerJob(@RequestBody Map<String, Object> param) {
        return jobServiceFeign.triggerJob(param);
    }
    
    @PostMapping("/pause")
    public ResponseResult pauseJob(@RequestBody Map<String, Object> param) {
        return jobServiceFeign.pauseJob(param);
    }
    
    @PostMapping("/resume")
    public ResponseResult resumeJob(@RequestBody Map<String, Object> param) {
        return jobServiceFeign.resumeJob(param);
    }

    /** ================= 任务执行日志相关接口 ================= */
    
    @GetMapping("/log/list")
    public ResponseResult getJobLogList(@RequestParam Map<String, Object> param) {
        return jobServiceFeign.getJobLogList(param);
    }
    
    @GetMapping("/log/detail")
    public ResponseResult getJobLogDetail(@RequestParam Map<String, Object> param) {
        return jobServiceFeign.getJobLogDetail(param);
    }
    
    @PostMapping("/log/clear")
    public ResponseResult clearJobLog(@RequestBody Map<String, Object> param) {
        return jobServiceFeign.clearJobLog(param);
    }

    /** ================= 任务统计相关接口 ================= */
    
    @GetMapping("/statistics/overview")
    public ResponseResult getJobStatistics(@RequestParam Map<String, Object> param) {
        return jobServiceFeign.getJobStatistics(param);
    }
    
    @GetMapping("/statistics/execution")
    public ResponseResult getExecutionStatistics(@RequestParam Map<String, Object> param) {
        return jobServiceFeign.getExecutionStatistics(param);
    }
    
    @GetMapping("/statistics/success")
    public ResponseResult getSuccessStatistics(@RequestParam Map<String, Object> param) {
        return jobServiceFeign.getSuccessStatistics(param);
    }
    
    @GetMapping("/statistics/fail")
    public ResponseResult getFailStatistics(@RequestParam Map<String, Object> param) {
        return jobServiceFeign.getFailStatistics(param);
    }

    /** ================= 任务监控相关接口 ================= */
    
    @GetMapping("/monitor/running")
    public ResponseResult getRunningJobs(@RequestParam Map<String, Object> param) {
        return jobServiceFeign.getRunningJobs(param);
    }
    
    @GetMapping("/monitor/queue")
    public ResponseResult getQueueJobs(@RequestParam Map<String, Object> param) {
        return jobServiceFeign.getQueueJobs(param);
    }
    
    @GetMapping("/monitor/executor")
    public ResponseResult getExecutorList(@RequestParam Map<String, Object> param) {
        return jobServiceFeign.getExecutorList(param);
    }
    
    @GetMapping("/monitor/health")
    public ResponseResult getExecutorHealth(@RequestParam Map<String, Object> param) {
        return jobServiceFeign.getExecutorHealth(param);
    }
    
    @PostMapping("/monitor/registry")
    public ResponseResult registryExecutor(@RequestBody Map<String, Object> param) {
        return jobServiceFeign.registryExecutor(param);
    }
    
    @PostMapping("/monitor/remove")
    public ResponseResult removeExecutor(@RequestBody Map<String, Object> param) {
        return jobServiceFeign.removeExecutor(param);
    }
}