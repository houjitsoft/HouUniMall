import request from '@/utils/request'

/**
 * 后台语言配置。勾选结果决定右上角语言菜单。
 */
export const adminLanguageApi = {
  getAdminLanguage() {
    return request.get('/web/base/sys-config/admin-language', { skipErrorMessage: true })
  },

  saveAdminLanguage(payload) {
    return request.post('/web/base/sys-config/admin-language', payload)
  }
}
