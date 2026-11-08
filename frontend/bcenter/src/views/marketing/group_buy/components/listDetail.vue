<template>
    <Popup ref="popRef" title="查看详情" width="auto">
        <div>
            <el-table :data="tableData?.join_list">
                <el-table-column label="用户信息" min-width="180">
                    <template #default="{ row }">
                        <div class="flex items-center">
                            <el-image
                                :src="row.user.avatar"
                                class="w-[47px] h-[47px] flex-none"
                            ></el-image>
                            <div class="ml-2">
                                <div>{{ row.user.nickname }}</div>
                                <div v-if="row.is_open == 1" class="text-error">团长</div>
                            </div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="订单编号">
                    <template #default="{ row }">
                        <div>{{ row.orderInfo.sn }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="商品信息" min-width="180px">
                    <template #default="{ row }">
                        <div class="flex items-center">
                            <el-image
                                :src="row.order_goods.goods_image"
                                class="w-[47px] h-[47px] flex-none"
                            ></el-image>
                            <div class="ml-2 truncate">{{ row.order_goods.goods_name }}</div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="订单金额">
                    <template #default="{ row }">
                        <div>{{ row.orderInfo.need_pay_money }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="支付状态">
                    <template #default="{ row }">
                        <div>{{ row.orderInfo.pay_is == 1 ? '已支付' : '待支付' }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="退款状态">
                    <template #default="{ row }">
                        <div v-if="row.is_refund == 1" class="text-error">已退款</div>
                        <div v-else>{{ '-' }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="订单状态">
                    <template #default="{ row }">
                        <div>{{ row.orderInfo.status_text }}</div>
                    </template>
                </el-table-column>
            </el-table>
        </div>
    </Popup>
</template>

<script setup lang="ts">
import { getPTDetail } from '@/api/marketing/group_buy'
//弹框ref
const popRef = shallowRef()
//拼团id
const id = ref()
//数据
const tableData = ref()

//打开弹框
const open = async (option: any) => {
    id.value = option.id
    await popRef.value.open()
    getData()
}

//获取数据
const getData = async () => {
    tableData.value = await getPTDetail({ id: id.value })
}

defineExpose({ open })
</script>

<style scoped lang="scss"></style>
