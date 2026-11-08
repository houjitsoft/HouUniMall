package com.houjiang.goods.service.impl;

import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.goods.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 数据统计服务实现
 * 
 * @author Amos
 */
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Random RANDOM = new Random();
    
    @Override
    public ResponseResult getReportOverview(Map<String, Object> param) {
        Map<String, Object> overview = new HashMap<>();
        
        // 总销售额
        overview.put("totalSales", 1258960.50);
        overview.put("salesGrowth", 15.8);
        
        // 总订单数
        overview.put("totalOrders", 3856);
        overview.put("ordersGrowth", 12.3);
        
        // 新增客户
        overview.put("newCustomers", 285);
        overview.put("customersGrowth", 8.5);
        
        // 客单价
        overview.put("avgOrderAmount", 326.52);
        overview.put("avgGrowth", 3.2);
        
        return ResponseResult.success(overview);
    }
    
    @Override
    public ResponseResult getReportDetail(Map<String, Object> param) {
        int page = param.get("page") != null ? (int) param.get("page") : 1;
        int limit = param.get("limit") != null ? (int) param.get("limit") : 20;
        
        List<Map<String, Object>> list = new ArrayList<>();
        
        // 生成最近30天的统计数据
        LocalDate endDate = LocalDate.now();
        for (int i = 0; i < 30; i++) {
            LocalDate date = endDate.minusDays(i);
            
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.format(DATE_FORMATTER));
            
            // 随机生成数据
            double salesAmount = 30000 + RANDOM.nextInt(20000);
            int orderCount = 100 + RANDOM.nextInt(50);
            int customerCount = 80 + RANDOM.nextInt(40);
            int goodsCount = 150 + RANDOM.nextInt(100);
            
            item.put("salesAmount", String.format("%.2f", salesAmount));
            item.put("orderCount", orderCount);
            item.put("customerCount", customerCount);
            item.put("goodsCount", goodsCount);
            item.put("avgOrderAmount", String.format("%.2f", salesAmount / orderCount));
            
            // 转化率
            double conversionRate = (customerCount * 100.0 / (customerCount + 20 + RANDOM.nextInt(30)));
            item.put("conversionRate", String.format("%.1f", conversionRate));
            
            // 退款
            double refundAmount = salesAmount * (0.01 + RANDOM.nextDouble() * 0.04);
            item.put("refundAmount", String.format("%.2f", refundAmount));
            item.put("refundRate", String.format("%.1f", refundAmount * 100 / salesAmount));
            
            list.add(item);
        }
        
        // 分页
        int start = (page - 1) * limit;
        int end = Math.min(start + limit, list.size());
        List<Map<String, Object>> pageList = start < list.size() ? list.subList(start, end) : new ArrayList<>();
        
        PageUtils pageUtils = new PageUtils(pageList, list.size(), limit, page);
        
        return ResponseResult.success(pageUtils);
    }
    
    @Override
    public ResponseResult getSalesTrend(Map<String, Object> param) {
        Map<String, Object> trend = new HashMap<>();
        
        List<String> dates = new ArrayList<>();
        List<Double> sales = new ArrayList<>();
        List<Integer> orders = new ArrayList<>();
        List<Integer> customers = new ArrayList<>();
        
        // 生成最近30天的趋势数据
        LocalDate endDate = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = endDate.minusDays(i);
            dates.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            
            // 随机生成趋势数据
            sales.add(30000.0 + RANDOM.nextInt(20000));
            orders.add(100 + RANDOM.nextInt(50));
            customers.add(80 + RANDOM.nextInt(40));
        }
        
        trend.put("dates", dates);
        trend.put("sales", sales);
        trend.put("orders", orders);
        trend.put("customers", customers);
        
        return ResponseResult.success(trend);
    }
    
    @Override
    public ResponseResult exportReport(Map<String, Object> param) {
        // 早期这里拼了个 /exports/xxx.xlsx 直接返回成功，前端拿到的是根本不存在的地址，点了必然 404。
        // 导出（拼 Excel + 落文件服务）尚未实现，这里明确报错，不给假链接
        log.warn("[统计导出] 导出功能尚未实现，已拒绝本次请求 param={}", param);
        return ResponseResult.error("统计报表导出功能尚未实现，请先线下导出或联系研发开通");
    }
    
    @Override
    public ResponseResult getSalesStatistics(Map<String, Object> param) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("todaySales", 45680.50);
        stats.put("todayOrders", 156);
        stats.put("todayGrowth", 8.5);
        
        stats.put("monthSales", 1258960.50);
        stats.put("monthOrders", 3856);
        stats.put("monthGrowth", 15.8);
        
        stats.put("yearSales", 12589605.00);
        stats.put("yearOrders", 38560);
        stats.put("yearGrowth", 25.3);
        
        return ResponseResult.success(stats);
    }
    
    @Override
    public ResponseResult getSalesTrendDetail(Map<String, Object> param) {
        return getSalesTrend(param);
    }
    
    @Override
    public ResponseResult getFinanceStatistics(Map<String, Object> param) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalRevenue", 1258960.50);
        stats.put("totalExpense", 856320.30);
        stats.put("totalProfit", 402640.20);
        stats.put("profitMargin", 31.98);
        
        stats.put("todayRevenue", 45680.50);
        stats.put("todayExpense", 31250.80);
        stats.put("todayProfit", 14429.70);
        
        stats.put("monthRevenue", 1258960.50);
        stats.put("monthExpense", 856320.30);
        stats.put("monthProfit", 402640.20);
        
        return ResponseResult.success(stats);
    }
    
    @Override
    public ResponseResult getInventoryStatisticsOverview(Map<String, Object> param) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalStock", 15680);
        stats.put("alertCount", 35);
        stats.put("todayInbound", 256);
        stats.put("todayOutbound", 189);
        
        stats.put("totalValue", 2568950.50);
        stats.put("avgTurnoverDays", 45);
        
        return ResponseResult.success(stats);
    }
    
    @Override
    public ResponseResult getCustomerStatistics(Map<String, Object> param) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalCustomers", 5680);
        stats.put("newCustomers", 285);
        stats.put("activeCustomers", 1256);
        stats.put("inactiveCustomers", 856);
        
        stats.put("avgPurchaseAmount", 326.52);
        stats.put("avgPurchaseCount", 3.5);
        stats.put("repeatPurchaseRate", 45.8);
        
        return ResponseResult.success(stats);
    }
    
    @Override
    public ResponseResult getGoodsStatisticsOverview(Map<String, Object> param) {
        Map<String, Object> stats = new HashMap<>();

        // ⚠️ 本服务尚未接入数据源（没有注入任何 mapper），以下均为占位数值。
        // 对外显式带 mock=true，避免运营把占位数据当成真实经营指标做决策
        stats.put("mock", true);

        stats.put("totalGoods", 1256);
        stats.put("onSaleGoods", 956);
        stats.put("soldOutGoods", 85);
        stats.put("alertGoods", 35);
        
        stats.put("avgPrice", 256.50);
        stats.put("avgStock", 125);
        stats.put("totalSales", 125896);
        
        return ResponseResult.success(stats);
    }
}
