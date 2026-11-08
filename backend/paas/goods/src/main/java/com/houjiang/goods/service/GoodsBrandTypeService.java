package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.GoodsBrandTypeEntity;

import java.util.Map;

/**
 * 品牌类型Service
 * @author Amos
 */
public interface GoodsBrandTypeService extends IService<GoodsBrandTypeEntity> {
    
    /**
     * 查询品牌类型列表
     */
    ResponseResult queryList(Map<String, Object> param);
    
    /**
     * 创建品牌类型
     */
    ResponseResult create(Map<String, Object> param);
    
    /**
     * 更新品牌类型
     */
    ResponseResult update(Map<String, Object> param);
    
    /**
     * 删除品牌类型
     */
    ResponseResult delete(Map<String, Object> param);
}
