package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * OA服务Feign客户端降级处理
/**
 * 只做降级处理，不包含业务逻辑
 * @author Amos
 */
@Component
public class OaServiceFeignFallback implements OaServiceFeign {

    @Override
    public ResponseResult getDashboardSummary() {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getTodoList(Integer page, Integer limit, String keyword) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getNoticeList(Integer page, Integer limit, String type) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getEmployeeList(Map<String, Object> queryParams) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult syncWecomEmployees() {
        return ResponseResult.fail("员工同步服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult syncPlatformEmployees() {
        return ResponseResult.fail("员工同步服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult syncPlatformEmployeesAlias() {
        return ResponseResult.fail("员工同步服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult syncWecomEmployeesAlias() {
        return ResponseResult.fail("员工同步服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createEmployee(Map<String, Object> employeeData) {
        return ResponseResult.fail("员工管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateEmployee(Integer id, Map<String, Object> employeeData) {
        return ResponseResult.fail("员工管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteEmployee(Integer id) {
        return ResponseResult.fail("员工管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getEmployeeDetail(Long id) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getEmployeeOptions(String fields, String status, String departmentId, Boolean includeResigned, Integer limit, String orgId) {
        return ResponseResult.fail("员工选项服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getEmployeeSalaryByEmployee(Integer employeeId, String orgId) {
        return ResponseResult.fail("员工薪资档案服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult upsertEmployeeSalary(Map<String, Object> salaryData) {
        return ResponseResult.fail("员工薪资档案服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getAttendanceRecord(Integer page, Integer limit, Long employeeId, String startDate, String endDate) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getAttendanceRecordPage(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getAttendanceStatisticsSummary(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getAttendanceStatisticsDepartment(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getAttendanceStatisticsIndividual(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getAttendanceStatisticsTrend(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getAttendanceListPage(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getAttendanceCheckinDetail(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult makeupAttendance(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult setAttendanceWhitelist(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult syncAttendanceLists(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult syncAttendanceListsWecom(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult syncAttendanceRoot(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult syncWecomAttendanceRoot(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getOaBackgroundJobStatus(Map<String, Object> body) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getLeavePage(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult applyLeave(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult approveLeave(Long id, Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult cancelLeave(Long id) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getLeaveStatistics(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getOvertimePage(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult applyOvertime(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult approveOvertime(Long id, Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult cancelOvertime(Long id) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getOvertimeStatistics(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getTravelPage(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult applyTravel(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult approveTravel(Long id, Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult cancelTravel(Long id) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult completeTravel(Long id, Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult checkIn(String location) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getAttendancePlatformConfig() {
        return ResponseResult.fail("考勤配置服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult saveAttendancePlatformConfig(Map<String, Object> params) {
        return ResponseResult.fail("考勤配置服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSalaryList(Integer page, Integer limit, Long employeeId, String yearMonth) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getDocumentList(Integer page, Integer limit, String keyword, String category) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getMeetingRoomList(Integer page, Integer limit, String name) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult reserveMeetingRoom(Long roomId, String startTime, String endTime, String subject) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getAssetListV2(Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createAsset(Map<String, Object> assetData) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateAsset(Map<String, Object> assetData) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteAsset(Long id) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getAssetDetail(Long id) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult allocateAsset(Long id, Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult recycleAsset(Long id, Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult maintenanceAsset(Long id, Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult scrapAsset(Long id, Map<String, Object> params) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getAssetStatisticsV2() {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getVehicleList(Integer page, Integer limit, String keyword, String status) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult applyVehicle(Long vehicleId, String startTime, String endTime, String purpose) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getReportStatistics(String type, String startDate, String endDate) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    /** ==================== HR管理扩展接口降级处理 ==================== */

    @Override
    public ResponseResult getDepartmentPage(Integer page, Integer size, String departmentName, String departmentCode, Long parentId) {
        return ResponseResult.fail("部门管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getDepartmentTree() {
        return ResponseResult.fail("部门管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult syncDepartmentsFromWecom() {
        return ResponseResult.fail("部门管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addDepartment(Map<String, Object> department) {
        return ResponseResult.fail("部门管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateDepartment(Long id, Map<String, Object> department) {
        return ResponseResult.fail("部门管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteDepartment(Long id) {
        return ResponseResult.fail("部门管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getDepartmentById(Long id) {
        return ResponseResult.fail("部门管理服务暂时不可用，请稍后重试");
    }

           @Override
           public ResponseResult getPositionPage(Integer page, Integer size, String name) {
               return ResponseResult.fail("职位管理服务暂时不可用，请稍后重试");
           }
       
           @Override
           public ResponseResult addPosition(Map<String, Object> position) {
               return ResponseResult.fail("职位管理服务暂时不可用，请稍后重试");
           }
       
           @Override
           public ResponseResult updatePosition(Integer id, Map<String, Object> position) {
               return ResponseResult.fail("职位管理服务暂时不可用，请稍后重试");
           }
       
           @Override
           public ResponseResult deletePosition(Integer id) {
               return ResponseResult.fail("职位管理服务暂时不可用，请稍后重试");
           }
       
           @Override
           public ResponseResult getPositionById(Integer id) {
               return ResponseResult.fail("职位管理服务暂时不可用，请稍后重试");
           }

           @Override
           public ResponseResult syncPositionsFromWecom() {
               return ResponseResult.fail("职位管理服务暂时不可用，请稍后重试");
           }

    @Override
    public ResponseResult getPerformancePage(Map<String, Object> params) {
        return ResponseResult.fail("绩效管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addPerformance(Map<String, Object> performance) {
        return ResponseResult.fail("绩效管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult startPerformanceEvaluation(Long id) {
        return ResponseResult.fail("绩效管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getPerformanceById(Long id) {
        return ResponseResult.fail("绩效管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deletePerformance(Long id) {
        return ResponseResult.fail("绩效管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRecruitPositionPage(Integer page, Integer size, String keyword, String status, String department) {
        return ResponseResult.fail("招聘管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addRecruitPosition(Map<String, Object> position) {
        return ResponseResult.fail("招聘管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateRecruitPosition(Long id, Map<String, Object> position) {
        return ResponseResult.fail("招聘管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteRecruitPosition(Long id) {
        return ResponseResult.fail("招聘管理服务暂时不可用，请稍后重试");
    }

    /** ==================== 薪资管理接口降级处理 ==================== */

    @Override
    public ResponseResult getSalarySettingPage(Map<String, Object> params) {
        return ResponseResult.fail("薪资设置服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSalarySettingById(Integer id) {
        return ResponseResult.fail("薪资设置服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult saveSalarySetting(Map<String, Object> salaryData) {
        return ResponseResult.fail("薪资设置服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateSalarySetting(Integer id, Map<String, Object> salaryData) {
        return ResponseResult.fail("薪资设置服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteSalarySetting(Integer id) {
        return ResponseResult.fail("薪资设置服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSalarySettingByEmployeeId(Integer employeeId) {
        return ResponseResult.fail("薪资设置服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult batchSaveSalarySetting(java.util.List<Map<String, Object>> salaryList) {
        return ResponseResult.fail("薪资设置服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSalarySummary(Map<String, Object> params) {
        return ResponseResult.fail("薪资统计服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getDepartmentStatistics(Map<String, Object> params) {
        return ResponseResult.fail("薪资统计服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getIndividualStatistics(Map<String, Object> params) {
        return ResponseResult.fail("薪资统计服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSalaryComparison(Map<String, Object> params) {
        return ResponseResult.fail("薪资统计服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult exportSalaryStatistics(Map<String, Object> params) {
        return ResponseResult.fail("薪资统计服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getPayslipPage(Map<String, Object> params) {
        return ResponseResult.fail("薪资发放服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getPayslipDetail(Integer id) {
        return ResponseResult.fail("薪资发放服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult generatePayslip(Map<String, Object> params) {
        return ResponseResult.fail("薪资发放服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult batchSendPayslip(Map<String, Object> params) {
        return ResponseResult.fail("薪资发放服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult sendPayslip(Integer id) {
        return ResponseResult.fail("薪资发放服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult resendPayslip(Integer id) {
        return ResponseResult.fail("薪资发放服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult downloadPayslip(Integer id) {
        return ResponseResult.fail("薪资发放服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult exportPayslip(Map<String, Object> params) {
        return ResponseResult.fail("薪资发放服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSalaryPayslipManagePage(Map<String, Object> params) {
        return ResponseResult.fail("工资条管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSalaryPayslipManageDetail(Long id) {
        return ResponseResult.fail("工资条管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult recordSalaryPayslipPayment(Long id, Map<String, Object> params) {
        return ResponseResult.fail("薪资发放服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSalaryPayslipPaymentRecords(Long id, Map<String, Object> params) {
        return ResponseResult.fail("薪资发放服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queueSalaryPayslipManage(Long id, Map<String, Object> params) {
        return ResponseResult.fail("薪资发放服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult sendSalaryPayslipManage(Long id, Map<String, Object> params) {
        return ResponseResult.fail("工资条管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult resendSalaryPayslipManage(Long id, Map<String, Object> params) {
        return ResponseResult.fail("工资条管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult batchSendSalaryPayslipManage(Map<String, Object> params) {
        return ResponseResult.fail("工资条管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSalaryStructurePage(Map<String, Object> params) {
        return ResponseResult.fail("薪资结构服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSalaryStructureById(Integer id) {
        return ResponseResult.fail("薪资结构服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult saveSalaryStructure(Map<String, Object> structureData) {
        return ResponseResult.fail("薪资结构服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateSalaryStructure(Integer id, Map<String, Object> structureData) {
        return ResponseResult.fail("薪资结构服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteSalaryStructure(Integer id) {
        return ResponseResult.fail("薪资结构服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSalaryStructureTypes() {
        return ResponseResult.fail("薪资结构服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSalaryStructureList() {
        return ResponseResult.fail("薪资结构服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSalaryCalculatePage(Map<String, Object> params) {
        return ResponseResult.fail("薪资计算服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSalaryCalculateDetail(Integer id) {
        return ResponseResult.fail("薪资计算服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult salaryCalculateEmployeesPage(Map<String, Object> params) {
        return ResponseResult.fail("薪资计算服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult listSalaryCalculateRules() {
        return ResponseResult.fail("薪资计算服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult previewSalaryCalculation(Map<String, Object> params) {
        return ResponseResult.fail("薪资计算服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult confirmSalaryCalculation(Map<String, Object> params) {
        return ResponseResult.fail("薪资计算服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult batchCalculate(Map<String, Object> params) {
        return ResponseResult.fail("薪资计算服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult recalculate(Integer id) {
        return ResponseResult.fail("薪资计算服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult validateCalculation(Map<String, Object> params) {
        return ResponseResult.fail("薪资计算服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getCalculationHistory(Map<String, Object> params) {
        return ResponseResult.fail("薪资计算服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult exportCalculation(Map<String, Object> params) {
        return ResponseResult.fail("薪资计算服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult syncEmployeeSalaryFromPayroll(Map<String, Object> params) {
        return ResponseResult.fail("薪资计算服务暂时不可用，请稍后重试");
    }

    /** ==================== 日报扣款规则降级处理 ==================== */

    @Override
    public ResponseResult getReportDeductionPage(Map<String, Object> params) {
        return ResponseResult.fail("日报扣款服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getReportDeductionDetail(Long id) {
        return ResponseResult.fail("日报扣款服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult computeReportDeduction(Map<String, Object> params) {
        return ResponseResult.fail("日报扣款服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult lockReportDeduction(Long id) {
        return ResponseResult.fail("日报扣款服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateReportDeductionAppeal(Long id, Map<String, Object> params) {
        return ResponseResult.fail("日报扣款服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getReportSubmitLogList(Map<String, Object> params) {
        return ResponseResult.fail("日报扣款服务暂时不可用，请稍后重试");
    }

    /** ==================== 节假日配置降级处理 ==================== */

    @Override
    public ResponseResult getHolidayConfigPage(Map<String, Object> params) {
        return ResponseResult.fail("节假日服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getHolidayConfigByMonth(String month) {
        return ResponseResult.fail("节假日服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult saveHolidayConfig(Map<String, Object> entity) {
        return ResponseResult.fail("节假日服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteHolidayConfig(Long id) {
        return ResponseResult.fail("节假日服务暂时不可用，请稍后重试");
    }

    /** ==================== 请假申请降级处理 ==================== */

    @Override
    public ResponseResult getLeaveApplyPage(Map<String, Object> params) {
        return ResponseResult.fail("请假服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult saveLeaveApply(Map<String, Object> entity) {
        return ResponseResult.fail("请假服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult approveLeaveApply(Long id, Map<String, Object> params) {
        return ResponseResult.fail("请假服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteLeaveApply(Long id) {
        return ResponseResult.fail("请假服务暂时不可用，请稍后重试");
    }

    /** ==================== 薪资规则管理接口降级处理 ==================== */

    @Override
    public ResponseResult getRulesPage(Map<String, Object> params) {
        return ResponseResult.fail("薪资规则服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRuleById(Integer id) {
        return ResponseResult.fail("薪资规则服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult saveRule(Map<String, Object> rule) {
        return ResponseResult.fail("薪资规则服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateRule(Integer id, Map<String, Object> rule) {
        return ResponseResult.fail("薪资规则服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteRule(Integer id) {
        return ResponseResult.fail("薪资规则服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateRuleStatus(Integer id, Map<String, Object> params) {
        return ResponseResult.fail("薪资规则服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult batchDeleteRules(Map<String, Object> params) {
        return ResponseResult.fail("薪资规则服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult ensureMealRule() {
        return ResponseResult.fail("薪资规则服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult ensureTaxRule() {
        return ResponseResult.fail("薪资规则服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult ensureNoProjectAcceptanceRule() {
        return ResponseResult.fail("薪资规则服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getNoProjectAcceptanceRuleConfig() {
        return ResponseResult.error("薪资阶梯规则服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult saveNoProjectAcceptanceRuleConfig(Map<String, Object> rule) {
        return ResponseResult.error("薪资阶梯规则服务暂时不可用，请稍后重试");
    }

    /** ==================== 合同管理接口降级处理 ==================== */

    @Override
    public ResponseResult getContractList(Map<String, Object> queryParams) {
        return ResponseResult.fail("合同管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractDetail(Integer id) {
        return ResponseResult.fail("合同管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createContract(Map<String, Object> contractData) {
        return ResponseResult.fail("合同管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateContract(Integer id, Map<String, Object> contractData) {
        return ResponseResult.fail("合同管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteContract(Integer id) {
        return ResponseResult.fail("合同管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult generateContractNumber() {
        return ResponseResult.fail("合同编号生成服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult disableContract(Integer id) {
        return ResponseResult.fail("合同作废服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult enableContract(Integer id) {
        return ResponseResult.fail("合同启用服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult completeContract(Integer id) {
        return ResponseResult.fail("合同完成服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult suspendContract(Integer id) {
        return ResponseResult.fail("合同中止服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult terminateContract(Integer id) {
        return ResponseResult.fail("合同终止服务暂时不可用，请稍后重试");
    }

    /** ==================== 合同文件管理接口降级处理 ==================== */

    @Override
    public ResponseResult uploadContractFile(Long contractId, org.springframework.web.multipart.MultipartFile file, String description) {
        return ResponseResult.fail("合同文件管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult saveContractFile(Map<String, Object> params) {
        return ResponseResult.fail("合同文件管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractFileList(Long contractId, Map<String, Object> params) {
        return ResponseResult.fail("合同文件管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getAllContractFiles(Long contractId) {
        return ResponseResult.fail("合同文件管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractFileDetail(Long fileId) {
        return ResponseResult.fail("合同文件管理服务暂时不可用，请稍后重试");
    }

    @Override
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> previewContractFile(Long fileId) {
        return null;
    }

    @Override
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadContractFile(Long fileId) {
        return null;
    }

    @Override
    public ResponseResult deleteContractFile(Long fileId) {
        return ResponseResult.fail("合同文件管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult batchDeleteContractFiles(java.util.List<Long> fileIds) {
        return ResponseResult.fail("合同文件管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractFileVersions(Long contractId, String originalFileName) {
        return ResponseResult.fail("合同文件管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractFilePreviewUrl(Long fileId) {
        return ResponseResult.fail("合同文件管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult checkContractFileExists(Map<String, String> params) {
        return ResponseResult.fail("合同文件管理服务暂时不可用，请稍后重试");
    }

    /** ==================== 合同分类管理接口降级处理 ==================== */

    @Override
    public ResponseResult getContractCategoryTree(Map<String, Object> params) {
        return ResponseResult.fail("合同分类管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractCategoryList(Map<String, Object> params) {
        return ResponseResult.fail("合同分类管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractCategoryDetail(Long id) {
        return ResponseResult.fail("合同分类管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createContractCategory(Map<String, Object> categoryData) {
        return ResponseResult.fail("合同分类管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateContractCategory(Long id, Map<String, Object> categoryData) {
        return ResponseResult.fail("合同分类管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteContractCategory(Long id) {
        return ResponseResult.fail("合同分类管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult generateCategoryCode(@RequestBody Map<String, Object> params) {
        return ResponseResult.fail("合同分类管理服务暂时不可用，请稍后重试");
    }

    /** ==================== 合同模板管理接口降级处理 ==================== */

    @Override
    public ResponseResult getContractTemplates(Map<String, Object> params) {
        return ResponseResult.fail("合同模板服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractTemplateById(Integer id) {
        return ResponseResult.fail("合同模板服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createContractTemplate(Map<String, Object> templateData) {
        return ResponseResult.fail("合同模板服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateContractTemplate(Integer id, Map<String, Object> templateData) {
        return ResponseResult.fail("合同模板服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteContractTemplate(Integer id) {
        return ResponseResult.fail("合同模板服务暂时不可用，请稍后重试");
    }

    /** ==================== 合同审批管理接口降级处理 ==================== */

    @Override
    public ResponseResult getContractApprovalList(Map<String, Object> params) {
        return ResponseResult.fail("合同审批管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractApprovalRecords(Long contractId) {
        return ResponseResult.fail("合同审批管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult submitContractApproval(Map<String, Object> approvalData) {
        return ResponseResult.fail("合同审批管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult processContractApproval(Map<String, Object> processData) {
        return ResponseResult.fail("合同审批管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult withdrawContractApproval(Long id) {
        return ResponseResult.fail("合同审批管理服务暂时不可用，请稍后重试");
    }

    /** ==================== 合同签署接口降级处理 ==================== */

    @Override
    public ResponseResult generateSignLink(Map<String, Object> params) {
        return ResponseResult.fail("合同签署服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractBySignToken(String token) {
        return ResponseResult.fail("合同签署服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult confirmSign(String token, Map<String, Object> signData) {
        return ResponseResult.fail("合同签署服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult verifyContractPhone(Map<String, Object> params) {
        return ResponseResult.fail("合同签署服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractSignInfo(Integer id, Map<String, Object> requestData) {
        return ResponseResult.fail("合同签署服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult signContract(Integer id, Map<String, Object> signData) {
        return ResponseResult.fail("合同签署服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getPaymentPlanList(Map<String, Object> params) {
        return ResponseResult.fail("付款计划服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getPaymentPlanStats() {
        return ResponseResult.fail("付款计划统计服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getUnrecordedPaidPlans(Map<String, Object> params) {
        return ResponseResult.fail("已打未记录付款计划服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult batchGenerateFinanceRecords(Map<String, Object> params) {
        return ResponseResult.fail("批量生成财务记录服务暂时不可用，请稍后重试");
    }

    /** ==================== 合同数据分析接口降级处理 ==================== */

    @Override
    public ResponseResult getContractAnalyticsStatistics(Map<String, Object> params) {
        return ResponseResult.fail("合同统计服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractAnalyticsTrend(Map<String, Object> params) {
        return ResponseResult.fail("合同趋势分析服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractAnalyticsTypeDistribution(Map<String, Object> params) {
        return ResponseResult.fail("合同类型分布服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractAnalyticsAmountAnalysis(Map<String, Object> params) {
        return ResponseResult.fail("合同金额分析服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractAnalyticsStatusDistribution(Map<String, Object> params) {
        return ResponseResult.fail("合同状态分布服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractAnalyticsPrediction(Map<String, Object> params) {
        return ResponseResult.fail("合同预测分析服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractAnalyticsRiskAnalysis(Map<String, Object> params) {
        return ResponseResult.fail("合同风险分析服务暂时不可用，请稍后重试");
    }

    @Override
    public org.springframework.http.ResponseEntity<byte[]> exportContractList(Map<String, Object> params) {
        // 降级处理：返回空内容和 503 状态码
        return org.springframework.http.ResponseEntity
                .status(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
                .build();
    }

    /** ==================== 联系人管理接口降级处理 ==================== */

    @Override
    public ResponseResult getContactListPage(Map<String, Object> params) {
        return ResponseResult.fail("联系人管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContactDetail(Integer id) {
        return ResponseResult.fail("联系人管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult saveContact(Map<String, Object> params) {
        return ResponseResult.fail("联系人管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateContact(Map<String, Object> params) {
        return ResponseResult.fail("联系人管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteContact(Integer id) {
        return ResponseResult.fail("联系人管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult batchDeleteContacts(Map<String, Object> params) {
        return ResponseResult.fail("联系人管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult searchContacts(Map<String, Object> params) {
        return ResponseResult.fail("联系人管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult importContacts(Map<String, Object> params) {
        return ResponseResult.fail("联系人管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult exportContacts(@RequestBody Map<String, Object> params) {
        return ResponseResult.fail("联系人管理服务暂时不可用，请稍后重试");
    }

    /** ==================== 待办事项接口降级处理 ==================== */

    @Override
    public ResponseResult getTodoList(Map<String, Object> params) {
        return ResponseResult.fail("待办事项服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getTodoCounts() {
        return ResponseResult.fail("待办事项服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateTodoStatus(Map<String, Object> params) {
        return ResponseResult.fail("待办事项服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createTodo(Map<String, Object> params) {
        return ResponseResult.fail("待办事项服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateTodo(Map<String, Object> params) {
        return ResponseResult.fail("待办事项服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteTodo(Map<String, Object> params) {
        return ResponseResult.fail("待办事项服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult todoDailySyncInfo() {
        return ResponseResult.fail("待办事项服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult todoDailySyncTokenReset() {
        return ResponseResult.fail("待办事项服务暂时不可用，请稍后重试");
    }

    @Override
    public org.springframework.http.ResponseEntity<byte[]> todoDailyIcs(String token) {
        return org.springframework.http.ResponseEntity.status(503).body("服务不可用".getBytes());
    }

    /** ==================== 首页临时记事本接口降级处理 ==================== */

    @Override
    public ResponseResult getQuickNoteList(Map<String, Object> params) {
        return ResponseResult.fail("临时记事本服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createQuickNote(Map<String, Object> params) {
        return ResponseResult.fail("临时记事本服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteQuickNote(Map<String, Object> params) {
        return ResponseResult.fail("临时记事本服务暂时不可用，请稍后重试");
    }

    /** ==================== 费用报销接口降级处理 ==================== */

    @Override
    public ResponseResult getExpenseCategoryList() {
        return ResponseResult.fail("费用分类服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getExpenseCategoryById(Integer id) {
        return ResponseResult.fail("费用分类服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addExpenseCategory(Map<String, Object> category) {
        return ResponseResult.fail("费用分类服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateExpenseCategory(Map<String, Object> category) {
        return ResponseResult.fail("费用分类服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteExpenseCategory(Integer id) {
        return ResponseResult.fail("费用分类服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getExpenseApplicationList() {
        return ResponseResult.fail("费用报销申请服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getExpenseApplicationById(Integer id) {
        return ResponseResult.fail("费用报销申请服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addExpenseApplication(Map<String, Object> application) {
        return ResponseResult.fail("费用报销申请服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateExpenseApplication(Map<String, Object> application) {
        return ResponseResult.fail("费用报销申请服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteExpenseApplication(Integer id) {
        return ResponseResult.fail("费用报销申请服务暂时不可用，请稍后重试");
    }

    /** ==================== 工作台菜单接口降级处理 ==================== */

    @Override
    public ResponseResult getWorkbenchMenus() {
        return ResponseResult.fail("工作台菜单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getWorkbenchSubMenus() {
        return ResponseResult.fail("工作台子菜单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getWorkbenchStats() {
        return ResponseResult.fail("工作台统计服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getApprovalMenus() {
        return ResponseResult.fail("审批菜单服务暂时不可用，请稍后重试");
    }

    /** ==================== 服务商管理接口降级处理 ==================== */

    @Override
    public ResponseResult getProviderList(Map<String, Object> params) {
        return ResponseResult.fail("服务商管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getProviderById(Integer id) {
        return ResponseResult.fail("服务商管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createProvider(Map<String, Object> providerData) {
        return ResponseResult.fail("服务商管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateProvider(Integer id, Map<String, Object> providerData) {
        return ResponseResult.fail("服务商管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteProvider(Integer id) {
        return ResponseResult.fail("服务商管理服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult generateProviderCode() {
        return ResponseResult.fail("服务商管理服务暂时不可用，请稍后重试");
    }

    /** ==================== 薪资模块完善 降级 ==================== */
    @Override public ResponseResult salaryItemPage(Map<String, Object> params) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryItemList(String category) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryItemSave(Map<String, Object> item) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryItemUpdate(Integer id, Map<String, Object> item) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryItemDelete(Integer id) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryItemDetail(Integer id) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryItemCalcParamsGet() { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryItemCalcParamsSave(Map<String, Object> body) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryTemplatePage(Map<String, Object> params) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryTemplateByEmployee(Integer employeeId) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryTemplateSave(Map<String, Object> entity) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryTemplateUpdate(Long id, Map<String, Object> entity) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryTemplateDelete(Long id) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryTemplateBatchSave(Map<String, Object> payload) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryPayrollPage(Map<String, Object> params) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryPayrollDetail(Long id) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryPayrollDelete(Long id) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryPayrollSummary(String salaryMonth, String orgId) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryPayrollMonths(String orgId) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryImportExcel(MultipartFile file, String salaryMonth, Boolean overwrite, String orgId) { return ResponseResult.fail("薪资服务不可用，导入失败"); }
    @Override public ResponseResult salaryImportJobStatus(Long jobId, String orgId) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryImportConfirmOverwrite(Long jobId, String orgId) { return ResponseResult.fail("薪资服务不可用"); }

    /** ==================== 薪资透视（Excel 横表）降级 ==================== */
    @Override public ResponseResult salaryPivotColumns() { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryPivotQuery(Map<String, Object> params) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryPivotRefreshRow(Map<String, Object> params) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public org.springframework.http.ResponseEntity<byte[]> salaryPivotExport(Map<String, Object> params) {
        return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE).build();
    }
    @Override public ResponseResult salaryPivotSendMailAll(Map<String, Object> params) { return ResponseResult.fail("薪资服务不可用"); }
    @Override public ResponseResult salaryPivotSendMailEach(Map<String, Object> params) { return ResponseResult.fail("薪资服务不可用"); }

    /** ==================== 日报接口降级处理 ==================== */
    @Override public ResponseResult dailyReportPage(Map<String, Object> params) { return ResponseResult.fail("日报服务暂时不可用"); }
    @Override public ResponseResult dailyReportSave(Map<String, Object> entity) { return ResponseResult.fail("日报服务暂时不可用"); }
    @Override public ResponseResult dailyReportDetail(Long id) { return ResponseResult.fail("日报服务暂时不可用"); }
    @Override public ResponseResult dailyReportDelete(Long id) { return ResponseResult.fail("日报服务暂时不可用"); }

    /** ==================== 企微 SCRM 降级 ==================== */
    @Override public ResponseResult wecomDepartmentTree() { return ResponseResult.fail("企微部门服务暂时不可用"); }
    @Override public ResponseResult wecomDepartmentSave(Map<String, Object> body) { return ResponseResult.fail("企微部门服务暂时不可用"); }
    @Override public ResponseResult wecomDepartmentDelete(Map<String, Object> body) { return ResponseResult.fail("企微部门服务暂时不可用"); }
    @Override public ResponseResult wecomDepartmentSync() { return ResponseResult.fail("企微部门服务暂时不可用"); }
    @Override public ResponseResult wecomScrmPage(String module, Map<String, Object> params) { return ResponseResult.fail("企微SCRM服务暂时不可用"); }
    @Override public ResponseResult wecomScrmSave(String module, Map<String, Object> body) { return ResponseResult.fail("企微SCRM服务暂时不可用"); }
    @Override public ResponseResult wecomScrmDelete(String module, Map<String, Object> body) { return ResponseResult.fail("企微SCRM服务暂时不可用"); }
    @Override public ResponseResult wecomScrmSync(String module, Map<String, Object> params) { return ResponseResult.fail("企微SCRM服务暂时不可用"); }
    @Override public ResponseResult wecomScrmAnalytics(String type, Map<String, Object> params) { return ResponseResult.fail("企微SCRM服务暂时不可用"); }
    @Override public ResponseResult wecomScrmExecuteTask(Long id) { return ResponseResult.fail("企微SCRM服务暂时不可用"); }
    @Override public ResponseResult wecomScrmImport(String module, Map<String, Object> body) { return ResponseResult.fail("企微SCRM服务暂时不可用"); }
    @Override public ResponseResult wecomArchiveStaff(Map<String, Object> params) { return ResponseResult.fail("企微SCRM服务暂时不可用"); }
    @Override public ResponseResult wecomArchiveContacts(Map<String, Object> params) { return ResponseResult.fail("企微SCRM服务暂时不可用"); }
    @Override public ResponseResult wecomArchiveMessages(Map<String, Object> params) { return ResponseResult.fail("企微SCRM服务暂时不可用"); }

    /** ==================== 供应商付款审批降级 ==================== */
    @Override
    public ResponseResult submitSupplierPaymentApproval(Map<String, Object> approvalData) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult processSupplierPaymentApproval(Map<String, Object> processData) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult withdrawSupplierPaymentApproval(Integer id) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSupplierPaymentApprovalBySpendingId(Integer spendingId) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSupplierPaymentPendingApprovals() {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSupplierPaymentApprovalStatusBySpendingIds(Map<String, Object> body) {
        return ResponseResult.fail("OA服务暂时不可用，请稍后重试");
    }
}
