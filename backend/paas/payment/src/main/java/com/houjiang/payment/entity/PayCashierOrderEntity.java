package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 统一收银台订单（hou_pay_cashier_order）
 * 对应前端 16 终端收银台页面下单/查询/关单
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_cashier_order")
public class PayCashierOrderEntity implements Serializable {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 收银台订单号（前端感知） */
    private String cashierNo;

    /** 商户订单号（下游业务订单） */
    private String mchOrderNo;

    /** Business idempotency key supplied by the mall/order service. */
    private String requestNo;

    /** Mall order number; mchOrderNo remains available for legacy callers. */
    private String mallOrderNo;

    /** 支付平台订单号（回调关联） */
    private String payOrderId;

    /** 下单用户ID */
    private String userId;

    /** 商户号 */
    private String mchNo;

    /** 应用ID */
    private String appId;

    /** 终端编码 */
    private String terminal;

    /** 支付方式编码 */
    private String payMethod;

    /** 支付金额（元） */
    private BigDecimal amount;

    /** 币种 */
    private String currency;

    /** 订单标题 */
    private String subject;

    /** 订单描述 */
    private String body;

    /** 客户端IP */
    private String clientIp;

    /** 业务回调地址 */
    private String notifyUrl;

    /** 支付完成跳转地址 */
    private String returnUrl;

    /** 扩展参数(JSON) */
    private String extParam;

    /** 下发给前端的调起参数(JSON) */
    private String payData;

    /** 状态：0-待支付；1-支付中；2-支付成功；3-支付失败；4-已关闭；5-已退款 */
    private Integer status;

    /** 错误码 */
    private String errCode;

    /** 错误信息 */
    private String errMsg;

    /** 支付过期时间 */
    private Date expireTime;

    /** 支付成功时间 */
    private Date successTime;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
