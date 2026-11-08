<template>
    <el-form-item label="配送方式" prop="goods_send_mode">
        <div class="w-80">
            <el-checkbox-group v-model="formData.goods_send_mode">
                <el-checkbox :label="0">快递配送</el-checkbox>
                <!-- <el-checkbox label="2">手机验证码登录</el-checkbox> -->
            </el-checkbox-group>

            <div class="form-tips">必须选择一种配送方式</div>
        </div>
    </el-form-item>
    <el-form-item label="运费设置" prop="express_type">
        <div class="w-80">
            <el-radio-group v-model="formData.express_type">
                <el-radio :label="0">包邮</el-radio>
                <el-radio :label="1">运费模版</el-radio>
            </el-radio-group>
        </div>
    </el-form-item>
    <el-form-item label="" prop="express_tpl_id" v-if="formData.express_type">
        <div class="w-80">
            <el-select v-model="formData.express_tpl_id" placeholder="请选择运费模版">
                <el-option v-for="item in expressList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
        </div>
    </el-form-item>
</template>

<script lang="ts" setup>
import { useVModels } from '@vueuse/core';
import { getGoodsOtherList } from "@/api/goods/goods";

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


type List = {
    id: number
    name: string
}

const expressList = shallowRef<List[]>([]);

const getList = async () => {
    try {
        const { express_list } = await getGoodsOtherList()
        expressList.value = express_list;
    } catch (error) {
        console.log(error)
    }
}

getList()
</script>
