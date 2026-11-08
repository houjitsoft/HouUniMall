package com.houjiang.merchant.common.plugin.wechat.enums;

import com.google.gson.annotations.SerializedName;

/**
 * 转账单状态枚举
 *
 * @author Amos
 */
public enum TransferBillStatus {
    
    @SerializedName("ACCEPTED")
    /** TODO 补充说明 */
    ACCEPTED("ACCEPTED", "已受理"),
    
    @SerializedName("PROCESSING")
    /** TODO 补充说明 */
    PROCESSING("PROCESSING", "处理中"),
    
    @SerializedName("WAIT_USER_CONFIRM")
    /** TODO 补充说明 */
    WAIT_USER_CONFIRM("WAIT_USER_CONFIRM", "等待用户确认"),
    
    @SerializedName("TRANSFERING")
    /** TODO 补充说明 */
    TRANSFERING("TRANSFERING", "转账中"),
    
    @SerializedName("SUCCESS")
    /** TODO 补充说明 */
    SUCCESS("SUCCESS", "转账成功"),
    
    @SerializedName("FAIL")
    /** TODO 补充说明 */
    FAIL("FAIL", "转账失败"),
    
    @SerializedName("CANCELING")
    /** TODO 补充说明 */
    CANCELING("CANCELING", "取消中"),
    
    @SerializedName("CANCELLED")
    /** TODO 补充说明 */
    CANCELLED("CANCELLED", "已取消");
    
    private final String code;
    private final String desc;
    
    TransferBillStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
}