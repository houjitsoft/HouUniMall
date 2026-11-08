<template>
    <div class="coupon-lists">
        <el-card class="!border-none" shadow="never">
            <el-alert
                type="warning"
                title="温馨提示：1.优惠券在发放时间内只要未关闭未删除符合条件就能领取；2.优惠券已结束不能继续领取，已发放的优惠券在用券时间内能可继续使用；3.已结束的优惠券允许后台删除，优惠券已删除不能继续领取已发放的优惠券不能继续使用"
                :closable="false"
                show-icon
            />

            <el-form ref="formRef" class="mb-[-16px] mt-[16px]" :model="queryParams" :inline="true">
                <el-form-item label="优惠券搜索">
                    <el-input
                        class="w-[280px]"
                        v-model="queryParams.name"
                        placeholder="请输入优惠券名称"
                        :clearable="true"
                        @keyup.enter="resetPage"
                    />
                </el-form-item>
                <el-form-item label="领取方式">
                    <el-select class="w-[280px]" v-model="queryParams.get_type">
                        <el-option label="全部" value />
                        <el-option label="用户领取" :value="0" />
                        <el-option label="系统发放" :value="1" />
                    </el-select>
                </el-form-item>
                <el-form-item label="活动时间">
                    <daterange-picker
                        v-model:startTime="queryParams.start_time"
                        v-model:endTime="queryParams.end_time"
                    />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="resetPage">查询</el-button>
                    <el-button @click="resetParams">重置</el-button>
                    <ai-agent-panel
                        class="ml-2"
                        code="marketing_plan"
                        label="AI 策划建议"
                        :payload="buildMarketingPayload"
                    />
                </el-form-item>
            </el-form>
        </el-card>
        <el-card class="!border-none mt-4" :body-style="{ 'padding-top': '10px' }" shadow="never">
            <!-- 选项卡 -->
            <el-tabs v-model="queryParams.status" @tab-change="changeTabs">
                <template v-for="(tabItem, tabIndex) in tabsParams.TabsEnumMap" :key="tabIndex">
                    <el-tab-pane
                        :label="`${tabItem.label}(${pager.extend[tabItem.type] || 0})`"
                        :name="tabItem.name"
                    />
                </template>
            </el-tabs>
            <el-button type="primary">
                <router-link
                    :to="{
                        path: getRoutePath('mall.coupon/detail')
                    }"
                >
                    + 新增优惠券
                </router-link>
            </el-button>

            <!-- 表格 -->
            <el-table size="large" class="mt-4" v-loading="pager.loading" :data="pager.lists">
                <el-table-column label="优惠券名称" min-width="190">
                    <template #default="{ row }">
                        <div class="text-[#101010]">{{ row.name }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="券内容" prop="coupon_content" min-width="190" />
                <el-table-column label="推广方式" min-width="120">
                    <template #default="{ row }">
                        <div v-if="row.get_type_text == '用户领取'">
                            {{ row.get_type_text }}
                        </div>
                        <div v-else class="text-success">
                            {{ row.get_type_text }}
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="使用时间" prop="use_time_text" min-width="200" />
                <el-table-column label="发放数量" prop="stock_type_text" min-width="100" />
                <el-table-column label="已使用/领取" min-width="120">
                    <template #default="{ row }">
                        <span>{{ row.has_use_num }}</span
                        >/
                        <span>{{ row.has_get_num }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="使用率" prop="has_use_ratio" min-width="100">
                    <template #default="{ row }"> {{ row.has_use_ratio }}% </template>
                </el-table-column>
                <el-table-column label="优惠券状态" min-width="120">
                    <template #default="{ row }">
                        <el-tag v-if="row?.status == 0" type="danger">未开始</el-tag>
                        <el-tag v-if="row?.status == 1" type="success">进行中</el-tag>
                        <el-tag v-if="row?.status == 2" type="info">已结束</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="创建时间" prop="create_time" min-width="180" />
                <el-table-column label="操作" width="280" fixed="right">
                    <template #default="{ row }">
                        <el-button v-perms="['mall.coupon/detail']" type="primary" :link="true">
                            <router-link
                                :to="{
                                    path: getRoutePath('mall.coupon/detail'),
                                    query: {
                                        id: row.id,
                                        read_only: 'detail'
                                    }
                                }"
                            >
                                详情
                            </router-link>
                        </el-button>

                        <el-button
                            v-if="row.status != 0"
                            v-perms="['mall.coupon/detail']"
                            type="primary"
                            :link="true"
                        >
                            <router-link
                                :to="{
                                    path: getRoutePath('mall.coupon/detail'),
                                    query: {
                                        id: row.id,
                                        read_only: true
                                    }
                                }"
                            >
                                编辑
                            </router-link>
                        </el-button>

                        <el-button
                            v-if="row.status == 0"
                            v-perms="['mall.coupon/detail']"
                            type="primary"
                            :link="true"
                        >
                            <router-link
                                :to="{
                                    path: getRoutePath('mall.coupon/detail'),
                                    query: {
                                        id: row.id
                                    }
                                }"
                            >
                                编辑
                            </router-link>
                        </el-button>

                        <el-button
                            v-if="row.can_sys_send"
                            v-perms="['mall.coupon/sys_send']"
                            type="primary"
                            :link="true"
                        >
                            <router-link
                                :to="{
                                    path: getRoutePath('mall.coupon/sys_send'),
                                    query: {
                                        id: row.id
                                    }
                                }"
                                v-if="row?.status == 1"
                            >
                                系统赠送
                            </router-link>
                        </el-button>

                        <el-button
                            v-if="row.can_start"
                            v-perms="['marketing:coupon:start']"
                            type="primary"
                            :link="true"
                            @click="handleStart(row.id, row.name)"
                        >
                            开始
                        </el-button>

                        <el-button
                            v-if="row.can_end"
                            v-perms="['marketing:coupon:end']"
                            type="primary"
                            :link="true"
                            @click="handleEnd(row.id, row.name)"
                        >
                            结束
                        </el-button>

                        <el-button
                            v-if="row.can_delete"
                            v-perms="['marketing:coupon:del']"
                            type="danger"
                            :link="true"
                            @click="handleDelete(row.id, row.name)"
                        >
                            删除
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
<script lang="ts" setup name="CouponLists">
import AiAgentPanel from '@/components/ai-agent-panel/index.vue'
import { usePaging } from '@/hooks/usePaging'
import { getRoutePath } from '@/router'
import { couponLists, couponStart, couponEnd, couponDel } from '@/api/marketing/coupon'
import type { CouponListQueryParamsType } from '@/api/marketing/coupon.d'
import feedback from '@/utils/feedback'

// Tab类型
enum TabsEnum {
    ALL = 'all',
    WAIT = 'wait',
    START = 'start',
    END = 'end'
}

const tabsParams = reactive({
    TabsEnumMap: [
        {
            label: '全部',
            name: '',
            type: TabsEnum.ALL
        },
        {
            label: '未开始',
            name: '1',
            type: TabsEnum.WAIT
        },
        {
            label: '进行中',
            name: '2',
            type: TabsEnum.START
        },
        {
            label: '已结束',
            name: '3',
            type: TabsEnum.END
        }
    ]
})

const queryParams = reactive<CouponListQueryParamsType>({
    name: '',
    get_type: '',
    start_time: '',
    end_time: '',
    status: ''
})

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: couponLists,
    params: queryParams
})

const changeTabs = (name: string) => {
    queryParams.status = name
    getLists()
}

const handleStart = async (id: number, name: string) => {
    try {
        await feedback.confirm(`确定开始发放(${name})？，请谨慎操作`, '开始发放')
        await couponStart({ id })
        getLists()
    } catch (error) {
        console.log('开启优惠券失败=>', error)
    }
}

const handleEnd = async (id: number, name: string) => {
    try {
        await feedback.confirm(
            `确定结束发放(${name})？结束发放的优惠券不能开始新的领取，请谨慎操作`,
            '结束发放'
        )
        await couponEnd({ id })
        getLists()
    } catch (error) {
        console.log('结束优惠券失败=>', error)
    }
}

const handleDelete = async (id: number, name: string) => {
    try {
        await feedback.confirm(`确定删除(${name})活动吗？`)
        await couponDel({ id })
        getLists()
    } catch (error) {
        console.log('删除优惠券失败=>', error)
    }
}

onActivated(() => {
    getLists()
})

onMounted(() => {
    getLists()
})

/**
 * 营销活动策划：把当前页优惠券的投放与使用情况交给智能体，
 * 让它基于真实使用率给玩法与力度建议（客群/预算未提供，会在结论里标注为假设）。
 */
const buildMarketingPayload = () => {
    const rows = (pager.lists || []).filter((r: any) => r && r.name)
    if (!rows.length) return ''
    const lines = rows.slice(0, 20).map((r: any, i: number) => {
        return `${i + 1}. ${r.name}｜券内容：${r.coupon_content || '未提供'}｜发放：${r.stock_type_text || '未提供'}｜已领/已用：${r.received_num ?? '?'}/${r.use_num ?? '?'}｜使用率：${r.has_use_ratio || '未提供'}｜状态：${r.status_text || r.status || '未知'}`
    })
    return `请基于以下优惠券活动的真实投放与使用情况，给出下一阶段营销策划建议（客群与预算未提供，请标注假设）：\n${lines.join('\n')}`
}
</script>
