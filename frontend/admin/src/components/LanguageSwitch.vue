<template>
  <div class="language-switch">
    <el-dropdown @command="handleLanguageChange">
      <el-button class="language-button" size="small">
        {{ currentLanguageName }}
        <el-icon class="el-icon--right">
          <arrow-down />
        </el-icon>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="lang in languages"
            :key="lang.value"
            :command="lang.value"
            :class="{ 'is-current': lang.value === currentLanguage }"
          >
            <span class="language-item">
              <span class="language-name">{{ lang.label }}</span>
              <el-icon v-if="lang.value === currentLanguage" class="check-icon">
                <Check />
              </el-icon>
            </span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ArrowDown, Check } from '@element-plus/icons-vue'
import { setLocale, getLocale } from '@/i18n-element'
import { useAdminLanguageMenu } from '@/composables/useAdminLanguageMenu'
import { ADMIN_LANGUAGES } from '@/locales/adminLanguageCatalog'

const { languages } = useAdminLanguageMenu()
const currentLocale = ref(getLocale())

const currentLanguageName = computed(() => {
  const current = languages.value.find((lang) => lang.value === currentLocale.value)
    || ADMIN_LANGUAGES.find((lang) => lang.value === currentLocale.value)
  return current ? current.label : '简体中文'
})

const currentLanguage = computed(() => currentLocale.value)

const handleLanguageChange = (lang) => {
  if (lang === currentLocale.value) return
  if (!setLocale(lang)) return
  currentLocale.value = lang
}

function syncFromEvent(event) {
  if (event.detail && event.detail !== currentLocale.value) {
    currentLocale.value = event.detail
  }
}

onMounted(() => {
  currentLocale.value = getLocale()
  window.addEventListener('language-changed', syncFromEvent)
})

onUnmounted(() => {
  window.removeEventListener('language-changed', syncFromEvent)
})
</script>

<style scoped>
.language-switch {
  display: inline-block;
}

.language-button {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  border-radius: 20px;
  padding: 8px 12px;
  font-size: 14px;
}

.language-button:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

:deep(.el-dropdown-menu__item) {
  font-size: 14px;
}

:deep(.el-dropdown-menu__item.is-disabled) {
  color: #666;
  cursor: not-allowed;
}

:deep(.el-dropdown-menu__item.is-current) {
  background-color: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

.language-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.language-name {
  flex: 1;
}

.check-icon {
  margin-left: 8px;
  color: #409eff;
  font-size: 16px;
}
</style>
