package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单返回平台
 * @author Amos
 */
@Data
public class OrderReturnPlatformVO {

    /**
     * 退款退货信息对象
     */
    private ReturnOrderDetailVO returnOrderDetail;

    /**
     * 订单编号
     */
    private String orderId;

    /**
     * 售后单号
     */
    private String returnCode;

    /**
     * 店铺名
     */
    private String shopName;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 平台介入状态
     */
    private Integer interventionState;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applicationTime;

    /**
     * 买家id
     */
    private String buyerUserId;

    /**
     * 卖家id
     */
    private Integer sellerUserId;

    /**
     * 售后状态
     */
    private Integer returnState;

    /**
     * 售后类型
     */
    private Integer returnMethodType;

    /**
     * 卖家凭证
     */
    private String sellerVoucher;

    /**
     * 卖家凭证
     */
    private List sellerVouchers;

    /**
     * 买家凭证
     */
    private String buyerVoucher;

    /**
     * 买家凭证
     */
    private List buyerVouchers;

    /**
     * 买家说明
     */
    private String buyerRemarks;

    /**
     * 卖家说明
     */
    private String sellerRemarks;

    /**
     * 店铺评分
     */
    private String shopRating;

    /**
     * 处理结果
     */
    private Integer processingResults;

    /**
     * 历史维权条数
     */
    private Integer historicalCount;

    /**
     * 处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date processingTime;

    /**
     * 失效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expriationTime;

    /**
     * 退款订单详情对象
     */
    private ReturnOrderDetailPlatVO returnOrderDetailPlat;

    /**
     * 质保金余额
     */
    private BigDecimal retentionMoneyBalance;
}
