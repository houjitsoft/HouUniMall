<template>
    <div class="article-lists">
        <el-card class="!border-none mt-4" shadow="never">
            <div class="flex mb-4">
                <router-link
                    :to="{
                        path: getRoutePath('printer.printer/add:edit')
                    }"
                >
                    <el-button type="primary"> 新增打印机 </el-button>
                </router-link>
            </div>
            <el-table size="large" v-loading="pager.loading" :data="pager.lists">
                <el-table-column label="打印机名称" min-width="100" property="name">
                </el-table-column>
                <el-table-column property="brand_text" label="设备名称" max-width="266" />
                <el-table-column property="brand_text" label="打印小票" max-width="266">
                    <template #default="{ row }">
                        <div v-if="row.order_print.order_create.status == 1">
                            下单打印（{{ row.order_print.order_create.nums }}联）
                        </div>
                        <div v-if="row.order_print.order_pay.status == 1">
                            付款后打印（{{ row.order_print.order_pay.nums }}联）
                        </div>
                        <div v-if="row.order_print.self_print.status == 1">
                            手动打印（{{ row.order_print.self_print.nums }}联）
                        </div>
                    </template>
                </el-table-column>
                <el-table-column property="printer_use" label="状态" max-width="266">
                    <template #default="{ row }">
                        <el-switch
                            :active-value="1"
                            :inactive-value="0"
                            v-model="row.status"
                            @click="handlestatus(row.id, row.status)"
                        ></el-switch>
                    </template>
                </el-table-column>
                <el-table-column label="创建时间" prop="create_time" min-width="120" />
                <el-table-column label="操作" width="120" fixed="right">
                    <template #default="{ row }">
                        <router-link
                            :to="{
                                path: getRoutePath('printer.printer/add:edit'),
                                query: {
                                    id: row.id
                                }
                            }"
                        >
                            <el-button v-perms="['article.articleCate/edit']" type="primary" link>
                                编辑
                            </el-button>
                        </router-link>
                        <el-button
                            v-perms="['article.article/delete']"
                            type="danger"
                            link
                            @click="handleDelete(row.id)"
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
    </div>
</template>
<script lang="ts" setup>
import { getprinterists, apiprinterDelete, apistatusprint } from '@/api/marketing/ticket'
import { usePaging } from '@/hooks/usePaging'
import feedback from '@/utils/feedback'
import { ref, shallowRef, onActivated } from 'vue'
import { getRoutePath } from '@/router'

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: getprinterists
})

const expand = ref<string[]>([])

const requestPagination = () => {
    getLists()
    expand.value = []
}

const handleDelete = async (id: number) => {
    try {
        await feedback.confirm('确定要删除？')
        await apiprinterDelete({ id })
        requestPagination()
    } catch (error) {
        console.log(error)
    }
}
const handlestatus = async (id: number, status: number) => {
    await apistatusprint({ id: id, status: status })
    requestPagination()
}

onActivated(() => {
    requestPagination()
})

requestPagination()
</script>
