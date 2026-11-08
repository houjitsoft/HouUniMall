<template>
    <div>
        <el-card class="!border-none" shadow="never">
            <el-alert
                type="warning"
                title="1、审核用户申请的售后订单；2、申请售后需要用户确认收货之后才能申请，平台同意退款后处于等待退款的状态，平台会自动进行原路退款操作"
                :closable="false"
                show-icon
            />

            <!-- Header -->
            <el-form ref="formRef" class="mb-[-16px] mt-[16px]" :model="queryParams" :inline="true">
                <el-form-item label="订单搜索">
                    <el-input
                        v-model="queryParams.key_word"
                        placeholder="请输入"
                        class="w-[320px]"
                        @keyup.enter="resetPage"
                    >
                        <template #prepend>
                            <el-select
                                v-model="queryParams.key_word_type"
                                placeholder="选择"
                                style="width: 100px"
                            >
                                <el-option label="订单编号" value="1" />
                                <el-option label="用户编号" value="2" />
                                <el-option label="用户昵称" value="3" />
                                <el-option label="收货人姓名" value="4" />
                                <el-option label="收货人手机" value="5" />
                                <el-option label="售后单号" value="6" />
                            </el-select>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item label="商品名称">
                    <el-input
                        v-model="queryParams.goods_name"
                        placeholder="请输入"
                        class="w-[280px]"
                        @keyup.enter="resetPage"
                    >
                    </el-input>
                </el-form-item>
                <el-form-item label="退款方式">
                    <el-select class="w-[280px]" v-model="queryParams.type">
                        <el-option label="全部" value="" />
                        <el-option label="仅退款" value="0" />
                        <el-option label="退款退货" value="1" />
                    </el-select>
                </el-form-item>
                <el-form-item label="申请时间">
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

        <!-- Main -->
        <el-card class="!border-none mt-4" :body-style="{ 'padding-top': '10px' }" shadow="never">
            <!-- 选项卡 -->
            <el-tabs v-model="queryParams.status" class="demo-tabs" @tab-change="changeTabs">
                <template v-for="(tabsItem, tabsIndex) in tabsParams.TabsEnumMap" :key="tabsIndex">
                    <el-tab-pane
                        :label="`${tabsItem.label}(${pager.extend[tabsItem.type] || 0})`"
                        :name="tabsItem.name"
                    />
                </template>
            </el-tabs>

            <!-- 表格 -->
            <el-table
                size="large"
                v-loading="pager.loading"
                :data="pager.lists"
                @selection-change="selectDataChange"
            >
                <el-table-column prop="id" label="ID" width="55" />
                <el-table-column prop="sn" label="售后单号" width="180" />
                <el-table-column label="商品信息" min-width="220">
                    <template #default="{ row }">
                        <router-link
                            :to="`/goods/lists`"
                            v-for="(item, index) in row.order_goods"
                            :key="index"
                        >
                            <div class="flex mb-[5px]">
                                <image-contain
                                    class="flex-none"
                                    :src="item.goods_image"
                                    :width="70"
                                    :height="70"
                                    :preview-src-list="[item.goods_image]"
                                    preview-teleported
                                    fit="cover"
                                />

                                <div class="ml-[6px] text-xs overflow-hidden">
                                    <el-tooltip
                                        effect="dark"
                                        :content="item.goods_name"
                                        placement="top"
                                    >
                                        <div class="text-base truncate goods-name">
                                            {{ item.goods_name }}
                                        </div>
                                    </el-tooltip>
                                    <div class="text-tx-placeholder">
                                        {{ item.goods_sku_value.join(',') }}
                                    </div>
                                    <div class="black">¥{{ item.goods_price }}</div>
                                </div>
                            </div>
                        </router-link>
                    </template>
                </el-table-column>
                <el-table-column label="买家昵称" min-width="100">
                    <template #default="{ row }">
                        {{ row.user.nickname }}
                    </template>
                </el-table-column>
                <el-table-column label="数量" min-width="100">
                    <template #default="{ row }">
                        {{ row.order_goods?.goods_num }}
                    </template>
                </el-table-column>
                <el-table-column label="实付金额" min-width="100">
                    <template #default="{ row }"> ¥{{ row.order.need_pay_money }} </template>
                </el-table-column>
                <el-table-column label="退款金额" min-width="90">
                    <template #default="{ row }"> ¥{{ row.refund_money }} </template>
                </el-table-column>
                <el-table-column prop="type_text" label="退款方式" min-width="90" />
                <el-table-column label="售后状态" min-width="100">
                    <template #default="{ row }">
                        <div v-if="row.status === 0">申请中</div>
                        <div v-if="row.status === 10" class="text-warning">等待退款</div>
                        <div v-if="row.status === 20" class="text-success">售后成功</div>
                        <div v-if="row.status === 30" class="text-danger">售后失败</div>
                    </template>
                </el-table-column>
                <el-table-column prop="create_time" label="申请时间" min-width="140" />
                <el-table-column label="操作" width="240" fixed="right">
                    <template #default="{ row }">
                        <el-button v-perms="['mall.orderAfterSale/detail']" type="primary" link>
                            <router-link
                                :to="{
                                    path: getRoutePath('mall.orderAfterSale/detail'),
                                    query: {
                                        id: row.id
                                    }
                                }"
                            >
                                订单详情
                            </router-link>
                        </el-button>
                        <ai-agent-panel
                            class="ml-2"
                            code="aftersale_ticket"
                            label="AI 处理建议"
                            link
                            size="small"
                            :payload="() => buildTicketPayload(row)"
                        />
                    </template>
                </el-table-column>
            </el-table>
            <div class="flex justify-end mt-4">
                <pagination v-model="pager" @change="getLists" />
            </div>
        </el-card>
    </div>
</template>
<script lang="ts" setup name="consumerLists">
import AiAgentPanel from '@/components/ai-agent-panel/index.vue'
import { usePaging } from '@/hooks/usePaging'
import { getRoutePath } from '@/router'
import type { AfterSaleType } from '@/api/order/after_sale'
import { getAfterSaleLists } from '@/api/order/after_sale'
import feedback from '@/utils/feedback'

// Tab类型
enum TabsEnum {
    ALL = 'all',
    APPLY = 'apply',
    FAIL = 'fail',
    SUCCESS = 'success',
    WAIT_REFUND = 'wait_refund'
}

const remarkRef = shallowRef()
const logisticsRef = shallowRef()
const tabsParams = reactive({
    TabsEnumMap: [
        {
            label: '全部',
            name: '',
            type: TabsEnum.ALL
        },
        {
            label: '申请中',
            name: 0,
            type: TabsEnum.APPLY
        },
        {
            label: '等待退款',
            name: 10,
            type: TabsEnum.WAIT_REFUND
        },
        {
            label: '售后成功',
            name: 20,
            type: TabsEnum.SUCCESS
        },
        {
            label: '售后失败',
            name: 30,
            type: TabsEnum.FAIL
        }
    ]
})

const queryParams = reactive<AfterSaleType>({
    status: '',
    goods_name: '',
    key_word: '',
    key_word_type: '1',
    order_from: '',
    type: '',
    start_time: '',
    end_time: ''
})

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: getAfterSaleLists,
    params: queryParams
})

const changeTabs = (name: string) => {
    queryParams.status = name
    getLists()
}

const handleMore = (event: string, row: any) => {
    switch (event) {
        case 'cancel':
            handleCancel(row.id)
            break
        case 'confirm':
            handleConfirm(row.id)
            break
        case 'remark':
            remarkRef.value.open(row.id, row.shop_remark)
            break
        case 'send':
            logisticsRef.value.open('send', row.id)
            break
        case 'delivery':
            logisticsRef.value.open('delivery', row.id)
            break
    }
}

const handleCancel = async (id: number) => {
    try {
        await feedback.confirm('确定要取消订单吗？')
        await apiOrderCancel({ id })
        getLists()
    } catch (error) {
        console.log('取消失败=>', error)
    }
}

const handleConfirm = async (id: number) => {
    try {
        await feedback.confirm('确认用户已收到货吗？')
        await apiOrderConfirm({ id })
        getLists()
    } catch (error) {
        console.log('取消失败=>', error)
    }
}

getLists()

/** 售后工单交给「售后工单智能处理」智能体分诊；只用列表已有字段，不额外请求 */
const buildTicketPayload = (row: any) => {
    if (!row) return ''
    const goods = (row.order_goods || [])
        .map((item: any) => item.goods_name || item.goodsName || item.name || '')
        .filter(Boolean)
        .join('、')
    const statusText = ({ 0: '申请中', 10: '等待退款', 20: '售后成功', 30: '售后失败' } as Record<number, string>)[row.status] || '未知'
    return [
        '请判定以下售后工单的类型、紧急度并给出处理方案：',
        `售后单号：${row.sn || '未提供'}`,
        `商品：${goods || '未提供'}`,
        `买家：${row.nickname || '未提供'}`,
        `退款方式：${row.type_text || '未提供'}`,
        `实付金额：${row.paid_amount ?? '未提供'}　退款金额：${row.refund_amount ?? '未提供'}`,
        `当前状态：${statusText}`,
        `申请时间：${row.create_time || '未提供'}`
    ].join('\n')
}
</script>
