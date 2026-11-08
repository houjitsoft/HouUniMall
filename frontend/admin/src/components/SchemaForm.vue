<template>
  <div class="schema-form">
    <div class="schema-form__mode">
      <el-checkbox v-model="advanced">JSON 高级模式</el-checkbox>
    </div>

    <el-form v-if="!advanced" label-position="top" class="schema-form__fields">
      <template v-if="fields.length">
        <el-form-item v-for="field in fields" :key="field.key" :label="field.label">
          <el-input-number
            v-if="field.type === 'number'"
            v-model="inner[field.key]"
            :min="field.min"
            :max="field.max"
            controls-position="right"
          />
          <el-switch v-else-if="field.type === 'boolean'" v-model="inner[field.key]" />
          <el-select v-else-if="field.options.length" v-model="inner[field.key]" placeholder="请选择">
            <el-option v-for="opt in field.options" :key="opt" :label="opt" :value="opt" />
          </el-select>
          <el-input v-else v-model="inner[field.key]" :placeholder="field.label" />
        </el-form-item>
      </template>
      <el-empty v-else description="该插件未定义配置字段，可切换到 JSON 高级模式" :image-size="60" />
    </el-form>

    <el-input
      v-else
      v-model="jsonText"
      type="textarea"
      :rows="12"
      placeholder='合法 JSON，例如 {"autoPublish":true}'
      @blur="applyJson"
    />

    <el-alert
      v-if="jsonError"
      class="schema-form__error"
      :title="jsonError"
      type="error"
      :closable="false"
      show-icon
    />
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  /** 配置定义（configSchema），JSON 字符串或对象 */
  schema: { type: [String, Object], default: '{}' },
  /** 当前配置值，JSON 字符串或对象 */
  modelValue: { type: [String, Object], default: '{}' }
})
const emit = defineEmits(['update:modelValue'])

const advanced = ref(false)
const jsonText = ref('{}')
const jsonError = ref('')
const inner = ref({})

const parse = (value) => {
  if (!value) return {}
  if (typeof value === 'object') return { ...value }
  try {
    const parsed = JSON.parse(value)
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch {
    return {}
  }
}

const fields = computed(() => {
  const schema = parse(props.schema)
  return Object.entries(schema).map(([key, def]) => ({
    key,
    label: def?.label || key,
    type: def?.type || 'string',
    options: def?.enum || def?.options || [],
    min: typeof def?.min === 'number' ? def.min : undefined,
    max: typeof def?.max === 'number' ? def.max : undefined,
    fallback: def?.default
  }))
})

/** 用 schema 默认值兜底，再叠加已保存的配置 */
const buildInner = () => {
  const base = {}
  for (const field of fields.value) {
    if (field.fallback !== undefined) base[field.key] = field.fallback
  }
  return { ...base, ...parse(props.modelValue) }
}

inner.value = buildInner()
jsonText.value = JSON.stringify(inner.value, null, 2)

watch(
  () => [props.schema, props.modelValue],
  () => {
    inner.value = buildInner()
    jsonText.value = JSON.stringify(inner.value, null, 2)
    jsonError.value = ''
  }
)

watch(
  inner,
  (value) => {
    jsonText.value = JSON.stringify(value, null, 2)
    emit('update:modelValue', JSON.stringify(value))
  },
  { deep: true }
)

const applyJson = () => {
  try {
    inner.value = parse(jsonText.value)
    jsonError.value = ''
    emit('update:modelValue', JSON.stringify(inner.value))
  } catch {
    jsonError.value = '配置必须是合法 JSON'
  }
}

defineExpose({
  /** 供父组件保存前校验 */
  validate: () => {
    jsonError.value = ''
    if (!advanced.value) return true
    try {
      parse(jsonText.value)
      return true
    } catch {
      jsonError.value = '配置必须是合法 JSON'
      return false
    }
  }
})
</script>

<style scoped>
.schema-form__mode {
  margin-bottom: 14px;
}
.schema-form__error {
  margin-top: 12px;
}
</style>
