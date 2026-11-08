package com.houjiang.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName ThirdAuthReturnDTO
 * @Description 第三方请求返回参数
 * @Author Amos
 * @Date 2023/6/13 17:16
 * @Version 1.0
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThirdAuthReturnDTO {

    /**
     * 用户信息
     */
    private String token;

    /**
     * 支付宝用户的唯一 userId
     */
    private String aliUserId;

    /**
     * 支付宝用户名
     */
    private String aliUserName;

    /**
     * 微信普通用户的标识，对当前开发者帐号唯一
     */
    private String openid;

    /**
     * 微信用户昵称
     */
    private String wxNickname;

    /**
     * 微信用户统一标识。针对一个微信开放平台帐号下的应用，同一用户的 unionid 是唯一的。
     */
    private String unionid;

    /**
     * 来源  6目前只有小程序传值
     */
    private Integer platForm;

    public ThirdAuthReturnDTO(String aliUserId, String aliUserName) {
        this.aliUserId = aliUserId;
        this.aliUserName = aliUserName;
    }

    public ThirdAuthReturnDTO(String openid, String wxNickname, String unionid) {
        this.openid = openid;
        this.wxNickname = wxNickname;
        this.unionid = unionid;
    }
}
