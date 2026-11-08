package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.IndexConfigDTO;
import com.houjiang.base.entity.BasePcIndexConfigEntity;
import com.houjiang.commons.util.ResponseResult;

/**
 * @ClassName BasePcIndexConfigService
 * @Description
 * @Author Amos
 * @Date 2023-05-24 09:39:22
 * @Version 1.0
 * @author Amos
 */
public interface BasePcIndexConfigService extends IService<BasePcIndexConfigEntity> {

    /**
     * 平台修改 首页 天天特价 特色专题 为你推荐 添加
     *
     * @param pcIndexConfig
     * @return
     */
    ResponseResult updatePcIndexConfig(IndexConfigDTO pcIndexConfig);
}

