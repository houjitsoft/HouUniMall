package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.vo.goods.GoodsDetailsListVO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 卖家订单详细信息
 * @author Amos
 */
@Data
public class SellerOrderDetailVO {

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 退款编号
     */
    private String returnCode;

    /**
     * 退款金额
     */
    private Double returnCash;

    /**
     * 退款状态 1未退款，2已退款
     */
    private Integer returnState;

    /**
     * 平台介入状态 1:待平台审核 ，2 已处理  ，3 买家撤销
     */
    private Integer interventionState;

    /**
     * 退款申请日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date returnAddTime;

    /**
     * 买家账号
     */
    private String buyerUserName;

    /**
     * 订单商品名
     */
    private String orderGoodsName;

    /**
     * 订单商品图
     */
    private String orderGoodsPic;

    /**
     * 订单商品数量
     */
    private String orderGoodsNum;

    /**
     * 退货数量
     */
    private String orderGoodsReturnNum;

    /**
     * 订单商品价格
     */
    private String orderGoodsPrice;

    /**
     * 买家账号
     */
    private String buyerUserAccount;

    /**
     * 退款理由
     */
    private String returnReason;

    /**
     * 收货人名称
     */
    private String orderReceiverName;

    /**
     * 收货人联系地址
     */
    private String orderReceiverAddress;

    /**
     * 收货人联系方式
     */
    private String orderReceiverContact;

    /**
     * 支付单号
     */
    private String paymentNumber;

    /**
     * 支付方式名称
     */
    private String paymentName;

    /**
     * 下单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderCreateTime;

    /**
     * 付款时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date paymentTime;

    /**
     * 应付金额（商品实际支付金额 + 运费）
     */
    private String orderPaymentAmount;

    /**
     * 商家备注
     */
    private String returnShopMessage;

    /**
     * 平台留言
     */
    private String returnPlatformMessage;

    /**
     * 商家处理结果  1-待处理  2-卖家审核通过 3-卖家审核不通过(暂废弃，状态同return_state)
     */
    private String returnShopHandle;

    /**
     * 步骤状态
     */
    private Integer stepState;

    /**
     * 运费
     */
    private String orderShippingFee;

    /**
     * 店铺名
     */
    private String shopName;

    /**
     * 退货物流单号
     */
    private String returnShippingCode;

    /**
     * 邮编
     */
    private String returnPostCode;

    /**
     * 联系手机
     */
    private String returnMobile;

    /**
     * 退货备注
     */
    private String returnMessage;

    /**
     * 退货类型
     */
    private Integer returnType;

    /**
     * 用户订单是否供应商代发货 1是  2否
     */
    private Integer dropShipping;

    /**
     * 退货物流公司
     */
    private String returnPostName;

    /**
     * 1可操作  0不可操作
     */
    private Integer operate;

    /**
     * 优惠价格（商品金额-支付金额）
     */
    private Double orderDiscountFee;

    /**
     * 1普通订单   2商家批发订单(商家购买批发商商品订单)  3爆品绑定订单   4直播订单
     */
    private Integer orderType;

    /**
     * 1普通订单   2商家批发订单(商家购买批发商商品订单)  3爆品绑定订单   4直播订单
     */
    private List<GoodsDetailsVO> goodsDetails;

    /**
     * 供应商退款价格
     */
    private BigDecimal supperRefundPrice;

    /**
     * 供应商退货价格
     */
    private BigDecimal supperReturnPrice;

    /**
     * 供应商订单总价 含运费
     */
    private BigDecimal supperPaymentAmount;

    /**
     * 询价单号
     */
    private String inquiryId;

    /**
     * 供货价
     */
    private Double orderSupplyAmount = 0.00;

    /**
     * 退还佣金(暂时废弃)
     */
    private String returnCommisionFee;

    /**
     * 商家处理时间
     */
    private String returnShopTime;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 买家id
     */
    private String buyerUserId;

    /**
     * 退款原因id
     */
    private Integer returnReasonId;

    /**
     * 退款方式
     */
    private Integer returnMethodStatus;

    /**
     * 商品详情
     */
    private List<GoodsDetailsListVO> orderGoodsDetail;

    /**
     * 关闭时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date closeTime;

    /**
     * 失效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expirationTime;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date returnFinishTime;

    /**
     * 唤起 0 不可唤起 1可以唤起
     */
    private Integer callStatus;

    /**
     * 售后红包金额
     */
    private Double returnRptCash = 0.00;

    /**
     * 退货凭证
     */
    private String returnVoucher;

    /**
     * 退货凭证
     */
    private List<String> returnVouchers;

    /**
     * 收货地址
     */
    private String returnAddrName;

    /**
     * 发货地址
     */
    private String orderSellerAddress;

    /**
     * 发货人姓名
     */
    private String orderSellerName;

    /**
     * 发货人联系方式
     */
    private String orderSellerContact;

    /**
     * 商家填写收货地址
     */
    private String returnAddr;

    /**
     * 商家填写收货地址id
     */
    private Integer returnAddrId;

    /**
     * 商家收货人
     */
    private String returnRealName;

    /**
     * 商家收货人联系电话
     */
    private String returnNumber;

    /**
     * 返回会员折扣
     */
    private Double returnMemberDiscount = 0.00;

    /**
     * 返回经销折扣
     */
    private Double returnSupperDiscountFee = 0.00;

    /**
     * 退还 供货价
     */
    private Double orderReturnPrice = 0.00;

    /**
     * 订单来源:0:自营订单,1:代发平台供应链订单,2:代发怡亚通供应链订单
     */
    private Integer orderSource;

    /**
     * 支付方式:1.线上支付  2.线下支付 3.货到付款 4.账期支付'
     */
    private Integer paymentCode;

    /**
     * 换货物流信息
     */
    private List<BarterShippingInfoVO> barterShippingInfoList;

    @Override
    public String toString() {
        return "SellerOrderDetailVO{" +
                "orderId='" + orderId + '\'' +
                ", returnCode='" + returnCode + '\'' +
                ", returnCash=" + returnCash +
                ", returnState=" + returnState +
                ", returnAddTime=" + returnAddTime +
                ", buyerUserName='" + buyerUserName + '\'' +
                ", orderGoodsName='" + orderGoodsName + '\'' +
                ", orderGoodsPic='" + orderGoodsPic + '\'' +
                ", orderGoodsNum='" + orderGoodsNum + '\'' +
                ", orderGoodsReturnNum='" + orderGoodsReturnNum + '\'' +
                ", orderGoodsPrice='" + orderGoodsPrice + '\'' +
                ", buyerUserAccount='" + buyerUserAccount + '\'' +
                ", returnReason='" + returnReason + '\'' +
                ", orderReceiverName='" + orderReceiverName + '\'' +
                ", orderReceiverAddress='" + orderReceiverAddress + '\'' +
                ", orderReceiverContact='" + orderReceiverContact + '\'' +
                ", paymentNumber='" + paymentNumber + '\'' +
                ", paymentName='" + paymentName + '\'' +
                ", orderCreateTime=" + orderCreateTime +
                ", paymentTime=" + paymentTime +
                ", orderPaymentAmount='" + orderPaymentAmount + '\'' +
                ", returnShopMessage='" + returnShopMessage + '\'' +
                ", returnPlatformMessage='" + returnPlatformMessage + '\'' +
                ", returnShopHandle='" + returnShopHandle + '\'' +
                ", stepState=" + stepState +
                ", orderShippingFee='" + orderShippingFee + '\'' +
                ", shopName='" + shopName + '\'' +
                ", returnShippingCode='" + returnShippingCode + '\'' +
                ", returnPostCode='" + returnPostCode + '\'' +
                ", returnMobile='" + returnMobile + '\'' +
                ", returnMessage='" + returnMessage + '\'' +
                ", returnType=" + returnType +
                ", dropShipping=" + dropShipping +
                ", returnPostName='" + returnPostName + '\'' +
                ", operate=" + operate +
                ", orderDiscountFee=" + orderDiscountFee +
                ", orderType=" + orderType +
                ", goodsDetails=" + goodsDetails +
                ", supperRefundPrice=" + supperRefundPrice +
                ", supperReturnPrice=" + supperReturnPrice +
                ", supperPaymentAmount=" + supperPaymentAmount +
                ", inquiryId='" + inquiryId + '\'' +
                '}';
    }
}
