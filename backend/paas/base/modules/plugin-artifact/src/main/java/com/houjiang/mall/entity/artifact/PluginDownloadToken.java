package com.houjiang.mall.entity.artifact;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 插件下载令牌留痕（{@code hou_plugin_download_token}，方案 §3.8）。
 *
 * <p><b>为什么落库</b>：令牌本身是无状态 HMAC 串，但「限次」必须有服务端状态，
 * 否则同一个链接可以被无限次下载；落库同时承担事后审计（谁在什么时候下了哪个包）。
 *
 * <p><b>{@code nonce} 的作用</b>：uk_nonce 保证同一个随机串只能被占用一次，
 * 签发撞串时换串重试；校验时再比对「留痕行的 nonce」与「载荷里的 nonce」是否一致，
 * 不一致说明有人拿 A 令牌的留痕去配 B 令牌，直接判无效。
 *
 * <p><b>为什么放在 {@code com.houjiang.mall.entity} 包下</b>：mall 启动类没有
 * {@code @MapperScan}，MyBatis-Plus 扫描的是启动类所在包 {@code com.houjiang.mall}，
 * Mapper 只有落在这个包下才会被注册（M1 已踩过一次，此处沿用同一套约定）。
 *
 * @author Amos
 * @since M2
 */
@Data
@TableName("hou_plugin_download_token")
public class PluginDownloadToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 令牌 ID（32 位十六进制）。
     */
    private String tokenId;

    /**
     * 防重放随机串（32 位十六进制），uk_nonce 唯一。
     */
    private String nonce;

    /**
     * 站点实例（域名/机器码）。
     */
    private String instanceId;

    /**
     * 下载用户。
     */
    private Long userId;

    private String pluginCode;

    private String version;

    /**
     * 制品 ID（与 hou_plugin_artifact.artifact_id 对齐，完整 sha256 十六进制）。
     */
    private String artifactId;

    private LocalDateTime issuedAt;

    private LocalDateTime expiresAt;

    /**
     * 限次而非一次性：兼容断点续传/重试。
     */
    private Integer maxHits;

    /**
     * 已命中次数。
     */
    private Integer hits;

    /**
     * 客户端 IP，泄露溯源。
     */
    private String clientIp;

    /**
     * 客户端 UA，泄露溯源。
     */
    private String userAgent;
}
