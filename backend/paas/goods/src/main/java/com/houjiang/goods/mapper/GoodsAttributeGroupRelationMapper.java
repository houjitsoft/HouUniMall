package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.goods.entity.GoodsAttributeGroupRelationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性组关联Mapper
 * @author Amos
 */
@Mapper
public interface GoodsAttributeGroupRelationMapper extends BaseMapper<GoodsAttributeGroupRelationEntity> {
    
    /**
     * 根据属性组ID删除关联关系
     */
    int deleteByAttributeGroupId(@Param("attributeGroupId") Integer attributeGroupId);
    
    /**
     * 批量插入属性组关联
     */
    int insertBatch(@Param("relations") List<GoodsAttributeGroupRelationEntity> relations);
    
    /**
     * 根据属性组ID查询关联列表
     */
    List<GoodsAttributeGroupRelationEntity> selectByAttributeGroupId(@Param("attributeGroupId") Integer attributeGroupId);
}
