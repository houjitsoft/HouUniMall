package com.houjiang.merchant.validate.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class ChannelMpValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Length(max = 100, message = "小程序名称不能超100个字符")
    @Schema(description = "小程序名称")
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

}
