package com.houjiang.plugin.artifact.model;

import java.io.Serializable;
import lombok.Data;

/**
 * 制品写入/读取的结果元信息。
 *
 * <p>md5 与 sha256 在写入过程中同步计算（一次读流完成，不二次读盘），
 * 落库到 {@code hou_plugin_artifact}，并作为制品签名规范化串的输入。
 *
 * @author Amos
 * @since M0
 */
@Data
public class StoredObject implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 存储键，契约 {@code {code}/{version}/{code}-{version}.jar}。
     */
    private String key;

    /**
     * 制品 md5（小写十六进制，32 位）。
     */
    private String md5;

    /**
     * 制品 sha256（小写十六进制，64 位）。
     */
    private String sha256;

    /**
     * 制品字节数。
     */
    private long sizeBytes;

    /**
     * 存储类型：local / cos。
     */
    private String storageType;

    /**
     * 构造空对象（便于反序列化与 Bean 绑定）。
     */
    public StoredObject() {
        // 默认构造
    }

    /**
     * 全参构造。
     *
     * @param key         存储键
     * @param md5         制品 md5
     * @param sha256      制品 sha256
     * @param sizeBytes   制品字节数
     * @param storageType 存储类型
     */
    public StoredObject(String key, String md5, String sha256, long sizeBytes, String storageType) {
        this.key = key;
        this.md5 = md5;
        this.sha256 = sha256;
        this.sizeBytes = sizeBytes;
        this.storageType = storageType;
    }
}
