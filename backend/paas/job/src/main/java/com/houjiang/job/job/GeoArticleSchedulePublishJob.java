package com.houjiang.job.job;

import com.houjiang.job.feign.GeoServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * GEO 文章定时发布：扫描 plan_time 到期的待发布图文/文章
 * @author Amos
 */
@Slf4j
public class GeoArticleSchedulePublishJob implements BaseJob {

    @Autowired
    private GeoServiceClient geoServiceClient;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Map<String, Object> resp = geoServiceClient.triggerScheduledPublish(Map.of("contentType", "article"));
            log.info("GEO 文章定时发布完成: {}", resp);
        } catch (Exception e) {
            log.warn("GEO 文章定时发布失败", e);
            // 🔴 必须外抛：在 execute() 内静默消化异常，Quartz 会把本次执行记成 success，
            // 管理端「定时任务 → 执行日志」里看不到任何异常。
            // 2026-09-22 教训：GEO 文章定时发布连续失败多日（先是插件路由 404、后是插件内
            // 序列化异常 500），日志全是 success，无人告警，直接表现为「好多天没自动发文章」。
            throw new JobExecutionException("GEO 文章定时发布失败: " + e.getMessage(), e, false);
        }
    }
}
