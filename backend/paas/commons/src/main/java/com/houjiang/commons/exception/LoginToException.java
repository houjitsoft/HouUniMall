package com.houjiang.commons.exception;

/**
 * @Description: 登录相关异常
 * @author Amos
 */
public class LoginToException extends RuntimeException {
    private static final long serialVersionUID = -2688474821224087715L;
    /**
     * 异常编号
     */
    private final int messageCode;

    /**
     * 对messageCode 异常信息进行补充说明
     */
    private final String detailMessage;

    public LoginToException(int messageCode, String message) {
        super(message);
        this.messageCode = messageCode;
        this.detailMessage = message;
    }

    public int getMessageCode() {
        return messageCode;
    }

    public String getDetailMessage() {
        return detailMessage;
    }
}