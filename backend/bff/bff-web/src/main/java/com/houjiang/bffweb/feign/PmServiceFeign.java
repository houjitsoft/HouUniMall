package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * PM服务Feign客户端接口
/**
 * 项目管理服务远程调用接口
 * <p>
 * 生产同机部署建议配置 {@code feign.pm.url=http://127.0.0.1:8304}，
 * 避免 Nacos 无 pm 实例时出现 Load balancer does not contain an instance。
 * @author Amos
 */
@FeignClient(value = "pm", fallback = PmServiceFeignFallback.class, url = "${feign.pm.url:}", path = "/plugin")
public interface PmServiceFeign {

    /**
     * 获取项目管理子菜单
     */
    @PostMapping("/pm/menu/project-menus")
    ResponseResult getProjectMenus(@RequestBody Map<String, Object> params);

    /**
     * 获取项目列表
     */
    @PostMapping("/pm/projects/list")
    ResponseResult getProjectList(@RequestBody Map<String, Object> params);

    /**
     * 获取项目详情
     */
    @PostMapping("/pm/projects/detail")
    ResponseResult getProjectDetail(@RequestBody Map<String, Object> params);

    /**
     * 创建项目
     */
    @PostMapping("/pm/projects")
    ResponseResult createProject(@RequestBody Map<String, Object> projectData);

    /**
     * 更新项目
     */
    @PostMapping("/pm/projects/update")
    ResponseResult updateProject(@RequestBody Map<String, Object> projectData);

    /**
     * 删除项目
     */
    @PostMapping("/pm/projects/delete")
    ResponseResult deleteProject(@RequestBody Map<String, Object> params);

    /**
     * 生成项目编号
     */
    @PostMapping("/pm/projects/generate-number")
    ResponseResult generateProjectNumber(@RequestBody Map<String, Object> params);

    /**
     * 获取项目状态枚举
     */
    @PostMapping("/pm/projects/enums/status")
    ResponseResult getProjectStatusEnum(@RequestBody Map<String, Object> params);

    /**
     * 获取项目分类枚举
     */
    @PostMapping("/pm/projects/enums/category")
    ResponseResult getProjectCategoryEnum(@RequestBody Map<String, Object> params);

    /**
     * 获取优先级枚举
     */
    @PostMapping("/pm/projects/enums/priority")
    ResponseResult getPriorityEnum(@RequestBody Map<String, Object> params);

    /**
     * 获取项目类型枚举
     */
    @PostMapping("/pm/projects/enums/type")
    ResponseResult getProjectTypeEnum(@RequestBody Map<String, Object> params);

    /**
     * 获取任务列表
     */
    @PostMapping("/pm/tasks/list")
    ResponseResult getTaskList(@RequestBody Map<String, Object> params);

    /**
     * 获取任务元数据（项目阶段、任务类型）
     */
    @PostMapping("/pm/tasks/meta")
    ResponseResult getTaskMeta(@RequestBody Map<String, Object> params);

    /**
     * 获取任务详情
     */
    @PostMapping("/pm/tasks/detail")
    ResponseResult getTaskDetail(@RequestBody Map<String, Object> params);

    /**
     * 创建任务
     */
    @PostMapping("/pm/tasks")
    ResponseResult createTask(@RequestBody Map<String, Object> taskData);

    /**
     * 更新任务
     */
    @PostMapping("/pm/tasks/update")
    ResponseResult updateTask(@RequestBody Map<String, Object> taskData);

    /**
     * 删除任务
     */
    @PostMapping("/pm/tasks/delete")
    ResponseResult deleteTask(@RequestBody Map<String, Object> params);

    /**
     * 完成任务
     */
    @PostMapping("/pm/tasks/complete")
    ResponseResult completeTask(@RequestBody Map<String, Object> params);

    /**
     * 更新任务状态
     */
    @PostMapping("/pm/tasks/status/update")
    ResponseResult updateTaskStatus(@RequestBody Map<String, Object> params);

    /**
     * 更新任务进度
     */
    @PostMapping("/pm/tasks/progress/update")
    ResponseResult updateTaskProgress(@RequestBody Map<String, Object> params);

    /**
     * 生成任务编号
     */
    @PostMapping("/pm/tasks/generate-number")
    ResponseResult generateTaskNumber(@RequestBody Map<String, Object> params);

    /**
     * 获取项目成员列表
     */
    @PostMapping("/pm/project/member")
    ResponseResult getProjectMembers(@RequestBody Map<String, Object> params);

    /**
     * 添加项目成员
     */
    @PostMapping("/pm/project/member/add")
    ResponseResult addProjectMember(@RequestBody Map<String, Object> memberData);

    /**
     * 移除项目成员
     */
    @PostMapping("/pm/project/member/remove")
    ResponseResult removeProjectMember(@RequestBody Map<String, Object> params);

    /**
     * 更新成员角色
     */
    @PostMapping("/pm/project/member/role/update")
    ResponseResult updateMemberRole(@RequestBody Map<String, Object> params);

    /**
     * 获取项目统计概览
     */
    @GetMapping("/pm/statistics/overview")
    ResponseResult getStatisticsOverview(@RequestParam Map<String, Object> params);

    /**
     * 获取任务统计
     */
    @PostMapping("/pm/statistics/task")
    ResponseResult getTaskStatistics(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/pm/statistics/task/options")
    ResponseResult getTaskStatisticsOptions(@RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/pm/statistics/task/export")
    feign.Response exportTaskStatistics(@RequestParam Map<String, Object> params);

    /**
     * 获取进度报表
     */
    @PostMapping("/pm/statistics/progress")
    ResponseResult getProgressReport(@RequestBody Map<String, Object> params);

    /**
     * 获取项目里程碑列表
     */
    @PostMapping("/pm/milestone/list")
    ResponseResult getMilestoneList(@RequestBody Map<String, Object> params);

    /**
     * 创建项目里程碑
     */
    @PostMapping("/pm/milestone/create")
    ResponseResult createMilestone(@RequestBody Map<String, Object> milestoneData);

    /**
     * 更新项目里程碑
     */
    @PostMapping("/pm/milestone/update")
    ResponseResult updateMilestone(@RequestBody Map<String, Object> milestoneData);

    /**
     * 删除项目里程碑
     */
    @PostMapping("/pm/milestone/delete")
    ResponseResult deleteMilestone(@RequestBody Map<String, Object> params);

    /**
     * 获取项目甘特图数据
     */
    @PostMapping("/pm/project/gantt")
    ResponseResult getProjectGanttData(@RequestBody Map<String, Object> params);

    /**
     * 获取用户参与的项目列表
     */
    @PostMapping("/pm/user/projects")
    ResponseResult getUserProjects(@RequestBody Map<String, Object> params);

    /**
     * 获取用户任务统计
     */
    @PostMapping("/pm/user/task-stats")
    ResponseResult getUserTaskStats(@RequestBody Map<String, Object> params);
    
    /**
     * 获取用户列表
     */
    @PostMapping("/pm/user/list")
    ResponseResult getUserList(@RequestBody Map<String, Object> params);

    /**
     * 批量删除任务
     */
    @PostMapping("/pm/tasks/batch-delete")
    ResponseResult batchDeleteTasks(@RequestBody Map<String, Object> params);

    /**
     * 批量更新任务状态
     */
    @PostMapping("/pm/tasks/batch-status")
    ResponseResult batchUpdateTaskStatus(@RequestBody Map<String, Object> params);

    /**
     * 批量分配任务
     */
    @PostMapping("/pm/tasks/batch-assign")
    ResponseResult batchAssignTasks(@RequestBody Map<String, Object> params);

    /**
     * 批量更新任务进度
     */
    @PostMapping("/pm/tasks/batch-progress")
    ResponseResult batchUpdateTaskProgress(@RequestBody Map<String, Object> params);

    /**
     * 更新任务排序
     */
    @PostMapping("/pm/tasks/sort")
    ResponseResult updateTaskSort(@RequestBody Map<String, Object> params);

    /**
     * 获取任务评论
     */
    @PostMapping("/pm/tasks/comments/list")
    ResponseResult getTaskComments(@RequestBody Map<String, Object> params);

    /**
     * 添加任务评论
     */
    @PostMapping("/pm/tasks/comments/add")
    ResponseResult addTaskComment(@RequestBody Map<String, Object> params);

    /**
     * 获取任务附件
     */
    @PostMapping("/pm/tasks/attachments/list")
    ResponseResult getTaskAttachments(@RequestBody Map<String, Object> params);

    /**
     * 上传任务附件
     */
    @PostMapping("/pm/tasks/attachments/upload")
    ResponseResult uploadTaskAttachment(@RequestBody Map<String, Object> params);

    /**
     * 导出任务数据
     */
    @GetMapping("/pm/tasks/export")
    ResponseResult exportTasks(@RequestParam Map<String, Object> params);

    /**
     * 导入任务数据
     */
    @PostMapping("/pm/tasks/import")
    ResponseResult importTasks(@RequestParam("file") MultipartFile file);

    /**
     * 下载任务导入模板
     */
    @GetMapping("/pm/tasks/template")
    ResponseResult downloadTemplate();

    // ================== 团队管理相关接口 ==================

    /**
     * 获取团队成员列表
     */
    @PostMapping("/pm/team/member/list")
    ResponseResult getTeamMembers(@RequestBody Map<String, Object> params);

    /**
     * 添加团队成员
     */
    @PostMapping("/pm/team/member/add")
    ResponseResult addTeamMember(@RequestBody Map<String, Object> memberData);

    /**
     * 更新团队成员
     */
    @PostMapping("/pm/team/member/update")
    ResponseResult updateTeamMember(@RequestBody Map<String, Object> memberData);

    /**
     * 移除团队成员
     */
    @PostMapping("/pm/team/member/remove")
    ResponseResult removeTeamMember(@RequestBody Map<String, Object> params);

    /**
     * 获取团队统计数据
     */
    @PostMapping("/pm/team/statistics")
    ResponseResult getTeamStatistics(@RequestBody Map<String, Object> params);

    /**
     * 获取团队详细统计
     */
    @PostMapping("/pm/team/statistics/detail")
    ResponseResult getTeamDetailStatistics(@RequestBody Map<String, Object> params);

    /**
     * 获取团队成员绩效统计
     */
    @PostMapping("/pm/team/member/performance")
    ResponseResult getTeamMemberPerformance(@RequestBody Map<String, Object> params);

    // ================== 进度跟踪相关接口 ==================

    /**
     * 获取进度概览
     */
    @PostMapping("/pm/progress/overview")
    ResponseResult getProgressOverview(@RequestBody Map<String, Object> params);

    /**
     * 获取进度统计
     */
    @PostMapping("/pm/progress/statistics")
    ResponseResult getProgressStatistics(@RequestBody Map<String, Object> params);

    /**
     * 获取里程碑列表
     */
    @PostMapping("/pm/progress/milestone/list")
    ResponseResult getProgressMilestoneList(@RequestBody Map<String, Object> params);

    /**
     * 创建里程碑
     */
    @PostMapping("/pm/progress/milestone/create")
    ResponseResult createProgressMilestone(@RequestBody Map<String, Object> params);

    /**
     * 更新里程碑
     */
    @PostMapping("/pm/progress/milestone/update")
    ResponseResult updateProgressMilestone(@RequestBody Map<String, Object> params);

    /**
     * 删除里程碑
     */
    @PostMapping("/pm/progress/milestone/delete")
    ResponseResult deleteProgressMilestone(@RequestBody Map<String, Object> params);

    /**
     * 获取进度详细报告
     */
    @PostMapping("/pm/progress/report")
    ResponseResult getProgressDetailReport(@RequestBody Map<String, Object> params);

    /**
     * 获取图表数据
     */
    @PostMapping("/pm/progress/chart")
    ResponseResult getProgressChartData(@RequestBody Map<String, Object> params);

    /**
     * 获取风险监控数据
     */
    @PostMapping("/pm/progress/risk")
    ResponseResult getProgressRiskData(@RequestBody Map<String, Object> params);

    // ================== 看板相关接口 ==================

    /**
     * 获取看板概览数据
     */
    @PostMapping("/pm/kanban/overview")
    ResponseResult getKanbanOverview(@RequestBody Map<String, Object> params);

    /**
     * 获取看板任务列表
     */
    @PostMapping("/pm/kanban/tasks")
    ResponseResult getKanbanTasks(@RequestBody Map<String, Object> params);

    /**
     * 获取任务状态统计
     */
    @PostMapping("/pm/kanban/status/statistics")
    ResponseResult getTaskStatusStatistics(@RequestBody Map<String, Object> params);

    /**
     * 更新任务状态（看板拖拽）
     */
    @PostMapping("/pm/kanban/task/status")
    ResponseResult updateKanbanTaskStatus(@RequestBody Map<String, Object> params);

    /**
     * 批量更新任务状态（看板）
     */
    @PostMapping("/pm/kanban/task/status/batch")
    ResponseResult batchUpdateKanbanTaskStatus(@RequestBody Map<String, Object> params);

    /**
     * 获取看板列配置
     */
    @PostMapping("/pm/kanban/columns")
    ResponseResult getKanbanColumns(@RequestBody Map<String, Object> params);

    /**
     * 更新看板列配置
     */
    @PostMapping("/pm/kanban/columns/update")
    ResponseResult updateKanbanColumns(@RequestBody Map<String, Object> params);

    /**
     * 获取看板过滤选项
     */
    @PostMapping("/pm/kanban/filters")
    ResponseResult getKanbanFilters(@RequestBody Map<String, Object> params);

    /**
     * 获取任务详情（看板）
     */
    @PostMapping("/pm/kanban/task/detail")
    ResponseResult getKanbanTaskDetail(@RequestBody Map<String, Object> params);

    /**
     * 获取看板活动日志
     */
    @PostMapping("/pm/kanban/activity/log")
    ResponseResult getKanbanActivityLog(@RequestBody Map<String, Object> params);

    /**
     * 获取项目进度看板
     */
    @PostMapping("/pm/kanban/progress")
    ResponseResult getProjectProgressKanban(@RequestBody Map<String, Object> params);

    // ================== 设置管理相关接口 ==================

    /**
     * 获取所有设置
     */
    @PostMapping("/pm/setting/all")
    ResponseResult getAllSettings(@RequestBody Map<String, Object> params);

    /**
     * 根据分类获取设置
     */
    @PostMapping("/pm/setting/category")
    ResponseResult getSettingsByCategory(@RequestBody Map<String, Object> params);

    /**
     * 根据键名获取设置值
     */
    @PostMapping("/pm/setting/value")
    ResponseResult getSettingValue(@RequestBody Map<String, Object> params);

    /**
     * 保存设置
     */
    @PostMapping("/pm/setting/save")
    ResponseResult saveSettings(@RequestBody Map<String, Object> params);

    /**
     * 批量保存设置
     */
    @PostMapping("/pm/setting/batch-save")
    ResponseResult batchSaveSettings(@RequestBody Map<String, Object> params);

    /**
     * 重置设置为默认值
     */
    @PostMapping("/pm/setting/reset")
    ResponseResult resetSettings(@RequestBody Map<String, Object> params);

    // ================== 日志和项目相关接口 ==================

    /**
     * 获取操作日志列表
     */
    @GetMapping("/pm/v1/logs")
    ResponseResult getLogList(@RequestParam Map<String, Object> params);

    /**
     * 根据任务ID查询操作日志
     */
    @GetMapping("/pm/v1/logs/task/{taskId}")
    ResponseResult getLogsByTaskId(@PathVariable("taskId") Integer taskId);

    /**
     * 根据项目ID查询操作日志
     */
    @GetMapping("/pm/v1/logs/project/{projectId}")
    ResponseResult getLogsByProjectId(@PathVariable("projectId") Integer projectId);

    /**
     * 添加任务备注
     */
    @PostMapping("/pm/tasks/remark/add")
    ResponseResult addTaskRemark(@RequestBody Map<String, Object> params);

    /**
     * 获取即将到期的项目列表
     */
    @PostMapping("/pm/projects/upcoming-deadlines")
    ResponseResult getUpcomingDeadlines(@RequestBody Map<String, Object> params);

    /**
     * 获取企微客户群列表（按项目经理过滤）
     */
    @PostMapping("/pm/projects/wecom-group-chats")
    ResponseResult listWecomGroupChats(@RequestBody Map<String, Object> params);

    // ================== AI智能服务相关接口 ==================

    /**
     * 训练AI模型
     */
    @PostMapping("/pm/ai/train-model")
    ResponseResult trainAiModel(@RequestBody Map<String, Object> params);

    /**
     * AI任务推荐
     */
    @PostMapping("/pm/ai/task-recommendation")
    ResponseResult getTaskRecommendation(@RequestBody Map<String, Object> params);

    /**
     * AI优先级预测
     */
    @PostMapping("/pm/ai/priority-prediction")
    ResponseResult predictTaskPriority(@RequestBody Map<String, Object> params);

    /**
     * AI工期估算
     */
    @PostMapping("/pm/ai/deadline-estimation")
    ResponseResult estimateDeadline(@RequestBody Map<String, Object> params);

    /**
     * AI风险评估
     */
    @PostMapping("/pm/ai/risk-assessment")
    ResponseResult assessProjectRisk(@RequestBody Map<String, Object> params);

    /**
     * AI绩效分析
     */
    @PostMapping("/pm/ai/performance-analysis")
    ResponseResult analyzeTeamPerformance(@RequestBody Map<String, Object> params);

    // ================== 测试管理 ==================
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/dashboard")
    ResponseResult testDashboard(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/release/list")
    ResponseResult testReleaseList(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/release/save")
    ResponseResult testReleaseSave(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/release/advance")
    ResponseResult testReleaseAdvance(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/release/entry-check")
    ResponseResult testReleaseEntryCheck(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/case/list")
    ResponseResult testCaseList(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/case/save")
    ResponseResult testCaseSave(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/case/delete")
    ResponseResult testCaseDelete(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/case/detail")
    ResponseResult testCaseDetail(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/opentest/config")
    ResponseResult testOpenTestConfig(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/opentest/config/save")
    ResponseResult testOpenTestConfigSave(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/opentest/preflight/run")
    ResponseResult testOpenTestPreflightRun(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/module/generate-template")
    ResponseResult testModuleGenerateTemplate(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/audit/page/generate-from-modules")
    ResponseResult testAuditPageGenerateFromModules(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/opentest/sessions")
    ResponseResult testOpenTestLinkedSessions(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/opentest/session/detail")
    ResponseResult testOpenTestSessionDetail(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/opentest/session/log")
    ResponseResult testOpenTestSessionLog(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/opentest/session/create")
    ResponseResult testOpenTestCreateSession(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/execution/list")
    ResponseResult testExecutionList(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/execution/save")
    ResponseResult testExecutionSave(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/defect/list")
    ResponseResult testDefectList(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/defect/save")
    ResponseResult testDefectSave(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/defect/transition")
    ResponseResult testDefectTransition(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/document/list")
    ResponseResult testDocumentList(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/document/save")
    ResponseResult testDocumentSave(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/opentest/workflow/status")
    ResponseResult testOpenTestWorkflowStatus(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/module/list")
    ResponseResult testModuleList(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/module/save")
    ResponseResult testModuleSave(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/module/delete")
    ResponseResult testModuleDelete(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/module/batch-import")
    ResponseResult testModuleBatchImport(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/audit/page/list")
    ResponseResult testAuditPageList(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/audit/page/save")
    ResponseResult testAuditPageSave(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/audit/page/delete")
    ResponseResult testAuditPageDelete(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/audit/page/batch-import")
    ResponseResult testAuditPageBatchImport(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/pm/test/audit/summary")
    ResponseResult testAuditSummary(@RequestBody Map<String, Object> params);
}
