package com.houjiang.job.job;

import com.houjiang.job.feign.OaServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Map;

/**
 * 每月5号自动计算各组织员工上月工资（默认计算上月）
 * 与 MonthlySalaryCalculationJob（每月1号）独立运行，互为补充
 * @author Amos
 */
@Slf4j
public class MonthlySalaryCalculationJob5th implements BaseJob {

    @Autowired
    private OaServiceClient oaServiceClient;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Map<String, Object> resp;
        try {
            resp = oaServiceClient.triggerMonthlySalary(Collections.emptyMap());
        } catch (Exception e) {
            // 必须向上抛出，否则 Quartz 一律记 success，hou_job_execution_log 看不出失败
            log.error("每月5号工资定时任务失败", e);
            throw new JobExecutionException("每月5号工资定时任务失败: " + e.getMessage(), e, false);
        }
        log.info("每月5号工资定时任务完成: {}", resp);
        SalaryJobResultSupport.warnIfNothingProcessed("每月5号工资定时任务", resp);
    }
}
