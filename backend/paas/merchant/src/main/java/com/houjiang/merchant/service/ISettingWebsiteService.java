package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.setting.SettingWebsiteValidate;
import com.houjiang.merchant.vo.setting.SettingWebsiteVo;

/**
 * 网站信息服务接口类
 */
public interface ISettingWebsiteService {

    /**
     * 获取网站信息
     *
     * @author Amos
     * @return SettingWebsiteVo
     */
    SettingWebsiteVo getWebsite();

    /**
     * 保存网站信息
     *
     * @author Amos
     * @param websiteValidate 参数
     */
    void setWebsite(SettingWebsiteValidate websiteValidate);

}
