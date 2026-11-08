/**
 * 消息中心API接口
 * @description 渠道管理、模板管理、发送记录、收件箱、订阅设置
 */

import request from '@/utils/request'

/**
 * 渠道管理API
 */
export const channelApi = {
  // 获取渠道列表
  getList: (params = {}) => {
    const query = {
      page: params.pageNum || params.page || 1,
      size: params.pageSize || params.size || 100
    }
    return request.get('/web/message/channel/list', { params: query })
  },
  
  // 获取启用的渠道列表
  getEnabled: () => request.get('/web/message/channel/enabled'),
  
  // 获取渠道详情
  getById: (id) => request.get(`/web/message/channel/${id}`),
  
  // 创建渠道
  create: (data) => request.post('/web/message/channel/create', data),
  
  // 更新渠道
  update: (data) => request.put('/web/message/channel/update', data),
  
  // 更新渠道状态
  updateStatus: (id, status) => request.put(`/web/message/channel/${id}/status`, null, { params: { status } }),
  
  // 删除渠道
  delete: (id) => request.delete(`/web/message/channel/${id}`)
}

/**
 * 模板管理API
 */
export const templateApi = {
  // 获取模板列表
  getList: (params = {}) => {
    // 转换参数名称以匹配后端
    const data = {
      page: params.pageNum || params.page || 1,
      size: params.pageSize || params.size || 20
    }
    if (params.bizType) data.bizType = params.bizType
    if (params.keyword) data.keyword = params.keyword
    if (params.channel) data.channel = params.channel
    if (params.status !== undefined && params.status !== null) data.status = params.status
    return request.post('/web/message/template/list', data)
  },
  
  // 获取启用的模板列表
  getEnabled: () => request.get('/web/message/template/enabled'),
  
  // 获取模板详情
  getById: (id) => request.get(`/web/message/template/${id}`),
  
  // 根据编码获取模板
  getByCode: (code) => request.get(`/web/message/template/code/${code}`),
  
  // 创建模板
  create: (data) => request.post('/web/message/template/create', data),
  
  // 更新模板
  update: (data) => request.post('/web/message/template/update', data),
  
  // 更新模板状态
  updateStatus: (id, status) => request.post(`/web/message/template/${id}/status`, null, { params: { status } }),
  
  // 批量更新模板状态
  batchUpdateStatus: (ids, status) => request.post('/web/message/template/batch/status', { ids, status }),
  
  // 删除模板
  delete: (id) => request.delete(`/web/message/template/${id}`)
}

/**
 * 发送记录API
 */
const buildRecordQuery = (params = {}, includePagination = false) => {
  const query = {}
  if (includePagination) {
    query.page = params.pageNum ?? params.page ?? 1
    query.size = params.pageSize ?? params.size ?? 20
  }

  const channel = params.channel || params.channelCode
  const sendStatus = params.sendStatus ?? params.status
  if (params.bizType) query.bizType = params.bizType
  if (params.eventType) query.eventType = params.eventType
  if (channel) query.channel = channel
  if (sendStatus !== undefined && sendStatus !== null && sendStatus !== '') query.sendStatus = sendStatus
  if (params.receiver) query.receiver = params.receiver
  if (params.startTime !== undefined && params.startTime !== null) query.startTime = params.startTime
  if (params.endTime !== undefined && params.endTime !== null) query.endTime = params.endTime
  return query
}

export const recordApi = {
  // 获取发送记录列表
  getList: (params = {}) => request.get('/web/message/record/list', {
    params: buildRecordQuery(params, true)
  }),
  
  // 获取记录详情
  getById: (id) => request.get(`/web/message/record/${id}`),
  
  // 获取发送统计
  getStatistics: (params = {}) => request.get('/web/message/record/statistics', {
    params: buildRecordQuery(params)
  }),
  
  // 重试发送
  resend: (id) => request.post(`/web/message/record/${id}/retry`)
}

/**
 * 收件箱API
 */
export const inboxApi = {
  // 获取收件箱列表
  getList: (params = {}) => {
    const query = {}
    if (params.page != null) query.page = params.page
    if (params.size != null) query.size = params.size
    if (params.isRead != null) query.isRead = params.isRead
    if (params.msgType != null) query.msgType = params.msgType
    return request.get('/web/message/inbox/list', { params: query })
  },
  
  // 获取未读数量
  getUnreadCount: () => request.get('/web/message/inbox/unread-count'),
  
  // 标记已读
  markAsRead: (id) => request.post(`/web/message/inbox/${id}/read`),
  
  // 批量标记已读
  markAllAsRead: () => request.post('/web/message/inbox/read-all'),
  
  // 删除消息
  delete: (id) => request.delete(`/web/message/inbox/${id}`)
}

/**
 * 订阅设置API
 */
export const subscribeApi = {
  // 获取用户订阅设置
  getUserSubscribe: (userId) => request.get(`/web/message/subscribe/user/${userId}`),
  
  // 保存订阅设置
  save: (data) => request.post('/web/message/subscribe/save', data),
  
  // 批量保存订阅设置
  batchSave: (data) => request.post('/web/message/subscribe/batch', data)
}

/**
 * 极光推送API
 */
export const jpushApi = {
  // 获取极光推送配置
  getConfig: (orgId) => request.get('/web/message/jpush/config', { params: { orgId } }),
  
  // 保存极光推送配置
  saveConfig: (data) => request.post('/web/message/jpush/config/save', data),
  
  // 测试极光推送连接
  testConnection: (data) => request.post('/web/message/jpush/config/test', data),
  
  // 发送推送消息
  sendPush: (data) => request.post('/web/message/jpush/push/send', data),
  
  // 获取推送历史列表
  getHistoryList: (params = {}) => {
    const query = {
      orgId: params.orgId,
      page: params.page || 1,
      size: params.size || 20
    }
    if (params.pushType) query.pushType = params.pushType
    if (params.sendStatus !== undefined && params.sendStatus !== null) query.sendStatus = params.sendStatus
    return request.get('/web/message/jpush/history/list', { params: query })
  },
  
  // 获取推送统计数据
  getStatistics: (orgId) => request.get('/web/message/jpush/statistics', { params: { orgId } })
}

// 统一导出
export const messageApi = {
  channel: channelApi,
  template: templateApi,
  record: recordApi,
  inbox: inboxApi,
  subscribe: subscribeApi,
  jpush: jpushApi
}

export default messageApi
