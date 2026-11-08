package com.houjiang.merchant.common.exception;

import com.houjiang.merchant.common.enums.ErrorEnum;

/**
 * 登录异常类
 * @author Amos
 */
public class LoginException extends BaseException {

    public LoginException(Integer code, String msg) {
        super(code, msg, ErrorEnum.SHOW_MSG.getCode());
    }

    public LoginException(Integer code, String msg, Integer show) {
        super(code, msg, show);
    }

}
