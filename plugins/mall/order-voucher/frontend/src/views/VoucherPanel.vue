<template>
  <main class="voucher-page">
    <header class="voucher-masthead">
      <div>
        <h1>优惠券</h1>
        <p class="voucher-lead">满 X 元减 Y 元的代金券模板：设置总量、每人限领与有效期，用户在店铺/商品页领取后下单抵扣。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增优惠券</el-button>
    </header>

    <el-card shadow="never">
      <el-form :inline="true" class="voucher-filter" @submit.prevent>
        <el-form-item label="券名称">
          <el-input v-model="filter.voucherTTitle" clearable style="width: 200px" @keyup.enter="fetchList" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.voucherTState" clearable placeholder="全部" style="width: 130px">
            <el-option label="生效中" :value="1" />
            <el-option label="已停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="fetchList">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rows" v-loading="loading" row-key="voucherTId">
        <el-table-column prop="voucherTId" label="ID" width="70" />
        <el-table-column prop="voucherTTitle" label="券名称" min-width="170" show-overflow-tooltip />
        <el-table-column label="面额 / 门槛" width="150" align="right">
          <template #default="{ row }">¥{{ row.voucherTPrice }} / 满¥{{ row.voucherTLimit }}</template>
        </el-table-column>
        <el-table-column label="发放 / 领取 / 使用" width="150" align="center">
          <template #default="{ row }">{{ row.voucherTTotal ?? '-' }} / {{ row.voucherTGiveout ?? 0 }} / {{ row.voucherTUsed ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="有效期" min-width="300">
          <template #default="{ row }">{{ fmt(row.voucherTStartDate) }} ~ {{ fmt(row.voucherTEndDate) }}</template>
        </el-table-column>
        <el-table-column label="范围" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="!row.shopId ? 'primary' : 'info'">{{ !row.shopId ? '全平台' : (row.shopName || '店铺') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="95">
          <template #default="{ row }">
            <el-tag size="small" :type="row.voucherTState === 1 ? 'success' : 'info'">{{ row.voucherTState === 1 ? '生效中' : '已停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.voucherTState === 1 ? 'warning' : 'success'" @click="toggleState(row)">
              {{ row.voucherTState === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="还没有优惠券" /></template>
      </el-table>

      <el-pagination
        class="voucher-pager"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="onPage"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.voucherTId ? '编辑优惠券' : '新增优惠券'" width="640px">
      <el-form label-position="top">
        <el-form-item label="券名称" required>
          <el-input v-model="form.voucherTTitle" maxlength="40" placeholder="例如：满200减30券" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="面额（元）" required>
              <el-input-number v-model="form.voucherTPrice" :min="0.01" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="使用门槛（满 X 元）" required>
              <el-input-number v-model="form.voucherTLimit" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="发放总量" required>
              <el-input-number v-model="form.voucherTTotal" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="有效期" required>
              <el-date-picker
                v-model="form.range"
                type="datetimerange"
                start-placeholder="开始"
                end-placeholder="结束"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="每人限领">
              <el-input-number v-model="form.voucherTEachlimit" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态">
              <el-switch v-model="form.voucherTState" :active-value="1" :inactive-value="0" active-text="生效" inline-prompt />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="券描述">
          <el-input v-model="form.voucherTDesc" type="textarea" :rows="2" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteTemplate, loadTemplates, saveTemplate, updateTemplateState } from '../api.js'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const dialogVisible = ref(false)
const filter = reactive({ voucherTTitle: '', voucherTState: null })

function emptyForm() {
  return {
    voucherTId: null,
    voucherTTitle: '',
    voucherTPrice: 10,
    voucherTLimit: 100,
    voucherTTotal: 1000,
    voucherTEachlimit: 1,
    voucherTState: 1,
    range: [],
    voucherTDesc: ''
  }
}

const form = reactive(emptyForm())

function fmt(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

async function fetchList() {
  loading.value = true
  try {
    const data = await loadTemplates({
      page: page.value,
      limit: pageSize.value,
      voucherTTitle: filter.voucherTTitle || undefined,
      voucherTState: filter.voucherTState ?? undefined
    })
    rows.value = data.list || []
    total.value = data.totalCount || 0
  } catch (error) {
    ElMessage.error(error?.message || '加载优惠券失败')
  } finally {
    loading.value = false
  }
}

function onPage(next) {
  page.value = next
  fetchList()
}

function resetFilter() {
  filter.voucherTTitle = ''
  filter.voucherTState = null
  page.value = 1
  fetchList()
}

function openCreate() {
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, emptyForm(), {
    voucherTId: row.voucherTId,
    voucherTTitle: row.voucherTTitle,
    voucherTPrice: Number(row.voucherTPrice),
    voucherTLimit: Number(row.voucherTLimit),
    voucherTTotal: row.voucherTTotal ?? 1000,
    voucherTEachlimit: row.voucherTEachlimit ?? 1,
    voucherTState: row.voucherTState === 1 ? 1 : 0,
    voucherTDesc: row.voucherTDesc || '',
    range: [fmt(row.voucherTStartDate), fmt(row.voucherTEndDate)]
  })
  dialogVisible.value = true
}

async function submit() {
  if (!form.voucherTTitle.trim()) { ElMessage.warning('请填写券名称'); return }
  if (!form.range || form.range.length !== 2) { ElMessage.warning('请选择有效期'); return }
  if (form.voucherTLimit < form.voucherTPrice) { ElMessage.warning('使用门槛不能低于面额'); return }
  saving.value = true
  try {
    await saveTemplate({
      voucherTId: form.voucherTId || undefined,
      voucherTTitle: form.voucherTTitle.trim(),
      voucherTPrice: form.voucherTPrice,
      voucherTLimit: form.voucherTLimit,
      voucherTTotal: form.voucherTTotal,
      voucherTEachlimit: form.voucherTEachlimit,
      voucherTState: form.voucherTState,
      voucherTStartDate: form.range[0],
      voucherTEndDate: form.range[1],
      voucherTDesc: form.voucherTDesc || undefined
    })
    dialogVisible.value = false
    ElMessage.success('已保存')
    await fetchList()
  } catch (error) {
    ElMessage.error(error?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function toggleState(row) {
  try {
    await updateTemplateState(row.voucherTId, row.voucherTState === 1 ? 0 : 1)
    ElMessage.success(row.voucherTState === 1 ? '已停用' : '已启用')
    await fetchList()
  } catch (error) {
    ElMessage.error(error?.message || '操作失败')
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除优惠券「${row.voucherTTitle}」？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await deleteTemplate(row.voucherTId)
    ElMessage.success('已删除')
    await fetchList()
  } catch (error) {
    ElMessage.error(error?.message || '删除失败')
  }
}

onMounted(fetchList)
</script>

<style scoped>
.voucher-page { padding: 16px; }
.voucher-masthead { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; margin-bottom: 16px; }
.voucher-masthead h1 { margin: 0 0 4px; font-size: 20px; }
.voucher-lead { margin: 0; color: var(--el-text-color-secondary); font-size: 13px; max-width: 720px; line-height: 1.6; }
.voucher-filter { margin-bottom: 4px; }
.voucher-pager { margin-top: 16px; justify-content: flex-end; }
</style>
