package com.houjiang.payment.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付方式枚举（统一收银台 Task 0）
 *
 * @author Amos
 */
@Getter
@AllArgsConstructor
public enum PayMethodEnum {

    /** 微信支付（JSAPI/APP/H5/Native/小程序） */
    WECHAT("WECHAT", "微信支付"),
    /** 支付宝（APP/WAP/PC/小程序） */
    ALIPAY("ALIPAY", "支付宝"),
    /** 账户余额 */
    BALANCE("BALANCE", "余额支付"),
    /** 银联 */
    UNIONPAY("UNIONPAY", "银联支付"),
    /** Apple Pay */
    APPLEPAY("APPLEPAY", "Apple Pay"),
    /** 华为 Pay */
    HUAWEIPAY("HUAWEIPAY", "华为 Pay"),
    /** PayPal */
    PAYPAL("PAYPAL", "PayPal"),
    /** Stripe 国际卡支付 */
    STRIPE("STRIPE", "Stripe"),
    /** 货到付款（线下收款，不调用第三方网关） */
    COD("COD", "货到付款"),
    /** 银行卡快捷支付 */
    BANKCARD("BANKCARD", "银行卡"),
    /** 云闪付（W4 新增，独立于银联 UNIONPAY，直连云闪付 SDK） */
    YSF_PAY("YSF_PAY", "云闪付"),
    /** 支付宝花呗信用支付（W4 新增） */
    ALI_CREDIT("ALI_CREDIT", "支付宝花呗"),
    /** FME 职工金融服务支付（W4 新增） */
    FME_PAY("FME_PAY", "FME职工金融"),
    /** 微信支付 V3 独立 API（W5 新增，对接微信支付 v3 SDK） */
    WECHAT_V3("WECHAT_V3", "微信支付V3"),
    /** 分账支付（W4 新增，联动 hou_pay_division_receiver） */
    SHARE_PAY("SHARE_PAY", "分账支付");

    private final String code;
    private final String name;

    public static PayMethodEnum ofCode(String code) {
        if (code == null) {
            return null;
        }
        for (PayMethodEnum m : values()) {
            if (m.code.equalsIgnoreCase(code)) {
                return m;
            }
        }
        return null;
    }
}
