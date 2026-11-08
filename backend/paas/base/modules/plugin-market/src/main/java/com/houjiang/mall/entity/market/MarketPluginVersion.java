package com.houjiang.mall.entity.market;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 插件市场-插件版本（hou_market_plugin_version）。
 *
 * <p>承载「可在线安装」所需的 JAR 地址、签名、迁移 SQL 与兼容性声明。
 *
 * @author Amos
 */
@Data
@TableName("hou_market_plugin_version")
public class MarketPluginVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String pluginCode;

    private String version;

    private String jarUrl;

    private String jarMd5;

    private Long jarSize;

    private String jarSignature;

    private String changelog;

    private String schemaSql;

    private String rollbackSql;

    private String pluginYaml;

    private String compatibilityJson;

    /**
     * 0=draft 1=published 3=pending 4=rejected 5=offline。
     *
     * <p>取值沿用《线上插件目录与分发方案》§3.2，与现有代码固化语义对齐：
     * published 必须是 1（{@code PluginMarketService} 的 {@code VERSION_APPROVED} 只认 1）。
     * 状态跃迁由 {@code PluginVersionStateMachine} 统一收口。
     */
    private Integer status;

    private String reviewNote;

    private LocalDateTime publishTime;

    private LocalDateTime createTime;

    // ---------------- 以下为分发链路扩列（方案 §3.2，DDL 见 plugin_artifact_tables.sql） ----------------

    /** LOCAL/OSS/COS */
    private String storageType;

    /** 存储对象键（不可变身份；jar_url 是可变网关地址，两者职责不同） */
    private String storageKey;

    /** 制品 SHA-256：md5 仅防误传，sha256 + 签名防篡改 */
    private String jarSha256;

    /** 签名密钥 ID，密钥轮换必需 */
    private String signatureKid;

    /** plugin.yaml 摘要，防清单被掉包 */
    private String manifestSha256;

    /** 0=上传中 1=完成 2=失败 */
    private Integer uploadStatus;

    /** 客户端幂等键 clientOpId */
    private String uploadId;

    /** 审核人 */
    private Long reviewerId;

    /** 提交审核时间 */
    private LocalDateTime submitTime;

    /** 下架时间 */
    private LocalDateTime offlineTime;

    /** 下架原因 */
    private String offlineReason;

    /** 版本级下载埋点 */
    private Integer versionDownloadCount;
}
