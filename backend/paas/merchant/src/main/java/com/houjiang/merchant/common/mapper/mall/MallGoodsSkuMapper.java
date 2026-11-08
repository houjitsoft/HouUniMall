package com.houjiang.merchant.common.mapper.mall;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.mall.MallGoodsSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品skuMapper
 * @author Amos
 */
@Mapper
public interface MallGoodsSkuMapper extends IBaseMapper<MallGoodsSku> {

    /**
     * C 端计价读 hou_mall_goods_sku。
     */
    @Select("SELECT * FROM hou_mall_goods_sku WHERE id = #{id} AND (delete_time IS NULL OR delete_time = 0) LIMIT 1")
    MallGoodsSku selectFrontById(@Param("id") Integer id);

    @Select("SELECT * FROM hou_mall_goods_sku WHERE goods_id = #{goodsId} AND (delete_time IS NULL OR delete_time = 0) LIMIT 1")
    List<MallGoodsSku> selectFrontByGoodsId(@Param("goodsId") Integer goodsId);
}
