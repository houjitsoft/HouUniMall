<template>
    <div>
        <el-form label-width="70px">
            <el-form-item label="是否启用">
                <el-radio-group v-model="content.enabled">
                    <el-radio :label="1">开启</el-radio>
                    <el-radio :label="0">停用</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="背景设置">
                <div class="flex-1">
                    <div class="form-tips">热销榜单通用背景图，建议图片尺寸：750px*300px</div>
                    <del-wrap v-for="(item, index) in content.data" :key="index" @close="handleDelete(index)"
                        class="max-w-[400px]">
                        <div class="bg-fill-light w-full p-4 mt-4">
                            <el-form-item label="背景图片">
                                <material-picker v-model="item.image" upload-class="bg-body" exclude-domain />
                            </el-form-item>
                            <el-form-item label="图片名称" class="mt-4">
                                <el-input v-model="item.name" placeholder="请输入名称" />
                            </el-form-item>
                            <el-form-item class="mt-[18px]" label="图片链接">
                                <link-picker v-model="item.link" />
                            </el-form-item>
                        </div>
                    </del-wrap>
                </div>
            </el-form-item>
            <el-form-item v-if="content.data?.length < limit">
                <el-button type="primary" @click="handleAdd">添加图片</el-button>
            </el-form-item>
        </el-form>
    </div>
</template>
<script setup>
import feedback from '@/utils/feedback'
const limit = 1
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
const handleDelete = (index) => {
    if (props.content.data?.length <= 1) {
        return feedback.msgError('最少保留一张图片')
    }
    props.content.data.splice(index, 1)
}
</script>

<style lang="scss" scoped>

</style>
