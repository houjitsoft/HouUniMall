package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.setting.SettingCopyrightValidate;
import com.houjiang.merchant.vo.setting.SettingCopyrightVo;

import java.util.List;

/**
 * 网站备案服务接口类
 */
public interface ISettingCopyrightService {

    /**
     * 获取网站备案信息
     *
     * @author Amos
     * @return List<SettingCopyrightVo>
     */
    List<SettingCopyrightVo> getCopyright();

    /**
     * 保存网站备案信息
     *
     * @author Amos
     * @param copyrightValidate 参数
     */
    void setCopyright(SettingCopyrightValidate copyrightValidate);

}
