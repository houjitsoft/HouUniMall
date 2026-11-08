package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.WorkflowServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 工作流服务Web控制器
/**
 * 
 * @author Amos
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/web/workflow")
public class WebWorkflowController {

    @Autowired
    private WorkflowServiceFeign workflowServiceFeign;

    /**
     * 启动流程实例
     */
    @PostMapping("/process/start")
    public ResponseResult startProcess(@RequestParam("processKey") String processKey,
                                     @RequestBody Object variables) {
        return workflowServiceFeign.startProcess(processKey, variables);
    }

    /**
     * 查询待办任务
     */
    @GetMapping("/task/todo")
    public ResponseResult getTodoTasks(@RequestParam("userId") Long userId) {
        return workflowServiceFeign.getTodoTasks(userId);
    }

    /**
     * 查询已办任务
     */
    @GetMapping("/task/done")
    public ResponseResult getDoneTasks(@RequestParam("userId") Long userId) {
        return workflowServiceFeign.getDoneTasks(userId);
    }

    /**
     * 完成任务
     */
    @PostMapping("/task/complete/{taskId}")
    public ResponseResult completeTask(@PathVariable("taskId") String taskId,
                                     @RequestParam("userId") Long userId,
                                     @RequestBody Object variables) {
        return workflowServiceFeign.completeTask(taskId, userId, variables);
    }

    /**
     * 获取流程定义列表
     */
    @GetMapping("/process/definitions")
    public ResponseResult getProcessDefinitions() {
        return workflowServiceFeign.getProcessDefinitions();
    }

    /**
     * 获取流程配置
     */
    @GetMapping("/process/config/{processKey}")
    public ResponseResult getProcessConfig(@PathVariable("processKey") String processKey) {
        return workflowServiceFeign.getProcessConfig(processKey);
    }

    /**
     * 更新流程配置
     */
    @PostMapping("/process/config")
    public ResponseResult updateProcessConfig(@RequestBody Object config) {
        return workflowServiceFeign.updateProcessConfig(config);
    }

    /**
     * 获取流程列表
     */
    @GetMapping("/process/list")
    public ResponseResult getProcessList(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String status,
                                       @RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer size) {
        return workflowServiceFeign.getProcessList(keyword, status, page, size);
    }

    /**
     * 获取用户待办任务
     */
    @GetMapping("/process/tasks")
    public ResponseResult getUserTasks(@RequestParam("userId") Long userId) {
        return workflowServiceFeign.getUserTasks(userId);
    }

    /**
     * 获取用户申请记录
     */
    @GetMapping("/process/applications")
    public ResponseResult getUserApplications(@RequestParam("userId") Long userId,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer size) {
        return workflowServiceFeign.getUserApplications(userId, status, page, size);
    }

    /**
     * 获取流程历史
     */
    @GetMapping("/process/history")
    public ResponseResult getProcessHistory(@RequestParam("userId") Long userId) {
        return workflowServiceFeign.getProcessHistory(userId);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/process/task/{taskId}")
    public ResponseResult getTask(@PathVariable("taskId") String taskId) {
        return workflowServiceFeign.getTask(taskId);
    }

    /**
     * 挂起流程实例
     */
    @PostMapping("/process/suspend/{processInstanceId}")
    public ResponseResult suspendProcessInstance(@PathVariable("processInstanceId") String processInstanceId) {
        return workflowServiceFeign.suspendProcessInstance(processInstanceId);
    }

    /**
     * 激活流程实例
     */
    @PostMapping("/process/activate/{processInstanceId}")
    public ResponseResult activateProcessInstance(@PathVariable("processInstanceId") String processInstanceId) {
        return workflowServiceFeign.activateProcessInstance(processInstanceId);
    }

    /**
     * 删除流程实例
     */
    @DeleteMapping("/process/{processInstanceId}")
    public ResponseResult deleteProcessInstance(@PathVariable("processInstanceId") String processInstanceId,
                                              @RequestParam("reason") String reason) {
        return workflowServiceFeign.deleteProcessInstance(processInstanceId, reason);
    }

    /**
     * 获取流程实例详情
     */
    @GetMapping("/process/instance/{processInstanceId}")
    public ResponseResult getProcessInstance(@PathVariable("processInstanceId") String processInstanceId) {
        return workflowServiceFeign.getProcessInstance(processInstanceId);
    }

    /**
     * 获取流程图
     */
    @GetMapping("/process/diagram/{processInstanceId}")
    public ResponseResult getProcessDiagram(@PathVariable("processInstanceId") String processInstanceId) {
        return workflowServiceFeign.getProcessDiagram(processInstanceId);
    }

    /**
     * 终止流程实例
     */
    @PostMapping("/process/terminate/{processInstanceId}")
    public ResponseResult terminateProcess(@PathVariable("processInstanceId") String processInstanceId,
                                         @RequestParam("reason") String reason) {
        return workflowServiceFeign.terminateProcess(processInstanceId, reason);
    }

    /**
     * 获取历史任务
     */
    @GetMapping("/task/history/{processInstanceId}")
    public ResponseResult getHistoryTasks(@PathVariable("processInstanceId") String processInstanceId) {
        return workflowServiceFeign.getHistoryTasks(processInstanceId);
    }

    /**
     * 获取流程变量
     */
    @GetMapping("/process/variables/{processInstanceId}")
    public ResponseResult getProcessVariables(@PathVariable("processInstanceId") String processInstanceId) {
        return workflowServiceFeign.getProcessVariables(processInstanceId);
    }

    /**
     * 设置流程变量
     */
    @PostMapping("/process/variables/{processInstanceId}")
    public ResponseResult setProcessVariables(@PathVariable("processInstanceId") String processInstanceId,
                                            @RequestBody Object variables) {
        return workflowServiceFeign.setProcessVariables(processInstanceId, variables);
    }

    /**
     * 获取流程节点
     */
    @GetMapping("/process/nodes/{processDefinitionId}")
    public ResponseResult getProcessNodes(@PathVariable("processDefinitionId") String processDefinitionId) {
        return workflowServiceFeign.getProcessNodes(processDefinitionId);
    }

    /**
     * 流程回退
     */
    @PostMapping("/process/rollback/{taskId}")
    public ResponseResult rollbackProcess(@PathVariable("taskId") String taskId,
                                        @RequestParam("targetNodeId") String targetNodeId,
                                        @RequestParam("userId") Long userId) {
        return workflowServiceFeign.rollbackProcess(taskId, targetNodeId, userId);
    }

    /**
     * 流程跳转
     */
    @PostMapping("/process/jump/{taskId}")
    public ResponseResult jumpProcess(@PathVariable("taskId") String taskId,
                                    @RequestParam("targetNodeId") String targetNodeId,
                                    @RequestParam("userId") Long userId) {
        return workflowServiceFeign.jumpProcess(taskId, targetNodeId, userId);
    }

    /**
     * 获取流程监控数据
     */
    @GetMapping("/monitor/process")
    public ResponseResult getProcessMonitor() {
        return workflowServiceFeign.getProcessMonitor();
    }

    /**
     * 获取任务监控数据
     */
    @GetMapping("/monitor/task")
    public ResponseResult getTaskMonitor() {
        return workflowServiceFeign.getTaskMonitor();
    }
}