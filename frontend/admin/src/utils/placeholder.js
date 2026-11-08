/**
 * 生成本地占位图 data URI，不依赖外部服务。
 * 用于替代 via.placeholder.com 等外部域名，避免 ERR_NAME_NOT_RESOLVED。
 * @param {number} [width=32] - 宽度（像素）
 * @param {number} [height] - 高度（像素），缺省时等于 width
 * @returns {string} data:image/svg+xml 格式的占位图 URI
 */
export function getPlaceholderDataUri(width = 32, height) {
  const w = Number(width) || 32
  const h = height != null ? Number(height) : w
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="${h}"><rect fill="#e5e7eb" width="${w}" height="${h}"/></svg>`
  return 'data:image/svg+xml,' + encodeURIComponent(svg)
}
