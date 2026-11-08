<template>
  <div class="kb-frame-page" :class="{ 'is-fullscreen': isFullscreen }" :style="frameStyle">
    <!--
      知识库正文 = WeKnora 原生控制台。
      WeKnora 前端是 TDesign、本端是 Element Plus，组件级复用不成立，
      因此不做重写，直接 iframe 承载其完整界面（知识库、文档、Wiki、Agent、调试全在内部）。
      登录态存在 WeKnora 自己的 localStorage（weknora_token），首次登录后长期免登，
      与后台 JWT 互不干扰；① 的 nginx 已用 CSP frame-ancestors 只放行本后台。
    -->
    <iframe
      :src="CONSOLE_URL"
      class="kb-frame"
      allow="clipboard-read; clipboard-write"
      referrerpolicy="no-referrer-when-downgrade"
    />
    <div class="kb-frame__bar">
      <button class="kb-frame__btn" type="button" @click="toggleFullscreen">
        {{ isFullscreen ? '退出全屏' : '全屏' }}
      </button>
      <a class="kb-frame__btn" :href="CONSOLE_URL" target="_blank" rel="noopener">新窗口</a>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

/** WeKnora 原生控制台地址 */
const CONSOLE_URL = 'https://hounora.shangliansoft.cn'

const isFullscreen = ref(false)
/** 内容区实际可用高度（已排除顶部栏与底部 footer） */
const innerHeight = ref(600)

/**
 * 高度必须取 .content 的 clientHeight —— 它已经扣掉了 header 和 footer。
 * 之前用 window.innerHeight 减顶部偏移，会把底部 footer 挤出来，
 * 表现为「© 2023 管理系统，版权所有」压在 iframe 下方多出一截。
 */
const frameStyle = computed(() =>
  isFullscreen.value ? { height: '100vh' } : { height: `${innerHeight.value}px` }
)

function recalc() {
  const content = document.querySelector('.content')
  if (content) {
    innerHeight.value = Math.max(content.clientHeight, 320)
  } else {
    innerHeight.value = Math.max(window.innerHeight - 100, 320)
  }
}

function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value
  // 退出全屏后重算一次，避免布局切换期间取到旧值
  if (!isFullscreen.value) {
    window.setTimeout(recalc, 50)
  }
}

onMounted(() => {
  recalc()
  window.addEventListener('resize', recalc)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', recalc)
})
</script>

<style scoped lang="scss">
.kb-frame-page {
  position: relative;
  width: 100%;
  overflow: hidden;

  /* 全屏：盖住整个视口，让 WeKnora 拿到全部宽度（内容区宽度不足以容纳它的两栏布局） */
  &.is-fullscreen {
    position: fixed;
    inset: 0;
    z-index: 2000;
    height: 100vh;
    background: #fff;
  }
}

.kb-frame {
  width: 100%;
  height: 100%;
  border: 0;
  display: block;
  background: #fff;
}

.kb-frame__bar {
  position: absolute;
  right: 14px;
  bottom: 12px;
  display: flex;
  gap: 6px;
  opacity: 0.5;
  transition: opacity 0.2s ease;

  &:hover {
    opacity: 1;
  }
}

.kb-frame__btn {
  padding: 4px 10px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--el-text-color-secondary);
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  text-decoration: none;
  cursor: pointer;

  &:hover {
    color: var(--el-color-primary);
    border-color: var(--el-color-primary-light-5);
  }
}
</style>
