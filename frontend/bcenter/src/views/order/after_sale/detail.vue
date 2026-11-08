<template>
    <div class="order-detail">
        <el-form class="demo-form-inline">
            <el-card class="!border-none" shadow="never">
                <el-page-header :content="$route.meta.title" @back="$router.back()" />
            </el-card>

            <el-card class="!border-none mt-4" shadow="never">
                <div class="pb-4 mb-4 text-lg font-medium" style="border-bottom: 1px solid #eeeeee">
                    售后信息
                </div>

                <div class="flex">
                    <div class="w-2/5">
                        <el-form-item label="退款状态">
                            {{ after_sale.status_text || '-' }}
                        </el-form-item>
                        <el-form-item label="申请时间">
                            {{ after_sale.create_time }}
                        </el-form-item>
                        <el-form-item label="退款金额">
                            <span class="text-[#fd3e47]"
                                >¥{{ after_sale.refund_money || '-' }}</span
                            >
                            <el-tooltip
                                class="box-item"
                                effect="dark"
                                content="未发货的订单，用户申请售后可以退回运费；已经发货的订单，用户申请售后不退回运费"
                                placement="top-start"
                            >
                                <el-icon size="18px" class="ml-1 text-[#ccc]"
                                    ><QuestionFilled
                                /></el-icon>
                            </el-tooltip>
                        </el-form-item>
                        <el-form-item label="退款说明">
                            <div class="break-all">
                                {{ after_sale.refund_content || '-' }}
                            </div>
                        </el-form-item>
                    </div>
                    <div class="w-3/5">
                        <el-form-item label="售后单号">
                            {{ after_sale.sn }}
                        </el-form-item>
                        <el-form-item label="退款方式">
                            {{ after_sale.type_text || '-' }}
                        </el-form-item>
                        <el-form-item label="退款原因">
                            {{ after_sale.refund_reason_text || '-' }}
                        </el-form-item>
                        <el-form-item label="退款凭证">
                            <template v-for="Img in after_sale.refund_image" :key="Img">
                                <image-contain
                                    class="flex-none mr-2"
                                    :src="Img"
                                    :width="44"
                                    :height="44"
                                    :preview-src-list="[...after_sale.refund_image]"
                                    preview-teleported
                                    fit="cover"
                                />
                            </template>
                        </el-form-item>
                        <!-- <el-form-item label="退款状态">
                            {{ '-' }}
                        </el-form-item> -->
                    </div>
                </div>

                <div class="pt-4" style="border-top: 1px solid #eeeeee">
                    <el-button
                        v-if="after_sale.sub_status == 14"
                        type="primary"
                        @click="handleaccepyExpress()"
                    >
                        同意退款
                    </el-button>
                    <el-button
                        type="primary"
                        @click="handleaccepyExp('accept')"
                        v-if="after_sale.sub_status == 13"
                    >
                        确认收货
                    </el-button>
                    <el-button
                        @click="handleaccepyExp('refuse')"
                        v-if="after_sale.sub_status == 13"
                    >
                        拒绝收货
                    </el-button>
                    <el-button
                        v-if="after_sale.can_audit"
                        type="primary"
                        @click="handleMore('accept')"
                    >
                        同意售后
                    </el-button>

                    <el-button
                        v-if="after_sale.can_audit"
                        type="danger"
                        @click="handleMore('refuse')"
                    >
                        拒绝售后
                    </el-button>

                    <el-button
                        v-if="after_sale.can_refund"
                        type="primary"
                        @click="handleMore('refund')"
                    >
                        确认退款
                    </el-button>
                </div>
            </el-card>
            <el-card class="!border-none mt-4" shadow="never">
                <div class="pb-4 mb-4 text-lg font-medium" style="border-bottom: 1px solid #eeeeee">
                    订单信息
                </div>

                <div class="flex">
                    <div class="w-2/5">
                        <el-form-item label="订单状态">
                            {{ after_sale.order_info?.status_text || '-' }}
                        </el-form-item>
                        <el-form-item label="订单编号">
                            {{ after_sale.order_info?.sn }}
                        </el-form-item>
                        <el-form-item label="订单类型">
                            {{ after_sale.order_info?.from_text || '-' }}
                        </el-form-item>
                        <el-form-item label="买家信息">
                            <router-link
                                class="text-primary"
                                :to="`/consumer/lists/detail?id=${after_sale.user.id}`"
                            >
                                {{ after_sale.user.nickname }}({{ after_sale.user.sn }})
                            </router-link>
                        </el-form-item>
                        <el-form-item label="支付时间">
                            {{ after_sale.order_info?.pay_time || '-' }}
                        </el-form-item>
                        <el-form-item label="用户备注">
                            <div class="break-all">
                                {{ after_sale.order_info?.user_remark || '-' }}
                            </div>
                        </el-form-item>
                    </div>
                    <div class="w-3/5">
                        <el-form-item label="支付状态">
                            <span v-if="after_sale.order_info?.pay_is">已支付</span>
                            <span v-else class="text-error">未支付</span>
                        </el-form-item>
                        <el-form-item label="支付方式">
                            {{ after_sale.order_info?.pay_way_text || '-' }}
                        </el-form-item>
                        <el-form-item label="订单来源">
                            {{ after_sale.order_info?.from_text || '-' }}
                        </el-form-item>
                        <el-form-item label="下单时间">
                            {{ after_sale.order_info?.create_time }}
                        </el-form-item>
                        <el-form-item label="完成时间">
                            {{ after_sale.order_info?.express_confirm_time || '-' }}
                        </el-form-item>
                        <el-form-item label="商家备注">
                            <div class="break-all">
                                {{ after_sale.order_info?.shop_remark }}
                            </div>
                        </el-form-item>
                    </div>
                </div>
            </el-card>
            <el-card class="!border-none mt-4" shadow="never" v-if="after_sale.return_express_no">
                <div class="pb-4 mb-4 text-lg font-medium" style="border-bottom: 1px solid #eeeeee">
                    退货信息
                </div>
                <el-form-item label="物流公司">
                    {{ after_sale.return_express_company || '-' }}
                </el-form-item>
                <el-form-item label="物流单号">
                    {{ after_sale.return_express_no || '-' }}
                </el-form-item>
                <el-form-item label="备注">
                    {{ after_sale.return_remark || '-' }}
                </el-form-item>
                <el-form-item label="退款凭证">
                    <!-- <el-image
                        :src="item"
                        v-for="item in after_sale.return_image"
                        :key="item"
                        class="w-[50px] ml-2"
                    ></el-image> -->
                    <image-contain
                        class="flex-none mr-2"
                        v-for="item in after_sale.return_image"
                        :src="item"
                        :width="44"
                        :height="44"
                        :preview-src-list="after_sale.return_image"
                        preview-teleported
                        fit="cover"
                        :key="item"
                    />
                </el-form-item>
                <el-form-item label="退款时间">
                    {{ after_sale.return_time || '-' }}
                </el-form-item>
            </el-card>
            <el-card class="!border-none mt-4" shadow="never">
                <div class="pb-4 mb-4 text-lg font-medium" style="border-bottom: 1px solid #eeeeee">
                    退款商品
                </div>
                <!-- 商品表格 -->
                <goods-table :goods="after_sale.order_goods"></goods-table>
            </el-card>

            <el-card class="!border-none mt-4" shadow="never">
                <div class="pb-4 mb-4 text-lg font-medium" style="border-bottom: 1px solid #eeeeee">
                    订单日志
                </div>
                <!-- 操作日志表哥 -->
                <el-table size="large" :data="after_sale.log">
                    <el-table-column prop="operator" label="操作人" min-width="100" />
                    <el-table-column prop="content" label="操作内容" min-width="100" />
                    <el-table-column prop="create_time" label="记录日期" min-width="100" />
                </el-table>
            </el-card>
        </el-form>

        <!-- Component -->
        <!-- 审核售后 -->
        <audit ref="auditRef" @success="getDetails"></audit>
        <express ref="ExpressRef" @success="getDetails"></express>
        <!-- 确认退款 -->
        <refund ref="refundRef" @success="getDetails"></refund>
    </div>
</template>

<script lang="ts" setup name="OrderDetail">
import { usePaging } from '@/hooks/usePaging'
import { getRoutePath } from '@/router'
import {
    getAfterSaleDetail,
    apiAfterSaleAudit,
    goodsReceive,
    acceptgoodsReceive
} from '@/api/order/after_sale'
import { ClientMap } from '@/enums/appEnums'
import feedback from '@/utils/feedback'
import Audit from './component/audit.vue'
import Refund from './component/refund.vue'
import Express from './component/express.vue'
import GoodsTable from './component/goods-table.vue'

const route = useRoute()
const auditRef = shallowRef()
const ExpressRef = shallowRef()
const refundRef = shallowRef()
const after_sale = reactive<any>({
    id: '',
    sn: '',
    create_time: '',
    order_info: {},
    refund_content: '',
    refund_image: [],
    type_text: '',
    refund_reason_text: '',
    refund_money: '',
    order_goods: [],
    log: [],
    user: {
        id: '',
        nickname: '',
        sn: ''
    },
    status_text: '已取消',
    update_time: '',
    can_audit: 0,
    can_refund: 0,
    return_express_company: '',
    return_express_no: '',
    return_remark: '',
    sub_status: '',
    return_time: '',
    return_image: []
})

const getDetails = async () => {
    const data = await getAfterSaleDetail({
        //@ts-ignore
        id: route.query.id
    })
    Reflect.ownKeys(after_sale).map((item: any) => {
        after_sale[item] = data[item]
    })
}
const handleaccepyExp = async (event: string) => {
    switch (event) {
        // queren退款
        case 'accept':
            await feedback.confirm('确定要同意收货该订单吗？')
            await goodsReceive({ id: after_sale.id, agree: 1 })
            getDetails()
            break
        // 拒绝退款
        case 'refuse':
            const refuse = {
                id: after_sale.id,
                refuse_content: ''
            }
            ExpressRef.value.open(refuse)
            break
    }
}
const handleMore = (event: string) => {
    switch (event) {
        // 接受退款
        case 'accept':
            handleAccept(after_sale.id)
            break
        // 拒绝退款
        case 'refuse':
            const refuse = {
                id: after_sale.id,
                refuse_content: ''
            }
            auditRef.value.open(refuse)
            break
        // 确认退款
        case 'refund':
            const refund = {
                id: after_sale.id,
                type_text: after_sale.type_text,
                refund_money: after_sale.refund_money
            }
            refundRef.value.open(refund)
            break
    }
}
const handleaccepyExpress = async () => {
    await feedback.confirm('确定要同意退款该订单吗？')
    await acceptgoodsReceive({ id: after_sale.id })
    getDetails()
}
const handleAccept = async (id: number) => {
    try {
        await feedback.confirm('确定要同意退款该订单吗？')
        await apiAfterSaleAudit({ id, agree: 1 })
        getDetails()
    } catch (error) {
        console.log('取消失败=>', error)
    }
}

getDetails()
</script>
