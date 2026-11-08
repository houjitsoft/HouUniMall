<template>
    <el-card class="!border-none" shadow="never">
        <el-form ref="formRef" class="mb-[-16px] mt-4" :model="queryParams" :inline="true">
            <el-form-item label="商品名称">
                <el-input class="w-[280px]" v-model="queryParams.goods_name" placeholder="请输入" />
            </el-form-item>
            <el-form-item label="拼团状态">
                <el-select class="w-[280px]" v-model="queryParams.status">
                    <el-option label="全部" value=" "></el-option>
                    <el-option label="未开始" :value="0"></el-option>
                    <el-option label="进行中" :value="1"></el-option>
                    <el-option label="已结束" :value="2"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="活动时间">
                <daterange-picker
                    v-model:startTime="queryParams.start_time"
                    v-model:endTime="queryParams.end_time"
                />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="resetPage">查询</el-button>
                <el-button @click="resetParams">重置</el-button>
            </el-form-item>
        </el-form>
    </el-card>
    <el-card class="!border-none mt-4" shadow="never">
        <div>
            <el-tabs v-model="queryParams.audit_status" @tab-change="changeTabs">
                <el-tab-pane name="" :label="`全部(${pager.extend.all || 0})`"></el-tab-pane>
                <el-tab-pane :name="10" :label="`待审核(${pager.extend.wait || 0})`"></el-tab-pane>
                <el-tab-pane
                    :name="20"
                    :label="`审核通过(${pager.extend.success || 0})`"
                ></el-tab-pane>
                <el-tab-pane
                    :name="30"
                    :label="`审核失败(${pager.extend.failed || 0})`"
                ></el-tab-pane>
            </el-tabs>
        </div>
        <el-button type="primary" @click="editPTGoods('add')">新增拼团商品</el-button>
        <el-table size="large" v-loading="pager.loading" :data="pager.lists" class="mt-4">
            <el-table-column label="拼团商品" min-width="220">
                <template #default="{ row }">
                    <div class="flex items-center">
                        <el-image
                            :src="row.goods.image"
                            class="w-[64px] h-[64px] rounded-lg flex-none"
                        ></el-image>
                        <div class="ml-2 truncate">{{ row.goods.name }}</div>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="拼团价" min-width="180">
                <template #default="{ row }">
                    <div>¥{{ row.min_price }}~¥{{ row.max_price }}</div>
                </template>
            </el-table-column>
            <el-table-column label="拼团数据" min-width="120">
                <template #default="{ row }">
                    <div>
                        <div>拼团数量：{{ row.open_count?.count || 0 }}</div>
                        <div>成团数量：{{ row.open_count?.success_count || 0 }}</div>
                        <div>参团人数：{{ row.open_count?.join_count || 0 }}</div>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="活动状态" min-width="120">
                <template #default="{ row }">
                    <div>
                        <el-tag v-if="row.status == 0" class="ml-2" type="warning">未开始</el-tag>
                        <el-tag v-if="row.status == 1" class="ml-2" type="success">进行中</el-tag>
                        <el-tag v-if="row.status == 2" class="ml-2" type="info">已结束</el-tag>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="审核状态" min-width="120" prop="audit_status_text">
            </el-table-column>
            <el-table-column label="活动时间" min-width="250">
                <template #default="{ row }">
                    <div>
                        <div>开始时间：{{ row.start_time }}</div>
                        <div>结束时间：{{ row.end_time }}</div>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="操作" min-width="240">
                <template #default="{ row }">
                    <div>
                        <el-button type="primary" link @click="editPTGoods('detail', row.id)"
                            >详情</el-button
                        >
                        <el-button
                            v-if="row.can_edit == 1"
                            type="primary"
                            link
                            @click="editPTGoods('edit', row.id)"
                            >编辑</el-button
                        >
                        <el-button
                            v-if="row.can_submit == 1"
                            type="primary"
                            link
                            @click="changeStatus(1, row.id)"
                            >提交审核</el-button
                        >
                        <el-button
                            v-if="row.can_open == 1"
                            type="primary"
                            link
                            @click="changeStatus(2, row.id)"
                            >开启活动</el-button
                        >
                        <el-button
                            v-if="row.can_close == 1"
                            type="primary"
                            link
                            @click="changeStatus(3, row.id)"
                            >结束活动</el-button
                        >
                        <el-button type="primary" link @click="changeStatus(4, row.id)"
                            >删除</el-button
                        >
                    </div>
                </template>
            </el-table-column>
        </el-table>
        <div class="flex justify-end mt-4">
            <pagination v-model="pager" @change="getLists" />
        </div>
    </el-card>
    <goodsEditPopVue ref="popRef" @close="getLists"></goodsEditPopVue>
</template>
<script setup lang="ts">
import { usePaging } from '@/hooks/usePaging'
import {
    getPTgoodsList,
    delPTGoods,
    PTGoodsSubmit,
    PTGoodsBegin,
    PTGoodsOver
} from '@/api/marketing/group_buy'
import goodsEditPopVue from './components/goodsEditPop.vue'
import feedback from '@/utils/feedback'

//搜索参数
const queryParams: any = ref({
    goods_name: '', //商品名称
    audit_status: '', //审核状态
    status: '', //活动状态
    start_time: '',
    end_time: ''
})
//弹框ref
const popRef = shallowRef()

//分页组件
const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: getPTgoodsList,
    params: queryParams.value
})

//标签切换
const changeTabs = () => {
    nextTick(() => {
        getLists()
    })
}

// 添加/详情/编辑 拼团商品
const editPTGoods = (type: string, id?: any) => {
    popRef.value.open({ type, id })
}

//修改拼团商品状态
const changeStatus = async (type: number, id: any) => {
    //type 1-提交审核 2-开启活动 3-结束活动 4-删除
    switch (type) {
        case 1:
            PTGoodsSubmit({ id })
            break
        case 2:
            PTGoodsBegin({ id })
            break
        case 3:
            await feedback.confirm('确定结束该活动吗？')

            PTGoodsOver({ id })
            break
        case 4:
            await feedback.confirm('确定删除该活动吗？')
            delPTGoods({ id })
            break
        default:
            break
    }
    getLists()
}

onMounted(() => {
    getLists()
})
</script>

<style lang="scss" scoped></style>
