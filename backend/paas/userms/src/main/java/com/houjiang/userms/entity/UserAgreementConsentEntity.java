package com.houjiang.userms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户协议同意记录实体类。
 *
 * <p>留痕用户每一次同意行为（注册 / 版本更新后重新同意）。
 * 同意记录只存版本号，正文按版本存于 {@code hou_base_agreement_version}，
 * 举证时按「协议编码 + 版本号」即可还原用户当时看到的原文。</p>
 *
 * @author Amos
 */
@Data
@TableName("hou_user_agreement_consent")
public class UserAgreementConsentEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名（冗余，便于后台检索）
     */
    private String username;

    /**
     * 协议ID
     */
    private Integer agreementId;

    /**
     * 协议编码
     */
    private String agreementCode;

    /**
     * 协议名称（同意时的快照）
     */
    private String agreementName;

    /**
     * 协议版本ID
     */
    private Integer versionId;

    /**
     * 同意的协议版本号
     */
    private String versionNo;

    /**
     * 同意场景：register=注册 / reagree=版本更新后重新同意 / other=其他
     */
    private String scene;

    /**
     * 同意时间
     */
    private Date consentTime;

    /**
     * 客户端IP
     */
    private String ip;

    /**
     * IP 归属地（异步解析，可为空）
     */
    private String ipLocation;

    /**
     * User-Agent 原文
     */
    private String ua;

    /**
     * 设备类型（PC / Mobile / Tablet）
     */
    private String device;

    /**
     * 浏览器名称及版本
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 来源端：web=管理后台 / h5 / app / openapi
     */
    private String source;

    /**
     * 状态：1=有效 0=已失效（版本更新后作废）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;
}
