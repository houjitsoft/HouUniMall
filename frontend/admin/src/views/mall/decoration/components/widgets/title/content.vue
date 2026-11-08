<template>
    <div class="p-2">
        <div class="flex bg-white p-2 rounded-lg items-baseline" v-if="content.style == 1">
            <div :style="{ 'font-size': content.title_size + 'px', color: content.title_color }">
                {{ content.title }}
            </div>
            <div
                :style="{
                    'font-size': content.subtitle_size + 'px',
                    color: content.subtitle_color
                }"
                class="ml-1"
            >
                {{ content.subtitle }}
            </div>
            <div
                v-if="content.right"
                class="ml-auto text-[12px]"
                :style="{
                    color: content.right_color
                }"
            >
                {{ content.right_title }}
            </div>
        </div>
        <div class="bg-white p-2 rounded-lg title-style" v-if="content.style == 2">
            <div
                class="title-content"
                :style="{
                    color: content.title_color,
                    'font-size': `${content.title_size}px`
                }"
            >
                {{ content.title }}
            </div>
            <div
                class="title-subtitle"
                :style="{
                    color: content.subtitle_color,
                    'font-size': `${content.subtitle_size}px`
                }"
            >
                {{ content.subtitle }}
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
.title-style {
    text-align: center;
    .title-content {
        display: inline-flex;
        align-items: center;
        &::before,
        &::after {
            content: '';
            display: inline-block;
            width: 49px;
            height: 1px;
            margin: 0 10px;
            background: #dcdfe6;
        }
    }
}
</style>
