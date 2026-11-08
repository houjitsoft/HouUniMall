package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 返回订单
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnOrderVO {

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 退货编号
     */
    private String returnCode;

    /**
     * 1-退款申请 2-退货申请
     */
    private String returnType;

    /**
     * 添加时间
     */
    private String returnAddTime;

    /**
     * 0-商家拒绝 1-新发起等待卖家审核 2-卖家审核通过 3-卖家审核不通过 4-卖家收到货物 5-平台审核通过 6-买家已填物流 7-平台拒绝
     */
    private String returnState;

    /**
     * 退货商品编号，0为退款
     */
    private String orderGoodsId;

    /**
     * 退款商品名称
     */
    private String orderGoodsName;

    /**
     * 商品单价
     */
    private String orderGoodsPrice;

    /**
     * 退货数量
     */
    private String orderGoodsNum;

    /**
     * 商品图片
     */
    private String orderGoodsPic;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 买家id
     */
    private String shopId;

    /**
     * 商品详情
     */
    private List<GoodsDetailsVO> goodsDetails;

    /**
     * 退款金额
     */
    private String returnCash;

    /**
     * 物流单号
     */
    private String orderShippingCode;

    /**
     * 退货物流Id
     */
    private String returnShippingCode;

    /**
     * 商家备注
     */
    private String returnShopMessage;

    /**
     * 退款理由
     */
    private String returnReason;

    /**
     * 退货备注
     */
    private String returnMessage;

    /**
     * 收货人的联系方式
     */
    private String orderReceiverContact;

    /**
     * 应付金额（商品实际支付金额 + 运费）
     */
    private String orderPaymentAmount;

    /**
     * 用户订单是否供应商代发货 1是  2否
     */
    private Integer dropShipping;

    /**
     * 供应商退货价格
     */
    private BigDecimal supperReturnPrice;

    /**
     * 供应商 实付价 含运费
     */
    private BigDecimal supperPaymentAmount;

    /**
     * 询价单id
     */
    private String inquiryId;

    /**
     * 购买用户id
     */
    private String buyUserId;

    /**
     * 用户名
     */
    private String buyUserName;
    /**
     * 卖家id
     */
    private String sellerUserId;
    /**
     * 卖家用户名
     */
    private String sellerUserName;

    /**
     * 订单商品详情
     */
    private List<GoodsOrderDetailsVO> goodsOrderDetailsVO;

    /**
     * 平台介入状态 0:待卖家上传凭证  1:待平台审核 2 已处理  3 买家撤销
     */
    private Integer interventionState;

    /**
     * 1.退款 2.仅退款 3退货退款 4 换货
     */
    private Integer returnMethodStatus;

    /**
     * 平台处理结果
     */
    private Integer processingResults;

    /**
     * 订单来源:0:自营订单,1:代发平台供应链订单,2:代发怡亚通供应链订单
     */
    private Integer orderSource;

    /**
     * 待发店铺id
     */
    private String deliverShopId;

    /**
     * 待发店铺名
     */
    private String deliverShopName;

    /**
     * 失效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expirationTime;

    /**
     * 商家是否自营
     */
    private String shopSelfSupport;

    /**
     * 平台退款凭证
     */
    private String returnPaymentVoucher;

    /**
     * 平台退款凭证
     */
    private List<String> returnPaymentVoucherList;

    /**
     * 1.线上支付  2.线下支付 3.货到付款
     */
    private Integer paymentCode;

    /**
     * 是否已寄回
     */
    private Integer isSendBack;

    /**
     * 寄回说明
     */
    private String sendBackNotes;
    /**
     *  店铺类型: 1-卖家店铺; 2:供应商店铺 3 品牌店铺
     */
    private Integer ShopType;

    @Override
    public String toString() {
        return "ReturnOrderVO{" +
                "orderId='" + orderId + '\'' +
                ", returnCode='" + returnCode + '\'' +
                ", returnType='" + returnType + '\'' +
                ", returnAddTime='" + returnAddTime + '\'' +
                ", returnState='" + returnState + '\'' +
                ", orderGoodsId='" + orderGoodsId + '\'' +
                ", orderGoodsName='" + orderGoodsName + '\'' +
                ", orderGoodsPrice='" + orderGoodsPrice + '\'' +
                ", orderGoodsNum='" + orderGoodsNum + '\'' +
                ", orderGoodsPic='" + orderGoodsPic + '\'' +
                ", shopName='" + shopName + '\'' +
                ", shopId='" + shopId + '\'' +
                ", goodsDetails=" + goodsDetails +
                ", returnCash='" + returnCash + '\'' +
                ", orderShippingCode='" + orderShippingCode + '\'' +
                ", returnShippingCode='" + returnShippingCode + '\'' +
                ", returnShopMessage='" + returnShopMessage + '\'' +
                ", returnReason='" + returnReason + '\'' +
                ", returnMessage='" + returnMessage + '\'' +
                ", orderReceiverContact='" + orderReceiverContact + '\'' +
                ", orderPaymentAmount='" + orderPaymentAmount + '\'' +
                ", dropShipping=" + dropShipping +
                ", supperReturnPrice=" + supperReturnPrice +
                ", supperPaymentAmount=" + supperPaymentAmount +
                ", inquiryId='" + inquiryId + '\'' +
                ", buyUserId=" + buyUserId +
                ", buyUserName='" + buyUserName + '\'' +
                '}';
    }
}