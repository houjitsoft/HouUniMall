package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.goods.GoodsAttrDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.goods.GoodsAttrVO;
import com.houjiang.goods.entity.GoodsAttributeGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性组Service
 * @author Amos
 */
public interface GoodsAttributeGroupService extends IService<GoodsAttributeGroupEntity> {
    
    /**
     * 查询属性组列表
     */
    ResponseResult queryList(Map<String, Object> param);
    
    /**
     * 创建属性组
     */
    ResponseResult create(Map<String, Object> param);
    
    /**
     * 更新属性组
     */
    ResponseResult update(Map<String, Object> param);
    
    /**
     * 删除属性组
     */
    ResponseResult delete(Map<String, Object> param);
    
    /**
     * 查询商品属性组列表（用于ES搜索）
     */
    List<GoodsAttrVO> selectGoodsAttributeGroupList(GoodsAttrDTO dto);
    
    /**
     * 根据属性列表查询商品公共ID列表（用于ES搜索）
     */
    List<Integer> selectCommonIdListByAttrs(List<Map<String, Object>> attrList);
}
