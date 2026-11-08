<template>
  <div v-loading="loading" class="storage-monitor-panel">
    <div class="monitor-toolbar">
      <el-button type="primary" :loading="loading" @click="loadMonitor">刷新监控</el-button>
    </div>

    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">存储配置总数</div>
          <div class="stat-value">{{ monitor.totalConfigs ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="stat-card stat-card--success">
          <div class="stat-label">已启用配置</div>
          <div class="stat-value">{{ monitor.enabledConfigs ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="stat-card stat-card--primary">
          <div class="stat-label">素材库文件</div>
          <div class="stat-value">{{ fileStats.total ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="stat-card stat-card--warning">
          <div class="stat-label">当前生效引擎</div>
          <div class="stat-value stat-value--sm">{{ activeTypeLabel }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <span>当前生效存储</span>
          </template>
          <el-descriptions v-if="monitor.activeConfig" :column="1" border size="small">
            <el-descriptions-item label="存储类型">{{ getStorageTypeName(monitor.activeOssType) }}</el-descriptions-item>
            <el-descriptions-item label="存储桶">{{ monitor.activeConfig.ossBacketName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="目录前缀">{{ monitor.activeConfig.ossFolder || '-' }}</el-descriptions-item>
            <el-descriptions-item label="访问域名">{{ monitor.activeConfig.ossEndpoint || '-' }}</el-descriptions-item>
            <el-descriptions-item label="集群/Region">{{ monitor.activeConfig.ossRegion || '-' }}</el-descriptions-item>
            <el-descriptions-item label="最近更新">{{ monitor.activeConfig.ossUpdateDate || '-' }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="暂无启用的存储配置，请先在「存储配置」中启用一项" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <span>素材库统计</span>
          </template>
          <div class="file-stat-grid">
            <div class="file-stat-item">
              <span class="file-stat-item__label">图片</span>
              <span class="file-stat-item__value">{{ fileStats.image ?? 0 }}</span>
            </div>
            <div class="file-stat-item">
              <span class="file-stat-item__label">视频</span>
              <span class="file-stat-item__value">{{ fileStats.video ?? 0 }}</span>
            </div>
            <div class="file-stat-item">
              <span class="file-stat-item__label">合计</span>
              <span class="file-stat-item__value">{{ fileStats.total ?? 0 }}</span>
            </div>
          </div>
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="说明"
            description="监控数据来自 OSS 配置表与系统素材库（hou_base_modularization_flie）。对象存储侧容量/流量需在各云厂商控制台查看。"
            style="margin-top: 16px"
          />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="panel-card">
      <template #header>
        <span>全部存储配置状态</span>
      </template>
      <el-table :data="monitor.configs || []" stripe border empty-text="暂无存储配置">
        <el-table-column prop="ossType" label="类型" width="140">
          <template #default="{ row }">
            <el-tag :type="getStorageTypeTag(row.ossType)">{{ getStorageTypeName(row.ossType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ossBacketName" label="存储桶" min-width="120" show-overflow-tooltip />
        <el-table-column prop="ossFolder" label="目录" min-width="120" show-overflow-tooltip />
        <el-table-column prop="ossEndpoint" label="访问域名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ossStates" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.ossStates === '01' ? 'success' : 'info'">
              {{ row.ossStates === '01' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ossUpdateDate" label="更新时间" width="170" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { queryStorageMonitor } from '@/api/setting/storage'

const loading = ref(false)
const monitor = reactive({
  totalConfigs: 0,
  enabledConfigs: 0,
  activeOssType: null,
  activeConfig: null,
  configs: [],
  fileStats: { total: 0, image: 0, video: 0 }
})

const fileStats = computed(() => monitor.fileStats || { total: 0, image: 0, video: 0 })

const activeTypeLabel = computed(() => {
  if (!monitor.activeOssType) return '未配置'
  return getStorageTypeName(monitor.activeOssType)
})

const getStorageTypeName = (type) => {
  const map = {
    aliyun: '阿里云 OSS',
    minio: 'MinIO',
    tencentCloud: '腾讯云 COS',
    westFss: '西部数码 FSS'
  }
  return map[type] || type || '-'
}

const getStorageTypeTag = (type) => {
  const map = {
    aliyun: 'success',
    minio: 'warning',
    tencentCloud: 'primary',
    westFss: 'danger'
  }
  return map[type] || 'info'
}

const loadMonitor = async () => {
  loading.value = true
  try {
    const res = await queryStorageMonitor()
    if (res && (res.code === 200 || res.code === 1)) {
      const data = res.data || {}
      Object.assign(monitor, data)
      monitor.fileStats = data.fileStats || { total: 0, image: 0, video: 0 }
    } else {
      ElMessage.warning(res?.message || '加载监控数据失败')
    }
  } catch (error) {
    console.error('加载存储监控失败:', error)
    ElMessage.error('加载存储监控失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadMonitor()
})

defineExpose({ refresh: loadMonitor })
</script>

<style scoped>
.storage-monitor-panel {
  padding-top: 4px;
}
.monitor-toolbar {
  margin-bottom: 16px;
}
.stat-row {
  margin-bottom: 16px;
}
.stat-card {
  margin-bottom: 12px;
  border: 1px solid #eef2f7;
}
.stat-card--success :deep(.stat-value) {
  color: #67c23a;
}
.stat-card--primary :deep(.stat-value) {
  color: #409eff;
}
.stat-card--warning :deep(.stat-value) {
  color: #e6a23c;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}
.stat-value--sm {
  font-size: 18px;
}
.panel-card {
  margin-bottom: 16px;
  border: 1px solid #eef2f7;
}
.file-stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.file-stat-item {
  padding: 16px;
  border-radius: 8px;
  background: #f8fafc;
  text-align: center;
}
.file-stat-item__label {
  display: block;
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}
.file-stat-item__value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}
</style>
