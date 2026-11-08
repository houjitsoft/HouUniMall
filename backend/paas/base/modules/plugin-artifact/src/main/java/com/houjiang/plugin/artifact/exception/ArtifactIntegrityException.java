package com.houjiang.plugin.artifact.exception;

import com.houjiang.commons.constant.PluginDistErrorCode;

/**
 * 制品完整性异常：校验和不匹配、实际大小与声明不一致、Range 区间非法等。
 *
 * <p>上层应映射为错误码 3010 CHECKSUM_MISMATCH（md5/sha256 不一致）。
 *
 * @author Amos
 * @since M0
 */
public class ArtifactIntegrityException extends ArtifactStorageException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造异常。
     *
     * @param message 中文错误描述
     */
    public ArtifactIntegrityException(String message) {
        super(PluginDistErrorCode.CHECKSUM_MISMATCH, message);
    }

    /**
     * 构造异常。
     *
     * @param message 中文错误描述
     * @param cause   原始异常
     */
    public ArtifactIntegrityException(String message, Throwable cause) {
        super(PluginDistErrorCode.CHECKSUM_MISMATCH, message, cause);
    }
}
