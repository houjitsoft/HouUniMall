package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.entity.BaseProtocolEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * 协议管理Service
 * @author Amos
 */
public interface BaseProtocolService extends IService<BaseProtocolEntity> {
    
    /**
     * 查询协议列表
     */
    ResponseResult queryList(Map<String, Object> param);
    
    /**
     * 保存或更新协议
     */
    ResponseResult saveOrUpdate(Map<String, Object> param);
    
    /**
     * 删除协议
     */
    ResponseResult deleteById(Map<String, Object> param);
}
