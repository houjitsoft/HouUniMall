/** 与后端 PmTaskMentionParser 一致的 @ 标记格式 */
export const TASK_MENTION_TOKEN_RE = /@\[([^\]]+)\]\(userId:(\d+)\)/g

/**
 * 插入 @ 员工标记
 * @param {string} name 显示名
 * @param {number|string} userId 用户 ID（hou_pm_user.user_id 或 id）
 */
export function buildTaskMentionToken(name, userId) {
  const display = (name || '未命名').trim()
  return `@[${display}](userId:${userId})`
}

/**
 * 展示用：@[姓名](userId:1) → @姓名
 */
export function formatTaskMentionDisplay(content) {
  if (!content) return content
  return String(content).replace(TASK_MENTION_TOKEN_RE, '@$1')
}

/**
 * 解析内容中所有被 @ 的用户 ID
 */
export function parseTaskMentionUserIds(content) {
  const ids = new Set()
  if (!content) return ids
  const re = new RegExp(TASK_MENTION_TOKEN_RE.source, 'g')
  let match
  while ((match = re.exec(content)) !== null) {
    ids.add(Number(match[2]))
  }
  return ids
}
