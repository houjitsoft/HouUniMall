<template>
    <el-card class="!border-none" shadow="never">
        <el-page-header content="查看采集详情" @back="$router.back()" />
    </el-card>
    <el-card class="!border-none mt-2" shadow="never">
        <div class="my-2">
            <el-button @click="handleAllup">批量上架</el-button>
            <el-button @click="handleAlldown">放入仓库</el-button>
        </div>

        <el-tabs v-model="activeName" @tab-click="getData()">
            <el-tab-pane
                v-for="(item, index) in tabs"
                :key="index"
                :label="`${item.label}`"
                :name="item.status"
            ></el-tab-pane>
            <el-table
                size="large"
                :data="goods_deal_no"
                v-if="activeName == 0"
                @selection-change="handleSelectionChange"
            >
                <el-table-column
                    fixed="left"
                    type="selection"
                    width="55"
                    :selectable="handleable"
                ></el-table-column>
                <el-table-column label="采集链接" min-width="150">
                    <template #default="{ row }">
                        <span class="line-2 block">
                            {{ row.link }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column label="商品信息" min-width="200">
                    <template #default="{ row }">
                        <div class="flex" v-if="row.content">
                            <div>
                                <el-image
                                    :src="row.content?.goods_image[0]"
                                    class="w-[50px] h-[50px] flex-none"
                                ></el-image>
                            </div>

                            <div class="ml-2">
                                <div class="line-2">
                                    {{ row.content?.name }}
                                </div>
                                <div v-if="row.content?.sku_type == 1">
                                    <el-tag size="mini">多规格</el-tag>
                                </div>
                            </div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="渠道" min-width="120" prop="channel_name">
                </el-table-column>
                <el-table-column label="状态" min-width="120">
                    <template #default="{ row }">
                        <el-tag type="success" v-if="row.status == 1">成功</el-tag>
                        <el-tag type="danger" v-if="row.status == 0">失败</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="成功时间" min-width="120" prop="create_time">
                </el-table-column>
                <el-table-column label="操作" min-width="180">
                    <template #default="{ row }">
                        <router-link
                            :to="{
                                path: getRoutePath('mall.Goods/add:edit'),
                                query: { gatherId: row.id, type: 'gather' }
                            }"
                        >
                            <el-button link type="primary">编辑</el-button>
                        </router-link>
                        <el-button link type="primary" @click="handleup(row.id)"
                            >上架销售</el-button
                        >
                        <el-button link type="primary" @click="handledown(row.id)"
                            >放入仓库</el-button
                        >
                    </template>
                </el-table-column>
            </el-table>
            <el-table size="large" :data="goods_deal_yes" v-if="activeName == 1">
                <el-table-column
                    fixed="left"
                    type="selection"
                    width="55"
                    :selectable="handleable"
                ></el-table-column>
                <el-table-column label="采集链接" min-width="150">
                    <template #default="{ row }">
                        <span class="line-2 block">
                            {{ row.link }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column label="商品信息" min-width="200">
                    <template #default="{ row }">
                        <div class="flex">
                            <div>
                                <el-image
                                    :src="row.content?.goods_image[0]"
                                    class="w-[50px] h-[50px] flex-none"
                                ></el-image>
                            </div>

                            <div class="ml-2">
                                <div class="line-2">
                                    {{ row.content?.name }}
                                </div>
                                <div v-if="row.content?.sku_type == 1">
                                    <el-tag size="mini">多规格</el-tag>
                                </div>
                            </div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="渠道" min-width="120" prop="channel_name">
                </el-table-column>
                <el-table-column label="状态" min-width="120">
                    <template #default="{ row }">
                        <el-tag type="success" v-if="row.status == 1">成功</el-tag>
                        <el-tag type="danger" v-if="row.status == 0">失败</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="成功时间" min-width="120" prop="create_time">
                </el-table-column>
                <el-table-column label="操作" min-width="180"> </el-table-column>
            </el-table>
        </el-tabs>
    </el-card>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { usePaging } from '@/hooks/usePaging'
import feedback from '@/utils/feedback'

import {
    apiGatherlists,
    apiGatherDel,
    apiGatherDetial,
    apiGatherSavegood
} from '@/api/application/gather'
import { getRoutePath } from '@/router'

const route = useRoute()
const router = useRouter()
const tabs = [
    {
        label: '待处理',
        status: 0
    },
    {
        label: '已处理',
        status: 1
    }
]
const activeName = ref(0)

const goods_deal_yes = ref([])
const goods_deal_no = ref([])
//
const getData = async () => {
    const data = await apiGatherDetial({ id: route.query.id })
    goods_deal_yes.value = data.goods_deal_yes
    goods_deal_no.value = data.goods_deal_no
}
const selectIds = ref([])
getData()
const handleSelectionChange = (row: any) => {
    selectIds.value = row.map((item: any) => {
        return item.id
    })
}

const handleable = (row: any) => {
    if (row.status == 0) {
        return false
    } else {
        return true
    }
}
const handleup = async (id: any) => {
    await feedback.confirm('确定上架销售吗？')
    await apiGatherSavegood({ ids: [id], goods_status: 1 })
    getData()
}
const handledown = async (id: any) => {
    await feedback.confirm('确定放入仓库吗？')
    await apiGatherSavegood({ ids: [id], goods_status: 0 })
    getData()
}
const handleAllup = async () => {
    await feedback.confirm('确定批量上架销售吗？')
    await apiGatherSavegood({ ids: selectIds.value, goods_status: 1 })
    getData()
}
const handleAlldown = async () => {
    await feedback.confirm('确定批量放入销售吗？')
    await apiGatherSavegood({ ids: selectIds.value, goods_status: 0 })
    getData()
}
</script>
