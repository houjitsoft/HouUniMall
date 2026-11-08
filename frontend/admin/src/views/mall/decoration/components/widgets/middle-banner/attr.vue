<template>
    <div>
        <el-form label-width="70px">
            <el-form-item label="是否启用">
                <el-radio-group v-model="content.enabled">
                    <el-radio :label="1">开启</el-radio>
                    <el-radio :label="0">停用</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="图片设置">
                <div class="flex-1">
                    <div class="form-tips">建议图片尺寸：750px*200px</div>
                    <del-wrap
                        v-for="(item, index) in content.data"
                        :key="index"
                        @close="handleDelete(index)"
                        class="max-w-[400px]"
                    >
                        <div class="flex items-center w-full p-4 mt-4 bg-fill-light">
                            <material-picker
                                v-model="item.image"
                                upload-class="bg-body"
                                exclude-domain
                            />
                            <div class="flex-1 ml-3">
                                <el-form-item label="图片名称">
                                    <el-input v-model="item.name" placeholder="请输入名称" />
                                </el-form-item>
                                <el-form-item class="mt-[18px]" label="图片链接">
                                    <link-picker v-model="item.link" />
                                </el-form-item>
                            </div>
                        </div>
                    </del-wrap>
                </div>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="handleAdd">添加图片</el-button>
            </el-form-item>
        </el-form>
    </div>
</template>
<script setup>
import feedback from '@/utils/feedback'
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
    props.content.data.push({
        image: '',
        name: '',
        link: {}
    })
}
const handleDelete = (index) => {
    if (props.content.data?.length <= 1) {
        return feedback.msgError('最少保留一张图片')
    }
    props.content.data.splice(index, 1)
}
</script>

<style lang="scss" scoped></style>
