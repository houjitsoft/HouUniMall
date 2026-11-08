package com.houjiang.merchant.vo.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class FinanceRechargeListVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "账号")
    private String acount;

    @Schema(description = "订单编号")
    private String sn;

    @Schema(description = "支付方式: [2=微信支付, 3=支付宝支付]")
    private String payWay;

    @Schema(description = "支付状态: [0=待支付, 1=已支付]")
    private Integer payStatus;

    @Schema(description = "退款状态: [0=未退款 , 1=已退款]")
    private Integer refundStatus;

    @Schema(description = "支付状态: [0=待支付, 1=已支付]")
    private String payStatusText;

    @Schema(description = "支付方式")
    private String payWayText;

    @Schema(description = "支付金额")
    private BigDecimal orderAmount;

    @Schema(description = "支付时间")
    private String payTime;

    @Schema(description = "创建时间")
    private String createTime;

}
