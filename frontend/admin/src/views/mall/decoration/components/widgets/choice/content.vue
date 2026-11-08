<template>
    <div class="p-1">
        <div class="flex bg-white p-1 mx-1 rounded-t-lg">
            <div
                v-for="(item, index) in content.data"
                :key="item.title"
                class="w-1/4 flex flex-col justify-center items-center pt-2"
                v-show="index < 4"
            >
                <div class="text-[14px]">
                    {{ item.title }}
                </div>
                <div
                    class="!m-0 !text-[10px]"
                    :class="
                        index == 0
                            ? ['checked-box', 'rounded-lg', 'p-1', 'text-white']
                            : 'form-tips'
                    "
                >
                    {{ item.sub_title }}
                </div>
            </div>
        </div>
        <div class="mx-1 bg-style"></div>
        <div
            class="flex flex-wrap px-1 mt-[-250px]"
            :class="{ 'pt-0': content.show_type == 1, 'justify-between': content.show_type == 2 }"
        >
            <div
                v-for="item in content.data[0].goodsLists.list"
                :key="item.id"
                class="p-2 bg-white mb-2 rounded-b-lg"
                :class="content.show_type == 1 ? 'w-full' : 'w-[49%]'"
            >
                <el-image :src="item.image" class="h-[200px] w-full rounded-t-lg" fit="cover">
                    <template #error>
                        <div class="w-full h-full flex justify-center items-center bg-[#ebf2ff]">
                            <el-icon :size="28"><PictureFilled /></el-icon>
                        </div>
                    </template>
                </el-image>
                <div class="text_hidden px-2">
                    {{ item.name || '商品名称' }}
                </div>
                <div class="px-2 flex items-center">
                    <span class="text-[10px]">¥</span>
                    <span class="ml-[2px]">
                        {{ item.price || 0 }}
                    </span>
                    <span class="ml-4" style="color: #999; font-size: 10px">
                        已售{{ item.sale_num || 0 }}
                    </span>
                    <!-- <div class="add-btn ml-auto">+</div> -->
                </div>
            </div>
        </div>
    </div>
</template>
<script setup>
import DecorationImg from '../../decoration-img.vue'
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
.checked-box {
    background: linear-gradient(108deg, rgba(172, 150, 101, 0.6) 0%, rgba(0, 0, 0, 0) 100%), #7c4d30;
}
.bg-style {
    height: 250px;
    background-image: linear-gradient(to bottom, rgb(255, 255, 255), rgba(255, 0, 0, 0));
}
</style>
