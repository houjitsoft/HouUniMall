package com.houjiang.order.entity;



import com.baomidou.mybatisplus.annotation.TableField;

import com.baomidou.mybatisplus.annotation.TableId;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;



import java.io.Serializable;

import java.util.Date;



/**

 * @TableName order_approval

 * @author Amos

 */

@TableName(value ="hou_order_approval")

@Data

public class OrderApprovalEntity implements Serializable {

    /**

     * 订单审批编号

     */

    @TableId

    private String orderApprovalId;



    /**

     * 订单id

     */

    private String orderId;



    /**

     * 发起人

     */

    private String orderSponsor;



    /**

     * 所属企业id

     */

    private Integer businessId;



    /**

     * 审批方式(1 依次审批，2 或签，3 会签)

     */

    private Integer approvalMethod;



    /**

     * 审批状态 0审批中 1审批成功 2已驳回 3已超时

     */

    private Integer approvalStatus;



    /**

     * 审批人1

     */

    private String approverOne;



    /**

     * 审批人备注1

     */

    private String approverRemarksOne;



    /**

     * 是否审批1

     */

    private Integer isApprovalOne;



    /**

     * 审批人1审批时间

     */

    private Date approvalTimeOne;



    /**

     * 审批人2

     */

    private String approverTwo;



    /**

     * 审批人备注2

     */

    private String approverRemarksTwo;



    /**

     * 是否审批2

     */

    private Integer isApprovalTwo;



    /**

     * 审批人2审批时间

     */

    private Date approvalTimeTwo;



    /**

     * 审批人3

     */

    private String approverThree;



    /**

     * 审批人备注3

     */

    private String approverRemarksThree;



    /**

     * 是否审批3

     */

    private Integer isApprovalThree;



    /**

     * 审批人3审批时间

     */

    private Date approvalTimeThree;



    /**

     * 申请时间

     */

    private Date applyTime;



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

        OrderApprovalEntity other = (OrderApprovalEntity) that;

        return (this.getOrderApprovalId() == null ? other.getOrderApprovalId() == null : this.getOrderApprovalId().equals(other.getOrderApprovalId()))

            && (this.getOrderId() == null ? other.getOrderId() == null : this.getOrderId().equals(other.getOrderId()))

            && (this.getOrderSponsor() == null ? other.getOrderSponsor() == null : this.getOrderSponsor().equals(other.getOrderSponsor()))

            && (this.getBusinessId() == null ? other.getBusinessId() == null : this.getBusinessId().equals(other.getBusinessId()))

            && (this.getApprovalMethod() == null ? other.getApprovalMethod() == null : this.getApprovalMethod().equals(other.getApprovalMethod()))

            && (this.getApprovalStatus() == null ? other.getApprovalStatus() == null : this.getApprovalStatus().equals(other.getApprovalStatus()))

            && (this.getApproverOne() == null ? other.getApproverOne() == null : this.getApproverOne().equals(other.getApproverOne()))

            && (this.getApproverRemarksOne() == null ? other.getApproverRemarksOne() == null : this.getApproverRemarksOne().equals(other.getApproverRemarksOne()))

            && (this.getIsApprovalOne() == null ? other.getIsApprovalOne() == null : this.getIsApprovalOne().equals(other.getIsApprovalOne()))

            && (this.getApprovalTimeOne() == null ? other.getApprovalTimeOne() == null : this.getApprovalTimeOne().equals(other.getApprovalTimeOne()))

            && (this.getApproverTwo() == null ? other.getApproverTwo() == null : this.getApproverTwo().equals(other.getApproverTwo()))

            && (this.getApproverRemarksTwo() == null ? other.getApproverRemarksTwo() == null : this.getApproverRemarksTwo().equals(other.getApproverRemarksTwo()))

            && (this.getIsApprovalTwo() == null ? other.getIsApprovalTwo() == null : this.getIsApprovalTwo().equals(other.getIsApprovalTwo()))

            && (this.getApprovalTimeTwo() == null ? other.getApprovalTimeTwo() == null : this.getApprovalTimeTwo().equals(other.getApprovalTimeTwo()))

            && (this.getApproverThree() == null ? other.getApproverThree() == null : this.getApproverThree().equals(other.getApproverThree()))

            && (this.getApproverRemarksThree() == null ? other.getApproverRemarksThree() == null : this.getApproverRemarksThree().equals(other.getApproverRemarksThree()))

            && (this.getIsApprovalThree() == null ? other.getIsApprovalThree() == null : this.getIsApprovalThree().equals(other.getIsApprovalThree()))

            && (this.getApprovalTimeThree() == null ? other.getApprovalTimeThree() == null : this.getApprovalTimeThree().equals(other.getApprovalTimeThree()))

            && (this.getApplyTime() == null ? other.getApplyTime() == null : this.getApplyTime().equals(other.getApplyTime()));

    }



    @Override

    public int hashCode() {

        final int prime = 31;

        int result = 1;

        result = prime * result + ((getOrderApprovalId() == null) ? 0 : getOrderApprovalId().hashCode());

        result = prime * result + ((getOrderId() == null) ? 0 : getOrderId().hashCode());

        result = prime * result + ((getOrderSponsor() == null) ? 0 : getOrderSponsor().hashCode());

        result = prime * result + ((getBusinessId() == null) ? 0 : getBusinessId().hashCode());

        result = prime * result + ((getApprovalMethod() == null) ? 0 : getApprovalMethod().hashCode());

        result = prime * result + ((getApprovalStatus() == null) ? 0 : getApprovalStatus().hashCode());

        result = prime * result + ((getApproverOne() == null) ? 0 : getApproverOne().hashCode());

        result = prime * result + ((getApproverRemarksOne() == null) ? 0 : getApproverRemarksOne().hashCode());

        result = prime * result + ((getIsApprovalOne() == null) ? 0 : getIsApprovalOne().hashCode());

        result = prime * result + ((getApprovalTimeOne() == null) ? 0 : getApprovalTimeOne().hashCode());

        result = prime * result + ((getApproverTwo() == null) ? 0 : getApproverTwo().hashCode());

        result = prime * result + ((getApproverRemarksTwo() == null) ? 0 : getApproverRemarksTwo().hashCode());

        result = prime * result + ((getIsApprovalTwo() == null) ? 0 : getIsApprovalTwo().hashCode());

        result = prime * result + ((getApprovalTimeTwo() == null) ? 0 : getApprovalTimeTwo().hashCode());

        result = prime * result + ((getApproverThree() == null) ? 0 : getApproverThree().hashCode());

        result = prime * result + ((getApproverRemarksThree() == null) ? 0 : getApproverRemarksThree().hashCode());

        result = prime * result + ((getIsApprovalThree() == null) ? 0 : getIsApprovalThree().hashCode());

        result = prime * result + ((getApprovalTimeThree() == null) ? 0 : getApprovalTimeThree().hashCode());

        result = prime * result + ((getApplyTime() == null) ? 0 : getApplyTime().hashCode());

        return result;

    }



    @Override

    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(getClass().getSimpleName());

        sb.append(" [");

        sb.append("Hash = ").append(hashCode());

        sb.append(", orderApprovalId=").append(orderApprovalId);

        sb.append(", orderId=").append(orderId);

        sb.append(", orderSponsor=").append(orderSponsor);

        sb.append(", businessId=").append(businessId);

        sb.append(", approvalMethod=").append(approvalMethod);

        sb.append(", approvalStatus=").append(approvalStatus);

        sb.append(", approverOne=").append(approverOne);

        sb.append(", approverRemarksOne=").append(approverRemarksOne);

        sb.append(", isApprovalOne=").append(isApprovalOne);

        sb.append(", approvalTimeOne=").append(approvalTimeOne);

        sb.append(", approverTwo=").append(approverTwo);

        sb.append(", approverRemarksTwo=").append(approverRemarksTwo);

        sb.append(", isApprovalTwo=").append(isApprovalTwo);

        sb.append(", approvalTimeTwo=").append(approvalTimeTwo);

        sb.append(", approverThree=").append(approverThree);

        sb.append(", approverRemarksThree=").append(approverRemarksThree);

        sb.append(", isApprovalThree=").append(isApprovalThree);

        sb.append(", approvalTimeThree=").append(approvalTimeThree);

        sb.append(", applyTime=").append(applyTime);

        sb.append(", serialVersionUID=").append(serialVersionUID);

        sb.append("]");

        return sb.toString();

    }

}