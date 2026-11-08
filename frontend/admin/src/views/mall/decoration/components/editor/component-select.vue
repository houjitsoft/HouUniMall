<template>
    <el-card class="!border-none flex" shadow="never">
        <div class="flex">
            <el-page-header @back="$router.back()" title=" " />
            <el-input
                v-model="pagetitle"
                placeholder="请输入页面名称"
                show-word-limit
                maxlength="20"
                @change="handlechange"
            ></el-input>
        </div>
    </el-card>
    <el-collapse
        v-model="activeNames"
        class="w-[290px] ml-2"
        v-for="item in decorateMenu"
        :key="item.name"
    >
        <el-collapse-item :title="item.title" :name="item.name">
            <div class="flex flex-wrap">
                <div v-for="citem in item.children" :key="citem.name">
                    <div
                        class="card flex flex-col justify-around items-center"
                        draggable="true"
                        @dragstart="dragStart(citem)"
                        @dragend="dragend"
                    >
                        <img :draggable="false" class="item-icon" :src="citem.icon" mode="" />
                        <div>
                            {{ citem.title }}
                        </div>
                    </div>
                </div>
            </div>
        </el-collapse-item>
    </el-collapse>
</template>
<script setup>
// ⚠️ vue API 必须显式导入：ref/computed/watch 未导入时构建期不报错，
// 运行时会抛 "ref is not defined" 导致页面编辑器整块白屏。
import { ref, watch } from 'vue'
import { decorateMenu } from '@/utils/decoration'

const props = defineProps({
    title: {
        type: String,
        default: ''
    }
})
const pagetitle = ref('')
watch(
    () => props.title,
    (value) => {
        pagetitle.value = value
    },
    {
        immediate: true
    }
)
const emit = defineEmits(['addwidgetName', 'update:title', 'resetdragIndex'])
const activeNames = ref(['basics', 'marketing', 'tools'])

/**
 * 拖拽开始事件处理
 * @param {Object} item - 被拖拽的组件项，包含name属性
 */
const dragStart = ({ name }) => {
    emit('addwidgetName', name)
}

/**
 * 拖拽结束事件处理
 */
const dragend = () => {
    emit('resetdragIndex', -2)
}

/**
 * 页面标题变更事件处理
 * @param {string} e - 新的标题值
 */
const handlechange = (e) => {
    emit('update:title', e)
}
</script>
<style lang="scss" scoped>
:deep(.el-card__body) {
    padding: 20px 10px;
}
.card {
    background-color: skyblue;
    width: 72px;
    height: 72px;
    margin-right: 12px;
    margin-top: 16px;
    cursor: move;
    border-radius: 5px;
    overflow: hidden;
    background-color: #fff;
    box-shadow: 0px 0px 6px rgba(0, 0, 0, 0.06);
    box-sizing: border-box;
    .item-icon {
        width: 26px;
        height: 26px;
    }
    &:hover {
        z-index: 99;
        border: 1px solid #4073fa;
    }
    /* 模板里每张卡片外面包了一层 div，:nth-of-type 永远匹配不到第 3n 张；
     * 要对 wrapper 取 3n 再命中内部 .card，否则每行只排得下 2 张。 */
    .flex-wrap > div:nth-child(3n) & {
        margin-right: 0;
    }
}

/* ⚠️ 工具类兜底（UnoCSS 已全局生效，此处同值兜底）。
 * 注意面板宽度不能写死 290px：首页装修 PageEditor 的 .editor-left 只有 280px，
 * 290+8(ml-2)=298 会溢出撑出横向滚动条。改为填满父容器自适应。 */
.\!border-none {
    border: none;
}
.w-\[290px\] {
    width: calc(100% - 8px);
    max-width: calc(100% - 8px);
}
.ml-2 {
    margin-left: 8px;
}
.flex {
    display: flex;
}
.flex-wrap {
    flex-wrap: wrap;
}
.flex-col {
    flex-direction: column;
}
.justify-around {
    justify-content: space-around;
}
.items-center {
    align-items: center;
}
</style>
