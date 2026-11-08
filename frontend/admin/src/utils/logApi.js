/**
 * 日志 API 响应解析与字段映射（审计库 MySQL + ELK 运行日志）
 */

/** 解析 BFF 统一响应 */
export function unwrap(res) {
  if (res?.code === 200) return res.data
  throw new Error(res?.message || '请求失败')
}

/** 解析分页结果 */
export function unwrapPage(res) {
  const data = unwrap(res) || {}
  return {
    records: data.records || [],
    total: data.total ?? 0,
    current: data.current ?? 1,
    size: data.size ?? 20,
    elkEnabled: data.elkEnabled !== false,
    elkError: data.elkError || ''
  }
}

/** 默认查询最近 N 小时（ELK 强制时间范围，控制性能） */
export function defaultDateRange(hours = 24) {
  const end = new Date()
  const start = new Date(end.getTime() - hours * 3600 * 1000)
  const fmt = (d) => {
    const p = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
  }
  return [fmt(start), fmt(end)]
}

export function mapOperationRow(r) {
  return {
    ...r,
    timestamp: r.createTime,
    operationDesc: r.operation,
    operationType: r.requestMethod,
    requestMethod: r.requestMethod || '',
    requestUrl: r.requestUrl || '',
    requestParams: r.requestParams,
    method: r.method,
    executionTime: r.executionTime,
    errorMsg: r.errorMsg,
    ipAddress: r.ip,
    operationTime: r.createTime,
    status: r.status === 1 ? 'success' : 'fail',
    source: 'audit'
  }
}

export function mapBusinessRow(r) {
  return {
    ...r,
    timestamp: r.createTime,
    userName: r.username,
    description: r.detail,
    result: 'SUCCESS',
    ipAddress: '',
    requestParams: r.beforeData,
    responseData: r.afterData,
    source: 'audit'
  }
}

export function mapAccessRow(r) {
  return {
    ...r,
    timestamp: r.createTime,
    ipAddress: r.ip,
    status: r.statusCode,
    responseTime: r.responseTime,
    source: 'audit'
  }
}

export function mapLoginRow(r) {
  return {
    ...r,
    loginTime: r.createTime,
    status: r.loginStatus === 1 ? 'success' : 'fail',
    userAgent: [r.device, r.browser, r.os].filter(Boolean).join(' / '),
    source: 'audit'
  }
}

export function mapAuditErrorRow(r) {
  return {
    ...r,
    timestamp: r.createTime,
    level: 'ERROR',
    type: 'system',
    service: r.module,
    message: r.errorMessage,
    exceptionClass: r.errorType,
    stackTrace: r.stackTrace,
    status: 'UNRESOLVED',
    count: 1,
    source: 'audit'
  }
}

export function mapElkRow(r) {
  return {
    ...r,
    source: 'elk'
  }
}
