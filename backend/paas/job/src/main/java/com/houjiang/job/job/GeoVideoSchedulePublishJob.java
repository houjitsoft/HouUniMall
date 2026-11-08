package com.houjiang.job.job;

import com.houjiang.job.feign.GeoServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * GEO 视频定时发布：扫描 plan_time 到期的待发布视频内容
 * @author Amos
 */
@Slf4j
public class GeoVideoSchedulePublishJob implements BaseJob {

    @Autowired
    private GeoServiceClient geoServiceClient;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Map<String, Object> resp = geoServiceClient.triggerScheduledPublish(Map.of("contentType", "video"));
            log.info("GEO 视频定时发布完成: {}", resp);
        } catch (Exception e) {
            log.warn("GEO 视频定时发布失败", e);
            // 🔴 必须外抛，理由同 GeoArticleSchedulePublishJob：静默消化会把执行记成 success，
            // 断更在管理端不可见。
            throw new JobExecutionException("GEO 视频定时发布失败: " + e.getMessage(), e, false);
        }
    }
}
