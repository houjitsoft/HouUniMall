<template>
  <main class="discount-page">
    <header class="discount-masthead">
      <div>
        <h1>商品折扣</h1>
        <p class="discount-lead">
          为指定商品设置限时折扣价。活动进行中买家按下单价成交；可配置限购次数与预热时间。
        </p>
      </div>
      <el-button type="primary" @click="openCreate">新增折扣</el-button>
    </header>

    <section class="discount-stats">
      <div class="discount-stat"><span>活动总数</span><strong>{{ total }}</strong></div>
      <div class="discount-stat"><span>进行中</span><strong>{{ runningCount }}</strong></div>
      <div class="discount-stat"><span>已停用/结束</span><strong>{{ stoppedCount }}</strong></div>
    </section>

    <el-card shadow="never">
      <el-form :inline="true" class="discount-filter" @submit.prevent>
        <el-form-item label="活动名称">
          <el-input v-model="filter.discountName" clearable placeholder="模糊搜索" style="width: 200px" @keyup.enter="fetchList" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.discountState" clearable placeholder="全部" style="width: 130px">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
            <el-option label="已失效" :value="3" />
            <el-option label="预热中" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="fetchList">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rows" v-loading="loading" row-key="discountId">
        <el-table-column prop="discountId" label="ID" width="70" />
        <el-table-column prop="discountName" label="活动名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="生效时间" min-width="300">
          <template #default="{ row }">{{ fmt(row.discountStartTime) }} ~ {{ fmt(row.discountEndTime) }}</template>
        </el-table-column>
        <el-table-column label="限购" width="120">
          <template #default="{ row }">
            {{ Number(row.discountUpperLimit) > 0 ? `限 ${row.discountUpperLimit} 件` : '不限' }}
          </template>
        </el-table-column>
        <el-table-column label="范围" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="!Number(row.shopId) ? 'primary' : 'info'">
              {{ !Number(row.shopId) ? '全平台' : (row.shopName || '店铺') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusType(row.discountState)">{{ statusText(row.discountState) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="Number(row.discountState) === 1 || Number(row.discountState) === 0 || Number(row.discountState) === 4"
              link
              type="warning"
              @click="invalidate(row)"
            >失效</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="还没有折扣活动" /></template>
      </el-table>

      <el-pagination
        class="discount-pager"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="onPage"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.discountId ? '编辑折扣' : '新增折扣'" width="820px" top="5vh">
      <el-form label-position="top">
        <el-row :gutter="12">
          <el-col :span="16">
            <el-form-item label="活动名称" required>
              <el-input v-model="form.discountName" maxlength="50" placeholder="例如：周末限时 8 折" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="限购件数（0=不限）">
              <el-input-number v-model="form.discountUpperLimit" :min="0" :precision="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="活动时间" required>
          <el-date-picker
            v-model="form.range"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="折扣商品（必选，填写折扣价）" required>
          <div class="discount-goods-head">
            <el-button type="primary" plain @click="openPicker">选择商品</el-button>
            <span class="discount-hint">已选 {{ form.goods.length }} 件。</span>
          </div>
          <div class="discount-goods">
            <div v-for="(goods, idx) in form.goods" :key="goods.commonId" class="discount-goods-row">
              <img v-if="goods.goodsImage" :src="goods.goodsImage" alt="" />
              <span class="discount-name">{{ goods.goodsName || `商品 ${goods.commonId}` }}</span>
              <span class="discount-hint">原价 ¥{{ goods.goodsPrice ?? '—' }}</span>
              <el-input-number
                v-model="goods.discountPrice"
                :min="0.01"
                :precision="2"
                controls-position="right"
                style="width: 140px"
              />
              <span class="discount-hint">折后价</span>
              <el-button link type="danger" @click="form.goods.splice(idx, 1)">移除</el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="picker.visible" title="选择折扣商品" width="760px" append-to-body>
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="商品名称">
          <el-input v-model="picker.keyword" clearable style="width: 240px" @keyup.enter="searchPicker" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="picker.loading" @click="searchPicker">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="picker.rows" v-loading="picker.loading" size="small" max-height="320" @selection-change="onPickSelect">
        <el-table-column type="selection" width="48" />
        <el-table-column prop="commonId" label="ID" width="70" />
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">
            <div class="discount-pick-cell">
              <img v-if="row.goodsImage" :src="row.goodsImage" alt="" />
              <span>{{ row.goodsName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="goodsPrice" label="原价" width="100" align="right" />
        <template #empty><el-empty description="没有可售商品" :image-size="56" /></template>
      </el-table>
      <el-pagination
        class="discount-pager"
        layout="total, prev, pager, next"
        :total="picker.total"
        :page-size="picker.pageSize"
        :current-page="picker.page"
        @current-change="onPickerPage"
      />
      <template #footer>
        <el-button @click="picker.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmPicker">加入（{{ picker.selected.length }}）</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteDiscount,
  invalidateDiscount,
  loadDiscountDetail,
  loadDiscounts,
  loadGoodsOptions,
  saveDiscount
} from '../api.js'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 20
const filter = reactive({ discountName: '', discountState: null })
const dialogVisible = ref(false)
const form = reactive(emptyForm())
const picker = reactive({
  visible: false,
  loading: false,
  keyword: '',
  rows: [],
  selected: [],
  page: 1,
  pageSize: 10,
  total: 0
})

const runningCount = computed(() => rows.value.filter((r) => Number(r.discountState) === 1).length)
const stoppedCount = computed(() => rows.value.filter((r) => [2, 3].includes(Number(r.discountState))).length)

function emptyForm() {
  return {
    discountId: null,
    discountName: '',
    discountUpperLimit: 0,
    discountLowerLimit: 1,
    preStartHour: 0,
    range: [],
    goods: []
  }
}

function fmt(v) {
  if (!v) return '—'
  return String(v).replace('T', ' ').slice(0, 19)
}

function statusText(state) {
  return ({ 0: '未开始', 1: '进行中', 2: '已结束', 3: '已失效', 4: '预热中' })[Number(state)] || '未知'
}

function statusType(state) {
  return ({ 0: 'info', 1: 'success', 2: 'info', 3: 'danger', 4: 'warning' })[Number(state)] || 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const data = await loadDiscounts({
      page: page.value,
      pageSize,
      discountName: filter.discountName || undefined,
      discountState: filter.discountState ?? undefined
    })
    rows.value = data.list || []
    total.value = data.totalCount || 0
  } catch (error) {
    ElMessage.error(error?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  filter.discountName = ''
  filter.discountState = null
  page.value = 1
  fetchList()
}

function onPage(next) {
  page.value = next
  fetchList()
}

function openCreate() {
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

async function openEdit(row) {
  Object.assign(form, emptyForm(), {
    discountId: row.discountId,
    discountName: row.discountName,
    discountUpperLimit: row.discountUpperLimit ?? 0,
    range: [fmt(row.discountStartTime), fmt(row.discountEndTime)]
  })
  try {
    const detail = await loadDiscountDetail(row.discountId)
    const list = detail.discountGoodsList || []
    form.goods = list.map((g) => ({
      commonId: g.commonId,
      goodsId: g.goodsId || g.commonId,
      goodsName: g.goodsName,
      goodsImage: g.goodsImage,
      goodsPrice: g.goodsPrice,
      discountPrice: Number(g.discountPrice) || Number(g.goodsPrice) || 0.01,
      discountAmount: g.discountAmount ?? 10
    }))
  } catch (error) {
    ElMessage.warning(error?.message || '详情加载失败，可继续编辑基础信息')
  }
  dialogVisible.value = true
}

function openPicker() {
  picker.visible = true
  picker.page = 1
  searchPicker()
}

async function searchPicker() {
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
    ElMessage.error(error?.message || '商品搜索失败')
  } finally {
    picker.loading = false
  }
}

function onPickerPage(next) {
  picker.page = next
  searchPicker()
}

function onPickSelect(selected) {
  picker.selected = selected
}

function confirmPicker() {
  const exist = new Set(form.goods.map((g) => g.commonId))
  for (const row of picker.selected) {
    if (exist.has(row.commonId)) continue
    const price = Number(row.goodsPrice) || 0
    form.goods.push({
      commonId: row.commonId,
      goodsId: row.goodsId || row.commonId,
      goodsName: row.goodsName,
      goodsImage: row.goodsImage,
      goodsPrice: price,
      discountPrice: price > 0 ? Number((price * 0.9).toFixed(2)) : 0.01,
      discountAmount: 9
    })
  }
  picker.visible = false
}

async function submit() {
  if (!form.discountName.trim()) {
    ElMessage.warning('请填写活动名称')
    return
  }
  if (!form.range || form.range.length !== 2) {
    ElMessage.warning('请选择活动时间')
    return
  }
  if (!form.goods.length) {
    ElMessage.warning('请至少选择一件折扣商品')
    return
  }
  for (const g of form.goods) {
    if (!g.discountPrice || Number(g.discountPrice) <= 0) {
      ElMessage.warning(`请填写「${g.goodsName || g.commonId}」的折扣价`)
      return
    }
  }
  saving.value = true
  try {
    await saveDiscount({
      discountId: form.discountId || undefined,
      discountName: form.discountName.trim(),
      discountStartTime: form.range[0],
      discountEndTime: form.range[1],
      discountUpperLimit: form.discountUpperLimit ?? 0,
      discountLowerLimit: 1,
      preStartHour: 0,
      discountGoodsList: form.goods.map((g) => ({
        commonId: g.commonId,
        goodsId: g.goodsId || g.commonId,
        goodsName: g.goodsName,
        goodsImage: g.goodsImage,
        goodsPrice: g.goodsPrice,
        discountPrice: g.discountPrice,
        discountAmount: g.discountAmount ?? 10,
        shopId: 0
      }))
    })
    ElMessage.success('已保存')
    dialogVisible.value = false
    fetchList()
  } catch (error) {
    ElMessage.error(error?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function invalidate(row) {
  try {
    await ElMessageBox.confirm(`确认把「${row.discountName}」置为失效？`, '提示', { type: 'warning' })
    await invalidateDiscount(row.discountId)
    ElMessage.success('已失效')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error?.message || '操作失败')
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除「${row.discountName}」？`, '提示', { type: 'warning' })
    await deleteDiscount(row.discountId)
    ElMessage.success('已删除')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error?.message || '删除失败')
  }
}

onMounted(fetchList)
</script>

<style scoped>
.discount-page { padding: 16px 20px 32px; }
.discount-masthead { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; margin-bottom: 16px; }
.discount-masthead h1 { margin: 0 0 6px; font-size: 22px; }
.discount-lead { margin: 0; color: #667085; max-width: 640px; line-height: 1.5; }
.discount-stats { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; margin-bottom: 16px; }
.discount-stat { background: #fff; border: 1px solid #eaecf0; border-radius: 10px; padding: 14px 16px; display: flex; flex-direction: column; gap: 6px; }
.discount-stat span { color: #667085; font-size: 13px; }
.discount-stat strong { font-size: 22px; color: #101828; }
.discount-filter { margin-bottom: 8px; }
.discount-pager { margin-top: 16px; justify-content: flex-end; }
.discount-goods-head { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.discount-hint { color: #98a2b3; font-size: 13px; }
.discount-goods { display: flex; flex-direction: column; gap: 8px; max-height: 260px; overflow: auto; }
.discount-goods-row, .discount-pick-cell { display: flex; align-items: center; gap: 10px; }
.discount-goods-row img, .discount-pick-cell img { width: 36px; height: 36px; object-fit: cover; border-radius: 6px; background: #f2f4f7; }
.discount-name { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
