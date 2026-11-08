<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                页面设置
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">背景设置</div>
            <el-form-item label="页面设置">
                <el-radio-group v-model="contentData.title_type">
                    <el-radio :label="1">默认</el-radio>
                    <el-radio :label="2">自定义背景</el-radio>
                </el-radio-group>
            </el-form-item>

            <el-form-item label="背景图片" v-if="contentData.title_type == 2">
                <div>
                    <material-picker v-model="contentData.bg_img" :limit="1" size="100px" />
                    <div class="form-tips">建议图片尺寸：750px*高度不限</div>
                </div>
            </el-form-item>
        </el-card>
    </el-form>
</template>
<script lang="ts" setup>
import type { PropType } from 'vue'

import type options from './options'

type OptionsType = ReturnType<typeof options>
const emits = defineEmits<(event: 'update:content', data: OptionsType['content']) => void>()
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

const contentData = computed({
    get: () => props.content,
    set: (newValue) => {
        emits('update:content', newValue)
    }
})
</script>

<style lang="scss" scoped>
:deep(.el-card__body) {
    width: 100%;
}
</style>
