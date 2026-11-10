<template>
  <div class="footer-container" role="contentinfo">
    <p v-if="!isCollapse">© {{ currentYear }} {{ t('system.name') }}. {{ t('system.allRightsReserved') }}</p>
    <p v-else>© {{ currentYear }}</p>
  </div>
</template>

<script setup>
// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
const currentYear = new Date().getFullYear()

// 定义组件属性
const props = defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.footer-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  box-sizing: border-box;
  background-color: #fff;
  text-align: center;
  padding: 0 15px;
  font-size: 14px;
  color: #666;
}

.footer-container p {
  margin: 0;
}

@media screen and (max-width: 768px) {
  .footer-container {
    padding: 0 10px;
  }
}
</style>