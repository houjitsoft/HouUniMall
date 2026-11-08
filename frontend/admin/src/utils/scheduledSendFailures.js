const READY_STATUSES = new Set(['正常', '将过期', 'normal', 'expiring'])

// 历史报错用于解释原因；能否重试必须结合当前账号，避免恢复后仍被旧报错锁死。
export function diagnoseSendFailure(task, accounts = []) {
  const error = String(task.errorMessage || '')
  let category = '发布异常'
  let hint = '查看完整错误，处理后重试。'
  if (/未完成实名认证|未实名认证|需实名认证/.test(error)) {
    category = '需要实名认证'
    hint = '在平台完成实名认证，再检测授权状态。'
  } else if (/未上线|永久拒绝|上线条件|入驻/.test(error)) {
    category = '入驻未通过'
    hint = '在平台处理入驻审核，通过前保持停发。'
  } else if (/网络环境异常.*完成验证|安全验证|验证码/.test(error)) {
    category = '需要平台验证'
    hint = '在平台完成安全验证并重新授权，再刷新任务状态。'
  } else if (/403|PermissionDenied|风控|暂时被限制|熔断/.test(error)) {
    category = '账号受限'
    hint = '先在平台确认账号恢复发布权限，再检测授权状态。'
  } else if (/not in whitelist|IP白名单|IP 白名单/.test(error)) {
    category = 'IP 白名单'
    hint = '确认公众号已允许发布服务器的出口 IP。'
  } else if (/掉线|过期|Cookie.*失效|重新绑定|未登录/.test(error)) {
    category = '授权失效'
    hint = '重新绑定有效授权，再重试原任务。'
  } else if (/封面/.test(error)) {
    category = '封面处理失败'
    hint = '需完成封面上传和确认后，平台才会接收发布。'
  } else if (/JDBC|rollback|数据库|connection/i.test(error)) {
    category = '执行连接异常'
    hint = '先核对平台是否已收稿，避免重复发布。'
  }
  const candidates = accounts.filter(account => account.platformCode === task.platform)
  const ready = candidates.some(account => account.publishReady === true && READY_STATUSES.has(account.authStatus))
  const pending = /确认前不要重试|已提交发布.*确认|发布结果.*确认/.test(error)
  if (pending) return { category: '等待平台确认', hint: '先在平台确认提交结果，避免重复发布。', retryable: false, action: '查看详情' }
  if (!ready) return { category, hint: `${hint} 当前没有可发布账号。`, retryable: false, action: '处理账号' }
  return { category, hint, retryable: true, action: '重试' }
}

export function retryFeedback(result = {}) {
  const queued = Number(result.queued ?? result.retried ?? 0)
  return queued > 0
    ? { type: 'success', message: `已重新排队 ${queued} 个平台任务，请等待实际发布结果` }
    : { type: 'warning', message: result.message || result.blocked?.[0]?.reason || '没有任务重新入队，请检查账号状态和失败原因' }
}
