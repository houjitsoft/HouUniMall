/**
 * 右下角知识库挂件（WeKnora「小匠」）接入层。
 *
 * 两件事：
 *
 * 1）启动：SDK 由 index.html 引入，但不带 data-channel，避免用页面里的长令牌自动初始化。
 *    真正的渠道配置放在站点上的 `weknora-widget.config.json`（不进仓库），
 *    运行时拉取后再 init —— 访问令牌不会出现在源码/开源包里。
 *
 * 2）上下文：把「谁、在哪个模块、哪个页面、看哪条数据」压成短字段，
 *    通过 WeKnora.setContext 注入。挂件 SDK 会把这些字段拼在每次提问前面：
 *        [Host context] 页面: 商品管理 / 商品列表 路由: /mall/goods/list  用户问题
 *    知识库检索与回答因此能落到具体模块，而不是泛泛而谈。
 *
 * 说明：上下文是拼进 query 文本的，字段越多越稀释向量检索，
 * 所以这里只放「定位类」短值，总长有上限（MAX_TOTAL_CHARS）。
 *
 * @author Amos
 */

/** 渠道配置候选地址，按顺序取第一个可用的 */
const CONFIG_URLS = ['/admin/weknora-widget.config.json', '/weknora-widget.config.json']

/** 单字段最长字符数，防止把整段报错/表格塞进去 */
const MAX_VALUE_CHARS = 90

/** 上下文总长上限（拼进 query，别把用户问题淹掉） */
const MAX_TOTAL_CHARS = 600

let initPromise = null
let widget = null
let widgetReady = false
let routeContext = {}
let pageContext = {}
let lastSignature = ''

function getSdk() {
  return typeof window !== 'undefined' ? window.WeKnora : null
}

function normalizeText(value) {
  if (value === null || value === undefined) return ''
  const text = String(value).replace(/\s+/g, ' ').trim()
  if (!text) return ''
  return text.length > MAX_VALUE_CHARS ? `${text.slice(0, MAX_VALUE_CHARS)}…` : text
}

/**
 * 拉取挂件渠道配置。站点上没放配置文件时返回 null（静默不挂载，不影响后台使用）。
 */
async function loadWidgetConfig() {
  if (typeof window !== 'undefined' && window.__WEKNORA_CONFIG__) {
    return window.__WEKNORA_CONFIG__
  }
  for (const url of CONFIG_URLS) {
    try {
      const res = await fetch(url, { credentials: 'omit', cache: 'no-cache' })
      if (!res.ok) continue
      const cfg = await res.json()
      if (cfg?.channel && cfg?.token) return cfg
    } catch {
      // 换下一个候选地址；全都失败就当作未配置
    }
  }
  return null
}

/**
 * 合并「路由级上下文」与「页面级上下文」，按总长截断。
 * @returns {Record<string, string>}
 */
export function buildAssistantContext() {
  const merged = { ...routeContext, ...pageContext }
  const result = {}
  let total = 0
  for (const [key, raw] of Object.entries(merged)) {
    const value = normalizeText(raw)
    if (!value) continue
    const cost = key.length + value.length
    if (total + cost > MAX_TOTAL_CHARS) break
    result[key] = value
    total += cost
  }
  return result
}

/**
 * 把当前上下文推给挂件。挂件未就绪时只记下，ready 后会补推一次。
 * @param {boolean} force 内容没变也重推（首次 ready / 重新打开面板时用）
 */
export function pushAssistantContext(force = false) {
  if (!widget || !widgetReady) return
  const context = buildAssistantContext()
  const signature = JSON.stringify(context)
  if (!signature || (!force && signature === lastSignature)) return
  lastSignature = signature
  try {
    widget.setContext(context)
  } catch (e) {
    console.warn('[WeKnora] 注入页面上下文失败', e)
  }
}

/**
 * 路由/菜单变化时由布局层调用，整体替换路由级上下文。
 * @param {Record<string, string>} next
 */
export function updateAssistantContext(next = {}) {
  routeContext = next || {}
  pushAssistantContext()
}

/**
 * 页面级上下文：业务页可以把「当前正在看哪条数据」补进来，
 * 例如 setAssistantPageContext({ 当前商品: '智能手表 X1', 商品ID: 1024 })。
 * 路由切换时由布局层自动清空。
 * @param {Record<string, string>} extra
 */
export function setAssistantPageContext(extra = {}) {
  pageContext = { ...pageContext, ...(extra || {}) }
  pushAssistantContext()
}

/** 清空页面级上下文 */
export function clearAssistantPageContext() {
  pageContext = {}
  pushAssistantContext()
}

/**
 * 从菜单树里找出当前路由命中的菜单链（一级 / 二级 / 三级名称）。
 * @param {Array} menus 菜单树
 * @param {string} path 当前路由 path
 * @returns {string[]}
 */
export function resolveMenuTrail(menus, path) {
  const normalizePath = (value) => String(value || '')
    .split(/[?#]/)[0]
    .trim()
    .replace(/\/+$/, '')
  const target = normalizePath(path)
  if (!target) return []

  const walk = (nodes, trail) => {
    let best = null
    for (const node of nodes || []) {
      const nodePath = normalizePath(node?.paths || node?.path)
      const nextTrail = [...trail, normalizeText(node?.name)].filter(Boolean)
      const childTrail = walk(node?.children, nextTrail)
      if (childTrail) best = childTrail
      const hitSelf = nodePath && (target === nodePath || target.startsWith(`${nodePath}/`))
      if (hitSelf && (!best || nextTrail.length > best.length)) best = nextTrail
    }
    return best
  }

  return walk(menus, []) || []
}

/**
 * 启动挂件。幂等，重复调用返回同一个 Promise。
 * @returns {Promise<object|null>}
 */
export function initAssistantWidget() {
  if (initPromise) return initPromise
  initPromise = (async () => {
    const config = await loadWidgetConfig()
    if (!config) return null
    const sdk = getSdk()
    if (!sdk) {
      console.warn('[WeKnora] SDK 未加载，跳过知识库挂件')
      return null
    }
    try {
      widget = sdk.init({
        channel: config.channel,
        token: config.token,
        position: config.position || 'bottom-right',
        primaryColor: config.primaryColor || config.primary_color || '#07c05f',
        title: config.title || '小匠',
        baseUrl: config.baseUrl || ''
      })
    } catch (e) {
      console.warn('[WeKnora] 初始化失败', e)
      return null
    }
    if (!widget) return null

    sdk.on('ready', () => {
      widgetReady = true
      // 首次就绪补推一次，避免用户在 iframe 挂载前就打开面板
      pushAssistantContext(true)
    })
    sdk.on('open', () => {
      // 面板打开时上下文可能已经变了（SPA 内部跳转），重推一次保证最新
      pushAssistantContext()
    })
    return widget
  })()
  return initPromise
}
