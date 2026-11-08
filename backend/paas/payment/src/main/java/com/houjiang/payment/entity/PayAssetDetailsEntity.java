package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @TableName ${database.prefix.payment}hou_pay_asset_details
 * @author Amos
 */
@TableName(value ="hou_pay_asset_details")
@Data
public class PayAssetDetailsEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer detailId;

    /**
     * 明细类型:0-默认;1-余额充值;2-订单退款;3-提现失败;4-余额提现;5-余额转账;6-购物消费;7-购物卡充值;8-余额扣款
     */
    private Integer detailType;

    /**
     * 资产类型:1-收入；2-支出
     */
    private Integer assetType;

    /**
     * 金额
     */
    private BigDecimal assetAmount;

    /**
     * 来源/去向
     */
    private String assetDirection;

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 上次更新时间
     */
    private Date lastUpdateTime;

    /**
     * 支付类型:1-余额；2-购物卡
     */
    private Integer payType;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 备注
     */
    private String detailComment;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private Integer orderStatus;

    /**
     * 1-实物；2-虚拟；3-门店
     */
    @TableField(exist = false)
    private Integer orderType;

    @TableField(exist = false)
    private Integer pastDue;

    @TableField(exist = false)
    private Integer isCancel;

    @TableField(exist = false)
    private Integer returnStatus;

    @TableField(exist = false)
    private Integer refundStatus;

    @TableField(exist = false)
    private Integer returnFlag;

    /**
     *0-待审核,1-进行中,2-打款完成,3-通过,4-不通过
     */
    @TableField(exist = false)
    private Integer auditStatus;

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
        PayAssetDetailsEntity other = (PayAssetDetailsEntity) that;
        return (this.getDetailId() == null ? other.getDetailId() == null : this.getDetailId().equals(other.getDetailId()))
            && (this.getDetailType() == null ? other.getDetailType() == null : this.getDetailType().equals(other.getDetailType()))
            && (this.getAssetType() == null ? other.getAssetType() == null : this.getAssetType().equals(other.getAssetType()))
            && (this.getAssetAmount() == null ? other.getAssetAmount() == null : this.getAssetAmount().equals(other.getAssetAmount()))
            && (this.getAssetDirection() == null ? other.getAssetDirection() == null : this.getAssetDirection().equals(other.getAssetDirection()))
            && (this.getOrderId() == null ? other.getOrderId() == null : this.getOrderId().equals(other.getOrderId()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getLastUpdateTime() == null ? other.getLastUpdateTime() == null : this.getLastUpdateTime().equals(other.getLastUpdateTime()))
            && (this.getPayType() == null ? other.getPayType() == null : this.getPayType().equals(other.getPayType()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getDetailComment() == null ? other.getDetailComment() == null : this.getDetailComment().equals(other.getDetailComment()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getDetailId() == null) ? 0 : getDetailId().hashCode());
        result = prime * result + ((getDetailType() == null) ? 0 : getDetailType().hashCode());
        result = prime * result + ((getAssetType() == null) ? 0 : getAssetType().hashCode());
        result = prime * result + ((getAssetAmount() == null) ? 0 : getAssetAmount().hashCode());
        result = prime * result + ((getAssetDirection() == null) ? 0 : getAssetDirection().hashCode());
        result = prime * result + ((getOrderId() == null) ? 0 : getOrderId().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getLastUpdateTime() == null) ? 0 : getLastUpdateTime().hashCode());
        result = prime * result + ((getPayType() == null) ? 0 : getPayType().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getDetailComment() == null) ? 0 : getDetailComment().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", detailId=").append(detailId);
        sb.append(", detailType=").append(detailType);
        sb.append(", assetType=").append(assetType);
        sb.append(", assetAmount=").append(assetAmount);
        sb.append(", assetDirection=").append(assetDirection);
        sb.append(", orderId=").append(orderId);
        sb.append(", createTime=").append(createTime);
        sb.append(", lastUpdateTime=").append(lastUpdateTime);
        sb.append(", payType=").append(payType);
        sb.append(", userId=").append(userId);
        sb.append(", detailComment=").append(detailComment);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}