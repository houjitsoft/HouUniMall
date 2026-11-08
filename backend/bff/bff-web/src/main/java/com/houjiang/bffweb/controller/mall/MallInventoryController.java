package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.GoodsServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mall商城库存管理接口控制器
/**
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/web/mall/inventory")
public class MallInventoryController {
    
    @Autowired
    private GoodsServiceFeign goodsServiceFeign;
    
    // =============== 库存统计 ===============
    
    /**
     * 获取库存统计数据
     */
    @PostMapping("/statistics/overview")
    public ResponseResult getStatisticsOverview(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getInventoryStatistics(param);
    }
    
    /**
     * 获取库存统计列表
     */
    @PostMapping("/statistics/list")
    public ResponseResult getStatisticsList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getInventoryList(param);
    }
    
    /**
     * 查看库存流水记录
     */
    @PostMapping("/statistics/flow")
    public ResponseResult getInventoryFlow(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getInventoryFlow(param);
    }
    
    // =============== 库存预警 ===============
    
    /**
     * 获取预警统计数据
     */
    @PostMapping("/alert/overview")
    public ResponseResult getAlertOverview(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getInventoryAlertStats(param);
    }
    
    /**
     * 获取预警商品列表
     */
    @PostMapping("/alert/list")
    public ResponseResult getAlertList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getInventoryAlertList(param);
    }
    
    /**
     * 调整预警值
     */
    @PostMapping("/alert/adjust")
    public ResponseResult adjustAlertValue(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.adjustInventoryAlert(param);
    }
    
    /**
     * 创建采购申请
     */
    @PostMapping("/alert/purchase")
    public ResponseResult createPurchaseRequest(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.createPurchaseRequest(param);
    }
    
    // =============== 库存盘点 ===============
    
    /**
     * 获取盘点单列表
     */
    @PostMapping("/count/list")
    public ResponseResult getCountList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getInventoryCountList(param);
    }
    
    /**
     * 创建盘点单
     */
    @PostMapping("/count/create")
    public ResponseResult createCount(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.createInventoryCount(param);
    }
    
    /**
     * 查看盘点单详情
     */
    @PostMapping("/count/detail")
    public ResponseResult getCountDetail(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getInventoryCountDetail(param);
    }
    
    /**
     * 开始盘点
     */
    @PostMapping("/count/start")
    public ResponseResult startCount(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.startInventoryCount(param);
    }
    
    /**
     * 完成盘点
     */
    @PostMapping("/count/complete")
    public ResponseResult completeCount(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.completeInventoryCount(param);
    }
    
    /**
     * 取消盘点
     */
    @PostMapping("/count/cancel")
    public ResponseResult cancelCount(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.cancelInventoryCount(param);
    }
    
    // =============== 出库管理 ===============
    
    /**
     * 获取出库单列表
     */
    @PostMapping("/outbound/list")
    public ResponseResult getOutboundList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getOutboundList(param);
    }
    
    /**
     * 创建出库单
     */
    @PostMapping("/outbound/create")
    public ResponseResult createOutbound(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.createOutbound(param);
    }
    
    /**
     * 查看出库单详情
     */
    @PostMapping("/outbound/detail")
    public ResponseResult getOutboundDetail(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getOutboundDetail(param);
    }
    
    /**
     * 审核出库单
     */
    @PostMapping("/outbound/approve")
    public ResponseResult approveOutbound(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.approveOutbound(param);
    }
    
    /**
     * 确认出库
     */
    @PostMapping("/outbound/confirm")
    public ResponseResult confirmOutbound(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.confirmOutbound(param);
    }
    
    /**
     * 取消出库单
     */
    @PostMapping("/outbound/cancel")
    public ResponseResult cancelOutbound(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.cancelOutbound(param);
    }
    
    // =============== 入库管理 ===============
    
    /**
     * 获取入库单列表
     */
    @PostMapping("/inbound/list")
    public ResponseResult getInboundList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getInboundList(param);
    }
    
    /**
     * 创建入库单
     */
    @PostMapping("/inbound/create")
    public ResponseResult createInbound(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.createInbound(param);
    }
    
    /**
     * 查看入库单详情
     */
    @PostMapping("/inbound/detail")
    public ResponseResult getInboundDetail(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getInboundDetail(param);
    }
    
    /**
     * 审核入库单
     */
    @PostMapping("/inbound/approve")
    public ResponseResult approveInbound(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.approveInbound(param);
    }
    
    /**
     * 确认入库
     */
    @PostMapping("/inbound/confirm")
    public ResponseResult confirmInbound(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.confirmInbound(param);
    }
    
    /**
     * 取消入库单
     */
    @PostMapping("/inbound/cancel")
    public ResponseResult cancelInbound(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.cancelInbound(param);
    }
}
