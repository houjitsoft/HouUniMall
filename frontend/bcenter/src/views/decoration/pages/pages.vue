<template>
    <el-card class="!border-none" shadow="never">
        <div class="flex justify-between items-center">
            <el-button type="primary" @click="handleAdd">新增页面</el-button>
            <el-form ref="formRef" :model="queryParams" class="flex items-center">
                <el-form-item label="页面名称">
                    <el-input
                        class="w-[280px]"
                        v-model="queryParams.name"
                        placeholder="请输入页面名称"
                        clearable
                        @keyup.enter="resetPage"
                    />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="resetPage" class="ml-2">查询</el-button>
                </el-form-item>
            </el-form>
        </div>
        <el-table size="large" v-loading="pager.loading" :data="pager.lists" class="mt-6">
            <el-table-column label="页面名称" prop="name" min-width="140">
                <template #default="{ row }">
                    <span class="mr-2">
                        {{ row.name }}
                    </span>
                    <el-tag type="success" v-if="row.isHome">首页</el-tag>
                </template>
            </el-table-column>
            <!-- <el-table-column label="预览" prop="name" min-width="140" /> -->
            <el-table-column label="更新时间" prop="updateTime" min-width="140" />
            <el-table-column label="创建时间" prop="createTime" min-width="140" />
            <el-table-column label="操作" prop="" min-width="140">
                <template #default="{ row }">
                    <el-button
                        type="primary"
                        link
                        v-if="!row.isHome"
                        @click="handlepageHome(row.id)"
                        >设为首页</el-button
                    >
                    <el-button type="primary" link @click="handleEdit(row.id)">装修</el-button>
                    <!-- <el-button type="primary" link>复制</el-button> -->
                    <el-button type="primary" link @click="handleDel(row.id)" v-if="!row.isHome"
                        >删除</el-button
                    >
                </template>
            </el-table-column>
        </el-table>
        <div class="flex justify-end mt-4">
            <pagination v-model="pager" @change="getLists" />
        </div>
    </el-card>
</template>
<script lang="ts" setup name="decorationPages">
import { usePaging } from '@/hooks/usePaging'
import { getHomeLists, delPages, setpageHome } from '@/api/decoration'
import feedback from '@/utils/feedback'

const router = useRouter()

const queryParams = reactive<any>({
    name: ''
})
const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: getHomeLists,
    params: queryParams
})
const handlepageHome = async (id: number) => {
    await setpageHome({ id })
    feedback.msgSuccess('已设为首页')
    getLists()
}
const handleAdd = () => {
    router.push('/decoration/pages')
}
const handleEdit = (val: number) => {
    router.push(`/decoration/pages?id=${val}`)
}
const handleDel = async (id: number) => {
    await feedback.confirm('确定要删除？')

    await delPages({ id })
    getLists()
}
getLists()
</script>
<style lang="scss" scoped>
:deep(.el-form-item) {
    margin-bottom: 0;
}
</style>
