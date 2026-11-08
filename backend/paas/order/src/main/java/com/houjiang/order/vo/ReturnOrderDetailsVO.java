package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.vo.goods.GoodsDetailsListVO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

/**
 * @ClassName ReturnOrderDetailsVO
 * @Description 退款退货信息
 * @Author Amos
 * @Date 2023/7/6 16:46
 * @Version 1.0
 * @author Amos
 */
@Data
public class ReturnOrderDetailsVO {
    /**
     * 1-退款申请 2-退货申请
     */
    private Integer returnType;

    /**
     * 退款申请日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date returnAddTime;

    /**
     * 退款状态 1未退款，2已退款
     */
    private Integer returnState;

    /**
     * 退款理由
     */
    private String returnReason;

    /**
     * 退款金额
     */
    private BigDecimal returnCash;

    /**
     * 退款订单号
     */
    private String orderReturnId;

    /**
     * 退货数量
     */
    private Integer orderGoodsNum;


    /**
     * 退款编号
     */
    private String returnCode;

    /**
     * 平台介入状态
     */
    private Integer interventionState;

    /**
     * 供货价
     */
    private Double orderSupplyAmount = 0.00;

    /**
     * 退还佣金(暂时废弃)
     */
    private String returnCommisionFee;

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
     * 店铺id
     */
    private Integer shopId;

    /**
     * 买家id
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
     * 历史状态
     */
    private String historicalReturnStatus;

    /**
     * 提交数量
     */
    private Integer resubmissionsNumber;

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
     * 售后时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date orderLastReturnTime;

    /**
     * 订单商品名
     */
    private String orderGoodsName;

    /**
     * 唤起 0 不可唤起 1可以唤起
     */
    private Integer callStatus;

    /**
     * 售后红包金额
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
     * 退货凭证
     */
    private List<String> returnVouchers;

    /**
     * 收货地址
     */
    private String returnAddrName;

    /**
     * 商家填写收货地址
     */
    private String returnAddr;

    /**
     * 商家收货人
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


    /** 是否为自营店铺 */
    private String shopSelfSupport ;

    /**
     * 仓库id
     */
    private Integer storageId;

    /**
     * 1.线上支付  2.线下支付 3.货到付款 4.账期支付
     */
    private Integer paymentCode;

}
