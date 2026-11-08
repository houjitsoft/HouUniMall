const SUBJECT_TYPES = new Set(['official_site', 'brand', 'creator', 'organization', 'other'])

const clean = (value) => String(value ?? '').trim()

export function buildAiPlatformRegistrationPayload(form = {}) {
  const subjectName = clean(form.subjectName)
  const subjectType = clean(form.subjectType)
  const publicId = clean(form.publicId)
  const homeUrl = clean(form.homeUrl)
  const note = clean(form.note)
  if (!subjectName) throw new Error('请填写监测主体名称')
  if (subjectName.length > 128) throw new Error('监测主体名称不能超过 128 个字符')
  if (!SUBJECT_TYPES.has(subjectType)) throw new Error('请选择有效的监测主体类型')
  if (publicId.length > 128) throw new Error('平台公开 ID 不能超过 128 个字符')
  if (homeUrl && !/^https?:\/\//i.test(homeUrl)) throw new Error('主页地址必须以 http:// 或 https:// 开头')
  if (homeUrl.length > 512) throw new Error('主页地址不能超过 512 个字符')
  if (note.length > 500) throw new Error('备注不能超过 500 个字符')
  return { subjectName, subjectType, publicId, homeUrl, note }
}

export function isAiPlatformRegistrationReadbackEqual(payload, model) {
  const registration = model?.registration || {}
  return ['subjectName', 'subjectType', 'publicId', 'homeUrl', 'note']
    .every((key) => clean(registration[key]) === clean(payload[key]))
}

export function registrationStatusLabel(status) {
  return ({ pending: '待验证', verified: '已验证', inactive: '已停用', unregistered: '未登记' })[status] || '未登记'
}

export function registrationStatusType(status) {
  return ({ pending: 'warning', verified: 'success', inactive: 'info', unregistered: 'info' })[status] || 'info'
}
