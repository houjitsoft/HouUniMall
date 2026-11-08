package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.setting.SettingSearchValidate;
import com.houjiang.merchant.vo.setting.SettingSearchDetailVo;

/**
 * 热门搜索服务接口类
 */
public interface ISettingSearchService {

    /**
     * 热门搜索详情
     *
     * @author Amos
     * @return SettingSearchDetailVo
     */
    SettingSearchDetailVo getConfig();

    /**
     * 热门搜索新增
     *
     * @author Amos
     * @param searchValidate 参数
     */
     void setConfig(SettingSearchValidate searchValidate);


}
