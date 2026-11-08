package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.order.dto.ReturnGoodsDetailsListDTO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 退款订单详情
 * @author Amos
 */
@Data
public class ReturnOrderDetailPlatVO {

    /**
     * 退款编号
     */
    private String returnCode;

    /**
     * 退款金额
     */
    private Double returnCash = 0.00;

    /**
     * 供货价
     */
    private Double orderSupplyAmount = 0.00;

    /**
     * 退还佣金(暂时废弃)
     */
    private String returnCommisionFee;

    /**
     * 退款理由
     */
    private String returnReason;


    /**
     * 创建时间
     */
    private String returnAddTime;

    /**
     * 商家备注
     */
    private String returnShopMessage;

    /**
     * 商家处理时间
     */
    private String returnShopTime;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 买家账号
     */
    private String buyerUserAccount;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 买家id
     */
    private String buyerUserId;

    /**
     * 退货备注
     */
    private String returnMessage;

    /**
     * 退款原因id
     */
    private Integer returnReasonId;

    /**
     * 退款方式
     */
    private Integer returnMethodStatus;

    /**
     * 提交次数
     */
    private Integer returnChangeNum;

    /**
     * 撤销次数
     */
    private Integer revokeNum;

    /**
     * 退货商品详情集合
     */
    private List<ReturnGoodsDetailsListDTO> orderGoodsDetail;

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
     * 订单商品名
     */
    private String orderGoodsName;

    /**
     * 审核状态
     */
    private Integer returnState;
    /**
     * 售后红包金额
     */
    private Double returnRptCash = 0.00;

    /**
     * 支付方式名称
     */
    private String paymentName;

    /**
     * 收货地址
     */
    private String returnAddrName;

    /**
     * 收货地址
     */
    private String orderReceiverAddress;

    /**
     * 支付单号
     */
    private String paymentNumber;

    /**
     * 应付金额（商品实际支付金额 + 运费）
     */
    private String orderPaymentAmount;

    /**
     * 平台留言
     */
    private String returnPlatformMessage;

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
     * 联系方式
     */
    private String returnMobile;

    /**
     * 收货人名称
     */
    private String orderReceiverName;

    /**
     * 收货人联系电话
     */
    private String orderReceiverContact;
}
