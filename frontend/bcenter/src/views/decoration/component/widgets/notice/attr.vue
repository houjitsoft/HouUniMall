<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                商城公告
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">公告风格</div>
            <el-form-item label="标题类型">
                <div>
                    <el-radio-group v-model="content.title_type">
                        <el-radio :label="1">无</el-radio>
                        <el-radio :label="2">图片</el-radio>
                    </el-radio-group>
                    <material-picker
                        v-model="content.titile_img"
                        upload-class="bg-body"
                        exclude-domain
                        v-if="content.title_type == 2"
                    />
                    <div class="form-tips" v-if="content.title_type == 2">
                        建议图片尺寸：56px*20px
                    </div>
                </div>
            </el-form-item>
            <el-form-item label="标题文字" v-if="content.title_type == 1">
                <el-input
                    v-model="content.title"
                    placeholder="请输入页面标题"
                    show-word-limit
                    maxlength="4"
                ></el-input>
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">按钮设置</div>
            <el-form-item label="右侧按钮">
                <el-radio-group v-model="content.right_btn">
                    <el-radio :label="1">显示</el-radio>
                    <el-radio :label="0">隐藏</el-radio>
                </el-radio-group>
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">公告内容</div>
            <draggable :list="content.lists" animation="300" :force-fallback="true" item-key="id">
                <template #item="{ element: item, index }">
                    <div class="w-full bg-[#f7f7f7] mb-4 rounded-lg p-6 box" style="cursor: move">
                        <el-icon
                            class="del"
                            :size="24"
                            style="cursor: pointer"
                            @click="handleDel(index)"
                            ><CircleCloseFilled
                        /></el-icon>
                        <div class="flex items-center">
                            <div class="flex-none mr-2">标题</div>
                            <el-input
                                v-model="item.title"
                                placeholder="请输入页面标题"
                                show-word-limit
                                maxlength="20"
                            ></el-input>
                        </div>
                        <div class="flex items-center mt-4">
                            <div class="flex-none mr-2">链接</div>
                            <link-picker v-model="item.link" />
                        </div>
                    </div>
                </template>
            </draggable>
            <div class="">
                <el-button type="" @click="handleAdd" class="w-full mt-4">添加公告</el-button>
            </div>
        </el-card>
    </el-form>
</template>
<script lang="ts" setup>
import Draggable from 'vuedraggable'

import type { PropType } from 'vue'
import type options from './options'
type OptionsType = ReturnType<typeof options>
const props = defineProps({
    content: {
        type: Object as PropType<OptionsType['content']>,
        default: () => ({})
    },
    styles: {
        type: Object as PropType<OptionsType['styles']>,
        default: () => ({})
    }
})

const handleAdd = () => {
    props.content.lists.push({ title: '', link: '' })
}
const handleDel = (index: number) => {
    props.content.lists.splice(index, 1)
}
</script>

<style lang="scss" scoped>
:deep(.el-card__body) {
    width: 100%;
}
.box {
    position: relative;
    .del {
        position: absolute;
        top: -10px;
        right: -10px;
    }
}
</style>
