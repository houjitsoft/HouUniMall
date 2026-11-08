<template>
    <div class="hot-goods mx-[10px] my-[10px]">

        <div class="goods-list flex mb-[10px] p-[10px] bg-white rounded-lg overflow-hidden"
            v-for="(item, index) in goodsList">
            <image-contain :src="item.image" :width="90" :height="90" preview-teleported fit="contain" />
            <div class="ml-[10px]">
                <div class="line-2 w-[210px] h-[40px]">{{ item.name }}</div>
                <div class="mt-[5px]">
                    <div class="inline-block pr-2 text-xs rounded-full hot-sales-num">
                        <img src="./images/icon_hot.png" class="w-[20px] h-[20px] inline-block" alt="">
                        <span class="text-[#FF2C3C] ml-2">销量 {{ item.all_sale_num }}</span>
                    </div>
                    <div class="text-xs mt-[5px]">
                        <span class="text-[#FF2C3C]">¥<span class="text-lg font-medium">{{ item.price
                        }}</span></span>
                        <span class="line-through text-[#999] ml-[4px]">¥{{ item.line_price }}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>// ⚠️ vue API 必须显式导入：缺失时构建期不报错，运行时报 'xxx is not defined' 导致页面白屏。
import { ref } from 'vue'

// import { getDecorateHotGoods } from '@/api/decoration'

// const goodsList = ref([])

// const getData = async () => {
//     const data = await getDecorateHotGoods({})
//     goodsList.value = data
// }
// getData()
</script>

<style lang="scss" scoped>
.hot-goods {
    .hot-sales-num {

        background-color: rgba($color: #ed5349, $alpha: 0.1);
    }
}
</style>