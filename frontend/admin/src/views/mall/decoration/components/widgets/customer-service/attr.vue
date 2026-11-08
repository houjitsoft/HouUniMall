<template>
    <div>
        <el-form label-width="90px">
            <el-form-item label="客服标题">
                <el-input class="w-[400px]" v-model="content.title" />
            </el-form-item>
            <el-form-item label="服务时间">
                <el-input class="w-[400px]" v-model="content.time" />
            </el-form-item>
            <el-form-item label="联系电话">
                <el-input class="w-[400px]" v-model="content.mobile" />
            </el-form-item>
            <el-form-item label="客服二维码">
                <div>
                    <material-picker v-model="content.qrcode" exclude-domain />
                    <div class="form-tips">建议图片尺寸：200*200像素；图片格式：jpg、png、jpeg</div>
                </div>
            </el-form-item>
        </el-form>
    </div>
</template>
<script setup>
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
