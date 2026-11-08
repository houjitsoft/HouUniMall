// @vitest-environment jsdom
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { createRouter, createMemoryHistory } from 'vue-router'
import PluginCenter from './index.vue'

const rows = [
  {
    code: 'erp', name: 'ERP 进销存', description: '进销存与生产计划一体化。', version: '2.4.0',
    installed: true, enabled: true, cateId: 81, category: '进销存', terminals: ['PC', 'APP'],
    licenseType: 'FREE', licensed: true, purchaseStatus: 'AVAILABLE'
  },
  {
    code: 'webpos', name: 'WebPOS 收银台', description: '门店收银与离线销售。', version: '1.8.2',
    installed: true, enabled: false, cateId: 32, category: '零售', terminals: ['PC'],
    licenseType: 'FREE', licensed: true, purchaseStatus: 'AVAILABLE'
  },
  {
    code: 'logging', name: '日志管理', description: '操作日志与登录审计。', version: '1.2.0',
    installed: false, enabled: false, cateId: 55, category: '运维', terminals: [],
    licenseType: 'FREE', licensed: true, purchaseStatus: 'AVAILABLE'
  },
  {
    code: 'mes', name: 'MES 制造执行', description: '车间工单与工序报工。', version: '3.0.1',
    installed: false, enabled: false, cateId: 81, category: '生产制造', terminals: ['PC'],
    licenseType: 'PAID', licensed: false, purchaseStatus: 'UNLICENSED', priceText: '付费 · ¥12000/年'
  }
]

vi.mock('@/api/plugin-center/pluginCenter', () => ({
  getPluginCategories: vi.fn(async () => [{ id: 81, name: '供应链与生产' }, { id: 32, name: '门店与履约' }, { id: 55, name: '开发与运维' }]),
  getPluginSummary: vi.fn(async () => ({ total: 16, installed: 11, enabled: 9, available: 5 })),
  // 与后端 status 口径对齐：默认已安装，不把未安装目录塞进插件中心
  getPluginList: vi.fn(async (params = {}) => {
    const status = params.status ?? 'installed'
    const list = rows.filter((row) => {
      if (status === 'installed') return row.installed
      if (status === 'enabled') return row.enabled
      if (status === 'disabled') return row.installed && !row.enabled
      if (status === 'available') return !row.installed
      return true
    })
    return { list, categories: ['进销存', '零售', '运维'] }
  }),
  installPlugin: vi.fn(async () => ({})),
  uninstallPlugin: vi.fn(async () => ({})),
  togglePlugin: vi.fn(async () => ({})),
  savePluginConfig: vi.fn(async () => ({}))
}))

vi.mock('@/utils/site', () => ({ getSiteInstanceId: () => 'test.example.com' }))

vi.mock('@/components/SchemaForm.vue', () => ({
  default: { name: 'SchemaForm', props: ['modelValue', 'schema'], template: '<div class="schema-form-stub" />' }
}))

const makeRouter = async () => {
  const stub = { template: '<div />' }
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: stub },
      { path: '/platform/plugins/market', component: stub },
      { path: '/platform/plugins/market/detail', component: stub },
      { path: '/platform/plugins/licenses', component: stub },
      { path: '/platform/plugins/create', component: stub }
    ]
  })
  await router.push('/')
  await router.isReady()
  return router
}

const mountPage = async () => {
  const router = await makeRouter()
  const wrapper = mount(PluginCenter, { global: { plugins: [ElementPlus, router] } })
  await flushPromises()
  return wrapper
}

/** 弹窗由 el-dialog teleport 到 body，需挂到 document 上查询 */
const mountPageAttached = async () => {
  const router = await makeRouter()
  const wrapper = mount(PluginCenter, { attachTo: document.body, global: { plugins: [ElementPlus, router] } })
  await flushPromises()
  return wrapper
}

const findButton = (wrapper, text) => wrapper.findAll('button').find((b) => b.text().includes(text))
const dialogRoot = () => document.body.querySelector('.plugin-type-dialog')
const dialogButtons = () => Array.from(document.body.querySelectorAll('.plugin-type-dialog button'))
const clickDialogButton = async (text) => {
  const btn = dialogButtons().find((b) => b.textContent.includes(text))
  btn.click()
  await flushPromises()
}
const pickType = async (label) => {
  const options = Array.from(document.body.querySelectorAll('.plugin-type-dialog .type-option'))
  const target = options.find((o) => o.textContent.includes(label))
  target.querySelector('input').click()
  await flushPromises()
}

const texts = (wrapper, selector) => wrapper.findAll(selector).map((n) => n.text().trim())

describe('插件中心页面', () => {
  beforeEach(() => { vi.clearAllMocks() })

  it('从地址选中分类，切换分类同步地址，再次导航更新筛选', async () => {
    const router = await makeRouter()
    await router.replace('/?cateId=81')
    const wrapper = mount(PluginCenter, { global: { plugins: [ElementPlus, router] } })
    await flushPromises()
    expect(wrapper.find('.cat-tab.is-active').text()).toContain('供应链与生产')
    // 默认 status=installed：cateId=81 下已安装仅 ERP
    expect(wrapper.findAll('.plugin-card')).toHaveLength(1)
    await router.push('/?cateId=32')
    await flushPromises()
    expect(wrapper.find('.cat-tab.is-active').text()).toContain('门店与履约')
    expect(wrapper.findAll('.plugin-card')).toHaveLength(1)
    await wrapper.findAll('.cat-tab')[0].trigger('click')
    await flushPromises()
    expect(router.currentRoute.value.query.cateId).toBeUndefined()
    // 全部已安装：erp + webpos
    expect(wrapper.findAll('.plugin-card')).toHaveLength(2)
    wrapper.unmount()
  })

  it('渲染 4 张统计卡并展示 summary 数值', async () => {
    const wrapper = await mountPage()
    expect(wrapper.findAll('.summary-card')).toHaveLength(4)
    expect(texts(wrapper, '.summary-body .label')).toEqual(['插件总数', '已安装', '已启用', '可安装'])
    expect(texts(wrapper, '.summary-body .value')).toEqual(['16', '11', '9', '5'])
  })

  it('营销活动入口 entry=marketing 时隐藏 Hero 与统计条，其它入口仍显示', async () => {
    const router = await makeRouter()
    await router.replace('/?cateId=4&entry=marketing')
    const compact = mount(PluginCenter, { global: { plugins: [ElementPlus, router] } })
    await flushPromises()
    expect(compact.find('.hero').exists()).toBe(false)
    expect(compact.find('.summary-strip').exists()).toBe(false)
    expect(compact.find('.addons-page').classes()).toContain('is-compact')
    expect(compact.find('.toolbar').exists()).toBe(true)
    compact.unmount()

    await router.replace('/?cateId=4')
    const normal = mount(PluginCenter, { global: { plugins: [ElementPlus, router] } })
    await flushPromises()
    expect(normal.find('.hero').exists()).toBe(true)
    expect(normal.find('.summary-strip').exists()).toBe(true)
    expect(normal.find('.addons-page').classes()).not.toContain('is-compact')
    normal.unmount()
  })

  it('页头渲染四个跨页快捷入口（插件模块不再挂二级菜单，入口收到页头）', async () => {
    const wrapper = await mountPage()
    expect(texts(wrapper, '.hero-actions .hero-btn')).toEqual(['进入插件市场', '创建插件', '授权管理', '插件订单'])
  })

  it('工具栏展示结果计数与视图切换', async () => {
    const wrapper = await mountPage()
    expect(wrapper.find('.result-count').text()).toBe('共 2 个插件')
    expect(wrapper.findAll('.view-btn')).toHaveLength(2)
    expect(wrapper.findAll('.view-btn')[0].classes()).toContain('is-active')
  })

  it('默认只展示已安装，状态筛选项不含未安装', async () => {
    const { getPluginList } = await import('@/api/plugin-center/pluginCenter')
    const wrapper = await mountPage()
    expect(getPluginList).toHaveBeenCalledWith(expect.objectContaining({ status: 'installed' }))
    expect(texts(wrapper, '.seg-btn')).toEqual(['已安装', '已启用', '已停用'])
    expect(wrapper.findAll('.plugin-card')).toHaveLength(2)
  })

  it('卡片视图按插件状态渲染状态胶囊', async () => {
    const wrapper = await mountPage()
    expect(wrapper.findAll('.plugin-card')).toHaveLength(2)
    expect(texts(wrapper, '.plugin-card .status-pill')).toEqual(['已启用', '已停用'])
    expect(wrapper.findAll('.plugin-card')[0].find('.status-pill').classes()).toContain('on')
    expect(wrapper.findAll('.plugin-card')[1].find('.status-pill').classes()).toContain('off')
  })

  it('点击「创建插件」先弹类型选择弹窗，取消不跳转', async () => {
    const wrapper = await mountPageAttached()
    await findButton(wrapper, '创建插件').trigger('click')
    await flushPromises()

    expect(dialogRoot()).not.toBeNull()
    expect(dialogButtons().some((b) => b.textContent.includes('下一步'))).toBe(true)

    await clickDialogButton('取消')
    expect(wrapper.vm.$router.currentRoute.value.path).toBe('/')
    wrapper.unmount()
  })

  it('类型卡片说明实现位置、交付差异和共同运行边界，默认仍为 standalone', async () => {
    const wrapper = await mountPageAttached()
    await findButton(wrapper, '创建插件').trigger('click')
    await flushPromises()
    const root = dialogRoot()
    const cards = Array.from(root.querySelectorAll('.type-option'))
    expect(cards).toHaveLength(2)
    expect(cards[0].querySelector('input').checked).toBe(true)
    expect(cards[0].textContent).toContain('runtime: standalone')
    expect(cards[0].textContent).toContain('随服务发布')
    expect(cards[0].textContent).toContain('backend/paas/{hostService}/')
    expect(cards[0].textContent).toContain('不单独交付插件 JAR')
    expect(cards[1].textContent).toContain('runtime: embedded')
    expect(cards[1].textContent).toContain('独立 JAR 交付')
    expect(cards[1].textContent).toContain('plugins/{hostService}/{code}/runtime/java/')
    for (const card of cards) {
      expect(Array.from(card.querySelectorAll('dt')).map(node => node.textContent)).toEqual(['实现位置', '交付更新', '适用场景'])
    }
    expect(root.textContent).toContain('宿主就是下一步所选的已有业务服务')
    expect(root.textContent).toContain('都不会新建服务、进程或端口')
    expect(root.textContent).toContain('没有插件专属的进程隔离')
    expect(root.textContent).toContain('同进程不等于沙箱')
    expect(root.textContent).toContain('性能主要取决于业务实现')
    expect(root.textContent).toContain('不保证无重启热更新')
    await clickDialogButton('下一步')
    expect(wrapper.vm.$router.currentRoute.value.query.runtime).toBe('standalone')
    wrapper.unmount()
  })

  it('选中形态并确认后才带 runtime 跳转创建页', async () => {
    const wrapper = await mountPageAttached()
    await findButton(wrapper, '创建插件').trigger('click')
    await flushPromises()
    await pickType('内嵌扩展')
    await clickDialogButton('下一步')

    const route = wrapper.vm.$router.currentRoute.value
    expect(route.path).toBe('/platform/plugins/create')
    expect(route.query.runtime).toBe('embedded')
    wrapper.unmount()
  })

  it('已安装卡片：独立服务不展示会写 PF4J 状态的配置/启停/卸载', async () => {
    const wrapper = await mountPage()
    const cards = wrapper.findAll('.plugin-card')
    expect(cards).toHaveLength(2)
    expect(texts(cards[0], '.actions button')).toEqual([''])
    expect(texts(cards[1], '.actions button')).toEqual([''])
  })

  it('授权价格按 licenseType 着色与展示', async () => {
    const wrapper = await mountPage()
    const prices = wrapper.findAll('.plugin-card .price')
    expect(prices).toHaveLength(2)
    expect(prices[0].text()).toBe('免费')
    expect(prices[0].classes()).toContain('free')
    expect(prices[1].text()).toBe('免费')
    expect(prices[1].classes()).toContain('free')
  })

  it('分类落在卡片封面、适用端渲染为 chip', async () => {
    const wrapper = await mountPage()
    const cards = wrapper.findAll('.plugin-card')
    expect(cards[0].find('.cover-cat').text()).toBe('进销存')
    expect(texts(cards[0], '.meta-row .chip')).toEqual(['PC · APP'])
    expect(texts(cards[1], '.meta-row .chip')).toEqual(['PC'])
  })

  it('共享字典决定分类名称和顺序，按分类 ID 筛选并统计已安装目录', async () => {
    const wrapper = await mountPage()
    const tabs = wrapper.findAll('.category-tabs button')
    // 已安装仅 erp(81) + webpos(32)；运维 logging 未安装不计入
    expect(tabs.map(tab => tab.text().replace(/\s+/g, ''))).toEqual(['全部2', '供应链与生产1', '门店与履约1', '开发与运维0'])
    await tabs[1].trigger('click')
    await flushPromises()
    expect(wrapper.findAll('.plugin-card')).toHaveLength(1)
    expect(wrapper.text()).toContain('ERP 进销存')
    expect(wrapper.findAll('.plugin-card').some(card => card.text().includes('WebPOS'))).toBe(false)
  })

  it('切换到列表视图后渲染列表行并隐藏卡片', async () => {
    const wrapper = await mountPage()
    await wrapper.findAll('.view-btn')[1].trigger('click')
    await flushPromises()
    expect(wrapper.findAll('.list-row')).toHaveLength(2)
    expect(wrapper.findAll('.plugin-card')).toHaveLength(0)
    expect(wrapper.findAll('.view-btn')[1].classes()).toContain('is-active')
  })
})
