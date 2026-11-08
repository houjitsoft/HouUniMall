<template>
    <div class="coupon-lists">
        <el-card class="!border-none" shadow="never">
            <el-alert
                type="warning"
                title="温馨提示：1.查看优惠券领取记录；2.优惠券可以作废。"
                :closable="false"
                show-icon
            />

            <el-form ref="formRef" class="mb-[-16px] mt-[16px]" :model="queryParams" :inline="true">
                <el-form-item label="用户信息">
                    <el-input
                        class="w-[280px]"
                        v-model="queryParams.user"
                        placeholder="请输入用户编号/昵称"
                        :clearable="true"
                        @keyup.enter="resetPage"
                    />
                </el-form-item>
                <el-form-item label="优惠券名称">
                    <el-input
                        class="w-[280px]"
                        v-model="queryParams.coupon_mame"
                        placeholder="请输入优惠券名称"
                        :clearable="true"
                        @keyup.enter="resetPage"
                    />
                </el-form-item>
                <el-form-item label="使用状态">
                    <el-select class="w-[280px]" v-model="queryParams.status">
                        <el-option label="全部" value />
                        <el-option label="未使用" :value="0" />
                        <el-option label="已使用" :value="1" />
                        <el-option label="已过期" :value="2" />
                        <el-option label="已作废" :value="3" />
                    </el-select>
                </el-form-item>
                <el-form-item label="领取时间">
                    <daterange-picker
                        v-model:startTime="queryParams.get_time_start"
                        v-model:endTime="queryParams.get_time_end"
                    />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="resetPage">查询</el-button>
                    <el-button @click="resetParams">重置</el-button>
                </el-form-item>
            </el-form>
        </el-card>
        <el-card class="!border-none mt-4" :body-style="{ 'padding-top': '10px' }" shadow="never">
            <el-button
                type="default"
                :plain="true"
                :disabled="!multipleSelection.length"
                @click="
                    handleRepeal(
                        multipleSelection.map((item) => item.id),
                        multipleSelection.map((item) => item.name).toString()
                    )
                "
            >
                批量作废
            </el-button>

            <!-- 表格 -->
            <el-table
                size="large"
                class="mt-4"
                v-loading="pager.loading"
                :data="pager.lists"
                @selection-change="handleSelectionChange"
            >
                <el-table-column type="selection" width="55" />
                <el-table-column label="优惠券名称" prop="coupon.name" min-width="140" />
                <el-table-column label="用户信息" min-width="180">
                    <template #default="{ row }">
                        <el-popover placement="top" width="300px" trigger="hover">
                            <div>
                                头像: <el-avatar :src="row.user.avatar" size="50"></el-avatar>
                            </div>
                            <div class="mt-[20px]">昵称: {{ row.user.nickname }}</div>
                            <div class="mt-[20px]">账号: {{ row.user.account || '-' }}</div>
                            <template #reference>
                                <el-tag>{{ row.user.nickname }}</el-tag>
                            </template>
                        </el-popover>
                    </template>
                </el-table-column>
                <el-table-column label="领取时间" prop="create_time" min-width="180" />
                <el-table-column label="使用状态" min-width="120">
                    <template #default="{ row }">
                        <el-tag v-if="row?.status == 0" type="danger">未使用</el-tag>
                        <el-tag v-if="row?.status == 1" type="success">已使用</el-tag>
                        <el-tag v-if="row?.status == 2" type="info">已过期</el-tag>
                        <el-tag v-if="row?.status == 3" type="info">已作废</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="使用时间" min-width="200">
                    <template #default="{ row }">
                        {{ row.use_time || '-' }}
                    </template>
                </el-table-column>
                <el-table-column label="过期时间" prop="end_time" min-width="180" />
                <el-table-column label="操作" width="120" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            v-if="row?.status == 1"
                            v-perms="['mall.order/detail']"
                            type="primary"
                            :link="true"
                        >
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

                        <el-button
                            v-perms="['marketing:coupon:repeal']"
                            v-if="row?.status == 0"
                            type="primary"
                            :link="true"
                            @click="handleRepeal([row.id], row.coupon_name)"
                        >
                            作废
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div class="flex justify-end mt-4">
                <pagination v-model="pager" @change="getLists" />
            </div>
        </el-card>
    </div>
</template>
<script lang="ts" setup name="CouponRecordLists">
import { usePaging } from '@/hooks/usePaging'
import { couponRecordLists, couponRepeal } from '@/api/marketing/coupon'
import type { CouponRecordListQueryParamsType } from '@/api/marketing/coupon.d'
import feedback from '@/utils/feedback'
import { ElTable } from 'element-plus'
import { getRoutePath } from '@/router'

const multipleSelection = ref<any[]>([])
const queryParams = reactive<CouponRecordListQueryParamsType>({
    user: '',
    coupon_mame: '',
    get_time_start: '',
    get_time_end: '',
    status: ''
})

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: couponRecordLists,
    params: queryParams
})

const handleSelectionChange = (value: any[]) => {
    multipleSelection.value = value
}

const handleRepeal = async (ids: number[], name: string) => {
    try {
        await feedback.confirm(`确定要将(${name})的优惠券作废吗？`)
        await couponRepeal({ ids })
        getLists()
    } catch (error) {
        console.log('作废优惠券失败=>', error)
    }
}

onMounted(() => {
    getLists()
})
</script>
