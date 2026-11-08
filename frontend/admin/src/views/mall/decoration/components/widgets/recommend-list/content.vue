<template>
    <div class="recommend-list mx-[10px] my-[10px]">
        <div class="flex items-center mx-[10px] py-[15px] text-[17px] font-medium">
            <span class="text-black" :style="{ color: content.topTitleColor }">{{
                content.topTitle
            }}</span>
            <span
                class="text-[14px] text-[#999] ml-2"
                :style="{ color: content.topSubTitleColor }"
                >{{ content.topSubtitle }}</span
            >
        </div>

        <div class="recommend-box flex mb-[10px]">
            <div
                class="recommend-item rounded-lg w-[108px] p-[8px]"
                v-for="(item, index) in content.data"
                :key="index"
                :style="{
                    'background-color': item.background_type == 1 ? item.background_color : '',
                    'background-image':
                        item.background_type == 2
                            ? `url(${getImageUrl(item.background_image)})`
                            : ''
                }"
            >
                <!-- <image-contain :src="item.cover" :width="90" :height="90" preview-teleported fit="contain" /> -->
                <decoration-img width="90px" height="90px" :src="item.cover" alt="" />
                <div class="mt-[10px] text-center text-white">
                    <div class="text-[14px] font-medium">{{ item.title }}</div>
                    <div class="text-[13px] mt-1">{{ item.subhead }}</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import DecorationImg from '../../decoration-img.vue'
import useAppStore from '@/stores/modules/app'
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
</script>

<style lang="scss" scoped>
.recommend-box {
    overflow-x: auto;

    .recommend-item {
        margin-right: 10px;
        margin-bottom: 10px;

        background-repeat: no-repeat;
        background-size: cover;
    }

    // .recommend-item:nth-child(2n) {
    //     margin-right: 0;
    // }
}
</style>
