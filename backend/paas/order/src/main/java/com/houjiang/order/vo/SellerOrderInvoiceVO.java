package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @ClassName SellerOrderInvoiceVO
 * @Description 卖家订单发票信息
 * @Author Amos
 * @Date 2023/7/5 13:20
 * @Version 1.0
 * @author Amos
 */
@Data
public class SellerOrderInvoiceVO {
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 店铺id
     */
    private Integer shopId;
    /**
     * 买家姓名
     */
    private String buyerUserName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /**
     * 订单的创建时间。
     * 用于记录订单的生成时间点，以便于后续的查询和统计分析。
     */
    private Date orderCreateTime;

    /**
     * 订单商品详情列表。
     * 该列表包含了订单中的所有商品细节，每个元素都是一个OrderGoodsDetailVO对象。
     * 通过这个列表，可以详细了解到订单中包含了哪些商品，以及每个商品的具体信息。
     */
    private List<OrderGoodsDetailVO> goodsList;

    /**
     * 开票状态:0-未开票;1-审核中;2-已出票;3-未通过
     */
    private Integer invoiceStatus;
    /**
     * 发票ID
     */
    private Integer invoiceId;
    /**
     * 应付金额（商品实际支付金额 + 运费）
     */
    private BigDecimal orderPaymentAmount;
    /**
     * 发票类型
     */
    private Integer invoiceType;
    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 可开票金额
     */
    private BigDecimal invoicableAmount;

    /**
     * 买家邮箱
     */
    private String userMail;
}
