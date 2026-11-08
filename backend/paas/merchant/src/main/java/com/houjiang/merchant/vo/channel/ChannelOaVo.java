package com.houjiang.merchant.vo.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class ChannelOaVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "公众号名称")
    private String name;
    @Schema(description = "原始ID")
    private String originalId;
    @Schema(description = "appId")
    private String appId;
    @Schema(description = "appSecret")
    private String appSecret;
    @Schema(description = "二维码")
    private String qrCode;
    @Schema(description = "URL")
    private String url;
    @Schema(description = "Token")
    private String token;
    @Schema(description = "EncodingAESKey")
    private String encodingAesKey ;
    @Schema(description = "消息加密方式")
    private Integer encryptionType;

    @Schema(description = "业务域名")
    private String businessDomain;
    @Schema(description = "JS接口安全域名")
    private String jsSecureDomain;
    @Schema(description = "网页授权域名")
    private String webAuthDomain;

}
