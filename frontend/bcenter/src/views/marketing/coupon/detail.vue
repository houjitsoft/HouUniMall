<template>
    <div class="coupon-detail">
        <el-card class="!border-none" shadow="never">
            <el-page-header :content="pageTitle" @back="handleRouteBack" />
        </el-card>

        <el-form ref="formRef" :model="formData" label-width="120px" :rules="rules">
            <template v-for="(compItem, compIndex) in comLists" :key="compIndex">
                <component
                    :is="compItem.comp"
                    :modelValue="formData"
                    :disabled="readOnly"
                ></component>
            </template>
        </el-form>

        <footer-btns>
            <el-button type="primary" @click="handleSave">保存</el-button>
        </footer-btns>
    </div>
</template>

<script lang="ts" setup name="CouponDetail">
import type { FormInstance, FormRules } from 'element-plus'
import { couponDetail, couponEdit, couponRename, couponAdd } from '@/api/marketing/coupon'
import type { CouponFormParamsType } from '@/api/marketing/coupon.d'
import useMultipleTabs from '@/hooks/useMultipleTabs'
import Setting from './component/setting.vue'
import Basic from './component/basic.vue'
import feedback from '@/utils/feedback'

const route = useRoute()
const router = useRouter()

const readOnly: any = route.query.read_only
const comLists = [
    {
        comp: markRaw(Basic)
    },
    {
        comp: markRaw(Setting)
    }
]
const formData = reactive<CouponFormParamsType>({
    id: '', // id
    condition_order_money: '', // 使用条件(订单满多少可用)：condition_money=1时生效
    condition_type: 0, // 使用条件类型: 0=无门槛, 1=订单满足金额
    get_limit: 0, // 领取次数类型: 0=不限制领取次数, 1=限制次数, 2=每天限制数量
    get_limit_num: '', // 限制数量 get_limit=1需要
    get_limit_day_num: '', // 每天限制数量 get_limit=2需要
    get_type: 0, // 领取限制类型: 0=用户领取, 1=商家赠送
    money: '', // 优惠券面额
    name: '', // 优惠券名称
    stock: '', // 发放数量:  send_total_type=2时生效
    stock_type: 0, // 发放数量限制: 0=无限数量,1=固定发放数量
    goods: [], // 使用商品
    use_goods_type: 0, // 适用商品类型: 0=全部商品,1=指定商品,2=指定商品不可用
    use_current_day: '', // 当日开始天内可用: use_time_type=2/3时生效
    use_next_day: '', // 次日开始天内可用: use_time_type=2/3时生效
    use_end_time: '', // 用券结束时间: use_time_type=1时生效
    use_start_time: '', // 用券开始时间: use_time_type=1时生效
    use_time_type: 0 // 用券时间类型: [0=固定时间, 1=领券当天起, 2=领券次日起]
})
const { removeTab } = useMultipleTabs()
const formRef = shallowRef<FormInstance>()
const rules = reactive<FormRules>({
    name: [{ required: true, message: '请输入优惠券名称', trigger: 'change' }],
    stock_type: [
        { required: true, message: '请输入发放数量', trigger: 'change' },
        {
            validator: (rule: object, value: string, callback: any) => {
                if (formData.stock_type == 2 && (formData.stock == '' || formData.stock < 0)) {
                    if (formData.stock == '') return callback(new Error('请输入发放数量'))
                    if (formData.stock < 0) return callback(new Error('发放数量不能为负数'))
                }
                callback()
            },
            trigger: 'blur'
        }
    ],
    use_goods_type: [
        { required: true, message: '请选择使用范围', trigger: 'change' },
        {
            validator: (rule: object, value: string, callback: any) => {
                if (formData.use_goods_type != 0 && !formData.goods.length) {
                    callback(new Error('请选择商品'))
                }
                callback()
            },
            trigger: 'change'
        }
    ],
    condition_type: [{ required: true, message: '请选择使用门槛', trigger: 'change' }],
    money: [{ required: true, message: '请输入优惠券金额', trigger: 'change' }],
    get_type: [{ required: true, message: '请选择领取方式', trigger: 'change' }],
    get_limit: [
        { required: true, message: '请选择领取次数', trigger: 'change' },
        {
            validator: (rule: object, value: string, callback: any) => {
                if (
                    formData.get_limit == 1 &&
                    (formData.get_limit_num == '' || formData.get_limit_num < 0)
                ) {
                    if (formData.get_limit_num == '')
                        return callback(new Error('请输入限制领取次数'))
                    if (formData.get_limit_num < 0)
                        return callback(new Error('限制领取次数不能为负数'))
                } else if (
                    formData.get_limit == 2 &&
                    (formData.get_limit_day_num == '' || formData.get_limit_day_num < 0)
                ) {
                    if (formData.get_limit_day_num == '')
                        return callback(new Error('请输入每天限制领取次数'))
                    if (formData.get_limit_day_num < 0)
                        return callback(new Error('每天限制领取次数不能为负数'))
                }
                callback()
            },
            trigger: 'blur'
        }
    ],
    use_time_type: [
        { required: true, message: '请选择用券时间', trigger: 'change' },
        {
            validator: (rule: object, value: string, callback: any) => {
                if (
                    formData.use_time_type == 0 &&
                    (!formData.use_start_time || !formData.use_end_time)
                ) {
                    callback(new Error('请选择开始时间和结束用券时间'))
                } else if (
                    formData.use_time_type == 1 &&
                    (formData.use_current_day == '' || formData.use_current_day < 0)
                ) {
                    callback(new Error('请输入正确的天数'))
                } else if (
                    formData.use_time_type == 2 &&
                    (formData.use_next_day == '' || formData.use_next_day < 0)
                ) {
                    callback(new Error('请输入正确的天数'))
                }
                callback()
            },
            trigger: 'blur'
        }
    ]
})

const pageTitle = computed(() => {
    if (readOnly && route.query.id) {
        return '优惠券详情'
    } else if (route.query.id) {
        return '编辑优惠券'
    } else {
        return '新增优惠券'
    }
})

const getDetails = async () => {
    const data = await couponDetail({
        id: route.query.id as any
    })
    Object.keys(formData).forEach((key) => {
        if (key == 'goods') {
            data[key].map((item: any) => {
                item.id = item.goods_detail.id
                item.image = item.goods_detail.image
                item.name = item.goods_detail.name
                item.min_price = item.goods_detail.min_price
                item.code = item.goods_detail.code
            })
        }
        //@ts-ignore
        formData[key] = data[key]
    })
    console.log(formData)
}

const handleSave = async () => {
    if (formData.stock_type == 0) {
        formData.stock = ''
    }
    console.log(formData.goods)

    formData.goods = formData.goods.map((item: any) => {
        return {
            goods_id: item.id ?? item.goods_detail.id ?? '',
            image: item.image ?? item.goods_detail.image ?? '',
            name: item.name ?? item.goods_detail.name ?? '',
            min_price: item.min_price ?? item.goods_detail.min_price ?? '',
            code: item.code ?? item.goods_detail.code ?? ''
        }
    })
    try {
        await formRef.value?.validate()
        if (route.query.id && readOnly != 'detail') {
            await couponEdit(formData)
        } else if (!route.query.id && !readOnly) {
            await couponAdd(formData)
        }
        handleRouteBack()
    } catch (error) {
        console.log('保存秒杀报错', error)
    }
}

const handleRouteBack = () => {
    removeTab()
    router.back()
}

onMounted(() => {
    route.query.id && getDetails()
})
</script>
