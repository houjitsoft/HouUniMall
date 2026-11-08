package com.houjiang.goods.service;

import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * 库存管理服务接口
 * 
 * @author Amos
 */
public interface InventoryService {
    
    // =============== 库存统计 ===============
    
    /**
     * 获取库存统计概览数据
     */
    ResponseResult getStatisticsOverview(Map<String, Object> param);
    
    /**
     * 获取库存统计列表
     */
    ResponseResult getStatisticsList(Map<String, Object> param);
    
    /**
     * 查看库存流水记录
     */
    ResponseResult getInventoryFlow(Map<String, Object> param);
    
    // =============== 库存预警 ===============
    
    /**
     * 获取预警统计概览
     */
    ResponseResult getAlertOverview(Map<String, Object> param);
    
    /**
     * 获取预警商品列表
     */
    ResponseResult getAlertList(Map<String, Object> param);
    
    /**
     * 调整预警值
     */
    ResponseResult adjustAlertValue(Map<String, Object> param);
    
    /**
     * 创建采购申请
     */
    ResponseResult createPurchaseRequest(Map<String, Object> param);
    
    // =============== 库存盘点 ===============
    
    /**
     * 获取盘点单列表
     */
    ResponseResult getCountList(Map<String, Object> param);
    
    /**
     * 创建盘点单
     */
    ResponseResult createCount(Map<String, Object> param);
    
    /**
     * 查看盘点单详情
     */
    ResponseResult getCountDetail(Map<String, Object> param);
    
    /**
     * 开始盘点
     */
    ResponseResult startCount(Map<String, Object> param);
    
    /**
     * 完成盘点
     */
    ResponseResult completeCount(Map<String, Object> param);
    
    /**
     * 取消盘点
     */
    ResponseResult cancelCount(Map<String, Object> param);
    
    // =============== 出库管理 ===============
    
    /**
     * 获取出库单列表
     */
    ResponseResult getOutboundList(Map<String, Object> param);
    
    /**
     * 创建出库单
     */
    ResponseResult createOutbound(Map<String, Object> param);
    
    /**
     * 查看出库单详情
     */
    ResponseResult getOutboundDetail(Map<String, Object> param);
    
    /**
     * 审核出库单
     */
    ResponseResult approveOutbound(Map<String, Object> param);
    
    /**
     * 确认出库
     */
    ResponseResult confirmOutbound(Map<String, Object> param);
    
    /**
     * 取消出库单
     */
    ResponseResult cancelOutbound(Map<String, Object> param);
    
    // =============== 入库管理 ===============
    
    /**
     * 获取入库单列表
     */
    ResponseResult getInboundList(Map<String, Object> param);
    
    /**
     * 创建入库单
     */
    ResponseResult createInbound(Map<String, Object> param);
    
    /**
     * 查看入库单详情
     */
    ResponseResult getInboundDetail(Map<String, Object> param);
    
    /**
     * 审核入库单
     */
    ResponseResult approveInbound(Map<String, Object> param);
    
    /**
     * 确认入库
     */
    ResponseResult confirmInbound(Map<String, Object> param);
    
    /**
     * 取消入库单
     */
    ResponseResult cancelInbound(Map<String, Object> param);
}
