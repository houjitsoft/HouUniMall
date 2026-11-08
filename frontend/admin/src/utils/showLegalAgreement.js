/**
 * 注册协议 / 服务条款 / 隐私政策 统一入口。
 *
 * 文案来源优先级：
 *   1. 后端「协议定义 + 生效版本」表（`/web/agreement/list`，可在后台维护、可版本化）
 *   2. 旧版系统配置键（`privacy_policy` / `service_terms` 等，兼容未发版的旧后端）
 *   3. 内置兜底文案
 *
 * 注册页通过 `loadAgreements('register')` 一次性拿到三项协议（含版本号），
 * 提交注册时把 `{code, versionNo}` 回传，后端据此校验并留痕。
 */
import { ElMessage, ElMessageBox } from 'element-plus'
import { websiteSettingApi } from '@/api/setting/website'
import { agreementApi } from '@/api/setting/agreement'

/** 已加载的协议清单缓存：scene -> list */
const cache = new Map()
/** 并发去重：scene -> Promise */
const inflight = new Map()

let dialogOpening = false

/**
 * 后端不可用时的兜底协议清单：名称用于渲染勾选框。
 *
 * 顺序与 sort 必须与 `scripts/sql/init/21-agreement-consent.sql` 中的协议定义一致
 * （服务协议 10 → 隐私政策 20 → 合规承诺书 30），否则接口恢复后注册页的展示顺序会突变。
 *
 * 注意：兜底模式下不带 versionNo，提交时不会伪造同意版本。
 */
const FALLBACK_AGREEMENTS = {
  register: [
    { code: 'USER_SERVICE_AGREEMENT', name: '用户服务协议', required: 1, defaultChecked: 0, sort: 10, versionNo: '', content: '' },
    { code: 'PRIVACY_POLICY', name: '隐私政策', required: 1, defaultChecked: 0, sort: 20, versionNo: '', content: '' },
    { code: 'COMPLIANCE_COMMITMENT', name: '账号与内容合规使用承诺书', required: 1, defaultChecked: 0, sort: 30, versionNo: '', content: '' }
  ],
  plugin_purchase: [
    { code: 'PLUGIN_COMMERCIAL_PURCHASE', name: '商业插件购买与维护协议', required: 1, defaultChecked: 0, sort: 10, versionNo: '', content: '' }
  ]
}

/**
 * 内置正文副本（由 scripts/gen_agreement_fallback.py 从 SQL 种子生成）。
 *
 * 为什么需要：协议接口不可用时，注册页仍然要求用户勾选「已阅读并同意」，
 * 此时若弹窗只能显示「正文暂未配置」，等于让用户在看不到条款的情况下作出同意。
 * 该副本仅在接口取不到正文时兜底；此场景前端不回传 agreementConsents，
 * 后端不会写入同意记录，因此不会产生「同意版本」与实际展示不一致的举证风险。
 *
 * 用动态 import 加载，避免这几 KB 法律文本进入注册页首屏包体。
 */
let fallbackContentPromise = null
function loadFallbackContent() {
  if (!fallbackContentPromise) {
    fallbackContentPromise = import('./agreementFallbackContent.js').catch((error) => {
      console.warn('[agreement] 内置协议正文加载失败', error?.message || error)
      return null
    })
  }
  return fallbackContentPromise
}

/** 旧版配置键兜底（未发版后端仍可用） */
const LEGACY_CONFIG_KEYS = {
  PRIVACY_POLICY: ['privacy_policy', 'privacy_terms', 'user_privacy', 'privacy'],
  USER_SERVICE_AGREEMENT: ['service_terms', 'terms_of_service', 'user_agreement', 'agreement'],
  COMPLIANCE_COMMITMENT: []
}

function extractConfigValue(data) {
  if (!data) return ''
  if (typeof data === 'string') return data.trim()
  const value = data.configValue || data.config_value || data.value || ''
  return typeof value === 'string' ? value.trim() : ''
}

async function fetchLegacyContent(code) {
  const keys = LEGACY_CONFIG_KEYS[code] || []
  for (const key of keys) {
    try {
      const res = await websiteSettingApi.getConfigByKey(key)
      if (res && res.code === 1) {
        const text = extractConfigValue(res.data)
        if (text) return text
      }
    } catch (error) {
      console.warn(`读取配置失败: ${key}`, error)
    }
  }
  return ''
}

/**
 * 加载某场景下的协议清单（含当前生效版本正文）。
 *
 * 加载失败或后端未发版时回退到内置清单，**不抛异常**，保证注册页可用。
 *
 * @param {string} [scene] 场景，默认 register
 * @returns {Promise<{agreements: Array, fromApi: boolean}>}
 */
export async function loadAgreements(scene = 'register') {
  if (cache.has(scene)) {
    const hit = cache.get(scene)
    return { agreements: hit.list, fromApi: hit.fromApi }
  }
  if (inflight.has(scene)) {
    return inflight.get(scene)
  }
  const task = (async () => {
    try {
      const res = await agreementApi.list(scene)
      const list = Array.isArray(res?.data) ? res.data : []
      if (list.length > 0) {
        cache.set(scene, { list, fromApi: true })
        return { agreements: list, fromApi: true }
      }
    } catch (error) {
      console.warn('[agreement] 协议接口不可用，回退内置文案', error?.message || error)
    }
    const fallback = FALLBACK_AGREEMENTS[scene] || []
    cache.set(scene, { list: fallback, fromApi: false })
    return { agreements: fallback, fromApi: false }
  })()
  inflight.set(scene, task)
  try {
    return await task
  } finally {
    inflight.delete(scene)
  }
}

/** 同步读取缓存（未加载过返回 null） */
export function getCachedAgreements(scene = 'register') {
  return cache.has(scene) ? cache.get(scene).list : null
}

/** 清空缓存（登出、版本变更后调用） */
export function resetAgreementCache() {
  cache.clear()
}

function findAgreement(codeOrAgreement, scene = 'register') {
  if (codeOrAgreement && typeof codeOrAgreement === 'object') {
    return codeOrAgreement
  }
  const list = getCachedAgreements(scene) || []
  return list.find((item) => item.code === codeOrAgreement) || null
}

/**
 * 解析协议正文（供自定义阅读弹窗复用）。
 *
 * 解析优先级：协议对象自带 content → 旧配置键兜底 → 内置副本 → 占位提示。
 *
 * @param {Object} agreement 协议对象
 * @returns {Promise<string>} 协议正文（未转义的纯文本）
 */
export async function resolveAgreementContent(agreement) {
  let content = (agreement.content || '').trim()
  if (!content) {
    // 接口没给正文：先试旧配置键（兼容未发版的旧后端），再退回内置副本
    content = await fetchLegacyContent(agreement.code)
  }
  if (!content) {
    const fallbackModule = await loadFallbackContent()
    content = (fallbackModule?.AGREEMENT_FALLBACK_CONTENT?.[agreement.code] || '').trim()
  }
  return content || `《${agreement.name}》正文暂未配置，请联系平台管理员。`
}

/**
 * 弹窗展示某个协议正文。
 *
 * @param {string|Object} codeOrAgreement 协议编码或协议对象
 * @param {string} [scene] 场景
 */
export async function showAgreement(codeOrAgreement, scene = 'register') {
  if (dialogOpening) return
  dialogOpening = true
  try {
    let agreement = findAgreement(codeOrAgreement, scene)
    if (!agreement) {
      const { agreements } = await loadAgreements(scene)
      agreement = Array.isArray(codeOrAgreement)
        ? null
        : agreements.find((item) => item.code === codeOrAgreement)
    }
    if (!agreement) {
      ElMessage.warning('未找到该协议，请刷新页面后重试')
      return
    }
    const text = await resolveAgreementContent(agreement)
    const title = agreement.versionNo ? `${agreement.name}（${agreement.versionNo}）` : agreement.name
    await ElMessageBox.alert(text, title, {
      confirmButtonText: '我已了解',
      customClass: 'agreement-dialog',
      // 正文为纯文本，转义后再换行渲染，避免历史配置中的 HTML 造成 XSS
      dangerouslyUseHTMLString: true,
      message: escapeAndWrap(text)
    })
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.message || '协议加载失败')
    }
  } finally {
    dialogOpening = false
  }
}

function escapeAndWrap(text) {
  const escaped = String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  return `<div style="max-height:60vh;overflow:auto;white-space:pre-wrap;line-height:1.8;">${escaped}</div>`
}

// ================= 兼容旧调用（登录页等处） =================

/** 服务条款（= 用户服务协议） */
export function showServiceTermsAgreement() {
  return showAgreement('USER_SERVICE_AGREEMENT')
}

/** 隐私政策 */
export function showPrivacyPolicyAgreement() {
  return showAgreement('PRIVACY_POLICY')
}

/** 账号与内容合规使用承诺书 */
export function showComplianceCommitmentAgreement() {
  return showAgreement('COMPLIANCE_COMMITMENT')
}
