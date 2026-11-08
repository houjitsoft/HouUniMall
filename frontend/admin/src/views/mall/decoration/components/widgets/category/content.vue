<template>
    <div class="category h-[640px]">
        <img :src="getImagesUrl" class="absolute top-0 left-0 z-10 w-full" alt="排名">
    </div>
</template>

<script setup>
// ⚠️ vue API 必须显式导入：缺失时构建期不报错，运行时报 'xxx is not defined' 导致页面白屏。
import { computed } from 'vue'
import No1 from "./images/No.1.png"
import No2 from "./images/No.2.png"
import No3 from "./images/No.3.png"
import No4 from "./images/No.4.png"
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

const imageArr = [No1, No2, No3, No4]

const getImagesUrl = computed(() => imageArr[props.content.type - 1])

</script>

<style lang="scss" scoped>

</style>