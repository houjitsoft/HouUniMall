package com.houjiang.commons.dto.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName AssetInfoEntity
 * @Description
 * @Author Amos
 * @Date 2022/8/12 17:17
 * @Version 1.0
 * @author Amos
 */
@Data
public class AssetTmpInfoDTO {

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 订单支付金额
     */
    private BigDecimal orderPaymentAmount;

    /**
     * 订单编号
     */
    private String orderId;

    /**
     * 是否需要审批
     */
    private Integer isApproval;

    /**
     * 订单结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderEndTime;

    /**
     * 虚拟订单 0 是 1 否
     */
    private Integer virtualGoods;

    /**
     * 虚拟订单有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date periodOfValidity;

    /**
     * 过期退款 0 是 1 否
     */
    private Integer pastDue;

    /**
     * 是否是门店自提订单 1是 2否
     */
    private Integer orderIsShopTake;

    /**
     * 支付单号
     */
    private String paymentNumber;

    private Integer shopId;




    /**
     * 嘉币支付的现金部分
     */
    private BigDecimal orderCashPayment;


    /**
     * 外部
     */
    private Integer outShopId;


    /**
     * 1-用家，2-批发
     */
    private Integer customerType;
}
