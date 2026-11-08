package com.houjiang.job.feign;

import com.houjiang.job.feign.support.InternalJobFeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * GEO 插件定时任务 Feign（定时发布触发）。
 *
 * <p>直接调用归属服务的插件 API；内部任务令牌由
 * {@link InternalJobFeignConfiguration} 注入，归属服务负责校验。
 *
 * @author Amos
 */
@FeignClient(name = "geo", url = "${feign.geo.url:}", contextId = "geoServiceClient",
        configuration = InternalJobFeignConfiguration.class)
public interface GeoServiceClient {

    /**
     * 扫描 plan_time 到期的待发布图文/文章并发布。
     *
     * @param body 请求体，{@code contentType} 指定内容类型
     * @return 处理结果，含 {@code processed} 字段
     */
    @PostMapping("/plugin/geo/job/feign/scheduled-publish")
    Map<String, Object> triggerScheduledPublish(@RequestBody Map<String, Object> body);

    /**
     * 触发 GEO 平台账号保活心跳（每日一轮，遍历 Cookie 账号探测登录态并标记失效）。
     *
     * @return 心跳统计结果 {checked, renewed, invalid, failed, details}
     */
    @PostMapping("/plugin/geo/job/feign/keepalive")
    Map<String, Object> triggerKeepalive();
}
