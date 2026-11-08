// 目录型语言包加载器
//
// 背景：src/locales/<lang>/**/*.json（actions / auth.login / dashboard / menu / plugin / user …）
// 历史上只被自研实现 i18n-element.js 通过 import.meta.glob 读取，vue-i18n 实例拿不到，
// 于是模板里的 $t('auth.login.*')、$t('dashboard.*') 全部解析不到、直接回显 key 字符串。
// 这里把同一批 JSON 归并出来给 vue-i18n 复用，两套实现共用一份数据源，避免再次分叉。

import { SUPPORTED_LOCALES } from './localePreference.js'

const modules = import.meta.glob('./**/*.json', { eager: true, import: 'default' })

function isPlainObject(value) {
  return Boolean(value) && typeof value === 'object' && !Array.isArray(value)
}

/** 深度合并，source 覆盖 target 的同名叶子节点 */
export function mergeDeep(target, source) {
  if (!isPlainObject(source)) return target
  Object.entries(source).forEach(([key, value]) => {
    if (isPlainObject(value)) {
      target[key] = mergeDeep(isPlainObject(target[key]) ? target[key] : {}, value)
    } else if (value != null) {
      target[key] = value
    }
  })
  return target
}

/**
 * 按文件路径生成命名空间：
 *   ./zh-CN/actions.json      -> zh-CN.actions.*
 *   ./zh-CN/auth/login.json   -> zh-CN.auth.login.*
 * 与 i18n-element.js 的解析规则保持一致。
 */
export function buildDirMessages() {
  const result = {}

  Object.entries(modules).forEach(([filePath, content]) => {
    const parts = filePath.replace(/^\.\//, '').split('/')
    const locale = parts.shift()
    if (!SUPPORTED_LOCALES.includes(locale)) return

    const fileName = parts.pop().replace(/\.json$/, '')
    // plugin.json 历史上多包了一层 plugin，归一化后保持 plugin.{code}.* 的形态
    const payload =
      fileName === 'plugin' && isPlainObject(content?.plugin) ? content.plugin : content
    if (!isPlainObject(payload)) return

    const namespaceParts = [...parts, fileName]
    let node = (result[locale] = result[locale] || {})
    for (let i = 0; i < namespaceParts.length; i += 1) {
      const part = namespaceParts[i]
      if (i === namespaceParts.length - 1) {
        node[part] = mergeDeep(isPlainObject(node[part]) ? node[part] : {}, payload)
      } else {
        node[part] = isPlainObject(node[part]) ? node[part] : {}
        node = node[part]
      }
    }
  })

  return result
}
