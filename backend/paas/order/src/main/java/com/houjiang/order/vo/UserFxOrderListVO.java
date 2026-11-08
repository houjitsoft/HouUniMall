package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 微客具体的分销订单
 * @author Amos
 */
@Data
public class UserFxOrderListVO {

    /**
     * 买家昵称
     */
    private String userName;

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 佣金状态 null全部 1待支付  2未入账  3已入账  4已退款  5已取消 6已冻结
     */
    private Integer orderCommissionStatus;

    /**
     * 下单时间
     */
    private String orderCreateTime;

    /**
     * 订单状态
     */
    private boolean isRefund;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 支付方式
     */
    private Integer payCode;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;
    /**
     * 一级分销佣金
     */
    private BigDecimal oneTgCommission;
    /**
     * 二级分销佣金
     */
    private BigDecimal twoTgCommission;
    /**
     * 分享佣金
     */
    private BigDecimal shareCommission;
    /**
     * 总运费
     */
    private BigDecimal totalShippingFee;
    /**
     * 退款金额
     */
    private BigDecimal totalReturnPrice;
    /**
     * 订单商品列表
     */
    private List<UserFxOrderGoodsVO> goodsList;

}
