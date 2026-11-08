/**
 * 合同管理API模块
 * @author Amos
 * @since 2024-01-01
 */

// 导入主服务的统一API服务
import request from '@/utils/request'

// 创建contractApi实例
const contractApi = request

/**
 * 合同管理API接口
 */
export const contractService = {
  /**
   * 获取合同列表
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 分页合同列表
   */
  getContractList: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/list', params)
      return response
    } catch (error) {
      console.error('获取合同列表失败:', error)
      throw error
    }
  },

  /**
   * 获取合同详情
   * @param {number} id 合同ID
   * @returns {Promise<Object>} 合同详细信息
   */
  getContractDetail: async (id) => {
    try {
      const response = await contractApi.post(`/web/oa/contract/${id}`, {})
      return response
    } catch (error) {
      console.error('获取合同详情失败:', error)
      throw error
    }
  },

  /**
   * 获取合同全部附件
   * @param {number|string} contractId 合同ID
   * @returns {Promise<Object>} 附件列表
   */
  getAllContractFiles: async (contractId) => {
    try {
      const response = await contractApi.post(`/web/oa/contract/file/all/${contractId}`, {})
      return response
    } catch (error) {
      console.error('获取合同附件失败:', error)
      throw error
    }
  },

  /**
   * 创建合同
   * @param {Object} contractData 合同数据
   * @returns {Promise<Object>} 创建结果
   */
  createContract: async (contractData) => {
    try {
      const response = await contractApi.post('/web/oa/contract/create', contractData)
      return response
    } catch (error) {
      console.error('创建合同失败:', error)
      throw error
    }
  },

  /**
   * 更新合同
   * @param {number} id 合同ID
   * @param {Object} contractData 更新的合同数据
   * @returns {Promise<Object>} 更新结果
   */
  updateContract: async (id, contractData) => {
    try {
      const response = await contractApi.post(`/web/oa/contract/update/${id}`, contractData)
      return response
    } catch (error) {
      console.error('更新合同失败:', error)
      throw error
    }
  },

  /**
   * 删除合同
   * @param {number} id 合同ID
   * @returns {Promise<Object>} 删除结果
   */
  deleteContract: async (id) => {
    try {
      const response = await contractApi.post(`/web/oa/contract/delete/${id}`, {})
      return response
    } catch (error) {
      console.error('删除合同失败:', error)
      throw error
    }
  },

  /**
   * 导出合同列表为 Excel 文件
   * @param {Object} params 导出参数（与列表查询参数保持一致）
   * @returns {Promise<Blob>} 返回 Blob 对象，需在调用方创建下载链接
   */
  exportContracts: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/export', params, {
        responseType: 'blob'
      })
      return response
    } catch (error) {
      console.error('导出合同失败:', error)
      throw error
    }
  },

  /**
   * 生成合同编号
   * @returns {Promise<Object>} 合同编号
   */
  generateContractNumber: async () => {
    try {
      // POST请求需要传递一个空对象，避免后端400错误
      const response = await contractApi.post('/web/oa/contract/generate-number', {})
      return response
    } catch (error) {
      console.error('生成合同编号失败:', error)
      throw error
    }
  },

  /**
   * 作废合同（带原因）
   * @param {number} id 合同ID
   * @param {string} reason 作废原因
   * @returns {Promise<Object>} 作废结果
   */
  invalidateContract: async (id, reason) => {
    try {
      const response = await contractApi.post(`/web/oa/contracts/invalidate/${id}`, { reason })
      return response
    } catch (error) {
      console.error('作废合同失败:', error)
      throw error
    }
  },

  /**
   * 作废合同
   * @param {number} id 合同ID
   * @returns {Promise<Object>} 作废结果
   */
  disableContract: async (id) => {
    try {
      const response = await contractApi.post(`/web/oa/contract/disable/${id}`, {})
      return response
    } catch (error) {
      console.error('作废合同失败:', error)
      throw error
    }
  },

  /**
   * 启用合同
   * @param {number} id 合同ID
   * @returns {Promise<Object>} 启用结果
   */
  enableContract: async (id) => {
    try {
      const response = await contractApi.post(`/web/oa/contract/enable/${id}`, {})
      return response
    } catch (error) {
      console.error('启用合同失败:', error)
      throw error
    }
  },

  /**
   * 生成签署链接
   * @param {number} contractId 合同ID
   * @returns {Promise<Object>} 签署链接
   */
  generateSignLink: async (contractId) => {
    try {
      // 直接生成带合同ID的链接，不再使用token
      const frontendBaseUrl = window.location.origin
      const signLink = `${frontendBaseUrl}/contract/sign/${contractId}`
      return {
        code: 1,
        data: {
          signLink: signLink,
          contractId: contractId,
          expireDays: 0 // 不再有有效期限制，通过登录验证
        }
      }
    } catch (error) {
      console.error('生成签署链接失败:', error)
      throw error
    }
  },

  /**
   * 验证合同签署手机号（验证码+手机号匹配）
   * @param {number} contractId 合同ID
   * @param {string} phone 手机号
   * @param {string} code 验证码
   * @returns {Promise<Object>} 验证结果
   */
  verifyContractPhone: async (contractId, phone, code) => {
    try {
      const response = await contractApi.post('/web/oa/contract/verify-phone', {
        contractId: contractId,
        phone: phone,
        code: code
      })
      return response
    } catch (error) {
      console.error('验证手机号失败:', error)
      throw error
    }
  },

  /**
   * 通过合同ID获取合同信息（用于签署页面，需要登录并验证手机号）
   * @param {number} contractId 合同ID
   * @param {string} loginPhone 登录的手机号
   * @returns {Promise<Object>} 合同信息
   */
  getContractForSign: async (contractId, loginPhone) => {
    try {
      const response = await contractApi.post(`/web/oa/contract/sign-info/${contractId}`, {
        loginPhone: loginPhone
      })
      return response
    } catch (error) {
      console.error('获取合同信息失败:', error)
      throw error
    }
  },

  /**
   * 签署合同
   * @param {number} contractId 合同ID
   * @param {Object} signData 签署数据（签署人姓名、手机号等）
   * @returns {Promise<Object>} 签署结果
   */
  signContract: async (contractId, signData) => {
    try {
      const response = await contractApi.post(`/web/oa/contract/sign/${contractId}`, signData)
      return response
    } catch (error) {
      console.error('签署失败:', error)
      throw error
    }
  },

  // ================= 合同分类相关 =================

  /**
   * 获取分类树形结构
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 分类树
   */
  getCategoryTree: async (params = {}) => {
    try {
      const response = await contractApi.get('/web/oa/contract/category/tree', { params: params || {} })
      return response
    } catch (error) {
      console.error('获取分类树失败:', error)
      throw error
    }
  },

  /**
   * 创建分类
   * @param {Object} categoryData 分类数据
   * @returns {Promise<Object>} 创建结果
   */
  createCategory: async (categoryData) => {
    try {
      const response = await contractApi.post('/web/oa/contract/category/create', categoryData)
      return response
    } catch (error) {
      console.error('创建分类失败:', error)
      throw error
    }
  },

  /**
   * 更新分类
   * @param {number} id 分类ID
   * @param {Object} categoryData 分类数据
   * @returns {Promise<Object>} 更新结果
   */
  updateCategory: async (id, categoryData) => {
    try {
      const response = await contractApi.post(`/web/oa/contract/category/update/${id}`, categoryData)
      return response
    } catch (error) {
      console.error('更新分类失败:', error)
      throw error
    }
  },

  /**
   * 删除分类
   * @param {number} id 分类ID
   * @returns {Promise<Object>} 删除结果
   */
  deleteCategory: async (id) => {
    try {
      const response = await contractApi.delete(`/web/oa/contract/category/delete/${id}`)
      return response
    } catch (error) {
      console.error('删除分类失败:', error)
      throw error
    }
  },

  // ================= 合同审批相关 =================

  /**
   * 获取审批列表
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 审批列表
   */
  getApprovalList: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/approval/list', params)
      return response
    } catch (error) {
      console.error('获取审批列表失败:', error)
      throw error
    }
  },

  /**
   * 提交审批
   * @param {Object} approvalData 审批数据
   * @returns {Promise<Object>} 提交结果
   */
  submitApproval: async (approvalData) => {
    try {
      const response = await contractApi.post('/web/oa/contract/approval/submit', approvalData)
      return response
    } catch (error) {
      console.error('提交审批失败:', error)
      throw error
    }
  },

  /**
   * 审批操作
   * @param {Object} actionData 操作数据 { approvalId, action, comment }
   * @returns {Promise<Object>} 操作结果
   */
  approvalAction: async (actionData) => {
    try {
      const response = await contractApi.post('/web/oa/contract/approval/process', actionData)
      return response
    } catch (error) {
      console.error('审批操作失败:', error)
      throw error
    }
  },

  /**
   * 撤回审批
   * @param {number} id 审批ID
   * @returns {Promise<Object>} 撤回结果
   */
  withdrawApproval: async (id) => {
    try {
      const response = await contractApi.post(`/web/oa/contract/approval/withdraw/${id}`)
      return response
    } catch (error) {
      console.error('撤回审批失败:', error)
      throw error
    }
  },

  // ================= 合同模板相关 =================

  /**
   * 获取模板列表
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 模板列表
   */
  getTemplates: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/template/templates', params)
      return response
    } catch (error) {
      console.error('获取模板列表失败:', error)
      throw error
    }
  },

  /**
   * 生成合同（从模板生成）
   * @param {Object} generateData 生成数据
   * @returns {Promise<Object>} 生成结果
   */
  generateContract: async (generateData) => {
    try {
      const response = await contractApi.post('/web/oa/contract/generate', generateData)
      return response
    } catch (error) {
      console.error('生成合同失败:', error)
      throw error
    }
  },

  /**
   * 预览合同（从模板预览）
   * @param {Object} previewData 预览数据
   * @returns {Promise<Object>} 预览结果
   */
  previewContract: async (previewData) => {
    try {
      const response = await contractApi.post('/web/oa/contract/preview', previewData)
      return response
    } catch (error) {
      console.error('预览合同失败:', error)
      throw error
    }
  },

  /**
   * 下载合同
   * @param {number} id 合同ID
   * @returns {Promise<Object>} 下载结果
   */
  downloadContract: async (id) => {
    try {
      const response = await contractApi.get(`/web/oa/contract/download/${id}`)
      return response
    } catch (error) {
      console.error('下载合同失败:', error)
      throw error
    }
  },

  // ================= 合同提醒相关 =================

  /**
   * 获取提醒列表
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 提醒列表
   */
  getReminderList: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/reminder/list', params)
      return response
    } catch (error) {
      console.error('获取提醒列表失败:', error)
      throw error
    }
  },

  /**
   * 获取提醒统计
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 统计结果
   */
  getReminderStatistics: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/reminder/statistics', params)
      return response
    } catch (error) {
      console.error('获取提醒统计失败:', error)
      throw error
    }
  },

  /**
   * 标记提醒为已读
   * @param {Object} params 参数（包含reminderId）
   * @returns {Promise<Object>} 标记结果
   */
  markReminderAsRead: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/reminder/mark-read', params)
      return response
    } catch (error) {
      console.error('标记提醒为已读失败:', error)
      throw error
    }
  },

  /**
   * 创建提醒
   * @param {Object} reminderData 提醒数据
   * @returns {Promise<Object>} 创建结果
   */
  createReminder: async (reminderData) => {
    try {
      const response = await contractApi.post('/web/oa/contract/reminder/create', reminderData)
      return response
    } catch (error) {
      console.error('创建提醒失败:', error)
      throw error
    }
  },

  /**
   * 删除提醒
   * @param {number} id 提醒ID
   * @returns {Promise<Object>} 删除结果
   */
  deleteReminder: async (id) => {
    try {
      const response = await contractApi.delete(`/web/oa/contract/reminder/delete/${id}`)
      return response
    } catch (error) {
      console.error('删除提醒失败:', error)
      throw error
    }
  },


  /**
   * 保存提醒设置
   * @param {Object} settingsData 设置数据
   * @returns {Promise<Object>} 保存结果
   */
  saveReminderSettings: async (settingsData) => {
    try {
      const response = await contractApi.post('/web/oa/contract/reminder/settings', settingsData)
      return response
    } catch (error) {
      console.error('保存提醒设置失败:', error)
      throw error
    }
  },

  /**
   * 获取提醒设置
   * @returns {Promise<Object>} 设置结果
   */
  getReminderSettings: async () => {
    try {
      const response = await contractApi.get('/web/oa/contract/reminder/settings/get')
      return response
    } catch (error) {
      console.error('获取提醒设置失败:', error)
      throw error
    }
  },

  // ================= 合同分析相关 =================

  /**
   * 获取分析统计
   * @param {Object} params 查询参数
   * @param {Object} config 可选，透传 axios 配置（如 skipAuthRedirect/skipErrorMessage）
   * @returns {Promise<Object>} 统计结果
   */
  getAnalyticsStatistics: async (params = {}, config = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/analytics/statistics', params, config)
      return response
    } catch (error) {
      console.error('获取分析统计失败:', error)
      throw error
    }
  },

  /**
   * 获取趋势分析
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 趋势结果
   */
  getAnalyticsTrend: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/analytics/trend', params)
      return response
    } catch (error) {
      console.error('获取趋势分析失败:', error)
      throw error
    }
  },

  /**
   * 获取类型分布
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 分布结果
   */
  getTypeDistribution: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/analytics/type-distribution', params)
      return response
    } catch (error) {
      console.error('获取类型分布失败:', error)
      throw error
    }
  },

  /**
   * 获取金额分析
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 分析结果
   */
  getAmountAnalysis: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/analytics/amount-analysis', params)
      return response
    } catch (error) {
      console.error('获取金额分析失败:', error)
      throw error
    }
  },

  /**
   * 获取状态分布
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 分布结果
   */
  getStatusDistribution: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/analytics/status-distribution', params)
      return response
    } catch (error) {
      console.error('获取状态分布失败:', error)
      throw error
    }
  },

  /**
   * 获取预测分析
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 预测结果
   */
  getPrediction: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/analytics/prediction', params)
      return response
    } catch (error) {
      console.error('获取预测分析失败:', error)
      throw error
    }
  },

  /**
   * 获取风险分析
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 风险结果
   */
  getRiskAnalysis: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/analytics/risk-analysis', params)
      return response
    } catch (error) {
      console.error('获取风险分析失败:', error)
      throw error
    }
  },

  /**
   * 获取分析摘要
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 摘要结果
   */
  getAnalysisSummary: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/analysis/summary', params)
      return response
    } catch (error) {
      console.error('获取分析摘要失败:', error)
      throw error
    }
  },

  /**
   * 获取分析图表
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 图表结果
   */
  getAnalysisChart: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/analysis/chart', params)
      return response
    } catch (error) {
      console.error('获取分析图表失败:', error)
      throw error
    }
  },

  /**
   * 获取对比分析
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 对比结果
   */
  getAnalysisComparison: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/analysis/comparison', params)
      return response
    } catch (error) {
      console.error('获取对比分析失败:', error)
      throw error
    }
  },

  /**
   * 获取表格摘要
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 摘要结果
   */
  getAnalysisTableSummary: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/analysis/table/summary', params)
      return response
    } catch (error) {
      console.error('获取表格摘要失败:', error)
      throw error
    }
  },

  /**
   * 获取表格详情
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 详情结果
   */
  getAnalysisTableDetail: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/analysis/table/detail', params)
      return response
    } catch (error) {
      console.error('获取表格详情失败:', error)
      throw error
    }
  },

  /**
   * 获取表格趋势
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 趋势结果
   */
  getAnalysisTableTrend: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/analysis/table/trend', params)
      return response
    } catch (error) {
      console.error('获取表格趋势失败:', error)
      throw error
    }
  },

  /**
   * 导出分析报告
   * @param {Object} params 导出参数
   * @returns {Promise<Object>} 导出结果
   */
  exportAnalysisReport: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/analysis/export', params)
      return response
    } catch (error) {
      console.error('导出分析报告失败:', error)
      throw error
    }
  },

  /**
   * 搜索合同
   * @param {Object} params 搜索参数
   * @returns {Promise<Object>} 搜索结果
   */
  searchContracts: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/search', params)
      return response
    } catch (error) {
      console.error('搜索合同失败:', error)
      throw error
    }
  },

  /**
   * 上传合同
   * @param {FormData} formData 文件数据
   * @returns {Promise<Object>} 上传结果
   */
  uploadContract: async (formData) => {
    try {
      const response = await contractApi.post('/web/oa/contract/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      return response
    } catch (error) {
      console.error('上传合同失败:', error)
      throw error
    }
  },

  // ================= 合同监控相关 =================

  /**
   * 获取合同监控列表
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 监控列表
   */
  getMonitorList: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/monitor/list', params)
      return response
    } catch (error) {
      console.error('获取监控列表失败:', error)
      throw error
    }
  },

  /**
   * 获取合同监控统计
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 统计结果
   */
  getMonitorStats: async (params = {}) => {
    try {
      const response = await contractApi.post('/web/oa/contract/monitor/stats', params)
      return response
    } catch (error) {
      console.error('获取监控统计失败:', error)
      throw error
    }
  }
}

// 导出便捷方法
export const {
  getContractList,
  getContractDetail,
  createContract,
  updateContract,
  deleteContract,
  exportContracts,
  generateContractNumber,
  getCategoryTree,
  createCategory,
  updateCategory,
  deleteCategory,
  getApprovalList,
  submitApproval,
  approvalAction,
  withdrawApproval,
  getTemplates,
  generateContract,
  previewContract,
  downloadContract,
  getReminderList,
  createReminder,
  deleteReminder,
  markAsRead,
  getReminderStatistics,
  saveReminderSettings,
  getReminderSettings,
  getAnalyticsStatistics,
  getAnalyticsTrend,
  getTypeDistribution,
  getAmountAnalysis,
  getStatusDistribution,
  getPrediction,
  getRiskAnalysis,
  getAnalysisSummary,
  getAnalysisChart,
  getAnalysisComparison,
  getAnalysisTableSummary,
  getAnalysisTableDetail,
  getAnalysisTableTrend,
  exportAnalysisReport,
  searchContracts,
  uploadContract,
  getMonitorList,
  getMonitorStats
} = contractService

// 导出contractApi实例
export { contractApi }

export default contractService
