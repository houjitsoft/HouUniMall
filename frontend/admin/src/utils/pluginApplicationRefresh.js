/**
 * 插件安装、卸载或启停成功后，立刻刷新右上角「插件应用」。
 *
 * 面板要同时满足两件事才显示：权限树里有 display_pos=plugin 的入口，
 * 以及应用目录里该插件已安装且已启用。这两份数据都会缓存在当前页面，
 * 只刷新市场列表时，入口要等整页刷新才会出现。
 */

const refreshers = new Set()

/**
 * 右上角面板注册自己的目录刷新函数。
 * @param {() => Promise<unknown>|unknown} refresher
 * @returns {() => void} 取消注册
 */
export function registerPluginApplicationRefresher(refresher) {
  if (typeof refresher !== 'function') return () => {}
  refreshers.add(refresher)
  return () => refreshers.delete(refresher)
}

/**
 * 清菜单缓存并重拉权限树、应用目录和插件路由。
 * 任一步失败都不抛出，避免安装已经成功却被刷新失败盖掉。
 * @returns {Promise<void>}
 */
export async function refreshPluginApplications() {
  const tasks = []
  tasks.push((async () => {
    const { refreshMenusAfterPermissionChange } = await import('@/utils/userMenusCache')
    await refreshMenusAfterPermissionChange()
  })())
  for (const refresher of [...refreshers]) {
    tasks.push(Promise.resolve().then(() => refresher()))
  }
  tasks.push((async () => {
    const { usePluginStore } = await import('@/stores/plugin')
    const pluginStore = usePluginStore()
    if (!pluginStore.loadedOnce && !pluginStore.loading) return
    await pluginStore.syncInstalledPlugins()
  })())
  const results = await Promise.allSettled(tasks)
  const rejected = results.find((item) => item.status === 'rejected')
  if (rejected) {
    console.warn('[pluginApplicationRefresh] 部分刷新失败', rejected.reason)
  }
}
