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
<script setup>// ⚠️ vue API 必须显式导入：缺失时构建期不报错，运行时报 'xxx is not defined' 导致页面白屏。
import { computed } from 'vue'

import useAppStore from '@/stores/modules/app'

const appStore = useAppStore()
/**
 * 图片地址拼接。不调 appStore.getImageUrl()：该 Pinia action 在装修编辑器
 * 这个懒加载 chunk 里取不到（报 "o.getImageUrl is not a function"）。
 * 直接读 state 的 oss_domain 自己拼，逻辑与 store action 一致。
 */
const getImageUrl = (url) => {
    if (!url) return ''
    if (url.startsWith('http')) return url
    let domain = ''
    try {
        domain = appStore.config?.oss_domain || ''
    } catch (e) {
        domain = ''
    }
    return domain + url
}

const props = defineProps({
    content: {
        type: Object,
        default: () => ({})
    },
    styles: {
        type: Object,
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
