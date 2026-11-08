<template>
    <el-scrollbar class="pages-preview-container">
        <div v-if="pageMeta !== null" class="absolute right-4 top-4" @click="handleClickPageMeta">
            <el-button>页面设置</el-button>
        </div>
        <div
            class="phone-header mx-[30px] pt-[20px] flex items-center"
            :style="{ 'background-color': pageMeta[0].content.title_type == 1 ? '#ffecc7' : '' }"
            @click="handleClickPageMeta"
        >
            <el-icon :size="20" class="ml-1"><ArrowLeft /></el-icon>
            <div class="search w-[223px] h-[32px] ml-[10px]">
                <div class="search-con flex items-center px-[15px]">
                    <icon name="el-icon-Search" :size="17" />
                    <span class="ml-[5px]">请输入关键词搜索</span>
                </div>
            </div>
        </div>

        <div
            class="shadow mx-[30px] pages-preview"
            :style="{
                background:
                    pageMeta[0].content.title_type == 1
                        ? ''
                        : `url(${pageMeta[0].content.bg_img}) center top / 100% no-repeat `
            }"
            @dragover.prevent="dragover"
            @drop="drop"
            :data-index="-1"
        >
            <div class="p-2" :class="{ 'bg-top': pageMeta[0].content.title_type == 1 }">
                <div class="bg-white h-[64px] rounded-lg flex items-center justify-between p-2">
                    <div class="flex">
                        <img src="./images/shopimg.png" alt="" />
                        <div class="ml-2 flex flex-col justify-around">
                            <div>顽刻olieskate</div>
                            <div class="flex">
                                <img src="./images/image2.png" alt="" />
                                <img src="./images/image1.png" alt="" class="ml-1" />
                            </div>
                        </div>
                    </div>
                    <div class="btn">关注</div>
                </div>
            </div>
            <draggable :list="pageData" animation="300" :force-fallback="true" item-key="id">
                <template #item="{ element: widget, index }">
                    <div>
                        <div
                            class="mouse-cover flex justify-center items-center"
                            v-if="showMouseCover(index, 'top')"
                        >
                            <span>释放鼠标将组建添加到此处</span>
                        </div>
                        <div
                            class="relative"
                            style="cursor: move"
                            @click="handleClick(widget, index)"
                        >
                            <!--  选中的边框  -->
                            <div
                                :data-index="index"
                                class="absolute w-full h-full z-[100] border-dashed"
                                :class="{
                                    select: index == modelValue,
                                    hide: canShowCom(widget.content),
                                    'border-[#dcdfe6] border-2': !widget?.disabled
                                }"
                            ></div>

                            <!--  选中的组件  -->
                            <slot>
                                <component
                                    :is="widgets[widget?.name]?.content"
                                    :content="widget.content"
                                    :styles="widget.styles"
                                    :key="widget.id"
                                    :data-index="index"
                                />
                            </slot>

                            <!--  部件操作按钮组  -->

                            <div class="widget-btns py-[5px]" v-if="index == modelValue">
                                <div>
                                    <el-tooltip
                                        effect="dark"
                                        :content="canShowCom(widget.content) ? '显示' : '隐藏'"
                                        placement="right"
                                    >
                                        <el-button
                                            class="py-[5px]"
                                            type="primary"
                                            :icon="canShowCom(widget.content) ? View : Hide"
                                            @click="changeShowCom(widget.content)"
                                        />
                                    </el-tooltip>
                                </div>
                                <div>
                                    <el-tooltip effect="dark" content="删除" placement="right">
                                        <el-button
                                            class="py-[5px]"
                                            type="primary"
                                            :icon="Delete"
                                            @click.stop="handleDel(index)"
                                        />
                                    </el-tooltip>
                                </div>
                                <div>
                                    <el-tooltip effect="dark" content="复制" placement="right">
                                        <el-button
                                            class="py-[5px]"
                                            type="primary"
                                            :icon="DocumentAdd"
                                            @click.stop="handleCopy(index, widget)"
                                        />
                                    </el-tooltip>
                                </div>
                                <div>
                                    <el-tooltip effect="dark" content="上移" placement="right">
                                        <el-button
                                            class="py-[5px]"
                                            type="primary"
                                            :icon="ArrowUpBold"
                                            :disabled="canMoveUpCom(index)"
                                            @click.stop="rearrangeArray(index, index - 1)"
                                        />
                                    </el-tooltip>
                                </div>
                                <div>
                                    <el-tooltip effect="dark" content="下移" placement="right">
                                        <el-button
                                            class="py-[5px]"
                                            type="primary"
                                            :icon="ArrowDownBold"
                                            :disabled="canMoveDownCom(index)"
                                            @click.stop="rearrangeArray(index, index + 1)"
                                        />
                                    </el-tooltip>
                                </div>
                            </div>
                        </div>
                        <div
                            class="mouse-cover flex justify-center items-center"
                            v-if="showMouseCover(index, 'bottom')"
                        >
                            <span>释放鼠标将组建添加到此处</span>
                        </div>
                    </div>
                </template>
            </draggable>
            <div
                class="mouse-cover flex justify-center items-center"
                v-if="showMouseCover(-1, 'bottom')"
            >
                <span>释放鼠标将组建添加到此处</span>
            </div>
        </div>
    </el-scrollbar>
</template>
<script lang="ts" setup>
import {
    ArrowDownBold,
    ArrowUpBold,
    Hide,
    View,
    DocumentAdd,
    Delete
} from '@element-plus/icons-vue'
import Draggable from 'vuedraggable'

import { cloneDeep } from 'lodash-es'
import type { PropType } from 'vue'
import { computed } from 'vue'

import widgets from '../widgets'
import { color } from 'echarts/core'

const props = defineProps({
    pageMeta: {
        type: Object as any,
        default: () => null
    },
    pageData: {
        type: Array as PropType<any[]>,
        default: () => []
    },
    modelValue: {
        type: Number,
        default: 0
    }
})
const showMouseCover = computed(() => {
    return (index: number, position: string) => {
        if (dragIndex.value === index && position == dragPosition.value) {
            return true
        }
        return false
    }
})
const dragIndex = ref(-2)
const dragPosition = ref('bottom')

const emit = defineEmits<{
    (event: 'update:modelValue', value: number): void
    (event: 'updatePageData', value: any[]): void
    (event: 'addwidget', value: any, position: any): void
    (event: 'delwidget', value: any): void
    (event: 'copywidget', index: any, component: any): void
}>()
const handleDel = (index: number) => {
    emit('delwidget', index)
}
const handleCopy = (index: number, component: any) => {
    emit('copywidget', index, component.name)
}
const resetdragIndex = () => {
    dragIndex.value = -2
}
defineExpose({ resetdragIndex })

const dragover = ($event: any) => {
    const index = parseInt($event.target.dataset.index)
    if (String(index) === 'NaN') {
        return
    }
    dragIndex.value = index

    //获取Y轴移动值
    const y = parseFloat($event.offsetY)
    const h = parseFloat(String($event.target.offsetHeight / 2))

    if (y <= h) {
        dragPosition.value = 'top'
    } else {
        dragPosition.value = 'bottom'
    }
    if (dragIndex.value == -1) {
        dragPosition.value = 'bottom'
    }
}
const drop = () => {
    emit('addwidget', dragIndex.value, dragPosition.value)
    dragIndex.value = -2
}
const oldModelValue = ref<number>(-1)

const handleClickPageMeta = () => {
    if (props.modelValue === -1) {
        emit('update:modelValue', oldModelValue.value)
    } else {
        oldModelValue.value = props.modelValue
        emit('update:modelValue', -1)
    }
}

const handleClick = (widget: any, index: number) => {
    if (widget.disabled) return
    emit('update:modelValue', index)
}

// 是否可以移动组件
const canMoveUpCom = computed(() => {
    return (index: number) => {
        return index === 0
    }
})

// 是否可以移动组件
const canMoveDownCom = computed(() => {
    return (index: number) => {
        return props.pageData?.length === index + 1
    }
})

// 是否显示组件
const canShowCom = computed(() => {
    return (data: any) => {
        return data?.enabled == 0
    }
})

// 修改组件显示/隐藏
const changeShowCom = (data: any) => {
    if (data.enabled === undefined) return
    data.enabled = data.enabled ? 0 : 1
}

const rearrangeArray = (currentIdx: number, targetIdx: number) => {
    if (
        currentIdx < 0 ||
        currentIdx >= props.pageData.length ||
        targetIdx < 0 ||
        targetIdx >= props.pageData.length
    ) {
        return
    }

    const newPageData = cloneDeep(props.pageData)
    const element = newPageData.splice(currentIdx, 1)[0]
    newPageData.splice(targetIdx, 0, element)
    emit('updatePageData', newPageData)
    emit('update:modelValue', targetIdx)
}
</script>

<style lang="scss" scoped>
.pages-preview-container {
    position: relative;
    :deep(.el-scrollbar__wrap) {
        width: 100%;
        display: flex;
        justify-content: center;
    }

    .pages-preview {
        background-color: #f8f8f8;
        width: 375px;
        min-height: 615px;
        color: #333;

        .select {
            @apply border-primary border-solid;
        }

        .hide::before {
            content: '已隐藏';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            color: white;
            font-size: 14px;
            background-color: rgba(0, 0, 0, 0.4);
        }

        .widget-btns {
            position: absolute;
            top: 10px;
            right: -60px;
            overflow: hidden;

            width: 46px;
            border-radius: 8px;
            @apply bg-primary;

            :deep(.el-button) {
                width: 46px;
                border-radius: 0;
            }
        }
    }
}
.mouse-cover {
    height: 40px;
    margin: 2px 0;
    border: 1px dashed var(--el-color-primary);
    background: rgba(var(--el-color-primary), 0.05);
    font-size: 14px;
    color: var(--el-color-primary);
    text-align: center;
}
.phone-header {
    overflow: hidden;
    cursor: pointer;
    background: url('@/assets/images/phone_header.png') no-repeat;
    background-size: 100%;
    width: 375px;
    height: 64px;
    .phone-title {
        margin-top: 20px;
        text-align: center;
        height: 44px;
        line-height: 44px;
    }
}
.bg-top {
    background-image: linear-gradient(to bottom, #ffecc7, #f8f8f8); /* 渐变背景 */
}
.search {
    .search-con {
        width: 223px;
        height: 32px;
        border-radius: 36px;
        background: #ffffff;
        color: #999999;
    }
}
.btn {
    width: 70px;
    height: 30px;
    border-radius: 20px;
    line-height: 30px;
    background: rgba(182, 123, 46, 0.1);
    text-align: center;
    color: #b67b2e;
}
</style>
