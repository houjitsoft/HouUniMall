<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                导航组
            </div>
        </el-card>
    </el-form>
    <el-card shadow="never" class="!border-none flex mt-2 w-full">
        <div class="mb-2 title flex items-center text-lg font-medium">导航样式</div>
        <el-form-item label="轮播风格">
            <el-radio-group v-model="content.type">
                <el-radio :label="1" border>图+文</el-radio>
                <el-radio :label="2" border>图片</el-radio>
                <el-radio :label="3" border>文字</el-radio>
            </el-radio-group>
        </el-form-item>
    </el-card>
    <el-card shadow="never" class="!border-none flex mt-2 w-full">
        <div class="mb-2 title flex items-center text-lg font-medium">展示样式</div>
        <el-form-item label="">
            <el-radio-group v-model="content.show_type">
                <el-radio :label="1">固定显示</el-radio>
                <el-radio :label="2">分页滑动</el-radio>
            </el-radio-group>
        </el-form-item>
        <el-form-item label="展示样式">
            <el-select v-model="content.show_nums" placeholder="请选择" class="w-full">
                <el-option label="3个" :value="1" />
                <el-option label="4个" :value="2" />
                <el-option label="5个" :value="3" />
            </el-select>
        </el-form-item>
        <el-form-item label="显示行数">
            <el-select v-model="content.show_rows" placeholder="请选择" class="w-full">
                <el-option label="1行" :value="1" />
                <el-option label="2行" :value="2" />
                <el-option label="3行" :value="3" />
                <el-option label="4行" :value="4" />
            </el-select>
        </el-form-item>
    </el-card>
    <el-card shadow="never" class="!border-none flex mt-2 w-full">
        <div class="mb-2 title flex items-center text-lg font-medium">导航设置</div>
        <div class="flex-1">
            <AddNav v-model="content.data" max="20" class="w-full" />
        </div>
    </el-card>

    <!-- <div>
        <el-form label-width="70px">
            <el-form-item label="是否启用">
                <el-radio-group v-model="content.enabled">
                    <el-radio :label="1">开启</el-radio>
                    <el-radio :label="0">停用</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="菜单设置">
                <div class="flex-1">
                    <div class="mb-4 form-tips">建议图片尺寸：100px*100px</div>
                    <AddNav v-model="content.data" max="20" />
                </div>
            </el-form-item>
        </el-form>
    </div> -->
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

<style lang="scss" scoped>
:deep(.el-card__body) {
    width: 100%;
}
</style>
