/**
 * 站点级二开前端通道 · 构建输入登记（T09 / R2.5）。
 *
 * 该模块只在 **Node 环境**（`vite.config.js` 与 `node --test`）被引用，绝不进入运行时
 * bundle。核心职责：扫描 `src/extensions/{hostService}/{code}/index.js`，把每个站点级扩展登记为
 * 独立的 Vite/Rollup 构建输入，使其产物成为独立 chunk（`assets/extensions/ext_{code}-*.js`）。
 *
 * 官方 `index.html` 入口（name=`main`）保持不变，因此官方视图目录与官方路由
 * 不会被这些额外的构建输入覆盖。
 */

import fs from 'node:fs'
import path from 'node:path'

const serviceNames = new Set(JSON.parse(fs.readFileSync(new URL('../../../../architecture/services.json', import.meta.url), 'utf8')).services.map((service) => service.name))

/** 站点级扩展入口/清单文件名（与 siteExtensionContract.js 保持一致）。 */
export const SITE_EXTENSION_ENTRY_FILE = 'index.js'
export const SITE_EXTENSION_MANIFEST_FILE = 'manifest.js'

/** Rollup 入口名前缀：用于把站点级扩展 chunk 与官方 `main` 区分开。 */
export const SITE_EXTENSION_ENTRY_PREFIX = 'ext_'

/** 站点级扩展 chunk 的输出子目录。 */
export const SITE_EXTENSION_ASSET_DIR = 'assets/extensions'

/**
 * 生成站点级扩展的 Rollup 入口名。
 * @param {string} code 扩展编码
 * @returns {string} 如 `ext_custom-goods`
 */
export function siteExtensionEntryName(code) {
  return `${SITE_EXTENSION_ENTRY_PREFIX}${String(code || '').trim()}`
}

/**
 * 判断一个 Rollup chunk 名是否属于站点级扩展入口。
 * @param {string} name chunk 名
 * @returns {boolean}
 */
export function isSiteExtensionEntryName(name) {
  return typeof name === 'string' && name.startsWith(SITE_EXTENSION_ENTRY_PREFIX)
}

/**
 * 列出 `extensionsDir` 下所有合法的站点级扩展 code（存在 `index.js` 的子目录）。
 * @param {string} extensionsDir `src/extensions` 绝对路径
 * @returns {string[]} 升序 code 列表；目录不存在时返回空数组
 */
function discoverSiteExtensionDirectories(extensionsDir) {
  const entries = new Map()
  if (!fs.existsSync(extensionsDir)) return entries
  for (const host of fs.readdirSync(extensionsDir, { withFileTypes: true })) {
    if (!host.isDirectory()) continue
    const hostDir = path.join(extensionsDir, host.name)
    if (fs.existsSync(path.join(hostDir, SITE_EXTENSION_ENTRY_FILE))) {
      throw new Error(`站点扩展须归属业务服务：${hostDir}`)
    }
    for (const entry of fs.readdirSync(hostDir, { withFileTypes: true })) {
      const directory = path.join(hostDir, entry.name)
      if (!entry.isDirectory() || !fs.existsSync(path.join(directory, SITE_EXTENSION_ENTRY_FILE))) continue
      if (!serviceNames.has(host.name)) throw new Error(`未知归属服务：${host.name}`)
      if (!/^[a-z0-9][a-z0-9._-]*$/.test(entry.name)) throw new Error(`扩展编码非法：${entry.name}`)
      if (entries.has(entry.name)) throw new Error(`扩展编码重复：${entry.name}`)
      entries.set(entry.name, directory)
    }
  }
  return new Map([...entries].sort(([a], [b]) => a.localeCompare(b)))
}

export function listSiteExtensionCodes(extensionsDir) {
  return [...discoverSiteExtensionDirectories(extensionsDir).keys()]
}

/**
 * 收集站点级扩展的 Rollup 构建输入映射（可直接展开到 `rollupOptions.input`）。
 * @param {string} extensionsDir `src/extensions` 绝对路径
 * @returns {Record<string,string>} 入口名 → 入口文件绝对路径
 */
export function collectSiteExtensionInputs(extensionsDir) {
  const inputs = {}
  for (const [code, directory] of discoverSiteExtensionDirectories(extensionsDir)) {
    inputs[siteExtensionEntryName(code)] = path.join(directory, SITE_EXTENSION_ENTRY_FILE)
  }
  return inputs
}

export default {
  SITE_EXTENSION_ENTRY_FILE,
  SITE_EXTENSION_MANIFEST_FILE,
  SITE_EXTENSION_ENTRY_PREFIX,
  SITE_EXTENSION_ASSET_DIR,
  siteExtensionEntryName,
  isSiteExtensionEntryName,
  listSiteExtensionCodes,
  collectSiteExtensionInputs
}
