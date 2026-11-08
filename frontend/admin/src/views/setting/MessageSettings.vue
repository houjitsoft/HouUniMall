<template>
  <div class="message-settings">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消息通知设置</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" type="card" class="settings-tabs">
        <el-tab-pane label="渠道配置" name="channel">
          <template #label>
            <span class="tab-label">
              <el-icon><Connection /></el-icon>
              渠道配置
            </span>
          </template>
          <MessageChannel />
        </el-tab-pane>
        
        <el-tab-pane label="模板管理" name="template">
          <template #label>
            <span class="tab-label">
              <el-icon><Document /></el-icon>
              模板管理
            </span>
          </template>
          <MessageTemplate />
        </el-tab-pane>
        
        <el-tab-pane label="发送记录" name="record">
          <template #label>
            <span class="tab-label">
              <el-icon><List /></el-icon>
              发送记录
            </span>
          </template>
          <MessageRecord />
        </el-tab-pane>

        <el-tab-pane label="极光推送" name="jpush">
          <template #label>
            <span class="tab-label">
              <el-icon><Bell /></el-icon>
              极光推送
            </span>
          </template>
          <JPushManage />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Connection, Document, List, Bell } from '@element-plus/icons-vue'
import MessageChannel from './message/MessageChannel.vue'
import MessageTemplate from './message/MessageTemplate.vue'
import MessageRecord from './message/MessageRecord.vue'
import JPushManage from './message/JPushManage.vue'

const activeTab = ref('channel')
</script>

<style scoped>
.message-settings {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.settings-tabs {
  margin-top: 20px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 5px;
}

:deep(.el-tabs__content) {
  padding: 20px 0;
}

:deep(.el-tab-pane) {
  min-height: 400px;
}
</style>
