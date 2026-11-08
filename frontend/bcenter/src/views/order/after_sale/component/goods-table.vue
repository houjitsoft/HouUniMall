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
                        <div class="text-tx-placeholder">{{ row?.goods_sku_value?.join(',') }}</div>
                    </div>
                </div>
            </template>
        </el-table-column>
        <el-table-column label="单价/数量" min-width="100">
            <template #default="{ row }">
                <div>
                    <div>¥{{ row.goods_price }}</div>
                    <div>
                        x <span>{{ row.goods_num }}</span>
                    </div>
                </div>
            </template>
        </el-table-column>
        <el-table-column label="运费" min-width="100">
            <template #default="{ row }">
                <div>¥{{ row.express_money }}</div>
            </template>
        </el-table-column>
        <el-table-column label="商品总价" min-width="100">
            <template #default="{ row }">
                <div>¥{{ row.goods_price * row.goods_num }}</div>
            </template>
        </el-table-column>
        <el-table-column label="实付金额" min-width="100">
            <template #default="{ row }">
                <div>¥{{ row.need_pay_money }}</div>
            </template>
        </el-table-column>
        <el-table-column label="操作" min-width="100">
            <template #default="{ row }">
                <el-button type="primary" link>
                    <router-link
                        :to="{
                            path: getRoutePath('mall.order/detail'),
                            query: {
                                id: row.order_id
                            }
                        }"
                    >
                        查看订单
                    </router-link>
                </el-button>
            </template>
        </el-table-column>
    </el-table>
</template>

<script lang="ts" setup>
import { getRoutePath } from '@/router'
import type { PropType } from 'vue'
defineProps({
    goods: {
        type: Array as PropType<any>,
        default: () => []
    }
})
</script>
