<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                用户信息
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <!-- <div class="mb-2 title flex items-center text-lg font-medium">展示样式</div> -->
            <el-form-item label="会员信息">
                <div>
                    <el-radio-group v-model="content.bgConfig">
                        <el-radio :label="1">跟随系统风格</el-radio>
                        <el-radio :label="0">背景图片</el-radio>
                    </el-radio-group>
                    <material-picker
                        v-if="content.bgConfig == 0"
                        v-model="content.image"
                        upload-class="bg-body"
                        exclude-domain
                    />
                    <div class="form-tips" v-if="content.bgConfig == 0">
                        建议图片尺寸：750px*115px
                    </div>
                </div>
            </el-form-item>
            <el-form-item label="会员信息">
                <el-radio-group v-model="content.showInfo">
                    <el-radio :label="1">显示</el-radio>
                    <el-radio :label="0">隐藏</el-radio>
                </el-radio-group>
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

<style lang="scss" scoped></style>
