<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                轮播图
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">展示样式</div>
            <el-form-item label="轮播风格">
                <el-radio-group v-model="content.image_type">
                    <el-radio :label="1">常规</el-radio>
                    <el-radio :label="2">大屏</el-radio>
                    <div class="form-tips" v-if="content.image_type == 2">
                        开启大屏样式后，此时该页面设置的头部背景/滚动后头部背景将失效
                    </div>
                </el-radio-group>
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">
                <span>上传图片</span>
                <span class="form-tips ml-4">
                    最多添加10张，建议尺寸：750px*高度不限，但需高度统一</span
                >
            </div>

            <div class="flex-1 w-full">
                <del-wrap
                    v-for="(item, index) in content.data"
                    :key="index"
                    @close="handleDelete(index)"
                    class="w-full"
                >
                    <div class="flex items-center w-full p-4 mt-4 bg-fill-light">
                        <material-picker
                            v-model="item.image"
                            upload-class="bg-body"
                            exclude-domain
                        />
                        <div class="flex-1 ml-3">
                            <el-form-item label="图片名称" class="mt-[18px]">
                                <el-input v-model="item.name" placeholder="请输入名称" />
                            </el-form-item>
                            <el-form-item class="mt-[18px]" label="图片链接">
                                <link-picker v-model="item.link" />
                            </el-form-item>
                        </div>
                    </div>
                </del-wrap>
            </div>
            <el-button type="primary" @click="handleAdd" class="w-full mt-4">添加图片</el-button>
        </el-card>
    </el-form>
</template>
<script lang="ts" setup>
import feedback from '@/utils/feedback'
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
    props.content.data.push({
        image: '',
        name: '',
        link: {}
    })
}
const handleDelete = (index: number) => {
    if (props.content.data?.length <= 1) {
        return feedback.msgError('最少保留一张图片')
    }
    props.content.data.splice(index, 1)
}
</script>

<style lang="scss" scoped>
:deep(.el-card__body) {
    width: 100%;
}
</style>
