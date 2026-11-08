package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.GoodsServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mall商城数据统计接口控制器
/**
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/web/mall/statistics")
public class MallStatisticsController {
    
    @Autowired
    private GoodsServiceFeign goodsServiceFeign;
    
    // =============== 统计报表 ===============
    
    /**
     * 获取统计概览
     */
    @PostMapping("/report/overview")
    public ResponseResult getReportOverview(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getStatisticsOverview(param);
    }
    
    /**
     * 获取统计明细
     */
    @PostMapping("/report/detail")
    public ResponseResult getReportDetail(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getStatisticsDetail(param);
    }
    
    /**
     * 获取销售趋势
     */
    @PostMapping("/report/trend")
    public ResponseResult getSalesTrend(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getStatisticsTrend(param);
    }
    
    /**
     * 导出统计报表
     */
    @PostMapping("/report/export")
    public ResponseResult exportReport(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.exportStatistics(param);
    }
    
    // =============== 销售统计 ===============
    
    /**
     * 获取销售统计
     */
    @PostMapping("/sales/overview")
    public ResponseResult getSalesStatistics(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getSalesStatistics(param);
    }
    
    /**
     * 获取销售趋势
     */
    @PostMapping("/sales/trend")
    public ResponseResult getSalesTrendDetail(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getSalesTrend(param);
    }
    
    // =============== 财务统计 ===============
    
    /**
     * 获取财务统计
     */
    @PostMapping("/finance/overview")
    public ResponseResult getFinanceStatistics(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getFinanceStatistics(param);
    }
    
    // =============== 库存统计 ===============
    
    /**
     * 获取库存统计
     */
    @PostMapping("/inventory/overview")
    public ResponseResult getInventoryStatistics(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getInventoryStatisticsOverview(param);
    }
    
    // =============== 客户统计 ===============
    
    /**
     * 获取客户统计
     */
    @PostMapping("/customer/overview")
    public ResponseResult getCustomerStatistics(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getCustomerStatistics(param);
    }
    
    // =============== 商品统计 ===============
    
    /**
     * 获取商品统计
     */
    @PostMapping("/goods/overview")
    public ResponseResult getGoodsStatistics(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getGoodsStatisticsOverview(param);
    }
}
