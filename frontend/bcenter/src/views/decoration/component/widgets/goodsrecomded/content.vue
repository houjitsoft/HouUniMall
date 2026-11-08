<template>
    <div class="p-2">
        <div class="flex bg-white p-2 rounded-t-lg">
            <div v-if="content.title_type == 1">
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
            <div class="ml-auto text-[#888888]">更多</div>
        </div>
        <div class="p-2 rounded-b-lg flex flex-wrap justify-between">
            <div class="w-[49%] mb-2" v-for="item in 4" :key="item">
                <img :src="image1" alt="" class="w-full" v-if="item == 1" />
                <img :src="image2" alt="" class="w-full" v-if="item == 2" />
                <img :src="image3" alt="" class="w-full" v-if="item == 3" />
                <img :src="image4" alt="" class="w-full" v-if="item == 4" />
                <div class="bg-white p-2 rounded-b-lg">
                    <div>马克图布x王丽鸥 联名IP款 帕恰狗</div>
                    <div>
                        <span class="text-[8px]">￥</span>268
                        <span class="text-[10px] text-[#999999]">已售8件</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import type { PropType } from 'vue'
import type options from './options'
import useAppStore from '@/stores/modules/app'
import image1 from './images/image1.png'
import image2 from './images/image2.png'
import image3 from './images/image3.png'
import image4 from './images/image4.png'
const { getImageUrl } = useAppStore()

type OptionsType = ReturnType<typeof options>
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
</script>

<style lang="scss" scoped>
.add-btn {
    width: 20px;
    height: 20px;
    line-height: 17px;
    border-radius: 50%;
    background-color: #1a69fc;
    text-align: center;
    color: white;
    font-size: 16px;
}
.cart-btn {
    width: 20px;
    height: 20px;
    line-height: 17px;
    border-radius: 50%;
    background-color: #1a69fc;
}
</style>
