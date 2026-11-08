<template>
  <div class="header-left">
    <el-button
      v-if="isMobile"
      class="mobile-menu-btn"
      text
      aria-label="打开菜单"
      @click="$emit('toggle-mobile-menu')"
    >
      <el-icon :size="22"><Menu /></el-icon>
    </el-button>
    <span class="app-title app-title--full">F2C一站式管理平台</span>
    <span class="app-title app-title--short">UniMall</span>
  </div>
</template>

<script setup>
import { Menu } from '@element-plus/icons-vue'

defineProps({
  isMobile: {
    type: Boolean,
    default: false
  }
})

defineEmits(['toggle-mobile-menu'])
</script>

<style scoped>
.header-left {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
}

.mobile-menu-btn {
  padding: 6px;
  margin-right: 2px;
  color: #303133;
}

.app-title {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0.01em;
}

.app-title--short {
  display: none;
}

@media screen and (max-width: 992px) {
  .app-title--full {
    display: none;
  }

  .app-title--short {
    display: inline;
    font-size: 15px;
    font-weight: 600;
    max-width: none;
  }
}

@media screen and (max-width: 480px) {
  .app-title--short {
    font-size: 14px;
  }
}
</style>
