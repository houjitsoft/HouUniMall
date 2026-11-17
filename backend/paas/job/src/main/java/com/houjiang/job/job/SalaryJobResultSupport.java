package com.houjiang.job.job;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 工资定时任务结果自检。
 *
 * <p>OA 侧 {@code monthly-salary} 接口在「没有任何工资条被计算」时同样返回成功（例如目标月份已全部定稿、
 * 或员工范围解析为空），此时 hou_job_execution_log 会记 success 但业务上等于没跑。这里显式打印告警，
 * 避免再次出现「日志成功、工资单空白」的排查黑洞。
 *
 * @author Amos
 */
@Slf4j
final class SalaryJobResultSupport {

    private SalaryJobResultSupport() {
    }

    static void warnIfNothingProcessed(String jobName, Map<String, Object> resp) {
        if (resp == null) {
            log.warn("{} 返回为空，未确认工资核算结果", jobName);
            return;
        }
        Object code = resp.get("code");
        if (code != null && !"1".equals(String.valueOf(code))) {
            log.warn("{} 返回非成功码 code={} message={}", jobName, code, resp.get("message"));
            return;
        }
        Object data = resp.get("data");
        if (!(data instanceof Map<?, ?> map)) {
            log.warn("{} 返回缺少 data 节点: {}", jobName, resp);
            return;
        }
        Object processed = map.get("processed");
        int count = processed instanceof Number number ? number.intValue() : -1;
        if (count == 0) {
            log.warn("{} 执行成功但 processed=0，本月没有任何工资条被写入，请核对月份与员工范围。month={}",
                    jobName, map.get("month"));
        } else if (count > 0) {
            log.info("{} 实际写入工资条 {} 条, month={}", jobName, count, map.get("month"));
        } else {
            log.warn("{} 返回未带 processed 字段，无法确认写入条数: {}", jobName, map);
        }
    }
}
