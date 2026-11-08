package com.houjiang.merchant.common.plugin.wechat.request;

import com.google.gson.annotations.SerializedName;

/**
 * 转账场景报备信息
 *
 * @author Amos
 */
public class TransferSceneReportInfo {
    
    @SerializedName("info_type")
    public String infoType;
    
    @SerializedName("info_content")
    public String infoContent;
    
    public TransferSceneReportInfo() {}
    
    public TransferSceneReportInfo(String infoType, String infoContent) {
        this.infoType = infoType;
        this.infoContent = infoContent;
    }
    
    public String getInfoType() {
        return infoType;
    }
    
    public void setInfoType(String infoType) {
        this.infoType = infoType;
    }
    
    public String getInfoContent() {
        return infoContent;
    }
    
    public void setInfoContent(String infoContent) {
        this.infoContent = infoContent;
    }
}