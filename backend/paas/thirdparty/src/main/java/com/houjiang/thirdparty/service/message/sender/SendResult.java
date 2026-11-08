package com.houjiang.thirdparty.service.message.sender;

import java.util.Map;

/**
 * 消息发送结果
 * 
 * @author Amos
 * @since 2025-12-26
 */
public class SendResult {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 错误码
     */
    private String errorCode;
    
    /**
     * 错误消息
     */
    private String errorMessage;
    
    /**
     * 第三方消息ID
     */
    private String messageId;

    /**
     * 是否命中已成功的幂等发送记录。
     */
    private boolean idempotentReplay;
    
    /**
     * 扩展数据
     */
    private Map<String, Object> extra;
    
    public SendResult() {}
    
    public SendResult(boolean success, String messageId) {
        this.success = success;
        this.messageId = messageId;
    }
    
    public static SendResult success(String messageId) {
        SendResult result = new SendResult();
        result.setSuccess(true);
        result.setMessageId(messageId);
        return result;
    }
    
    public static SendResult fail(String errorCode, String errorMessage) {
        SendResult result = new SendResult();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        return result;
    }
    
    /** Getters and Setters */
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public boolean isIdempotentReplay() {
        return idempotentReplay;
    }

    public void setIdempotentReplay(boolean idempotentReplay) {
        this.idempotentReplay = idempotentReplay;
    }
    
    public Map<String, Object> getExtra() {
        return extra;
    }
    
    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}
