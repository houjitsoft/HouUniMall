<template>
    <el-menu
        :default-active="modelValue"
        class="w-[160px] min-h-[668px] pages-menu"
        @select="handleSelect"
    >
        <el-menu-item v-for="(item, key) in menus" :index="key" :key="item.id">
            <span>{{ item.name }}</span>
        </el-menu-item>
    </el-menu>
</template>
<script setup>
defineProps({
    menus: {
        type: Object,
        default: () => ({})
    },
    modelValue: {
        type: String,
        default: '1'
    }
})
const emit = defineEmits(['update:modelValue'])

/**
 * 菜单选择事件处理
 * @param {string} index - 选中的菜单项索引
 */
const handleSelect = (index) => {
    emit('update:modelValue', index)
}
</script>

<style lang="scss" scoped>
.pages-menu {
    :deep(.el-menu-item) {
        border-color: transparent;
        &.is-active {
            border-right-width: 2px;
            border-color: var(--el-color-primary);
            background-color: var(--el-color-primary-light-9);
        }
    }
}
</style>
