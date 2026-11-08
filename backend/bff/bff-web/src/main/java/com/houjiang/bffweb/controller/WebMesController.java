package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.MesServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 制造执行系统Web控制器
/**
 * 
 * @description 提供MES制造执行系统相关功能的Web接口
 * @author Amos
 * @date 2024-12-20
 */
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/mes")
public class WebMesController {

    @Autowired
    private MesServiceFeign mesServiceFeign;

    /** ================= 生产计划相关接口 ================= */
    
    @GetMapping("/plan/list")
    public ResponseResult getProductionPlanList(@RequestParam Map<String, Object> param) {
        return mesServiceFeign.getProductionPlanList(param);
    }
    
    @GetMapping("/plan/detail")
    public ResponseResult getProductionPlanDetail(@RequestParam Map<String, Object> param) {
        return mesServiceFeign.getProductionPlanDetail(param);
    }
    
    @PostMapping("/plan/create")
    public ResponseResult createProductionPlan(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.createProductionPlan(param);
    }
    
    @PostMapping("/plan/update")
    public ResponseResult updateProductionPlan(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.updateProductionPlan(param);
    }
    
    @PostMapping("/plan/approve")
    public ResponseResult approveProductionPlan(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.approveProductionPlan(param);
    }
    
    @PostMapping("/plan/reject")
    public ResponseResult rejectProductionPlan(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.rejectProductionPlan(param);
    }
    
    @PostMapping("/plan/publish")
    public ResponseResult publishProductionPlan(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.publishProductionPlan(param);
    }

    /** ================= 工单管理相关接口 ================= */
    
    @GetMapping("/workorder/list")
    public ResponseResult getWorkOrderList(@RequestParam Map<String, Object> param) {
        return mesServiceFeign.getWorkOrderList(param);
    }
    
    @GetMapping("/workorder/detail")
    public ResponseResult getWorkOrderDetail(@RequestParam Map<String, Object> param) {
        return mesServiceFeign.getWorkOrderDetail(param);
    }
    
    @PostMapping("/workorder/create")
    public ResponseResult createWorkOrder(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.createWorkOrder(param);
    }
    
    @PostMapping("/workorder/update")
    public ResponseResult updateWorkOrder(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.updateWorkOrder(param);
    }
    
    @PostMapping("/workorder/start")
    public ResponseResult startWorkOrder(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.startWorkOrder(param);
    }
    
    @PostMapping("/workorder/pause")
    public ResponseResult pauseWorkOrder(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.pauseWorkOrder(param);
    }
    
    @PostMapping("/workorder/resume")
    public ResponseResult resumeWorkOrder(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.resumeWorkOrder(param);
    }
    
    @PostMapping("/workorder/complete")
    public ResponseResult completeWorkOrder(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.completeWorkOrder(param);
    }
    
    @PostMapping("/workorder/cancel")
    public ResponseResult cancelWorkOrder(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.cancelWorkOrder(param);
    }

    /** ================= 设备管理相关接口 ================= */
    
    @GetMapping("/equipment/list")
    public ResponseResult getEquipmentList(@RequestParam Map<String, Object> param) {
        return mesServiceFeign.getEquipmentList(param);
    }
    
    @GetMapping("/equipment/detail")
    public ResponseResult getEquipmentDetail(@RequestParam Map<String, Object> param) {
        return mesServiceFeign.getEquipmentDetail(param);
    }
    
    @PostMapping("/equipment/create")
    public ResponseResult createEquipment(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.createEquipment(param);
    }
    
    @PostMapping("/equipment/update")
    public ResponseResult updateEquipment(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.updateEquipment(param);
    }
    
    @PostMapping("/equipment/start")
    public ResponseResult startEquipment(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.startEquipment(param);
    }
    
    @PostMapping("/equipment/stop")
    public ResponseResult stopEquipment(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.stopEquipment(param);
    }
    
    @PostMapping("/equipment/maintenance")
    public ResponseResult maintenanceEquipment(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.maintenanceEquipment(param);
    }
    
    @PostMapping("/equipment/repair")
    public ResponseResult repairEquipment(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.repairEquipment(param);
    }

    /** ================= 质量管理相关接口 ================= */
    
    @GetMapping("/quality/list")
    public ResponseResult getQualityCheckList(@RequestParam Map<String, Object> param) {
        return mesServiceFeign.getQualityCheckList(param);
    }
    
    @GetMapping("/quality/detail")
    public ResponseResult getQualityCheckDetail(@RequestParam Map<String, Object> param) {
        return mesServiceFeign.getQualityCheckDetail(param);
    }
    
    @PostMapping("/quality/create")
    public ResponseResult createQualityCheck(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.createQualityCheck(param);
    }
    
    @PostMapping("/quality/update")
    public ResponseResult updateQualityCheck(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.updateQualityCheck(param);
    }
    
    @PostMapping("/quality/approve")
    public ResponseResult approveQualityCheck(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.approveQualityCheck(param);
    }
    
    @PostMapping("/quality/reject")
    public ResponseResult rejectQualityCheck(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.rejectQualityCheck(param);
    }

    /** ================= 物料管理相关接口 ================= */
    
    @GetMapping("/material/list")
    public ResponseResult getMaterialList(@RequestParam Map<String, Object> param) {
        return mesServiceFeign.getMaterialList(param);
    }
    
    @GetMapping("/material/detail")
    public ResponseResult getMaterialDetail(@RequestParam Map<String, Object> param) {
        return mesServiceFeign.getMaterialDetail(param);
    }
    
    @PostMapping("/material/create")
    public ResponseResult createMaterial(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.createMaterial(param);
    }
    
    @PostMapping("/material/update")
    public ResponseResult updateMaterial(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.updateMaterial(param);
    }
    
    @PostMapping("/material/inbound")
    public ResponseResult inboundMaterial(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.inboundMaterial(param);
    }
    
    @PostMapping("/material/outbound")
    public ResponseResult outboundMaterial(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.outboundMaterial(param);
    }
    
    @GetMapping("/material/inventory")
    public ResponseResult getMaterialInventory(@RequestParam Map<String, Object> param) {
        return mesServiceFeign.getMaterialInventory(param);
    }

    /** ================= 生产统计相关接口 ================= */
    
    @GetMapping("/statistics/overview")
    public ResponseResult getProductionStatistics(@RequestParam Map<String, Object> param) {
        return mesServiceFeign.getProductionStatistics(param);
    }
    
    @GetMapping("/statistics/daily")
    public ResponseResult getDailyProductionStatistics(@RequestParam Map<String, Object> param) {
        return mesServiceFeign.getDailyProductionStatistics(param);
    }
    
    @GetMapping("/statistics/weekly")
    public ResponseResult getWeeklyProductionStatistics(@RequestParam Map<String, Object> param) {
        return mesServiceFeign.getWeeklyProductionStatistics(param);
    }
    
    @GetMapping("/statistics/monthly")
    public ResponseResult getMonthlyProductionStatistics(@RequestParam Map<String, Object> param) {
        return mesServiceFeign.getMonthlyProductionStatistics(param);
    }
    
    @GetMapping("/statistics/equipment")
    public ResponseResult getEquipmentUtilization(@RequestParam Map<String, Object> param) {
        return mesServiceFeign.getEquipmentUtilization(param);
    }
    
    @GetMapping("/statistics/quality")
    public ResponseResult getQualityStatistics(@RequestParam Map<String, Object> param) {
        return mesServiceFeign.getQualityStatistics(param);
    }

    /** ================= 生产任务相关接口 ================= */
    
    @PostMapping("/production-task/list")
    public ResponseResult getProductionTaskList(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.getProductionTaskList(param);
    }
    
    @PostMapping("/production-task/sub-items")
    public ResponseResult getProductionTaskSubItems(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.getProductionTaskSubItems(param);
    }
    
    @PostMapping("/production-task/requisition")
    public ResponseResult productionTaskRequisition(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.productionTaskRequisition(param);
    }
    
    @PostMapping("/production-task/close")
    public ResponseResult closeProductionTask(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.closeProductionTask(param);
    }
    
    @PostMapping("/production-task/scrap")
    public ResponseResult scrapProductionTask(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.scrapProductionTask(param);
    }
    
    @PostMapping("/production-task/cancel-scrap")
    public ResponseResult cancelScrapProductionTask(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.cancelScrapProductionTask(param);
    }

    /** ================= 车间计划相关接口 ================= */
    
    @PostMapping("/workshop-plan/list")
    public ResponseResult getWorkshopPlanList(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.getWorkshopPlanList(param);
    }
    
    @PostMapping("/workshop-plan/process-list")
    public ResponseResult getWorkshopPlanProcessList(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.getWorkshopPlanProcessList(param);
    }
    
    @PostMapping("/workshop-plan/add-process")
    public ResponseResult addWorkshopPlanProcess(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.addWorkshopPlanProcess(param);
    }
    
    @PostMapping("/workshop-plan/modify-work-center")
    public ResponseResult modifyWorkCenter(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.modifyWorkCenter(param);
    }
    
    @PostMapping("/workshop-plan/modify-process-line-no")
    public ResponseResult modifyProcessLineNo(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.modifyProcessLineNo(param);
    }
    
    @PostMapping("/workshop-plan/delete-process")
    public ResponseResult deleteProcess(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.deleteProcess(param);
    }

    /** ================= 员工报工相关接口 ================= */
    
    @PostMapping("/employee-report/list")
    public ResponseResult getEmployeeReportList(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.getEmployeeReportList(param);
    }
    
    @PostMapping("/employee-report/start")
    public ResponseResult startEmployeeReport(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.startEmployeeReport(param);
    }
    
    @PostMapping("/employee-report/report")
    public ResponseResult reportEmployeeReport(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.reportEmployeeReport(param);
    }
    
    @PostMapping("/employee-report/complete")
    public ResponseResult completeEmployeeReport(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.completeEmployeeReport(param);
    }

    /** ================= 点检作业相关接口 ================= */
    
    @PostMapping("/inspection-task/list")
    public ResponseResult getInspectionTaskList(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.getInspectionTaskList(param);
    }

    /** ================= 工艺路线相关接口 ================= */
    
    @PostMapping("/process-route/list")
    public ResponseResult getProcessRouteList(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.getProcessRouteList(param);
    }
    
    @PostMapping("/process-route/detail-list")
    public ResponseResult getProcessRouteDetailList(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.getProcessRouteDetailList(param);
    }
    
    @PostMapping("/process-route/add")
    public ResponseResult addProcessRoute(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.addProcessRoute(param);
    }
    
    @PostMapping("/process-route/delete")
    public ResponseResult deleteProcessRoute(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.deleteProcessRoute(param);
    }
    
    @PostMapping("/process-route/examine")
    public ResponseResult examineProcessRoute(@RequestBody Map<String, Object> param) {
        return mesServiceFeign.examineProcessRoute(param);
    }
}
