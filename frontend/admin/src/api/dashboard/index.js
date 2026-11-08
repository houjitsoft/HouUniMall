/**
 * Dashboard 偏好与模块数据 API
 * 透传 bff-web: /web/base/dashboard/pref/* 与业务聚合接口
 *
 * @author Amos
 * @since 2026-05-13
 */
import request from '@/utils/request'

export const dashboardApi = {
  /**
   * 查询当前用户 Dashboard 偏好
   * @returns {Promise<Object>} ResponseResult 结构
   */
  getPref() {
    return request.post('/web/base/dashboard/pref/get')
  },

  /**
   * 保存当前用户 Dashboard 偏好
   * @param {Object} pref - { cols, refreshInterval, items: [{ moduleId, visible, sort, x, y, w, h }] }
   * @returns {Promise<Object>}
   */
  savePref(pref) {
    return request.post('/web/base/dashboard/pref/save', pref)
  },

  /**
   * 重置当前用户 Dashboard 偏好
   * @returns {Promise<Object>}
   */
  resetPref() {
    return request.post('/web/base/dashboard/pref/reset')
  }
}

export default dashboardApi
