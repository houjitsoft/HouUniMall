package com.houjiang.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 退货表
 * @TableName order_return
 * @author Amos
 */
@TableName(value ="hou_order_return")
@Data
public class OrderReturnEntity implements Serializable {
    /**
     * 退货记录ID
     */
    @TableId(type = IdType.AUTO)
    private Integer orderReturnId;

    /**
     * 订单编号
     */
    private String orderId;

    /**
     * 订单总额
     */
    private BigDecimal orderAmount;

    /**
     * 退货数量
     */
    private Integer orderGoodsNum;

    /**
     * 退货编号
     */
    private String returnCode;

    /**
     * 1-退款申请 2-退货申请
     */
    private Boolean returnType;

    /**
     * 卖家ID(shopId)
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 卖家Id
     */
    private String sellerUserId;

    /**
     * 买家ID
     */
    private String buyerUserId;

    /**
     * 买家会员名
     */
    private String buyerUserAccount;

    /**
     * 添加时间
     */
    private Date returnAddTime;

    /**
     * 退款理由id
     */
    private Integer returnReasonId;

    /**
     * 退款理由
     */
    private String returnReason;

    /**
     * 退货备注
     */
    private String returnMessage;

    /**
     * 收货人
     */
    private String returnRealName;

    /**
     * 收货地址id(商家地址库id)
     */
    private Integer returnAddrId;

    /**
     * 收货地址
     */
    private String returnAddrName;

    /**
     * 收货地址详情
     */
    private String returnAddr;

    /**
     * 单号
     */
    private String returnShippingCode;

    /**
     * 物流id
     */
    private Integer returnPostCode;

    /**
     * 联系电话
     */
    private String returnTel;

    /**
     * 联系手机
     */
    private String returnMobile;

    /**
     * 0-商家拒绝 1-新发起等待卖家审核 2-卖家审核通过 3-卖家审核不通过 4-卖家收到货物 5-平台审核通过 6-买家已填物流 7-平台拒绝 8-退款关闭 9-平台介入 10验收不通过 11待买家退货 12待验收 13退货关闭
     */
    private Integer returnState;

    /**
     * 退款金额
     */
    private BigDecimal returnCash;

    /**
     * 商家处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date returnShopTime;

    /**
     * 商家备注
     */
    private String returnShopMessage;

    /**
     * 退款完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date returnFinishTime;

    /**
     * 退还佣金(暂时废弃)
     */
    private BigDecimal returnCommisionFee;

    /**
     * 分销代发货  0：不代发货 1：代发货(分销订单DD) 2：代发货（供应订单SP）(暂时废弃)
     */
    private Integer behalfDeliver;

    /**
     * 退还平台红包金额
     */
    private BigDecimal returnRptCash;

    /**
     * 商家处理结果  1-待处理  2-卖家审核通过 3-卖家审核不通过(暂废弃，状态同return_state)
     */
    private Boolean returnShopHandle;


    /**
     * 是否结算
     */
    private Boolean orderIsSettlement;

    /**
     * 结算时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderSettlementTime;

    /**
     * 结算单号
     */
    private String orderSettlementId;

    /**
     * 审核操作人
     */
    private String orderOperator;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderOperatorTime;

    /**
     * 时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date ts;

    /**
     * 供应商是否结算
     */
    private Boolean orderSupplyIsSettlement;

    /**
     * 供应商结算时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderSupplySettlementTime;

    /**
     * 供应商结算id
     */
    private String orderSupplySettlementId;

    /**
     * 退还会员折扣
     */
    private BigDecimal returnMemberDiscount;

    /**
     * 退还经销等级折扣
     */
    private BigDecimal returnSupperDiscountFee;

    /**
     * 退还运费 订单全退退运费 半退不退
     */
    private BigDecimal returnShippingFee;

    /**
     * 1.退款 2.仅退款 3退货退款
     */
    private Integer returnMethodStatus;

    /**
     * 填写物流时间
     */
    private Date fillLogisticsTime;

    /**
     * 关闭时间
     */
    private Date closeTime;

    /**
     * 用户修改次数
     */
    private Integer returnChangeNum;

    /**
     * 失效时间
     */
    private Date expirationTime;

    /**
     * 撤销次数
     */
    private Integer revokeNum;

    /**
     * 退货凭证
     */
    private String returnVoucher;

    /**
     * 0 不可唤起 1 可以唤起
     */
    private Integer callStatus;

    /**
     * 重新提交次数
     */
    private Integer resubmissionsNumber;

    /**
     * 历史状态
     */
    private String historicalReturnStatus;

    /**
     * 收货人联系电话
     */
    private String returnNumber;

    /**
     * 平台退款凭证
     */
    private String returnPaymentVoucher;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 仓库id
     */
    private Integer storageId;

    /**
     * 是否已寄回
     */
    private Integer isSendBack;

    /**
     * 寄回说明
     */
    private String sendBackNotes;

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
        OrderReturnEntity other = (OrderReturnEntity) that;
        return (this.getOrderReturnId() == null ? other.getOrderReturnId() == null : this.getOrderReturnId().equals(other.getOrderReturnId()))
            && (this.getOrderId() == null ? other.getOrderId() == null : this.getOrderId().equals(other.getOrderId()))
            && (this.getOrderAmount() == null ? other.getOrderAmount() == null : this.getOrderAmount().equals(other.getOrderAmount()))
            && (this.getOrderGoodsNum() == null ? other.getOrderGoodsNum() == null : this.getOrderGoodsNum().equals(other.getOrderGoodsNum()))
            && (this.getReturnCode() == null ? other.getReturnCode() == null : this.getReturnCode().equals(other.getReturnCode()))
            && (this.getReturnType() == null ? other.getReturnType() == null : this.getReturnType().equals(other.getReturnType()))
            && (this.getShopId() == null ? other.getShopId() == null : this.getShopId().equals(other.getShopId()))
            && (this.getShopName() == null ? other.getShopName() == null : this.getShopName().equals(other.getShopName()))
            && (this.getSellerUserId() == null ? other.getSellerUserId() == null : this.getSellerUserId().equals(other.getSellerUserId()))
            && (this.getBuyerUserId() == null ? other.getBuyerUserId() == null : this.getBuyerUserId().equals(other.getBuyerUserId()))
            && (this.getBuyerUserAccount() == null ? other.getBuyerUserAccount() == null : this.getBuyerUserAccount().equals(other.getBuyerUserAccount()))
            && (this.getReturnAddTime() == null ? other.getReturnAddTime() == null : this.getReturnAddTime().equals(other.getReturnAddTime()))
            && (this.getReturnReasonId() == null ? other.getReturnReasonId() == null : this.getReturnReasonId().equals(other.getReturnReasonId()))
            && (this.getReturnReason() == null ? other.getReturnReason() == null : this.getReturnReason().equals(other.getReturnReason()))
            && (this.getReturnMessage() == null ? other.getReturnMessage() == null : this.getReturnMessage().equals(other.getReturnMessage()))
            && (this.getReturnRealName() == null ? other.getReturnRealName() == null : this.getReturnRealName().equals(other.getReturnRealName()))
            && (this.getReturnAddrId() == null ? other.getReturnAddrId() == null : this.getReturnAddrId().equals(other.getReturnAddrId()))
            && (this.getReturnAddrName() == null ? other.getReturnAddrName() == null : this.getReturnAddrName().equals(other.getReturnAddrName()))
            && (this.getReturnAddr() == null ? other.getReturnAddr() == null : this.getReturnAddr().equals(other.getReturnAddr()))
            && (this.getReturnShippingCode() == null ? other.getReturnShippingCode() == null : this.getReturnShippingCode().equals(other.getReturnShippingCode()))
            && (this.getReturnPostCode() == null ? other.getReturnPostCode() == null : this.getReturnPostCode().equals(other.getReturnPostCode()))
            && (this.getReturnTel() == null ? other.getReturnTel() == null : this.getReturnTel().equals(other.getReturnTel()))
            && (this.getReturnMobile() == null ? other.getReturnMobile() == null : this.getReturnMobile().equals(other.getReturnMobile()))
            && (this.getReturnState() == null ? other.getReturnState() == null : this.getReturnState().equals(other.getReturnState()))
            && (this.getReturnCash() == null ? other.getReturnCash() == null : this.getReturnCash().equals(other.getReturnCash()))
            && (this.getReturnShopTime() == null ? other.getReturnShopTime() == null : this.getReturnShopTime().equals(other.getReturnShopTime()))
            && (this.getReturnShopMessage() == null ? other.getReturnShopMessage() == null : this.getReturnShopMessage().equals(other.getReturnShopMessage()))
            && (this.getReturnFinishTime() == null ? other.getReturnFinishTime() == null : this.getReturnFinishTime().equals(other.getReturnFinishTime()))
            && (this.getReturnCommisionFee() == null ? other.getReturnCommisionFee() == null : this.getReturnCommisionFee().equals(other.getReturnCommisionFee()))
            && (this.getBehalfDeliver() == null ? other.getBehalfDeliver() == null : this.getBehalfDeliver().equals(other.getBehalfDeliver()))
            && (this.getReturnRptCash() == null ? other.getReturnRptCash() == null : this.getReturnRptCash().equals(other.getReturnRptCash()))
            && (this.getReturnShopHandle() == null ? other.getReturnShopHandle() == null : this.getReturnShopHandle().equals(other.getReturnShopHandle()))
            && (this.getOrderIsSettlement() == null ? other.getOrderIsSettlement() == null : this.getOrderIsSettlement().equals(other.getOrderIsSettlement()))
            && (this.getOrderSettlementTime() == null ? other.getOrderSettlementTime() == null : this.getOrderSettlementTime().equals(other.getOrderSettlementTime()))
            && (this.getOrderSettlementId() == null ? other.getOrderSettlementId() == null : this.getOrderSettlementId().equals(other.getOrderSettlementId()))
            && (this.getOrderOperator() == null ? other.getOrderOperator() == null : this.getOrderOperator().equals(other.getOrderOperator()))
            && (this.getOrderOperatorTime() == null ? other.getOrderOperatorTime() == null : this.getOrderOperatorTime().equals(other.getOrderOperatorTime()))
            && (this.getTs() == null ? other.getTs() == null : this.getTs().equals(other.getTs()))
            && (this.getOrderSupplyIsSettlement() == null ? other.getOrderSupplyIsSettlement() == null : this.getOrderSupplyIsSettlement().equals(other.getOrderSupplyIsSettlement()))
            && (this.getOrderSupplySettlementTime() == null ? other.getOrderSupplySettlementTime() == null : this.getOrderSupplySettlementTime().equals(other.getOrderSupplySettlementTime()))
            && (this.getOrderSupplySettlementId() == null ? other.getOrderSupplySettlementId() == null : this.getOrderSupplySettlementId().equals(other.getOrderSupplySettlementId()))
            && (this.getReturnMemberDiscount() == null ? other.getReturnMemberDiscount() == null : this.getReturnMemberDiscount().equals(other.getReturnMemberDiscount()))
            && (this.getReturnSupperDiscountFee() == null ? other.getReturnSupperDiscountFee() == null : this.getReturnSupperDiscountFee().equals(other.getReturnSupperDiscountFee()))
            && (this.getReturnShippingFee() == null ? other.getReturnShippingFee() == null : this.getReturnShippingFee().equals(other.getReturnShippingFee()))
            && (this.getReturnMethodStatus() == null ? other.getReturnMethodStatus() == null : this.getReturnMethodStatus().equals(other.getReturnMethodStatus()))
            && (this.getFillLogisticsTime() == null ? other.getFillLogisticsTime() == null : this.getFillLogisticsTime().equals(other.getFillLogisticsTime()))
            && (this.getCloseTime() == null ? other.getCloseTime() == null : this.getCloseTime().equals(other.getCloseTime()))
            && (this.getReturnChangeNum() == null ? other.getReturnChangeNum() == null : this.getReturnChangeNum().equals(other.getReturnChangeNum()))
            && (this.getExpirationTime() == null ? other.getExpirationTime() == null : this.getExpirationTime().equals(other.getExpirationTime()))
            && (this.getRevokeNum() == null ? other.getRevokeNum() == null : this.getRevokeNum().equals(other.getRevokeNum()))
            && (this.getReturnVoucher() == null ? other.getReturnVoucher() == null : this.getReturnVoucher().equals(other.getReturnVoucher()))
            && (this.getCallStatus() == null ? other.getCallStatus() == null : this.getCallStatus().equals(other.getCallStatus()))
            && (this.getResubmissionsNumber() == null ? other.getResubmissionsNumber() == null : this.getResubmissionsNumber().equals(other.getResubmissionsNumber()))
            && (this.getHistoricalReturnStatus() == null ? other.getHistoricalReturnStatus() == null : this.getHistoricalReturnStatus().equals(other.getHistoricalReturnStatus()))
            && (this.getReturnNumber() == null ? other.getReturnNumber() == null : this.getReturnNumber().equals(other.getReturnNumber()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getOrderReturnId() == null) ? 0 : getOrderReturnId().hashCode());
        result = prime * result + ((getOrderId() == null) ? 0 : getOrderId().hashCode());
        result = prime * result + ((getOrderAmount() == null) ? 0 : getOrderAmount().hashCode());
        result = prime * result + ((getOrderGoodsNum() == null) ? 0 : getOrderGoodsNum().hashCode());
        result = prime * result + ((getReturnCode() == null) ? 0 : getReturnCode().hashCode());
        result = prime * result + ((getReturnType() == null) ? 0 : getReturnType().hashCode());
        result = prime * result + ((getShopId() == null) ? 0 : getShopId().hashCode());
        result = prime * result + ((getShopName() == null) ? 0 : getShopName().hashCode());
        result = prime * result + ((getSellerUserId() == null) ? 0 : getSellerUserId().hashCode());
        result = prime * result + ((getBuyerUserId() == null) ? 0 : getBuyerUserId().hashCode());
        result = prime * result + ((getBuyerUserAccount() == null) ? 0 : getBuyerUserAccount().hashCode());
        result = prime * result + ((getReturnAddTime() == null) ? 0 : getReturnAddTime().hashCode());
        result = prime * result + ((getReturnReasonId() == null) ? 0 : getReturnReasonId().hashCode());
        result = prime * result + ((getReturnReason() == null) ? 0 : getReturnReason().hashCode());
        result = prime * result + ((getReturnMessage() == null) ? 0 : getReturnMessage().hashCode());
        result = prime * result + ((getReturnRealName() == null) ? 0 : getReturnRealName().hashCode());
        result = prime * result + ((getReturnAddrId() == null) ? 0 : getReturnAddrId().hashCode());
        result = prime * result + ((getReturnAddrName() == null) ? 0 : getReturnAddrName().hashCode());
        result = prime * result + ((getReturnAddr() == null) ? 0 : getReturnAddr().hashCode());
        result = prime * result + ((getReturnShippingCode() == null) ? 0 : getReturnShippingCode().hashCode());
        result = prime * result + ((getReturnPostCode() == null) ? 0 : getReturnPostCode().hashCode());
        result = prime * result + ((getReturnTel() == null) ? 0 : getReturnTel().hashCode());
        result = prime * result + ((getReturnMobile() == null) ? 0 : getReturnMobile().hashCode());
        result = prime * result + ((getReturnState() == null) ? 0 : getReturnState().hashCode());
        result = prime * result + ((getReturnCash() == null) ? 0 : getReturnCash().hashCode());
        result = prime * result + ((getReturnShopTime() == null) ? 0 : getReturnShopTime().hashCode());
        result = prime * result + ((getReturnShopMessage() == null) ? 0 : getReturnShopMessage().hashCode());
        result = prime * result + ((getReturnFinishTime() == null) ? 0 : getReturnFinishTime().hashCode());
        result = prime * result + ((getReturnCommisionFee() == null) ? 0 : getReturnCommisionFee().hashCode());
        result = prime * result + ((getBehalfDeliver() == null) ? 0 : getBehalfDeliver().hashCode());
        result = prime * result + ((getReturnRptCash() == null) ? 0 : getReturnRptCash().hashCode());
        result = prime * result + ((getReturnShopHandle() == null) ? 0 : getReturnShopHandle().hashCode());
        result = prime * result + ((getOrderIsSettlement() == null) ? 0 : getOrderIsSettlement().hashCode());
        result = prime * result + ((getOrderSettlementTime() == null) ? 0 : getOrderSettlementTime().hashCode());
        result = prime * result + ((getOrderSettlementId() == null) ? 0 : getOrderSettlementId().hashCode());
        result = prime * result + ((getOrderOperator() == null) ? 0 : getOrderOperator().hashCode());
        result = prime * result + ((getOrderOperatorTime() == null) ? 0 : getOrderOperatorTime().hashCode());
        result = prime * result + ((getTs() == null) ? 0 : getTs().hashCode());
        result = prime * result + ((getOrderSupplyIsSettlement() == null) ? 0 : getOrderSupplyIsSettlement().hashCode());
        result = prime * result + ((getOrderSupplySettlementTime() == null) ? 0 : getOrderSupplySettlementTime().hashCode());
        result = prime * result + ((getOrderSupplySettlementId() == null) ? 0 : getOrderSupplySettlementId().hashCode());
        result = prime * result + ((getReturnMemberDiscount() == null) ? 0 : getReturnMemberDiscount().hashCode());
        result = prime * result + ((getReturnSupperDiscountFee() == null) ? 0 : getReturnSupperDiscountFee().hashCode());
        result = prime * result + ((getReturnShippingFee() == null) ? 0 : getReturnShippingFee().hashCode());
        result = prime * result + ((getReturnMethodStatus() == null) ? 0 : getReturnMethodStatus().hashCode());
        result = prime * result + ((getFillLogisticsTime() == null) ? 0 : getFillLogisticsTime().hashCode());
        result = prime * result + ((getCloseTime() == null) ? 0 : getCloseTime().hashCode());
        result = prime * result + ((getReturnChangeNum() == null) ? 0 : getReturnChangeNum().hashCode());
        result = prime * result + ((getExpirationTime() == null) ? 0 : getExpirationTime().hashCode());
        result = prime * result + ((getRevokeNum() == null) ? 0 : getRevokeNum().hashCode());
        result = prime * result + ((getReturnVoucher() == null) ? 0 : getReturnVoucher().hashCode());
        result = prime * result + ((getCallStatus() == null) ? 0 : getCallStatus().hashCode());
        result = prime * result + ((getResubmissionsNumber() == null) ? 0 : getResubmissionsNumber().hashCode());
        result = prime * result + ((getHistoricalReturnStatus() == null) ? 0 : getHistoricalReturnStatus().hashCode());
        result = prime * result + ((getReturnNumber() == null) ? 0 : getReturnNumber().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", orderReturnId=").append(orderReturnId);
        sb.append(", orderId=").append(orderId);
        sb.append(", orderAmount=").append(orderAmount);
        sb.append(", orderGoodsNum=").append(orderGoodsNum);
        sb.append(", returnCode=").append(returnCode);
        sb.append(", returnType=").append(returnType);
        sb.append(", shopId=").append(shopId);
        sb.append(", shopName=").append(shopName);
        sb.append(", sellerUserId=").append(sellerUserId);
        sb.append(", buyerUserId=").append(buyerUserId);
        sb.append(", buyerUserAccount=").append(buyerUserAccount);
        sb.append(", returnAddTime=").append(returnAddTime);
        sb.append(", returnReasonId=").append(returnReasonId);
        sb.append(", returnReason=").append(returnReason);
        sb.append(", returnMessage=").append(returnMessage);
        sb.append(", returnRealName=").append(returnRealName);
        sb.append(", returnAddrId=").append(returnAddrId);
        sb.append(", returnAddrName=").append(returnAddrName);
        sb.append(", returnAddr=").append(returnAddr);
        sb.append(", returnShippingCode=").append(returnShippingCode);
        sb.append(", returnPostCode=").append(returnPostCode);
        sb.append(", returnTel=").append(returnTel);
        sb.append(", returnMobile=").append(returnMobile);
        sb.append(", returnState=").append(returnState);
        sb.append(", returnCash=").append(returnCash);
        sb.append(", returnShopTime=").append(returnShopTime);
        sb.append(", returnShopMessage=").append(returnShopMessage);
        sb.append(", returnFinishTime=").append(returnFinishTime);
        sb.append(", returnCommisionFee=").append(returnCommisionFee);
        sb.append(", behalfDeliver=").append(behalfDeliver);
        sb.append(", returnRptCash=").append(returnRptCash);
        sb.append(", returnShopHandle=").append(returnShopHandle);
        sb.append(", orderIsSettlement=").append(orderIsSettlement);
        sb.append(", orderSettlementTime=").append(orderSettlementTime);
        sb.append(", orderSettlementId=").append(orderSettlementId);
        sb.append(", orderOperator=").append(orderOperator);
        sb.append(", orderOperatorTime=").append(orderOperatorTime);
        sb.append(", ts=").append(ts);
        sb.append(", orderSupplyIsSettlement=").append(orderSupplyIsSettlement);
        sb.append(", orderSupplySettlementTime=").append(orderSupplySettlementTime);
        sb.append(", orderSupplySettlementId=").append(orderSupplySettlementId);
        sb.append(", returnMemberDiscount=").append(returnMemberDiscount);
        sb.append(", returnSupperDiscountFee=").append(returnSupperDiscountFee);
        sb.append(", returnShippingFee=").append(returnShippingFee);
        sb.append(", returnMethodStatus=").append(returnMethodStatus);
        sb.append(", fillLogisticsTime=").append(fillLogisticsTime);
        sb.append(", closeTime=").append(closeTime);
        sb.append(", returnChangeNum=").append(returnChangeNum);
        sb.append(", expirationTime=").append(expirationTime);
        sb.append(", revokeNum=").append(revokeNum);
        sb.append(", returnVoucher=").append(returnVoucher);
        sb.append(", callStatus=").append(callStatus);
        sb.append(", resubmissionsNumber=").append(resubmissionsNumber);
        sb.append(", historicalReturnStatus=").append(historicalReturnStatus);
        sb.append(", returnNumber=").append(returnNumber);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}