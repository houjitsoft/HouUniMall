<template>
  <section class="hero-section">
    <div class="hero-bg" aria-hidden="true" />
    <div class="hero-content">
      <div class="welcome-text">
        <p v-if="config.showGreeting" class="welcome-eyebrow">{{ greetingText }}</p>
        <h2>{{ t('dashboard.welcome.title') }}</h2>
        <p v-if="config.showDescription" class="welcome-desc">{{ t('dashboard.welcome.description') }}</p>
      </div>
      <div class="welcome-toolbar">
        <el-tooltip
          v-if="config.showRefresh"
          :content="t('dashboard.toolbar.refresh')"
          placement="top"
          :disabled="isMobile"
        >
          <el-button
            class="toolbar-btn"
            :icon="Refresh"
            :circle="!isMobile"
            :loading="refreshing"
            @click="emit('refresh')"
          />
        </el-tooltip>
        <el-tooltip
          v-if="config.showNotification"
          :content="t('dashboard.toolbar.notifications')"
          placement="top"
          :disabled="isMobile"
        >
          <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0">
            <el-button
              class="toolbar-btn"
              :icon="Bell"
              :circle="!isMobile"
              data-testid="notification-trigger"
              @click="emit('notifications')"
            />
          </el-badge>
        </el-tooltip>
        <el-tooltip
          v-if="config.showSettings"
          :content="t('dashboard.toolbar.settings')"
          placement="top"
          :disabled="isMobile"
        >
          <el-button class="toolbar-btn" :icon="Setting" :circle="!isMobile" @click="emit('settings')" />
        </el-tooltip>
        <el-button
          v-if="config.showEdit"
          class="edit-btn"
          :type="editMode ? 'danger' : 'primary'"
          :icon="editMode ? Check : EditPen"
          :round="isMobile"
          @click="emit('toggle-edit')"
        >
          <span v-if="!isMobile">{{ editMode ? t('dashboard.editMode.done') : t('dashboard.editMode.edit') }}</span>
        </el-button>
      </div>
    </div>
  </section>
</template>

<script setup>
/**
 * 欢迎区 + 工具栏。
 * 2026-09-21：原先整块写死在 Dashboard.vue，现抽成区块并支持配置——
 * 问候语 / 描述 / 刷新 / 通知 / 设置 / 编辑 六个开关可单独关，关掉的功能不删，
 * 只是不再占用工具栏位置（编辑模式在设置抽屉里始终能退出，避免把自己锁死）。
 */
import { Refresh, Bell, Setting, EditPen, Check } from '@element-plus/icons-vue'
import { useI18n } from '@/i18n-element'

const { t } = useI18n()

defineProps({
  config: { type: Object, required: true },
  greetingText: { type: String, default: '' },
  refreshing: { type: Boolean, default: false },
  unreadCount: { type: Number, default: 0 },
  editMode: { type: Boolean, default: false },
  isMobile: { type: Boolean, default: false }
})

const emit = defineEmits(['refresh', 'notifications', 'settings', 'toggle-edit'])
</script>
