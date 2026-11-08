import request from '@/utils/request'

/**
 * 注册协议（协议定义 + 版本 + 用户同意记录）接口
 *
 * 与 BFF `/web/agreement/**` 对齐；协议正文与清单由后端「协议定义 + 生效版本」表下发，
 * 前端不再硬编码文案，便于运营在后台维护。
 *
 * 【skipAuthRedirect 说明】协议是「附加能力」，不是进入后台的前置条件。
 * 它的接口一旦返回未登录类错误（401 / 11006 / 含「重新登录」），request.js 会清 token
 * 并强跳登录页；2026-09-17 就因此把「待补签协议」查询失败放大成「所有用户一进后台
 * 即被踢回登录页」的生产事故。这里统一声明 skipAuthRedirect，让协议接口的失败
 * 只降级为「本次不提示补签」，绝不影响整个会话。
 */
export const agreementApi = {
  /**
   * 按场景查询启用中的协议及其当前生效版本正文
   * @param {string} [scene] 场景，默认 register
   * @returns {Promise<Object>} 协议列表（含 versionNo、content）
   */
  list(scene = 'register') {
    return request.get('/web/agreement/list', { params: { scene }, skipAuthRedirect: true })
  },

  /**
   * 查询当前登录用户待重新同意的协议（版本更新后补签）
   * @param {Object} [payload]
   * @param {string} [payload.scene]
   * @returns {Promise<Object>} { pending, pendingCount, requiredCount }
   */
  pending(payload = {}) {
    return request.post('/web/agreement/pending', payload, { skipAuthRedirect: true })
  },

  /**
   * 版本更新后重新同意
   * @param {Array<{code: string, versionNo: string}>} agreements 已勾选的协议及版本号
   * @param {string} [source] 来源端
   * @returns {Promise<Object>} 记录结果
   */
  reagree(agreements, source = 'web') {
    return request.post('/web/agreement/reagree', { agreements, source }, { skipAuthRedirect: true })
  },

  // ================= 后台：协议同意记录 =================

  /**
   * 后台：同意记录分页查询
   * @param {Object} payload page/limit/userId/username/agreementCode/beginTime/endTime/status
   * @returns {Promise<Object>} { list, totalCount, currPage, pageSize }
   */
  consentPage(payload) {
    return request.post('/web/agreement/consent/page', payload)
  },

  /**
   * 后台：同意记录导出（返回筛选后的全量数据，前端生成 Excel）
   * @param {Object} payload 同分页查询条件
   * @returns {Promise<Object>} 记录列表
   */
  consentExport(payload) {
    return request.post('/web/agreement/consent/export', payload)
  },

  /**
   * 后台：查询某用户的同意记录
   * @param {Object} payload userId / includeInvalid
   * @returns {Promise<Object>} 记录列表
   */
  consentListByUser(payload) {
    return request.post('/web/agreement/consent/listByUser', payload)
  },

  // ================= 平台侧：接入站点的协议回传记录 =================

  /**
   * 平台后台：站点回传的签署记录分页
   *
   * 数据源是平台表 hou_platform_site_consent（各开源/商业站点回传的副本），
   * 与上面 /web/agreement/consent/** 的本站记录（hou_user_agreement_consent）是两个口径。
   * @param {Object} payload page/limit/siteCode/agreementCode/userId/username/scene
   * @returns {Promise<Object>} { list, totalCount, currPage, pageSize }
   */
  platformConsentPage(payload) {
    return request.post('/web/platform/consent/page', payload)
  },

  /**
   * 平台后台：已接入回传的站点清单（含首次/最近上报时间与累计次数）
   * @returns {Promise<Object>} 站点列表 [{ siteCode, siteName, firstReportTime, lastReportTime, reportCount }]
   */
  platformConsentSites() {
    return request.post('/web/platform/consent/sites', {})
  },

  // ================= 后台：协议定义与版本 =================

  /**
   * 后台：协议定义分页
   * @param {Object} payload page/limit/code/name/scene/status
   * @returns {Promise<Object>} 分页结果
   */
  adminPage(payload) {
    return request.post('/web/agreement/admin/page', payload)
  },

  /**
   * 后台：新增或更新协议定义
   * @param {Object} payload
   * @returns {Promise<Object>} 协议ID
   */
  adminSave(payload) {
    return request.post('/web/agreement/admin/save', payload)
  },

  /**
   * 后台：删除协议定义（连同全部版本，历史同意记录保留）
   * @param {number} id 协议ID
   * @returns {Promise<Object>} 操作结果
   */
  adminDelete(id) {
    return request.post('/web/agreement/admin/delete', { id })
  },

  /**
   * 后台：查询某协议的版本列表
   * @param {Object} payload agreementId 或 code
   * @returns {Promise<Object>} 版本列表
   */
  versionList(payload) {
    return request.post('/web/agreement/version/list', payload)
  },

  /**
   * 后台：新增或更新协议版本（草稿）
   * @param {Object} payload
   * @returns {Promise<Object>} 版本ID
   */
  versionSave(payload) {
    return request.post('/web/agreement/version/save', payload)
  },

  /**
   * 后台：发布协议版本（发布后未同意该版本的用户需重新同意）
   * @param {number} id 版本ID
   * @returns {Promise<Object>} 操作结果
   */
  versionPublish(id) {
    return request.post('/web/agreement/version/publish', { id })
  },

  /**
   * 后台：下线协议版本
   * @param {number} id 版本ID
   * @returns {Promise<Object>} 操作结果
   */
  versionOffline(id) {
    return request.post('/web/agreement/version/offline', { id })
  },

  /**
   * 后台：删除协议版本（已发布版本须先下线）
   * @param {number} id 版本ID
   * @returns {Promise<Object>} 操作结果
   */
  versionDelete(id) {
    return request.post('/web/agreement/version/delete', { id })
  }
}

export default agreementApi
