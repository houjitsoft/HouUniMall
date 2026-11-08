<template>
  <el-menu-item
    v-if="!hasChildren"
    :index="menuIndex"
  >
    <el-icon v-if="item.icon && iconComponent">
      <component :is="iconComponent" />
    </el-icon>
    <template #title>{{ item.name }}</template>
  </el-menu-item>

  <el-sub-menu v-else :index="String(item.id)">
    <template #title>
      <el-icon v-if="item.icon && iconComponent">
        <component :is="iconComponent" />
      </el-icon>
      <span>{{ item.name }}</span>
    </template>
    <SecondaryMenuItem
      v-for="child in item.children"
      :key="child.id"
      :item="child"
      :get-icon="getIcon"
    />
  </el-sub-menu>
</template>

<script setup>
import { computed } from 'vue'
import { isMallWorkbenchMenu, isOaWorkbenchMenu, resolveMenuItemPath } from '@/utils/menuNavigate'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  getIcon: {
    type: Function,
    required: true
  }
})

const hasChildren = computed(() => {
  if (isOaWorkbenchMenu(props.item) || isMallWorkbenchMenu(props.item)) return false
  const ch = props.item?.children
  return Array.isArray(ch) && ch.length > 0
})

const menuIndex = computed(() => resolveMenuItemPath(props.item))

const iconComponent = computed(() => props.getIcon(props.item?.icon))
</script>

<script>
export default {
  name: 'SecondaryMenuItem'
}
</script>
