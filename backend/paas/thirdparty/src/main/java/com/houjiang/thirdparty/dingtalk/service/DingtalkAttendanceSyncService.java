package com.houjiang.thirdparty.dingtalk.service;

import java.util.Map;

/**
 * @author Amos
 */
public interface DingtalkAttendanceSyncService {

    /**
     * TODO 补充方法说明
     * @param orgId TODO
     * @param month TODO
     * @return TODO
     */
    Map<String, Object> syncMonth(String orgId, String month);
}
