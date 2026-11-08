package com.houjiang.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 平台介入表
 * @TableName order_return_platform
 * @author Amos
 */
@TableName("hou_order_return_platform")
@Data
public class OrderReturnPlatformEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer platformInterventionId;

    /**
     * 订单编号
     */
    private String orderId;

    /**
     * 售后单号
     */
    private String returnCode;

    /**
     * 店铺名
     */
    private String shopName;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 平台介入状态 0:待卖家上传凭证  1:待平台审核 2 已处理  3 买家撤销
     */
    private Integer interventionState;

    /**
     * 申请时间
     */
    private Date applicationTime;

    /**
     * 买家id
     */
    private String buyerUserId;

    /**
     * 卖家id(店铺id)
     */
    private String sellerUserId;

    /**
     * 售后状态  卖家审核不通过
     */
    private Integer returnState;

    /**
     * 卖家凭证
     */
    private String sellerVoucher;

    /**
     * 买家凭证
     */
    private String buyerVoucher;

    /**
     * 买家备注说明
     */
    private String buyerRemarks;

    /**
     * 卖家备注说明
     */
    private String sellerRemarks;

    /**
     * 平台处理结果 1 买家胜利  0卖家胜利 2 用户撤销
     */
    private Integer processingResults;

    /**
     * 平台处理时间
     */
    private Date processingTime;

    /**
     * 失效时间
     */
    private Date expriationTime;

    /**
     * 1.退款,2.仅退款,3退货退款
     */
    private Integer returnMethodType;

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
        OrderReturnPlatformEntity other = (OrderReturnPlatformEntity) that;
        return (this.getPlatformInterventionId() == null ? other.getPlatformInterventionId() == null : this.getPlatformInterventionId().equals(other.getPlatformInterventionId()))
            && (this.getOrderId() == null ? other.getOrderId() == null : this.getOrderId().equals(other.getOrderId()))
            && (this.getReturnCode() == null ? other.getReturnCode() == null : this.getReturnCode().equals(other.getReturnCode()))
            && (this.getShopName() == null ? other.getShopName() == null : this.getShopName().equals(other.getShopName()))
            && (this.getShopId() == null ? other.getShopId() == null : this.getShopId().equals(other.getShopId()))
            && (this.getInterventionState() == null ? other.getInterventionState() == null : this.getInterventionState().equals(other.getInterventionState()))
            && (this.getApplicationTime() == null ? other.getApplicationTime() == null : this.getApplicationTime().equals(other.getApplicationTime()))
            && (this.getBuyerUserId() == null ? other.getBuyerUserId() == null : this.getBuyerUserId().equals(other.getBuyerUserId()))
            && (this.getSellerUserId() == null ? other.getSellerUserId() == null : this.getSellerUserId().equals(other.getSellerUserId()))
            && (this.getReturnState() == null ? other.getReturnState() == null : this.getReturnState().equals(other.getReturnState()))
            && (this.getSellerVoucher() == null ? other.getSellerVoucher() == null : this.getSellerVoucher().equals(other.getSellerVoucher()))
            && (this.getBuyerVoucher() == null ? other.getBuyerVoucher() == null : this.getBuyerVoucher().equals(other.getBuyerVoucher()))
            && (this.getBuyerRemarks() == null ? other.getBuyerRemarks() == null : this.getBuyerRemarks().equals(other.getBuyerRemarks()))
            && (this.getSellerRemarks() == null ? other.getSellerRemarks() == null : this.getSellerRemarks().equals(other.getSellerRemarks()))
            && (this.getProcessingResults() == null ? other.getProcessingResults() == null : this.getProcessingResults().equals(other.getProcessingResults()))
            && (this.getProcessingTime() == null ? other.getProcessingTime() == null : this.getProcessingTime().equals(other.getProcessingTime()))
            && (this.getExpriationTime() == null ? other.getExpriationTime() == null : this.getExpriationTime().equals(other.getExpriationTime()))
            && (this.getReturnMethodType() == null ? other.getReturnMethodType() == null : this.getReturnMethodType().equals(other.getReturnMethodType()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getPlatformInterventionId() == null) ? 0 : getPlatformInterventionId().hashCode());
        result = prime * result + ((getOrderId() == null) ? 0 : getOrderId().hashCode());
        result = prime * result + ((getReturnCode() == null) ? 0 : getReturnCode().hashCode());
        result = prime * result + ((getShopName() == null) ? 0 : getShopName().hashCode());
        result = prime * result + ((getShopId() == null) ? 0 : getShopId().hashCode());
        result = prime * result + ((getInterventionState() == null) ? 0 : getInterventionState().hashCode());
        result = prime * result + ((getApplicationTime() == null) ? 0 : getApplicationTime().hashCode());
        result = prime * result + ((getBuyerUserId() == null) ? 0 : getBuyerUserId().hashCode());
        result = prime * result + ((getSellerUserId() == null) ? 0 : getSellerUserId().hashCode());
        result = prime * result + ((getReturnState() == null) ? 0 : getReturnState().hashCode());
        result = prime * result + ((getSellerVoucher() == null) ? 0 : getSellerVoucher().hashCode());
        result = prime * result + ((getBuyerVoucher() == null) ? 0 : getBuyerVoucher().hashCode());
        result = prime * result + ((getBuyerRemarks() == null) ? 0 : getBuyerRemarks().hashCode());
        result = prime * result + ((getSellerRemarks() == null) ? 0 : getSellerRemarks().hashCode());
        result = prime * result + ((getProcessingResults() == null) ? 0 : getProcessingResults().hashCode());
        result = prime * result + ((getProcessingTime() == null) ? 0 : getProcessingTime().hashCode());
        result = prime * result + ((getExpriationTime() == null) ? 0 : getExpriationTime().hashCode());
        result = prime * result + ((getReturnMethodType() == null) ? 0 : getReturnMethodType().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", platformInterventionId=").append(platformInterventionId);
        sb.append(", orderId=").append(orderId);
        sb.append(", returnCode=").append(returnCode);
        sb.append(", shopName=").append(shopName);
        sb.append(", shopId=").append(shopId);
        sb.append(", interventionState=").append(interventionState);
        sb.append(", applicationTime=").append(applicationTime);
        sb.append(", buyerUserId=").append(buyerUserId);
        sb.append(", sellerUserId=").append(sellerUserId);
        sb.append(", returnState=").append(returnState);
        sb.append(", sellerVoucher=").append(sellerVoucher);
        sb.append(", buyerVoucher=").append(buyerVoucher);
        sb.append(", buyerRemarks=").append(buyerRemarks);
        sb.append(", sellerRemarks=").append(sellerRemarks);
        sb.append(", processingResults=").append(processingResults);
        sb.append(", processingTime=").append(processingTime);
        sb.append(", expriationTime=").append(expriationTime);
        sb.append(", returnMethodType=").append(returnMethodType);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}