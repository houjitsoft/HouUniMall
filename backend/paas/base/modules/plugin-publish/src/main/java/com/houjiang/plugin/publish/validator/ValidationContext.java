package com.houjiang.plugin.publish.validator;

import java.util.function.Predicate;
import lombok.Data;

/**
 * 清单校验的上下文（把外部依赖抽出来，让校验器本身变成纯函数、可单测）。
 *
 * @author Amos
 * @since M1
 */
@Data
public class ValidationContext {

    /**
     * 接口入参的插件编码，必须与 jar 内 {@code plugin.yaml} 的 code 一致。
     */
    private String pluginCode;

    /**
     * 接口入参的版本号，必须与 jar 内 {@code plugin.yaml} 的 version 一致。
     */
    private String version;

    /**
     * 制品字节数（服务端实际收到并落盘的字节数，不信任客户端声明）。
     */
    private long sizeBytes;

    /**
     * 该 pluginCode 的历史最大版本（SemVer 比较口径）；没有历史版本时为 null 或空白。
     */
    private String maxExistingVersion;

    /**
     * 制品体积上限（字节），超出报 3017 SIZE_EXCEEDED。
     */
    private long maxArtifactBytes;

    /**
     * 依赖是否已在上架状态：入参为被依赖插件编码，返回 true 表示在市场中存在且已上架（status=2）。
     */
    private Predicate<String> dependencyPublished = code -> true;

    /**
     * 构造上下文。
     *
     * @param pluginCode         插件编码
     * @param version            版本号
     * @param sizeBytes          制品字节数
     * @param maxExistingVersion 历史最大版本，可为 null
     * @param maxArtifactBytes   体积上限
     */
    public ValidationContext(String pluginCode,
                             String version,
                             long sizeBytes,
                             String maxExistingVersion,
                             long maxArtifactBytes) {
        this.pluginCode = pluginCode;
        this.version = version;
        this.sizeBytes = sizeBytes;
        this.maxExistingVersion = maxExistingVersion;
        this.maxArtifactBytes = maxArtifactBytes;
    }

    /**
     * 无参构造（便于 Bean 绑定与测试）。
     */
    public ValidationContext() {
        this(null, null, 0L, null, 0L);
    }
}
