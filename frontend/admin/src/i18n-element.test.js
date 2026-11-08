import { afterEach, describe, expect, it } from 'vitest'
import {
  createPluginI18n,
  getLocale,
  registerPluginMessages,
  setLocale,
  t,
  getBusinessMessages,
  unregisterPluginMessages
} from './i18n-element'

describe('宿主与插件多语言运行时契约', () => {
  const originalLocale = getLocale()

  afterEach(() => {
    unregisterPluginMessages('aicode')
    setLocale(originalLocale)
  })

  it('动态插件语言包优先于宿主兼容文案，并按插件命名空间查找', () => {
    registerPluginMessages('aicode', {
      'zh-CN': { menu: { name: '自定义 AI' } },
      en: { menu: { name: 'Custom AI' } }
    })

    setLocale('en')
    expect(t('plugin.aicode.menu.name')).toBe('Custom AI')
    expect(createPluginI18n('aicode').t('menu.name')).toBe('Custom AI')

    setLocale('zh-CN')
    expect(createPluginI18n('aicode').t('menu.name')).toBe('自定义 AI')
  })

  it('卸载插件后回退到宿主内置文案', () => {
    // 显式指定语言：t() 按当前语言取值，上面用例切换过 en，不能依赖残留状态
    setLocale('zh-CN')
    expect(getBusinessMessages('zh-CN')?.plugin?.aicode?.name).toBe('AI 开发')
    registerPluginMessages('aicode', { 'zh-CN': { menu: { name: '临时名称' } } })
    expect(t('plugin.aicode.menu.name')).toBe('临时名称')

    unregisterPluginMessages('aicode')
    expect(t('plugin.aicode.name')).toBe('AI 开发')
  })
})
