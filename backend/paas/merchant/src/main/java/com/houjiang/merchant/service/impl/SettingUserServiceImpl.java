package com.houjiang.merchant.service.impl;

import com.houjiang.merchant.service.ISettingUserService;
import com.houjiang.merchant.validate.setting.SettingUserValidate;
import com.houjiang.merchant.vo.setting.SettingUserVo;
import com.houjiang.merchant.common.util.ConfigUtils;
import com.houjiang.merchant.common.util.UrlUtils;
import org.springframework.stereotype.Service;

/**
 * 用户设置服务实现类
 */
@Service
public class SettingUserServiceImpl implements ISettingUserService {

    /**
     * 用户设置详情
     *
     * @author Amos
     * @return SettingUserVo
     */
    @Override
    public SettingUserVo getConfig() {
        String defaultAvatar = ConfigUtils.get("default_image", "user_avatar", "");
        SettingUserVo vo = new SettingUserVo();
        vo.setDefaultAvatar(UrlUtils.toShopAbsoluteUrl(defaultAvatar));
        return vo;
    }

    /**
     * 用户设置保存
     *
     * @author Amos
     * @param userValidate 参数
     */
    @Override
    public void setConfig(SettingUserValidate userValidate) {
        ConfigUtils.set("default_image", "user_avatar", UrlUtils.toShopRelativeUrl(userValidate.getDefaultAvatar()));
    }

}
