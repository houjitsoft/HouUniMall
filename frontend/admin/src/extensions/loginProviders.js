/**
 * 登录方式扩展点（Login Providers）
 *
 * 宿主登录页（views/permission/Login.vue）本身只内置「账号密码 / 手机号」两种登录方式。
 * 其余登录方式（微信扫码、企业微信、钉钉、OIDC……）一律由插件通过本扩展点注入，
 * 宿主不感知任何具体第三方登录的业务细节。
 *
 * 契约：插件前端产物（frontend/index.mjs）默认导出里可声明
 *
 * 一个插件可声明多个登录方式（plugins/auth/unilogin 就是按渠道声明微信 / 企微 / 钉钉…）：
 *
 *   export default {
 *     loginProviders: [
 *       {
 *         code: 'wechat',                // 登录方式唯一编码（全局唯一，通常是渠道编码）
 *         title: '微信快捷登录',          // 兜底标题，插件未国际化时使用
 *         titleKey: 'login.wechat',      // 可选，插件 i18n key
 *         icon: 'ChatDotRound',          // 可选，宿主图标名或组件
 *         entry: UniLoginEntry          // 必填，自渲染 Vue 组件：自带按钮 + 交互（弹窗/跳转）
 *       }
 *     ]
 *   }
 *
 * 宿主职责：
 * 1) 登录期装载声明了 loginProviders 的插件（未登录也能用，见 utils/loginPluginLoader.js）；
 * 2) 登录后由插件 store 统一装载（stores/plugin.js），两处注册按 pluginCode 去重；
 * 3) 登录页只负责把 entry 组件渲染到「其他登录方式」区域，交互完全归插件；
 * 4) 插件卸载时同步摘除，登录页入口随之消失。
 */

import { shallowReactive } from 'vue'

/** @type {Array<{code:string,pluginCode:string,title?:string,titleKey?:string,icon?:any,entry:any}>} */
const providers = shallowReactive([])

/**
 * 注册一个登录方式
 * @param {{code:string,pluginCode?:string,title?:string,titleKey?:string,icon?:any,entry:any}} provider
 * @returns {boolean} 是否注册成功
 */
export function registerLoginProvider(provider) {
  if (!provider || !provider.code || !provider.entry) return false
  unregisterLoginProvider(provider.code)
  providers.push({ ...provider, pluginCode: provider.pluginCode || provider.code })
  return true
}

/**
 * 注销一个登录方式
 * @param {string} code 登录方式编码
 * @returns {boolean}
 */
export function unregisterLoginProvider(code) {
  const index = providers.findIndex((item) => item.code === code)
  if (index === -1) return false
  providers.splice(index, 1)
  return true
}

/**
 * 批量注册某个插件声明的登录方式（幂等：先清理该插件的旧注册）
 * @param {string} pluginCode 插件编码
 * @param {Array} list 插件 manifest.loginProviders
 * @returns {number} 实际注册数量
 */
export function registerPluginLoginProviders(pluginCode, list) {
  unregisterPluginLoginProviders(pluginCode)
  if (!Array.isArray(list) || !list.length) return 0
  let count = 0
  list.forEach((item, index) => {
    if (!item || !item.entry) return
    const code = item.code || `${pluginCode}:${index}`
    if (registerLoginProvider({ ...item, code, pluginCode })) count += 1
  })
  return count
}

/**
 * 移除某个插件注册的全部登录方式
 * @param {string} pluginCode 插件编码
 * @returns {boolean}
 */
export function unregisterPluginLoginProviders(pluginCode) {
  let removed = false
  for (let index = providers.length - 1; index >= 0; index -= 1) {
    if (providers[index].pluginCode === pluginCode) {
      providers.splice(index, 1)
      removed = true
    }
  }
  return removed
}

/** 当前已注册的登录方式（按注册顺序） */
export function listLoginProviders() {
  return providers
}

/** 测试与排障用：清空全部注册 */
export function resetLoginProviders() {
  providers.splice(0, providers.length)
}
