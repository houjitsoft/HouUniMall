package com.houjiang.goods.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 库存管理控制器
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/goods/inventory")
public class InventoryController {
    
    @Autowired
    private InventoryService inventoryService;
    
    // =============== 库存统计 ===============
    
    /**
     * 获取库存统计概览数据
     */
    @PostMapping("/statistics/overview")
    public ResponseResult getStatisticsOverview(@RequestBody Map<String, Object> param) {
        return inventoryService.getStatisticsOverview(param);
    }
    
    /**
     * 获取库存统计列表
     */
    @PostMapping("/statistics/list")
    public ResponseResult getStatisticsList(@RequestBody Map<String, Object> param) {
        return inventoryService.getStatisticsList(param);
    }
    
    /**
     * 查看库存流水记录
     */
    @PostMapping("/statistics/flow")
    public ResponseResult getInventoryFlow(@RequestBody Map<String, Object> param) {
        return inventoryService.getInventoryFlow(param);
    }
    
    // =============== 库存预警 ===============
    
    /**
     * 获取预警统计概览
     */
    @PostMapping("/alert/overview")
    public ResponseResult getAlertOverview(@RequestBody Map<String, Object> param) {
        return inventoryService.getAlertOverview(param);
    }
    
    /**
     * 获取预警商品列表
     */
    @PostMapping("/alert/list")
    public ResponseResult getAlertList(@RequestBody Map<String, Object> param) {
        return inventoryService.getAlertList(param);
    }
    
    /**
     * 调整预警值
     */
    @PostMapping("/alert/adjust")
    public ResponseResult adjustAlertValue(@RequestBody Map<String, Object> param) {
        return inventoryService.adjustAlertValue(param);
    }
    
    /**
     * 创建采购申请
     */
    @PostMapping("/alert/purchase")
    public ResponseResult createPurchaseRequest(@RequestBody Map<String, Object> param) {
        return inventoryService.createPurchaseRequest(param);
    }
    
    // =============== 库存盘点 ===============
    
    /**
     * 获取盘点单列表
     */
    @PostMapping("/count/list")
    public ResponseResult getCountList(@RequestBody Map<String, Object> param) {
        return inventoryService.getCountList(param);
    }
    
    /**
     * 创建盘点单
     */
    @PostMapping("/count/create")
    public ResponseResult createCount(@RequestBody Map<String, Object> param) {
        return inventoryService.createCount(param);
    }
    
    /**
     * 查看盘点单详情
     */
    @PostMapping("/count/detail")
    public ResponseResult getCountDetail(@RequestBody Map<String, Object> param) {
        return inventoryService.getCountDetail(param);
    }
    
    /**
     * 开始盘点
     */
    @PostMapping("/count/start")
    public ResponseResult startCount(@RequestBody Map<String, Object> param) {
        return inventoryService.startCount(param);
    }
    
    /**
     * 完成盘点
     */
    @PostMapping("/count/complete")
    public ResponseResult completeCount(@RequestBody Map<String, Object> param) {
        return inventoryService.completeCount(param);
    }
    
    /**
     * 取消盘点
     */
    @PostMapping("/count/cancel")
    public ResponseResult cancelCount(@RequestBody Map<String, Object> param) {
        return inventoryService.cancelCount(param);
    }
    
    // =============== 出库管理 ===============
    
    /**
     * 获取出库单列表
     */
    @PostMapping("/outbound/list")
    public ResponseResult getOutboundList(@RequestBody Map<String, Object> param) {
        return inventoryService.getOutboundList(param);
    }
    
    /**
     * 创建出库单
     */
    @PostMapping("/outbound/create")
    public ResponseResult createOutbound(@RequestBody Map<String, Object> param) {
        return inventoryService.createOutbound(param);
    }
    
    /**
     * 查看出库单详情
     */
    @PostMapping("/outbound/detail")
    public ResponseResult getOutboundDetail(@RequestBody Map<String, Object> param) {
        return inventoryService.getOutboundDetail(param);
    }
    
    /**
     * 审核出库单
     */
    @PostMapping("/outbound/approve")
    public ResponseResult approveOutbound(@RequestBody Map<String, Object> param) {
        return inventoryService.approveOutbound(param);
    }
    
    /**
     * 确认出库
     */
    @PostMapping("/outbound/confirm")
    public ResponseResult confirmOutbound(@RequestBody Map<String, Object> param) {
        return inventoryService.confirmOutbound(param);
    }
    
    /**
     * 取消出库单
     */
    @PostMapping("/outbound/cancel")
    public ResponseResult cancelOutbound(@RequestBody Map<String, Object> param) {
        return inventoryService.cancelOutbound(param);
    }
    
    // =============== 入库管理 ===============
    
    /**
     * 获取入库单列表
     */
    @PostMapping("/inbound/list")
    public ResponseResult getInboundList(@RequestBody Map<String, Object> param) {
        return inventoryService.getInboundList(param);
    }
    
    /**
     * 创建入库单
     */
    @PostMapping("/inbound/create")
    public ResponseResult createInbound(@RequestBody Map<String, Object> param) {
        return inventoryService.createInbound(param);
    }
    
    /**
     * 查看入库单详情
     */
    @PostMapping("/inbound/detail")
    public ResponseResult getInboundDetail(@RequestBody Map<String, Object> param) {
        return inventoryService.getInboundDetail(param);
    }
    
    /**
     * 审核入库单
     */
    @PostMapping("/inbound/approve")
    public ResponseResult approveInbound(@RequestBody Map<String, Object> param) {
        return inventoryService.approveInbound(param);
    }
    
    /**
     * 确认入库
     */
    @PostMapping("/inbound/confirm")
    public ResponseResult confirmInbound(@RequestBody Map<String, Object> param) {
        return inventoryService.confirmInbound(param);
    }
    
    /**
     * 取消入库单
     */
    @PostMapping("/inbound/cancel")
    public ResponseResult cancelInbound(@RequestBody Map<String, Object> param) {
        return inventoryService.cancelInbound(param);
    }
}
