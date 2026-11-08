<template>
  <el-select
    :model-value="modelValue"
    :placeholder="placeholder"
    :loading="loading"
    :clearable="clearable"
    filterable
    :style="{ width: width }"
    @update:model-value="handleChange"
  >
    <el-option
      v-for="item in options"
      :key="item.activityId"
      :label="item.activityTitle"
      :value="item.activityId"
    >
      <span>{{ item.activityTitle }}</span>
      <span class="activity-select__meta">{{ enableText(item) }}</span>
    </el-option>
  </el-select>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { loadAllActivities } from '../api.js'

const props = defineProps({
  modelValue: { type: [Number, String], default: '' },
  placeholder: { type: String, default: '全部活动' },
  clearable: { type: Boolean, default: true },
  width: { type: String, default: '240px' },
  autoSelectFirst: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue', 'change', 'loaded'])

const options = ref([])
const loading = ref(false)

function enableText(row) {
  return Number(row.activityIsEnable) === 1 ? '可用' : '不可用'
}

function handleChange(value) {
  emit('update:modelValue', value)
  emit('change', value)
}

async function load() {
  loading.value = true
  try {
    options.value = await loadAllActivities()
    emit('loaded', options.value)
    if (props.autoSelectFirst && !props.modelValue && options.value.length) {
      handleChange(options.value[0].activityId)
    }
  } catch (error) {
    ElMessage.error(error?.message || '加载活动列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)

defineExpose({ reload: load })
</script>

<style scoped>
.activity-select__meta {
  float: right;
  margin-left: 12px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
