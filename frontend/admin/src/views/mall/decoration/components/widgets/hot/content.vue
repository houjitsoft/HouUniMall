<template>
    <div class="hot mx-[10px] my-[10px] bg-white rounded-lg overflow-hidden">
        <div class="hot-header flex justify-between text-white items-center py-[12px] px-[10px] mb-[8px]">
            <div class="text-lg font-medium">热门商品</div>
            <div class="more-btn flex items-center rounded-2xl text-xs px-[8px]">
                <span class="mt-[2px]">更多</span>
                <el-icon>
                    <ArrowRight />
                </el-icon>
            </div>
        </div>

        <div class="goods-list flex mb-[10px] p-[10px]" v-for="(item, index) in goodsList">
            <div class="w-[120px] h-[120px] relative">
                <img :src="imageArr[index]" v-if="index <= 3" class="w-[24px] h-[30px] absolute left-0 top-0 z-10"
                    alt="排名">
                <image-contain :src="item.image" :width="100" :height="100" preview-teleported fit="contain" />
            </div>
            <div class="ml-[10px]">
                <div class="line-2 w-[210px] h-[42px]">{{ item.name }}</div>
                <div class="flex items-end justify-between">
                    <div class="mt-[5px]">
                        <div class="buy-num-btn text-xs px-[10px] py-[2px] rounded-2xl">已经有 {{ item.all_sale_num }}人购买
                        </div>
                        <div class="text-xs mt-[5px]">
                            <span class="text-[#FF2C3C]">¥<span class="text-lg font-medium">{{ item.price
                            }}</span></span>
                            <span class="line-through text-[#999] ml-[4px]">¥{{ item.line_price }}</span>
                        </div>
                    </div>
                    <div class="buy-btn text-sm text-white px-[15px] py-[5px] rounded-full h-[28px]">立即抢</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>// ⚠️ vue API 必须显式导入：缺失时构建期不报错，运行时报 'xxx is not defined' 导致页面白屏。
import { ref } from 'vue'

// import { getDecorateHotGoods } from '@/api/decoration'
// import No1 from "./images/No.1.png"
// import No2 from "./images/No.2.png"
// import No3 from "./images/No.3.png"
// import No4 from "./images/No.4.png"

// const goodsList = ref([])
// const imageArr = [No1, No2, No3, No4]

// const getData = async () => {
//     const data = await getDecorateHotGoods({
//         limit: 10
//     })
//     goodsList.value = data
// }
// getData()
</script>

<style lang="scss" scoped>
.hot {
    &-header {
        background: url(./images/hot_bg.png) no-repeat;
        background-size: 340px 50px;
    }

    .more-btn {
        background-color: rgba($color: #FFFFFF, $alpha: 0.4);
    }

    .buy-num-btn {
        color: #F79C0C;
        background-color: rgba($color: #F79C0C, $alpha: 0.1);
    }

    .buy-btn {
        background: linear-gradient(90.00deg, #f95f2f 0%, #ff2c3c 100%);
    }
}
</style>