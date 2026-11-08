<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                我的订单
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <el-form-item label="标题名称">
                <el-input class="w-[400px]" v-model="content.title" />
            </el-form-item>
            <el-form-item label="菜单设置">
                <div class="flex-1">
                    <AddSub :max="5" :min="5" v-model="content.data" />
                </div>
            </el-form-item>
        </el-card>
    </el-form>
</template>
<script setup>
import AddSub from '../../add-sub.vue'
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
