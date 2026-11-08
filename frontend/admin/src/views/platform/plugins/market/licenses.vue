<template>
  <div class="license-page">
    <header class="page-header">
      <div>
        <h1>授权管理</h1>
        <p>查看本站点的插件授权、手动激活授权码、解绑不再使用的绑定</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Refresh" @click="openActivate">激活授权码</el-button>
        <el-button :icon="Grid" @click="goMarket">插件市场</el-button>
        <el-button :icon="Refresh" :loading="loading" @click="loadLicenses">刷新</el-button>
      </div>
    </header>

    <el-alert type="info" :closable="false" class="scope-tip">
      <template #title>
        当前站点实例：<strong>{{ siteInstanceId || '（未能识别域名）' }}</strong>
        本页面只展示绑定到该实例的授权；未绑定实例的授权不会出现在列表中。
      </template>
    </el-alert>

    <el-table v-loading="loading" :data="licenses" border stripe empty-text="当前站点暂无已激活授权">
      <el-table-column prop="pluginCode" label="插件" min-width="200">
        <template #default="{ row }">
          <div class="plugin-cell">
            <span class="plugin-name">{{ pluginNameOf(row.pluginCode) }}</span>
            <span class="plugin-code">{{ row.pluginCode }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="licenseType" label="类型" width="110">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.licenseType)" size="small">{{ typeLabel(row.licenseType) }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="licenseKey" label="授权码" min-width="260">
        <template #default="{ row }">
          <div class="key-cell">
            <code>{{ revealed.has(row.licenseKey) ? row.licenseKey : maskKey(row.licenseKey) }}</code>
            <el-button link type="primary" @click="toggleReveal(row.licenseKey)">
              {{ revealed.has(row.licenseKey) ? '隐藏' : '显示' }}
            </el-button>
            <el-button link type="primary" @click="copyKey(row.licenseKey)">复制</el-button>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="instanceId" label="绑定实例" width="180">
        <template #default="{ row }">
          <span>{{ row.instanceId || '—' }}</span>
        </template>
      </el-table-column>

      <el-table-column label="有效期" width="180">
        <template #default="{ row }">
          <span :class="{ expired: isExpired(row) }">
            {{ expireText(row) }}
          </span>
        </template>
      </el-table-column>

      <el-table-column label="官方维护" min-width="200">
        <template #default="{ row }">
          <el-tag :type="maintenanceTag(row)" size="small" effect="plain">
            {{ maintenanceText(row) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="goDetail(row.pluginCode)">去安装</el-button>
          <el-button
            v-if="needsRenew(row)"
            link
            type="warning"
            @click="goRenew(row.pluginCode)"
          >
            续费维护
          </el-button>
          <el-button link type="danger" @click="handleUnbind(row)">解绑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <p class="hint">
      说明：<b>解绑</b>只解除本实例与授权码的绑定关系，授权本身仍然有效、可在其它实例重新激活；
      平台侧的<b>吊销</b>才会让授权永久作废。插件仍处于安装状态时后端会拒绝解绑，请先在插件中心卸载。
    </p>

    <el-dialog v-model="activateVisible" title="激活授权码" width="480px">
      <el-form label-width="90px">
        <el-form-item label="授权码">
          <el-input
            v-model="activateKey"
            placeholder="HOU-XXXXXXXX-XXXXXXXXXXXXXXXX"
            clearable
            @keyup.enter="submitActivate"
          />
        </el-form-item>
        <el-form-item label="绑定域名">
          <el-input :model-value="siteInstanceId" readonly />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="activateVisible = false">取消</el-button>
        <el-button type="primary" :loading="activating" @click="submitActivate">确认激活</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Grid, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { activateLicense, getMarketList, getMyLicenses, unbindMyLicense } from '@/api/plugin-center/pluginMarket'
import { getSiteInstanceId } from '@/utils/site'
import { formatMaintenanceCountdown } from '@/utils/pluginInstallFeedback'

const router = useRouter()

/** 本站点实例标识：后端按此字段做站点隔离 */
const siteInstanceId = getSiteInstanceId()

const loading = ref(false)
const activating = ref(false)
const licenses = ref([])
const pluginNames = ref({})
const revealed = ref(new Set())

const activateVisible = ref(false)
const activateKey = ref('')

const goMarket = () => router.push('/platform/plugins/market')
const goDetail = (pluginCode) => router.push(`/platform/plugins/market/detail?code=${encodeURIComponent(pluginCode)}`)
const goRenew = (pluginCode) => router.push(`/platform/plugins/market/checkout?code=${encodeURIComponent(pluginCode)}&renew=1`)

const pluginNameOf = (code) => pluginNames.value[code] || code

const maintenanceText = (row) => formatMaintenanceCountdown(row.maintenanceUntil || row.maintenance_until).text
const maintenanceTag = (row) => {
  const level = formatMaintenanceCountdown(row.maintenanceUntil || row.maintenance_until).level
  return ({ ok: 'success', warn: 'warning', danger: 'danger', none: 'info' })[level] || 'info'
}
const needsRenew = (row) => {
  const level = formatMaintenanceCountdown(row.maintenanceUntil || row.maintenance_until).level
  return level === 'warn' || level === 'danger' || level === 'none'
}

const loadLicenses = async () => {
  loading.value = true
  try {
    const data = await getMyLicenses(siteInstanceId)
    licenses.value = Array.isArray(data) ? data : []
    await loadPluginNames()
  } catch (error) {
    ElMessage.error(error?.message || '加载授权列表失败')
    licenses.value = []
  } finally {
    loading.value = false
  }
}

/** 市场侧插件名 -> 编码映射，仅用于列表可读性；失败不影响授权数据本身 */
const loadPluginNames = async () => {
  try {
    const data = await getMarketList({ instanceId: siteInstanceId })
    const map = {}
    for (const item of data?.list ?? []) {
      if (item?.code) {
        map[item.code] = item.name || item.code
      }
    }
    pluginNames.value = map
  } catch {
    /* 取名失败不影响主体数据 */
  }
}

const typeLabel = (type) => ({ FREE: '免费', PAID: '付费', TRIAL: '试用' }[String(type).toUpperCase()] || type || '—')
const typeTag = (type) => ({ FREE: 'info', PAID: 'success', TRIAL: 'warning' }[String(type).toUpperCase()] || 'info')

const isExpired = (row) => !row.permanent && row.expireTime && new Date(row.expireTime).getTime() < Date.now()
const expireText = (row) => (row.permanent ? '永久有效' : row.expireTime ? `有效期至 ${row.expireTime}` : '—')

const maskKey = (key = '') => (key.length > 16 ? `${key.slice(0, 12)}********${key.slice(-4)}` : '****')

const toggleReveal = (key) => {
  const next = new Set(revealed.value)
  if (next.has(key)) {
    next.delete(key)
  } else {
    next.add(key)
  }
  revealed.value = next
}

const copyKey = async (key) => {
  try {
    await navigator.clipboard.writeText(key)
    ElMessage.success('授权码已复制')
  } catch {
    ElMessage.warning('复制失败，请手动选择复制')
  }
}

const openActivate = () => {
  activateKey.value = ''
  activateVisible.value = true
}

const submitActivate = async () => {
  const key = activateKey.value.trim()
  if (!key) {
    ElMessage.warning('请输入授权码')
    return
  }
  activating.value = true
  try {
    await activateLicense(key, undefined, siteInstanceId)
    ElMessage.success('授权已激活')
    activateVisible.value = false
    await loadLicenses()
  } catch (error) {
    ElMessage.error(error?.message || '激活失败，请核对授权码')
  } finally {
    activating.value = false
  }
}

const handleUnbind = async (row) => {
  try {
    await ElMessageBox.confirm(
      `解绑后「${pluginNameOf(row.pluginCode)}」在当前站点将失去授权，需先卸载插件才能重新安装。确定解绑吗？`,
      '解绑确认',
      { type: 'warning', confirmButtonText: '确定解绑', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  try {
    await unbindMyLicense(row.licenseKey, siteInstanceId)
    ElMessage.success('已解绑，该授权码可在其它实例重新激活')
    await loadLicenses()
  } catch (error) {
    ElMessage.error(error?.message || '解绑失败')
  }
}

onMounted(loadLicenses)
</script>

<style scoped>
.license-page {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.page-header h1 {
  margin: 0 0 6px;
  font-size: 20px;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.scope-tip {
  margin-bottom: 16px;
}

.plugin-cell {
  display: flex;
  flex-direction: column;
  line-height: 1.5;
}

.plugin-name {
  font-weight: 500;
}

.plugin-code {
  color: #909399;
  font-size: 12px;
}

.key-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.key-cell code {
  font-size: 12px;
  word-break: break-all;
}

.expired {
  color: #f56c6c;
}

.hint {
  margin-top: 14px;
  color: #909399;
  font-size: 12px;
  line-height: 1.7;
}
</style>
