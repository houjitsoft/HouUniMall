/**
 * 邮件设置 API
 */
import request from '@/utils/request'
import { templateApi } from '@/api/setting/message'

export const emailApi = {
  /** 获取邮件 SMTP 配置 */
  getSettings: () => request.get('/web/message/email/settings'),

  /** 保存邮件 SMTP 配置 */
  saveSettings: (data) => request.post('/web/message/email/settings', data),

  /** 发送测试邮件 */
  sendTest: (testEmail) => request.post('/web/message/email/test', { testEmail })
}

export { templateApi }

export default emailApi
