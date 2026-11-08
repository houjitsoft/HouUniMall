package com.houjiang.commons.exception;


/**
 * 自定义异常
 * @author Amos
 *
 */
public class UserDefinedException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	/** 错误码 */
	private GlobalExceptionEnum errorCode;
	/** 错误上下文 */
	private String errorContext;

	public UserDefinedException(GlobalExceptionEnum noForbidden, String errorMsg) {
		this.errorCode = noForbidden;
	}

	public GlobalExceptionEnum getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(GlobalExceptionEnum errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorContext() {
		return errorContext;
	}

	public void setErrorContext(String errorContext) {
		this.errorContext = errorContext;
	}

}
