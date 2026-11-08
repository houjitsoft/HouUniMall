<template>
  <div class="test-i18n">
    <h2>国际化测试</h2>
    <p>平台标语: {{ t('auth.login.platformTagline') }}</p>
    <p>欢迎登录: {{ t('auth.login.welcomeLogin') }}</p>
    <p>用户名: {{ t('auth.login.username') }}</p>
    <p>当前语言: {{ locale }}</p>
    <button @click="switchToEnglish">切换到英文</button>
    <button @click="switchToChinese">切换到中文</button>
  </div>
</template>

<script setup>
// 使用 Element Plus 国际化系统
import { useI18n, setLocale, getLocale } from '@/i18n-element'
import { computed } from 'vue'

const { t } = useI18n()

const locale = computed({
  get: () => getLocale(),
  set: (value) => setLocale(value)
})

const switchToEnglish = () => {
  setLocale('en')
}

const switchToChinese = () => {
  setLocale('zh-CN')
}
</script>

<style scoped>
.test-i18n {
  padding: 20px;
  border: 1px solid #ccc;
  margin: 20px;
}
</style>