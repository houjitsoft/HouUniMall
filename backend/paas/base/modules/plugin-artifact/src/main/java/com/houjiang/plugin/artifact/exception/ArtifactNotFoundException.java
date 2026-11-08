package com.houjiang.plugin.artifact.exception;

import com.houjiang.commons.constant.PluginDistErrorCode;

/**
 * 制品不存在异常。
 *
 * <p>上层应映射为错误码 3009 ARTIFACT_NOT_FOUND。
 *
 * @author Amos
 * @since M0
 */
public class ArtifactNotFoundException extends ArtifactStorageException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造异常。
     *
     * @param key 存储键
     */
    public ArtifactNotFoundException(String key) {
        super(PluginDistErrorCode.ARTIFACT_NOT_FOUND, "制品不存在：key=" + key);
    }

    /**
     * 构造异常。
     *
     * @param key    存储键
     * @param detail 补充描述
     */
    public ArtifactNotFoundException(String key, String detail) {
        super(PluginDistErrorCode.ARTIFACT_NOT_FOUND, "制品不存在：key=" + key + "，" + detail);
    }
}
