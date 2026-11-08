package com.houjiang.merchant.common.exception;

import com.houjiang.merchant.common.enums.ErrorEnum;

/**
 * 支付失败异常
 * @author Amos
 */
public class PaymentException extends BaseException {

    public PaymentException(String msg) {
        super(ErrorEnum.FAILED.getCode(), msg, ErrorEnum.SHOW_MSG.getCode());
    }

    public PaymentException(String msg, Integer errCode) {
        super(errCode, msg, ErrorEnum.SHOW_MSG.getCode());
    }

}
