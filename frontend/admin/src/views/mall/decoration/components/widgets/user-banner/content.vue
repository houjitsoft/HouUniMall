<template>
    <div class="banner mx-[10px] mt-[10px]">
        <div class="banner-image">
            <decoration-img width="100%" height="100px" :src="getImage" fit="contain" />
        </div>
    </div>
</template>
<script setup>// ⚠️ vue API 必须显式导入：缺失时构建期不报错，运行时报 'xxx is not defined' 导致页面白屏。
import { computed } from 'vue'

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
const getImage = computed(() => {
    const { data } = props.content
    if (Array.isArray(data)) {
        return data[0] ? data[0].image : ''
    }
    return ''
})
</script>

<style lang="scss" scoped></style>
