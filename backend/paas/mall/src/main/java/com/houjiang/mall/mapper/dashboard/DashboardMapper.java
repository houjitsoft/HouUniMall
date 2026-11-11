package com.houjiang.mall.mapper.dashboard;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
    /**
     * 最近 N 天每日成交统计（交易趋势）
     *
     * 2026-11-11 修正：原读 hou_mall_commission_record（分销佣金），真实业务无数据。
     * 改为从 hou_order_base 聚合真实订单，并用递归 CTE 生成连续日期序列再左连，
     * 保证某天没有订单时该天仍返回 0 —— 前端交易趋势按「最后 7 条 / 前 7 条」切片，
     * 日期断档会让近 7 日与上 7 日的边界整体串位，环比就假了。
     *
     * @param days 天数（含今日）
     * @return 每日统计列表 [{date, amount, count}]
     */
    @Select("WITH RECURSIVE seq AS (" +
            "SELECT CURDATE() AS dt " +
            "UNION ALL " +
            "SELECT seq.dt - INTERVAL 1 DAY FROM seq WHERE seq.dt > CURDATE() - INTERVAL ${days} DAY) " +
            "SELECT seq.dt AS `date`, " +
            "COALESCE(SUM(CASE WHEN o.order_status IN (2,3,4,5,6,11) THEN o.order_payment_amount ELSE 0 END), 0) AS amount, " +
            "COUNT(o.order_id) AS `count` " +
            "FROM seq " +
            "LEFT JOIN hou_order_base o ON DATE(o.order_create_time) = seq.dt " +
            "AND o.order_del = 0 AND o.order_status IN (1,2,3,4,5,6,11) " +
            "GROUP BY seq.dt ORDER BY seq.dt")
    List<Map<String, Object>> dailyCommissionStats(@Param("days") int days);

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
    /*
     * ==================== 首页工作台统计口径（2026-11-11 修正） ====================
     * 原实现读 hou_mall_distribution_order / hou_mall_customer（分销订单、分销客户），
     * 这两张表在真实商城业务里没有数据，导致首页 8 张卡片恒为 0。
     * 真实交易数据在 hou_order_base（C 端订单主表），故统一改从该表聚合：
     *   下单口径（订单量 / 下单用户数）：order_status IN (1,2,3,4,5,6,11)
     *     —— 即待付款、已付款、待发货、已发货、已签收、确认收货、待自提，
     *        排除 7 取消 / 8 退款中 / 9 已退款 / 12 已过期
     *   成交口径（成交额）：order_status IN (2,3,4,5,6,11)，不含待付款
     *   删除标志：huo_order_base 用 order_del，不是 deleted
     * 订单状态枚举来源：OrderBaseServiceImpl#orderSellingTransactionExcel
     */

    @Select("SELECT COUNT(*) FROM hou_order_base WHERE order_del = 0 AND order_status IN (1,2,3,4,5,6,11)")
    int countAllDistributionOrders();

    /**
     * 今日下单量（排除取消/退款/过期）
     * @return 今日订单数
     */
    @Select("SELECT COUNT(*) FROM hou_order_base WHERE order_del = 0 AND order_status IN (1,2,3,4,5,6,11) AND DATE(order_create_time) = CURDATE()")
    int countTodayDistributionOrdersByDate();

    /**
     * 昨日下单量（排除取消/退款/过期）
     * @return 昨日订单数
     */
    @Select("SELECT COUNT(*) FROM hou_order_base WHERE order_del = 0 AND order_status IN (1,2,3,4,5,6,11) AND DATE(order_create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    int countYesterdayDistributionOrders();

    /**
     * 累计成交额（已支付口径）
     * @return 累计成交金额
     */
    @Select("SELECT COALESCE(SUM(order_payment_amount), 0) FROM hou_order_base WHERE order_del = 0 AND order_status IN (2,3,4,5,6,11)")
    BigDecimal sumAllOrderAmount();

    /**
     * 今日成交额（已支付口径，不含待付款）
     * @return 今日成交金额
     */
    @Select("SELECT COALESCE(SUM(order_payment_amount), 0) FROM hou_order_base WHERE order_del = 0 AND order_status IN (2,3,4,5,6,11) AND DATE(order_create_time) = CURDATE()")
    BigDecimal sumTodayOrderAmount();

    /**
     * 昨日成交额（已支付口径，不含待付款）
     * @return 昨日成交金额
     */
    @Select("SELECT COALESCE(SUM(order_payment_amount), 0) FROM hou_order_base WHERE order_del = 0 AND order_status IN (2,3,4,5,6,11) AND DATE(order_create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    BigDecimal sumYesterdayOrderAmount();

    /**
     * 累计下单用户数（按 buyer_user_id 去重）
     * @return 累计买家数
     */
    @Select("SELECT COUNT(DISTINCT buyer_user_id) FROM hou_order_base WHERE order_del = 0 AND order_status IN (1,2,3,4,5,6,11)")
    int countAllCustomers();

    /**
     * 今日下单用户数（按 buyer_user_id 去重）
     * @return 今日买家数
     */
    @Select("SELECT COUNT(DISTINCT buyer_user_id) FROM hou_order_base WHERE order_del = 0 AND order_status IN (1,2,3,4,5,6,11) AND DATE(order_create_time) = CURDATE()")
    int countTodayNewCustomers();

    /**
     * 昨日下单用户数（按 buyer_user_id 去重）
     * @return 昨日买家数
     */
    @Select("SELECT COUNT(DISTINCT buyer_user_id) FROM hou_order_base WHERE order_del = 0 AND order_status IN (1,2,3,4,5,6,11) AND DATE(order_create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
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
