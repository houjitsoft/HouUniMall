<template>
    <div class="goods__edit">
        <el-card class="!border-none" shadow="never">
            <el-page-header :content="$route.meta.title" @back="handleRouteBack" />
        </el-card>
        <el-card class="!border-none mt-4" shadow="never" v-if="route.query.id">
            <div class="flex items-center">
                <el-icon v-if="formData.audit_status == 1" size="34px" color="#0aa91e"
                    ><SuccessFilled
                /></el-icon>
                <el-icon v-else-if="formData.audit_status == 2" size="34px" color="#ff3c3d"
                    ><CircleCloseFilled
                /></el-icon>
                <span class="text-[20px] ml-4">{{ formData.audit_status_text }}</span>
                <span class="ml-4 text-[#ff3c3d]" v-if="formData.audit_status == 2"
                    >原因：{{ formData.audit_content }}</span
                >
            </div>
        </el-card>
        <el-card class="mt-4 !border-none" shadow="never">
            <div class="flex items-center gap-3">
                <span class="text-tip text-[13px]">按当前商品信息生成标题、卖点与详情文案，结果可复制后手动填入</span>
                <div class="flex-1" />
                <ai-agent-panel
                    code="copy_generate"
                    label="AI 生成标题与卖点"
                    :payload="buildCopyPayload"
                />
            </div>
        </el-card>
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
                        <el-tab-pane :label="tabsItem.label" :name="tabsItem.type">
                            <component
                                :is="tabsItem.comp"
                                :ref="
                                    tabsItem.type === TabsEnum.GOODS_I18N
                                        ? setGoodsI18nRef
                                        : undefined
                                "
                                :modelValue="formData"
                                :goods-id="formData.id || route.query.id"
                                :read-only="read_only"
                            ></component>
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
import AiAgentPanel from '@/components/ai-agent-panel/index.vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useDictOptions } from '@/hooks/useDictOptions'
import { getGoodsDetail, apiGoodsEdit, apiGoodsAdd } from '@/api/goods/goods'
import type { GoodsForm } from '@/api/goods/goods'
import useMultipleTabs from '@/hooks/useMultipleTabs'
import { apiGathergooddetial, apiGathergoodSave } from '@/api/application/gather'
import PriceStock from './component/price-stock.vue'

const BaseSetup = defineAsyncComponent(() => import('./component/base-setup.vue'))
const ExpressSetup = defineAsyncComponent(() => import('./component/express-setup.vue'))
const GoodsDetail = defineAsyncComponent(() => import('./component/goods-detail.vue'))
const SalesSetup = defineAsyncComponent(() => import('./component/sales-setup.vue'))
const GoodsI18n = defineAsyncComponent(() => import('./component/goods-i18n.vue'))
import feedback from '@/utils/feedback'

const route = useRoute()
const router = useRouter()

// Tab类型
enum TabsEnum {
    BASE_SETUP = 'base_setup',
    PRICE_STOCK = 'price_stock',
    EXPRESS_SETUP = 'express_setup',
    GOODS_DETAIL = 'goods_detail',
    SALES_SETUP = 'sales_setup',
    GOODS_I18N = 'goods_i18n'
}
const read_only: any = !!route.query.read_only
const goodsI18nRef = shallowRef<{
    saveForGoods: (goodsId: number | string) => Promise<boolean>
} | null>(null)

function setGoodsI18nRef(instance: any) {
    goodsI18nRef.value = instance || null
}

const baseTabs = [
    {
        label: '基础设置',
        comp: markRaw(BaseSetup),
        type: TabsEnum.BASE_SETUP
    },
    {
        label: '价格/库存',
        comp: markRaw(PriceStock),
        type: TabsEnum.PRICE_STOCK
    },
    {
        label: '物流设置',
        comp: markRaw(ExpressSetup),
        type: TabsEnum.EXPRESS_SETUP
    },
    {
        label: '商品详情',
        comp: markRaw(GoodsDetail),
        type: TabsEnum.GOODS_DETAIL
    },
    {
        label: '销售设置',
        comp: markRaw(SalesSetup),
        type: TabsEnum.SALES_SETUP
    }
]

const i18nTab = {
    label: '多语言',
    comp: markRaw(GoodsI18n),
    type: TabsEnum.GOODS_I18N
}

const tabsParams = reactive({
    active: TabsEnum.BASE_SETUP as string,
    TabsEnumMap: [...baseTabs, i18nTab] as Array<{ label: string; comp: any; type: string }>
})

const formData = reactive<GoodsForm>({
    id: '',
    name: '',
    code: '',
    audit_status: 0,
    audit_status_text: '',
    audit_content: '',
    descript: '',
    category_ids: [],
    shop_category_id: '',
    goods_image: [],
    video_is: 0,
    video_from: 1,
    video_cover: '',
    video_uri: '',
    sku_type: 0,
    sku_name_list: [
        {
            name: '',
            has_image: 0,
            value: [
                {
                    value: '',
                    image: ''
                }
            ]
        }
    ],
    sku: [
        {
            id: '',
            image: '',
            sku_value_arr: [],
            price: '',
            line_price: '',
            market_price: '',
            stock: 0,
            weight: 0,
            volume: '',
            code: ''
        }
    ],
    goods_send_mode: [0],
    express_type: 0,
    express_tpl_id: '',
    content: '',
    stock_warning: '',
    virtual_sale_num: '',
    virtual_visit_num: '',
    sort: 0,
    shop_recommend: 1,
    status: 1
})

const { removeTab } = useMultipleTabs()
const formRef = shallowRef<FormInstance>()
const rules = reactive<FormRules>({
    code: [{ required: true, message: '请输入商品货号', trigger: 'blur' }],
    name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
    category_ids: [{ required: true, message: '请选择平台分类', trigger: 'blur' }],
    shop_category_id: [{ required: true, message: '请选择商品分类', trigger: 'blur' }],
    goods_image: [{ required: true, message: '请输入商品轮播图', trigger: 'blur' }],
    video_uri: [{ required: true, message: '请上传视频', trigger: 'blur' }],
    sku_type: [{ required: true, message: '请上传视频', trigger: 'blur' }],
    goods_send_mode: [{ required: true, message: '请选择配送方式', trigger: 'blur' }],
    express_type: [{ required: true, message: '请选择运费设置', trigger: 'blur' }],
    express_tpl_id: [{ required: true, message: '请选择运费模版', trigger: 'blur' }],
    content: [{ required: true, message: '请输商品详情', trigger: 'blur' }],
    statu: [{ required: true, message: '请选择销售状态', trigger: 'blur' }]
})

const getDetails = async () => {
    let data = ''
    if (route.query.type) {
        const { content } = await apiGathergooddetial({
            id: route.query.gatherId
        })
        // content.id = route.query.gatherId
        data = content
    } else {
        data = await getGoodsDetail({
            id: route.query.id
        })
    }

    Object.keys(formData).forEach((key) => {
        //@ts-ignore
        formData[key] = data[key]
    })
}

// const { optionsData } = useDictOptions<{
//     article_cate: any[];
// }>({
//     article_cate: {},
// });

const handleSave = async () => {
    formRef.value?.validate(async (valid, requiredFields: any) => {
        // 基础设置必填字段
        const basicFields = ['code', 'name', 'category_ids', 'goods_image', 'video_url']
        // 价格库存必填字段
        const skuFields = ['sku_type', 'price', 'stock']
        // 物流设置必填字段
        const expressFields = ['goods_send_mode', 'express_type', 'express_tpl_id']
        // 商品详情必填字段
        const contentFields = ['content', 'express_type', 'express_tpl_id']
        // 销售设置必填字段
        const salesFields = ['content', 'express_type', 'express_tpl_id']
        console.log(requiredFields)

        for (const key in requiredFields) {
            if (basicFields.includes(key)) tabsParams.active = TabsEnum.BASE_SETUP
            else if (skuFields.includes(key)) tabsParams.active = TabsEnum.PRICE_STOCK
            else if (expressFields.includes(key)) tabsParams.active = TabsEnum.EXPRESS_SETUP
            else if (contentFields.includes(key)) tabsParams.active = TabsEnum.GOODS_DETAIL
            else if (salesFields.includes(key)) tabsParams.active = TabsEnum.SALES_SETUP

            feedback.msgError(requiredFields[key][0].message)

            return
        }
        if (route.query.type) {
            console.log(formData)

            await apiGathergoodSave({ id: route.query.gatherId, content: formData })
        } else {
            let savedGoodsId: number | string = formData.id || (route.query.id as string) || ''
            if (route.query.id) {
                await apiGoodsEdit(formData)
            } else {
                savedGoodsId = await apiGoodsAdd(formData)
            }
            if (savedGoodsId) {
                await goodsI18nRef.value?.saveForGoods(savedGoodsId)
            }
        }
        handleRouteBack()
    })
}

const handleRouteBack = () => {
    removeTab()
    router.back()
}
if (route.query.id || route.query.gatherId) {
    getDetails()
}

/** 把当前商品信息交给「商品标题与文案生成」智能体；字段缺失就标注，不编造 */
const buildCopyPayload = () => {
    const d: any = formData || {}
    if (!d.name) return ''
    const lines = [
        '请根据以下商品信息生成标题、卖点与详情文案：',
        `商品名称：${d.name}`,
        `分类：${d.category_name || d.shop_category_name || '未提供'}`,
        `售价：${d.price ?? '未提供'}`,
        `库存：${d.stock ?? d.stock_num ?? '未提供'}`
    ]
    if (Array.isArray(d.spec_list) && d.spec_list.length) {
        lines.push(`规格：${d.spec_list.map((i: any) => i.name || i.spec_name || '').filter(Boolean).join('、')}`)
    }
    return lines.join('\n')
}
</script>
