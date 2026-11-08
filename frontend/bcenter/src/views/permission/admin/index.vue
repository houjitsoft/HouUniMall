<template>
    <div class="admin">
        <el-card class="!border-none" shadow="never">
            <el-form class="mb-[-16px]" :model="formData" inline>
                <el-form-item label="统一账号">
                    <el-input v-model="formData.account" class="w-[280px]" clearable @keyup.enter="resetPage" />
                </el-form-item>
                <el-form-item label="管理员名称">
                    <el-input v-model="formData.name" class="w-[280px]" clearable @keyup.enter="resetPage" />
                </el-form-item>
                <el-form-item label="角色">
                    <el-select v-model="formData.role_id" class="w-[280px]" clearable>
                        <el-option label="全部" value="" />
                        <el-option v-for="item in optionsData.role" :key="item.id" :label="item.name" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="resetPage">查询</el-button>
                    <el-button @click="resetParams">重置</el-button>
                </el-form-item>
            </el-form>
        </el-card>
        <el-card v-loading="pager.loading" class="mt-4 !border-none" shadow="never">
            <el-button v-perms="['auth.admin/add']" type="primary" @click="handleAdd">新增管理员</el-button>
            <div class="mt-4">
                <el-table :data="pager.lists" size="large">
                    <el-table-column label="ID" prop="id" min-width="60" />
                    <el-table-column label="统一账号" min-width="160">
                        <template #default="{ row }">{{ row.login_account || row.loginAccount || '-' }}</template>
                    </el-table-column>
                    <el-table-column label="名称" prop="name" min-width="120" />
                    <el-table-column label="角色" prop="role_arr" min-width="140" show-tooltip-when-overflow />
                    <el-table-column label="状态" min-width="90">
                        <template #default="{ row }">
                            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="changeStatus(row)" />
                        </template>
                    </el-table-column>
                    <el-table-column label="最近登录" prop="login_last_time" min-width="180" />
                    <el-table-column label="操作" width="150" fixed="right">
                        <template #default="{ row }">
                            <el-button v-perms="['auth.admin/edit']" type="primary" link @click="handleEdit(row)">编辑</el-button>
                            <el-button v-if="row.isSuper !== 1 && row.super !== 1" v-perms="['auth.admin/delete']" type="danger" link @click="handleDelete(row.id)">删除</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
            <div class="flex justify-end mt-4"><pagination v-model="pager" @change="getLists" /></div>
        </el-card>
        <edit-popup v-if="showEdit" ref="editRef" @success="getLists" @close="showEdit = false" />
    </div>
</template>

<script lang="ts" setup name="admin">
import { adminEdit, adminLists, adminDelete } from '@/api/perms/admin'
import { roleAll } from '@/api/perms/role'
import { useDictOptions } from '@/hooks/useDictOptions'
import { usePaging } from '@/hooks/usePaging'
import feedback from '@/utils/feedback'
import EditPopup from './edit.vue'

const editRef = shallowRef<InstanceType<typeof EditPopup>>()
const formData = reactive({ account: '', name: '', role_id: '' })
const showEdit = ref(false)
const { pager, getLists, resetParams, resetPage } = usePaging({ fetchFun: adminLists, params: formData })

const changeStatus = (row: any) => {
    adminEdit({ id: row.id, userId: row.user_id ?? row.userId, name: row.name, role_id: row.role_id, status: row.status, login_all: row.login_all }).finally(getLists)
}
const handleAdd = async () => { showEdit.value = true; await nextTick(); editRef.value?.open('add') }
const handleEdit = async (row: any) => { showEdit.value = true; await nextTick(); editRef.value?.open('edit'); editRef.value?.setFormData(row) }
const handleDelete = async (id: number) => { await feedback.confirm('确定删除该管理员吗？'); await adminDelete({ id }); getLists() }

const { optionsData } = useDictOptions<{ role: any[] }>({ role: { api: roleAll } })
onMounted(getLists)
</script>
