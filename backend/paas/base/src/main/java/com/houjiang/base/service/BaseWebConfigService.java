package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.BaseWebConfigDTO;
import com.houjiang.base.entity.BaseWebConfigEntity;
import com.houjiang.commons.util.ResponseResult;

/**
 * 网站配置接口
 * @author Amos
 */
public interface BaseWebConfigService extends IService<BaseWebConfigEntity> {

    /**
     * 保存配置
     * @param param
     * @return
     */
    ResponseResult editConfig(BaseWebConfigDTO param);
}
