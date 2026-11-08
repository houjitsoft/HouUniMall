<template>
  <main class="pintuan-page">
    <header class="pintuan-masthead">
      <div>
        <h1>拼团</h1>
        <p class="pintuan-lead">选商品建活动，设置成团人数与生效时间；可查看团单、启停与删除。成团/退款仍由订单链路处理。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增拼团</el-button>
    </header>

    <el-card shadow="never">
      <el-form :inline="true" class="pintuan-filter" @submit.prevent>
        <el-form-item label="活动名称">
          <el-input v-model="filter.name" clearable style="width: 200px" @keyup.enter="fetchList" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" clearable placeholder="全部" style="width: 130px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
            <el-option label="待开启" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="fetchList">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rows" v-loading="loading" row-key="pintuanId">
        <el-table-column prop="pintuanId" label="ID" width="70" />
        <el-table-column prop="name" label="活动名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="成团人数" width="95" align="center">
          <template #default="{ row }">{{ row.personNum }} 人</template>
        </el-table-column>
        <el-table-column label="商品" min-width="140">
          <template #default="{ row }">commonId {{ row.commonId ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="生效时间" min-width="280">
          <template #default="{ row }">{{ fmt(row.startTime) }} ~ {{ fmt(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="95">
          <template #default="{ row }">
            <el-tag size="small" :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="openTeams(row)">团单</el-button>
            <el-button link :type="Number(row.status) === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ Number(row.status) === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="还没有拼团活动" /></template>
      </el-table>

      <el-pagination
        class="pintuan-pager"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="onPage"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.pintuanId ? '编辑拼团' : '新增拼团'" width="760px" top="6vh">
      <el-form label-position="top">
        <el-form-item label="活动名称" required>
          <el-input v-model="form.name" maxlength="50" placeholder="例如：秋冬新品 3 人团" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="成团人数" required>
              <el-input-number v-model="form.personNum" :min="2" :max="20" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="生效时间" required>
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
        </el-row>

        <el-form-item label="拼团商品" required>
          <div class="pintuan-goods-head">
            <el-button type="primary" plain @click="openPicker">选择商品</el-button>
            <span class="pintuan-hint" v-if="pickedGoods">已选：{{ pickedGoods.commonName }}（commonId {{ pickedGoods.commonId }}）</span>
          </div>
          <el-table :data="specRows" size="small" max-height="260" class="pintuan-spec-table">
            <el-table-column prop="specName" label="规格" min-width="140" show-overflow-tooltip />
            <el-table-column label="原价" width="110" align="right">
              <template #default="{ row }">{{ row.goodsPrice != null ? `¥${row.goodsPrice}` : '-' }}</template>
            </el-table-column>
            <el-table-column label="拼团价" width="140">
              <template #default="{ row }">
                <el-input-number v-model="row.pintuanPrice" :min="0.01" :precision="2" :controls="false" style="width: 120px" />
              </template>
            </el-table-column>
            <el-table-column label="单独买价" width="140">
              <template #default="{ row }">
                <el-input-number v-model="row.priceOne" :min="0.01" :precision="2" :controls="false" style="width: 120px" />
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="先点「选择商品」" :image-size="56" />
            </template>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="picker.visible" title="选择商品" width="780px" append-to-body>
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="商品名称">
          <el-input v-model="picker.keyword" clearable style="width: 240px" @keyup.enter="searchPicker" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="picker.loading" @click="searchPicker">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="picker.rows" v-loading="picker.loading" size="small" max-height="380">
        <el-table-column prop="commonId" label="ID" width="70" />
        <el-table-column label="商品" min-width="240">
          <template #default="{ row }">
            <div class="pintuan-pick-cell">
              <img v-if="row.commonImage" :src="row.commonImage" alt="" />
              <span>{{ row.commonName || `商品 ${row.commonId}` }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="commonPrice" label="原价" width="100" align="right" />
        <el-table-column prop="commonStock" label="库存" width="90" align="right" />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :loading="picking === row.commonId" @click="pick(row)">选择</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="没有可售商品，请先在商品管理上架" :image-size="60" /></template>
      </el-table>
      <el-pagination
        class="pintuan-pager"
        layout="total, prev, pager, next"
        :total="picker.total"
        :page-size="picker.pageSize"
        :current-page="picker.page"
        @current-change="onPickerPage"
      />
    </el-dialog>

    <el-drawer v-model="team.visible" :title="`团单 · ${team.activityName || ''}`" size="640px">
      <el-table :data="team.rows" v-loading="team.loading" size="small">
        <el-table-column prop="markId" label="团 ID" min-width="120" show-overflow-tooltip />
        <el-table-column prop="userName" label="团长" width="120" show-overflow-tooltip />
        <el-table-column label="进度" width="100" align="center">
          <template #default="{ row }">{{ row.num ?? '-' }}/{{ (row.num || 0) + (row.surplusNum || 0) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ teamStatusText(row.status) }}</template>
        </el-table-column>
        <el-table-column label="开团时间" min-width="160">
          <template #default="{ row }">{{ fmt(row.createdTime) }}</template>
        </el-table-column>
        <template #empty><el-empty description="暂无团单" :image-size="60" /></template>
      </el-table>
      <el-pagination
        class="pintuan-pager"
        layout="total, prev, pager, next"
        :total="team.total"
        :page-size="team.pageSize"
        :current-page="team.page"
        @current-change="onTeamPage"
      />
    </el-drawer>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deletePintuan,
  loadGoodsOptions,
  loadGoodsSpecs,
  loadPintuanList,
  loadTeamList,
  savePintuan,
  updatePintuanStatus
} from '../api.js'

const loading = ref(false)
const saving = ref(false)
const picking = ref(null)
const rows = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const dialogVisible = ref(false)
const filter = reactive({ name: '', status: null })
const pickedGoods = ref(null)
const specRows = ref([])

const form = reactive({
  pintuanId: null,
  name: '',
  personNum: 3,
  commonId: null,
  range: []
})

const picker = reactive({
  visible: false,
  keyword: '',
  rows: [],
  total: 0,
  page: 1,
  pageSize: 10,
  loading: false
})

const team = reactive({
  visible: false,
  loading: false,
  activityName: '',
  commonId: null,
  rows: [],
  total: 0,
  page: 1,
  pageSize: 10
})

function fmt(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

function statusText(status) {
  const value = Number(status)
  if (value === 1) return '启用'
  if (value === 2) return '待开启'
  return '停用'
}

function statusType(status) {
  const value = Number(status)
  if (value === 1) return 'success'
  if (value === 2) return 'warning'
  return 'info'
}

function teamStatusText(status) {
  const value = Number(status)
  if (value === 1) return '已成团'
  if (value === 2) return '失败'
  if (value === 3) return '已退款'
  return '拼团中'
}

async function fetchList() {
  loading.value = true
  try {
    const data = await loadPintuanList({
      page: page.value,
      pageSize: pageSize.value,
      name: filter.name || undefined,
      status: filter.status ?? undefined
    })
    rows.value = data.list || []
    total.value = data.totalCount || 0
  } catch (error) {
    ElMessage.error(error?.message || '加载拼团活动失败')
  } finally {
    loading.value = false
  }
}

function onPage(next) {
  page.value = next
  fetchList()
}

function resetFilter() {
  filter.name = ''
  filter.status = null
  page.value = 1
  fetchList()
}

function resetForm() {
  Object.assign(form, { pintuanId: null, name: '', personNum: 3, commonId: null, range: [] })
  pickedGoods.value = null
  specRows.value = []
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    pintuanId: row.pintuanId,
    name: row.name || '',
    personNum: row.personNum || 3,
    commonId: row.commonId,
    range: [fmt(row.startTime), fmt(row.endTime)].filter((item) => item !== '-')
  })
  pickedGoods.value = row.commonId
    ? { commonId: row.commonId, commonName: `商品 ${row.commonId}` }
    : null
  const specs = Array.isArray(row.goodsSpecList) ? row.goodsSpecList : []
  specRows.value = specs.map((spec) => ({
    commonId: spec.commonId || row.commonId,
    goodsId: spec.goodsId,
    specName: spec.goodsSpecName || `规格 ${spec.goodsId}`,
    goodsPrice: spec.priceOri != null ? Number(spec.priceOri) : null,
    pintuanPrice: spec.price != null ? Number(spec.price) : null,
    priceOne: spec.priceOne != null ? Number(spec.priceOne) : (spec.priceOri != null ? Number(spec.priceOri) : null)
  }))
  if (!specRows.value.length && row.commonId) {
    pick({ commonId: row.commonId, commonName: `商品 ${row.commonId}` }, false)
  }
  dialogVisible.value = true
}

function openPicker() {
  picker.visible = true
  picker.keyword = ''
  picker.page = 1
  fetchPicker()
}

async function fetchPicker() {
  picker.loading = true
  try {
    const data = await loadGoodsOptions({
      page: picker.page,
      pageSize: picker.pageSize,
      keyword: picker.keyword || undefined
    })
    picker.rows = data.list || []
    picker.total = data.totalCount || 0
  } catch (error) {
    ElMessage.error(error?.message || '加载商品失败')
  } finally {
    picker.loading = false
  }
}

function searchPicker() {
  picker.page = 1
  fetchPicker()
}

function onPickerPage(next) {
  picker.page = next
  fetchPicker()
}

async function pick(goods, close = true) {
  picking.value = goods.commonId
  try {
    const specs = await loadGoodsSpecs(goods.commonId)
    if (!specs.length) {
      ElMessage.warning('该商品没有可用规格')
      return
    }
    pickedGoods.value = {
      commonId: goods.commonId,
      commonName: goods.commonName || specs[0].commonName || `商品 ${goods.commonId}`
    }
    form.commonId = goods.commonId
    specRows.value = specs.map((spec) => ({
      commonId: goods.commonId,
      goodsId: spec.goodsId,
      specName: spec.specName || '默认规格',
      goodsPrice: spec.goodsPrice,
      pintuanPrice: spec.goodsPrice != null ? Number((Number(spec.goodsPrice) * 0.9).toFixed(2)) : null,
      priceOne: spec.goodsPrice
    }))
    if (close) {
      picker.visible = false
      ElMessage.success(`已选择「${pickedGoods.value.commonName}」共 ${specs.length} 个规格，请确认拼团价`)
    }
  } catch (error) {
    ElMessage.error(error?.message || '加载商品规格失败')
  } finally {
    picking.value = null
  }
}

async function submit() {
  if (!form.name.trim()) { ElMessage.warning('请填写活动名称'); return }
  if (!form.commonId) { ElMessage.warning('请选择商品'); return }
  if (!form.range || form.range.length !== 2) { ElMessage.warning('请选择生效时间'); return }
  if (!specRows.value.length) { ElMessage.warning('请选择商品规格'); return }
  const missing = specRows.value.filter((row) => row.pintuanPrice == null || row.pintuanPrice <= 0)
  if (missing.length) { ElMessage.warning('请为每个规格填写拼团价'); return }

  saving.value = true
  try {
    await savePintuan({
      pintuanId: form.pintuanId || undefined,
      name: form.name.trim(),
      personNum: form.personNum,
      commonId: form.commonId,
      startTime: form.range[0],
      endTime: form.range[1],
      goodsSpecList: specRows.value.map((row) => ({
        commonId: row.commonId,
        goodsId: row.goodsId,
        priceOri: row.goodsPrice,
        price: row.pintuanPrice,
        priceOne: row.priceOne != null ? row.priceOne : row.goodsPrice
      }))
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

async function toggleStatus(row) {
  const next = Number(row.status) === 1 ? 0 : 1
  try {
    await updatePintuanStatus(row.pintuanId, next)
    ElMessage.success(next === 1 ? '已启用' : '已停用')
    await fetchList()
  } catch (error) {
    ElMessage.error(error?.message || '操作失败')
  }
}

async function openTeams(row) {
  if (!row.commonId) {
    ElMessage.warning('该活动缺少商品，无法查团单')
    return
  }
  team.visible = true
  team.activityName = row.name
  team.commonId = row.commonId
  team.page = 1
  await fetchTeams()
}

async function fetchTeams() {
  team.loading = true
  try {
    const data = await loadTeamList({
      page: team.page,
      pageSize: team.pageSize,
      commonId: team.commonId
    })
    team.rows = data.list || []
    team.total = data.totalCount || 0
  } catch (error) {
    ElMessage.error(error?.message || '加载团单失败')
  } finally {
    team.loading = false
  }
}

function onTeamPage(next) {
  team.page = next
  fetchTeams()
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除拼团「${row.name}」？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await deletePintuan(row.pintuanId)
    ElMessage.success('已删除')
    await fetchList()
  } catch (error) {
    ElMessage.error(error?.message || '删除失败')
  }
}

onMounted(fetchList)
</script>

<style scoped>
.pintuan-page { padding: 16px; }
.pintuan-masthead { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; margin-bottom: 16px; }
.pintuan-masthead h1 { margin: 0 0 4px; font-size: 20px; }
.pintuan-lead { margin: 0; color: var(--el-text-color-secondary); font-size: 13px; max-width: 720px; line-height: 1.6; }
.pintuan-filter { margin-bottom: 4px; }
.pintuan-pager { margin-top: 16px; justify-content: flex-end; }
.pintuan-hint { font-size: 12px; color: var(--el-text-color-secondary); }
.pintuan-goods-head { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.pintuan-spec-table { width: 100%; }
.pintuan-pick-cell { display: flex; align-items: center; gap: 8px; }
.pintuan-pick-cell img { width: 36px; height: 36px; object-fit: cover; border-radius: 4px; }
</style>
