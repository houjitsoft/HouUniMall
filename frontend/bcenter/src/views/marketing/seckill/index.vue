<template>
    <div class="goods-lists">
        <el-card class="!border-none" shadow="never">
            <el-form ref="formRef" class="mb-[-16px]" :model="queryParams" :inline="true">
                <el-form-item label="商品名称">
                    <el-input
                        class="w-[280px]"
                        v-model="queryParams.goods_name"
                        placeholder="请输入名称"
                        clearable
                        @keyup.enter="resetPage"
                    />
                </el-form-item>
                <el-form-item label="活动状态">
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
        <el-card class="!border-none mt-4" :body-style="{ 'padding-top': '10px' }" shadow="never">
            <!-- 选项卡 -->
            <el-tabs v-model="queryParams.audit_status" @tab-change="changeTabs">
                <template v-for="(tabItem, tabIndex) in tabsParams.TabsEnumMap" :key="tabIndex">
                    <el-tab-pane
                        :label="`${tabItem.label}(${pager.extend[tabItem.type] || 0})`"
                        :name="tabItem.name"
                    />
                </template>
            </el-tabs>
            <el-button
                type="primary"
                @click="handleSeckillPopup('add', 0)"
            >
                新增秒杀商品
            </el-button>

            <!-- 表格 -->
            <el-table size="large" v-loading="pager.loading" :data="pager.lists" class="mt-4">
                <el-table-column label="秒杀商品" min-width="240">
                    <template #default="{ row }">
                        <div class="flex items-center">
                            <el-image
                                fit="cover"
                                :src="row.goods.image"
                                class="flex-none w-[58px] h-[58px]"
                            />
                            <div class="ml-4 overflow-hidden">
                                <el-tooltip effect="dark" :content="row.goods.name" placement="top">
                                    <div class="text-base line-2">
                                        {{ row.goods.name }}
                                    </div>
                                </el-tooltip>
                            </div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="秒杀价" prop="price" min-width="160">
                    <template #default="{ row }"> ¥{{ row.price_text }} </template>
                </el-table-column>
                <el-table-column label="秒杀数据" prop="price" min-width="160">
                    <template #default="{ row }">
                        <div>秒杀订单：{{ row?.order_count?.count || '0' }}单</div>
                        <div>秒杀销售量：{{ row?.order_count?.sale_num || '0' }}件</div>
                        <div>秒杀销售额：{{ row?.order_count?.sale_money || '0' }}元</div>
                    </template>
                </el-table-column>
                <el-table-column label="活动状态" prop="price" min-width="160">
                    <template #default="{ row }">
                        <el-tag v-if="row?.status == 0" type="danger">未开始</el-tag>
                        <el-tag v-if="row?.status == 1" type="success">进行中</el-tag>
                        <el-tag v-if="row?.status == 2" type="info">已结束</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="审核状态" min-width="140">
                    <template #default="{ row }">
                        <div>{{ row.audit_status_text || '-' }}</div>
                        <el-tooltip
                            effect="dark"
                            placement="top"
                            class="mt-2"
                            v-if="row.audit_content"
                            :content="row.audit_content"
                        >
                            <div class="text-base line-2">
                                原因:{{ row.audit_content }}
                            </div>
                        </el-tooltip>
                    </template>
                </el-table-column>
                <el-table-column label="活动时间" prop="create_time" min-width="180" />
                <el-table-column label="操作" width="250" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            v-perms="['mall.ms/detail']"
                            type="primary"
                            link
                            @click="handleSeckillPopup('detail', row.id)"
                        >
                            详情
                        </el-button>

                        <el-button
                            v-perms="['mall.ms/add', 'mall.ms/edit']"
                            type="primary"
                            link
                            v-if="row.can_edit"
                            @click="handleSeckillPopup('edit', row.id)"
                        >

                            编辑
                        </el-button>

                        <el-button
                            v-perms="['mall.ms/submit']"
                            type="primary"
                            link
                            v-if="row.can_submit"
                            @click="handleSubmit(row.id)"
                        >
                            提交审核
                        </el-button>

                        <el-button
                            v-perms="['mall.ms/open']"
                            type="primary"
                            link
                            v-if="row.can_open"
                            @click="handleOpen(row.id)"
                        >
                           开启活动
                        </el-button>

                        <el-button
                            v-perms="['mall.ms/close']"
                            type="primary"
                            link
                            v-if="row.can_close"
                            @click="handleClose(row)"
                        >
                            结束活动
                        </el-button>

                        <el-button
                            v-perms="['mall.ms/delete']"
                            type="danger"
                            link
                            @click="handleDelete(row)"
                        >
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div class="flex justify-end mt-4">
                <pagination v-model="pager" @change="getLists" />
            </div>
        </el-card>

<!--        使用秒杀编辑/新增组件-->
        <SeckillEdit v-if="showSeckill" ref="seckillRef" @success="() => {
            getLists();
            showSeckill = false;
        }"></SeckillEdit>
    </div>
</template>
<script lang="ts" setup name="SeckillLists">
import { usePaging } from '@/hooks/usePaging'
import { getRoutePath } from '@/router'
import { getSeckillLists, apiSeckillSubmit, apiSeckillOpen, apiSeckillClose, apiSeckillDelete } from '@/api/marketing/seckill'
import type { SeckillListsQueryType } from '@/api/marketing/seckill.d'
import feedback from '@/utils/feedback'

// 组件
import SeckillEdit from "./edit.vue"

// Tab类型
enum TabsEnum {
    ALL = 'all',
    WAIT_SUBMIT = 'wait_submit',
    WAIT = 'wait',
    SUCCESS = 'success',
    FAILED = 'failed'
}

const seckillRef = shallowRef()
const showSeckill = shallowRef(false)
const tabsParams = reactive({
    TabsEnumMap: [
        {
            label: '全部',
            name: '',
            type: TabsEnum.ALL
        },
        {
            label: '待提交',
            name: '0',
            type: TabsEnum.WAIT_SUBMIT
        },
        {
            label: '待审核',
            name: '10',
            type: TabsEnum.WAIT
        },
        {
            label: '审核通过',
            name: '20',
            type: TabsEnum.SUCCESS
        },
        {
            label: '审核失败',
            name: '30',
            type: TabsEnum.FAILED
        }
    ]
})

const queryParams = reactive<SeckillListsQueryType>({
    goods_name: '',
    audit_status: '',
    status: '',
    start_time: '',
    end_time: ''
})

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: getSeckillLists,
    params: queryParams
})

const changeTabs = (name: string) => {
    queryParams.audit_status = name
    getLists()
}

const handleSeckillPopup = async (mode: string, id: number) => {
    try {
        showSeckill.value = true
        await nextTick()
        seckillRef.value.open(mode, id)
    } catch (error) {
        console.log(error)
    }
}

const handleSubmit = async (id: number) => {
    try {
        await feedback.confirm('审核过程中将不能修改秒杀活动，是否确认提交？', '提交审核')
        await apiSeckillSubmit({ id })
        getLists()
    } catch (error) {
        console.log('提交审核商品失败=>', error)
    }
}

const handleOpen = async (id: number) => {
    try {
        await feedback.confirm(`开启活动将不能修改秒杀活动，是否确认开启？`, '开启活动')
        await apiSeckillOpen({ id })
        getLists()
    } catch (error) {
        console.log('开启活动失败=>', error)
    }
}

const handleClose = async (row: any) => {
    try {
        await feedback.confirm(`确认结束：(${row.goods.name}商品)的秒杀活动吗？请谨慎处理！`, '结束活动')
        await apiSeckillClose({ id: row.id })
        getLists()
    } catch (error) {
        console.log('结束活动失败=>', error)
    }
}

const handleDelete = async (row: any) => {
    try {
        await feedback.confirm(`确认删除：(${row.goods.name}商品)的秒杀活动吗？请谨慎操作！`, '删除活动')
        await apiSeckillDelete({ id: row.id })
        getLists()
    } catch (error) {
        console.log('删除活动失败=>', error)
    }
}

getLists()
</script>
