package com.houjiang.merchant.service.system;

import com.houjiang.merchant.validate.system.LoginsValidate;
import com.houjiang.merchant.vo.system.LoginVo;

/**
 * 系统登录服务接口类
 */
public interface ILoginService {

    /**
     * 登录
     *
     * @author Amos
     * @param loginsValidate 登录参数
     * @return SystemLoginVo
     */
    LoginVo login(LoginsValidate loginsValidate);

    /**
     * 退出
     *
     * @author Amos
     * @param token 令牌
     */
    void logout(String token);

}
