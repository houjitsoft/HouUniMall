<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                搜索框
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">文字位置</div>
            <el-form-item label="文字位置">
                <el-radio-group v-model="content.text_type">
                    <el-radio :label="1" border>居左</el-radio>
                    <el-radio :label="2" border>居中</el-radio>
                </el-radio-group>
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">提示文字</div>
            <el-form-item label="提示文字">
                <el-input
                    v-model="content.text"
                    placeholder="请输入提示文字"
                    show-word-limit
                    maxlength="12"
                ></el-input>
            </el-form-item>
        </el-card>
    </el-form>
</template>
<script setup>
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
