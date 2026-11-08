package com.houjiang.mall.service.decoration;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.houjiang.mall.service.setting.CommonConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 * 商城风格（likeshop：hou_merchant_config type=mall_style name=all/default）
 * @author Amos
 */
@Service
@RequiredArgsConstructor
public class MallStyleAdminService {

    private static final String TYPE = "mall_style";
    private static final String KEY_ALL = "all";
    private static final String KEY_DEFAULT = "default";

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

    private final CommonConfigService commonConfigService;

    public Map<String, Object> getMallStyle() {
        String raw = commonConfigService.getOrDefault(TYPE, KEY_ALL, DEFAULT_ALL_JSON);
        try {
            JSONObject json = JSON.parseObject(raw);
            if (json != null && json.containsKey("list")) {
                ensurePcDefaults(json);
                return json;
            }
        } catch (Exception ignored) {
            // fall through
        }
        JSONObject fallback = JSON.parseObject(DEFAULT_ALL_JSON);
        ensurePcDefaults(fallback);
        return fallback;
    }

    /**
     * PC 端布局/导航默认值（旧数据只有 current+list 时补齐，不覆盖已保存字段）
     */
    private void ensurePcDefaults(JSONObject json) {
        if (json == null) {
            return;
        }
        if (!StringUtils.hasText(json.getString("pc_layout"))) {
            json.put("pc_layout", "hybrid");
        }
        if (!StringUtils.hasText(json.getString("pc_container"))) {
            json.put("pc_container", "1280");
        }
        if (!StringUtils.hasText(json.getString("pc_radius"))) {
            json.put("pc_radius", "md");
        }
        if (!StringUtils.hasText(json.getString("pc_currency"))) {
            json.put("pc_currency", "CNY");
        }
        if (json.get("pc_show_rank") == null) {
            json.put("pc_show_rank", true);
        }
        if (json.get("pc_show_recommend") == null) {
            json.put("pc_show_recommend", true);
        }
        if (isMissingOrEmptyList(json.get("pc_nav"))) {
            json.put("pc_nav", defaultPcNav());
        }
        if (isMissingOrEmptyList(json.get("pc_floors"))) {
            json.put("pc_floors", defaultPcFloors());
        }
    }

    /**
     * HTTP 请求体由 Jackson 反序列化为 ArrayList，读取数据库 JSON 时则是 JSONArray；
     * 两者都属于合法的列表值，不能因为具体实现类型不同而覆盖用户配置。
     */
    private static boolean isMissingOrEmptyList(Object value) {
        return !(value instanceof Collection<?> collection) || collection.isEmpty();
    }

    private static JSONArray defaultPcNav() {
        JSONArray arr = new JSONArray();
        arr.add(pcNav("首页", "Home", "/"));
        arr.add(pcNav("商家店铺", "Stores", "/store/list"));
        arr.add(pcNav("特色馆", "Featured", "/feature"));
        arr.add(pcNav("积分商城", "Points", "/points"));
        arr.add(pcNav("限时秒杀", "Flash Sale", "/activity/seckill"));
        arr.add(pcNav("限时折扣", "Deals", "/activity/discount"));
        arr.add(pcNav("头条资讯", "News", "/information"));
        return arr;
    }

    private static JSONArray defaultPcFloors() {
        JSONArray arr = new JSONArray();
        arr.add(pcFloor("热门推荐", "大家都在买", "hotGoods"));
        arr.add(pcFloor("新品上架", "潮流好物抢先看", "newGoods"));
        arr.add(pcFloor("猜你喜欢", "为你精选好物", "guessGoods"));
        return arr;
    }

    private static JSONObject pcFloor(String name, String subtitle, String source) {
        JSONObject item = new JSONObject();
        item.put("name", name);
        item.put("subtitle", subtitle);
        item.put("source", source);
        item.put("categoryId", 0);
        item.put("visible", true);
        item.put("bannerImage", "");
        item.put("bannerLink", "");
        return item;
    }

    private static JSONObject pcNav(String name, String nameEn, String path) {
        JSONObject item = new JSONObject();
        item.put("name", name);
        item.put("name_en", nameEn);
        item.put("path", path);
        item.put("visible", true);
        return item;
    }

    @SuppressWarnings("unchecked")
    public void setMallStyle(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空");
        }
        Object dataObj = params.get("data");
        if (dataObj == null) {
            dataObj = params;
        }
        JSONObject dataJson;
        if (dataObj instanceof Map<?, ?> map) {
            dataJson = new JSONObject((Map<String, Object>) map);
        } else if (dataObj instanceof String str) {
            dataJson = JSON.parseObject(str);
        } else {
            dataJson = JSON.parseObject(JSON.toJSONString(dataObj));
        }
        if (dataJson == null || !dataJson.containsKey("list")) {
            throw new IllegalArgumentException("商城风格数据格式错误");
        }
        ensurePcDefaults(dataJson);
        commonConfigService.set(TYPE, KEY_ALL, dataJson.toJSONString());

        String current = dataJson.getString("current");
        JSONArray list = dataJson.getJSONArray("list");
        JSONObject currentObj = new JSONObject();
        if (StringUtils.hasText(current) && list != null) {
            for (int i = 0; i < list.size(); i++) {
                JSONObject item = list.getJSONObject(i);
                if (item != null && current.equals(item.getString("className"))) {
                    currentObj = item;
                    break;
                }
            }
        }
        if (currentObj.isEmpty() && list != null && !list.isEmpty()) {
            currentObj = list.getJSONObject(0);
        }
        commonConfigService.set(TYPE, KEY_DEFAULT, currentObj.toJSONString());
    }
}
