<template>
  <el-cascader ref="cascaderRef" :model-value="modelValue" :options="emptyOptions" :props="areaCascaderProps"
    :style="{ width }" clearable :placeholder="placeholder" @update:model-value="emit('update:modelValue', $event)"
    @expand-change="emit('expand-change', $event)" @change="emit('change', $event)" />
</template>

<script setup>
import { ref, watch } from 'vue'
import {
  fetchDistrictChildren,
  preloadRootDistricts,
  seedRootDistricts,
} from './districtCache'

const props = defineProps({
  modelValue: {
    type: [String, Array],
    default: '',
  },
  /** 兼容旧用法：父级已加载的省级数据会写入共享缓存，避免重复请求 */
  options: {
    type: Array,
    default: () => [],
  },
  placeholder: {
    type: String,
    default: '请选择地址',
  },
  width: {
    type: String,
    default: '400px',
  },
})

const emit = defineEmits(['update:modelValue', 'expand-change', 'change'])

const cascaderRef = ref(null)
const emptyOptions = []

watch(
  () => props.options,
  (list) => seedRootDistricts(list),
  { immediate: true },
)

function handleAreaLazyLoad(node, resolve) {
  const parentId = !node || node.level === 0 ? 0 : node.data.id
  fetchDistrictChildren(parentId).then(resolve)
}

const areaCascaderProps = {
  lazy: true,
  label: 'name',
  value: 'id',
  children: 'cities',
  leaf: (_data, node) => node.level >= 3,
  lazyLoad: handleAreaLazyLoad,
}

preloadRootDistricts

defineExpose({
  getCheckedNodes: () => cascaderRef.value?.getCheckedNodes?.(),
})
</script>
