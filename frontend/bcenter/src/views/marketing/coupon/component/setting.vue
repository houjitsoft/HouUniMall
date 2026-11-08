<template>
    <el-card class="!border-none mt-4" shadow="never">
        <template #header>
            <span class="font-medium text-xl">优惠券设置</span>
        </template>

        <el-form-item label="优惠券金额" prop="money">
            <div class="w-80">
                <div>
                    <el-input
                        class="w-[280px]"
                        v-model="formData.money"
                        placeholder="请输入"
                        type="text"
                        :disabled="disabled"
                        :show-word-limit="true"
                        :clearable="true"
                    />
                    <span class="ml-4">元</span>
                </div>
                <div class="form-tips">面额需大于0元，支持整数</div>
            </div>
        </el-form-item>

        <el-form-item label="领取方式" prop="get_type">
            <div>
                <el-radio-group v-model="formData.get_type" :disabled="disabled">
                    <el-radio :label="0">用户领取</el-radio>
                    <el-radio :label="1">系统赠送</el-radio>
                </el-radio-group>
                <div class="form-tips">
                    用户领取：指用户自行领取；系统赠送：指通过后台给某个用户发放
                </div>
            </div>
        </el-form-item>

        <el-form-item label="使用门槛" prop="condition_type">
            <div>
                <div>
                    <el-radio-group v-model="formData.condition_type" :disabled="disabled">
                        <el-radio :label="0">无门槛</el-radio>
                    </el-radio-group>
                </div>
                <div class="mt-4">
                    <el-radio-group v-model="formData.condition_type" :disabled="disabled">
                        <el-radio :label="1">订单满</el-radio>
                    </el-radio-group>
                    <el-input
                        class="w-[180px] ml-4"
                        v-model="formData.condition_order_money"
                        placeholder="请输入价格"
                        type="text"
                        :disabled="disabled"
                        :show-word-limit="true"
                        :clearable="true"
                    >
                    </el-input>
                    <span class="ml-4">元可用</span>
                </div>
            </div>
        </el-form-item>

        <el-form-item label="领券次数" prop="get_limit">
            <div>
                <div>
                    <el-radio-group v-model="formData.get_limit" :disabled="disabled">
                        <el-radio :label="0">不限次数</el-radio>
                        <el-radio :label="1">限制领取次数</el-radio>
                        <el-radio :label="2">每天限制领取</el-radio>
                    </el-radio-group>
                </div>
                <div class="mt-4" v-if="formData.get_limit == 2 || formData.get_limit == 1">
                    <el-input
                        class="w-[310px]"
                        v-if="formData.get_limit == 1"
                        v-model.number="formData.get_limit_num"
                        placeholder="请输入"
                        type="text"
                        :disabled="disabled"
                        :show-word-limit="true"
                        :clearable="true"
                    >
                    </el-input>
                    <el-input
                        class="w-[310px]"
                        v-if="formData.get_limit == 2"
                        v-model.number="formData.get_limit_day_num"
                        placeholder="请输入"
                        type="text"
                        :disabled="disabled"
                        :show-word-limit="true"
                        :clearable="true"
                    >
                    </el-input>
                    <span class="ml-2">次</span>
                </div>
            </div>
        </el-form-item>

        <el-form-item label="使用范围" prop="use_goods_type">
            <div class="w-full">
                <el-radio-group v-model="formData.use_goods_type" :disabled="disabled">
                    <el-radio :label="0">全场通用</el-radio>
                    <el-radio :label="1">指定商品可用</el-radio>
                    <el-radio :label="2">指定商品不可用</el-radio>
                </el-radio-group>

                <template v-if="formData.use_goods_type != 0">
                    <Goods v-model="formData.goods" :disabled="disabled"></Goods>
                </template>

                <div class="form-tips">
                    用户领取：指用户自行领取；系统赠送：指通过后台给某个用户发放
                </div>
            </div>
        </el-form-item>
    </el-card>
</template>

<script lang="ts" setup>
import { useVModels } from '@vueuse/core'
import Goods from '@/views/marketing/coupon/component/goods.vue'

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

// 获取到优惠券商品参数
// watchEffect(() => {
//     console.log(formData.value.goods)

//     const value = formData.value.goods
//     try {
//         if (!value.length) {
//             formData.value.goods = []
//             return '已清空商品'
//         }
//         const data = value.map((item: any) => {
//             return {
//                 goods_id: item.id ?? item.goods_detail.id ?? '',
//                 image: item.image ?? item.goods_detail.image ?? '',
//                 name: item.name ?? item.goods_detail.name ?? '',
//                 min_price: item.min_price ?? item.goods_detail.min_price ?? '',
//                 code: item.code ?? item.goods_detail.code ?? ''
//             }
//         })
//         formData.value.goods = data
//     } catch (error) {
//         console.log(error)
//     }
// })
</script>
