<template>
    <div class="pages-setting">
        <el-scrollbar class="w-full" style="height: 100%">
            <keep-alive>
                <component
                    :is="widgets[widget?.name]?.attr"
                    :content="widget?.content"
                    :styles="widget?.styles"
                    :type="type"
                    @update:content="handleUpdateContent"
                />
            </keep-alive>
        </el-scrollbar>
    </div>
</template>
<script setup>
import widgets from '../widgets'

const emits = defineEmits(['update:content'])

/**
 * 处理内容更新事件
 * @param {any} data - 更新的内容数据
 */
const handleUpdateContent = (data) => {
    emits('update:content', data)
}

defineProps({
    widget: {
        type: Object,
        default: () => ({})
    },
    type: {
        type: String,
        default: 'mobile'
    }
})
</script>
