package com.houjiang.job.job;

import com.houjiang.job.feign.OaServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Map;

/**
 * 每日自动同步第三方考勤（仅 syncMode=auto 且已启用的组织）。
 * 建议 cron：0 0 2 * * ?（每天凌晨 2 点）
 * @author Amos
 */
@Slf4j
public class AttendanceSyncJob implements BaseJob {

    @Autowired
    private OaServiceClient oaServiceClient;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Map<String, Object> resp = oaServiceClient.triggerAttendanceSync(Collections.emptyMap());
            log.info("考勤定时同步任务完成: {}", resp);
        } catch (Exception e) {
            log.warn("考勤定时同步任务失败", e);
        }
    }
}
