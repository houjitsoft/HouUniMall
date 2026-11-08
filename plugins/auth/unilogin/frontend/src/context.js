/**
 * 插件运行上下文（宿主注入）
 *
 * 宿主在装载插件时注入 context：登录期由 utils/loginPluginLoader.js 注入，
 * 登录后由 stores/plugin.js 注入。插件只依赖这个契约，不感知宿主内部实现。
 */
let context = null

export function setPluginContext(ctx) {
  context = ctx || null
}

export function getPluginContext() {
  return context
}

/**
 * 调用宿主 Core Port。
 *
 * 各登录渠道的服务端能力全部由宿主 auth 服务提供（/web/auth/unilogin/{channel}/**），
 * 插件不自建后端、不直连任何内部接口，新增渠道也不需要新增后端服务以外的组件。
 */
export function callHost(path, body = {}, options = {}) {
  const ctx = getPluginContext()
  if (!ctx?.callCoreApi && !ctx?.callApi) {
    throw new Error('插件上下文未就绪')
  }
  const invoke = ctx.callCoreApi || ctx.callApi
  return invoke(path, body, options)
}
