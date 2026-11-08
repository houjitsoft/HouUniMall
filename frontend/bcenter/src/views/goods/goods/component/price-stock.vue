<template>
    <el-form-item label="商品规格" prop="sku_type">
        <div class="w-80">
            <el-radio-group v-model="modelValue.sku_type" class="ml-4">
                <el-radio :label="0">单规格</el-radio>
                <el-radio :label="1">多规格</el-radio>
            </el-radio-group>
        </div>
    </el-form-item>

    <div v-show="modelValue.sku_type">
        <specifications :modelValue="modelValue" @update:modelValue="emit('update:modelValue', $event)"></specifications>
    </div>

    <div v-if="!modelValue.sku_type">
        <el-form-item label="商品价格" prop="sku[0].price" :rules="[
            {
                required: true,
                message: '请输入商品金额',
                trigger: ['blur', 'change']
            }
        ]">
            <div class="w-60">
                <el-input v-model="modelValue.sku[0].price" placeholder="请输入金额">
                    <template #append>元</template>
                </el-input>
            </div>
        </el-form-item>
        <el-form-item label="划线价">
            <div class="w-60">
                <el-input v-model="modelValue.sku[0].line_price" placeholder="请输入划线价">
                    <template #append>元</template>
                </el-input>
            </div>
        </el-form-item>
        <el-form-item label="成本价">
            <div class="w-60">
                <el-input v-model="modelValue.sku[0].market_price" placeholder="请输入成本价">
                    <template #append>元</template>
                </el-input>
            </div>
        </el-form-item>
        <el-form-item label="库存" prop="sku[0].stock" :rules="[
            {
                required: true,
                message: '请输入库存',
                trigger: ['blur', 'change']
            }
        ]">
            <div class="w-60">
                <el-input v-model="modelValue.sku[0].stock" placeholder="请输入库存数量" />
            </div>
        </el-form-item>
        <el-form-item label="体积">
            <div class="w-60">
                <el-input v-model="modelValue.sku[0].volume" placeholder="请输入">
                    <template #append>m³</template>
                </el-input>
            </div>
        </el-form-item>
        <el-form-item label="重量">
            <div class="w-60">
                <el-input v-model="modelValue.sku[0].weight" placeholder="请输入">
                    <template #append>KG</template>
                </el-input>
            </div>
        </el-form-item>
        <el-form-item label="条形码">
            <div class="w-60">
                <el-input v-model="modelValue.sku[0].code" placeholder="请输入" />
            </div>
        </el-form-item>
    </div>
</template>

<script lang="ts" setup>
import { getRoutePath } from "@/router";
import Specifications from "./specifications.vue";

const emit = defineEmits(['update:modelValue'])

withDefaults(
    defineProps<{
        modelValue?: any;
    }>(),
    {
        modelValue: {},
    }
)
</script>
