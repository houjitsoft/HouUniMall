package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.vo.goods.GoodsDetailsListVO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @ClassName ReturnOrderDetailListVO
 * @Description
 * @Author Amos
 * @Date 2023/7/17 14:51
 * @Version 1.0
 * @author Amos
 */
@Data
public class ReturnOrderDetailListVO {
    /**
     * 退款编号
     */
    private String returnCode;

    /**
     * 退款金额
     */
    private Double returnCash = 0.00;

    /**
     * 平台介入状态
     */
    private Integer interventionState;

    /**
     * 订单供货金额
     */
    private Double orderSupplyAmount = 0.00;

    /**
     * 退款佣金费用
     */
    private String returnCommisionFee;

    /**
     * 退款原因
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
     * 买家账户名
     */
    private String buyerUserAccount;

    /**
     * 商家名称
     */
    private String shopName;

    /**
     * 退货物流单号
     */
    private String returnShippingCode;

    /**
     * 退货物流公司
     */
    private String returnShippingCodeName;

    /**
     * 退货物流id
     */
    private String returnPostCode;

    /**
     * 填写物流联系电话
     */
    private String returnMobile;

    /**
     * 商家id
     */
    private Integer shopId;

    /**
     * 买家用户id
     */
    private String buyerUserId;

    /**
     * 说明
     */
    private String returnMessage;

    /**
     * 退款原因id
     */
    private Integer returnReasonId;

    /**
     * 退款方式状态
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
     * 历史状态
     */
    private String historicalReturnStatus;

    /**
     * 提交数量
     */
    private Integer resubmissionsNumber;

    /**
     * 订单商品详情列表
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
     * 售后时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date orderLastReturnTime;

    /**
     * 订单商品名称
     */
    private String orderGoodsName;

    /**
     * 审核状态
     */
    private Integer returnState;

    /**
     * 唤起状态
     */
    private Integer callStatus;

    /**
     * 退款RPT金额
     */
    private Double returnRptCash = 0.00;

    /**
     * 支付方式名称
     */
    private String paymentName;

    /**
     * 退货凭证
     */
    private String returnVoucher;

    /**
     * 退货凭证列表
     */
    private List<String> returnVouchers;

    /**
     * 收货地址名称
     */
    private String returnAddrName;

    /**
     * 商家填写收货地址
     */
    private String returnAddr;

    /**
     * 商家收货人姓名
     */
    private String returnRealName;

    /**
     * 商家收货人联系电话
     */
    private String returnNumber;

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
     * 收货地址
     */
    private String orderReceiverAddress;

    /**
     * 支付单号
     */
    private String paymentNumber;

    /**
     * 订单支付金额
     */
    private String orderPaymentAmount;

    /**
     * 平台处理备注
     */
    private String returnPlatformMessage;

    /**
     * 完成时间字符串
     */
    private String returnFinishTimes;

    /**
     * 退款会员折扣金额
     */
    private Double returnMemberDiscount = 0.00;

    /**
     * 退款超级折扣金额
     */
    private Double returnSupperDiscountFee = 0.00;

    /**
     * 订单退还供货价
     */
    private Double orderReturnPrice = 0.00;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品实际支付价格
     */
    private Double orderGoodsPaymentAmount;

    /**
     * 商品总金额
     */
    private Double orderGoodsAmount;
}
