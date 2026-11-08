/**
 * 移动端页面模板API
 *
 * 走BFF 透传到base 服务的 baseModularizationd（注意：该 Controller 未加 /base 前缀，
 * 见 BaseServiceFeign 注释）。操作的是 hou_base_modularizationd.page_template_code，
 * 与 uniapp 前台首页取数是同一张表 —— 后台选完前台立即生效。
 *
 * 命名沿用既有 table/接口前缀口径，不做品牌改名。
 */
import request from '@/utils/request'

/**
 * 查询模板库
 * @returns {Promise} 模板列表
 */
export function getPageTemplates() {
  return request.post('/web/base/page/template/list')
}

/**
 * 查询某页面当前使用的模板 code
 * @param {Object} data - { pageId }
 * @returns {Promise} templateCode 字符串，未设置时为空串
 */
export function getCurrentPageTemplate(data) {
  return request.post('/web/base/page/template/current', data)
}

/**
 * 为某页面选择模板
 * @param {Object} data - { pageId, templateCode }；templateCode 传空串取消套用
 * @returns {Promise} 实际生效的 templateCode
 */
export function applyPageTemplate(data) {
  return request.post('/web/base/page/template/apply', data)
}

/**
 * 查询组件化页面列表（模板选择页需先选页面）
 * @param {Object} data - 查询参数
 * @returns {Promise} 页面列表
 */
export function getModularizationdList(data) {
  return request.post('/web/base/page/modularizationd/list', data)
}