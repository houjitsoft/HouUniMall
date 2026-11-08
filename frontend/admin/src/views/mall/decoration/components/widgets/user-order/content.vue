<template>
    <div class="user-order">
        <div
            v-if="content.title"
            class="py-[10px] px-[15px] title flex items-center justify-between"
        >
            <div class="">{{ content.title }}</div>
            <div class="flex items-center">
                <span>查看全部</span>
                <el-icon>
                    <ArrowRight />
                </el-icon>
            </div>
        </div>

        <div class="flex pt-[20px] pb-[10px]">
            <div
                v-for="(item, index) in content.data"
                :key="index"
                :class="{ 'flex-1': item.display }"
            >
                <div class="flex flex-col items-center mb-[15px]">
                    <decoration-img
                        v-if="item.display"
                        width="28px"
                        height="28px"
                        :src="item.image"
                        alt=""
                    />
                    <div v-if="item.display" class="mt-[7px]">{{ item.name }}</div>
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
console.log('props:', props.content)
</script>

<style lang="scss" scoped>
.user-order {
    margin: 10px 10px 0;
    background-color: #fff;
    border-radius: 7px;

    .title {
        border-bottom: 1px solid #e5e5e5;

        div {
            &:nth-child(1) {
                font-size: 16px;
                font-weight: 500;
            }

            &:nth-child(2) {
                font-size: 12px;
                color: #888888;
            }
        }
    }
}
</style>
