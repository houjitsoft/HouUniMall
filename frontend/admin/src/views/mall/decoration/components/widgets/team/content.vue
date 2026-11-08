<template>
    <div class="p-2">
        <div class="flex bg-white p-2 rounded-t-lg">
            <div v-if="content.titleType == 1">
                {{ content.title }}
            </div>
            <div v-else>
                <el-image
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
            </div>

            <div class="ml-auto" :style="{ color: content.rightcolor }" v-if="content.rightBtn">
                {{ content.rightTitle }}
            </div>
        </div>
        <div class="bg-style" v-if="content.showType != 1"></div>
        <div class="" v-if="content.showType == 1">
            <div v-for="(item, index) in 3" :key="item" class="mb-2 bg-white p-2 flex">
                <img src="./image/goods1.png" class="w-[100px]" fit="cover" v-if="index == 0" />
                <img src="./image/goods2.png" class="w-[100px]" fit="cover" v-if="index == 1" />
                <img src="./image/goods3.png" class="w-[100px]" fit="cover" v-if="index == 2" />
                <div class="p-1 flex flex-col justify-between w-full">
                    <div>
                        <div class="text-[14px]">马克图布x王丽鸥 联名IP款 帕恰狗</div>
                        <div class="text-[10px] text-[#999]">已抢1000件</div>
                    </div>
                    <div class="flex items-center w-full">
                        <span class="text-[10px]">¥</span><span class="text-[14px]">268</span>
                        <span class="text-[10px] ml-2 line-through text-[#999]">¥</span
                        ><span class="text-[10px] line-through text-[#999]">268</span>
                        <div
                            class="ml-auto w-[64px] h-[22px] bg-[#B67B2E] text-white text-center text-[12px] leading-[22px] rounded-full"
                        >
                            去拼团
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="flex flex-wrap justify-between mt-[-150px]" v-if="content.showType == 2">
            <div v-for="(item, index) in 3" :key="item" class="w-[49%] mb-2 bg-white p-2">
                <img src="./image/goods1.png" class="w-full" fit="cover" v-if="index == 0" />
                <img src="./image/goods2.png" class="w-full" fit="cover" v-if="index == 1" />
                <img src="./image/goods3.png" class="w-full" fit="cover" v-if="index == 2" />
                <div class="p-1">
                    <div class="text-[14px]">马克图布x王丽鸥 联名IP款 帕恰狗</div>
                    <div class="flex items-center">
                        <span class="text-[10px]">¥</span><span class="text-[14px]">268</span>
                        <img src="./image/team.png" class="ml-2" fit="cover" />
                    </div>
                </div>
            </div>
        </div>
        <div
            class="flex flex-wrap justify-between mt-[-150px]"
            v-if="content.showType == 3 || content.showType == 4"
        >
            <div v-for="(item, index) in 3" :key="item" class="w-[32%] mb-2 bg-white p-2">
                <img src="./image/goods1.png" class="w-full" fit="cover" v-if="index == 0" />
                <img src="./image/goods2.png" class="w-full" fit="cover" v-if="index == 1" />
                <img src="./image/goods3.png" class="w-full" fit="cover" v-if="index == 2" />
                <div class="flex items-baseline justify-center mt-1">
                    <span class="text-[10px]" v-if="content.showType == 4">¥</span
                    ><span class="text-[16px]" v-if="content.showType == 4">268</span>

                    <span class="text-[10px] line-through text-[#999]">¥</span
                    ><span class="text-[12px] line-through text-[#999]">268</span>
                </div>
                <div class="flex items-center justify-center" v-if="content.showType == 3">
                    <img src="./image/price.png" fit="cover" />
                </div>
            </div>
        </div>
    </div>
</template>
<script setup>// ⚠️ vue API 必须显式导入：缺失时构建期不报错，运行时报 'xxx is not defined' 导致页面白屏。
import { ref } from 'vue'


import { getMsDataList } from '@/api/decoration'
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

const dataList = ref([])

const getDataList = async () => {
    // 预览态取真实拼团数据；后台无对应路由时静默降级，不影响编辑器使用。
    try {
        const res = await getMsDataList()
        dataList.value = res?.data?.lists ?? res?.lists ?? res?.data ?? []
    } catch (e) {
        dataList.value = []
    }
}

getDataList()
</script>

<style lang="scss" scoped>
.bg-style {
    height: 150px;
    background-image: linear-gradient(to bottom, rgb(255, 255, 255), rgba(255, 0, 0, 0));
}
</style>
