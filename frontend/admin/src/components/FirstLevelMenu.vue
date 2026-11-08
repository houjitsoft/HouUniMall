<template>
  <div class="first-level-menu">
    <el-menu
      :default-active="activeIndex"
      :collapse="isCollapse"
      :unique-opened="false"
      :collapse-transition="false"
      mode="vertical"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      @select="handleMenuSelect"
      v-loading="loading"
    >
      <el-menu-item
        v-for="menu in visibleMenus"
        :key="menu.id"
        :index="menuKey(menu)"
      >
        <el-icon v-if="menu.icon">
          <component :is="getIconComponent(menu.icon)" />
        </el-icon>
        <template #title>
          <span>{{ menu.name }}</span>
        </template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
/**
 * 左侧竖栏一级菜单：只渲染 display_pos=left 的一级项。
 *
 * 与右上角「插件应用」入口的分工见 utils/menuDisplay.js：
 * 一级项要么在左侧、要么在右上角，按 display_pos 二选一，不重复出现。
 * 窄屏使用同一应用面板，核心侧栏只保留 left 项。
 */
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import {
  navigateFromMenuIndex,
  navigateFromMenuNode,
  normalizeMenuTree
} from '@/utils/firstLevelMenuNav.js'
import {
  pickLeftNavItems,
  collectNavItems,
  findMenuById,
  pathWithinMenu
} from '@/utils/menuDisplay.js'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const emit = defineEmits(['navigate'])

const menuList = ref([])
const loading = ref(false)

const getIconComponent = (iconName) =>
  ElementPlusIconsVue[iconName] || ElementPlusIconsVue.Document

const isCollapse = computed(() => appStore.sidebar.opened)

/** el-menu 的 index：优先用菜单路径，缺省时用 id 兜底 */
const menuKey = (menu) => menu?.paths || menu?.path || `/menu-${menu?.id}`

/** 全部可见一级项（分组根下钻，见 menuDisplay.collectNavItems） */
const navItems = computed(() => collectNavItems(menuList.value))

/** 本组件实际渲染的一级项 */
const visibleMenus = computed(() => pickLeftNavItems(menuList.value))

/**
 * 高亮项：当前路由落在哪个一级项的子树里就高亮哪个（最长路径优先）。
 * 一级项自身的 paths 往往不是最终路由（如 /mall → /mall/dashboard/overview），
 * 只比 route.path 相等会一直高亮不上。
 */
const activeIndex = computed(() => {
  const current = route.path || ''
  let best = ''
  for (const menu of visibleMenus.value) {
    if (!pathWithinMenu(menu, current)) continue
    const key = menuKey(menu)
    if (key.length > best.length) best = key
  }
  return best
})

/** 点击前记下点选的一级项，二级菜单据此联动 */
const markActive = (menu) => {
  if (menu?.id != null) {
    appStore.setActiveNavMenu(menu.id)
  }
}

const handleMenuSelect = (index) => {
  const hit = visibleMenus.value.find((m) => menuKey(m) === index)
  if (hit) {
    markActive(hit)
    navigateFromMenuNode(hit, { router, onNavigate: () => emit('navigate') })
    return
  }
  navigateFromMenuIndex(index, {
    menus: menuList.value,
    firstLevelMenus: navItems.value,
    router,
    onNavigate: () => emit('navigate')
  })
}

const applyMenusFromStore = (menus) => {
  menuList.value = normalizeMenuTree(Array.isArray(menus) ? menus : [])
}

// store 菜单晚于组件挂载时到达（如 Redis/认证抖动后重试成功），需同步刷新一级菜单
watch(
  () => appStore.dynamicMenus,
  (menus) => {
    applyMenusFromStore(menus)
  },
  { deep: true }
)

// 路由直接进入（刷新、地址栏输入）时，补齐点选态，避免二级菜单靠猜
watch(
  () => route.path,
  (path) => {
    if (appStore.activeNavMenuId) {
      const pinned = findMenuById(menuList.value, appStore.activeNavMenuId)
      if (pinned && pathWithinMenu(pinned, path)) return
    }
    let bestId = null
    let bestLen = 0
    for (const menu of navItems.value) {
      if (!pathWithinMenu(menu, path)) continue
      const len = String(menu?.paths || menu?.path || '').length
      if (len > bestLen) {
        bestLen = len
        bestId = menu.id
      }
    }
    if (bestId != null) appStore.setActiveNavMenu(bestId)
  },
  { immediate: true }
)

onMounted(async () => {
  if (appStore.menuLoaded && appStore.dynamicMenus && appStore.dynamicMenus.length > 0) {
    applyMenusFromStore(appStore.dynamicMenus)
    return
  }
  loading.value = true
  try {
    const menuData = await appStore.fetchMenus(false)
    if (menuData && menuData.length > 0) {
      applyMenusFromStore(menuData)
      appStore.setDynamicMenus(menuList.value)
    }
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.first-level-menu {
  flex: 1; /* 填满剩余空间 */
  overflow-y: auto;

  :deep(.el-menu) {
    border: none;
    height: 100%;
    width: 100% !important;
  }
}
</style>
