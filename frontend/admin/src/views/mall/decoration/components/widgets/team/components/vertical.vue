<template>
    <div class="flex overflow-x-scroll">
        <div class="flex flex-col w-[110px] mr-2" v-for="item in content.showNum" :key="item">
            <el-image
                :src="dataList[item - 1] ? dataList[item - 1]?.goods.image : shopImg"
                class="w-[100px] h-[100px] rounded-lg"
            ></el-image>
            <div class="truncate w-[100px] mt-2" v-if="content.showLists.includes(1)">
                {{ dataList[item - 1]?.goods.name || '-' }}
            </div>
            <div>
                <span
                    class="bg-[#FF2C3C] rounded text-white text-sm px-1"
                    v-if="content.showLists.includes(3)"
                    >抢</span
                >
                <span class="text-base text-[#FF2C3C] ml-2" v-if="content.showLists.includes(2)"
                    >¥{{ dataList[item - 1]?.ms_sku_item.price || '-' }}</span
                >
            </div>
        </div>
    </div>
</template>

<script setup>
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

<style scoped lang="scss"></style>
