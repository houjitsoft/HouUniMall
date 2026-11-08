package com.houjiang.userms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 企业用户信息表
 * @TableName user_business_info
 * @author Amos
 */
@TableName(value ="hou_user_business_info")
@Data
public class UserBusinessInfoEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 公司税号
     */
    private String taxId;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 营业执照
     */
    private String businessLicense;

    /**
     * 银行卡号
     */
    private String bankCardNo;

    /**
     * 开户行
     */
    private String registerBank;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String contactNumber;

    /**
     * 公司地址_省
     */
    private String companyProvinceId;

    /**
     * 公司地址_市
     */
    private String companyCityId;

    /**
     * 公司地址_区
     */
    private String companyDistrictId;

    /**
     * 公司地址_详细地址
     */
    private String companyDetailAddress;

    /**
     * 邮箱地址
     */
    private String mailAddress;

    /**
     * 关联用户ID
     */
    private String userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 上次更新时间
     */
    private Date lastUpdateTime;

    /**
     * 审核状态:0-待审核,1-审核通过,2-审核不通过
     */
    private Integer isApproved;

    /**
     * 审核备注
     */
    private String auditComment;

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
        UserBusinessInfoEntity other = (UserBusinessInfoEntity) that;
        return (this.getTaxId() == null ? other.getTaxId() == null : this.getTaxId().equals(other.getTaxId()))
            && (this.getCompanyName() == null ? other.getCompanyName() == null : this.getCompanyName().equals(other.getCompanyName()))
            && (this.getBusinessLicense() == null ? other.getBusinessLicense() == null : this.getBusinessLicense().equals(other.getBusinessLicense()))
            && (this.getBankCardNo() == null ? other.getBankCardNo() == null : this.getBankCardNo().equals(other.getBankCardNo()))
            && (this.getRegisterBank() == null ? other.getRegisterBank() == null : this.getRegisterBank().equals(other.getRegisterBank()))
            && (this.getContactName() == null ? other.getContactName() == null : this.getContactName().equals(other.getContactName()))
            && (this.getContactNumber() == null ? other.getContactNumber() == null : this.getContactNumber().equals(other.getContactNumber()))
            && (this.getCompanyProvinceId() == null ? other.getCompanyProvinceId() == null : this.getCompanyProvinceId().equals(other.getCompanyProvinceId()))
            && (this.getCompanyCityId() == null ? other.getCompanyCityId() == null : this.getCompanyCityId().equals(other.getCompanyCityId()))
            && (this.getCompanyDistrictId() == null ? other.getCompanyDistrictId() == null : this.getCompanyDistrictId().equals(other.getCompanyDistrictId()))
            && (this.getCompanyDetailAddress() == null ? other.getCompanyDetailAddress() == null : this.getCompanyDetailAddress().equals(other.getCompanyDetailAddress()))
            && (this.getMailAddress() == null ? other.getMailAddress() == null : this.getMailAddress().equals(other.getMailAddress()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getLastUpdateTime() == null ? other.getLastUpdateTime() == null : this.getLastUpdateTime().equals(other.getLastUpdateTime()))
            && (this.getIsApproved() == null ? other.getIsApproved() == null : this.getIsApproved().equals(other.getIsApproved()))
            && (this.getAuditComment() == null ? other.getAuditComment() == null : this.getAuditComment().equals(other.getAuditComment()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getTaxId() == null) ? 0 : getTaxId().hashCode());
        result = prime * result + ((getCompanyName() == null) ? 0 : getCompanyName().hashCode());
        result = prime * result + ((getBusinessLicense() == null) ? 0 : getBusinessLicense().hashCode());
        result = prime * result + ((getBankCardNo() == null) ? 0 : getBankCardNo().hashCode());
        result = prime * result + ((getRegisterBank() == null) ? 0 : getRegisterBank().hashCode());
        result = prime * result + ((getContactName() == null) ? 0 : getContactName().hashCode());
        result = prime * result + ((getContactNumber() == null) ? 0 : getContactNumber().hashCode());
        result = prime * result + ((getCompanyProvinceId() == null) ? 0 : getCompanyProvinceId().hashCode());
        result = prime * result + ((getCompanyCityId() == null) ? 0 : getCompanyCityId().hashCode());
        result = prime * result + ((getCompanyDistrictId() == null) ? 0 : getCompanyDistrictId().hashCode());
        result = prime * result + ((getCompanyDetailAddress() == null) ? 0 : getCompanyDetailAddress().hashCode());
        result = prime * result + ((getMailAddress() == null) ? 0 : getMailAddress().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getLastUpdateTime() == null) ? 0 : getLastUpdateTime().hashCode());
        result = prime * result + ((getIsApproved() == null) ? 0 : getIsApproved().hashCode());
        result = prime * result + ((getAuditComment() == null) ? 0 : getAuditComment().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", taxId=").append(taxId);
        sb.append(", companyName=").append(companyName);
        sb.append(", businessLicense=").append(businessLicense);
        sb.append(", bankCardNo=").append(bankCardNo);
        sb.append(", registerBank=").append(registerBank);
        sb.append(", contactName=").append(contactName);
        sb.append(", contactNumber=").append(contactNumber);
        sb.append(", companyProvinceId=").append(companyProvinceId);
        sb.append(", companyCityId=").append(companyCityId);
        sb.append(", companyDistrictId=").append(companyDistrictId);
        sb.append(", companyDetailAddress=").append(companyDetailAddress);
        sb.append(", mailAddress=").append(mailAddress);
        sb.append(", userId=").append(userId);
        sb.append(", createTime=").append(createTime);
        sb.append(", lastUpdateTime=").append(lastUpdateTime);
        sb.append(", isApproved=").append(isApproved);
        sb.append(", auditComment=").append(auditComment);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}