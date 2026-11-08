<template>
    <div class="attribute-tabs flex flex-col h-full w-full">
        <div class="flex justify-between attribute-header bg-body rounded-t-lg">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                {{ title }}
            </div>
            <div class="tabs-header flex" v-if="!$slots.default">
                <div
                    class="tabs-header__item"
                    :class="{ active: active == 'content' }"
                    @click="active = 'content'"
                >
                    <span>内容</span>
                </div>
                <div
                    class="tabs-header__item"
                    :class="{ active: active == 'styles' }"
                    @click="active = 'styles'"
                >
                    <span>样式</span>
                </div>
            </div>
        </div>
        <div class="attribute-content flex flex-col h-full w-full" v-if="$slots.default">
            <slot></slot>
        </div>
        <div class="tabs flex flex-col h-full w-full" v-else>
            <div class="flex flex-col h-full w-full" v-show="active == 'content'">
                <slot name="content"></slot>
            </div>
            <div class="flex flex-col h-full w-full" v-show="active == 'styles'">
                <slot name="styles"></slot>
            </div>
        </div>
    </div>
</template>

<script setup>
// ⚠️ vue API 必须显式导入：缺失时构建期不报错，运行时报 'xxx is not defined' 导致页面白屏。
import { ref } from 'vue'
defineProps({
    title: {
        type: String,
        default: ''
    },
    desc: {
        type: String,
        default: ''
    }
})

const active = ref('content')
</script>

<style lang="scss" scoped>
.attribute-tabs {
    .attribute-header {
        padding: 14px 20px;
        border-bottom: 1px solid var(--el-bg-color-page);

        .tabs-header {
            height: 28px;
            line-height: 28px;
            background: #f5f5f5;
            border-radius: 14px;
            &__item {
                flex: 1;
                text-align: center;
                cursor: pointer;
                padding: 0 28px;
                &.active {
                    border-radius: 14px;
                    @apply bg-primary text-white;
                }
            }
        }
    }
}
</style>
