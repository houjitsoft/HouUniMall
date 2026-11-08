<template>
  <main class="tpage">
    <header class="tpage-masthead">
      <div>
        <h1>千客千价</h1>
        <p class="tpage-lead">
          为同一商品按人群配置差异化价格：全部用户 / 指定用户 / 会员等级 / 标签，命中优先级最高的一条生效。
          价格方式支持指定价、折扣百分比、立减金额，可限定生效时间段。
        </p>
      </div>
      <el-button type="primary" @click="openCreate">新增策略</el-button>
    </header>

    <el-card shadow="never">
      <el-form :inline="true" class="tpage-filter" @submit.prevent>
        <el-form-item label="商品ID">
          <el-input v-model="filter.goodsId" clearable style="width: 150px" @keyup.enter="fetchList" />
        </el-form-item>
        <el-form-item label="人群类型">
          <el-select v-model="filter.segmentType" clearable placeholder="全部" style="width: 140px">
            <el-option v-for="s in SEGMENTS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="fetchList">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rows" v-loading="loading" row-key="id">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="商品" min-width="150">
          <template #default="{ row }">
            <span>商品 #{{ row.goodsId }}</span>
            <el-tag v-if="row.skuId" size="small" type="info" class="tpage-sku">SKU #{{ row.skuId }}</el-tag>
            <el-tag v-else size="small" type="info" class="tpage-sku">整品</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="人群" min-width="150">
          <template #default="{ row }">
            {{ segmentLabel(row.segmentType) }}
            <span v-if="row.segmentValue" class="tpage-segval">：{{ row.segmentValue }}</span>
          </template>
        </el-table-column>
        <el-table-column label="价格方式" min-width="140">
          <template #default="{ row }">{{ priceText(row) }}</template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" align="center" />
        <el-table-column label="生效时间" min-width="220">
          <template #default="{ row }">{{ rangeText(row) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch
              :model-value="Number(row.status) === 1"
              @change="(v) => toggleStatus(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="还没有价格策略" /></template>
      </el-table>

      <el-pagination
        class="tpage-pager"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="onPage"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑价格策略' : '新增价格策略'" width="640px">
      <el-form label-position="top">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="商品ID" required>
              <el-input-number v-model="form.goodsId" :min="1" :controls="false" style="width: 100%" placeholder="商品 goodsId" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="SKU ID（留空对整品生效）">
              <el-input-number v-model="form.skuId" :min="1" :controls="false" style="width: 100%" placeholder="可选" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="人群类型" required>
              <el-select v-model="form.segmentType" style="width: 100%">
                <el-option v-for="s in SEGMENTS" :key="s.value" :label="s.label" :value="s.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="segmentValueLabel" :required="form.segmentType !== 0">
              <el-input v-model="form.segmentValue" :placeholder="segmentValuePlaceholder" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="价格方式" required>
              <el-select v-model="form.priceType" style="width: 100%">
                <el-option v-for="p in PRICE_TYPES" :key="p.value" :label="p.label" :value="p.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="priceValueLabel" required>
              <el-input-number
                v-model="form.priceValue"
                :min="0.01"
                :precision="2"
                :controls="false"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="优先级（越大越优先）">
              <el-input-number v-model="form.priority" :min="0" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inline-prompt />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="生效时间（留空表示长期有效）">
          <el-date-picker
            v-model="form.range"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="x"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { loadPriceList, savePriceConfig, updatePriceStatus, deletePriceConfig } from '../api.js'

const SEGMENTS = [
  { value: 0, label: '全部用户' },
  { value: 1, label: '指定用户' },
  { value: 2, label: '会员等级' },
  { value: 3, label: '标签' }
]
const PRICE_TYPES = [
  { value: 1, label: '指定价格' },
  { value: 2, label: '折扣百分比（95=95折）' },
  { value: 3, label: '立减金额' }
]

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const filter = reactive({ goodsId: '', segmentType: null, status: null })

function emptyForm() {
  return {
    id: null,
    goodsId: null,
    skuId: null,
    segmentType: 0,
    segmentValue: '',
    priceType: 1,
    priceValue: null,
    priority: 0,
    status: 1,
    range: null,
    remark: ''
  }
}
const form = reactive(emptyForm())

const segmentValueLabel = computed(() => {
  const t = form.segmentType
  if (t === 1) return '用户ID'
  if (t === 2) return '会员等级编码'
  if (t === 3) return '标签编码'
  return '人群值（可留空）'
})
const segmentValuePlaceholder = computed(() => {
  const t = form.segmentType
  if (t === 1) return '单个 userId'
  if (t === 2) return '如 vip1 / vip2'
  if (t === 3) return '如 new_user'
  return '全部用户无需填写'
})
const priceValueLabel = computed(() => {
  const t = form.priceType
  if (t === 2) return '折扣百分比（如 95 表示 95 折）'
  if (t === 3) return '立减金额（元）'
  return '指定价格（元）'
})

function segmentLabel(v) {
  return (SEGMENTS.find((s) => s.value === v) || {}).label || `类型${v}`
}

function priceText(row) {
  if (row.priceType === 2) return `${row.priceValue}%（${(Number(row.priceValue) / 10).toFixed(1)} 折）`
  if (row.priceType === 3) return `立减 ¥${row.priceValue}`
  return `定价 ¥${row.priceValue}`
}

function fmtTime(ms) {
  if (!ms) return ''
  const d = new Date(Number(ms))
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

function rangeText(row) {
  if (!row.startTime && !row.endTime) return '长期有效'
  return `${fmtTime(row.startTime) || '不限'} ~ ${fmtTime(row.endTime) || '不限'}`
}

async function fetchList() {
  loading.value = true
  try {
    const data = await loadPriceList({
      page: page.value,
      size: pageSize.value,
      goodsId: filter.goodsId ? Number(filter.goodsId) : undefined,
      segmentType: filter.segmentType ?? undefined,
      status: filter.status ?? undefined
    })
    rows.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error(error?.message || '加载价格策略失败')
  } finally {
    loading.value = false
  }
}

function onPage(next) {
  page.value = next
  fetchList()
}

function resetFilter() {
  filter.goodsId = ''
  filter.segmentType = null
  filter.status = null
  page.value = 1
  fetchList()
}

function openCreate() {
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    goodsId: row.goodsId,
    skuId: row.skuId || null,
    segmentType: row.segmentType,
    segmentValue: row.segmentValue || '',
    priceType: row.priceType,
    priceValue: Number(row.priceValue),
    priority: row.priority || 0,
    status: Number(row.status),
    range: row.startTime || row.endTime ? [row.startTime, row.endTime] : null,
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

async function submit() {
  if (!form.goodsId) { ElMessage.warning('请填写商品ID'); return }
  if (form.segmentType !== 0 && !String(form.segmentValue).trim()) {
    ElMessage.warning('非「全部用户」时人群值必填'); return
  }
  if (form.priceValue == null || form.priceValue <= 0) { ElMessage.warning('请填写有效的价格值'); return }
  saving.value = true
  try {
    const range = form.range || []
    await savePriceConfig({
      id: form.id || undefined,
      goodsId: form.goodsId,
      skuId: form.skuId || null,
      segmentType: form.segmentType,
      segmentValue: form.segmentType === 0 ? null : String(form.segmentValue).trim(),
      priceType: form.priceType,
      priceValue: form.priceValue,
      priority: form.priority || 0,
      status: form.status,
      startTime: range[0] ? Number(range[0]) : null,
      endTime: range[1] ? Number(range[1]) : null,
      remark: form.remark || undefined
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

async function toggleStatus(row, checked) {
  const next = checked ? 1 : 0
  try {
    await updatePriceStatus(row.id, next)
    row.status = next
    ElMessage.success(checked ? '已启用' : '已禁用')
  } catch (error) {
    ElMessage.error(error?.message || '状态更新失败')
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除商品 #${row.goodsId} 的这条价格策略？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await deletePriceConfig(row.id)
    ElMessage.success('已删除')
    await fetchList()
  } catch (error) {
    ElMessage.error(error?.message || '删除失败')
  }
}

onMounted(fetchList)
</script>

<style scoped>
.tpage { padding: 16px; }
.tpage-masthead { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; margin-bottom: 16px; }
.tpage-masthead h1 { margin: 0 0 4px; font-size: 20px; }
.tpage-lead { margin: 0; color: var(--el-text-color-secondary); font-size: 13px; max-width: 760px; line-height: 1.6; }
.tpage-filter { margin-bottom: 4px; }
.tpage-pager { margin-top: 16px; justify-content: flex-end; }
.tpage-sku { margin-left: 6px; }
.tpage-segval { color: var(--el-text-color-secondary); }
</style>
