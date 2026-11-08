package com.houjiang.merchant.common.plugin.wechat.response;

import com.google.gson.annotations.SerializedName;
import com.houjiang.merchant.common.plugin.wechat.enums.TransferBillStatus;

/**
 * 商家转账到零钱响应（新接口V3）
 *
 * @author Amos
 */
public class TransferToUserResponse {
    
    @SerializedName("out_bill_no")
    public String outBillNo;
    
    @SerializedName("transfer_bill_no")
    public String transferBillNo;
    
    @SerializedName("create_time")
    public String createTime;
    
    @SerializedName("state")
    public TransferBillStatus state;
    
    @SerializedName("package_info")
    public String packageInfo;
    
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
    
    public TransferBillStatus getState() {
        return state;
    }
    
    public void setState(TransferBillStatus state) {
        this.state = state;
    }
    
    public String getPackageInfo() {
        return packageInfo;
    }
    
    public void setPackageInfo(String packageInfo) {
        this.packageInfo = packageInfo;
    }
}