package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName OrderInvoiceEntity
 * @Description 卖家订单发票信息实体类
 * @Author Amos
 * @Date 2022/7/7 19:20
 * @Version 1.0
 * @author Amos
 */
@Data
public class PlatformOrderInvoiceVO {
    /**
     * 订单号
     */
    private String orderId;
    /**
     *店铺id
     */
    private Integer shopId;
    /**
     *开票店铺
     */
    private String shopName;
    /**
     *买家账号
     */
    private String buyerUserName;
    /**
     *申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date invoiceCreateTime;
    /**
     *开票时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date invoiceFinishTime;
    /**
     *发票状态
     */
    private Integer invoiceStatus;
    /**
     *发票id
     */
    private Integer invoiceId;
    /**
     *发票金额
     */
    private BigDecimal invoiceAmount;
    /**
     *发票内容
     */
    private Integer invoiceContent;
    /**
     *发票类型
     */
    private Integer invoiceType;
    /**
     *订单状态
     */
    private Integer orderStatus;
    /**
     *抬头类型
     */
    private Integer invoiceHeadType;
    /**
     *发票抬头
     */
    private String invoiceHead;

}
