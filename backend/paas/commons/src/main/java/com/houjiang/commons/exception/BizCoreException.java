package com.houjiang.commons.exception;

import com.houjiang.commons.constant.CommonStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BizCoreException extends RuntimeException {
    private static final long serialVersionUID = -430613633714952314L;
    private String errorMsg;
    private Integer code;

    public BizCoreException(String errorMsg) {
        super(errorMsg);
        this.code = CommonStatusEnum.FAIL.getCode();
        this.errorMsg = errorMsg;
    }

    public BizCoreException(BizCodeEnume resultCode) {
        super(resultCode.getMsg());
        this.errorMsg = resultCode.getMsg();
        this.code = resultCode.getCode();
    }

    public BizCoreException(Integer errorCode, String errorMsg) {
        super(errorMsg);
        this.errorMsg = errorMsg;
        this.code = errorCode;
    }

    public BizCoreException(String errorMsg, BizCodeEnume resultCode) {
        super(errorMsg);
        this.errorMsg = errorMsg;
        this.code = resultCode.getCode();
    }

}
