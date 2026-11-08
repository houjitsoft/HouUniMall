<template>
  <el-dropdown trigger="click" class="lang-dropdown" :placement="placement" @command="changeLanguage">
    <button type="button" class="hc-header-chip hc-header-chip--lang" aria-label="切换语言">
      <span class="hc-header-chip__text">{{ currentLabel }}</span>
      <el-icon v-if="showChevron" class="hc-header-chip__chevron"><ArrowDown /></el-icon>
    </button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="item in languages"
          :key="item.value"
          :command="item.value"
          :disabled="item.value === currentLanguage"
        >
          {{ item.label }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { setLocale, getLocale } from '@/i18n-element'
import { useAdminLanguageMenu } from '@/composables/useAdminLanguageMenu'
import { ADMIN_LANGUAGES } from '@/locales/adminLanguageCatalog'

const { languages } = useAdminLanguageMenu()

const props = defineProps({
  modelValue: {
    type: String,
    default: 'zh-CN'
  },
  placement: {
    type: String,
    default: 'bottom-end'
  },
  showChevron: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue'])

const currentLanguage = ref(getLocale() || props.modelValue)

const currentLabel = computed(() => {
  const current = languages.value.find((item) => item.value === currentLanguage.value)
    || ADMIN_LANGUAGES.find((item) => item.value === currentLanguage.value)
  return current?.label || '简体中文'
})

watch(() => props.modelValue, (newVal) => {
  if (newVal && newVal !== currentLanguage.value) {
    currentLanguage.value = newVal
  }
})

const changeLanguage = (lang) => {
  if (lang === currentLanguage.value) return
  if (!setLocale(lang)) return
  currentLanguage.value = lang
  emit('update:modelValue', lang)
}

function syncFromEvent(event) {
  const next = event?.detail || getLocale()
  if (next && next !== currentLanguage.value) {
    currentLanguage.value = next
    emit('update:modelValue', next)
  }
}

onMounted(() => {
  window.addEventListener('language-changed', syncFromEvent)
})

onUnmounted(() => {
  window.removeEventListener('language-changed', syncFromEvent)
})
</script>

<style scoped>
.lang-dropdown {
  display: inline-flex;
  align-items: center;
  line-height: 1;
  vertical-align: middle;
}
</style>
