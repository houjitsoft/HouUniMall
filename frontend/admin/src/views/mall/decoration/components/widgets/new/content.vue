<template>
    <div class="new-list mx-[10px] my-[10px]">
        <div class="flex items-center">
            <div
                class="flex items-center mx-[10px] my-[15px] text-[17px] font-medium"
                :style="{ color: content.topTitleColor }"
            >
                {{ content.title }}
            </div>
            <div class="ml-1 text-info text-xs" :style="{ color: content.topSubTitleColor }">
                {{ content.subtitle }}
            </div>
        </div>

        <div class="new-box flex flex-wrap justify-between mb-[10px]">
            <image-contain
                :style="{ 'border-radius': '10px' }"
                v-show="item.image && false"
                v-for="item in content.data"
                :key="item"
                :src="getImageUrl(item.image)"
                :width="164"
                :height="225"
                preview-teleported
                fit="fill"
            />
            <div
                class="new-item rounded-lg overflow-hidden bg-white w-[164px]"
                v-for="(item, index) in goodsList"
                :key="item.id"
            >
                <image-contain
                    :src="item.image"
                    :width="164"
                    :height="164"
                    preview-teleported
                    fit="contain"
                />
                <div class="ml-[10px]">
                    <div class="line-2 h-[40px] pt-[5px] leading-[18px] text_hidden">
                        {{ item.name }}
                    </div>
                    <div class="mt-[5px] mb-[10px] text-xs">
                        <span class="text-[#FF2C3C]"
                            >¥<span class="text-lg font-medium">{{ item.min_price }}</span></span
                        >
                        <span class="line-through text-[#999] ml-[4px]"
                            >¥{{ item.min_line_price }}</span
                        >
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>// ⚠️ vue API 必须显式导入：缺失时构建期不报错，运行时报 'xxx is not defined' 导致页面白屏。
import { ref } from 'vue'

import { getGoodsLists } from '@/api/goods/goods'
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

const goodsList = ref([])
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

const getData = async () => {
    // 预览态取真实商品；后台无对应路由时静默降级，不影响编辑器使用。
    try {
        const res = await getGoodsLists({ limit: 5 })
        goodsList.value = res?.data?.lists ?? res?.lists ?? []
    } catch (e) {
        goodsList.value = []
    }
}
getData()
</script>

<style lang="scss" scoped>
.new-box {
    .new-item {
        margin-right: 10px;
        margin-bottom: 10px;
    }

    .new-item:nth-child(2n) {
        margin-right: 0;
    }
}
</style>
