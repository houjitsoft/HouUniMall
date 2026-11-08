import request from '../../utils/request'

/**
 * AI 中台 - 智能体（Agent）统一管理
 *
 * 后台在这里配置「用哪个模型 + 什么提示词 + 何时触发 + 对谁生效」，
 * 前台按 code 取回配置后调用（/web/ai/agent/config）。
 */
export const agentApi = {
  /** 智能体列表（分页 + 场景/状态/关键词筛选） */
  getList(params) {
    return request({ url: '/web/ai/agent/list', method: 'post', data: params })
  },

  /** 智能体详情 */
  getDetail(code) {
    return request({ url: '/web/ai/agent/detail', method: 'get', params: { code } })
  },

  /** 新增或更新（命中 id 或 code 即更新） */
  save(data) {
    return request({ url: '/web/ai/agent/save', method: 'post', data })
  },

  /** 启用 / 停用 */
  updateStatus(data) {
    return request({ url: '/web/ai/agent/status', method: 'post', data })
  },

  /** 删除（内置智能体后端会拒绝） */
  remove(data) {
    return request({ url: '/web/ai/agent/delete', method: 'post', data })
  },

  /** 场景字典 */
  getScenes() {
    return request({ url: '/web/ai/agent/scenes', method: 'get' })
  },

  /** 调用记录（分页） */
  getInvokeLogs(params) {
    return request({ url: '/web/ai/agent/invoke/logs', method: 'post', data: params })
  },

  /** 前台取可执行配置（后台用于预览「前台会拿到什么」） */
  getConfig(code) {
    return request({ url: '/web/ai/agent/config', method: 'get', params: { code } })
  }
}

export default agentApi
