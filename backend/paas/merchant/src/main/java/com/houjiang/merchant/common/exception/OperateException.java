package com.houjiang.merchant.common.exception;

import com.houjiang.merchant.common.enums.ErrorEnum;

/**
 * 操作系统异常
 * @author Amos
 */
public class OperateException extends BaseException {

    public OperateException(String msg) {
        super(ErrorEnum.FAILED.getCode(), msg, ErrorEnum.SHOW_MSG.getCode());
    }

    public OperateException(String msg, Integer errCode) {
        super(errCode, msg, ErrorEnum.SHOW_MSG.getCode());
    }

    public OperateException(String msg, Integer errCode, Integer showCode) {
        super(errCode, msg, showCode);
    }
}
