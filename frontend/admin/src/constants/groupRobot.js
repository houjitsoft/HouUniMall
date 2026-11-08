import { getAttendancePlatformLabel, isThirdPartyAttendanceSource } from '@/constants/attendancePlatform'

const WECOM_WEBHOOK_PATTERN = /^https:\/\/qyapi\.weixin\.qq\.com\/cgi-bin\/webhook\/send/i
const DINGTALK_WEBHOOK_PATTERN = /^https:\/\/oapi\.dingtalk\.com\/robot\/send/i
const FEISHU_WEBHOOK_PATTERN = /^https:\/\/open\.feishu\.cn\/open-apis\/bot\/v2\/hook/i

export const GROUP_SYNC_MODE_ALL = 'all'
export const GROUP_SYNC_MODE_NONE = 'none'
export const GROUP_SYNC_MODE_WEBHOOK = 'webhook'
export const GROUP_SYNC_MODE_WECOM_APP = 'wecom_app'
export const GROUP_SYNC_MODE_WECOM_EXTERNAL_GROUP = 'wecom_external_group'

/** 企微三种推送渠道说明（可独立勾选启用） */
export const WECOM_GROUP_SYNC_CHANNELS = [
  {
    key: 'webhook',
    title: '① 群机器人',
    subtitle: '纯内部群，可配置多个 Webhook',
    scenario: '企业内部群'
  },
  {
    key: 'wecom_app',
    title: '② 企微应用消息',
    subtitle: '推送给项目经理等项目成员（企业微信）',
    scenario: '企业成员 · 应用消息'
  },
  {
    key: 'external_group',
    title: '③ 客户群群发',
    subtitle: '发到客户群，群内微信用户可收到',
    scenario: '外部客户群'
  }
]

/**
 * 解析 group_sync_mode 为各渠道勾选状态
 * @param {string} mode 持久化值（逗号分隔 / all / 历史单值）
 * @param {{ hasProjectMembers?: boolean, hasWebhook?: boolean, hasExternalGroup?: boolean }} options
 */
export function parseGroupSyncChannels(mode, options = {}) {
  const {
    hasProjectMembers = false,
    hasWebhook = false,
    hasExternalGroup = false
  } = options
  const channels = {
    webhook: false,
    wecom_app: false,
    wecom_external_group: false
  }
  const raw = (mode || '').trim()

  if (raw === GROUP_SYNC_MODE_NONE) {
    return channels
  }

  if (!raw || raw === GROUP_SYNC_MODE_ALL) {
    if (hasProjectMembers) channels.wecom_app = true
    if (hasWebhook) channels.webhook = true
    if (hasExternalGroup) channels.wecom_external_group = true
    return channels
  }

  if (raw === GROUP_SYNC_MODE_WEBHOOK) {
    channels.webhook = true
    if (hasProjectMembers) channels.wecom_app = true
    return channels
  }
  if (raw === GROUP_SYNC_MODE_WECOM_APP) {
    channels.wecom_app = true
    return channels
  }
  if (raw === GROUP_SYNC_MODE_WECOM_EXTERNAL_GROUP) {
    channels.wecom_external_group = true
    return channels
  }

  raw.split(',').forEach((part) => {
    const key = part.trim()
    if (key === 'webhook') channels.webhook = true
    if (key === 'wecom_app') channels.wecom_app = true
    if (key === 'wecom_external_group') channels.wecom_external_group = true
  })
  return channels
}

/** 将勾选状态序列化为 group_sync_mode（三渠道全开时用 all，避免超出 DB varchar(32)） */
export function serializeGroupSyncChannels(channels = {}) {
  const parts = []
  if (channels.webhook) parts.push(GROUP_SYNC_MODE_WEBHOOK)
  if (channels.wecom_app) parts.push(GROUP_SYNC_MODE_WECOM_APP)
  if (channels.wecom_external_group) parts.push(GROUP_SYNC_MODE_WECOM_EXTERNAL_GROUP)
  if (parts.length === 3) return GROUP_SYNC_MODE_ALL
  return parts.length ? parts.join(',') : GROUP_SYNC_MODE_NONE
}

/** 写入接口前压缩 group_sync_mode，与后端 PmProjectGroupSyncMode.compactForStorage 对齐 */
export function compactGroupSyncModeForStorage(mode) {
  const raw = (mode || '').trim()
  if (!raw) return ''
  if (raw === GROUP_SYNC_MODE_NONE || raw === GROUP_SYNC_MODE_ALL) return raw
  const channels = parseGroupSyncChannels(raw, {
    hasProjectMembers: true,
    hasWebhook: true,
    hasExternalGroup: true
  })
  const serialized = serializeGroupSyncChannels(channels)
  if (serialized.length <= 32) return serialized
  return GROUP_SYNC_MODE_ALL
}

export function isGroupSyncChannelEnabled(mode, channel) {
  const raw = (mode || '').trim()
  if (!raw || raw === GROUP_SYNC_MODE_ALL) {
    return true
  }
  const channels = parseGroupSyncChannels(raw, {})
  return !!channels[channel]
}

export function resolveProjectWebhookField(source) {
  if (source === 'dingtalk') return 'ddWebhook'
  if (source === 'feishu') return 'feishuWebhook'
  if (source === 'wechat-work') return 'wxWebhook'
  return null
}

export function parseProjectWebhooks(raw) {
  if (!raw || !String(raw).trim()) return []
  const set = new Set()
  String(raw)
    .split(/[\r\n,;]+/)
    .map((s) => s.trim())
    .filter(Boolean)
    .forEach((url) => set.add(url))
  return [...set]
}

export function serializeProjectWebhooks(urls) {
  if (!Array.isArray(urls)) return ''
  const unique = []
  const seen = new Set()
  urls.forEach((item) => {
    const value = (item || '').trim()
    if (value && !seen.has(value)) {
      seen.add(value)
      unique.push(value)
    }
  })
  return unique.join('\n')
}

export function parseProjectGroupChatIds(raw) {
  return parseProjectGroupChatEntries(raw).map((entry) => entry.chatId)
}

/** 从存储值解析纯 chatId（兼容历史 chatId|owner|name 被截断的数据） */
export function normalizeStoredGroupChatId(raw) {
  const trimmed = String(raw || '').trim()
  if (!trimmed) return ''
  const pipe = trimmed.indexOf('|')
  return pipe > 0 ? trimmed.slice(0, pipe).trim() : trimmed
}

export function parseProjectGroupChatEntries(raw) {
  return parseProjectWebhooks(raw).map((line) => {
    const chatId = normalizeStoredGroupChatId(line)
    if (!chatId) return null
    return { chatId, ownerUserid: '', groupName: '' }
  }).filter(Boolean)
}

export function serializeProjectGroupChatIds(ids, options = []) {
  if (!Array.isArray(ids)) return serializeProjectWebhooks(ids)
  // 数据库 wx_group_chat_id 仅存 chatId（varchar 有限），群主/群名发送时从缓存表解析
  return ids.filter(Boolean).map((id) => String(id).trim()).filter(Boolean).join('\n')
}

export function resolveProjectWebhook(project, source) {
  if (!project) return ''
  const field = resolveProjectWebhookField(source)
  if (!field) return ''
  const snake = field.replace(/([A-Z])/g, '_$1').toLowerCase()
  return (project[field] || project[snake] || '').trim()
}

export function resolveProjectWebhookList(project, source) {
  return parseProjectWebhooks(resolveProjectWebhook(project, source))
}

export function resolveProjectWxGroupChatIds(project) {
  if (!project) return []
  const raw = project.wxGroupChatId || project.wx_group_chat_id || ''
  return parseProjectGroupChatIds(raw)
}

export function validateGroupRobotWebhook(source, url) {
  const value = (url || '').trim()
  if (!value) return false
  if (source === 'dingtalk') return DINGTALK_WEBHOOK_PATTERN.test(value)
  if (source === 'feishu') return FEISHU_WEBHOOK_PATTERN.test(value)
  if (source === 'wechat-work') return WECOM_WEBHOOK_PATTERN.test(value)
  return false
}

/** 项目是否至少配置了一种可推送渠道（且已勾选启用） */
export function projectHasGroupRobot(project, source) {
  if (!project) return false
  if (source === 'wechat-work') {
    const status = getWecomGroupSyncStatus(project)
    const mode = project.groupSyncMode || project.group_sync_mode || ''
    const enabled = parseGroupSyncChannels(mode, {
      hasProjectMembers: status.wecomApp,
      hasWebhook: status.webhook,
      hasExternalGroup: status.externalGroup
    })
    return (enabled.webhook && status.webhook)
      || (enabled.wecom_app && status.wecomApp)
      || (enabled.wecom_external_group && status.externalGroup)
  }
  return resolveProjectWebhookList(project, source).some((url) => validateGroupRobotWebhook(source, url))
}

/** 企微三渠道配置状态（前端展示用） */
export function getWecomGroupSyncStatus(project) {
  const webhook = resolveProjectWebhookList(project, 'wechat-work').some((url) =>
    validateGroupRobotWebhook('wechat-work', url)
  )
  const externalGroup = resolveProjectWxGroupChatIds(project).length > 0
  const wecomApp = !!(
    project?.managerId
    || project?.projectAdminId
    || project?.project_user_id
    || project?.adminIds
    || project?.user_ids
    || project?.userIds
  )
  return { webhook, wecomApp, externalGroup }
}

export function getProjectGroupSyncSummary(project, source) {
  if (source !== 'wechat-work') {
    const n = resolveProjectWebhookList(project, source).filter((u) => validateGroupRobotWebhook(source, u)).length
    return n ? `已配置 ${n} 个群机器人` : '未配置'
  }
  const s = getWecomGroupSyncStatus(project)
  const mode = project?.groupSyncMode || project?.group_sync_mode || ''
  const enabled = parseGroupSyncChannels(mode, {
    hasProjectMembers: s.wecomApp,
    hasWebhook: s.webhook,
    hasExternalGroup: s.externalGroup
  })
  const parts = []
  if (enabled.webhook && s.webhook) parts.push('群机器人')
  if (enabled.wecom_app && s.wecomApp) parts.push('应用消息')
  if (enabled.wecom_external_group && s.externalGroup) parts.push('客户群')
  return parts.length ? `已启用：${parts.join('、')}` : '未启用推送'
}

export function getGroupSyncSectionTitle(source) {
  const label = getAttendancePlatformLabel(source)
  if (isThirdPartyAttendanceSource(source)) {
    return `${label}群同步`
  }
  return '群消息同步'
}

export function getGroupRobotPlaceholder(source) {
  if (source === 'dingtalk') return '粘贴钉钉群机器人 Webhook 地址'
  if (source === 'feishu') return '粘贴飞书群机器人 Webhook 地址'
  if (source === 'wechat-work') return '粘贴企业微信群机器人 Webhook 地址'
  return '请先在考勤设置中选择钉钉/企业微信/飞书'
}

export function getGroupRobotHelp(source) {
  if (source === 'dingtalk') {
    return '可添加多个链接，任务通知将同时推送到全部群。钉钉群 → 智能群助手 → 添加机器人 → 复制 Webhook'
  }
  if (source === 'feishu') {
    return '可添加多个链接。飞书群 → 群机器人 → 复制 Webhook'
  }
  if (source === 'wechat-work') {
    return '三种方式将同时推送，尽量保证企业成员与群内微信用户都能收到。内部群请配置 Webhook；外部群请同时配置应用消息与客户群'
  }
  return '当前为平台考勤模式，如需群同步请先在「考勤设置」切换为钉钉或企业微信'
}
