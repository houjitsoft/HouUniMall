<template>
    <div class="news">
        <div class="flex items-center news-title mx-[10px] my-[15px] text-[17px] font-medium">
            最新资讯
        </div>
        <div
            v-for="item in newsList"
            :key="item.id"
            class="news-card flex bg-white px-[10px] py-[16px] text-[#333] border-[#f2f2f2] border-b"
        >
            <div class="mr-[10px]" v-if="item.image">
                <img :src="item.image" class="w-[120px] h-[90px] object-contain" />
            </div>
            <div class="flex flex-col justify-between flex-1">
                <div class="text-[15px] font-medium line-clamp-2">{{ item.title }}</div>
                <div class="line-clamp-1 text-sm mt-[8px]">
                    {{ item.desc }}
                </div>

                <div class="text-[#999] text-xs w-full flex justify-between mt-[8px]">
                    <div>{{ item.create_time }}</div>
                    <div class="flex items-center">
                        <icon name="el-icon-View" />
                        <div class="ml-[5px]">{{ item.click }}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>// ⚠️ vue API 必须显式导入：缺失时构建期不报错，运行时报 'xxx is not defined' 导致页面白屏。
import { ref } from 'vue'

import { getDecorateArticle } from '@/api/decoration'
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
const newsList = ref([])
const getData = async () => {
    // 预览态取真实资讯；接口不可用（后台无 decorate 路由/token 校验）时静默降级，
    // 不影响编辑器使用，页面保存时仍以 content 里已配置的数据为准。
    try {
        const res = await getDecorateArticle({ limit: 10 })
        newsList.value = res?.data?.lists ?? res?.lists ?? res?.data ?? []
    } catch (e) {
        newsList.value = []
    }
}
getData()
</script>

<style lang="scss" scoped>
.news {
    .news-title {
        &::before {
            content: '';
            width: 4px;
            height: 17px;
            display: block;
            margin-right: 5px;
            background: #4173ff;
        }
    }
}
</style>
