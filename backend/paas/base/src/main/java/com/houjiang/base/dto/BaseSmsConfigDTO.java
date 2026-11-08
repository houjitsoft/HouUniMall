package com.houjiang.base.dto;

import lombok.Data;

/**
 * 修改sms 配置
 * @author Amos
 */
@Data
public class BaseSmsConfigDTO {

    /**
     * token
     */
    private String token;

    /**
     * id
     */
    private Long id;

    /** 组织 ID 仅用于兼容回显，实际写入由服务端从登录上下文解析。 */
    private String orgId;

    /**
     * 私钥
     */
    private String smsSecretId;

    /**
     * 密钥
     */
    private String smsSecretKey;

    /**
     * 短信工具应用APP的id
     */
    private String smsSdkAppId;

    /**
     * 公司标识
     */
    private String smsSign;

    /**
     * 启用和禁用状态，00禁用  01启用，默认启用
     */
    private String smsStatus;

    /**
     * oss类型
     */
    private String smsType;

    /**
     * 地区类型
     */
    private String smsRegion;

    /**
     * 伪删除标识 00删除 01正常，默认正常
     */
    private String smsIsactive;

    /**
     * 信息配置备注
     */
    private String smsRemark;

    /**
     * 注册，绑定手机模板编码
     */
    private String smsRegistersTel;

    /**
     * 修改手机号模板编码
     */
    private String smsUpdateTemplate;

    /**
     * 修改银行卡模板编码
     */
    private String smsUpdateBankcard;

    /**
     * 邀请模板编码
     */
    private String smsInvite;

    /**
     * 登录模板编码
     */
    private String smsLogin;

    /**
     * 通用模板编码
     */
    private String smsGeneral;

    /**
     * 商家提现
     */
    private String smsModifyPwd;

    /**
     * 平台 库存不足 短信模板
     */
    private String smsInventory;

    /**
     * 发货成功，提醒短信
     */
    private String smsShipments;

    /**
     * 平台台，提醒商家发货
     */
    private String smsPushTheDelivery;

    /**
     * 通用
     */
    private String smsGeneralTemplate;

    /**
     * 商家提醒用户付款
     */
    private String smsGoodsObligation;

    /**
     * 报价提醒
     */
    private String smsQuotationReminder;

    /**
     * 商家审核还款通过
     */
    private String smsCollectionSuccess;

    /**
     * 商家审核还款拒绝
     */
    private String smsCollectionError;

    /**
     * 商家提醒还款
     */
    private String smsRemindRepayment;

    /**
     * 企业审核成功
     */
    private String smsBusinessSuccess;

    /**
     * 企业审核失败
     */
    private String smsBusinessError;

    /**
     * 订单审批成功
     */
    private String smsApprovalSuccess;

    /**
     * 订单审批失败
     */
    private String smsApprovalError;
}
