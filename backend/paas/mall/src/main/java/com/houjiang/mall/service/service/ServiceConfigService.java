package com.houjiang.mall.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.service.ServiceConfig;
import com.houjiang.mall.mapper.service.ServiceConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 客服配置 Service
 * 
 * @author Amos
 */
@Service
public class ServiceConfigService extends ServiceImpl<ServiceConfigMapper, ServiceConfig> {
    
    /**
     * 获取客服配置（单例模式）
     * @param shopId 店铺 ID
     * @return 客服配置
     */
    public ServiceConfig getConfig(Long shopId) {
        LambdaQueryWrapper<ServiceConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceConfig::getShopId, shopId);
        
        List<ServiceConfig> list = this.list(wrapper);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }
    
    /**
     * 保存客服配置（单例模式）
     * @param serviceConfig 客服配置对象
     * @return 是否成功
     */
    public boolean saveConfig(ServiceConfig serviceConfig) {
        // 1. 先查询该店铺是否已有配置
        ServiceConfig existConfig = getConfig(serviceConfig.getShopId());
        
        if (existConfig != null) {
            // 2. 如果已有配置，更新它
            serviceConfig.setId(existConfig.getId());
            return this.updateById(serviceConfig);
        } else {
            // 3. 如果没有配置，新增
            return this.save(serviceConfig);
        }
    }
}
