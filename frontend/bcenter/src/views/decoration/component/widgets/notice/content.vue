<template>
    <div class="notice mx-2 bg-white px-2 py-[8px] rounded-lg">
        <div class="flex items-center h-full">
            <div
                v-if="content.title_type == 1"
                class="h-[19px] text-center text-xs rounded-sm flex items-center"
            >
                <img class="flex-none" style="height: 19px" src="./images/notice.png" />
                <span class="ml-1">
                    {{ content.title }}
                </span>
            </div>
            <el-image
                v-else
                :src="getImageUrl(content.titile_img)"
                class="h-[20px] w-[56px]"
                fit="cover"
            >
                <template #error>
                    <div class="w-full h-full flex justify-center items-center bg-[#ebf2ff]">
                        <el-icon :size="14"><PictureFilled /></el-icon>
                    </div>
                </template>
            </el-image>

            <div class="flex items-center">
                <div class="mx-[6px] text-[#f1f1f1]">|</div>
                <div class="truncate ml-[4px]">公告标题</div>
            </div>
            <el-icon class="ml-auto" v-if="content.right_btn"><ArrowRight /></el-icon>
        </div>
    </div>
</template>
<script lang="ts" setup>
import type { PropType } from 'vue'
import type options from './options'
import useAppStore from '@/stores/modules/app'

type OptionsType = ReturnType<typeof options>
const { getImageUrl } = useAppStore()

const props = defineProps({
    content: {
        type: Object as PropType<OptionsType['content']>,
        default: () => ({})
    },
    styles: {
        type: Object as PropType<OptionsType['styles']>,
        default: () => ({})
    }
})

const getImage = computed(() => {
    const { data } = props.content
    if (Array.isArray(data)) {
        return data[0] ? data[0].image : ''
    }
    return ''
})
</script>
