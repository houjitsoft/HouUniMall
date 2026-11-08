<template>
  <div class="backup-config-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>备份配置</h2>
      <p>配置备份相关的详细参数</p>
    </div>

    <!-- 配置表单 -->
    <el-tabs v-model="activeTab">
      <!-- 基础配置 -->
      <el-tab-pane label="基础配置" name="basic">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>基础配置</span>
            </div>
          </template>
          
          <el-form :model="basicConfig" label-width="150px">
            <el-form-item label="备份存储位置">
              <el-input v-model="basicConfig.storagePath" placeholder="请输入备份文件存储路径" />
              <span class="form-tip">备份文件存储的目录路径</span>
            </el-form-item>
            
            <el-form-item label="存储类型">
              <el-select v-model="basicConfig.storageType" placeholder="请选择存储类型">
                <el-option label="本地存储" value="local" />
                <el-option label="FTP存储" value="ftp" />
                <el-option label="SFTP存储" value="sftp" />
                <el-option label="云存储" value="cloud" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="FTP服务器地址" v-if="basicConfig.storageType === 'ftp' || basicConfig.storageType === 'sftp'">
              <el-input v-model="basicConfig.ftpHost" placeholder="请输入FTP服务器地址" />
            </el-form-item>
            
            <el-form-item label="FTP端口" v-if="basicConfig.storageType === 'ftp' || basicConfig.storageType === 'sftp'">
              <el-input-number v-model="basicConfig.ftpPort" :min="1" :max="65535" />
            </el-form-item>
            
            <el-form-item label="FTP用户名" v-if="basicConfig.storageType === 'ftp' || basicConfig.storageType === 'sftp'">
              <el-input v-model="basicConfig.ftpUsername" placeholder="请输入FTP用户名" />
            </el-form-item>
            
            <el-form-item label="FTP密码" v-if="basicConfig.storageType === 'ftp' || basicConfig.storageType === 'sftp'">
              <el-input v-model="basicConfig.ftpPassword" placeholder="请输入FTP密码" show-password />
            </el-form-item>
            
            <el-form-item label="云存储提供商" v-if="basicConfig.storageType === 'cloud'">
              <el-select v-model="basicConfig.cloudProvider" placeholder="请选择云存储提供商">
                <el-option label="阿里云OSS" value="aliyun" />
                <el-option label="腾讯云COS" value="tencent" />
                <el-option label="AWS S3" value="aws" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <!-- 数据库配置 -->
      <el-tab-pane label="数据库配置" name="database">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>数据库备份配置</span>
            </div>
          </template>
          
          <el-form :model="databaseConfig" label-width="150px">
            <el-form-item label="数据库类型">
              <el-select v-model="databaseConfig.dbType" placeholder="请选择数据库类型">
                <el-option label="MySQL" value="mysql" />
                <el-option label="PostgreSQL" value="postgresql" />
                <el-option label="Oracle" value="oracle" />
                <el-option label="SQL Server" value="sqlserver" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="数据库主机">
              <el-input v-model="databaseConfig.dbHost" placeholder="请输入数据库主机地址" />
            </el-form-item>
            
            <el-form-item label="数据库端口">
              <el-input-number v-model="databaseConfig.dbPort" :min="1" :max="65535" />
            </el-form-item>
            
            <el-form-item label="数据库名称">
              <el-input v-model="databaseConfig.dbName" placeholder="请输入数据库名称" />
            </el-form-item>
            
            <el-form-item label="数据库用户名">
              <el-input v-model="databaseConfig.dbUsername" placeholder="请输入数据库用户名" />
            </el-form-item>
            
            <el-form-item label="数据库密码">
              <el-input v-model="databaseConfig.dbPassword" placeholder="请输入数据库密码" show-password />
            </el-form-item>
            
            <el-form-item label="备份选项">
              <el-checkbox-group v-model="databaseConfig.backupOptions">
                <el-checkbox label="structure">备份表结构</el-checkbox>
                <el-checkbox label="data">备份数据</el-checkbox>
                <el-checkbox label="triggers">备份触发器</el-checkbox>
                <el-checkbox label="routines">备份存储过程</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <!-- 文件配置 -->
      <el-tab-pane label="文件配置" name="files">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>文件备份配置</span>
            </div>
          </template>
          
          <el-form :model="filesConfig" label-width="150px">
            <el-form-item label="备份目录列表">
              <el-input 
                v-model="filesConfig.backupDirs" 
                type="textarea" 
                :rows="6" 
                placeholder="请输入需要备份的目录路径，每行一个"
              />
            </el-form-item>
            
            <el-form-item label="排除目录列表">
              <el-input 
                v-model="filesConfig.excludeDirs" 
                type="textarea" 
                :rows="4" 
                placeholder="请输入需要排除的目录路径，每行一个"
              />
            </el-form-item>
            
            <el-form-item label="排除文件类型">
              <el-input 
                v-model="filesConfig.excludeExtensions" 
                placeholder="请输入需要排除的文件类型，用逗号分隔，如：.tmp,.log"
              />
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <!-- 高级配置 -->
      <el-tab-pane label="高级配置" name="advanced">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>高级配置</span>
            </div>
          </template>
          
          <el-form :model="advancedConfig" label-width="150px">
            <el-form-item label="压缩级别">
              <el-slider v-model="advancedConfig.compressionLevel" :min="1" :max="9" show-input />
              <span class="form-tip">压缩级别越高，压缩比越大，但耗时越长</span>
            </el-form-item>
            
            <el-form-item label="并发数">
              <el-input-number v-model="advancedConfig.concurrentThreads" :min="1" :max="16" />
              <span class="form-tip">同时进行备份操作的线程数</span>
            </el-form-item>
            
            <el-form-item label="超时时间(秒)">
              <el-input-number v-model="advancedConfig.timeout" :min="30" :max="3600" />
              <span class="form-tip">单个备份操作的超时时间</span>
            </el-form-item>
            
            <el-form-item label="重试次数">
              <el-input-number v-model="advancedConfig.retryCount" :min="0" :max="10" />
              <span class="form-tip">备份失败时的重试次数</span>
            </el-form-item>
            
            <el-form-item label="日志级别">
              <el-select v-model="advancedConfig.logLevel" placeholder="请选择日志级别">
                <el-option label="调试(DEBUG)" value="debug" />
                <el-option label="信息(INFO)" value="info" />
                <el-option label="警告(WARN)" value="warn" />
                <el-option label="错误(ERROR)" value="error" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 操作按钮 -->
    <div class="form-actions">
      <el-button type="primary" @click="handleSave" :loading="saving">
        <el-icon><Check /></el-icon>
        保存配置
      </el-button>
      <el-button @click="handleReset">
        <el-icon><Refresh /></el-icon>
        重置
      </el-button>
    </div>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Refresh } from '@element-plus/icons-vue'

// 数据
const activeTab = ref('basic')
const saving = ref(false)

const basicConfig = reactive({
  storagePath: '/backup',
  storageType: 'local',
  ftpHost: '',
  ftpPort: 21,
  ftpUsername: '',
  ftpPassword: '',
  cloudProvider: 'aliyun'
})

const databaseConfig = reactive({
  dbType: 'mysql',
  dbHost: 'localhost',
  dbPort: 3306,
  dbName: 'myapp',
  dbUsername: 'root',
  dbPassword: '',
  backupOptions: ['structure', 'data']
})

const filesConfig = reactive({
  backupDirs: '/var/www/html\n/home/user/documents',
  excludeDirs: '/var/www/html/cache\n/home/user/documents/temp',
  excludeExtensions: '.tmp,.log,.cache'
})

const advancedConfig = reactive({
  compressionLevel: 6,
  concurrentThreads: 4,
  timeout: 300,
  retryCount: 3,
  logLevel: 'info'
})

// 方法
const loadConfig = async () => {
  try {
    // 模拟加载配置
    ElMessage.success('配置加载成功')
  } catch (error) {
    ElMessage.error('加载配置失败: ' + error.message)
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    // 模拟保存配置
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('配置保存成功')
  } catch (error) {
    ElMessage.error('保存配置失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

const handleReset = () => {
  ElMessage.info('配置已重置')
}

// 生命周期
onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.backup-config-container {
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

.card-header {
  font-weight: 600;
  color: #303133;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.form-actions {
  margin-top: 30px;
  text-align: center;
}
</style>
