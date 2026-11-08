package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.decorate.DecoratePageValidate;
import com.houjiang.merchant.vo.decorate.DecoratePageVo;

public interface IDecoratePageService {

    /**
     * 页面装修详情
     *
     * @author Amos
     * @param id 主键
     * @return DecoratePageVo
     */
    DecoratePageVo detail(Integer id);

    /**
     * 页面装修保存
     *
     * @author Amos
     * @param decoratePageValidate 参数
     */
    void save(DecoratePageValidate decoratePageValidate);
}
