package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 品牌店铺支付配置信息表
 * @TableName ${database.prefix.payment}hou_pay_config_shop
 * @author Amos
 */
@TableName(value ="hou_pay_config_shop")
@Data
public class PayConfigShopEntity implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
     * 微信小程序回调
     */
    private String appletNotifyUrl;

    /**
     * 微信wap端证书地址
     */
    private String weChatWapPath;

    /**
     * 店铺id
     */
    private Integer shopId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}