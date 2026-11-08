import request from '@/utils/request'

/**
 * 网站基础配置开关接口
 */
export const websiteSettingApi = {
  /**
   * 获取网站基础开关配置（国际化、多组织、AI 导购入口）
   * @returns {Promise<Object>} 配置结果
   */
  getWebsiteSwitches() {
    // 与 BFF GET /web/base/sys-config/website-switches 对齐；避免线上未发版时 POST /query 404
    return request.get('/web/base/sys-config/website-switches')
  },

  /**
   * 保存网站基础开关配置（国际化、多组织、AI 导购入口）
   * @param {Object} payload 保存参数
   * @param {boolean} payload.internationalizationEnabled 是否开启国际化
   * @param {boolean} payload.multiOrganizationEnabled 是否开启多组织
   * @param {boolean} payload.aiAssistantEnabled 是否显示商城 AI 导购入口
   * @returns {Promise<Object>} 保存结果
   */
  saveWebsiteSwitches(payload) {
    return request.post('/web/base/sys-config/website-switches', payload)
  },

  /**
   * 根据配置键读取系统配置（用于服务条款/隐私政策等文案）
   * @param {string} configKey 配置键
   * @returns {Promise<Object>} 配置详情
   */
  getConfigByKey(configKey) {
    return request.get('/web/base/sys-config/config-key', { params: { configKey } })
  }
}
