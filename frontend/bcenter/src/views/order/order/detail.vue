<template>
    <div class="order-detail">
        <el-form class="demo-form-inline">
            <el-card class="!border-none" shadow="never">
                <el-page-header :content="$route.meta.title" @back="handleRouteBack" />
            </el-card>

            <el-card class="!border-none mt-4" shadow="never">
                <div class="pb-4 mb-4 text-lg font-medium" style="border-bottom: 1px solid #eeeeee">
                    订单信息
                </div>

                <div class="flex">
                    <div class="w-2/5">
                        <el-form-item label="订单状态">
                            {{ order.status_text || '-' }}
                        </el-form-item>
                        <el-form-item label="订单编号">
                            {{ order.sn }}
                        </el-form-item>
                        <el-form-item label="订单类型">
                            {{ order.type_text || '-' }}
                        </el-form-item>
                        <el-form-item label="订单来源">
                            {{ order.from_text || '-' }}
                        </el-form-item>
                        <el-form-item label="支付时间">
                            {{ order.pay_time }}
                        </el-form-item>
                        <!--                        <el-form-item label="退款状态">-->
                        <!--                            {{ '-' }}-->
                        <!--                        </el-form-item>-->
                        <el-form-item label="商家备注">
                            {{ order.shop_remark || '-' }}
                        </el-form-item>
                    </div>
                    <div class="w-3/5">
                        <el-form-item label="支付状态">
                            <span v-if="order.pay_is">已支付</span>
                            <span v-else class="text-error">未支付</span>
                        </el-form-item>
                        <el-form-item label="支付方式">
                            {{ order.pay_way_text || '-' }}
                        </el-form-item>
                        <el-form-item label="买家信息">
                            <router-link
                                class="text-primary"
                                :to="`/consumer/lists/detail?id=${order.user.id}`"
                            >
                                {{ order.user.nickname }}({{ order.user.sn }})
                            </router-link>
                        </el-form-item>
                        <el-form-item label="下单时间">
                            {{ order.create_time || '-' }}
                        </el-form-item>
                        <el-form-item label="完成时间">
                            {{ order.express_confirm_time || '-' }}
                        </el-form-item>
                        <el-form-item label="用户备注">
                            <div class="break-all">
                                {{ order.user_remark }}
                            </div>
                        </el-form-item>
                    </div>
                </div>

                <div class="pt-4" style="border-top: 1px solid #eeeeee">
                    <el-button v-if="order.can_cancel" type="danger" @click="handleMore('cancel')">
                        取消订单
                    </el-button>

                    <el-button
                        v-if="order.can_confirm"
                        type="primary"
                        @click="handleMore('confirm')"
                    >
                        确认收货
                    </el-button>

                    <el-button v-if="order.status === 1" type="primary" @click="handleMore('send')">
                        去发货
                    </el-button>

                    <el-button
                        v-if="order.can_see_logistics"
                        type="primary"
                        @click="handleMore('delivery')"
                    >
                        查看物流
                    </el-button>

                    <el-button type="default" @click="handleMore('remark')"> 商家备注 </el-button>
                </div>
            </el-card>

            <el-card class="!border-none mt-4" shadow="never">
                <div class="pb-4 mb-4 text-lg font-medium" style="border-bottom: 1px solid #eeeeee">
                    收货及物流信息
                </div>

                <div class="flex">
                    <div class="w-2/5">
                        <el-form-item label="配送方式">
                            {{ order.delivery_type_text || '-' }}
                        </el-form-item>
                        <el-form-item label="收货人">
                            {{ order.address_contact || '-' }}
                        </el-form-item>
                        <el-form-item label="手机号码">
                            {{ order.address_mobile || '-' }}
                        </el-form-item>
                        <el-form-item label="收货地址">
                            {{ order.address_content || '-' }}
                        </el-form-item>
                    </div>
                    <div class="w-3/5">
                        <el-form-item label="发货状态">
                            {{ order.express_is_text || '-' }}
                        </el-form-item>
                        <el-form-item label="物流公司">
                            {{ order.express_company?.name || '-' }}
                        </el-form-item>
                        <el-form-item label="物流单号">
                            {{ order.express_no || '-' }}
                        </el-form-item>
                        <el-form-item label="发货时间">
                            {{ order.express_time || '-' }}
                        </el-form-item>
                    </div>
                </div>
            </el-card>

            <el-card class="!border-none mt-4" shadow="never">
                <div class="pb-4 mb-4 text-lg font-medium" style="border-bottom: 1px solid #eeeeee">
                    商品信息
                </div>
                <!-- 商品表格 -->
                <goods-table :goods="order.goods" :coupon-name="order.coupon_name"></goods-table>
                <!-- 商品总结 -->
                <div class="mt-4 mr-[30px] text-right">
                    <div class="font-medium mb-4">
                        <span class="text-xl">商品总额:</span>
                        <span class="text-xl">¥{{ order.goods_money }}</span>
                    </div>
                    <div class="font-medium mb-4">
                        <span class="text-xl">优惠金额:</span>
                        <span class="text-xl text-error">¥{{ order.dec_money }}</span>
                    </div>
                    <div class="font-medium mb-4">
                        <span class="text-xl">运费:</span>
                        <span class="text-xl">¥{{ order.express_money }}</span>
                    </div>
                    <div class="font-medium">
                        <span class="text-xl">实际支付:</span>
                        <span class="text-xl">¥{{ order.need_pay_money }}</span>
                    </div>
                    <!-- <div class="text-[#979797] mt-2">运费：¥{{ order.express_money }}</div> -->
                </div>
            </el-card>

            <el-card class="!border-none mt-4" shadow="never">
                <div class="pb-4 mb-4 text-lg font-medium" style="border-bottom: 1px solid #eeeeee">
                    订单日志
                </div>
                <!-- 操作日志表哥 -->
                <el-table size="large" :data="order.log">
                    <el-table-column prop="operator" label="操作人" min-width="100" />
                    <el-table-column prop="content" label="操作内容" min-width="100" />
                    <el-table-column prop="create_time" label="记录日期" min-width="100" />
                </el-table>
            </el-card>
        </el-form>

        <!-- Component -->
        <!-- 订单备注 -->
        <remark ref="remarkRef" @success="getDetails"></remark>
        <!-- 发货 ｜｜ 查看物流 -->
        <logistics ref="logisticsRef" @success="getDetails"></logistics>
    </div>
</template>

<script lang="ts" setup name="OrderDetail">
import { usePaging } from '@/hooks/usePaging'
import { getRoutePath } from '@/router'
import { getOrderDetail, apiOrderCancel, apiOrderConfirm, apiAftersale } from '@/api/order/order'
import { ClientMap } from '@/enums/appEnums'
import feedback from '@/utils/feedback'
import Remark from './component/remark.vue'
import Logistics from './component/logistics.vue'
import GoodsTable from './component/goods-table.vue'
import useMultipleTabs from '@/hooks/useMultipleTabs'
const { removeTab } = useMultipleTabs()

const route = useRoute()
const router = useRouter()
const remarkRef = shallowRef()
const logisticsRef = shallowRef()
const order = reactive<any>({
    id: '',
    create_time: '',
    goods_money: '',
    sn: '',
    status: 4,
    pay_is: 1,
    discount_money: '',
    pay_time: '',
    coupon_name: '',
    address_contact: '',
    address_mobile: '',
    address_content: '',
    delivery_type_text: '',
    dec_money: '',
    express_money: '',
    need_pay_money: '',
    express_is_text: '',
    express_time: '',
    express_no: '',
    express_confirm_time: '',
    user_remark: '',
    shop_remark: '',
    from_text: '',
    pay_way_text: '',
    goods: [],
    log: [],
    user: {
        id: '',
        nickname: '',
        sn: ''
    },
    express_company: null,
    can_cancel: 0,
    status_text: '已取消',
    can_see_logistics: 0,
    can_confirm: 0,
    type_text: ''
})

const getDetails = async () => {
    const data = await getOrderDetail({
        //@ts-ignore
        id: route.query.id
    })
    Reflect.ownKeys(order).map((item: any) => {
        order[item] = data[item]
    })
}

const handleMore = async (event: string) => {
    switch (event) {
        case 'cancel':
            handleCancel(order.id)
            break
        case 'confirm':
            handleConfirm(order.id)
            break
        case 'remark':
            remarkRef.value.open(order.id, order.shop_remark)
            break
        case 'send':
            // await apiAftersale(order.id)
            logisticsRef.value.open('send', order.id)
            break
        case 'delivery':
            logisticsRef.value.open('delivery', order.id)
            break
    }
}

const handleCancel = async (id: number) => {
    try {
        await feedback.confirm('确定要取消订单吗？')
        await apiOrderCancel({ id })
        getDetails()
    } catch (error) {
        console.log('取消失败=>', error)
    }
}

const handleConfirm = async (id: number) => {
    try {
        await feedback.confirm('确认用户已收到货吗？')
        await apiOrderConfirm({ id })
        getDetails()
    } catch (error) {
        console.log('确认失败=>', error)
    }
}

const handleRouteBack = () => {
    removeTab()
    router.back()
}

getDetails()
</script>
