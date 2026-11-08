<template>
    <div class="edit-popup">
        <popup
            ref="popupRef"
            :title="popupTitle"
            :async="true"
            width="1180px"
            :confirmButtonText="popupConfirmText"
            @confirm="handleSubmit"
            @close="handleClose"
        >
            <!-- 商品信息 -->
            <div class="my-4 text-xl font-medium">商品信息</div>
            <!-- Component -->
            <goods-table :goods="order.goods"></goods-table>

            <!-- 去发货 -->
            <template v-if="mode == 'send'">
                <!-- 收货信息 -->
                <div class="my-4 text-xl font-medium">收货信息</div>
                <div class="my-[30px] leading-9">
                    <div>收货人: {{ order.address_contact }}</div>
                    <div>手机号码: {{ order.address_mobile }}</div>
                    <div class="flex">
                        <div class="mr-1">收货地址:</div>
                        <div class="flex-1">{{ order.address_content }}</div>
                    </div>
                </div>

                <!-- 物流配送 -->
                <div class="my-4 text-xl font-medium">物流配送</div>
                <el-form ref="formRef" :model="formData" label-width="84px" :rules="formRules">
                    <el-form-item label="发货方式" prop="has_express">
                        <el-radio-group v-model="formData.has_express">
                            <el-radio :label="1" size="large">需要物流</el-radio>
                            <el-radio :label="0" size="large">无需物流</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item label="物流公司" prop="express_no" v-if="formData.has_express">
                        <div class="pr-4 md:flex-1 phone:block sm:flex">
                            <el-select
                                v-model="formData.express_company_id"
                                placeholder="选择"
                                class="phone:mb-4 phone:w-full sm:w-[40%]"
                            >
                                <el-option
                                    v-for="sitem in delivery.lists"
                                    :key="sitem.id"
                                    :label="sitem.name"
                                    :value="sitem.id"
                                />
                            </el-select>
                            <el-input
                                v-model="formData.express_no"
                                placeholder="请输入"
                                class="w-full phone:mb-4"
                                @keyup.enter="resetPage"
                            >
                            </el-input>
                        </div>
                    </el-form-item>
                </el-form>
            </template>

            <template v-else>
                <!-- 物流信息 -->
                <div class="my-4 text-xl font-medium">物流信息</div>

                <template v-if="order.express_company_id != 0">
                    <div class="my-[30px] leading-9">
                        <div>快递公司: {{ order.express_company.name }}</div>
                        <div>快递单号: {{ order.express_no }}</div>
                        <div>发货时间: {{ order.express_time }}</div>
                    </div>

                    <!-- 物流轨迹 -->
                    <div class="my-4 text-xl font-medium">物流轨迹</div>
                    <el-table size="large" v-if="order.logistics.length" :data="order.logistics">
                        <el-table-column prop="0" label="日期" width="220" />
                        <el-table-column prop="1" label="轨迹" min-width="220" />
                    </el-table>
                    <div v-else class="flex nr weight-500 m-t-60 row-center">
                        {{ order.logistics_error }}
                    </div>
                </template>
                <template v-else>
                    <div class="my-[30px] leading-9">
                        <div>快递方式: 无需物流</div>
                    </div>
                </template>
            </template>
        </popup>
    </div>
</template>
<script lang="ts" setup>
import {
    getOrderDetail,
    apiOrderDelivery,
    apiOrderLogistics,
    apiAftersale
} from '@/api/order/order'
import { getExpressCompanyLists } from '@/api/setting/delivery'
import type { DeliveryType } from '@/api/order/order'
import Popup from '@/components/popup/index.vue'
import GoodsTable from './goods-table.vue'
import type { FormInstance, FormRules } from 'element-plus'
import feedback from '@/utils/feedback'

const emit = defineEmits(['success', 'close'])

const formRef = shallowRef<FormInstance>()
const popupRef = shallowRef<InstanceType<typeof Popup>>()
const mode = ref('send')
const order = reactive<any>({
    goods: [],
    // 发货
    address_contact: '',
    address_content: '',
    address_mobile: '',

    // 查询物流
    logistics: [],
    express_no: '',
    express_time: '',
    express_company_id: '',
    express_company: {
        name: ''
    },
    logistics_error: ''
})
// 快递公司
const delivery = reactive<any>({
    lists: []
})
// 发货表单
const formData = reactive<DeliveryType>({
    id: '',
    has_express: 1,
    express_no: '',
    express_company_id: ''
})
// 发货规则
const formRules = reactive<FormRules>({
    has_express: [{ required: true, message: '请选择物流方式', trigger: ['blur'] }],
    express_no: [{ required: true, message: '请输入快递单号', trigger: ['blur'] }]
})

const popupTitle = computed(() => {
    return mode.value == 'send' ? '发货' : '查看物流'
})
const popupConfirmText = computed(() => {
    return mode.value == 'send' ? '发货' : '确认'
})

const handleSubmit = async () => {
    if (mode.value == 'delivery') {
        popupRef.value?.close()
        return
    }
    const { after_sale } = await apiAftersale({
        id: formData.id
    })
    after_sale.order_goods = []
    const no_can_send = after_sale?.order_goods.map((item: any) => {
        if (item.after_sale !== 0) {
            return item.goods_name
        }
    })
    if (no_can_send.length) {
        feedback.confirm(`商品：（${no_can_send.join(',')}），当前处于售后中，请前往处理`)
        // console.log(no_can_send)
    } else {
        await formRef.value?.validate()
        await apiOrderDelivery(formData)
    }
    await formRef.value?.resetFields()
    popupRef.value?.close()

    emit('success')
}

const open = (type = 'send', id: number) => {
    formData.id = id
    mode.value = type
    getDetail()
    popupRef.value?.open()
}

const getDetail = async () => {
    if (mode.value == 'send') {
        const data: any = await getOrderDetail({
            id: formData.id as number
        })
        Reflect.ownKeys(order).map((item: any) => {
            if (!data[item]) return
            order[item] = data[item]
        })
        const lists = await getExpressCompanyLists()
        delivery.lists = lists
    } else {
        const data = await apiOrderLogistics({ id: formData.id as number })
        Reflect.ownKeys(order).map((item: any) => {
            if (!data[item]) return
            order[item] = data[item]
        })
    }
}

const handleClose = () => {
    formRef.value?.resetFields()
    emit('close')
}

defineExpose({
    open
})
</script>
