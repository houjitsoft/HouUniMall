package com.houjiang.merchant.vo.setting;

import com.alibaba.fastjson2.JSONArray;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class SettingLoginVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "登录方式")
    private JSONArray loginWay;

    @Schema(description = "强制绑定手机")
    private Integer coerceMobile;

    @Schema(description = "是否开启协议")
    private Integer loginAgreement;

    @Schema(description = "第三方的登录")
    private Integer thirdAuth;

    @Schema(description = "微信登录")
    private Integer wechatAuth;

    @Schema(description = "qq登录")
    private Integer qqAuth;

}
