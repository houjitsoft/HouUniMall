package com.houjiang.job.feign;

import com.houjiang.job.feign.support.InternalJobFeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * OA 插件定时任务 Feign（定时任务触发）。
 *
 * <p>直接调用归属服务的插件 API；内部任务令牌由
 * {@link InternalJobFeignConfiguration} 注入，归属服务负责校验。
 *
 * @author Amos
 */
@FeignClient(name = "oa", url = "${feign.oa.url:}", contextId = "oaServiceClient",
        configuration = InternalJobFeignConfiguration.class)
public interface OaServiceClient {

    /**
     * 触发月度工资计算（含工资前考勤同步）。
     *
     * @param body 请求体，可含 {@code month}（yyyy-MM），缺省取上月
     * @return 处理结果，含 {@code processed} 字段
     */
    @PostMapping("/plugin/oa/job/feign/monthly-salary")
    Map<String, Object> triggerMonthlySalary(@RequestBody Map<String, Object> body);

    /**
     * 触发考勤定时同步（仅 syncMode=auto 且已启用的组织）。
     *
     * @param body 请求体，可含 {@code month}（yyyy-MM），缺省取当月
     * @return 同步统计结果 {synced, skipped, failed}
     */
    @PostMapping("/plugin/oa/job/feign/attendance-sync")
    Map<String, Object> triggerAttendanceSync(@RequestBody Map<String, Object> body);
}
