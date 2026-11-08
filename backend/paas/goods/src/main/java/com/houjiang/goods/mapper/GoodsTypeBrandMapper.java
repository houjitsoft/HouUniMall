package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.goods.entity.GoodsTypeBrandEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.goods.entity.GoodsTypeBrand
 * @author Amos
 */
@Mapper
public interface GoodsTypeBrandMapper extends BaseMapper<GoodsTypeBrandEntity> {

    /**
     * 查询类型品牌
     *
     * @param typeId
     * @return
     */
    List<GoodsTypeBrandEntity> selectTypeBrand(@Param("typeId") Integer typeId);

    /**
     * 新增类型品牌
     *
     * @param typeBrandList
     */
    void insertTypeBrandBatch(@Param("typeBrandList") List<GoodsTypeBrandEntity> typeBrandList);

    /**
     * 删除类型品牌
     *
     * @param typeId
     */
    void delectTypeSpecBrand(@Param("typeId") Integer typeId);

    /**
     * 修改类型品牌
     *
     * @param updateBrands
     */
    void updateTypeBrandBatch(@Param("typeBrandList") List<GoodsTypeBrandEntity> updateBrands);

    /**
     * 通过类型Id删除
     *
     * @param typeId
     */
    void deleteByTypeId(@Param("typeId") Integer typeId);
}




