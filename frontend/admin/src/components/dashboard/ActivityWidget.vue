<template>
  <div class="placeholder-widget">
    <el-empty :image-size="72" :description="t('dashboard.modules.comingSoon')" />
  </div>
</template>

<script setup>
/**
 * 活动管理 Widget 占位
 * 真实接口接入前统一使用"功能开发中"占位。
 * 接入方式：参考 OaTodoWidget/PaymentWidget，替换为真实 API + UI。
 *
 * @author Amos
 * @since 2026-05-13
 */
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
</script>

<style scoped lang="scss">
.placeholder-widget { padding: 12px 0; text-align: center; }
</style>
