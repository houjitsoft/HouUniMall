package com.houjiang.job.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.job.entity.JobPlatformEntity;
import com.houjiang.job.mapper.JobPlatformMapper;
import com.houjiang.job.service.JobExecutionLogService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Quartz 任务执行监听器，记录执行日志（成功/失败）
 * @author Amos
 */
@Slf4j
@Component
public class JobExecutionListener implements JobListener {

    @Autowired
    @Qualifier("Scheduler")
    private Scheduler scheduler;

    @Autowired
    private JobExecutionLogService jobExecutionLogService;

    @Autowired
    private JobPlatformMapper jobPlatformMapper;

    @PostConstruct
    public void register() {
        try {
            scheduler.getListenerManager().addJobListener(this);
        } catch (SchedulerException e) {
            log.warn("注册任务执行监听器失败: {}", e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "JobExecutionListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        String invokeTarget = context.getJobDetail().getKey().getName();
        JobPlatformEntity job = jobPlatformMapper.selectOne(new LambdaQueryWrapper<JobPlatformEntity>()
                .eq(JobPlatformEntity::getInvokeTarget, invokeTarget)
                .last("LIMIT 1"));
        String jobName = job != null ? job.getJobName() : invokeTarget;
        Long jobId = job != null ? job.getJobId() : null;
        jobExecutionLogService.recordStart(jobName, invokeTarget, jobId);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        String invokeTarget = context.getJobDetail().getKey().getName();
        boolean success = jobException == null;
        String errorMsg = success ? "" : jobException.getMessage();
        jobExecutionLogService.recordFinish(invokeTarget, success, errorMsg);
    }
}
