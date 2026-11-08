package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.goods.entity.GoodsShopCatVersionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品店铺分类版本(GoodsShopCatVersion)表数据库访问层
 *
 * @author Amos
 * @since 2025-05-28 17:25:13
 */
@Mapper
public interface GoodsShopCatVersionMapper extends BaseMapper<GoodsShopCatVersionEntity> {


    void insertBatch(@Param("list")  List<GoodsShopCatVersionEntity> goodsShopCatVersionEntityList);
}
