package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName OrderInvoiceDetailEntity
 * @Description 平台发票详情实体类
 * @Author Amos
 * @Date 2022/7/12 09:32
 * @Version 1.0
 * @author Amos
 */
@Data
public class PlatformOrderInvoiceDetailVO {
    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 订单创建时间，格式为"yyyy-MM-dd HH:mm:ss"，时区为"GMT+8"
     * 使用@JsonFormat注解在序列化时将java.util.Date转换为字符串，使用@DateTimeFormat注解在反序列化时将字符串转换为java.util.Date
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date orderCreateTime;

    /**
     * 发票类型
     */
    private Integer invoiceType;

    /**
     * 发票状态
     */
    private Integer invoiceStatus;

    /**
     * 发票内容
     */
    private Integer invoiceContent;

    /**
     * 发票抬头类型
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
     * 银行账号
     */
    private String bankAccount;

    /**
     * 订单发票地址信息
     */
    private OrderInvoiceAddressVO address;

    /**
     * 发票金额，使用BigDecimal类型存储以提供高精度计算
     */
    private BigDecimal invoiceAmount;

    /**
     * 审核备注
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
     * 地址所在省份ID
     */
    private Integer addressProvince;

    /**
     * 地址所在城市ID
     */
    private Integer addressCity;

    /**
     * 地址所在地区ID
     */
    private Integer addressArea;

    /**
     * 地址详细信息
     */
    private String addressDetail;

    /**
     * 用户第三方用户名
     */
    private String userUserName;

    /**
     * 完成时间，格式为"yyyy-MM-dd HH:mm:ss"，时区为"GMT+8"
     * 使用@JsonFormat注解在序列化时将java.util.Date转换为字符串，使用@DateTimeFormat注解在反序列化时将字符串转换为java.util.Date
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    /**
     * 创建时间，格式为"yyyy-MM-dd HH:mm:ss"，时区为"GMT+8"
     * 使用@JsonFormat注解在序列化时将java.util.Date转换为字符串，使用@DateTimeFormat注解在反序列化时将字符串转换为java.util.Date
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 店铺名称
     */
    private String shopName;


}
