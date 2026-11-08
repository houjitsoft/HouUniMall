import { createApp } from 'vue'
import { createPinia } from 'pinia'
// 装修组件（components/widgets/**）的模板与样式使用 Tailwind/Uno 风格的工具类，
// 必须引入 virtual:uno.css 才会真正生成对应规则（见 uno.config.ts）。
import 'virtual:uno.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import './styles/design-system.css'
import './styles/mobile.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import { useAppStore } from './stores/app'
import { permissionDirective } from './utils/permission'
import { initUserMenusCrossTabSync } from './utils/userMenusCache'
import { initAdminTheme } from './utils/adminTheme'
import { initAssistantWidget } from './utils/weknora'
// 使用 Element Plus 国际化系统
import { getElementPlusLocale } from './i18n-element'
// GEO 模块（复制自 frontend/geo）：i18n + 全局样式
import { i18n } from '@/locales'

// 尽早恢复主题，避免首屏闪白/闪色
initAdminTheme()

const app = createApp(App)

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册权限指令
app.directive('perms', permissionDirective)


const pinia = createPinia()
app.use(pinia)

const appStore = useAppStore()
if (typeof window !== 'undefined') {
  const rebuildMenus = () => appStore._rebuildMenus()
  window.addEventListener('language-changed', rebuildMenus)
  window.addEventListener('plugin-i18n-changed', rebuildMenus)
}

// 初始化认证状态（从localStorage恢复userInfo）
const authStore = useAuthStore()
authStore.initAuth()

// 【重要修复】不再在应用启动时获取用户信息
// 原因：
// 1. initAuth()已经从localStorage恢复userInfo，不需要再API调用
// 2. 如果token过期，调用getUserInfo()会触发"用户未登录"错误
// 3. 响应拦截器会清除localStorage并跳转登录页
// 4. 导致用户刷新页面后被强制登出
// 5. 与main.js、routeGuard、Layout的getUserInfo调用形成三重调用
// 解决方案：
// - 只使用initAuth从localStorage恢复userInfo，不调用API
// - 如果token过期，由用户实际访问页面时的API请求触发错误
// - 响应拦截器会处理跳转，用户体验更好

const currentElementLocale = getElementPlusLocale()
app.use(ElementPlus, {
  locale: currentElementLocale
})

app.use(router)

// GEO 模块 i18n（复制自 frontend/geo/src/main.js）
app.use(i18n)

// 宿主 Element Plus i18n 与 GEO/vue-i18n 共用同一个语言事件。
if (typeof window !== 'undefined') {
  window.addEventListener('language-changed', (event) => {
    const locale = event.detail
    if (locale && i18n.global.locale.value !== locale && i18n.global.availableLocales.includes(locale)) {
      i18n.global.locale.value = locale
    }
  })
}

app.mount('#app')

// 右下角知识库挂件（WeKnora「小匠」）：渠道配置从站点上的
// weknora-widget.config.json 读取，页面上下文由 layouts/Layout.vue 持续注入。
initAssistantWidget()

initUserMenusCrossTabSync()
