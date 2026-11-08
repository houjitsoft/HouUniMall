package com.houjiang.plugin.publish.util;

import com.houjiang.plugin.artifact.util.ArtifactDigests;
import java.nio.charset.StandardCharsets;

/**
 * 制品 ID（artifact_id）生成规则。
 *
 * <p>公式：{@code sha256(pluginCode|version|md5)}，取<b>完整 64 位十六进制</b>。
 *
 * <p>早期方案取前 40 位截断，存在碰撞风险；字段长度本身成本为零，
 * 因此按决策改为完整摘要（建表 DDL 同步为 {@code VARCHAR(64)}）。
 *
 * <p>把 code / version / md5 一起哈希，是为了让「同一份 jar 换个版本号」
 * 与「同一版本号换个 jar」都得到不同的制品身份，避免复用。
 *
 * @author Amos
 * @since M1
 */
public final class ArtifactIds {

    /**
     * 私有构造，工具类不允许实例化。
     */
    private ArtifactIds() {
    }

    /**
     * 生成制品 ID。
     *
     * @param pluginCode 插件编码
     * @param version    版本号
     * @param md5        制品 md5（小写十六进制）
     * @return 完整 64 位 sha256 十六进制
     */
    public static String generate(String pluginCode, String version, String md5) {
        String seed = pluginCode + "|" + version + "|" + md5;
        return ArtifactDigests.sha256Hex(seed.getBytes(StandardCharsets.UTF_8));
    }
}
