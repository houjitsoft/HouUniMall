let currentContext = null
export function setPluginContext(context) { currentContext = context || null }
export function getPluginContext() {
  if (!currentContext) throw new Error('logging plugin context is not initialized')
  return currentContext
}
