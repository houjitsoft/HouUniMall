package com.houjiang.mall.entity.artifact;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 插件制品（{@code hou_plugin_artifact}）—— 制品的权威元信息与上传幂等锁。
 *
 * <p>与 {@code hou_market_plugin_version} 的分工：版本表承载「市场语义」（状态、审核、上下架），
 * 本表承载「制品物理身份」（sha256 / md5 / 存储键），
 * 二者通过 {@code artifact_id} 关联，版本表里的 jar 地址是可变的网关地址，本表的 key 才是不可变身份。
 *
 * <p><b>为什么放在 {@code com.houjiang.mall.entity} 包下</b>：mall 启动类没有显式
 * {@code @MapperScan}，MyBatis-Plus 走的是自动配置的包扫描，范围是启动类所在包
 * {@code com.houjiang.mall}。Mapper 必须落在这个包下才会被注册，
 * 配套实体也一并放这里，与 plugin-market 的 {@code com.houjiang.mall.entity.market} 保持同一套约定。
 *
 * <p><b>M2 归位说明</b>：本类原先放在 plugin-publish 的 {@code com.houjiang.mall.entity.publish}。
 * 下载链路（令牌签发 → 授权判定 → 回源）全部实现在 plugin-artifact，
 * 都要读这张表；若实体留在 plugin-publish，plugin-artifact 就得反向依赖它，构成模块环。
 * 因此把实体与 Mapper 迁到本模块（DDL 本来就在 plugin-artifact 的
 * {@code sql/plugin_artifact_tables.sql} 里），plugin-publish 改为依赖本模块。
 * 仅改包路径，字段与建表语句均未变动。
 *
 * @author Amos
 * @since M1（M2 迁移到 plugin-artifact）
 */
@Data
@TableName("hou_plugin_artifact")
public class PluginArtifact implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 制品 ID = sha256(code|version|md5) 的完整 64 位十六进制，不可变身份。
     */
    private String artifactId;

    private String pluginCode;

    private String version;

    /**
     * 存储类型：local / minio / cos。
     */
    private String storageType;

    /**
     * 存储键 {@code {code}/{version}/{code}-{version}.jar} 或 commercial-* 前缀。
     */
    private String storageKey;

    /** jar/source/oci; source and OCI are delivered independently from embedded JARs. */
    private String artifactKind = "jar";
    private String hostService;
    private String runtimeArtifact;
    private String sourceLicense;

    /**
     * 制品签名密钥 ID（与发行清单 kid / hou_market_plugin_version.signature_kid 对齐）。
     */
    private String signatureKid;

    /**
     * 制品 md5（小写十六进制）。
     */
    private String md5;

    /**
     * 制品 sha256（小写十六进制）。
     */
    private String sha256;

    /**
     * 制品字节数。
     */
    private Long sizeBytes;

    /**
     * 0=上传中 1=已完成 2=已作废。
     */
    private Integer uploadStatus;

    /**
     * 客户端幂等键（clientOpId），命中即返回首次结果。
     */
    private String uploadId;

    private Long createdBy;

    private LocalDateTime createTime;
}
