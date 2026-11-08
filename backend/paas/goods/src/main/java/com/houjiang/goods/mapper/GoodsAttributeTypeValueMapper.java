package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.goods.entity.GoodsAttributeTypeValueEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性类型值Mapper
 * @author Amos
 */
@Mapper
public interface GoodsAttributeTypeValueMapper extends BaseMapper<GoodsAttributeTypeValueEntity> {
    
    /**
     * 根据属性类型ID删除所有属性值
     */
    int deleteByAttributeTypeId(@Param("attributeTypeId") Integer attributeTypeId);
    
    /**
     * 批量插入属性值
     */
    int insertBatch(@Param("values") List<GoodsAttributeTypeValueEntity> values);
    
    /**
     * 根据属性类型ID查询属性值列表
     */
    List<GoodsAttributeTypeValueEntity> selectByAttributeTypeId(@Param("attributeTypeId") Integer attributeTypeId);
}
