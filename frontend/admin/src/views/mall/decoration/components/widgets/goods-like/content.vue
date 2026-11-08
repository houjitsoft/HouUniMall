<template>
    <div class="goods-like h-[258px]">
        <img src="./images/goods_like.png" class="absolute top-0 left-0 z-10 w-full" alt="排名">
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

</style>