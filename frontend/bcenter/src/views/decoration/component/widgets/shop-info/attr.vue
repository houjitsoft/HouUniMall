<template>
    <div class="bg-white p-3">
        <el-form label-width="70px">
            <el-form-item label="店铺背景">
                <div>
                    <el-radio-group v-model="content.style">
                        <el-radio label="1">系统默认图片</el-radio>
                        <el-radio label="2">自定义</el-radio>
                    </el-radio-group>
                    <div class="flex-1" v-if="content.style == 2">
                        <material-picker
                            v-model="content.background"
                            upload-class="bg-body"
                            exclude-domain
                        />
                        <div class="form-tips">最多添加5张，建议图片尺寸：750px*360px</div>
                    </div>
                </div>
            </el-form-item>
        </el-form>
    </div>
</template>
<script lang="ts" setup>
import feedback from '@/utils/feedback'
import type { PropType } from 'vue'
import type options from './options'
import Draggable from 'vuedraggable'
const limit = 5
type OptionsType = ReturnType<typeof options>
const props = defineProps({
    content: {
        type: Object as PropType<OptionsType['content']>,
        default: () => ({})
    },
    styles: {
        type: Object as PropType<OptionsType['styles']>,
        default: () => ({})
    },
    type: {
        type: String as PropType<'mobile' | 'pc'>,
        default: 'mobile'
    }
})

const handleAdd = () => {
    if (props.content.data?.length < limit) {
        props.content.data.push({
            image: '',
            name: '',
            link: {}
        })
    } else {
        feedback.msgError(`最多添加${limit}张图片`)
    }
}
const handleDelete = (index: number) => {
    if (props.content.data?.length <= 1) {
        return feedback.msgError('最少保留一张图片')
    }
    props.content.data.splice(index, 1)
}
</script>

<style lang="scss" scoped></style>
