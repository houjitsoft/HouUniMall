package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.setting.SettingLoginValidate;
import com.houjiang.merchant.vo.setting.SettingLoginVo;

/**
 * 登录设置服务接口类
 */
public interface ISettingLoginService {

    /**
     * 登录设置详情
     *
     * @author Amos
     * @return SettingLoginVo
     */
    SettingLoginVo getRegisterConfig();

    /**
     * 登录设置保存
     *
     * @author Amos
     * @param loginValidate 参数
     */
    void setRegisterConfig(SettingLoginValidate loginValidate);

}
