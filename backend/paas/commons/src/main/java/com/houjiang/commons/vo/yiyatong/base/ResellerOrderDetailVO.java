package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName ResellerOrderDetailVO
 * @Description
 * @Author Amos
 * @Date 2024/11/14 9:20
 * @Version 1.0
 * @author Amos
 */

@Data
public class ResellerOrderDetailVO {

    /**
     * 订单类型 1 商品订单 2采集订单
     */
    private Integer orderType;
    /**
     * 商品总价
     */
    private BigDecimal totalGoodsPrice;
    /**
     * 供应商ID
     */
    private String supplierId;
    /**
     * 支付渠道名称
     */
    private String payChannelName;
    /**
     * 订单编号
     */
    private String orderSn;
    /**
     * 支付时间
     */
    private String payTime;
    /**
     * 订单状态描述
     */
    private String orderStatusDesc;
    /**
     * 批次订单编号
     */
    private String batchOrderSn;
    /**
     * 订单状态: 10-已提交待付款,20-已付款待发,30-已发货待收货,40-确认收货交易成功,50-用户超时未支付订单自动取消,70-订单发货前售后关闭,80-订单发货后售后关闭
     */
    private Integer orderStatus;
    /**
     * 买家ID
     */
    private String buyerId;
    /**
     * 异位多支付流水号
     */
    private String ywdPaySn;
    /**
     * 客户端类型（0表示未知类型客户端, 1：IOS，2：Android，3：WAP，4：微信）, 默认为0
     */
    private Integer clientType;
    /**
     * 修改时间
     */
    private String modifyTime;
    /**
     * 支付金额
     */
    private BigDecimal payAmount;
    /**
     * 收货人姓名
     */
    private String receiveName;
    /**
     * 应用ID
     */
    private String appId;
    /**
     * 确认收货时间
     */
    private String confirmReceiveTime;
    /**
     * 第三方订单号
     */
    private String thirdOrderSn;
    /**
     * 订单ID
     */
    private String id;
    /**
     * 预支付编号
     */
    private String prepaySn;
    /**
     * 收货人手机号
     */
    private String receiveMobile;
    /**
     * 供应商名称
     */
    private String supplierName;
    /**
     * 订单完成时间
     */
    private String finishTime;
    /**
     * 订单来源 1-开发平台,2-线上下单
     */
    private Integer orderSource;
    /**
     * 结算时间
     */
    private String settleTime;
    /**
     * 应用名称
     */
    private String appName;
    /**
     * 配送方式(配送方式 1普通快递 2_到店自提 3_同城配送 )
     */
    private Integer deliveryType;
    /**
     *订单起源: 0-自有订单 1-1688平台
     */
    private Integer orderOrigin;

    /**
     * 配送费用，表示配送商品所需的费用
     */
    private BigDecimal deliveryPrice;

    /**
     * 订单总金额，包括商品价格、配送费等所有费用的总和
     */
    private BigDecimal totalOrderPrice;

    /**
     * 买家名称，购买商品的客户名称
     */
    private String buyerName;

    /**
     * 订单类型描述，对订单类型的详细描述
     */
    private String orderTypeDesc;

    /**
     * 发货时间，商家发送商品的时间
     */
    private String sendTime;

    /**
     * 订单关闭时间，订单被取消或完成的时间
     */
    private String orderCloseTime;

    /**
     * 操作类型描述，描述订单当前操作类型的详细说明
     */
    private String operationTypeDesc;

    /**
     * 订单来源描述，对订单来源的详细解释
     */
    private String orderSourceDesc;

    /**
     * 创建时间，订单被创建的时间
     */
    private String createTime;

    /**
     * 配送类型描述，对配送方式的详细说明
     */
    private String deliveryTypeDesc;

    /**
     * 操作类型，标识订单当前的操作类型，如创建、支付、发货等
     */
    private Integer operationType;

    /**
     * 支付渠道(0 未知, 默认值; 1支付宝;2微信;3微信公众号;4 银联;5账期支付
     */
    private String payChannel;

    /**
     * 订单对应商品信息
     */
    private List<ResellerOrderItemVO> goodsList;

    /**
     * 物流信息列表
     */
    private List<OrderExpressVO> deliverList;

    /**
     * 收货地址信息
     */
    private OrderRecieveAddrVO recieveAddr;
}
