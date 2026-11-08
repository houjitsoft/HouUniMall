package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 制造执行系统服务Feign客户端降级处理
/**
 * 只做降级处理，不包含业务逻辑
 * @author Amos
 */
@Component
public class MesServiceFeignFallback implements MesServiceFeign {

    private static final String ERROR_MSG = "MES服务暂时不可用，请稍后重试";

    /** ================= 生产计划相关接口 ================= */
    @Override
    public ResponseResult getProductionPlanList(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getProductionPlanDetail(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult createProductionPlan(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult updateProductionPlan(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult deleteProductionPlan(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult approveProductionPlan(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult rejectProductionPlan(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult publishProductionPlan(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }

    /** ================= 工单管理相关接口 ================= */
    @Override
    public ResponseResult getWorkOrderList(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getWorkOrderDetail(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult createWorkOrder(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult updateWorkOrder(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult startWorkOrder(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult finishWorkOrder(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult pauseWorkOrder(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult resumeWorkOrder(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult completeWorkOrder(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult cancelWorkOrder(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }

    /** ================= 设备管理相关接口 ================= */
    @Override
    public ResponseResult getEquipmentList(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getEquipmentDetail(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult equipmentMaintenance(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult equipmentRepair(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getEquipmentStatus(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult createEquipment(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult updateEquipment(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult startEquipment(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult stopEquipment(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult maintenanceEquipment(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult repairEquipment(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }

    /** ================= 质量管理相关接口 ================= */
    @Override
    public ResponseResult getQualityInspectionList(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult qualityInspect(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getDefectList(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult handleDefect(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getQualityCheckList(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getQualityCheckDetail(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult createQualityCheck(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult updateQualityCheck(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult approveQualityCheck(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult rejectQualityCheck(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }

    /** ================= 物料管理相关接口 ================= */
    @Override
    public ResponseResult getMaterialInventory(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult materialRequisition(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult materialReturn(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getMaterialTrace(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getMaterialList(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getMaterialDetail(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult createMaterial(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult updateMaterial(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult inboundMaterial(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult outboundMaterial(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }

    /** ================= 生产统计相关接口 ================= */
    @Override
    public ResponseResult getProductionStatistics(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getQualityStatistics(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getEquipmentStatistics(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getOEE(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getDailyProductionStatistics(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getWeeklyProductionStatistics(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getMonthlyProductionStatistics(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getEquipmentUtilization(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }

    /** ================= 生产任务相关接口 ================= */
    @Override
    public ResponseResult getProductionTaskList(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getProductionTaskSubItems(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult productionTaskRequisition(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult closeProductionTask(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult scrapProductionTask(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult cancelScrapProductionTask(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }

    /** ================= 车间计划相关接口 ================= */
    @Override
    public ResponseResult getWorkshopPlanList(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getWorkshopPlanProcessList(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult addWorkshopPlanProcess(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult modifyWorkCenter(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult modifyProcessLineNo(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult deleteProcess(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }

    /** ================= 员工报工相关接口 ================= */
    @Override
    public ResponseResult getEmployeeReportList(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult startEmployeeReport(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult reportEmployeeReport(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult completeEmployeeReport(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }

    /** ================= 点检作业相关接口 ================= */
    @Override
    public ResponseResult getInspectionTaskList(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }

    /** ================= 工艺路线相关接口 ================= */
    @Override
    public ResponseResult getProcessRouteList(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult getProcessRouteDetailList(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult addProcessRoute(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult deleteProcessRoute(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
    @Override
    public ResponseResult examineProcessRoute(Map<String, Object> param) { return ResponseResult.fail(ERROR_MSG); }
}
