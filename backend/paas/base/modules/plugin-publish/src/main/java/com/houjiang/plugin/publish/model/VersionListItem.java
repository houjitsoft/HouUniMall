package com.houjiang.plugin.publish.model;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 版本列表项（{@code /versions/list} 返回结构）。
 *
 * <p><b>刻意不包含 jarUrl</b>：方案 §4.1 明确要求「不返回裸 jar_url」，
 * 制品地址必须由 {@code /download-url} 在通过购买/授权校验后单独签发。
 *
 * @author Amos
 * @since M1
 */
@Data
public class VersionListItem {

    /**
     * 版本号。
     */
    private String version;

    /**
     * 版本状态（0=draft 1=published 3=pending 4=rejected 5=offline）。
     */
    private Integer status;

    /**
     * 状态中文描述。
     */
    private String statusName;

    /**
     * 制品 md5。
     */
    private String jarMd5;

    /**
     * 制品字节数。
     */
    private Long jarSize;

    /**
     * 制品签名；未签名为 null。
     */
    private String signature;

    /**
     * 签名密钥 ID。
     */
    private String kid;

    /**
     * 发布时间；未发布为 null。
     */
    private LocalDateTime publishTime;
}
