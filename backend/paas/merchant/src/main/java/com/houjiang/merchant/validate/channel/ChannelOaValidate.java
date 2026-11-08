package com.houjiang.merchant.validate.channel;

import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class ChannelOaValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Length(max = 100, message = "小程序名称不能超100个字符")
    @Schema(description = "公众号名称")
    private String name = "";

    @Length(max = 100, message = "原始ID不能超100个字符")
    @Schema(description = "原始ID")
    private String originalId = "";

    @Length(max = 100, message = "appId不能超100个字符")
    @Schema(description = "appId")
    private String appId = "";

    @Length(max = 200, message = "appSecret不能超100个字符")
    @Schema(description = "appSecret")
    private String appSecret = "";

    @Length(max = 300, message = "小程序码不能超300个字符")
    @Schema(description = "二维码")
    private String qrCode = "";

    @Length(max = 300, message = "URL不能超300个字符")
    @Schema(description = "URL")
    private String url = "";

    @Length(max = 200, message = "Token不能超200个字符")
    @Schema(description = "Token")
    private String token = "";

    @Length(max = 43, message = "EncodingAESKey不能超43个字符")
    @Schema(description = "EncodingAESKey")
    private String encodingAesKey = "";

    @IntegerContains(values = {1, 2, 3}, message = "encryptionType不是合法值")
    @Schema(description = "消息加密方式")
    private Integer encryptionType = 1;

}
