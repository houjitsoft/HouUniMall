package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.goods.entity.GoodsBaseVersionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品sku版本(GoodsBaseVersion)表数据库访问层
 *
 * @author Amos
 * @since 2025-05-28 17:23:34
 */
@Mapper
public interface GoodsBaseVersionMapper extends BaseMapper<GoodsBaseVersionEntity> {


    void insertBatch(@Param("list") List<GoodsBaseVersionEntity> goodsBaseVersionEntityList);
}
