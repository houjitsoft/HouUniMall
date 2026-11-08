package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.GoodsAttributeTypeEntity;

import java.util.Map;

/**
 * 属性类型Service
 * @author Amos
 */
public interface GoodsAttributeTypeService extends IService<GoodsAttributeTypeEntity> {
    
    /**
     * 分页查询属性类型列表
     */
    ResponseResult queryPage(Map<String, Object> param);
    
    /**
     * 查询所有属性类型列表（不分页）
     */
    ResponseResult queryList();
    
    /**
     * 创建属性类型
     */
    ResponseResult create(Map<String, Object> param);
    
    /**
     * 更新属性类型
     */
    ResponseResult update(Map<String, Object> param);
    
    /**
     * 删除属性类型
     */
    ResponseResult delete(Map<String, Object> param);
}
