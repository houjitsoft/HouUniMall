package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.goods.entity.GoodsBrandTypeRelationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 品牌类型关联Mapper
 * @author Amos
 */
@Mapper
public interface GoodsBrandTypeRelationMapper extends BaseMapper<GoodsBrandTypeRelationEntity> {
    
    /**
     * 根据品牌类型ID删除关联关系
     */
    int deleteByBrandTypeId(@Param("brandTypeId") Integer brandTypeId);
    
    /**
     * 批量插入品牌关联
     */
    int insertRelations(@Param("brandTypeId") Integer brandTypeId, @Param("brandIds") List<Integer> brandIds);
    
    /**
     * 根据品牌类型ID查询品牌ID列表
     */
    List<Integer> selectBrandIdsByTypeId(@Param("brandTypeId") Integer brandTypeId);
}
