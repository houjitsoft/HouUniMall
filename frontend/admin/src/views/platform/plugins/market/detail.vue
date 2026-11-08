<template>
  <div class="market-detail">
    <header class="page-header">
      <el-button :icon="ArrowLeft" @click="goBack">返回市场</el-button>
      <el-button :icon="Refresh" :loading="loading" @click="loadDetail">刷新</el-button>
    </header>

    <el-skeleton v-if="loading && !plugin" :rows="6" animated />

    <template v-else-if="plugin">
      <section class="hero">
        <div class="plugin-icon" :style="iconStyle">
          <img v-if="plugin.iconUrl" :src="plugin.iconUrl" :alt="plugin.name" />
          <span v-else>{{ iconText }}</span>
        </div>
        <div class="hero-main">
          <div class="name-row">
            <h2>{{ plugin.name }}</h2>
            <el-tag size="small" effect="plain">{{ plugin.category || '未分类' }}</el-tag>
            <el-tag v-if="plugin.installed" type="success" size="small" effect="dark">已安装</el-tag>
            <el-tag
              v-if="needsLicense"
              :type="plugin.licensed ? 'success' : 'warning'"
              size="small"
              effect="plain"
            >
              {{ purchaseStatusText }}
            </el-tag>
          </div>
          <div class="code">{{ plugin.code }}</div>
          <p class="desc">{{ plugin.description || plugin.shortDesc || '暂无简介' }}</p>
          <div class="meta-row">
            <el-tag v-for="tag in plugin.tags || []" :key="tag" size="small" type="info" effect="plain">
              {{ tag }}
            </el-tag>
          </div>
        </div>
        <div class="hero-side">
          <div class="price" :class="priceClass">{{ priceText }}</div>
          <button
            v-if="hostMissing"
            type="button"
            class="host-miss-btn host-miss-btn--outline"
            :title="plugin.hostUnavailableReason || '点击查看如何购买/安装宿主插件'"
            @click.stop="onHostHintClick"
          >{{ hostHint }}</button>
          <el-button
            v-if="needsLicense && !plugin.licensed"
            type="primary"
            @click="handlePurchase"
          >
            立即购买
          </el-button>
          <el-button
            v-if="needsLicense && !plugin.licensed"
            type="warning"
            plain
            @click="handleActivate"
          >
            激活授权
          </el-button>
          <el-button
            v-if="needsLicense && plugin.licensed && maintenanceLevel !== 'ok'"
            type="warning"
            @click="goRenew"
          >
            续费维护
          </el-button>
          <el-button
            v-if="!plugin.installed"
            type="primary"
            :loading="installing === latestVersion"
            :disabled="!latestVersion || !licensed"
            @click="handleInstall(latestVersion)"
          >
            安装最新版
          </el-button>
          <el-button
            v-else-if="canUpgrade"
            type="warning"
            :loading="upgrading"
            :disabled="!licensed"
            @click="openUpgrade(latestVersion)"
          >
            升级到 {{ latestVersion }}
          </el-button>
          <el-button v-else type="success" plain @click="goAddons">去插件中心管理</el-button>
          <template v-if="plugin.licensed && isStandaloneService(pluginCode)">
            <el-button plain @click="downloadArtifact('source')">下载源码（Apache-2.0）</el-button>
            <el-button plain @click="downloadArtifact('runtime')">下载容器运行包</el-button>
          </template>
        </div>
      </section>

      <section class="info-grid">
        <div class="info-item"><span>最新版本</span><strong>{{ plugin.latestVersion || '-' }}</strong></div>
        <div class="info-item"><span>下载次数</span><strong>{{ plugin.downloadCount ?? 0 }}</strong></div>
        <div class="info-item"><span>安装次数</span><strong>{{ plugin.installCount ?? 0 }}</strong></div>
        <div class="info-item"><span>授权方式</span><strong>{{ licenseText }}</strong></div>
        <div class="info-item"><span>授权状态</span><strong>{{ licenseStatusText }}</strong></div>
        <div class="info-item"><span>官方维护</span><strong :class="maintenanceClass">{{ maintenanceText }}</strong></div>
      </section>

      <section class="detail-grid">
        <div class="panel">
          <h3>兼容与依赖</h3>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="适用端">
              {{ (plugin.terminals || []).join('、') || '未声明' }}
            </el-descriptions-item>
            <el-descriptions-item label="依赖插件">
              {{ (plugin.dependencies || []).join('、') || '无' }}
            </el-descriptions-item>
            <el-descriptions-item label="评分">
              {{ plugin.ratingAvg ?? 0 }}（{{ plugin.ratingCount ?? 0 }} 人）
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="panel">
          <h3>安装说明</h3>
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="安装会做什么"
            description="下载该版本安装包 → 校验 MD5 摘要 → 落盘到站点插件目录 → 登记安装状态。"
          />
          <el-alert
            class="tip"
            type="warning"
            :closable="false"
            show-icon
            title="付费插件说明"
            description="付费插件必须先在本站点激活授权码才能安装；授权码由平台在购买后签发，本站点只能激活。插件在业务进程内真实生效仍由宿主接入后启用。"
          />
        </div>
      </section>

      <section class="table-panel">
        <div class="panel-head">
          <h3>可用版本</h3>
          <span class="muted">共 {{ versions.length }} 个已审核版本</span>
        </div>
        <el-table v-loading="loading" :data="versions" row-key="id">
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="changelog">
                <div class="changelog-label">更新说明</div>
                <pre>{{ row.changelog || '该版本未填写更新说明' }}</pre>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="版本" width="120">
            <template #default="{ row }"><strong>{{ row.version }}</strong></template>
          </el-table-column>
          <el-table-column label="发布时间" width="160">
            <template #default="{ row }">{{ formatTime(row.publishTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="!plugin.installed || plugin.installedVersion === row.version"
                type="primary"
                link
                :loading="installing === row.version"
                :disabled="!licensed || plugin.installedVersion === row.version"
                @click="handleInstall(row.version)"
              >
                {{ plugin.installedVersion === row.version ? '当前版本' : '安装此版本' }}
              </el-button>
              <el-button
                v-else
                type="warning"
                link
                :loading="upgrading && upgradeTarget === row.version"
                :disabled="!licensed"
                @click="openUpgrade(row.version)"
              >
                升级到此版本
              </el-button>
            </template>
          </el-table-column>
          <template #empty><el-empty description="该插件暂无已审核版本" /></template>
        </el-table>
      </section>
    </template>

    <el-empty v-else description="未找到该插件，请从插件市场重新进入" />

    <PluginUpgradeRiskDialog
      v-model="upgradeDialogVisible"
      :plugin-code="pluginCode"
      :target-version="upgradeTarget"
      :host-service="pluginCode"
      :maintenance-until="plugin?.maintenanceUntil || plugin?.license?.maintenanceUntil || plugin?.licenseExpireTime"
      :confirming="upgrading"
      @confirm="submitUpgrade"
    />
  </div>
</template>

<script setup>
import { isStandaloneService, installServiceAndWait, downloadServiceArtifact, upgradeServiceAndWait } from '@/api/plugin-center/service'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  hostUnavailable,
  hostHintText,
  promptHostRequired
} from './hostRequiredPrompt'
import { activateLicense, getMarketDetail, getMarketVersions } from '@/api/plugin-center/pluginMarket'
import { installAndWait, upgradeAndWait } from '@/api/plugin-center/pluginUpdate'
import { getSiteInstanceId } from '@/utils/site'
import { formatInstallFailure, formatMaintenanceCountdown } from '@/utils/pluginInstallFeedback'
import { refreshPluginApplications } from '@/utils/pluginApplicationRefresh'
import PluginUpgradeRiskDialog from './components/PluginUpgradeRiskDialog.vue'

const route = useRoute()
const router = useRouter()

/** 本站点实例标识：激活时必须带上，否则授权不绑定站点，「授权管理」页会查不到 */
const siteInstanceId = getSiteInstanceId()

const loading = ref(false)
const plugin = ref(null)
const versions = ref([])
const installing = ref('')
/** 安装进行中的流水线阶段（DOWNLOAD / VERIFY / COMPAT / BACKUP / STOP / SCHEMA / START / HEALTH） */
const installStep = ref('')
const installVersion = ref('')
const upgradeDialogVisible = ref(false)
const upgradeTarget = ref('')
const upgrading = ref(false)

const pluginCode = computed(() => String(route.query.code || '').trim())
const latestVersion = computed(
  () => plugin.value?.latestVersion || versions.value[0]?.version || ''
)
const canUpgrade = computed(() => {
  if (!plugin.value?.installed) return false
  const current = String(plugin.value?.installedVersion || '').trim()
  const latest = String(latestVersion.value || '').trim()
  return Boolean(latest) && current !== latest
})
const iconText = computed(() => String(plugin.value?.name || '?').trim().charAt(0))
const iconStyle = computed(() => {
  const palette = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#7c4dff', '#13c2c2']
  const code = String(plugin.value?.code || '')
  const hash = [...code].reduce((acc, ch) => acc + ch.charCodeAt(0), 0)
  return { background: palette[hash % palette.length] }
})
const priceClass = computed(() => {
  const type = String(plugin.value?.licenseType || 'FREE').toUpperCase()
  if (type === 'PAID') return 'paid'
  return 'free'
})
const licenseText = computed(() => {
  const type = String(plugin.value?.licenseType || 'FREE').toUpperCase()
  if (type === 'PAID') return '付费'
  return '免费'
})

/** 付费插件：必须先激活授权码才能安装 */
const needsLicense = computed(() => String(plugin.value?.licenseType || '').toUpperCase() === 'PAID')
const hostMissing = computed(() => hostUnavailable(plugin.value))
const hostHint = computed(() => hostHintText(plugin.value, plugin.value ? [plugin.value] : []))

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

const guardHostThen = async (next) => {
  if (!hostMissing.value) {
    next()
    return
  }
  await promptHostRequired(plugin.value, {
    catalog: [],
    onGoHost: goHostPlugin
  })
}

/** 右侧镂空「需 xx 服务」按钮：打开宿主引导弹窗 */
const onHostHintClick = () => {
  guardHostThen(() => {})
}

const priceText = computed(() => plugin.value?.priceText || (needsLicense.value ? '未定价' : '免费'))
const licensed = computed(() => !needsLicense.value || Boolean(plugin.value?.licensed))
const licenseStatusText = computed(() => {
  if (!needsLicense.value) return '无需授权'
  if (!plugin.value?.licensed) return '未授权'
  return plugin.value?.licenseExpireTime ? `已授权（至 ${plugin.value.licenseExpireTime}）` : '已授权（永久）'
})
const purchaseStatusText = computed(() => {
  if (plugin.value?.purchaseStatus === 'PURCHASED' || plugin.value?.licensed) return '已购买'
  if (plugin.value?.purchaseStatus === 'FREE' || !needsLicense.value) return '免费'
  return '未购买'
})

const maintenanceInfo = computed(() =>
  formatMaintenanceCountdown(plugin.value?.maintenanceUntil || plugin.value?.license?.maintenanceUntil)
)
const maintenanceText = computed(() => maintenanceInfo.value.text)
const maintenanceLevel = computed(() => maintenanceInfo.value.level)
const maintenanceClass = computed(() => ({
  'mt-ok': maintenanceLevel.value === 'ok',
  'mt-warn': maintenanceLevel.value === 'warn',
  'mt-danger': maintenanceLevel.value === 'danger',
}))

const goRenew = () => {
  router.push(`/platform/plugins/market/checkout?code=${encodeURIComponent(pluginCode.value)}&renew=1`)
}

const formatTime = (value) => {
  if (!value) return '-'
  const text = String(value).replace('T', ' ')
  return text.length >= 16 ? text.slice(0, 16) : text
}

const loadDetail = async () => {
  if (!pluginCode.value) {
    ElMessage.warning('缺少插件编码，请从插件市场进入')
    return
  }
  loading.value = true
  try {
    const data = await getMarketDetail(pluginCode.value, siteInstanceId)
    plugin.value = data?.plugin ?? null
    if (data?.versions?.length) {
      versions.value = data.versions
    } else {
      // 详情未带版本列表时，回源租户视角的已发布版本接口（含 changelog 与兼容性判定）
      versions.value = await getMarketVersions(pluginCode.value, plugin.value?.version)
        .catch(() => [])
    }
  } catch (error) {
    plugin.value = null
    versions.value = []
    ElMessage.error(error?.message || '获取插件详情失败')
  } finally {
    loading.value = false
  }
}

const handleInstall = (version) => {
  guardHostThen(() => {
    // 运行方式由插件清单 plugin.yaml 声明、安装链路按清单执行，前端无需再选——直接安装。
    installVersion.value = version || latestVersion.value
    submitInstall()
  })
}
const downloadArtifact = async (kind) => {
  try { await downloadServiceArtifact(pluginCode.value, latestVersion.value, kind) }
  catch (error) { ElMessage.error(error.message || '下载失败') }
}

const submitInstall = async () => {
  const name = plugin.value?.name || pluginCode.value
  const version = installVersion.value
  installing.value = version || 'latest'
  try {
    // 走 plugin-update 编排链路：下载 → 四重校验 → 兼容性预检 → 建表 → 装载 → 健康检查。
    // 运行方式由包内 plugin.yaml 的 runtime 决定，前端不再弹窗选择，点安装直接执行。
    const installer = isStandaloneService(pluginCode.value) ? installServiceAndWait : installAndWait
    const { result, finalStatus } = await installer(pluginCode.value, version, (status) => {
      installStep.value = status?.step || status?.data?.step || ''
    })
    const state = String(finalStatus?.state ?? result?.state ?? '').toUpperCase()
    if (state === 'FAILED') {
      ElMessage.error(formatInstallFailure(finalStatus || result))
    } else if (state === 'ROLLED_BACK') {
      ElMessage.warning(formatInstallFailure({ ...(finalStatus || result || {}), state: 'ROLLED_BACK' }))
    } else if (state === 'SUCCESS') {
      ElMessage.success(`「${name}」安装成功（v${finalStatus?.currentVersion || result?.version || version || '-'}）`)
      await refreshPluginApplications()
    }
    await loadDetail()
  } catch (error) {
    ElMessage.error(formatInstallFailure(error?.installStatus || { errorMsg: error?.message, state: 'FAILED' }))
  } finally {
    installing.value = ''
    installStep.value = ''
  }
}

const openUpgrade = (version) => {
  if (!licensed.value) {
    ElMessage.warning('请先购买或激活授权后再升级')
    return
  }
  upgradeTarget.value = version || latestVersion.value
  upgradeDialogVisible.value = true
}

const submitUpgrade = async () => {
  const name = plugin.value?.name || pluginCode.value
  const version = upgradeTarget.value
  upgrading.value = true
  try {
    const upgrader = isStandaloneService(pluginCode.value) ? upgradeServiceAndWait : upgradeAndWait
    const { result, finalStatus } = await upgrader(pluginCode.value, version, (status) => {
      installStep.value = status?.step || status?.data?.step || ''
    })
    const state = String(finalStatus?.state ?? result?.state ?? '').toUpperCase()
    if (state === 'FAILED') {
      ElMessage.error(formatInstallFailure(finalStatus || result))
    } else if (state === 'ROLLED_BACK') {
      ElMessage.warning(formatInstallFailure({ ...(finalStatus || result || {}), state: 'ROLLED_BACK' }))
    } else if (state === 'SUCCESS') {
      ElMessage.success(`「${name}」已升级到 v${version || finalStatus?.currentVersion || '-'}`)
      upgradeDialogVisible.value = false
      await refreshPluginApplications()
    }
    await loadDetail()
  } catch (error) {
    ElMessage.error(formatInstallFailure(error?.installStatus || { errorMsg: error?.message, state: 'FAILED' }))
  } finally {
    upgrading.value = false
    installStep.value = ''
  }
}

const handleActivate = async () => {
  let input
  try {
    input = await ElMessageBox.prompt(
      '付费插件需先激活授权码，激活成功后方可安装。请输入平台签发的授权码：',
      '激活插件授权',
      {
        confirmButtonText: '确认激活',
        cancelButtonText: '取消',
        inputPlaceholder: 'HOU-XXXXXXXX-XXXXXXXXXXXXXXXX',
        inputValidator: (value) => (String(value || '').trim() ? true : '请输入授权码')
      }
    )
  } catch {
    return
  }
  try {
    const res = await activateLicense(String(input.value).trim(), pluginCode.value, siteInstanceId)
    const data = res?.data ?? {}
    const expireText = data.permanent ? '永久有效' : `有效期至 ${data.expireTime || '-'}`
    ElMessage.success(`「${plugin.value?.name || pluginCode.value}」授权已激活（${expireText}）`)
    await loadDetail()
  } catch (error) {
    ElMessage.error(error?.message || '激活失败，请核对授权码')
  }
}

const goBack = () => {
  router.push('/platform/plugins/market')
}

const goAddons = () => {
  router.push('/platform/plugins')
}

/** 在线购买：下单 → 支付 → 自动发证并绑定本站点；缺宿主时先引导去宿主货架 */
const handlePurchase = () => {
  if (!pluginCode.value) {
    ElMessage.warning('缺少插件编码，请从插件市场进入')
    return
  }
  guardHostThen(() => {
    router.push(`/platform/plugins/market/checkout?code=${encodeURIComponent(pluginCode.value)}`)
  })
}

onMounted(loadDetail)
</script>

<style scoped>
.market-detail{padding:20px;min-width:0;color:#303133}
.page-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:16px}
.hero{display:flex;align-items:flex-start;gap:16px;padding:20px;border:1px solid #e4e7ed;border-radius:6px;background:#fff}
.plugin-icon{display:flex;align-items:center;justify-content:center;width:64px;height:64px;border-radius:10px;color:#fff;font-size:28px;font-weight:600;flex:0 0 auto;overflow:hidden}
.plugin-icon img{width:100%;height:100%;object-fit:cover}
.hero-main{flex:1 1 auto;min-width:0}
.name-row{display:flex;align-items:center;gap:8px;flex-wrap:wrap}
.name-row h2{margin:0;font-size:20px;line-height:26px}
.code{margin-top:4px;color:#909399;font:12px Consolas,monospace}
.desc{margin:10px 0 0;color:#606266;font-size:13px;line-height:21px}
.meta-row{display:flex;flex-wrap:wrap;gap:6px;margin-top:10px}
.hero-side{display:flex;flex-direction:column;align-items:flex-end;gap:10px;flex:0 0 auto}
.host-miss-btn{
  max-width:240px;
  height:32px;
  padding:0 12px;
  border-radius:6px;
  font-size:12px;
  font-weight:500;
  line-height:30px;
  white-space:nowrap;
  cursor:pointer;
  pointer-events:auto;
  position:relative;
  z-index:2;
}
.host-miss-btn--outline{
  border:1px solid #d03050;
  background:transparent;
  color:#d03050;
}
.host-miss-btn--outline:hover{background:rgba(208,48,80,.08)}
.host-miss-btn--outline:active{background:rgba(208,48,80,.14)}
.price{font-size:18px;font-weight:600}
.price.free{color:#67c23a}
.price.paid{color:#e6a23c}
.price.trial{color:#409eff}
.info-grid{display:grid;grid-template-columns:repeat(5,minmax(0,1fr));margin-top:14px;border:1px solid #e4e7ed;border-radius:6px;background:#fff}
.info-item{min-height:84px;padding:16px 20px;border-right:1px solid #ebeef5}
.info-item:last-child{border-right:0}
.info-item span{display:block;color:#909399;font-size:13px}
.info-item strong{display:block;margin-top:8px;font-size:20px;line-height:26px}
.info-item strong.mt-ok{color:#67c23a}
.info-item strong.mt-warn{color:#e6a23c}
.info-item strong.mt-danger{color:#f56c6c}
.detail-grid{display:grid;grid-template-columns:minmax(0,1fr) minmax(0,1fr);gap:14px;margin-top:14px}
.panel{padding:16px;border:1px solid #e4e7ed;border-radius:6px;background:#fff}
.panel h3{margin:0 0 12px;font-size:15px;line-height:22px}
.panel .tip{margin-top:10px}
.table-panel{margin-top:14px;padding:16px;border:1px solid #e4e7ed;border-radius:6px;background:#fff;overflow:hidden}
.panel-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:12px}
.panel-head h3{margin:0;font-size:15px;line-height:22px}
.muted{color:#909399;font-size:12px}
.changelog{padding:8px 12px}
.changelog-label{margin-bottom:6px;color:#909399;font-size:12px}
.changelog pre{margin:0;max-height:200px;overflow:auto;padding:10px 12px;border-radius:4px;background:#f5f7fa;color:#303133;font:12px/18px Consolas,monospace;white-space:pre-wrap}
@media(max-width:1200px){.detail-grid{grid-template-columns:1fr}}
@media(max-width:900px){.market-detail{padding:12px}.hero{flex-wrap:wrap}.info-grid{grid-template-columns:repeat(2,minmax(0,1fr))}}
</style>
