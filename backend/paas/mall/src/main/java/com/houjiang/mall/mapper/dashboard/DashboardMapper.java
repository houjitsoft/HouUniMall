package com.houjiang.mall.mapper.dashboard;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Dashboard聚合查询Mapper
 * 从mall已有表中统计概览数据
 *
 * @author Amos
 */
@Mapper
public interface DashboardMapper {

    /**
     * 店铺总数（未删除）
     *
     * @return 店铺数量
     */
    @Select("SELECT COUNT(*) FROM hou_mall_shop WHERE deleted = 0")
    int countShops();

    /**
     * 正常营业的店铺数
     *
     * @return 正常状态店铺数量
     */
    @Select("SELECT COUNT(*) FROM hou_mall_shop WHERE deleted = 0 AND status = 1")
    int countActiveShops();

    /**
     * 分销员总数
     *
     * @return 分销员数量
     */
    @Select("SELECT COUNT(*) FROM hou_mall_distributor WHERE deleted = 0")
    int countDistributors();

    /**
     * 正常状态的分销员数
     *
     * @return 正常状态分销员数量
     */
    @Select("SELECT COUNT(*) FROM hou_mall_distributor WHERE deleted = 0 AND status = 1")
    int countActiveDistributors();

    /**
     * 累计佣金总额
     *
     * @return 佣金总额
     */
    @Select("SELECT COALESCE(SUM(commission_amount), 0) FROM hou_mall_commission_record WHERE deleted = 0")
    BigDecimal sumTotalCommission();

    /**
     * 今日佣金总额
     *
     * @return 今日佣金
     */
    @Select("SELECT COALESCE(SUM(commission_amount), 0) FROM hou_mall_commission_record WHERE deleted = 0 AND DATE(create_time) = CURDATE()")
    BigDecimal sumTodayCommission();

    /**
     * 今日新增分销订单数
     *
     * @return 今日订单数
     */
    @Select("SELECT COUNT(*) FROM hou_mall_distribution_order WHERE deleted = 0 AND DATE(create_time) = CURDATE()")
    int countTodayDistributionOrders();

    /**
     * 库存预警商品数（可用数量 <= 预警数量）
     *
     * @return 预警商品数
     */
    @Select("SELECT COUNT(*) FROM hou_mall_stock WHERE deleted = 0 AND available_qty <= warning_qty")
    int countLowStockGoods();

    /**
     * 待处理风控预警数
     *
     * @return 待处理预警数
     */
    @Select("SELECT COUNT(*) FROM hou_mall_risk_alert WHERE deleted = 0 AND status = 0")
    int countPendingAlerts();

    /**
     * 待审核商家入驻申请数
     *
     * @return 待审核数
     */
    @Select("SELECT COUNT(*) FROM hou_mall_merchant_entry WHERE deleted = 0 AND status = 0")
    int countPendingMerchantEntries();

    /**
     * 待处理返利记录数
     *
     * @return 待审核返利数
     */
    @Select("SELECT COUNT(*) FROM hou_mall_rebate_record WHERE deleted = 0 AND status = 0")
    int countPendingRebates();

    /**
     * 最近N天每日佣金统计（销售趋势）
     *
     * @param days 天数
     * @return 每日统计列表 [{date, amount, count}]
     */
    @Select("SELECT DATE(create_time) AS `date`, " +
            "COALESCE(SUM(commission_amount), 0) AS amount, " +
            "COUNT(*) AS `count` " +
            "FROM hou_mall_commission_record " +
            "WHERE deleted = 0 AND create_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(create_time) ORDER BY `date`")
    /**
     * TODO 补充方法说明
     * @param days TODO
     * @return TODO
     */
    List<Map<String, Object>> dailyCommissionStats(int days);

    /**
     * 分销商品按佣金排行TOP N
     *
     * @param limit 数量限制
     * @return 商品排行 [{goodsId, goodsName, totalCommission, orderCount}]
     */
    @Select("SELECT dg.goods_id AS goodsId, dg.goods_name AS goodsName, " +
            "COALESCE(SUM(cr.commission_amount), 0) AS totalCommission, " +
            "COUNT(cr.id) AS orderCount " +
            "FROM hou_mall_distribution_goods dg " +
            "LEFT JOIN hou_mall_commission_record cr ON cr.deleted = 0 " +
            "WHERE dg.deleted = 0 AND dg.status = 1 " +
            "GROUP BY dg.goods_id, dg.goods_name " +
            "ORDER BY totalCommission DESC LIMIT #{limit}")
    /**
     * TODO 补充方法说明
     * @param limit TODO
     * @return TODO
     */
    List<Map<String, Object>> goodsCommissionRank(int limit);

    /**
     * 最近N条分销订单（实时订单流）
     *
     * @param limit 数量限制
     * @return 最近订单列表
     */
    @Select("SELECT order_no AS orderNo, order_amount AS amount, " +
            "DATE_FORMAT(create_time, '%H:%i:%s') AS `time`, order_status AS status " +
            "FROM hou_mall_distribution_order " +
            "WHERE deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    /**
     * TODO 补充方法说明
     * @param limit TODO
     * @return TODO
     */
    List<Map<String, Object>> recentDistributionOrders(int limit);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT COUNT(*) FROM hou_mall_distribution_order WHERE deleted = 0")
    int countAllDistributionOrders();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT COUNT(*) FROM hou_mall_distribution_order WHERE deleted = 0 AND DATE(create_time) = CURDATE()")
    int countTodayDistributionOrdersByDate();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT COUNT(*) FROM hou_mall_distribution_order WHERE deleted = 0 AND DATE(create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    int countYesterdayDistributionOrders();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT COALESCE(SUM(order_amount), 0) FROM hou_mall_distribution_order WHERE deleted = 0")
    BigDecimal sumAllOrderAmount();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT COALESCE(SUM(order_amount), 0) FROM hou_mall_distribution_order WHERE deleted = 0 AND DATE(create_time) = CURDATE()")
    BigDecimal sumTodayOrderAmount();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT COALESCE(SUM(order_amount), 0) FROM hou_mall_distribution_order WHERE deleted = 0 AND DATE(create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    BigDecimal sumYesterdayOrderAmount();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT COUNT(*) FROM hou_mall_customer WHERE deleted = 0")
    int countAllCustomers();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT COUNT(*) FROM hou_mall_customer WHERE deleted = 0 AND DATE(create_time) = CURDATE()")
    int countTodayNewCustomers();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT COUNT(*) FROM hou_mall_customer WHERE deleted = 0 AND DATE(create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    int countYesterdayNewCustomers();

    @Select("SELECT s.id, s.shop_name AS name, COALESCE(SUM(o.order_amount), 0) AS sale_money, " +
            "COALESCE(s.shop_logo, '') AS logo " +
            "FROM hou_mall_shop s " +
            "LEFT JOIN hou_mall_distribution_order o ON o.shop_id = s.id AND o.deleted = 0 " +
            "WHERE s.deleted = 0 " +
            "GROUP BY s.id, s.shop_name, s.shop_logo " +
            "ORDER BY sale_money DESC LIMIT #{limit}")
    /**
     * TODO 补充方法说明
     * @param limit TODO
     * @return TODO
     */
    List<Map<String, Object>> topShopsBySales(int limit);

    @Select("SELECT s.id, s.shop_name AS name, COUNT(o.id) AS visit_num, COALESCE(s.shop_logo, '') AS logo " +
            "FROM hou_mall_shop s " +
            "LEFT JOIN hou_mall_distribution_order o ON o.shop_id = s.id AND o.deleted = 0 " +
            "WHERE s.deleted = 0 " +
            "GROUP BY s.id, s.shop_name, s.shop_logo " +
            "ORDER BY visit_num DESC LIMIT #{limit}")
    /**
     * TODO 补充方法说明
     * @param limit TODO
     * @return TODO
     */
    List<Map<String, Object>> topShopsByOrderCount(int limit);
}
