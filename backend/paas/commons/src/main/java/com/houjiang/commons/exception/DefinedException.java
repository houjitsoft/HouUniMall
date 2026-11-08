package com.houjiang.commons.exception;

import com.houjiang.commons.util.ResponseResult;
import lombok.EqualsAndHashCode;
import org.apache.poi.ss.formula.functions.T;

/**
 * @Description: 自定义异常
 * @author Amos
 */
@EqualsAndHashCode(callSuper = true)
public class DefinedException extends RuntimeException {

    private static final long serialVersionUID = -420925980366951255L;
    /**
     * 异常编号
     */
    private final int messageCode;

    /**
     * 对messageCode 异常信息进行补充说明
     */
    private final String detailMessage;

    public DefinedException(int messageCode, String message) {
        super(message);
        this.messageCode = messageCode;
        this.detailMessage = message;
    }

    public DefinedException(String message) {
        super(message);
        this.messageCode = 5000;
        this.detailMessage = message;
    }

    /**
     * 构造函数，接收一个枚举类型，该枚举类型定义了异常的编号和信息。
     * @param resultCode 包含异常编号和信息的枚举对象
     */
    public DefinedException(BizCodeEnume resultCode) {
        super(resultCode.getMsg());
        this.messageCode = resultCode.getCode();
        this.detailMessage = resultCode.getMsg();
    }

    /**
     * 构造函数，接收一个枚举类型，该枚举类型定义了异常的编号和信息。
     * @param result 包含异常编号和信息的枚举对象
     */
    public DefinedException(ResponseResult<T> result) {
        super(result.getMessage());
        this.messageCode = result.getCode();
        this.detailMessage = result.getMessage();
    }

    public int getMessageCode() {
        return messageCode;
    }

    public String getDetailMessage() {
        return detailMessage;
    }
}
