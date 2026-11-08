package com.houjiang.thirdparty.dingtalk.service;

import com.houjiang.commons.dto.ThirdPartyConfigDTO;
import com.houjiang.thirdparty.dingtalk.dto.AttendancePlatformConfigDTO;

import java.util.Map;

/**
 * 钉钉集成服务（配置、连接测试、考勤来源）
 * @author Amos
 */
public interface DingtalkIntegrationService {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    ThirdPartyConfigDTO getConfig();

    /**
     * TODO 补充方法说明
     * @param config TODO
     * @return TODO
     */
    boolean saveConfig(ThirdPartyConfigDTO config);

    /**
     * TODO 补充方法说明
     * @param config TODO
     * @return TODO
     */
    boolean testConnection(ThirdPartyConfigDTO config);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Map<String, Object> healthCheck();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    AttendancePlatformConfigDTO getAttendancePlatformConfig();

    /**
     * TODO 补充方法说明
     * @param orgId TODO
     * @return TODO
     */
    AttendancePlatformConfigDTO getAttendancePlatformConfig(String orgId);

    /**
     * TODO 补充方法说明
     * @param config TODO
     * @return TODO
     */
    boolean saveAttendancePlatformConfig(AttendancePlatformConfigDTO config);
}
