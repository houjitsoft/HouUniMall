<template>
  <div class="timezone-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>时区配置</span>
        </div>
      </template>
      
      <el-form :model="timezoneForm" label-width="120px">
        <el-form-item label="系统时区">
          <el-select v-model="timezoneForm.systemTimezone" placeholder="请选择系统时区">
            <el-option
              v-for="timezone in timezoneOptions"
              :key="timezone.value"
              :label="timezone.label"
              :value="timezone.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="默认时区">
          <el-select v-model="timezoneForm.defaultTimezone" placeholder="请选择默认时区">
            <el-option
              v-for="timezone in timezoneOptions"
              :key="timezone.value"
              :label="timezone.label"
              :value="timezone.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="时间格式">
          <el-select v-model="timezoneForm.timeFormat" placeholder="请选择时间格式">
            <el-option label="24小时制 (HH:mm:ss)" value="24" />
            <el-option label="12小时制 (hh:mm:ss AM/PM)" value="12" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="日期格式">
          <el-select v-model="timezoneForm.dateFormat" placeholder="请选择日期格式">
            <el-option label="YYYY-MM-DD" value="YYYY-MM-DD" />
            <el-option label="MM/DD/YYYY" value="MM/DD/YYYY" />
            <el-option label="DD/MM/YYYY" value="DD/MM/YYYY" />
            <el-option label="YYYY年MM月DD日" value="YYYY年MM月DD日" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="自动同步">
          <el-switch v-model="timezoneForm.autoSync" />
          <span class="form-tip">开启后系统将自动同步网络时间</span>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveTimezoneConfig">保存配置</el-button>
          <el-button @click="resetTimezoneConfig">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const timezoneForm = reactive({
  systemTimezone: 'Asia/Shanghai',
  defaultTimezone: 'Asia/Shanghai',
  timeFormat: '24',
  dateFormat: 'YYYY-MM-DD',
  autoSync: true
})

const timezoneOptions = ref([
  { label: '北京时间 (UTC+8)', value: 'Asia/Shanghai' },
  { label: '东京时间 (UTC+9)', value: 'Asia/Tokyo' },
  { label: '首尔时间 (UTC+9)', value: 'Asia/Seoul' },
  { label: '香港时间 (UTC+8)', value: 'Asia/Hong_Kong' },
  { label: '台北时间 (UTC+8)', value: 'Asia/Taipei' },
  { label: '新加坡时间 (UTC+8)', value: 'Asia/Singapore' },
  { label: '伦敦时间 (UTC+0)', value: 'Europe/London' },
  { label: '纽约时间 (UTC-5)', value: 'America/New_York' },
  { label: '洛杉矶时间 (UTC-8)', value: 'America/Los_Angeles' }
])

const saveTimezoneConfig = () => {
  console.log('保存时区配置:', timezoneForm)
  ElMessage.success('时区配置保存成功')
}

const resetTimezoneConfig = () => {
  Object.assign(timezoneForm, {
    systemTimezone: 'Asia/Shanghai',
    defaultTimezone: 'Asia/Shanghai',
    timeFormat: '24',
    dateFormat: 'YYYY-MM-DD',
    autoSync: true
  })
  ElMessage.info('时区配置已重置')
}

onMounted(() => {
  // 加载时区配置
  console.log('加载时区配置')
})
</script>

<style scoped>
.timezone-config {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
</style>
