package com.houjiang.mall.mapper.order;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 订单主表轻量只读查询（hou_order_base）。
 *
 * <p>背景：order 服务 /order/base/queryOrders 的 C 端 SQL（queryOrderListByPhone）
 * 只查 order_receiver_name，不带联系方式/地址（买家端不需要）。admin 后台订单列表
 * 的「收货人」列三者都要，而 PC 版链路 queryOrdersByPC 内嵌 userms Feign 依赖，
 * 不宜为补两列引入。故 mall 侧直接对本库轻量补查（2026-10-30，同库同实例）。
 *
 * @author Amos
 */
@Mapper
public interface OrderBaseQueryMapper {

    /**
     * 按订单号批量查收货人三元组。
     *
     * @param orderIds 订单号集合（≤ 每页大小，50 以内）
     * @return orderId / receiverName / receiverContact / receiverAddress
     */
    @Select("<script>"
            + "SELECT order_id AS orderId, order_receiver_name AS receiverName,"
            + " order_receiver_contact AS receiverContact, order_receiver_address AS receiverAddress"
            + " FROM hou_order_base WHERE order_id IN"
            + " <foreach collection='orderIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    List<Map<String, Object>> selectReceivers(@Param("orderIds") List<String> orderIds);

    /**
     * 按订单状态统计全平台单量（后台列表 tab 徽标用，不含软删与自提单，与
     * order 服务 queryOrderList 的基础过滤口径一致）。
     *
     * @return orderStatus / cnt
     */
    @Select("SELECT order_status AS orderStatus, COUNT(*) AS cnt FROM hou_order_base"
            + " WHERE order_del = 0 AND order_is_shop_take <> 1 GROUP BY order_status")
    List<Map<String, Object>> selectStatusCounts();
}
