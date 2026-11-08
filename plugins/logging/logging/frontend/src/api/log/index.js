/**
 * 日志管理模块 API
 *
 * 所有日志相关接口定义，统一通过 BFF 网关 /web/log 路径访问。
 * 基于本地封装的请求工具（utils/request.js）进行请求。
 *
 * @author Amos
 * @since 2026-04-13
 */

import request from '../../utils/request.js'

// Keep the legacy client call shape locally while routing Logging endpoints
// through the paid-plugin context instead of the host Core API adapter.
const apiClient = {
  get(url, params = {}, config = {}) {
    return request.get(url, { ...config, params })
  },
  post(url, data = {}, config = {}) {
    return request.post(url, data, config)
  },
  delete(url, config = {}) {
    return request.delete(url, config)
  }
}

// ==================== API 端点常量 ====================

/** 日志模块基础路径 */
const LOG_BASE = '/log'

/**
 * 日志管理 API 端点常量
 */
export const LOG_ENDPOINTS = {
  // 分页查询
  OPERATION_PAGE: `${LOG_BASE}/operation/page`,
  ACCESS_PAGE:    `${LOG_BASE}/access/page`,
  LOGIN_PAGE:     `${LOG_BASE}/login/page`,
  ERROR_PAGE:     `${LOG_BASE}/error/page`,
  BUSINESS_PAGE:  `${LOG_BASE}/business/page`,
  SYSTEM_PAGE:    `${LOG_BASE}/system/page`,

  // 日志分析
  ANALYSIS_OVERVIEW:   `${LOG_BASE}/analysis/overview`,
  ANALYSIS_TREND:      `${LOG_BASE}/analysis/trend`,
  ANALYSIS_TOP_ERRORS: `${LOG_BASE}/analysis/top-errors`,

  // 日志设置
  SETTING_LIST:   `${LOG_BASE}/setting/list`,
  SETTING_UPDATE: `${LOG_BASE}/setting/update`,

  // 日志清理（type 为动态路径参数）
  CLEAN: (type) => `${LOG_BASE}/${type}/clean`,

  // ELK 运行日志
  ELK_PAGE: `${LOG_BASE}/elk/page`,
  ELK_LEVEL_STATS: `${LOG_BASE}/elk/level-stats`
}

// ==================== 操作日志 API ====================

/**
 * 操作日志 API
 */
export const operationLogAPI = {
  /**
   * 操作日志分页查询
   *
   * @param {Object} params       查询条件
   * @param {string} [params.username]      用户名
   * @param {string} [params.module]        模块
   * @param {string} [params.operation]     操作类型
   * @param {string} [params.status]        执行状态
   * @param {string} [params.startTime]     开始时间（yyyy-MM-dd HH:mm:ss）
   * @param {string} [params.endTime]       结束时间（yyyy-MM-dd HH:mm:ss）
   * @param {string} [params.keyword]       关键词
   * @param {number} [current=1]            当前页码
   * @param {number} [size=20]              每页条数
   * @returns {Promise<Object>}  分页结果 { code, data: { records, total, size, current } }
   */
  getPage: (params = {}, current = 1, size = 20) =>
    apiClient.post(LOG_ENDPOINTS.OPERATION_PAGE, params, {
      params: { current, size }
    }),

  /**
   * 清理操作日志
   *
   * @param {number} days  保留最近 N 天的数据，超出部分将被删除
   * @returns {Promise<Object>}  操作结果
   */
  clean: (days) =>
    apiClient.delete(LOG_ENDPOINTS.CLEAN('operation'), {
      params: { days }
    })
}

// ==================== 访问日志 API ====================

/**
 * 访问日志 API
 */
export const accessLogAPI = {
  /**
   * 访问日志分页查询
   *
   * @param {Object} params       查询条件
   * @param {string} [params.username]    用户名
   * @param {string} [params.method]      HTTP 方法（GET/POST/PUT/DELETE）
   * @param {string} [params.path]        请求路径
   * @param {string} [params.statusCode]  响应状态码
   * @param {string} [params.startTime]   开始时间
   * @param {string} [params.endTime]     结束时间
   * @param {string} [params.ip]          IP 地址
   * @param {number} [current=1]          当前页码
   * @param {number} [size=20]            每页条数
   * @returns {Promise<Object>}  分页结果
   */
  getPage: (params = {}, current = 1, size = 20) =>
    apiClient.post(LOG_ENDPOINTS.ACCESS_PAGE, params, {
      params: { current, size }
    }),

  /**
   * 清理访问日志
   *
   * @param {number} days  保留最近 N 天
   * @returns {Promise<Object>}  操作结果
   */
  clean: (days) =>
    apiClient.delete(LOG_ENDPOINTS.CLEAN('access'), {
      params: { days }
    })
}

// ==================== 登录日志 API ====================

/**
 * 登录日志 API
 */
export const loginLogAPI = {
  /**
   * 登录日志分页查询
   *
   * @param {Object} params       查询条件
   * @param {string} [params.username]      用户名
   * @param {string} [params.loginType]     登录方式
   * @param {string} [params.loginStatus]   登录结果（success/fail）
   * @param {string} [params.startTime]     开始时间
   * @param {string} [params.endTime]       结束时间
   * @param {string} [params.ip]            IP 地址
   * @param {number} [current=1]            当前页码
   * @param {number} [size=20]              每页条数
   * @returns {Promise<Object>}  分页结果
   */
  getPage: (params = {}, current = 1, size = 20) =>
    apiClient.post(LOG_ENDPOINTS.LOGIN_PAGE, params, {
      params: { current, size }
    }),

  /**
   * 清理登录日志
   *
   * @param {number} days  保留最近 N 天
   * @returns {Promise<Object>}  操作结果
   */
  clean: (days) =>
    apiClient.delete(LOG_ENDPOINTS.CLEAN('login'), {
      params: { days }
    })
}

// ==================== 错误日志 API ====================

/**
 * 错误日志 API
 */
export const errorLogAPI = {
  /**
   * 错误日志分页查询
   *
   * @param {Object} params       查询条件
   * @param {string} [params.module]      模块名称
   * @param {string} [params.errorType]   错误类型
   * @param {string} [params.startTime]   开始时间
   * @param {string} [params.endTime]     结束时间
   * @param {string} [params.keyword]     关键词
   * @param {number} [current=1]          当前页码
   * @param {number} [size=20]            每页条数
   * @returns {Promise<Object>}  分页结果
   */
  getPage: (params = {}, current = 1, size = 20) =>
    apiClient.post(LOG_ENDPOINTS.ERROR_PAGE, params, {
      params: { current, size }
    }),

  /**
   * 清理错误日志
   *
   * @param {number} days  保留最近 N 天
   * @returns {Promise<Object>}  操作结果
   */
  clean: (days) =>
    apiClient.delete(LOG_ENDPOINTS.CLEAN('error'), {
      params: { days }
    })
}

// ==================== 业务日志 API ====================

/**
 * 业务日志 API
 */
export const businessLogAPI = {
  /**
   * 业务日志分页查询
   *
   * @param {Object} params       查询条件
   * @param {string} [params.username]    用户名
   * @param {string} [params.module]      业务模块
   * @param {string} [params.action]      操作类型
   * @param {string} [params.targetType]  目标对象类型
   * @param {string} [params.startTime]   开始时间
   * @param {string} [params.endTime]     结束时间
   * @param {string} [params.keyword]     关键词
   * @param {number} [current=1]          当前页码
   * @param {number} [size=20]            每页条数
   * @returns {Promise<Object>}  分页结果
   */
  getPage: (params = {}, current = 1, size = 20) =>
    apiClient.post(LOG_ENDPOINTS.BUSINESS_PAGE, params, {
      params: { current, size }
    }),

  /**
   * 清理业务日志
   *
   * @param {number} days  保留最近 N 天
   * @returns {Promise<Object>}  操作结果
   */
  clean: (days) =>
    apiClient.delete(LOG_ENDPOINTS.CLEAN('business'), {
      params: { days }
    })
}

// ==================== 系统日志 API ====================

/**
 * 系统日志 API
 */
export const systemLogAPI = {
  /**
   * 系统日志分页查询
   *
   * @param {Object} params       查询条件
   * @param {string} [params.username]    操作用户名
   * @param {string} [params.module]      模块名称
   * @param {string} [params.action]      操作类型
   * @param {string} [params.startTime]   开始时间
   * @param {string} [params.endTime]     结束时间
   * @param {string} [params.keyword]     关键词
   * @param {number} [current=1]          当前页码
   * @param {number} [size=20]            每页条数
   * @returns {Promise<Object>}  分页结果
   */
  getPage: (params = {}, current = 1, size = 20) =>
    apiClient.post(LOG_ENDPOINTS.SYSTEM_PAGE, params, {
      params: { current, size }
    }),

  /**
   * 清理系统日志
   *
   * @param {number} days  保留最近 N 天
   * @returns {Promise<Object>}  操作结果
   */
  clean: (days) =>
    apiClient.delete(LOG_ENDPOINTS.CLEAN('system'), {
      params: { days }
    })
}

// ==================== 日志分析 API ====================

/**
 * 日志分析 API
 */
export const logAnalysisAPI = {
  /**
   * 获取日志统计概览
   *
   * 返回各类日志总数、今日新增量、错误率等汇总指标。
   *
   * @returns {Promise<Object>}  统计概览数据
   */
  getOverview: () =>
    apiClient.get(LOG_ENDPOINTS.ANALYSIS_OVERVIEW),

  /**
   * 获取日志趋势数据
   *
   * 按时间范围统计每天的日志数量，支持按模块和日志类型过滤。
   *
   * @param {Object} params            查询条件
   * @param {string} [params.startDate]  开始日期（yyyy-MM-dd）
   * @param {string} [params.endDate]    结束日期（yyyy-MM-dd）
   * @param {string} [params.logType]    日志类型（operation/access/login/error/business）
   * @param {string} [params.module]     模块名称
   * @returns {Promise<Array>}  趋势数据列表，每项包含 date 和 count
   */
  getTrend: (params = {}) =>
    apiClient.post(LOG_ENDPOINTS.ANALYSIS_TREND, params),

  /**
   * 获取热门错误排行
   *
   * 按错误类型分组统计，返回出现次数最多的前 N 条。
   *
   * @param {number} [limit=10]  返回最大条数
   * @returns {Promise<Array>}  错误排行列表，每项包含 errorType 和 count
   */
  getTopErrors: (limit = 10) =>
    apiClient.get(LOG_ENDPOINTS.ANALYSIS_TOP_ERRORS, { limit })
}

// ==================== 日志设置 API ====================

/**
 * 日志设置 API
 */
export const logSettingAPI = {
  /**
   * 获取日志配置项列表
   *
   * 返回系统中所有已配置的日志模块设置。
   *
   * @returns {Promise<Array>}  配置项列表
   */
  getList: () =>
    apiClient.post(LOG_ENDPOINTS.SETTING_LIST),

  /**
   * 更新日志配置项
   *
   * @param {Object} params          更新参数
   * @param {string} params.key      配置键名
   * @param {string} params.value    配置新值
   * @returns {Promise<Object>}  操作结果
   */
  update: (params) =>
    apiClient.post(LOG_ENDPOINTS.SETTING_UPDATE, params)
}

// ==================== ELK 运行日志 API ====================

/**
 * ELK 运行日志（系统日志 / 运行错误，来自 Elasticsearch）
 */
export const elkLogAPI = {
  getPage: (params = {}, current = 1, size = 20) =>
    apiClient.post(LOG_ENDPOINTS.ELK_PAGE, params, { params: { current, size } }),

  levelStats: (params = {}) =>
    apiClient.post(LOG_ENDPOINTS.ELK_LEVEL_STATS, params)
}

// ==================== 统一导出 ====================

/**
 * 日志管理服务对象（统一入口）
 */
export const logService = {
  operation: operationLogAPI,
  access:    accessLogAPI,
  login:     loginLogAPI,
  error:     errorLogAPI,
  business:  businessLogAPI,
  system:    systemLogAPI,
  analysis:  logAnalysisAPI,
  setting:   logSettingAPI,
  elk:       elkLogAPI
}

export default logService
