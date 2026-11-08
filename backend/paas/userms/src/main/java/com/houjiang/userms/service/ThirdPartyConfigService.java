package com.houjiang.userms.service;

import com.houjiang.commons.dto.ThirdPartyConfigDTO;

/**
 * 第三方集成配置Service接口
 * 
 * @author Amos
 * @since 2025-01-07
 */
public interface ThirdPartyConfigService {

    /**
     * 根据平台获取配置
     * 
     * @param platform 平台名称
     * @return 配置信息
     */
    ThirdPartyConfigDTO getConfigByPlatform(String platform);

    /**
     * 保存平台配置
     * 
     * @param platform 平台名称
     * @param config 配置信息
     * @return 是否成功
     */
    boolean saveConfig(String platform, ThirdPartyConfigDTO config);

    /**
     * 测试平台连接
     * 
     * @param platform 平台名称
     * @param config 配置信息
     * @return 是否成功
     */
    boolean testConnection(String platform, ThirdPartyConfigDTO config);

    /**
     * 删除平台配置
     * 
     * @param platform 平台名称
     * @return 是否成功
     */
    boolean deleteConfig(String platform);
}