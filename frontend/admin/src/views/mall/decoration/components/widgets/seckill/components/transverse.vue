<template>
    <div class="w-full">
        <div class="rounded-lg flex w-full mb-2" v-for="item in content.showNum" :key="item">
            <el-image
                :src="dataList[item - 1] ? dataList[item - 1]?.goods.image : shopImg"
                class="w-[100px] h-[100px] flex-none"
            ></el-image>
            <div class="p-1 flex-1">
                <div class="text-sm flex-1 text_hidden" v-if="content.showLists.includes(1)">
                    {{ dataList[item - 1]?.goods.name || '-' }}
                </div>
                <div class="form-tips">
                    已抢 {{ dataList[item - 1]?.ms_sku_item.sale_num || '0' }} 件
                </div>
                <div class="flex">
                    <div
                        class="text-[#FF2C3C] text-xl font-medium"
                        v-if="content.showLists.includes(2)"
                    >
                        ¥{{ dataList[item - 1]?.ms_sku_item.price || '-' }}
                    </div>
                    <div class="text-white bg-[#FF2C3C] py-1 px-2 ml-auto rounded-full">去抢购</div>
                </div>
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
