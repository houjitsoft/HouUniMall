<template>
  <div class="sidebar-menu">
    <el-menu
      :default-active="activeMenu"
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
      <!-- 一级菜单 -->
      <template v-for="menu in firstLevelMenus" :key="menu.id">
        <!-- 如果有子菜单，显示为可展开的菜单项 -->
        <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.paths">
          <template #title>
            <el-icon v-if="menu.icon">
              <component :is="getIconComponent(menu.icon)" />
            </el-icon>
            <span>{{ menu.name }}</span>
          </template>
          <!-- 二级菜单 -->
          <template v-for="secondMenu in menu.children" :key="secondMenu.id">
            <!-- 如果二级菜单有子菜单，显示为可展开的菜单项 -->
            <el-sub-menu v-if="secondMenu.children && secondMenu.children.length > 0" :index="secondMenu.paths">
              <template #title>
                <el-icon v-if="secondMenu.icon">
                  <component :is="getIconComponent(secondMenu.icon)" />
                </el-icon>
                <span @click.stop="handleSubMenuClick(secondMenu)">{{ secondMenu.name }}</span>
              </template>
              <!-- 三级菜单 -->
              <el-menu-item 
                v-for="thirdMenu in secondMenu.children" 
                :key="thirdMenu.id"
                :index="thirdMenu.paths"
              >
                <el-icon v-if="thirdMenu.icon">
                  <component :is="getIconComponent(thirdMenu.icon)" />
                </el-icon>
                <template #title>
                  <span>{{ thirdMenu.name }}</span>
                </template>
              </el-menu-item>
            </el-sub-menu>
            <!-- 如果二级菜单没有子菜单，显示为普通菜单项 -->
            <el-menu-item v-else :index="secondMenu.paths">
              <el-icon v-if="secondMenu.icon">
                <component :is="getIconComponent(secondMenu.icon)" />
              </el-icon>
              <template #title>
                <span>{{ secondMenu.name }}</span>
              </template>
            </el-menu-item>
          </template>
        </el-sub-menu>
        <!-- 如果没有子菜单，显示为普通菜单项 -->
        <el-menu-item v-else :index="menu.paths">
          <el-icon v-if="menu.icon">
            <component :is="getIconComponent(menu.icon)" />
          </el-icon>
          <template #title>
            <span>{{ menu.name }}</span>
          </template>
        </el-menu-item>
      </template>
    </el-menu>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { ElMessage } from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

// 菜单列表
const menuList = ref([])
const loading = ref(false)

// 获取图标组件
const getIconComponent = (iconName) => {
  // 如果图标名称在 Element Plus 图标中存在，则返回对应图标组件
  if (ElementPlusIconsVue[iconName]) {
    return ElementPlusIconsVue[iconName]
  }
  // 默认返回 Document 图标
  return ElementPlusIconsVue.Document
}

const isCollapse = computed(() => appStore.sidebar.opened)

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta && meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

// 获取一级菜单
const firstLevelMenus = computed(() => {
  // 过滤出一级菜单（pid为0且type为M的菜单）
  return (menuList.value || []).filter(menu => 
    menu.pid === 0 && 
    menu.type === 'M' && 
    menu.is_show === 1
  )
})

const handleMenuSelect = (index) => {
  router.push(index)
}

// 处理二级菜单点击（跳转到第一个子菜单）
const handleSubMenuClick = (menu) => {
  if (menu.children && menu.children.length > 0) {
    const firstChild = menu.children[0]
    const targetPath = firstChild.paths || firstChild.path
    if (targetPath) {
      router.push(targetPath)
    }
  }
}

// 规范化路径（仅补齐开头斜杠，不做业务映射）
const normalizePath = (path) => {
  let p = path || ''
  if (p && !p.startsWith('/')) p = '/' + p
  return p
}

const normalizeMenuTree = (menus = []) => {
  return (menus || []).map(m => {
    const item = { ...m }
    item.paths = normalizePath(item.paths)
    if (Array.isArray(item.children) && item.children.length > 0) {
      item.children = normalizeMenuTree(item.children)
    }
    return item
  })
}

// 获取用户菜单（通过appStore统一管理，避免重复请求）
const fetchUserMenus = async (forceRefresh = false) => {
  // 如果appStore已有缓存，直接使用
  if (!forceRefresh && appStore.menuLoaded && appStore.dynamicMenus && appStore.dynamicMenus.length > 0) {
    menuList.value = appStore.dynamicMenus
    return
  }
  loading.value = true
  try {
    const menuData = await appStore.fetchMenus(forceRefresh)
    if (menuData && menuData.length > 0) {
      menuList.value = normalizeMenuTree(menuData)
      appStore.setDynamicMenus(menuList.value)
    }
  } catch (error) {
    console.error('获取用户菜单失败:', error)
  } finally {
    loading.value = false
  }
}

// store 菜单晚到时同步刷新侧栏
watch(
  () => appStore.dynamicMenus,
  (menus) => {
    if (Array.isArray(menus) && menus.length > 0) {
      menuList.value = normalizeMenuTree(menus)
    }
  },
  { deep: true }
)

// 组件挂载时获取菜单数据（优先使用缓存）
onMounted(() => {
  fetchUserMenus(false)
})
</script>

<style lang="scss" scoped>
.sidebar-menu {
  height: 100%;
  overflow-y: auto;
  
  .sidebar-logo {
    height: 60px;
    line-height: 60px;
    text-align: center;
    background-color: #304156;
    color: #fff;
    overflow: hidden;
    transition: all 0.3s;
  }

  .sidebar-logo h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    white-space: nowrap;
  }
  
  :deep(.el-menu) {
    border: none;
    height: calc(100% - 60px);
    width: 100% !important;
  }
  
  :deep(.el-sub-menu .el-menu) {
    background-color: #1f2d3d !important;
  }
  
  :deep(.el-sub-menu .el-menu .el-menu-item) {
    background-color: #1f2d3d !important;
    min-width: 200px;
  }
  
  :deep(.el-sub-menu .el-menu .el-menu-item:hover) {
    background-color: #001528 !important;
  }
}
</style>
