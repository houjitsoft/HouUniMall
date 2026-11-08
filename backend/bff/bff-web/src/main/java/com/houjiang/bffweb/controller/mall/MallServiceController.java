package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.GoodsServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mall商城售后服务接口控制器
/**
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/web/mall/service")
public class MallServiceController {
    
    @Autowired
    private GoodsServiceFeign goodsServiceFeign;
    
    // =============== 服务设置 ===============
    
    /**
     * 获取售后政策配置
     */
    @PostMapping("/setting/policy/get")
    public ResponseResult getPolicyConfig(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getServicePolicy(param);
    }
    
    /**
     * 保存售后政策配置
     */
    @PostMapping("/setting/policy/save")
    public ResponseResult savePolicyConfig(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.saveServicePolicy(param);
    }
    
    /**
     * 获取售后原因列表
     */
    @PostMapping("/setting/reason/list")
    public ResponseResult getReasonList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getServiceReasonList(param);
    }
    
    /**
     * 保存售后原因
     */
    @PostMapping("/setting/reason/save")
    public ResponseResult saveReason(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.saveServiceReason(param);
    }
    
    /**
     * 删除售后原因
     */
    @PostMapping("/setting/reason/delete")
    public ResponseResult deleteReason(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.deleteServiceReason(param);
    }
    
    // =============== 服务统计 ===============
    
    /**
     * 获取服务统计概览
     */
    @PostMapping("/statistics/overview")
    public ResponseResult getStatisticsOverview(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getServiceStatisticsOverview(param);
    }
    
    /**
     * 获取服务类型统计
     */
    @PostMapping("/statistics/type")
    public ResponseResult getTypeStatistics(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getServiceTypeStatistics(param);
    }
    
    /**
     * 获取满意度统计
     */
    @PostMapping("/statistics/satisfaction")
    public ResponseResult getSatisfactionStatistics(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getServiceSatisfactionStatistics(param);
    }
    
    /**
     * 获取服务统计明细
     */
    @PostMapping("/statistics/detail")
    public ResponseResult getStatisticsDetail(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getServiceStatisticsDetail(param);
    }
    
    // =============== 服务记录 ===============
    
    /**
     * 获取服务记录列表
     */
    @PostMapping("/record/list")
    public ResponseResult getRecordList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getServiceRecordList(param);
    }
    
    /**
     * 获取服务记录详情
     */
    @PostMapping("/record/detail")
    public ResponseResult getRecordDetail(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getServiceRecordDetail(param);
    }
    
    /**
     * 获取服务进度
     */
    @PostMapping("/record/timeline")
    public ResponseResult getRecordTimeline(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getServiceRecordTimeline(param);
    }
    
    /**
     * 获取服务评价
     */
    @PostMapping("/record/review")
    public ResponseResult getRecordReview(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getServiceRecordReview(param);
    }
    
    // =============== 服务工单 ===============
    
    /**
     * 获取工单列表
     */
    @PostMapping("/ticket/list")
    public ResponseResult getTicketList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getServiceTicketList(param);
    }
    
    /**
     * 获取工单详情
     */
    @PostMapping("/ticket/detail")
    public ResponseResult getTicketDetail(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getServiceTicketDetail(param);
    }
    
    /**
     * 接单
     */
    @PostMapping("/ticket/accept")
    public ResponseResult acceptTicket(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.acceptServiceTicket(param);
    }
    
    /**
     * 处理工单
     */
    @PostMapping("/ticket/process")
    public ResponseResult processTicket(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.processServiceTicket(param);
    }
    
    /**
     * 完成工单
     */
    @PostMapping("/ticket/complete")
    public ResponseResult completeTicket(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.completeServiceTicket(param);
    }
    
    /**
     * 取消工单
     */
    @PostMapping("/ticket/cancel")
    public ResponseResult cancelTicket(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.cancelServiceTicket(param);
    }
    
    /**
     * 获取工单统计
     */
    @PostMapping("/ticket/count")
    public ResponseResult getTicketCount(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.getServiceTicketCount(param);
    }
}
