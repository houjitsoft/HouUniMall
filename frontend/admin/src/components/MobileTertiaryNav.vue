<template>
  <div v-if="tabs.length" class="mobile-tertiary-wrap">
    <div v-if="sectionTitle" class="mobile-tertiary-head">
      <span class="mobile-tertiary-section">{{ sectionTitle }}</span>
    </div>
    <nav class="mobile-tertiary-nav" :aria-label="sectionTitle || '子菜单'">
      <div class="mobile-tertiary-scroll">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          type="button"
          class="mobile-tertiary-tab"
          :class="{ 'is-active': isActive(tab.path) }"
          @click="go(tab.path)"
        >
          {{ tab.name }}
        </button>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { resolveTertiaryMenuTabs } from '@/utils/menuNavigate'

const props = defineProps({
  menuItems: {
    type: Array,
    default: () => []
  },
  activeMenu: {
    type: String,
    default: ''
  },
  /** 一级模块名，如「项目管理」 */
  moduleTitle: {
    type: String,
    default: ''
  }
})

const route = useRoute()
const router = useRouter()

const tertiaryMeta = computed(() => {
  const path = props.activeMenu || route.path
  return resolveTertiaryMenuTabs(props.menuItems, path)
})

const sectionTitle = computed(() => tertiaryMeta.value.parentTitle || props.moduleTitle || '')
const tabs = computed(() => tertiaryMeta.value.tabs)

/**
 * 判断 Tab 是否为当前路由
 * @param {string} tabPath Tab 路径
 * @returns {boolean}
 */
const isActive = (tabPath) => {
  const current = props.activeMenu || route.path
  const target = current.startsWith('/') ? current : `/${current}`
  const tab = tabPath.startsWith('/') ? tabPath : `/${tabPath}`
  if (target === tab) return true
  return target.startsWith(`${tab}/`)
}

/**
 * 跳转至三级菜单页
 * @param {string} path 目标路径
 * @returns {void}
 */
const go = (path) => {
  if (!path || isActive(path)) return
  router.push(path).catch(() => {})
}
</script>

<style scoped>
.mobile-tertiary-wrap {
  background: #fff;
  border-bottom: 1px solid var(--hc-border, #e4e7ed);
}

.mobile-tertiary-head {
  padding: 6px 12px 0;
}

.mobile-tertiary-section {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.mobile-tertiary-nav {
  position: relative;
}

.mobile-tertiary-nav::before,
.mobile-tertiary-nav::after {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  width: 16px;
  z-index: 1;
  pointer-events: none;
}

.mobile-tertiary-nav::before {
  left: 0;
  background: linear-gradient(90deg, #fff 0%, transparent 100%);
}

.mobile-tertiary-nav::after {
  right: 0;
  background: linear-gradient(270deg, #fff 0%, transparent 100%);
}

.mobile-tertiary-scroll {
  display: flex;
  gap: 0;
  padding: 0 8px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.mobile-tertiary-scroll::-webkit-scrollbar {
  display: none;
}

.mobile-tertiary-tab {
  flex-shrink: 0;
  border: none;
  background: transparent;
  color: #606266;
  font-size: 14px;
  line-height: 1.2;
  min-height: 44px;
  padding: 10px 14px 8px;
  cursor: pointer;
  position: relative;
  font-family: inherit;
  white-space: nowrap;
}

.mobile-tertiary-tab.is-active {
  color: #409eff;
  font-weight: 600;
}

.mobile-tertiary-tab.is-active::after {
  content: '';
  position: absolute;
  left: 14px;
  right: 14px;
  bottom: 0;
  height: 2px;
  border-radius: 2px 2px 0 0;
  background: #409eff;
}

.mobile-tertiary-tab:active {
  opacity: 0.75;
}
</style>
