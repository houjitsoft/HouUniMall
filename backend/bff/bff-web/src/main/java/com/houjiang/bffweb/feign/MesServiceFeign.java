package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 制造执行系统服务Feign客户端
/**
 * 
 * @description 提供MES制造执行系统相关功能的远程调用接口
 * @author Amos
 * @date 2024-12-20
 */
@FeignClient(value = "mes", url = "${feign.mes.url:}", path = "/plugin/mes", fallback = MesServiceFeignFallback.class)
public interface MesServiceFeign {

    // ================= 生产计划相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/production-plan/list")
    ResponseResult getProductionPlanList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/production-plan/detail")
    ResponseResult getProductionPlanDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/production-plan/create")
    ResponseResult createProductionPlan(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/production-plan/update")
    ResponseResult updateProductionPlan(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/production-plan/delete")
    ResponseResult deleteProductionPlan(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/production-plan/approve")
    ResponseResult approveProductionPlan(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/production-plan/reject")
    ResponseResult rejectProductionPlan(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/production-plan/publish")
    ResponseResult publishProductionPlan(@RequestBody Map<String, Object> param);

    // ================= 工单管理相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/work-order/list")
    ResponseResult getWorkOrderList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/work-order/detail")
    ResponseResult getWorkOrderDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/work-order/create")
    ResponseResult createWorkOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/work-order/update")
    ResponseResult updateWorkOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/work-order/start")
    ResponseResult startWorkOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/work-order/finish")
    ResponseResult finishWorkOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/work-order/pause")
    ResponseResult pauseWorkOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/work-order/resume")
    ResponseResult resumeWorkOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/work-order/complete")
    ResponseResult completeWorkOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/work-order/cancel")
    ResponseResult cancelWorkOrder(@RequestBody Map<String, Object> param);

    // ================= 设备管理相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/equipment/list")
    ResponseResult getEquipmentList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/equipment/detail")
    ResponseResult getEquipmentDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/equipment/maintenance")
    ResponseResult equipmentMaintenance(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/equipment/repair")
    ResponseResult equipmentRepair(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/equipment/status")
    ResponseResult getEquipmentStatus(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/equipment/create")
    ResponseResult createEquipment(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/equipment/update")
    ResponseResult updateEquipment(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/equipment/start")
    ResponseResult startEquipment(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/equipment/stop")
    ResponseResult stopEquipment(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/equipment/maintenance")
    ResponseResult maintenanceEquipment(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/equipment/repair")
    ResponseResult repairEquipment(@RequestBody Map<String, Object> param);

    // ================= 质量管理相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/quality/inspection-list")
    ResponseResult getQualityInspectionList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/quality/inspect")
    ResponseResult qualityInspect(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/quality/defect-list")
    ResponseResult getDefectList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/quality/defect-handle")
    ResponseResult handleDefect(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/quality/check-list")
    ResponseResult getQualityCheckList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/quality/check-detail")
    ResponseResult getQualityCheckDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/quality/check-create")
    ResponseResult createQualityCheck(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/quality/check-update")
    ResponseResult updateQualityCheck(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/quality/check-approve")
    ResponseResult approveQualityCheck(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/quality/check-reject")
    ResponseResult rejectQualityCheck(@RequestBody Map<String, Object> param);

    // ================= 物料管理相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/material/inventory")
    ResponseResult getMaterialInventory(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/material/requisition")
    ResponseResult materialRequisition(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/material/return")
    ResponseResult materialReturn(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/material/trace")
    ResponseResult getMaterialTrace(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/material/list")
    ResponseResult getMaterialList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/material/detail")
    ResponseResult getMaterialDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/material/create")
    ResponseResult createMaterial(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/material/update")
    ResponseResult updateMaterial(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/material/inbound")
    ResponseResult inboundMaterial(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/material/outbound")
    ResponseResult outboundMaterial(@RequestBody Map<String, Object> param);

    // ================= 生产统计相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/statistics/production")
    ResponseResult getProductionStatistics(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/statistics/quality")
    ResponseResult getQualityStatistics(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/statistics/equipment")
    ResponseResult getEquipmentStatistics(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/statistics/oee")
    ResponseResult getOEE(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/statistics/daily-production")
    ResponseResult getDailyProductionStatistics(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/statistics/weekly-production")
    ResponseResult getWeeklyProductionStatistics(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/statistics/monthly-production")
    ResponseResult getMonthlyProductionStatistics(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/statistics/equipment-utilization")
    ResponseResult getEquipmentUtilization(@RequestParam Map<String, Object> param);

    // ================= 生产任务相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/production-plan/list")
    ResponseResult getProductionTaskList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/production-plan/sub-items")
    ResponseResult getProductionTaskSubItems(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/production-plan/requisition")
    ResponseResult productionTaskRequisition(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/production-plan/close")
    ResponseResult closeProductionTask(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/production-plan/scrap")
    ResponseResult scrapProductionTask(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/production-plan/cancel-scrap")
    ResponseResult cancelScrapProductionTask(@RequestBody Map<String, Object> param);

    // ================= 车间计划相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/workshop-plan/list")
    ResponseResult getWorkshopPlanList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/workshop-plan/process-list")
    ResponseResult getWorkshopPlanProcessList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/workshop-plan/add-process")
    ResponseResult addWorkshopPlanProcess(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/workshop-plan/modify-work-center")
    ResponseResult modifyWorkCenter(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/workshop-plan/modify-process-line-no")
    ResponseResult modifyProcessLineNo(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/workshop-plan/delete-process")
    ResponseResult deleteProcess(@RequestBody Map<String, Object> param);

    // ================= 员工报工相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/employee-report/list")
    ResponseResult getEmployeeReportList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/employee-report/start")
    ResponseResult startEmployeeReport(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/employee-report/report")
    ResponseResult reportEmployeeReport(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/employee-report/complete")
    ResponseResult completeEmployeeReport(@RequestBody Map<String, Object> param);

    // ================= 点检作业相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/inspection-task/list")
    ResponseResult getInspectionTaskList(@RequestBody Map<String, Object> param);

    // ================= 工艺路线相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/process-route/list")
    ResponseResult getProcessRouteList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/process-route/detail-list")
    ResponseResult getProcessRouteDetailList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/process-route/add")
    ResponseResult addProcessRoute(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/process-route/delete")
    ResponseResult deleteProcessRoute(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/process-route/examine")
    ResponseResult examineProcessRoute(@RequestBody Map<String, Object> param);
}
