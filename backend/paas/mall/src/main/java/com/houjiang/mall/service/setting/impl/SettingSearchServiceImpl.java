package com.houjiang.mall.service.setting.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.mall.entity.setting.SettingHotSearch;
import com.houjiang.mall.mapper.setting.SettingHotSearchMapper;
import com.houjiang.mall.service.setting.CommonConfigService;
import com.houjiang.mall.service.setting.SettingSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 热门搜索 Service 实现。对应 like-admin SettingSearchServiceImpl。
 * status 存于 hou_merchant_config(type='hot_search', name='status')；
 * 关键词列表存于 hou_merchant_hot_search。
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettingSearchServiceImpl implements SettingSearchService {

    private static final String CFG_TYPE = "hot_search";
    private static final String CFG_NAME_STATUS = "status";

    private final SettingHotSearchMapper hotSearchMapper;
    private final CommonConfigService commonConfigService;

    @Override
    public Map<String, Object> getConfig() {
        int status = parseInt(commonConfigService.getOrDefault(CFG_TYPE, CFG_NAME_STATUS, "0"), 0);
        List<SettingHotSearch> list = hotSearchMapper.selectList(
                new QueryWrapper<SettingHotSearch>().orderByDesc("sort"));
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("status", status);
        vo.put("data", list);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setConfig(Map<String, Object> params) {
        if (params == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        Object statusObj = params.get("status");
        if (statusObj == null) {
            throw new IllegalArgumentException("status 参数缺失");
        }
        // 保存 status
        commonConfigService.set(CFG_TYPE, CFG_NAME_STATUS, statusObj.toString());

        // 重置关键词列表（与 like-admin 一致：先全量删除再批量插入）
        hotSearchMapper.delete(new QueryWrapper<SettingHotSearch>().ge("id", 0));

        Object dataObj = params.get("data");
        if (!(dataObj instanceof List)) {
            return;
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) dataObj;
        for (Map<String, Object> item : data) {
            if (item == null) continue;
            Object name = item.get("name");
            Object sort = item.get("sort");
            if (name == null || name.toString().trim().isEmpty()) {
                continue;
            }
            SettingHotSearch row = new SettingHotSearch();
            row.setName(name.toString());
            row.setSort(sort == null ? 0 : parseInt(sort.toString(), 0));
            hotSearchMapper.insert(row);
        }
    }

    private int parseInt(String s, int defaults) {
        if (s == null || s.isEmpty()) {
            return defaults;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaults;
        }
    }
}
