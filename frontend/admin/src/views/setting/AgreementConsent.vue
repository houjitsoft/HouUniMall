<template>
  <div class="agreement-consent">
    <el-tabs v-model="activeTab" class="consent-tabs">
      <el-tab-pane label="本站签署记录" name="local">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <div class="page-header__title">
            协议同意记录
            <el-tag type="info" size="small" effect="plain" class="page-header__tag">注册 / 安装 EULA / 插件购买 / 版本更新补签</el-tag>
          </div>
          <div class="page-header__actions">
            <el-button :loading="exporting" @click="handleExport">导出 Excel</el-button>
            <el-button :loading="loading" @click="fetchList">刷新</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-form" @submit.prevent>
        <el-form-item label="用户">
          <el-input
            v-model="query.username"
            placeholder="用户名 / 手机号"
            clearable
            style="width: 170px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input
            v-model="query.userId"
            placeholder="精确匹配"
            clearable
            style="width: 170px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="协议类型">
          <el-select v-model="query.agreementCode" placeholder="全部" clearable style="width: 200px">
            <el-option
              v-for="item in agreementOptions"
              :key="item.code"
              :label="item.name"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="同意时间">
          <el-date-picker
            v-model="query.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="有效" :value="1" />
            <el-option label="已失效" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="list" border stripe max-height="560">
        <el-table-column prop="consentTime" label="同意时间" width="170" />
        <el-table-column prop="username" label="用户" width="140" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户ID" width="200" show-overflow-tooltip />
        <el-table-column prop="agreementName" label="协议名称" min-width="170" show-overflow-tooltip />
        <el-table-column prop="versionNo" label="版本号" width="100" />
        <el-table-column prop="scene" label="场景" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.scene === 'register' ? 'success' : 'warning'" effect="plain">
              {{ sceneText(row.scene) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" width="140" show-overflow-tooltip />
        <el-table-column prop="ipLocation" label="归属地" width="120" show-overflow-tooltip />
        <el-table-column prop="device" label="设备" width="90" />
        <el-table-column prop="browser" label="浏览器" width="120" show-overflow-tooltip />
        <el-table-column prop="os" label="操作系统" width="120" show-overflow-tooltip />
        <el-table-column prop="source" label="来源" width="90" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '有效' : '已失效' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无同意记录" />
        </template>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.limit"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pager"
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </el-card>
      </el-tab-pane>

      <el-tab-pane label="站点回传记录" name="platform">
        <el-card shadow="never">
          <template #header>
            <div class="page-header">
              <div class="page-header__title">
                站点回传记录
                <el-tag type="info" size="small" effect="plain" class="page-header__tag">各接入站点回传的协议签署凭据（平台汇总）</el-tag>
              </div>
              <div class="page-header__actions">
                <el-button :loading="platformExporting" @click="handlePlatformExport">导出 Excel</el-button>
                <el-button :loading="platformLoading" @click="fetchPlatformList">刷新</el-button>
              </div>
            </div>
            <div v-if="platformSites.length" class="site-chips">
              <el-tag
                v-for="site in platformSites"
                :key="site.siteCode"
                :effect="platformQuery.siteCode === site.siteCode ? 'dark' : 'plain'"
                type="primary"
                class="site-chip"
                @click="toggleSiteFilter(site.siteCode)"
              >
                {{ site.siteName || site.siteCode }}
                <span class="site-chip__meta">{{ site.reportCount }} 次 · {{ (site.lastReportTime || '').slice(0, 16) }}</span>
              </el-tag>
            </div>
          </template>

          <el-form :inline="true" :model="platformQuery" class="search-form" @submit.prevent>
            <el-form-item label="站点">
              <el-select v-model="platformQuery.siteCode" placeholder="全部" clearable style="width: 220px" @change="handlePlatformSearch">
                <el-option
                  v-for="site in platformSites"
                  :key="site.siteCode"
                  :label="`${site.siteName || site.siteCode}（${site.siteCode}）`"
                  :value="site.siteCode"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="协议类型">
              <el-select v-model="platformQuery.agreementCode" placeholder="全部" clearable style="width: 200px" @change="handlePlatformSearch">
                <el-option v-for="item in agreementOptions" :key="item.code" :label="item.name" :value="item.code" />
              </el-select>
            </el-form-item>
            <el-form-item label="场景">
              <el-select v-model="platformQuery.scene" placeholder="全部" clearable style="width: 150px" @change="handlePlatformSearch">
                <el-option label="注册" value="register" />
                <el-option label="重新同意" value="reagree" />
                <el-option label="安装确认（历史）" value="install" />
                <el-option label="插件购买" value="plugin_purchase" />
              </el-select>
            </el-form-item>
            <el-form-item label="用户">
              <el-input v-model="platformQuery.username" placeholder="用户名 / 手机号" clearable style="width: 170px" @keyup.enter="handlePlatformSearch" />
            </el-form-item>
            <el-form-item label="用户ID">
              <el-input v-model="platformQuery.userId" placeholder="精确匹配" clearable style="width: 170px" @keyup.enter="handlePlatformSearch" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="platformLoading" @click="handlePlatformSearch">查询</el-button>
              <el-button @click="handlePlatformReset">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="platformLoading" :data="platformList" border stripe max-height="560">
            <el-table-column label="站点" width="180" show-overflow-tooltip>
              <template #default="{ row }">
                <div class="site-cell">
                  <div class="site-cell__name">{{ row.siteName || row.siteCode }}</div>
                  <div class="site-cell__code">{{ row.siteCode }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="consentTime" label="同意时间" width="170" />
            <el-table-column prop="username" label="用户" width="140" show-overflow-tooltip />
            <el-table-column prop="userId" label="用户ID" width="110" show-overflow-tooltip />
            <el-table-column prop="agreementName" label="协议名称" min-width="170" show-overflow-tooltip />
            <el-table-column prop="versionNo" label="版本号" width="100" />
            <el-table-column prop="scene" label="场景" width="110">
              <template #default="{ row }">
                <el-tag size="small" :type="row.scene === 'register' ? 'success' : 'warning'" effect="plain">
                  {{ sceneText(row.scene) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="source" label="来源" width="90" />
            <el-table-column prop="ip" label="IP" width="140" show-overflow-tooltip />
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handlePlatformDetail(row)">详情</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无站点回传记录" />
            </template>
          </el-table>

          <el-pagination
            v-model:current-page="platformQuery.page"
            v-model:page-size="platformQuery.limit"
            :total="platformTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            class="pager"
            @size-change="fetchPlatformList"
            @current-change="fetchPlatformList"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-drawer v-model="detailVisible" title="同意记录详情" size="480px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="同意时间">{{ detail.consentTime }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ detail.username }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detail.userId }}</el-descriptions-item>
        <el-descriptions-item label="协议名称">{{ detail.agreementName }}</el-descriptions-item>
        <el-descriptions-item label="协议编码">{{ detail.agreementCode }}</el-descriptions-item>
        <el-descriptions-item label="协议版本">{{ detail.versionNo }}</el-descriptions-item>
        <el-descriptions-item label="同意场景">{{ sceneText(detail.scene) }}</el-descriptions-item>
        <el-descriptions-item label="客户端IP">{{ detail.ip }}</el-descriptions-item>
        <el-descriptions-item label="IP归属地">{{ detail.ipLocation || '—' }}</el-descriptions-item>
        <el-descriptions-item label="设备类型">{{ detail.device }}</el-descriptions-item>
        <el-descriptions-item label="浏览器">{{ detail.browser }}</el-descriptions-item>
        <el-descriptions-item label="操作系统">{{ detail.os }}</el-descriptions-item>
        <el-descriptions-item label="来源端">{{ detail.source }}</el-descriptions-item>
        <el-descriptions-item label="记录状态">
          {{ detail.status === 1 ? '有效' : '已失效（已有新版本同意记录）' }}
        </el-descriptions-item>
        <el-descriptions-item label="User-Agent">
          <div class="ua-text">{{ detail.ua }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <el-alert
        class="detail-tip"
        type="info"
        :closable="false"
        show-icon
        title="举证方式"
        description="按「协议编码 + 版本号」在后台协议版本中即可还原用户同意时看到的正文原文。"
      />
    </el-drawer>

    <el-drawer v-model="platformDetailVisible" title="站点回传记录详情" size="480px">
      <el-descriptions v-if="platformDetail" :column="1" border>
        <el-descriptions-item label="站点">{{ platformDetail.siteName || platformDetail.siteCode }}（{{ platformDetail.siteCode }}）</el-descriptions-item>
        <el-descriptions-item label="同意时间">{{ platformDetail.consentTime }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ platformDetail.username }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ platformDetail.userId }}</el-descriptions-item>
        <el-descriptions-item label="协议名称">{{ platformDetail.agreementName }}</el-descriptions-item>
        <el-descriptions-item label="协议编码">{{ platformDetail.agreementCode }}</el-descriptions-item>
        <el-descriptions-item label="协议版本">{{ platformDetail.versionNo }}</el-descriptions-item>
        <el-descriptions-item label="同意场景">{{ sceneText(platformDetail.scene) }}</el-descriptions-item>
        <el-descriptions-item label="来源端">{{ platformDetail.source }}</el-descriptions-item>
        <el-descriptions-item label="客户端IP">{{ platformDetail.ip }}</el-descriptions-item>
        <el-descriptions-item label="User-Agent">
          <div class="ua-text">{{ platformDetail.userAgent }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <el-alert
        class="detail-tip"
        type="info"
        :closable="false"
        show-icon
        title="数据口径"
        description="本记录由接入站点的后端在用户签署时自动回传，是站点内记录的副本；完整审计仍以站点后台为准。"
      />
    </el-drawer>
  </div>
</template>

<script setup>
/**
 * 协议同意记录（后台审计页）
 *
 * 支持按用户、协议类型、时间范围查询与导出，用于用户同意行为的合规审计与举证。
 * 导出在前端生成 Excel：后端 `/web/agreement/consent/export` 返回筛选后的全量数据（上限 20000 条）。
 */
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as XLSX from 'xlsx'
import { agreementApi } from '@/api/setting/agreement'

const loading = ref(false)
const exporting = ref(false)
const list = ref([])
const total = ref(0)
const agreementOptions = ref([])
const detailVisible = ref(false)
const detail = ref(null)

const query = reactive({
  page: 1,
  limit: 20,
  username: '',
  userId: '',
  agreementCode: '',
  dateRange: [],
  status: ''
})

// ================= 站点回传（平台侧） =================
const activeTab = ref('local')
const platformLoading = ref(false)
const platformExporting = ref(false)
const platformList = ref([])
const platformTotal = ref(0)
const platformSites = ref([])
const platformDetailVisible = ref(false)
const platformDetail = ref(null)

const platformQuery = reactive({
  page: 1,
  limit: 20,
  siteCode: '',
  agreementCode: '',
  scene: '',
  username: '',
  userId: ''
})

const sceneText = (scene) => {
  if (scene === 'register') return '注册'
  if (scene === 'reagree') return '重新同意'
  if (scene === 'install') return '安装确认（历史）'
  if (scene === 'plugin_purchase') return '插件购买'
  return scene || '其他'
}

/** 组装后端查询参数 */
const buildPayload = () => {
  const payload = {
    page: query.page,
    limit: query.limit
  }
  if (query.username) payload.username = query.username.trim()
  if (query.userId) payload.userId = query.userId.trim()
  if (query.agreementCode) payload.agreementCode = query.agreementCode
  if (query.status !== '' && query.status !== null && query.status !== undefined) {
    payload.status = query.status
  }
  if (Array.isArray(query.dateRange) && query.dateRange.length === 2) {
    payload.beginTime = query.dateRange[0]
    payload.endTime = query.dateRange[1]
  }
  return payload
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await agreementApi.consentPage(buildPayload())
    if (res && res.code === 1 && res.data) {
      list.value = res.data.list || []
      total.value = res.data.totalCount || 0
    } else {
      list.value = []
      total.value = 0
      ElMessage.warning(res?.message || '查询失败')
    }
  } catch (error) {
    list.value = []
    total.value = 0
    ElMessage.error(error?.message || '查询失败')
  } finally {
    loading.value = false
  }
}

/** 协议类型下拉：取后台协议定义（各场景合并去重） */
const fetchAgreementOptions = async () => {
  try {
    const res = await agreementApi.adminPage({ page: 1, limit: 200 })
    const rows = res?.code === 1 ? (res.data?.list || []) : []
    const map = new Map()
    rows.forEach((item) => {
      if (item.code && !map.has(item.code)) {
        map.set(item.code, { code: item.code, name: item.name })
      }
    })
    agreementOptions.value = Array.from(map.values())
  } catch (error) {
    console.warn('加载协议下拉失败', error)
  }
}

const handleSearch = () => {
  query.page = 1
  fetchList()
}

const handleReset = () => {
  query.page = 1
  query.username = ''
  query.userId = ''
  query.agreementCode = ''
  query.dateRange = []
  query.status = ''
  fetchList()
}

const handleDetail = (row) => {
  detail.value = row
  detailVisible.value = true
}

const handleExport = async () => {
  exporting.value = true
  try {
    const payload = buildPayload()
    delete payload.page
    delete payload.limit
    const res = await agreementApi.consentExport(payload)
    const rows = res?.code === 1 ? (res.data || []) : []
    if (!rows.length) {
      ElMessage.warning('当前条件下没有可导出的记录')
      return
    }
    const sheetRows = rows.map((row) => ({
      同意时间: row.consentTime,
      用户: row.username,
      用户ID: row.userId,
      协议名称: row.agreementName,
      协议编码: row.agreementCode,
      协议版本: row.versionNo,
      同意场景: sceneText(row.scene),
      IP: row.ip,
      IP归属地: row.ipLocation,
      设备类型: row.device,
      浏览器: row.browser,
      操作系统: row.os,
      来源端: row.source,
      状态: row.status === 1 ? '有效' : '已失效',
      'User-Agent': row.ua
    }))
    const sheet = XLSX.utils.json_to_sheet(sheetRows)
    const book = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(book, sheet, '协议同意记录')
    const stamp = new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-')
    XLSX.writeFile(book, `协议同意记录_${stamp}.xlsx`)
    ElMessage.success(`已导出 ${sheetRows.length} 条记录`)
  } catch (error) {
    ElMessage.error(error?.message || '导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  fetchAgreementOptions()
  fetchList()
})

// 首次切到「站点回传」页签再拉数据；每次切换刷新站点清单统计
watch(activeTab, (tab) => {
  if (tab === 'platform') {
    fetchPlatformSites()
    fetchPlatformList()
  }
})

// ================= 站点回传（平台侧） =================

const buildPlatformPayload = () => {
  const payload = { page: platformQuery.page, limit: platformQuery.limit }
  if (platformQuery.siteCode) payload.siteCode = platformQuery.siteCode
  if (platformQuery.agreementCode) payload.agreementCode = platformQuery.agreementCode
  if (platformQuery.scene) payload.scene = platformQuery.scene
  if (platformQuery.username) payload.username = platformQuery.username.trim()
  if (platformQuery.userId) payload.userId = platformQuery.userId.trim()
  return payload
}

const fetchPlatformList = async () => {
  platformLoading.value = true
  try {
    const res = await agreementApi.platformConsentPage(buildPlatformPayload())
    if (res && res.code === 1 && res.data) {
      platformList.value = res.data.list || []
      platformTotal.value = res.data.totalCount || 0
    } else {
      platformList.value = []
      platformTotal.value = 0
      ElMessage.warning(res?.message || '查询失败')
    }
  } catch (error) {
    platformList.value = []
    platformTotal.value = 0
    ElMessage.error(error?.message || '查询失败')
  } finally {
    platformLoading.value = false
  }
}

const fetchPlatformSites = async () => {
  try {
    const res = await agreementApi.platformConsentSites()
    platformSites.value = res?.code === 1 ? (res.data || []) : []
  } catch (error) {
    console.warn('加载接入站点失败', error)
  }
}

const toggleSiteFilter = (siteCode) => {
  platformQuery.siteCode = platformQuery.siteCode === siteCode ? '' : siteCode
  handlePlatformSearch()
}

const handlePlatformSearch = () => {
  platformQuery.page = 1
  fetchPlatformList()
}

const handlePlatformReset = () => {
  platformQuery.page = 1
  platformQuery.siteCode = ''
  platformQuery.agreementCode = ''
  platformQuery.scene = ''
  platformQuery.username = ''
  platformQuery.userId = ''
  fetchPlatformList()
}

const handlePlatformDetail = (row) => {
  platformDetail.value = row
  platformDetailVisible.value = true
}

/** 平台导出：后端无 export 专用接口，前端按 100/页翻页拉全量（上限 20000）后生成 Excel */
const handlePlatformExport = async () => {
  platformExporting.value = true
  try {
    const rows = []
    const pageSize = 100
    for (let pageNo = 1; rows.length < 20000; pageNo++) {
      const res = await agreementApi.platformConsentPage({ ...buildPlatformPayload(), page: pageNo, limit: pageSize })
      const list = res?.code === 1 ? (res.data?.list || []) : []
      rows.push(...list)
      const totalCount = res?.data?.totalCount || 0
      if (list.length < pageSize || rows.length >= totalCount) break
    }
    if (!rows.length) {
      ElMessage.warning('当前条件下没有可导出的记录')
      return
    }
    const sheetRows = rows.map((row) => ({
      站点: row.siteName || row.siteCode,
      站点编码: row.siteCode,
      同意时间: row.consentTime,
      用户: row.username,
      用户ID: row.userId,
      协议名称: row.agreementName,
      协议编码: row.agreementCode,
      协议版本: row.versionNo,
      同意场景: sceneText(row.scene),
      来源端: row.source,
      IP: row.ip,
      'User-Agent': row.userAgent
    }))
    const sheet = XLSX.utils.json_to_sheet(sheetRows)
    const book = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(book, sheet, '站点回传记录')
    const stamp = new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-')
    XLSX.writeFile(book, `站点回传记录_${stamp}.xlsx`)
    ElMessage.success(`已导出 ${sheetRows.length} 条记录`)
  } catch (error) {
    ElMessage.error(error?.message || '导出失败')
  } finally {
    platformExporting.value = false
  }
}
</script>

<style scoped>
.agreement-consent {
  padding: 16px;
}

/* 站点回传：接入站点统计标签（点击筛选） */
.site-chips {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.site-chip {
  cursor: pointer;
  height: auto;
  padding: 4px 10px;
  line-height: 1.4;
}

.site-chip__meta {
  margin-left: 6px;
  font-size: 12px;
  opacity: 0.75;
}

/* 站点列：名称 + 编码两行 */
.site-cell__name {
  font-weight: 500;
}

.site-cell__code {
  font-size: 12px;
  color: var(--el-text-color-secondary, #909399);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.page-header__title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-header__tag {
  font-weight: normal;
}

.search-form {
  margin-bottom: 8px;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.pager {
  margin-top: 16px;
  justify-content: flex-end;
}

.ua-text {
  word-break: break-all;
  line-height: 1.6;
  font-size: 12px;
  color: #606266;
}

.detail-tip {
  margin-top: 16px;
}
</style>
