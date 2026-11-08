<template>
    <el-form label-width="80px">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                店铺推荐
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">店铺推荐</div>
            <el-form-item label="标题">
                <el-input
                    v-model="content.title"
                    maxlength="6"
                    clearable
                    show-word-limit
                ></el-input>
            </el-form-item>
            <el-form-item label="商品数量">
                <el-slider v-model="content.showNum" max="18" show-input />
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">展示方式</div>
            <el-radio-group v-model="content.sortType" class="mt-4">
                <el-radio :label="1" size="medium" border>横向滑动</el-radio>
                <el-radio :label="2" size="medium" border>一行三个</el-radio>
            </el-radio-group>
        </el-card>
    </el-form>
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
    if (props.content.data.length >= 5) {
        return feedback.msgError('最多添加五张图片')
    }
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

<style lang="scss" scoped>
:deep(.el-card__body) {
    width: 100%;
}
</style>
