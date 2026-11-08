package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.setting.SettingUserValidate;
import com.houjiang.merchant.vo.setting.SettingUserVo;

/**
 * 用户设置服务接口类
 */
public interface ISettingUserService {

    /**
     * 用户设置详情
     *
     * @author Amos
     * @return SettingUserVo
     */
    SettingUserVo getConfig();

    /**
     * 用户设置保存
     *
     * @author Amos
     * @param userValidate 参数
     */
    void setConfig(SettingUserValidate userValidate);

}
