<template>
    <div>
        <el-card class="!border-none" shadow="never">
            <el-page-header :content="$route.meta.title" @back="$router.back()" />
        </el-card>
        <el-form label-width="120px">
            <el-card class="!border-none mt-4" shadow="never">
                <div class="text-xl font-medium">商品信息</div>
                <div class="mt-4">
                    <el-form-item label="商品编号">
                        <div>{{ detailData.code }}</div>
                    </el-form-item>
                    <el-form-item label="商品图片">
                        <el-image :src="detailData.image" class="w-[58px] h-[58px]"> </el-image>
                    </el-form-item>
                    <el-form-item label="商品名称">
                        <div>{{ detailData.name }}</div>
                    </el-form-item>
                </div>
            </el-card>
            <el-card class="!border-none mt-4" shadow="never">
                <div>
                    <el-form-item label="参与折扣">
                        <div>
                            <el-radio-group v-model="detailData.is_discount">
                                <el-radio :label="1">参与</el-radio>
                                <el-radio :label="0">不参与</el-radio>
                            </el-radio-group>
                            <div class="form-tips">
                                是否参与会员折扣，选择不参与则用户下单将不享受会员折扣
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item label="折扣状态">
                        <div class="flex-none">
                            <el-radio-group
                                v-model="detailData.discount_type"
                                @change="distriTypeChange"
                            >
                                <el-radio :label="0">默认会员等级设置</el-radio>
                                <el-radio :label="1">单独设置折扣</el-radio>
                                <el-radio :label="2">单独设置价格</el-radio>
                            </el-radio-group>
                            <el-table :data="formatTableData" class="mt-4">
                                <el-table-column label="会员等级" prop="name"></el-table-column>
                                <el-table-column label="商品规格">
                                    <template #default="{ row }">
                                        <div>{{ row.sku_value_arr.join(',') || '默认' }}</div>
                                    </template>
                                </el-table-column>
                                <el-table-column label="价格" prop="price"></el-table-column>
                                <el-table-column
                                    :label="`会员价(${
                                        detailData.discount_type == 2 ? '元' : '折'
                                    })`"
                                    min-width="180"
                                >
                                    <template #default="{ row }">
                                        <div>
                                            <div
                                                class="flex items-center"
                                                v-if="detailData.discount_type == 0"
                                            >
                                                <span v-if="!row.is_discount"> 无折扣 </span>

                                                <span v-else>
                                                    {{ row.discount_ratio }}折 =
                                                    {{ row.discount_price }}元
                                                </span>
                                            </div>
                                            <div
                                                class="flex items-center"
                                                v-if="detailData.discount_type == 1"
                                            >
                                                <el-input
                                                    class="w-[140px]"
                                                    v-model="row.alone_discount"
                                                    :disabled="detailData.discount_type == 0"
                                                >
                                                    <template #append>
                                                        <div>折</div>
                                                    </template>
                                                </el-input>
                                                <div class="ml-2">
                                                    =
                                                    {{ (row.price * row.alone_discount) / 10 }}元
                                                </div>
                                            </div>
                                            <div v-if="detailData.discount_type == 2">
                                                <el-input
                                                    v-model="row.alone_price"
                                                    class="w-[150px]"
                                                >
                                                    <template #append>
                                                        <div>元</div>
                                                    </template>
                                                </el-input>
                                            </div>
                                        </div>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </div>
                    </el-form-item>
                </div>
            </el-card>
        </el-form>
        <footer-btns>
            <el-button type="primary" @click="submit">保存</el-button>
        </footer-btns>
    </div>
</template>
<script setup lang="ts">
import { getDiscountDetail, discountSave } from '@/api/marketing/member_discount'

interface IDetailData {
    code: string
    name: string
    id: number
    image: string
    is_discount: number
    user_level: any[]
    discount_type: number
}

const route = useRoute()
const id = route.query.id
const formatTableData: any = ref([])
//折扣详情数据
const detailData = ref<IDetailData>({
    code: '',
    name: '',
    id: 0,
    image: '',
    is_discount: 1,
    user_level: [],
    discount_type: 1
})
const newDistributionLevel: any = ref([])

//获取详情数据
const getDetailData = async () => {
    detailData.value = await getDiscountDetail({ id })
}

//格式化表格数据
const formatTable = () => {
    formatTableData.value = []
    detailData.value.user_level.forEach((item: any, index) => {
        item.discount_list.forEach((item1: any, index1: any) => {
            //默认折扣等级
            if (detailData.value.discount_type == 0) {
                const res: any = {}
                Object.keys(item1).map((key) => {
                    res[key] = item1[key]
                })
                res.alone_discount = item.alone_discount
                res.alone_price = item.alone_price
                res.discount_ratio = item.discount_ratio
                res.is_discount = item.is_discount
                formatTableData.value.push({ ...res, name: item.name })
            } else {
                formatTableData.value.push({ ...item1, name: item.name })
            }
        })
    })
}

//佣金规则切换
const distriTypeChange = (value: any) => {
    nextTick(() => {
        formatTable()
    })
}

//保存
const submit = async () => {
    newDistributionLevel.value = detailData.value.user_level
    newDistributionLevel.value.forEach((item: any, index: any) => {
        //清空
        item.discount_list = []
        formatTableData.value.map((item1: any) => {
            if (item1.user_level_id == item.id) {
                item.discount_list.push(item1)
            }
        })
    })
    await discountSave({
        id: detailData.value.id,
        discount_type: detailData.value.discount_type,
        user_level: newDistributionLevel.value,
        is_discount: detailData.value.is_discount
    })
    await getDetailData()
}

onMounted(async () => {
    await getDetailData()
    await formatTable()
})
</script>

<style scoped lang="scss">
:deep(.el-form-item__content) {
    display: block;
}
</style>
