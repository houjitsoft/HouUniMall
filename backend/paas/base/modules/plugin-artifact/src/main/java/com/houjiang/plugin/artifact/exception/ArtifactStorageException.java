package com.houjiang.plugin.artifact.exception;

import com.houjiang.commons.constant.CommonStatusEnum;

/**
 * 制品存储异常基类，统一携带可返回给前端的错误码（见 {@code PluginDistErrorCode}）。
 *
 * @author Amos
 * @since M0
 */
public class ArtifactStorageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码。
     */
    private final int code;

    /**
     * 使用默认失败码构造。
     *
     * @param message 中文错误描述
     */
    public ArtifactStorageException(String message) {
        this(CommonStatusEnum.FAIL.getCode(), message);
    }

    /**
     * 使用默认失败码并携带根因。
     *
     * @param message 中文错误描述
     * @param cause   原始异常
     */
    public ArtifactStorageException(String message, Throwable cause) {
        this(CommonStatusEnum.FAIL.getCode(), message, cause);
    }

    /**
     * 构造异常。
     *
     * @param code    业务错误码（3001-3018 体系）
     * @param message 中文错误描述
     */
    public ArtifactStorageException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造异常。
     *
     * @param code    业务错误码
     * @param message 中文错误描述
     * @param cause   原始异常
     */
    public ArtifactStorageException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * @return 业务错误码
     */
    public int getCode() {
        return code;
    }
}
