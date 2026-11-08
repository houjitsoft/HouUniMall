<template>
    <div class="p-2">
        <div class="bg-white p-2 rounded-lg">
            <div class="flex justify-between items-center">
                <div class="font-medium">{{ content.title }}</div>
                <div class="form-tips">更多</div>
            </div>
            <div class="flex mt-2">
                <div class="w-1/3">
                    <img src="./images/image1.png" alt="" />
                    <div class="text-sm px-1 pt-1">品牌秋季男士卫衣灰色时尚戴帽色...</div>
                    <div class="px-1">
                        <span class="text-xs">¥</span>
                        <span class="text-xl font-medium">268.8</span>
                    </div>
                </div>
                <div class="w-1/3 ml-2">
                    <img src="./images/image2.png" alt="" />
                    <div class="text-sm px-1 pt-1">品牌秋季男士卫衣灰色时尚戴帽色...</div>
                    <div class="px-1">
                        <span class="text-xs">¥</span>
                        <span class="text-xl font-medium">268.8</span>
                    </div>
                </div>
                <div class="w-1/3 ml-2">
                    <img src="./images/image3.png" alt="" />
                    <div class="text-sm px-1 pt-1">品牌秋季男士卫衣灰色时尚戴帽色...</div>
                    <div class="px-1">
                        <span class="text-xs">¥</span>
                        <span class="text-xl font-medium">268.8</span>
                    </div>
                </div>
            </div>
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
</style>
