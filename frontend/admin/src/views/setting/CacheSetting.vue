<template>
  <div class="cache-setting-container">
    <div class="page-header">
      <h2>缓存设置</h2>
      <p>配置系统缓存相关参数（运行时参数来自 commons + Nacos redis.yaml）</p>
    </div>

    <el-alert
      v-if="runtimeInfo.redisHost"
      type="info"
      :closable="false"
      show-icon
      class="runtime-alert"
      :title="`Redis 运行时：${runtimeInfo.redisHost}:${runtimeInfo.redisPort || '-'} / db${runtimeInfo.redisDatabase ?? '-'}`"
    >
      <template #default>
        <div class="runtime-lines">
          <span>连接状态：{{ runtimeInfo.redisConnected ? '正常' : '异常' }}</span>
          <span>命令超时：{{ runtimeInfo.redisCommandTimeoutMs || '-' }} ms</span>
          <span>Token TTL（HouConfig）：{{ runtimeInfo.runtimeTokenExpire || '-' }} 秒</span>
          <span v-if="runtimeInfo.redisMemoryUsedMb">已用内存：{{ runtimeInfo.redisMemoryUsedMb }} MB</span>
        </div>
      </template>
    </el-alert>

    <el-card v-loading="loading" class="config-card">
      <template #header>
        <span>缓存配置</span>
      </template>

      <el-form :model="cacheConfig" label-width="200px" class="config-form">
        <el-divider content-position="left">Redis缓存配置</el-divider>

        <el-form-item label="Redis过期时间">
          <el-input-number
            v-model="cacheConfig.redisExpire"
            :min="0"
            :max="604800"
            controls-position="right"
            placeholder="秒"
          />
          <span class="form-tip">（秒，默认取 HouConfig.TOKEN_OUTTIME={{ runtimeInfo.runtimeTokenExpire || 86400 }}）</span>
        </el-form-item>

        <el-form-item label="Redis最大内存">
          <el-input-number
            v-model="cacheConfig.redisMaxMemory"
            :min="0"
            controls-position="right"
            placeholder="MB"
          />
          <span class="form-tip">（MB，0=不限制；有值时来自 Redis INFO maxmemory）</span>
        </el-form-item>

        <el-form-item label="Redis连接池大小">
          <el-input-number
            v-model="cacheConfig.redisPoolSize"
            :min="1"
            :max="200"
            controls-position="right"
          />
          <span class="form-tip">（运行时读取 spring.redis.lettuce.pool.max-active）</span>
        </el-form-item>

        <el-divider content-position="left">应用缓存配置</el-divider>

        <el-form-item label="应用缓存过期时间">
          <el-input-number
            v-model="cacheConfig.appExpire"
            :min="0"
            :max="86400"
            controls-position="right"
            placeholder="秒"
          />
          <span class="form-tip">（秒）</span>
        </el-form-item>

        <el-form-item label="应用缓存最大大小">
          <el-input-number
            v-model="cacheConfig.appMaxSize"
            :min="0"
            controls-position="right"
            placeholder="MB"
          />
          <span class="form-tip">（MB）</span>
        </el-form-item>

        <el-divider content-position="left">静态资源缓存配置</el-divider>

        <el-form-item label="静态资源缓存时间">
          <el-input-number
            v-model="cacheConfig.staticExpire"
            :min="0"
            :max="31536000"
            controls-position="right"
            placeholder="秒"
          />
          <span class="form-tip">（秒，31536000=1年）</span>
        </el-form-item>

        <el-form-item label="静态资源压缩">
          <el-switch v-model="cacheConfig.staticCompression" />
        </el-form-item>

        <el-divider content-position="left">高级配置</el-divider>

        <el-form-item label="启用缓存预热">
          <el-switch v-model="cacheConfig.preloadEnabled" />
        </el-form-item>

        <el-form-item label="启用缓存压缩">
          <el-switch v-model="cacheConfig.compressionEnabled" />
        </el-form-item>

        <el-form-item label="缓存命中率阈值">
          <el-input-number
            v-model="cacheConfig.hitRateThreshold"
            :min="0"
            :max="100"
            controls-position="right"
            placeholder="%"
          />
          <span class="form-tip">（%，低于此值将触发告警）</span>
        </el-form-item>

        <el-form-item label="自动清理策略">
          <el-select v-model="cacheConfig.cleanupStrategy" placeholder="选择清理策略">
            <el-option label="LRU（最近最少使用）" value="lru" />
            <el-option label="LFU（最少使用频率）" value="lfu" />
            <el-option label="FIFO（先进先出）" value="fifo" />
            <el-option label="TTL（基于时间）" value="ttl" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveConfig">
            <el-icon><Check /></el-icon>
            保存配置
          </el-button>
          <el-button :loading="loading" @click="loadConfig">
            <el-icon><Refresh /></el-icon>
            重新加载
          </el-button>
          <el-button type="info" :loading="testing" @click="testConfig">
            <el-icon><Connection /></el-icon>
            测试连接
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Refresh, Connection } from '@element-plus/icons-vue'
import { getCacheSettings, saveCacheSettings, testCacheConnection } from '@/api/setting/cache'

const loading = ref(false)
const saving = ref(false)
const testing = ref(false)

const defaultConfig = () => ({
  redisExpire: 86400,
  redisMaxMemory: 0,
  redisPoolSize: 10,
  appExpire: 1800,
  appMaxSize: 256,
  staticExpire: 86400,
  staticCompression: true,
  preloadEnabled: true,
  compressionEnabled: true,
  hitRateThreshold: 80,
  cleanupStrategy: 'lru'
})

const cacheConfig = reactive(defaultConfig())

const runtimeInfo = reactive({
  redisHost: '',
  redisPort: null,
  redisDatabase: null,
  redisCommandTimeoutMs: null,
  runtimeTokenExpire: null,
  redisMemoryUsedMb: null,
  redisConnected: false
})

const applySettings = (data) => {
  if (!data) return
  Object.assign(cacheConfig, defaultConfig(), {
    redisExpire: data.redisExpire,
    redisMaxMemory: data.redisMaxMemory,
    redisPoolSize: data.redisPoolSize,
    appExpire: data.appExpire,
    appMaxSize: data.appMaxSize,
    staticExpire: data.staticExpire,
    staticCompression: data.staticCompression,
    preloadEnabled: data.preloadEnabled,
    compressionEnabled: data.compressionEnabled,
    hitRateThreshold: data.hitRateThreshold,
    cleanupStrategy: data.cleanupStrategy || 'lru'
  })
  runtimeInfo.redisHost = data.redisHost || ''
  runtimeInfo.redisPort = data.redisPort
  runtimeInfo.redisDatabase = data.redisDatabase
  runtimeInfo.redisCommandTimeoutMs = data.redisCommandTimeoutMs
  runtimeInfo.runtimeTokenExpire = data.runtimeTokenExpire
  runtimeInfo.redisMemoryUsedMb = data.redisMemoryUsedMb
  runtimeInfo.redisConnected = !!data.redisConnected
}

const loadConfig = async () => {
  loading.value = true
  try {
    const res = await getCacheSettings()
    if (res && (res.code === 200 || res.code === 1)) {
      applySettings(res.data)
    } else {
      ElMessage.warning(res?.message || '加载配置失败')
    }
  } catch (error) {
    ElMessage.error('加载配置失败: ' + (error?.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

const saveConfig = async () => {
  saving.value = true
  try {
    const res = await saveCacheSettings({ ...cacheConfig })
    if (res && (res.code === 200 || res.code === 1)) {
      applySettings(res.data)
      ElMessage.success('配置保存成功')
    } else {
      ElMessage.error(res?.message || '保存配置失败')
    }
  } catch (error) {
    ElMessage.error('保存配置失败: ' + (error?.message || '网络错误'))
  } finally {
    saving.value = false
  }
}

const testConfig = async () => {
  testing.value = true
  try {
    const res = await testCacheConnection()
    if (res && (res.code === 200 || res.code === 1)) {
      runtimeInfo.redisConnected = true
      ElMessage.success(res.message || res.data || '连接测试成功')
    } else {
      runtimeInfo.redisConnected = false
      ElMessage.error(res?.message || '连接测试失败')
    }
  } catch (error) {
    runtimeInfo.redisConnected = false
    ElMessage.error('连接测试失败: ' + (error?.message || '网络错误'))
  } finally {
    testing.value = false
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.cache-setting-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.runtime-alert {
  margin-bottom: 16px;
}

.runtime-lines {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 13px;
}

.config-card {
  margin-bottom: 20px;
}

.config-form {
  max-width: 800px;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}

.el-divider {
  margin: 30px 0 20px 0;
}
</style>
