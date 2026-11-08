package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 工作流服务Feign客户端
/**
 * 
 * @author Amos
 * @since 2024-01-01
 */
@FeignClient(name = "workflow", path = "/workflow", contextId = "workflowServiceFeign")
public interface WorkflowServiceFeign {

    /**
     * 启动流程实例
     */
    @PostMapping("/process/start")
    ResponseResult startProcess(@RequestParam("processKey") String processKey,
                               @RequestBody Object variables);

    /**
     * 查询待办任务
     */
    @GetMapping("/task/todo")
    ResponseResult getTodoTasks(@RequestParam("userId") Long userId);

    /**
     * 查询已办任务
     */
    @GetMapping("/task/done")
    ResponseResult getDoneTasks(@RequestParam("userId") Long userId);

    /**
     * 完成任务
     */
    @PostMapping("/task/complete/{taskId}")
    ResponseResult completeTask(@PathVariable("taskId") String taskId,
                               @RequestParam("userId") Long userId,
                               @RequestBody Object variables);

    /**
     * 获取流程定义列表
     */
    @GetMapping("/process/definitions")
    ResponseResult getProcessDefinitions();

    /**
     * 获取流程配置
     */
    @GetMapping("/process/config/{processKey}")
    ResponseResult getProcessConfig(@PathVariable("processKey") String processKey);

    /**
     * 更新流程配置
     */
    @PostMapping("/process/config")
    ResponseResult updateProcessConfig(@RequestBody Object config);

    /**
     * 获取流程列表
     */
    @GetMapping("/process/list")
    ResponseResult getProcessList(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size);

    /**
     * 获取用户待办任务
     */
    @GetMapping("/process/tasks")
    ResponseResult getUserTasks(@RequestParam("userId") Long userId);

    /**
     * 获取用户申请记录
     */
    @GetMapping("/process/applications")
    ResponseResult getUserApplications(@RequestParam("userId") Long userId,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(required = false) Integer page,
                                     @RequestParam(required = false) Integer size);

    /**
     * 获取流程历史
     */
    @GetMapping("/process/history")
    ResponseResult getProcessHistory(@RequestParam("userId") Long userId);

    /**
     * 获取任务详情
     */
    @GetMapping("/process/task/{taskId}")
    ResponseResult getTask(@PathVariable("taskId") String taskId);

    /**
     * 挂起流程实例
     */
    @PostMapping("/process/suspend/{processInstanceId}")
    ResponseResult suspendProcessInstance(@PathVariable("processInstanceId") String processInstanceId);

    /**
     * 激活流程实例
     */
    @PostMapping("/process/activate/{processInstanceId}")
    ResponseResult activateProcessInstance(@PathVariable("processInstanceId") String processInstanceId);

    /**
     * 删除流程实例
     */
    @DeleteMapping("/process/{processInstanceId}")
    ResponseResult deleteProcessInstance(@PathVariable("processInstanceId") String processInstanceId,
                                       @RequestParam("reason") String reason);

    /**
     * 获取流程实例详情
     */
    @GetMapping("/process/instance/{processInstanceId}")
    ResponseResult getProcessInstance(@PathVariable("processInstanceId") String processInstanceId);

    /**
     * 获取流程图
     */
    @GetMapping("/process/diagram/{processInstanceId}")
    ResponseResult getProcessDiagram(@PathVariable("processInstanceId") String processInstanceId);

    /**
     * 终止流程实例
     */
    @PostMapping("/process/terminate/{processInstanceId}")
    ResponseResult terminateProcess(@PathVariable("processInstanceId") String processInstanceId,
                                   @RequestParam("reason") String reason);

    /**
     * 获取历史任务
     */
    @GetMapping("/task/history/{processInstanceId}")
    ResponseResult getHistoryTasks(@PathVariable("processInstanceId") String processInstanceId);

    /**
     * 获取流程变量
     */
    @GetMapping("/process/variables/{processInstanceId}")
    ResponseResult getProcessVariables(@PathVariable("processInstanceId") String processInstanceId);

    /**
     * 设置流程变量
     */
    @PostMapping("/process/variables/{processInstanceId}")
    ResponseResult setProcessVariables(@PathVariable("processInstanceId") String processInstanceId,
                                      @RequestBody Object variables);

    /**
     * 获取流程节点
     */
    @GetMapping("/process/nodes/{processDefinitionId}")
    ResponseResult getProcessNodes(@PathVariable("processDefinitionId") String processDefinitionId);

    /**
     * 流程回退
     */
    @PostMapping("/process/rollback/{taskId}")
    ResponseResult rollbackProcess(@PathVariable("taskId") String taskId,
                                 @RequestParam("targetNodeId") String targetNodeId,
                                 @RequestParam("userId") Long userId);

    /**
     * 流程跳转
     */
    @PostMapping("/process/jump/{taskId}")
    ResponseResult jumpProcess(@PathVariable("taskId") String taskId,
                             @RequestParam("targetNodeId") String targetNodeId,
                             @RequestParam("userId") Long userId);

    /**
     * 获取流程监控数据
     */
    @GetMapping("/monitor/process")
    ResponseResult getProcessMonitor();

    /**
     * 获取任务监控数据
     */
    @GetMapping("/monitor/task")
    ResponseResult getTaskMonitor();
}