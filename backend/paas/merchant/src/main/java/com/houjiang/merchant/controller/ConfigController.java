package com.houjiang.merchant.controller;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.common.annotation.MerchantPermission;
import com.houjiang.merchant.service.IConfigService;
import com.houjiang.merchant.service.ISettingDictDataService;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.validate.config.ConfigSetServiceValidate;
import com.houjiang.merchant.vo.setting.SettingDictDataVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置数据管理控制器
 * getConfig接口已在SecurityConfig白名单中配置，无需认证
 * 
 * @author Amos
 * @since 2026-04-28
 */
@Slf4j
@RestController
@RequestMapping("merchant/config")
@Tag(name = "配置数据管理")
public class ConfigController {

    @Resource
    IConfigService iConfigService;

    @Resource
    ISettingDictDataService iSettingDictDataService;

    /**
     * 公共配置（白名单接口，无需认证）
     * 
     * @return AjaxResult<JSONObject> 配置数据
     */
    @GetMapping("/getConfig")
    @Operation(summary="公共配置")
    public AjaxResult<JSONObject> getConfig() {
        JSONObject map = iConfigService.getConfigData();
        return AjaxResult.success(map);
    }

    /**
     * 字典数据（商家后台 useDictData 调用）
     * 前端传 type，返回 { [type]: [{name,value,...}] }
     */
    @GetMapping("/dict")
    @Operation(summary = "字典数据")
    public AjaxResult<JSONObject> dict(@RequestParam(value = "type", required = false) String type,
                                       @RequestParam(value = "dictType", required = false) String dictType) {
        String key = StringUtils.isNotEmpty(type) ? type : dictType;
        JSONObject ret = new JSONObject();
        if (StringUtils.isEmpty(key)) {
            return AjaxResult.success(ret);
        }
        Map<String, String> params = new HashMap<>();
        params.put("dictType", key);
        try {
            List<SettingDictDataVo> list = iSettingDictDataService.all(params);
            ret.put(key, list);
        } catch (Exception e) {
            log.warn("字典类型不存在或查询失败 type={}: {}", key, e.getMessage());
            ret.put(key, List.of());
        }
        return AjaxResult.success(ret);
    }

    /**
     * 获取客服设置
     * 
     * @return AjaxResult<JSONObject> 客服设置数据
     */
    @GetMapping("/get_service")
    @MerchantPermission(anyOf = "config/get_service:set_service")
    @Operation(summary="客服设置")
    public AjaxResult<JSONObject> getService() {
        JSONObject object = iConfigService.getService(MerchantThreadLocal.getShopId());
        return AjaxResult.success(object);
    }

    /**
     * 设置客服配置
     * 
     * @param serviceValidate 客服设置参数
     * @return AjaxResult<Object> 操作结果
     */
    @PostMapping("/set_service")
    @MerchantPermission(anyOf = "config/get_service:set_service")
    @Operation(summary="客服设置")
    public AjaxResult<Object> setService(@Validated @RequestBody ConfigSetServiceValidate serviceValidate) {
        serviceValidate.setShopId(MerchantThreadLocal.getShopId());
        iConfigService.setService(serviceValidate);
        return AjaxResult.success();
    }

}
