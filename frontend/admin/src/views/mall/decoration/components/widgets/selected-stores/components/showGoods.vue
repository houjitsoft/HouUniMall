<template>
    <div>
        <div class="bg-white rounded-lg p-2 mb-[10px]" v-for="item in content.showNum" :key="item">
            <div class="flex">
                <el-image
                    :src="storeData[item - 1] ? storeData[item - 1]?.logo : shopImg"
                    class="w-[50px] h-[50px] flex-none rounded"
                ></el-image>
                <div class="flex flex-col justify-between ml-2">
                    <div class="text_hidden_line1">{{ storeData[item - 1]?.name || '-' }}</div>

                    <el-rate
                        :model-value="storeData[item - 1]?.score"
                        disabled
                        show-score
                        score-templat="100"
                        text-color="#ff9900"
                        score-template="{value}"
                    />
                </div>
                <div
                    class="flex-none ml-auto p-1 text-xs self-start text-white bg-gradient-to-r from-[#F95F2F] to-[#FF2C3C] rounded-full"
                >
                    进店逛逛
                </div>
            </div>
            <div class="flex">
                <div
                    class="flex flex-col w-[80px] mr-[10px]"
                    v-for="(item1, index) in storeData[item - 1]?.goods"
                    :key="index"
                >
                    <el-image :src="item1?.image" class="w-[80px] h-[80px] rounded"></el-image>
                    <div class="text_hidden_line1 text-xs">{{ item1?.name }}</div>
                    <div>
                        <span class="text-[#FF2C3C]">¥{{ item1?.price }}</span>
                        <span class="form-tips ml-2">¥{{ item1?.line_price }}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import shopImg from '../images/shop.png'
const props = defineProps({
    content: {
        type: Object,
        default: () => ({})
    },
    styles: {
        type: Object,
        default: () => ({})
    },
    storeData: {
        type: Object,
        default: () => ({})
    }
})
</script>

<style scoped lang="scss"></style>
