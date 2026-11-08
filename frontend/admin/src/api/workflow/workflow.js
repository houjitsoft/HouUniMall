/**
 * Workflow模块API接口
 * 使用主服务的统一API服务层
 */

// 导入主服务的统一API服务
import request from '@/utils/request'

// 创建workflowApi实例
const workflowApi = request

/**
 * 工作流流程管理API
 */
export const workflowService = {
  // 获取流程列表
  getProcessList: async (params) => {
    try {
      const response = await workflowApi.get('/api/web/workflow/process/list', { params })
      return response
    } catch (error) {
      console.error('获取流程列表失败:', error)
      throw error
    }
  },

  // 获取流程配置详情
  getProcessConfig: async (processKey) => {
    try {
      const response = await workflowApi.get(`/web/workflow/process/config/${processKey}`)
      return response
    } catch (error) {
      console.error('获取流程配置详情失败:', error)
      throw error
    }
  },

  // 更新流程配置
  updateProcessConfig: async (data) => {
    try {
      const response = await workflowApi.put('/api/web/workflow/process/config', data)
      return response
    } catch (error) {
      console.error('更新流程配置失败:', error)
      throw error
    }
  },

  // 启动流程
  startProcess: async (data) => {
    try {
      const response = await workflowApi.post('/web/workflow/process/start', data)
      return response
    } catch (error) {
      console.error('启动流程失败:', error)
      throw error
    }
  },

  // 完成任务
  completeTask: async (data) => {
    try {
      const response = await workflowApi.post('/web/workflow/process/complete-task', data)
      return response
    } catch (error) {
      console.error('完成任务失败:', error)
      throw error
    }
  },

  // 获取用户待办任务
  getUserTasks: async (userId) => {
    try {
      const response = await workflowApi.get('/api/web/workflow/process/tasks', { 
        params: { userId } 
      })
      return response
    } catch (error) {
      console.error('获取用户待办任务失败:', error)
      throw error
    }
  },

  // 获取用户申请记录
  getUserApplications: async (params) => {
    try {
      const response = await workflowApi.get('/api/web/workflow/process/applications', { 
        params 
      })
      return response
    } catch (error) {
      console.error('获取用户申请记录失败:', error)
      throw error
    }
  },

  // 获取流程历史
  getProcessHistory: async (userId) => {
    try {
      const response = await workflowApi.get('/api/web/workflow/process/history', { 
        params: { userId } 
      })
      return response
    } catch (error) {
      console.error('获取流程历史失败:', error)
      throw error
    }
  },

  // 获取流程实例详情
  getProcessInstance: async (processInstanceId) => {
    try {
      const response = await workflowApi.get(`/web/workflow/process/instance/${processInstanceId}`)
      return response
    } catch (error) {
      console.error('获取流程实例详情失败:', error)
      throw error
    }
  },

  // 获取任务详情
  getTask: async (taskId) => {
    try {
      const response = await workflowApi.get(`/web/workflow/process/task/${taskId}`)
      return response
    } catch (error) {
      console.error('获取任务详情失败:', error)
      throw error
    }
  },

  // 挂起流程实例
  suspendProcessInstance: async (processInstanceId) => {
    try {
      const response = await workflowApi.post(`/web/workflow/process/suspend/${processInstanceId}`)
      return response
    } catch (error) {
      console.error('挂起流程实例失败:', error)
      throw error
    }
  },

  // 激活流程实例
  activateProcessInstance: async (processInstanceId) => {
    try {
      const response = await workflowApi.post(`/web/workflow/process/activate/${processInstanceId}`)
      return response
    } catch (error) {
      console.error('激活流程实例失败:', error)
      throw error
    }
  },

  // 删除流程实例
  deleteProcessInstance: async (processInstanceId, reason) => {
    try {
      const response = await workflowApi.delete(`/web/workflow/process/${processInstanceId}`, { 
        params: { reason } 
      })
      return response
    } catch (error) {
      console.error('删除流程实例失败:', error)
      throw error
    }
  }
}

/**
 * 工作流表单管理API
 */
export const formService = {
  // 获取表单定义
  getFormDefinition: async (formKey) => {
    try {
      const response = await workflowApi.get(`/web/workflow/form/definition/${formKey}`)
      return response
    } catch (error) {
      console.error('获取表单定义失败:', error)
      throw error
    }
  },

  // 保存表单定义
  saveFormDefinition: async (data) => {
    try {
      const response = await workflowApi.post('/web/workflow/form/definition', data)
      return response
    } catch (error) {
      console.error('保存表单定义失败:', error)
      throw error
    }
  },

  // 更新表单定义
  updateFormDefinition: async (data) => {
    try {
      const response = await workflowApi.put('/api/web/workflow/form/definition', data)
      return response
    } catch (error) {
      console.error('更新表单定义失败:', error)
      throw error
    }
  }
}

// 导出API实例
export { workflowApi, formService as formApi }

export default workflowService