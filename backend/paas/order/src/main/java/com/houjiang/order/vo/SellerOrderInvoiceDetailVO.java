package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.order.entity.OrderInvoiceAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName SellerOrderInvoiceDetailVO
 * @Description
 * @Author Amos
 * @Date 2023/8/1 10:03
 * @Version 1.0
 * @author Amos
 */
@Data
public class SellerOrderInvoiceDetailVO {

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 订单创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderCreateTime;

    /**
     * 发票类型: 0-普票；1-增值税专用票
     */
    private Integer invoiceType;

    /**
     * 发票状态
     */
    private Integer invoiceStatus;

    /**
     * 发发票内容:0-商品明细;1-商品类别
     */
    private Integer invoiceContent;

    /**
     * 发票抬头类型 抬头类型:1-个人；2-单位
     */
    private Integer invoiceHeadType;

    /**
     * 发票抬头
     */
    private String invoiceHead;

    /**
     * 发票ID
     */
    private Integer invoiceId;

    /**
     * 地址ID
     */
    private Integer addressId;

    /**
     * 公司税号
     */
    private String companyTaxId;

    /**
     * 注册地址
     */
    private String registerAddress;

    /**
     * 注册号
     */
    private String registerNumber;

    /**
     * 开户银行
     */
    private String openingBank;

    /**
     * 银行账户
     */
    private String bankAccount;

    /**
     * 发票地址实体
     */
    private OrderInvoiceAddressEntity address;

    /**
     * 发票金额
     */
    private BigDecimal invoiceAmount;

    /**
     * 审核意见
     */
    private String auditComment;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String userMobile;

    /**
     * 收票人邮箱
     */
    private String userMail;

    /**
     * 地址省份
     */
    private Integer addressProvince;

    /**
     * 地址城市
     */
    private Integer addressCity;

    /**
     * 地址区域
     */
    private Integer addressArea;

    /**
     * 地址详细信息
     */
    private String addressDetail;

    /**
     * 发票图片
     */
    private String invoicePic;

}
