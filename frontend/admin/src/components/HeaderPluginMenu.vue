<template>
  <div class="header-plugin-menu">
    <el-popover v-model:visible="panelVisible" trigger="click" :width="panelWidth" placement="bottom-end"
      popper-class="header-plugin-menu__popper" @show="openPanel">
      <template #reference>
        <button type="button" class="hc-header-chip hc-header-chip--group"
          :class="{ 'is-active': panelVisible || isAnyActive }" title="插件应用" aria-label="插件应用">
          <span class="header-plugin-menu__icon-wrap"><el-icon><Grid /></el-icon></span>
          <span class="hc-header-chip__text">插件应用</span><el-icon><ArrowDown /></el-icon>
        </button>
      </template>
      <el-input v-model="search" clearable placeholder="搜索应用名称或编码" aria-label="搜索应用" :prefix-icon="Search" />
      <div class="plugin-menu-panel" v-loading="loading && !catalogLoaded">
        <div class="plugin-menu-panel__cates" role="tablist" aria-label="应用分类">
          <button v-for="group in pluginGroups" :key="group.id" type="button" role="tab"
            :aria-selected="group.id === activeGroupId" class="plugin-menu-panel__cate"
            :class="{ 'is-active': group.id === activeGroupId }" @click="activeGroupId = group.id">
            <span>{{ group.name }}</span><span class="plugin-menu-panel__cate-count">{{ group.items.length }}</span>
          </button>
        </div>
        <div class="plugin-menu-panel__items">
          <div v-for="item in activeItems" :key="item.id" class="plugin-menu-panel__card">
            <button type="button" class="plugin-menu-panel__item" :class="{ 'is-active': isItemActive(item) }"
              :title="item.unavailable ? `${item.name}暂不可用` : item.name" @click="handleSelect(item)">
              <span class="plugin-menu-panel__item-icon"><el-icon><component :is="getIconComponent(item.icon)" /></el-icon></span>
              <span class="plugin-menu-panel__item-name">{{ item.name }}</span>
              <span v-if="item.unavailable" class="plugin-menu-panel__item-badge">暂不可用</span>
            </button>
            <button type="button" class="plugin-menu-panel__favorite" :class="{ 'is-favorite': favorites.includes(item.code) }"
              :aria-label="`${favorites.includes(item.code) ? '取消常用' : '设为常用'}：${item.name}`"
              :aria-pressed="favorites.includes(item.code)" @click="toggleFavorite(item.code)">
              <el-icon><component :is="favorites.includes(item.code) ? StarFilled : Star" /></el-icon>
            </button>
          </div>
          <div v-if="!activeItems.length && !loading" class="plugin-menu-panel__guide" role="status">
            <p class="plugin-menu-panel__guide-title">{{ search ? '未找到匹配的应用' : '还没有可使用的应用' }}</p>
            <p class="plugin-menu-panel__guide-desc">
              {{ search ? '换个关键词再试，或到插件市场查看全部应用。' : '应用按账号安装。请先去插件市场购买或用授权码安装，装好后会出现在这里。' }}
            </p>
            <button type="button" class="plugin-menu-panel__guide-btn" @click="openPluginMarket">
              去插件市场安装
            </button>
          </div>
        </div>
      </div>
      <div class="plugin-menu-panel__footer">
        <span v-if="loadError || appStore.pluginCategoriesError" role="status">{{ loadError ? '应用状态' : '应用分类' }}加载失败，<button type="button" @click="refreshApplications">重试</button></span>
        <span v-else>仅展示本账号已安装可进入的应用</span>
        <el-button link type="primary" @click="openPluginMarket">插件市场</el-button>
      </div>
    </el-popover>
    <span class="hc-header-divider" aria-hidden="true"></span>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useWindowSize } from '@vueuse/core'
import { useRoute, useRouter } from 'vue-router'
import { Grid, ArrowDown, Search, Star, StarFilled } from '@element-plus/icons-vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { getSiteInstanceId } from '@/utils/site'
import { getPluginList } from '@/api/plugin-center/pluginCenter'
import { normalizeMenuTree, navigateFromMenuNode } from '@/utils/firstLevelMenuNav.js'
import { ALL_CATE_ID, pathWithinMenu, findMenuById } from '@/utils/menuDisplay.js'
import { buildApplicationItems, groupApplicationItems, filterApplicationItems, FAVORITES_CATE_ID } from '@/utils/pluginApplicationNav'
import { registerPluginApplicationRefresher } from '@/utils/pluginApplicationRefresh'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()
const emit = defineEmits(['navigate'])
const { width: viewportWidth } = useWindowSize()
const panelWidth = computed(() => Math.min(560, Math.max(280, viewportWidth.value - 24)))
const panelVisible = ref(false)
const search = ref('')
const activeGroupId = ref(ALL_CATE_ID)
const catalog = ref([])
const catalogLoaded = ref(false)
const loading = ref(false)
const loadError = ref(false)
const favorites = ref([])
let requestSequence = 0
// 右上角只展示当前账号权限树里能直接进入的应用；介绍/详情只走插件中心。
const permissionTree = computed(() => normalizeMenuTree(authStore.token ? (appStore.baseMenus || []) : []))
const items = computed(() => buildApplicationItems(permissionTree.value, catalog.value))
const pluginGroups = computed(() => groupApplicationItems(items.value, appStore.pluginCategories, favorites.value, { includeEmpty: false }))
const activeItems = computed(() => search.value.trim()
  ? filterApplicationItems(items.value, search.value)
  : (pluginGroups.value.find(group => group.id === activeGroupId.value)?.items || items.value))
const pluginCenterMenu = computed(() => findMenuById(permissionTree.value, 4))
const favoriteKey = computed(() => {
  const user = authStore.userInfo
  const id = user?.userId ?? user?.user_id ?? user?.id
  return id == null ? '' : `unimall:application-favorites:${user?.tenantId || ''}:${id}`
})
watch(favoriteKey, key => {
  try {
    const saved = key ? JSON.parse(localStorage.getItem(key) || '[]') : []
    favorites.value = Array.isArray(saved) ? saved.filter(code => typeof code === 'string') : []
  } catch { favorites.value = [] }
}, { immediate: true })
const toggleFavorite = code => {
  favorites.value = favorites.value.includes(code) ? favorites.value.filter(value => value !== code) : [...favorites.value, code]
  if (favoriteKey.value) {
    try { localStorage.setItem(favoriteKey.value, JSON.stringify(favorites.value)) } catch { /* 浏览器禁用存储时保留当前会话选择。 */ }
  }
}
watch(pluginGroups, groups => {
  if (!groups.some(group => group.id === activeGroupId.value)) activeGroupId.value = ALL_CATE_ID
})
const getIconComponent = name => ElementPlusIconsVue[name] || ElementPlusIconsVue.Document
const isItemActive = item => pathWithinMenu(item.menu, route.path || '')
const isAnyActive = computed(() => items.value.some(isItemActive))
const navigate = menu => {
  panelVisible.value = false
  if (menu.id != null) appStore.setActiveNavMenu(menu.id)
  navigateFromMenuNode(menu, { router, onNavigate: () => emit('navigate') })
}
const handleSelect = item => {
  if (item.unavailable) {
    ElMessage.warning(item.unavailableReason === 'disabled' ? '该应用已停用，请先在插件中心启用' : '该应用暂不可用，请联系管理员查看运行状态')
    return
  }
  if (!item.menu) {
    ElMessage.warning('暂无该应用使用权限，请联系管理员分配菜单')
    return
  }
  navigate(item.menu)
}
const openPluginCenter = () => { if (pluginCenterMenu.value) navigate(pluginCenterMenu.value) }
const openPluginMarket = () => {
  panelVisible.value = false
  router.push('/platform/plugins/market')
}
const loadCatalog = async () => {
  const sequence = ++requestSequence
  loading.value = true
  try {
    const data = await getPluginList({ status: 'installed', instanceId: getSiteInstanceId() })
    const list = Array.isArray(data) ? data : data?.list
    if (!Array.isArray(list)) throw new Error('应用目录响应格式异常')
    if (sequence !== requestSequence) return
    catalog.value = list
    catalogLoaded.value = true
    loadError.value = false
  } catch {
    if (sequence === requestSequence) loadError.value = true
  } finally {
    if (sequence === requestSequence) loading.value = false
  }
}
const openPanel = () => {
  search.value = ''
  activeGroupId.value = pluginGroups.value.some(group => group.id === FAVORITES_CATE_ID) ? FAVORITES_CATE_ID : ALL_CATE_ID
  void refreshApplications()
}
const refreshApplications = () => Promise.allSettled([loadCatalog(), appStore.loadPluginCategories()])
watch(() => authStore.token, () => {
  ++requestSequence
  catalog.value = []
  catalogLoaded.value = false
  loadError.value = false
  loading.value = false
  panelVisible.value = false
})
let stopApplicationRefresh = () => {}
onMounted(async () => {
  stopApplicationRefresh = registerPluginApplicationRefresher(() => refreshApplications())
  if (!appStore.menuLoaded) await appStore.fetchMenus(false)
  await refreshApplications()
})
onBeforeUnmount(() => stopApplicationRefresh())
</script>

<style scoped>
.header-plugin-menu {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.hc-header-chip--group {
  height: 34px;
  padding: 0 13px;
  gap: 7px;
  border-radius: 999px;
  font-weight: 500;
}

.header-plugin-menu__icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: rgba(64, 158, 255, 0.12);
  color: #409eff;
  transition: background 0.18s ease, color 0.18s ease;
}

.hc-header-chip--group:hover .header-plugin-menu__icon-wrap,
.hc-header-chip--group.is-active .header-plugin-menu__icon-wrap {
  background: #409eff;
  color: #fff;
}

/* ---------- 面板 ---------- */
.plugin-menu-panel {
  display: flex;
  min-height: 220px;
  max-height: 460px;
}

.plugin-menu-panel__cates {
  flex: 0 0 132px;
  margin: 0;
  padding: 4px;
  list-style: none;
  border-right: 1px solid #f0f2f5;
  overflow-y: auto;
}

.plugin-menu-panel__cate {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #303133;
  transition: background 0.15s ease, color 0.15s ease;
}

.plugin-menu-panel__cate:hover {
  background: #f5f7fa;
}

.plugin-menu-panel__cate.is-active {
  background: #ecf5ff;
  color: #337ecc;
  font-weight: 600;
}

.plugin-menu-panel__cate-count {
  font-size: 12px;
  color: #a8abb2;
  font-weight: 400;
}

.plugin-menu-panel__items {
  flex: 1;
  min-width: 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(96px, 1fr));
  align-content: start;
  gap: 10px;
  padding: 8px 10px;
  overflow-y: auto;
}

.plugin-menu-panel__item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px 6px 10px;
  border: 1px solid #eef1f6;
  border-radius: 10px;
  background: #fff;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.plugin-menu-panel__item:hover {
  background: #f5faff;
  border-color: #b3d8ff;
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.14);
  transform: translateY(-2px);
}

.plugin-menu-panel__item.is-active {
  background: #f0f7ff;
  border-color: #409eff;
}


.plugin-menu-panel__item-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: #f2f6fb;
  color: #5a6d92;
  font-size: 18px;
}


.plugin-menu-panel__item:hover .plugin-menu-panel__item-icon {
  background: #e1efff;
  color: #409eff;
}

.plugin-menu-panel__item.is-active .plugin-menu-panel__item-icon {
  background: #409eff;
  color: #fff;
}

.plugin-menu-panel__item-name {
  font-size: 12px;
  line-height: 1.35;
  color: #303133;
  text-align: center;
  word-break: break-all;
}

.plugin-menu-panel__item.is-active .plugin-menu-panel__item-name {
  color: #337ecc;
  font-weight: 600;
}

.plugin-menu-panel__item-badge {
  position: absolute;
  top: 6px;
  right: 6px;
  height: 16px;
  padding: 0 5px;
  border-radius: 8px;
  background: #f2f3f5;
  color: #909399;
  font-size: 10px;
  line-height: 16px;
  white-space: nowrap;
}

.plugin-menu-panel__guide {
  grid-column: 1 / -1;
  margin: 8px 4px 12px;
  padding: 18px 16px;
  border-radius: 12px;
  background: linear-gradient(180deg, #edf5ff 0%, #f7fbff 100%);
  border: 1px solid #c6e0ff;
  text-align: center;
}
.plugin-menu-panel__guide-title {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 700;
  color: #1f2a37;
}
.plugin-menu-panel__guide-desc {
  margin: 0 0 14px;
  font-size: 13px;
  line-height: 1.55;
  color: #4b5565;
}
.plugin-menu-panel__guide-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 168px;
  height: 36px;
  padding: 0 16px;
  border: 0;
  border-radius: 8px;
  background: #409eff;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
}
.plugin-menu-panel__guide-btn:hover { background: #337ecc; }
.plugin-menu-panel { margin-top: 12px; }
.plugin-menu-panel__cate { width: 100%; border: 0; background: transparent; text-align: left; font-family: inherit; }
.plugin-menu-panel__card { position: relative; }
.plugin-menu-panel__item { width: 100%; min-height: 104px; padding-top: 22px; }
.plugin-menu-panel__favorite { position: absolute; right: 5px; top: 4px; border: 0; background: transparent; color: #909399; cursor: pointer; padding: 4px; }
.plugin-menu-panel__favorite.is-favorite { color: #c58b16; }
.plugin-menu-panel__footer { display: flex; align-items: center; justify-content: space-between; gap: 8px; border-top: 1px solid #ebeef5; padding-top: 10px; font-size: 12px; color: #909399; }
.plugin-menu-panel__footer button { cursor: pointer; }
@media (max-width: 600px) {
  .plugin-menu-panel__cates { flex-basis: 108px; padding: 2px; }
  .plugin-menu-panel__cate { padding: 8px 4px; font-size: 12px; }
  .plugin-menu-panel__items { grid-template-columns: repeat(2, minmax(70px, 1fr)); gap: 6px; padding: 6px; }
  .plugin-menu-panel__footer { flex-wrap: wrap; }
  .hc-header-chip--group { padding: 0 8px; }
}
</style>
