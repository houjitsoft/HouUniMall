package com.houjiang.merchant.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 异常基类
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseException extends RuntimeException {

    private Integer code;
    private String msg;
    private Integer show;

    public BaseException(Integer code, String msg, Integer show) {
        this.code = code;
        this.msg = msg;
        this.show = show;
    }
}
