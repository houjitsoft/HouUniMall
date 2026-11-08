package com.houjiang.userms.context;


import com.houjiang.commons.vo.user.UserInfoVO;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class LoginContext {

	private UserInfoVO userInfo;

    private static ThreadLocal<LoginContext> local = new ThreadLocal<LoginContext>();

    public LoginContext(UserInfoVO userInfo) {
    	this.userInfo = userInfo;
    }

    /**
     * 绑定上下文
     * @param context 上下文
     */
    public static void binding(LoginContext context) {
        local.set(context);
    }

    /**
     * 获取请求上下文
     *
     * @return 请求上下文
     */
    public static LoginContext getContext() {
        return local.get();
    }

    /**
     * 移除上下文
     */
    public static void remove() {
        local.set(null);
    }

}
