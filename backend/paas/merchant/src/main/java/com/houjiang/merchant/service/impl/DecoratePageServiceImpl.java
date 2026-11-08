package com.houjiang.merchant.service.impl;

import com.houjiang.merchant.common.validation.Assert;
import com.houjiang.merchant.service.IDecoratePageService;
import com.houjiang.merchant.validate.decorate.DecoratePageValidate;
import com.houjiang.merchant.vo.decorate.DecoratePageVo;
import com.houjiang.merchant.common.entity.decorate.DecoratePage;
import com.houjiang.merchant.common.mapper.decorate.DecoratePageMapper;
import com.houjiang.merchant.common.util.TimeUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class DecoratePageServiceImpl implements IDecoratePageService {

    @Resource
    DecoratePageMapper decoratePageMapper;

    /**
     * 页面装修详情
     *
     * @author Amos
     * @param id 主键
     * @return DecoratePageVo
     */
    @Override
    public DecoratePageVo detail(Integer id) {
        DecoratePage decoratePage = decoratePageMapper.selectById(id);
        Assert.notNull(decoratePage, "数据不存在!");
        DecoratePageVo vo = new DecoratePageVo();
        vo.setId(decoratePage.getId());
        vo.setName(decoratePage.getName());
        vo.setType(decoratePage.getType());
        vo.setData(decoratePage.getData());
        vo.setCommon(decoratePage.getCommon());

        vo.setCreateTime(TimeUtils.timestampToDate(decoratePage.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(decoratePage.getUpdateTime()));
        return vo;
    }

    /**
     * 页面装修保存
     *
     * @author Amos
     * @param decoratePageValidate 参数
     */
    @Override
    public void save(DecoratePageValidate decoratePageValidate) {
        DecoratePage decoratePage = decoratePageMapper.selectById(decoratePageValidate.getId());
        Assert.notNull(decoratePage, "数据不存在!");

        decoratePage.setName(decoratePageValidate.getName());
        decoratePage.setType(decoratePageValidate.getType());
        decoratePage.setCommon(decoratePageValidate.getCommon());
        decoratePage.setData(decoratePageValidate.getData());
        decoratePage.setUpdateTime(System.currentTimeMillis() / 1000);
        decoratePageMapper.updateById(decoratePage);
    }

}
