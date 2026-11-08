/** GEO 项目切换需重建页面，清理创作页的一次性自动生成参数则保留当前实例。 */
export function isGeoProjectRoutePath(path) {
  const value = String(path || '')
  return value === '/geo'
    || value.startsWith('/geo/')
    || value === '/plugin/geo'
    || value.startsWith('/plugin/geo/')
}

export function getGeoViewKey(route, projectId) {
  let viewPath = route.fullPath
  if (
    route.path === '/geo/content/ai-studio/generate'
    || route.path === '/plugin/geo/content/ai-studio/generate'
  ) {
    const url = new URL(route.fullPath, 'https://unimall.houjit.com')
    url.searchParams.delete('autoGenerate')
    viewPath = `${url.pathname}${url.search}${url.hash}`
  }
  return isGeoProjectRoutePath(route.path)
    ? `${viewPath}#geo-project-${projectId ?? 'none'}`
    : viewPath
}
