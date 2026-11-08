<template>
    <el-card class="!border-none" shadow="never">
        <el-alert
            type="warning"
            title="默认根据平台设置的会员等级折扣自动打折，商户也可以单独设置会员价，拼团和秒杀等营销活动设置参与会员价但是不生效"
            :closable="false"
            show-icon
        />
        <el-form ref="formRef" class="mb-[-16px] mt-4" :model="queryParams" :inline="true">
            <el-form-item label="商品信息">
                <el-input
                    class="w-[280px]"
                    v-model="queryParams.name"
                    placeholder="请输入商品名称/编号"
                    clearable
                />
            </el-form-item>
            <el-form-item label="商品状态">
                <el-select class="w-[280px]" v-model="queryParams.type">
                    <el-option label="全部" value />
                    <el-option label="销售中" value="sale" />
                    <el-option label="仓库中" value="not_sale" />
                </el-select>
            </el-form-item>
            <el-form-item label="参与折扣">
                <el-select class="w-[280px]" v-model="queryParams.is_discount">
                    <el-option label="全部" value />
                    <el-option label="不参与折扣" value="0" />
                    <el-option label="参与折扣" value="1" />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="resetPage">查询</el-button>
                <el-button @click="resetParams">重置</el-button>
            </el-form-item>
        </el-form>
    </el-card>
    <el-card class="!border-none mt-4" shadow="never">
        <el-button type="primary" @click="batchChangeStauts(1)">参与折扣</el-button>
        <el-button @click="batchChangeStauts(0)">取消参与</el-button>
        <div>
            <el-table
                ref="tableRef"
                size="large"
                v-loading="pager.loading"
                :data="pager.lists"
                class="mt-4"
            >
                <el-table-column type="selection" width="55" />
                <el-table-column label="商品名称" min-width="180">
                    <template #default="{ row }">
                        <div class="flex items-center">
                            <el-image
                                :src="row.image"
                                class="w-[64px] h-[64px] rounded-lg flex-none"
                            ></el-image>
                            <div class="ml-2 line-2">{{ row.name }}</div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="价格" min-width="140">
                    <template #default="{ row }">
                        <div>{{ row.min_price }}~{{ row.max_price }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="库存" prop="total_stock" min-width="140" />
                <el-table-column label="商品状态" min-width="140">
                    <template #default="{ row }">
                        <el-tag v-if="row?.status == 1" type="success">销售中</el-tag>
                        <el-tag type="info" v-else>仓库中</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="折扣状态" min-width="140">
                    <template #default="{ row }">
                        <div>{{ row.is_discount == 1 ? '参与' : '不参与' }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="操作" min-width="180">
                    <template #default="{ row }">
                        <div>
                            <el-button
                                v-perms="['mall.goods/discount_detailt']"
                                type="primary"
                                link
                            >
                                <router-link
                                    :to="{
                                        path: getRoutePath('mall.goods/discount_detail'),
                                        query: {
                                            id: row.id
                                        }
                                    }"
                                >
                                    设置会员价
                                </router-link>
                            </el-button>

                            <el-button
                                type="primary"
                                v-if="row.is_discount == 1"
                                link
                                @click="changStatus(0, row.name, row.id)"
                                >不参与</el-button
                            >
                            <el-button
                                type="primary"
                                v-if="row.is_discount != 1"
                                link
                                @click="changStatus(1, row.name, row.id)"
                                >参与折扣</el-button
                            >
                        </div>
                    </template>
                </el-table-column>
            </el-table>
            <div class="flex justify-end mt-4">
                <pagination v-model="pager" @change="getLists" />
            </div>
        </div>
    </el-card>
</template>
<script setup lang="ts">
import { getGoodsLists } from '@/api/goods/goods'
import { getRoutePath } from '@/router'
import { discountStatus } from '@/api/marketing/member_discount'
import { usePaging } from '@/hooks/usePaging'
import feedback from '@/utils/feedback'

//搜索参数
const queryParams: any = ref({
    name: '', //商品信息
    is_discount: '', //折扣状态
    type: '' //商品状态
})
//表格ref
const tableRef = shallowRef()

//修改折扣状态
const changStatus = async (stauts: any, name: string, id: number) => {
    //status 0-取消折扣 1-参与折扣
    if (stauts == 0) {
        await feedback.customConfirm(
            '确认该商品：\t',
            '\t取消折扣吗？请谨慎操作',
            name,
            'color:red',
            '取消折扣'
        )
    }
    if (stauts == 1) {
        await feedback.customConfirm(
            '确认该商品：\t',
            '\t参与折扣吗？请谨慎操作',
            name,
            'color:red',
            '参与折扣'
        )
    }
    await discountStatus({ ids: [id], is_discount: stauts })
    getLists()
}

//批量修改折扣状态
const batchChangeStauts = async (status: any) => {
    const ids = tableRef.value.getSelectionRows().map((item: any) => {
        return item.id
    })
    if (ids.length == 0) {
        feedback.msgError('没有选择商品！')
        return
    }
    switch (status) {
        case 0:
            await feedback.confirm('确定将已选商品取消折扣？')
            break

        case 1:
            await feedback.confirm('确定将已选商品参与折扣？')
            break
        default:
            break
    }
    await discountStatus({ ids: ids, is_discount: status })
    getLists()
}

//分页组件
const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: getGoodsLists,
    params: queryParams.value
})

onMounted(() => {
    getLists()
})
</script>
