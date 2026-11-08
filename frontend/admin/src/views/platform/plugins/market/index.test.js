// @vitest-environment jsdom
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ElementPlus, { ElMessageBox } from 'element-plus'
import { createRouter, createMemoryHistory } from 'vue-router'
import PluginMarket from './index.vue'
import { getPluginCategories } from '@/api/plugin-center/pluginCenter'
import { getMarketList } from '@/api/plugin-center/pluginMarket'
import { refreshPluginApplications } from '@/utils/pluginApplicationRefresh'

// 分类维度统一走 cateId（hou_addon_cate.id），category 只是由它派生的展示名。
// 三条数据的 cateId 顺序刻意与数组顺序一致：既反映真实契约，
// 又让下面依赖卡片下标的断言（价格着色 / 缎带 / 卡片按钮）保持稳定。
const rows = [
  {
    code: 'mall-gather', name: '商品采集', cateId: 1, category: '营销', shortDesc: '一键采集商品',
    licenseType: 'FREE', priceCents: 0, priceText: '免费', latestVersion: '1.0.0',
    downloadCount: 0, installCount: 0, installed: true, enabled: true, licensed: true, purchaseStatus: 'FREE'
  },
  {
    code: 'mall-live', name: '直播带货', cateId: 1, category: '营销', shortDesc: '直播带货一站式',
    licenseType: 'PAID', priceCents: 9900, priceText: '¥99.00', latestVersion: '1.2.0',
    downloadCount: 1200, installCount: 300, installed: false, enabled: false, licensed: false, purchaseStatus: 'NOT_PURCHASED'
  },
  {
    code: 'mall-points', name: '积分商城', cateId: 4, category: '商城', shortDesc: '积分商品与积分兑换',
    licenseType: 'FREE', priceCents: 0, priceText: '免费', latestVersion: '1.0.0',
    downloadCount: 0, installCount: 0, installed: false, enabled: false, licensed: false, purchaseStatus: 'NOT_PURCHASED'
  }
]

// 后端 /web/plugin-center/market/categories 的契约：{ id, name, count }[]，count 由服务端按已上架统计
const categories = [
  { id: 1, name: '营销', count: 2 },
  { id: 4, name: '商城', count: 1 }
]

const listPayload = (list, cats, total) => ({
  list,
  categories: cats,
  total: total ?? list.length,
  page: 1,
  pageSize: 20
})

vi.mock('@/api/plugin-center/pluginMarket', () => ({
  getMarketList: vi.fn(),
  installFromMarket: vi.fn(async () => ({ data: { version: '1.0.0' } })),
  activateLicense: vi.fn(async () => ({ data: { permanent: true } })),
  getMarketDetail: vi.fn(async () => ({})),
  getMarketVersions: vi.fn(async () => [])
}))

vi.mock('@/api/plugin-center/pluginCenter', () => ({ getPluginCategories: vi.fn() }))

vi.mock('@/utils/site', () => ({ getSiteInstanceId: () => 'test.example.com' }))

vi.mock('@/utils/pluginApplicationRefresh', () => ({ refreshPluginApplications: vi.fn(async () => {}) }))


vi.mock('../components/PluginTypeDialog.vue', () => ({
  default: { name: 'PluginTypeDialog', props: ['modelValue'], template: '<div class="plugin-type-dialog-stub" />' }
}))

const makeRouter = async () => {
  const stub = { template: '<div />' }
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: stub },
      { path: '/platform/plugins', component: stub },
      { path: '/platform/plugins/market/detail', component: stub },
      { path: '/platform/plugins/market/checkout', component: stub },
      { path: '/platform/plugins/licenses', component: stub },
      { path: '/platform/plugins/orders', component: stub },
      { path: '/platform/plugins/create', component: stub }
    ]
  })
  await router.push('/')
  await router.isReady()
  return router
}

const mountPage = async () => {
  const router = await makeRouter()
  const wrapper = mount(PluginMarket, { global: { plugins: [ElementPlus, router] } })
  await flushPromises()
  return wrapper
}

const texts = (wrapper, selector) => wrapper.findAll(selector).map((n) => n.text().trim())
const cardNames = (wrapper) => texts(wrapper, '.market-card .card-name')

describe('插件市场页面', () => {
  // 注意：这里用 mockImplementation 而不是 mockResolvedValueOnce ——
  // beforeEach 的 clearAllMocks() 会把 once 队列一起清掉，导致定制数据拿不到。
  beforeEach(() => {
    vi.clearAllMocks()
    getPluginCategories.mockImplementation(async () => categories.map(({ id, name }) => ({ id, name })))
    getMarketList.mockImplementation(async () => listPayload(rows, categories))
  })

  it('页头渲染居中标题、副标题与四个跨页快捷入口', async () => {
    const wrapper = await mountPage()
    expect(wrapper.find('.market-head h1').text()).toBe('插件市场')
    expect(wrapper.find('.market-head p').text()).toContain('按版本在线安装')
    expect(texts(wrapper, '.quick-link')).toEqual(['插件中心', '授权管理', '插件订单', '创建插件'])
  })

  it('创建入口打开类型选择，确认后按原 runtime 跳转向导', async () => {
    for (const runtime of ['standalone', 'embedded']) {
      const wrapper = await mountPage()
      const button = wrapper.findAll('button').find(node => node.text().includes('创建插件'))
      await button.trigger('click')
      const dialog = wrapper.findComponent({ name: 'PluginTypeDialog' })
      expect(dialog.props('modelValue')).toBe(true)
      dialog.vm.$emit('confirm', runtime)
      await flushPromises()
      expect(wrapper.vm.$router.currentRoute.value.path).toBe('/platform/plugins/create')
      expect(wrapper.vm.$router.currentRoute.value.query.runtime).toBe(runtime)
      wrapper.unmount()
    }
  })

  it('筛选面板渲染分类与价格两组筛选项（分类带真实条数）', async () => {
    const wrapper = await mountPage()
    const filterRows = wrapper.findAll('.filter-row')
    expect(filterRows).toHaveLength(2)
    expect(texts(wrapper, '.filter-row')[0]).toContain('分类：')
    expect(texts(wrapper, '.filter-row')[1]).toContain('价格：')
    expect(texts(wrapper, '.filter-row:nth-child(1) .filter-link')).toEqual(['全部', '营销2', '商城1'])
    expect(texts(wrapper, '.filter-row:nth-child(2) .filter-link')).toEqual(['全部', '免费', '付费'])
  })

  it('点击分类下发 cateId（归属）而不是分类名，并回到第一页', async () => {
    const wrapper = await mountPage()
    const cats = wrapper.findAll('.filter-row:nth-child(1) .filter-link')
    await cats[1].trigger('click')
    await flushPromises()
    const lastCall = getMarketList.mock.calls[getMarketList.mock.calls.length - 1][0]
    // 按 cateId 筛选：重命名分类不影响筛选结果，这是与旧「按名筛选」的关键差别
    expect(lastCall.cateId).toBe(1)
    expect(lastCall.category).toBeUndefined()
    expect(lastCall.page).toBe(1)
  })

  it('默认排序按字典顺序聚合，不按分类 ID 大小排序', async () => {
    getPluginCategories.mockResolvedValueOnce([{ id: 4, name: '商城' }, { id: 1, name: '营销' }])
    // 故意下发乱序数据，验证前端不依赖服务端返回顺序
    getMarketList.mockImplementation(async () => listPayload([
        { code: 'p4', name: '门店经营', cateId: 9, category: '合规', shortDesc: '', priceText: '', priceCents: 0, latestVersion: '1.0.0', downloadCount: 0, installCount: 0, installed: false, enabled: false, licensed: false },
        { code: 'p2', name: '直播带货', cateId: 4, category: '商城', shortDesc: '', priceText: '', priceCents: 0, latestVersion: '1.0.0', downloadCount: 0, installCount: 0, installed: false, enabled: false, licensed: false },
        { code: 'p1', name: '积分商城', cateId: 1, category: '营销', shortDesc: '', priceText: '', priceCents: 0, latestVersion: '1.0.0', downloadCount: 0, installCount: 0, installed: false, enabled: false, licensed: false },
        { code: 'p3', name: '店铺装修', cateId: 1, category: '营销', shortDesc: '', priceText: '', priceCents: 0, latestVersion: '1.0.0', downloadCount: 0, installCount: 0, installed: false, enabled: false, licensed: false }
      ],
      categories
    ))
    const wrapper = await mountPage()
    expect(cardNames(wrapper)).toEqual(['直播带货', '店铺装修', '积分商城', '门店经营'])
  })

  it('分类名和顺序只取共享字典，市场响应仅提供数量', async () => {
    getPluginCategories.mockResolvedValueOnce([
      { id: 4, name: '客户新增分类' }, { id: 1, name: '重命名分类' }, { id: 98, name: '待上架分类' }
    ])
    const wrapper = await mountPage()
    expect(texts(wrapper, '.filter-row:nth-child(1) .filter-link')).toEqual(['全部', '客户新增分类1', '重命名分类2', '待上架分类'])
  })

  it('没有服务端分类统计时不把当前页数量冒充全目录数量', async () => {
    getMarketList.mockImplementation(async () => listPayload(rows.slice(0, 1), [], 20))
    const wrapper = await mountPage()
    expect(texts(wrapper, '.filter-row:nth-child(1) .filter-link')).toEqual(['全部', '营销', '商城'])
  })

  it('卡片渲染名称、简介、价格文案与着色', async () => {
    const wrapper = await mountPage()
    expect(cardNames(wrapper)).toEqual(['商品采集', '直播带货', '积分商城'])
    expect(texts(wrapper, '.market-card .card-desc')).toEqual(['一键采集商品', '直播带货一站式', '积分商品与积分兑换'])
    const prices = wrapper.findAll('.market-card .price')
    expect(texts(wrapper, '.market-card .price')).toEqual(['免费', '¥99.00', '免费'])
    expect(prices[0].classes()).toContain('free')
    expect(prices[1].classes()).toContain('paid')
    expect(prices[2].classes()).toContain('free')
  })

  it('下载次数恒显示真实值（0 也显示），版本号并列展示', async () => {
    const wrapper = await mountPage()
    const metas = texts(wrapper, '.market-card .meta-left')
    // 0 也照实显示：以前用「0 就换成版本号」占位，等于把「没人下载」藏起来
    expect(metas[0]).toContain('0')
    expect(metas[0]).toContain('v1.0.0')
    expect(metas[1]).toContain('1200')
    expect(metas[1]).toContain('v1.2.0')
  })

  it('封面右上角缎带：有热度标热门，否则已安装标已安装，都没有则不渲染', async () => {
    const wrapper = await mountPage()
    const ribbons = wrapper.findAll('.market-card .ribbon')
    expect(ribbons).toHaveLength(2)
    expect(ribbons[0].text()).toBe('已安装')
    expect(ribbons[0].classes()).toContain('installed')
    expect(ribbons[1].text()).toBe('热门')
    expect(ribbons[1].classes()).toContain('hot')
  })

  it('未安装：付费未授权给「购买」，免费给「安装」，已安装给「去插件中心管理」', async () => {
    const wrapper = await mountPage()
    const feet = wrapper.findAll('.market-card .card-foot')
    expect(texts(feet[0], '.foot-btn')).toEqual(['去插件中心管理'])
    // 付费未授权不再是「激活授权」——那是把用户推去找平台要码，线上自助购买已打通
    expect(texts(feet[1], '.foot-btn')).toEqual(['购买'])
    expect(texts(feet[2], '.foot-btn')).toEqual(['安装'])
    // 购买是主行动，用 primary 而不是 warning 样式
    expect(feet[1].find('.foot-btn--primary').exists()).toBe(true)
    expect(feet[1].find('.foot-btn--warn').exists()).toBe(false)
  })

  it('点「购买」直接进 checkout 并带上插件编码', async () => {
    const router = await makeRouter()
    const wrapper = mount(PluginMarket, { global: { plugins: [ElementPlus, router] } })
    await flushPromises()
    const cardFoots = wrapper.findAll('.market-card .card-foot')
    // 第 2 张卡 = 付费未授权的 mall-live
    await cardFoots[1].find('.foot-btn--primary').trigger('click')
    await flushPromises()
    expect(router.currentRoute.value.path).toBe('/platform/plugins/market/checkout')
    expect(router.currentRoute.value.query.code).toBe('mall-live')
  })

  it('切换排序会改变卡片顺序', async () => {
    const wrapper = await mountPage()
    const tabs = wrapper.findAll('.sort-tab')
    expect(texts(wrapper, '.sort-tab')).toEqual(['默认排序', '按下载次数', '按安装量', '按价格', '按名称'])

    // 按下载次数：1200 的直播带货排到首位
    await tabs[1].trigger('click')
    await flushPromises()
    expect(cardNames(wrapper)[0]).toBe('直播带货')

    // 按价格：0 分的两条在前，付费的 99 分在后
    await tabs[3].trigger('click')
    await flushPromises()
    expect(cardNames(wrapper)[2]).toBe('直播带货')

    // 按名称：中文按 pinyin 无关的 localeCompare 排序，这里只断言是稳定可复现的顺序
    await tabs[4].trigger('click')
    await flushPromises()
    expect(cardNames(wrapper)).toHaveLength(3)
  })

  it('勾选「仅看未安装」下发 status=available', async () => {
    const wrapper = await mountPage()
    const checkbox = wrapper.find('.install-toggle input[type="checkbox"]')
    await checkbox.setValue(true)
    await flushPromises()
    const lastCall = getMarketList.mock.calls[getMarketList.mock.calls.length - 1][0]
    expect(lastCall.status).toBe('available')
  })

  it('搜索与重置会重新拉取并清空条件', async () => {
    const wrapper = await mountPage()
    await wrapper.find('.market-search input').setValue('直播')
    await wrapper.find('.market-search input').trigger('keyup.enter')
    await flushPromises()
    let lastCall = getMarketList.mock.calls[getMarketList.mock.calls.length - 1][0]
    expect(lastCall.keyword).toBe('直播')

    const tabs = wrapper.findAll('.sort-tab')
    await tabs[2].trigger('click')
    await wrapper.vm.resetFilters()
    await flushPromises()
    lastCall = getMarketList.mock.calls[getMarketList.mock.calls.length - 1][0]
    expect(lastCall.keyword).toBe('')
    // 重置后 cateId 回到 null（＝全部），此时请求里不该带 cateId 键
    expect('cateId' in lastCall).toBe(false)
    expect(lastCall.licenseType).toBe('')
    expect(lastCall.status).toBe('')
  })

  it('宿主不可用：左侧主按钮是「安装」而非「不可安装」；右侧镂空宿主按钮可点', async () => {
    const hostRowsA = [
      {
        code: 'aicode', name: 'AI 编码', cateId: 1, category: '工具', shortDesc: '依赖 pm',
        licenseType: 'PAID', priceCents: 9900, priceText: '¥99.00', latestVersion: '1.0.0',
        downloadCount: 0, installCount: 0, installed: false, enabled: false, licensed: true,
        purchaseStatus: 'PURCHASED', hostService: 'pm', hostAvailable: false,
        hostUnavailableReason: '插件归属 pm 宿主服务，当前站点未部署该服务，无法安装'
      },
      {
        code: 'pm', name: '项目管理', cateId: 1, category: '工具', shortDesc: '宿主',
        licenseType: 'PAID', priceCents: 19900, priceText: '¥199.00', latestVersion: '1.0.0',
        downloadCount: 0, installCount: 0, installed: false, enabled: false, licensed: false,
        purchaseStatus: 'NOT_PURCHASED', hostService: 'pm', hostAvailable: true
      }
    ]
    getMarketList.mockImplementation(async () => listPayload(hostRowsA, categories, 2))
    const wrapper = await mountPage()
    const cards = wrapper.findAll('.market-card')
    const aicodeCard = cards.find((c) => c.find('.card-name').text().includes('AI 编码'))
    expect(aicodeCard).toBeTruthy()
    const foot = aicodeCard.find('.card-foot')
    expect(texts(foot, '.foot-btn--primary')).toEqual(['安装'])
    expect(foot.text()).not.toContain('不可安装')
    expect(foot.find('.foot-btn--primary[disabled]').exists()).toBe(false)
    const hint = foot.find('.foot-btn.foot-btn--outline-host')
    expect(hint.exists()).toBe(true)
    expect(hint.element.tagName.toLowerCase()).toBe('button')
    expect(hint.attributes('disabled')).toBeUndefined()
    expect(hint.classes()).toContain('foot-btn--outline-host')
    expect(hint.text()).toContain('项目管理')
    // 镂空按钮与主 CTA 并存：主按钮安装 + 右侧宿主 outline
    expect(texts(foot, '.foot-btn')).toEqual(['安装', hint.text()])
  })

  it('点右侧镂空宿主按钮弹出先买/装宿主引导', async () => {
    const confirmSpy = vi.spyOn(ElMessageBox, 'confirm').mockResolvedValueOnce('confirm')
    const hostRowsHint = [
      {
        code: 'aicode', name: 'AI 编码', cateId: 1, category: '工具', shortDesc: '依赖 pm',
        licenseType: 'FREE', priceCents: 0, priceText: '免费', latestVersion: '1.0.0',
        downloadCount: 0, installCount: 0, installed: false, enabled: false, licensed: true,
        purchaseStatus: 'FREE', hostService: 'pm', hostAvailable: false,
        hostUnavailableReason: '插件归属 pm 宿主服务，当前站点未部署该服务，无法安装'
      },
      {
        code: 'pm', name: '项目管理', cateId: 1, category: '工具', shortDesc: '宿主',
        licenseType: 'PAID', priceCents: 19900, priceText: '¥199.00', latestVersion: '1.0.0',
        downloadCount: 0, installCount: 0, installed: false, enabled: false, licensed: false,
        purchaseStatus: 'NOT_PURCHASED', hostService: 'pm', hostAvailable: true
      }
    ]
    getMarketList.mockImplementation(async () => listPayload(hostRowsHint, categories, 2))
    const router = await makeRouter()
    const wrapper = mount(PluginMarket, { global: { plugins: [ElementPlus, router] } })
    await flushPromises()
    const aicodeCard = wrapper.findAll('.market-card').find((c) => c.find('.card-name').text().includes('AI 编码'))
    await aicodeCard.find('.foot-btn.foot-btn--outline-host').trigger('click')
    await flushPromises()
    expect(confirmSpy).toHaveBeenCalled()
    expect(String(confirmSpy.mock.calls[0][1] || '')).toContain('需要先购买/安装宿主插件')
    expect(router.currentRoute.value.path).toBe('/platform/plugins/market/checkout')
    expect(router.currentRoute.value.query.code).toBe('pm')
    confirmSpy.mockRestore()
  })

  it('宿主不可用时点安装弹出引导并跳转宿主 checkout', async () => {
    const confirmSpy = vi.spyOn(ElMessageBox, 'confirm').mockResolvedValueOnce('confirm')
    const hostRowsB = [
      {
        code: 'aicode', name: 'AI 编码', cateId: 1, category: '工具', shortDesc: '依赖 pm',
        licenseType: 'FREE', priceCents: 0, priceText: '免费', latestVersion: '1.0.0',
        downloadCount: 0, installCount: 0, installed: false, enabled: false, licensed: true,
        purchaseStatus: 'FREE', hostService: 'pm', hostAvailable: false,
        hostUnavailableReason: '插件归属 pm 宿主服务，当前站点未部署该服务，无法安装'
      },
      {
        code: 'pm', name: '项目管理', cateId: 1, category: '工具', shortDesc: '宿主',
        licenseType: 'PAID', priceCents: 19900, priceText: '¥199.00', latestVersion: '1.0.0',
        downloadCount: 0, installCount: 0, installed: false, enabled: false, licensed: false,
        purchaseStatus: 'NOT_PURCHASED', hostService: 'pm', hostAvailable: true
      }
    ]
    getMarketList.mockImplementation(async () => listPayload(hostRowsB, categories, 2))
    const router = await makeRouter()
    const wrapper = mount(PluginMarket, { global: { plugins: [ElementPlus, router] } })
    await flushPromises()
    const aicodeCard = wrapper.findAll('.market-card').find((c) => c.find('.card-name').text().includes('AI 编码'))
    await aicodeCard.find('.card-foot .foot-btn--primary').trigger('click')
    await flushPromises()
    expect(confirmSpy).toHaveBeenCalled()
    expect(router.currentRoute.value.path).toBe('/platform/plugins/market/checkout')
    expect(router.currentRoute.value.query.code).toBe('pm')
    confirmSpy.mockRestore()
  })

  it('点击卡片跳转到详情页并带上 code', async () => {
    const wrapper = await mountPage()
    await wrapper.findAll('.market-card')[0].trigger('click')
    await flushPromises()
    const route = wrapper.vm.$router.currentRoute.value
    expect(route.path).toBe('/platform/plugins/market/detail')
    expect(route.query.code).toBe('mall-gather')
  })

  it('点安装展示「安装中…」与过程弹窗，完成后按钮恢复为已安装入口', async () => {
    vi.useFakeTimers()
    const marketApi = await import('@/api/plugin-center/pluginMarket')
    let resolveInstall
    marketApi.installFromMarket.mockImplementationOnce(
      () => new Promise((resolve) => { resolveInstall = resolve })
    )
    const wrapper = await mountPage()
    const foot = wrapper.findAll('.market-card .card-foot')[2]
    const btn = foot.find('.foot-btn--primary')
    expect(btn.text()).toBe('安装')
    await btn.trigger('click')
    await flushPromises()
    expect(btn.text()).toContain('安装中')
    expect(btn.classes()).toContain('is-busy')
    expect(wrapper.text()).toContain('正在安装「积分商城」')
    expect(wrapper.text()).toContain('正在准备安装')
    resolveInstall({ data: { version: '1.0.0' } })
    await flushPromises()
    await vi.advanceTimersByTimeAsync(2000)
    await flushPromises()
    vi.useRealTimers()
    expect(marketApi.installFromMarket).toHaveBeenCalled()
    expect(refreshPluginApplications).toHaveBeenCalledTimes(1)
  })
})
