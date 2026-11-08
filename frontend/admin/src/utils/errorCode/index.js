/**
 * 统一错误码映射层（admin 端）—— 纯函数 API
 *
 * 解决的问题：
 * 1. 后端原文直出：过去 `ElMessage({ message: res.message })` 把 SQL、异常根因、内部类名
 *    直接弹给用户（体验问题 + 信息泄露）。现在任何路径都不直出后端原文，原文只进 console.warn。
 * 2. 判定靠字符串：过去用 `res.message.includes('未登录')` 等十余个关键词做分支，
 *    后端改一个字就失效。现在主判定只看码值 + 映射表。
 * 3. 高风险动作误触发：过去 `res.code === 11006` 就清 token 跳登录，
 *    而 11006 在骑手端是「只能提现到本人账户」。现在只有 category === 'AUTH' 才跳登录。
 *
 * 设计：本文件只做「判定 + 查表」，不依赖 axios；真正的 UI 动作由 presentError 统一分发，
 * 业务侧通过 skipAuthRedirect / skipErrorMessage 开关覆盖。
 *
 * 灰度说明：本轮新旧码都能处理 —— 命中新码直接用映射；
 * 命中 LEGACY_TO_NEW 的旧码先归一到新码再用映射；两者都命中不了才走关键词兜底。
 */
import {
  ERROR_CODE_MAP,
  LEGACY_TO_NEW,
  CANONICAL_SUCCESS_CODE,
  COMPAT_SUCCESS_CODES,
  FAIL_CODE
} from './errorCodeMap.generated.js'
import {
  CATEGORY_TEXT,
  CATEGORY_UI,
  sanitizeBackendMessage,
  inferCategoryByKeyword
} from './categoryFallback.js'
import {
  compatZeroAsSuccess,
  passAsEmptyNames,
  enableKeywordFallback,
  enableBackendMessageLog,
  legacyAuthCodes
} from './config.js'

/** 名称 → 码值 的运行时索引（避免 config.js 里写魔法数字）。 */
const NAME_TO_CODE = (() => {
  const map = {}
  for (const code of Object.keys(ERROR_CODE_MAP)) {
    const item = ERROR_CODE_MAP[code]
    if (item && item.name) {
      map[item.name] = Number(code)
    }
  }
  return map
})()

/** 「查询为空」放行集合：由 config.js 的名称声明解析而来，禁止再写魔法数字白名单。 */
export const PASS_AS_EMPTY = new Set(
  passAsEmptyNames.map(name => NAME_TO_CODE[name]).filter(code => code !== undefined)
)

/**
 * 统一成功判定。
 *
 * 解决的问题：成功/失败码值语义在两端反转 —— 后端 CommonStatusEnum 定义 SUCCESS=1 / FAIL=0，
 * admin 判 `code===200 || code===1` 为成功，cmsadmin 却把 0 以及 5 个错误码当成功放行。
 * 现在成功与否只看码集合，不再看 message。
 *
 * @param {number|string} code 响应码
 * @returns {boolean} true 表示成功
 */
export function isSuccessCode(code) {
  const n = Number(code)
  if (Number.isNaN(n)) return false
  if (compatZeroAsSuccess && n === FAIL_CODE) return true
  return COMPAT_SUCCESS_CODES.includes(n)
}

/**
 * @returns {number} 唯一规范成功码
 */
export function canonicalSuccessCode() {
  return CANONICAL_SUCCESS_CODE
}

/**
 * 把码值归一到统一新码。
 *
 * @param {number|string} code 响应码（新码或旧码）
 * @returns {number|null} 统一新码；无法归一（含歧义旧码）时返回 null
 */
export function normalizeCode(code) {
  const n = Number(code)
  if (Number.isNaN(n)) return null
  if (ERROR_CODE_MAP[n]) return n
  const candidates = LEGACY_TO_NEW[n]
  if (Array.isArray(candidates) && candidates.length === 1) {
    return candidates[0]
  }
  return null
}

/**
 * 按名称取统一新码（供业务侧显式声明使用）。
 *
 * @param {string} name 错误码英文名，如 PERMISSION_ACCESS_DENIED
 * @returns {number|null} 码值
 */
export function codeOfName(name) {
  const code = NAME_TO_CODE[name]
  return code === undefined ? null : code
}

/**
 * 解析错误码 → 展示信息（纯函数，无副作用）。
 *
 * 优先级：① 新码命中映射 ② 旧码归一后命中映射 ③ 按 category 兜底 ④ SYSTEM 兜底。
 * 任何路径都不返回后端原文 —— exposeToUser=false 的码一律出 category 兜底文案。
 *
 * @param {number|string} code 响应码
 * @param {{ backendMessage?: string }} [options] 可选：后端原文（仅用于关键词推类别）
 * @returns {{ code: number, legacyCode: number|null, name: string|null, text: string,
 *             uiAction: string, category: string, level: string, retryable: boolean,
 *             exposeToUser: boolean, matched: boolean }} 展示信息
 */
export function resolveError(code, options = {}) {
  const raw = Number(code)
  const unified = normalizeCode(raw)

  if (unified !== null) {
    const item = ERROR_CODE_MAP[unified]
    const text = item.exposeToUser
      ? item.userMessage
      : (CATEGORY_TEXT[item.category] || CATEGORY_TEXT.SYSTEM)
    return {
      code: unified,
      legacyCode: unified === raw ? null : raw,
      name: item.name,
      text,
      uiAction: item.uiAction,
      category: item.category,
      level: item.level,
      retryable: item.retryable,
      exposeToUser: item.exposeToUser,
      matched: true
    }
  }

  // 兜底：① 本端显式声明的未认领旧码 ② 按关键词推类别（兼容期开关） ③ 降级为 SYSTEM
  let category = 'SYSTEM'
  if (legacyAuthCodes.includes(raw)) {
    category = 'AUTH'
  } else if (enableKeywordFallback) {
    const guessed = inferCategoryByKeyword(options.backendMessage)
    if (guessed) category = guessed
  }
  return {
    code: Number.isNaN(raw) ? 0 : raw,
    legacyCode: Number.isNaN(raw) ? null : raw,
    name: null,
    text: CATEGORY_TEXT[category] || CATEGORY_TEXT.SYSTEM,
    uiAction: CATEGORY_UI[category] || 'TOAST',
    category,
    level: 'ERROR',
    retryable: false,
    exposeToUser: false,
    matched: false
  }
}

/**
 * 是否应按「空状态」放行（不弹 Toast）。
 *
 * @param {number|string} code 响应码
 * @returns {boolean} true 表示放行
 */
export function shouldPassAsEmpty(code) {
  const unified = normalizeCode(code)
  if (unified === null) return false
  return PASS_AS_EMPTY.has(unified)
}

/**
 * 是否应触发「清 token + 跳登录」。
 *
 * 硬性约束：只有 AUTH 类别可以触发跳登录（PRD §6.1），
 * 不再由 message 关键词或裸码 11006 触发。业务声明 skipAuthRedirect 时仍可覆盖。
 *
 * @param {{ category: string }} resolved resolveError 的返回值
 * @returns {boolean} true 表示应跳登录
 */
export function shouldRedirectLogin(resolved) {
  return !!resolved && resolved.category === 'AUTH'
}

/**
 * 把后端原文只写进控制台（绝不出屏）。
 *
 * @param {Object} res 响应体
 * @param {Object} [extra] 附加上下文（url 等）
 */
export function logBackendMessage(res, extra = {}) {
  if (!enableBackendMessageLog) return
  const message = res && res.message !== undefined ? res.message : res
  const check = sanitizeBackendMessage(typeof message === 'string' ? message : '')
  console.warn('[后端原文·仅日志，禁止出屏]', {
    ...extra,
    code: res && res.code,
    message,
    sensitive: !check.safe,
    sensitiveReason: check.reason
  })
}

/**
 * 展示错误（唯一有副作用的函数）。
 *
 * 按 uiAction 分发展示动作；业务侧可用 skipErrorMessage 静默、skipAuthRedirect 阻止跳登录。
 *
 * @param {Object} resolved resolveError 的返回值
 * @param {{ skipErrorMessage?: boolean, skipAuthRedirect?: boolean,
 *           onRedirectLogin?: Function, onEmptyState?: Function }} [options] 选项
 * @returns {string} 实际展示的文案（便于调用方写日志或单测断言）
 */
export function presentError(resolved, options = {}) {
  if (!resolved) return ''
  const {
    skipErrorMessage = false,
    skipAuthRedirect = false,
    onRedirectLogin,
    onEmptyState
  } = options

  if (resolved.category === 'AUTH' && !skipAuthRedirect) {
    if (typeof onRedirectLogin === 'function') {
      onRedirectLogin(resolved)
    }
    return resolved.text
  }

  // 空状态与静默场景不弹 Toast
  if (resolved.uiAction === 'EMPTY_STATE' || resolved.uiAction === 'NONE' || skipErrorMessage) {
    if (resolved.uiAction === 'EMPTY_STATE' && typeof onEmptyState === 'function') {
      onEmptyState(resolved)
    }
    return resolved.text
  }

  showToast(resolved)
  return resolved.text
}

/**
 * 弹 Toast（动态 import，避免在纯函数模块里硬依赖 Element Plus，便于单测与其它端复用）。
 *
 * @param {Object} resolved resolveError 的返回值
 */
function showToast(resolved) {
  const duration = resolved.uiAction === 'DIALOG' ? 5000 : 3000
  try {
    import('element-plus')
      .then(({ ElMessage }) => {
        ElMessage({ message: resolved.text, type: 'error', duration, showClose: true })
      })
      .catch(() => {
        // 非 Element Plus 环境（如单测）静默降级为 console.warn
        console.warn('[错误提示]', resolved.text)
      })
  } catch (error) {
    console.warn('[错误提示·降级]', resolved.text)
  }
}

export { CATEGORY_TEXT, CATEGORY_UI, ERROR_CODE_MAP, LEGACY_TO_NEW }
