package com.houjiang.merchant.controller.mall;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.util.ConfigUtils;
import com.houjiang.merchant.common.util.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 配送方式 / 物流追踪配置
 * 供 BFF MallDeliveryController 与商家后台转发使用。
 */
@RestController
@RequestMapping(value = {"merchant/mall.DeliveryType", "merchant/mall.deliveryType"})
@Tag(name = "配送方式配置")
public class MallDeliveryTypeController {

    @GetMapping("/get_config")
    @Operation(summary = "获取配送方式配置")
    public AjaxResult<JSONObject> getConfig() {
        JSONObject ret = new JSONObject();
        ret.put("express_is", Integer.parseInt(ConfigUtils.get("mall_delivery_type", "express_is", "1")));
        ret.put("express_name", ConfigUtils.get("mall_delivery_type", "express_name", "快递发货"));
        ret.put("store_is", Integer.parseInt(ConfigUtils.get("mall_delivery_type", "store_is", "0")));
        ret.put("store_name", ConfigUtils.get("mall_delivery_type", "store_name", "到店自提"));
        return AjaxResult.success(ret);
    }

    @PostMapping("/set_config")
    @Operation(summary = "设置配送方式配置")
    public AjaxResult<Object> setConfig(@RequestBody Map<String, Object> params) {
        if (params == null) {
            params = Map.of();
        }
        if (params.get("express_is") != null) {
            ConfigUtils.set("mall_delivery_type", "express_is", String.valueOf(params.get("express_is")));
        }
        if (params.get("express_name") != null) {
            ConfigUtils.set("mall_delivery_type", "express_name", String.valueOf(params.get("express_name")));
        }
        if (params.get("store_is") != null) {
            ConfigUtils.set("mall_delivery_type", "store_is", String.valueOf(params.get("store_is")));
        }
        if (params.get("store_name") != null) {
            ConfigUtils.set("mall_delivery_type", "store_name", String.valueOf(params.get("store_name")));
        }
        return AjaxResult.success();
    }

    @GetMapping("/get_logistics_config")
    @Operation(summary = "获取物流追踪配置")
    public AjaxResult<JSONObject> getLogisticsConfig() {
        JSONObject ret = new JSONObject();
        String expressConfig = ConfigUtils.get("mall_delivery_type", "express_config", "");
        ret.put("express_config", StringUtils.isNull(expressConfig) ? "" : expressConfig);
        ret.put("express_engine", ConfigUtils.get("mall_delivery_type", "express_engine", ""));
        return AjaxResult.success(ret);
    }

    @PostMapping("/set_logistics_config")
    @Operation(summary = "设置物流追踪配置")
    public AjaxResult<Object> setLogisticsConfig(@RequestBody Map<String, Object> params) {
        if (params == null) {
            params = Map.of();
        }
        if (params.get("express_config") != null) {
            Object config = params.get("express_config");
            String value = config instanceof String ? (String) config : JSONObject.toJSONString(config);
            ConfigUtils.set("mall_delivery_type", "express_config", value);
        }
        if (params.get("express_engine") != null) {
            ConfigUtils.set("mall_delivery_type", "express_engine", String.valueOf(params.get("express_engine")));
        }
        return AjaxResult.success();
    }
}
