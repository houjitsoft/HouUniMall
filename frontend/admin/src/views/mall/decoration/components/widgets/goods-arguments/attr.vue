<template>
    <el-form label-width="80px">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                商品参数
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">商品参数</div>
            <el-form-item label="显示内容" class="mt-2">
                <el-checkbox-group v-model="content.info" class="">
                    <el-checkbox :label="1">配送信息</el-checkbox>
                    <el-checkbox :label="2">规格选择</el-checkbox>
                </el-checkbox-group>
            </el-form-item>
        </el-card>
    </el-form>
</template>
<script setup>
// ⚠️ vue API 必须显式导入：缺失时构建期不报错，运行时报 'xxx is not defined' 导致页面白屏。
import { ref } from 'vue'
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
const activeIndex = ref(1)
</script>

<style lang="scss" scoped>
:deep(.el-card__body) {
    width: 100%;
}
.card {
    width: 100px;
    height: 40px;
    line-height: 40px;
    border-radius: 5px;
    border: 1px solid rgba(237, 241, 243, 1);
    text-align: center;
    cursor: pointer;
}
.active {
    border: 1px solid rgba(64, 115, 250, 1);
}
</style>
