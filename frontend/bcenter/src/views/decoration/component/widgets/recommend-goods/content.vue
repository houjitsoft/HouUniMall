<template>
    <!-- 商品推荐 -->
    <div class="store-recommend-header p-[10px] flex">
        <div class="w-[3px] h-[20px] bg-[#f7222a] mr-[10px]" />
        <div class="text-lg font-medium">店铺推荐</div>
    </div>
    <div class="flex pb-4 overflow-x-auto store-recommend-content">
        <div 
            class="w-[105px] ml-[10px]"
            v-for="item in content.data.list" :key="item.id"
        >
            <image-contain
                width="105px"
                height="105px"
                :src="item.image"
            />
            <div class="truncate m-[4px]">{{ item.name }}</div>
            <div class="text-[#f7222a] text-[14px] mx-[4px]">
                <span class="text-[12px]">¥</span>
                <span>{{ item.price }}</span>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import type { PropType } from "vue";
import type options from "./options";
import DecorationImg from "../../decoration-img.vue";
type OptionsType = ReturnType<typeof options>;
const props = defineProps({
    content: {
        type: Object as PropType<OptionsType["content"]>,
        default: () => ({}),
    },
    styles: {
        type: Object as PropType<OptionsType["styles"]>,
        default: () => ({}),
    },
});
</script>

<style lang="scss" scoped></style>
