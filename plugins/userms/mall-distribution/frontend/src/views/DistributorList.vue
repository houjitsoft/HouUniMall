<template>
  <div class="distributor-list">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>分销商管理</span>
          <el-button type="primary" @click="openCreate">新增分销员</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="分销商编号 / 姓名 / 手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待审核" :value="0" />
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column label="邀请码" min-width="170">
          <template #default="{ row }">
            <span class="code-cell">{{ row.distributorNo }}</span>
            <el-button link type="primary" @click="copyText(row.distributorNo, '邀请码已复制')">复制</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="姓名" width="110" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="userId" label="用户ID" width="110" />
        <el-table-column prop="levelId" label="等级" width="80" />
        <el-table-column prop="parentNo" label="上级邀请码" width="150">
          <template #default="{ row }">{{ row.parentNo || '-' }}</template>
        </el-table-column>
        <el-table-column prop="subCount" label="下级数" width="90" align="right" />
        <el-table-column prop="totalCommission" label="累计佣金" width="110" align="right" />
        <el-table-column prop="availableCommission" label="可提现" width="110" align="right" />
        <el-table-column prop="pendingCommission" label="待结算" width="110" align="right" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="加入时间" width="170" />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="success" :disabled="row.status !== 0" @click="handleAudit(row, 1)">通过</el-button>
            <el-button link type="danger" :disabled="row.status !== 0" @click="handleAudit(row, 2)">驳回</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'primary'" :disabled="row.status === 0" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无分销商" />
        </template>
      </el-table>

      <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :current-page="query[pageKey]"
        :page-size="query[sizeKey]"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="onCurrentChange"
        @size-change="onSizeChange"
      />
    </el-card>

    <!-- 新增 / 编辑 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分销员' : '新增分销员'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="form.userId" :disabled="isEdit" placeholder="商城用户 ID（数字）" />
          <div class="field-tip" v-if="!isEdit">必须是对应商城已有用户，一个用户只能有一个分销员身份</div>
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="11 位手机号" />
        </el-form-item>
        <el-form-item label="邀请码">
          <el-input v-model="form.distributorNo" :placeholder="isEdit ? '邀请码' : '留空自动生成'" />
          <div class="field-tip">可自定义（便于线下印制），必须全站唯一</div>
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="form.levelId" placeholder="留空挂默认等级" clearable style="width: 100%">
            <el-option v-for="lv in levels" :key="lv.id" :label="lv.name" :value="lv.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="上级邀请码">
          <el-input v-model="form.parentNo" placeholder="选填，填上级的分销商编号" />
          <div class="field-tip">填了即绑定上下级关系，上级可拿二级佣金；清空表示解除</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="正常" :value="1" />
            <el-option label="待审核" :value="0" />
            <el-option label="禁用" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { distributorApi, distributorLevelApi } from '../api/distribution'
import { runAction, useTable } from '../utils/useTable'

// distributor 读 page/size（不是 pageNum/pageSize）
const {
  loading, list, total, query, pageKey, sizeKey,
  load, search, reset, onCurrentChange, onSizeChange
} = useTable(distributorApi.list, {
  defaultQuery: { keyword: '', status: null },
  pageKeys: ['page', 'size']
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)
const levels = ref([])

const form = reactive({
  id: null,
  userId: '',
  realName: '',
  phone: '',
  distributorNo: '',
  levelId: null,
  parentNo: '',
  status: 1
})

const rules = {
  userId: [{ required: true, message: '请填写用户ID', trigger: 'blur' }],
  realName: [{ required: true, message: '请填写姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请填写手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

function statusText(status) {
  if (status === 0) return '待审核'
  if (status === 1) return '正常'
  if (status === 2) return '禁用'
  return status === null || status === undefined ? '-' : `状态${status}`
}

function statusTagType(status) {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'default'
}

async function copyText(text, tip) {
  if (!text) return
  try {
    await navigator.clipboard.writeText(String(text))
    ElMessage.success(tip || '已复制')
  } catch (e) {
    ElMessage.warning('复制失败，请手动选中复制')
  }
}

async function loadLevels() {
  try {
    const res = await distributorLevelApi.all({})
    levels.value = Array.isArray(res?.data) ? res.data : (res?.data?.list || [])
  } catch (e) {
    levels.value = []
  }
}

function resetForm() {
  Object.assign(form, {
    id: null, userId: '', realName: '', phone: '',
    distributorNo: '', levelId: null, parentNo: '', status: 1
  })
}

function openCreate() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    userId: row.userId,
    realName: row.realName || '',
    phone: row.phone || '',
    distributorNo: row.distributorNo || '',
    levelId: row.levelId || null,
    parentNo: row.parentNo || '',
    status: row.status == null ? 1 : row.status
  })
  dialogVisible.value = true
}

async function submit() {
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return
  saving.value = true
  try {
    if (isEdit.value) {
      const res = await distributorApi.edit({
        id: form.id,
        realName: form.realName,
        phone: form.phone,
        distributorNo: form.distributorNo,
        levelId: form.levelId,
        // 传空串表示解除上级
        parentNo: form.parentNo || '',
        status: form.status
      })
      if (res && res.code !== 1) {
        ElMessage.error(res.message || '保存失败')
        return
      }
      ElMessage.success('已保存')
    } else {
      const res = await distributorApi.create({
        userId: form.userId,
        realName: form.realName,
        phone: form.phone,
        distributorNo: form.distributorNo,
        levelId: form.levelId,
        parentNo: form.parentNo || '',
        status: form.status
      })
      if (res && res.code !== 1) {
        ElMessage.error(res.message || '创建失败')
        return
      }
      ElMessage.success(`已创建，邀请码：${res?.data?.distributorNo || ''}`)
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function handleAudit(row, status) {
  const action = status === 1 ? '通过' : '驳回'
  let remark = ''
  if (status === 2) {
    const res = await ElMessageBox.prompt('请填写驳回原因', '驳回分销商', {
      inputPattern: /\S+/,
      inputErrorMessage: '驳回原因不能为空'
    })
    remark = res.value
  } else {
    await ElMessageBox.confirm(`确认${action}分销商「${row.realName || row.distributorNo}」？`, '审核确认', { type: 'warning' })
  }
  await runAction(
    () => distributorApi.audit({ id: row.id, status, remark }),
    load,
    `已${action}`
  )
}

async function toggleStatus(row) {
  const next = row.status === 1 ? 2 : 1
  await runAction(
    () => distributorApi.updateStatus({ id: row.id, status: next }),
    load,
    next === 1 ? '已启用' : '已禁用'
  )
}

function handleReset() {
  reset({ keyword: '', status: null })
}

onMounted(() => {
  load()
  loadLevels()
})
</script>

<style scoped>
.card-header { display: flex; align-items: center; justify-content: space-between; }
.search-form { margin-bottom: 8px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.code-cell { font-family: var(--font-mono, monospace); margin-right: 6px; }
.field-tip { font-size: 12px; color: var(--el-text-color-secondary); line-height: 1.5; margin-top: 2px; }
</style>
