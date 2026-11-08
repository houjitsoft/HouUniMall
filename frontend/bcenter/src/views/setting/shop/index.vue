<template>
    <div class="shop__edit">
        <el-card class="mt-4 !border-none" shadow="never" :body-style="{ 'padding-top': '10px' }">
            <el-form
                ref="formRef"
                :model="formData"
                label-width="120px"
                :rules="rules"
                :disabled="read_only"
            >
                <!-- 选项卡 -->
                <el-tabs v-model="tabsParams.active" class="demo-tabs">
                    <template
                        v-for="(tabsItem, tabsIndex) in tabsParams.TabsEnumMap"
                        :key="tabsIndex"
                    >
                        <el-tab-pane
                            :label="tabsItem.label"
                            :name="tabsItem.type"
                            class="pt-4"
                            lazy
                        >
                            <component :is="tabsItem.comp" :modelValue="formData"></component>
                        </el-tab-pane>
                    </template>
                </el-tabs>
            </el-form>
        </el-card>
        <footer-btns>
            <el-button type="primary" @click="handleSave" :disabled="read_only">保存</el-button>
        </footer-btns>
    </div>
</template>

<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus'
import { getShopConfig, setShopConfig } from '@/api/setting/shop'
import type { ShopModal } from '@/api/setting/shop.d'
import BaseSetup from './components/basics.vue'
import Certification from './components/certification.vue'
import Info from './components/info.vue'
import feedback from '@/utils/feedback'

const route = useRoute()
const router = useRouter()

// Tab类型
enum TabsEnum {
    BASE_SETUP = 'base_setup',
    CERTIFICATION = 'certification',
    INFO = 'info'
}

const tabsParams = reactive({
    active: TabsEnum.BASE_SETUP,
    TabsEnumMap: [
        {
            label: '基础设置',
            comp: BaseSetup,
            type: TabsEnum.BASE_SETUP
        },
        {
            label: '经营信息',
            comp: Info,
            type: TabsEnum.INFO
        },
        {
            label: '资质信息',
            comp: Certification,
            type: TabsEnum.CERTIFICATION
        }
    ]
})

const formData = reactive<any>({
    name: '',
    type_text: '',
    shop_category: {
        name: ''
    },
    sale_orders: '',
    logo: '',
    desc: '',
    contact: '',
    mobile: [0],
    business_license: '',
    other_license: [],
    shop_status: '',
    return_mobile: '',
    return_contact: '',
    return_province_id: '',
    return_city_id: '',
    return_district_id: '',
    return_address_info: '',
    province_id: '',
    city_id: '',
    district_id: '',
    address_info: '',
    lng: '',
    lat: '',
    business_status: 1
})

const formRef = shallowRef<FormInstance>()
const rules = reactive<FormRules>({
    logo: [{ required: true, message: '请上传LOGO', trigger: 'blur' }],
    contact: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
    mobile: [{ required: true, message: '请输入联系人电话', trigger: 'blur' }],
    business_license: [{ required: true, message: '请上传营业执照', trigger: 'blur' }]
})

const getDetails = async () => {
    const data = await getShopConfig()
    Object.keys(formData).forEach((key) => {
        //@ts-ignore
        formData[key] = data[key]
    })
    console.log(formData)
}

const handleSave = () => {
    formRef.value?.validate(async (valid, requiredFields: any) => {
        // 基础设置必填字段
        const basicFields = ['logo', 'contact', 'mobile']
        // 资质信息必填字段
        const licenseFields = ['business_license']
        for (const key in requiredFields) {
            if (basicFields.includes(key)) tabsParams.active = TabsEnum.BASE_SETUP
            else if (licenseFields.includes(key)) tabsParams.active = TabsEnum.CERTIFICATION
            feedback.msgError(requiredFields[key][0].message)
            return
        }
        await setShopConfig(formData)
    })
}

getDetails()
</script>
