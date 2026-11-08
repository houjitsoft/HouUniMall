<template>
    <el-form-item label="库存预警">
        <div class="w-80">
            <el-input v-model="formData.stock_warning" placeholder="" clearable />
            <div class="form-tips">
                设置最低库存预警值，不填或填0表示不做库存预警
            </div>
        </div>
    </el-form-item>
    <!-- <el-form-item label="虚拟销量">
        <div class="w-80">
            <el-input v-model="formData.virtual_sale_num" placeholder="请输入虚拟销量" clearable />
        </div>
    </el-form-item>
    <el-form-item label="虚拟浏览量">
        <div class="w-80">
            <el-input v-model="formData.virtual_visit_num" placeholder="请输入虚拟浏览量" clearable />
        </div>
    </el-form-item> -->
    <el-form-item label="排序">
        <div class="w-80">
            <el-input v-model="formData.sort" placeholder="" clearable />
            <div class="form-tips">默认为0，数值越大越排前</div>
        </div>
    </el-form-item>
    <!-- <el-form-item label="店铺推荐">
        <div class="w-80">
            <el-radio-group v-model="formData.shop_recommend" class="ml-4">
                <el-radio :label="1">推荐</el-radio>
                <el-radio :label="0">不推荐</el-radio>
            </el-radio-group>
            <div class="form-tips">勾选店内推荐后，推荐商品会在相关推荐位显示</div>
        </div>
    </el-form-item> -->
    <el-form-item label="销售状态" prop="status">
        <div class="w-80">
            <el-radio-group v-model="formData.status" class="ml-4">
                <el-radio :label="1">上架销售</el-radio>
                <el-radio :label="0">放入仓库</el-radio>
            </el-radio-group>
        </div>
    </el-form-item>
</template>

<script lang="ts" setup>
import { useVModels } from '@vueuse/core';

const props = withDefaults(
    defineProps<{
        modelValue?: any;
    }>(),
    {
        modelValue: {},
    }
);

const emit = defineEmits(['update:modelValue'])
const { modelValue: formData } = useVModels(props, emit);
</script>
