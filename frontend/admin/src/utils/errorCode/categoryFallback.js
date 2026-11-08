/**
 * 错误类别兜底文案 / 默认展示动作 / 后端原文脱敏规则
 *
 * 解决的问题（PRD §6.2、§6.3）：
 * 1. 映射表不可能覆盖全部历史裸码，必须有一个稳定的兜底维度 —— 按 category 出固定文案，
 *    查不到 category 时降级为 SYSTEM。
 * 2. 后端 message 里可能含 SQL、异常类名、堆栈、内部 URL、文件路径（如 bff-web 曾经的
 *    "服务端异常：" + simpleCause），一律禁止出屏，命中即替换为 category 兜底文案。
 *
 * 本文件是纯数据 + 纯函数，不依赖 axios / Vue / Element Plus，便于其它端直接复制复用。
 */

/** 9 类固定兜底文案：未命中映射表时使用，绝不直出后端原文。 */
export const CATEGORY_TEXT = {
  AUTH: '登录已失效，请重新登录',
  PERMISSION: '你没有该操作权限，请联系管理员',
  VALIDATION: '提交的信息有误，请检查后重试',
  BIZ: '操作未成功，请按提示调整后重试',
  NOT_FOUND: '没有找到相关数据',
  CONFLICT: '数据已变更，请刷新后重试',
  UPSTREAM: '服务暂时不可用，请稍后再试',
  SYSTEM: '操作失败，请稍后重试',
  RATE_LIMIT: '操作过于频繁，请稍后再试'
}

/** 兜底时的默认展示动作，与后端 UiAction 枚举同名。 */
export const CATEGORY_UI = {
  AUTH: 'REDIRECT_LOGIN',
  PERMISSION: 'TOAST',
  VALIDATION: 'INLINE',
  BIZ: 'TOAST',
  NOT_FOUND: 'EMPTY_STATE',
  CONFLICT: 'DIALOG',
  UPSTREAM: 'SILENT_RETRY',
  SYSTEM: 'TOAST',
  RATE_LIMIT: 'TOAST'
}

/**
 * 敏感内容命中特征：命中即判定「后端原文不可出屏」。
 * 覆盖 SQL / 堆栈 / 内部服务名 / URL / 文件路径 / 内部标识 / 第三方原文。
 */
export const SENSITIVE_PATTERNS = [
  /\b(SELECT|INSERT|UPDATE|DELETE)\b/i,
  /\b(FROM|WHERE|JOIN)\s+[a-z_]/i,
  /SQLSyntaxError|BadSqlGrammar|Duplicate entry|DataAccessException|MyBatis/i,
  /NullPointerException|\.java:|Caused by|at\s+com\./i,
  /\bException\b|stackTrace/i,
  /feign|nacos|redis|mysql|rabbit|xxl-job/i,
  /https?:\/\//i,
  /localhost|127\.0\.0\.1|:\d{2,5}\//,
  /\/data\/|\/home\/|[A-Za-z]:\\/,
  /\.jar|\.class\b/,
  /token|Authorization|signature|access_token/i
]

/**
 * 判定后端原文是否可出屏。
 *
 * @param {string} message 后端返回的 message 原文
 * @returns {{ safe: boolean, reason: string|null }} safe=false 表示必须改用兜底文案
 */
export function sanitizeBackendMessage(message) {
  if (typeof message !== 'string' || message.trim() === '') {
    return { safe: false, reason: 'empty' }
  }
  for (const pattern of SENSITIVE_PATTERNS) {
    if (pattern.test(message)) {
      return { safe: false, reason: pattern.toString() }
    }
  }
  return { safe: true, reason: null }
}

/**
 * 关键词 → 类别兜底表（兼容期使用）。
 *
 * 解决的问题：admin 端历史上用 message.includes('未登录') 等十余个关键词做分支，
 * 后端改一个字就失效。这里把关键词收敛成「推类别」，且只在映射表未命中时启用
 * （enableKeywordFallback 开关），不再作为主判定。
 */
export const KEYWORD_CATEGORY_RULES = [
  { category: 'UPSTREAM', keywords: ['暂时不可用', '服务不可用', '下游', '稍后重试', '稍后再试', '网络超时'] },
  { category: 'AUTH', keywords: ['未登录', '请重新登录', '请先登录', '请重新登陆', 'token失效', 'Token失效', 'token非法', '认证失败', '认证过期'] },
  { category: 'PERMISSION', keywords: ['权限不足', '无权限', '没有权限', 'Permission denied'] },
  { category: 'NOT_FOUND', keywords: ['不存在', '未找到', '没有查询到', '没有获取到'] },
  { category: 'VALIDATION', keywords: ['不能为空', '格式不正确', '参数'] }
]

/**
 * 按关键词推断类别（仅兜底用）。
 *
 * @param {string} message 后端 message 原文
 * @returns {string|null} 类别名，推断不出时返回 null
 */
export function inferCategoryByKeyword(message) {
  if (typeof message !== 'string' || message.trim() === '') return null
  for (const rule of KEYWORD_CATEGORY_RULES) {
    for (const keyword of rule.keywords) {
      if (message.includes(keyword)) {
        return rule.category
      }
    }
  }
  return null
}
