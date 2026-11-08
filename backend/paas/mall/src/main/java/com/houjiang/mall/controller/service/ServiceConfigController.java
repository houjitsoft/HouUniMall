package com.houjiang.mall.controller.service;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.service.ServiceConfig;
import com.houjiang.mall.service.service.ServiceConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 客服设置 Controller
 * 路径：/mall/service/config/*
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/service/config")
@RequiredArgsConstructor
public class ServiceConfigController {

    private final ServiceConfigService serviceConfigService;

    /**
     * 获取客服设置信息
     * POST /mall/service/config/get
     * 
     * @param params 查询参数（包含 shop_id）
     * @return 客服配置信息
     */
    @PostMapping("/get")
    public ResponseResult<ServiceConfig> getcustomerService(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取客服设置信息，参数：{}", params);
            
            if (!params.containsKey("shop_id")) {
                return ResponseResult.error("缺少店铺 ID 参数");
            }
            
            Long shopId = Long.parseLong(params.get("shop_id").toString());
            ServiceConfig config = serviceConfigService.getConfig(shopId);
            
            // 如果没有配置，返回空对象而不是错误
            if (config == null) {
                config = new ServiceConfig();
                config.setShopId(shopId);
            }
            
            return ResponseResult.success(config);
        } catch (Exception e) {
            log.error("获取客服设置信息失败", e);
            return ResponseResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 保存客服设置配置
     * POST /mall/service/config/save
     * 
     * @param params 客服配置对象
     * @return 是否成功
     */
    @PostMapping("/save")
    public ResponseResult<Boolean> setcustomerService(@RequestBody Map<String, Object> params) {
        try {
            log.info("保存客服设置配置，参数：{}", params);
            
            ServiceConfig serviceConfig = convertToServiceConfig(params);
            boolean result = serviceConfigService.saveConfig(serviceConfig);
            
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("保存客服设置配置失败", e);
            return ResponseResult.error("保存失败：" + e.getMessage());
        }
    }

    /**
     * 将 Map 参数转换为 ServiceConfig 对象
     */
    private ServiceConfig convertToServiceConfig(Map<String, Object> params) {
        ServiceConfig config = new ServiceConfig();
        
        if (params.containsKey("id")) {
            config.setId(Long.parseLong(params.get("id").toString()));
        }
        if (params.containsKey("shop_id")) {
            config.setShopId(Long.parseLong(params.get("shop_id").toString()));
        }
        if (params.containsKey("service_qq")) {
            config.setServiceQq(params.get("service_qq").toString());
        }
        if (params.containsKey("service_wechat")) {
            config.setServiceWechat(params.get("service_wechat").toString());
        }
        if (params.containsKey("service_phone")) {
            config.setServicePhone(params.get("service_phone").toString());
        }
        if (params.containsKey("service_email")) {
            config.setServiceEmail(params.get("service_email").toString());
        }
        if (params.containsKey("service_hours")) {
            config.setServiceHours(params.get("service_hours").toString());
        }
        if (params.containsKey("auto_reply")) {
            config.setAutoReply(params.get("auto_reply").toString());
        }
        
        return config;
    }
}
