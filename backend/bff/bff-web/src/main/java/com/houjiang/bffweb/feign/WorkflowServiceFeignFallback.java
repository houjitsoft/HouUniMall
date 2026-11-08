package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

/**
 * 工作流服务Feign客户端降级处理
/**
 * 
 * @author Amos
 * @since 2024-01-01
 */
@Component
public class WorkflowServiceFeignFallback implements WorkflowServiceFeign {

    @Override
    public ResponseResult startProcess(String processKey, Object variables) {
        return ResponseResult.error("启动流程失败");
    }

    @Override
    public ResponseResult getTodoTasks(Long userId) {
        return ResponseResult.error("获取待办任务失败");
    }

    @Override
    public ResponseResult getDoneTasks(Long userId) {
        return ResponseResult.error("获取已办任务失败");
    }

    @Override
    public ResponseResult completeTask(String taskId, Long userId, Object variables) {
        return ResponseResult.error("完成任务失败");
    }

    @Override
    public ResponseResult getProcessDefinitions() {
        return ResponseResult.error("获取流程定义失败");
    }

    @Override
    public ResponseResult getProcessConfig(String processKey) {
        return ResponseResult.error("获取流程配置失败");
    }

    @Override
    public ResponseResult updateProcessConfig(Object config) {
        return ResponseResult.error("更新流程配置失败");
    }

    @Override
    public ResponseResult getProcessList(String keyword, String status, Integer page, Integer size) {
        return ResponseResult.error("获取流程列表失败");
    }

    @Override
    public ResponseResult getUserTasks(Long userId) {
        return ResponseResult.error("获取用户待办任务失败");
    }

    @Override
    public ResponseResult getUserApplications(Long userId, String status, Integer page, Integer size) {
        return ResponseResult.error("获取用户申请记录失败");
    }

    @Override
    public ResponseResult getProcessHistory(Long userId) {
        return ResponseResult.error("获取流程历史失败");
    }

    @Override
    public ResponseResult getTask(String taskId) {
        return ResponseResult.error("获取任务详情失败");
    }

    @Override
    public ResponseResult suspendProcessInstance(String processInstanceId) {
        return ResponseResult.error("挂起流程实例失败");
    }

    @Override
    public ResponseResult activateProcessInstance(String processInstanceId) {
        return ResponseResult.error("激活流程实例失败");
    }

    @Override
    public ResponseResult deleteProcessInstance(String processInstanceId, String reason) {
        return ResponseResult.error("删除流程实例失败");
    }

    @Override
    public ResponseResult getProcessInstance(String processInstanceId) {
        return ResponseResult.error("获取流程实例失败");
    }

    @Override
    public ResponseResult getProcessDiagram(String processInstanceId) {
        return ResponseResult.error("获取流程图失败");
    }

    @Override
    public ResponseResult terminateProcess(String processInstanceId, String reason) {
        return ResponseResult.error("终止流程失败");
    }

    @Override
    public ResponseResult getHistoryTasks(String processInstanceId) {
        return ResponseResult.error("获取历史任务失败");
    }

    @Override
    public ResponseResult getProcessVariables(String processInstanceId) {
        return ResponseResult.error("获取流程变量失败");
    }

    @Override
    public ResponseResult setProcessVariables(String processInstanceId, Object variables) {
        return ResponseResult.error("设置流程变量失败");
    }

    @Override
    public ResponseResult getProcessNodes(String processDefinitionId) {
        return ResponseResult.error("获取流程节点失败");
    }

    @Override
    public ResponseResult rollbackProcess(String taskId, String targetNodeId, Long userId) {
        return ResponseResult.error("流程回退失败");
    }

    @Override
    public ResponseResult jumpProcess(String taskId, String targetNodeId, Long userId) {
        return ResponseResult.error("流程跳转失败");
    }

    @Override
    public ResponseResult getProcessMonitor() {
        return ResponseResult.error("获取流程监控数据失败");
    }

    @Override
    public ResponseResult getTaskMonitor() {
        return ResponseResult.error("获取任务监控数据失败");
    }
}