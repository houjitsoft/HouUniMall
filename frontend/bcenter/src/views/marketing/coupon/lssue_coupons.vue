<template>
    <div class="Issue-coupons">
        <el-card class="!border-none" shadow="never">
            <el-page-header content="发放优惠券" @back="handleRouteBack" />
        </el-card>

        <el-form ref="formRef" :model="formData" label-width="120px" :rules="rules">
            <el-card class="!border-none mt-4" shadow="never">
                <template #header>
                    <span class="text-xl font-medium">优惠券信息</span>
                </template>
                <el-form-item label="优惠券名称">
                    {{ coupons.name }}
                </el-form-item>
                <el-form-item label="优惠券内容">
                    {{ coupons.coupon_content }}
                </el-form-item>
                <el-form-item label="使用范围">
                    {{ coupons.use_goods_type_text }}
                </el-form-item>
                <el-form-item label="领取次数">
                    {{ coupons.get_limit_text }}
                </el-form-item>
                <el-form-item label="用券时间">
                    {{ coupons.use_time_text }}
                </el-form-item>
                <el-form-item label="发放数量">
                    {{ coupons.stock_type == 0 ? '不限数量' : coupons.stock + '张' }}
                </el-form-item>
                <el-form-item label="已发放">
                    {{ coupons.has_get_num }}
                </el-form-item>
                <el-form-item label="已使用">
                    {{ coupons.has_use_num }}
                </el-form-item>
                <el-form-item label="使用率"> {{ coupons.has_use_ratio }}% </el-form-item>
            </el-card>

            <el-card class="!border-none mt-4" shadow="never">
                <template #header>
                    <span class="text-xl font-medium">发放设置</span>
                </template>
                <el-form-item label="每人发放" prop="nums">
                    <div>
                        <el-input
                            class="w-[200px]"
                            v-model="formData.nums"
                            placeholder="请输入"
                            type="text"
                            :show-word-limit="true"
                            :clearable="true"
                        />
                        <span class="ml-4">张</span>
                    </div>
                </el-form-item>
                <el-form-item label="发放范围" prop="user_ids">
                    <user-picker v-model="selectData">
                        <template #btn>
                            <el-button type="primary"> 选择用户 </el-button>
                            <span class="ml-4 text-[#999]"> 最多支持选择10个用户 </span>
                        </template>
                    </user-picker>
                </el-form-item>
            </el-card>
        </el-form>

        <footer-btns>
            <el-button type="primary" @click="handleSave">保存</el-button>
        </footer-btns>
    </div>
</template>

<script lang="ts" setup name="IssueCoupons">
import type { FormInstance, FormRules } from 'element-plus'
import useMultipleTabs from '@/hooks/useMultipleTabs'
import { couponDetail, couponSend } from '@/api/marketing/coupon'
import type { CouponSendFormType } from '@/api/marketing/coupon.d'

const route = useRoute()
const router = useRouter()
const { removeTab } = useMultipleTabs()

const selectData = ref([])
const formData = reactive<CouponSendFormType>({
    user_ids: [],
    nums: '',
    id: route.query.id as any
})
const coupons = reactive<any>({
    sn: '',
    name: '',
    coupon_content: '',
    use_goods_type_text: '',
    use_time_text: '',
    get_limit_text: '',
    stock_type: 0,
    stock: 0,
    has_get_num: '',
    has_use_num: '',
    has_use_ratio: ''
})
const formRef = shallowRef<FormInstance>()
const rules = reactive<FormRules>({
    nums: [{ required: true, message: '请输入发放数量', trigger: 'change' }],
    user_ids: [
        {
            required: true,
            validator: (rule: object, value: string, callback: any) => {
                if (!formData.user_ids.length) {
                    callback(new Error('请选择发放用户'))
                }
                callback()
            },
            trigger: 'blur'
        }
    ]
})

watchEffect(() => {
    const value = selectData.value
    formData.user_ids = value.map((item) => (item as unknown as any).id)
})

const getDetails = async () => {
    const data = await couponDetail({
        id: route.query.id as any
    })
    Object.keys(coupons).forEach((key) => {
        //@ts-ignore
        coupons[key] = data[key]
    })
}

const handleSave = async () => {
    try {
        await formRef.value?.validate()
        if (route.query.id) {
            await couponSend(formData)
        }
        handleRouteBack()
    } catch (error) {
        console.log('发放优惠券报错', error)
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
