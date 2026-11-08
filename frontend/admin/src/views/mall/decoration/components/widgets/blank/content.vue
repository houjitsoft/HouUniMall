<template>
    <div class="bg-white" :style="{ height: content.height + 'px' }" style="opacity: 0"></div>
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

<style lang="scss" scoped></style>
