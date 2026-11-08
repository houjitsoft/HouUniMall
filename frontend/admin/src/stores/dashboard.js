/**
 * Dashboard Pinia Store
 * - 负责用户偏好的加载、合并、保存、重置
 * - 与后端 /web/base/dashboard/pref/* 对接
 * - 失败时使用 localStorage 作为本地兜底
 *
 * 偏好分两层：
 * - items：工作台模块网格里的卡片（moduleId / visible / sort / x / y / w / h）
 * - sections：页面六大区块（hero / stats / quick / todo / trend / widgets），
 *   每块带自己的 config（见 @/config/dashboard/sections.js）
 *
 * @author Amos
 * @since 2026-05-13
 * @updated 2026-09-21 新增区块级偏好
 */
import { defineStore } from 'pinia'
import { markRaw } from 'vue'
import { dashboardApi } from '@/api/dashboard'
import { DASHBOARD_MODULES, buildDefaultItems } from '@/config/dashboard/modules'
import {
  DASHBOARD_SECTIONS,
  getSectionById,
  buildDefaultSectionConfig,
  buildDefaultSections,
  sanitizeSectionConfig
} from '@/config/dashboard/sections'

const LS_KEY = 'houcloud:dashboard:pref'
const DEFAULT_COLS = 12
const DEFAULT_REFRESH = 0

/**
 * 合并已有偏好 items 与注册中心的模块：
 * - 旧偏好中存在、但已从注册中心移除的模块被丢弃
 * - 注册中心新增、旧偏好没有的模块追加到末尾（visible=1）
 * @param {Array} items
 * @returns {Array}
 */
const normalizeItems = (items) => {
  const validIds = new Set(DASHBOARD_MODULES.map((m) => m.id))
  const list = Array.isArray(items) ? items.filter((it) => it && validIds.has(it.moduleId)) : []
  const seen = new Set(list.map((it) => it.moduleId))
  DASHBOARD_MODULES.forEach((m, idx) => {
    if (!seen.has(m.id)) {
      list.push({
        moduleId: m.id,
        visible: 1,
        sort: list.length + idx,
        x: 0,
        y: 0,
        w: m.defaultW,
        h: m.defaultH
      })
    }
  })
  // 按 sort 再规整一遍
  list.sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
  list.forEach((it, idx) => {
    it.sort = idx
  })
  return list
}

/**
 * 合并已有区块偏好与区块注册表：
 * - 注册中心没有的区块丢弃
 * - 注册表新增的区块追加到末尾（visible=1）
 * - config 逐块 sanitize，脏字段丢弃、缺失补默认
 * @param {Array} sections
 * @returns {Array}
 */
const normalizeSections = (sections) => {
  const validIds = new Set(DASHBOARD_SECTIONS.map((s) => s.id))
  const list = Array.isArray(sections)
    ? sections
      .filter((it) => it && validIds.has(it.id))
      .map((it) => ({
        id: it.id,
        visible: it.visible === 0 ? 0 : 1,
        sort: Number.isFinite(Number(it.sort)) ? Number(it.sort) : 0,
        config: sanitizeSectionConfig(it.id, it.config)
      }))
    : []
  const byId = new Map(list.map((it) => [it.id, it]))

  /**
   * 注册表里有、老偏好里没有的区块（如 2026-09-21 商城化新增的 alerts / rank）。
   * 直接追加到末尾会把首屏内容挤到第二屏，因此这种情况下按注册表默认顺序整表重排，
   * 用户已有的显隐与区块配置全部保留；补齐之后再调序就不会再被重置。
   */
  const missing = DASHBOARD_SECTIONS.filter((s) => !byId.has(s.id))
  if (missing.length === 0) {
    list.sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
    list.forEach((it, idx) => { it.sort = idx })
    return list
  }

  return DASHBOARD_SECTIONS
    .slice()
    .sort((a, b) => a.defaultSort - b.defaultSort)
    .map((section, idx) => {
      const hit = byId.get(section.id)
      return hit
        ? { ...hit, sort: idx }
        : { id: section.id, visible: 1, sort: idx, config: buildDefaultSectionConfig(section.id) }
    })
}

export const useDashboardStore = defineStore('dashboard', {
  state: () => ({
    cols: DEFAULT_COLS,
    refreshInterval: DEFAULT_REFRESH,
    items: [],
    sections: [],
    version: 1,
    editMode: false,
    loading: false,
    lastLoadedAt: 0,
    /**
     * 插件注入的工作台面板：{ [slot]: { code, component } }
     *
     * 2026-09-20：商业模块插件化迁移（fc40b9488）把宿主首页的「每日待办 + 临时记事本」
     * 整块删掉换成一句静态提示，但没有留下插件把面板注回首页的通道，导致已安装 OA 插件
     * 的用户也看不到记事本。这里补上挂载位，插件注册后首页按 slot 渲染。
     */
    pluginPanels: {}
  }),

  getters: {
    /** 可见的模块（按 sort 升序） */
    visibleItems: (state) => state.items.filter((it) => it.visible === 1),
    /** 隐藏的模块 */
    hiddenItems: (state) => state.items.filter((it) => it.visible !== 1),
    /** 是否处于默认布局（items 为空代表未保存过） */
    isDefaultLayout: (state) => state.items.length === 0 || state.version <= 1,
    /** 插件提供的「每日待办（含右侧临时记事本）」面板，未安装插件时为 null */
    todoPanel: (state) => state.pluginPanels?.dailyTodo?.component || null,
    /**
     * 区块是否可渲染：注册表声明了 requiresPluginPanel 的区块，
     * 只有对应插件面板已注册（插件已安装且启用、产物加载成功）才算存在。
     * 判定依据是 pluginPanels 里有没有该 slot —— 这是「插件真的能渲染」的
     * 唯一事实源（比查 plugin store 的 installed/enabled 更严：加载失败也会被剔除）。
     * 命中不了的区块整块从列表里消失，页面不留任何容器 / 标题 / 边距 / 占位高度。
     */
    sectionRenderable: (state) => (meta) => {
      if (!meta?.requiresPluginPanel) return true
      return Boolean(state.pluginPanels?.[meta.requiresPluginPanel]?.component)
    },
    /** 可见区块（按 sort 升序），带注册表元数据，供页面渲染 */
    orderedSections: function (state) {
      const renderable = this.sectionRenderable
      return state.sections
        .filter((it) => it.visible === 1)
        .map((it) => {
          const meta = getSectionById(it.id)
          return meta ? { ...it, _meta: meta } : null
        })
        .filter(Boolean)
        .filter((it) => renderable(it._meta))
    },
    /** 全部区块（含隐藏），带注册表元数据，供设置面板使用（同样剔除插件缺失的区块） */
    allSections: function (state) {
      const renderable = this.sectionRenderable
      return state.sections
        .map((it) => {
          const meta = getSectionById(it.id)
          return meta ? { ...it, _meta: meta } : null
        })
        .filter(Boolean)
        .filter((it) => renderable(it._meta))
    },
    /** 取某个区块的生效配置 */
    sectionConfig: (state) => (id) => {
      const hit = state.sections.find((it) => it.id === id)
      return hit?.config || buildDefaultSectionConfig(id)
    }
  },

  actions: {
    /**
     * 注册插件面板（后注册者覆盖同 slot，先到先得由调用方保证）
     * @param {string} slot 挂载位（如 dailyTodo）
     * @param {{code:string, component:object}} entry
     */
    registerPluginPanel(slot, entry) {
      if (!slot || !entry?.component) return
      this.pluginPanels = { ...this.pluginPanels, [slot]: markRaw(entry) }
    },

    /** 卸载某个插件注册的全部面板 */
    unregisterPluginPanels(code) {
      const next = {}
      Object.entries(this.pluginPanels || {}).forEach(([slot, entry]) => {
        if (entry?.code !== code) next[slot] = entry
      })
      this.pluginPanels = next
    },

    /** 切换区块显示/隐藏 */
    toggleSectionVisible(id) {
      const it = this.sections.find((s) => s.id === id)
      if (it) it.visible = it.visible === 1 ? 0 : 1
    },

    /** 上移 / 下移一个区块（设置面板用，dir: -1 上移 / 1 下移） */
    moveSection(id, dir) {
      const idx = this.sections.findIndex((s) => s.id === id)
      const next = idx + dir
      if (idx < 0 || next < 0 || next >= this.sections.length) return
      const list = this.sections.slice()
      const [moved] = list.splice(idx, 1)
      list.splice(next, 0, moved)
      list.forEach((it, i) => { it.sort = i })
      this.sections = list
    },

    /** 根据拖拽结果重排区块 sort */
    reorderSections(orderedIds) {
      const map = new Map(this.sections.map((it) => [it.id, it]))
      const next = []
      orderedIds.forEach((id, idx) => {
        const it = map.get(id)
        if (it) {
          it.sort = idx
          next.push(it)
          map.delete(id)
        }
      })
      Array.from(map.values()).forEach((it) => next.push(it))
      next.forEach((it, i) => { it.sort = i })
      this.sections = next
    },

    /**
     * 局部更新区块配置，与默认值合并后 sanitize
     * @param {string} id
     * @param {Object} patch
     */
    updateSectionConfig(id, patch) {
      const it = this.sections.find((s) => s.id === id)
      if (!it) return
      it.config = sanitizeSectionConfig(id, { ...(it.config || {}), ...(patch || {}) })
    },

    /** 恢复单个区块的默认配置 */
    resetSectionConfig(id) {
      const it = this.sections.find((s) => s.id === id)
      if (it) it.config = buildDefaultSectionConfig(id)
    },

    /**
     * 加载偏好：优先读后端，失败则读 localStorage，再失败取默认
     */
    async loadPref() {
      this.loading = true
      try {
        const res = await dashboardApi.getPref()
        const data = (res && res.data) || {}
        this.cols = data.cols || DEFAULT_COLS
        this.refreshInterval = data.refreshInterval ?? DEFAULT_REFRESH
        this.version = data.version || 1
        this.items = normalizeItems(data.items && data.items.length ? data.items : buildDefaultItems())
        this.sections = normalizeSections(
          Array.isArray(data.sections) && data.sections.length ? data.sections : buildDefaultSections()
        )
        this.lastLoadedAt = Date.now()
        this._saveLocal()
      } catch (err) {
        console.warn('[dashboard] 加载后端偏好失败，使用本地缓存:', err?.message || err)
        this._loadLocal()
      } finally {
        this.loading = false
      }
    },

    /**
     * 保存偏好到后端（失败时仍写本地）
     */
    async savePref() {
      const payload = {
        cols: this.cols,
        refreshInterval: this.refreshInterval,
        items: this.items.map((it, idx) => ({
          moduleId: it.moduleId,
          visible: it.visible ?? 1,
          sort: idx,
          x: it.x ?? 0,
          y: it.y ?? 0,
          w: it.w ?? 4,
          h: it.h ?? 1
        })),
        sections: this.sections.map((it, idx) => ({
          id: it.id,
          visible: it.visible ?? 1,
          sort: idx,
          config: it.config || {}
        }))
      }
      this._saveLocal()
      try {
        await dashboardApi.savePref(payload)
        this.version += 1
        return true
      } catch (err) {
        console.warn('[dashboard] 保存后端偏好失败，仅存本地:', err?.message || err)
        return false
      }
    },

    /** 重置为默认布局 */
    async resetPref() {
      this.cols = DEFAULT_COLS
      this.refreshInterval = DEFAULT_REFRESH
      this.items = buildDefaultItems()
      this.sections = buildDefaultSections()
      this.version = 1
      this._saveLocal()
      try {
        await dashboardApi.resetPref()
      } catch (err) {
        console.warn('[dashboard] 重置后端偏好失败:', err?.message || err)
      }
    },

    /** 切换模块显示/隐藏 */
    toggleVisible(moduleId) {
      const it = this.items.find((i) => i.moduleId === moduleId)
      if (it) {
        it.visible = it.visible === 1 ? 0 : 1
      }
    },

    /** 根据拖拽结果重排 sort */
    reorder(orderedIds) {
      const map = new Map(this.items.map((it) => [it.moduleId, it]))
      const next = []
      orderedIds.forEach((id, idx) => {
        const it = map.get(id)
        if (it) {
          it.sort = idx
          next.push(it)
          map.delete(id)
        }
      })
      // 未在 orderedIds 中的项追加保持原相对顺序
      Array.from(map.values()).forEach((it) => next.push(it))
      this.items = next
    },

    /** 进入/退出编辑模式 */
    setEditMode(v) {
      this.editMode = !!v
    },

    /** 设置列数 */
    setCols(n) {
      const v = Number(n) || DEFAULT_COLS
      this.cols = Math.max(1, Math.min(24, v))
    },

    /** 设置刷新间隔（秒，0 关闭） */
    setRefreshInterval(n) {
      const v = Number(n)
      this.refreshInterval = Number.isFinite(v) && v >= 0 ? v : DEFAULT_REFRESH
    },

    /** 写本地 */
    _saveLocal() {
      try {
        localStorage.setItem(LS_KEY, JSON.stringify({
          cols: this.cols,
          refreshInterval: this.refreshInterval,
          version: this.version,
          items: this.items,
          sections: this.sections
        }))
      } catch (_) { /* ignore quota */ }
    },

    /** 读本地 */
    _loadLocal() {
      try {
        const raw = localStorage.getItem(LS_KEY)
        if (raw) {
          const parsed = JSON.parse(raw)
          this.cols = parsed.cols || DEFAULT_COLS
          this.refreshInterval = parsed.refreshInterval ?? DEFAULT_REFRESH
          this.version = parsed.version || 1
          this.items = normalizeItems(parsed.items || buildDefaultItems())
          this.sections = normalizeSections(parsed.sections || buildDefaultSections())
          return
        }
      } catch (_) { /* fallthrough */ }
      this.cols = DEFAULT_COLS
      this.refreshInterval = DEFAULT_REFRESH
      this.items = buildDefaultItems()
      this.sections = buildDefaultSections()
      this.version = 1
    }
  }
})

export default useDashboardStore
