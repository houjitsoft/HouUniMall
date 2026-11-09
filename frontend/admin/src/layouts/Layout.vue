<template>
  <el-container class="layout-container" :class="{ 'is-mobile': isMobile }">
    <!-- 移动端遮罩 -->
    <Transition name="fade">
      <div
        v-if="isMobile && mobileSidebarOpen"
        class="sidebar-overlay"
        aria-hidden="true"
        @click="closeMobileSidebar"
      />
    </Transition>

    <!-- 侧边栏 -->
    <el-aside
      :width="sidebarWidth"
      class="sidebar"
      :class="{ 'sidebar-mobile-open': isMobile && mobileSidebarOpen }"
    >
      <div class="sidebar-header">
        <router-link
          v-if="!isCollapse || isMobile"
          to="/dashboard"
          class="logo"
          aria-label="返回首页"
          title="返回首页"
        >
          <span class="logo-mark">H</span>
          <span v-if="!isCollapse || isMobile" class="logo-text">UniMall</span>
        </router-link>
        <router-link
          v-else
          to="/dashboard"
          class="logo-mini"
          aria-label="返回首页"
          title="返回首页"
        >H</router-link>
      </div>

      <!-- 左侧竖栏一级菜单（display_pos=left）；右上角插件位的一级项在窄屏时一并收纳进抽屉 -->
      <FirstLevelMenu
        @navigate="onPrimaryMenuNavigate"
      />

      <!-- 侧栏底部：用户为主锚点，清缓存 / 折叠为次级操作（小匠上传在顶栏右上角） -->
      <div class="sidebar-footer" :class="{ 'is-collapse': isCollapse && !isMobile }">
        <div class="sidebar-footer__user">
          <UserInfo placement="top" :is-collapse="isCollapse && !isMobile" @command="handleCommand" />
          <!-- 每日待办：从顶栏移至会员中心同一行（侧栏常驻，不随顶栏开关隐藏） -->
          <HeaderDailyTodo />
        </div>

        <div class="sidebar-footer__actions">
          <el-tooltip content="清除缓存" placement="right" :disabled="!isCollapse || isMobile">
            <button
              type="button"
              class="sidebar-footer__action"
              :disabled="clearMenuCacheLoading"
              aria-label="清除缓存"
              @click="clearCacheDialogVisible = true"
            >
              <el-icon><Refresh /></el-icon>
              <span v-if="!isCollapse || isMobile" class="sidebar-footer__action-text">清缓存</span>
            </button>
          </el-tooltip>

          <el-tooltip
            v-if="!isMobile"
            :content="isCollapse ? '展开侧栏' : '收起侧栏'"
            placement="right"
            :disabled="!isCollapse"
          >
            <button
              type="button"
              class="sidebar-footer__action"
              :aria-label="isCollapse ? '展开侧栏' : '收起侧栏'"
              @click="toggleCollapse"
            >
              <el-icon v-if="isCollapse"><Expand /></el-icon>
              <el-icon v-else><Fold /></el-icon>
              <span v-if="!isCollapse" class="sidebar-footer__action-text">收起</span>
            </button>
          </el-tooltip>
        </div>
      </div>
    </el-aside>

    <!-- 主容器 -->
    <el-container class="main-wrapper">
      <!-- 头部（可由主题设置关闭） -->
      <el-header
        v-show="themeSettings.showTopNav"
        class="header"
        :height="`${headerHeight}px`"
      >
        <div class="header-left">
          <HeaderLeft
            :is-mobile="isMobile"
            @toggle-mobile-menu="toggleMobileSidebar"
          />

          <!--
            组织切换：仅「需要组织作用域」的模块显示（项目管理/GEO/系统设置）。
            商城、订单、商品等模块不依赖组织，默认隐藏，避免顶栏出现无意义入口。
          -->
          <div v-if="orgSwitcherVisible && hasMultipleOrgs" class="org-selector">
            <el-dropdown trigger="click" @command="handleOrgChange">
              <button type="button" class="hc-header-chip hc-header-chip--org" aria-label="切换组织">
                <span class="hc-header-chip__text">{{ currentOrgName }}</span>
                <el-icon class="hc-header-chip__chevron"><ArrowDown /></el-icon>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="org in orgList"
                    :key="org.org_id"
                    :command="org.org_id"
                    :disabled="isCurrentOrg(org.org_id)"
                  >
                    {{ org.org_name }}
                    <el-icon v-if="org.is_primary === 1"><Star /></el-icon>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div v-else-if="orgSwitcherVisible && currentOrgName" class="org-info">
            <span class="hc-header-chip hc-header-chip--org hc-header-chip--readonly">
              <span class="hc-header-chip__text">{{ currentOrgName }}</span>
            </span>
          </div>
        </div>

        <div class="header-right">
          <div class="header-toolbar">
            <!-- 应用入口在桌面与窄屏均由同一个分类面板提供，核心侧栏不混入应用。 -->
            <!-- 每日待办已移至左下角「会员中心」同一行（见 sidebar-footer__user）。 -->
            <HeaderPluginMenu @navigate="onPrimaryMenuNavigate" />

            <!-- 多语言：保留在顶栏右侧 -->
            <div v-if="internationalizationEnabled" class="language-selector-wrapper">
              <LanguageSelector v-model="currentLanguage" />
            </div>

            <!-- 运行告警：线上报错的未处理数量，点击进告警中心 -->
            <ErrorAlertBell />

            <!-- 小匠帮你传：顶栏右上角仅图标入口（hover 提示 + aria-label 在组件内） -->
            <el-tooltip content="小匠帮你传" placement="bottom">
              <XiaojiangUpload />
            </el-tooltip>

          </div>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-container class="main-container">
        <!-- 智能车间菜单 -->
        <el-aside
          v-if="isWorkshopRoute"
          :width="secondarySidebarWidth"
          class="secondary-sidebar"
        >
          <WorkshopSidebarMenu />
        </el-aside>

        <!-- 二级和三级菜单组件：插件模块自带页面内导航，不挂二级菜单 -->
        <TertiaryMenu
          v-if="!isWorkshopRoute && !isPluginsRoute"
          v-model:is-secondary-collapse="isSecondaryCollapse"
          :is-mobile="isMobile"
          @select="handleSecondaryMenuSelect"
        />

        <!-- 页面内容 -->
        <el-main class="content">
          <div class="content-body">
            <transition :name="themeSettings.pageAnimation ? 'page-fade' : ''">
              <component
                v-if="pluginPageLayout"
                :is="pluginPageLayout"
                :key="route.meta.pluginCode"
              />
              <router-view v-else :key="route.fullPath" />
            </transition>
          </div>
        </el-main>
      </el-container>

      <!-- 底部 -->
      <el-footer v-show="!isMobile" class="footer">
        <Footer />
      </el-footer>

      <!-- 清缓存弹窗 -->
      <el-dialog
        v-model="clearCacheDialogVisible"
        :width="isMobile ? '92%' : '660px'"
        class="clear-cache-dialog"
        append-to-body
        align-center
      >
        <template #header>
          <div class="clear-cache-header">
            <span class="clear-cache-header__icon">
              <el-icon><Delete /></el-icon>
            </span>
            <div class="clear-cache-header__text">
              <h3 class="clear-cache-header__title">清除缓存</h3>
              <p class="clear-cache-header__sub">按类型清理 Redis 缓存，选择后立即生效</p>
            </div>
          </div>
        </template>

        <div class="cache-options">
          <button
            v-for="opt in cacheOptions"
            :key="opt.type"
            type="button"
            class="cache-option"
            :class="`cache-option--${opt.type}`"
            :disabled="clearCacheLoading"
            @click="handleClearCache(opt.type)"
          >
            <span v-if="opt.badge" class="cache-option__badge">{{ opt.badge }}</span>
            <span class="cache-option__icon">
              <el-icon v-if="clearCacheLoading && clearingCacheType === opt.type" class="is-loading"><Loading /></el-icon>
              <el-icon v-else><component :is="opt.icon" /></el-icon>
            </span>
            <span class="cache-option__title">{{ opt.label }}</span>
            <span class="cache-option__desc">{{ opt.desc }}</span>
          </button>
        </div>

        <p class="clear-cache-note">
          <el-icon><InfoFilled /></el-icon>
          <span>菜单与数据缓存可随时安全清除；「全部缓存」会连登录态一起清空，所有用户需要重新登录。</span>
        </p>
      </el-dialog>

      <!-- 协议版本更新后的补签弹窗（含必选协议时不可关闭） -->
      <AgreementReagreeDialog />
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
// 使用 Element Plus 国际化系统
import { setLocale, getLocale, useI18n } from '@/i18n-element'
const { t } = useI18n()
import { 
  DataBoard, 
  User, 
  Goods, 
  Document, 
  Setting, 
  Expand, 
  Fold,
  List,
  Promotion,
  Box,
  Wallet,
  Bell,
  Lock,
  Operation,
  ArrowDown,
  Star,
  Refresh,
  Menu,
  Delete,
  Loading,
  InfoFilled
} from '@element-plus/icons-vue'
import FirstLevelMenu from '@/components/FirstLevelMenu.vue'
import TertiaryMenu from '@/components/TertiaryMenu.vue'
// Purchased legacy MES sources are optional in a filtered source release.
const workshopComponents = import.meta.glob('../components/mes/WorkshopSidebarMenu.vue', { eager: true, import: 'default' })
const WorkshopSidebarMenu = workshopComponents['../components/mes/WorkshopSidebarMenu.vue']
import LanguageSelector from '@/components/LanguageSelector.vue'
import UserInfo from '@/components/UserInfo.vue'
import Footer from '@/components/Footer.vue'
import HeaderLeft from '@/components/HeaderLeft.vue'
import HeaderPluginMenu from '@/components/HeaderPluginMenu.vue'
import HeaderDailyTodo from '@/components/HeaderDailyTodo.vue'
import XiaojiangUpload from '@/components/XiaojiangUpload.vue'
import ErrorAlertBell from '@/components/ErrorAlertBell.vue'
import AgreementReagreeDialog from '@/components/AgreementReagreeDialog.vue'
import { useAppStore } from '@/stores/app'
import { resolveMenuNavigatePath } from '@/utils/menuNavigate'
import { useAuthStore } from '@/stores/auth'
import { usePluginStore } from '@/stores/plugin'
import { switchOrganizationAndReload } from '@/utils/orgSwitch'
import { isOrgFreeRoute as matchOrgFreeRoute } from '@/utils/orgFreeRoute'
import { websiteSettingApi } from '@/api/setting/website'
import { clearMenuCacheAndRefresh } from '@/utils/userMenusCache'
import {
  clearAssistantPageContext,
  resolveMenuTrail,
  updateAssistantContext
} from '@/utils/weknora'
import {
  ADMIN_THEME_CHANGED_EVENT,
  loadAdminTheme,
  normalizeAdminTheme
} from '@/utils/adminTheme'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const MOBILE_BREAKPOINT = 992

// 使用 Element Plus 国际化系统
const appStore = useAppStore()
const authStore = useAuthStore()
const pluginStore = usePluginStore()

// 状态管理
const isCollapse = computed(() => appStore.sidebar.opened)
const isSecondaryCollapse = ref(false)
const isMobile = ref(false)
const mobileSidebarOpen = ref(false)
const headerHeight = computed(() => (isMobile.value ? 52 : 56))
const currentLanguage = ref(getLocale())
const router = useRouter()
const route = useRoute()
const internationalizationEnabled = ref(localStorage.getItem('internationalizationEnabled') !== '0')
const multiOrganizationEnabled = ref(localStorage.getItem('multiOrganizationEnabled') !== '0')
const clearMenuCacheLoading = ref(false)
const clearCacheDialogVisible = ref(false)
const clearCacheLoading = ref(false)
// 当前正在清除的缓存类型（用于按钮加载态展示）
const clearingCacheType = ref('')
/** 主题设置（顶栏、动画等布局开关） */
const themeSettings = ref(normalizeAdminTheme(loadAdminTheme()))

const onAdminThemeChanged = (event) => {
  themeSettings.value = normalizeAdminTheme(event?.detail || loadAdminTheme())
}

// 清缓存弹窗选项配置（横向卡片一行展示；badge 用于标出常用项与高风险项）
// desc 长度刻意保持接近，配合 text-wrap: balance 让三张卡片的说明都均分为两行、不断出孤字
const cacheOptions = [
  { type: 'menu', label: '菜单缓存', desc: '清除菜单与权限缓存，侧栏立即刷新', icon: Menu, badge: '常用' },
  { type: 'data', label: '数据缓存', desc: '清除业务数据缓存（薪资、考勤、商品等）', icon: DataBoard },
  { type: 'all', label: '全部缓存', desc: '清除所有 Redis 缓存，含登录态', icon: Delete, badge: '高风险' }
]
const selectedOrgId = ref(localStorage.getItem('currentOrgId') || '')

// 判断是否为智能车间路由
const isWorkshopRoute = computed(() => {
  if (!WorkshopSidebarMenu) return false
  return route.path.startsWith('/mes/workshop')
})

/**
 * 插件模块路由（插件中心 / 插件市场 / 授权管理 / 插件订单）。
 *
 * 这四页共用一个左侧二级菜单（插件中心、插件市场），但插件中心本身已经是完整的
 * 工作台页面、插件市场是货架式全宽列表，二级菜单只是把同一级入口重复了一遍。
 * 因此这里整段不渲染二级菜单，跨页入口改由页面内的快捷入口承担。
 */
const isPluginsRoute = computed(() => route.path.startsWith('/platform/plugins'))

// Product-specific project context and page shells are supplied by the installed frontend package.
const pluginPageLayout = computed(() => pluginStore.manifests[route.meta.pluginCode]?.layout || null)

const sidebarWidth = computed(() => {
  if (isMobile.value) return '240px'
  return isCollapse.value ? '64px' : '170px'
})

const secondarySidebarWidth = computed(() => {
  if (isMobile.value) return '100%'
  return isSecondaryCollapse.value ? '64px' : '160px'
})

const updateDeviceMode = () => {
  const mobile = window.innerWidth < MOBILE_BREAKPOINT
  isMobile.value = mobile
  appStore.toggleDevice(mobile ? 'mobile' : 'desktop')
  if (mobile) {
    mobileSidebarOpen.value = false
    isSecondaryCollapse.value = true
  }
}

const toggleMobileSidebar = () => {
  mobileSidebarOpen.value = !mobileSidebarOpen.value
}

const closeMobileSidebar = () => {
  mobileSidebarOpen.value = false
}

const onPrimaryMenuNavigate = () => {
  if (isMobile.value) {
    closeMobileSidebar()
  }
}

watch(
  () => route.path,
  () => {
    if (isMobile.value) {
      closeMobileSidebar()
    }
  }
)

// 计算属性：组织列表
const orgList = computed(() => {
  const sourceList = authStore.userInfo?.orgList || authStore.userInfo?.organizationList
  if (authStore.userInfo && sourceList) {
    // 后端返回的是List<Map<String, Object>>格式，每个Map包含org_id, org_name, is_primary等字段
    // 已经按is_primary DESC排序，主组织（is_primary=1）会排在前面
    if (Array.isArray(sourceList)) {
      // 确保主组织（is_primary=1）排在前面
      return sourceList.sort((a, b) => {
        const aPrimary = a.is_primary === 1 || a.is_primary === '1' ? 1 : 0
        const bPrimary = b.is_primary === 1 || b.is_primary === '1' ? 1 : 0
        return bPrimary - aPrimary
      })
    }
    // 兼容旧格式：Map<String, String>格式（orgId: orgName）
    else if (typeof sourceList === 'object' && !Array.isArray(sourceList)) {
      const orgEntries = Object.entries(sourceList)
      return orgEntries.map(([orgId, orgName], index) => ({
        org_id: orgId,
        org_name: orgName,
        is_primary: index === 0 ? 1 : 0  // 第一个组织设为主组织
      }))
    }
  }
  return []
})

// 计算属性：是否有多个组织
const hasMultipleOrgs = computed(() => {
  return orgList.value.length > 1
})

// 计算属性：当前组织名称
const currentOrgName = computed(() => {
  if (orgList.value.length > 0) {
    const current = orgList.value.find(org => String(org.org_id ?? org.orgId ?? org.id) === String(selectedOrgId.value))
    if (current) return current.org_name || current.orgName || ''
    return orgList.value[0].org_name || orgList.value[0].orgName || ''
  }
  return ''
})

/** 当前路由是否属于「无组织作用域」的模块（规则见 @/utils/orgFreeRoute） */
const isOrgFreeRoute = () => matchOrgFreeRoute(route.path || '')

/** 组织切换器是否显示：多组织开启 + 有组织信息 + 当前模块需要组织作用域 */
const orgSwitcherVisible = computed(() => {
  if (!multiOrganizationEnabled.value) return false
  if (!currentOrgName.value && !hasMultipleOrgs.value) return false
  return !isOrgFreeRoute()
})

// 判断是否为当前组织
const isCurrentOrg = (orgId) => {
  if (orgList.value.length > 0) {
    return String(selectedOrgId.value) === String(orgId)
  }
  return false
}

// 处理组织切换
const handleOrgChange = (orgId) => {
  const normalized = orgId == null ? '' : String(orgId).trim()
  if (!normalized) return
  selectedOrgId.value = normalized
  switchOrganizationAndReload(normalized)
}

// 图标映射
const iconMap = {
  DataBoard,
  User,
  Goods,
  Document,
  Setting,
  List,
  Promotion,
  Box,
  Wallet,
  Bell,
  Lock,
  Operation,
  ArrowDown,
  Star
}

// 获取图标组件
const getIconComponent = (iconName) => {
  return iconMap[iconName] || Document
}

// 切换侧边栏折叠
const toggleCollapse = () => {
  appStore.toggleSidebar()
}

// 清除菜单本地缓存并重新拉取菜单与按钮权限
const handleClearMenuCache = async () => {
  if (clearMenuCacheLoading.value) return
  clearMenuCacheLoading.value = true
  try {
    await clearMenuCacheAndRefresh({ clearServerCache: false })
    ElMessage.success('菜单缓存已清除，权限已刷新')
  } catch (e) {
    console.error('清除菜单缓存失败:', e)
    ElMessage.error('清除菜单缓存失败')
  } finally {
    clearMenuCacheLoading.value = false
  }
}

/**
 * 通用清缓存处理
 * @param {'menu'|'data'|'all'} type 缓存类型
 */
const handleClearCache = async (type) => {
  if (clearCacheLoading.value) return

  const typeLabels = { menu: '菜单缓存', data: '数据缓存', all: '全部缓存' }
  const label = typeLabels[type] || type

  if (type === 'all') {
    try {
      await ElMessageBox.confirm(
        '清除全部缓存将导致所有用户退出登录，确定继续吗？',
        '警告',
        { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
      )
    } catch {
      return
    }
  }

  clearCacheLoading.value = true
  clearingCacheType.value = type
  try {
    // 服务端对 type=all 有二次确认校验（会清 token 导致全体登出），必须带 confirm，
    // 否则 permission 服务返回 400，BFF 再包成 22008「服务开小差了」，看起来像服务故障。
    const payload = type === 'all' ? { type, confirm: 'CLEAR_ALL_CACHE' } : { type }
    const res = await request.post('/web/permission/cache/clear', payload)
    if (res.code === 1 || res.code === 200) {
      ElMessage.success(`${label}已清除`)
      clearCacheDialogVisible.value = false
      // 菜单缓存需要刷新侧栏
      if (type === 'menu' || type === 'data' || type === 'all') {
        await clearMenuCacheAndRefresh({ clearServerCache: false })
      }
    } else {
      ElMessage.error(res.message || `${label}清除失败`)
    }
  } catch (e) {
    console.error('清除缓存失败:', e)
    ElMessage.error('清除缓存失败')
  } finally {
    clearCacheLoading.value = false
    clearingCacheType.value = ''
  }
}

// 用户下拉菜单处理
// 之前 profile / settings 分支只有 console.log，点击没有任何响应（标品测试问题）。
// 个人中心是独立页面，设置落到系统设置的基础配置页；退出登录由 UserInfo 自身的确认流程处理。
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile').catch(() => {})
      break
    case 'settings':
      router.push('/setting/system/basic').catch(() => {})
      break
    case 'logout':
      break
  }
}

// 处理二级菜单选择
const handleSecondaryMenuSelect = (index) => {
  if (index == null || index === '') return
  const target = resolveMenuNavigatePath(appStore.dynamicMenus, String(index))
  if (!target) return
  router.push(target).catch((err) => {
    if (err.name !== 'NavigationDuplicated') {
      console.error('路由跳转错误:', err)
    }
  })
}

// 右下角知识库挂件（「小匠」）页面上下文：菜单链 / 路由 / 标题 / 用户 / 组织 / 路由参数。
// 挂件把这些字段拼在每次提问前面，知识库才知道用户当下在哪个模块问问题。
// 字段宜短不宜多——它们是拼进 query 文本的，过长会稀释向量检索。
const syncAssistantContext = () => {
  nextTick(() => {
    const heading = (
      document.querySelector('.content .page-header h1') ||
      document.querySelector('.content h1') ||
      document.querySelector('.content h2')
    )?.textContent
    const trail = resolveMenuTrail(appStore.dynamicMenus, route.path)
    const params = Object.entries(route.query || {})
      .filter(([key, value]) => value != null && value !== '' && !/token|password|secret|sign|auth/i.test(key))
      .slice(0, 6)
      .map(([key, value]) => `${key}=${Array.isArray(value) ? value.join(',') : value}`)
      .join('&')

    updateAssistantContext({
      系统: 'UniMall 管理后台',
      页面: trail.join(' / '),
      路由: route.path,
      标题: heading || '',
      用户: authStore.userInfo?.userName || authStore.userInfo?.userMobile || '',
      组织: currentOrgName.value || '',
      参数: params
    })
  })
}

watch(() => route.path, () => {
  // 换页后页面级上下文（如「当前商品」）必须失效，否则会带错数据
  clearAssistantPageContext()
  syncAssistantContext()
})

watch(() => appStore.dynamicMenus, () => syncAssistantContext())

// 初始化语言和菜单状态
onMounted(async () => {
  syncAssistantContext()
  updateDeviceMode()
  window.addEventListener('resize', updateDeviceMode)
  window.addEventListener(ADMIN_THEME_CHANGED_EVENT, onAdminThemeChanged)
  themeSettings.value = normalizeAdminTheme(loadAdminTheme())
  appStore.sidebar.opened = !!themeSettings.value.sidebarCollapse

  const savedLang = localStorage.getItem('language')
  if (savedLang) {
    currentLanguage.value = savedLang
    setLocale(savedLang)
  }

  try {
    const res = await websiteSettingApi.getWebsiteSwitches()
    if (res?.code === 1 && res?.data) {
      internationalizationEnabled.value = res.data.internationalizationEnabled !== false
      multiOrganizationEnabled.value = res.data.multiOrganizationEnabled !== false
      localStorage.setItem('internationalizationEnabled', internationalizationEnabled.value ? '1' : '0')
      localStorage.setItem('multiOrganizationEnabled', multiOrganizationEnabled.value ? '1' : '0')
    }
  } catch (error) {
    console.warn('读取网站基础开关失败，使用本地缓存配置:', error)
  }

  // T10：加载已启用插件的前端产物（动态路由 + 菜单），单个插件失败不影响宿主
  pluginStore.loadInstalledPlugins().catch((error) => {
    console.warn('[Layout] 插件前端加载失败:', error?.message || error)
  })

  // 顶栏右上角「插件应用」的分类来源（来自插件中心分类表），失败降级为「未分类」桶
  appStore.loadPluginCategories()

  // T09：站点级二开前端通道（构建期静态装配，路由命名空间 /ext/{code}/**）。
  // 与上面的租户级动态插件是两条独立的路。动态导入让 registry 独立成 chunk；
  // 未启用任何站点级扩展时 installSiteExtensions 不注册任何路由，零副作用。
  import('@/extensions/registry')
    .then(({ installSiteExtensions }) => installSiteExtensions(router))
    .catch((error) => {
      console.warn('[Layout] 站点级二开登记失败:', error?.message || error)
    })
  
  // 【重要修复】不再在Layout组件中获取用户信息
  // 原因：
  // 1. 用户登录时authStore.usernameLogin/phoneLogin已经获取了userInfo
  // 2. 每次路由切换都会重新mount Layout组件，导致重复调用getUserInfo
  // 3. 如果token在Redis中过期，getUserInfo会返回"用户未登录"错误
  // 4. 响应拦截器会清除localStorage并跳转登录页，导致已登录用户被强制登出
  // 解决方案：
  // - Layout组件只使用authStore中已有的userInfo，不主动获取
  // - 如果userInfo不存在，由路由守卫在进入页面前处理
  // - 避免在已登录状态下因token过期导致的意外登出
})

onUnmounted(() => {
  window.removeEventListener('resize', updateDeviceMode)
  window.removeEventListener(ADMIN_THEME_CHANGED_EVENT, onAdminThemeChanged)
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  min-height: 100vh;
  display: flex;
  overflow: hidden;
}

.sidebar-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(2px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.sidebar {
  background: linear-gradient(180deg, var(--hc-sidebar-bg) 0%, var(--hc-sidebar-bg-end, var(--hc-sidebar-bg)) 100%);
  color: var(--hc-sidebar-text);
  transition: width 0.28s ease, transform 0.28s ease, background 0.25s ease;
  position: relative;
  height: 100vh;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.08);
  z-index: 1001;
}

.layout-container.is-mobile .sidebar {
  position: fixed;
  left: 0;
  top: 0;
  transform: translateX(-100%);
}

.layout-container.is-mobile .sidebar.sidebar-mobile-open {
  transform: translateX(0);
}

.sidebar :deep(.first-level-menu) {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar :deep(.el-menu) {
  background-color: transparent !important;
}

.sidebar :deep(.el-menu-item:hover),
.sidebar :deep(.el-menu-item.is-active) {
  background-color: color-mix(in srgb, var(--hc-primary) 12%, transparent) !important;
}

.sidebar :deep(.el-menu-item),
.sidebar :deep(.el-menu-item .el-icon) {
  color: var(--hc-sidebar-text) !important;
}

.sidebar :deep(.el-menu-item.is-active),
.sidebar :deep(.el-menu-item.is-active .el-icon) {
  color: var(--hc-sidebar-active, var(--hc-primary)) !important;
}

.sidebar-header {
  height: var(--hc-header-height);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--hc-sidebar-header);
  color: white;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
  overflow: hidden;
  color: inherit;
  text-decoration: none;
  cursor: pointer;
}

.logo:focus-visible,
.logo-mini:focus-visible {
  outline: 2px solid var(--hc-primary, #409eff);
  outline-offset: 3px;
  border-radius: 4px;
}

.logo-mark {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, var(--hc-primary, #409eff) 0%, var(--el-color-primary-dark-2, #337ecc) 100%);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
}

.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease;
}

.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}

.logo-text {
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.logo-mini {
  color: inherit;
  text-decoration: none;
  cursor: pointer;
  font-size: 20px;
  font-weight: 700;
}

/* 侧栏底部：用户卡 + 次级操作（清缓存 / 收起） */
.sidebar-footer {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 10px 10px 12px;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.12) 0%, rgba(0, 0, 0, 0.22) 100%);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.sidebar-footer__user {
  display: flex;
  align-items: stretch;
  gap: 6px;
  min-width: 0;
}

/* 会员中心占满剩余宽度，待办保持紧凑图标+文字 */
.sidebar-footer__user > :deep(.el-dropdown) {
  width: auto;
  flex: 1 1 auto;
  min-width: 0;
}

.sidebar-footer__user :deep(.header-daily-todo) {
  flex: 0 0 auto;
  display: flex;
}

.sidebar-footer__user :deep(.header-daily-todo .hc-header-chip) {
  width: auto;
  min-width: 44px;
  padding: 8px 12px;
  justify-content: center;
}

/* 折叠态：会员中心与待办纵向堆叠，待办仅显图标 */
.sidebar-footer.is-collapse .sidebar-footer__user {
  flex-direction: column;
}

.sidebar-footer.is-collapse .sidebar-footer__user :deep(.header-daily-todo .hc-header-chip) {
  width: 100%;
  padding: 8px 0;
}

.sidebar-footer.is-collapse .sidebar-footer__user :deep(.header-daily-todo .header-daily-todo__label) {
  display: none;
}

.sidebar-footer__actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
}

.sidebar-footer__actions:has(.sidebar-footer__action:only-child) {
  grid-template-columns: 1fr;
}

.sidebar-footer__action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-width: 0;
  height: 32px;
  padding: 0 8px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.04);
  color: var(--hc-sidebar-text);
  font-size: 12px;
  font-family: inherit;
  line-height: 1;
  cursor: pointer;
  transition: color 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.sidebar-footer__action:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.16);
}

.sidebar-footer__action:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.sidebar-footer__action-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-footer.is-collapse {
  padding: 8px 6px 10px;
  gap: 8px;
}

.sidebar-footer.is-collapse .sidebar-footer__actions {
  grid-template-columns: 1fr;
}

.sidebar-footer.is-collapse .sidebar-footer__action {
  width: 100%;
  height: 36px;
  padding: 0;
}

/* 深色侧栏内覆盖浅色 chip：做成完整用户卡片 */
.sidebar-footer :deep(.lang-dropdown),
.sidebar-footer :deep(.el-dropdown) {
  width: 100%;
}

.sidebar-footer :deep(.hc-header-chip) {
  width: 100%;
  min-height: 44px;
  max-height: none;
  height: auto;
  padding: 8px 10px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.05);
  box-shadow: none;
  color: rgba(255, 255, 255, 0.92);
  font-size: 13px;
  font-weight: 500;
  justify-content: flex-start;
  gap: 10px;
  transition: color 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.sidebar-footer :deep(.hc-header-chip:hover) {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.16);
}

.sidebar-footer :deep(.hc-header-chip .username) {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: 0.01em;
}

.sidebar-footer :deep(.el-avatar) {
  flex-shrink: 0;
  border: 1px solid rgba(255, 255, 255, 0.18);
}

.sidebar-footer.is-collapse :deep(.hc-header-chip) {
  justify-content: center;
  padding: 8px 0;
  min-height: 44px;
}

.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
  min-width: 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--hc-header-bg);
  border-bottom: 1px solid var(--hc-border);
  padding: 0 20px;
  flex-shrink: 0;
  box-shadow: var(--hc-shadow-sm);
  z-index: 10;
}

.layout-container.is-mobile .header {
  padding: 0 12px;
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  min-width: 0;
}

.header-left {
  flex: 1;
  min-width: 0;
  height: 100%;
  display: flex;
  align-items: center;
}

/* 顶栏左侧的组织切换：与标题留间距，空间不足时不被压缩 */
.header-left .org-selector,
.header-left .org-info {
  margin-left: 12px;
  flex-shrink: 0;
}

.header-right {
  flex-shrink: 0;
  min-width: 0;
}

.org-selector,
.org-info,
.language-selector-wrapper {
  display: inline-flex;
  align-items: center;
  height: var(--hc-header-control-height);
  line-height: 1;
}

.org-selector :deep(.el-dropdown),
.language-selector-wrapper :deep(.lang-dropdown) {
  display: inline-flex;
  align-items: center;
  line-height: 1;
  vertical-align: middle;
}

.header-toolbar :deep(.xiaojiang-upload-wrapper) {
  display: inline-flex;
  align-items: center;
  height: var(--hc-header-control-height);
}

.header-toolbar :deep(.xiaojiang-icon) {
  width: 18px;
  height: 18px;
  object-fit: contain;
  flex-shrink: 0;
}

.header-toolbar :deep(.user-info .el-avatar) {
  width: 26px !important;
  height: 26px !important;
  flex-shrink: 0;
}

.header-toolbar :deep(.user-info .username) {
  font-size: 13px;
  color: var(--hc-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.layout-container.is-mobile .header-toolbar {
  gap: 6px;
}

.layout-container.is-mobile .hc-header-chip--org {
  min-width: 0;
  max-width: 120px;
  padding: 0 8px;
}

.layout-container.is-mobile .header-toolbar :deep(.user-info .username) {
  display: none;
}

.layout-container.is-mobile .header-toolbar :deep(.user-info.hc-header-chip) {
  padding: 0 4px;
  min-width: var(--hc-header-control-height);
  justify-content: center;
}

.main-container {
  background-color: var(--hc-content-bg);
  flex-direction: row;
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

.layout-container.is-mobile .main-container {
  flex-direction: column;
}

.secondary-sidebar {
  background-color: #ffffff;
  border-right: 1px solid var(--hc-border);
  transition: width 0.28s ease;
  height: 100%;
  overflow-y: auto;
  flex-shrink: 0;
  box-shadow: 1px 0 4px rgba(16, 24, 40, 0.04);
}

.content {
  --el-main-padding: 0;
  position: relative;
  padding: 0;
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  flex: 1;
  min-width: 0;
}

.content-body {
  min-height: 100%;
  height: auto;
}

.footer {
  background-color: #ffffff;
  border-top: 1px solid var(--hc-border);
  padding: 0;
  text-align: center;
  color: var(--hc-text-secondary);
  font-size: 13px;
  flex-shrink: 0;
}

/* ---------- 清除缓存弹窗：按风险分级的操作卡片 ---------- */
.clear-cache-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.clear-cache-header__icon {
  width: 38px;
  height: 38px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 11px;
  background: linear-gradient(135deg, #eef4ff, #dfe9ff);
  color: #3b82f6;
  font-size: 19px;
  box-shadow: inset 0 0 0 1px rgba(59, 130, 246, 0.12);
}

.clear-cache-header__text {
  min-width: 0;
}

.clear-cache-header__title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.35;
  color: #1f2937;
  letter-spacing: 0.01em;
}

.clear-cache-header__sub {
  margin: 2px 0 0;
  font-size: 12px;
  line-height: 1.4;
  color: #8b95a5;
}

.cache-options {
  display: flex;
  align-items: stretch;
  gap: 14px;
  padding-top: 2px;
}

.cache-option {
  position: relative;
  flex: 1 1 0;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 22px 16px 18px;
  border-radius: 14px;
  border: 1px solid var(--hc-border, #e8ebf0);
  background: #fff;
  font-family: inherit;
  text-align: center;
  cursor: pointer;
  outline: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.cache-option:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 键盘可达：焦点环用主色描边，不改变卡片形状 */
.cache-option:focus-visible {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.18);
}

.cache-option__badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 1px 7px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 500;
  line-height: 1.6;
  letter-spacing: 0.02em;
}

.cache-option__icon {
  width: 48px;
  height: 48px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 15px;
  font-size: 21px;
  flex-shrink: 0;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.cache-option__icon .el-icon {
  font-size: 21px;
}

.cache-option__title {
  margin-top: 2px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.01em;
  color: #1f2937;
}

.cache-option__desc {
  /* 统一两行高度，三张卡片的文字块底边对齐 */
  min-height: 34px;
  font-size: 12px;
  line-height: 1.55;
  color: #8b95a5;
  /* 断行只落在逗号/空格处，避免「薪资」被拆成「薪 / 资」、「缓存」被拆成「缓 / 存」 */
  word-break: keep-all;
  text-wrap: balance;
}

/* 菜单缓存：蓝 · 常用项 */
.cache-option--menu .cache-option__icon {
  background: linear-gradient(135deg, #eaf3ff, #d7e8ff);
  color: #2f7ff0;
  box-shadow: 0 4px 10px rgba(47, 127, 240, 0.12);
}

.cache-option--menu .cache-option__badge {
  background: #eaf3ff;
  color: #2f7ff0;
}

.cache-option--menu:not(:disabled):hover {
  border-color: #9ec6f8;
  box-shadow: 0 10px 22px rgba(47, 127, 240, 0.14);
  transform: translateY(-2px);
}

/* 数据缓存：琥珀 */
.cache-option--data .cache-option__icon {
  background: linear-gradient(135deg, #fff3e2, #ffe6c7);
  color: #d98a1c;
  box-shadow: 0 4px 10px rgba(217, 138, 28, 0.12);
}

.cache-option--data:not(:disabled):hover {
  border-color: #f0cf94;
  box-shadow: 0 10px 22px rgba(217, 138, 28, 0.14);
  transform: translateY(-2px);
}

/* 全部缓存：危险项，整卡浅红底 + 红边，避免与安全项混淆 */
.cache-option--all {
  border-color: #f6d5d5;
  background: linear-gradient(180deg, #fffafa, #fff);
}

.cache-option--all .cache-option__icon {
  background: linear-gradient(135deg, #ffeaea, #ffd8d8);
  color: #e05252;
  box-shadow: 0 4px 10px rgba(224, 82, 82, 0.12);
}

.cache-option--all .cache-option__badge {
  background: #ffeaea;
  color: #d93b3b;
}

.cache-option--all:not(:disabled):hover {
  border-color: #efa9a9;
  background: linear-gradient(180deg, #fff5f5, #fff);
  box-shadow: 0 10px 22px rgba(224, 82, 82, 0.16);
  transform: translateY(-2px);
}

.cache-option:not(:disabled):hover .cache-option__icon {
  transform: scale(1.06);
}

/* 底部安全提示 */
.clear-cache-note {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin: 14px 0 0;
  padding: 9px 12px;
  border-radius: 9px;
  background: #f7f9fc;
  font-size: 12px;
  line-height: 1.55;
  color: #7d8798;

  .el-icon {
    flex-shrink: 0;
    margin-top: 1px;
    font-size: 13px;
    color: #a2acbb;
  }
}

/* 小屏：卡片改为横向紧凑行，图标居左，避免三卡竖排拉得过长 */
@media (max-width: 768px) {
  .cache-options {
    flex-direction: column;
    gap: 10px;
  }

  .cache-option {
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
    gap: 2px 12px;
    padding: 14px;
    text-align: left;
  }

  .cache-option__icon {
    width: 42px;
    height: 42px;
    border-radius: 13px;
  }

  .cache-option__title {
    margin-top: 0;
    flex: 1 1 auto;
  }

  .cache-option__desc {
    flex: 1 0 100%;
    min-height: 0;
    padding-left: 54px;
  }

  /* 徽标在窄卡里会压住标题，窄屏省略 */
  .cache-option__badge {
    display: none;
  }
}
</style>
