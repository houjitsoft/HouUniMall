package com.houjiang.plugin.publish.model;

import java.io.InputStream;
import lombok.Data;

/**
 * 上传命令（服务端内部入参，与 Controller 的 multipart 解耦，便于单测）。
 *
 * @author Amos
 * @since M1
 */
@Data
public class UploadCommand {

    /**
     * 插件编码，必须与 jar 内 plugin.yaml 的 code 一致。
     */
    private String pluginCode;

    /**
     * 版本号（SemVer），必须与 jar 内 plugin.yaml 的 version 一致。
     */
    private String version;

    /**
     * 客户端幂等键（UUID），重传同值直接返回首次结果。
     */
    private String uploadId;

    /**
     * 更新说明，可为空。
     */
    private String changelog;

    /**
     * 制品字节流；由本服务对象在使用完毕后关闭。
     */
    private InputStream jarStream;

    /**
     * 上传人，可为空。
     */
    private Long createdBy;
}
