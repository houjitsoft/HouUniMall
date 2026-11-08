package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * OA服务Feign客户端
/**
 * 只做服务调用，不包含业务逻辑。
 * OA 作为 Mall PF4J 插件运行，所有调用固定进入 Mall 的插件分发通道。
 * @author Amos
 */
@FeignClient(name = "oa", url = "${feign.oa.url:}", path = "/plugin/oa", contextId = "oaServiceFeign", fallback = OaServiceFeignFallback.class)
public interface OaServiceFeign {

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/dashboard/summary")
    ResponseResult getDashboardSummary();

    /**
     * TODO 补充方法说明
/**
     * @param page TODO
     * @param limit TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/dashboard/todo")
    ResponseResult getTodoList(
            @RequestParam Integer page,
            @RequestParam Integer limit,
            @RequestParam(required = false) String keyword);

    /**
     * TODO 补充方法说明
/**
     * @param page TODO
     * @param limit TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/notice")
    ResponseResult getNoticeList(
            @RequestParam Integer page,
            @RequestParam Integer limit,
            @RequestParam(required = false) String type);

    /**
     * TODO 补充方法说明
/**
     * @param queryParams TODO
     * @return TODO
     */
    @PostMapping("/hr/employee/v2")
    ResponseResult getEmployeeList(@RequestBody Map<String, Object> queryParams);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/hr/employee/sync-wecom")
    ResponseResult syncWecomEmployees();

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/hr/employee/sync-platform")
    ResponseResult syncPlatformEmployees();

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/hr/employee-sync/platform")
    ResponseResult syncPlatformEmployeesAlias();

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/hr/employee-sync/wecom")
    ResponseResult syncWecomEmployeesAlias();

    /**
     * TODO 补充方法说明
/**
     * @param employeeData TODO
     * @return TODO
     */
    @PostMapping("/hr/employee/v2/create")
    ResponseResult createEmployee(@RequestBody Map<String, Object> employeeData);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @param employeeData TODO
     * @return TODO
     */
    @PostMapping("/hr/employee/v2/{id}")
    ResponseResult updateEmployee(@PathVariable Integer id, @RequestBody Map<String, Object> employeeData);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @DeleteMapping("/hr/employee/v2/{id}")
    ResponseResult deleteEmployee(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @GetMapping("/hr/employee/v2/{id}")
    ResponseResult getEmployeeDetail(@PathVariable Long id);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/hr/employee/v2/options")
    ResponseResult getEmployeeOptions(
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) Boolean includeResigned,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String orgId);

    // ==================== 员工薪资档案 ====================

    /**
     * TODO 补充方法说明
/**
     * @param employeeId TODO
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/hr/employee-salary/by-employee/{employeeId}")
    ResponseResult getEmployeeSalaryByEmployee(@PathVariable Integer employeeId,
                                               @RequestParam(required = false) String orgId);

    /**
     * TODO 补充方法说明
/**
     * @param salaryData TODO
     * @return TODO
     */
    @PostMapping("/hr/employee-salary/upsert")
    ResponseResult upsertEmployeeSalary(@RequestBody Map<String, Object> salaryData);

    /**
     * TODO 补充方法说明
/**
     * @param page TODO
     * @param limit TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/attendance/record")
    ResponseResult getAttendanceRecord(
            @RequestParam Integer page,
            @RequestParam Integer limit,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/attendance/record/page")
    ResponseResult getAttendanceRecordPage(@RequestBody Map<String, Object> params);

    @PostMapping("/attendance/statistics/summary")
    ResponseResult getAttendanceStatisticsSummary(@RequestBody Map<String, Object> params);

    @PostMapping("/attendance/statistics/department")
    ResponseResult getAttendanceStatisticsDepartment(@RequestBody Map<String, Object> params);

    @PostMapping("/attendance/statistics/individual")
    ResponseResult getAttendanceStatisticsIndividual(@RequestBody Map<String, Object> params);

    @PostMapping("/attendance/statistics/trend")
    ResponseResult getAttendanceStatisticsTrend(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/attendance/lists/page")
    ResponseResult getAttendanceListPage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/attendance/lists/checkin")
    ResponseResult getAttendanceCheckinDetail(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/attendance/lists/makeup")
    ResponseResult makeupAttendance(@RequestBody Map<String, Object> params);

    @PostMapping("/attendance/lists/whitelist")
    ResponseResult setAttendanceWhitelist(@RequestBody Map<String, Object> params);

    /** 按考勤设置同步（新 OA）；旧包可能无此映射 */
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/attendance/lists/sync")
    ResponseResult syncAttendanceLists(@RequestBody Map<String, Object> params);

    /** 企微同步（当前线上 oa.jar 5/27 仅有此接口） */
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/attendance/lists/sync-wecom")
    ResponseResult syncAttendanceListsWecom(@RequestBody Map<String, Object> params);

    /** 新 OA AttendanceController 路径 */
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/attendance/sync")
    ResponseResult syncAttendanceRoot(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/attendance/sync-wecom")
    ResponseResult syncWecomAttendanceRoot(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/background-job/status")
    ResponseResult getOaBackgroundJobStatus(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/attendance/leave/page")
    ResponseResult getLeavePage(@RequestBody Map<String, Object> params);

    @PostMapping("/attendance/leave/apply")
    ResponseResult applyLeave(@RequestBody Map<String, Object> params);

    @PostMapping("/attendance/leave/approve/{id}")
    ResponseResult approveLeave(@PathVariable("id") Long id, @RequestBody Map<String, Object> params);

    @PostMapping("/attendance/leave/cancel/{id}")
    ResponseResult cancelLeave(@PathVariable("id") Long id);

    @PostMapping("/attendance/leave/statistics")
    ResponseResult getLeaveStatistics(@RequestBody(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/attendance/overtime/page")
    ResponseResult getOvertimePage(@RequestBody Map<String, Object> params);

    @PostMapping("/attendance/overtime/apply")
    ResponseResult applyOvertime(@RequestBody Map<String, Object> params);

    @PostMapping("/attendance/overtime/approve/{id}")
    ResponseResult approveOvertime(@PathVariable("id") Long id, @RequestBody Map<String, Object> params);

    @PostMapping("/attendance/overtime/cancel/{id}")
    ResponseResult cancelOvertime(@PathVariable("id") Long id);

    @PostMapping("/attendance/overtime/statistics")
    ResponseResult getOvertimeStatistics(@RequestBody(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/attendance/travel/page")
    ResponseResult getTravelPage(@RequestBody Map<String, Object> params);

    @PostMapping("/attendance/travel/apply")
    ResponseResult applyTravel(@RequestBody Map<String, Object> params);

    @PostMapping("/attendance/travel/approve/{id}")
    ResponseResult approveTravel(@PathVariable("id") Long id, @RequestBody Map<String, Object> params);

    @PostMapping("/attendance/travel/cancel/{id}")
    ResponseResult cancelTravel(@PathVariable("id") Long id);

    @PostMapping("/attendance/travel/complete/{id}")
    ResponseResult completeTravel(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/attendance/checkin")
    ResponseResult checkIn(@RequestParam(required = false) String location);

    /** 考勤来源配置由 OA 直读同库配置，不能依赖 thirdparty/钉钉服务。 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/attendance/platform-config")
    ResponseResult getAttendancePlatformConfig();

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/attendance/platform-config")
    ResponseResult saveAttendancePlatformConfig(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param page TODO
     * @param limit TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/salary/payslip")
    ResponseResult getSalaryList(
            @RequestParam Integer page,
            @RequestParam Integer limit,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String yearMonth);

    /**
     * TODO 补充方法说明
/**
     * @param page TODO
     * @param limit TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/document")
    ResponseResult getDocumentList(
            @RequestParam Integer page,
            @RequestParam Integer limit,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category);

    /**
     * TODO 补充方法说明
/**
     * @param page TODO
     * @param limit TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/meeting/room")
    ResponseResult getMeetingRoomList(
            @RequestParam Integer page,
            @RequestParam Integer limit,
            @RequestParam(required = false) String name);

    /**
     * TODO 补充方法说明
/**
     * @param roomId TODO
     * @param startTime TODO
     * @param endTime TODO
     * @param subject TODO
     * @return TODO
     */
    @PostMapping("/meeting/reserve")
    ResponseResult reserveMeetingRoom(
            @RequestParam Long roomId,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam String subject);

    // ==================== 资产管理接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/asset/list")
    ResponseResult getAssetListV2(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param assetData TODO
     * @return TODO
     */
    @PostMapping("/api/asset/create")
    ResponseResult createAsset(@RequestBody Map<String, Object> assetData);

    /**
     * TODO 补充方法说明
/**
     * @param assetData TODO
     * @return TODO
     */
    @PostMapping("/api/asset/update")
    ResponseResult updateAsset(@RequestBody Map<String, Object> assetData);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/asset/delete/{id}")
    ResponseResult deleteAsset(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/asset/detail/{id}")
    ResponseResult getAssetDetail(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/asset/allocate/{id}")
    ResponseResult allocateAsset(@PathVariable("id") Long id, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/asset/recycle/{id}")
    ResponseResult recycleAsset(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/asset/maintenance/{id}")
    ResponseResult maintenanceAsset(@PathVariable("id") Long id, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/asset/scrap/{id}")
    ResponseResult scrapAsset(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/asset/statistics")
    ResponseResult getAssetStatisticsV2();

    /**
     * TODO 补充方法说明
/**
     * @param page TODO
     * @param limit TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/vehicle")
    ResponseResult getVehicleList(
            @RequestParam Integer page,
            @RequestParam Integer limit,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status);

    /**
     * TODO 补充方法说明
/**
     * @param vehicleId TODO
     * @param startTime TODO
     * @param endTime TODO
     * @param purpose TODO
     * @return TODO
     */
    @PostMapping("/vehicle/apply")
    ResponseResult applyVehicle(
            @RequestParam Long vehicleId,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam String purpose);

    /**
     * TODO 补充方法说明
/**
     * @param type TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/report/statistics")
    ResponseResult getReportStatistics(
            @RequestParam String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate);

    // ==================== HR管理扩展接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param page TODO
     * @param size TODO
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/system/department/page")
    ResponseResult getDepartmentPage(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String departmentCode,
            @RequestParam(required = false) Long parentId);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/system/department/tree")
    ResponseResult getDepartmentTree();

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/system/department/sync-wecom")
    ResponseResult syncDepartmentsFromWecom();

    /**
     * TODO 补充方法说明
/**
     * @param department TODO
     * @return TODO
     */
    @PostMapping("/system/department")
    ResponseResult addDepartment(@RequestBody Map<String, Object> department);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @param department TODO
     * @return TODO
     */
    @PostMapping("/system/department/{id:\\d+}")
    ResponseResult updateDepartment(@PathVariable Long id, @RequestBody Map<String, Object> department);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @DeleteMapping("/system/department/{id:\\d+}")
    ResponseResult deleteDepartment(@PathVariable Long id);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @GetMapping("/system/department/{id:\\d+}")
    ResponseResult getDepartmentById(@PathVariable Long id);

           /**
            * TODO 补充方法说明
/**
            * @param page TODO
            * @param size TODO
            * @param false TODO
            * @return TODO
            */
           @GetMapping("/system/position/page")
           ResponseResult getPositionPage(
                   @RequestParam Integer page,
                   @RequestParam Integer size,
                   @RequestParam(required = false) String name);
       
           /**
            * TODO 补充方法说明
/**
            * @param position TODO
            * @return TODO
            */
           @PostMapping("/system/position")
           ResponseResult addPosition(@RequestBody Map<String, Object> position);
       
           /**
            * TODO 补充方法说明
/**
            * @param id TODO
            * @param position TODO
            * @return TODO
            */
           @PostMapping("/system/position/{id}")
           ResponseResult updatePosition(@PathVariable Integer id, @RequestBody Map<String, Object> position);
       
           /**
            * TODO 补充方法说明
/**
            * @param id TODO
            * @return TODO
            */
           @DeleteMapping("/system/position/{id}")
           ResponseResult deletePosition(@PathVariable Integer id);
       
           /**
            * TODO 补充方法说明
/**
            * @param id TODO
            * @return TODO
            */
           @GetMapping("/system/position/{id}")
           ResponseResult getPositionById(@PathVariable Integer id);

           /**
            * TODO 补充方法说明
/**
            * @return TODO
            */
           @PostMapping("/system/position/sync-wecom")
           ResponseResult syncPositionsFromWecom();

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/hr/performance/page")
    ResponseResult getPerformancePage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param performance TODO
     * @return TODO
     */
    @PostMapping("/hr/performance")
    ResponseResult addPerformance(@RequestBody Map<String, Object> performance);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @GetMapping("/hr/performance/{id}")
    ResponseResult getPerformanceById(@PathVariable Long id);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @PostMapping("/hr/performance/{id}/start")
    ResponseResult startPerformanceEvaluation(@PathVariable Long id);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @DeleteMapping("/hr/performance/{id}")
    ResponseResult deletePerformance(@PathVariable Long id);

    /**
     * TODO 补充方法说明
/**
     * @param page TODO
     * @param size TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/api/hr/recruit/positions")
    ResponseResult getRecruitPositionPage(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department);

    /**
     * TODO 补充方法说明
/**
     * @param position TODO
     * @return TODO
     */
    @PostMapping("/api/hr/recruit/positions")
    ResponseResult addRecruitPosition(@RequestBody Map<String, Object> position);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @param position TODO
     * @return TODO
     */
    @PostMapping("/api/hr/recruit/positions/{id}")
    ResponseResult updateRecruitPosition(@PathVariable Long id, @RequestBody Map<String, Object> position);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @PostMapping("/api/hr/recruit/positions/{id}")
    ResponseResult deleteRecruitPosition(@PathVariable Long id);

    // ==================== 薪资管理接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/setting/page")
    ResponseResult getSalarySettingPage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @GetMapping("/api/salary/setting/{id}")
    ResponseResult getSalarySettingById(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param salaryData TODO
     * @return TODO
     */
    @PostMapping("/api/salary/setting")
    ResponseResult saveSalarySetting(@RequestBody Map<String, Object> salaryData);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @param salaryData TODO
     * @return TODO
     */
    @PostMapping("/api/salary/setting/{id}")
    ResponseResult updateSalarySetting(@PathVariable Integer id, @RequestBody Map<String, Object> salaryData);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @DeleteMapping("/api/salary/setting/{id}")
    ResponseResult deleteSalarySetting(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param employeeId TODO
     * @return TODO
     */
    @GetMapping("/api/salary/setting/employee/{employeeId}")
    ResponseResult getSalarySettingByEmployeeId(@PathVariable Integer employeeId);

    /**
     * TODO 补充方法说明
/**
     * @param salaryList TODO
     * @return TODO
     */
    @PostMapping("/api/salary/setting/batch")
    ResponseResult batchSaveSalarySetting(@RequestBody java.util.List<Map<String, Object>> salaryList);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/statistics/summary")
    ResponseResult getSalarySummary(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/statistics/department")
    ResponseResult getDepartmentStatistics(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/statistics/individual")
    ResponseResult getIndividualStatistics(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/statistics/comparison")
    ResponseResult getSalaryComparison(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/statistics/export")
    ResponseResult exportSalaryStatistics(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/payment/page")
    ResponseResult getPayslipPage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @GetMapping("/api/salary/payment/{id}")
    ResponseResult getPayslipDetail(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/payment/generate")
    ResponseResult generatePayslip(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/payment/batch-send")
    ResponseResult batchSendPayslip(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @PostMapping("/api/salary/payment/send/{id}")
    ResponseResult sendPayslip(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @PostMapping("/api/salary/payment/resend/{id}")
    ResponseResult resendPayslip(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @PostMapping("/api/salary/payment/download/{id}")
    ResponseResult downloadPayslip(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/payment/export")
    ResponseResult exportPayslip(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/payslip/page")
    ResponseResult getSalaryPayslipManagePage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @PostMapping("/api/salary/payslip/detail/{id}")
    ResponseResult getSalaryPayslipManageDetail(@PathVariable Long id);

    @PostMapping("/api/oa/salary/payslip/payment/{id}")
    ResponseResult recordSalaryPayslipPayment(@PathVariable Long id, @RequestBody Map<String, Object> params);

    @PostMapping("/api/oa/salary/payslip/payment-records/{id}")
    ResponseResult getSalaryPayslipPaymentRecords(@PathVariable Long id, @RequestBody Map<String, Object> params);

    @PostMapping("/api/salary/payslip/queue/{id}")
    ResponseResult queueSalaryPayslipManage(@PathVariable Long id, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/payslip/send/{id}")
    ResponseResult sendSalaryPayslipManage(@PathVariable Long id, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/payslip/resend/{id}")
    ResponseResult resendSalaryPayslipManage(@PathVariable Long id, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/payslip/batch-send")
    ResponseResult batchSendSalaryPayslipManage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/structure/page")
    ResponseResult getSalaryStructurePage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @GetMapping("/api/salary/structure/{id}")
    ResponseResult getSalaryStructureById(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param structureData TODO
     * @return TODO
     */
    @PostMapping("/api/salary/structure")
    ResponseResult saveSalaryStructure(@RequestBody Map<String, Object> structureData);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @param structureData TODO
     * @return TODO
     */
    @PostMapping("/api/salary/structure/{id}")
    ResponseResult updateSalaryStructure(@PathVariable Integer id, @RequestBody Map<String, Object> structureData);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @DeleteMapping("/api/salary/structure/{id}")
    ResponseResult deleteSalaryStructure(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/api/salary/structure/types")
    ResponseResult getSalaryStructureTypes();

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/api/salary/structure/list")
    ResponseResult getSalaryStructureList();

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/calculate/page")
    ResponseResult getSalaryCalculatePage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @GetMapping("/api/salary/calculate/{id}")
    ResponseResult getSalaryCalculateDetail(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/calculate/employees/page")
    ResponseResult salaryCalculateEmployeesPage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/salary/calculate/rules")
    ResponseResult listSalaryCalculateRules();

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/calculate/preview")
    ResponseResult previewSalaryCalculation(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/calculate/confirm")
    ResponseResult confirmSalaryCalculation(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/calculate/batch")
    ResponseResult batchCalculate(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @PostMapping("/api/salary/calculate/recalculate/{id}")
    ResponseResult recalculate(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/calculate/validate")
    ResponseResult validateCalculation(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/calculate/history")
    ResponseResult getCalculationHistory(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/calculate/export")
    ResponseResult exportCalculation(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/calculate/sync-employee-salary-from-payroll")
    ResponseResult syncEmployeeSalaryFromPayroll(@RequestBody Map<String, Object> params);

    // ==================== 日报扣款规则接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/report-deduction/page")
    ResponseResult getReportDeductionPage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @GetMapping("/api/report-deduction/{id}")
    ResponseResult getReportDeductionDetail(@PathVariable Long id);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/report-deduction/compute")
    ResponseResult computeReportDeduction(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @PostMapping("/api/report-deduction/lock/{id}")
    ResponseResult lockReportDeduction(@PathVariable Long id);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/report-deduction/appeal/{id}")
    ResponseResult updateReportDeductionAppeal(@PathVariable Long id, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/report-deduction/submit-log/list")
    ResponseResult getReportSubmitLogList(@RequestBody Map<String, Object> params);

    // ==================== 节假日配置接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/holiday-config/page")
    ResponseResult getHolidayConfigPage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param month TODO
     * @return TODO
     */
    @GetMapping("/api/holiday-config/month/{month}")
    ResponseResult getHolidayConfigByMonth(@PathVariable String month);

    /**
     * TODO 补充方法说明
/**
     * @param entity TODO
     * @return TODO
     */
    @PostMapping("/api/holiday-config/save")
    ResponseResult saveHolidayConfig(@RequestBody Map<String, Object> entity);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @PostMapping("/api/holiday-config/delete/{id}")
    ResponseResult deleteHolidayConfig(@PathVariable Long id);

    // ==================== 请假申请接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/leave-apply/page")
    ResponseResult getLeaveApplyPage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param entity TODO
     * @return TODO
     */
    @PostMapping("/api/leave-apply/save")
    ResponseResult saveLeaveApply(@RequestBody Map<String, Object> entity);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/leave-apply/approve/{id}")
    ResponseResult approveLeaveApply(@PathVariable Long id, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @PostMapping("/api/leave-apply/delete/{id}")
    ResponseResult deleteLeaveApply(@PathVariable Long id);

    // ==================== 薪资规则管理接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/rules/page")
    ResponseResult getRulesPage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @GetMapping("/api/salary/rules/{id}")
    ResponseResult getRuleById(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param rule TODO
     * @return TODO
     */
    @PostMapping("/api/salary/rules/save")
    ResponseResult saveRule(@RequestBody Map<String, Object> rule);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @param rule TODO
     * @return TODO
     */
    @PostMapping("/api/salary/rules/update/{id}")
    ResponseResult updateRule(@PathVariable Integer id, @RequestBody Map<String, Object> rule);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @DeleteMapping("/api/salary/rules/{id}")
    ResponseResult deleteRule(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/rules/status/{id}")
    ResponseResult updateRuleStatus(@PathVariable Integer id, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/rules/batch-delete")
    ResponseResult batchDeleteRules(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/salary/rules/ensure-meal")
    ResponseResult ensureMealRule();

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/salary/rules/ensure-tax")
    ResponseResult ensureTaxRule();

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/salary/rules/ensure-no-project-acceptance")
    ResponseResult ensureNoProjectAcceptanceRule();

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/salary/rules/no-project-acceptance/config")
    ResponseResult getNoProjectAcceptanceRuleConfig();

    /**
     * TODO 补充方法说明
/**
     * @param rule TODO
     * @return TODO
     */
    @PostMapping("/api/salary/rules/no-project-acceptance/save")
    ResponseResult saveNoProjectAcceptanceRuleConfig(@RequestBody Map<String, Object> rule);

    // ==================== 合同管理接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param queryParams TODO
     * @return TODO
     */
    @PostMapping("/api/contract/list")
    ResponseResult getContractList(@RequestBody Map<String, Object> queryParams);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/contract/{id}")
    ResponseResult getContractDetail(@PathVariable("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param contractData TODO
     * @return TODO
     */
    @PostMapping("/api/contract/create")
    ResponseResult createContract(@RequestBody Map<String, Object> contractData);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/contract/update/{id}")
    ResponseResult updateContract(@PathVariable("id") Integer id, @RequestBody Map<String, Object> contractData);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/contract/delete/{id}")
    ResponseResult deleteContract(@PathVariable("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/contract/generate-number")
    ResponseResult generateContractNumber();

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/contract/disable/{id}")
    ResponseResult disableContract(@PathVariable("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/contract/enable/{id}")
    ResponseResult enableContract(@PathVariable("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/contract/complete/{id}")
    ResponseResult completeContract(@PathVariable("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/contract/suspend/{id}")
    ResponseResult suspendContract(@PathVariable("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/contract/terminate/{id}")
    ResponseResult terminateContract(@PathVariable("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/contract/sign-info/{id}")
    ResponseResult getContractSignInfo(@PathVariable("id") Integer id, @RequestBody Map<String, Object> requestData);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/contract/sign/{id}")
    ResponseResult signContract(@PathVariable("id") Integer id, @RequestBody Map<String, Object> signData);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/payment-plan/list")
    ResponseResult getPaymentPlanList(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/contract/payment-plan/stats")
    ResponseResult getPaymentPlanStats();

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/payment-plan/unrecorded-paid")
    ResponseResult getUnrecordedPaidPlans(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/payment-plan/batch-generate-finance-records")
    ResponseResult batchGenerateFinanceRecords(@RequestBody Map<String, Object> params);

    // ==================== 合同数据分析接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/analytics/statistics")
    ResponseResult getContractAnalyticsStatistics(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/analytics/trend")
    ResponseResult getContractAnalyticsTrend(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/analytics/type-distribution")
    ResponseResult getContractAnalyticsTypeDistribution(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/analytics/amount-analysis")
    ResponseResult getContractAnalyticsAmountAnalysis(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/analytics/status-distribution")
    ResponseResult getContractAnalyticsStatusDistribution(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/analytics/prediction")
    ResponseResult getContractAnalyticsPrediction(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/analytics/risk-analysis")
    ResponseResult getContractAnalyticsRiskAnalysis(@RequestBody Map<String, Object> params);

    /**
     * 导出合同列表为 Excel 文件
/**
     * <p>返回二进制字节数组（xlsx 文件流），链路不加 ResponseResult 封装。</p>
     *
     * @param params 查询参数（与 list 接口保持一致）
     * @return 包含 xlsx 文件内容的 ResponseEntity
     */
    @PostMapping(value = "/api/contract/export", consumes = "application/json", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ResponseEntity<byte[]> exportContractList(@RequestBody Map<String, Object> params);

    // ==================== 合同文件管理接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param contractId TODO
     * @return TODO
     */
    @PostMapping(value = "/api/contract/file/upload/{contractId}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseResult uploadContractFile(
            @PathVariable Long contractId,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "description", required = false) String description);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/file/save")
    ResponseResult saveContractFile(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param contractId TODO
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/file/list/{contractId}")
    ResponseResult getContractFileList(
            @PathVariable Long contractId,
            @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param contractId TODO
     * @return TODO
     */
    @PostMapping("/api/contract/file/all/{contractId}")
    ResponseResult getAllContractFiles(@PathVariable Long contractId);

    /**
     * TODO 补充方法说明
/**
     * @param fileId TODO
     * @return TODO
     */
    @PostMapping("/api/contract/file/{fileId}")
    ResponseResult getContractFileDetail(@PathVariable Long fileId);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/api/contract/file/preview/{fileId}")
    ResponseEntity<Resource> previewContractFile(@PathVariable("fileId") Long fileId);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/api/contract/file/download/{fileId}")
    ResponseEntity<Resource> downloadContractFile(@PathVariable("fileId") Long fileId);

    /**
     * TODO 补充方法说明
/**
     * @param fileId TODO
     * @return TODO
     */
    @PostMapping("/api/contract/file/delete/{fileId}")
    ResponseResult deleteContractFile(@PathVariable Long fileId);

    /**
     * TODO 补充方法说明
/**
     * @param fileIds TODO
     * @return TODO
     */
    @PostMapping("/api/contract/file/batch-delete")
    ResponseResult batchDeleteContractFiles(@RequestBody List<Long> fileIds);

    /**
     * TODO 补充方法说明
/**
     * @param contractId TODO
     * @param originalFileName TODO
     * @return TODO
     */
    @GetMapping("/api/contract/file/versions/{contractId}")
    ResponseResult getContractFileVersions(
            @PathVariable Long contractId,
            @RequestParam String originalFileName);

    /**
     * TODO 补充方法说明
/**
     * @param fileId TODO
     * @return TODO
     */
    @PostMapping("/api/contract/file/preview-url/{fileId}")
    ResponseResult getContractFilePreviewUrl(@PathVariable Long fileId);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/file/check-exists")
    ResponseResult checkContractFileExists(@RequestBody Map<String, String> params);

    // ==================== 合同分类管理接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/api/contract/category/tree")
    ResponseResult getContractCategoryTree(@RequestBody(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/category/list")
    ResponseResult getContractCategoryList(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @GetMapping("/api/contract/category/{id}")
    ResponseResult getContractCategoryDetail(@PathVariable Long id);

    /**
     * TODO 补充方法说明
/**
     * @param categoryData TODO
     * @return TODO
     */
    @PostMapping("/api/contract/category/create")
    ResponseResult createContractCategory(@RequestBody Map<String, Object> categoryData);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @param categoryData TODO
     * @return TODO
     */
    @PostMapping("/api/contract/category/{id}")
    ResponseResult updateContractCategory(@PathVariable Long id, @RequestBody Map<String, Object> categoryData);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @DeleteMapping("/api/contract/category/{id}")
    ResponseResult deleteContractCategory(@PathVariable Long id);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/category/generate-code")
    ResponseResult generateCategoryCode(@RequestBody Map<String, Object> params);

    // ==================== 合同模板管理接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/template/templates")
    ResponseResult getContractTemplates(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/api/contract/template/{id}")
    ResponseResult getContractTemplateById(@PathVariable("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param templateData TODO
     * @return TODO
     */
    @PostMapping("/api/contract/template/create")
    ResponseResult createContractTemplate(@RequestBody Map<String, Object> templateData);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/contract/template/{id}")
    ResponseResult updateContractTemplate(@PathVariable("id") Integer id, @RequestBody Map<String, Object> templateData);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/api/contract/template/{id}")
    ResponseResult deleteContractTemplate(@PathVariable("id") Integer id);

    // ==================== 合同审批管理接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/approval/list")
    ResponseResult getContractApprovalList(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param contractId TODO
     * @return TODO
     */
    @PostMapping("/api/contract/approval/contract/{contractId}")
    ResponseResult getContractApprovalRecords(@PathVariable Long contractId);

    /**
     * TODO 补充方法说明
/**
     * @param approvalData TODO
     * @return TODO
     */
    @PostMapping("/api/contract/approval/submit")
    ResponseResult submitContractApproval(@RequestBody Map<String, Object> approvalData);

    /**
     * TODO 补充方法说明
/**
     * @param processData TODO
     * @return TODO
     */
    @PostMapping("/api/contract/approval/process")
    ResponseResult processContractApproval(@RequestBody Map<String, Object> processData);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @PostMapping("/api/contract/approval/withdraw/{id}")
    ResponseResult withdrawContractApproval(@PathVariable Long id);

    // ==================== 服务商打款审批接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param approvalData TODO
     * @return TODO
     */
    @PostMapping("/api/supplier/payment/approval/submit")
    ResponseResult submitSupplierPaymentApproval(@RequestBody Map<String, Object> approvalData);

    /**
     * TODO 补充方法说明
/**
     * @param processData TODO
     * @return TODO
     */
    @PostMapping("/api/supplier/payment/approval/process")
    ResponseResult processSupplierPaymentApproval(@RequestBody Map<String, Object> processData);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @PostMapping("/api/supplier/payment/approval/withdraw/{id}")
    ResponseResult withdrawSupplierPaymentApproval(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param spendingId TODO
     * @return TODO
     */
    @GetMapping("/api/supplier/payment/approval/spending/{spendingId}")
    ResponseResult getSupplierPaymentApprovalBySpendingId(@PathVariable Integer spendingId);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/supplier/payment/approval/pending")
    ResponseResult getSupplierPaymentPendingApprovals();

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/api/supplier/payment/approval/status-by-spending-ids")
    ResponseResult getSupplierPaymentApprovalStatusBySpendingIds(@RequestBody Map<String, Object> body);

    // ==================== 合同签署接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/sign/generate-link")
    ResponseResult generateSignLink(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param token TODO
     * @return TODO
     */
    @GetMapping("/api/contract/sign/info/{token}")
    ResponseResult getContractBySignToken(@PathVariable String token);

    /**
     * TODO 补充方法说明
/**
     * @param token TODO
     * @param signData TODO
     * @return TODO
     */
    @PostMapping("/api/contract/sign/confirm/{token}")
    ResponseResult confirmSign(@PathVariable String token, @RequestBody Map<String, Object> signData);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/contract/verify-phone")
    ResponseResult verifyContractPhone(@RequestBody Map<String, Object> params);

    // ==================== 联系人管理接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/contact/list/page")
    ResponseResult getContactListPage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @PostMapping("/contact/list/{id}")
    ResponseResult getContactDetail(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/contact/list/save")
    ResponseResult saveContact(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/contact/list/update")
    ResponseResult updateContact(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @PostMapping("/contact/list/delete/{id}")
    ResponseResult deleteContact(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/contact/list/batch-delete")
    ResponseResult batchDeleteContacts(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/contact/list/search")
    ResponseResult searchContacts(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/contact/list/import")
    ResponseResult importContacts(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/contact/list/export")
    ResponseResult exportContacts(@RequestBody Map<String, Object> params);

    // ==================== 待办事项接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/todo/list")
    ResponseResult getTodoList(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/todo/counts")
    ResponseResult getTodoCounts();

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/todo/updateStatus")
    ResponseResult updateTodoStatus(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/todo/create")
    ResponseResult createTodo(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/todo/update")
    ResponseResult updateTodo(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/todo/delete")
    ResponseResult deleteTodo(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/todo/daily/sync-info")
    ResponseResult todoDailySyncInfo();

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/todo/daily/sync-token/reset")
    ResponseResult todoDailySyncTokenReset();

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping(value = "/todo/daily/ics/{token}", produces = "text/calendar;charset=UTF-8")
    org.springframework.http.ResponseEntity<byte[]> todoDailyIcs(@PathVariable("token") String token);

    // ==================== 首页临时记事本接口 ====================

    /**
     * 获取当前登录用户的临时记事本便签列表（仅本人可见）
     *
     * @param params 查询参数，包含 page、pageSize
     * @return 便签分页列表（records/total/page/size）
     */
    @PostMapping("/quicknote/list")
    ResponseResult getQuickNoteList(@RequestBody Map<String, Object> params);

    /**
     * 创建临时便签（文本/图片/附件）
     *
     * @param params 创建参数，包含 content、images、attachments
     * @return 创建后的便签实体
     */
    @PostMapping("/quicknote/create")
    ResponseResult createQuickNote(@RequestBody Map<String, Object> params);

    /**
     * 删除临时便签（仅本人，逻辑删除）
     *
     * @param params 删除参数，包含 id
     * @return 删除结果
     */
    @PostMapping("/quicknote/delete")
    ResponseResult deleteQuickNote(@RequestBody Map<String, Object> params);

    // ==================== 费用报销接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/expense/category/list")
    ResponseResult getExpenseCategoryList();

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @PostMapping("/expense/category/{id}")
    ResponseResult getExpenseCategoryById(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param category TODO
     * @return TODO
     */
    @PostMapping("/expense/category/add")
    ResponseResult addExpenseCategory(@RequestBody Map<String, Object> category);

    /**
     * TODO 补充方法说明
/**
     * @param category TODO
     * @return TODO
     */
    @PostMapping("/expense/category/update")
    ResponseResult updateExpenseCategory(@RequestBody Map<String, Object> category);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @PostMapping("/expense/category/delete/{id}")
    ResponseResult deleteExpenseCategory(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/expense/application/list")
    ResponseResult getExpenseApplicationList();

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @PostMapping("/expense/application/{id}")
    ResponseResult getExpenseApplicationById(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param application TODO
     * @return TODO
     */
    @PostMapping("/expense/application/add")
    ResponseResult addExpenseApplication(@RequestBody Map<String, Object> application);

    /**
     * TODO 补充方法说明
/**
     * @param application TODO
     * @return TODO
     */
    @PostMapping("/expense/application/update")
    ResponseResult updateExpenseApplication(@RequestBody Map<String, Object> application);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @PostMapping("/expense/application/delete/{id}")
    ResponseResult deleteExpenseApplication(@PathVariable Integer id);

    // ==================== 工作台菜单接口====================

    /**
     * 获取工作台菜单数据
/**
     * 动态调用docs/sql/permission.sql 中 pid=260 的内容（与 OA 端 GET /workbench/menus 一致）
     */
    @GetMapping("/workbench/menus")
    ResponseResult getWorkbenchMenus();

    /**
     * 获取工作台子菜单数据
/**
     * 获取工作台主菜单(pid=260)下的所有子菜单（GET 与 OA 一致，便于无 body 与缓存）
     */
    @GetMapping("/workbench/submenus")
    ResponseResult getWorkbenchSubMenus();

    /**
     * 获取工作台统计数据
/**
     * 获取工作台相关的统计信息
     */
    @PostMapping("/workbench/stats")
    ResponseResult getWorkbenchStats();

    /**
     * 获取审批菜单数据
/**
     * 动态调取审批相关的菜单数据
     */
    @PostMapping("/workbench/approval/menus")
    ResponseResult getApprovalMenus();

    // ==================== 服务商管理接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/provider/list")
    ResponseResult getProviderList(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @GetMapping("/provider/{id}")
    ResponseResult getProviderById(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param providerData TODO
     * @return TODO
     */
    @PostMapping("/provider/create")
    ResponseResult createProvider(@RequestBody Map<String, Object> providerData);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @param providerData TODO
     * @return TODO
     */
    @PostMapping("/provider/{id}")
    ResponseResult updateProvider(@PathVariable Integer id, @RequestBody Map<String, Object> providerData);

    /**
     * TODO 补充方法说明
/**
     * @param id TODO
     * @return TODO
     */
    @DeleteMapping("/provider/{id}")
    ResponseResult deleteProvider(@PathVariable Integer id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/provider/generate-code")
    ResponseResult generateProviderCode();

    // ==================== 薪资模块完善 新接口 ====================

    // 薪资项目字典
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/item/page")
    ResponseResult salaryItemPage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param "category" TODO
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/api/salary/item/list")
    ResponseResult salaryItemList(@RequestParam(value = "category", required = false) String category);

    /**
     * TODO 补充方法说明
/**
     * @param item TODO
     * @return TODO
     */
    @PostMapping("/api/salary/item")
    ResponseResult salaryItemSave(@RequestBody Map<String, Object> item);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/api/salary/item/calc-params")
    ResponseResult salaryItemCalcParamsGet();

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/api/salary/item/calc-params")
    ResponseResult salaryItemCalcParamsSave(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/salary/item/{id:\\d+}")
    ResponseResult salaryItemUpdate(@PathVariable("id") Integer id, @RequestBody Map<String, Object> item);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/api/salary/item/{id:\\d+}")
    ResponseResult salaryItemDelete(@PathVariable("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/api/salary/item/{id:\\d+}")
    ResponseResult salaryItemDetail(@PathVariable("id") Integer id);

    // 员工薪资模板
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/template/page")
    ResponseResult salaryTemplatePage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/api/salary/template/by-employee/{employeeId}")
    ResponseResult salaryTemplateByEmployee(@PathVariable("employeeId") Integer employeeId);

    /**
     * TODO 补充方法说明
/**
     * @param entity TODO
     * @return TODO
     */
    @PostMapping("/api/salary/template")
    ResponseResult salaryTemplateSave(@RequestBody Map<String, Object> entity);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/salary/template/{id}")
    ResponseResult salaryTemplateUpdate(@PathVariable("id") Long id, @RequestBody Map<String, Object> entity);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/api/salary/template/{id}")
    ResponseResult salaryTemplateDelete(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @param payload TODO
     * @return TODO
     */
    @PostMapping("/api/salary/template/batch-save")
    ResponseResult salaryTemplateBatchSave(@RequestBody Map<String, Object> payload);

    // 月度工资条
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/payroll/page")
    ResponseResult salaryPayrollPage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/api/salary/payroll/{id}")
    ResponseResult salaryPayrollDetail(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/api/salary/payroll/{id}")
    ResponseResult salaryPayrollDelete(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @param "salaryMonth" TODO
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/api/salary/payroll/summary")
    ResponseResult salaryPayrollSummary(@RequestParam(value = "salaryMonth", required = false) String salaryMonth,
                                        @RequestParam(value = "orgId", required = false) String orgId);

    /**
     * TODO 补充方法说明
/**
     * @param "orgId" TODO
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/api/salary/payroll/months")
    ResponseResult salaryPayrollMonths(@RequestParam(value = "orgId", required = false) String orgId);

    // Excel 导入（表单上传，Feign 仅支持单文件 MultipartFile）
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping(value = "/api/salary/import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseResult salaryImportExcel(@RequestPart("file") MultipartFile file,
                                     @RequestParam(value = "salaryMonth", required = false) String salaryMonth,
                                     @RequestParam(value = "overwrite", defaultValue = "false") Boolean overwrite,
                                     @RequestParam(value = "orgId", required = false) String orgId);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/salary/import/status/{jobId}")
    ResponseResult salaryImportJobStatus(@PathVariable("jobId") Long jobId,
                                         @RequestParam(value = "orgId", required = false) String orgId);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/salary/import/confirm/{jobId}")
    ResponseResult salaryImportConfirmOverwrite(@PathVariable("jobId") Long jobId,
                                                @RequestParam(value = "orgId", required = false) String orgId);

    // ==================== 薪资透视（Excel 横表） ====================

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/api/salary/pivot/columns")
    ResponseResult salaryPivotColumns();

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/pivot/query")
    ResponseResult salaryPivotQuery(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping(value = "/api/salary/pivot/export", consumes = "application/json", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ResponseEntity<byte[]> salaryPivotExport(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/pivot/send-mail-all")
    ResponseResult salaryPivotSendMailAll(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/pivot/send-mail-each")
    ResponseResult salaryPivotSendMailEach(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/salary/pivot/refresh-row")
    ResponseResult salaryPivotRefreshRow(@RequestBody Map<String, Object> params);

    // ==================== 日报接口 ====================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/dailyreport/page")
    ResponseResult dailyReportPage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param entity TODO
     * @return TODO
     */
    @PostMapping("/api/dailyreport/save")
    ResponseResult dailyReportSave(@RequestBody Map<String, Object> entity);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/api/dailyreport/{id}")
    ResponseResult dailyReportDetail(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/api/dailyreport/delete/{id}")
    ResponseResult dailyReportDelete(@PathVariable("id") Long id);

    // ==================== 企微 SCRM ====================

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/wecom/department/tree")
    ResponseResult wecomDepartmentTree();

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/wecom/department/save")
    ResponseResult wecomDepartmentSave(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/wecom/department/delete")
    ResponseResult wecomDepartmentDelete(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/wecom/department/sync")
    ResponseResult wecomDepartmentSync();

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/wecom/scrm/{module}/page")
    ResponseResult wecomScrmPage(@PathVariable("module") String module, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/wecom/scrm/{module}/save")
    ResponseResult wecomScrmSave(@PathVariable("module") String module, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/wecom/scrm/{module}/delete")
    ResponseResult wecomScrmDelete(@PathVariable("module") String module, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/wecom/scrm/{module}/sync")
    ResponseResult wecomScrmSync(@PathVariable("module") String module, @RequestBody(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/wecom/scrm/analytics/{type}")
    ResponseResult wecomScrmAnalytics(@PathVariable("type") String type, @RequestBody(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/wecom/scrm/task/{id}/execute")
    ResponseResult wecomScrmExecuteTask(@PathVariable("id") Long id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/wecom/scrm/{module}/import")
    ResponseResult wecomScrmImport(@PathVariable("module") String module, @RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/wecom/scrm/archive/staff")
    ResponseResult wecomArchiveStaff(@RequestBody(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/wecom/scrm/archive/contacts")
    ResponseResult wecomArchiveContacts(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/wecom/scrm/archive/messages")
    ResponseResult wecomArchiveMessages(@RequestBody Map<String, Object> params);
}
