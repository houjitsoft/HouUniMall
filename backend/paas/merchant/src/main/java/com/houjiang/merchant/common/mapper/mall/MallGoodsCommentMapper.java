package com.houjiang.merchant.common.mapper.mall;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.mall.MallGoodsComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 商品评论Mapper
 * @author Amos
 */
@Mapper
public interface MallGoodsCommentMapper extends IBaseMapper<MallGoodsComment> {
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT AVG(score) FROM hou_merchant_mall_goods_comment WHERE shop_id = #{shopId} AND delete_time = 0")
    BigDecimal selectAvgScoreByShopId(@Param("shopId") Integer shopId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT AVG(describe_score) FROM hou_merchant_mall_goods_comment WHERE shop_id = #{shopId} AND delete_time = 0")
    BigDecimal selectAvgDescribeScoreByShopId(@Param("shopId") Integer shopId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT AVG(service_score) FROM hou_merchant_mall_goods_comment WHERE shop_id = #{shopId} AND delete_time = 0")
    BigDecimal selectAvgServiceScoreByShopId(@Param("shopId") Integer shopId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT AVG(logistics_score) FROM hou_merchant_mall_goods_comment WHERE shop_id = #{shopId} AND delete_time = 0")
    BigDecimal selectAvgLogisticsScoreByShopId(@Param("shopId") Integer shopId);
}
