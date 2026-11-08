<template>
    <div>
        <el-card class="!border-none" shadow="never">
            <el-alert type="warning" :closable="false" show-icon>
                <template #title>
                    <div>1、会员商城下单列表</div>
                    <div>2、订单状态有待付款，待发货，待收货，已完成，已关闭</div>
                    <div>
                        3、待付款订单取消后则为已关闭。待付款订单支付后则为待发货。待发货订单发货后则为待收货。待收货订单收货后则为已完成。
                    </div>
                </template>
            </el-alert>

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
                                <el-option label="商品名称" value="1" />
                                <el-option label="商品货号" value="2" />
                                <el-option label="订单编号" value="3" />
                            </el-select>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item label="买家信息">
                    <el-input
                        v-model="queryParams.buyer_key_word"
                        placeholder="请输入"
                        class="w-[320px]"
                        @keyup.enter="resetPage"
                    >
                        <template #prepend>
                            <el-select
                                v-model="queryParams.buyer_key_word_type"
                                placeholder="Select"
                                style="width: 115px"
                            >
                                <el-option label="用户昵称" value="1" />
                                <el-option label="用户编号" value="2" />
                                <el-option label="收货人姓名" value="3" />
                                <el-option label="收货人手机" value="4" />
                            </el-select>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item label="订单来源">
                    <el-select class="w-[280px]" v-model="queryParams.from">
                        <el-option
                            v-for="(item, key) in ClientMap"
                            :key="key"
                            :label="item"
                            :value="key"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="支付方式">
                    <el-select class="w-[280px]" v-model="queryParams.pay_way">
                        <el-option
                            v-for="(item, key) in PayMap"
                            :key="key"
                            :label="item"
                            :value="key"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="时间类型">
                    <el-select
                        v-model="queryParams.time_type"
                        placeholder="全部"
                        style="width: 115px"
                    >
                        <el-option label="下单时间" value="create" />
                        <el-option label="支付时间" value="pay" />
                    </el-select>
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
                <el-table-column type="selection" width="55" />
                <el-table-column prop="sn" label="订单编号" width="180" />
                <el-table-column prop="type_text" label="订单类型" width="160">
                    <template #default="{ row }">
                        <span>{{ row.type_text }}</span>
                        <span v-if="row?.pt_open?.status_text">
                            ({{ row?.pt_open?.status_text }})
                        </span>
                    </template>
                </el-table-column>
                <el-table-column prop="nickname" label="买家昵称" width="130" />
                <el-table-column prop="goods" label="商品信息" min-width="220">
                    <template #default="{ row }">
                        <router-link
                            v-for="(item, index) in row.goods"
                            :key="index"
                            :to="`/goods/lists`"
                            v-show="index <= 2"
                        >
                            <div class="flex mb-[5px]">
                                <image-contain
                                    class="flex-none"
                                    :src="item.goods_image"
                                    :width="70"
                                    :height="70"
                                    :preview-src-list="[item.goods_image]"
                                    preview-teleported
                                    fit="contain"
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
                                    <div class="black">
                                        ¥{{ item.goods_price }} &nbsp;&nbsp;x {{ item.goods_num }}
                                    </div>
                                </div>
                            </div>
                        </router-link>
                        <el-button
                            type="primary"
                            @click="handleMore('all_goods', row)"
                            link
                            v-if="row.goods.length > 2"
                        >
                            查看更多
                            <el-icon>
                                <ArrowRight />
                            </el-icon>
                        </el-button>
                    </template>
                </el-table-column>
                <el-table-column prop="need_pay_money" label="实付金额" min-width="160">
                    <template #default="{ row }">
                        <div>¥{{ row.need_pay_money }}</div>
                        <div v-if="row.express_money != '0.00'" class="text-[#979797] mt-2">
                            (含运费：¥{{ row.express_money }})
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="date" label="收货人" min-width="90">
                    <template #default="{ row }">
                        <el-popover placement="top" width="300px" trigger="hover">
                            <div>收件人: {{ row.address_contact }}</div>
                            <div class="mt-[20px]">手机号: {{ row.address_mobile }}</div>
                            <div class="mt-[20px]">
                                地&nbsp;&nbsp;&nbsp;址: {{ row.address_content }}
                            </div>
                            <template #reference>
                                <el-tag>{{ row.address_contact }}</el-tag>
                            </template>
                        </el-popover>
                    </template>
                </el-table-column>
                <el-table-column label="支付状态" min-width="100">
                    <template #default="{ row }">
                        <div v-if="row.pay_is">已支付</div>
                        <div v-else class="text-error">未支付</div>
                    </template>
                </el-table-column>
                <el-table-column prop="status_text" label="订单状态" min-width="100" />
                <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                        <el-button v-perms="['mall.order/detail']" type="primary" link>
                            <router-link
                                :to="{
                                    path: getRoutePath('mall.order/detail'),
                                    query: {
                                        id: row.id
                                    }
                                }"
                            >
                                订单详情
                            </router-link>
                        </el-button>

                        <!-- <el-button v-perms="['mall.Order/confirm']" v-if="row.can_confirm === 1" type="primary" link @click="handleMore('confirm', row)">
                            确认订单
                        </el-button>

                        <el-button v-perms="['mall.Order/send']" v-if="row.status === 1" type="primary" link @click="handleMore('send', row)">
                            去发货
                        </el-button>

                        <el-button v-perms="['mall.order/logistics']" v-if="row.can_see_logistics" type="primary" link @click="handleMore('delivery', row)">
                            查看物流
                        </el-button> -->

                        <el-dropdown @command="handleMore($event, row)">
                            <el-button type="primary" link class="mt-[2px] mx-2">
                                更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
                            </el-button>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item command="cancel" v-if="row.can_cancel"
                                        >取消订单</el-dropdown-item
                                    >
                                    <el-dropdown-item command="confirm" v-if="row.can_confirm"
                                        >确认收货</el-dropdown-item
                                    >
                                    <el-dropdown-item command="send" v-if="row.status === 1"
                                        >去发货</el-dropdown-item
                                    >
                                    <el-dropdown-item
                                        command="delivery"
                                        v-if="row.can_see_logistics"
                                        >查看物流</el-dropdown-item
                                    >
                                    <el-dropdown-item @click="hanldePrinte(row.id)"
                                        >小票打印</el-dropdown-item
                                    >
                                    <el-dropdown-item command="remark">订单备注</el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </template>
                </el-table-column>
            </el-table>
            <div class="flex justify-end mt-4">
                <pagination v-model="pager" @change="getLists" />
            </div>
        </el-card>

        <!-- Component -->
        <!-- 订单备注 -->
        <remark
            ref="remarkRef"
            v-if="showRemark"
            @success="getLists"
            @close="showRemark = false"
        ></remark>
        <!-- 发货 ｜｜ 查看物流 -->
        <logistics
            ref="logisticsRef"
            v-if="showLogistics"
            @success="getLists"
            @close="showLogistics = false"
        ></logistics>
        <!-- 查看全部商品 -->
        <all-goods
            ref="allGoodsRef"
            v-if="showGoodsList"
            @close="showGoodsList = false"
        ></all-goods>
    </div>
</template>
<script lang="ts" setup name="orderLists">
import { usePaging } from '@/hooks/usePaging'
import { getRoutePath } from '@/router'
import type { OrderForm } from '@/api/order/order'
import { getOrderLists, apiOrderCancel, apiOrderConfirm } from '@/api/order/order'
import { ClientMap, PayMap } from '@/enums/appEnums'
import feedback from '@/utils/feedback'
import Remark from './component/remark.vue'
import Logistics from './component/logistics.vue'
import AllGoods from './component/all-goods.vue'
import { apiprint } from '@/api/marketing/ticket'
// Tab类型
enum TabsEnum {
    ALL_ORDER = 'all_count',
    PAY_COUNT = 'pay_count',
    DELIVER_COUNT = 'delivery_count',
    RECEIVE_COUNT = 'receive_count',
    FINISH_COUNT = 'finish_count',
    CANCEL_COUNT = 'close_count'
}

const remarkRef = shallowRef()
const logisticsRef = shallowRef()
const allGoodsRef = shallowRef()

const showRemark = ref(false)
const showLogistics = ref(false)
const showGoodsList = ref(false)
const tabsParams = reactive({
    TabsEnumMap: [
        {
            label: '全部',
            name: '',
            type: TabsEnum.ALL_ORDER
        },
        {
            label: '待支付',
            name: 0,
            type: TabsEnum.PAY_COUNT
        },
        {
            label: '待发货',
            name: 1,
            type: TabsEnum.DELIVER_COUNT
        },
        {
            label: '待收货',
            name: 2,
            type: TabsEnum.RECEIVE_COUNT
        },
        {
            label: '已完成',
            name: 3,
            type: TabsEnum.FINISH_COUNT
        },
        {
            label: '已取消',
            name: 4,
            type: TabsEnum.CANCEL_COUNT
        }
    ]
})

const specParams = reactive<any>({
    selectData: []
})
const hanldePrinte = async (id: number) => {
    await apiprint({ id })
    feedback.msgSuccess('打印成功')
}
const queryParams = reactive<OrderForm>({
    status: '',
    key_word: '',
    key_word_type: '1',
    buyer_key_word: '',
    buyer_key_word_type: '1',
    from: '',
    pay_way: '',
    time_type: '',
    start_time: '',
    end_time: ''
})

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: getOrderLists,
    params: queryParams
})

const selectDataChange = (value: any[]) => {
    specParams.selectData = value.map((item) => item.ids)
}

const changeTabs = (name: string) => {
    queryParams.status = name
    getLists()
}

const handleMore = async (event: string, row: any) => {
    switch (event) {
        case 'cancel':
            handleCancel(row.id)
            break
        case 'confirm':
            handleConfirm(row.id)
            break
        case 'remark':
            showRemark.value = true
            await nextTick()
            remarkRef.value.open(row.id, row.shop_remark)
            break
        case 'send':
            showLogistics.value = true
            await nextTick()
            logisticsRef.value.open('send', row.id)
            break
        case 'delivery':
            showLogistics.value = true
            await nextTick()
            logisticsRef.value.open('delivery', row.id)
            break
        case 'all_goods':
            showGoodsList.value = true
            await nextTick()
            allGoodsRef.value.open(row.goods)
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

onActivated(() => {
    getLists()
})

getLists()
</script>
