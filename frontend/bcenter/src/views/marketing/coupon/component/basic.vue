<template>
    <el-card class="!border-none mt-4" shadow="never">
        <template #header>
            <span class="font-medium text-xl">基础信息</span>
        </template>

        <el-form-item label="优惠券名称" prop="name">
            <div class="w-80">
                <el-input
                    v-model="formData.name"
                    placeholder="请输入优惠券名称"
                    type="text"
                    :disabled="disabled == 'detail'"
                    :show-word-limit="true"
                    :clearable="true"
                />
            </div>
        </el-form-item>

        <el-form-item label="发放数量" prop="stock_type">
            <div class="w-90">
                <div>
                    <el-input
                        class="w-[220px]"
                        v-model="formData.stock"
                        placeholder="请输入发放数量"
                        type="text"
                        :disabled="formData.stock_type == 0 || disabled == 'detail'"
                        :show-word-limit="true"
                        :clearable="true"
                    />
                    <el-checkbox
                        class="ml-4"
                        v-model="formData.stock_type"
                        label="不限数量"
                        :disabled="disabled"
                        :true-label="0"
                        :false-label="1"
                    />
                </div>
                <div class="form-tips">
                    编辑进行中的优惠券，发放总量只能增加不能减少，请谨慎设置。最多1000000张
                </div>
            </div>
        </el-form-item>

        <el-form-item label="使用时间" prop="use_time_type">
            <div>
                <div>
                    <el-radio-group v-model="formData.use_time_type" :disabled="disabled">
                        <el-radio :label="0">固定日期</el-radio>
                        <el-radio :label="1">领券当日起</el-radio>
                        <el-radio :label="2">领券次日起</el-radio>
                    </el-radio-group>
                </div>
                <div class="mt-4">
                    <daterange-picker
                        v-if="formData.use_time_type == 0"
                        style="width: auto"
                        :disabled="disabled"
                        v-model:startTime="formData.use_start_time"
                        v-model:endTime="formData.use_end_time"
                    />
                    <el-input
                        class="w-[280px]"
                        v-if="formData.use_time_type == 1"
                        v-model.number="formData.use_current_day"
                        placeholder="请输入"
                        type="text"
                        :disabled="disabled"
                        :show-word-limit="true"
                        :clearable="true"
                    >
                    </el-input>
                    <el-input
                        class="w-[280px]"
                        v-if="formData.use_time_type == 2"
                        v-model.number="formData.use_next_day"
                        placeholder="请输入"
                        type="text"
                        :disabled="disabled"
                        :show-word-limit="true"
                        :clearable="true"
                    />
                    <span class="ml-2">天内可用</span>
                </div>
            </div>
        </el-form-item>
    </el-card>
</template>

<script lang="ts" setup>
import { useVModels } from '@vueuse/core'
import Goods from './goods.vue'

const props = withDefaults(
    defineProps<{
        modelValue?: any
        disabled: boolean
    }>(),
    {
        modelValue: {},
        disabled: false
    }
)

const emit = defineEmits(['update:modelValue'])
const { modelValue: formData } = useVModels(props, emit)
</script>
