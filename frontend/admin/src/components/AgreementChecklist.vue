<template>
  <div class="agreement-checklist">
    <el-checkbox
      v-for="item in agreements"
      :key="item.code"
      v-model="checked[item.code]"
      class="checklist-item"
      @change="handleChange"
    >
      <span class="checklist-text">
        我已阅读并同意
        <el-link type="primary" @click.prevent="openAgreement(item)">《{{ item.name }}》</el-link>
        <el-tag v-if="isRequired(item)" size="small" type="danger" effect="plain" class="checklist-tag">必选</el-tag>
      </span>
    </el-checkbox>
    <div v-if="error" class="checklist-error">{{ error }}</div>
  </div>
</template>

<script setup>
/**
 * 注册协议勾选组（可复用）。
 *
 * 协议清单来自后端「协议定义 + 生效版本」表，按场景下发；接口不可用时回退内置文案，
 * 保证注册页可用。父组件通过 ref 调用 `validate()` 做提交前校验、`getConsents()` 取
 * 回传后端的 {code, versionNo} 列表。
 */
import { ref, reactive, onMounted } from 'vue'
import { loadAgreements, showAgreement } from '@/utils/showLegalAgreement'

const props = defineProps({
  /** 场景：register / login / shop_join */
  scene: {
    type: String,
    default: 'register'
  },
  /** 校验失败时的通用提示（各注册方式文案可定制） */
  emptyMessage: {
    type: String,
    default: '请阅读并勾选同意全部必选协议'
  }
})

const emit = defineEmits(['change'])

const agreements = ref([])
const checked = reactive({})
const error = ref('')
/** 清单是否来自后端接口（决定提交时是否回传版本号） */
const fromApi = ref(false)

const isRequired = (item) => Number(item?.required) === 1

const openAgreement = (item) => {
  showAgreement(item, props.scene)
}

const handleChange = () => {
  validate({ silent: true })
  emit('change', { complete: isComplete(), consents: getConsents(), fromApi: fromApi.value })
}

/** 必选协议是否全部勾选 */
const isComplete = () => {
  const requiredList = agreements.value.filter(isRequired)
  if (requiredList.length === 0) {
    return true
  }
  return requiredList.every((item) => !!checked[item.code])
}

/** 未勾选的必选协议名称 */
const missingNames = () => agreements.value
  .filter(isRequired)
  .filter((item) => !checked[item.code])
  .map((item) => item.name)

/**
 * 校验：必选协议全部勾选。
 * @param {{silent?: boolean}} [options] silent=true 时只更新提示、不返回最终结论
 * @returns {{ok: boolean, message: string}}
 */
const validate = (options = {}) => {
  const missing = missingNames()
  if (missing.length > 0) {
    error.value = `请阅读并勾选同意《${missing.join('》《')}》后再提交`
    return { ok: false, message: error.value }
  }
  error.value = ''
  if (options.silent) {
    return { ok: true, message: '' }
  }
  return { ok: true, message: '' }
}

/**
 * 回传后端的协议列表：仅包含已勾选项。
 * 清单不来自接口时返回 null，由调用方决定降级（不上报，避免版本号被误判为过期）。
 */
const getConsents = () => {
  if (!fromApi.value) {
    return null
  }
  return agreements.value
    .filter((item) => checked[item.code])
    .map((item) => ({ code: item.code, versionNo: item.versionNo }))
}

const clearError = () => {
  error.value = ''
}

onMounted(async () => {
  try {
    const { agreements: list, fromApi: api } = await loadAgreements(props.scene)
    agreements.value = list
    fromApi.value = api
    list.forEach((item) => {
      checked[item.code] = Number(item.defaultChecked) === 1
    })
    validate({ silent: true })
  } catch (e) {
    console.warn('[AgreementChecklist] 加载协议失败', e)
    agreements.value = []
    fromApi.value = false
  }
})

defineExpose({
  validate,
  getConsents,
  isComplete,
  missingNames,
  clearError,
  fromApi,
  agreements
})
</script>

<style scoped>
.agreement-checklist {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
}

.checklist-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  height: auto;
  margin-right: 0;
  line-height: 1.6;
}

.checklist-item :deep(.el-checkbox__label) {
  white-space: normal;
  line-height: 1.5;
}

.checklist-text {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

.checklist-tag {
  margin-left: 4px;
  transform: scale(0.85);
}

.checklist-error {
  margin-top: 2px;
  font-size: 12px;
  color: #f56c6c;
  line-height: 1.5;
}
</style>
