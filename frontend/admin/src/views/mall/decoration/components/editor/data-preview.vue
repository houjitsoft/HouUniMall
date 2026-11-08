<template>
    <el-scrollbar class="pages-preview-container">
        <div v-if="pageMeta !== null" class="absolute right-4 top-4">
            <div>
                <el-button @click="handleClicktitle(1)" :class="{ 'active-btn': title == '秒杀' }"
                    >秒杀</el-button
                >
            </div>
            <div class="mt-2">
                <el-button @click="handleClicktitle(2)" :class="{ 'active-btn': title == '拼团' }">
                    拼团</el-button
                >
            </div>
        </div>
        <div class="phone-header" @click="handleClickPageMeta">
            <div
                class="phone-title"
                :style="{
                    color: metaContent.title_color == 1 ? 'black' : 'white',
                    background:
                        metaContent.titlebg_type == 1
                            ? `${metaContent.titlebg_color}`
                            : `url(${metaContent.titlebg_img}) center top / 100% no-repeat`
                }"
            >
                <span> {{ metaContent.title }} </span>
            </div>
        </div>

        <div
            class="shadow pages-preview"
            :style="{
                background:
                    metaContent.title_type == 1
                        ? ''
                        : `url(${metaContent.bg_img}) center top / 100% no-repeat ${metaContent.bg_color}`
            }"
            @dragover.prevent="dragover"
            @drop="drop"
            :data-index="-1"
        >
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
<script setup>// ⚠️ vue API 必须显式导入：缺失时构建期不报错，运行时报 'xxx is not defined' 导致页面白屏。

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
import { computed, ref } from 'vue'

import widgets from '../widgets'

const EMPTY_META = {
    title: '秒杀',
    title_color: 1,
    titlebg_type: 1,
    titlebg_color: '#FFFFFF',
    titlebg_img: '',
    title_type: 1,
    bg_img: '',
    bg_color: '#F8F8F8'
}

const props = defineProps({
    pageMeta: {
        type: [Array, Object],
        default: () => null
    },
    pageData: {
        type: Array,
        default: () => []
    },
    modelValue: {
        type: Number,
        default: 0
    }
})

const metaContent = computed(() => {
    const raw = props.pageMeta
    const meta = Array.isArray(raw) ? raw[0] : raw
    return { ...EMPTY_META, ...(meta?.content || {}) }
})

/**
 * 判断是否显示鼠标拖拽覆盖层
 * 根据拖拽索引和位置判断是否显示拖拽提示
 */
const showMouseCover = computed(() => {
    return (index, position) => {
        if (dragIndex.value === index && position == dragPosition.value) {
            return true
        }
        return false
    }
})
const dragIndex = ref(-2)
const dragPosition = ref('bottom')

const emit = defineEmits(['update:modelValue', 'updatePageData', 'addwidget', 'delwidget', 'copywidget', 'updataTitle'])

/**
 * 删除组件事件处理
 * @param {number} index - 要删除的组件索引
 */
const handleDel = (index) => {
    emit('delwidget', index)
}

/**
 * 复制组件事件处理
 * @param {number} index - 组件索引
 * @param {Object} component - 组件数据对象
 */
const handleCopy = (index, component) => {
    emit('copywidget', index, component.name)
}

/**
 * 重置拖拽索引
 */
const resetdragIndex = () => {
    dragIndex.value = -2
}
defineExpose({ resetdragIndex })

/**
 * 拖拽悬停事件处理
 * 计算拖拽位置（上方/下方）并更新拖拽索引
 * @param {DragEvent} $event - 拖拽事件对象
 */
const dragover = ($event) => {
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

/**
 * 拖拽放下事件处理
 * 触发添加组件事件并重置拖拽状态
 */
const drop = () => {
    emit('addwidget', dragIndex.value, dragPosition.value)
    dragIndex.value = -2
}
const title = ref('秒杀')
const oldModelValue = ref(-1)

/**
 * 点击秒杀/拼团标题切换
 * @param {number} value - 1为秒杀，2为拼团
 */
const handleClicktitle = (value) => {
    value == 1 ? (title.value = '秒杀') : (title.value = '拼团')
    emit('updataTitle', value)
}

/**
 * 点击页面元数据区域，切换页面设置选中状态
 */
const handleClickPageMeta = () => {
    if (props.modelValue === -1) {
        emit('update:modelValue', oldModelValue.value)
    } else {
        oldModelValue.value = props.modelValue
        emit('update:modelValue', -1)
    }
}

/**
 * 点击组件选中事件
 * @param {Object} widget - 被点击的组件数据
 * @param {number} index - 组件索引
 */
const handleClick = (widget, index) => {
    if (widget.disabled) return
    emit('update:modelValue', index)
}

/**
 * 判断组件是否可以上移
 * @param {number} index - 组件索引
 * @returns {boolean} 是否可以上移（索引为0时不可上移）
 */
const canMoveUpCom = computed(() => {
    return (index) => {
        return index === 0
    }
})

/**
 * 判断组件是否可以下移
 * @param {number} index - 组件索引
 * @returns {boolean} 是否可以下移（已在最后时不可下移）
 */
const canMoveDownCom = computed(() => {
    return (index) => {
        return props.pageData?.length === index + 1
    }
})

/**
 * 判断组件是否隐藏
 * @param {Object} data - 组件内容数据
 * @returns {boolean} 组件是否隐藏
 */
const canShowCom = computed(() => {
    return (data) => {
        return data?.enabled == 0
    }
})

/**
 * 切换组件显示/隐藏状态
 * @param {Object} data - 组件内容数据，包含enabled属性
 */
const changeShowCom = (data) => {
    if (data.enabled === undefined) return
    data.enabled = data.enabled ? 0 : 1
}

/**
 * 重新排列组件顺序
 * 将当前索引的组件移动到目标索引位置
 * @param {number} currentIdx - 当前组件索引
 * @param {number} targetIdx - 目标索引
 */
const rearrangeArray = (currentIdx, targetIdx) => {
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
    /* 视窗容器允许被中栏宽度收窄，手机预览随之等比缩小，窄屏也不再被裁切 */
    :deep(.el-scrollbar__view) {
        max-width: 100%;
    }

    .pages-preview {
        background-color: #f8f8f8;
        width: 375px;
        max-width: 100%;
        margin-left: auto;
        margin-right: auto;
        min-height: 615px;
        color: #333;

        .select {
            border-color: var(--el-color-primary);
            border-style: solid;
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
            background: var(--el-color-primary);

            :deep(.el-button) {
                width: 46px;
                border-radius: 0;
            }
        }
    }
}
.mouse-cover {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 40px;
    margin: 2px 0;
    border: 1px dashed var(--el-color-primary);
    background: var(--el-color-primary-light-9, rgba(64, 158, 255, 0.05));
    font-size: 14px;
    color: var(--el-color-primary);
    text-align: center;
}
.phone-header {
    overflow: hidden;
    cursor: pointer;
    background: #fff;
    border: 1px solid #e5e7eb;
    border-bottom: none;
    border-radius: 28px 28px 0 0;
    width: 375px;
    max-width: 100%;
    margin-left: auto;
    margin-right: auto;
    height: 64px;
    box-shadow: inset 0 12px 0 #111827;
    .phone-title {
        margin-top: 20px;
        text-align: center;
        height: 44px;
        line-height: 44px;
    }
}
.active-btn {
    background-color: #eeeffe;
    color: #4a5dff;
    border: 1px solid #c9ceff;
}

/* ⚠️ 本项目没有 Tailwind/UnoCSS：模板里的 Tailwind 风格工具类（absolute / right-4 /
 * mx-[30px] / w-full …）此前一直是空操作，导致「秒杀/拼团」切换按钮错位到左上、
 * 手机预览缺 30px 边距被裁切、组件选中边框与右侧操作按钮全部失效。
 * 这里在本组件作用域内补齐等效样式——只作用于本组件模板，不波及全局样式表。 */
.absolute {
    position: absolute;
}
.right-4 {
    right: 16px;
}
.top-4 {
    top: 16px;
}
.mt-2 {
    margin-top: 8px;
}
.relative {
    position: relative;
}
.w-full {
    width: 100%;
}
.h-full {
    height: 100%;
}
.z-\[100\] {
    z-index: 100;
}
.border-dashed {
    border-style: dashed;
}
.border-2 {
    border-width: 2px;
}
.border-\[\#dcdfe6\] {
    border-color: #dcdfe6;
}
.py-\[5px\] {
    padding-top: 5px;
    padding-bottom: 5px;
}

/* 中栏偏窄时（≈1450px 以下）手机右侧已不够 60px 放操作按钮，
 * 改成收进手机内部贴右边，避免按钮被 el-scrollbar 的 overflow:hidden 裁掉。 */
@media (max-width: 1450px) {
    .pages-preview-container .pages-preview .widget-btns {
        right: 4px;
    }
}
</style>
