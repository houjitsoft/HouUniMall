<template>
  <main class="seckill-page">
    <header class="seckill-masthead">
      <div>
        <h1>限时秒杀</h1>
        <p class="seckill-lead">
          按天开秒杀场次（每天某整点开抢），场次下挂秒杀商品与秒杀价；开始前用户可预约，开抢后按秒杀价与限购数下单。
          秒杀价是<strong>规格价</strong>——一个商品有多个规格时逐规格定价，各规格独立限量。
        </p>
      </div>
      <el-button type="primary" @click="openCreate">新增秒杀</el-button>
    </header>

    <el-card shadow="never">
      <el-form :inline="true" class="seckill-filter" @submit.prevent>
        <el-form-item label="场次名称">
          <el-input v-model="filter.seckillName" clearable style="width: 200px" @keyup.enter="fetchList" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.seckillState" clearable placeholder="全部" style="width: 130px">
            <el-option label="正常" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="fetchList">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rows" v-loading="loading" row-key="seckillId" @expand-change="onExpand">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="sk-base-goods" v-loading="goodsLoading[row.seckillId]">
              <div class="sk-base-goods-head">
                <span>该场次已挂秒杀商品（{{ (goodsOfBase[row.seckillId] || []).length }} 条规格）</span>
                <el-button link type="primary" @click="loadBaseGoods(row)">刷新</el-button>
              </div>
              <el-table :data="goodsOfBase[row.seckillId] || []" size="small" max-height="260">
                <el-table-column prop="goodsName" label="商品" min-width="180" show-overflow-tooltip />
                <el-table-column label="规格" width="150" show-overflow-tooltip>
                  <template #default="{ row: g }">{{ g.specValueName || '默认规格' }}</template>
                </el-table-column>
                <el-table-column prop="goodsPrice" label="原价" width="95" align="right" />
                <el-table-column prop="seckillPrice" label="秒杀价" width="95" align="right" />
                <el-table-column prop="seckillNum" label="秒杀数量" width="100" align="right" />
                <el-table-column label="预约人数" width="90" align="right">
                  <template #default="{ row: g }">{{ g.subscribeCount ?? 0 }}</template>
                </el-table-column>
                <el-table-column label="操作" width="90" fixed="right">
                  <template #default="{ row: g }">
                    <el-button link type="danger" @click="removeGoods(row, g)">移除</el-button>
                  </template>
                </el-table-column>
                <template #empty>
                  <el-empty description="该场次还没有秒杀商品" :image-size="60" />
                </template>
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="seckillId" label="ID" width="70" />
        <el-table-column prop="seckillName" label="场次名称" min-width="170" show-overflow-tooltip />
        <el-table-column label="开始日期" width="110">
          <template #default="{ row }">{{ fmtDate(row.seckillStartDate) }}</template>
        </el-table-column>
        <el-table-column label="开抢时间点" width="105" align="center">
          <template #default="{ row }">{{ row.seckillHour != null ? `${row.seckillHour} 点` : '-' }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="165">
          <template #default="{ row }">{{ fmt(row.seckillEndTime) }}</template>
        </el-table-column>
        <el-table-column label="限购" width="130" align="center">
          <template #default="{ row }">
            {{ row.seckillLowerLimit ?? 0 }} ~ {{ row.seckillUpperLimit ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="95">
          <template #default="{ row }">
            <el-tag size="small" :type="Number(row.seckillState) === 1 ? 'success' : 'info'">
              {{ Number(row.seckillState) === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="还没有秒杀场次" /></template>
      </el-table>

      <el-pagination
        class="seckill-pager"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="onPage"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增秒杀场次" width="720px">
      <el-form label-position="top">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="场次名称" required>
              <el-input v-model="form.seckillName" maxlength="50" placeholder="例如：双十一天 0 点场" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="开始日期" required>
              <el-date-picker v-model="form.seckillStartDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="开抢时间点" required>
              <el-input-number v-model="form.seckillHour" :min="0" :max="23" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="结束时间" required>
              <el-date-picker
                v-model="form.seckillEndTime"
                type="datetime"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="最低限购">
              <el-input-number v-model="form.seckillLowerLimit" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="最高限购">
              <el-input-number v-model="form.seckillUpperLimit" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="秒杀商品（按规格定价，秒杀价留空的行不生效）" required>
          <div class="sk-spec-box">
            <el-table :data="specRows" size="small" max-height="300">
              <el-table-column prop="commonName" label="商品" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格" width="130" show-overflow-tooltip>
                <template #default="{ row }">{{ row.specName || '默认规格' }}</template>
              </el-table-column>
              <el-table-column prop="goodsPrice" label="原价" width="90" align="right" />
              <el-table-column label="秒杀价" width="125">
                <template #default="{ row }">
                  <el-input-number
                    v-model="row.seckillPrice"
                    :min="0.01"
                    :precision="2"
                    :controls="false"
                    size="small"
                    style="width: 100%"
                    placeholder="必填"
                  />
                </template>
              </el-table-column>
              <el-table-column label="秒杀数量" width="115">
                <template #default="{ row }">
                  <el-input-number v-model="row.seckillNum" :min="1" :controls="false" size="small" style="width: 100%" />
                </template>
              </el-table-column>
              <el-table-column label="每人限购" width="115">
                <template #default="{ row }">
                  <el-input-number v-model="row.goodsUpperLimit" :min="1" :controls="false" size="small" style="width: 100%" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="70">
                <template #default="{ $index }">
                  <el-button link type="danger" @click="dropRow($index)">删</el-button>
                </template>
              </el-table-column>
              <template #empty>
                <el-empty description="还没有选商品，先点下面的「选择商品」" :image-size="60" />
              </template>
            </el-table>
            <div class="sk-spec-actions">
              <el-button @click="openPicker">+ 选择商品</el-button>
              <span class="seckill-hint">
                选商品会把它的全部规格拉进来，逐行填秒杀价；同一商品的多个规格共用商品行的限购。
              </span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="说明">
          <el-input v-model="form.seckillExplain" type="textarea" :rows="2" maxlength="200" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.seckillState" :active-value="1" :inactive-value="0" active-text="正常" inline-prompt />
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
          <el-input
            v-model="picker.keyword"
            clearable
            placeholder="输入商品名称搜索"
            style="width: 240px"
            @keyup.enter="searchPicker"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="picker.loading" @click="searchPicker">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="picker.rows" v-loading="picker.loading" size="small" max-height="380">
        <el-table-column prop="commonId" label="ID" width="70" />
        <el-table-column label="商品" min-width="240">
          <template #default="{ row }">
            <div class="sk-pick-cell">
              <img v-if="row.commonImage" :src="row.commonImage" alt="" />
              <span>{{ row.commonName || `商品 ${row.commonId}` }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="commonPrice" label="原价" width="100" align="right" />
        <el-table-column prop="commonStock" label="库存" width="90" align="right" />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              :disabled="pickedCommonIds.has(row.commonId)"
              :loading="picking === row.commonId"
              @click="pick(row)"
            >
              {{ pickedCommonIds.has(row.commonId) ? '已选' : '选择' }}
            </el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="没有可参与秒杀的商品" :image-size="60" /></template>
      </el-table>
      <el-pagination
        class="seckill-pager"
        layout="total, prev, pager, next"
        :total="picker.total"
        :page-size="picker.pageSize"
        :current-page="picker.page"
        @current-change="onPickerPage"
      />
      <template #footer>
        <el-button type="primary" @click="picker.visible = false">完成</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteSeckill,
  loadGoodsOptions,
  loadGoodsSpecs,
  loadSeckillGoods,
  loadSeckillList,
  loadSeckillSubscribeCount,
  removeSeckillGoods,
  saveSeckill
} from '../api.js'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const dialogVisible = ref(false)
const filter = reactive({ seckillName: '', seckillState: null })

const form = reactive({
  seckillName: '',
  seckillStartDate: '',
  seckillHour: 0,
  seckillEndTime: '',
  seckillLowerLimit: 0,
  seckillUpperLimit: 5,
  seckillState: 1,
  seckillExplain: ''
})

// 弹窗内待提交的规格行（商品 × 规格 一行）
const specRows = ref([])
const picking = ref(null)

const picker = reactive({
  visible: false,
  keyword: '',
  rows: [],
  total: 0,
  page: 1,
  pageSize: 10,
  loading: false
})

// 场次展开后的已配置商品
const goodsOfBase = reactive({})
const goodsLoading = reactive({})

const pickedCommonIds = computed(() => new Set(specRows.value.map((row) => row.commonId)))

function fmt(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

function fmtDate(value) {
  return value ? String(value).slice(0, 10) : '-'
}

async function fetchList() {
  loading.value = true
  try {
    const data = await loadSeckillList({
      page: page.value,
      limit: pageSize.value,
      seckillName: filter.seckillName || undefined,
      seckillState: filter.seckillState ?? undefined
    })
    rows.value = data.list || []
    total.value = data.totalCount || 0
  } catch (error) {
    ElMessage.error(error?.message || '加载秒杀场次失败')
  } finally {
    loading.value = false
  }
}

function onPage(next) {
  page.value = next
  fetchList()
}

function resetFilter() {
  filter.seckillName = ''
  filter.seckillState = null
  page.value = 1
  fetchList()
}

function openCreate() {
  Object.assign(form, {
    seckillName: '',
    seckillStartDate: '',
    seckillHour: 0,
    seckillEndTime: '',
    seckillLowerLimit: 0,
    seckillUpperLimit: 5,
    seckillState: 1,
    seckillExplain: ''
  })
  specRows.value = []
  dialogVisible.value = true
}

/* ---------------- 商品选择 ---------------- */

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
      limit: picker.pageSize,
      commonName: picker.keyword || undefined
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

async function pick(goods) {
  if (pickedCommonIds.value.has(goods.commonId)) return
  picking.value = goods.commonId
  try {
    const specs = await loadGoodsSpecs(goods.commonId)
    if (!specs.length) {
      ElMessage.warning('该商品还没有可售规格，先去商品里补规格')
      return
    }
    specs.forEach((spec) => {
      specRows.value.push({
        commonId: goods.commonId,
        commonName: spec.commonName || goods.commonName || `商品 ${goods.commonId}`,
        commonImage: spec.commonImage || goods.commonImage || '',
        goodsId: spec.goodsId,
        specName: spec.specName || '默认规格',
        goodsPrice:
          spec.goodsPrice != null
            ? Number(spec.goodsPrice)
            : goods.commonPrice != null
              ? Number(goods.commonPrice)
              : null,
        seckillPrice: null,
        seckillNum: 1,
        goodsUpperLimit: form.seckillUpperLimit || 1
      })
    })
    ElMessage.success(`已加入「${goods.commonName || goods.commonId}」的 ${specs.length} 个规格，逐行填秒杀价`)
  } catch (error) {
    ElMessage.error(error?.message || '加载商品规格失败')
  } finally {
    picking.value = null
  }
}

function dropRow(index) {
  specRows.value.splice(index, 1)
}

/* ---------------- 提交 ---------------- */

function buildGoodsList() {
  const groups = new Map()
  specRows.value.forEach((row) => {
    if (row.seckillPrice == null || row.seckillPrice <= 0) return
    if (!groups.has(row.commonId)) {
      groups.set(row.commonId, {
        commonId: row.commonId,
        goodsName: row.commonName || '',
        goodsImage: row.commonImage || '',
        goodsUpperLimit: row.goodsUpperLimit || 0,
        seckillNum: 0,
        specLists: []
      })
    }
    const group = groups.get(row.commonId)
    group.specLists.push({
      commonId: row.commonId,
      goodsId: row.goodsId,
      goodsPrice: row.goodsPrice,
      seckillPrice: row.seckillPrice,
      seckillNum: row.seckillNum || 0
    })
    group.seckillNum += row.seckillNum || 0
    group.goodsUpperLimit = Math.max(group.goodsUpperLimit, row.goodsUpperLimit || 0)
  })
  return [...groups.values()]
}

async function submit() {
  if (!form.seckillName.trim()) { ElMessage.warning('请填写场次名称'); return }
  if (!form.seckillStartDate) { ElMessage.warning('请选择开始日期'); return }
  if (!form.seckillEndTime) { ElMessage.warning('请选择结束时间'); return }
  if (!specRows.value.length) { ElMessage.warning('请先选商品'); return }
  const goodsList = buildGoodsList()
  if (!goodsList.length) { ElMessage.warning('请至少给一个规格填秒杀价'); return }
  const missing = specRows.value.filter((row) => row.seckillPrice == null || row.seckillPrice <= 0).length
  if (missing) {
    ElMessage.warning(`有 ${missing} 个规格没填秒杀价，这些规格不会生效`)
  }
  saving.value = true
  try {
    await saveSeckill({
      seckillName: form.seckillName.trim(),
      seckillStartDate: form.seckillStartDate,
      seckillHour: form.seckillHour,
      seckillEndTime: form.seckillEndTime,
      seckillStartTime: `${form.seckillStartDate} ${String(form.seckillHour).padStart(2, '0')}:00:00`,
      seckillLowerLimit: form.seckillLowerLimit,
      seckillUpperLimit: form.seckillUpperLimit,
      seckillState: form.seckillState,
      seckillExplain: form.seckillExplain || undefined,
      seckillGoodsList: goodsList
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

/* ---------------- 场次下的已挂商品 ---------------- */

function onExpand(row, expandedRows) {
  if (expandedRows.includes(row)) loadBaseGoods(row)
}

async function loadBaseGoods(row) {
  goodsLoading[row.seckillId] = true
  try {
    const [list, counts] = await Promise.all([
      loadSeckillGoods(row.seckillId),
      loadSeckillSubscribeCount(row.seckillId)
    ])
    goodsOfBase[row.seckillId] = (list || []).map((g) => ({
      ...g,
      subscribeCount: counts && g.goodsId != null ? Number(counts[g.goodsId] || 0) : 0
    }))
  } catch (error) {
    ElMessage.error(error?.message || '加载秒杀商品失败')
  } finally {
    goodsLoading[row.seckillId] = false
  }
}

async function removeGoods(base, goods) {
  try {
    await ElMessageBox.confirm(
      `确认把「${goods.goodsName || goods.commonId}」从这个场次移除？该商品在此场次下的全部规格都会被删掉。`,
      '提示',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await removeSeckillGoods(base.seckillId, goods.commonId)
    ElMessage.success('已移除')
    await loadBaseGoods(base)
  } catch (error) {
    ElMessage.error(error?.message || '移除失败')
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除秒杀场次「${row.seckillName}」？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await deleteSeckill(row.seckillId)
    ElMessage.success('已删除')
    await fetchList()
  } catch (error) {
    ElMessage.error(error?.message || '删除失败')
  }
}

onMounted(fetchList)
</script>

<style scoped>
.seckill-page { padding: 16px; }
.seckill-masthead { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; margin-bottom: 16px; }
.seckill-masthead h1 { margin: 0 0 4px; font-size: 20px; }
.seckill-lead { margin: 0; color: var(--el-text-color-secondary); font-size: 13px; max-width: 760px; line-height: 1.6; }
.seckill-filter { margin-bottom: 4px; }
.seckill-pager { margin-top: 16px; justify-content: flex-end; }
.seckill-hint { font-size: 12px; color: var(--el-text-color-secondary); margin-top: 4px; }

.sk-base-goods { padding: 8px 12px 12px; background: var(--el-fill-color-lighter); }
.sk-base-goods-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; font-size: 13px; }

.sk-spec-box { width: 100%; }
.sk-spec-actions { display: flex; align-items: center; gap: 12px; margin-top: 8px; }
.sk-spec-actions .seckill-hint { margin-top: 0; }

.sk-pick-cell { display: flex; align-items: center; gap: 8px; }
.sk-pick-cell img { width: 32px; height: 32px; object-fit: cover; border-radius: 4px; flex: none; }
</style>
