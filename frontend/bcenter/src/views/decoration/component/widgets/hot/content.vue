<template>
    <div class="p-2">
        <div class="bg-white p-2 rounded-lg title-style" v-if="content.style == 1">
            <div
                class="title-content"
                :style="{
                    color: content.title_color,
                    'font-size': `${content.title_size}px`
                }"
            >
                {{ content.title }}
            </div>
            <div
                class="title-subtitle"
                :style="{
                    color: content.subtitle_color,
                    'font-size': `${content.subtitle_size}px`
                }"
            >
                {{ content.subtitle }}
            </div>
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
import DecorationImg from '../../decoration-img.vue'
import image1 from '../goodsrecomded/images/image1.png'
import image2 from '../goodsrecomded/images/image2.png'
import image3 from '../goodsrecomded/images/image3.png'
import image4 from '../goodsrecomded/images/image4.png'
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
.title-style {
    text-align: center;
    .title-content {
        display: inline-flex;
        align-items: center;
        &::before,
        &::after {
            content: '';
            display: inline-block;
            width: 49px;
            height: 1px;
            margin: 0 10px;
            background: #dcdfe6;
        }
    }
}
</style>
