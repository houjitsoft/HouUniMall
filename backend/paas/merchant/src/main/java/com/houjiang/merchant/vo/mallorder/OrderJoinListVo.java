package com.houjiang.merchant.vo.mallorder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class OrderJoinListVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "收货人")
    private String addressContact;

    @Schema(description = "收货人电话")
    private String addressMobile;

    @Schema(description = "收货人地址")
    private String addressContent;

    @Schema(description = "自动取消提醒时间")
    private String autoCancelRemainTime;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "售后按钮")
    private Integer canApplyRefund;
    @Schema(description = "取消按钮")
    private Integer canCancel;

    @Schema(description = "确认按钮")
    private Integer canConfirm;

    @Schema(description = "物流按钮")
    private Integer canSeeLogistics;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "用户编号")
    private String userSn;

    @Schema(description = "下单用户")
    private String nickname;

    @Schema(description = "实付金额")
    private String payMoney;

    @Schema(description = "是否支付: [0=未支付, 1=已支付]")
    private Integer payIs;

    @Schema(description = "支付状态描述")
    private String payIsMsg;

    @Schema(description = "优惠券金额")
    private BigDecimal couponMoney;

    @Schema(description = "删除按钮")
    private Integer deleteBtn;

    @Schema(description = "发货按钮")
    private Integer deliverBtn;
    @Schema(description = "创建时间")
    private Long createTime;
    /** 获取总优惠金额 */
    @Schema(description = "总优惠金额")
    private BigDecimal decMoney;
    @Schema(description = "配送方式")
    private Integer deliveryType;

    @Schema(description = "会员折扣总额")
    private BigDecimal discountMoney;

    @Schema(description = "收货时间")
    private Long expressConfirmTime;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "订单类型 0普通订单 10秒杀 20 拼团")
    private Integer type;

    @Schema(description = "秒杀id")
    private Integer msId;

    @Schema(description = "应付金额")
    private BigDecimal needPayMoney;

    @Schema(description = "拼团id")
    private Integer ptId;

    @Schema(description = "拼团开团id")
    private Integer ptOpenId;

    @Schema(description = "拼团参团id")
    private Integer ptOpenJoinId;

    @Schema(description = "支付时间")
    private Long payTime;

    @Schema(description = "订单号")
    private String sn;
    @Schema(description = "状态 0待付款 1待发货 2待收货 3已完成 4已关闭")
    private Integer status;

    @Schema(description = "已发货 0未发货 1已发货")
    private Integer expressIs;

    @Schema(description = "运费总额")
    private BigDecimal expressMoney;

}
