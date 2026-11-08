package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 发票模版管理表
 * @TableName order_invoice_template_info
 * @author Amos
 */
@TableName(value ="hou_order_invoice_template_info")
@Data
public class BaseInvoiceTemplateInfoEntity implements Serializable {
    /**
     * 
     */
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

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        BaseInvoiceTemplateInfoEntity other = (BaseInvoiceTemplateInfoEntity) that;
        return (this.getInvoiceTmpId() == null ? other.getInvoiceTmpId() == null : this.getInvoiceTmpId().equals(other.getInvoiceTmpId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getInvoiceType() == null ? other.getInvoiceType() == null : this.getInvoiceType().equals(other.getInvoiceType()))
            && (this.getInvoiceHeadType() == null ? other.getInvoiceHeadType() == null : this.getInvoiceHeadType().equals(other.getInvoiceHeadType()))
            && (this.getInvoiceHead() == null ? other.getInvoiceHead() == null : this.getInvoiceHead().equals(other.getInvoiceHead()))
            && (this.getCompanyTaxId() == null ? other.getCompanyTaxId() == null : this.getCompanyTaxId().equals(other.getCompanyTaxId()))
            && (this.getRegisterAddress() == null ? other.getRegisterAddress() == null : this.getRegisterAddress().equals(other.getRegisterAddress()))
            && (this.getRegisterNumber() == null ? other.getRegisterNumber() == null : this.getRegisterNumber().equals(other.getRegisterNumber()))
            && (this.getOpeningBank() == null ? other.getOpeningBank() == null : this.getOpeningBank().equals(other.getOpeningBank()))
            && (this.getBankAccount() == null ? other.getBankAccount() == null : this.getBankAccount().equals(other.getBankAccount()))
            && (this.getIsDefault() == null ? other.getIsDefault() == null : this.getIsDefault().equals(other.getIsDefault()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getLastUpdateTime() == null ? other.getLastUpdateTime() == null : this.getLastUpdateTime().equals(other.getLastUpdateTime()))
            && (this.getInvoiceContent() == null ? other.getInvoiceContent() == null : this.getInvoiceContent().equals(other.getInvoiceContent()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getInvoiceTmpId() == null) ? 0 : getInvoiceTmpId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getInvoiceType() == null) ? 0 : getInvoiceType().hashCode());
        result = prime * result + ((getInvoiceHeadType() == null) ? 0 : getInvoiceHeadType().hashCode());
        result = prime * result + ((getInvoiceHead() == null) ? 0 : getInvoiceHead().hashCode());
        result = prime * result + ((getCompanyTaxId() == null) ? 0 : getCompanyTaxId().hashCode());
        result = prime * result + ((getRegisterAddress() == null) ? 0 : getRegisterAddress().hashCode());
        result = prime * result + ((getRegisterNumber() == null) ? 0 : getRegisterNumber().hashCode());
        result = prime * result + ((getOpeningBank() == null) ? 0 : getOpeningBank().hashCode());
        result = prime * result + ((getBankAccount() == null) ? 0 : getBankAccount().hashCode());
        result = prime * result + ((getIsDefault() == null) ? 0 : getIsDefault().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getLastUpdateTime() == null) ? 0 : getLastUpdateTime().hashCode());
        result = prime * result + ((getInvoiceContent() == null) ? 0 : getInvoiceContent().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", invoiceTmpId=").append(invoiceTmpId);
        sb.append(", userId=").append(userId);
        sb.append(", invoiceType=").append(invoiceType);
        sb.append(", invoiceHeadType=").append(invoiceHeadType);
        sb.append(", invoiceHead=").append(invoiceHead);
        sb.append(", companyTaxId=").append(companyTaxId);
        sb.append(", registerAddress=").append(registerAddress);
        sb.append(", registerNumber=").append(registerNumber);
        sb.append(", openingBank=").append(openingBank);
        sb.append(", bankAccount=").append(bankAccount);
        sb.append(", isDefault=").append(isDefault);
        sb.append(", createTime=").append(createTime);
        sb.append(", lastUpdateTime=").append(lastUpdateTime);
        sb.append(", invoiceContent=").append(invoiceContent);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}