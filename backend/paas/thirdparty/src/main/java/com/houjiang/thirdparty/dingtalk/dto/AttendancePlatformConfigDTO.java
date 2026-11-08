package com.houjiang.thirdparty.dingtalk.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 考勤数据来源配置（存 hou_thirdparty_oa_config.platform=attendance-platform）
 * @author Amos
 */
@Data
public class AttendancePlatformConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 考勤来源：platform / dingtalk / wechat-work / feishu */
    private String source = "platform";

    /** 同步方式：auto / manual */
    private String syncMode = "auto";

    /** 第三方同步是否启用 */
    private Boolean enabled = false;

    /** 钉钉是否已配置且可用（只读，查询时填充） */
    private Boolean dingtalkConfigured;

    /** 企业微信是否已配置且可用（只读，查询时填充） */
    private Boolean wechatWorkConfigured;

    /** 飞书是否已配置且可用（只读，查询时填充） */
    private Boolean feishuConfigured;
}
