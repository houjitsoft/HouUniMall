package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 任务调度服务降级处理类
/**
 * 
 * @description 当任务调度服务不可用时，提供降级响应
 * @author Amos
 * @date 2024-12-20
 */
@Component
public class JobServiceFeignFallback implements JobServiceFeign {

    @Override
    public ResponseResult getJobList(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getJobModules() {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getJobDetail(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createJob(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateJob(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteJob(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult enableJob(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult disableJob(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult triggerJob(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult pauseJob(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult resumeJob(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getJobLogList(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getJobLogDetail(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult clearJobLog(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getJobStatistics(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getJobExecutionStatistics(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getExecutionStatistics(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSuccessStatistics(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getFailStatistics(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getJobSuccessRate(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getJobMonitorStatus(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getJobAlerts(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateAlertConfig(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    /** ================= 任务执行器相关接口 ================= */

    @Override
    public ResponseResult getRunningJobs(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getQueueJobs(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getExecutorList(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getExecutorHealth(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult registryExecutor(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult removeExecutor(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getQueueStats(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getQueueList(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult cancelQueue(Long id) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult retryQueue(Long id) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult executeQueue(Long id) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult batchCancelQueue(Map<String, Object> param) {
        return ResponseResult.fail("任务调度服务暂时不可用，请稍后重试");
    }
}