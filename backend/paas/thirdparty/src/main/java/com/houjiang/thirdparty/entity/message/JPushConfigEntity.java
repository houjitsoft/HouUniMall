package com.houjiang.thirdparty.entity.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 极光推送配置实体
 *
 * @author Amos
 * @since 2025-07-02
 */
@Data
@TableName("hou_message_jpush_config")
public class JPushConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 组织ID */
    private String orgId;

    /** 极光应用 AppKey */
    private String appKey;

    /** 极光应用 Master Secret（存储时加密，读取时脱敏） */
    private String masterSecret;

    /** 是否启用iOS推送(APNs) */
    private Integer apnsEnabled;

    /** APNs证书路径 */
    private String apnsCertificate;

    /** APNs证书密码 */
    private String apnsPassword;

    /** APNs环境：0-开发 1-生产 */
    private Integer apnsProduction;

    /** 默认推送标题 */
    private String defaultTitle;

    /** 默认推送图标URL */
    private String defaultIcon;

    /** 推送渠道：android/ios/all */
    private String channel;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
