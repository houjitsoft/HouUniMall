package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @ClassName queryPurchaseOrderVO
 * @Description
 * @Author Amos
 * @Date 2024/7/25 11:17
 * @Version 1.0
 * @author Amos
 */
@Data
public class queryPurchaseOrderVO {
    /**
     * 订单编号，唯一标识一个订单。
     */
    private String orderId;

    /**
     * 进货单号
     */
    private String purchaseNumber;

    /**
     * 供应商名称，表示订单所属的供应商商店。
     */
    private String shopName;


    /**
     * 订单折扣金额，表示订单总金额的折扣部分。
     */
    private BigDecimal orderDiscountFee;

    /**
     * 订单运费，表示订单中支付的运输费用。
     */
    private BigDecimal orderShippingFee;

    /**
     * 订单实际支付金额，包括商品金额、折扣和运费等。
     */
    private BigDecimal orderPaymentAmount;

    /**
     * 订单状态，表示订单的当前状态，如待支付、已支付、已发货等。
     */
    private String orderStatus;

    /**
     * 订单创建时间，记录订单的创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderCreateTime;

    /**
     * 发票状态，表示发票的开具状态，如未开具、已开具等。
     */
    private String invoiceStatus;


    /**
     * 用户id
     */
    private String userId;

    /**
     * 1.线上支付  2.线下支付 3.货到付款 4.账期支付
     */
    private String paymentCode;

    /**
     * 审核状态  0 待上传   1 待审核 2 已审核 3 审核驳回
     */
    private Integer isApproval;

    /**
     * 商品状态 0下架，1正常，10违规（禁售）,2店铺下架商品下架,3商品删除,4定时发布，5放入仓库
     */
    private Integer commonState;

    /**
     * 商品详情
     */
    private List<queryPurchaseGoodsDetailsVO> goodsDetailsList;

    /**
     * 经销折扣金额
     */
    private BigDecimal dealerDiscountAmout;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 买方
     */
    private String orderReceiverName;

    /**
     * 委托代理人：
     */
    private String buyerUserName;

    /**
     * 退款状态
     */
    private Integer orderRefundStatus;

    /**
     * 退货状态
     */
    private Integer orderReturnStatus;

    /**
     * 换货状态：0：无换货 1：申请换货中 2：换货完成  3：商家拒绝换货
     */
    private Integer orderBarterStatus;

    /**
     * 1.退款,2.仅退款,3退货退款
     */
    private Integer returnMethodType;

    /**
     * 0-商家拒绝 1-新发起等待卖家审核 2-卖家审核通过 3-卖家审核不通过 4-卖家收到货物 5-平台审核通过 6-买家已填物流 7-平台拒绝
     */
    private Integer returnState;

    /**
     * 商品名称，表示订单中购买的商品名称。
     */
    private String goodsName;

    /**
     * 订单中商品的数量。
     */
    private String orderGoodsNum;

    /**
     * 商品单价，表示每个商品的售价。
     */
    private BigDecimal goodsPrice;

    /**
     * 订货编码
     */
    private String goodsCode;


    /**
     * 是否虚拟商品
     */
    private Integer virtualGoods;

    /**
     * 订单最后售后时间
     */
    private String orderLastReturnTime;

    /**
     * 商家名称
     * 用于存储业务实体的名称，便于在订单、发票等商业文件中引用
     */
    private String businessName;

    /**
     * 订单接收者地址
     * 该地址用于指示商家将订单商品配送到的地点
     */
    private String orderReceiverAddress;

    /**
     * 订单接收者联系方式
     * 用于商家与订单接收者之间的沟通和协调，确保订单顺利送达
     */
    private String orderReceiverContact;

    /**
     * 开户银行
     * 指定商家用于商业交易的银行名称，用于支付和结算目的
     */
    private String openingBank;

    /**
     * 银行账号
     * 关联开户银行，用于接收支付和进行财务交易
     */
    private String bankAccount;

}


