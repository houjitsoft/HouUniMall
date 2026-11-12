package com.houjiang.userms.dto;

import lombok.Data;

/**
 * 用户行为与留存分析 · 查询条件
 *
 * <p>时间范围为闭区间（yyyy-MM-dd），起止由 Service 做合法性校验与默认值兜底。
 * 所有指标按所选范围从真实库表实时聚合，缺失维度不补数。</p>
 *
 * @author Amos
 */
@Data
public class BehaviorAnalyticsQueryDTO {

    /** 起始日期 yyyy-MM-dd，闭区间 */
    private String startDate;

    /** 结束日期 yyyy-MM-dd，闭区间 */
    private String endDate;
}
