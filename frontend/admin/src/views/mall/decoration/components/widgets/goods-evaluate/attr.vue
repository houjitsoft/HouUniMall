<template>
    <el-form label-width="70px">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                商品参数
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">商品评价</div>

            <el-form-item label="标题">
                <el-input
                    v-model="content.title"
                    maxlength="6"
                    clearable
                    show-word-limit
                ></el-input>
            </el-form-item>
            <el-form-item label="评价数量">
                <el-slider v-model="content.show_conmment" max="10" show-input />
            </el-form-item>
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
</script>

<style lang="scss" scoped>
:deep(.el-card__body) {
    width: 100%;
}
</style>
