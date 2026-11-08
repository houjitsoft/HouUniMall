<template>
  <div class="log-setting">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>日志设置</span>
          <el-button type="primary" @click="saveSettings">保存设置</el-button>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基础配置 -->
        <el-tab-pane label="基础配置" name="basic">
          <el-form :model="basicSettings" label-width="150px" class="setting-form">
            <el-form-item label="日志级别">
              <el-select v-model="basicSettings.level" placeholder="请选择日志级别">
                <el-option label="DEBUG" value="DEBUG"></el-option>
                <el-option label="INFO" value="INFO"></el-option>
                <el-option label="WARN" value="WARN"></el-option>
                <el-option label="ERROR" value="ERROR"></el-option>
                <el-option label="FATAL" value="FATAL"></el-option>
              </el-select>
            </el-form-item>
            
            <el-form-item label="日志格式">
              <el-input 
                v-model="basicSettings.pattern" 
                type="textarea" 
                :rows="3"
                placeholder="请输入日志格式模式"
              ></el-input>
            </el-form-item>
            
            <el-form-item label="日志文件路径">
              <el-input v-model="basicSettings.filePath" placeholder="请输入日志文件路径"></el-input>
            </el-form-item>
            
            <el-form-item label="日志文件大小">
              <el-input-number 
                v-model="basicSettings.maxFileSize" 
                :min="1" 
                :max="1000"
                controls-position="right"
              ></el-input-number>
              <span style="margin-left: 10px;">MB</span>
            </el-form-item>
            
            <el-form-item label="日志文件数量">
              <el-input-number 
                v-model="basicSettings.maxBackupIndex" 
                :min="1" 
                :max="100"
                controls-position="right"
              ></el-input-number>
            </el-form-item>
            
            <el-form-item label="启用异步日志">
              <el-switch v-model="basicSettings.asyncEnabled"></el-switch>
            </el-form-item>
            
            <el-form-item label="异步队列大小">
              <el-input-number 
                v-model="basicSettings.asyncQueueSize" 
                :min="100" 
                :max="10000"
                controls-position="right"
                :disabled="!basicSettings.asyncEnabled"
              ></el-input-number>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 应用配置 -->
        <el-tab-pane label="应用配置" name="app">
          <el-form :model="appSettings" label-width="150px" class="setting-form">
            <el-form-item label="应用名称">
              <el-input v-model="appSettings.applicationName" placeholder="请输入应用名称"></el-input>
            </el-form-item>
            
            <el-form-item label="环境标识">
              <el-select v-model="appSettings.environment" placeholder="请选择环境">
                <el-option label="开发环境" value="dev"></el-option>
                <el-option label="测试环境" value="test"></el-option>
                <el-option label="生产环境" value="prod"></el-option>
              </el-select>
            </el-form-item>
            
            <el-form-item label="服务名称">
              <el-input v-model="appSettings.serviceName" placeholder="请输入服务名称"></el-input>
            </el-form-item>
            
            <el-form-item label="实例ID">
              <el-input v-model="appSettings.instanceId" placeholder="请输入实例ID"></el-input>
            </el-form-item>
            
            <el-form-item label="版本号">
              <el-input v-model="appSettings.version" placeholder="请输入版本号"></el-input>
            </el-form-item>
            
            <el-form-item label="启用链路追踪">
              <el-switch v-model="appSettings.tracingEnabled"></el-switch>
            </el-form-item>
            
            <el-form-item label="追踪ID字段名">
              <el-input 
                v-model="appSettings.traceIdField" 
                placeholder="请输入追踪ID字段名"
                :disabled="!appSettings.tracingEnabled"
              ></el-input>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 输出配置 -->
        <el-tab-pane label="输出配置" name="output">
          <el-form :model="outputSettings" label-width="150px" class="setting-form">
            <el-form-item label="控制台输出">
              <el-switch v-model="outputSettings.consoleEnabled"></el-switch>
            </el-form-item>
            
            <el-form-item label="文件输出">
              <el-switch v-model="outputSettings.fileEnabled"></el-switch>
            </el-form-item>
            
            <el-form-item label="数据库输出">
              <el-switch v-model="outputSettings.databaseEnabled"></el-switch>
            </el-form-item>
            
            <el-form-item label="远程输出">
              <el-switch v-model="outputSettings.remoteEnabled"></el-switch>
            </el-form-item>
            
            <el-form-item label="远程服务器地址" v-if="outputSettings.remoteEnabled">
              <el-input v-model="outputSettings.remoteHost" placeholder="请输入远程服务器地址"></el-input>
            </el-form-item>
            
            <el-form-item label="远程服务器端口" v-if="outputSettings.remoteEnabled">
              <el-input-number 
                v-model="outputSettings.remotePort" 
                :min="1" 
                :max="65535"
                controls-position="right"
              ></el-input-number>
            </el-form-item>
            
            <el-form-item label="远程协议" v-if="outputSettings.remoteEnabled">
              <el-select v-model="outputSettings.remoteProtocol" placeholder="请选择远程协议">
                <el-option label="TCP" value="tcp"></el-option>
                <el-option label="UDP" value="udp"></el-option>
                <el-option label="HTTP" value="http"></el-option>
              </el-select>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 过滤配置 -->
        <el-tab-pane label="过滤配置" name="filter">
          <el-form :model="filterSettings" label-width="150px" class="setting-form">
            <el-form-item label="启用日志过滤">
              <el-switch v-model="filterSettings.filterEnabled"></el-switch>
            </el-form-item>
            
            <el-form-item label="过滤规则" v-if="filterSettings.filterEnabled">
              <div class="filter-rules">
                <div v-for="(rule, index) in filterSettings.rules" :key="index" class="filter-rule">
                  <el-input v-model="rule.pattern" placeholder="匹配模式" style="width: 200px; margin-right: 10px;"></el-input>
                  <el-select v-model="rule.action" placeholder="动作" style="width: 100px; margin-right: 10px;">
                    <el-option label="接受" value="ACCEPT"></el-option>
                    <el-option label="拒绝" value="DENY"></el-option>
                  </el-select>
                  <el-button type="danger" size="small" @click="removeRule(index)">删除</el-button>
                </div>
                <el-button type="primary" size="small" @click="addRule">添加规则</el-button>
              </div>
            </el-form-item>
            
            <el-form-item label="敏感信息脱敏">
              <el-switch v-model="filterSettings.maskingEnabled"></el-switch>
            </el-form-item>
            
            <el-form-item label="脱敏字段" v-if="filterSettings.maskingEnabled">
              <el-tag
                v-for="(field, index) in filterSettings.maskingFields"
                :key="index"
                closable
                @close="removeMaskingField(index)"
                style="margin-right: 10px; margin-bottom: 5px;"
              >
                {{ field }}
              </el-tag>
              <el-input
                v-model="newMaskingField"
                placeholder="输入字段名后按回车添加"
                style="width: 200px;"
                @keyup.enter="addMaskingField"
              ></el-input>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { logSettingAPI } from '@/api/log/index.js'
import { unwrap } from '@/utils/logApi.js'

const activeTab = ref('basic')
const loading = ref(false)

const basicSettings = reactive({
  level: 'INFO',
  pattern: '%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n',
  filePath: '/logs/application.log',
  maxFileSize: 100,
  maxBackupIndex: 10,
  asyncEnabled: true,
  asyncQueueSize: 1000
})

const appSettings = reactive({
  applicationName: 'houcloud-system',
  environment: 'prod',
  serviceName: 'bff-web',
  instanceId: 'instance-001',
  version: '1.0.0',
  tracingEnabled: true,
  traceIdField: 'traceId'
})

const outputSettings = reactive({
  consoleEnabled: true,
  fileEnabled: true,
  databaseEnabled: false,
  remoteEnabled: false,
  remoteHost: 'localhost',
  remotePort: 5000,
  remoteProtocol: 'tcp'
})

const filterSettings = reactive({
  filterEnabled: true,
  rules: [],
  maskingEnabled: true,
  maskingFields: []
})

const newMaskingField = ref('')

/**
 * 将后端返回的设置列表填充到各个表单对象
 * @param {Array} settingList 后端返回的配置项数组，每项包含 key 和 value
 */
const applySettings = (settingList) => {
  if (!Array.isArray(settingList)) return
  settingList.forEach(item => {
    const { key, value } = item
    // basic
    if (key === 'log.level') basicSettings.level = value
    else if (key === 'log.pattern') basicSettings.pattern = value
    else if (key === 'log.filePath') basicSettings.filePath = value
    else if (key === 'log.maxFileSize') basicSettings.maxFileSize = Number(value)
    else if (key === 'log.maxBackupIndex') basicSettings.maxBackupIndex = Number(value)
    else if (key === 'log.asyncEnabled') basicSettings.asyncEnabled = value === 'true'
    else if (key === 'log.asyncQueueSize') basicSettings.asyncQueueSize = Number(value)
    // app
    else if (key === 'log.applicationName') appSettings.applicationName = value
    else if (key === 'log.environment') appSettings.environment = value
    else if (key === 'log.serviceName') appSettings.serviceName = value
    else if (key === 'log.instanceId') appSettings.instanceId = value
    else if (key === 'log.version') appSettings.version = value
    else if (key === 'log.tracingEnabled') appSettings.tracingEnabled = value === 'true'
    else if (key === 'log.traceIdField') appSettings.traceIdField = value
    // output
    else if (key === 'log.consoleEnabled') outputSettings.consoleEnabled = value === 'true'
    else if (key === 'log.fileEnabled') outputSettings.fileEnabled = value === 'true'
    else if (key === 'log.databaseEnabled') outputSettings.databaseEnabled = value === 'true'
    else if (key === 'log.remoteEnabled') outputSettings.remoteEnabled = value === 'true'
    else if (key === 'log.remoteHost') outputSettings.remoteHost = value
    else if (key === 'log.remotePort') outputSettings.remotePort = Number(value)
    else if (key === 'log.remoteProtocol') outputSettings.remoteProtocol = value
    // filter
    else if (key === 'log.filterEnabled') filterSettings.filterEnabled = value === 'true'
    else if (key === 'log.maskingEnabled') filterSettings.maskingEnabled = value === 'true'
    else if (key === 'log.maskingFields') {
      try { filterSettings.maskingFields = JSON.parse(value) } catch { filterSettings.maskingFields = [] }
    }
    else if (key === 'log.filterRules') {
      try { filterSettings.rules = JSON.parse(value) } catch { filterSettings.rules = [] }
    }
  })
}

/**
 * 加载日志设置列表
 * 页面初始化时调用，从后端拉取当前配置并填充表单
 */
const loadSettings = async () => {
  loading.value = true
  try {
    applySettings(unwrap(await logSettingAPI.getList()) || [])
  } catch (error) {
    console.error('获取日志设置失败:', error)
    ElMessage.error(error.message || '获取日志设置失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 将所有当前表单数据序列化为 key-value 数组并批量保存
 * @returns {Promise<void>}
 */
const saveSettings = async () => {
  loading.value = true
  try {
    // 构建待更新的配置项列表
    const updates = [
      { key: 'log.level',            value: basicSettings.level },
      { key: 'log.pattern',          value: basicSettings.pattern },
      { key: 'log.filePath',         value: basicSettings.filePath },
      { key: 'log.maxFileSize',      value: String(basicSettings.maxFileSize) },
      { key: 'log.maxBackupIndex',   value: String(basicSettings.maxBackupIndex) },
      { key: 'log.asyncEnabled',     value: String(basicSettings.asyncEnabled) },
      { key: 'log.asyncQueueSize',   value: String(basicSettings.asyncQueueSize) },
      { key: 'log.applicationName',  value: appSettings.applicationName },
      { key: 'log.environment',      value: appSettings.environment },
      { key: 'log.serviceName',      value: appSettings.serviceName },
      { key: 'log.instanceId',       value: appSettings.instanceId },
      { key: 'log.version',          value: appSettings.version },
      { key: 'log.tracingEnabled',   value: String(appSettings.tracingEnabled) },
      { key: 'log.traceIdField',     value: appSettings.traceIdField },
      { key: 'log.consoleEnabled',   value: String(outputSettings.consoleEnabled) },
      { key: 'log.fileEnabled',      value: String(outputSettings.fileEnabled) },
      { key: 'log.databaseEnabled',  value: String(outputSettings.databaseEnabled) },
      { key: 'log.remoteEnabled',    value: String(outputSettings.remoteEnabled) },
      { key: 'log.remoteHost',       value: outputSettings.remoteHost },
      { key: 'log.remotePort',       value: String(outputSettings.remotePort) },
      { key: 'log.remoteProtocol',   value: outputSettings.remoteProtocol },
      { key: 'log.filterEnabled',    value: String(filterSettings.filterEnabled) },
      { key: 'log.maskingEnabled',   value: String(filterSettings.maskingEnabled) },
      { key: 'log.maskingFields',    value: JSON.stringify(filterSettings.maskingFields) },
      { key: 'log.filterRules',      value: JSON.stringify(filterSettings.rules) }
    ]

    // 逐条调用 update 接口
    await Promise.all(updates.map((item) => logSettingAPI.update(item).then(unwrap)))
    ElMessage.success('日志设置保存成功')
  } catch (error) {
    console.error('保存日志设置失败:', error)
    ElMessage.error('保存日志设置失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 向过滤规则列表添加一条空规则
 */
const addRule = () => {
  filterSettings.rules.push({ pattern: '', action: 'ACCEPT' })
}

/**
 * 删除指定索引的过滤规则
 * @param {number} index 规则在 rules 数组中的索引
 */
const removeRule = (index) => {
  filterSettings.rules.splice(index, 1)
}

/**
 * 添加一个新的脱敏字段（回车触发）
 * 若字段名为空或已存在则不添加
 */
const addMaskingField = () => {
  const field = newMaskingField.value.trim()
  if (field && !filterSettings.maskingFields.includes(field)) {
    filterSettings.maskingFields.push(field)
    newMaskingField.value = ''
  }
}

/**
 * 删除指定索引的脱敏字段
 * @param {number} index 字段在 maskingFields 数组中的索引
 */
const removeMaskingField = (index) => {
  filterSettings.maskingFields.splice(index, 1)
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.log-setting {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.setting-form {
  padding: 20px;
}

.filter-rules {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
}

.filter-rule {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.filter-rule:last-child {
  margin-bottom: 0;
}
</style>
