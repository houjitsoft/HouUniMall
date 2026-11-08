package com.houjiang.mall.entity.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发票实体类
 * 表名: hou_mall_invoice
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_invoice")
public class Invoice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发票编号
     */
    private String invoiceNo;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 买家ID
     */
    private Long buyerId;

    /**
     * 发票类型: 1-增值税专用发票 2-增值税普通发票 3-电子发票
     */
    private Integer invoiceType;

    /**
     * 发票抬头
     */
    private String invoiceTitle;

    /**
     * 纳税人识别号
     */
    private String taxNo;

    /**
     * 发票金额
     */
    private BigDecimal invoiceAmount;

    /**
     * 税额
     */
    private BigDecimal taxAmount;

    /**
     * 价税合计
     */
    private BigDecimal totalAmount;

    /**
     * 开票内容
     */
    private String invoiceContent;

    /**
     * 公司地址
     */
    private String companyAddress;

    /**
     * 公司电话
     */
    private String companyPhone;

    /**
     * 开户银行
     */
    private String bankName;

    /**
     * 银行账号
     */
    private String bankAccount;

    /**
     * 收件人
     */
    private String receiverName;

    /**
     * 收件电话
     */
    private String receiverPhone;

    /**
     * 收件地址
     */
    private String receiverAddress;

    /**
     * 电子发票PDF
     */
    private String pdfUrl;

    /**
     * 状态: 0-待开具 1-已开具 2-已邮寄 3-已作废
     */
    private Integer status;

    /**
     * 开票时间
     */
    private LocalDateTime issueTime;

    /**
     * 邮寄时间
     */
    private LocalDateTime mailTime;

    /**
     * 作废时间
     */
    private LocalDateTime voidTime;

    /**
     * 作废原因
     */
    private String voidReason;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer deleted;
}
