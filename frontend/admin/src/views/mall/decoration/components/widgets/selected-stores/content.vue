<template>
    <div class="seckill">
        <div class="flex p-3 items-end">
            <div class="text-xl font-medium" :style="{ color: content.topTitleColor }">
                {{ content.title }}
            </div>
            <div class="ml-1 text-info text-xs" :style="{ color: content.topSubTitleColor }">
                {{ content.subtitle }}
            </div>
            <div class="ml-auto text-info text-xs" :style="{ color: content.moreTitleColor }">
                {{ content.moretitle }} >
            </div>
        </div>
        <div class="px-2">
            <noGoods
                :store-data="sotreData"
                :content="content"
                v-if="content.sortType == '1'"
            ></noGoods>
            <showGoods
                :store-data="sotreData"
                :content="content"
                v-if="content.sortType == '2'"
            ></showGoods>
        </div>
    </div>
</template>
<script setup>// ⚠️ vue API 必须显式导入：缺失时构建期不报错，运行时报 'xxx is not defined' 导致页面白屏。
import { ref } from 'vue'

import noGoods from './components/noGoods.vue'
import showGoods from './components/showGoods.vue'
import { getSelectedStoreList } from '@/api/decoration'

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

const sotreData = ref([])

const getDataList = async () => {
    // 预览态取真实店铺；后台无对应路由时静默降级，不影响编辑器使用。
    try {
        const res = await getSelectedStoreList()
        sotreData.value = res?.data?.lists ?? res?.lists ?? res?.data ?? []
    } catch (e) {
        sotreData.value = []
    }
}

getDataList()
</script>

<style lang="scss" scoped></style>
