package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.BaseAiConfigDTO;
import com.houjiang.base.dto.BaseAiProviderCredentialDTO;
import com.houjiang.base.entity.BaseAiConfigEntity;
import com.houjiang.commons.util.ResponseResult;

/**
 * AI服务配置Service接口
 *
 * @author Amos
 * @since 2026-01-07
 */
public interface BaseAiConfigService extends IService<BaseAiConfigEntity> {

    /**
     * 分页查询AI配置列表
     */
    ResponseResult queryAiConfigList(BaseAiConfigDTO param);

    /**
     * 查询所有启用的AI配置
     */
    ResponseResult queryEnabledConfigs();

    /**
     * 获取默认AI配置
     */
    ResponseResult getDefaultConfig();

    /**
     * 保存或更新AI配置
     */
    ResponseResult saveOrUpdateConfig(BaseAiConfigDTO param);

    /**
     * 按品牌（API 端点）批量下发共享凭证到该品牌全部模型模板
     */
    ResponseResult saveProviderCredential(BaseAiProviderCredentialDTO param);

    /**
     * 删除AI配置
     */
    ResponseResult deleteConfig(Integer id);

    /**
     * 启用/禁用AI配置
     */
    ResponseResult toggleEnabled(Integer id, Integer isEnabled);

    /**
     * 设置为默认配置
     */
    ResponseResult setAsDefault(Integer id);

    /**
     * 测试AI配置连接
     */
    ResponseResult testConnection(Integer id);
}
