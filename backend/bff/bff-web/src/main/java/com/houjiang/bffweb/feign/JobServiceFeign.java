package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 任务调度服务Feign客户端
/**
 * <p>
 * 生产同机部署建议配置 {@code feign.job.url=http://127.0.0.1:8313}（Nacos job.yaml 端口），
 * 避免 Nacos 无 job 实例时出现 Load balancer does not contain an instance。
 * @author Amos
 */
@FeignClient(name = "jobServiceFeign", url = "${feign.job.url:http://127.0.0.1:8313}", fallback = JobServiceFeignFallback.class)
public interface JobServiceFeign {

    // ================= 任务管理相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/job/list")
    ResponseResult getJobList(@RequestParam Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/job/modules")
    ResponseResult getJobModules();
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/job/detail")
    ResponseResult getJobDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/job/create")
    ResponseResult createJob(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/job/update")
    ResponseResult updateJob(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/job/delete")
    ResponseResult deleteJob(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/job/enable")
    ResponseResult enableJob(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/job/disable")
    ResponseResult disableJob(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/job/trigger")
    ResponseResult triggerJob(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/job/pause")
    ResponseResult pauseJob(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/job/resume")
    ResponseResult resumeJob(@RequestBody Map<String, Object> param);

    // ================= 任务执行日志相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/job/log/list")
    ResponseResult getJobLogList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/job/log/detail")
    ResponseResult getJobLogDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/job/log/clear")
    ResponseResult clearJobLog(@RequestBody Map<String, Object> param);

    // ================= 任务统计相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/job/statistics/overview")
    ResponseResult getJobStatistics(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/job/statistics/execution")
    ResponseResult getJobExecutionStatistics(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/job/statistics/execution")
    ResponseResult getExecutionStatistics(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/job/statistics/success")
    ResponseResult getSuccessStatistics(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/job/statistics/fail")
    ResponseResult getFailStatistics(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/job/statistics/success-rate")
    ResponseResult getJobSuccessRate(@RequestParam Map<String, Object> param);

    // ================= 任务监控相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/job/monitor/status")
    ResponseResult getJobMonitorStatus(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/job/monitor/alerts")
    ResponseResult getJobAlerts(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/job/monitor/alert-config")
    ResponseResult updateAlertConfig(@RequestBody Map<String, Object> param);

    // ================= 任务执行器相关接口=================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/job/executor/running")
    ResponseResult getRunningJobs(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/job/executor/queue")
    ResponseResult getQueueJobs(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/job/executor/list")
    ResponseResult getExecutorList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/job/executor/health")
    ResponseResult getExecutorHealth(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/job/executor/registry")
    ResponseResult registryExecutor(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/job/executor/remove")
    ResponseResult removeExecutor(@RequestBody Map<String, Object> param);

    // ================= 系统队列管理 =================

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/queue/stats")
    ResponseResult getQueueStats(@RequestParam Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/queue/list")
    ResponseResult getQueueList(@RequestParam Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/queue/cancel/{id}")
    ResponseResult cancelQueue(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/queue/retry/{id}")
    ResponseResult retryQueue(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/queue/execute/{id}")
    ResponseResult executeQueue(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/queue/batchCancel")
    ResponseResult batchCancelQueue(@RequestBody Map<String, Object> param);
}