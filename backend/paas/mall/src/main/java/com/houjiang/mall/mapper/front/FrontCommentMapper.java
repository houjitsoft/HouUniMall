package com.houjiang.mall.mapper.front;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 商城前台商品评论查询Mapper
 * 数据来源 hou_mall_goods_comment
 *
 * @author Amos
 */
@Mapper
public interface FrontCommentMapper {

    /**
     * 统计商品可见评论数
     *
     * @param goodsId 商品ID
     * @return 评论数量
     */
    @Select("SELECT COUNT(*) FROM hou_mall_goods_comment"
            + " WHERE goods_id = #{goodsId} AND is_show = 1 AND delete_time = 0")
    long countGoodsComments(@Param("goodsId") Long goodsId);

    /**
     * 分页查询商品评论（按时间倒序）
     *
     * @param goodsId 商品ID
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 评论记录列表
     */
    @Select("SELECT id, user_id, goods_id, score, content, images, has_images,"
            + " describe_score, service_score, logistics_score, create_time"
            + " FROM hou_mall_goods_comment"
            + " WHERE goods_id = #{goodsId} AND is_show = 1 AND delete_time = 0"
            + " ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> selectGoodsComments(@Param("goodsId") Long goodsId,
                                                  @Param("limit") int limit,
                                                  @Param("offset") int offset);
}
