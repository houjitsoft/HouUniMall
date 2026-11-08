/** 站点根目录静态模板（仓库 public/templates，部署后 URL 为 /templates/...） */
export const SALARY_IMPORT_TEMPLATE_CSV = '/templates/工资导入模板.csv'
export const SALARY_IMPORT_TEMPLATE_XLSX = '/templates/工资导入模板.xlsx'

/**
 * 触发浏览器下载 public 目录下的静态文件
 * @param {string} url 如 /templates/工资导入模板.xlsx
 * @param {string} [filename] 保存文件名
 */
export function downloadPublicAsset(url, filename) {
  const link = document.createElement('a')
  link.href = url
  if (filename) link.download = filename
  link.rel = 'noopener'
  link.style.display = 'none'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
