package com.houjiang.merchant.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Amos
 */
@Data
@TableName("hou_merchant_user_recharge")
public class RechargeOrder {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "充值金额")
    private BigDecimal money;

    @Schema(description = "订单编号")
    private String sn;

    @Schema(description = "支付方式: [2=微信支付, 3=支付宝支付]")
    private Integer payWay;

    @Schema(description = "支付状态: [0=待支付, 1=已支付]")
    private Integer payStatus;

    @Schema(description = "支付时间")
    private Long payTime;

    @Schema(description = "下单终端")
    private Integer terminal;

    @Schema(description = "支付编号")
    private String transactionSn;

    @Schema(description = "微信小程序发货信息录入")
    private Integer wechatMiniExpressSync;

    @Schema(description = "录入时间")
    private Long wechatMiniExpressSyncTime;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

    @TableField(exist = false)
    @Schema(description = "退款状态: [0=未退款 , 1=已退款]")
    private Integer refundStatus;

    @TableField(exist = false)
    @Schema(description = "退款交易流水号")
    private BigInteger refundTransactionId;

}
