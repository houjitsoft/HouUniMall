package com.houjiang.order.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
 /**
 * @ClassName
 * @Description  订单发票表
 * @Author Amos
 * @Date 2023-05-31 11:59:38
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_order_invoice_info")
@Data
public class OrderInvoiceInfoEntity  implements Serializable {
    /**
     * 发票ID
     */
    @TableId(type = IdType.AUTO)
    private Integer invoiceId;
    /**
     * 订单ID
     */
    private String orderId;
    /**
     * 发票内容:0-商品明细;1-商品类别
     */
    private Integer invoiceContent;
    /**
     * 商家审核备注
     */
    private String auditComment;
    /**
     * 申请时间
     */
    private Date createTime;
    /**
     * 开票时间
     */
    private Date finishTime;
    /**
     * 发票金额
     */
    private Double invoiceAmount;
    /**
     * 开票状态:0-未开票;1-审核中;2-已出票;3-未通过
     */
    private Integer invoiceStatus;
    /**
     * 上次更新时间
     */
    private Date lastUpdateTime;
    /**
     * 收票人ID
     */
    private String userId;
    /**
     * 收票人姓名
     */
    private String userName;
    /**
     * 收票人手机号
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
     * 发票类型: 0-普票；1-增值税专用票
     */
    private Integer invoiceType;
    /**
     * 抬头类型:1-个人；2-单位
     */
    private Integer invoiceHeadType;
    /**
     * 发票抬头
     */
    private String invoiceHead;
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
     * 发票图片
     */
    private String invoicePic;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
