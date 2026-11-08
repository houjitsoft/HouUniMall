<template>
  <el-footer class="footer-container">
    <p v-if="!isCollapse">© 2023 {{ t('system.name') }}. {{ t('system.allRightsReserved') }}</p>
    <p v-else>© 2023</p>
  </el-footer>
</template>

<script setup>
// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()

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
  background-color: #fff;
  text-align: center;
  padding: 15px;
  font-size: 14px;
  color: #666;
  box-shadow: 0 -1px 4px rgba(0, 21, 41, 0.08);
  transition: padding 0.28s;
}

@media screen and (max-width: 768px) {
  .footer-container {
    padding: 10px;
  }
}
</style>