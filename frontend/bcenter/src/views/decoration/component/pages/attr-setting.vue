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
<script lang="ts" setup>
import type { PropType } from 'vue'

import widgets from '../widgets'

const emits = defineEmits(['update:content'])
const handleUpdateContent = (data: any) => {
    emits('update:content', data)
}

defineProps({
    widget: {
        type: Object as PropType<Record<string, any>>,
        default: () => ({})
    },
    type: {
        type: String as PropType<'mobile' | 'pc'>,
        default: 'mobile'
    }
})
</script>
