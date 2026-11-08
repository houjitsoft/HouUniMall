<template>
  <el-config-provider :locale="currentElementLocale">
    <div id="app">
      <router-view />
    </div>
  </el-config-provider>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { ElConfigProvider } from 'element-plus'
// 使用统一的 Element Plus 国际化系统
import i18nElement, { getElementPlusLocale, setLocale, getLocale } from '@/i18n-element'

// 当前语言（响应式）
const currentLocaleValue = ref(getLocale())

// 获取当前语言的 Element Plus locale（响应式）
const currentElementLocale = computed(() => {
  return getElementPlusLocale(currentLocaleValue.value)
})

// 监听语言切换事件（只更新本地响应式变量，不再调用 setLocale 避免循环）
const handleLanguageChange = (event) => {
  if (event.detail && event.detail !== currentLocaleValue.value) {
    currentLocaleValue.value = event.detail
  }
}

onMounted(() => {
  window.addEventListener('language-changed', handleLanguageChange)
})
</script>

<style>
html, body {
  height: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden; /* 防止页面滚动，确保布局稳定 */
}

/* 合同签署、服务运维页允许整页滚动 */
body:has(.contract-sign-page),
body:has(.ops-page) {
  overflow: auto;
}

#app {
  height: 100%;
  margin: 0;
  padding: 0;
}

/* 上述公开页的 #app 允许高度超出视口 */
#app:has(.contract-sign-page),
#app:has(.ops-page) {
  height: auto;
  min-height: 100%;
}

* {
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 修复 el-select 箭头位置问题 */
.el-select {
  display: inline-block;
  position: relative;
}

.el-select .el-input {
  display: flex;
  align-items: center;
}

.el-select .el-input__wrapper {
  display: flex;
  align-items: center;
  position: relative;
}

.el-select .el-input__inner {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding-right: 30px;
}

.el-select .el-input__suffix {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  pointer-events: none;
}

.el-select .el-input__suffix-inner {
  display: flex;
  align-items: center;
}

/* 确保选择框有足够宽度时，箭头不会换行 */
.el-select .el-input__wrapper {
  min-width: 0;
}

.el-select .el-input {
  width: 100%;
}
</style>