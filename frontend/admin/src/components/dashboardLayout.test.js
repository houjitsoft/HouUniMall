// @vitest-environment jsdom
import { readFileSync } from 'node:fs'
import { URL as NodeURL } from 'node:url'
import { mount } from '@vue/test-utils'
import { defineComponent, markRaw, nextTick } from 'vue'
import { parse, compileStyle } from '@vue/compiler-sfc'
import { ElEmpty } from 'element-plus'
import { compileString } from 'sass-embedded'
import { afterEach, describe, expect, it, vi } from 'vitest'
import WidgetsSection from './dashboard/sections/WidgetsSection.vue'
import Footer from './Footer.vue'

const sortable = vi.hoisted(() => ({ create: vi.fn(), destroy: vi.fn() }))
vi.mock('sortablejs', () => ({ default: { create: sortable.create } }))
vi.mock('@/i18n-element', () => ({ useI18n: () => ({ t: (key) => key }) }))

const passthrough = { template: '<div><slot /><slot name="header" /></div>' }
const button = { template: '<button><slot /></button>' }
const widget = markRaw(defineComponent({ template: '<div class="real-widget">模块内容</div>' }))
const item = (w) => ({ moduleId: 'goods', w, _meta: {
  title: 'goods.title', color: '#409eff', icon: widget, component: widget, route: '/goods'
} })
const wrappers = []
function mountWidgets(props = {}) {
  const wrapper = mount(WidgetsSection, {
    props: { config: { showTitle: true }, ...props },
    global: { components: { ElEmpty }, stubs: {
      'el-card': passthrough, 'el-tooltip': passthrough,
      'el-button': button, 'el-icon': passthrough
    } }
  })
  wrappers.push(wrapper)
  return wrapper
}
afterEach(() => {
  wrappers.splice(0).forEach((wrapper) => wrapper.unmount())
  vi.useRealTimers()
  vi.clearAllMocks()
})

const css = compileString(readFileSync(new NodeURL('../views/dashboard.page.scss', import.meta.url), 'utf8')).css
function rule(selector) {
  const escaped = selector.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return css.match(new RegExp(`${escaped} \\{([^}]+)\\}`))?.[1] || ''
}

describe('工作台原组件布局回归（DOM 与 CSS 契约，非像素证明）', () => {
  it('空态使用真实 ElEmpty，图片 80 且横跨所有列，无虚构模块', () => {
    const wrapper = mountWidgets()
    expect(wrapper.findAll('.widget-cell')).toHaveLength(0)
    expect(wrapper.findComponent(ElEmpty).props('imageSize')).toBe(80)
    expect(wrapper.get('.widget-grid > .widget-empty').classes()).toContain('el-empty')
    expect(wrapper.get('.el-empty__image').attributes('style')).toContain('width: 80px')
    expect(rule('.dashboard .widget-grid .widget-empty')).toContain('grid-column: 1/-1')
    expect(rule('.dashboard .widget-grid .widget-empty')).toContain('box-sizing: border-box')
    expect(rule('.dashboard .widget-grid .widget-empty')).toContain('padding: 20px 16px')
  })

  it('从桌面切换移动端时列数和现有模块跨度同时变为 1', async () => {
    const wrapper = mountWidgets({ items: [item(8)], cols: 12 })
    expect(wrapper.get('.widget-cell').element.style.gridColumn).toBe('span 8')
    await wrapper.setProps({ isMobile: true })
    expect(wrapper.get('.widget-grid').element.style.gridTemplateColumns).toBe('repeat(1, minmax(0, 1fr))')
    expect(wrapper.get('.widget-cell').element.style.gridColumn).toBe('span 1')
    expect(wrapper.find('.widget-empty').exists()).toBe(false)
  })

  it.each([
    [0, 8, 1, 1], [-3, -2, 1, 1], [3.8, 8, 3, 3],
    [99, 99, 24, 24], [NaN, NaN, 12, 4], [Infinity, Infinity, 12, 4],
    [12, 0, 12, 1], [12, 2.9, 12, 2], [12, undefined, 12, 4]
  ])('安全限制 cols=%s、w=%s，不产生超列 span', (cols, w, expectedCols, span) => {
    const wrapper = mountWidgets({ cols, items: [item(w)] })
    expect(wrapper.get('.widget-grid').element.style.gridTemplateColumns).toBe(`repeat(${expectedCols}, minmax(0, 1fr))`)
    expect(wrapper.get('.widget-cell').element.style.gridColumn).toBe(`span ${span}`)
  })

  it('保留非空模块导航、隐藏、拖拽与 reorder', async () => {
    sortable.create.mockReturnValue({ destroy: sortable.destroy })
    const wrapper = mountWidgets({ items: [item(4)] })
    await wrapper.get('.widget-actions button').trigger('click')
    expect(wrapper.emitted('navigate')).toEqual([['/goods']])
    await wrapper.setProps({ editMode: true })
    await nextTick()
    await wrapper.get('.widget-actions button').trigger('click')
    expect(wrapper.emitted('hide-item')).toEqual([['goods']])
    const [grid, options] = sortable.create.mock.calls.at(-1)
    expect(grid).toBe(wrapper.get('.widget-grid').element)
    expect(options.handle).toBe('.drag-handle')
    expect(options.draggable).toBe('.widget-cell')
    options.onEnd()
    expect(wrapper.emitted('reorder')).toEqual([[['goods']]])
    await wrapper.setProps({ editMode: false })
    await nextTick()
    expect(sortable.destroy).toHaveBeenCalledOnce()
  })

  it.each(['2026-11-10T12:00:00', '2031-02-01T12:00:00'])('Footer 展开与折叠均使用运行时当前年：%s', async (date) => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date(date))
    const wrapper = mount(Footer)
    wrappers.push(wrapper)
    expect(wrapper.element.tagName).toBe('DIV')
    expect(wrapper.attributes('role')).toBe('contentinfo')
    expect(wrapper.find('footer, el-footer').exists()).toBe(false)
    expect(wrapper.text()).toBe(`© ${new Date().getFullYear()} system.name. system.allRightsReserved`)
    await wrapper.setProps({ isCollapse: true })
    expect(wrapper.text()).toBe(`© ${new Date().getFullYear()}`)
  })

  it('Footer scoped 样式在外层 footer 内居中，不额外增加高度、段落外边距或阴影', () => {
    const { descriptor } = parse(readFileSync(new NodeURL('./Footer.vue', import.meta.url), 'utf8'))
    expect(descriptor.styles[0].scoped).toBe(true)
    const result = compileStyle({ source: descriptor.styles[0].content, id: 'data-v-footer-test', scoped: true })
    expect(result.errors).toEqual([])
    for (const declaration of ['display: flex', 'align-items: center', 'justify-content: center', 'height: 100%', 'box-sizing: border-box', 'margin: 0']) {
      expect(result.code).toContain(declaration)
    }
    expect(result.code).not.toContain('box-shadow')
    expect(result.code).toContain('padding: 0 15px')
    expect(result.code).toContain('padding: 0 10px')
  })

  it('编译后的 hero 保持不收缩、自适应高度及原两行 clamp', () => {
    const hero = rule('.dashboard .dashboard-top-pair .dashboard-section--pair[data-id=hero] > .hero-section')
    expect(hero).toContain('flex: 1 0 auto')
    expect(hero).toContain('height: auto')
    expect(hero).toContain('align-self: stretch')
    expect(css).toContain('-webkit-line-clamp: 2')
    expect(rule('.dashboard .hero-section')).toContain('overflow: hidden')
  })

  it('防御性断点规则显式匹配列数，1100 以下保留原 block', () => {
    expect(css).toMatch(/@media \(min-width: 1280px\)\s*\{\s*\.dashboard \.dashboard-top-pair\s*\{\s*grid-template-columns: minmax\(0, 1\.45fr\) minmax\(0, 1fr\) minmax\(0, 1\.05fr\) !important/)
    expect(css).toMatch(/@media \(min-width: 769px\) and \(max-width: 1279px\)\s*\{\s*\.dashboard \.dashboard-top-pair\s*\{\s*grid-template-columns: repeat\(2, minmax\(0, 1fr\)\) !important/)
    expect(css).toMatch(/@media \(min-width: 769px\) and \(max-width: 1100px\)\s*\{\s*\.dashboard \.dashboard-top-pair\s*\{\s*display: block/)
  })
})
