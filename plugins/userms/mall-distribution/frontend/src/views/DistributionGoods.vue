<template>
  <div class="distribution-goods">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>分销商品</span>
          <div class="header-actions">
            <span class="header-tip">按商品覆盖全局佣金比例；未配置的商品走「佣金规则」的默认比例</span>
            <el-button type="primary" @click="openCreate">新增分销商品</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="query.goodsName" placeholder="按商品名称搜索" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="goodsId" label="商品ID" width="100" />
        <el-table-column prop="goodsName" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="佣金比例" width="120" align="right">
          <template #default="{ row }">{{ rateText(row.commissionRate) }}</template>
        </el-table-column>
        <el-table-column label="预估佣金（100 元订单）" width="180" align="right">
          <template #default="{ row }">¥{{ (Number(row.commissionRate || 0) * 100).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'info'">
              {{ Number(row.status) === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑比例</el-button>
            <el-button link :type="Number(row.status) === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ Number(row.status) === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="还没有分销商品，新增后该商品才参与佣金计算" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分销商品' : '新增分销商品'" width="520px">
      <el-form label-position="top">
        <el-form-item label="商品 ID" required>
          <el-input v-model="form.goodsId" :disabled="Boolean(form.id)" placeholder="商城商品 ID" />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="form.goodsName" maxlength="120" placeholder="用于后台识别，可与商品标题一致" />
        </el-form-item>
        <el-form-item label="佣金比例（%）" required>
          <el-input-number v-model="form.ratePercent" :min="0" :max="100" :precision="2" :step="0.5" controls-position="right" style="width: 200px" />
          <span class="form-hint">例如填 8.5 表示订单实付的 8.5%</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="店铺 ID（平台统一配置留 0）">
          <el-input v-model="form.shopId" placeholder="0" />
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
import { distributionGoodsApi } from '../api/distribution'
import { runAction, useTable } from '../utils/useTable'

const {
  loading, list, total, query, pageKey, sizeKey,
  load, search, reset, onCurrentChange, onSizeChange
} = useTable(distributionGoodsApi.list, {
  defaultQuery: { goodsName: '', status: null, shopId: null },
  pageKeys: ['page', 'size']
})

const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({ id: null, goodsId: '', goodsName: '', ratePercent: 0, status: 1, shopId: '0' })

function rateText(value) {
  const percent = Number(value || 0) * 100
  return `${Number.isInteger(percent) ? percent : percent.toFixed(2)}%`
}

function openCreate() {
  Object.assign(form, { id: null, goodsId: '', goodsName: '', ratePercent: 0, status: 1, shopId: '0' })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    goodsId: String(row.goodsId ?? ''),
    goodsName: row.goodsName || '',
    ratePercent: Number(row.commissionRate || 0) * 100,
    status: Number(row.status) === 0 ? 0 : 1,
    shopId: String(row.shopId ?? '0')
  })
  dialogVisible.value = true
}

async function submit() {
  if (!String(form.goodsId).trim()) {
    ElMessage.warning('请填写商品 ID')
    form.goodsId = String(form.goodsId || '').trim()
    return
  }
  saving.value = true
  try {
    const payload = {
      goodsId: Number(form.goodsId),
      goodsName: form.goodsName || '',
      commissionRate: Number((Number(form.ratePercent) / 100).toFixed(4)),
      status: form.status,
      shopId: Number(form.shopId || 0)
    }
    await runAction(
      () => (form.id ? distributionGoodsApi.update({ ...payload, id: form.id }) : distributionGoodsApi.create(payload)),
      load,
      form.id ? '分销商品已更新' : '分销商品已新增'
    )
    dialogVisible.value = false
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const next = Number(row.status) === 1 ? 0 : 1
  await runAction(
    () => distributionGoodsApi.updateStatus({ id: row.id, status: next }),
    load,
    next === 1 ? '已启用分销' : '已停用分销'
  )
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除商品「${row.goodsName || row.goodsId}」的分销配置？`, '删除分销商品', { type: 'warning' })
  } catch {
    return
  }
  await runAction(() => distributionGoodsApi.remove(row.id), load, '已删除')
}

function handleReset() {
  reset({ goodsName: '', status: null, shopId: null })
}

onMounted(load)
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}
.header-tip {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.search-form {
  margin-bottom: 8px;
}
.pagination {
  margin-top: 14px;
  justify-content: flex-end;
}
.form-hint {
  margin-left: 12px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
