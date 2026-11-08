package com.houjiang.job.job;

import com.houjiang.job.feign.OaServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Map;

/**
 * 每月自动计算各组织员工工资（默认计算上月）
 * @author Amos
 */
@Slf4j
public class MonthlySalaryCalculationJob implements BaseJob {

    @Autowired
    private OaServiceClient oaServiceClient;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Map<String, Object> resp = oaServiceClient.triggerMonthlySalary(Collections.emptyMap());
            log.info("月度工资定时任务完成: {}", resp);
        } catch (Exception e) {
            log.warn("月度工资定时任务失败", e);
        }
    }
}
