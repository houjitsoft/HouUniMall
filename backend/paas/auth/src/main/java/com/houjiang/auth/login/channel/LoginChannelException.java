package com.houjiang.auth.login.channel;

/**
 * 渠道登录过程中的业务异常
 *
 * <p>携带可直接展示给用户的提示文案；抛出后由服务层统一转成 ResponseResult，
 * 避免把第三方原始错误（含 appid 等）直接暴露给前端。</p>
 *
 * @author Amos
 */
public class LoginChannelException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LoginChannelException(String message) {
        super(message);
    }

    public LoginChannelException(String message, Throwable cause) {
        super(message, cause);
    }
}
