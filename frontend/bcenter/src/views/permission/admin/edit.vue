<template>
    <div class="edit-popup">
        <popup ref="popupRef" :title="popupTitle" :async="true" width="560px" @confirm="handleSubmit" @close="handleClose">
            <el-form ref="formRef" :model="formData" label-width="90px" :rules="formRules">
                <el-form-item label="统一账号" prop="user_id">
                    <el-select v-model="formData.user_id" class="w-full" filterable remote clearable :remote-method="searchAccounts" :loading="accountLoading" :disabled="mode === 'edit'" placeholder="搜索手机号或邮箱">
                        <el-option v-for="item in accounts" :key="item.userId" :label="item.displayName || item.loginAccount" :value="Number(item.userId)" />
                    </el-select>
                </el-form-item>
                <el-form-item label="头像"><material-picker v-model="formData.face" :limit="1" /></el-form-item>
                <el-form-item label="名称" prop="name"><el-input v-model="formData.name" clearable /></el-form-item>
                <el-form-item label="部门"><el-tree-select v-model="formData.dept_id" class="w-full" :data="optionsData.dept" clearable multiple node-key="id" :props="{ value: 'id', label: 'name', children: 'sub' }" check-strictly /></el-form-item>
                <el-form-item label="岗位"><el-select v-model="formData.jobs_id" class="w-full" clearable multiple><el-option v-for="item in optionsData.jobs" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
                <el-form-item label="角色" prop="role_id"><el-select v-model="formData.role_id" class="w-full" multiple clearable :disabled="formData.super === 1"><el-option v-for="item in optionsData.role" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
                <el-form-item v-if="formData.root !== 1" label="状态"><el-radio-group v-model="formData.status"><el-radio :label="1">正常</el-radio><el-radio :label="0">停用</el-radio></el-radio-group></el-form-item>
                <el-form-item label="允许多处登录"><el-switch v-model="formData.login_all" :active-value="1" :inactive-value="0" /></el-form-item>
            </el-form>
        </popup>
    </div>
</template>

<script lang="ts" setup>
import type { FormInstance } from 'element-plus'
import Popup from '@/components/popup/index.vue'
import { useDictOptions } from '@/hooks/useDictOptions'
import { adminAccountOptions, adminAdd, adminDetail, adminEdit } from '@/api/perms/admin'
import { roleAll } from '@/api/perms/role'
import { jobsAll } from '@/api/org/post'
import { deptLists } from '@/api/org/department'

const emit = defineEmits(['success', 'close'])
const formRef = shallowRef<FormInstance>()
const popupRef = shallowRef<InstanceType<typeof Popup>>()
const mode = ref('add')
const popupTitle = computed(() => mode.value === 'edit' ? '编辑管理员' : '新增管理员')
const formData = reactive<any>({ id: '', user_id: null, name: '', dept_id: [], jobs_id: [], role_id: [], face: '', status: 1, login_all: 1, root: 0, super: 0 })
const accounts = ref<any[]>([])
const accountLoading = ref(false)
const formRules = reactive<any>({
    user_id: [{ required: true, message: '请选择统一账号', trigger: 'change' }],
    name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
    role_id: [{ type: 'array', required: true, message: '请选择角色', trigger: 'change' }]
})
const { optionsData } = useDictOptions<{ role: any[]; jobs: any[]; dept: any[] }>({ role: { api: roleAll }, jobs: { api: jobsAll }, dept: { api: deptLists } })
const searchAccounts = async (keyword = '') => {
    accountLoading.value = true
    try { const res = await adminAccountOptions({ keyword }); accounts.value = res?.data || [] }
    finally { accountLoading.value = false }
}
const handleSubmit = async () => {
    await formRef.value?.validate()
    const payload = { ...formData, userId: formData.user_id, loginAll: formData.login_all, deptId: formData.dept_id, jobsId: formData.jobs_id, roleId: formData.role_id }
    if (mode.value === 'edit') await adminEdit(payload); else await adminAdd(payload)
    popupRef.value?.close(); emit('success')
}
const open = async (type = 'add') => { mode.value = type; if (type === 'add') await searchAccounts(); popupRef.value?.open() }
const setFormData = async (row: any) => {
    const data = await adminDetail({ id: row.id })
    Object.assign(formData, data, { user_id: data.user_id ?? data.userId, login_all: data.login_all ?? data.loginAll, dept_id: data.dept_id ?? data.deptId ?? [], jobs_id: data.jobs_id ?? data.jobsId ?? [], role_id: data.role_id ?? data.roleId ?? [] })
}
const handleClose = () => emit('close')
defineExpose({ open, setFormData })
</script>
