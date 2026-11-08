package com.houjiang.payment.dto;


import com.houjiang.commons.constant.CommonType;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author Amos
 * @date 2021-01-0516:21
 * @Description: 充值和提现公共请求参数
 **/
@Data
public class FXCommonDTO {
    /**
     * 用户token
     */
    @NotNull(message = "登录信息失效，请重新登录")
    private String token;

    @NotNull(message = "请确认您的交易金额")
    private BigDecimal totalMoney;

    /**
     * 支付方式 1支付宝 2微信 3余额
     */
    @NotNull(message = "请选择您的支付方式")
    private Integer payCode;

    /**
     * 请求的支付来源 手机端1PC 2wap微信浏览器 3安卓 5ios 6微信小程序 7wap浏览器
     */
    @NotNull
    private Integer platFrom;

    /**
     * 支付来源
     */
    private Integer payFrom;

    /**
     * 交易状态
     */
    private Integer recordStatus;

    /**
     * 账户类型 1蜂币交易  2积分交易
     */
    private Integer recordType;

    /**
     * 交易类型 1蜂币充值 2充值升级
     */
    private Integer tradeTypeId = 1;

    /**
     * 交易描述
     */
    private String tradeDesc;

    /**
     * 金额正负判断(1:+0:-)
     */
    private Integer plusOrMinus;

    /**
     * 第三方请求单号
     */
    private String thirdPayId;

    /**
     * 交易单号
     */
    private String recordId;

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
    private String wxOpenId;

    /**
     * 微信用户昵称
     */
    private String wxUserName;

    /**
     * 升级团长配置id
     */
    private Integer configId;

    /**
     * 支付订单号
     */
    private String orderId;

    /**
     * 是否合并支付
     */
    private Byte mergePay;

    /**
     * 手续费
     */
    private BigDecimal taxAmount = BigDecimal.ZERO;

    /**
     * 设置平台请求来源
     * @param platFrom
     */
    public Integer getPayFrom(Integer platFrom) {
        if(platFrom.equals(CommonType.payPlatFrom.PC.getCode())){
            // PC
            return CommonType.payPlatFrom.PC.getCode();
        }else if(platFrom.equals(CommonType.payPlatFrom.ANDROID.getCode()) || platFrom.equals(CommonType.payPlatFrom.IOS.getCode())){
            // APP
            return CommonType.PayRequestFrom.APP.getCode();
        }else if(platFrom.equals(CommonType.payPlatFrom.WAP.getCode())){
            // 微信浏览器
            return CommonType.PayRequestFrom.WX_BROWSER.getCode();
        }else if(platFrom.equals(CommonType.payPlatFrom.MINI_PROGRAM.getCode())){
            // 小程序
            return CommonType.PayRequestFrom.MINI_PROGRAM.getCode();
        }else {
            // WAP
            return CommonType.PayRequestFrom.WAP.getCode();
        }
    }
}
