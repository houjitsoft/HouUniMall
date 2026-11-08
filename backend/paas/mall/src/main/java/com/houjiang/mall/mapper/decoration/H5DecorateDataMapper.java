package com.houjiang.mall.mapper.decoration;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * H5 首页装修桥接的数据查询 Mapper。
 *
 * <p>新版装修配置里，秒杀/拼团只存「取数方式 + 展示条数」、不存商品明细，
 * 商品列表组件在后台未手动挑选时也只有空的 goodsLists。
 * 因此桥接渲染时需要按配置实时取数，这里统一提供查询能力。
 *
 * <p>说明：秒杀/拼团表由 order 服务的 order-seckill / order-pintuan 插件维护，
 * 与 mall 同库；这里只读，避免装修页渲染再跨服务调用。
 */
@Mapper
public interface H5DecorateDataMapper {

    /**
     * 查询进行中的秒杀场次。
     *
     * @param limit 最多返回条数
     * @return 场次列表，含 seckill_id / seckill_name / seckill_end_time
     */
    List<Map<String, Object>> selectOngoingSeckill(@Param("limit") int limit);

    /**
     * 查询指定秒杀场次下的商品。
     *
     * @param seckillId 秒杀活动编号
     * @param limit     最多返回条数
     * @return 商品列表，含 goods_id / common_id / goods_name / goods_image / goods_price / seckill_price / common_salenum
     */
    List<Map<String, Object>> selectSeckillGoods(@Param("seckillId") Object seckillId, @Param("limit") int limit);

    /**
     * 查询进行中的拼团活动（关联商品主信息）。
     *
     * @param limit 最多返回条数
     * @return 拼团列表，含 common_id / person_num / common_name / common_image / common_price
     */
    List<Map<String, Object>> selectOngoingPintuan(@Param("limit") int limit);

    /**
     * 查询在售商品（用于「自动取数」模式的商品列表组件）。
     *
     * @param limit 最多返回条数
     * @return 商品列表
     */
    List<Map<String, Object>> selectSaleGoods(@Param("limit") int limit);
}
