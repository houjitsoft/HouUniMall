<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                我的服务
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <el-form-item label="排版样式">
                <el-radio-group v-model="content.style">
                    <el-radio :label="1">横排</el-radio>
                    <el-radio :label="2">竖排</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="标题名称">
                <el-input class="w-[400px]" v-model="content.title" />
            </el-form-item>
            <el-form-item label="菜单设置">
                <div class="flex-1">
                    <AddNav v-model="content.data" />
                </div>
            </el-form-item>
        </el-card>
    </el-form>
</template>
<script setup>
import AddNav from '../../add-nav.vue'
defineProps({
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

<style lang="scss" scoped></style>
