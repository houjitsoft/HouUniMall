<template>
    <el-card class="!border-none" shadow="never">
        <el-form ref="formRef" class="mb-[-16px] mt-4" :model="queryParams" :inline="true">
            <el-form-item label="团长信息">
                <el-input
                    class="w-[280px]"
                    v-model="queryParams.nickname"
                    placeholder="请输入名称"
                />
            </el-form-item>
            <el-form-item label="拼团商品">
                <el-input
                    class="w-[280px]"
                    v-model="queryParams.goods_name"
                    placeholder="请输入名称"
                />
            </el-form-item>
            <el-form-item label="开团时间">
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
        <el-tabs v-model="queryParams.status" @tab-change="changeTabs">
            <el-tab-pane name="" :label="`全部(${pager.extend.all || 0})`"></el-tab-pane>
            <el-tab-pane name="0" :label="`拼团中(${pager.extend.wait || 0})`"></el-tab-pane>
            <el-tab-pane name="1" :label="`拼团成功(${pager.extend.success || 0})`"></el-tab-pane>
            <el-tab-pane name="2" :label="`拼团失败(${pager.extend.failed || 0})`"></el-tab-pane>
        </el-tabs>
        <el-table size="large" v-loading="pager.loading" :data="pager.lists" class="mt-4">
            <el-table-column label="团长" min-width="230">
                <template #default="{ row }">
                    <div class="flex items-center">
                        <el-image
                            :src="row.user.avatar"
                            class="w-[64px] h-[64px] flex-none"
                        ></el-image>
                        <div class="ml-2">{{ row.user.nickname }}</div>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="拼团商品" min-width="230">
                <template #default="{ row }">
                    <div>
                        <div class="flex items-center">
                            <el-image
                                :src="row.order_goods.goods_image"
                                class="w-[64px] h-[64px] flex-none"
                            ></el-image>
                            <div class="ml-2 truncate">{{ row.order_goods.goods_name }}</div>
                        </div>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="成团人数" min-width="120" prop="user_num"> </el-table-column>
            <el-table-column label="参团人数" min-width="120" prop="join_num"> </el-table-column>
            <el-table-column label="拼团状态" min-width="120" prop="status_text"> </el-table-column>
            <el-table-column label="开团时间" min-width="120" prop="update_time"> </el-table-column>
            <el-table-column label="操作" min-width="120">
                <template #default="{ row }">
                    <div>
                        <el-button type="primary" link @click="checkDetail(row.id)"
                            >查看详情</el-button
                        >
                        <el-button
                            type="primary"
                            link
                            v-if="row.can_close"
                            @click="handleClose(row.order_goods, row.id)"
                            >结束拼团</el-button
                        >
                    </div>
                </template>
            </el-table-column>
        </el-table>
        <div class="flex justify-end mt-4">
            <pagination v-model="pager" @change="getLists" />
        </div>
    </el-card>
    <listDetailVue ref="popRef"></listDetailVue>
</template>
<script setup lang="ts">
import { usePaging } from '@/hooks/usePaging'
import { getPTList, PTOver } from '@/api/marketing/group_buy'
import listDetailVue from './components/listDetail.vue'
import feedback from '@/utils/feedback'

//弹框ref
const popRef = shallowRef()

//搜索参数
const queryParams: any = ref({
    nickname: '', //搜索团长
    goods_name: '', //搜索商品
    status: '', //拼团状态
    start_time: '', //开始时间
    end_time: '' //结束时间
})

//分页组件
const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: getPTList,
    params: queryParams.value
})

//标签页切换
const changeTabs = async () => {
    await nextTick()
    getLists()
}

//查看详情
const checkDetail = (id: number) => {
    popRef.value.open({ id })
}
const handleClose = async (goods: any, id: number) => {
    await feedback.confirm(
        `确认结束：(${goods?.goods_name})的拼团活动吗？结束活动会使拼团中的拼团失败，请谨慎处理`
    )
    await PTOver({ id })
    getLists()
}
onMounted(() => {
    getLists()
})
</script>

<style lang="scss" scoped></style>
