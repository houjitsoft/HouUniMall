package com.houjiang.job.config;

import com.houjiang.job.service.JobExecutionLogService;
import org.junit.jupiter.api.Test;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 工资定时任务失败必须落 failed：监听器以 jobException 是否为空判定成功。
 * 该类任务此前在 execute 内吞掉异常，Quartz 收不到异常，执行日志一律 success。
 */
class JobFailureRecordingTest {

    private static final String TARGET = "com.houjiang.job.job.MonthlySalaryCalculationJob";

    @Test
    void exceptionIsRecordedAsFailedWithMessage() {
        JobExecutionLogService logService = mock(JobExecutionLogService.class);
        JobExecutionListener listener = listenerWith(logService);
        JobExecutionContext context = context();

        JobExecutionException ex = new JobExecutionException(
                "月度工资定时任务失败: boom", new RuntimeException("boom"), false);
        listener.jobWasExecuted(context, ex);

        verify(logService).recordFinish(eq(TARGET), eq(false), eq("月度工资定时任务失败: boom"));
    }

    @Test
    void noExceptionIsRecordedAsSuccess() {
        JobExecutionLogService logService = mock(JobExecutionLogService.class);
        JobExecutionListener listener = listenerWith(logService);

        listener.jobWasExecuted(context(), null);

        verify(logService).recordFinish(eq(TARGET), eq(true), eq(""));
    }

    private JobExecutionListener listenerWith(JobExecutionLogService logService) {
        JobExecutionListener listener = new JobExecutionListener();
        ReflectionTestUtils.setField(listener, "jobExecutionLogService", logService);
        return listener;
    }

    private JobExecutionContext context() {
        JobExecutionContext context = mock(JobExecutionContext.class);
        JobDetail detail = mock(JobDetail.class);
        when(context.getJobDetail()).thenReturn(detail);
        when(detail.getKey()).thenReturn(new JobKey(TARGET));
        return context;
    }
}
