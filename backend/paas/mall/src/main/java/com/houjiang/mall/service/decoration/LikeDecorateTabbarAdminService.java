package com.houjiang.mall.service.decoration;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.mall.entity.decoration.DecorateTabbar;
import com.houjiang.mall.entity.decoration.LikeDecorateTabbar;
import com.houjiang.mall.mapper.decoration.LikeDecorateTabbarMapper;
import com.houjiang.mall.service.setting.CommonConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 底部导航（hou_decorate_tabbar + tabbar/style 配置），兼容 likeshop 字段 list / style。
 * @author Amos
 */
@Service
@RequiredArgsConstructor
public class LikeDecorateTabbarAdminService {

    private static final String STYLE_TYPE = "tabbar";
    private static final String STYLE_NAME = "style";

    private final LikeDecorateTabbarMapper likeDecorateTabbarMapper;
    private final DecorateTabbarService decorateTabbarService;
    private final CommonConfigService commonConfigService;

    public Map<String, Object> detail() {
        List<Map<String, Object>> list = loadList();
        if (list.isEmpty()) {
            list = defaultTabbarList();
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", list);
        data.put("style", loadStyle());
        return data;
    }

    private List<Map<String, Object>> defaultTabbarList() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(tab("首页", "/pages/index/index"));
        list.add(tab("分类", "/pages/goods/category"));
        list.add(tab("购物车", "/pages/cart/cart"));
        list.add(tab("我的", "/pages/user/user"));
        return list;
    }

    private Map<String, Object> tab(String name, String path) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("selected", "");
        item.put("unselected", "");
        Map<String, Object> link = new LinkedHashMap<>();
        link.put("path", path);
        item.put("link", link);
        return item;
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(Map<String, Object> params) {
        if (params == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        saveList(params.get("list"));
        saveStyle(params.get("style"));
    }

    @SuppressWarnings("unchecked")
    private void saveList(Object listObj) {
        likeDecorateTabbarMapper.delete(new LambdaQueryWrapper<>());
        if (!(listObj instanceof List<?> rawList) || rawList.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis() / 1000;
        for (Object item : rawList) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            LikeDecorateTabbar tab = new LikeDecorateTabbar();
            tab.setName(str(map.get("name")));
            tab.setSelected(str(map.get("selected")));
            tab.setUnselected(str(map.get("unselected")));
            Object link = map.get("link");
            if (link != null) {
                tab.setLink(link instanceof String s ? s : JSON.toJSONString(link));
            }
            tab.setIsShow(1);
            tab.setCreateTime(now);
            tab.setUpdateTime(now);
            likeDecorateTabbarMapper.insert(tab);
        }
    }

    private void saveStyle(Object styleObj) {
        if (!(styleObj instanceof Map<?, ?> styleMap)) {
            return;
        }
        Map<String, String> stored = new LinkedHashMap<>();
        String defaultColor = color(styleMap, "default_color", "defaultColor", "#999999");
        String selectedColor = color(styleMap, "selected_color", "selectedColor", "#EA5455");
        stored.put("default_color", defaultColor);
        stored.put("selected_color", selectedColor);
        stored.put("defaultColor", defaultColor);
        stored.put("selectedColor", selectedColor);
        commonConfigService.set(STYLE_TYPE, STYLE_NAME, JSON.toJSONString(stored));
    }

    private List<Map<String, Object>> loadList() {
        List<Map<String, Object>> list = loadListFromLikeTable();
        if (!list.isEmpty()) {
            return list;
        }
        return loadListFromMallDecorateTabbar();
    }

    private List<Map<String, Object>> loadListFromLikeTable() {
        List<LikeDecorateTabbar> rows = likeDecorateTabbarMapper.selectList(
                new LambdaQueryWrapper<LikeDecorateTabbar>()
                        .orderByAsc(LikeDecorateTabbar::getId));
        List<Map<String, Object>> list = new ArrayList<>();
        for (LikeDecorateTabbar tab : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", tab.getId());
            item.put("name", tab.getName() != null ? tab.getName() : "");
            item.put("selected", tab.getSelected() != null ? tab.getSelected() : "");
            item.put("unselected", tab.getUnselected() != null ? tab.getUnselected() : "");
            item.put("link", parseLink(tab.getLink()));
            list.add(item);
        }
        return list;
    }

    /** 兼容旧版 hou_mall_decorate_tabbar.content JSON 存储 */
    private List<Map<String, Object>> loadListFromMallDecorateTabbar() {
        DecorateTabbar row = decorateTabbarService.getTabbarDetail(null);
        if (row == null || !StringUtils.hasText(row.getContent())) {
            return new ArrayList<>();
        }
        try {
            JSONArray array = JSON.parseArray(row.getContent());
            if (array == null || array.isEmpty()) {
                return new ArrayList<>();
            }
            List<Map<String, Object>> list = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                Object element = array.get(i);
                if (!(element instanceof Map<?, ?> map)) {
                    continue;
                }
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", str(map.get("name")));
                item.put("selected", str(map.get("selected")));
                item.put("unselected", str(map.get("unselected")));
                Object link = map.get("link");
                if (link instanceof Map<?, ?> linkMap) {
                    item.put("link", new LinkedHashMap<>(linkMap));
                } else if (link != null) {
                    item.put("link", parseLink(String.valueOf(link)));
                } else {
                    item.put("link", new LinkedHashMap<>());
                }
                list.add(item);
            }
            return list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private Map<String, Object> loadStyle() {
        Map<String, String> styleMap = commonConfigService.getJsonMap(STYLE_TYPE, STYLE_NAME);
        Map<String, Object> style = new LinkedHashMap<>();
        style.put("default_color", color(styleMap, "default_color", "defaultColor", "#999999"));
        style.put("selected_color", color(styleMap, "selected_color", "selectedColor", "#EA5455"));
        return style;
    }

    private static Object parseLink(String linkJson) {
        if (!StringUtils.hasText(linkJson)) {
            return new LinkedHashMap<>();
        }
        try {
            return JSON.parse(linkJson);
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private static String color(Map<?, ?> map, String snakeKey, String camelKey, String defaultValue) {
        Object v = map.get(snakeKey);
        if (v == null) {
            v = map.get(camelKey);
        }
        return v != null && StringUtils.hasText(String.valueOf(v)) ? String.valueOf(v) : defaultValue;
    }

    private static String str(Object v) {
        return v == null ? "" : String.valueOf(v);
    }
}
