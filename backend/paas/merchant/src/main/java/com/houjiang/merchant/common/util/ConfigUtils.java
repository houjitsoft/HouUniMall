package com.houjiang.merchant.common.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.cache.ConfigCache;
import com.houjiang.merchant.common.entity.Config;
import com.houjiang.merchant.common.entity.setting.MallPay;
import com.houjiang.merchant.common.enums.DeliveryEnum;
import com.houjiang.merchant.common.enums.PaymentEnum;
import com.houjiang.merchant.common.enums.UserWithDrawEnum;
import com.houjiang.merchant.common.mapper.ConfigMapper;
import com.houjiang.merchant.common.mapper.setting.MallPayMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库配置操作工具
 */
public class ConfigUtils {

    /**
     * 根据类型获取配置
     *
     * @author Amos
     * @param type 类型
     * @return Map<String, String>
     */
    public static Map<String, String> get(String type) {
        Map<String, String> cache = ConfigCache.get(type);
        if (!cache.isEmpty()) {
            return cache;
        }

        ConfigMapper model = SpringUtils.getBean(ConfigMapper.class);
        List<Config> configs = model.selectList(
                new QueryWrapper<Config>()
                        .select("id", "type"+"", "name", "value")
                        .eq("type", type));

        Map<String, String> map = new LinkedHashMap<>();
        for (Config config : configs) {
            map.put(config.getName(), config.getValue());
        }

        ConfigCache.set();
        return map;
    }

    /**
     * 根据类型和名称获取配置
     *
     * @author Amos
     * @param type 类型
     * @param name 名称
     * @return String
     */
    public static String get(String type, String name) {
        String cache = ConfigCache.get(type, name);
        if (!StringUtils.isNull(cache) && !StringUtils.isEmpty(cache)) {
            return cache;
        }

        ConfigMapper model = SpringUtils.getBean(ConfigMapper.class);
        Config config = model.selectOne(
                new QueryWrapper<Config>()
                        .select("id", "type", "name", "value")
                        .eq("name", name)
                        .eq("type", type));

        ConfigCache.set();
        return config.getValue();
    }

    /**
     * 根据类型和名称获取配置
     *
     * @author Amos
     * @param type 类型
     * @param name 名称
     * @return String
     */
    public static String get(String type, String name, String defaults) {
        String cache = ConfigCache.get(type, name);
//        if (!StringUtils.isNull(cache) && !StringUtils.isEmpty(cache)) {
//            return cache;
//        }

        ConfigMapper model = SpringUtils.getBean(ConfigMapper.class);
        Config config = model.selectOne(
                new QueryWrapper<Config>()
                        .select("id", "type", "name", "value")
                        .eq("type", type)
                        .eq("name", name));

        if (config == null || StringUtils.isEmpty(config.getValue())) {
            return defaults;
        }

        ConfigCache.set();

        return config.getValue();
    }

    /**
     * 根据类型和名称获取配置(JSON自定转Map)
     *
     * @author Amos
     * @param type 类型
     * @param name 名称
     * @return String
     */
    public static Map<String, String> getMap(String type, String name) {
        String cache = ConfigCache.get(type, name);
        if (!StringUtils.isNull(cache) && !StringUtils.isEmpty(cache)) {
            cache = "[]".equals(cache) ? "{}" : cache;
            return MapUtils.jsonToMap(cache);
        }

        ConfigMapper model = SpringUtils.getBean(ConfigMapper.class);

        Config config = model.selectOne(
                new QueryWrapper<Config>()
                        .select("id", "type", "name", "value")
                        .eq("type", type)
                        .eq("name", name));

        if (config == null) {
            return null;
        }

        if (config.getValue().equals("") || config.getValue().equals("[]") || config.getValue().equals("{}")) {
            return new LinkedHashMap<>();
        }

        ConfigCache.set();
        return MapUtils.jsonToMap(config.getValue());
    }

    /**
     * 设置配置的值
     *
     * @author Amos
     * @param type 类型
     * @param name 名称
     * @param val 值
     */
    public static void set(String type, String name, String val) {
        ConfigMapper model = SpringUtils.getBean(ConfigMapper.class);
        Config config = model.selectOne(
                new QueryWrapper<Config>()
                        .eq("type", type)
                        .eq("name", name));

        if (config != null) {
            config.setValue(val);
            config.setUpdateTime(System.currentTimeMillis() / 1000);
            model.updateById(config);
        } else {
            Config systemConfig = new Config();
            systemConfig.setType(type);
            systemConfig.setName(name);
            systemConfig.setValue(val);
            systemConfig.setCreateTime(System.currentTimeMillis() / 1000);
            systemConfig.setUpdateTime(System.currentTimeMillis() / 1000);
            model.insert(systemConfig);
        }

        ConfigCache.set();
    }

    /** 返回支付宝配置信息 */
    public static String getAliDevPay(String field) {
        MallPayMapper model = SpringUtils.getBean(MallPayMapper.class);
        MallPay devPay = model.selectOne(
                new QueryWrapper<MallPay>()
                        .eq("pay_type", PaymentEnum.ALI_PAY.getCode()));
        if (StringUtils.isNull(devPay)) {
            return "";
        } else {
            JSONObject jsonObject = JSONObject.parseObject(devPay.getConfig().toString());
            return StringUtils.isNull(jsonObject.getString(field)) ? "" : jsonObject.getString(field);
        }

    }

    public static JSONArray deliveryConfigText() {
        JSONArray ret = new JSONArray();
        ret.add(new ArrayList<Object>() {{
            add(DeliveryEnum.DELIVERY_TYPE_EXPRESS.getCode());
            add(ConfigUtils.get("mall_delivery_type", "express_name", "快递发货"));
        }});
        return ret;
    }

    /**
     * @notes 商家提现设置
     * @return array
     * @author Amos
     */
    public static JSONObject getShopWithdrawConfig() {
        Map<String, String> config = ConfigUtils.get("shop_withdraw");
        JSONObject ret = new JSONObject();
        ret.put("sw_min_money", StringUtils.isNull(config.get("sw_min_money")) ? new BigDecimal(100) : new BigDecimal(config.get("sw_min_money")));
        ret.put("sw_max_money", StringUtils.isNull(config.get("sw_max_money")) ? new BigDecimal(10000) : new BigDecimal(config.get("sw_max_money")));
        ret.put("sw_service_ratio", StringUtils.isNull(config.get("sw_service_ratio")) ? new BigDecimal(10) : new BigDecimal(config.get("sw_service_ratio")));
        return ret;
    }

    public static JSONObject serviceConfig() {
        JSONObject ret = new JSONObject();
        JSONObject defaultJSON = new JSONObject();
        defaultJSON.put("way", "1");
        defaultJSON.put("name", "");
        defaultJSON.put("remarks", "");
        defaultJSON.put("phone", "");
        defaultJSON.put("business_time", "");
        defaultJSON.put("qr_code", "");
        defaultJSON.put("enterprise_id", "");
        defaultJSON.put("kefu_link", "");
        JSONObject mnp = JSONObject.parseObject(ConfigUtils.get("service_config", "mnp", JSONObject.toJSONString(defaultJSON)));
        JSONObject oa = JSONObject.parseObject(ConfigUtils.get("service_config", "oa", JSONObject.toJSONString(defaultJSON)));
        JSONObject h5 = JSONObject.parseObject(ConfigUtils.get("service_config", "h5", JSONObject.toJSONString(defaultJSON)));
        if (StringUtils.isNotEmpty(mnp.getString("qr_code"))) {
            mnp.put("qr_code", UrlUtils.toAbsoluteUrl(mnp.getString("qr_code")));
        }
        if (StringUtils.isNotEmpty(oa.getString("qr_code"))) {
            oa.put("qr_code", UrlUtils.toAbsoluteUrl(oa.getString("qr_code")));
        }
        if (StringUtils.isNotEmpty(h5.getString("qr_code"))) {
            h5.put("qr_code", UrlUtils.toAbsoluteUrl(h5.getString("qr_code")));
        }
        ret.put("mnp", mnp);
        ret.put("oa", oa);
        ret.put("h5", h5);
        return ret;
    }

    public static JSONObject getUserWithdraw() {
        JSONObject ret = new JSONObject();
        Map<String, String> config = ConfigUtils.get("user_withdraw");
        JSONArray actionLists = StringUtils.isNull(config.get("action_lists")) ? new JSONArray() : JSONArray.parseArray(config.get("action_lists"));
        List<Integer> actionList = new ArrayList<Integer>();
        if (actionLists.size() > 0) {
            actionLists.forEach(item -> {
                actionList.add(Integer.valueOf(item.toString()));
            });
        }

        ret.put("action_lists", actionList);
        ret.put("action_text_lists", UserWithDrawEnum.TYPE_TEXT_SELECT_ARR);
        ret.put("min_money", StringUtils.isNull(config.get("min_money")) ? new BigDecimal(10) : new BigDecimal(config.get("min_money")));
        ret.put("max_money", StringUtils.isNull(config.get("max_money")) ? new BigDecimal(100) : new BigDecimal(config.get("max_money")));
        ret.put("service_ratio", StringUtils.isNull(config.get("service_ratio")) ? new BigDecimal(10) : new BigDecimal(config.get("service_ratio")));
        ret.put("wechat_interface", 1);
        return ret;
    }
}
