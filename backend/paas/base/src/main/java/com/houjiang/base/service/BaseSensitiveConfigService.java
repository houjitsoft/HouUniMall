package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.BaseSensitiveConfigDTO;
import com.houjiang.base.entity.BaseSensitiveConfigEntity;
import com.houjiang.commons.util.ResponseResult;

/**
 * @ClassName BaseSensitiveConfigService
 * @Description  敏感词配置
 * @Author Amos
 * @Date 2024-01-30 16:48:22
 * @Version 1.0
 * @author Amos
 */
public interface BaseSensitiveConfigService extends IService<BaseSensitiveConfigEntity> {
    /**
     * 平台 查询敏感词配置
     * @param param
     * @return
     */
    ResponseResult querySensitiveConfig(BaseSensitiveConfigDTO param);

    /**
     * 平台 修改敏感词配置
     * @param param
     * @return
     */
    ResponseResult updateSensitiveConfig(BaseSensitiveConfigDTO param);
}
