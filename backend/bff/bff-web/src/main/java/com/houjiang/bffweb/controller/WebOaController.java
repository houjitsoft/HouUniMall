package com.houjiang.bffweb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.bffweb.util.ResponseResult;
import com.houjiang.bffweb.feign.OaServiceFeign;
import feign.FeignException;
import com.houjiang.bffweb.feign.OssServiceFeign;
import com.houjiang.bffweb.feign.WecomServiceFeign;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OA系统Web端BFF适配器
/**
 * 只做请求转发，不包含业务逻辑
 * @author Amos
 */
@Slf4j
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/oa")
public class WebOaController {

    @Autowired
    private OaServiceFeign oaServiceFeign;

    @Autowired
    private WecomServiceFeign wecomServiceFeign;

    @Autowired
    private OssServiceFeign ossServiceFeign;

    @Autowired
    private com.houjiang.bffweb.support.PluginServiceResolver pluginServiceResolver;

    private static final ObjectMapper JSON = new ObjectMapper();

    private String resolveOaUrl(String path) {
        String normalized = path == null || path.isBlank() ? "/" : path;
        if (!normalized.startsWith("/")) normalized = "/" + normalized;
        if (normalized.startsWith("/api/oa")) normalized = normalized.substring("/api/oa".length());
        else if (normalized.startsWith("/oa")) normalized = normalized.substring("/oa".length());
        if (normalized.isBlank()) normalized = "/";
        return pluginServiceResolver.resolve("oa").url() + "/plugin/oa" + normalized;
    }

    /**
     * 将 OA 服务的文件流直接透传给客户端，避免 Feign 无法传递二进制流的问题
/**
     *
     * @param oaPath        OA 服务侧完整路径
     * @param bffRequest    BFF 收到的原始请求（用于转发 Authorization）
     * @param bffResponse   BFF 的输出响应
     */
    private void proxyFileStream(String oaPath, HttpServletRequest bffRequest, HttpServletResponse bffResponse) {
        HttpURLConnection conn = null;
        try {
            String targetUrl = resolveOaUrl(oaPath);
            conn = (HttpURLConnection) new URL(targetUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(60_000);
            // 转发 Authorization（请求头优先，其次 URL 参数 token，供 img/iframe 预览使用）
            String auth = bffRequest.getHeader("Authorization");
            if (auth == null || auth.isBlank()) {
                String queryToken = bffRequest.getParameter("token");
                if (queryToken != null && !queryToken.isBlank()) {
                    auth = queryToken.startsWith("Bearer ") ? queryToken : ("Bearer " + queryToken);
                }
            }
            if (auth != null && !auth.isBlank()) {
                conn.setRequestProperty("Authorization", auth);
            }
            String orgIdHeader = bffRequest.getHeader("X-Org-Id");
            if (orgIdHeader != null && !orgIdHeader.isBlank()) conn.setRequestProperty("X-Org-Id", orgIdHeader);
            conn.connect();
            int status = conn.getResponseCode();
            if (status != 200) {
                bffResponse.setStatus(status);
                bffResponse.setContentType("application/json;charset=UTF-8");
                bffResponse.getWriter().write("{\"code\":" + status + ",\"message\":\"文件服务返回: " + status + "\"}");
                return;
            }
            // 转发响应头
            String contentType = conn.getContentType();
            if (contentType != null) bffResponse.setContentType(contentType);
            long contentLength = conn.getContentLengthLong();
            if (contentLength > 0) bffResponse.setContentLengthLong(contentLength);
            String disposition = conn.getHeaderField("Content-Disposition");
            if (disposition != null) bffResponse.setHeader("Content-Disposition", disposition);
            bffResponse.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            // 流式写出
            try (InputStream in = conn.getInputStream()) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) != -1) {
                    bffResponse.getOutputStream().write(buf, 0, len);
                }
                bffResponse.getOutputStream().flush();
            }
        } catch (com.houjiang.bffweb.support.PluginServiceResolver.ResolutionException e) {
            bffResponse.setStatus(e.status());
        } catch (Exception e) {
            log.error("文件流转发失败，路径: {}", oaPath, e);
            try {
                if (!bffResponse.isCommitted()) {
                    bffResponse.setStatus(500);
                    bffResponse.setContentType("application/json;charset=UTF-8");
                    bffResponse.getWriter().write("{\"code\":500,\"message\":\"文件转发失败: " + e.getMessage() + "\"}");
                }
            } catch (Exception ignored) {}
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * 多文件工资表导入：Feign 无法正确序列化 MultipartFile[]，直连 OA 转发 multipart。
     */
    private ResponseResult proxySalaryImportMultipart(MultipartFile[] files, String salaryMonth,
                                                      Boolean overwrite, String orgId,
                                                      HttpServletRequest request) {
        HttpURLConnection conn = null;
        try {
            String boundary = "----SalaryImport" + System.currentTimeMillis();
            String targetUrl = resolveOaUrl("/api/oa/salary/import");
            conn = (HttpURLConnection) new URL(targetUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30_000);
            conn.setReadTimeout(120_000);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            String auth = request.getHeader("Authorization");
            if (auth != null && !auth.isBlank()) {
                conn.setRequestProperty("Authorization", auth);
            }
            String orgIdHeader = request.getHeader("X-Org-Id");
            if (orgIdHeader != null && !orgIdHeader.isBlank()) conn.setRequestProperty("X-Org-Id", orgIdHeader);
            try (OutputStream out = conn.getOutputStream()) {
                for (MultipartFile file : files) {
                    if (file == null || file.isEmpty()) {
                        continue;
                    }
                    writeMultipartFile(out, boundary, "file", file);
                }
                writeMultipartField(out, boundary, "salaryMonth", salaryMonth != null ? salaryMonth : "");
                writeMultipartField(out, boundary, "overwrite", String.valueOf(Boolean.TRUE.equals(overwrite)));
                if (orgId != null && !orgId.isBlank()) {
                    writeMultipartField(out, boundary, "orgId", orgId.trim());
                }
                out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            }
            int status = conn.getResponseCode();
            InputStream in = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
            if (in == null) {
                return ResponseResult.fail(status, "薪资导入失败: HTTP " + status);
            }
            String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            if (status >= 400) {
                log.error("OA 工资导入转发失败 status={} body={}", status, body);
                return ResponseResult.fail(status, "薪资导入失败: HTTP " + status);
            }
            return JSON.readValue(body, ResponseResult.class);
        } catch (com.houjiang.bffweb.support.PluginServiceResolver.ResolutionException e) {
            return ResponseResult.fail(e.status(), e.getMessage());
        } catch (Exception e) {
            log.error("OA 工资导入 multipart 转发异常", e);
            return ResponseResult.fail(500, "薪资导入失败: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void writeMultipartField(OutputStream out, String boundary, String name, String value) throws Exception {
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write((value != null ? value : "").getBytes(StandardCharsets.UTF_8));
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private void writeMultipartFile(OutputStream out, String boundary, String name, MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.xlsx";
        String contentType = file.getContentType() != null ? file.getContentType()
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n")
                .getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(file.getBytes());
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping("/dashboard/summary")
    public ResponseResult getDashboardSummary() {
        return oaServiceFeign.getDashboardSummary();
    }

    @PostMapping("/dashboard/todo")
    public ResponseResult getTodoList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String keyword) {
        return oaServiceFeign.getTodoList(page, limit, keyword);
    }

    @PostMapping("/notice")
    public ResponseResult getNoticeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String type) {
        return oaServiceFeign.getNoticeList(page, limit, type);
    }

    @PostMapping("/hr/employee")
    public ResponseResult getEmployeeList(@RequestBody Map<String, Object> queryParams) {
        return oaServiceFeign.getEmployeeList(queryParams);
    }

    /** 薪资计算员工列表（与 /salary/calculate/employees/page 等价，便于按模块发现） */
    @PostMapping("/hr/employee/salary-page")
    public ResponseResult salaryEmployeePageForCalc(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.salaryCalculateEmployeesPage(params);
    }

    @PostMapping("/hr/employee/sync-wecom")
    public ResponseResult syncWecomEmployees() {
        return oaServiceFeign.syncWecomEmployees();
    }

    @PostMapping("/hr/employee/sync-platform")
    public ResponseResult syncPlatformEmployees() {
        return oaServiceFeign.syncPlatformEmployees();
    }

    /** 兼容：避免 /hr/employee/{id} 将 sync-platform 误解析为 id */
    @PostMapping("/hr/employee-sync/wecom")
    public ResponseResult syncWecomEmployeesAlias() {
        return oaServiceFeign.syncWecomEmployeesAlias();
    }

    @PostMapping("/hr/employee-sync/platform")
    public ResponseResult syncPlatformEmployeesAlias() {
        return oaServiceFeign.syncPlatformEmployeesAlias();
    }

    @PostMapping("/hr/employee/create")
    public ResponseResult createEmployee(@RequestBody Map<String, Object> employeeData) {
        return oaServiceFeign.createEmployee(employeeData);
    }

    /**
     * 前端 employee.js L77 使用 PUT，paas EmployeeController L152 亦为 @PostMapping("/{id}")
/**
     * 修复前：@PostMapping 导致 PUT /web/oa/hr/employee/{id} 返回 405 Method Not Allowed
     */
    @PostMapping("/hr/employee/{id:\\d+}")
    public ResponseResult updateEmployee(@PathVariable Integer id, @RequestBody Map<String, Object> employeeData) {
        return oaServiceFeign.updateEmployee(id, employeeData);
    }

    /**
     * 前端 employee.js L92 使用 DELETE，paas EmployeeController L173 为 @DeleteMapping("/{id}")
/**
     * 修复前：@PostMapping 导致 DELETE /web/oa/hr/employee/delete/{id} 返回 405
     */
    @DeleteMapping("/hr/employee/delete/{id:\\d+}")
    public ResponseResult deleteEmployee(@PathVariable Integer id) {
        return oaServiceFeign.deleteEmployee(id);
    }

    /**
     * 前端 employee.js L46 使用 GET /web/oa/hr/employee/{id}，paas EmployeeController L92 为 @GetMapping("/{id}")
/**
     * 修复前：BFF 层未注册该 GET 路由，详情接口 404/405
     */
    @GetMapping("/hr/employee/{id:\\d+}")
    public ResponseResult getEmployeeDetailById(@PathVariable Long id) {
        return oaServiceFeign.getEmployeeDetail(id);
    }

    /** 保留旧的 POST 入口做向后兼容（避免破坏历史调用方） */
    @PostMapping("/hr/employee/detail/{id:\\d+}")
    public ResponseResult getEmployeeDetail(@PathVariable Long id) {
        return oaServiceFeign.getEmployeeDetail(id);
    }

    @PostMapping("/hr/employee/options")
    public ResponseResult getEmployeeOptions(
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) Boolean includeResigned,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String orgId) {
        return oaServiceFeign.getEmployeeOptions(fields, status, departmentId, includeResigned, limit, orgId);
    }

    /** ==================== 员工薪资档案 ==================== */

    @GetMapping("/hr/employee-salary/by-employee/{employeeId}")
    public ResponseResult getEmployeeSalaryByEmployee(@PathVariable Integer employeeId,
                                                    @RequestParam(required = false) String orgId) {
        return oaServiceFeign.getEmployeeSalaryByEmployee(employeeId, orgId);
    }

    @PostMapping("/hr/employee-salary/upsert")
    public ResponseResult upsertEmployeeSalary(@RequestBody Map<String, Object> salaryData) {
        return oaServiceFeign.upsertEmployeeSalary(salaryData);
    }

    /** ==================== 服务商管理接口 ==================== */

    @PostMapping("/provider/list")
    public ResponseResult getProviderList(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getProviderList(params);
    }

    @GetMapping("/provider/{id}")
    public ResponseResult getProviderById(@PathVariable Integer id) {
        return oaServiceFeign.getProviderById(id);
    }

    @PostMapping("/provider/create")
    public ResponseResult createProvider(@RequestBody Map<String, Object> providerData) {
        return oaServiceFeign.createProvider(providerData);
    }

    @PostMapping("/provider/{id}")
    public ResponseResult updateProvider(@PathVariable Integer id, @RequestBody Map<String, Object> providerData) {
        return oaServiceFeign.updateProvider(id, providerData);
    }

    @DeleteMapping("/provider/{id}")
    public ResponseResult deleteProvider(@PathVariable Integer id) {
        return oaServiceFeign.deleteProvider(id);
    }

    @GetMapping("/provider/generate-code")
    public ResponseResult generateProviderCode() {
        return oaServiceFeign.generateProviderCode();
    }

    @PostMapping("/attendance/record")
    public ResponseResult getAttendanceRecord(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return oaServiceFeign.getAttendanceRecord(page, limit, employeeId, startDate, endDate);
    }

    @PostMapping("/attendance/record/page")
    public ResponseResult getAttendanceRecordPage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getAttendanceRecordPage(params);
    }

    @PostMapping("/attendance/statistics/summary")
    public ResponseResult getAttendanceStatisticsSummary(@RequestBody(required = false) Map<String, Object> params) {
        return oaServiceFeign.getAttendanceStatisticsSummary(params != null ? params : new HashMap<>());
    }

    @PostMapping("/attendance/statistics/department")
    public ResponseResult getAttendanceStatisticsDepartment(@RequestBody(required = false) Map<String, Object> params) {
        return oaServiceFeign.getAttendanceStatisticsDepartment(params != null ? params : new HashMap<>());
    }

    @PostMapping("/attendance/statistics/individual")
    public ResponseResult getAttendanceStatisticsIndividual(@RequestBody(required = false) Map<String, Object> params) {
        return oaServiceFeign.getAttendanceStatisticsIndividual(params != null ? params : new HashMap<>());
    }

    @PostMapping("/attendance/statistics/trend")
    public ResponseResult getAttendanceStatisticsTrend(@RequestBody(required = false) Map<String, Object> params) {
        return oaServiceFeign.getAttendanceStatisticsTrend(params != null ? params : new HashMap<>());
    }

    /**
     * 浏览器误打开 BFF API（GET）时重定向到前端考勤页，避免 5000 / No static resource
     */
    @GetMapping({
            "/attendance/lists/page",
            "/attendance/lists",
            "/attendance/sync",
            "/attendance/wecom/sync",
            "/attendance/lists/checkin",
            "/attendance/lists/sync"
    })
    public void redirectAttendanceApiToPage(HttpServletResponse response) throws java.io.IOException {
        response.sendRedirect("/oa/attendance/lists");
    }

    @PostMapping("/attendance/lists/page")
    public ResponseResult getAttendanceListPage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getAttendanceListPage(params);
    }

    @PostMapping("/attendance/lists/checkin")
    public ResponseResult getAttendanceCheckinDetail(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getAttendanceCheckinDetail(params);
    }

    @PostMapping("/attendance/lists/makeup")
    public ResponseResult makeupAttendance(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.makeupAttendance(params);
    }

    @PostMapping("/attendance/lists/whitelist")
    public ResponseResult setAttendanceWhitelist(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.setAttendanceWhitelist(params);
    }

    /**
     * 按考勤设置来源同步第三方考勤（企微/钉钉/飞书 → 各自分表）
/**
     * 兼容 /attendance/sync 与 /attendance/lists/sync 两种前端路径
     */
    @PostMapping({"/attendance/sync", "/attendance/lists/sync"})
    public ResponseResult<Map<String, Object>> syncThirdPartyAttendance(@RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> body = params != null ? params : new HashMap<>();
        if (!body.containsKey("month")) {
            body.put("month", java.time.YearMonth.now().toString());
        }
        return invokeOaAttendanceSync(body);
    }

    @PostMapping("/attendance/wecom/sync")
    public ResponseResult<Map<String, Object>> syncWecomAttendance(@RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> body = params != null ? params : new HashMap<>();
        if (!body.containsKey("month")) {
            body.put("month", java.time.YearMonth.now().toString());
        }
        try {
            return oaServiceFeign.syncAttendanceListsWecom(body);
        } catch (FeignException.NotFound e) {
            return oaServiceFeign.syncWecomAttendanceRoot(body);
        }
    }

    /**
     * 兼容多版本 oa.jar：优先 lists/sync，404 时回退 lists/sync-wecom（当前线上包），再回退 /oa/attendance/sync。
     */
    @SuppressWarnings("rawtypes")
    private ResponseResult<Map<String, Object>> invokeOaAttendanceSync(Map<String, Object> body) {
        try {
            return oaServiceFeign.syncAttendanceLists(body);
        } catch (FeignException.NotFound e1) {
            log.warn("OA lists/sync 404，回退 lists/sync-wecom: {}", e1.getMessage());
            try {
                return oaServiceFeign.syncAttendanceListsWecom(body);
            } catch (FeignException.NotFound e2) {
                log.warn("OA lists/sync-wecom 404，回退 /oa/attendance/sync: {}", e2.getMessage());
                return oaServiceFeign.syncAttendanceRoot(body);
            }
        }
    }

  /** ==================== 企微 SCRM ==================== */

    @PostMapping("/wecom/department/tree")
    public ResponseResult wecomDepartmentTree() {
        return oaServiceFeign.wecomDepartmentTree();
    }

    @PostMapping("/wecom/department/save")
    public ResponseResult wecomDepartmentSave(@RequestBody Map<String, Object> body) {
        return oaServiceFeign.wecomDepartmentSave(body);
    }

    @PostMapping("/wecom/department/delete")
    public ResponseResult wecomDepartmentDelete(@RequestBody Map<String, Object> body) {
        return oaServiceFeign.wecomDepartmentDelete(body);
    }

    @PostMapping("/wecom/department/sync")
    public ResponseResult wecomDepartmentSync() {
        return oaServiceFeign.wecomDepartmentSync();
    }

    @PostMapping("/wecom/scrm/{module}/page")
    public ResponseResult wecomScrmPage(@PathVariable String module, @RequestBody Map<String, Object> params) {
        return oaServiceFeign.wecomScrmPage(module, params);
    }

    @PostMapping("/wecom/scrm/{module}/save")
    public ResponseResult wecomScrmSave(@PathVariable String module, @RequestBody Map<String, Object> body) {
        return oaServiceFeign.wecomScrmSave(module, body);
    }

    @PostMapping("/wecom/scrm/{module}/delete")
    public ResponseResult wecomScrmDelete(@PathVariable String module, @RequestBody Map<String, Object> body) {
        return oaServiceFeign.wecomScrmDelete(module, body);
    }

    @PostMapping("/wecom/scrm/{module}/sync")
    public ResponseResult wecomScrmSync(@PathVariable String module, @RequestBody(required = false) Map<String, Object> params) {
        return oaServiceFeign.wecomScrmSync(module, params != null ? params : new HashMap<>());
    }

    @PostMapping("/background-job/status")
    public ResponseResult getBackgroundJobStatus(@RequestBody Map<String, Object> body) {
        return oaServiceFeign.getOaBackgroundJobStatus(body);
    }

    @PostMapping("/wecom/scrm/analytics/{type}")
    public ResponseResult wecomScrmAnalytics(@PathVariable String type, @RequestBody(required = false) Map<String, Object> params) {
        return oaServiceFeign.wecomScrmAnalytics(type, params != null ? params : new HashMap<>());
    }

    @PostMapping("/wecom/scrm/task/{id}/execute")
    public ResponseResult wecomScrmExecuteTask(@PathVariable Long id) {
        return oaServiceFeign.wecomScrmExecuteTask(id);
    }

    @PostMapping("/wecom/scrm/{module}/import")
    public ResponseResult wecomScrmImport(@PathVariable String module, @RequestBody Map<String, Object> body) {
        return oaServiceFeign.wecomScrmImport(module, body);
    }

    @PostMapping("/wecom/scrm/archive/staff")
    public ResponseResult wecomArchiveStaff(@RequestBody(required = false) Map<String, Object> params) {
        return oaServiceFeign.wecomArchiveStaff(params != null ? params : new HashMap<>());
    }

    @PostMapping("/wecom/scrm/archive/contacts")
    public ResponseResult wecomArchiveContacts(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.wecomArchiveContacts(params);
    }

    @PostMapping("/wecom/scrm/archive/messages")
    public ResponseResult wecomArchiveMessages(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.wecomArchiveMessages(params);
    }

    @PostMapping("/attendance/leave/page")
    public ResponseResult getLeavePage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getLeavePage(params);
    }

    @PostMapping("/attendance/leave/apply")
    public ResponseResult applyLeave(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.applyLeave(params);
    }

    @PostMapping("/attendance/leave/approve/{id}")
    public ResponseResult approveLeave(@PathVariable("id") Long id, @RequestBody Map<String, Object> params) {
        return oaServiceFeign.approveLeave(id, params);
    }

    @PostMapping("/attendance/leave/cancel/{id}")
    public ResponseResult cancelLeave(@PathVariable("id") Long id) {
        return oaServiceFeign.cancelLeave(id);
    }

    @PostMapping("/attendance/leave/statistics")
    public ResponseResult getLeaveStatistics(@RequestBody(required = false) Map<String, Object> params) {
        return oaServiceFeign.getLeaveStatistics(params != null ? params : new HashMap<>());
    }

    @PostMapping("/attendance/overtime/page")
    public ResponseResult getOvertimePage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getOvertimePage(params);
    }

    @PostMapping("/attendance/overtime/apply")
    public ResponseResult applyOvertime(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.applyOvertime(params);
    }

    @PostMapping("/attendance/overtime/approve/{id}")
    public ResponseResult approveOvertime(@PathVariable("id") Long id, @RequestBody Map<String, Object> params) {
        return oaServiceFeign.approveOvertime(id, params);
    }

    @PostMapping("/attendance/overtime/cancel/{id}")
    public ResponseResult cancelOvertime(@PathVariable("id") Long id) {
        return oaServiceFeign.cancelOvertime(id);
    }

    @PostMapping("/attendance/overtime/statistics")
    public ResponseResult getOvertimeStatistics(@RequestBody(required = false) Map<String, Object> params) {
        return oaServiceFeign.getOvertimeStatistics(params != null ? params : new HashMap<>());
    }

    @PostMapping("/attendance/travel/page")
    public ResponseResult getTravelPage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getTravelPage(params);
    }

    @PostMapping("/attendance/travel/apply")
    public ResponseResult applyTravel(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.applyTravel(params);
    }

    @PostMapping("/attendance/travel/approve/{id}")
    public ResponseResult approveTravel(@PathVariable("id") Long id, @RequestBody Map<String, Object> params) {
        return oaServiceFeign.approveTravel(id, params);
    }

    @PostMapping("/attendance/travel/cancel/{id}")
    public ResponseResult cancelTravel(@PathVariable("id") Long id) {
        return oaServiceFeign.cancelTravel(id);
    }

    @PostMapping("/attendance/travel/complete/{id}")
    public ResponseResult completeTravel(@PathVariable("id") Long id,
                                         @RequestBody(required = false) Map<String, Object> params) {
        return oaServiceFeign.completeTravel(id, params != null ? params : new HashMap<>());
    }

    @PostMapping("/attendance/checkin")
    public ResponseResult checkIn(@RequestParam(required = false) String location) {
        return oaServiceFeign.checkIn(location);
    }

    @PostMapping("/salary/payslip")
    public ResponseResult getSalaryList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String yearMonth) {
        return oaServiceFeign.getSalaryList(page, limit, employeeId, yearMonth);
    }

    @PostMapping("/document")
    public ResponseResult getDocumentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return oaServiceFeign.getDocumentList(page, limit, keyword, category);
    }

    @PostMapping("/meeting/room")
    public ResponseResult getMeetingRoomList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String name) {
        return oaServiceFeign.getMeetingRoomList(page, limit, name);
    }

    @PostMapping("/meeting/reserve")
    public ResponseResult reserveMeetingRoom(
            @RequestParam Long roomId,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam String subject) {
        return oaServiceFeign.reserveMeetingRoom(roomId, startTime, endTime, subject);
    }

    /** ==================== 资产管理接口 ==================== */

    @PostMapping("/asset/list")
    public ResponseResult getAssetListV2(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getAssetListV2(params);
    }

    @PostMapping("/asset/create")
    public ResponseResult createAsset(@RequestBody Map<String, Object> assetData) {
        return oaServiceFeign.createAsset(assetData);
    }

    @PostMapping("/asset/update")
    public ResponseResult updateAsset(@RequestBody Map<String, Object> assetData) {
        return oaServiceFeign.updateAsset(assetData);
    }

    @PostMapping("/asset/delete/{id}")
    public ResponseResult deleteAsset(@PathVariable Long id) {
        return oaServiceFeign.deleteAsset(id);
    }

    @PostMapping("/asset/detail/{id}")
    public ResponseResult getAssetDetail(@PathVariable Long id) {
        return oaServiceFeign.getAssetDetail(id);
    }

    @PostMapping("/asset/allocate/{id}")
    public ResponseResult allocateAsset(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        return oaServiceFeign.allocateAsset(id, params);
    }

    @PostMapping("/asset/recycle/{id}")
    public ResponseResult recycleAsset(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> params) {
        return oaServiceFeign.recycleAsset(id, params);
    }

    @PostMapping("/asset/maintenance/{id}")
    public ResponseResult maintenanceAsset(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        return oaServiceFeign.maintenanceAsset(id, params);
    }

    @PostMapping("/asset/scrap/{id}")
    public ResponseResult scrapAsset(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> params) {
        return oaServiceFeign.scrapAsset(id, params);
    }

    @PostMapping("/asset/statistics")
    public ResponseResult getAssetStatisticsV2() {
        return oaServiceFeign.getAssetStatisticsV2();
    }

    @PostMapping("/vehicle")
    public ResponseResult getVehicleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return oaServiceFeign.getVehicleList(page, limit, keyword, status);
    }

    @PostMapping("/vehicle/apply")
    public ResponseResult applyVehicle(
            @RequestParam Long vehicleId,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam String purpose) {
        return oaServiceFeign.applyVehicle(vehicleId, startTime, endTime, purpose);
    }

    @PostMapping("/report/statistics")
    public ResponseResult getReportStatistics(
            @RequestParam String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return oaServiceFeign.getReportStatistics(type, startDate, endDate);
    }

    /** ==================== 合同管理接口 ==================== */

    @RequestMapping(value = "/contract/list", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult getContractList(@RequestBody(required = false) Map<String, Object> queryParams) {
        long startMs = System.currentTimeMillis();
        try {
            Map<String, Object> body = queryParams != null ? queryParams : new HashMap<>();
            ResponseResult result = oaServiceFeign.getContractList(body);
            long costMs = System.currentTimeMillis() - startMs;
            if (costMs > 3000) {
                log.warn("合同列表 BFF 转发偏慢: {}ms, hostService=oa, params={}", costMs, body);
            } else {
                log.debug("合同列表 BFF 转发完成: {}ms", costMs);
            }
            return result;
        } catch (Exception e) {
            long costMs = System.currentTimeMillis() - startMs;
            log.error("合同列表 BFF 转发失败, {}ms, hostService=oa", costMs, e);
            String msg = e.getMessage();
            if (msg != null && msg.toLowerCase().contains("timed out")) {
                return ResponseResult.fail(504, "合同列表查询超时，请确认 OA 服务已启动且 feign.oa.url 指向正确地址");
            }
            if (msg != null && msg.length() > 240) {
                msg = msg.substring(0, 240);
            }
            return ResponseResult.fail(500, "合同列表查询失败: " + (msg != null ? msg : e.getClass().getSimpleName()));
        }
    }

    /**
     * 导出合同列表为 Excel 文件
/**
     * <p>
     * 直接转发 OA 服务返回的二进制内容，并将文件流写入 HttpServletResponse。
     * 注意：此接口返回的是文件流，不能用 ResponseResult 封装。
     * </p>
     *
     * @param params   查询参数（与 list 接口保持一致）
     * @param response HTTP 响应对象，用于直接写出文件流
     */
    @PostMapping("/contract/export")
    public void exportContractList(@RequestBody(required = false) Map<String, Object> params,
                                   HttpServletResponse response) {
        try {
            Map<String, Object> body = params != null ? params : new HashMap<>();
            org.springframework.http.ResponseEntity<byte[]> result = oaServiceFeign.exportContractList(body);
            if (result == null || result.getBody() == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":-1,\"message\":\"导出失败，数据为空\"}");
                return;
            }
            // 转发响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // 转发文件名称响应头
            org.springframework.http.HttpHeaders headers = result.getHeaders();
            String contentDisposition = headers.getFirst("Content-Disposition");
            if (contentDisposition != null) {
                response.setHeader("Content-Disposition", contentDisposition);
            } else {
                String fileName = java.net.URLEncoder.encode("合同列表.xlsx",
                        java.nio.charset.StandardCharsets.UTF_8);
                response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            }
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            // 写出文件内容
            response.getOutputStream().write(result.getBody());
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("导出合同列表 BFF 转发失败", e);
            try {
                if (!response.isCommitted()) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":-1,\"message\":\"导出失败: " + e.getMessage() + "\"}");
                }
            } catch (java.io.IOException ioe) {
                log.error("写出错误响应失败", ioe);
            }
        }
    }

    @PostMapping("/contract/create")
    public ResponseResult createContract(@RequestBody Map<String, Object> contractData) {
        ResponseResult result = oaServiceFeign.createContract(contractData);
        if (result != null && result.getMessage() != null) {
            String lowerMsg = result.getMessage().toLowerCase();
            if (lowerMsg.contains("uk_contract_number") && lowerMsg.contains("duplicate entry")) {
                return ResponseResult.error("合同编号已存在，请更换合同编号或直接查看已有合同");
            }
        }
        return result;
    }

    /** 注意：具体的路径必须放在通用路径 {id} 之前，避免路由冲突 */
    @PostMapping("/contract/verify-phone")
    public ResponseResult verifyContractPhone(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.verifyContractPhone(params);
    }

    @PostMapping("/contract/generate-number")
    public ResponseResult generateContractNumber(@RequestBody(required = false) Map<String, Object> params) {
        return oaServiceFeign.generateContractNumber();
    }

    /** 注意：sign-info路径必须在 {id} 之前，避免路由冲突 */
    @PostMapping("/contract/sign-info/{id}")
    public ResponseResult getContractSignInfo(@PathVariable Integer id, @RequestBody Map<String, Object> requestData) {
        return oaServiceFeign.getContractSignInfo(id, requestData);
    }

    @PostMapping("/contract/{id}")
    public ResponseResult getContractDetail(@PathVariable Integer id) {
        return oaServiceFeign.getContractDetail(id);
    }

    @PostMapping("/contract/update/{id}")
    public ResponseResult updateContract(@PathVariable Integer id, @RequestBody Map<String, Object> contractData) {
        return oaServiceFeign.updateContract(id, contractData);
    }

    @PostMapping("/contract/delete/{id}")
    public ResponseResult deleteContract(@PathVariable Integer id) {
        return oaServiceFeign.deleteContract(id);
    }

    @PostMapping("/contract/disable/{id}")
    public ResponseResult disableContract(@PathVariable Integer id) {
        return oaServiceFeign.disableContract(id);
    }

    @PostMapping("/contract/enable/{id}")
    public ResponseResult enableContract(@PathVariable Integer id) {
        return oaServiceFeign.enableContract(id);
    }

    @PostMapping("/contract/complete/{id}")
    public ResponseResult completeContract(@PathVariable Integer id) {
        return oaServiceFeign.completeContract(id);
    }

    @PostMapping("/contract/suspend/{id}")
    public ResponseResult suspendContract(@PathVariable Integer id) {
        return oaServiceFeign.suspendContract(id);
    }

    @PostMapping("/contract/terminate/{id}")
    public ResponseResult terminateContract(@PathVariable Integer id) {
        return oaServiceFeign.terminateContract(id);
    }

    // ==================== 合同数据分析接口 ====================

    /**
     * 获取合同统计数据
/**
     *
     * @param params 查询参数
     * @return ResponseResult 合同统计数据
     */
    @PostMapping("/contract/analytics/statistics")
    public ResponseResult getContractAnalyticsStatistics(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getContractAnalyticsStatistics(params);
    }

    /**
     * 获取合同趋势数据
/**
     *
     * @param params 查询参数（startDate, endDate 等）
     * @return ResponseResult 合同趋势数据
     */
    @PostMapping("/contract/analytics/trend")
    public ResponseResult getContractAnalyticsTrend(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getContractAnalyticsTrend(params);
    }

    /**
     * 获取合同类型分布
/**
     *
     * @param params 查询参数
     * @return ResponseResult 合同类型分布数据
     */
    @PostMapping("/contract/analytics/type-distribution")
    public ResponseResult getContractAnalyticsTypeDistribution(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getContractAnalyticsTypeDistribution(params);
    }

    /**
     * 获取合同金额分析
/**
     *
     * @param params 查询参数
     * @return ResponseResult 合同金额分析数据
     */
    @PostMapping("/contract/analytics/amount-analysis")
    public ResponseResult getContractAnalyticsAmountAnalysis(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getContractAnalyticsAmountAnalysis(params);
    }

    /**
     * 获取合同状态分布
/**
     *
     * @param params 查询参数
     * @return ResponseResult 合同状态分布数据
     */
    @PostMapping("/contract/analytics/status-distribution")
    public ResponseResult getContractAnalyticsStatusDistribution(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getContractAnalyticsStatusDistribution(params);
    }

    /**
     * 获取合同预测数据
/**
     *
     * @param params 查询参数
     * @return ResponseResult 合同预测分析数据
     */
    @PostMapping("/contract/analytics/prediction")
    public ResponseResult getContractAnalyticsPrediction(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getContractAnalyticsPrediction(params);
    }

    /**
     * 获取合同风险分析数据
/**
     *
     * @param params 查询参数
     * @return ResponseResult 合同风险分析数据
     */
    @PostMapping("/contract/analytics/risk-analysis")
    public ResponseResult getContractAnalyticsRiskAnalysis(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getContractAnalyticsRiskAnalysis(params);
    }

    // ==================== 合同文件管理接口 ====================

    /**
     * 上传合同文件（两步方案：先上传OSS获取URL，再保存文件记录到OA）
/**
     *
     * @param contractId  合同ID
     * @param file        上传的文件
     * @param description 文件描述（可选）
     * @return 上传结果，包含文件信息
     */
    @PostMapping(value = "/contract/file/upload/{contractId}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseResult uploadContractFile(
            @PathVariable Long contractId,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "description", required = false) String description) {
        try {
            log.info("合同文件上传开始，合同ID：{}，文件名：{}，大小：{} bytes", contractId, file.getOriginalFilename(), file.getSize());

            // 第一步：上传文件到OSS
            ResponseResult ossResult = ossServiceFeign.uploadFile(file);
            if (ossResult == null || (ossResult.getCode() != 1 && ossResult.getCode() != 200)) {
                String errMsg = ossResult != null ? ossResult.getMessage() : "OSS服务无响应";
                log.error("OSS文件上传失败：{}", errMsg);
                return ResponseResult.error("文件上传失败：" + errMsg);
            }

            // 获取OSS返回的文件URL
            String fileUrl = ossResult.getData() != null ? ossResult.getData().toString() : null;
            if (fileUrl == null || fileUrl.isEmpty()) {
                log.error("OSS上传成功但未返回文件URL");
                return ResponseResult.error("文件上传失败：未获取到文件地址");
            }
            log.info("OSS文件上传成功，URL：{}", fileUrl);

            // 第二步：保存文件记录到OA服务
            String originalFileName = file.getOriginalFilename();
            String fileType = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileType = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
            }

            Map<String, Object> saveParams = new HashMap<>();
            saveParams.put("contractId", contractId);
            saveParams.put("fileName", originalFileName);
            saveParams.put("filePath", fileUrl);
            saveParams.put("fileSize", file.getSize());
            saveParams.put("fileType", fileType);
            saveParams.put("mimeType", file.getContentType());
            saveParams.put("description", description != null ? description : "");

            ResponseResult saveResult = oaServiceFeign.saveContractFile(saveParams);
            log.info("合同文件记录保存结果：{}", saveResult);
            return saveResult;

        } catch (Exception e) {
            log.error("合同文件上传失败，合同ID：{}", contractId, e);
            return ResponseResult.error("文件上传失败：" + e.getMessage());
        }
    }

    @PostMapping("/contract/file/save")
    public ResponseResult saveContractFile(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.saveContractFile(params);
    }

    @PostMapping("/contract/file/list/{contractId}")
    public ResponseResult getContractFileList(
            @PathVariable Long contractId,
            @RequestBody Map<String, Object> params) {
        return oaServiceFeign.getContractFileList(contractId, params);
    }

    @PostMapping("/contract/file/all/{contractId}")
    public ResponseResult getAllContractFiles(@PathVariable Long contractId) {
        return oaServiceFeign.getAllContractFiles(contractId);
    }

    @PostMapping("/contract/file/{fileId}")
    public ResponseResult getContractFileDetail(@PathVariable Long fileId) {
        return oaServiceFeign.getContractFileDetail(fileId);
    }

    /**
     * 合同文件预览：直接流式转发 OA 文件内容给浏览器，避免 Feign 无法传递二进制流
     */
    @RequestMapping(value = "/contract/file/preview/{fileId}", method = {RequestMethod.GET, RequestMethod.POST})
    public void previewContractFile(@PathVariable Long fileId,
                                    HttpServletRequest request, HttpServletResponse response) {
        proxyFileStream("/api/oa/contract/file/preview/" + fileId, request, response);
    }

    /**
     * 合同文件下载：直接流式转发 OA 文件内容给浏览器，避免 Feign 无法传递二进制流
     */
    @RequestMapping(value = "/contract/file/download/{fileId}", method = {RequestMethod.GET, RequestMethod.POST})
    public void downloadContractFile(@PathVariable Long fileId,
                                     HttpServletRequest request, HttpServletResponse response) {
        proxyFileStream("/api/oa/contract/file/download/" + fileId, request, response);
    }

    /**
     * 合同文件删除：支持 POST 和 DELETE 两种 HTTP 方法
/**
     * 前端 OAApiClient.delete() 发送 DELETE 方法，兼容兼容旧 POST 方式
     */
    @RequestMapping(value = "/contract/file/delete/{fileId}",
                    method = {RequestMethod.POST, RequestMethod.DELETE})
    public ResponseResult deleteContractFile(@PathVariable Long fileId) {
        return oaServiceFeign.deleteContractFile(fileId);
    }

    /**
     * 合同文件批量删除
     */
    @PostMapping("/contract/file/batch-delete")
    public ResponseResult batchDeleteContractFiles(@RequestBody List<Long> fileIds) {
        return oaServiceFeign.batchDeleteContractFiles(fileIds);
    }

    /**
     * 合同文件版本历史：支持 GET 和 POST 两种方法
/**
     * 前端 OAApiClient.get() 发送 GET 方法，BFF 同时保留 POST 兼容
     */
    @RequestMapping(value = "/contract/file/versions/{contractId}",
                    method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult getContractFileVersions(
            @PathVariable Long contractId,
            @RequestParam String originalFileName) {
        return oaServiceFeign.getContractFileVersions(contractId, originalFileName);
    }

    @PostMapping("/contract/file/preview-url/{fileId}")
    public ResponseResult getContractFilePreviewUrl(@PathVariable Long fileId) {
        return oaServiceFeign.getContractFilePreviewUrl(fileId);
    }

    @PostMapping("/contract/file/check-exists")
    public ResponseResult checkContractFileExists(@RequestBody Map<String, String> params) {
        return oaServiceFeign.checkContractFileExists(params);
    }

    /** ==================== 合同分类管理接口 ==================== */

    @GetMapping("/contract/category/tree")
    public ResponseResult getContractCategoryTreeGet() {
        return getContractCategoryTreeInternal(new HashMap<>());
    }

    @PostMapping("/contract/category/tree")
    public ResponseResult getContractCategoryTreePost(@RequestBody(required = false) Map<String, Object> params) {
        return getContractCategoryTreeInternal(params != null ? params : new HashMap<>());
    }

    private ResponseResult getContractCategoryTreeInternal(Map<String, Object> params) {
        try {
            return oaServiceFeign.getContractCategoryTree(params);
        } catch (Exception e) {
            log.error("合同分类树 BFF 转发失败", e);
            String msg = e.getMessage();
            if (msg != null && msg.length() > 240) {
                msg = msg.substring(0, 240);
            }
            return ResponseResult.fail(500, "合同分类树查询失败: " + (msg != null ? msg : e.getClass().getSimpleName()));
        }
    }

    @PostMapping("/contract/category/list")
    public ResponseResult getContractCategoryList(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getContractCategoryList(params);
    }

    @PostMapping("/contract/category/{id}")
    public ResponseResult getContractCategoryDetail(@PathVariable Long id) {
        return oaServiceFeign.getContractCategoryDetail(id);
    }

    @PostMapping("/contract/category/create")
    public ResponseResult createContractCategory(@RequestBody Map<String, Object> categoryData) {
        return oaServiceFeign.createContractCategory(categoryData);
    }

    @PostMapping("/contract/category/update/{id}")
    public ResponseResult updateContractCategory(@PathVariable Long id, @RequestBody Map<String, Object> categoryData) {
        return oaServiceFeign.updateContractCategory(id, categoryData);
    }

    @PostMapping("/contract/category/delete/{id}")
    public ResponseResult deleteContractCategory(@PathVariable Long id) {
        return oaServiceFeign.deleteContractCategory(id);
    }

    /** ==================== 合同模板管理接口 ==================== */

    @PostMapping("/contract/template/templates")
    public ResponseResult getContractTemplates(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getContractTemplates(params);
    }

    @GetMapping("/contract/template/{id}")
    public ResponseResult getContractTemplateById(@PathVariable Integer id) {
        return oaServiceFeign.getContractTemplateById(id);
    }

    @PostMapping("/contract/template/create")
    public ResponseResult createContractTemplate(@RequestBody Map<String, Object> templateData) {
        return oaServiceFeign.createContractTemplate(templateData);
    }

    @PostMapping("/contract/template/{id}")
    public ResponseResult updateContractTemplate(@PathVariable Integer id, @RequestBody Map<String, Object> templateData) {
        return oaServiceFeign.updateContractTemplate(id, templateData);
    }

    @DeleteMapping("/contract/template/{id}")
    public ResponseResult deleteContractTemplate(@PathVariable Integer id) {
        return oaServiceFeign.deleteContractTemplate(id);
    }

    /** ==================== 合同签署接口 ==================== */

    @PostMapping("/contract/sign/generate-link")
    public ResponseResult generateSignLink(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.generateSignLink(params);
    }

    @GetMapping("/contract/sign/info/{token}")
    public ResponseResult getContractBySignToken(@PathVariable String token) {
        return oaServiceFeign.getContractBySignToken(token);
    }

    @PostMapping("/contract/sign/confirm/{token}")
    public ResponseResult confirmSign(@PathVariable String token, @RequestBody Map<String, Object> signData) {
        return oaServiceFeign.confirmSign(token, signData);
    }

    @PostMapping("/contract/sign/{id}")
    public ResponseResult signContract(@PathVariable Integer id, @RequestBody Map<String, Object> signData) {
        return oaServiceFeign.signContract(id, signData);
    }

    @PostMapping("/contract/category/generate-code")
    public ResponseResult generateCategoryCode(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.generateCategoryCode(params);
    }

    /** ==================== 合同审批管理接口 ==================== */

    @PostMapping("/contract/approval/list")
    public ResponseResult getContractApprovalList(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getContractApprovalList(params);
    }

    @PostMapping("/contract/approval/contract/{contractId}")
    public ResponseResult getContractApprovalRecords(@PathVariable Long contractId) {
        return oaServiceFeign.getContractApprovalRecords(contractId);
    }

    @PostMapping("/contract/approval/submit")
    public ResponseResult submitContractApproval(@RequestBody Map<String, Object> approvalData) {
        return oaServiceFeign.submitContractApproval(approvalData);
    }

    @PostMapping("/contract/approval/process")
    public ResponseResult processContractApproval(@RequestBody Map<String, Object> processData) {
        return oaServiceFeign.processContractApproval(processData);
    }

    @PostMapping("/contract/approval/withdraw/{id}")
    public ResponseResult withdrawContractApproval(@PathVariable Long id) {
        return oaServiceFeign.withdrawContractApproval(id);
    }

    /** ==================== 服务商打款审批 ==================== */

    @PostMapping("/supplier/payment/approval/submit")
    public ResponseResult submitSupplierPaymentApproval(@RequestBody Map<String, Object> approvalData) {
        return oaServiceFeign.submitSupplierPaymentApproval(approvalData);
    }

    @PostMapping("/supplier/payment/approval/process")
    public ResponseResult processSupplierPaymentApproval(@RequestBody Map<String, Object> processData) {
        return oaServiceFeign.processSupplierPaymentApproval(processData);
    }

    @PostMapping("/supplier/payment/approval/withdraw/{id}")
    public ResponseResult withdrawSupplierPaymentApproval(@PathVariable Integer id) {
        return oaServiceFeign.withdrawSupplierPaymentApproval(id);
    }

    @GetMapping("/supplier/payment/approval/spending/{spendingId}")
    public ResponseResult getSupplierPaymentApprovalBySpendingId(@PathVariable Integer spendingId) {
        return oaServiceFeign.getSupplierPaymentApprovalBySpendingId(spendingId);
    }

    @PostMapping("/supplier/payment/approval/pending")
    public ResponseResult getSupplierPaymentPendingApprovals() {
        return oaServiceFeign.getSupplierPaymentPendingApprovals();
    }

    @PostMapping("/supplier/payment/approval/status-by-spending-ids")
    public ResponseResult getSupplierPaymentApprovalStatusBySpendingIds(@RequestBody Map<String, Object> body) {
        return oaServiceFeign.getSupplierPaymentApprovalStatusBySpendingIds(body);
    }

    /**
     * 前端 organization.js L163 用 GET，paas DepartmentController L62 亦为 @GetMapping("/page")
/**
     * 兼容 GET 和 POST：避免旧页面以 POST 调用时 405
     */
    @RequestMapping(value = "/hr/department/page", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult getDepartmentPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String departmentCode,
            @RequestParam(required = false) Long parentId) {
        return oaServiceFeign.getDepartmentPage(page, size, departmentName, departmentCode, parentId);
    }

    @RequestMapping(value = "/hr/department/tree", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult getDepartmentTree() {
        return oaServiceFeign.getDepartmentTree();
    }

    @PostMapping("/hr/department")
    public ResponseResult addDepartment(@RequestBody Map<String, Object> department) {
        return oaServiceFeign.addDepartment(department);
    }

    /** 保留旧的 POST /update/{id} 做向后兼容 */
    @PostMapping("/hr/department/update/{id}")
    public ResponseResult updateDepartment(@PathVariable Long id, @RequestBody Map<String, Object> department) {
        return oaServiceFeign.updateDepartment(id, department);
    }

    /** 保留旧的 POST /delete/{id} 做向后兼容 */
    @PostMapping("/hr/department/delete/{id}")
    public ResponseResult deleteDepartment(@PathVariable Long id) {
        return oaServiceFeign.deleteDepartment(id);
    }

    /** RESTful：前端 organization.js L215 使用 PUT /web/oa/hr/department/{id} */
    @PostMapping("/hr/department/{id:\\d+}")
    public ResponseResult updateDepartmentRest(@PathVariable Long id, @RequestBody Map<String, Object> department) {
        return oaServiceFeign.updateDepartment(id, department);
    }

    /** RESTful：前端 organization.js L226 使用 DELETE /web/oa/hr/department/{id} */
    @DeleteMapping("/hr/department/{id:\\d+}")
    public ResponseResult deleteDepartmentRest(@PathVariable Long id) {
        return oaServiceFeign.deleteDepartment(id);
    }

    /** RESTful：前端 organization.js L193 使用 GET /web/oa/hr/department/{id} */
    @GetMapping("/hr/department/{id:\\d+}")
    public ResponseResult getDepartmentById(@PathVariable Long id) {
        return oaServiceFeign.getDepartmentById(id);
    }

    @PostMapping("/hr/department/sync-wecom")
    public ResponseResult syncDepartmentsFromWecom() {
        return oaServiceFeign.syncDepartmentsFromWecom();
    }

           /**
            * 兼容 GET 和 POST：前端 organization.js 用 GET + params，旧页面可能用 POST
/**
            * 修复前：仅 @PostMapping 导致前端 GET /hr/position/page 返回 405 Method Not Allowed
            */
           @RequestMapping(value = "/hr/position/page", method = {RequestMethod.GET, RequestMethod.POST})
           public ResponseResult getPositionPage(
                   @RequestParam(defaultValue = "1") Integer page,
                   @RequestParam(defaultValue = "10") Integer size,
                   @RequestParam(required = false) String name) {
               return oaServiceFeign.getPositionPage(page, size, name);
           }
       
           @PostMapping("/hr/position")
           public ResponseResult addPosition(@RequestBody Map<String, Object> position) {
               return oaServiceFeign.addPosition(position);
           }
       
           /** 保留旧的 POST /update/{id} 做向后兼容 */
           @PostMapping("/hr/position/update/{id}")
           public ResponseResult updatePosition(@PathVariable Integer id, @RequestBody Map<String, Object> position) {
               return oaServiceFeign.updatePosition(id, position);
           }
       
           /** 保留旧的 POST /delete/{id} 做向后兼容 */
           @PostMapping("/hr/position/delete/{id}")
           public ResponseResult deletePosition(@PathVariable Integer id) {
               return oaServiceFeign.deletePosition(id);
           }
       
           /** RESTful：前端 organization.js L336 使用 PUT /web/oa/hr/position/{id} */
           @PostMapping("/hr/position/{id}")
           public ResponseResult updatePositionRest(@PathVariable Integer id, @RequestBody Map<String, Object> position) {
               return oaServiceFeign.updatePosition(id, position);
           }
       
           /** RESTful：前端 organization.js L347 使用 DELETE /web/oa/hr/position/{id} */
           @DeleteMapping("/hr/position/{id}")
           public ResponseResult deletePositionRest(@PathVariable Integer id) {
               return oaServiceFeign.deletePosition(id);
           }
       
           /** RESTful：前端 organization.js L314 使用 GET /web/oa/hr/position/{id} */
           @GetMapping("/hr/position/{id}")
           public ResponseResult getPositionById(@PathVariable Integer id) {
               return oaServiceFeign.getPositionById(id);
           }

           @PostMapping("/hr/position/sync-wecom")
           public ResponseResult syncPositionsFromWecom() {
               return oaServiceFeign.syncPositionsFromWecom();
           }

    @PostMapping("/performance/page")
    public ResponseResult getPerformancePage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getPerformancePage(params);
    }

    @GetMapping("/performance/{id}")
    public ResponseResult getPerformanceById(@PathVariable Long id) {
        return oaServiceFeign.getPerformanceById(id);
    }

    @PostMapping("/performance")
    public ResponseResult addPerformance(@RequestBody Map<String, Object> performance) {
        return oaServiceFeign.addPerformance(performance);
    }

    @PostMapping("/performance/{id}/start")
    public ResponseResult startPerformanceEvaluation(@PathVariable Long id) {
        return oaServiceFeign.startPerformanceEvaluation(id);
    }

    @DeleteMapping("/performance/{id}")
    public ResponseResult deletePerformance(@PathVariable Long id) {
        return oaServiceFeign.deletePerformance(id);
    }

    @PostMapping("/recruit/positions")
    public ResponseResult getRecruitPositionPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department) {
        return oaServiceFeign.getRecruitPositionPage(page, size, keyword, status, department);
    }

    @PostMapping("/recruit/positions/add")
    public ResponseResult addRecruitPosition(@RequestBody Map<String, Object> position) {
        return oaServiceFeign.addRecruitPosition(position);
    }

    @PostMapping("/recruit/positions/update/{id}")
    public ResponseResult updateRecruitPosition(@PathVariable Long id, @RequestBody Map<String, Object> position) {
        return oaServiceFeign.updateRecruitPosition(id, position);
    }

    @PostMapping("/recruit/positions/delete/{id}")
    public ResponseResult deleteRecruitPosition(@PathVariable Long id) {
        return oaServiceFeign.deleteRecruitPosition(id);
    }

    /** ==================== 薪资管理接口 ==================== */

    @PostMapping("/salary/setting/page")
    public ResponseResult getSalarySettingPage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getSalarySettingPage(params);
    }

    @PostMapping("/salary/setting/detail/{id}")
    public ResponseResult getSalarySettingById(@PathVariable Integer id) {
        return oaServiceFeign.getSalarySettingById(id);
    }

    @PostMapping("/salary/setting")
    public ResponseResult saveSalarySetting(@RequestBody Map<String, Object> salaryData) {
        return oaServiceFeign.saveSalarySetting(salaryData);
    }

    @PostMapping("/salary/setting/update/{id}")
    public ResponseResult updateSalarySetting(@PathVariable Integer id, @RequestBody Map<String, Object> salaryData) {
        return oaServiceFeign.updateSalarySetting(id, salaryData);
    }

    @PostMapping("/salary/setting/delete/{id}")
    public ResponseResult deleteSalarySetting(@PathVariable Integer id) {
        return oaServiceFeign.deleteSalarySetting(id);
    }

    @PostMapping("/salary/setting/employee/{employeeId}")
    public ResponseResult getSalarySettingByEmployeeId(@PathVariable Integer employeeId) {
        return oaServiceFeign.getSalarySettingByEmployeeId(employeeId);
    }

    @PostMapping("/salary/setting/batch")
    public ResponseResult batchSaveSalarySetting(@RequestBody java.util.List<Map<String, Object>> salaryList) {
        return oaServiceFeign.batchSaveSalarySetting(salaryList);
    }

    @PostMapping("/salary/statistics/summary")
    public ResponseResult getSalarySummary(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getSalarySummary(params);
    }

    @PostMapping("/salary/statistics/department")
    public ResponseResult getDepartmentStatistics(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getDepartmentStatistics(params);
    }

    @PostMapping("/salary/statistics/individual")
    public ResponseResult getIndividualStatistics(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getIndividualStatistics(params);
    }

    @PostMapping("/salary/statistics/comparison")
    public ResponseResult getSalaryComparison(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getSalaryComparison(params);
    }

    @PostMapping("/salary/statistics/export")
    public ResponseResult exportSalaryStatistics(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.exportSalaryStatistics(params);
    }

    @PostMapping("/salary/payment/page")
    public ResponseResult getPayslipPage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getPayslipPage(params);
    }

    @PostMapping("/salary/payment/{id}")
    public ResponseResult getPayslipDetail(@PathVariable Integer id) {
        return oaServiceFeign.getPayslipDetail(id);
    }

    @PostMapping("/salary/payment/generate")
    public ResponseResult generatePayslip(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.generatePayslip(params);
    }

    @PostMapping("/salary/payment/batch-send")
    public ResponseResult batchSendPayslip(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.batchSendPayslip(params);
    }

    @PostMapping("/salary/payment/send/{id}")
    public ResponseResult sendPayslip(@PathVariable Integer id) {
        return oaServiceFeign.sendPayslip(id);
    }

    @PostMapping("/salary/payment/resend/{id}")
    public ResponseResult resendPayslip(@PathVariable Integer id) {
        return oaServiceFeign.resendPayslip(id);
    }

    @PostMapping("/salary/payment/download/{id}")
    public ResponseResult downloadPayslip(@PathVariable Integer id) {
        return oaServiceFeign.downloadPayslip(id);
    }

    @PostMapping("/salary/payment/export")
    public ResponseResult exportPayslip(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.exportPayslip(params);
    }

    @PostMapping("/salary/payslip/page")
    public ResponseResult getSalaryPayslipManagePage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getSalaryPayslipManagePage(params);
    }

    @PostMapping("/salary/payslip/detail/{id}")
    public ResponseResult getSalaryPayslipManageDetail(@PathVariable Long id) {
        return oaServiceFeign.getSalaryPayslipManageDetail(id);
    }

    @PostMapping("/salary/payslip/payment/{id}")
    public ResponseResult recordSalaryPayslipPayment(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> params) {
        return oaServiceFeign.recordSalaryPayslipPayment(
                id, params != null ? params : new java.util.HashMap<>());
    }

    @PostMapping("/salary/payslip/payment-records/{id}")
    public ResponseResult getSalaryPayslipPaymentRecords(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> params) {
        return oaServiceFeign.getSalaryPayslipPaymentRecords(
                id, params != null ? params : new java.util.HashMap<>());
    }

    @PostMapping("/salary/payslip/queue/{id}")
    public ResponseResult queueSalaryPayslipManage(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> params) {
        return oaServiceFeign.queueSalaryPayslipManage(id, params != null ? params : new java.util.HashMap<>());
    }

    @PostMapping("/salary/payslip/send/{id}")
    public ResponseResult sendSalaryPayslipManage(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> params) {
        return oaServiceFeign.sendSalaryPayslipManage(id, params != null ? params : new java.util.HashMap<>());
    }

    @PostMapping("/salary/payslip/resend/{id}")
    public ResponseResult resendSalaryPayslipManage(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> params) {
        return oaServiceFeign.resendSalaryPayslipManage(id, params != null ? params : new java.util.HashMap<>());
    }

    @PostMapping("/salary/payslip/batch-send")
    public ResponseResult batchSendSalaryPayslipManage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.batchSendSalaryPayslipManage(params);
    }

    @PostMapping("/salary/structure/page")
    public ResponseResult getSalaryStructurePage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getSalaryStructurePage(params);
    }

    @PostMapping("/salary/structure/detail/{id}")
    public ResponseResult getSalaryStructureById(@PathVariable Integer id) {
        return oaServiceFeign.getSalaryStructureById(id);
    }

    @PostMapping("/salary/structure")
    public ResponseResult saveSalaryStructure(@RequestBody Map<String, Object> structureData) {
        return oaServiceFeign.saveSalaryStructure(structureData);
    }

    @PostMapping("/salary/structure/update/{id}")
    public ResponseResult updateSalaryStructure(@PathVariable Integer id, @RequestBody Map<String, Object> structureData) {
        return oaServiceFeign.updateSalaryStructure(id, structureData);
    }

    @PostMapping("/salary/structure/delete/{id}")
    public ResponseResult deleteSalaryStructure(@PathVariable Integer id) {
        return oaServiceFeign.deleteSalaryStructure(id);
    }

    @PostMapping("/salary/structure/types")
    public ResponseResult getSalaryStructureTypes() {
        return oaServiceFeign.getSalaryStructureTypes();
    }

    @PostMapping("/salary/structure/list")
    public ResponseResult getSalaryStructureList() {
        return oaServiceFeign.getSalaryStructureList();
    }

    @PostMapping("/salary/calculate/page")
    public ResponseResult getSalaryCalculatePage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getSalaryCalculatePage(params);
    }

    /**
     * 修复：与 Feign 定义、paas 层 Controller 保持一致，使用 GET 方法
/**
     * 原 @PostMapping 会导致前端 GET /oa/salary/calculate/{id} 返回 405 Method Not Allowed
     */
    @GetMapping("/salary/calculate/{id}")
    public ResponseResult getSalaryCalculateDetail(@PathVariable Integer id) {
        return oaServiceFeign.getSalaryCalculateDetail(id);
    }

    @PostMapping("/salary/calculate/employees/page")
    public ResponseResult salaryCalculateEmployeesPage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.salaryCalculateEmployeesPage(params);
    }

    @PostMapping("/salary/calculate/rules")
    public ResponseResult listSalaryCalculateRules() {
        return oaServiceFeign.listSalaryCalculateRules();
    }

    @PostMapping("/salary/calculate/preview")
    public ResponseResult previewSalaryCalculation(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.previewSalaryCalculation(params);
    }

    @PostMapping("/salary/calculate/confirm")
    public ResponseResult confirmSalaryCalculation(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.confirmSalaryCalculation(params);
    }

    @PostMapping("/salary/calculate/batch")
    public ResponseResult batchCalculate(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.batchCalculate(params);
    }

    @PostMapping("/salary/calculate/recalculate/{id}")
    public ResponseResult recalculate(@PathVariable Integer id) {
        return oaServiceFeign.recalculate(id);
    }

    @PostMapping("/salary/calculate/validate")
    public ResponseResult validateCalculation(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.validateCalculation(params);
    }

    @PostMapping("/salary/calculate/history")
    public ResponseResult getCalculationHistory(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getCalculationHistory(params);
    }

    @PostMapping("/salary/calculate/export")
    public ResponseResult exportCalculation(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.exportCalculation(params);
    }

    @PostMapping("/salary/calculate/sync-employee-salary-from-payroll")
    public ResponseResult syncEmployeeSalaryFromPayroll(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.syncEmployeeSalaryFromPayroll(params);
    }

    // ==================== 日报扣款规则代理 ====================

    /**
     * 日报扣款结果分页
/**
     * @param params page/limit/salaryMonth/employeeName/tier
     * @return 分页结果
     */
    @PostMapping("/report-deduction/page")
    public ResponseResult getReportDeductionPage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getReportDeductionPage(params);
    }

    /**
     * 日报扣款详情
/**
     * @param id 主键
     * @return 详情
     */
    @GetMapping("/report-deduction/{id}")
    public ResponseResult getReportDeductionDetail(@PathVariable Long id) {
        return oaServiceFeign.getReportDeductionDetail(id);
    }

    /**
     * 一键计算某月扣款
/**
     * @param params { salaryMonth, employeeIds }
     * @return 计算后的扣款记录
     */
    @PostMapping("/report-deduction/compute")
    public ResponseResult computeReportDeduction(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.computeReportDeduction(params);
    }

    /**
     * 锁定扣款记录（写入薪资条后不允许重算）
/**
     * @param id 主键
     * @return 锁定结果
     */
    @PostMapping("/report-deduction/lock/{id}")
    public ResponseResult lockReportDeduction(@PathVariable Long id) {
        return oaServiceFeign.lockReportDeduction(id);
    }

    /**
     * 申诉状态更新
/**
     * @param id 主键
     * @param params { appealStatus, appealApprovalId }
     * @return 更新结果
     */
    @PostMapping("/report-deduction/appeal/{id}")
    public ResponseResult updateReportDeductionAppeal(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        return oaServiceFeign.updateReportDeductionAppeal(id, params);
    }

    /**
     * 某员工某月的日报提交流水
/**
     * @param params employeeId/salaryMonth
     * @return 流水列表
     */
    @PostMapping("/report-deduction/submit-log/list")
    public ResponseResult getReportSubmitLogList(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getReportSubmitLogList(params);
    }

    /** ==================== 节假日配置代理 ==================== */

    @PostMapping("/holiday-config/page")
    public ResponseResult getHolidayConfigPage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getHolidayConfigPage(params);
    }

    @GetMapping("/holiday-config/month/{month}")
    public ResponseResult getHolidayConfigByMonth(@PathVariable String month) {
        return oaServiceFeign.getHolidayConfigByMonth(month);
    }

    @PostMapping("/holiday-config/save")
    public ResponseResult saveHolidayConfig(@RequestBody Map<String, Object> entity) {
        return oaServiceFeign.saveHolidayConfig(entity);
    }

    @PostMapping("/holiday-config/delete/{id}")
    public ResponseResult deleteHolidayConfig(@PathVariable Long id) {
        return oaServiceFeign.deleteHolidayConfig(id);
    }

    /** ==================== 请假申请代理 ==================== */

    @PostMapping("/leave-apply/page")
    public ResponseResult getLeaveApplyPage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getLeaveApplyPage(params);
    }

    @PostMapping("/leave-apply/save")
    public ResponseResult saveLeaveApply(@RequestBody Map<String, Object> entity) {
        return oaServiceFeign.saveLeaveApply(entity);
    }

    @PostMapping("/leave-apply/approve/{id}")
    public ResponseResult approveLeaveApply(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        return oaServiceFeign.approveLeaveApply(id, params);
    }

    @PostMapping("/leave-apply/delete/{id}")
    public ResponseResult deleteLeaveApply(@PathVariable Long id) {
        return oaServiceFeign.deleteLeaveApply(id);
    }

    /** ==================== 薪资规则管理 ==================== */

    @PostMapping("/salary/rules/page")
    public ResponseResult getRulesPage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getRulesPage(params);
    }

    @PostMapping("/salary/rules/detail/{id}")
    public ResponseResult getRuleById(@PathVariable Integer id) {
        return oaServiceFeign.getRuleById(id);
    }

    @PostMapping("/salary/rules/save")
    public ResponseResult saveRule(@RequestBody Map<String, Object> rule) {
        return oaServiceFeign.saveRule(rule);
    }

    @PostMapping("/salary/rules/update/{id}")
    public ResponseResult updateRule(@PathVariable Integer id, @RequestBody Map<String, Object> rule) {
        return oaServiceFeign.updateRule(id, rule);
    }

    @PostMapping("/salary/rules/delete/{id}")
    public ResponseResult deleteRule(@PathVariable Integer id) {
        return oaServiceFeign.deleteRule(id);
    }

    @PostMapping("/salary/rules/status/{id}")
    public ResponseResult updateRuleStatus(@PathVariable Integer id, @RequestBody Map<String, Object> params) {
        return oaServiceFeign.updateRuleStatus(id, params);
    }

    @PostMapping("/salary/rules/batch-delete")
    public ResponseResult batchDeleteRules(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.batchDeleteRules(params);
    }

    @PostMapping("/salary/rules/ensure-meal")
    public ResponseResult ensureMealRule() {
        return oaServiceFeign.ensureMealRule();
    }

    @PostMapping("/salary/rules/ensure-tax")
    public ResponseResult ensureTaxRule() {
        return oaServiceFeign.ensureTaxRule();
    }

    @PostMapping("/salary/rules/ensure-no-project-acceptance")
    public ResponseResult ensureNoProjectAcceptanceRule() {
        return oaServiceFeign.ensureNoProjectAcceptanceRule();
    }

    @PostMapping("/salary/rules/no-project-acceptance/config")
    public ResponseResult getNoProjectAcceptanceRuleConfig() {
        return oaServiceFeign.getNoProjectAcceptanceRuleConfig();
    }

    @PostMapping("/salary/rules/no-project-acceptance/save")
    public ResponseResult saveNoProjectAcceptanceRuleConfig(@RequestBody Map<String, Object> rule) {
        return oaServiceFeign.saveNoProjectAcceptanceRuleConfig(rule);
    }

    /** ==================== 联系人管理接�?==================== */

    @PostMapping("/contact/list")
    public ResponseResult getContactList(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getContactListPage(params);
    }

    @PostMapping("/contact/list/{id}")
    public ResponseResult getContactDetail(@PathVariable Integer id) {
        return oaServiceFeign.getContactDetail(id);
    }

    @PostMapping("/contact/list/save")
    public ResponseResult saveContact(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.saveContact(params);
    }

    @PostMapping("/contact/list/update")
    public ResponseResult updateContact(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.updateContact(params);
    }

    @PostMapping("/contact/list/delete/{id}")
    public ResponseResult deleteContact(@PathVariable Integer id) {
        return oaServiceFeign.deleteContact(id);
    }

    @PostMapping("/contact/list/batch-delete")
    public ResponseResult batchDeleteContacts(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.batchDeleteContacts(params);
    }

    @PostMapping("/contact/list/search")
    public ResponseResult searchContacts(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.searchContacts(params);
    }

    @PostMapping("/contact/list/import")
    public ResponseResult importContacts(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.importContacts(params);
    }

    @PostMapping("/contact/list/export")
    public ResponseResult exportContacts(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.exportContacts(params);
    }

    /** ==================== 待办事项接口 ==================== */

    @PostMapping("/todo/list")
    public ResponseResult getTodoList(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getTodoList(params);
    }

    @GetMapping("/todo/counts")
    public ResponseResult getTodoCounts() {
        return oaServiceFeign.getTodoCounts();
    }

    @PostMapping("/todo/updateStatus")
    public ResponseResult updateTodoStatus(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.updateTodoStatus(params);
    }

    @PostMapping("/todo/create")
    public ResponseResult createTodo(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.createTodo(params);
    }

    @PostMapping("/todo/update")
    public ResponseResult updateTodo(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.updateTodo(params);
    }

    @PostMapping("/todo/delete")
    public ResponseResult deleteTodo(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.deleteTodo(params);
    }

    @GetMapping("/todo/daily/sync-info")
    public ResponseResult todoDailySyncInfo() {
        return oaServiceFeign.todoDailySyncInfo();
    }

    @PostMapping("/todo/daily/sync-token/reset")
    public ResponseResult todoDailySyncTokenReset() {
        return oaServiceFeign.todoDailySyncTokenReset();
    }

    @GetMapping("/todo/daily/ics/{token}")
    public void todoDailyIcs(@PathVariable("token") String token, jakarta.servlet.http.HttpServletResponse response) {
        try {
            String normalized = token != null && token.endsWith(".ics")
                    ? token.substring(0, token.length() - 4) : token;
            org.springframework.http.ResponseEntity<byte[]> result = oaServiceFeign.todoDailyIcs(normalized);
            if (result == null || result.getBody() == null) {
                response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.setStatus(result.getStatusCode().value());
            response.setContentType("text/calendar;charset=UTF-8");
            response.setHeader("Content-Disposition", "inline; filename=\"houcloud-daily-todo.ics\"");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.getOutputStream().write(result.getBody());
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("转发每日待办 ICS 失败 token={}", token, e);
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /** ==================== 首页临时记事本接口 ==================== */

    @PostMapping("/quicknote/list")
    public ResponseResult getQuickNoteList(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getQuickNoteList(params);
    }

    @PostMapping("/quicknote/create")
    public ResponseResult createQuickNote(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.createQuickNote(params);
    }

    @PostMapping("/quicknote/delete")
    public ResponseResult deleteQuickNote(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.deleteQuickNote(params);
    }

    /** ==================== 费用报销接口 ==================== */

    @PostMapping("/expense/category/list")
    public ResponseResult getExpenseCategoryList() {
        return oaServiceFeign.getExpenseCategoryList();
    }

    @PostMapping("/expense/category/{id}")
    public ResponseResult getExpenseCategoryById(@PathVariable Integer id) {
        return oaServiceFeign.getExpenseCategoryById(id);
    }

    @PostMapping("/expense/category/add")
    public ResponseResult addExpenseCategory(@RequestBody Map<String, Object> category) {
        return oaServiceFeign.addExpenseCategory(category);
    }

    @PostMapping("/expense/category/update")
    public ResponseResult updateExpenseCategory(@RequestBody Map<String, Object> category) {
        return oaServiceFeign.updateExpenseCategory(category);
    }

    @PostMapping("/expense/category/delete/{id}")
    public ResponseResult deleteExpenseCategory(@PathVariable Integer id) {
        return oaServiceFeign.deleteExpenseCategory(id);
    }

    @PostMapping("/expense/application/list")
    public ResponseResult getExpenseApplicationList() {
        return oaServiceFeign.getExpenseApplicationList();
    }

    @PostMapping("/expense/application/{id}")
    public ResponseResult getExpenseApplicationById(@PathVariable Integer id) {
        return oaServiceFeign.getExpenseApplicationById(id);
    }

    @PostMapping("/expense/application/add")
    public ResponseResult addExpenseApplication(@RequestBody Map<String, Object> application) {
        return oaServiceFeign.addExpenseApplication(application);
    }

    @PostMapping("/expense/application/update")
    public ResponseResult updateExpenseApplication(@RequestBody Map<String, Object> application) {
        return oaServiceFeign.updateExpenseApplication(application);
    }

    @PostMapping("/expense/application/delete/{id}")
    public ResponseResult deleteExpenseApplication(@PathVariable Integer id) {
        return oaServiceFeign.deleteExpenseApplication(id);
    }

    // ==================== 工作台菜单接�?====================

    /**
     * 获取工作台菜单数�?
/**
     * 动态调�?docs/sql/permission.sql �?pid=260 的内�?
     */
    @PostMapping("/workbench/menus")
    public ResponseResult getWorkbenchMenus() {
        return oaServiceFeign.getWorkbenchMenus();
    }

    /**
     * 获取工作台子菜单数据
/**
     * 获取工作台主菜单(pid=260)下的所有子菜单
     */
    @RequestMapping(value = "/workbench/submenus", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult getWorkbenchSubMenus() {
        return oaServiceFeign.getWorkbenchSubMenus();
    }

    /**
     * 获取工作台统计数�?
/**
     * 获取工作台相关的统计信息
     */
    @PostMapping("/workbench/stats")
    public ResponseResult getWorkbenchStats() {
        return oaServiceFeign.getWorkbenchStats();
    }

    /**
     * 获取审批菜单数据
/**
     * 动态调取审批相关的菜单数据
     */
    @PostMapping("/workbench/approval/menus")
    public ResponseResult getApprovalMenus() {
        return oaServiceFeign.getApprovalMenus();
    }

    /** ==================== 薪资模块完善 新路由 ==================== */

    @PostMapping("/salary/item/page")
    public ResponseResult salaryItemPage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.salaryItemPage(params);
    }

    @PostMapping("/salary/item/list")
    public ResponseResult salaryItemList(@RequestBody(required = false) Map<String, Object> params) {
        String category = params != null ? (String) params.get("category") : null;
        return oaServiceFeign.salaryItemList(category);
    }

    @PostMapping("/salary/item/save")
    public ResponseResult salaryItemSave(@RequestBody Map<String, Object> item) {
        return oaServiceFeign.salaryItemSave(item);
    }

    @PostMapping("/salary/item/update/{id}")
    public ResponseResult salaryItemUpdate(@PathVariable Integer id, @RequestBody Map<String, Object> item) {
        return oaServiceFeign.salaryItemUpdate(id, item);
    }

    @PostMapping("/salary/item/delete/{id}")
    public ResponseResult salaryItemDelete(@PathVariable Integer id) {
        return oaServiceFeign.salaryItemDelete(id);
    }

    @PostMapping("/salary/item/detail/{id}")
    public ResponseResult salaryItemDetail(@PathVariable Integer id) {
        return oaServiceFeign.salaryItemDetail(id);
    }

    /**
     * 核算参数（月计薪天数）。生产 oa 旧包无 /calc-params 时 Feign GET 会误命中 /{id}，故走 list 回退。
     */
    @PostMapping("/salary/item/calc-params/get")
    public ResponseResult salaryItemCalcParamsGet() {
        try {
            ResponseResult feignRes = oaServiceFeign.salaryItemCalcParamsGet();
            if (isFeignSuccess(feignRes) && feignRes.getData() != null) {
                return feignRes;
            }
        } catch (Exception e) {
            log.debug("oa calc-params 直连不可用，使用 list 回退: {}", e.getMessage());
        }
        return ResponseResult.success(buildCalcParamsFromSalaryItemList(oaServiceFeign.salaryItemList(null)));
    }

    @PostMapping("/salary/item/calc-params/save")
    public ResponseResult salaryItemCalcParamsSave(@RequestBody Map<String, Object> body) {
        try {
            ResponseResult feignRes = oaServiceFeign.salaryItemCalcParamsSave(body);
            if (isFeignSuccess(feignRes)) {
                return feignRes;
            }
        } catch (Exception e) {
            log.debug("oa calc-params 保存直连不可用，使用 item 回退: {}", e.getMessage());
        }
        return saveCalcParamsViaSalaryItemList(body);
    }

    private static boolean isFeignSuccess(ResponseResult res) {
        if (res == null) {
            return false;
        }
        int code = res.getCode();
        return code == 1 || code == 200 || code == 0;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildCalcParamsFromSalaryItemList(ResponseResult listRes) {
        Map<String, Object> params = new HashMap<>();
        params.put("monthlyWorkDays", 21.75);
        params.put("id", null);
        if (!isFeignSuccess(listRes) || !(listRes.getData() instanceof List)) {
            return params;
        }
        for (Object row : (List<?>) listRes.getData()) {
            if (!(row instanceof Map)) {
                continue;
            }
            Map<String, Object> m = (Map<String, Object>) row;
            if (!"MONTHLY_WORK_DAYS".equals(String.valueOf(m.get("itemCode")))) {
                continue;
            }
            params.put("id", m.get("id"));
            Object dv = m.get("defaultValue");
            if (dv != null && !String.valueOf(dv).trim().isEmpty()) {
                try {
                    params.put("monthlyWorkDays", new BigDecimal(String.valueOf(dv).trim()).doubleValue());
                } catch (NumberFormatException ignored) {
                    // keep default
                }
            }
            break;
        }
        return params;
    }

    @SuppressWarnings("unchecked")
    private ResponseResult saveCalcParamsViaSalaryItemList(Map<String, Object> body) {
        Object raw = body != null ? body.get("monthlyWorkDays") : null;
        if (raw == null || raw.toString().trim().isEmpty()) {
            return ResponseResult.fail(400, "请填写月计薪天数");
        }
        String value;
        try {
            BigDecimal days = new BigDecimal(raw.toString().trim());
            if (days.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseResult.fail(400, "月计薪天数必须大于 0");
            }
            value = days.stripTrailingZeros().toPlainString();
        } catch (NumberFormatException e) {
            return ResponseResult.fail(400, "月计薪天数格式不正确");
        }

        ResponseResult listRes = oaServiceFeign.salaryItemList(null);
        Integer existingId = null;
        if (isFeignSuccess(listRes) && listRes.getData() instanceof List) {
            for (Object row : (List<?>) listRes.getData()) {
                if (!(row instanceof Map)) {
                    continue;
                }
                Map<String, Object> m = (Map<String, Object>) row;
                if ("MONTHLY_WORK_DAYS".equals(String.valueOf(m.get("itemCode")))) {
                    Object idObj = m.get("id");
                    if (idObj instanceof Number) {
                        existingId = ((Number) idObj).intValue();
                    } else if (idObj != null) {
                        existingId = Integer.parseInt(idObj.toString());
                    }
                    break;
                }
            }
        }

        Map<String, Object> item = new HashMap<>();
        item.put("itemCode", "MONTHLY_WORK_DAYS");
        item.put("itemName", "月计薪天数");
        item.put("category", "stat");
        item.put("calculationType", 1);
        item.put("defaultValue", value);
        item.put("isTaxable", 0);
        item.put("isDisplay", 0);
        item.put("displayOrder", 0);
        item.put("status", 1);
        item.put("remark", "用于出勤比例与请假日薪：实发基本工资=全额×min(出勤天数/月计薪天数,1)");

        if (existingId != null) {
            return oaServiceFeign.salaryItemUpdate(existingId, item);
        }
        return oaServiceFeign.salaryItemSave(item);
    }

    @PostMapping("/salary/template/page")
    public ResponseResult salaryTemplatePage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.salaryTemplatePage(params);
    }

    @PostMapping("/salary/template/by-employee/{employeeId}")
    public ResponseResult salaryTemplateByEmployee(@PathVariable Integer employeeId) {
        return oaServiceFeign.salaryTemplateByEmployee(employeeId);
    }

    @PostMapping("/salary/template/save")
    public ResponseResult salaryTemplateSave(@RequestBody Map<String, Object> entity) {
        return oaServiceFeign.salaryTemplateSave(entity);
    }

    @PostMapping("/salary/template/update/{id}")
    public ResponseResult salaryTemplateUpdate(@PathVariable Long id, @RequestBody Map<String, Object> entity) {
        return oaServiceFeign.salaryTemplateUpdate(id, entity);
    }

    @PostMapping("/salary/template/delete/{id}")
    public ResponseResult salaryTemplateDelete(@PathVariable Long id) {
        return oaServiceFeign.salaryTemplateDelete(id);
    }

    @PostMapping("/salary/template/batch-save")
    public ResponseResult salaryTemplateBatchSave(@RequestBody Map<String, Object> payload) {
        return oaServiceFeign.salaryTemplateBatchSave(payload);
    }

    @PostMapping("/salary/payroll/page")
    public ResponseResult salaryPayrollPage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.salaryPayrollPage(params);
    }

    @PostMapping("/salary/payroll/detail/{id}")
    public ResponseResult salaryPayrollDetail(@PathVariable Long id) {
        return oaServiceFeign.salaryPayrollDetail(id);
    }

    @PostMapping("/salary/payroll/delete/{id}")
    public ResponseResult salaryPayrollDelete(@PathVariable Long id) {
        return oaServiceFeign.salaryPayrollDelete(id);
    }

    @PostMapping("/salary/payroll/summary")
    public ResponseResult salaryPayrollSummary(@RequestBody(required = false) Map<String, Object> params) {
        String month = params != null ? (String) params.get("salaryMonth") : null;
        String orgId = params != null ? (String) params.get("orgId") : null;
        return oaServiceFeign.salaryPayrollSummary(month, orgId);
    }

    @PostMapping("/salary/payroll/months")
    public ResponseResult salaryPayrollMonths(@RequestBody(required = false) Map<String, Object> params) {
        String orgId = params != null ? (String) params.get("orgId") : null;
        return oaServiceFeign.salaryPayrollMonths(orgId);
    }

    /**
     * Excel 导入 —— 表单上传
     */
    @PostMapping(value = "/salary/import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseResult salaryImportExcel(@RequestPart("file") MultipartFile[] files,
                                            @RequestParam(value = "salaryMonth", required = false, defaultValue = "") String salaryMonth,
                                            @RequestParam(value = "overwrite", defaultValue = "false") Boolean overwrite,
                                            @RequestParam(value = "orgId", required = false) String orgId,
                                            HttpServletRequest request) {
        if (files == null || files.length == 0) {
            return ResponseResult.fail(400, "请选择工资文件");
        }
        String month = (salaryMonth != null && !salaryMonth.isBlank()) ? salaryMonth.trim() : null;
        if (files.length == 1) {
            return oaServiceFeign.salaryImportExcel(files[0], month, overwrite, orgId);
        }
        return proxySalaryImportMultipart(files, month, overwrite, orgId, request);
    }

    @PostMapping("/salary/import/status/{jobId}")
    public ResponseResult salaryImportJobStatus(@PathVariable Long jobId,
                                                @RequestParam(value = "orgId", required = false) String orgId) {
        return oaServiceFeign.salaryImportJobStatus(jobId, orgId);
    }

    @PostMapping("/salary/import/confirm/{jobId}")
    public ResponseResult salaryImportConfirmOverwrite(@PathVariable Long jobId,
                                                       @RequestParam(value = "orgId", required = false) String orgId) {
        return oaServiceFeign.salaryImportConfirmOverwrite(jobId, orgId);
    }

    // ==================== 薪资透视（Excel 横表） ====================

    /**
     * 获取薪资横表列定义（GET/POST 均可）
     */
    @RequestMapping(value = "/salary/pivot/columns", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult salaryPivotColumns() {
        return oaServiceFeign.salaryPivotColumns();
    }

    /**
     * 按月查询员工薪资横表
/**
     * POST body 或 GET query：month、keyword、orgId
     */
    @RequestMapping(value = "/salary/pivot/query", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult salaryPivotQuery(@RequestBody(required = false) Map<String, Object> body,
                                           @RequestParam(value = "month", required = false) String month,
                                           @RequestParam(value = "keyword", required = false) String keyword,
                                           @RequestParam(value = "orgId", required = false) String orgId) {
        Map<String, Object> params = body != null ? new HashMap<>(body) : new HashMap<>();
        if (month != null && !month.isBlank()) {
            params.put("month", month);
        }
        if (keyword != null) {
            params.put("keyword", keyword);
        }
        if (orgId != null && !orgId.isBlank()) {
            params.put("orgId", orgId);
        }
        return oaServiceFeign.salaryPivotQuery(params);
    }

    /**
     * 导出整张工资表 xlsx
     */
    @PostMapping("/salary/pivot/export")
    public void salaryPivotExport(@RequestBody(required = false) Map<String, Object> params,
                                  HttpServletResponse response) {
        try {
            Map<String, Object> body = params != null ? params : new HashMap<>();
            org.springframework.http.ResponseEntity<byte[]> result = oaServiceFeign.salaryPivotExport(body);
            if (result == null || result.getBody() == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":-1,\"message\":\"导出失败，数据为空\"}");
                return;
            }
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            org.springframework.http.HttpHeaders headers = result.getHeaders();
            String contentDisposition = headers.getFirst("Content-Disposition");
            if (contentDisposition != null) {
                response.setHeader("Content-Disposition", contentDisposition);
            } else {
                String month = body.get("month") != null ? String.valueOf(body.get("month")) : "";
                String rawName = buildExportFileName(month);
                String fileName = java.net.URLEncoder.encode(rawName, java.nio.charset.StandardCharsets.UTF_8);
                response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            }
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.getOutputStream().write(result.getBody());
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("导出薪资横表 BFF 转发失败", e);
            try {
                if (!response.isCommitted()) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":-1,\"message\":\"导出失败: " + e.getMessage() + "\"}");
                }
            } catch (java.io.IOException ioe) {
                log.error("写出错误响应失败", ioe);
            }
        }
    }

    /**
     * 整张工资表邮件发送
     */
    @PostMapping("/salary/pivot/send-mail-all")
    public ResponseResult salaryPivotSendMailAll(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.salaryPivotSendMailAll(params);
    }

    /**
     * 逐人独立工资条邮件
     */
    @PostMapping("/salary/pivot/send-mail-each")
    public ResponseResult salaryPivotSendMailEach(@RequestBody(required = false) Map<String, Object> params) {
        if (params == null) params = new HashMap<>();
        return oaServiceFeign.salaryPivotSendMailEach(params);
    }

    /**
     * 单人刷新薪资行（点击单元格就地重算）
     */
    @PostMapping("/salary/pivot/refresh-row")
    public ResponseResult salaryPivotRefreshRow(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.salaryPivotRefreshRow(params);
    }

    // ==================== 日报接口 ====================

    /**
     * 分页查询日报
     */
    @PostMapping("/dailyreport/page")
    public ResponseResult dailyReportPage(@RequestBody(required = false) Map<String, Object> params) {
        if (params == null) params = new HashMap<>();
        return oaServiceFeign.dailyReportPage(params);
    }

    /**
     * 保存日报（新增或更新）
     */
    @PostMapping("/dailyreport/save")
    public ResponseResult dailyReportSave(@RequestBody Map<String, Object> entity) {
        return oaServiceFeign.dailyReportSave(entity);
    }

    /**
     * 获取日报详情
     */
    @GetMapping("/dailyreport/{id}")
    public ResponseResult dailyReportDetail(@PathVariable Long id) {
        return oaServiceFeign.dailyReportDetail(id);
    }

    /**
     * 删除日报（逻辑删除）
     */
    @PostMapping("/dailyreport/delete/{id}")
    public ResponseResult dailyReportDelete(@PathVariable Long id) {
        return oaServiceFeign.dailyReportDelete(id);
    }

    private static String buildExportFileName(String month) {
        if (month == null || month.isBlank()) {
            return "工资表待审核.xlsx";
        }
        try {
            return java.time.YearMonth.parse(month.trim()).getMonthValue() + "月工资表待审核.xlsx";
        } catch (Exception e) {
            return month.trim() + "工资表待审核.xlsx";
        }
    }
}
