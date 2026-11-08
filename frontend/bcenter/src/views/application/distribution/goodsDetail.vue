<template>
    <div>
        <el-card class="!border-none" shadow="never">
            <el-page-header :content="$route.meta.title" @back="$router.back()" />
        </el-card>
        <el-form label-width="120px">
            <el-card class="!border-none mt-4" shadow="never">
                <div class="font-medium text-xl">商品信息</div>
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
                    <el-form-item label="分销状态">
                        <div>
                            <el-radio-group v-model="detailData.is_distribution">
                                <el-radio :label="1">参与分销</el-radio>
                                <el-radio :label="0">不参与分销</el-radio>
                            </el-radio-group>
                            <div class="form-tips">
                                是否参与分销，选择不参与分销将不产生分销佣金
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item label="分销状态">
                        <div class="flex-none">
                            <el-radio-group
                                v-model="detailData.distribution_type"
                                @change="distriTypeChange"
                            >
                                <el-radio :label="0">默认分销等级佣金规则</el-radio>
                                <el-radio :label="1">单独设置佣金比例</el-radio>
                                <el-radio :label="2">单独设置佣金</el-radio>
                            </el-radio-group>
                            <el-table :data="formatTableData">
                                <el-table-column label="佣金规则" prop="name"></el-table-column>
                                <el-table-column label="商品规格">
                                    <template #default="{ row }">
                                        <div>{{ row.sku_value_arr.join(',') || '默认' }}</div>
                                    </template>
                                </el-table-column>
                                <el-table-column label="价格" prop="price"></el-table-column>
                                <el-table-column
                                    :label="`自购佣金比例(${
                                        detailData.distribution_type == 2 ? '元' : '%'
                                    })`"
                                    min-width="180"
                                >
                                    <template #default="{ row }">
                                        <div>
                                            <div
                                                class="flex items-center"
                                                v-if="detailData.distribution_type != 2"
                                            >
                                                <el-input
                                                    class="w-[80px]"
                                                    v-model="row.alone_self_ratio"
                                                    :disabled="detailData.distribution_type == 0"
                                                ></el-input>
                                                <div class="ml-2">
                                                    =
                                                    {{
                                                        (
                                                            (row.price * row.alone_self_ratio) /
                                                            100
                                                        ).toFixed(2)
                                                    }}元
                                                </div>
                                            </div>
                                            <div v-if="detailData.distribution_type == 2">
                                                <el-input
                                                    v-model="row.alone_self_money"
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
                                <el-table-column
                                    :label="`一级佣金比例(${
                                        detailData.distribution_type == 2 ? '元' : '%'
                                    })`"
                                    min-width="180"
                                >
                                    <template #default="{ row }">
                                        <div>
                                            <div
                                                class="flex items-center"
                                                v-if="detailData.distribution_type != 2"
                                            >
                                                <el-input
                                                    class="w-[80px]"
                                                    v-model="row.alone_one_ratio"
                                                    :disabled="detailData.distribution_type == 0"
                                                ></el-input>
                                                <div class="ml-2">
                                                    =
                                                    {{
                                                        (
                                                            (row.price * row.alone_one_ratio) /
                                                            100
                                                        ).toFixed(2)
                                                    }}元
                                                </div>
                                            </div>
                                            <div v-if="detailData.distribution_type == 2">
                                                <el-input
                                                    v-model="row.alone_one_money"
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
                                <el-table-column
                                    :label="`二级佣金比例(${
                                        detailData.distribution_type == 2 ? '元' : '%'
                                    })`"
                                    min-width="180"
                                >
                                    <template #default="{ row }">
                                        <div class="flex items-center">
                                            <div
                                                class="flex items-center"
                                                v-if="detailData.distribution_type != 2"
                                            >
                                                <el-input
                                                    class="w-[80px]"
                                                    v-model="row.alone_two_ratio"
                                                    :disabled="detailData.distribution_type == 0"
                                                ></el-input>
                                                <div class="ml-2">
                                                    =
                                                    {{
                                                        (
                                                            (row.price * row.alone_two_ratio) /
                                                            100
                                                        ).toFixed(2)
                                                    }}元
                                                </div>
                                            </div>
                                            <div v-if="detailData.distribution_type == 2">
                                                <el-input
                                                    v-model="row.alone_two_money"
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
import { distributionDetail, distributionSave } from '@/api/application/distribution'

interface IDetailData {
    code: string
    name: string
    id: number
    image: string
    is_distribution: number
    distribution_level: any[]
    distribution_type: number
}

const route = useRoute()
const id = route.query.id
const formatTableData: any = ref([])
//分销详情数据
const detailData = ref<IDetailData>({
    code: '',
    name: '',
    id: 0,
    image: '',
    is_distribution: 1,
    distribution_level: [],
    distribution_type: 1
})
const newDistributionLevel: any = ref([])

//获取详情数据
const getDetailData = async () => {
    detailData.value = await distributionDetail({ id })
}

//格式化表格数据
const formatTable = () => {
    formatTableData.value = []
    detailData.value.distribution_level.forEach((item: any, index) => {
        item.distribution_list.forEach((item1: any, index1: any) => {
            //默认分销等级
            if (detailData.value.distribution_type == 0) {
                const res: any = {}
                Object.keys(item1).map((key) => {
                    res[key] = item1[key]
                })
                res.alone_self_ratio = item.self_ratio
                res.alone_one_ratio = item.one_ratio
                res.alone_two_ratio = item.two_ratio
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
    newDistributionLevel.value = detailData.value.distribution_level
    newDistributionLevel.value.forEach((item: any, index: any) => {
        //清空
        item.distribution_list = []
        formatTableData.value.map((item1: any) => {
            if (item1.distribution_level_id == item.id) {
                item.distribution_list.push(item1)
            }
        })
    })
    await distributionSave({
        id: detailData.value.id,
        distribution_type: detailData.value.distribution_type,
        distribution_level: newDistributionLevel.value,
        is_distribution: detailData.value.is_distribution
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
