/**
 * 系统设置API接口
 * 使用主服务的统一API服务层
 */

// 导入主服务的统一API服务
import request from '@/utils/request'

/**
 * 系统基础配置API接口
 */
export const systemBasicService = {
  // 获取系统基础配置
  getSystemBasicConfig: async () => {
    try {
      const response = await request.get('/api/web/setting/system/basic')
      return response
    } catch (error) {
      console.error('获取系统基础配置失败:', error)
      throw error
    }
  },

  // 保存系统基础配置
  saveSystemBasicConfig: async (data) => {
    try {
      const response = await request.post('/web/setting/system/basic', data)
      return response
    } catch (error) {
      console.error('保存系统基础配置失败:', error)
      throw error
    }
  },

  // 获取网络配置
  getNetworkConfig: async () => {
    try {
      const response = await request.get('/api/web/setting/system/network')
      return response
    } catch (error) {
      console.error('获取网络配置失败:', error)
      throw error
    }
  },

  // 保存网络配置
  saveNetworkConfig: async (data) => {
    try {
      const response = await request.post('/web/setting/system/network', data)
      return response
    } catch (error) {
      console.error('保存网络配置失败:', error)
      throw error
    }
  },

  // 获取文件配置
  getFileConfig: async () => {
    try {
      const response = await request.get('/api/web/setting/system/file')
      return response
    } catch (error) {
      console.error('获取文件配置失败:', error)
      throw error
    }
  },

  // 保存文件配置
  saveFileConfig: async (data) => {
    try {
      const response = await request.post('/web/setting/system/file', data)
      return response
    } catch (error) {
      console.error('保存文件配置失败:', error)
      throw error
    }
  },

  // 获取第三方配置
  getThirdPartyConfig: async (type) => {
    try {
      const response = await request.get(`/web/setting/third-party/${type}`)
      return response
    } catch (error) {
      console.error('获取第三方配置失败:', error)
      // 返回默认空配置，避免页面报错
      return { code: 200, data: null }
    }
  },

  // 批量获取全部第三方配置（设置页一次加载）
  getThirdPartyConfigsBatch: async () => {
    try {
      const response = await request.get('/web/setting/third-party/batch')
      return response
    } catch (error) {
      console.error('批量获取第三方配置失败:', error)
      return { code: 200, data: null }
    }
  },

  // 保存第三方配置
  saveThirdPartyConfig: async (type, data) => {
    try {
      const response = await request.post(`/web/setting/third-party/${type}`, data)
      return response
    } catch (error) {
      console.error('保存第三方配置失败:', error)
      throw error
    }
  },

  // 测试第三方连接
  testThirdPartyConnection: async (type, config) => {
    try {
      const response = await request.post(`/web/setting/third-party/${type}/test`, config)
      return response
    } catch (error) {
      console.error('测试第三方连接失败:', error)
      throw error
    }
  }
}

/**
 * 系统信息API接口
 */
export const systemInfoService = {
  // 获取系统基本信息
  getSystemInfo: async () => {
    try {
      const response = await request.get('/api/web/setting/system/info')
      return response
    } catch (error) {
      console.error('获取系统信息失败:', error)
      throw error
    }
  },

  // 获取系统运行状态
  getSystemStatus: async () => {
    try {
      const response = await request.get('/api/web/setting/system/status')
      return response
    } catch (error) {
      console.error('获取系统状态失败:', error)
      throw error
    }
  },

  // 刷新系统信息
  refreshSystemInfo: async () => {
    try {
      const response = await request.post('/web/setting/system/refresh')
      return response
    } catch (error) {
      console.error('刷新系统信息失败:', error)
      throw error
    }
  },

  // 清理系统缓存
  clearSystemCache: async () => {
    try {
      const response = await request.post('/web/setting/system/cache/clear')
      return response
    } catch (error) {
      console.error('清理系统缓存失败:', error)
      throw error
    }
  },

  // 导出系统信息
  exportSystemInfo: async () => {
    try {
      const response = await request.get('/api/web/setting/system/export', {
        responseType: 'blob'
      })
      return response
    } catch (error) {
      console.error('导出系统信息失败:', error)
      throw error
    }
  }
}

/**
 * 系统参数API接口
 */
export const systemParamsService = {
  // 获取系统参数列表
  getParamsList: async (params) => {
    try {
      const response = await request.post('/web/setting/params/list', params)
      return response
    } catch (error) {
      console.error('获取系统参数列表失败:', error)
      throw error
    }
  },

  // 获取系统参数详情
  getParamDetail: async (id) => {
    try {
      const response = await request.get(`/api/web/setting/params/${id}`)
      return response
    } catch (error) {
      console.error('获取系统参数详情失败:', error)
      throw error
    }
  },

  // 创建系统参数
  createParam: async (data) => {
    try {
      const response = await request.post('/web/setting/params', data)
      return response
    } catch (error) {
      console.error('创建系统参数失败:', error)
      throw error
    }
  },

  // 更新系统参数
  updateParam: async (id, data) => {
    try {
      const response = await request.put(`/api/web/setting/params/${id}`, data)
      return response
    } catch (error) {
      console.error('更新系统参数失败:', error)
      throw error
    }
  },

  // 删除系统参数
  deleteParam: async (id) => {
    try {
      const response = await request.delete(`/api/web/setting/params/${id}`)
      return response
    } catch (error) {
      console.error('删除系统参数失败:', error)
      throw error
    }
  },

  // 批量删除系统参数
  batchDeleteParams: async (ids) => {
    try {
      const response = await request.post('/web/setting/params/batch-delete', { ids })
      return response
    } catch (error) {
      console.error('批量删除系统参数失败:', error)
      throw error
    }
  }
}

/**
 * 系统日志API接口
 */
export const systemLogsService = {
  // 获取系统日志列表
  getLogsList: async (params) => {
    try {
      const response = await request.post('/web/setting/logs/list', params)
      return response
    } catch (error) {
      console.error('获取系统日志列表失败:', error)
      throw error
    }
  },

  // 获取日志详情
  getLogDetail: async (id) => {
    try {
      const response = await request.get(`/api/web/setting/logs/${id}`)
      return response
    } catch (error) {
      console.error('获取日志详情失败:', error)
      throw error
    }
  },

  // 清理系统日志
  clearLogs: async (params) => {
    try {
      const response = await request.post('/web/setting/logs/clear', params)
      return response
    } catch (error) {
      console.error('清理系统日志失败:', error)
      throw error
    }
  },

  // 导出系统日志
  exportLogs: async (params) => {
    try {
      const response = await request.post('/web/setting/logs/export', params, {
        responseType: 'blob'
      })
      return response
    } catch (error) {
      console.error('导出系统日志失败:', error)
      throw error
    }
  }
}

/**
 * 系统备份API接口
 */
export const systemBackupService = {
  // 获取备份列表
  getBackupList: async (params) => {
    try {
      const response = await request.post('/web/setting/backup/list', params)
      return response
    } catch (error) {
      console.error('获取备份列表失败:', error)
      throw error
    }
  },

  // 创建备份
  createBackup: async (data) => {
    try {
      const response = await request.post('/web/setting/backup/create', data)
      return response
    } catch (error) {
      console.error('创建备份失败:', error)
      throw error
    }
  },

  // 恢复备份
  restoreBackup: async (id) => {
    try {
      const response = await request.post(`/api/web/setting/backup/restore/${id}`)
      return response
    } catch (error) {
      console.error('恢复备份失败:', error)
      throw error
    }
  },

  // 删除备份
  deleteBackup: async (id) => {
    try {
      const response = await request.delete(`/api/web/setting/backup/${id}`)
      return response
    } catch (error) {
      console.error('删除备份失败:', error)
      throw error
    }
  },

  // 下载备份
  downloadBackup: async (id) => {
    try {
      const response = await request.get(`/api/web/setting/backup/download/${id}`, {
        responseType: 'blob'
      })
      return response
    } catch (error) {
      console.error('下载备份失败:', error)
      throw error
    }
  }
}

/**
 * 系统监控API接口
 */
export const systemMonitorService = {
  // 获取监控数据
  getMonitorData: async () => {
    try {
      const response = await request.get('/api/web/setting/monitor/data')
      return response
    } catch (error) {
      console.error('获取监控数据失败:', error)
      throw error
    }
  },

  // 获取性能指标
  getPerformanceMetrics: async (params) => {
    try {
      const response = await request.post('/web/setting/monitor/performance', params)
      return response
    } catch (error) {
      console.error('获取性能指标失败:', error)
      throw error
    }
  },

  // 获取服务状态
  getServiceStatus: async () => {
    try {
      const response = await request.get('/api/web/setting/monitor/services')
      return response
    } catch (error) {
      console.error('获取服务状态失败:', error)
      throw error
    }
  },

  // 获取告警信息
  getAlerts: async (params) => {
    try {
      const response = await request.post('/web/setting/monitor/alerts', params)
      return response
    } catch (error) {
      console.error('获取告警信息失败:', error)
      throw error
    }
  }
}

// 为了保持向后兼容，单独导出函数
export const {
  getSystemBasicConfig,
  saveSystemBasicConfig,
  getNetworkConfig,
  saveNetworkConfig,
  getFileConfig,
  saveFileConfig,
  getThirdPartyConfig,
  getThirdPartyConfigsBatch,
  saveThirdPartyConfig,
  testThirdPartyConnection
} = systemBasicService

export const {
  getSystemInfo,
  getSystemStatus,
  refreshSystemInfo,
  clearSystemCache,
  exportSystemInfo
} = systemInfoService

export const {
  getParamsList,
  getParamDetail,
  createParam,
  updateParam,
  deleteParam,
  batchDeleteParams
} = systemParamsService

export const {
  getLogsList,
  getLogDetail,
  clearLogs,
  exportLogs
} = systemLogsService

export const {
  getBackupList,
  createBackup,
  restoreBackup,
  deleteBackup,
  downloadBackup
} = systemBackupService

export const {
  getMonitorData,
  getPerformanceMetrics,
  getServiceStatus,
  getAlerts
} = systemMonitorService

// 为了保持向后兼容，导出统一的settingApi
export const settingApi = {
  ...systemBasicService,
  ...systemInfoService,
  ...systemParamsService,
  ...systemLogsService,
  ...systemBackupService,
  ...systemMonitorService
}

// 导出服务实例
export default {
  systemBasicService,
  systemInfoService,
  systemParamsService,
  systemLogsService,
  systemBackupService,
  systemMonitorService,
  settingApi
}