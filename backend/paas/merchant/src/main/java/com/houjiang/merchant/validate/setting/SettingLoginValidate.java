package com.houjiang.merchant.validate.setting;

import com.alibaba.fastjson2.JSONArray;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class SettingLoginValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "登录方式")
    private JSONArray loginWay;

    @Schema(description = "强制绑定手机")
    private Integer coerceMobile = 0;

    @Schema(description = "政策协议")
    private Integer loginAgreement = 0;

    @Schema(description = "第三方登录")
    private Integer thirdAuth = 0;

    @Schema(description = "微信登录")
    private Integer wechatAuth;

    @Schema(description = "qq登录")
    private Integer qqAuth;

}
