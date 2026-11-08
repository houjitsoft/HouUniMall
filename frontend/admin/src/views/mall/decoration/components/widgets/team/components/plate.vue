<template>
    <div class="grid grid-cols-2 gap-1 h-[180px] mb-2" v-for="item in 1" :key="item">
        <div class="p-3 bg-primary-light-9 rounded-lg flex flex-col card">
            <div class="truncate" v-if="content.showLists.includes(1)">
                {{ dataList[1 + item * 3 - 4]?.goods.name || '-' }}
            </div>
            <div class="text-[#FF2C3C]" v-if="content.showLists.includes(2)">
                ¥{{ dataList[1 + item * 3 - 4]?.ms_sku_item.price || '-' }}
            </div>
            <div class="bg-[#FF2C3C] text-white px-2 rounded-full self-start">去抢购</div>
            <div class="felx-1 flex items-end self-end mt-auto">
                <el-image
                    :src="
                        dataList[1 + item * 3 - 4]
                            ? dataList[1 + item * 3 - 4]?.goods.image
                            : shopImg
                    "
                    class="w-[80px] h-[80px] rounded-lg"
                ></el-image>
            </div>
        </div>
        <div class="grid grid-rows-2 grid-flow-col gap-1 card">
            <div class="p-2 bg-primary-light-8 rounded-lg flex flex-col">
                <template v-if="dataList[2 + item * 3 - 4]">
                    <div class="text_hidden_line1">
                        {{ dataList[2 + item * 3 - 4]?.goods.name || '-' }}
                    </div>
                    <div class="flex justify-between">
                        <div>
                            <div class="text-[#FF2C3C]" v-if="content.showPrice == '1'">
                                ¥{{ dataList[2 + item * 3 - 4]?.ms_sku_item.price || '-' }}
                            </div>
                            <div class="bg-[#FF2C3C] text-white px-2 rounded-full self-start">
                                去抢购
                            </div>
                        </div>
                        <el-image
                            :src="dataList[2 + item * 3 - 4]?.goods.image"
                            class="w-[50px] h-[50px] rounded-lg"
                        ></el-image>
                    </div>
                </template>
            </div>
            <div class="p-2 bg-primary-light-7 rounded-lg card">
                <template v-if="dataList[3 + item * 3 - 4]">
                    <div class="text_hidden_line1">
                        {{ dataList[3 + item * 3 - 4]?.goods.name || '-' }}
                    </div>
                    <div class="flex justify-between">
                        <div>
                            <div class="text-[#FF2C3C]" v-if="content.showPrice == '1'">
                                ¥{{ dataList[3 + item * 3 - 4]?.ms_sku_item.price || '-' }}
                            </div>
                            <div class="bg-[#FF2C3C] text-white px-2 rounded-full self-start">
                                去抢购
                            </div>
                        </div>
                        <el-image
                            :src="dataList[3 + item * 3 - 4]?.goods.image"
                            class="w-[50px] h-[50px] rounded-lg"
                        ></el-image>
                    </div>
                </template>
            </div>
        </div>
    </div>
</template>

<script setup>
import { isObjEmpty } from '@/utils/util'
import shopImg from '@/views/decoration/images/theme_example/shop.png'

const props = defineProps({
    content: {
        type: Object,
        default: () => ({})
    },
    styles: {
        type: Object,
        default: () => ({})
    },
    dataList: {
        type: Array,
        default: () => []
    }
})
</script>

<style scoped lang="scss">
.card {
    &:nth-child(1) {
        background: url('../image/bg-red.png') no-repeat center;
    }
    &:nth-child(2) {
        background: url('../image/bg-green.png') no-repeat center;
    }
    &:nth-child(3) {
        background: url('../image/bg-yellow.png') no-repeat center;
    }
}
</style>
