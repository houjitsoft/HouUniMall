<template>
  <el-tooltip :content="tooltipText" placement="bottom">
    <button
      type="button"
      class="error-alert-bell"
      aria-label="运行告警"
      @click="goAlertCenter"
    >
      <el-badge :value="count" :hidden="count <= 0" :max="99">
        <el-icon :size="18"><Bell /></el-icon>
      </el-badge>
    </button>
  </el-tooltip>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import { errorAlertAPI } from '@/api/log/index.js'

/**
 * 运行告警铃铛
 *
 * 顶栏常驻入口：定时拉取未处理告警数量，有未处理就亮红点。
 * 日志服务不可用时静默降级为 0 —— 告警中心自己挂了不该在顶栏制造噪音。
 */
const router = useRouter()
const count = ref(0)
const tooltipText = ref('运行告警')
let timer = null

/**
 * 拉取未处理告警数量
 */
async function loadCount() {
  try {
    const res = await errorAlertAPI.getUnreadCount()
    if (res && (res.code === 200 || res.code === 0) && res.data) {
      count.value = Number(res.data.count) || 0
      tooltipText.value = count.value > 0 ? `有 ${count.value} 条未处理运行告警` : '运行告警'
    }
  } catch (e) {
    // 服务不可用时不打扰用户
  }
}

/**
 * 跳转到告警中心
 */
function goAlertCenter() {
  router.push('/dcenter/error-alert')
}

onMounted(() => {
  loadCount()
  timer = setInterval(loadCount, 60000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<style scoped>
.error-alert-bell {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: inherit;
  cursor: pointer;
}

.error-alert-bell:hover {
  background: rgba(0, 0, 0, 0.06);
}
</style>
