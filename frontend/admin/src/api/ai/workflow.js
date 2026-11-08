import request from '../../utils/request'

/**
 * AI 中台 - 工作流（可视化编排）
 *
 * 后台画布保存的 nodes/edges 整体作为 graph 提交，执行引擎按图执行；
 * 与智能体的关系是：智能体 mode=workflow 时按 workflowCode 取这张画布来跑。
 */
export const workflowApi = {
  /** 工作流列表（分页 + 端/状态/关键词筛选） */
  getList(params) {
    return request({ url: '/web/ai/workflow/list', method: 'post', data: params })
  },

  /** 详情（含 graph） */
  getDetail(code) {
    return request({ url: '/web/ai/workflow/detail', method: 'get', params: { code } })
  },

  /** 新增或更新（按 code 命中即更新，graph 必须是 JSON 字符串） */
  save(data) {
    return request({ url: '/web/ai/workflow/save', method: 'post', data })
  },

  /** 复制一份（副本默认停用） */
  copy(data) {
    return request({ url: '/web/ai/workflow/copy', method: 'post', data })
  },

  /** 删除（内置拒绝） */
  remove(data) {
    return request({ url: '/web/ai/workflow/delete', method: 'post', data })
  },

  /** 启停 */
  updateStatus(data) {
    return request({ url: '/web/ai/workflow/status', method: 'post', data })
  },

  /** 只校验不落库 */
  validate(data) {
    return request({ url: '/web/ai/workflow/validate', method: 'post', data })
  },

  /** 试运行 */
  run(data) {
    return request({ url: '/web/ai/workflow/run', method: 'post', data, timeout: 300000 })
  },

  /** 运行记录 */
  getRuns(params) {
    return request({ url: '/web/ai/workflow/runs', method: 'post', data: params })
  },

  /** 运行详情（含节点轨迹） */
  getRunDetail(runNo) {
    return request({ url: '/web/ai/workflow/run/detail', method: 'get', params: { runNo } })
  }
}
