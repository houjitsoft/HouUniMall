package com.houjiang.merchant.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class RefundRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "退款编号")
    private String sn;

    @Schema(description = "关联用户ID")
    private Integer userId;

    @Schema(description = "来源订单ID")
    private Integer orderId;

    @Schema(description = "来源单号SN")
    private String orderSn;

    @Schema(description = "订单类型: [order=商品订单, recharge=充值订单]")
    private String orderType;

    @Schema(description = "总应付款金额")
    private BigDecimal orderAmount;

    @Schema(description = "本次退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "平台交易流水号")
    private String transactionId;

    @Schema(description = "退款方式: 1=线上退款, 2=线下退款")
    private Integer refundWay;

    @Schema(description = "退款类型: 1=后台退款")
    private Integer refundType;

    @Schema(description = "退款状态: 0=退款中, 1=退款成功, 2=退款失败")
    private Integer refundStatus;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

}
