<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                商品展示
            </div>
        </el-card>

        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">分类样式</div>
            <el-form-item label="分类样式">
                <el-radio-group v-model="content.type">
                    <el-radio :label="1" border>风格1</el-radio>
                    <el-radio :label="2" border>风格2</el-radio>
                </el-radio-group>
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
.border-box {
    border: 1px solid rgb(233, 233, 238);
    // border-color: inherit;
    cursor: pointer;
}
.active {
    border: 1px solid var(--el-color-primary);
}
.add-btn {
    width: 25px;
    height: 25px;
    line-height: 23px;
    border-radius: 50%;
    background-color: #1a69fc;
    text-align: center;
    color: white;
    font-size: 16px;
}
.cart-btn {
    width: 25px;
    height: 25px;
    border-radius: 50%;
    background-color: #1a69fc;
}
.border-box {
    border: 1px solid rgb(233, 233, 238);
    // border-color: inherit;
    cursor: pointer;
}
.active {
    border: 1px solid var(--el-color-primary);
}
</style>
