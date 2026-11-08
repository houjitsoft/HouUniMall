package com.houjiang.payment.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 支付配置信息表(PayConfig)
 *
 * @since 2023-05-10 14:36:26
 * @author Amos
 */
@TableName("hou_pay_config")
@Data
public class PayConfigEntity implements Serializable {
    /**
     * 主键
     */
    private Integer id;
    /**
     * 支付方式 00微信 PC h5 01 app 02微信小程序 03支付宝APP
     */
    private String paymentMode;
    /**
     * APP Id
     */
    private String appID;

    /**
     * APP Secret
     */
    private String appSecret;

    /**
     * 商户号
     */
    private String appMCHId;

    /**
     * APP密钥
     */
    private String appAPIKey;
    /**
     * 微信退款URL
     */
    private String refundUrl;

    /**
     * 微信支付回调接口
     */
    private String notifyUrl;

    /**
     * 微信异步通知页面路径
     */
    private String redirectUrl;

    /**
     * 微信pc端证书地址
     */
    private String weChatPcPath;

    /**
     * 支付宝网关请求地址
     */
    private String gatewayURL;

    /**
     * 支付宝服务器异步通知页面路径
     */
    private String shoppingNotifyUrl;

    /**
     * 支付宝付款服务器异步通知页面路径
     */
    private String rechargeNotifyUrl2;

    /**
     * 支付宝页面跳转同步通知页面路径
     */
    private String shoppingReturnUrl;

    /**
     * 支付宝pc购物回调
     */
    private String shoppingReturnUrlPc;

    /**
     * 启用和禁用状态，00禁用  01启用，默认启用
     */
    private String paymentStatus;

    /**
     * 伪删除标识 00删除 01正常，默认正常
     */
    private String paymentIsActive;

    /**
     * 更新时间
     */
    private Date paymentUpdateDate;

    /**
     * 支付宝公钥证书
     */
    private String certPublicKeyRes2;
    /**
     * 支付宝根证书
     */
    private String certRootKey;
    /**
     * 应用公钥证书
     */
    private String certAppPublicKey;

    /**
     * plus会员回调地址
     */
    private String plusNotifyUrl;

    @TableField(exist = false)
    private Integer tagId;
    /**
     * 返回给前端的标识 0 初始化成功但无配置信息  1已经存在配置信息并返回  2初始化失败
     */
    @TableField(exist = false)
    private Integer flag;




    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

