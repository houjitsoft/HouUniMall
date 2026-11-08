// @vitest-environment jsdom
import { beforeEach, describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { parse } from '@vue/compiler-sfc'
import {
  DEFAULT_ADMIN_THEME,
  ADMIN_THEME_STORAGE_KEY,
  normalizeAdminTheme,
  loadAdminTheme,
  saveAdminTheme,
  resolveThemeMode,
  mixColor,
  applyAdminTheme
} from './adminTheme.js'

describe('adminTheme', () => {
  beforeEach(() => {
    localStorage.clear()
    document.documentElement.className = ''
    document.documentElement.removeAttribute('data-theme')
    document.documentElement.removeAttribute('style')
  })

  it('normalizeAdminTheme fills defaults and clamps invalid values', () => {
    expect(normalizeAdminTheme(null)).toEqual(DEFAULT_ADMIN_THEME)
    expect(normalizeAdminTheme({ mode: 'neon', primaryColor: 'red', sidebarTheme: 'pink' })).toMatchObject({
      mode: 'light',
      primaryColor: '#409EFF',
      sidebarTheme: 'dark'
    })
    expect(normalizeAdminTheme({ mode: 'dark', primaryColor: '#abc', showTopNav: false }).primaryColor).toBe('#AABBCC')
  })

  it('不再启用全局面包屑，包括旧版保存的开启设置', () => {
    expect(DEFAULT_ADMIN_THEME.showBreadcrumb).toBe(false)
    expect(normalizeAdminTheme({ showBreadcrumb: true }).showBreadcrumb).toBe(false)
    localStorage.setItem(ADMIN_THEME_STORAGE_KEY, JSON.stringify({ showBreadcrumb: true }))
    expect(loadAdminTheme().showBreadcrumb).toBe(false)
    applyAdminTheme(loadAdminTheme())
    expect(document.documentElement.getAttribute('data-show-breadcrumb')).toBe('off')
  })

  it('公共布局去掉重复标题行，同时保留一级菜单、二三级导航和页面出口', () => {
    const source = readFileSync(resolve(dirname(fileURLToPath(import.meta.url)), '../layouts/Layout.vue'), 'utf8')
    const { descriptor, errors } = parse(source)
    expect(errors).toEqual([])
    expect(descriptor.template.content).not.toMatch(/el-breadcrumb|layout-breadcrumb/)
    expect(descriptor.template.content).toContain('<FirstLevelMenu')
    expect(descriptor.template.content).toContain('<TertiaryMenu')
    expect(descriptor.template.content).toContain('<router-view')
    expect(descriptor.scriptSetup.content).not.toContain('breadcrumbItems')

    const themeView = readFileSync(resolve(dirname(fileURLToPath(import.meta.url)), '../views/setting/WebsiteTheme.vue'), 'utf8')
    expect(parse(themeView).descriptor.template.content).not.toContain('themeForm.showBreadcrumb')
  })

  it('save/load round-trips through localStorage', () => {
    const saved = saveAdminTheme({
      mode: 'dark',
      primaryColor: '#67C23A',
      sidebarTheme: 'green',
      showTopNav: false,
      showBreadcrumb: false,
      sidebarCollapse: true,
      pageAnimation: false,
      customCSS: 'body{outline:1px solid red}'
    })
    expect(JSON.parse(localStorage.getItem(ADMIN_THEME_STORAGE_KEY))).toEqual(saved)
    expect(loadAdminTheme()).toEqual(saved)
  })

  it('resolveThemeMode respects explicit and system preference', () => {
    expect(resolveThemeMode('light')).toBe('light')
    expect(resolveThemeMode('dark')).toBe('dark')
    expect(['light', 'dark']).toContain(resolveThemeMode('auto'))
  })

  it('mixColor blends toward white/black', () => {
    expect(mixColor('#000000', 0.5, 'white')).toBe('#808080')
    expect(mixColor('#FFFFFF', 0.5, 'black')).toBe('#808080')
  })

  it('applyAdminTheme sets dark class and primary css vars', () => {
    applyAdminTheme({
      mode: 'dark',
      primaryColor: '#E6A23C',
      sidebarTheme: 'blue'
    }, { persist: true })

    expect(document.documentElement.classList.contains('dark')).toBe(true)
    expect(document.documentElement.getAttribute('data-theme')).toBe('dark')
    expect(document.documentElement.style.getPropertyValue('--el-color-primary')).toBe('#E6A23C')
    expect(document.documentElement.style.getPropertyValue('--hc-sidebar-bg')).toBe('#1d39c4')
    expect(document.documentElement.getAttribute('data-sidebar-theme')).toBe('blue')
  })
})
