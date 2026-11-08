<template>
    <div class="">
        <div class="flex flex-wrap p-2 justify-between" v-if="content.show_type == 2">
            <div
                v-for="item in content.goodsLists.length ? content.goodsLists : [{}]"
                :key="item.id"
                class="bg-white mb-2 rounded-lg w-full h-[100px] flex"
            >
                <el-image :src="item.image" class="w-[100px] h-[100px] rounded-lg" fit="cover">
                    <template #error>
                        <div class="w-full h-full flex justify-center items-center bg-[#ebf2ff]">
                            <el-icon :size="28"><PictureFilled /></el-icon></div
                    ></template>
                </el-image>

                <div class="p-2 flex-1 flex flex-col justify-between">
                    <div class="text_hidden" v-if="content.show_content.includes(1)">
                        {{ item.name || '商品名称' }}
                    </div>
                    <div>
                        <div class="flex items-center">
                            <span class="text-[10px]" v-if="content.show_content.includes(2)"
                                >¥</span
                            >
                            <span class="ml-[2px]" v-if="content.show_content.includes(2)">
                                {{ item.min_price || 0 }}
                            </span>
                            <span
                                class="text-[10px] ml-2 line-through"
                                v-if="content.show_content.includes(3)"
                                style="color: #999; font-size: 10px"
                                >¥</span
                            >
                            <span
                                class="ml-[2px] line-through"
                                v-if="content.show_content.includes(3)"
                                style="color: #999; font-size: 10px"
                            >
                                {{ item.max_line_price || 0 }}
                            </span>
                            <span
                                class="ml-auto"
                                style="color: #999; font-size: 10px"
                                v-if="content.show_content.includes(4)"
                            >
                                已售{{ item.sale_num || 0 }}
                            </span>
                        </div>
                        <div class="text_hidden" v-if="content.show_content.includes(5)">
                            <span style="color: #999; font-size: 10px">
                                {{ item.shopName || '店铺名称' }}
                            </span>
                            <span class="ml-1" style="font-size: 10px"> 进店 </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div
            class="flex flex-wrap p-2 w-full"
            :class="{ 'pt-0': content.show_type == 1, 'justify-between': content.show_type == 3 }"
            v-else
        >
            <div
                v-for="(item, index) in content.goodsLists.length ? content.goodsLists : [{}]"
                :key="item.id"
                class="bg-white mb-2 rounded-lg mr-[3px]"
                :class="
                    content.show_type == 1
                        ? 'w-full'
                        : content.show_type == 3
                        ? 'w-[49%]'
                        : 'w-[32%]'
                "
                v-show="(index < 3 && content.show_type == 5) || content.show_type != 5"
            >
                <el-image
                    :src="item.image"
                    class="w-full rounded-t-lg"
                    fit="cover"
                    :class="
                        content.show_type == 1
                            ? 'h-[300px]'
                            : content.show_type == 3
                            ? 'h-[200px]'
                            : 'h-[130px]'
                    "
                >
                    <template #error>
                        <div class="w-full h-full flex justify-center items-center bg-[#ebf2ff]">
                            <el-icon :size="28"><PictureFilled /></el-icon>
                        </div>
                    </template>
                </el-image>
                <div class="text_hidden px-2" v-if="content.show_content.includes(1)">
                    {{ item.name || '商品名称' }}
                </div>
                <div class="px-2 flex items-center pb-2 w-full">
                    <div class="w-full">
                        <span class="text-[10px]" v-if="content.show_content.includes(2)">¥</span>
                        <span class="ml-[2px]" v-if="content.show_content.includes(2)">
                            {{ item.min_price || 0 }}
                        </span>
                        <span
                            class="text-[10px] ml-2 line-through"
                            v-if="content.show_content.includes(3)"
                            style="color: #999; font-size: 10px"
                            >¥</span
                        >
                        <span
                            class="ml-[2px] line-through"
                            v-if="content.show_content.includes(3)"
                            style="color: #999; font-size: 10px"
                        >
                            {{ item.max_line_price || 0 }}
                        </span>
                        <div
                            class="ml-auto"
                            style="color: #999; font-size: 10px"
                            v-if="content.show_type == 4 || content.show_type == 5"
                            v-show="content.show_content.includes(4)"
                        >
                            已售{{ item.sale_num || 0 }}
                        </div>

                        <span
                            class=""
                            style="color: #999; font-size: 10px"
                            v-else
                            v-show="content.show_content.includes(4)"
                        >
                            已售{{ item.sale_num || 0 }}
                        </span>
                        <div
                            class="text_hidden"
                            v-if="content.show_content.includes(5)"
                            v-show="
                                content.show_type == 1 ||
                                content.show_type == 2 ||
                                content.show_type == 3
                            "
                        >
                            <span style="color: #999; font-size: 10px">
                                {{ item.shopName || '店铺名称' }}
                            </span>
                            <span class="ml-1" style="font-size: 10px"> 进店 </span>
                        </div>
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
import useAppStore from '@/stores/modules/app'

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
