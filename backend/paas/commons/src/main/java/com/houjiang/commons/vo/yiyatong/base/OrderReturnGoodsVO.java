package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName OrderReturnGoodsVO
 * @Description
 * @Author Amos
 * @Date 2024/11/14 10:39
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderReturnGoodsVO {


    /**
     * 下单方式：1:开放平台,2:其它, 不知什么原因，实际返回为1-开放平台,2-线上下单
     */
    private String orderType;

    /**
     * 快递公司编号，用于标识配送货物的物流公司
     */
    private String sendDeliveryCorpSn;

    /**
     * 用户联系电话，用于在配送过程中与用户进行沟通
     */
    private String userPhone;

    /**
     * 退货商品明细
     */
    private List<OrderReturnGoodsItemVO> goodsList;

    /**
     * 是否删除 默认为0（1：删除，0：未删除）
     *
     */
    private Integer modifyUserId;

    /**
     * 售后状态（1,待卖家审核2,卖家拒绝退款：3,退款成功：4,卖家拒绝退货退款：5,待买家退货：6,买家已退货，待卖家收货：7,买家已退货，卖家拒绝收货：8,卖家已收货,待确认退款：9,退货退款成功：10,卖家拒绝换货：11,买家已退货，待卖家换货：12,卖家已换货，待买家收货：13,换货成功：14,已关闭：15,卖家同意退款,16：卖家同意退款, 17：卖家拒绝退款，18：退款成功）
     */
    private Integer returnStatus;

    /**
     * 分销商id
     *
     */
    private String buyerId;

    /**
     * 客服记录-客服备注
     * 用于记录客户服务相关的备注信息，帮助跟踪和解决问题
     */
    private String customerServiceRemark;

    /**
     * 退货物流信息明细
     */
    private List<DeliveryUpdateVO> deliveryList;

    /**
     * 退款日志明细
     */
    private List<OrderReturnLogVO> refundLogVO;

    /**
     * 快递公司编号
     */
    private String deliveryCorpSn;

    /**
     * 修改时间
     */
    private String modifyTime;

    /**
     * 收货人姓名
     */
    private String receiveName;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 退货单号
     */
    private String returnSn;

    /**
     * 发货快递单号
     */
    private String sendDeliverySn;

    /**
     * 申请退款金额
     */
    private BigDecimal applyReturnAmount;

    /**
     * 订单项ID
     */
    private String id;

    /**
     * 申诉倒计时
     */
    private String appealCountdown;

    /**
     * 完成时间
     */
    private String finishTime;

    /**
     * 供应商操作ID
     */
    private String supplierOperationId;

    /**
     * 收货地址
     */
    private String receiveAddress;

    /**
     * 订单起源 0-自有平台 1-1688平台
     */
    private Integer orderOrigin;

    /**
     * 售后类型，1：退款，2：退货退款，3：换货，4：仅退款,5：部分退款
     */
    private String returnTypeName;

    /**
     * 订单总金额
     */
    private BigDecimal totalOrderPrice;

    /**
     * 供应商运营方式(业务平台运营方式 :1自营、2分销)
     */
    private String supplierOperationType;

    /**
     * 发货地址
     */
    private String sendAddress;

    /**
     * 发货时间
     */
    private String sendTime;

    /**
     * 用户地址
     */
    private String userAddress;

    /**
     * 供应商用户名
     */
    private String supplierUserName;

    /**
     * 退货状态名称
     */
    private String returnStatusName;

    /**
     * 下单方式
     */
    private Integer orderSourceDesc;

    /**
     * 退货备注
     */
    private String returnComment;

    /**
     * 申请退货图片URL
     */
    private String applyReturnPictureurl;

    /**
     * 实际退货原因
     */
    private String returnRealReason;

    /**
     * 运营方式(申诉平台运营方式 :1自营、2分销)
     */
    private Integer operationType;

    /**
     * 退货上传图片URL集合
     */
    private List<String> pictureurlList;

    /**
     * 售后类型，1：退款，2：退货退款，3：换货，4：仅退款,5：部分退款
     */
    private Integer returnType;

    /**
     * 供应商ID
     */
    private String supplierId;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 可退总额
     */
    private BigDecimal totalPrice;

    /**
     * 发货快递名称
     */
    private String sendDeliveryName;

    /**
     * 申诉状态(0无,1可申诉,2已申诉)
     */
    private Integer isAppeal;

    /**
     * 实际退货原因ID
     */
    private String returnRealReasonId;

    /**
     * 创建订单时间
     */
    private String createOrderTime;

    /**
     * 自动处理时间
     */
    private String autoProcessTime;

    /**
     * 退货时间
     */
    private String returnGoodsTime;

    /**
     * 快递单号
     */
    private String deliverySn;

    /**
     * 供应商操作名称
     */
    private String supplierOperationName;

    /**
     * 退货原因
     */
    private String returnReason;

    /**
     * 渠道退货单号
     */
    private String channelReturnSn;

    /**
     * 运营ID
     */
    private String operationId;

    /**
     * 第三方订单号
     */
    private String thirdOrderSn;

    /**
     * 商品总数量
     */
    private Integer totalGoodsQuantity;

    /**
     * 分销市场基础金额
     */
    private BigDecimal distriMarketBaseAmount;

    /**
     * 发货方式，1自己联系，2无需物流
     */
    private Integer returnWay;

    /**
     * 转卖操作类型
     */
    private String resellerOperationType;

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 下单方式 1-开放平台,2-线上下单
     */
    private Integer orderSource;

    /**
     * 收货人电话
     */
    private String receivePhone;

    /**
     * 发货人姓名
     */
    private String sendName;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 售后订单物流信息明细
     */
    private List<OrderReturnGoodsDeliveryItemVO> deliveryItemVOList;

    /**
     * 订单邮费
     */
    private BigDecimal postFee;
    /**
     * 操作名称
     */
    private String operationName;
    /**
     * 买家名称
     */
    private String buyerName;
    /**
     * 用户名称
     */
    private String userName;
    /**
     * 分销商名称
     */
    private String distributor;
    /**
     * 供应商电话
     */
    private String supplierPhone;
    /**
     * 供应商原因
     */
    private String supplierReason;
    /**
     * 供货商品总额
     */
    private BigDecimal supplyGoodsAmount;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 快递公司名称
     */
    private String deliveryCorpName;
    /**
     * 发货电话
     */
    private String sendPhone;
    /**
     * 租户ID
     */
    private String tenantId;
    /**
     * 分销市场金额
     */
    private BigDecimal distriMarketAmount;

    /**
     * 换货物流信息明细
     */
    private List<DeliveryUpdateVO> exchangeDeliveryList;

    /**
     * 实际退费金额
     */
    private BigDecimal actualReturnAmount;
}
