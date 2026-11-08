package com.houjiang.thirdparty.feishu.service;

import java.util.Map;

/**
 * @author Amos
 */
public interface FeishuAttendanceSyncService {

    /**
     * TODO 补充方法说明
     * @param orgId TODO
     * @param month TODO
     * @return TODO
     */
    Map<String, Object> syncMonth(String orgId, String month);
}
