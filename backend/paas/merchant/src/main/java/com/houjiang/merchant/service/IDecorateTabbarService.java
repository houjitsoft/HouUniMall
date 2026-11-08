package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.decorate.DecorateTabsValidate;
import com.houjiang.merchant.vo.decorate.DecorateTabbarVo;

/**
 * 底部导航服务接口类
 */
public interface IDecorateTabbarService {

    /**
     * 底部导航详情
     *
     * @author Amos
     * @return DecorateTabbarVo
     */
    DecorateTabbarVo detail();

    /**
     * 底部导航保存
     *
     * @author Amos
     * @param tabsValidate 参数
     */
    void save(DecorateTabsValidate tabsValidate);

}
