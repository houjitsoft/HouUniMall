import { describe, expect, it } from 'vitest'
import {
  normalizeAdminLanguageConfig,
  normalizeLanguageCode,
  resolveAdminLocale,
  visibleAdminLanguages
} from './adminLanguageCatalog.js'

describe('adminLanguageCatalog', () => {
  it('把设置页旧编码收成切换器认识的编码', () => {
    expect(normalizeLanguageCode('en-US')).toBe('en')
    expect(normalizeLanguageCode('zh-TW')).toBe('zh-TW')
    expect(normalizeLanguageCode('ja-JP')).toBe('ja')
    expect(normalizeLanguageCode('ko-KR')).toBe('ko')
  })

  it('未保存配置时默认只开放简体中文和 English', () => {
    const visible = visibleAdminLanguages(null).map((item) => item.label)
    expect(visible).toEqual(['简体中文', 'English'])
  })

  it('右上角只显示勾选的语言，并沿用设置页的名称', () => {
    const visible = visibleAdminLanguages({
      supportedLanguages: ['zh-CN', 'en-US', 'ja-JP', 'ms-MY'],
      multiLanguage: true,
      defaultLanguage: 'zh-CN'
    }).map((item) => item.label)
    expect(visible).toEqual(['简体中文', 'English', '日本語', 'Bahasa Melayu'])
  })

  it('关闭多语言后右上角只保留默认语言', () => {
    const visible = visibleAdminLanguages({
      supportedLanguages: ['zh-CN', 'en'],
      multiLanguage: false,
      defaultLanguage: 'en'
    }).map((item) => item.value)
    expect(visible).toEqual(['en'])
  })

  it('当前语言被取消勾选后回到默认语言', () => {
    const config = normalizeAdminLanguageConfig({
      supportedLanguages: ['zh-CN', 'fr'],
      defaultLanguage: 'zh-CN',
      multiLanguage: true,
      autoDetect: false
    })
    expect(resolveAdminLocale({
      config,
      storedLocale: 'en',
      browserLocales: ['en-US']
    })).toBe('zh-CN')
  })

  it('没有已选语言且开启自动检测时，用浏览器语言里第一个已勾选的', () => {
    const config = normalizeAdminLanguageConfig({
      supportedLanguages: ['zh-CN', 'ja'],
      defaultLanguage: 'zh-CN',
      autoDetect: true
    })
    expect(resolveAdminLocale({
      config,
      storedLocale: '',
      browserLocales: ['ja-JP', 'zh-CN']
    })).toBe('ja')
  })

  it('勾选列表为空时不生成配置', () => {
    expect(normalizeAdminLanguageConfig({ supportedLanguages: [] }, { useDefaultWhenEmpty: false })).toBeNull()
  })
})
