package com.houjiang.merchant.common.entity.mall;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallRefund implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "修改时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "用户id")
    private Integer userId;
    @Schema(description = "售后单号")
    private String sn;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "售后id")
    private Integer orderAfterSaleId;

    @Schema(description = "支付方式")
    private Integer payWay;
    @Schema(description = "售后单号")
    private String fromSn;
    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "金额")
    private BigDecimal money;

    @Schema(description = "原订单支付金额")
    private BigDecimal orderMoney;
    @Schema(description = "第三方流水号")
    private String transactionSn;
    @Schema(description = "状态 0 退款中 1 成功 2失败")
    private Integer status;
}