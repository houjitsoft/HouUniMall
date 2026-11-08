package com.houjiang.commons.vo.order;

import lombok.Data;

/**
 * @ClassName InvoiceEntityInfoVO
 * @Description
 * @Author Amos
 * @Date 2022/7/29 10:59
 * @Version 1.0
 * @author Amos
 */
@Data
public class InvoiceEntityInfoVO {
    /**
     * 发票id
     */
    private Integer invoiceId;
    /**
     * 开票状态:0-未开票;1-审核中;2-已出票;3-未通过
     */
    private Integer invoiceStatus;


    /**
     * 发票内容:0-商品明细;1-商品类别
     */
    private Integer invoiceContent;

    /**
     * 收票人姓名
     */
    private String userName;

    /**
     * 收票人电话
     */
    private String userMobile;

    /**
     * 收票人邮件
     */
    private String userMail;

    /**
     * 收票人地址-省
     */
    private Integer addressProvince;

    /**
     * 收票人地址-市
     */
    private Integer addressCity;

    /**
     * 收票地址-区
     */
    private Integer addressArea;

    /**
     * 详细地址
     */
    private String addressDetail;

    /**
     * 发票类型: 0-普票；1-增值税专用票
     */
    private Integer invoiceType;

    /**
     * 抬头类型:1-个人；2-单位
     */
    private Integer invoiceHeadType;

    /**
     * 发票抬头
     */
    private String invoiceHead;

    /**
     * 发票id
     */
    private Integer invoiceTmpId;

    /**
     * 是否默认
     */
    private Integer isDefault;

    /**
     * 单位税号
     */
    private String companyTaxId;

    /**
     * 注册地址
     */
    private String registerAddress;

    /**
     * 注册电话
     */
    private String registerNumber;

    /**
     * 开户银行
     */
    private String openingBank;

    /**
     * 银行帐号
     */
    private String bankAccount;

    /**
     * 商户订单id
     */
    private String orderId;

    /**
     * 用户id
     */
    private String userId;

    private String token;

    private Integer addressId;

    private String addressInfo;

    private String completeAddress;

    private String createTime;

    private String lastUpdateTime;

    private String preCompleteAddress;

}
