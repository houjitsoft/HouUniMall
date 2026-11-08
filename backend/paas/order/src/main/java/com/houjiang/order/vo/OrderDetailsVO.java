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
 * @ClassName OrderDetailsVO
 * @Description 已售订单列表
 * @Author Amos
 * @Date 2023/6/29 14:55
 * @Version 1.0
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsVO {
    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 支付码
     */
    private Integer payCode;

    /**
     * 订单状态，具体状态值根据业务定义
     */
    private Integer orderStatus;

    /**
     * 快递拼音简写
     */
    private String expressPinyin;

    /**
     * 订单创建时间，格式为：yyyy-MM-dd HH:mm:ss，时区为GMT+8
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date orderCreateTime;

    /**
     * 买家用户名
     */
    private String buyerUserName;

    /**
     * 买家用户ID
     */
    private String buyerUserId;

    /**
     * 订单收货人姓名
     */
    private String orderReceiverName;

    /**
     * 订单物流单号
     */
    private String orderShippingCode;

    /**
     * 订单退款状态，具体状态值根据业务定义
     */
    private Integer orderRefundStatus;

    /**
     * 订单退货状态，具体状态值根据业务定义
     */
    private Integer orderReturnStatus;

    /**
     * 订单支付金额
     */
    private Double orderPaymentAmount;

    /**
     * 订单收货地址
     */
    private String orderReceiverAddress;

    /**
     * 订单收货人联系电话
     */
    private String orderReceiverContact;

    /**
     * 订单运费，BigDecimal类型用于表示大额货币值，精确到小数点后两位，如：10.50元则表示为10.50元，注意与orderPaymentAmount区分开来。
     */
    private BigDecimal orderShippingFee;

    /**
     * 发货店铺名称（可能为快递公司名称）
     */
    private String deliverShopName;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 虚拟订单 0 是 1 否
     */
    private Integer virtualGoods;
    /**
     * 虚拟订单有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date periodOfValidity;
    /**
     * 过期退款 0 是 1 否
     */
    private Integer pastDue;
    /**
     * 虚拟商品兑换码
     */
    private String redeemCode;
    /**
     * 核销二维码
     */
    private String redeemCodeUrl;
    /**
     * 是否核销0是1否是否核销0是1否
     */
    private Integer isCancel;
    /**
     * 核销时间
     */
    private Date cancelDate;
    /**
     * 门店id
     */
    private Integer orderGateShopId;
    /**
     * 门店名字
     */
    private String shopGateName;
    /**
     * 商品信息
     */
    private List<GoodsDetailsVO> goodsDetails;
    /**
     *
     */
    private String zbName;
    /**
     * 满减赠赠品信息json
     */
    private String giveawayInfo;
    /**
     * 满减赠赠品信息
     */
    private List<ManJianZengPinVO> zengpinInfo;
    /**
     * 用户订单是否供应商代发货 1是  2否
     */
    private Integer dropShipping;
    /**
     * 0 不是plus订单 1 是plus订单
     */
    private Integer plusOrder;

    /**
     * 支付方式
     */
    private Integer paymentCode;

    /**
     * 开票状态:0-未开票;1-审核中;2-已出票;3-未通过
     */
    private Integer invoiceStatus;

    /**
     * 公司名称
     */
    private String expressName;

    /**
     * 订单原价
     */
    private BigDecimal orderGoodsAmount;

    /**
     * 红包
     */
    private BigDecimal orderRptPrice;

    /**
     * 会员折扣
     */
    private BigDecimal baseMemberDiscount;

    /**
     * plus折扣
     */
    private BigDecimal plusDiscount;

    /**
     * 满减
     */
    private BigDecimal manjianDiscount;

    /**
     * 满折
     */
    private BigDecimal manzheDiscount;

    /**
     * 改价
     */
    private BigDecimal orderAdjustFee;

    /**
     * 优惠券
     */
    private BigDecimal voucherPrice;

    /**
     * 经销折扣
     */
    private BigDecimal distributionDiscount;

    /**
     * 订单商品数量
     */
    private Integer orderGoodsNum;

    /**
     * 订单折扣费用
     */
    private BigDecimal orderDiscountFee;

    /**
     * 骑手id
     */
    private String distributorId;

    /**
     * 0 待取货 1，待配送
     */
    private String pickup;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date paymentTime;

    /**
     * 订单留言
     */
    private String orderMessage;

    /**
     * 订单收货留言
     */
    private String orderShippingMessage;

    /**
     * 银行账号
     */
    private String bankAccount;

    /**
     * 开户银行
     */
    private String openingBank;

    /**
     * 经销折扣金额
     */
    private BigDecimal dealerDiscountAmout;

    /**
     * 子订单
     */
    private String subOrderNo;
    /**
     * 父订单
     */
    private String fathOrderNo;

    /**
     * 是否是代发订单1.是。0否
     */
    private Integer isOriginal;

    /**
     * 经销商店铺名称
     */
    private String dealerShopName;

    /**
     *  普通商品订单 2 商家批发订单
     */
    private Integer orderType;

    /**
     * 店铺id
     */
    private Integer shopId;

    private Integer orderSource;




    /**
     * 嘉币支付的现金部分
     */
    private BigDecimal orderCashPayment;

    /**
     * 用家、批发
     */
    private String customerTypeName;
}
