package com.houjiang.thirdparty.wecom.service;

import java.util.List;
import java.util.Map;

/**
 * 企业微信考勤同步
 * @author Amos
 */
public interface WecomAttendanceSyncService {

    /**
     * 按月份同步打卡数据，返回按日聚合后的记录（供 OA 落库）。
     * 当前月仅同步到昨天，历史月同步到月末，未来月不调用企微打卡接口。
     *
     * @param orgId  组织ID
     * @param params month(yyyy-MM)、可选 userIds（OA 已同步员工 userid 列表）
     * @return 同步结果，包含 syncMonth、syncStartDate、syncEndDate 和聚合记录
     */
    Map<String, Object> syncMonth(String orgId, Map<String, Object> params);
}
