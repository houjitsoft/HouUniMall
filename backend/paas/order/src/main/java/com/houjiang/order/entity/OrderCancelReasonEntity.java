package com.houjiang.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单取消原因表
 * @TableName order_cancel_reason
 * @author Amos
 */
@TableName(value ="hou_order_cancel_reason")
@Data
public class OrderCancelReasonEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer cancelReasonId;

    /**
     * 取消订单的原因
     */
    private String cancelReasonContent;

    /**
     * 类型 1-退款 2-退货
     */
    private Boolean cancelIdentity;

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
        OrderCancelReasonEntity other = (OrderCancelReasonEntity) that;
        return (this.getCancelReasonId() == null ? other.getCancelReasonId() == null : this.getCancelReasonId().equals(other.getCancelReasonId()))
            && (this.getCancelReasonContent() == null ? other.getCancelReasonContent() == null : this.getCancelReasonContent().equals(other.getCancelReasonContent()))
            && (this.getCancelIdentity() == null ? other.getCancelIdentity() == null : this.getCancelIdentity().equals(other.getCancelIdentity()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCancelReasonId() == null) ? 0 : getCancelReasonId().hashCode());
        result = prime * result + ((getCancelReasonContent() == null) ? 0 : getCancelReasonContent().hashCode());
        result = prime * result + ((getCancelIdentity() == null) ? 0 : getCancelIdentity().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", cancelReasonId=").append(cancelReasonId);
        sb.append(", cancelReasonContent=").append(cancelReasonContent);
        sb.append(", cancelIdentity=").append(cancelIdentity);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}