package com.houjiang.merchant.common.plugin.wechat.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 商家转账到零钱请求（新接口V3）
 *
 * @author Amos
 */
public class TransferToUserRequest {
    
    @SerializedName("appid")
    public String appid;
    
    @SerializedName("out_bill_no")
    public String outBillNo;
    
    @SerializedName("transfer_scene_id")
    public String transferSceneId;
    
    @SerializedName("openid")
    public String openid;
    
    @SerializedName("user_name")
    public String userName;
    
    @SerializedName("transfer_amount")
    public Long transferAmount;
    
    @SerializedName("transfer_remark")
    public String transferRemark;
    
    @SerializedName("notify_url")
    public String notifyUrl;
    
    @SerializedName("user_recv_perception")
    public String userRecvPerception;
    
    @SerializedName("transfer_scene_report_infos")
    public List<TransferSceneReportInfo> transferSceneReportInfos;
    
    public String getAppid() {
        return appid;
    }
    
    public void setAppid(String appid) {
        this.appid = appid;
    }
    
    public String getOutBillNo() {
        return outBillNo;
    }
    
    public void setOutBillNo(String outBillNo) {
        this.outBillNo = outBillNo;
    }
    
    public String getTransferSceneId() {
        return transferSceneId;
    }
    
    public void setTransferSceneId(String transferSceneId) {
        this.transferSceneId = transferSceneId;
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
    
    public String getNotifyUrl() {
        return notifyUrl;
    }
    
    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }
    
    public String getUserRecvPerception() {
        return userRecvPerception;
    }
    
    public void setUserRecvPerception(String userRecvPerception) {
        this.userRecvPerception = userRecvPerception;
    }
    
    public List<TransferSceneReportInfo> getTransferSceneReportInfos() {
        return transferSceneReportInfos;
    }
    
    public void setTransferSceneReportInfos(List<TransferSceneReportInfo> transferSceneReportInfos) {
        this.transferSceneReportInfos = transferSceneReportInfos;
    }
}