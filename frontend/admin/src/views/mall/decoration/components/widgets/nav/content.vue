<template>
    <div class="nav bg-white pt-[15px] pb-[8px]">
        <div class="flex flex-wrap">
            <div
                v-for="(item, index) in content.data"
                :key="index"
                class="flex flex-col items-center mb-[15px]"
                :class="{
                    'w-1/3': content.show_nums == 1,
                    'w-1/4': content.show_nums == 2,
                    'w-1/5': content.show_nums == 3
                }"
                v-show="showDiv(index + 1)"
            >
                <decoration-img
                    width="41px"
                    height="41px"
                    :src="item.image"
                    alt=""
                    v-if="content.type == 2 || content.type == 1"
                />
                <div class="mt-[7px]" v-if="content.type == 3 || content.type == 1">
                    {{ item.name }}
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
const showDiv = (index) => {
    const numArr = [3, 4, 5]
    return index > numArr[props.content.show_nums - 1] * props.content.show_rows ? false : true
}
</script>

<style lang="scss" scoped></style>
