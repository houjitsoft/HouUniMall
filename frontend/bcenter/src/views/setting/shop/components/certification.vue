<template>
    <el-form-item label="营业执照" prop="business_license">
        <material-picker v-model="modelValue.business_license" :limit="1" />
    </el-form-item>
    <el-form-item label="其他资质">
        <div>
            <material-picker v-model="modelValue.other_license" :limit="5" />
            <div class="form-tips">
                最多支持上传5张
            </div>
        </div>
    </el-form-item>
</template>

<script lang="ts" setup>
withDefaults(
    defineProps<{
        modelValue?: any;
    }>(),
    {
        modelValue: {},
    }
);
</script>
