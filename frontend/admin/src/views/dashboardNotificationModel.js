export const normalizeNotification = (raw = {}) => ({
  id: String(raw.id),
  title: raw.title || '未命名消息',
  content: raw.content || '',
  read: Number(raw.isRead) === 1,
  bizType: raw.bizType || '',
  bizId: raw.bizId == null ? '' : String(raw.bizId),
  createTime: Number(raw.createTime || 0)
})

export const normalizeNotificationPage = (response) => {
  const payload = response?.data || response || {}
  const list = Array.isArray(payload.list)
    ? payload.list
    : (Array.isArray(payload.records) ? payload.records : [])
  return {
    list: list.map(normalizeNotification),
    total: Math.max(0, Number(payload.total || 0)),
    page: Math.max(1, Number(payload.page || 1)),
    size: Math.max(1, Number(payload.size || 20))
  }
}

export const resolveNotificationRoute = (message = {}) => {
  if (!message.bizId) return null
  const id = encodeURIComponent(String(message.bizId))
  if (message.bizType === 'PM_TASK') return `/project/task/detail/${id}`
  if (message.bizType === 'PM_PROJECT') return `/project/list?projectId=${id}`
  return null
}

export const notificationApiId = (id) => {
  const numeric = Number(id)
  return Number.isSafeInteger(numeric) ? numeric : String(id)
}

export const formatNotificationTime = (seconds) => {
  const timestamp = Number(seconds || 0)
  if (!timestamp) return ''
  return new Intl.DateTimeFormat(undefined, {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(timestamp * 1000))
}
