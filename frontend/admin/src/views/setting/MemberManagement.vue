<template>
  <div class="member-management">
    <PermissionTabs />

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>会员管理</span>
          <span class="sub-title">为 Cloud 平台会员账号分配角色组（数据来自 hou_user_info）</span>
        </div>
      </template>

      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="账号/手机">
          <el-input v-model="searchForm.username" placeholder="用户名或手机号" clearable />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="searchForm.realName" placeholder="昵称" clearable />
        </el-form-item>
        <el-form-item label="角色组">
          <el-select v-model="searchForm.roleId" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="role in roleList" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="注册终端">
          <el-select v-model="searchForm.registerTerminal" placeholder="全部" clearable style="width: 140px">
            <el-option label="商城" value="mall" />
            <el-option label="houcloud" value="houcloud" />
            <el-option label="hougeo" value="hougeo" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="memberList" v-loading="loading" stripe border>
        <el-table-column prop="id" label="员工ID" width="180" show-overflow-tooltip />
        <el-table-column prop="username" label="账号" width="140" />
        <el-table-column prop="realName" label="昵称" width="120" />
        <el-table-column prop="mobile" label="手机号" width="130" />
        <el-table-column label="注册终端" width="110">
          <template #default="{ row }">
            <el-tag
              size="small"
              :type="registerTerminalTagType(row.registerTerminal)"
              effect="plain"
            >
              {{ row.registerTerminalText || registerTerminalLabel(row.registerTerminal) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色组" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="name in (row.roles || [])" :key="name" size="small" type="primary" class="role-tag">
              {{ name }}
            </el-tag>
            <span v-if="!row.roles || row.roles.length === 0" class="no-role">未分配</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.statusText || (row.status === 1 ? '正常' : '禁用') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="170" />
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openRoleDialog(row)">分配角色</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalCount"
          :page-sizes="[10, 15, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadMemberList"
          @current-change="loadMemberList"
        />
      </div>
    </el-card>

    <el-dialog v-model="roleDialogVisible" title="分配角色组" width="520px" :close-on-click-modal="false">
      <div class="dialog-user">
        <p><strong>账号：</strong>{{ currentMember.username }}</p>
        <p><strong>昵称：</strong>{{ currentMember.realName || '-' }}</p>
      </div>
      <el-checkbox-group v-model="selectedRoleIds" class="role-check-group">
        <el-checkbox v-for="role in roleList" :key="role.id" :label="role.id" :value="role.id">
          {{ role.name }}
          <span v-if="role.desc" class="role-desc">（{{ role.desc }}）</span>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveMemberRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import PermissionTabs from '@/components/setting/PermissionTabs.vue'
import { refreshMenusAfterPermissionChange } from '@/utils/userMenusCache'

const loading = ref(false)
const saving = ref(false)
const currentPage = ref(1)
const pageSize = ref(15)
const totalCount = ref(0)
const memberList = ref([])
const roleList = ref([])

const searchForm = reactive({
  username: '',
  realName: '',
  roleId: null,
  status: null,
  registerTerminal: null
})

const roleDialogVisible = ref(false)
const currentMember = ref({})
const selectedRoleIds = ref([])

const registerTerminalLabel = (code) => {
  if (code === 'mall') return '商城'
  if (code === 'houcloud') return 'houcloud'
  if (code === 'hougeo') return 'hougeo'
  return code || '-'
}

const registerTerminalTagType = (code) => {
  if (code === 'mall') return 'warning'
  if (code === 'houcloud') return 'primary'
  if (code === 'hougeo') return 'success'
  return 'info'
}

const loadRoleList = async () => {
  try {
    const res = await request.get('/web/permission/role/all')
    if (res.code === 1 || res.code === 200) {
      roleList.value = res.data || []
    }
  } catch (e) {
    console.error('加载角色列表失败', e)
  }
}

const loadMemberList = async () => {
  try {
    loading.value = true
    const res = await request.post('/web/permission/user/list', {
      pageNo: currentPage.value,
      pageSize: pageSize.value,
      username: searchForm.username || undefined,
      realName: searchForm.realName || undefined,
      roleId: searchForm.roleId || undefined,
      status: searchForm.status ?? undefined,
      registerTerminal: searchForm.registerTerminal || undefined
    })
    if (res.code === 1 || res.code === 200) {
      const pageData = res.data || {}
      memberList.value = pageData.records || res.records || []
      totalCount.value = pageData.total ?? res.total ?? 0
    } else {
      ElMessage.error(res.message || '加载会员列表失败')
      memberList.value = []
      totalCount.value = 0
    }
  } catch (e) {
    console.error('加载会员列表失败', e)
    ElMessage.error('加载会员列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadMemberList()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.roleId = null
  searchForm.status = null
  searchForm.registerTerminal = null
  handleSearch()
}

const openRoleDialog = async (row) => {
  currentMember.value = row
  selectedRoleIds.value = []
  try {
    const res = await request.post('/web/permission/user/roles', { userId: String(row.id) })
    if (res.code === 1 || res.code === 200) {
      selectedRoleIds.value = res.data || []
      roleDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取会员角色失败')
    }
  } catch (e) {
    console.error('获取会员角色失败', e)
    ElMessage.error('获取会员角色失败')
  }
}

const saveMemberRoles = async () => {
  try {
    saving.value = true
    const res = await request.post('/web/permission/user/roles/set', {
      userId: String(currentMember.value.id),
      roleIds: selectedRoleIds.value.map((id) => Number(id))
    })
    if (res.code === 1 || res.code === 200) {
      ElMessage.success('角色分配成功')
      roleDialogVisible.value = false
      loadMemberList()
      refreshMenusAfterPermissionChange().catch(() => {})
    } else {
      ElMessage.error(res.message || '角色分配失败')
    }
  } catch (e) {
    console.error('角色分配失败', e)
    ElMessage.error('角色分配失败')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadRoleList()
  await loadMemberList()
})
</script>

<style scoped>
.member-management {
  padding: 0;
}

.card-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.sub-title {
  font-size: 13px;
  color: #909399;
}

.search-form {
  margin-bottom: 16px;
}

.role-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}

.no-role {
  color: #909399;
  font-size: 13px;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.dialog-user {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.dialog-user p {
  margin: 4px 0;
}

.role-check-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.role-desc {
  color: #909399;
  font-size: 12px;
}
</style>
