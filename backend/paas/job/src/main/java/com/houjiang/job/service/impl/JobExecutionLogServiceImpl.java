package com.houjiang.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.job.entity.JobExecutionLogEntity;
import com.houjiang.job.mapper.JobExecutionLogMapper;
import com.houjiang.job.service.JobExecutionLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Amos
 */
@Service
public class JobExecutionLogServiceImpl extends ServiceImpl<JobExecutionLogMapper, JobExecutionLogEntity>
        implements JobExecutionLogService {

    /** 正在执行的日志ID，key=invokeTarget */
    private final ConcurrentHashMap<String, Long> runningLogMap = new ConcurrentHashMap<>();

    @Override
    public ResponseResult list(Map<String, Object> params) {
        int page = parseInt(params.get("page"), 1);
        int limit = parseInt(params.get("limit"), 20);
        String status = params.get("status") != null ? String.valueOf(params.get("status")) : null;
        Long jobId = params.get("jobId") != null ? Long.valueOf(String.valueOf(params.get("jobId"))) : null;
        String keyword = params.get("keyword") != null ? String.valueOf(params.get("keyword")) : null;

        LambdaQueryWrapper<JobExecutionLogEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(JobExecutionLogEntity::getStatus, status);
        }
        if (jobId != null) {
            wrapper.eq(JobExecutionLogEntity::getJobId, jobId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(JobExecutionLogEntity::getJobName, keyword)
                    .or().like(JobExecutionLogEntity::getInvokeTarget, keyword));
        }
        wrapper.orderByDesc(JobExecutionLogEntity::getStartTime);

        IPage<JobExecutionLogEntity> pageResult = this.page(new Page<>(page, limit), wrapper);
        return ResponseResult.success(PageUtils.getPage(pageResult));
    }

    @Override
    public ResponseResult detail(Long id) {
        JobExecutionLogEntity entity = this.getById(id);
        if (entity == null) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "日志不存在");
        }
        return ResponseResult.success(entity);
    }

    @Override
    public ResponseResult clear(Map<String, Object> params) {
        Integer days = params.get("days") != null ? Integer.valueOf(String.valueOf(params.get("days"))) : 30;
        Date cutoff = new Date(System.currentTimeMillis() - days * 24L * 60 * 60 * 1000);
        this.remove(new LambdaQueryWrapper<JobExecutionLogEntity>().lt(JobExecutionLogEntity::getStartTime, cutoff));
        return ResponseResult.success();
    }

    @Override
    public void recordStart(String jobName, String invokeTarget, Long jobId) {
        JobExecutionLogEntity log = new JobExecutionLogEntity();
        log.setJobId(jobId);
        log.setJobName(jobName != null ? jobName : invokeTarget);
        log.setInvokeTarget(invokeTarget);
        log.setStatus("running");
        log.setStartTime(new Date());
        this.save(log);
        runningLogMap.put(invokeTarget, log.getId());
    }

    @Override
    public void recordFinish(String invokeTarget, boolean success, String errorMsg) {
        Long logId = runningLogMap.remove(invokeTarget);
        JobExecutionLogEntity log;
        if (logId != null) {
            log = this.getById(logId);
        } else {
            log = this.getOne(new LambdaQueryWrapper<JobExecutionLogEntity>()
                    .eq(JobExecutionLogEntity::getInvokeTarget, invokeTarget)
                    .eq(JobExecutionLogEntity::getStatus, "running")
                    .orderByDesc(JobExecutionLogEntity::getStartTime)
                    .last("LIMIT 1"));
        }
        if (log == null) {
            return;
        }
        Date end = new Date();
        log.setEndTime(end);
        log.setStatus(success ? "success" : "failed");
        log.setErrorMsg(errorMsg != null ? errorMsg : "");
        if (log.getStartTime() != null) {
            log.setDurationMs(end.getTime() - log.getStartTime().getTime());
        }
        this.updateById(log);
    }

    private int parseInt(Object val, int defaultVal) {
        if (val == null) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(String.valueOf(val));
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
