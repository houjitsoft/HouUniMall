package com.houjiang.thirdparty.service.esign;

import com.houjiang.commons.dto.ThirdPartyConfigDTO;
import com.houjiang.thirdparty.service.ThirdPartyConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 电子签名服务工厂
 * 根据配置选择具体的电子签名服务实现
 * 
 * @author Amos
 * @since 2025-01-07
 */
@Slf4j
@Component
public class EsignServiceFactory {
    
    @Autowired
    private ThirdPartyConfigService thirdPartyConfigService;
    
    @Autowired
    private List<EsignService> esignServices;
    
    private final Map<String, EsignService> serviceCache = new HashMap<>();
    
    /**
     * 获取电子签名服务实例
     * 
     * @param orgId 组织ID
     * @return 电子签名服务实例
     */
    public EsignService getEsignService(String orgId) {
        try {
            // 获取电子签名配置
            ThirdPartyConfigDTO config = thirdPartyConfigService.getConfigByPlatform("esign");
            
            if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
                log.warn("电子签名服务未配置或未启用，orgId: {}", orgId);
                return null;
            }
            
            ThirdPartyConfigDTO.EsignConfig esignConfig = config.getEsign();
            if (esignConfig == null || esignConfig.getProvider() == null) {
                log.warn("电子签名服务提供商未配置，orgId: {}", orgId);
                return null;
            }
            
            String provider = esignConfig.getProvider();
            String cacheKey = provider + "_" + orgId;
            
            // 从缓存获取
            if (serviceCache.containsKey(cacheKey)) {
                return serviceCache.get(cacheKey);
            }
            
            // 查找对应的服务实现
            EsignService service = esignServices.stream()
                    .filter(s -> provider.equals(s.getProviderCode()))
                    .findFirst()
                    .orElse(null);
            
            if (service == null) {
                log.error("未找到电子签名服务实现，provider: {}", provider);
                return null;
            }
            
            // 缓存服务实例
            serviceCache.put(cacheKey, service);
            return service;
            
        } catch (Exception e) {
            log.error("获取电子签名服务失败，orgId: {}", orgId, e);
            return null;
        }
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        serviceCache.clear();
    }
}

