package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 短信配置信息表(SmsInfo)
 *
 * @since 2023-05-10 14:00:51
 * @author Amos
 */
@TableName("hou_base_sms_info")
@Data
public class SmsInfoEntity implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属组织 ID，短信密钥和通道按组织隔离。 */
    private String orgId;
    /**
     * 私钥
     */
    private String smsSecretId;
    /**
     * 密钥
     */
    private String smsSecretKey;
    /**
     * 短信工具应用APP的id
     */
    private String smsSdkAppId;
    /**
     * 公司标识
     */
    private String smsSign;
    /**
     * 启用和禁用状态，00禁用  01启用，默认启用
     */
    private String smsStatus;
    /**
     * oss类型
     */
    private String smsType;

    /**
     * 地区类型
     */
    private String smsRegion;
    /**
     * 伪删除标识 00删除 01正常，默认正常
     */
    @TableField("sms_isActive")
    private String smsIsactive;
    /**
     * 信息配置备注
     */
    private String smsRemark;
    /**
     * 创建时间
     */
    @TableField("sms_createdDate")
    private Date smsCreateddate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
