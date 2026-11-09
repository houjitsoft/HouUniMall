package com.houjiang.bffweb.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * likeshop 商城风格默认配置（与 mall MallStyleAdminService 保持一致）
 * @author Amos
 */
public final class MallStyleDefaults {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String DEFAULT_ALL_JSON = "{\"current\":\"gold_theme\",\"list\":["
            + "{\"name\":\"经典红\",\"className\":\"red_theme\",\"color\":\"#FF2C3C\",\"dark_color\":\"#EF1D2D\","
            + "\"nav_text_color\":\"#000000\",\"nav_bg_color\":\"#FFFFFF\",\"variable\":{\"--theme-color\":\"#FF2C3C\","
            + "\"--theme-light-color\":\"#FFE9EB\",\"--theme-disabled-color\":\"#FF959D\",\"--theme-dark-color\":\"#EF1D2D\"}},"
            + "{\"name\":\"活力橙\",\"className\":\"orange_theme\",\"color\":\"#F7971E\",\"dark_color\":\"#FFD200\","
            + "\"nav_text_color\":\"#000000\",\"nav_bg_color\":\"#ffffff\",\"variable\":{\"--theme-color\":\"#F7971E\","
            + "\"--theme-light-color\":\"#FFF4E8\",\"--theme-disabled-color\":\"#FBCB8E\",\"--theme-dark-color\":\"#FFD200\"}},"
            + "{\"name\":\"美妆色\",\"className\":\"pink_theme\",\"color\":\"#FD498F\",\"dark_color\":\"#FA444D\","
            + "\"nav_text_color\":\"#000000\",\"nav_bg_color\":\"#ffffff\",\"variable\":{\"--theme-color\":\"#FD498F\","
            + "\"--theme-light-color\":\"#FEECF4\",\"--theme-disabled-color\":\"#FCA1A5\",\"--theme-dark-color\":\"#FA444D\"}},"
            + "{\"name\":\"高级金\",\"className\":\"gold_theme\",\"color\":\"#E0A356\",\"dark_color\":\"#EBC389\","
            + "\"nav_text_color\":\"#000000\",\"nav_bg_color\":\"#ffffff\",\"variable\":{\"--theme-color\":\"#E0A356\","
            + "\"--theme-light-color\":\"#FBF7F1\",\"--theme-disabled-color\":\"#EFD1AA\",\"--theme-dark-color\":\"#EBC389\"}},"
            + "{\"name\":\"科技蓝\",\"className\":\"blue_theme\",\"color\":\"#2F80ED\",\"dark_color\":\"#56CCF2\","
            + "\"nav_text_color\":\"#000000\",\"nav_bg_color\":\"#ffffff\",\"variable\":{\"--theme-color\":\"#2F80ED\","
            + "\"--theme-light-color\":\"#E9F2FD\",\"--theme-disabled-color\":\"#96BFF6\",\"--theme-dark-color\":\"#56CCF2\"}},"
            + "{\"name\":\"生鲜绿\",\"className\":\"green_theme\",\"color\":\"#2EC840\",\"dark_color\":\"#3DE650\","
            + "\"nav_text_color\":\"#000000\",\"nav_bg_color\":\"#ffffff\",\"variable\":{\"--theme-color\":\"#2EC840\","
            + "\"--theme-light-color\":\"#E9FAEB\",\"--theme-disabled-color\":\"#96E39F\",\"--theme-dark-color\":\"#3DE650\"}},"
            + "{\"name\":\"梦幻紫\",\"className\":\"purple_theme\",\"color\":\"#A74BFD\",\"dark_color\":\"#CB60FF\","
            + "\"nav_text_color\":\"#000000\",\"nav_bg_color\":\"#ffffff\",\"variable\":{\"--theme-color\":\"#A74BFD\","
            + "\"--theme-light-color\":\"#F7EFFF\",\"--theme-disabled-color\":\"#A495FF\",\"--theme-dark-color\":\"#CB60FF\"}},"
            + "{\"name\":\"自定义\",\"className\":\"custom_theme\",\"color\":\"#44E6DB\",\"dark_color\":\"#32C8BE\","
            + "\"nav_text_color\":\"#000000\",\"nav_bg_color\":\"#ffffff\",\"variable\":{\"--theme-color\":\"#44E6DB\","
            + "\"--theme-light-color\":\"#ACF1ED\",\"--theme-disabled-color\":\"#C7F2EF\",\"--theme-dark-color\":\"#32C8BE\"}}"
            + "]}";

    private MallStyleDefaults() {
    }

    public static Map<String, Object> all() {
        try {
            Map<String, Object> map = MAPPER.readValue(DEFAULT_ALL_JSON, new TypeReference<Map<String, Object>>() {});
            return ensurePcFields(map);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    /**
     * 补齐 PC 布局/导航/楼层字段（不覆盖已有非空配置）
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> ensurePcFields(Map<String, Object> style) {
        if (style == null) {
            return all();
        }
        if (style.get("pc_template_code") == null) {
            style.put("pc_template_code", "");
        }
        if (isBlank(style.get("pc_layout"))) {
            style.put("pc_layout", "hybrid");
        }
        if (isBlank(style.get("pc_container"))) {
            style.put("pc_container", "1280");
        }
        if (isBlank(style.get("pc_radius"))) {
            style.put("pc_radius", "md");
        }
        if (isBlank(style.get("pc_currency"))) {
            style.put("pc_currency", "CNY");
        }
        if (style.get("pc_show_rank") == null) {
            style.put("pc_show_rank", true);
        }
        if (style.get("pc_show_recommend") == null) {
            style.put("pc_show_recommend", true);
        }
        if (!(style.get("pc_nav") instanceof List<?> nav) || nav.isEmpty()) {
            style.put("pc_nav", defaultPcNav());
        }
        if (!(style.get("pc_floors") instanceof List<?> floors) || floors.isEmpty()) {
            style.put("pc_floors", defaultPcFloors());
        }
        return style;
    }

    /** 对 index/config 返回体中的 mall_style 补齐 PC 楼层等字段 */
    @SuppressWarnings("unchecked")
    public static void enrichIndexConfig(Map<String, Object> data) {
        if (data == null) {
            return;
        }
        Object raw = data.get("mall_style");
        if (raw instanceof Map<?, ?> map) {
            Map<String, Object> style = new LinkedHashMap<>();
            map.forEach((k, v) -> style.put(String.valueOf(k), v));
            data.put("mall_style", ensurePcFields(style));
        } else {
            data.put("mall_style", all());
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean isLikeshopFormat(Object data) {
        if (!(data instanceof Map<?, ?> map)) {
            return false;
        }
        Object list = map.get("list");
        return list instanceof List<?> items && !items.isEmpty();
    }

    private static boolean isBlank(Object v) {
        return v == null || String.valueOf(v).isBlank();
    }

    private static List<Map<String, Object>> defaultPcNav() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(pcNav("首页", "Home", "/"));
        list.add(pcNav("商家店铺", "Stores", "/store/list"));
        list.add(pcNav("特色馆", "Featured", "/feature"));
        list.add(pcNav("积分商城", "Points", "/points"));
        list.add(pcNav("限时秒杀", "Flash Sale", "/activity/seckill"));
        list.add(pcNav("限时折扣", "Deals", "/activity/discount"));
        list.add(pcNav("头条资讯", "News", "/information"));
        return list;
    }

    private static List<Map<String, Object>> defaultPcFloors() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(pcFloor("热门推荐", "大家都在买", "hotGoods"));
        list.add(pcFloor("新品上架", "潮流好物抢先看", "newGoods"));
        list.add(pcFloor("猜你喜欢", "为你精选好物", "guessGoods"));
        return list;
    }

    private static Map<String, Object> pcFloor(String name, String subtitle, String source) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("subtitle", subtitle);
        item.put("source", source);
        item.put("categoryId", 0);
        item.put("visible", true);
        item.put("bannerImage", "");
        item.put("bannerLink", "");
        return item;
    }

    private static Map<String, Object> pcNav(String name, String nameEn, String path) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("name_en", nameEn);
        item.put("path", path);
        item.put("image", "");
        item.put("target", 0);
        item.put("visible", true);
        return item;
    }
}
