<template>
    <el-table size="large" :data="goods">
        <el-table-column prop="goods_code" label="商品货号" min-width="100" />
        <el-table-column prop="goods" label="商品信息" min-width="220">
            <template #default="{ row }">
                <div class="flex mb-[5px]">
                    <image-contain
                        class="flex-none"
                        :src="row.goods_image"
                        :width="70"
                        :height="70"
                        :preview-src-list="[row.goods_image]"
                        preview-teleported
                        fit="cover"
                    />
                    <div class="ml-[6px] text-xs overflow-hidden">
                        <div class="mt-2 goods-name">{{ row.goods_name }}</div>
                        <div class="text-gray-300">{{ row.goods_sku_value.join(',') }}</div>
                    </div>
                </div>
            </template>
        </el-table-column>
        <el-table-column label="单价/数量" min-width="100">
            <template #default="{ row }">
                <div class="ml-2 inline-block text-center">
                    <div>¥{{ row.goods_price }}</div>
                    <div>
                        x <span>{{ row.goods_num }}</span>
                    </div>
                </div>
            </template>
        </el-table-column>
        <el-table-column label="优惠" min-width="100">
            <template #default="{ row }">
                <!-- <div class="text-[#979797]">(含运费: {{ row.express_money }})</div> -->
                <el-popover placement="top" width="300px" trigger="hover">
                    <template v-if="couponName">
                        <div class="text-primary">{{ couponName }}</div>
                        <div class="mt-[4px]">优惠金额: ¥{{ row.coupon_money }}</div>
                    </template>

                    <div class="text-primary mt-[10px]">会员折扣</div>
                    <div class="mt-[4px]">优惠金额: ¥{{ row.discount_money }}</div>
                    <div class="text-primary mt-[10px]">优惠券</div>
                    <div class="mt-[4px]">优惠金额: ¥{{ row.coupon_money }}</div>
                    <template #reference>
                        <div class="flex items-center">
                            <div class="font-medium">¥{{ row.dec_money }}</div>

                            <!-- <span class="text-[#979797]">(含优惠: {{ row.dec_money }})</span> -->
                            <el-icon size="18px" class="ml-1 text-[#ccc]"
                                ><QuestionFilled
                            /></el-icon>
                        </div>
                    </template>
                </el-popover>
            </template>
        </el-table-column>

        <el-table-column label="运费" min-width="100">
            <template #default="{ row }"> ¥{{ row.express_money }} </template>
        </el-table-column>
        <el-table-column label="商品总价" min-width="100">
            <template #default="{ row }"> ¥{{ row.goods_money }} </template>
        </el-table-column>
        <el-table-column label="售后状态" min-width="100">
            <template #default="{ row }">
                <span class="" v-if="row.after_sale_detail">{{
                    row.after_sale_detail.status_text
                }}</span>
                <span v-else>无售后</span>
            </template>
        </el-table-column>
        <!-- <el-table-column label="实付金额" min-width="100">
            <template #default="{ row }">
                <div class="ml-2">¥{{ row.need_pay_money }} </div>
            </template>
        </el-table-column> -->
    </el-table>
</template>

<script lang="ts" setup>
import type { PropType } from 'vue'
defineProps({
    goods: {
        type: Array as PropType<any>,
        default: () => []
    },
    couponName: {
        type: String,
        default: ''
    }
})
</script>
