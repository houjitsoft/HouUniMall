package com.houjiang.goods.service;

import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * 数据统计服务接口
 * 
 * @author Amos
 */
public interface StatisticsService {
    
    // =============== 统计报表 ===============
    
    /**
     * 获取统计概览
     */
    ResponseResult getReportOverview(Map<String, Object> param);
    
    /**
     * 获取统计明细
     */
    ResponseResult getReportDetail(Map<String, Object> param);
    
    /**
     * 获取销售趋势
     */
    ResponseResult getSalesTrend(Map<String, Object> param);
    
    /**
     * 导出统计报表
     */
    ResponseResult exportReport(Map<String, Object> param);
    
    // =============== 销售统计 ===============
    
    /**
     * 获取销售统计
     */
    ResponseResult getSalesStatistics(Map<String, Object> param);
    
    /**
     * 获取销售趋势详情
     */
    ResponseResult getSalesTrendDetail(Map<String, Object> param);
    
    // =============== 财务统计 ===============
    
    /**
     * 获取财务统计
     */
    ResponseResult getFinanceStatistics(Map<String, Object> param);
    
    // =============== 库存统计 ===============
    
    /**
     * 获取库存统计概览
     */
    ResponseResult getInventoryStatisticsOverview(Map<String, Object> param);
    
    // =============== 客户统计 ===============
    
    /**
     * 获取客户统计
     */
    ResponseResult getCustomerStatistics(Map<String, Object> param);
    
    // =============== 商品统计 ===============
    
    /**
     * 获取商品统计概览
     */
    ResponseResult getGoodsStatisticsOverview(Map<String, Object> param);
}
