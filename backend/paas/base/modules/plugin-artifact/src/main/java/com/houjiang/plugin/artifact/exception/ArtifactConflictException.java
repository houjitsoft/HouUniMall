package com.houjiang.plugin.artifact.exception;

import com.houjiang.commons.constant.PluginDistErrorCode;

/**
 * 制品冲突异常：目标存储键已存在。
 *
 * <p>这是「绝不覆盖已发布制品」保护机制的唯一出口。任何试图写入已存在 key 的行为都必须走到这里，
 * 上层应映射为错误码 3012 VERSION_CONFLICT（同版本已存在 / 版本号未递增）。
 *
 * @author Amos
 * @since M0
 */
public class ArtifactConflictException extends ArtifactStorageException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造异常。
     *
     * @param key 冲突的存储键
     */
    public ArtifactConflictException(String key) {
        super(PluginDistErrorCode.VERSION_CONFLICT, "制品已存在，不允许覆盖：key=" + key);
    }

    /**
     * 构造异常。
     *
     * @param key    存储键
     * @param detail 补充描述
     */
    public ArtifactConflictException(String key, String detail) {
        super(PluginDistErrorCode.VERSION_CONFLICT, "制品已存在，不允许覆盖：key=" + key + "，" + detail);
    }
}
