package com.houjiang.mall.mapper.distribution;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.distribution.DistributionOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 分销订单Mapper
 * 
 * @author Amos
 */
@Mapper
public interface DistributionOrderMapper extends BaseMapper<DistributionOrder> {

    /**
     * 按买家聚合统计「我推广过的客户」：去重 buyer_id，带订单数 / 累计金额 / 最近下单时间。
     *
     * <p>只统计未失效的订单（order_status != 4），退款关闭的单不计入客户业绩。
     */
    @Select("SELECT o.buyer_id AS buyerId, COUNT(*) AS orderCount, "
            + "IFNULL(SUM(o.order_amount), 0) AS totalAmount, MAX(o.create_time) AS lastOrderTime, "
            + "IFNULL((SELECT SUM(c.commission_amount) FROM hou_mall_commission_record c "
            + "  WHERE c.deleted = 0 AND c.status <> 2 AND c.distributor_id = o.distributor_id "
            + "    AND c.order_id IN (SELECT x.order_id FROM hou_mall_distribution_order x "
            + "      WHERE x.deleted = 0 AND x.distributor_id = o.distributor_id AND x.buyer_id = o.buyer_id)), 0) AS totalCommission "
            + "FROM hou_mall_distribution_order o "
            + "WHERE o.distributor_id = #{distributorId} AND o.deleted = 0 AND o.order_status <> 4 "
            + "GROUP BY o.buyer_id, o.distributor_id "
            + "ORDER BY lastOrderTime DESC "
            + "LIMIT #{offset}, #{size}")
    List<Map<String, Object>> selectCustomerStats(@Param("distributorId") Long distributorId,
                                                  @Param("offset") long offset,
                                                  @Param("size") long size);

    /**
     * 统计「我推广过的客户」总数（去重 buyer_id）。
     */
    @Select("SELECT COUNT(*) FROM (SELECT buyer_id FROM hou_mall_distribution_order "
            + "WHERE distributor_id = #{distributorId} AND deleted = 0 AND order_status <> 4 "
            + "GROUP BY buyer_id) t")
    long countCustomers(@Param("distributorId") Long distributorId);
}
