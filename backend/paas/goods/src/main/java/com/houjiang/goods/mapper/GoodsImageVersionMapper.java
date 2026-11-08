package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.goods.entity.GoodsImageVersionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品图片版本(GoodsImageVersion)表数据库访问层
 *
 * @author Amos
 * @since 2025-05-28 17:24:54
 */
@Mapper
public interface GoodsImageVersionMapper extends BaseMapper<GoodsImageVersionEntity> {


    void insertBatch(@Param("list") List<GoodsImageVersionEntity> goodsImageVersionEntityList);
}
