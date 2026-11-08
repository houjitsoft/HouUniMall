package com.houjiang.mall.vo.setting;

import lombok.Data;

import java.io.Serializable;

/**
 * 商城支付方式（场景）展示 VO。
 * 对应 like-admin SettingPaymentMethodVo。
 *
 * @author Amos
 */
@Data
public class SettingPaymentMethodVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 支付方式（场景）主键，对应 hou_merchant_mall_pay_scene.id */
    private Integer id;

    /** 关联的支付配置 id（hou_merchant_mall_pay.id） */
    private Integer payConfigId;

    /** 支付方式名称（中文，由 payType 派生：1=余额支付,2=微信支付,3=支付宝支付） */
    private String payTypeName;

    /** 支付中心稳定渠道代码：BALANCE / WECHAT / ALIPAY。 */
    private String payMethod;

    /** 图标 url（来自 hou_merchant_mall_pay.icon） */
    private String icon;

    /** 场景：1=微信小程序, 2=微信公众号, 3=H5 ... */
    private Integer scene;

    /** 状态：0=关闭, 1=开启 */
    private Integer status;

    /** 默认支付：0=否, 1=是 */
    private Integer isDefault;
}
