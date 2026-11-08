<template>
    <div class="workbench">
        <header class="workbench-hero">
            <div>
                <h1>经营工作台</h1>
                <p>掌握店铺今日表现，优先处理订单、商品与库存事项。</p>
            </div>
            <span>{{ workbenchData.now || '数据正在更新' }}</span>
        </header>
        <div class="lg:flex">
            <el-card class="!border-none mb-4 md:mr-4" shadow="never">
                <template #header>
                    <div class="flex items-center">
                        <image-contain
                            class="flex-none mr-4"
                            :src="workbenchData?.shop?.logo"
                            :width="48"
                            :height="48"
                            radius="50%"
                            :preview-src-list="[workbenchData?.shop?.logo]"
                            preview-teleported
                            fit="cover"
                        />
                        <div>
                            <div class="flex items-center card-title">
                                {{ workbenchData.shop.name }}
                            </div>
                            <div class="text-xs text-[#999999]">
                                <!-- 开通时间：{{ workbenchData.shop.create_time }} -->
                            </div>
                        </div>
                    </div>
                </template>
                <div class="md:w-[400px]">
                    <div class="flex leading-9">
                        <div class="w-20">当前版本</div>
                        <div>
                            {{ workbenchData.version }}
                        </div>
                    </div>
                    <div class="flex leading-9">
                        <div class="w-20">开通时间：</div>
                        <div>
                            {{ workbenchData.shop.create_time }}
                        </div>
                        <!-- <a
                            href="https://likeadmin.cn"
                            target="_blank"
                        >
                            <el-button type="primary" size="small">
                                官网
                            </el-button>
                        </a>
                        <a
                            class="ml-2"
                            href="https://likeadmin.cn"
                            target="_blank"
                        >
                            <el-button type="danger" size="small">
                                开发文档
                            </el-button>
                        </a> -->
                    </div>
                </div>
            </el-card>
            <el-card class="!border-none mb-4 flex-1" shadow="never">
                <template #header>
                    <div>
                        <span class="card-title">今日数据</span>
                        <span class="ml-4 text-xs text-tx-secondary">
                            更新时间：{{ workbenchData.now }}
                        </span>
                    </div>
                </template>

                <div class="flex flex-wrap">
                    <div class="w-1/2 md:w-1/4">
                        <div class="leading-10">今日收入</div>
                        <div class="text-4xl">
                            {{ workbenchData.today_data.today_sale }}
                        </div>
                        <div class="text-base">
                            <span class="text-[#111111]"
                                >昨日: {{ workbenchData.today_data.yesterday_sale }}</span
                            >
                            <span
                                class="ml-3 text-[#ff2735]"
                                v-if="
                                    workbenchData.today_data.today_sale >
                                    workbenchData.today_data.yesterday_sale
                                "
                            >
                                涨:
                                {{
                                    (
                                        workbenchData.today_data.today_sale -
                                        workbenchData.today_data.yesterday_sale
                                    ).toFixed(2)
                                }}
                            </span>
                            <span class="ml-3 text-[#389e0d]" v-else>
                                降:
                                {{
                                    (
                                        workbenchData.today_data.yesterday_sale -
                                        workbenchData.today_data.today_sale
                                    ).toFixed(2)
                                }}
                            </span>
                        </div>
                    </div>
                    <div class="w-1/2 md:w-1/4">
                        <div class="leading-10">订单量</div>
                        <div class="text-4xl">
                            {{ workbenchData.today_data.today_orders }}
                        </div>
                        <div class="text-base">
                            <span class="text-[#111111]"
                                >昨日: {{ workbenchData.today_data.yesterday_orders }}</span
                            >
                            <span
                                class="ml-3 text-[#ff2735]"
                                v-if="
                                    workbenchData.today_data.today_orders >
                                    workbenchData.today_data.yesterday_orders
                                "
                            >
                                涨:
                                {{
                                    (
                                        workbenchData.today_data.today_orders -
                                        workbenchData.today_data.yesterday_orders
                                    ).toFixed(2)
                                }}
                            </span>
                            <span class="ml-3 text-[#389e0d]" v-else>
                                降:
                                {{
                                    (
                                        workbenchData.today_data.yesterday_orders -
                                        workbenchData.today_data.today_orders
                                    ).toFixed(2)
                                }}
                            </span>
                        </div>
                    </div>
                    <div class="w-1/2 md:w-1/4">
                        <div class="leading-10">进店人数</div>
                        <div class="text-4xl">
                            {{ workbenchData.today_data.today_shop_visit }}
                        </div>
                        <div class="text-base">
                            <span class="text-[#111111]"
                                >昨日: {{ workbenchData.today_data.yesterday_shop_visit }}</span
                            >
                            <span
                                class="ml-3 text-[#ff2735]"
                                v-if="
                                    workbenchData.today_data.today_shop_visit >
                                    workbenchData.today_data.yesterday_shop_visit
                                "
                            >
                                涨:
                                {{
                                    (
                                        workbenchData.today_data.today_shop_visit -
                                        workbenchData.today_data.yesterday_shop_visit
                                    ).toFixed(2)
                                }}
                            </span>
                            <span class="ml-3 text-[#389e0d]" v-else>
                                降:
                                {{
                                    (
                                        workbenchData.today_data.yesterday_shop_visit -
                                        workbenchData.today_data.today_shop_visit
                                    ).toFixed(2)
                                }}
                            </span>
                        </div>
                    </div>
                    <div class="w-1/2 md:w-1/4">
                        <div class="leading-10">商品浏览人数</div>
                        <div class="text-4xl">
                            {{ workbenchData.today_data.today_goods_visit }}
                        </div>
                        <div class="text-base">
                            <span class="text-[#111111]"
                                >昨日: {{ workbenchData.today_data.yesterday_goods_visit }}</span
                            >
                            <span
                                class="ml-3 text-[#ff2735]"
                                v-if="
                                    workbenchData.today_data.today_goods_visit >
                                    workbenchData.today_data.yesterday_goods_visit
                                "
                            >
                                涨:
                                {{
                                    (
                                        workbenchData.today_data.today_goods_visit -
                                        workbenchData.today_data.yesterday_goods_visit
                                    ).toFixed(2)
                                }}
                            </span>
                            <span class="ml-3 text-[#389e0d]" v-else>
                                降:
                                {{
                                    (
                                        workbenchData.today_data.yesterday_goods_visit -
                                        workbenchData.today_data.today_goods_visit
                                    ).toFixed(2)
                                }}
                            </span>
                        </div>
                    </div>
                </div>
            </el-card>
        </div>
        <div class="lg:flex">
            <el-card class="!border-none flex-1 mb-4 md:mr-4" shadow="never">
                <template #header>
                    <div>
                        <span class="card-title">待办事项</span>
                    </div>
                </template>

                <div class="flex flex-1">
                    <div class="w-1/3 text-center">
                        <div class="text-4xl">
                            {{ workbenchData.wait_work.wait_send }}
                        </div>
                        <div class="leading-10">待发货</div>
                    </div>
                    <div class="w-1/3 text-center">
                        <div class="text-4xl">
                            {{ workbenchData.wait_work.wait_audit }}
                        </div>
                        <div class="leading-10">待审核商品</div>
                    </div>
                    <!-- <div class="w-1/4 text-center">
                        <div class="text-4xl">
                            {{ workbenchData.wait_work.wait_reply }}
                        </div>
                        <div class="leading-10">待审核评价</div>
                    </div> -->
                    <div class="w-1/3 text-center">
                        <div class="text-4xl text-[#ff0000]">
                            {{ workbenchData.wait_work.goods_stock_warning }}
                        </div>
                        <div class="leading-10">库存预警</div>
                    </div>
                </div>
            </el-card>
            <el-card class="flex-1 mb-4 !border-none" shadow="never">
                <template #header>
                    <span>常用功能</span>
                </template>
                <div class="flex flex-wrap">
                    <div v-for="item in menu" class="flex flex-col items-center w-1/5" :key="item">
                        <router-link
                            :to="{
                                path: getRoutePath(item.auth)
                            }"
                            class="flex flex-col items-center mb-3"
                        >
                            <image-contain width="40px" height="40px" :src="item?.icon" />
                            <div class="mt-2">{{ item.name }}</div>
                        </router-link>
                    </div>
                </div>
            </el-card>
        </div>
        <div class="md:flex">
            <el-card class="flex-1 !border-none md:mr-4 mb-4" shadow="never">
                <template #header>
                    <span>销售额趋势图</span>
                </template>
                <div>
                    <v-charts
                        style="height: 350px"
                        :option="workbenchData.salesOption"
                        :autoresize="true"
                    />
                </div>
            </el-card>
            <el-card class="flex-1 !border-none mb-4" shadow="never">
                <template #header>
                    <span>商品排行榜(前10个)</span>
                </template>
                <el-table size="large" :data="workbenchData.today_data.goods_sale_rank">
                    <el-table-column prop="index" label="排名" width="90">
                        <template #default="{ $index }">{{ $index + 1 }}</template>
                    </el-table-column>
                    <el-table-column prop="shop" label="商品名称" min-width="220">
                        <template #default="{ row }">
                            <div class="flex items-center">
                                <image-contain
                                    class="flex-none"
                                    :src="row.image"
                                    :width="40"
                                    :height="40"
                                    :preview-src-list="[row.image]"
                                    preview-teleported
                                    fit="contain"
                                />
                                <div class="ml-4 text-base">{{ row.name }}</div>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column prop="sale_money" label="销售金额(元)" width="130" />
                </el-table>
            </el-card>
        </div>
    </div>
</template>

<script lang="ts" setup name="workbench">
import { getWorkbench } from '@/api/app'
import vCharts from 'vue-echarts'
import { getRoutePath } from '@/router'

import GoodsIcon from '@/assets/images/workbench/goods.png'
import DecorationIcon from '@/assets/images/workbench/decoration.png'
import ConsumerIcon from '@/assets/images/workbench/consumer.png'
import OrderIcon from '@/assets/images/workbench/order.png'
import CommentIcon from '@/assets/images/workbench/comment.png'

const menu = ref([
    {
        name: '发布商品',
        auth: 'mall.Goods/add:edit',
        icon: GoodsIcon
    },
    {
        name: '订单管理',
        auth: 'mall.order/lists',
        icon: OrderIcon
    },
    {
        name: '评价管理',
        auth: 'mall.goodsComment/lists',
        icon: CommentIcon
    },
    {
        name: '用户管理',
        auth: 'user.user/lists',
        icon: ConsumerIcon
    },
    {
        name: '装修管理',
        auth: 'decorate.page/detail',
        icon: DecorationIcon
    }
])

// 表单数据
const workbenchData: any = reactive({
    now: '',
    version: '',
    // 待办事项
    wait_work: {
        wait_send: 0,
        wait_audit: 0,
        wait_reply: 0,
        goods_stock_warning: 0
    },
    shop: {
        // 商家数据
        logo: '',
        name: ''
    },
    today_data: {
        // 今日数据
        goods_sale_rank: [],
        today_goods_visit: 0,
        today_orders: 0,
        today_sale: 0,
        today_shop_visit: 0,
        yesterday_goods_visit: 0,
        yesterday_orders: 0,
        yesterday_sale: 0,
        yesterday_shop_visit: 0
    },
    menu: [], // 常用功能

    salesOption: {
        xAxis: {
            type: 'category',
            data: [0]
        },
        yAxis: {
            type: 'value'
        },
        legend: {
            data: ['销售额']
        },
        itemStyle: {
            // 点的颜色。
            color: '#ef4f55'
        },
        tooltip: {
            trigger: 'axis'
        },
        series: [
            {
                name: '销售额',
                data: [0],
                type: 'line',
                smooth: true
            }
        ]
    }
})

// 获取工作台主页数据
const getData = () => {
    getWorkbench()
        .then((res: any) => {
            workbenchData.now = res.now
            workbenchData.version = res.version
            workbenchData.shop = res.shop
            workbenchData.today_data = res.today_data
            workbenchData.wait_work = res.wait_work

            // 清空echarts 数据
            workbenchData.salesOption.xAxis.data = []
            workbenchData.salesOption.series[0].data = []

            res.today_data.sale_7day.date.reverse().forEach((item: any) => {
                workbenchData.salesOption.xAxis.data.push(item)
            })
            res.today_data.sale_7day.list[0].data.forEach((item: any) => {
                workbenchData.salesOption.series[0].data.push(item)
            })
        })
        .catch((err: any) => {
            console.log('err', err)
        })
}

onMounted(() => {
    getData()
})
</script>

<style lang="scss" scoped>
.workbench {
    color: var(--el-text-color-primary);
}

.workbench-hero {
    display: flex;
    min-height: 116px;
    margin-bottom: 16px;
    padding: 24px 28px;
    align-items: center;
    justify-content: space-between;
    gap: 20px;
    overflow: hidden;
    color: #fff;
    background: #23272e;
    border-radius: var(--radius-lg);
    position: relative;
}

.workbench-hero::after {
    position: absolute;
    width: 180px;
    height: 180px;
    right: 56px;
    top: -82px;
    border: 28px solid var(--brand-500);
    border-radius: 50%;
    content: '';
    opacity: .88;
}

.workbench-hero h1 {
    margin: 0;
    color: #fff;
    font-size: 28px;
    letter-spacing: -.03em;
}

.workbench-hero p {
    margin: 8px 0 0;
    color: rgba(255,255,255,.68);
    font-size: 13px;
}

.workbench-hero > span {
    z-index: 1;
    padding: 8px 12px;
    color: rgba(255,255,255,.86);
    background: rgba(255,255,255,.08);
    border: 1px solid rgba(255,255,255,.14);
    border-radius: var(--radius-pill);
    font-size: 12px;
}

.workbench :deep(.el-card) {
    border: 1px solid var(--el-border-color-light) !important;
    box-shadow: 0 8px 24px rgba(29, 32, 38, .035);
}

.workbench :deep(.text-4xl) {
    color: var(--brand-500);
    font-weight: 700;
    letter-spacing: -.03em;
}

.workbench :deep(a:hover) {
    color: var(--brand-600);
}

@media (max-width: 768px) {
    .workbench-hero { min-height: 104px; padding: 20px; }
    .workbench-hero h1 { font-size: 23px; }
    .workbench-hero > span { display: none; }
}
</style>
