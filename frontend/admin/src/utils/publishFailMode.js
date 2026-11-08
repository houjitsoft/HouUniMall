/** 分发失败 mode → 用户可见标签/说明（与后端 PublishFailModes 对齐） */

const MODE_META = {
  unsupported: { label: '未对接', hint: '该平台暂无自动发文能力', tagType: 'info' },
  not_configured: { label: '未配凭证', hint: '需配置生产环境开放平台 OAuth 凭证', tagType: 'warning' },
  adapter_missing: { label: '上传适配器未就绪', hint: '该平台真实视频上传适配器尚未配置，请改发 B 站或手工上传', tagType: 'warning' },
  cookie: { label: 'Cookie 失效', hint: '请到平台授权重新抓取创作者中心 Cookie', tagType: 'danger' },
  cookie_invalid: { label: 'Cookie 失效', hint: '请到平台授权重新抓取创作者中心 Cookie', tagType: 'danger' },
  platform_rejected: { label: '发布被平台拒绝', hint: '请查看平台返回原因，调整内容或参数后重试', tagType: 'warning' },
  cookie_draft_pending: { label: '草稿已入箱', hint: '草稿已在公众号草稿箱，请手动点「发表」，或绑定 AppID/AppSecret 走官方 freepublish', tagType: 'warning' },
  publish_pending: { label: '待公众号确认', hint: '微信已提交发布，请在公众号后台确认结果；确认前不要重试', tagType: 'warning' },
  ip_not_whitelisted: { label: 'IP 未加白名单', hint: '到微信公众平台「设置与开发 → 基本配置 → IP白名单」加入本机出口 IP，约 10 分钟后重试官方 freepublish', tagType: 'warning' },
  oauth_required: { label: '需 OAuth 授权', hint: '请在平台账号授权页使用开放平台 OAuth 重新绑定账号', tagType: 'danger' },
  permission_required: { label: '缺少平台权限', hint: '请在开放平台为应用申请发布权限后重新授权账号', tagType: 'warning' },
  sandbox: { label: '沙箱', hint: '当前为沙箱模式，非生产发布', tagType: 'warning' }
}

export function normalizePublishFailMode(mode) {
  if (mode == null || mode === '') return ''
  return String(mode).trim().toLowerCase()
}

/** 历史任务曾把「适配器未配置」误写成 cookie，按失败原文纠正 */
export function resolvePublishFailMode(mode, failReason = '') {
  const reason = String(failReason || '')
  const lower = reason.toLowerCase()
  if (
    reason.includes('适配器尚未配置')
    || reason.includes('上传适配器')
    || reason.includes('浏览器自动化适配器')
    || /video uploader is not configured/i.test(reason)
    || /adapter_missing/i.test(reason)
  ) {
    return 'adapter_missing'
  }
  if (
    reason.includes('草稿已保存到后台草稿箱')
    || reason.includes('Cookie 通道无法自动正式发表')
    || /cookie_draft_pending/i.test(reason)
  ) {
    return 'cookie_draft_pending'
  }
  if (
    reason.includes('40164')
    || /not in whitelist/i.test(reason)
    || reason.includes('IP白名单')
    || reason.includes('IP 白名单')
    || /ip_not_whitelisted/i.test(reason)
  ) {
    return 'ip_not_whitelisted'
  }
  if (
    (lower.includes('cookie') && /(过期|失效|无效|缺少|未配置|expired|invalid|missing)/i.test(reason))
    || reason.includes('登录已过期')
    || reason.includes('登录状态已失效')
    || reason.includes('请重新登录')
    || reason.includes('请重新绑定')
    || reason.includes('未登录')
    || /not logged in|session expired/i.test(reason)
  ) return 'cookie_invalid'
  const normalized = normalizePublishFailMode(mode)
  if (normalized === 'cookie_draft_pending' || normalized === 'publish_pending' || normalized === 'ip_not_whitelisted') {
    return normalized
  }
  if ((normalized === 'cookie' || normalized === 'cookie_invalid') && reason) {
    return 'platform_rejected'
  }
  return normalized
}

export function publishFailModeMeta(mode) {
  const key = normalizePublishFailMode(mode)
  return MODE_META[key] || { label: '发布失败', hint: '请查看失败原因后重试', tagType: 'danger' }
}

export function publishFailModeLabel(mode, failReason) {
  return publishFailModeMeta(resolvePublishFailMode(mode, failReason)).label
}

export function publishFailModeHint(mode, failReason) {
  return publishFailModeMeta(resolvePublishFailMode(mode, failReason)).hint
}

export function publishFailModeTagType(mode, failReason) {
  return publishFailModeMeta(resolvePublishFailMode(mode, failReason)).tagType
}

/** 组合展示：标签 + 原始错误 */
export function formatPublishFailMessage(row) {
  const reason = row?.error || row?.failReason || row?.message || ''
  const mode = resolvePublishFailMode(row?.mode || row?.failMode, reason)
  const meta = mode ? publishFailModeMeta(mode) : null
  if (meta && mode) {
    const body = reason && !String(reason).includes(meta.label) ? reason : meta.hint
    return `[${meta.label}] ${body}`
  }
  return reason || '平台 API 返回错误'
}
