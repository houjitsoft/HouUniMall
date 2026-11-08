package com.houjiang.mall.mapper.front;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 商城前台店铺查询Mapper
 * 数据来源 hou_merchant_mall_shop（商家平台店铺表，商品表 shop_id 与其对应）
 *
 * @author Amos
 */
@Mapper
public interface FrontShopMapper {

    /**
     * 统计店铺总数（支持名称关键字过滤）
     *
     * @param keyword 店铺名称关键字，可为null
     * @return 店铺数量
     */
    @Select("<script>SELECT COUNT(*) FROM hou_merchant_mall_shop WHERE delete_time = 0"
            + "<if test='keyword != null and keyword != \"\"'> AND name LIKE CONCAT('%', #{keyword}, '%')</if></script>")
    long countShops(@Param("keyword") String keyword);

    /**
     * 分页查询店铺列表
     *
     * @param keyword 店铺名称关键字，可为null
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 店铺记录列表
     */
    @Select("<script>SELECT id, name, logo, `desc`, contact, mobile, address_info, create_time"
            + " FROM hou_merchant_mall_shop WHERE delete_time = 0"
            + "<if test='keyword != null and keyword != \"\"'> AND name LIKE CONCAT('%', #{keyword}, '%')</if>"
            + " ORDER BY id ASC LIMIT #{limit} OFFSET #{offset}</script>")
    List<Map<String, Object>> selectShops(@Param("keyword") String keyword,
                                          @Param("limit") int limit,
                                          @Param("offset") int offset);

    /**
     * 查询单个店铺
     *
     * @param id 店铺ID
     * @return 店铺记录，不存在返回null
     */
    @Select("SELECT id, name, logo, `desc`, contact, mobile, address_info, create_time"
            + " FROM hou_merchant_mall_shop WHERE id = #{id} AND delete_time = 0")
    Map<String, Object> selectShopById(@Param("id") Long id);

    /**
     * 统计店铺收藏数（同时作为粉丝数展示）
     *
     * @param shopId 店铺ID
     * @return 收藏数量
     */
    @Select("SELECT COUNT(*) FROM hou_merchant_mall_shop_collect WHERE shop_id = #{shopId} AND delete_time = 0")
    long countShopCollects(@Param("shopId") Long shopId);

    /**
     * 统计店铺在售商品数
     *
     * @param shopId 店铺ID
     * @return 商品数量
     */
    @Select("SELECT COUNT(*) FROM hou_mall_goods WHERE shop_id = #{shopId} AND delete_time = 0")
    long countShopGoods(@Param("shopId") Long shopId);

    /**
     * 查询店铺商品预览图（店铺卡片/店招展示用）
     *
     * @param shopId 店铺ID
     * @param limit  最多返回条数
     * @return 商品预览记录（id、name、image、min_price）
     */
    @Select("SELECT id, name, image, min_price FROM hou_mall_goods"
            + " WHERE shop_id = #{shopId} AND delete_time = 0"
            + " ORDER BY create_time DESC LIMIT #{limit}")
    List<Map<String, Object>> selectShopGoodsPreview(@Param("shopId") Long shopId,
                                                     @Param("limit") int limit);

    /**
     * 查询店铺商品涉及的经营分类（根据商品 category_ids 关联全局分类表）
     *
     * @param shopId 店铺ID
     * @return 分类记录（id、name）
     */
    @Select("SELECT DISTINCT c.id, c.name FROM hou_mall_goods g"
            + " JOIN hou_mall_goods_category c ON FIND_IN_SET(c.id, g.category_ids) > 0"
            + " WHERE g.shop_id = #{shopId} AND g.delete_time = 0 AND c.delete_time = 0"
            + " ORDER BY c.sort ASC, c.id ASC")
    List<Map<String, Object>> selectShopCategories(@Param("shopId") Long shopId);
}
