<template>
  <div class="tertiary-menu-wrapper" :class="{ 'is-mobile': isMobile }">
    <el-aside
      v-if="hasSecondaryMenus"
      :width="secondaryWidth"
      class="secondary-sidebar"
      :class="{ 'secondary-sidebar--mobile': isMobile }"
    >
      <SecondaryMenu
        :menu-items="secondaryMenus"
        :menu-title="activeMainMenuTitle"
        :active-menu="activeSecondaryMenu"
        :is-collapse="isSecondaryCollapse"
        :is-mobile="isMobile"
        @update:isCollapse="toggleSecondaryCollapse"
        @select="handleSecondaryMenuSelect"
      />
    </el-aside>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SecondaryMenu from '@/components/SecondaryMenu.vue'
import { useAppStore } from '@/stores/app'
import {
  findFirstLevelMenuByAmbiguousRoot,
  filterGeoMenuSubtree,
  isMenuPathPrefix,
  isOaWorkbenchPath,
  OA_WORKBENCH_MENU_ID,
  resolveMenuItemPath,
  resolveSecondaryMenuGroup,
  sanitizeOaSecondaryMenus
} from '@/utils/menuNavigate'
import { isMenuGroup, resolveGroupedModuleMenu, resolveFirstLevelMenuByDescendant } from '@/utils/firstLevelMenuNav'
import { findMenuById, pathWithinMenu } from '@/utils/menuDisplay'
// 定义组件属性
const props = defineProps({
  isSecondaryCollapse: {
    type: Boolean,
    default: false
  },
  isMobile: {
    type: Boolean,
    default: false
  }
})

// 定义事件
const emit = defineEmits(['update:isSecondaryCollapse', 'select'])

const appStore = useAppStore()
const route = useRoute()
const router = useRouter()

// 二级菜单相关
const secondaryMenus = ref([])
const activeMainMenuTitle = ref('')
const hasSecondaryMenus = ref(false)
const activeSecondaryMenu = ref('')

/** 当前路由是否属于 GEO 模块 */
const isGeoRoute = computed(() => route.path === '/geo' || route.path.startsWith('/geo/'))

const secondaryWidth = computed(() => {
  if (props.isMobile) return '100%'
  if (props.isSecondaryCollapse) return '64px'
  return isGeoRoute.value ? '180px' : '160px'
})

// 切换二级菜单折叠
const toggleSecondaryCollapse = (collapse) => {
  emit('update:isSecondaryCollapse', collapse)
}

// 处理二级菜单选择（仅通知父级 Layout 跳转，避免重复 push）
const handleSecondaryMenuSelect = (index) => {
  activeSecondaryMenu.value = index
  emit('select', index)
}
const updateSecondaryMenus = (currentPath) => {
  // 如果传入的路径为空，使用当前路由路径
  if (!currentPath) {
    currentPath = route.path
  }

  // 获取当前一级菜单
  const firstLevelMenus = appStore.dynamicMenus.filter(menu => menu.pid === 0 && menu.type === 'M' && menu.is_show === 1)

  const normalizedCurrentPath = currentPath && currentPath !== '/' && currentPath.endsWith('/')
    ? currentPath.slice(0, -1)
    : currentPath

  // 0) 点选优先：左侧竖栏或右上角「插件应用」点选过的一级项直接作为当前主菜单，
  //    二级菜单跟着点选走，避免同一路由被多个一级项前缀命中时跳错栏。
  let currentMainMenu = null
  const pinnedId = appStore.activeNavMenuId
  if (pinnedId) {
    const pinned = findMenuById(appStore.dynamicMenus, pinnedId)
    if (pinned) {
      const pinnedPath = pinned.paths || pinned.path || ''
      const hitSelf = pinnedPath && isMenuPathPrefix(normalizedCurrentPath, pinnedPath)
      if (hitSelf || pathWithinMenu(pinned, normalizedCurrentPath)) {
        currentMainMenu = pinned
      }
    }
  }

  const normalizeMenuPath = (path) => {
    if (!path) return ''
    return path.startsWith('/') ? path : '/' + path
  }

  const pathMatches = (current, target) => {
    if (!target) return false
    if (current.startsWith(target + '?')) return true
    return isMenuPathPrefix(current, target)
  }

  // 查找当前路径对应的一级菜单
  // 1) 优先用一级菜单自身 paths 前缀匹配（避免商城子菜单 /setting/* 抢走 Cloud 系统设置）
  // 2) 若无直接匹配，再按子级路径回退匹配
  let directBest = null
  let directBestLen = 0
  if (!currentMainMenu) {
    for (const menu of firstLevelMenus) {
      const normalizedMenuPath = normalizeMenuPath(menu.paths)
      if (!normalizedMenuPath) continue
      if (normalizedCurrentPath === normalizedMenuPath || normalizedCurrentPath === normalizedMenuPath + '/') {
        currentMainMenu = menu
        directBest = null
        break
      }
      if (pathMatches(normalizedCurrentPath, normalizedMenuPath)) {
        const perms = menu.perms || ''
        const preferShell = perms.startsWith('shell:') ? 1 : 0
        const score = normalizedMenuPath.length * 10 + preferShell
        if (score > directBestLen) {
          directBest = menu
          directBestLen = score
        }
      }
    }
  }

  if (!currentMainMenu && directBest) {
    currentMainMenu = directBest
  }

  if (!currentMainMenu) {
    currentMainMenu = resolveFirstLevelMenuByDescendant(firstLevelMenus, normalizedCurrentPath)
  }

  // /oa/workbench 等：一级 paths=/oa 为宽根，前缀匹配被排除且常无「工作台」子菜单权限时，仍应展示 OA 二三级侧栏
  if (!currentMainMenu) {
    const byRoot = findFirstLevelMenuByAmbiguousRoot(firstLevelMenus, normalizedCurrentPath)
    if (byRoot) {
      currentMainMenu = byRoot
    }
  }

  // 命中叶子页时保留其所属模块根，避免兄弟菜单随路由切换消失。
  if (currentMainMenu && isMenuGroup(currentMainMenu)) {
    currentMainMenu = resolveGroupedModuleMenu(currentMainMenu, normalizedCurrentPath) || currentMainMenu
  }

  const moduleGroup = resolveSecondaryMenuGroup(appStore.dynamicMenus, normalizedCurrentPath)

  const applyOaMenuList = (title, items) => {
    activeMainMenuTitle.value = title
    secondaryMenus.value = sanitizeOaSecondaryMenus(items)
    hasSecondaryMenus.value = secondaryMenus.value.length > 0
  }

  if (moduleGroup?.items?.length) {
    applyOaMenuList(moduleGroup.title, moduleGroup.items)
  } else if (currentMainMenu && currentMainMenu.children && currentMainMenu.children.length > 0) {
    const visibleChildren = (currentMainMenu.children || []).filter((m) => m.is_show !== 0 && m.type !== 'A')
    // GEO 模块（含移动端回退）：同样剔除遗留的 /im /ai /orderfood 等旧菜单
    const isGeoMainMenu = normalizeMenuPath(currentMainMenu.paths) === '/geo' || currentMainMenu.name === 'GEO'
    applyOaMenuList(
      currentMainMenu.name,
      isGeoMainMenu ? filterGeoMenuSubtree(visibleChildren) : visibleChildren
    )
  } else {
    // 默认情况：只有在路径是 /dashboard 时才显示默认
    if (currentPath === '/dashboard' || currentPath === '/') {
      activeMainMenuTitle.value = '仪表盘'
      secondaryMenus.value = []
      hasSecondaryMenus.value = false
    } else if (currentMainMenu) {
      // 如果匹配到菜单但没有子菜单，也设置菜单标题但不显示二级菜单
      activeMainMenuTitle.value = currentMainMenu.name
      secondaryMenus.value = []
      hasSecondaryMenus.value = false
    } else {
      // 未匹配到任何菜单
      activeMainMenuTitle.value = '仪表盘'
      secondaryMenus.value = []
      hasSecondaryMenus.value = false
    }
  }

  // 设置当前激活的二级菜单（OA 工作台多路径统一到菜单项 paths）
  if (isOaWorkbenchPath(normalizedCurrentPath)) {
    const workbenchItem = secondaryMenus.value.find(
      (m) =>
        m?.name === '工作台' ||
        Number(m?.id) === Number(OA_WORKBENCH_MENU_ID) ||
        normalizeMenuPath(m?.paths || m?.path) === '/plugin/oa/apps'
    )
    activeSecondaryMenu.value = workbenchItem
      ? resolveMenuItemPath(workbenchItem)
      : '/plugin/oa/apps'
  } else {
    activeSecondaryMenu.value = normalizedCurrentPath
  }
}

// 监听路由变化，更新二级菜单
watch(
  () => route.path,
  (newPath) => {
    const pathToUse = newPath || route.path
    setTimeout(() => {
      updateSecondaryMenus(pathToUse)
    }, 0)
  },
  { immediate: true, flush: 'post' }
)

// 监听动态菜单变化
watch(
  () => appStore.dynamicMenus,
  () => {
    updateSecondaryMenus(route.path)
  },
  { deep: true, flush: 'post' }
)
</script>

<style scoped>
.tertiary-menu-wrapper {
  flex-shrink: 0;
}

.secondary-sidebar {
  background-color: #ffffff;
  border-right: 1px solid #e4e7ed;
  transition: width 0.28s ease;
  height: calc(100vh - var(--hc-header-height));
  box-shadow: 1px 0 4px rgba(16, 24, 40, 0.04);
}

.tertiary-menu-wrapper.is-mobile .secondary-sidebar {
  width: 100% !important;
  height: auto !important;
  border-right: none;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: none;
}
</style>
