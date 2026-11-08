<template>
  <div class="addons-page" :class="{ 'is-compact': compactEntry }">
    <!-- ── Hero：品牌渐变主视觉，承载站点信息与快捷入口 ──
         从「营销活动」一级菜单进入时（?entry=marketing）隐藏，直接进列表。 -->
    <section v-if="!compactEntry" class="hero">
      <div class="hero-copy">
        <div class="hero-tags">
          <span class="hero-badge">插件中心</span>
          <span class="hero-site">
            <el-icon><Link /></el-icon>{{ siteInstanceId || '当前站点' }}
          </span>
        </div>
        <h1>为你的商城按需装配能力</h1>
        <p>这里只管理本站已安装的插件（启停与配置）；挑选、购买新能力请前往插件市场。</p>
        <div class="hero-actions">
          <button type="button" class="hero-btn hero-btn--solid" @click="goMarket">
            <el-icon><Shop /></el-icon>进入插件市场
          </button>
          <button type="button" class="hero-btn" @click="goCreate">
            <el-icon><Plus /></el-icon>创建插件
          </button>
          <button type="button" class="hero-btn" @click="goLicenses">
            <el-icon><Key /></el-icon>授权管理
          </button>
          <button type="button" class="hero-btn" @click="goOrders">
            <el-icon><Tickets /></el-icon>插件订单
          </button>
        </div>
      </div>

      <div class="hero-art" aria-hidden="true">
        <svg viewBox="0 0 260 180" width="260" height="180">
          <defs>
            <linearGradient id="cubeA" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0%" stop-color="#ffffff" stop-opacity="0.95" />
              <stop offset="100%" stop-color="#9ec5ff" stop-opacity="0.55" />
            </linearGradient>
            <linearGradient id="cubeB" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0%" stop-color="#a5f3fc" stop-opacity="0.9" />
              <stop offset="100%" stop-color="#38bdf8" stop-opacity="0.5" />
            </linearGradient>
            <linearGradient id="cubeC" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0%" stop-color="#ffffff" stop-opacity="0.65" />
              <stop offset="100%" stop-color="#c7d2fe" stop-opacity="0.35" />
            </linearGradient>
          </defs>
          <g opacity="0.95">
            <rect x="24" y="46" width="76" height="76" rx="18" fill="url(#cubeA)" />
            <rect x="52" y="20" width="76" height="76" rx="18" fill="url(#cubeB)" />
            <rect x="112" y="70" width="76" height="76" rx="18" fill="url(#cubeC)" />
            <rect x="168" y="34" width="62" height="62" rx="16" fill="url(#cubeA)" opacity="0.7" />
          </g>
          <g stroke="#ffffff" stroke-opacity="0.55" stroke-width="2" stroke-linecap="round">
            <path d="M62 84h34M79 67v34" />
            <path d="M136 108h34" />
          </g>
          <circle cx="199" cy="112" r="7" fill="#22d3ee" />
          <circle cx="38" cy="132" r="5" fill="#ffffff" opacity="0.7" />
          <circle cx="228" cy="26" r="4" fill="#ffffff" opacity="0.5" />
        </svg>
      </div>

      <div class="hero-glow hero-glow--a" aria-hidden="true" />
      <div class="hero-glow hero-glow--b" aria-hidden="true" />
    </section>

    <!-- ── 概览统计（营销活动入口同样隐藏） ── -->
    <section v-if="!compactEntry" class="summary-strip">
      <button
        v-for="item in summaryCards"
        :key="item.label"
        type="button"
        class="summary-card"
        :class="item.tone"
        @click="onSummaryClick(item)"
      >
        <div class="summary-icon">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <div class="summary-body">
          <span class="label">{{ item.label }}</span>
          <strong class="value">{{ item.value }}</strong>
        </div>
        <span class="summary-hint">{{ item.hint }}</span>
      </button>
    </section>

    <!-- ── 工具栏：搜索 / 状态 / 分类 / 视图 ── -->
    <section class="toolbar">
      <div class="toolbar-row">
        <el-input
          v-model="query.keyword"
          class="filter-search"
          :prefix-icon="Search"
          clearable
          placeholder="搜索插件名称、编码或说明"
          @keyup.enter="loadPlugins"
        />
        <div class="segmented" role="group" aria-label="安装状态筛选">
          <button
            v-for="opt in statusOptions"
            :key="opt.value"
            type="button"
            class="seg-btn"
            :class="{ 'is-active': query.status === opt.value }"
            @click="setStatus(opt.value)"
          >
            {{ opt.label }}
          </button>
        </div>
        <span class="toolbar-spacer" />
        <span class="result-count">共 {{ visiblePlugins.length }} 个插件</span>
        <div class="view-switch" role="group" aria-label="视图切换">
          <button
            type="button"
            class="view-btn"
            :class="{ 'is-active': viewMode === 'grid' }"
            title="卡片视图"
            @click="viewMode = 'grid'"
          >
            <el-icon><Grid /></el-icon>
          </button>
          <button
            type="button"
            class="view-btn"
            :class="{ 'is-active': viewMode === 'list' }"
            title="列表视图"
            @click="viewMode = 'list'"
          >
            <el-icon><List /></el-icon>
          </button>
        </div>
        <el-button :icon="Refresh" :loading="loading" title="刷新" @click="reload" />
      </div>

      <div v-if="categoryTabs.length" class="category-tabs">
        <button
          type="button"
          class="cat-tab"
          :class="{ 'is-active': query.cateId === null }"
          @click="setCategory(null)"
        >
          全部 <i>{{ categoryTotal }}</i>
        </button>
        <button
          v-for="cat in categoryTabs"
          :key="cat.id"
          type="button"
          class="cat-tab"
          :class="{ 'is-active': query.cateId === cat.id }"
          @click="setCategory(cat.id)"
        >
          {{ cat.name }} <i>{{ cat.count }}</i>
        </button>
      </div>
    </section>

    <!-- ── 卡片视图 ── -->
    <section v-if="viewMode === 'grid'" class="card-grid">
      <template v-if="loading">
        <div v-for="n in 6" :key="`sk-${n}`" class="plugin-card is-skeleton">
          <div class="card-cover sk-block" />
          <div class="card-body">
            <div class="sk-line" style="width: 58%" />
            <div class="sk-line sk-line--sm" style="width: 38%" />
            <div class="sk-line sk-line--sm" style="width: 92%; margin-top: 14px" />
            <div class="sk-line sk-line--sm" style="width: 70%" />
          </div>
        </div>
      </template>

      <template v-else>
        <article v-for="row in visiblePlugins" :key="row.code" class="plugin-card">
          <div class="card-cover" :style="coverStyle(row)">
            <span class="cover-monogram">{{ iconText(row) }}</span>
            <span v-if="row.category" class="cover-cat">{{ row.category }}</span>
          </div>

          <div class="card-body">
            <div class="avatar" :style="iconStyle(row)">
              <img v-if="row.iconUrl" :src="row.iconUrl" :alt="row.name" />
              <span v-else>{{ iconText(row) }}</span>
            </div>

            <div class="card-head">
              <div class="card-title">
                <h3 :title="row.name">{{ row.name }}</h3>
                <div class="code">{{ row.code }} · v{{ row.version || '-' }}</div>
              </div>
              <span class="status-pill" :class="statusOf(row).tone">
                <i class="dot" />{{ statusOf(row).text }}
              </span>
            </div>

            <p class="desc">{{ row.description || '暂无简介' }}</p>

            <div class="meta-row">
              <span v-if="(row.terminals || []).length" class="chip">
                {{ (row.terminals || []).join(' · ') }}
              </span>
              <span v-else class="chip chip-ghost">适用端未声明</span>
              <span v-if="(row.dependencies || []).length" class="chip chip-ghost">
                依赖 {{ row.dependencies.length }} 项
              </span>
            </div>
          </div>

          <div class="card-foot">
            <span class="price" :class="priceClass(row)">{{ row.priceText || priceText(row) }}</span>
            <div class="actions">
              <template v-if="!row.installed">
                <el-button
                  v-if="isInstallBlocked(row)"
                  class="btn-warn"
                  size="small"
                  @click="goMarketDetail(row)"
                >
                  {{ row.purchaseStatus === 'UNAVAILABLE' ? '查看市场' : '去购买' }}
                </el-button>
                <el-button v-else type="primary" size="small" @click="handleInstall(row)">安装</el-button>
              </template>
              <template v-else>
                <el-button
                  v-if="canUpgrade(row)"
                  type="warning"
                  size="small"
                  @click="openUpgrade(row)"
                >升级</el-button>
                <el-button v-if="!isStandaloneService(row.code)" size="small" @click="openConfig(row)">配置</el-button>
                <el-button v-if="!isStandaloneService(row.code)" size="small" :class="row.enabled ? 'btn-warn' : 'btn-ok'" @click="handleToggle(row)">
                  {{ row.enabled ? '停用' : '启用' }}
                </el-button>
                <el-dropdown trigger="click" placement="bottom-end" @command="handleMore($event, row)">
                  <el-button size="small" :icon="MoreFilled" title="更多操作" />
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="detail">查看详情</el-dropdown-item>
                      <el-dropdown-item v-if="!isStandaloneService(row.code)" command="uninstall" divided>卸载插件</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </div>
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
        <p class="empty-title">没有匹配的插件</p>
        <p class="empty-desc">试着换个关键词或清空筛选条件；也可以去插件市场看看新上架的扩展。</p>
        <div class="empty-actions">
          <el-button size="small" @click="resetFilters">清空筛选</el-button>
          <el-button size="small" type="primary" @click="goMarket">去插件市场</el-button>
        </div>
      </div>
    </section>

    <!-- ── 列表视图 ── -->
    <section v-else v-loading="loading" class="list-panel">
      <div class="list-head">
        <span class="col-main">插件</span>
        <span class="col-cat">分类 / 适用端</span>
        <span class="col-ver">版本</span>
        <span class="col-state">状态</span>
        <span class="col-price">授权</span>
        <span class="col-act">操作</span>
      </div>
      <template v-if="visiblePlugins.length">
        <div v-for="row in visiblePlugins" :key="row.code" class="list-row">
          <div class="col-main">
            <div class="plugin-icon is-sm" :style="iconStyle(row)">
              <img v-if="row.iconUrl" :src="row.iconUrl" :alt="row.name" />
              <span v-else>{{ iconText(row) }}</span>
            </div>
            <div class="list-name">
              <strong>{{ row.name }}</strong>
              <span class="code">{{ row.code }}</span>
            </div>
          </div>
          <div class="col-cat">
            <span v-if="row.category" class="chip chip-cat">{{ row.category }}</span>
            <span v-if="(row.terminals || []).length" class="chip">{{ (row.terminals || []).join(' · ') }}</span>
            <span v-if="!row.category && !(row.terminals || []).length" class="muted">—</span>
          </div>
          <div class="col-ver">v{{ row.version || '-' }}</div>
          <div class="col-state">
            <span class="status-pill" :class="statusOf(row).tone">
              <i class="dot" />{{ statusOf(row).text }}
            </span>
          </div>
          <div class="col-price">
            <span class="price" :class="priceClass(row)">{{ row.priceText || priceText(row) }}</span>
          </div>
          <div class="col-act">
            <template v-if="!row.installed">
              <el-button v-if="isInstallBlocked(row)" class="btn-warn" size="small" @click="goMarketDetail(row)">
                {{ row.purchaseStatus === 'UNAVAILABLE' ? '查看市场' : '去购买' }}
              </el-button>
              <el-button v-else type="primary" size="small" @click="handleInstall(row)">安装</el-button>
            </template>
            <template v-else>
              <el-button v-if="canUpgrade(row)" type="warning" size="small" @click="openUpgrade(row)">升级</el-button>
              <el-button v-if="!isStandaloneService(row.code)" size="small" @click="openConfig(row)">配置</el-button>
              <el-button v-if="!isStandaloneService(row.code)" size="small" :class="row.enabled ? 'btn-warn' : 'btn-ok'" @click="handleToggle(row)">
                {{ row.enabled ? '停用' : '启用' }}
              </el-button>
              <el-dropdown trigger="click" placement="bottom-end" @command="handleMore($event, row)">
                <el-button size="small" :icon="MoreFilled" title="更多操作" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="detail">查看详情</el-dropdown-item>
                    <el-dropdown-item v-if="!isStandaloneService(row.code)" command="uninstall" divided>卸载插件</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </div>
        </div>
      </template>
      <div v-else-if="!loading" class="empty-block">
        <p class="empty-title">没有匹配的插件</p>
        <p class="empty-desc">试着换个关键词或清空筛选条件。</p>
      </div>
    </section>

    <el-drawer v-model="drawerVisible" :title="currentPlugin?.name || '插件配置'" size="520px">
      <template v-if="currentPlugin">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="插件编码">{{ currentPlugin.code }}</el-descriptions-item>
          <el-descriptions-item label="说明">{{ currentPlugin.description }}</el-descriptions-item>
          <el-descriptions-item label="入口路径">{{ currentPlugin.entryPath || '-' }}</el-descriptions-item>
          <el-descriptions-item label="依赖插件">
            {{ (currentPlugin.dependencies || []).join('、') || '无' }}
          </el-descriptions-item>
        </el-descriptions>
        <schema-form ref="configFormRef" v-model="configJson" :schema="currentPlugin.configSchema" />
      </template>
      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveConfig">保存</el-button>
      </template>
    </el-drawer>

    <plugin-type-dialog v-model="typeDialogVisible" @confirm="handleTypeConfirm" />

    <PluginUpgradeRiskDialog
      v-model="upgradeDialogVisible"
      :plugin-code="upgradeRow?.code || ''"
      :target-version="upgradeRow?.latestVersion || ''"
      :host-service="upgradeRow?.code || ''"
      :maintenance-until="upgradeRow?.licenseExpireTime || upgradeRow?.maintenanceUntil"
      :confirming="upgrading"
      @confirm="submitUpgrade"
    />
  </div>
</template>

<script setup>
import { isStandaloneService, installServiceAndWait, upgradeServiceAndWait } from '@/api/plugin-center/service'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Box,
  CircleCheck,
  Download,
  Grid,
  Key,
  Link,
  List,
  MoreFilled,
  Plus,
  Refresh,
  Search,
  Shop,
  Tickets,
  VideoPlay
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SchemaForm from '@/components/SchemaForm.vue'
import PluginTypeDialog from './components/PluginTypeDialog.vue'
import PluginUpgradeRiskDialog from './market/components/PluginUpgradeRiskDialog.vue'
import { getPluginList, getPluginSummary, getPluginCategories, savePluginConfig, togglePlugin } from '@/api/plugin-center/pluginCenter'
import { installAndWait, uninstallFromUpdate, upgradeAndWait } from '@/api/plugin-center/pluginUpdate'
import { getSiteInstanceId } from '@/utils/site'
import { formatInstallFailure } from '@/utils/pluginInstallFeedback'
import { refreshPluginApplications } from '@/utils/pluginApplicationRefresh'
import { pluginCategoryId } from '@/utils/pluginApplicationNav'

const loading = ref(false), plugins = ref([]), drawerVisible = ref(false), currentPlugin = ref(null), configJson = ref('{}')
const typeDialogVisible = ref(false)
const upgradeDialogVisible = ref(false)
const upgradeRow = ref(null)
const upgrading = ref(false)
const router = useRouter()
const route = useRoute()
const siteInstanceId = getSiteInstanceId()
const summary = ref({ total: 0, installed: 0, enabled: 0, available: 0 })
// 插件中心默认只看已安装：未安装目录属于插件市场，避免「装了一个却看到一长串」。
const query = reactive({ keyword: '', cateId: null, status: 'installed' })
/** 仅「营销活动」一级菜单带 entry=marketing；其它入口（插件中心/直链）为 false */
const compactEntry = computed(() => String(route.query.entry || '') === 'marketing')
watch(() => route.query.cateId, value => {
  query.cateId = typeof value === 'string' && /^\d+$/.test(value) && Number.isSafeInteger(Number(value))
    ? Number(value) : null
}, { immediate: true })
const categories = ref([])
const configFormRef = ref(null)
const viewMode = ref('grid')
/** 安装/升级进行中的任务状态；非 null 时用于展示进度文案 */
const installProgress = ref(null)

// 取值沿用后端 getPluginList 的 status 口径。中心页不提供「未安装」——去市场安装。
const statusOptions = [
  { label: '已安装', value: 'installed' },
  { label: '已启用', value: 'enabled' },
  { label: '已停用', value: 'disabled' }
]

const needsLicense = (row) => ['PAID', 'TRIAL'].includes(String(row?.licenseType || '').toUpperCase())
const isInstallBlocked = (row) => row?.purchaseStatus === 'UNAVAILABLE' || (needsLicense(row) && !row?.licensed)
const priceClass = (row) => {
  const type = String(row?.licenseType || '').toUpperCase()
  return type === 'PAID' ? 'paid' : type === 'TRIAL' ? 'trial' : 'free'
}
const priceText = (row) => {
  const type = String(row?.licenseType || '').toUpperCase()
  if (type === 'FREE') return '免费'
  if (type === 'PAID') return '付费'
  if (type === 'TRIAL') return '试用'
  return '未定价'
}
/** 卡片头部状态胶囊：已启用 / 已停用 / 未安装 */
const statusOf = (row) => {
  if (row?.enabled) return { text: '已启用', tone: 'on' }
  if (row?.installed) return { text: '已停用', tone: 'off' }
  return { text: '未安装', tone: 'idle' }
}

const summaryCards = computed(() => [
  { label: '插件总数', value: summary.value.total, icon: Box, tone: 'blue', hint: '目录内全部插件' },
  { label: '已安装', value: summary.value.installed, icon: CircleCheck, tone: 'green', hint: '本站点已装配' },
  { label: '已启用', value: summary.value.enabled, icon: VideoPlay, tone: 'cyan', hint: '正在生效' },
  { label: '可安装', value: summary.value.available, icon: Download, tone: 'amber', hint: '待装配能力' }
])

/**
 * 分类改为前端侧筛选：后端一次返回全部（插件中心无分页），
 * 这样分类 Tab 上可以显示真实数量，切换分类也不必重新请求。
 */
const categoryTabs = computed(() => {
  const counts = new Map()
  plugins.value.forEach(row => { const id = pluginCategoryId(row); counts.set(id, (counts.get(id) || 0) + 1) })
  const tabs = categories.value.map(c => ({ id: Number(c.id), name: c.name, count: counts.get(Number(c.id)) || 0 }))
  if (counts.has(0)) tabs.push({ id: 0, name: '未分类', count: counts.get(0) })
  return tabs
})
const categoryTotal = computed(() => plugins.value.length)

/** 排序：已启用 → 已停用 → 未安装，同组内按名称，保证列表稳定可读 */
const rankOf = (row) => (row?.enabled ? 0 : row?.installed ? 1 : 2)
const visiblePlugins = computed(() => {
  let list = plugins.value
  if (query.cateId !== null) list = list.filter(row => pluginCategoryId(row) === query.cateId)
  return [...list].sort(
    (a, b) => rankOf(a) - rankOf(b) || String(a?.name || '').localeCompare(String(b?.name || ''), 'zh')
  )
})

/** 插件头像底色：按编码哈希取一组品牌渐变，保证同一插件颜色稳定（与插件市场同色系） */
const palette = [
  ['#6E9BF5', '#1D4ED8'],
  ['#34C6A8', '#0F766E'],
  ['#F0B03C', '#B45309'],
  ['#9B85F0', '#5B21B6'],
  ['#3FC4DE', '#0E7490'],
  ['#F08098', '#9F1239']
]
const hashOf = (code = '') => [...String(code)].reduce((acc, ch) => acc + ch.charCodeAt(0), 0)
const iconText = (row) => String(row?.name || '?').trim().charAt(0)
const iconStyle = (row) => {
  const [from, to] = palette[hashOf(row?.code) % palette.length]
  return { backgroundImage: `linear-gradient(135deg, ${from}, ${to})` }
}
/** 卡片封面：同色系更淡的斜向渐变，配一个放大的半透明字母做底纹 */
const coverStyle = (row) => {
  const [from, to] = palette[hashOf(row?.code) % palette.length]
  return {
    backgroundImage: `linear-gradient(120deg, ${from}24 0%, ${to}1f 55%, ${from}12 100%)`
  }
}

const loadSummary = async () => { summary.value = await getPluginSummary() }
const loadPlugins = async () => {
  loading.value = true
  try {
    // category 由前端筛选，故不下发，以便分类 Tab 能统计全量数量
    const data = await getPluginList({ keyword: query.keyword, status: query.status, instanceId: siteInstanceId })
    plugins.value = data?.list ?? []
  } finally {
    loading.value = false
  }
}
const loadCategories = async () => { categories.value = await getPluginCategories() }
const reload = () => Promise.all([loadSummary(), loadPlugins(), loadCategories()])
const resetFilters = () => { Object.assign(query, { keyword: '', status: 'installed' }); setCategory(null); loadPlugins() }
const setStatus = (value) => { query.status = value; loadPlugins() }
/** 统计卡点击：切到对应状态筛选（「可安装」跳转市场，不在中心堆未安装目录） */
const onSummaryClick = (card) => {
  if (card?.label === '可安装') {
    goMarket()
    return
  }
  if (card?.label === '已安装') return setStatus('installed')
  if (card?.label === '已启用') return setStatus('enabled')
  if (card?.label === '插件总数') return setStatus('installed')
}
const setCategory = id => {
  query.cateId = id == null ? null : Number(id)
  router.replace({ query: { ...route.query, cateId: query.cateId === null ? undefined : String(query.cateId) } })
}
const goMarket = () => router.push('/platform/plugins/market')
// 插件模块不再挂左侧二级菜单，跨页入口统一收到页头
const goLicenses = () => router.push('/platform/plugins/licenses')
const goOrders = () => router.push('/platform/plugins/orders')
const goMarketDetail = (row) => { router.push(`/platform/plugins/market/detail?code=${encodeURIComponent(row.code)}`) }
// 先选形态：用户确认后才带 runtime 进入创建向导，取消或关闭弹窗不跳转。
const goCreate = () => { typeDialogVisible.value = true }
const handleTypeConfirm = (runtime) => {
  typeDialogVisible.value = false
  router.push({ path: '/platform/plugins/create', query: { runtime } })
}
const handleMore = async (command, row) => {
  if (command === 'detail') return goMarketDetail(row)
  if (command === 'uninstall') return handleUninstall(row)
}
/**
 * 安装插件：走 /web/plugin-center/update/install 完整编排（下载 → 校验 → 建表 → 装载 → 健康检查）。
 *
 * 原先走的是 plugin-center/install（只写一行安装状态位），线上等于没有任何校验。
 * 编排是异步任务，这里轮询到终态再提示，避免「点了没反应」或「还在装就提示成功」。
 */
const handleInstall = async (row) => {
  try {
    const installer = isStandaloneService(row.code) ? installServiceAndWait : installAndWait
    const { result, finalStatus } = await installer(row.code, row.latestVersion || row.version, (status) => {
      installProgress.value = status
    })
    const state = String(finalStatus?.state ?? result?.state ?? '').toUpperCase()
    if (state === 'FAILED') {
      ElMessage.error(formatInstallFailure(finalStatus || result))
    } else if (state === 'ROLLED_BACK') {
      ElMessage.warning(formatInstallFailure({ ...(finalStatus || result || {}), state: 'ROLLED_BACK' }))
    } else if (state === 'SUCCESS') {
      ElMessage.success('插件已安装')
      await refreshPluginApplications()
    }
  } catch (error) {
    ElMessage.error(formatInstallFailure(error?.installStatus || { errorMsg: error?.message, state: 'FAILED' }))
  } finally {
    installProgress.value = null
    await reload()
  }
}

const canUpgrade = (row) => {
  if (!row?.installed) return false
  const current = String(row.version || '').trim()
  const latest = String(row.latestVersion || '').trim()
  return Boolean(latest) && current !== latest
}

const openUpgrade = (row) => {
  upgradeRow.value = row
  upgradeDialogVisible.value = true
}

const submitUpgrade = async () => {
  const row = upgradeRow.value
  if (!row) return
  upgrading.value = true
  try {
    const upgrader = isStandaloneService(row.code) ? upgradeServiceAndWait : upgradeAndWait
    const { result, finalStatus } = await upgrader(row.code, row.latestVersion, (status) => {
      installProgress.value = status
    })
    const state = String(finalStatus?.state ?? result?.state ?? '').toUpperCase()
    if (state === 'FAILED') {
      ElMessage.error(formatInstallFailure(finalStatus || result))
    } else if (state === 'ROLLED_BACK') {
      ElMessage.warning(formatInstallFailure({ ...(finalStatus || result || {}), state: 'ROLLED_BACK' }))
    } else if (state === 'SUCCESS') {
      ElMessage.success(`已升级到 v${row.latestVersion || '-'}`)
      upgradeDialogVisible.value = false
      await refreshPluginApplications()
    }
  } catch (error) {
    ElMessage.error(formatInstallFailure(error?.installStatus || { errorMsg: error?.message, state: 'FAILED' }))
  } finally {
    upgrading.value = false
    installProgress.value = null
    await reload()
  }
}

const handleToggle = async (row) => {
  await togglePlugin(row.code, !row.enabled)
  ElMessage.success(row.enabled ? '插件已停用' : '插件已启用')
  await reload()
  await refreshPluginApplications()
}
/**
 * 卸载插件：走 /web/plugin-center/update/uninstall 编排版，回收版本目录并清除迁移审计。
 *
 * purgeData 固定 false —— 删业务表不可逆，这里不提供入口；
 * 真要回收数据由运维在服务端按需执行。
 */
const handleUninstall = async (row) => {
  await ElMessageBox.confirm(`确定卸载「${row.name}」？插件配置将保留，业务数据不会删除。`, '卸载插件', { type: 'warning' })
  try {
    const result = await uninstallFromUpdate(row.code, false)
    const state = String(result?.state ?? '').toUpperCase()
    if (state === 'FAILED') {
      ElMessage.error(`卸载失败：${result?.errorMsg || '未知原因'}`)
    } else {
      ElMessage.success('插件已卸载')
      await refreshPluginApplications()
    }
  } catch (error) {
    ElMessage.error(`卸载失败：${error?.message || '未知原因'}`)
  } finally {
    await reload()
  }
}
const openConfig = (row) => { currentPlugin.value = row; configJson.value = row.configJson || '{}'; drawerVisible.value = true }
const handleSaveConfig = async () => {
  if (!currentPlugin.value) return
  if (configFormRef.value?.validate && !configFormRef.value.validate()) {
    ElMessage.warning('配置必须是合法 JSON')
    return
  }
  try { JSON.parse(configJson.value || '{}') } catch { ElMessage.warning('配置必须是合法 JSON'); return }
  await savePluginConfig(currentPlugin.value.code, configJson.value || '{}')
  ElMessage.success('配置已保存')
  drawerVisible.value = false
  await reload()
}
onMounted(reload)
</script>

<style scoped>
.addons-page {
  --pl-primary: var(--hc-primary, #409eff);
  --pl-text: #1f2937;
  --pl-text-sub: #6b7280;
  --pl-text-mute: #9ca3af;
  --pl-line: #eef1f5;
  --pl-surface: #f1f4f8;
  --pl-ok: #10b981;
  --pl-warn: #d97706;
  --pl-radius: 14px;

  padding: 20px 24px 32px;
  min-width: 0;
  color: var(--pl-text);
  background: var(--hc-content-bg, #f4f6f9);
  min-height: 100%;
}

/* ── Hero ── */
.hero {
  position: relative;
  display: flex;
  align-items: center;
  gap: 24px;
  overflow: hidden;
  padding: 28px 32px;
  border-radius: 18px;
  background: linear-gradient(125deg, #10275e 0%, #1d4ed8 42%, #2f8fff 74%, #22d3ee 100%);
  color: #fff;
  box-shadow: 0 10px 30px rgba(29, 78, 216, 0.18);
}
.hero-copy { position: relative; z-index: 2; flex: 1 1 auto; min-width: 0; }
.hero-tags { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.hero-badge {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.28);
  font-size: 12px;
  letter-spacing: 0.04em;
}
.hero-site {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.82);
}
.hero h1 { margin: 12px 0 0; font-size: 26px; line-height: 34px; font-weight: 650; letter-spacing: 0.2px; }
.hero p { margin: 8px 0 0; max-width: 560px; color: rgba(255, 255, 255, 0.82); font-size: 13px; line-height: 21px; }
.hero-actions { display: flex; align-items: center; gap: 10px; margin-top: 18px; flex-wrap: wrap; }
.hero-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 16px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
}
.hero-btn:hover { background: rgba(255, 255, 255, 0.22); transform: translateY(-1px); }
.hero-btn--solid {
  background: #fff;
  border-color: #fff;
  color: #1d4ed8;
  font-weight: 600;
}
.hero-btn--solid:hover { background: #eef4ff; }

.hero-art { position: relative; z-index: 2; flex: 0 0 auto; }
.hero-art svg { display: block; filter: drop-shadow(0 8px 18px rgba(2, 20, 60, 0.28)); }
.hero-glow { position: absolute; border-radius: 50%; pointer-events: none; }
.hero-glow--a {
  width: 260px;
  height: 260px;
  right: -60px;
  top: -110px;
  background: radial-gradient(circle, rgba(34, 211, 238, 0.45), transparent 68%);
}
.hero-glow--b {
  width: 320px;
  height: 320px;
  left: -80px;
  bottom: -180px;
  background: radial-gradient(circle, rgba(147, 197, 253, 0.4), transparent 70%);
}

/* ── 统计条 ── */
.summary-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}
.summary-card {
  appearance: none;
  border: 0;
  width: 100%;
  text-align: left;
  cursor: pointer;
  font: inherit;
  color: inherit;
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  border-radius: var(--pl-radius);
  background: #fff;
  border: 1px solid var(--pl-line);
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}
.summary-card:hover { box-shadow: 0 8px 20px rgba(16, 24, 40, 0.07); transform: translateY(-2px); }
.summary-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
.summary-card.blue .summary-icon { background: rgba(37, 99, 235, 0.1); color: #2563eb; }
.summary-card.green .summary-icon { background: rgba(16, 185, 129, 0.1); color: var(--pl-ok); }
.summary-card.cyan .summary-icon { background: rgba(6, 182, 212, 0.1); color: #06b6d4; }
.summary-card.amber .summary-icon { background: rgba(245, 158, 11, 0.12); color: var(--pl-warn); }
.summary-body { min-width: 0; }
.summary-body .label { display: block; color: var(--pl-text-sub); font-size: 13px; }
.summary-body .value {
  display: block;
  margin-top: 2px;
  font-size: 26px;
  line-height: 32px;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.5px;
}
.summary-hint { margin-left: auto; color: var(--pl-text-mute); font-size: 12px; white-space: nowrap; }

/* ── 工具栏 ── */
.toolbar {
  margin-top: 16px;
  padding: 12px 14px;
  border: 1px solid var(--pl-line);
  border-radius: var(--pl-radius);
  background: #fff;
}
.toolbar-row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.filter-search { width: 280px; }
.toolbar-spacer { flex: 1 1 auto; }
.result-count { color: var(--pl-text-mute); font-size: 12px; white-space: nowrap; }

.segmented {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 3px;
  border-radius: 9px;
  background: #f5f7fa;
}
.seg-btn {
  height: 28px;
  padding: 0 12px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: var(--pl-text-sub);
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
}
.seg-btn:hover { color: var(--pl-primary); }
.seg-btn.is-active {
  background: #fff;
  color: var(--pl-primary);
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(16, 24, 40, 0.1);
}

.view-switch {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 3px;
  border-radius: 8px;
  background: #f5f7fa;
}
.view-btn {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #909399;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
}
.view-btn:hover { color: var(--pl-primary); }
.view-btn.is-active {
  background: #fff;
  color: var(--pl-primary);
  box-shadow: 0 1px 3px rgba(16, 24, 40, 0.1);
}

.category-tabs {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed var(--pl-line);
}
.cat-tab {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 28px;
  padding: 0 12px;
  border: 1px solid var(--pl-line);
  border-radius: 999px;
  background: #fff;
  color: var(--pl-text-sub);
  font-size: 13px;
  cursor: pointer;
  transition: border-color 0.2s ease, color 0.2s ease, background 0.2s ease;
}
.cat-tab i { font-style: normal; font-size: 11px; color: var(--pl-text-mute); }
.cat-tab:hover { border-color: #bcd7fb; color: var(--pl-primary); }
.cat-tab.is-active {
  border-color: var(--pl-primary);
  background: rgba(64, 158, 255, 0.08);
  color: var(--pl-primary);
  font-weight: 500;
}
.cat-tab.is-active i { color: var(--pl-primary); }

/* ── 卡片视图 ── */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  margin-top: 16px;
  min-height: 160px;
  align-content: start;
}
.plugin-card {
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 16px;
  background: #fff;
  border: 1px solid var(--pl-line);
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
  transition: box-shadow 0.22s ease, transform 0.22s ease, border-color 0.22s ease;
}
.plugin-card:not(.is-skeleton):hover {
  border-color: #d6e4fb;
  box-shadow: 0 12px 28px rgba(16, 24, 40, 0.1);
  transform: translateY(-3px);
}

/* 封面：淡色渐变 + 放大的半透明字母底纹 */
.card-cover {
  position: relative;
  height: 76px;
  overflow: hidden;
}
.cover-monogram {
  position: absolute;
  right: -6px;
  bottom: -22px;
  font-size: 92px;
  line-height: 1;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.55);
  letter-spacing: -4px;
  transition: transform 0.3s ease;
}
.plugin-card:hover .cover-monogram { transform: scale(1.06) rotate(-3deg); }
.cover-cat {
  position: absolute;
  left: 14px;
  top: 12px;
  height: 22px;
  padding: 0 9px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.7);
  color: #374151;
  font-size: 12px;
  line-height: 22px;
  backdrop-filter: blur(4px);
}

.card-body { padding: 0 18px 14px; }
.avatar {
  width: 52px;
  height: 52px;
  margin-top: -26px;
  border-radius: 14px;
  border: 3px solid #fff;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(16, 24, 40, 0.14);
}
.avatar img { width: 100%; height: 100%; object-fit: cover; }

.card-head {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-top: 12px;
}
.card-title { flex: 1 1 auto; min-width: 0; }
.card-title h3 {
  margin: 0;
  font-size: 15px;
  line-height: 22px;
  font-weight: 600;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-title .code { margin-top: 3px; color: var(--pl-text-mute); font: 12px Consolas, monospace; }

/* 状态胶囊 */
.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 24px;
  padding: 0 9px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
}
.status-pill .dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }
.status-pill.on { background: rgba(16, 185, 129, 0.1); color: #059669; }
.status-pill.off { background: rgba(245, 158, 11, 0.12); color: var(--pl-warn); }
.status-pill.idle { background: var(--pl-surface); color: var(--pl-text-sub); }

.desc {
  margin: 10px 0 0;
  color: var(--pl-text-sub);
  font-size: 13px;
  line-height: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 40px;
}

.meta-row { display: flex; flex-wrap: wrap; gap: 6px; align-items: center; margin-top: 12px; }
.chip {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 8px;
  border-radius: 6px;
  font-size: 12px;
  background: var(--pl-surface);
  color: #6b7280;
  white-space: nowrap;
}
.chip-cat { background: rgba(64, 158, 255, 0.08); color: var(--pl-primary); }
.chip-ghost { background: transparent; border: 1px dashed #e3e8ef; color: var(--pl-text-mute); }
.muted { color: var(--pl-text-mute); font-size: 12px; }

/* 窄卡片上价格与按钮会挤在一行，这里允许折行：按钮折到下一行右对齐 */
.card-foot {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px 12px;
  padding: 12px 18px;
  border-top: 1px solid #f3f5f9;
  background: #fcfdff;
}
.price { font-size: 13px; font-weight: 500; white-space: nowrap; }
.price.paid { color: var(--pl-warn); }
.price.trial { color: var(--pl-primary); }
.price.free { color: var(--pl-ok); }
.actions { display: flex; gap: 8px; flex-wrap: wrap; justify-content: flex-end; align-items: center; }

/* 骨架屏 */
.plugin-card.is-skeleton { padding-bottom: 0; }
.sk-block { background: #eef1f6; }
.sk-line {
  height: 14px;
  border-radius: 6px;
  background: #eef1f6;
  margin-top: 10px;
}
.sk-line--sm { height: 11px; }
.plugin-card.is-skeleton .card-body { padding-top: 16px; }

/* 空状态 */
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

/* Element Plus 没有对应语义色，这里给按钮补两个变体 */
.actions :deep(.btn-warn),
.col-act :deep(.btn-warn) {
  background-color: rgba(245, 158, 11, 0.1);
  border-color: transparent;
  color: var(--pl-warn);
}
.actions :deep(.btn-warn:hover),
.col-act :deep(.btn-warn:hover) {
  background-color: rgba(245, 158, 11, 0.18);
  border-color: transparent;
  color: var(--pl-warn);
}
.actions :deep(.btn-ok),
.col-act :deep(.btn-ok) {
  background-color: rgba(16, 185, 129, 0.12);
  border-color: transparent;
  color: #059669;
}
.actions :deep(.btn-ok:hover),
.col-act :deep(.btn-ok:hover) {
  background-color: rgba(16, 185, 129, 0.2);
  border-color: transparent;
  color: #059669;
}

/* ── 列表视图 ── */
.list-panel {
  margin-top: 16px;
  border: 1px solid var(--pl-line);
  border-radius: var(--pl-radius);
  background: #fff;
  min-height: 160px;
  overflow: hidden;
}
.list-head,
.list-row {
  display: grid;
  grid-template-columns: minmax(200px, 1.6fr) minmax(140px, 1fr) 80px 92px 104px minmax(200px, auto);
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
}
.list-head {
  background: #fafbfd;
  border-bottom: 1px solid var(--pl-line);
  color: var(--pl-text-sub);
  font-size: 12px;
}
.list-row { border-bottom: 1px solid #f5f7fa; font-size: 13px; transition: background 0.15s ease; }
.list-row:last-child { border-bottom: 0; }
.list-row:hover { background: #f8fafd; }
.col-main { display: flex; align-items: center; gap: 10px; min-width: 0; }
.plugin-icon {
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  flex-shrink: 0;
}
.plugin-icon.is-sm { width: 38px; height: 38px; border-radius: 10px; font-size: 16px; }
.plugin-icon img { width: 100%; height: 100%; object-fit: cover; }
.list-name { min-width: 0; }
.list-name strong {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.list-name .code { color: var(--pl-text-mute); font: 12px Consolas, monospace; }
.col-cat { display: flex; flex-wrap: wrap; gap: 6px; align-items: center; }
.col-ver { color: var(--pl-text-sub); font-size: 12px; }
.col-act { display: flex; gap: 8px; align-items: center; justify-content: flex-end; flex-wrap: wrap; }
.list-panel .empty-block { border: 0; border-top: 1px dashed #eef1f5; }

@media (max-width: 1280px) {
  .summary-hint { display: none; }
}

@media (max-width: 1200px) {
  .list-head { display: none; }
  .list-row { grid-template-columns: 1fr; gap: 8px; }
  .col-act { justify-content: flex-start; }
  .hero-art { display: none; }
}

@media (max-width: 900px) {
  .addons-page { padding: 12px; }
  .hero { padding: 22px 20px; }
  .hero h1 { font-size: 22px; line-height: 30px; }
  .summary-strip { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .filter-search { width: 100%; }
  .segmented { width: 100%; overflow-x: auto; }
  .card-grid { grid-template-columns: 1fr; }
}
</style>
