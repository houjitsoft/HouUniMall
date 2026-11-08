import request from '../../utils/request'

// AI API 接口
export const aiApi = {
  // ================= AI管理接口 =================

  getStatistics() {
    return request({ url: '/web/ai/manage/statistics', method: 'post' })
  },

  getList(params) {
    return request({ url: '/web/ai/manage/list', method: 'post', data: params })
  },

  getDetail(params) {
    return request({ url: '/web/ai/manage/detail', method: 'post', data: params })
  },

  create(data) {
    return request({ url: '/web/ai/manage/create', method: 'post', data })
  },

  update(data) {
    return request({ url: '/web/ai/manage/update', method: 'post', data })
  },

  delete(params) {
    return request({ url: '/web/ai/manage/delete', method: 'post', data: params })
  },

  pause(params) {
    return request({ url: '/web/ai/manage/pause', method: 'post', data: params })
  },

  resume(params) {
    return request({ url: '/web/ai/manage/resume', method: 'post', data: params })
  },

  copy(params) {
    return request({ url: '/web/ai/manage/copy', method: 'post', data: params })
  },

  getTypes() {
    return request({ url: '/web/ai/manage/types', method: 'get' })
  },

  batchUpdateStatus(data) {
    return request({ url: '/web/ai/manage/batch/status', method: 'post', data })
  },

  getGenerationRecords(params) {
    return request({ url: '/web/ai/manage/generation/list', method: 'post', data: params })
  },

  // ================= 模型接口 =================

  getEnabledModels() {
    return request({ url: '/web/ai/model/enabled', method: 'get' })
  },

  // ================= 提示词接口 =================

  getPrompts(params) {
    return request({ url: '/web/ai/prompt/list', method: 'post', data: params })
  },

  getPromptDetail(params) {
    return request({ url: '/web/ai/prompt/detail', method: 'post', data: params })
  },

  createPrompt(data) {
    return request({ url: '/web/ai/prompt/create', method: 'post', data })
  },

  updatePrompt(data) {
    return request({ url: '/web/ai/prompt/update', method: 'post', data })
  },

  deletePrompt(params) {
    return request({ url: '/web/ai/prompt/delete', method: 'post', data: params })
  },

  getPromptCategories() {
    return request({ url: '/web/ai/prompt/category/list', method: 'get' })
  },

  createPromptCategory(data) {
    return request({ url: '/web/ai/prompt/category/create', method: 'post', data })
  },

  updatePromptCategory(data) {
    return request({ url: '/web/ai/prompt/category/update', method: 'post', data })
  },

  deletePromptCategory(params) {
    return request({ url: '/web/ai/prompt/category/delete', method: 'post', data: params })
  },

  // ================= 对话接口 =================

  chat(data) {
    return request({ url: '/web/ai/chat', method: 'post', data })
  },

  getChatHistory(params) {
    return request({ url: '/web/ai/chat/history', method: 'post', data: params })
  },

  clearChatHistory(params) {
    return request({ url: '/web/ai/chat/clear', method: 'post', data: params })
  },

  // ================= 文本生成接口 =================

  generateText(data) {
    return request({ url: '/web/ai/text/generate', method: 'post', data })
  },

  summarizeText(data) {
    return request({ url: '/web/ai/text/summary', method: 'post', data })
  },

  translateText(data) {
    return request({ url: '/web/ai/text/translate', method: 'post', data })
  },

  correctText(data) {
    return request({ url: '/web/ai/text/correct', method: 'post', data })
  },

  // ================= 图像生成接口 =================

  generateImage(data) {
    return request({ url: '/web/ai/image/generate', method: 'post', data })
  },

  recognizeImage(data) {
    return request({ url: '/web/ai/image/recognize', method: 'post', data })
  },

  processImage(data) {
    return request({ url: '/web/ai/image/process', method: 'post', data })
  },

  // ================= 数据分析接口 =================

  dataAnalyze(data) {
    return request({ url: '/web/ai/data/analyze', method: 'post', data })
  },

  dataPredict(data) {
    return request({ url: '/web/ai/data/predict', method: 'post', data })
  },

  dataRecommend(data) {
    return request({ url: '/web/ai/data/recommend', method: 'post', data })
  },

  // ================= AI配置接口 =================

  getConfigAll() {
    return request({ url: '/web/ai/config/all', method: 'get' })
  },

  getConfigList() {
    return request({ url: '/web/ai/config/list', method: 'post' })
  },

  saveConfig(data) {
    return request({ url: '/web/ai/config/save', method: 'post', data })
  },

  batchSaveConfig(data) {
    return request({ url: '/web/ai/config/batch', method: 'post', data })
  },

  // ================= 计量与余额（AI 中台 hou_ai_*） =================

  /** 计量总览：{ totalCalls, modules: { geo, aiagent, mall } } */
  getUsageSummary() {
    return request({ url: '/web/ai/usage/summary', method: 'get' })
  },

  /** 计量明细分页：参数 module / model / bizType / tenantId / page / limit */
  getUsageRecords(params) {
    return request({ url: '/web/ai/usage/records', method: 'post', data: params })
  },

  /** 余额列表：参数 page / limit / subjectType / subjectKey */
  getBalanceList(params) {
    return request({ url: '/web/ai/balance/list', method: 'post', data: params })
  },

  /** 单个主体余额明细 */
  getBalanceDetail(params) {
    return request({ url: '/web/ai/balance/detail', method: 'get', params })
  },

  /** 充值（token 数量） */
  rechargeBalance(data) {
    return request({ url: '/web/ai/balance/recharge', method: 'post', data })
  },

  /** 手动扣减（原子，余额不足返回 success=false） */
  consumeBalance(data) {
    return request({ url: '/web/ai/balance/consume', method: 'post', data })
  }
}

export default aiApi
