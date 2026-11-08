package com.houjiang.mall.service.dashboard;

import com.houjiang.mall.mapper.dashboard.DashboardMapper;
import com.houjiang.plugin.api.extension.mall.DashboardWidgetExtension;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Dashboard聚合数据服务
 * 汇总mall服务内各表统计数据
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardMapper dashboardMapper;
    private final ExtensionInvoker extensionInvoker;

    /**
     * 获取概览统计数据
     * 聚合店铺、分销、佣金、库存预警等核心指标
     *
     * @return 概览数据Map
     */
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>(16);
        try {
            // 店铺统计
            overview.put("totalShops", dashboardMapper.countShops());
            overview.put("activeShops", dashboardMapper.countActiveShops());

            // 分销员统计
            overview.put("totalDistributors", dashboardMapper.countDistributors());
            overview.put("activeDistributors", dashboardMapper.countActiveDistributors());

            // 佣金统计
            overview.put("totalCommission", dashboardMapper.sumTotalCommission());
            overview.put("todayCommission", dashboardMapper.sumTodayCommission());

            // 今日分销订单
            overview.put("todayDistributionOrders", dashboardMapper.countTodayDistributionOrders());

            // 预警与待处理
            overview.put("lowStockGoods", dashboardMapper.countLowStockGoods());
            overview.put("pendingAlerts", dashboardMapper.countPendingAlerts());
            overview.put("pendingMerchantEntries", dashboardMapper.countPendingMerchantEntries());
            overview.put("pendingRebates", dashboardMapper.countPendingRebates());
        } catch (Exception e) {
            log.error("聚合概览数据异常", e);
        }
        return overview;
    }

    /**
     * 获取实时监控数据
     * 包含最近分销订单流、待处理计数
     *
     * @return 实时数据Map
     */
    public Map<String, Object> getRealtime() {
        Map<String, Object> realtime = new HashMap<>(8);
        try {
            realtime.put("pendingAlerts", dashboardMapper.countPendingAlerts());
            realtime.put("lowStockGoods", dashboardMapper.countLowStockGoods());
            realtime.put("pendingMerchantEntries", dashboardMapper.countPendingMerchantEntries());
            realtime.put("pendingRebates", dashboardMapper.countPendingRebates());
            realtime.put("orderStream", dashboardMapper.recentDistributionOrders(10));
        } catch (Exception e) {
            log.error("聚合实时数据异常", e);
        }
        return realtime;
    }

    /**
     * 获取销售趋势数据
     * 按时间维度聚合佣金/分销订单统计
     *
     * @param timeRange 时间范围: day/week/month
     * @return 趋势数据Map，包含chartData列表
     */
    public Map<String, Object> getSalesTrend(String timeRange) {
        Map<String, Object> trend = new HashMap<>(4);
        int days;
        switch (timeRange) {
            case "day": days = 1; break;
            case "month": days = 30; break;
            default: days = 7; break;
        }
        try {
            trend.put("chartData", dashboardMapper.dailyCommissionStats(days));
            trend.put("timeRange", timeRange);
        } catch (Exception e) {
            log.error("聚合销售趋势异常", e);
            trend.put("chartData", new ArrayList<>());
        }
        return trend;
    }

    /**
     * 获取商品佣金排行
     *
     * @param limit 排行数量限制
     * @return 商品排行列表
     */
    public List<Map<String, Object>> getGoodsRank(int limit) {
        try {
            List<Map<String, Object>> rank = dashboardMapper.goodsCommissionRank(limit);
            // 补充排名序号
            for (int i = 0; i < rank.size(); i++) {
                rank.get(i).put("rank", i + 1);
            }
            return rank;
        } catch (Exception e) {
            log.error("聚合商品排行异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取快捷操作待处理数据
     *
     * @return 各类待处理计数
     */
    public Map<String, Object> getQuickActions() {
        Map<String, Object> actions = new HashMap<>(8);
        try {
            actions.put("pendingAlerts", dashboardMapper.countPendingAlerts());
            actions.put("lowStockWarnings", dashboardMapper.countLowStockGoods());
            actions.put("pendingMerchantEntries", dashboardMapper.countPendingMerchantEntries());
            actions.put("pendingRebates", dashboardMapper.countPendingRebates());
        } catch (Exception e) {
            log.error("聚合快捷操作数据异常", e);
        }
        return actions;
    }

    /**
     * 汇聚已安装插件经 {@link DashboardWidgetExtension} 贡献的看板挂件（阶段2：宿主触发扩展点）。
     *
     * <p>无插件或引擎未启用时返回空列表；插件抛异常由 {@code ExtensionInvoker} 隔离，不影响主流程。
     * 新增的插件挂件会随安装/启用自动出现在看板，无需改这里。
     */
    public List<Object> collectPluginWidgets() {
        DashboardWidgetExtension.Context ctx = new DashboardWidgetExtension.Context();
        extensionInvoker.invoke(DashboardWidgetExtension.class, ctx);
        return ctx.widgets == null ? Collections.emptyList() : ctx.widgets;
    }

    /**
     * Mall 管理后台工作台（like-admin 数据结构）
     */
    public Map<String, Object> getWorkbenchOverview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("now", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        try {
            CompletableFuture<Integer> todayCountF =
                    asyncInt(() -> dashboardMapper.countTodayDistributionOrdersByDate());
            CompletableFuture<Integer> yesterdayCountF =
                    asyncInt(() -> dashboardMapper.countYesterdayDistributionOrders());
            CompletableFuture<Integer> allCountF =
                    asyncInt(() -> dashboardMapper.countAllDistributionOrders());
            CompletableFuture<BigDecimal> todayMoneyF =
                    asyncDecimal(() -> dashboardMapper.sumTodayOrderAmount());
            CompletableFuture<BigDecimal> yesterdayMoneyF =
                    asyncDecimal(() -> dashboardMapper.sumYesterdayOrderAmount());
            CompletableFuture<BigDecimal> allMoneyF =
                    asyncDecimal(() -> dashboardMapper.sumAllOrderAmount());
            CompletableFuture<Integer> todayUsersF =
                    asyncInt(() -> dashboardMapper.countTodayNewCustomers());
            CompletableFuture<Integer> yesterdayUsersF =
                    asyncInt(() -> dashboardMapper.countYesterdayNewCustomers());
            CompletableFuture<Integer> allUsersF =
                    asyncInt(() -> dashboardMapper.countAllCustomers());
            CompletableFuture<Map<String, Object>> business15F =
                    CompletableFuture.supplyAsync(this::buildBusiness15FromCommission);
            CompletableFuture<List<Map<String, Object>>> shopTopF =
                    asyncList(() -> dashboardMapper.topShopsBySales(5));
            CompletableFuture<List<Map<String, Object>>> shopVisitTopF =
                    asyncList(() -> dashboardMapper.topShopsByOrderCount(5));

            CompletableFuture.allOf(
                    todayCountF, yesterdayCountF, allCountF, todayMoneyF, yesterdayMoneyF, allMoneyF,
                    todayUsersF, yesterdayUsersF, allUsersF, business15F, shopTopF, shopVisitTopF
            ).get(8, TimeUnit.SECONDS);

            int todayCount = todayCountF.getNow(0);
            int yesterdayCount = yesterdayCountF.getNow(0);
            int allCount = allCountF.getNow(0);
            BigDecimal todayMoney = todayMoneyF.getNow(BigDecimal.ZERO);
            BigDecimal yesterdayMoney = yesterdayMoneyF.getNow(BigDecimal.ZERO);
            BigDecimal allMoney = allMoneyF.getNow(BigDecimal.ZERO);
            Map<String, Object> business15 = business15F.getNow(emptyBusiness15());

            Map<String, Object> order = new LinkedHashMap<>();
            order.put("today", orderSlice(todayCount, todayMoney));
            order.put("yesterday", orderSlice(yesterdayCount, yesterdayMoney));
            order.put("all", orderSlice(allCount, allMoney));
            order.put("business15", business15);
            data.put("order", order);

            int todayUsers = todayUsersF.getNow(0);
            int yesterdayUsers = yesterdayUsersF.getNow(0);
            int allUsers = allUsersF.getNow(0);
            Map<String, Object> userAdd = new LinkedHashMap<>();
            userAdd.put("today", todayUsers);
            userAdd.put("yesterday", yesterdayUsers);
            userAdd.put("all", allUsers);
            data.put("user_add_detail", userAdd);

            Map<String, Object> indexVisit = new LinkedHashMap<>();
            indexVisit.put("today", visitSlice(todayUsers));
            indexVisit.put("yesterday", visitSlice(yesterdayUsers));
            indexVisit.put("all", visitSlice(allUsers));
            indexVisit.put("business15", business15);
            data.put("index_visit", indexVisit);

            data.put("shop_top", shopTopF.getNow(Collections.emptyList()));
            data.put("shop_visit_top", shopVisitTopF.getNow(Collections.emptyList()));
        } catch (TimeoutException e) {
            log.warn("工作台统计查询超时(8s)，返回默认骨架数据");
            fillWorkbenchSkeleton(data);
        } catch (Exception e) {
            log.warn("工作台统计查询失败: {}", e.getMessage());
            fillWorkbenchSkeleton(data);
        }

        Map<String, Object> version = new LinkedHashMap<>();
        version.put("name", "商城管理后台");
        version.put("version", "2.1.1");
        data.put("version", version);

        // 阶段2：宿主触发扩展点，让已安装插件往工作台贡献自定义挂件（无插件时为空列表）。
        data.put("pluginWidgets", collectPluginWidgets());
        return data;
    }

    private void fillWorkbenchSkeleton(Map<String, Object> data) {
        Map<String, Object> business15 = emptyBusiness15();
        Map<String, Object> order = new LinkedHashMap<>();
        order.put("today", orderSlice(0, BigDecimal.ZERO));
        order.put("yesterday", orderSlice(0, BigDecimal.ZERO));
        order.put("all", orderSlice(0, BigDecimal.ZERO));
        order.put("business15", business15);
        data.put("order", order);
        Map<String, Object> userAdd = new LinkedHashMap<>();
        userAdd.put("today", 0);
        userAdd.put("yesterday", 0);
        userAdd.put("all", 0);
        data.put("user_add_detail", userAdd);
        Map<String, Object> indexVisit = new LinkedHashMap<>();
        indexVisit.put("today", visitSlice(0));
        indexVisit.put("yesterday", visitSlice(0));
        indexVisit.put("all", visitSlice(0));
        indexVisit.put("business15", business15);
        data.put("index_visit", indexVisit);
        data.put("shop_top", Collections.emptyList());
        data.put("shop_visit_top", Collections.emptyList());
    }

    private Map<String, Object> emptyBusiness15() {
        Map<String, Object> business15 = new LinkedHashMap<>();
        business15.put("date", Collections.emptyList());
        Map<String, Object> series = new LinkedHashMap<>();
        series.put("name", "营业额");
        series.put("data", Collections.emptyList());
        business15.put("list", List.of(series));
        return business15;
    }

    private CompletableFuture<Integer> asyncInt(java.util.concurrent.Callable<Integer> supplier) {
        return CompletableFuture.supplyAsync(() -> safeInt(supplier));
    }

    private CompletableFuture<BigDecimal> asyncDecimal(java.util.concurrent.Callable<BigDecimal> supplier) {
        return CompletableFuture.supplyAsync(() -> safeDecimal(supplier));
    }

    private CompletableFuture<List<Map<String, Object>>> asyncList(
            java.util.concurrent.Callable<List<Map<String, Object>>> supplier) {
        return CompletableFuture.supplyAsync(() -> safeList(supplier));
    }

    public Map<String, Object> checkVersion(Map<String, Object> params) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("result", false);
        result.put("version", "2.1.1");
        return result;
    }

    public Map<String, Object> checkLegal(Map<String, Object> params) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("result", true);
        return result;
    }

    private Map<String, Object> orderSlice(int count, BigDecimal money) {
        Map<String, Object> slice = new LinkedHashMap<>();
        slice.put("count", count);
        slice.put("money", money != null ? money : BigDecimal.ZERO);
        slice.put("date", LocalDate.now().toString());
        return slice;
    }

    private Map<String, Object> visitSlice(int count) {
        Map<String, Object> slice = new LinkedHashMap<>();
        slice.put("count", count);
        slice.put("date", LocalDate.now().toString());
        slice.put("money", 0);
        return slice;
    }

    private Map<String, Object> buildBusiness15FromCommission() {
        List<Map<String, Object>> stats = safeList(() -> dashboardMapper.dailyCommissionStats(15));
        List<String> dates = new ArrayList<>();
        List<Object> amounts = new ArrayList<>();
        for (Map<String, Object> row : stats) {
            Object date = row.get("date");
            if (date != null) {
                dates.add(String.valueOf(date));
            }
            amounts.add(row.getOrDefault("amount", 0));
        }
        Map<String, Object> series = new LinkedHashMap<>();
        series.put("name", "营业额");
        series.put("data", amounts);
        Map<String, Object> business15 = new LinkedHashMap<>();
        business15.put("date", dates);
        business15.put("list", List.of(series));
        return business15;
    }

    private int safeInt(java.util.concurrent.Callable<Integer> supplier) {
        try {
            Integer v = supplier.call();
            return v != null ? v : 0;
        } catch (Exception e) {
            log.warn("工作台统计查询失败: {}", e.getMessage());
            return 0;
        }
    }

    private BigDecimal safeDecimal(java.util.concurrent.Callable<BigDecimal> supplier) {
        try {
            BigDecimal v = supplier.call();
            return v != null ? v : BigDecimal.ZERO;
        } catch (Exception e) {
            log.warn("工作台金额统计失败: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private <T> List<T> safeList(java.util.concurrent.Callable<List<T>> supplier) {
        try {
            List<T> list = supplier.call();
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            log.warn("工作台列表查询失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
