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
 * @Description  发票模版管理表
 * @Author Amos
 * @Date 2023-07-04 13:32:33
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_order_invoice_template_info")
@Data
public class OrderInvoiceTemplateInfoEntity  implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer invoiceTmpId;
    /**
     * 用户ID
     */
    private String userId;
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
     * 是否默认：1-是；0-否
     */
    private Integer isDefault;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 上次更新时间
     */
    private Date lastUpdateTime;
    /**
     * 发票内容:0-商品明细;1-商品类别
     */
    private Integer invoiceContent;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
