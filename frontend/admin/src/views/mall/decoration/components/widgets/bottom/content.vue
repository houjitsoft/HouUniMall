<template>
    <div class="py-3 bg-white flex justify-between p-1">
        <div class="w-[45%] flex justify-around">
            <div
                class="flex flex-col justify-center items-center"
                v-if="content.content.includes(1)"
            >
                <el-icon :size="18"><House /></el-icon>
                <div class="mt-1">首页</div>
            </div>
            <div
                class="flex flex-col justify-center items-center"
                v-if="content.content.includes(2)"
            >
                <el-icon :size="18"><Service /></el-icon>
                <div class="mt-1">客服</div>
            </div>
            <div
                class="flex flex-col justify-center items-center"
                v-if="content.content.includes(3)"
            >
                <el-icon :size="18"><Star /></el-icon>
                <div class="mt-1">收藏</div>
            </div>
            <div
                class="flex flex-col justify-center items-center"
                v-if="content.content.includes(4)"
            >
                <el-icon :size="18"><ShoppingCart /></el-icon>
                <div class="mt-1">购物车</div>
            </div>
        </div>
        <div class="w-[65%] flex justify-around">
            <div class="btn1" v-if="content.add_btn.show">加入购物车</div>
            <div class="btn2 ml-1" v-if="content.buy_btn.show">立即购买</div>
        </div>
    </div>
</template>

<script setup>


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
</script>

<style lang="scss" scoped>
.new-box {
    .new-item {
        margin-right: 10px;
        margin-bottom: 10px;
    }

    .new-item:nth-child(2n) {
        margin-right: 0;
    }
}
.btn1 {
    width: 100%;
    height: 40px;
    border: 1px solid #b67b2e;
    color: #b67b2e;
    border-radius: 20px;
    line-height: 40px;
    font-size: 12px;
    text-align: center;
}
.btn2 {
    width: 100%;
    height: 40px;
    border: 1px solid #b67b2e;
    color: white;
    background-color: #b67b2e;
    border-radius: 20px;
    line-height: 40px;
    font-size: 12px;
    text-align: center;
}
</style>
