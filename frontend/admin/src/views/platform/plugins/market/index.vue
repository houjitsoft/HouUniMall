<template>
  <div class="market-page">
    <!-- ── 页头：居中标题 + 跨页快捷入口（二级菜单已移除，入口收到页面内） ── -->
    <header class="market-head">
      <nav class="quick-nav" aria-label="插件模块快捷入口">
        <button type="button" class="quick-link" @click="goAddons">
          <el-icon><Grid /></el-icon>插件中心
        </button>
        <button type="button" class="quick-link" @click="goLicenses">
          <el-icon><Key /></el-icon>授权管理
        </button>
        <button type="button" class="quick-link" @click="goOrders">
          <el-icon><Tickets /></el-icon>插件订单
        </button>
        <button type="button" class="quick-link quick-link--primary" @click="goCreate">
          <el-icon><Plus /></el-icon>创建插件
        </button>
      </nav>
      <h1>插件市场</h1>
      <p>浏览平台已上架的商城插件，按版本在线安装；已安装的插件回到「插件中心」统一管理启停与配置。</p>
    </header>

    <!-- ── 筛选面板：分类 / 价格 ── -->
    <section class="filter-panel">
      <div class="filter-row">
        <span class="filter-label">分类：</span>
        <div class="filter-links">
          <!--
            分类维度统一按 cateId（hou_addon_cate.id）工作：
            此前按分类「名」字符串筛选，而市场数据里的分类文案与分类字典零重叠
            （营销工具 / 内容营销 … vs 营销 / 工具 …），于是 Tab 与列表各说各话。
            现在 Tab 的 id / name / count 全部由后端分类字典下发，未归类插件落到
            id=0 的「未分类」桶，不再依赖列表里的自由文本。
          -->
          <button
            type="button"
            class="filter-link"
            :class="{ 'is-active': query.cateId === null }"
            @click="setCategory(null)"
          >
            全部
          </button>
          <button
            v-for="cat in categoryOptions"
            :key="cat.id"
            type="button"
            class="filter-link"
            :class="{ 'is-active': query.cateId === cat.id }"
            @click="setCategory(cat.id)"
          >
            {{ cat.name }}<em v-if="cat.count !== null">{{ cat.count }}</em>
          </button>
        </div>
      </div>

      <div class="filter-row">
        <span class="filter-label">价格：</span>
        <div class="filter-links">
          <button
            v-for="opt in priceOptions"
            :key="opt.value"
            type="button"
            class="filter-link"
            :class="{ 'is-active': query.licenseType === opt.value }"
            @click="setLicenseType(opt.value)"
          >
            {{ opt.label }}
          </button>
        </div>
      </div>
    </section>

    <!-- ── 排序 / 工具条 ── -->
    <section class="sort-bar">
      <div class="sort-tabs" role="group" aria-label="排序方式">
        <button
          v-for="opt in sortOptions"
          :key="opt.value"
          type="button"
          class="sort-tab"
          :class="{ 'is-active': sortKey === opt.value }"
          @click="sortKey = opt.value"
        >
          {{ opt.label }}
        </button>
      </div>

      <label class="install-toggle">
        <el-checkbox
          :model-value="query.status === 'available'"
          @change="setOnlyUninstalled"
        >
          仅看未安装
        </el-checkbox>
      </label>

      <span class="sort-spacer" />

      <span class="result-count">共 {{ total }} 个插件</span>
      <el-input
        v-model="query.keyword"
        class="market-search"
        :prefix-icon="Search"
        clearable
        placeholder="输入关键词搜索"
        @keyup.enter="search"
        @clear="search"
      />
      <el-button :icon="Refresh" :loading="loading" title="刷新" @click="loadList" />
    </section>

    <!-- ── 插件货架 ── -->
    <section class="market-grid">
      <template v-if="loading">
        <div v-for="n in 8" :key="`sk-${n}`" class="market-card is-skeleton">
          <div class="card-media sk-block" />
          <div class="card-info">
            <div class="sk-line" style="width: 62%" />
            <div class="sk-line sk-line--sm" style="width: 88%" />
            <div class="sk-line sk-line--sm" style="width: 44%" />
          </div>
        </div>
      </template>

      <template v-else>
        <article
          v-for="item in visiblePlugins"
          :key="item.code"
          class="market-card"
          :title="`查看「${item.name}」详情`"
          @click="goDetail(item)"
        >
          <div class="card-media" :style="mediaStyle(item)">
            <span class="media-deco media-deco--a" aria-hidden="true" />
            <span class="media-deco media-deco--b" aria-hidden="true" />
            <span class="media-icon" :style="iconTone(item)">
              <img v-if="item.iconUrl" :src="item.iconUrl" :alt="item.name" />
              <span v-else>{{ iconText(item) }}</span>
            </span>
            <span v-if="item.category" class="media-cat">{{ item.category }}</span>
            <span v-if="badgeOf(item)" class="ribbon" :class="badgeOf(item).tone">
              <i>{{ badgeOf(item).text }}</i>
            </span>
          </div>

          <div class="card-info">
            <h3 class="card-name" :title="item.name">{{ item.name }}</h3>
            <p class="card-desc">{{ item.shortDesc || item.description || '暂无简介' }}</p>
            <div class="card-meta">
              <span class="meta-left">
                <!-- 下载次数：平台统一存储的真实值（全网安装人次），0 也照实显示，
                     不再用版本号占位——那会让「没人下载」和「有下载」看起来一样 -->
                <el-icon><Download /></el-icon>{{ Number(item.downloadCount) || 0 }}
                <em class="meta-sep">·</em>v{{ item.latestVersion || '-' }}
              </span>
              <span class="meta-right">
                <span class="price" :class="priceClass(item)">{{ priceText(item) }}</span>
              </span>
            </div>
          </div>

          <div class="card-foot" @click.stop>
            <template v-if="!item.installed">
              <!-- 付费未授权：购买 + 授权码安装并排，避免小白只看到「去管理」却无处下手。 -->
              <template v-if="installBlocked(item)">
                <button
                  type="button"
                  class="foot-btn foot-btn--primary"
                  :disabled="!!installing"
                  @click="onPurchaseClick(item)"
                >
                  购买
                </button>
                <button
                  type="button"
                  class="foot-btn foot-btn--outline-license"
                  :disabled="!!installing || activating === item.code"
                  @click="onActivateLicenseClick(item)"
                >
                  {{ activating === item.code ? '激活中…' : '授权码安装' }}
                </button>
              </template>
              <!-- 宿主不可用：左侧仍显示正常「安装」（禁止「不可安装」灰按钮）；
                   点安装或点右侧红字依赖提示，都会弹窗引导先买/装宿主。 -->
              <button
                v-else-if="hostUnavailable(item)"
                type="button"
                class="foot-btn foot-btn--primary"
                :class="{ 'is-busy': installing === item.code }"
                :disabled="!!installing"
                :title="item.hostUnavailableReason || '需要先购买/安装宿主插件'"
                @click="onInstallClick(item)"
              >
                <span v-if="installing === item.code" class="foot-btn-spinner" aria-hidden="true" />
                {{ installing === item.code ? '安装中…' : '安装' }}
              </button>
              <button
                v-else
                type="button"
                class="foot-btn foot-btn--primary"
                :class="{ 'is-busy': installing === item.code }"
                :disabled="!!installing"
                @click="onInstallClick(item)"
              >
                <span v-if="installing === item.code" class="foot-btn-spinner" aria-hidden="true" />
                {{ installing === item.code ? '安装中…' : '安装' }}
              </button>
            </template>
            <button v-else type="button" class="foot-btn" @click="goAddons">
              去插件中心管理
            </button>
            <span v-if="needsLicense(item) && item.licensed" class="foot-hint is-ok">
              已购买
            </span>
            <!-- 宿主依赖提示：右侧镂空按钮，明确可点；打开「先买/装宿主」引导；左侧主按钮仍是购买/安装文案 -->
            <button
              v-if="hostUnavailable(item)"
              type="button"
              class="foot-btn foot-btn--outline-host"
              :title="item.hostUnavailableReason || '点击查看如何购买/安装宿主插件'"
              @click.stop="onHostHintClick(item)"
            >
              {{ hostHintText(item, plugins) }}
            </button>
          </div>
        </article>
      </template>

      <div v-if="!loading && !visiblePlugins.length" class="empty-block">
        <svg viewBox="0 0 120 96" width="120" height="96" aria-hidden="true">
          <rect x="10" y="26" width="44" height="44" rx="12" fill="#e8eefb" />
          <rect x="60" y="18" width="44" height="44" rx="12" fill="#dbeafe" />
          <rect x="34" y="52" width="44" height="44" rx="12" fill="#f1f5f9" />
          <path d="M28 48h16M36 40v16" stroke="#94a3b8" stroke-width="3" stroke-linecap="round" />
        </svg>
        <p class="empty-title">没有找到符合条件的插件</p>
        <p class="empty-desc">换个关键词、清空筛选，或联系平台上架你需要的扩展能力。</p>
        <div class="empty-actions">
          <el-button size="small" @click="resetFilters">清空筛选</el-button>
        </div>
      </div>
    </section>

    <el-pagination
      v-if="total > pageSize"
      v-model:current-page="page"
      v-model:page-size="pageSize"
      class="market-pagination"
      background
      layout="total, prev, pager, next, sizes"
      :total="total"
      :page-sizes="[12, 20, 40, 80]"
      @current-change="loadList"
      @size-change="handlePageSizeChange"
    />

    <p class="market-note">
      免费插件点「安装」即下载并落盘；付费插件点「购买」在线支付成功后自动签发授权并继续下载安装，
      无需先找平台要授权码。已经在别处拿到授权码的，到
      <el-button link type="primary" size="small" @click="goLicenses">授权管理</el-button>
      激活即可。
    </p>

    <plugin-type-dialog v-model="typeDialogVisible" @confirm="handleRuntimeConfirm" />

    <!-- 安装过程：接口很快时也要有可见步骤，避免「一点就成功」像闪一下 bug -->
    <el-dialog
      v-model="installProgressVisible"
      :title="installProgressTitle"
      width="440px"
      align-center
      :append-to-body="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      class="install-progress-dialog"
    >
      <el-steps :active="installProgressStep" finish-status="success" align-center>
        <el-step title="准备" />
        <el-step title="下载校验" />
        <el-step title="装载激活" />
        <el-step title="完成" />
      </el-steps>
      <p class="install-progress-tip">{{ installProgressTip }}</p>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Download, Grid, Key, Plus, Refresh, Search, Tickets } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { isStandaloneService, installServiceAndWait } from '@/api/plugin-center/service'
import { activateLicense, getMarketList, installFromMarket } from '@/api/plugin-center/pluginMarket'
import { getSiteInstanceId } from '@/utils/site'
import { getPluginCategories } from '@/api/plugin-center/pluginCenter'
import PluginTypeDialog from '../components/PluginTypeDialog.vue'
import {
  hostUnavailable,
  hostHintText,
  promptHostRequired
} from './hostRequiredPrompt'
import { refreshPluginApplications } from '@/utils/pluginApplicationRefresh'

/** 本站点实例标识：授权以「绑定实例」做站点隔离，统一由当前访问域名派生，不允许手填 */
const siteInstanceId = getSiteInstanceId()

const router = useRouter()

const loading = ref(false)
const plugins = ref([])
const categories = ref([])
const categoryCounts = ref([])
const installing = ref('')
const activating = ref('')
const installTarget = ref(null)
const installVersion = ref('')
/** 安装过程弹窗：步骤文案随请求推进，并保证最短展示时间 */
const installProgressVisible = ref(false)
const installProgressTitle = ref('正在安装')
const installProgressStep = ref(0)
const installProgressTip = ref('')
const INSTALL_PROGRESS_TIPS = [
  '正在准备安装…',
  '正在下载并校验安装包…',
  '正在向宿主服务装载并激活…',
  '安装完成，正在同步右上角应用入口…'
]
/** 最短可见过程，避免接口过快时用户感觉「闪一下」 */
const INSTALL_PROGRESS_MIN_MS = 1600
const INSTALL_PROGRESS_TICK_MS = 480
/**
 * 分类维度：一律用 cateId（分类外键）而不是分类名。
 *
 * 为什么改：分类名是展示值，会随后台重命名变化；而市场历史数据的 category 又是
 * 自由文本（营销工具 / 内容营销 …），与分类字典（营销 / 工具 …）零重叠，
 * 于是「点 Tab 筛不出东西」。cateId 是归属，重命名分类不影响筛选。
 *
 * cateId = null 表示「全部」；0 表示「未分类」兜底桶。
 */
const query = reactive({ keyword: '', cateId: null, licenseType: '', status: '' })
/** 排序在前端做：市场目录体量小（当前个位数），排序切换无需再打一次接口 */
const sortKey = ref('default')
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const priceOptions = [
  { label: '全部', value: '' },
  { label: '免费', value: 'FREE' },
  { label: '付费', value: 'PAID' }
]

/**
 * 排序项按「当前数据真的支持」来给，不摆空架子：
 * 下载量、安装量、价格、名称都是市场条目自带的字段，可直接排。
 */
const sortOptions = [
  { label: '默认排序', value: 'default' },
  { label: '按下载次数', value: 'download' },
  { label: '按安装量', value: 'install' },
  { label: '按价格', value: 'price' },
  { label: '按名称', value: 'name' }
]

/** 付费插件：必须先激活授权码才能安装 */
const needsLicense = (item) => String(item?.licenseType || '').toUpperCase() === 'PAID'
const installBlocked = (item) => needsLicense(item) && !item?.licensed

/**
 * 封面底色用色：与插件中心同一套（同一插件跨页配色一致）。
 * 刻意压深一档、避开霓虹粉彩 —— 货架页一眼看过去要像「商品图」，不能像糖果色块。
 */
const palette = [
  ['#6E9BF5', '#1D4ED8'],
  ['#34C6A8', '#0F766E'],
  ['#F0B03C', '#B45309'],
  ['#9B85F0', '#5B21B6'],
  ['#3FC4DE', '#0E7490'],
  ['#F08098', '#9F1239']
]
const hashOf = (code = '') => [...String(code)].reduce((acc, ch) => acc + ch.charCodeAt(0), 0)
const iconText = (item) => String(item?.name || '?').trim().charAt(0)
/** 封面底色：饱和渐变，作为插件「商品图」的底；同编码颜色稳定 */
const mediaStyle = (item) => {
  const [from, to] = palette[hashOf(item?.code) % palette.length]
  return { backgroundImage: `linear-gradient(135deg, ${from} 0%, ${to} 100%)` }
}
/** 封面中央白色 App 图标块里的首字取同色系的深色端，保证对比度 */
const iconTone = (item) => {
  const [, to] = palette[hashOf(item?.code) % palette.length]
  return { color: to }
}
const priceClass = (item) => {
  const type = String(item?.licenseType || 'FREE').toUpperCase()
  if (type === 'PAID') return 'paid'
  return 'free'
}
const priceText = (item) => item?.priceText || (needsLicense(item) ? '未定价' : '免费')

/**
 * 封面右上角缎带。优先展示「热门」，其次展示「已安装」。
 *
 * 热门依赖真实热度（下载量 / 安装量 / 平台标注的 热门 标签），
 * 平台目前还没有沉淀热度数据，所以线上现在会稳定落到「已安装」——
 * 这是真实状态，比硬编码一个假的「热门」诚实；等热度数据上来会自动切换。
 */
const badgeOf = (item) => {
  const tags = item?.tags || []
  const downloads = Number(item?.downloadCount) || 0
  const installs = Number(item?.installCount) || 0
  if (tags.includes('热门') || downloads >= 1000 || installs >= 100) {
    return { text: '热门', tone: 'hot' }
  }
  if (item?.installed) return { text: '已安装', tone: 'installed' }
  return null
}

/** 分类名称和顺序统一取插件中心字典；市场统计只提供数量。 */
const categoryOptions = computed(() => {
  const counts = new Map(categoryCounts.value.map(c => [Number(c.id), Number(c.count)]))
  const tabs = categories.value.map(c => ({ id: Number(c.id), name: c.name, count: Number.isFinite(counts.get(Number(c.id))) ? counts.get(Number(c.id)) : null }))
  if (counts.has(0) && counts.get(0) > 0) tabs.push({ id: 0, name: '未分类', count: counts.get(0) })
  return tabs
})
const loadCategories = async () => {
  try { categories.value = await getPluginCategories() }
  catch (error) { ElMessage.error(error?.message || '获取插件分类失败') }
}

/**
 * 排序在前端完成的理由：市场目录当前是个位数条目、单页即可装下，
 * 切换排序不必再往返一次接口。**若目录增长到多页**，这里的排序只会作用于当前页，
 * 那时应把 sortKey 下发给 `/web/plugin-center/market/list` 由服务端排序。
 */
const visiblePlugins = computed(() => {
  const list = [...plugins.value]
  const num = (v) => Number(v) || 0
  switch (sortKey.value) {
    case 'download':
      return list.sort((a, b) => num(b.downloadCount) - num(a.downloadCount) || num(b.installCount) - num(a.installCount))
    case 'install':
      return list.sort((a, b) => num(b.installCount) - num(a.installCount) || num(b.downloadCount) - num(a.downloadCount))
    case 'price':
      return list.sort((a, b) => num(a.priceCents) - num(b.priceCents) || String(a.name || '').localeCompare(String(b.name || ''), 'zh'))
    case 'name':
      return list.sort((a, b) => String(a.name || '').localeCompare(String(b.name || ''), 'zh'))
    default:
      // ID 与排序独立；按分类字典返回的顺序分组。
      const categoryOrder = new Map(categories.value.map((category, index) => [Number(category.id), index]))
      return list.sort(
        (a, b) =>
          (categoryOrder.get(Number(a?.cateId)) ?? Number.MAX_SAFE_INTEGER) - (categoryOrder.get(Number(b?.cateId)) ?? Number.MAX_SAFE_INTEGER) ||
          String(a?.name || '').localeCompare(String(b?.name || ''), 'zh')
      )
  }
})

const loadList = async () => {
  loading.value = true
  try {
    const data = await getMarketList({
      keyword: query.keyword,
      // 分类筛选走 cateId（归属）；cateId 为 null 表示「全部」，此时不下发该键
      ...(query.cateId === null ? {} : { cateId: query.cateId }),
      licenseType: query.licenseType,
      status: query.status,
      page: page.value,
      pageSize: pageSize.value,
      instanceId: siteInstanceId
    })
    categoryCounts.value = data?.categories ?? []
    plugins.value = data?.list ?? []
    total.value = Number(data?.total ?? plugins.value.length) || 0
  } catch (error) {
    ElMessage.error(error?.message || '获取插件市场失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  Object.assign(query, { keyword: '', cateId: null, licenseType: '', status: '' })
  sortKey.value = 'default'
  page.value = 1
  loadList()
}
const setLicenseType = (value) => { query.licenseType = value; search() }
/** 分类筛选：null = 全部，0 = 未分类兜底桶，其余为 hou_addon_cate.id */
const setCategory = (id) => { query.cateId = id === null || id === undefined ? null : Number(id); search() }
const setOnlyUninstalled = (checked) => { query.status = checked ? 'available' : ''; search() }

const search = () => {
  page.value = 1
  loadList()
}

const handlePageSizeChange = () => {
  page.value = 1
  loadList()
}

const goDetail = (item) => {
  router.push({ path: '/platform/plugins/market/detail', query: { code: item.code } })
}

const goAddons = () => {
  router.push('/platform/plugins')
}

/**
 * 进入购买页（下单 → 支付 → 自动换票安装）。
 *
 * <p>为什么不让卡片直接调下单接口：下单必须先勾选《商业插件购买与维护协议》、
 * 选定价格套餐，这两步都在 checkout 页完成；卡片直接下单等于绕过合规勾选。</p>
 */
const goCheckout = (item) => {
  router.push(`/platform/plugins/market/checkout?code=${encodeURIComponent(item.code)}`)
}

/**
 * 跳转到宿主插件货架：优先 checkout（付费未购），否则详情页。
 * 通用按 hostService code 匹配，不硬编码 pm。
 */
const goHostPlugin = ({ hostCode, hostListing }) => {
  if (!hostCode) return
  const paidUnlicensed = hostListing
    && String(hostListing.licenseType || '').toUpperCase() === 'PAID'
    && !hostListing.licensed
  if (paidUnlicensed) {
    router.push(`/platform/plugins/market/checkout?code=${encodeURIComponent(hostCode)}`)
    return
  }
  router.push({ path: '/platform/plugins/market/detail', query: { code: hostCode } })
}

const guardHostThen = async (item, next) => {
  if (!hostUnavailable(item)) {
    next()
    return
  }
  await promptHostRequired(item, {
    catalog: plugins.value,
    onGoHost: goHostPlugin
  })
}

const onPurchaseClick = (item) => {
  guardHostThen(item, () => goCheckout(item))
}

const onActivateLicenseClick = (item) => {
  if (!item?.code || activating.value || installing.value) return
  guardHostThen(item, () => handleActivateLicense(item))
}

const handleActivateLicense = async (item) => {
  let input
  try {
    input = await ElMessageBox.prompt(
      `请输入「${item.name || item.code}」的授权码。激活成功后会自动进入安装。`,
      '授权码安装',
      {
        confirmButtonText: '确认激活并安装',
        cancelButtonText: '取消',
        inputPlaceholder: 'HOU-XXXXXXXX-XXXXXXXXXXXXXXXX',
        inputValidator: (value) => (String(value || '').trim() ? true : '请输入授权码')
      }
    )
  } catch {
    return
  }
  activating.value = item.code
  try {
    const res = await activateLicense(String(input.value).trim(), item.code, siteInstanceId)
    const data = res?.data ?? {}
    const expireText = data.permanent ? '永久有效' : `有效期至 ${data.expireTime || '-'}`
    ElMessage.success(`授权已激活（${expireText}），开始安装…`)
    await loadList()
    const refreshed = plugins.value.find(row => row.code === item.code) || { ...item, licensed: true, installed: false }
    await handleInstall(refreshed)
  } catch (error) {
    ElMessage.error(error?.message || '激活失败，请核对授权码')
  } finally {
    activating.value = ''
  }
}

const onInstallClick = (item) => {
  if (installing.value) return
  guardHostThen(item, () => handleInstall(item))
}

/** 右侧镂空「需 xx 服务」按钮：直接打开宿主引导弹窗 */
const onHostHintClick = (item) => {
  guardHostThen(item, () => {})
}

// 先选类型：与插件中心页头同款弹窗，确认后才带类型进入创建向导。
const typeDialogVisible = ref(false)
const goCreate = () => { typeDialogVisible.value = true }
const handleRuntimeConfirm = (runtime) => {
  typeDialogVisible.value = false
  router.push({ path: '/platform/plugins/create', query: { runtime } })
}

const goLicenses = () => {
  router.push('/platform/plugins/licenses')
}

const goOrders = () => {
  router.push('/platform/plugins/orders')
}

const handleInstall = (item) => {
  installTarget.value = item
  installVersion.value = item.latestVersion || ''
  // 运行方式由插件清单 plugin.yaml 声明、安装链路按清单执行，前端无需再选——直接安装。
  submitInstall('EMBEDDED')
}

const submitInstall = async (installProfile) => {
  const item = installTarget.value
  if (!item?.code || installing.value) return
  installing.value = item.code
  installProgressTitle.value = `正在安装「${item.name}」`
  installProgressStep.value = 0
  installProgressTip.value = INSTALL_PROGRESS_TIPS[0]
  installProgressVisible.value = true
  const startedAt = Date.now()
  const tick = window.setInterval(() => {
    if (installProgressStep.value < 2) {
      installProgressStep.value += 1
      installProgressTip.value = INSTALL_PROGRESS_TIPS[installProgressStep.value]
    }
  }, INSTALL_PROGRESS_TICK_MS)
  try {
    const res = isStandaloneService(item.code)
      ? await installServiceAndWait(item.code, installVersion.value)
      : await installFromMarket(item.code, installVersion.value, installProfile, siteInstanceId)
    const data = res?.finalStatus ?? res?.data ?? {}
    window.clearInterval(tick)
    installProgressStep.value = 3
    installProgressTip.value = INSTALL_PROGRESS_TIPS[3]
    const remain = INSTALL_PROGRESS_MIN_MS - (Date.now() - startedAt)
    if (remain > 0) {
      await new Promise((resolve) => window.setTimeout(resolve, remain))
    }
    ElMessage.success(`「${item.name}」安装成功（v${data.version || item.latestVersion || '-'}）`)
    await loadList()
    await refreshPluginApplications()
  } catch (error) {
    window.clearInterval(tick)
    ElMessage.error(error?.message || '安装失败')
  } finally {
    window.clearInterval(tick)
    installProgressVisible.value = false
    installProgressStep.value = 0
    installProgressTip.value = ''
    installing.value = ''
  }
}

onMounted(() => {
  loadCategories()
  loadList()
})
</script>

<style scoped>
.market-page {
  --pl-primary: var(--hc-primary, #409eff);
  --pl-text: #1f2937;
  --pl-text-sub: #6b7280;
  --pl-text-mute: #9aa3b2;
  --pl-line: #e8ecf3;
  --pl-line-soft: #f1f4f9;
  --pl-surface: #f1f4f8;
  --pl-ok: #10b981;
  --pl-warn: #d97706;
  --pl-radius: 12px;

  padding: 22px 24px 36px;
  min-width: 0;
  color: var(--pl-text);
  background: var(--hc-content-bg, #f4f6f9);
  min-height: 100%;
}

/* ── 页头 ── */
.market-head {
  position: relative;
  padding: 6px 0 20px;
  text-align: center;
}
.market-head h1 {
  margin: 0;
  font-size: 27px;
  line-height: 38px;
  font-weight: 650;
  letter-spacing: 0.4px;
  color: #111827;
}
.market-head p {
  margin: 8px auto 0;
  max-width: 720px;
  color: var(--pl-text-mute);
  font-size: 13px;
  line-height: 21px;
}

/* 跨页快捷入口：二级菜单已移除，插件中心 / 授权 / 订单 / 创建 收到这里 */
.quick-nav {
  position: absolute;
  right: 0;
  top: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.quick-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 30px;
  padding: 0 12px;
  border: 1px solid var(--pl-line);
  border-radius: 8px;
  background: #fff;
  color: var(--pl-text-sub);
  font-size: 13px;
  cursor: pointer;
  transition: border-color 0.2s ease, color 0.2s ease, background 0.2s ease;
}
.quick-link:hover { border-color: #bcd7fb; color: var(--pl-primary); background: #f7faff; }
.quick-link--primary {
  border-color: transparent;
  background: rgba(64, 158, 255, 0.1);
  color: var(--pl-primary);
  font-weight: 500;
}
.quick-link--primary:hover { background: rgba(64, 158, 255, 0.16); }

/* ── 筛选面板 ── */
.filter-panel {
  padding: 6px 22px;
  border: 1px solid var(--pl-line);
  border-radius: var(--pl-radius);
  background: #fff;
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.03);
}
.filter-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 13px 0;
  border-bottom: 1px solid var(--pl-line-soft);
}
.filter-row:last-child { border-bottom: 0; }
.filter-label {
  flex: 0 0 auto;
  width: 52px;
  height: 26px;
  line-height: 26px;
  color: var(--pl-text-mute);
  font-size: 13px;
}
.filter-links {
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px 4px;
}
.filter-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 26px;
  padding: 0 10px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: var(--pl-text-sub);
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  transition: color 0.18s ease, background 0.18s ease;
}
.filter-link em {
  font-style: normal;
  color: var(--pl-text-mute);
  font-size: 12px;
}
.filter-link:hover { color: var(--pl-primary); background: #f5f9ff; }
.filter-link.is-active { color: var(--pl-primary); font-weight: 600; background: rgba(64, 158, 255, 0.08); }
.filter-link.is-active em { color: var(--pl-primary); }

/* ── 排序 / 工具条 ── */
.sort-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 14px;
  padding: 10px 14px;
  border: 1px solid var(--pl-line);
  border-radius: var(--pl-radius);
  background: #fff;
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.03);
}
.sort-tabs { display: flex; align-items: center; gap: 4px; flex-wrap: wrap; }
.sort-tab {
  height: 30px;
  padding: 0 12px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: var(--pl-text-sub);
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  transition: color 0.18s ease, background 0.18s ease;
}
.sort-tab:hover { color: var(--pl-primary); background: #f5f9ff; }
.sort-tab.is-active {
  background: var(--pl-primary);
  color: #fff;
  font-weight: 500;
}
.install-toggle { display: inline-flex; align-items: center; padding-left: 12px; border-left: 1px solid var(--pl-line-soft); }
.install-toggle :deep(.el-checkbox__label) { font-size: 13px; color: var(--pl-text-sub); }
.sort-spacer { flex: 1 1 auto; }
.result-count { color: var(--pl-text-mute); font-size: 12px; white-space: nowrap; }
.market-search { width: 240px; }

/* ── 货架 ── */
.market-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 18px;
  margin-top: 16px;
  min-height: 180px;
  align-content: start;
}

.market-card {
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: var(--pl-radius);
  background: #fff;
  border: 1px solid var(--pl-line);
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
  cursor: pointer;
  transition: box-shadow 0.22s ease, transform 0.22s ease, border-color 0.22s ease;
}
.market-card:not(.is-skeleton):hover {
  border-color: #cfe0fb;
  box-shadow: 0 12px 26px rgba(16, 24, 40, 0.1);
  transform: translateY(-3px);
}

/* 封面：饱和渐变商品图 + 中央白色 App 图标块 + 分类角标 + 状态缎带 */
.card-media {
  position: relative;
  aspect-ratio: 16 / 10;
  min-height: 132px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.media-deco { position: absolute; border-radius: 50%; pointer-events: none; }
.media-deco--a {
  width: 190px;
  height: 190px;
  right: -66px;
  top: -78px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.4), transparent 68%);
}
.media-deco--b {
  width: 150px;
  height: 150px;
  left: -54px;
  bottom: -74px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.22), transparent 70%);
}
.media-icon {
  position: relative;
  z-index: 1;
  width: 64px;
  height: 64px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.94);
  font-size: 26px;
  font-weight: 700;
  box-shadow: 0 6px 18px rgba(8, 20, 48, 0.22);
  transition: transform 0.3s ease;
}
.market-card:hover .media-icon { transform: scale(1.05); }
.media-icon img { width: 100%; height: 100%; object-fit: cover; border-radius: 18px; }
.media-cat {
  position: absolute;
  left: 10px;
  top: 10px;
  z-index: 1;
  height: 22px;
  padding: 0 9px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.86);
  color: #3f4a5c;
  font-size: 12px;
  line-height: 22px;
  backdrop-filter: blur(4px);
}
/* 右上角斜角缎带：热门 / 已安装 */
.ribbon {
  position: absolute;
  top: 0;
  right: 0;
  z-index: 2;
  width: 78px;
  height: 78px;
  overflow: hidden;
  pointer-events: none;
}
.ribbon i {
  position: absolute;
  top: 14px;
  right: -26px;
  width: 112px;
  transform: rotate(45deg);
  text-align: center;
  font-style: normal;
  font-size: 12px;
  line-height: 21px;
  color: #fff;
  box-shadow: 0 2px 6px rgba(8, 20, 48, 0.22);
}
.ribbon.hot i { background: linear-gradient(135deg, #ff8a3d, #f4511e); }
.ribbon.installed i { background: linear-gradient(135deg, #34d399, #0d9488); }

.card-info { padding: 12px 14px 10px; flex: 1 1 auto; min-width: 0; }
.card-name {
  margin: 0;
  font-size: 15px;
  line-height: 22px;
  font-weight: 600;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-desc {
  margin: 5px 0 0;
  color: var(--pl-text-sub);
  font-size: 12px;
  line-height: 19px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 10px;
}
.meta-left {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--pl-text-mute);
  font-size: 12px;
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
}
.meta-right { display: inline-flex; align-items: center; gap: 8px; flex-shrink: 0; }
.meta-sep { font-style: normal; opacity: .45; margin: 0 1px; }
.price { font-size: 14px; font-weight: 600; white-space: nowrap; }
.price.paid { color: #ef4444; }
.price.trial { color: var(--pl-primary); }
.price.free { color: var(--pl-ok); }

.card-foot {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 9px 14px;
  border-top: 1px solid var(--pl-line-soft);
  background: #fbfcfe;
}
.foot-btn {
  height: 28px;
  padding: 0 12px;
  border: 1px solid var(--pl-line);
  border-radius: 7px;
  background: #fff;
  color: var(--pl-text-sub);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease;
}
.foot-btn:hover { border-color: #bcd7fb; color: var(--pl-primary); }
.foot-btn--primary {
  border-color: transparent;
  background: var(--pl-primary);
  color: #fff;
  font-weight: 500;
}
.foot-btn--primary:hover { background: #2f8fff; border-color: transparent; color: #fff; }
.foot-btn:disabled,
.foot-btn.is-busy {
  opacity: 0.72;
  cursor: not-allowed;
  pointer-events: none;
}
.foot-btn-spinner {
  display: inline-block;
  width: 12px;
  height: 12px;
  margin-right: 6px;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: #fff;
  border-radius: 50%;
  vertical-align: -1px;
  animation: foot-spin 0.7s linear infinite;
}
@keyframes foot-spin {
  to { transform: rotate(360deg); }
}
.install-progress-tip {
  margin: 22px 0 4px;
  text-align: center;
  color: var(--pl-text-sub);
  font-size: 14px;
  line-height: 1.5;
}
.foot-btn--warn {
  border-color: rgba(217, 119, 6, 0.3);
  background: rgba(245, 158, 11, 0.1);
  color: var(--pl-warn);
}
.foot-btn--warn:hover { background: rgba(245, 158, 11, 0.18); border-color: transparent; color: var(--pl-warn); }
.foot-hint { margin-left: auto; color: var(--pl-text-mute); font-size: 12px; white-space: nowrap; }
.foot-hint.is-ok { color: var(--pl-ok); }
.foot-hint.is-blocked { color: var(--pl-danger, #d03050); }
.foot-btn--outline-license {
  border-color: #b3d8ff;
  color: #337ecc;
  background: #fff;
}
.foot-btn--outline-license:hover {
  border-color: #409eff;
  color: #409eff;
  background: #ecf5ff;
}
/* 缺宿主：镂空/描边按钮，视觉上明确可点，避免被当成灰字 */
.foot-btn--outline-host {
  margin-left: auto;
  border-color: var(--pl-danger, #d03050);
  background: transparent;
  color: var(--pl-danger, #d03050);
  font-weight: 500;
  cursor: pointer;
  pointer-events: auto;
  position: relative;
  z-index: 2;
}
.foot-btn--outline-host:hover {
  background: rgba(208, 48, 80, 0.08);
  border-color: var(--pl-danger, #d03050);
  color: var(--pl-danger, #d03050);
}
.foot-btn--outline-host:active {
  background: rgba(208, 48, 80, 0.14);
}

/* 骨架屏 */
.market-card.is-skeleton { cursor: default; }
.sk-block { background: #eef1f6; }
.sk-line { height: 13px; border-radius: 6px; background: #eef1f6; margin-top: 9px; }
.sk-line--sm { height: 11px; }

/* 空态 */
.empty-block {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 44px 20px;
  background: #fff;
  border: 1px dashed #e0e6f0;
  border-radius: var(--pl-radius);
}
.empty-title { margin: 10px 0 0; font-size: 15px; font-weight: 600; color: #374151; }
.empty-desc { margin: 0; color: var(--pl-text-mute); font-size: 13px; }
.empty-actions { display: flex; gap: 8px; margin-top: 12px; }

.market-pagination { justify-content: flex-end; margin: 18px 0 6px; }
.market-note {
  margin: 14px 0 0;
  color: var(--pl-text-mute);
  font-size: 12px;
  line-height: 20px;
}
.activate-tip { margin: 0 0 14px; color: #606266; font-size: 13px; line-height: 20px; }

@media (max-width: 1400px) {
  .market-head { padding-bottom: 56px; }
  .quick-nav { position: static; justify-content: center; margin-bottom: 14px; }
}

@media (max-width: 900px) {
  .market-page { padding: 12px; }
  .market-head h1 { font-size: 22px; line-height: 30px; }
  .filter-label { width: 46px; }
  .market-search { width: 100%; }
  .sort-bar { align-items: stretch; }
  .install-toggle { border-left: 0; padding-left: 0; }
  .market-grid { grid-template-columns: 1fr; }
}
</style>
