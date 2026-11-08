import { defineStore } from 'pinia'
import { 
  DataBoard, 
  User, 
  Goods, 
  Document, 
  Setting 
} from '@element-plus/icons-vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { getUserMenus } from '@/api/auth/auth'
import { getToken } from '@/utils/auth'
import { invalidateUserMenusCache, readUserMenusCache, refreshPermissionsFromServer } from '@/utils/userMenusCache'
import { mergeSettingAiConfigMenu } from '@/utils/settingAiConfigMenuMerge'
import { normalizeCrmMenus } from '@/utils/crmMenuNormalize'
import { mergePluginMenus, normalizePluginMenuNames } from '@/utils/pluginMenuMerge'
import { normalizePlatformPluginMenus } from '@/utils/platformPluginMenu'
import { getPluginCategories } from '@/api/plugin-center/pluginCenter'

// 注册图标组件
const iconComponents = {
  DataBoard,
  User,
  Goods,
  Document,
  Setting
}

// 全局菜单请求Promise，防止并发重复请求
let _menuFetchPromise = null
let _menuFetchToken = null
let _menuFetchVersion = 0
let _categoryFetchToken = null
let _categoryFetchVersion = 0

const menuListFromResponse = response => {
  if (Array.isArray(response)) return response
  if (!response || (response.code != null && response.code !== 1 && response.code !== 200)) return null
  const data = response.data
  return Array.isArray(data) ? data : Array.isArray(data?.list) ? data.list : null
}

/**
 * 深拷贝菜单树，避免各来源的补全逻辑反复污染服务端原始数据
 * @param {Array} menus 菜单树
 * @returns {Array}
 */
function cloneMenuTree(menus) {
  try {
    return JSON.parse(JSON.stringify(menus))
  } catch {
    return menus
  }
}

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebar: {
      opened: false,
    },
    device: 'desktop',
    language: 'zh-CN',
    // 动态菜单列表（= baseMenus + 各来源补全 + 插件菜单）
    dynamicMenus: [],
    // 服务端返回的原始菜单树（重建菜单树的数据源）
    baseMenus: [],
    // 外部来源贡献的菜单（T10：插件前端产物）
    extraMenus: [],
    // 菜单是否已加载
    menuLoaded: false,
    // 插件中心分类（hou_addon_cate），顶栏右上角按分类聚合插件菜单
    pluginCategories: [],
    pluginCategoriesError: false,
    // 当前被点选的导航一级项 id（左侧竖栏或右上角插件菜单共用），
    // 用于让二级菜单跟着点选走，而不是只靠路由反推
    activeNavMenuId: null
  }),
  
  getters: {
    sidebarRoutes: (state) => {
      // 如果有动态菜单，则使用动态菜单
      if (state.dynamicMenus && state.dynamicMenus.length > 0) {
        return state.dynamicMenus.map(menu => ({
          path: menu.paths,
          meta: {
            title: menu.name,
            icon: menu.icon || 'Document'
          }
        }))
      }
      
      // 否则使用静态菜单
      return [
        {
          path: '/dashboard',
          meta: {
            title: 'menu.dashboard',
            icon: 'DataBoard'
          }
        },
        {
          path: '/users',
          meta: {
            title: 'menu.userManagement',
            icon: 'User'
          }
        },
        {
          path: '/products',
          meta: {
            title: 'menu.productManagement',
            icon: 'Goods'
          }
        },
        {
          path: '/orders',
          meta: {
            title: 'menu.orderManagement',
            icon: 'Document'
          }
        },
        {
          path: '/settings',
          meta: {
            title: 'menu.systemSettings',
            icon: 'Setting'
          }
        }
      ]
    },
    
    // 获取图标组件
    getIconComponent: (state) => (iconName) => {
      // 首先尝试从 Element Plus 图标中获取
      if (ElementPlusIconsVue[iconName]) {
        return ElementPlusIconsVue[iconName]
      }
      
      // 然后尝试从已注册的图标组件中获取
      if (iconComponents[iconName]) {
        return iconComponents[iconName]
      }
      
      // 默认返回 Document 图标
      return ElementPlusIconsVue.Document
    }
  },
  
  actions: {
    toggleSidebar() {
      this.sidebar.opened = !this.sidebar.opened
    },
    
    closeSidebar() {
      this.sidebar.opened = false
    },
    
    openSidebar() {
      this.sidebar.opened = true
    },
    
    toggleDevice(device) {
      this.device = device
    },
    
    setLanguage(language) {
      this.language = language
    },
    
    // 设置动态菜单
    setDynamicMenus(menus) {
      this.dynamicMenus = menus
    },

    /**
     * 接收服务端菜单树并重建（各来源补全 + 插件菜单）
     * @param {Array} menuData 服务端菜单树
     * @returns {Array} 最终生效的菜单树
     */
    _applyMenuData(menuData) {
      if (!Array.isArray(menuData)) menuData = []
      this.baseMenus = menuData
      // 只有接收服务器菜单（含成功的空结果）才完成权限加载。
      // 插件语言包的提前重建不能让深链守卫跳过首次权限请求。
      this.menuLoaded = true
      return this._rebuildMenus()
    },

    /**
     * 由 baseMenus 重建动态菜单树（幂等，可在插件菜单变化时重复调用）
     *
     * 顺序：Core 菜单补全（设置等）→ 插件菜单（T10）
     * 每次都在深拷贝上执行，保证服务端原始数据不被就地污染。
     * @returns {Array} 最终生效的菜单树
     */
    _rebuildMenus() {
      if (!Array.isArray(this.baseMenus) || this.baseMenus.length === 0) {
        this.setDynamicMenus([])
        return []
      }
      const tree = cloneMenuTree(this.baseMenus)
      mergeSettingAiConfigMenu(tree)
      normalizeCrmMenus(tree)
      normalizePlatformPluginMenus(tree)
      mergePluginMenus(tree, this.extraMenus)
      normalizePluginMenuNames(tree)
      this.setDynamicMenus(tree)
      return tree
    },

    /**
     * 设置外部来源贡献的菜单（插件前端产物）
     * @param {Array} menus 菜单节点
     */
    setExtraMenus(menus) {
      this.extraMenus = Array.isArray(menus) ? menus : []
      if (this.baseMenus?.length) {
        this._rebuildMenus()
      }
    },

    /**
     * 全局统一获取用户菜单：先读 localStorage 缓存，再后台拉服务端更新
     * @param {boolean} forceRefresh - 是否强制走网络
     * @returns {Promise<Array>} 菜单数据
     */
    async fetchMenus(forceRefresh = false) {
      const token = getToken()
      if (!token) return []

      if (!forceRefresh && this.menuLoaded && this.dynamicMenus?.length > 0) {
        return this.dynamicMenus
      }

      if (_menuFetchPromise && _menuFetchToken === token) {
        return _menuFetchPromise
      }

      const version = ++_menuFetchVersion
      _menuFetchToken = token
      const isCurrent = () => version === _menuFetchVersion && token === getToken()

      _menuFetchPromise = (async () => {
        try {
          let menuData = []
          if (!forceRefresh) {
            const cached = readUserMenusCache({})
            if (Array.isArray(cached) && cached.length > 0) {
              menuData = this._applyMenuData(cached)
              if (typeof navigator !== 'undefined' && navigator.onLine) {
                refreshPermissionsFromServer().catch(() => {})
                getUserMenus({}, { forceRefresh: true })
                  .then((fresh) => {
                    if (!isCurrent()) return
                    const list = menuListFromResponse(fresh)
                    if (list !== null) this._applyMenuData(list)
                  })
                  .catch(() => {})
              }
              return menuData
            }
          }

          const response = await getUserMenus({}, { forceRefresh })
          if (!isCurrent()) return []
          const list = menuListFromResponse(response)
          return list === null ? [] : this._applyMenuData(list)
        } catch (error) {
          if (!isCurrent()) return []
          const cached = readUserMenusCache({})
          if (Array.isArray(cached) && cached.length > 0) {
            console.warn('[AppStore] 获取菜单失败，使用本地缓存', error?.message)
            return this._applyMenuData(cached)
          }
          console.error('[AppStore] 获取菜单失败:', error)
          return []
        }
      })()

      // 缓存分支可同步完成；必须在赋值后清理，否则会永久复用已经完成的 Promise。
      try {
        return await _menuFetchPromise
      } finally {
        if (version === _menuFetchVersion) _menuFetchPromise = null
      }
    },

    /**
     * 加载插件中心分类（顶栏右上角「插件应用」的分类来源）。
     * 失败时保留当前会话最近成功字典，并提供面板重试状态。
     */
    async loadPluginCategories() {
      const token = getToken()
      if (token !== _categoryFetchToken) {
        this.pluginCategories = []
        this.pluginCategoriesError = false
      }
      _categoryFetchToken = token
      const version = ++_categoryFetchVersion
      const isCurrent = () => version === _categoryFetchVersion && token === getToken()
      if (!token) return this.pluginCategories
      try {
        const list = await getPluginCategories()
        if (!isCurrent()) return this.pluginCategories
        if (!Array.isArray(list)) throw new Error('应用分类响应格式异常')
        this.pluginCategories = list
        this.pluginCategoriesError = false
      } catch (error) {
        if (isCurrent()) {
          console.warn('[AppStore] 插件分类加载失败:', error?.message || error)
          this.pluginCategoriesError = true
        }
      }
      return this.pluginCategories
    },

    /**
     * 记录当前点选的导航一级项，二级菜单据此联动
     * @param {number|string|null} id
     */
    setActiveNavMenu(id) {
      this.activeNavMenuId = id == null ? null : String(id)
    },

    /**
     * 清除菜单内存与本地缓存
     * @param {{ skipInvalidate?: boolean, keepVisible?: boolean }} [options]
     * skipInvalidate 为 true 时不重复 bump 失效戳（跨标签同步时用）。
     * keepVisible 为 true 时只作废进行中的菜单请求，侧栏继续显示当前一二级菜单，
     * 等下一次 fetchMenus 成功后再替换，避免清缓存时菜单先消失再出现。
     */
    clearMenus(options = {}) {
      const { skipInvalidate = false, keepVisible = false } = options
      if (!skipInvalidate) {
        invalidateUserMenusCache()
      }
      if (!keepVisible) {
        this.dynamicMenus = []
        this.baseMenus = []
        this.menuLoaded = false
        this.activeNavMenuId = null
        if (_categoryFetchToken !== getToken()) {
          ++_categoryFetchVersion
          _categoryFetchToken = null
          this.pluginCategories = []
          this.pluginCategoriesError = false
        }
      }
      ++_menuFetchVersion
      _menuFetchToken = null
      _menuFetchPromise = null
    }
  }
})
