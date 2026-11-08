<template>
    <div class="search">
        <div
            class="search-con flex items-center px-[15px]"
            :class="{ 'justify-center': content.text_type == 2 }"
        >
            <icon name="el-icon-Search" :size="17" />
            <span class="ml-[5px]">{{ content.text }}</span>
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
.search {
    background-color: #ffffff;
    padding: 7px 12px;

    .search-con {
        height: 100%;
        height: 36px;
        border-radius: 36px;
        background: #f4f4f4;
        color: #999999;
    }
}
</style>
