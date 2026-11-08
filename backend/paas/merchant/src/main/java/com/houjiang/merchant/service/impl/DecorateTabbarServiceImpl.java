package com.houjiang.merchant.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.service.IDecorateTabbarService;
import com.houjiang.merchant.validate.decorate.DecorateTabsValidate;
import com.houjiang.merchant.vo.decorate.DecorateTabsListsVo;
import com.houjiang.merchant.vo.decorate.DecorateTabbarVo;
import com.houjiang.merchant.vo.decorate.DecorateTabsStyleVo;
import com.houjiang.merchant.common.entity.decorate.DecorateTabbar;
import com.houjiang.merchant.common.mapper.decorate.DecorateTabbarMapper;
import com.houjiang.merchant.common.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 底部导航服务实现类
 */
@Service
public class DecorateTabbarServiceImpl implements IDecorateTabbarService {

    @Resource
    DecorateTabbarMapper decorateTabbarMapper;

    /**
     * 底部导航详情
     *
     * @author Amos
     * @return DecorateTabbarVo
     */
    @Override
    public DecorateTabbarVo detail() {

        List<DecorateTabbar> list = decorateTabbarMapper.selectList(
                new QueryWrapper<DecorateTabbar>()
                    .orderByAsc("id"));

        List<DecorateTabsListsVo> tabList = new LinkedList<>();
        for (DecorateTabbar tab: list) {
            DecorateTabsListsVo vo = new DecorateTabsListsVo();
            vo.setId(tab.getId());
            vo.setName(tab.getName());
            vo.setSelected(tab.getSelected());
            vo.setUnselected(tab.getUnselected());
            vo.setLink(JSON.parse(tab.getLink()));
            vo.setCreateTime(TimeUtils.timestampToDate(tab.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(tab.getUpdateTime()));
            tabList.add(vo);
        }

        String tabbar = ConfigUtils.get("tabbar", "style", "{}");

        DecorateTabbarVo response = new DecorateTabbarVo();
        DecorateTabsStyleVo styleVo = new DecorateTabsStyleVo();
        Map<String,String> styleMap = MapUtils.jsonToMap(tabbar);
        styleVo.setDefaultColor(styleMap.get("defaultColor"));
        styleVo.setSelectedColor(styleMap.get("selectedColor"));
        response.setStyle(styleVo);
        response.setList(tabList);
        return response;
    }

    /**
     * 底部导航保存
     *
     * @author Amos
     * @param tabsValidate 参数
     */
    @Override
    @Transactional
    public void save(DecorateTabsValidate tabsValidate) {
        decorateTabbarMapper.delete(new QueryWrapper<DecorateTabbar>().gt("id", 0));

        for (DecorateTabsListsVo obj : tabsValidate.getList()) {
            DecorateTabbar model = new DecorateTabbar();
            model.setName(obj.getName());
            model.setLink(JSON.toJSONString(obj.getLink()));
            model.setSelected(obj.getSelected());
            model.setUnselected(obj.getUnselected());
            model.setCreateTime(System.currentTimeMillis() / 1000);
            model.setUpdateTime(System.currentTimeMillis() / 1000);
            decorateTabbarMapper.insert(model);
        }

        if (StringUtils.isNotNull(tabsValidate.getStyle())) {
            ConfigUtils.set("tabbar", "style", JSON.toJSONString(tabsValidate.getStyle()));
        }
    }

}
