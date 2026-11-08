package com.houjiang.merchant.common.plugin.wechat.response;

import com.google.gson.annotations.SerializedName;
import com.houjiang.merchant.common.plugin.wechat.enums.TransferBillStatus;

/**
 * 转账查询响应（新接口V3）
 *
 * @author Amos
 */
public class TransferQueryResponse {
    
    @SerializedName("out_bill_no")
    public String outBillNo;
    
    @SerializedName("transfer_bill_no")
    public String transferBillNo;
    
    @SerializedName("create_time")
    public String createTime;
    
    @SerializedName("update_time")
    public String updateTime;
    
    @SerializedName("state")
    public TransferBillStatus state;
    
    @SerializedName("transfer_amount")
    public Long transferAmount;
    
    @SerializedName("transfer_remark")
    public String transferRemark;
    
    @SerializedName("openid")
    public String openid;
    
    @SerializedName("user_name")
    public String userName;
    
    @SerializedName("fail_reason")
    public String failReason;
    
    public String getOutBillNo() {
        return outBillNo;
    }
    
    public void setOutBillNo(String outBillNo) {
        this.outBillNo = outBillNo;
    }
    
    public String getTransferBillNo() {
        return transferBillNo;
    }
    
    public void setTransferBillNo(String transferBillNo) {
        this.transferBillNo = transferBillNo;
    }
    
    public String getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    
    public String getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
    
    public TransferBillStatus getState() {
        return state;
    }
    
    public void setState(TransferBillStatus state) {
        this.state = state;
    }
    
    public Long getTransferAmount() {
        return transferAmount;
    }
    
    public void setTransferAmount(Long transferAmount) {
        this.transferAmount = transferAmount;
    }
    
    public String getTransferRemark() {
        return transferRemark;
    }
    
    public void setTransferRemark(String transferRemark) {
        this.transferRemark = transferRemark;
    }
    
    public String getOpenid() {
        return openid;
    }
    
    public void setOpenid(String openid) {
        this.openid = openid;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getFailReason() {
        return failReason;
    }
    
    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }
}