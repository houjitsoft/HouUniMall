package com.houjiang.commons.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 第三方集成配置DTO
 * 
 * @author Amos
 * @since 2025-01-07
 */
@Data
public class ThirdPartyConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 组织ID
     */
    private String orgId;

    /**
     * 平台名称 (dingtalk, wechat-work, feishu, yuque)
     */
    private String platform;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 钉钉配置
     */
    private DingtalkConfig dingtalk;

    /**
     * 企业微信配置
     */
    private WechatWorkConfig wechatWork;

    /**
     * 飞书配置
     */
    private FeishuConfig feishu;

    /**
     * 语雀配置
     */
    private YuqueConfig yuque;

    /**
     * 电子签名配置
     */
    private EsignConfig esign;

    /**
     * 微信登录配置（微信公众号扫码登录）
     */
    private WechatLoginConfig wechatLogin;

    /**
     * QQ 登录配置（QQ 互联网站应用）
     */
    private QqLoginConfig qqLogin;

    /**
     * 描述
     */
    private String description;

    /**
     * 钉钉配置类
     */
    @Data
    public static class DingtalkConfig implements Serializable {
        private String appKey;
        private String appSecret;
        private String agentId;
        private String callbackUrl;
    }

    /**
     * 企业微信配置类
     */
    @Data
    public static class WechatWorkConfig implements Serializable {
        private String corpId;
        /** 自建应用 Secret（如厚匠科技），用于应用消息等 */
        private String secret;
        /** 通讯录 Secret（部门/成员同步），未填时回退使用 secret */
        private String addressBookSecret;
        /** 客户联系 Secret，未填时回退使用 secret */
        private String contactSecret;
        /** 打卡数据 Secret（getcheckindata），未填时回退使用 secret */
        private String checkinSecret;
        private String agentId;
        private String callbackUrl;
    }

    /**
     * 飞书配置类
     */
    @Data
    public static class FeishuConfig implements Serializable {
        private String appId;
        private String appSecret;
        private String encryptKey;
        private String callbackUrl;
    }

    /**
     * 语雀配置类
     */
    @Data
    public static class YuqueConfig implements Serializable {
        private String appId;
        private String appSecret;
        private String accessToken;
        private String callbackUrl;
    }

    /**
     * 电子签名配置类
     */
    @Data
    public static class EsignConfig implements Serializable {
        /**
         * 服务提供商 (fadada-法大大, shangshangqian-上上签, esign-e签宝, tencent-腾讯电子签)
         */
        private String provider;
        
        /**
         * AppId/ClientId
         */
        private String appId;
        
        /**
         * AppSecret/ClientSecret
         */
        private String appSecret;
        
        /**
         * API地址
         */
        private String apiUrl;
        
        /**
         * 回调地址
         */
        private String callbackUrl;
        
        /**
         * 其他配置参数（JSON格式）
         */
        private String extraConfig;
    }

    /**
     * 微信登录配置类（微信公众号扫码登录）
     */
    @Data
    public static class WechatLoginConfig implements Serializable {
        /**
         * 微信公众号AppID
         */
        private String appId;
        
        /**
         * 微信公众号AppSecret
         */
        private String appSecret;
        
        /**
         * 回调URL（redirect_uri）
         */
        private String callbackUrl;
    }

    /**
     * QQ 登录配置类（QQ 互联网站应用，platform = qq-login）
     */
    @Data
    public static class QqLoginConfig implements Serializable {
        /**
         * 应用 APP ID
         */
        private String appId;

        /**
         * 应用 APP Key
         */
        private String appSecret;

        /**
         * 回调URL（redirect_uri）
         */
        private String callbackUrl;
    }
}
