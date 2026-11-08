package com.houjiang.plugin.publish.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 上传接口的返回体（方案 §4.1：{@code {artifactId, versionId, md5, sha256, size, validations}}）。
 *
 * <p><b>不返回 jar_url</b>：制品地址是受控资源，只能经签名下载链接获取，
 * 上传结果里暴露裸地址等于把防盗链设计作废。
 *
 * @author Amos
 * @since M1
 */
@Data
public class UploadResult {

    /**
     * 制品 ID（sha256(code|version|md5) 完整十六进制）。
     */
    private String artifactId;

    /**
     * 版本记录 ID（hou_market_plugin_version.id）。
     */
    private Long versionId;

    /**
     * 服务端重算的 md5（不信任客户端传值）。
     */
    private String md5;

    /**
     * 服务端重算的 sha256。
     */
    private String sha256;

    /**
     * 服务端统计的字节数。
     */
    private long size;

    /**
     * 签名密钥 ID；未启用签名时为 null。
     */
    private String kid;

    /**
     * 清单校验结果（含 WARN 级提示，便于发布者自查）。
     */
    private List<ValidationIssue> validations = new ArrayList<>();

    /**
     * 是否命中幂等（同一 uploadId 重传，或同 code+version 的 draft 已存在）。
     */
    private boolean idempotent;
}
