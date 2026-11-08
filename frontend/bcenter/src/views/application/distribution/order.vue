<template>
    <div>
        <el-card class="!border-none" shadow="never">
            <el-alert type="warning" title="分销订单明细。" :closable="false" show-icon />
            <el-form ref="formRef" class="mb-[-16px] mt-4" :model="queryParams" :inline="true">
                <el-form-item label="订单信息">
                    <el-input
                        class="w-[280px]"
                        v-model="queryParams.order_sn"
                        placeholder="请输入订单编号"
                        clearable
                    />
                </el-form-item>
                <el-form-item label="买家信息">
                    <el-input
                        class="w-[280px]"
                        v-model="queryParams.user"
                        placeholder="请输入用户名称/编号"
                        clearable
                    />
                </el-form-item>
                <el-form-item label="分销商信息">
                    <el-input
                        class="w-[280px]"
                        v-model="queryParams.user_distribution"
                        placeholder="分销商名称/编码"
                        clearable
                    />
                </el-form-item>
                <el-form-item label="商品信息">
                    <el-input
                        class="w-[280px]"
                        v-model="queryParams.goods_name"
                        placeholder="商品名称/编码"
                        clearable
                    />
                </el-form-item>
                <el-form-item label="佣金状态">
                    <el-select class="w-[280px]" v-model="queryParams.status">
                        <el-option label="全部" value />
                        <el-option label="待结算" value="0" />
                        <el-option label="已入账" value="1" />
                        <el-option label="已失效" value="2" />
                    </el-select>
                </el-form-item>
                <el-form-item label="下单时间">
                    <daterange-picker
                        v-model:startTime="queryParams.start_time"
                        v-model:endTime="queryParams.end_time"
                    />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="resetPage">查询</el-button>
                    <el-button @click="resetParams">重置</el-button>
                </el-form-item>
            </el-form>
        </el-card>
        <el-card class="!border-none mt-4" shadow="never">
            <el-table size="large" v-loading="pager.loading" :data="pager.lists">
                <el-table-column label="买家信息" prop="sn" min-width="180">
                    <template #default="{ row }">
                        <div>
                            <div>{{ row.user.nickname }}</div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="订单信息" prop="sn" min-width="180">
                    <template #default="{ row }">
                        <div>{{ row.order_info.sn }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="商品信息" prop="sn" min-width="230">
                    <template #default="{ row }">
                        <div class="flex items-center">
                            <el-image
                                class="h-[64px] w-[64px] rounded flex-none"
                                :src="row.order_goods.goods_image"
                            ></el-image>
                            <span class="ml-2 truncate">{{ row.order_goods.goods_name }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="单价/数量" prop="sn" min-width="120">
                    <template #default="{ row }">
                        <div>¥{{ row.order_goods.goods_price }}</div>
                        <div>x{{ row.order_goods.goods_num }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="实付金额" prop="sn" min-width="120">
                    <template #default="{ row }">
                        <div>¥{{ row.order_goods.need_pay_money }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="当前分销等级" prop="level_text" min-width="120" />
                <el-table-column label="分销商信息" prop="sn" min-width="150">
                    <template #default="{ row }">
                        <div>{{ row.user_distribution.nickname || '-' }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="当前佣金比例" prop="ratio" min-width="120" />
                <el-table-column label="佣金金额" prop="money" min-width="120" />
                <el-table-column label="佣金状态" prop="money" min-width="120">
                    <template #default="{ row }">
                        <span v-if="row.status == 0">待结算</span>
                        <span v-if="row.status == 1">已结算</span>
                        <span v-if="row.status == 2">已失效</span>
                    </template>
                </el-table-column>
                <el-table-column label="结算时间" prop="settlement_time" min-width="120">
                    <template #default="{ row }">
                        <div>{{ row.settlement_time || '-' }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="下单时间" prop="create_time" min-width="120" />
            </el-table>
            <div class="flex justify-end mt-4">
                <pagination v-model="pager" @change="getLists" />
            </div>
        </el-card>
    </div>
</template>
<script setup lang="ts">
import { usePaging } from '@/hooks/usePaging'
import { getOrderList } from '@/api/application/distribution'
import { status } from 'nprogress'
const queryParams: any = ref({
    order_sn: '', //订单编号
    user: '', //买家信息
    user_distribution: '', //分销商信息
    goods_name: '', //商品信息
    status: '', //佣金状态
    start_time: '', //开始时间
    end_time: '' //结束时间
})

//分页组件
const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: getOrderList,
    params: queryParams.value
})

onMounted(() => {
    getLists()
})
</script>
