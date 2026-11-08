package com.houjiang.merchant.common.mapper.mall;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.mall.MallGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 商品Mapper
 * @author Amos
 */
@Mapper
public interface MallGoodsMapper extends IBaseMapper<MallGoods> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT `id`,`name`,`sale_money`,cast((sale_money * 100) as signed) as sale_rank,`image` FROM `hou_merchant_mall_goods` WHERE (  `shop_id` = #{shopId} ) AND `hou_merchant_mall_goods`.`delete_time` = '0' ORDER BY `sale_rank` DESC,`id` DESC LIMIT #{limit}")
    List<Map<String, Object>> selectWithCast(@Param("shopId") Integer shopId, @Param("limit") Integer limit);

    /**
     * C 端计价读 hou_mall_goods（与 PC/uniapp 商品同源；hou_merchant_mall_goods 可能为空）。
     */
    @Select("SELECT * FROM hou_mall_goods WHERE id = #{id} AND (delete_time IS NULL OR delete_time = 0) LIMIT 1")
    MallGoods selectFrontById(@Param("id") Integer id);
}
