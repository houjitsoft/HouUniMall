package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.entity.BaseKd100ConfigEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * 快递100配置Service
 * @author Amos
 */
public interface BaseKd100ConfigService extends IService<BaseKd100ConfigEntity> {

    /**
     * 查询快递100配置
     * @param param 查询参数
     * @return 配置信息
     */
    ResponseResult queryConfig(Map<String, Object> param);

    /**
     * 更新快递100配置
     * @param param 配置参数
     * @return 操作结果
     */
    ResponseResult updateConfig(Map<String, Object> param);
}
