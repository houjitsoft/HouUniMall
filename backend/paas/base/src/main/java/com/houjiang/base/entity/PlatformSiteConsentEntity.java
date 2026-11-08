package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 平台侧：站点回传的协议签署记录实体。
 *
 * <p>站点（客户部署的站）里用户同意协议后，记录先落在站内 {@code hou_user_agreement_consent}，
 * 再由站点把同一条签署凭据回传到平台，落在本表，供平台统一留存与统计。</p>
 *
 * <p>🔴 口径：本表只做「平台侧留痕」，不替代站内记录。站点侧回传失败不阻断站内留痕——
 * 合规主记录始终在站内，平台侧是副本。</p>
 *
 * @author Amos
 */
@Data
@TableName("hou_platform_site_consent")
public class PlatformSiteConsentEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 站点唯一标识 */
    private String siteCode;

    /** 站点名称（冗余） */
    private String siteName;

    /** 站内用户ID（平台侧不做用户体系关联，仅留痕） */
    private String userId;

    /** 登录名/手机号 */
    private String username;

    /** 协议编码 */
    private String agreementCode;

    /** 协议名称（冗余） */
    private String agreementName;

    /** 协议版本号 */
    private String versionNo;

    /** 场景：register / reagree / plugin_purchase */
    private String scene;

    /** 来源端 */
    private String source;

    /** 同意时客户端IP */
    private String ip;

    /** 同意时 UA */
    private String userAgent;

    /** 站内记录的同意时间 */
    private Date consentTime;

    private Date createTime;

    private Date updateTime;
}
