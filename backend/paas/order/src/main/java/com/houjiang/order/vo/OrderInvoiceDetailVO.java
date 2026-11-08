package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName OrderInvoiceDetailEntity
 * @Description 发票详情实体类
 * @Author Amos
 * @Date 2022/7/12 09:32
 * @Version 1.0
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderInvoiceDetailVO {

    /**
     * 订单状态 1.待付款 2.已付款 3.待发货 4.已发货 5.已签收 6.确认收货 7.取消 8.退款中 9.已退款 11.待自提
     * 12 虚拟订单已过期
     */
    private Integer orderStatus;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 下单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderCreateTime;

    /**
     * 发票类型: 0-普票；1-增值税专用票
     */
    private Integer invoiceType;

    /**
     * 开票状态:0-未开票;1-审核中;2-已出票;3-未通过
     */
    private Integer invoiceStatus;

    /**
     * 发票内容
     */
    private Integer invoiceContent;

    /**
     * 抬头类型:1-个人；2-单位
     */
    private Integer invoiceHeadType;

    /**
     * 发票抬头
     */
    private String invoiceHead;

    /**
     * 商家审核备注
     */
    private String auditComment;

    /**
     * 发票ID
     */
    private Integer invoiceId;

    /**
     * 单位税号
     */
    private String companyTaxId;

    /**
     * 注册地址
     */
    private String registerAddress;

    /**
     * 注册电话
     */
    private String registerNumber;

    /**
     * 开户银行
     */
    private String openingBank;

    /**
     * 银行账号
     */
    private String bankAccount;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户联系方式
     */
    private String userMobile;

    /**
     * 收票人邮箱
     */
    private String userMail;

    /**
     * 收票地址-省
     */
    private Integer addressProvince;

    /**
     * 收票地址-市
     */
    private Integer addressCity;

    /**
     * 收票地址-区
     */
    private Integer addressArea;

    /**
     * 详细地址
     */
    private String addressDetail;

    /**
     * 公司地址
     */
    private String completeAddress;

    /**
     * 发票金额
     */
    private BigDecimal invoiceAmount;

    /**
     * 预完成公司地址
     */
    private String preCompleteAddress;

    /**
     * 图片
     */
    private String invoicePic;


}
