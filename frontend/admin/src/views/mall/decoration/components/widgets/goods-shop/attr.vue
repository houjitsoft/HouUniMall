<template>
    <div>
        <el-form label-width="80px">
            <el-card shadow="never" class="!border-none flex">
                <div
                    class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
                >
                    店铺招牌
                </div>
            </el-card>
            <el-card shadow="never" class="!border-none flex mt-2 w-full">
                <div class="mb-2 title flex items-center text-lg font-medium">店铺招牌</div>
                <el-form-item label="店铺背景">
                    <div>
                        <el-radio-group v-model="content.showBg">
                            <el-radio :label="1">系统默认图片</el-radio>
                            <el-radio :label="0">自定义</el-radio>
                        </el-radio-group>
                        <material-picker
                            v-if="content.showBg == 0"
                            v-model="content.image"
                            upload-class="bg-body"
                            exclude-domain
                        />
                        <div class="form-tips" v-if="content.showBg == 0">
                            建议图片尺寸：750px*64px
                        </div>
                    </div>
                </el-form-item>
            </el-card>
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
</script>

<style lang="scss" scoped></style>
