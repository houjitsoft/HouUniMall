import { createMenuMessages } from './menuMessages.js'
import { createVideoStudioMessages } from './videoStudioMessages.js'
import { createConversionGoalsMessages } from './conversionGoalsMessages.js'
import { createOperationGuideMessages } from './operationGuideMessages.js'

export default {
  common: { retry: '重试', loading: '正在加载…', planned: '待开发' },
  header: {
    chooseProject: '选择 GEO 项目', openAccountMenu: '打开账号菜单', profile: '个人中心',
    settings: '设置', logout: '退出登录', loggingOut: '正在退出…', user: '用户'
  },
  language: { switch: '切换语言' },
  project: {
    loadFailed: '项目加载失败，请重试', empty: '请先创建或选择 GEO 项目后再加载工作台',
    loadingContext: '正在加载项目上下文…', welcome: '欢迎使用 HouGEO', registerWebsite: '请先登记您的网站',
    onboardingDescription: '创建您的第一个 GEO 项目，开启品牌 AI 搜索优化之旅。', name: '项目名称',
    domain: '网站域名', namePlaceholder: '如：厚匠企业官网', domainPlaceholder: '如：houjit.com',
    createNow: '立即创建', nameRequired: '请填写项目名称', createSuccess: '项目创建成功，开始您的 GEO 优化之旅！',
    unnamed: '项目 {id}', currentPage: '当前页面'
  },
  navigation: { primary: '一级菜单', secondary: '二级菜单', openMenu: '打开菜单' },
  menu: createMenuMessages('zh-CN'),
  videoStudio: createVideoStudioMessages('zh-CN'),
  conversionGoals: createConversionGoalsMessages('zh-CN'),
  operationGuide: createOperationGuideMessages('zh-CN')
}
