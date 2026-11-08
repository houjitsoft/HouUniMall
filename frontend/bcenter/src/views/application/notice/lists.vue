<template>
    <div>
        <el-card class="!border-none mt-4" shadow="never">
            <el-table size="large" :data="pager.lists" v-loading="pager.loading">
                <el-table-column label="标题" prop="title" min-width="120" />
                <el-table-column label="优先级" prop="priority_name" min-width="120">
                    <template #default="{ row }">
                        <el-tag type="danger" v-if="row.priority == 1">紧急</el-tag>
                        <el-tag type="primary" v-if="row.priority == 2">重要</el-tag>
                        <el-tag type="warning" v-if="row.priority == 3">提示</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="创建时间" prop="create_time" min-width="160" />
                <el-table-column label="操作" min-width="80" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            v-perms="['notice.notice/set']"
                            type="primary"
                            link
                            @click="handleEdit(row)"
                        >
                            查看
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div class="flex mt-4 justify-end">
                <pagination v-model="pager" @change="getLists" />
            </div>
        </el-card>

        <edit-popup v-if="showEdit" ref="editRef" @success="getLists" @close="showEdit = false" />
    </div>
</template>
<script lang="ts" setup name="notice">
import { NoticeLists } from '@/api/notice'
import { usePaging } from '@/hooks/usePaging'
import { getRoutePath } from '@/router'
import EditPopup from './edit.vue'
import feedback from '@/utils/feedback'

const editRef = shallowRef<InstanceType<typeof EditPopup>>()
const queryParams = ref({
    title: ''
})
const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: NoticeLists,
    params: queryParams.value
})
const mode = ref('add')

const showEdit = ref(false)

const handleAdd = async () => {
    showEdit.value = true
    await nextTick()
    editRef.value?.open('add')
}
const handleEdit = async (data: any) => {
    showEdit.value = true
    await nextTick()
    editRef.value?.open('edit')
    editRef.value?.getDetail(data)
}

onActivated(() => {
    getLists()
})

getLists()
</script>
<style scoped lang="scss">
:deep(.el-form-item) {
    margin-bottom: 0;
}
</style>
