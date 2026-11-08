<template>
    <div class="article-lists">
        <el-card class="!border-none mt-4" shadow="never">
            <div class="flex mb-4">
                <router-link
                    :to="{
                        path: getRoutePath('printer.printerTemplate/edit')
                    }"
                >
                    <el-button type="primary"> 新增模版 </el-button>
                </router-link>
            </div>
            <el-table size="large" v-loading="pager.loading" :data="pager.lists">
                <el-table-column label="分类名称" min-width="100" property="name">
                </el-table-column>
                <el-table-column property="type_name" label="模版类型" max-width="266" />
                <el-table-column property="printer_use" label="是否显示" max-width="266">
                    <template #default="{ row }">
                        <span v-if="row.printer_use == 1">已使用</span>
                        <span v-else>未使用</span>
                    </template>
                </el-table-column>
                <el-table-column label="创建时间" prop="create_time" min-width="120" />
                <el-table-column label="操作" width="120" fixed="right">
                    <template #default="{ row }">
                        <router-link
                            :to="{
                                path: getRoutePath('printer.printerTemplate/edit'),
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
import { gettemLists, apitemDelete } from '@/api/marketing/ticket'
import { usePaging } from '@/hooks/usePaging'
import feedback from '@/utils/feedback'
import { ref, shallowRef, onActivated } from 'vue'
import { getRoutePath } from '@/router'

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: gettemLists
})

const expand = ref<string[]>([])

const requestPagination = () => {
    getLists()
    expand.value = []
}

const handleDelete = async (id: number) => {
    try {
        await feedback.confirm('确定要删除？')
        await apitemDelete({ id })
        requestPagination()
    } catch (error) {
        console.log(error)
    }
}

onActivated(() => {
    requestPagination()
})

requestPagination()
</script>
