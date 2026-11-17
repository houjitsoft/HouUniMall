package com.houjiang.job.job;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * 结果自检只负责告警，不得因 processed=0 或返回结构异常把任务抛成失败。
 */
class SalaryJobResultSupportTest {

    @Test
    void zeroProcessedDoesNotThrow() {
        assertDoesNotThrow(() -> SalaryJobResultSupport.warnIfNothingProcessed("月度工资定时任务", resp(0)));
    }

    @Test
    void normalProcessedDoesNotThrow() {
        assertDoesNotThrow(() -> SalaryJobResultSupport.warnIfNothingProcessed("月度工资定时任务", resp(9)));
    }

    @Test
    void malformedResponseDoesNotThrow() {
        assertDoesNotThrow(() -> SalaryJobResultSupport.warnIfNothingProcessed("月度工资定时任务", null));
        Map<String, Object> noData = new HashMap<>();
        noData.put("code", 1);
        assertDoesNotThrow(() -> SalaryJobResultSupport.warnIfNothingProcessed("月度工资定时任务", noData));
        Map<String, Object> failed = new HashMap<>();
        failed.put("code", 20010);
        failed.put("message", "考勤同步失败");
        assertDoesNotThrow(() -> SalaryJobResultSupport.warnIfNothingProcessed("月度工资定时任务", failed));
    }

    private Map<String, Object> resp(int processed) {
        Map<String, Object> data = new HashMap<>();
        data.put("month", "2026-10");
        data.put("processed", processed);
        Map<String, Object> root = new HashMap<>();
        root.put("code", 1);
        root.put("data", data);
        return root;
    }
}
