let currentContext = null

export function setPluginContext(context) { currentContext = context || null }
export function getPluginContext() {
  if (!currentContext) throw new Error('mall-distribution 插件运行上下文尚未初始化')
  return currentContext
}
