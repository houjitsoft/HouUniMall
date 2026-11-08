package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 通企付 H5 收银台订单（hou_pay_h5cash_order）
 * 关联接口：27. POST /h5Cash/getH5Url
 * 关联表DDL：docs/sql/patches/h5cash_order.sql
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_h5cash_order")
public class H5CashOrderEntity implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商户号 */
    private String mchNo;

    /** 应用ID */
    private String appId;

    /** 商户订单号 */
    private String mchOrderNo;

    /** 请求流水号 */
    private String reqSeqNo;

    /** 通企付订单号（响应回填） */
    private String payOrderId;

    /** 金额（分） */
    private Long amount;

    /** 币种 */
    private String currency;

    /** 订单描述 */
    private String body;

    /** 商品信息(JSONArray) */
    private String goodsInfo;

    /** 商户扩展参数 */
    private String extParam;

    /** 异步通知地址 */
    private String notifyUrl;

    /** 前端跳转地址 */
    private String jumpUrl;

    /** 微信点金跳转 */
    private String frontUrl;

    /** 链接有效期(秒) */
    private Integer validTime;

    /** 订单失效时间(秒) */
    private Integer expiredTime;

    /** 展示支付方式 */
    private String payTypeInfo;

    /** 直接支付方式 */
    private String directPayType;

    /** 渠道参数(JSON) */
    private String channelExtra;

    /** 通企付返回的 H5Url */
    private String h5Url;

    /** 状态：0生成 1已下单 2已支付 3已退款 4已关闭 5异常 */
    private Integer state;

    /** 客户端IP */
    private String clientIp;

    /** 支付成功时间 */
    private Date paySuccessTime;

    /** 订单失效时间 */
    private Date expireTime;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
