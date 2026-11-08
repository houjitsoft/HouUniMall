package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.PmServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// 添加缺失的导入语句
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

/**
 * 项目管理BFF适配器
/**
 * 纯转发层，不包含业务逻辑
 * @author Amos
 */
@Slf4j
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/pm")
public class WebPmController {

    @Autowired
    private PmServiceFeign pmServiceFeign;



    /** ================== 菜单管理接口 ================== */
    @PostMapping("/menu/project-menus")
    public ResponseResult getProjectMenus(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getProjectMenus(params);
    }

    /** ================== 项目管理接口 ================== */
    @PostMapping("/project/list")
    public ResponseResult getProjectList(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getProjectList(params);
    }

    @PostMapping("/project/detail")
    public ResponseResult getProjectDetail(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getProjectDetail(params);
    }

    @PostMapping("/project")
    public ResponseResult createProject(@RequestBody Map<String, Object> projectData) {
        log.info("BFF-Web 创建项目 - 收到数据: {}", projectData);
        log.info("BFF-Web 创建项目 - category: {}, priority: {}", 
                projectData.get("category"), projectData.get("priority"));
        return pmServiceFeign.createProject(projectData);
    }

    @PostMapping("/project/update")
    public ResponseResult updateProject(@RequestBody Map<String, Object> projectData) {
        return pmServiceFeign.updateProject(projectData);
    }

    @PostMapping("/project/delete")
    public ResponseResult deleteProject(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.deleteProject(params);
    }

    @PostMapping("/project/generate-number")
    public ResponseResult generateProjectNumber(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.generateProjectNumber(params);
    }

    @PostMapping("/project/enums/status")
    public ResponseResult getProjectStatusEnum(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getProjectStatusEnum(params);
    }

    @PostMapping("/project/enums/category")
    public ResponseResult getProjectCategoryEnum(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getProjectCategoryEnum(params);
    }

    @PostMapping("/project/enums/priority")
    public ResponseResult getPriorityEnum(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getPriorityEnum(params);
    }

    @PostMapping("/project/enums/type")
    public ResponseResult getProjectTypeEnum(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getProjectTypeEnum(params);
    }

    /** ================== 任务管理接口 ================== */
    @PostMapping("/task/list")
    public ResponseResult getTaskList(@RequestBody Map<String, Object> params) {
        String orgId = extractOrgId(params);
        if (orgId == null) {
            return ResponseResult.error("orgId参数是必须的，请传递组织ID");
        }
        params.put("orgId", orgId);
        return pmServiceFeign.getTaskList(params);
    }

    /**
     * 兼容 orgId 为 String/Number，以及 org_id 字段名
     */
    private String extractOrgId(Map<String, Object> params) {
        if (params == null) {
            return null;
        }
        Object value = params.get("orgId");
        if (value == null) {
            value = params.get("org_id");
        }
        if (value == null) {
            return null;
        }
        String orgId = String.valueOf(value).trim();
        return orgId.isEmpty() || "null".equalsIgnoreCase(orgId) ? null : orgId;
    }

    /** 任务列表接口（复数形式，与前端API 调用保持一致） */
    @PostMapping("/tasks/meta")
    public ResponseResult getTaskMeta(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getTaskMeta(params);
    }

    @PostMapping("/tasks/list")
    public ResponseResult getTasksList(@RequestBody Map<String, Object> params) {
        log.info("BFF-Web 收到任务列表请求 /tasks/list - 参数: {}", params);
        ResponseResult result = getTaskList(params);
        log.info("BFF-Web 任务列表响应 - code: {}, message: {}, data: {}", 
                 result != null ? result.getCode() : "null", 
                 result != null ? result.getMessage() : "null",
                 result != null ? result.getData() : "null");
        return result;
    }

    /** 复数路径 /tasks/* 与前端 project.js 保持一致（/task/* 保留兼容） */
    @PostMapping("/tasks/detail")
    public ResponseResult getTasksDetail(@RequestBody Map<String, Object> params) {
        return getTaskDetail(params);
    }

    @PostMapping("/tasks/update")
    public ResponseResult updateTasksPlural(@RequestBody Map<String, Object> taskData) {
        return updateTask(taskData);
    }

    @PostMapping("/tasks/delete")
    public ResponseResult deleteTasksPlural(@RequestBody Map<String, Object> params) {
        return deleteTask(params);
    }

    @PostMapping("/tasks/complete")
    public ResponseResult completeTasksPlural(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.completeTask(params);
    }

    @PostMapping("/tasks/status/update")
    public ResponseResult updateTasksStatusPlural(@RequestBody Map<String, Object> params) {
        return updateTaskStatus(params);
    }

    @PostMapping("/tasks/progress/update")
    public ResponseResult updateTasksProgressPlural(@RequestBody Map<String, Object> params) {
        return updateTaskProgress(params);
    }

    @PostMapping("/tasks/generate-number")
    public ResponseResult generateTasksNumberPlural(@RequestBody Map<String, Object> params) {
        return generateTaskNumber(params);
    }

    @PostMapping("/tasks/batch-delete")
    public ResponseResult batchDeleteTasksPlural(@RequestBody Map<String, Object> params) {
        return batchDeleteTasks(params);
    }

    @PostMapping("/tasks/batch-status")
    public ResponseResult batchUpdateTasksStatusPlural(@RequestBody Map<String, Object> params) {
        return batchUpdateTaskStatus(params);
    }

    @PostMapping("/tasks/batch-assign")
    public ResponseResult batchAssignTasksPlural(@RequestBody Map<String, Object> params) {
        return batchAssignTasks(params);
    }

    @PostMapping("/tasks/batch-progress")
    public ResponseResult batchUpdateTasksProgressPlural(@RequestBody Map<String, Object> params) {
        return batchUpdateTaskProgress(params);
    }

    @PostMapping("/tasks/sort")
    public ResponseResult updateTasksSortPlural(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.updateTaskSort(params);
    }

    @PostMapping("/tasks/comment/list")
    public ResponseResult getTasksCommentsPlural(@RequestBody Map<String, Object> params) {
        return getTaskComments(params);
    }

    @PostMapping("/tasks/comment/add")
    public ResponseResult addTasksCommentPlural(@RequestBody Map<String, Object> commentData) {
        return addTaskComment(commentData);
    }

    @PostMapping("/tasks/attachment/list")
    public ResponseResult getTasksAttachmentsPlural(@RequestBody Map<String, Object> params) {
        return getTaskAttachments(params);
    }

    @PostMapping("/tasks/attachment/upload")
    public ResponseResult uploadTasksAttachmentPlural(@RequestBody Map<String, Object> attachmentData) {
        return uploadTaskAttachment(attachmentData);
    }

    @PostMapping("/tasks/import")
    public ResponseResult importTasksPlural(@RequestParam("file") MultipartFile file) {
        return importTasks(file);
    }

    @GetMapping("/tasks/export")
    public void exportTasksPlural(@RequestParam Map<String, Object> params, HttpServletResponse response) {
        exportTasks(params, response);
    }

    @GetMapping("/tasks/template")
    public void downloadTasksTemplatePlural(HttpServletResponse response) {
        downloadTemplate(response);
    }

    @PostMapping("/task/detail")
    public ResponseResult getTaskDetail(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getTaskDetail(params);
    }

    @PostMapping("/task")
    public ResponseResult createTask(@RequestBody Map<String, Object> taskData) {
        return pmServiceFeign.createTask(taskData);
    }

    /** 创建任务接口（复数形式，与前端API调用保持一致） */
    @PostMapping("/tasks")
    public ResponseResult createTaskPlural(@RequestBody Map<String, Object> taskData) {
        return pmServiceFeign.createTask(taskData);
    }

    @PostMapping("/task/update")
    public ResponseResult updateTask(@RequestBody Map<String, Object> taskData) {
        return pmServiceFeign.updateTask(taskData);
    }

    @PostMapping("/task/delete")
    public ResponseResult deleteTask(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.deleteTask(params);
    }

    @PostMapping("/task/complete")
    public ResponseResult completeTask(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.completeTask(params);
    }

    @PostMapping("/task/status/update")
    public ResponseResult updateTaskStatus(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.updateTaskStatus(params);
    }

    @PostMapping("/task/progress/update")
    public ResponseResult updateTaskProgress(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.updateTaskProgress(params);
    }

    @PostMapping("/task/generate-number")
    public ResponseResult generateTaskNumber(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.generateTaskNumber(params);
    }

    /** ================== 项目成员管理接口 ================== */
    @PostMapping("/project/member/list")
    public ResponseResult getProjectMembers(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getProjectMembers(params);
    }

    @PostMapping("/project/member/add")
    public ResponseResult addProjectMember(@RequestBody Map<String, Object> memberData) {
        return pmServiceFeign.addProjectMember(memberData);
    }

    @PostMapping("/project/member/remove")
    public ResponseResult removeProjectMember(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.removeProjectMember(params);
    }

    @PostMapping("/project/member/role/update")
    public ResponseResult updateMemberRole(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.updateMemberRole(params);
    }

    /** ================== 统计报表接口 ================== */
    @GetMapping("/statistics/overview")
    public ResponseResult getStatisticsOverview(@RequestParam Map<String, Object> params) {
        try {
            return pmServiceFeign.getStatisticsOverview(params);
        } catch (Exception e) {
            log.error("获取项目统计概览失败: {}", e.getMessage());
            return ResponseResult.fail("项目管理服务暂时不可用");
        }
    }

    @PostMapping("/statistics/task")
    public ResponseResult getTaskStatistics(@RequestBody Map<String, Object> params) {
        try {
            return pmServiceFeign.getTaskStatistics(params);
        } catch (Exception e) {
            log.error("获取任务统计失败: {}", e.getMessage());
            return ResponseResult.fail("项目管理服务暂时不可用");
        }
    }

    @GetMapping("/statistics/task/options")
    public ResponseResult getTaskStatisticsOptions(@RequestParam Map<String, Object> params) {
        return pmServiceFeign.getTaskStatisticsOptions(params);
    }

    @GetMapping("/statistics/task/export")
    public void exportTaskStatistics(@RequestParam Map<String, Object> params,
                                     HttpServletResponse servletResponse) throws java.io.IOException {
        feign.Response downstream = pmServiceFeign.exportTaskStatistics(params);
        servletResponse.setStatus(downstream.status());
        copyFirstHeader(downstream, servletResponse, "Content-Type");
        copyFirstHeader(downstream, servletResponse, "Content-Disposition");
        servletResponse.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        if (downstream.body() != null) {
            try (InputStream input = downstream.body().asInputStream()) {
                input.transferTo(servletResponse.getOutputStream());
            }
        }
    }

    private static void copyFirstHeader(feign.Response response,
                                        HttpServletResponse servletResponse,
                                        String headerName) {
        java.util.Collection<String> values = response.headers().get(headerName);
        if (values == null) {
            values = response.headers().get(headerName.toLowerCase());
        }
        if (values != null && !values.isEmpty()) {
            servletResponse.setHeader(headerName, values.iterator().next());
        }
    }

    @PostMapping("/statistics/progress")
    public ResponseResult getProgressReport(@RequestBody Map<String, Object> params) {
        try {
            return pmServiceFeign.getProgressReport(params);
        } catch (Exception e) {
            log.error("获取进度报告失败: {}", e.getMessage());
            return ResponseResult.fail("项目管理服务暂时不可用");
        }
    }

    /** ================== 里程碑管理接口 ================== */
    @PostMapping("/milestone/list")
    public ResponseResult getMilestoneList(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getMilestoneList(params);
    }

    @PostMapping("/milestone/create")
    public ResponseResult createMilestone(@RequestBody Map<String, Object> milestoneData) {
        return pmServiceFeign.createMilestone(milestoneData);
    }

    @PostMapping("/milestone/update")
    public ResponseResult updateMilestone(@RequestBody Map<String, Object> milestoneData) {
        return pmServiceFeign.updateMilestone(milestoneData);
    }

    @PostMapping("/milestone/delete")
    public ResponseResult deleteMilestone(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.deleteMilestone(params);
    }

    /** ================== 甘特图数据接口 ================== */
    @PostMapping("/project/gantt")
    public ResponseResult getProjectGanttData(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getProjectGanttData(params);
    }

    /** ================== 用户相关接口 ================== */
    @PostMapping("/user/projects")
    public ResponseResult getUserProjects(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getUserProjects(params);
    }

    @PostMapping("/user/task-stats")
    public ResponseResult getUserTaskStats(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getUserTaskStats(params);
    }

    /** ================== 批量操作接口 ================== */
    @PostMapping("/task/batch-delete")
    public ResponseResult batchDeleteTasks(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.batchDeleteTasks(params);
    }

    @PostMapping("/task/batch-status")
    public ResponseResult batchUpdateTaskStatus(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.batchUpdateTaskStatus(params);
    }

    @PostMapping("/task/batch-assign")
    public ResponseResult batchAssignTasks(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.batchAssignTasks(params);
    }

    @PostMapping("/task/batch-progress")
    public ResponseResult batchUpdateTaskProgress(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.batchUpdateTaskProgress(params);
    }

    /** ================== 任务评论接口 ================== */
    @PostMapping("/task/comment/list")
    public ResponseResult getTaskComments(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getTaskComments(params);
    }

    @PostMapping("/task/comment/add")
    public ResponseResult addTaskComment(@RequestBody Map<String, Object> commentData) {
        return pmServiceFeign.addTaskComment(commentData);
    }

    /** ================== 任务附件接口 ================== */
    @PostMapping("/task/attachment/list")
    public ResponseResult getTaskAttachments(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getTaskAttachments(params);
    }

    @PostMapping("/task/attachment/upload")
    public ResponseResult uploadTaskAttachment(@RequestBody Map<String, Object> attachmentData) {
        return pmServiceFeign.uploadTaskAttachment(attachmentData);
    }
    
    /** ================== 用户管理接口 ================== */
    @PostMapping("/user/list")
    public ResponseResult getUserList(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getUserList(params);
    }

    /** ================== 导入导出接口 ================== */
    @GetMapping("/task/export")
    public void exportTasks(@RequestParam Map<String, Object> params, HttpServletResponse response) {
        // 直接转发请求到PM服务，不通过Feign接口
        // 这里应该直接调用PM服务的导出接口
        // 由于实现细节复杂，这里简化处理
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=tasks.xlsx");
            response.getWriter().write("Task export functionality needs to be implemented");
        } catch (Exception e) {
            // 异常处理
        }
    }

    @PostMapping("/task/import")
    public ResponseResult importTasks(@RequestParam("file") MultipartFile file) {
        return pmServiceFeign.importTasks(file);
    }

    @GetMapping("/task/template")
    public void downloadTemplate(HttpServletResponse response) {
        // 直接转发请求到PM服务，不通过Feign接口
        // 这里应该直接调用PM服务的模板下载接口
        // 由于实现细节复杂，这里简化处理
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=task_template.xlsx");
            response.getWriter().write("Task template download functionality needs to be implemented");
        } catch (Exception e) {
            // 异常处理
        }
    }

    /** ================== 团队管理接口 ================== */

    @PostMapping("/team/member/list")
    public ResponseResult getTeamMembers(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getTeamMembers(params);
    }

    @PostMapping("/team/member/add")
    public ResponseResult addTeamMember(@RequestBody Map<String, Object> memberData) {
        return pmServiceFeign.addTeamMember(memberData);
    }

    @PostMapping("/team/member/update")
    public ResponseResult updateTeamMember(@RequestBody Map<String, Object> memberData) {
        return pmServiceFeign.updateTeamMember(memberData);
    }

    @PostMapping("/team/member/remove")
    public ResponseResult removeTeamMember(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.removeTeamMember(params);
    }

    @PostMapping("/team/statistics")
    public ResponseResult getTeamStatistics(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getTeamStatistics(params);
    }

    @PostMapping("/team/statistics/detail")
    public ResponseResult getTeamDetailStatistics(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getTeamDetailStatistics(params);
    }

    @PostMapping("/team/member/performance")
    public ResponseResult getTeamMemberPerformance(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getTeamMemberPerformance(params);
    }

    /** ================== 进度跟踪接口 ================== */

    @PostMapping("/progress/overview")
    public ResponseResult getProgressOverview(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getProgressOverview(params);
    }

    @PostMapping("/progress/statistics")
    public ResponseResult getProgressStatistics(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getProgressStatistics(params);
    }

    @PostMapping("/progress/milestone/list")
    public ResponseResult getProgressMilestoneList(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getProgressMilestoneList(params);
    }

    @PostMapping("/progress/milestone/create")
    public ResponseResult createProgressMilestone(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.createProgressMilestone(params);
    }

    @PostMapping("/progress/milestone/update")
    public ResponseResult updateProgressMilestone(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.updateProgressMilestone(params);
    }

    @PostMapping("/progress/milestone/delete")
    public ResponseResult deleteProgressMilestone(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.deleteProgressMilestone(params);
    }

    @PostMapping("/progress/report")
    public ResponseResult getProgressDetailReport(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getProgressDetailReport(params);
    }

    @PostMapping("/progress/chart")
    public ResponseResult getProgressChartData(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getProgressChartData(params);
    }

    @PostMapping("/progress/risk")
    public ResponseResult getProgressRiskData(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getProgressRiskData(params);
    }

    /** ================== 看板管理接口 ================== */

    @PostMapping("/kanban/overview")
    public ResponseResult getKanbanOverview(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getKanbanOverview(params);
    }

    @PostMapping("/kanban/tasks")
    public ResponseResult getKanbanTasks(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getKanbanTasks(params);
    }

    @PostMapping("/kanban/status/statistics")
    public ResponseResult getTaskStatusStatistics(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getTaskStatusStatistics(params);
    }

    @PostMapping("/kanban/task/status")
    public ResponseResult updateKanbanTaskStatus(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.updateKanbanTaskStatus(params);
    }

    @PostMapping("/kanban/task/status/batch")
    public ResponseResult batchUpdateKanbanTaskStatus(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.batchUpdateKanbanTaskStatus(params);
    }

    @PostMapping("/kanban/columns")
    public ResponseResult getKanbanColumns(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getKanbanColumns(params);
    }

    @PostMapping("/kanban/columns/update")
    public ResponseResult updateKanbanColumns(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.updateKanbanColumns(params);
    }

    @PostMapping("/kanban/filters")
    public ResponseResult getKanbanFilters(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getKanbanFilters(params);
    }

    @PostMapping("/kanban/task/detail")
    public ResponseResult getKanbanTaskDetail(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getKanbanTaskDetail(params);
    }

    @PostMapping("/kanban/activity/log")
    public ResponseResult getKanbanActivityLog(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getKanbanActivityLog(params);
    }

    @PostMapping("/kanban/progress")
    public ResponseResult getProjectProgressKanban(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getProjectProgressKanban(params);
    }

    /** ================== 设置管理接口 ================== */

    @PostMapping("/setting/all")
    public ResponseResult getAllSettings(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getAllSettings(params);
    }

    @PostMapping("/setting/category")
    public ResponseResult getSettingsByCategory(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getSettingsByCategory(params);
    }

    @PostMapping("/setting/value")
    public ResponseResult getSettingValue(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getSettingValue(params);
    }

    @PostMapping("/setting/save")
    public ResponseResult saveSettings(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.saveSettings(params);
    }

    @PostMapping("/setting/batch-save")
    public ResponseResult batchSaveSettings(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.batchSaveSettings(params);
    }

    @PostMapping("/setting/reset")
    public ResponseResult resetSettings(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.resetSettings(params);
    }

    // ================== AI智能服务接口 ==================

    /**
     * 获取操作日志列表
     */
    @PostMapping("/log/list")
    public ResponseResult getLogList(@RequestBody Map<String, Object> params) {
        Integer taskId = toIntegerParam(params.get("taskId"));
        if (taskId != null) {
            return pmServiceFeign.getLogsByTaskId(taskId);
        }
        Integer projectId = toIntegerParam(params.get("projectId"));
        if (projectId != null) {
            return pmServiceFeign.getLogsByProjectId(projectId);
        }
        return pmServiceFeign.getLogList(params);
    }

    @PostMapping("/tasks/remark/add")
    public ResponseResult addTaskRemark(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.addTaskRemark(params);
    }

    private static Integer toIntegerParam(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text)) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取即将到期的项目列表
     */
    @PostMapping("/project/upcoming-deadlines")
    public ResponseResult getUpcomingDeadlines(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getUpcomingDeadlines(params);
    }

    /**
     * 获取企微客户群列表（按项目经理企微账号）
     */
    @PostMapping("/projects/wecom-group-chats")
    public ResponseResult listWecomGroupChats(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.listWecomGroupChats(params);
    }

    /** ================== AI智能服务接口 ================== */

    @PostMapping("/ai/train-model")
    public ResponseResult trainAiModel(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.trainAiModel(params);
    }

    @PostMapping("/ai/task-recommendation")
    public ResponseResult getTaskRecommendation(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.getTaskRecommendation(params);
    }

    @PostMapping("/ai/priority-prediction")
    public ResponseResult predictTaskPriority(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.predictTaskPriority(params);
    }

    @PostMapping("/ai/deadline-estimation")
    public ResponseResult estimateDeadline(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.estimateDeadline(params);
    }

    @PostMapping("/ai/risk-assessment")
    public ResponseResult assessProjectRisk(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.assessProjectRisk(params);
    }

    @PostMapping("/ai/performance-analysis")
    public ResponseResult analyzeTeamPerformance(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.analyzeTeamPerformance(params);
    }

    /** ================== 测试管理 ================== */
    @PostMapping("/test/dashboard")
    public ResponseResult testDashboard(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testDashboard(params);
    }

    @PostMapping("/test/release/list")
    public ResponseResult testReleaseList(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testReleaseList(params);
    }

    @PostMapping("/test/release/save")
    public ResponseResult testReleaseSave(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testReleaseSave(params);
    }

    @PostMapping("/test/release/advance")
    public ResponseResult testReleaseAdvance(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testReleaseAdvance(params);
    }

    @PostMapping("/test/release/entry-check")
    public ResponseResult testReleaseEntryCheck(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testReleaseEntryCheck(params);
    }

    @PostMapping("/test/case/list")
    public ResponseResult testCaseList(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testCaseList(params);
    }

    @PostMapping("/test/case/save")
    public ResponseResult testCaseSave(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testCaseSave(params);
    }

    @PostMapping("/test/case/delete")
    public ResponseResult testCaseDelete(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testCaseDelete(params);
    }

    @PostMapping("/test/case/detail")
    public ResponseResult testCaseDetail(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testCaseDetail(params);
    }

    @PostMapping("/test/opentest/config")
    public ResponseResult testOpenTestConfig(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testOpenTestConfig(params);
    }

    @PostMapping("/test/opentest/config/save")
    public ResponseResult testOpenTestConfigSave(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testOpenTestConfigSave(params);
    }

    @PostMapping("/test/opentest/preflight/run")
    public ResponseResult testOpenTestPreflightRun(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testOpenTestPreflightRun(params);
    }

    @PostMapping("/test/module/generate-template")
    public ResponseResult testModuleGenerateTemplate(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testModuleGenerateTemplate(params);
    }

    @PostMapping("/test/audit/page/generate-from-modules")
    public ResponseResult testAuditPageGenerateFromModules(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testAuditPageGenerateFromModules(params);
    }

    @PostMapping("/test/opentest/sessions")
    public ResponseResult testOpenTestLinkedSessions(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testOpenTestLinkedSessions(params);
    }

    @PostMapping("/test/opentest/session/detail")
    public ResponseResult testOpenTestSessionDetail(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testOpenTestSessionDetail(params);
    }

    @PostMapping("/test/opentest/session/log")
    public ResponseResult testOpenTestSessionLog(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testOpenTestSessionLog(params);
    }

    @PostMapping("/test/opentest/session/create")
    public ResponseResult testOpenTestCreateSession(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testOpenTestCreateSession(params);
    }

    @PostMapping("/test/execution/list")
    public ResponseResult testExecutionList(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testExecutionList(params);
    }

    @PostMapping("/test/execution/save")
    public ResponseResult testExecutionSave(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testExecutionSave(params);
    }

    @PostMapping("/test/defect/list")
    public ResponseResult testDefectList(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testDefectList(params);
    }

    @PostMapping("/test/defect/save")
    public ResponseResult testDefectSave(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testDefectSave(params);
    }

    @PostMapping("/test/defect/transition")
    public ResponseResult testDefectTransition(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testDefectTransition(params);
    }

    @PostMapping("/test/document/list")
    public ResponseResult testDocumentList(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testDocumentList(params);
    }

    @PostMapping("/test/document/save")
    public ResponseResult testDocumentSave(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testDocumentSave(params);
    }

    @PostMapping("/test/opentest/workflow/status")
    public ResponseResult testOpenTestWorkflowStatus(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testOpenTestWorkflowStatus(params);
    }

    @PostMapping("/test/module/list")
    public ResponseResult testModuleList(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testModuleList(params);
    }

    @PostMapping("/test/module/save")
    public ResponseResult testModuleSave(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testModuleSave(params);
    }

    @PostMapping("/test/module/delete")
    public ResponseResult testModuleDelete(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testModuleDelete(params);
    }

    @PostMapping("/test/module/batch-import")
    public ResponseResult testModuleBatchImport(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testModuleBatchImport(params);
    }

    @PostMapping("/test/audit/page/list")
    public ResponseResult testAuditPageList(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testAuditPageList(params);
    }

    @PostMapping("/test/audit/page/save")
    public ResponseResult testAuditPageSave(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testAuditPageSave(params);
    }

    @PostMapping("/test/audit/page/delete")
    public ResponseResult testAuditPageDelete(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testAuditPageDelete(params);
    }

    @PostMapping("/test/audit/page/batch-import")
    public ResponseResult testAuditPageBatchImport(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testAuditPageBatchImport(params);
    }

    @PostMapping("/test/audit/summary")
    public ResponseResult testAuditSummary(@RequestBody Map<String, Object> params) {
        return pmServiceFeign.testAuditSummary(params);
    }
}
