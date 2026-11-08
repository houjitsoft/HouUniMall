package com.houjiang.job.job;

import com.houjiang.job.feign.GeoServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * GEO 平台账号保活心跳：每日探测 Cookie 账号登录态，失效账号标记 expired。
 * cron 建议每日 09:00（0 0 9 * * ?），与设计文档一致，避免高频访问触发平台风控。
 *
 * @author Amos
 */
@Slf4j
public class GeoAccountKeepaliveJob implements BaseJob {

    @Autowired
    private GeoServiceClient geoServiceClient;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Map<String, Object> resp = geoServiceClient.triggerKeepalive();
            log.info("GEO 账号保活心跳完成: {}", resp);
        } catch (Exception e) {
            log.warn("GEO 账号保活心跳失败", e);
            // 🔴 必须外抛：静默消化会把执行记成 success，账号 Cookie 失效、登录态探测中断
            // 在管理端「定时任务」里完全不可见，只能等发布失败才发现。
            throw new JobExecutionException("GEO 账号保活心跳失败: " + e.getMessage(), e, false);
        }
    }
}
