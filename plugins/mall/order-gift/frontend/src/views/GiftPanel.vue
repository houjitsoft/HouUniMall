<template>
  <main class="gift-page">
    <header class="gift-masthead">
      <div>
        <h1>满赠</h1>
        <p class="gift-lead">
          满额送赠品：门槛按订单商品金额判定，支持固定赠品与多选一赠品池；赠品库存按赠出实时扣减。
        </p>
      </div>
      <div class="gift-actions">
        <el-button @click="openPreview">预检</el-button>
        <el-button type="primary" @click="openCreate">新增规则</el-button>
      </div>
    </header>

    <section class="gift-stats">
      <div class="gift-stat"><span>规则数</span><strong>{{ stats.ruleCount ?? 0 }}</strong></div>
      <div class="gift-stat"><span>启用中</span><strong>{{ stats.enabledCount ?? 0 }}</strong></div>
      <div class="gift-stat"><span>赠品项</span><strong>{{ stats.itemCount ?? 0 }}</strong></div>
      <div class="gift-stat"><span>已赠出</span><strong>{{ stats.recordCount ?? 0 }}</strong></div>
    </section>

    <el-tabs v-model="tab">
      <el-tab-pane label="规则" name="rules">
        <el-card shadow="never" v-loading="loading">
          <el-table :data="rules">
            <el-table-column prop="ruleName" label="规则名称" min-width="160" />
            <el-table-column label="门槛" width="130">
              <template #default="{ row }">满 ¥{{ row.thresholdAmount }}</template>
            </el-table-column>
            <el-table-column label="方式" width="120">
              <template #default="{ row }">
                <el-tag size="small" :type="row.giftMode === 'CHOOSE' ? 'warning' : 'success'">
                  {{ row.giftMode === 'CHOOSE' ? '多选一' : '固定赠品' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="赠品" min-width="240">
              <template #default="{ row }">
                <span v-for="item in row.items" :key="item.id" class="gift-chip">
                  {{ item.giftTitle }} ×{{ item.giftQty }}
                  <template v-if="item.stockTotal > 0">（剩 {{ Math.max(0, item.stockTotal - item.stockUsed) }}）</template>
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
                <el-button link type="danger" @click="remove(row)">删除</el-button>
              </template>
            </el-table-column>
            <template #empty><el-empty description="还没有满赠规则" /></template>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="赠出记录" name="records" lazy>
        <el-card shadow="never">
          <el-form :inline="true">
            <el-form-item label="订单号"><el-input v-model="recordFilter.orderNo" clearable style="width: 200px" /></el-form-item>
            <el-form-item label="会员 ID"><el-input v-model="recordFilter.userId" clearable style="width: 200px" /></el-form-item>
            <el-form-item><el-button type="primary" :loading="recordLoading" @click="fetchRecords">查询</el-button></el-form-item>
          </el-form>
          <el-table :data="records" v-loading="recordLoading">
            <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
            <el-table-column prop="userId" label="会员 ID" min-width="170" show-overflow-tooltip />
            <el-table-column prop="giftTitle" label="赠品" min-width="160" />
            <el-table-column prop="giftQty" label="数量" width="90" align="right" />
            <el-table-column prop="createTime" label="时间" min-width="180" />
            <template #empty><el-empty description="暂无赠出记录" /></template>
          </el-table>
          <el-pagination
            class="gift-pager"
            background
            layout="total, prev, pager, next"
            :total="recordTotal"
            :page-size="recordSize"
            :current-page="recordPage"
            @current-change="onRecordPage"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑规则' : '新增规则'" width="880px">
      <el-form label-position="top">
        <el-row :gutter="12">
          <el-col :span="10"><el-form-item label="规则名称"><el-input v-model="form.ruleName" maxlength="100" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="门槛金额（元）"><el-input-number v-model="form.thresholdAmount" :min="0.01" :precision="2" controls-position="right" /></el-form-item></el-col>
          <el-col :span="4">
            <el-form-item label="赠品方式">
              <el-select v-model="form.giftMode">
                <el-option label="固定赠品" value="FIXED" />
                <el-option label="多选一" value="CHOOSE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="4"><el-form-item label="优先级"><el-input-number v-model="form.priority" :min="0" :max="9999" controls-position="right" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="生效范围">
              <el-select v-model="form.scopeType" style="width: 100%">
                <el-option label="全场" value="ALL" />
                <el-option label="指定类目" value="CATEGORY" />
                <el-option label="指定商品" value="GOODS" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="form.scopeType === 'CATEGORY' ? '类目 ID（逗号分隔）' : '商品 ID（逗号分隔）'">
              <el-input v-model="scopeIdsText" :disabled="form.scopeType === 'ALL'" placeholder="例如 12,15" />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inline-prompt />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="赠品池">
          <el-table :data="form.items" size="small" style="width: 100%">
            <el-table-column label="商品 ID" width="160">
              <template #default="{ row }"><el-input v-model="row.goodsId" size="small" /></template>
            </el-table-column>
            <el-table-column label="SKU ID（可选）" width="150">
              <template #default="{ row }"><el-input v-model="row.skuId" size="small" /></template>
            </el-table-column>
            <el-table-column label="赠品名称" min-width="180">
              <template #default="{ row }"><el-input v-model="row.giftTitle" size="small" /></template>
            </el-table-column>
            <el-table-column label="每单数量" width="120">
              <template #default="{ row }"><el-input-number v-model="row.giftQty" :min="1" size="small" controls-position="right" /></template>
            </el-table-column>
            <el-table-column label="总库存（0=不限）" width="150">
              <template #default="{ row }"><el-input-number v-model="row.stockTotal" :min="0" size="small" controls-position="right" /></template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="right">
              <template #default="{ $index }"><el-button link type="danger" @click="form.items.splice($index, 1)">移除</el-button></template>
            </el-table-column>
          </el-table>
          <el-button link type="primary" style="margin-top:8px" @click="addItem">加一个赠品</el-button>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" maxlength="255" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="满赠预检" width="560px">
      <el-form :inline="true">
        <el-form-item label="订单金额"><el-input-number v-model="preview.amount" :min="0" :precision="2" controls-position="right" /></el-form-item>
        <el-form-item><el-button type="primary" :loading="previewing" @click="runPreview">看看能送什么</el-button></el-form-item>
      </el-form>
      <el-alert v-if="previewResult" :title="previewResult.matched ? '命中规则：' + previewResult.ruleName : '未命中任何规则'" :type="previewResult.matched ? 'success' : 'info'" :closable="false" />
      <el-table v-if="previewResult && previewResult.gifts && previewResult.gifts.length" :data="previewResult.gifts" size="small" style="margin-top:12px">
        <el-table-column prop="title" label="赠品" min-width="180" />
        <el-table-column prop="giftQty" label="每单数量" width="110" align="right" />
        <el-table-column label="剩余" width="110" align="right">
          <template #default="{ row }">{{ row.remaining < 0 ? '不限' : row.remaining }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteRule, evaluate, loadRecords, loadRules, saveRule } from '../api.js'

const tab = ref('rules')
const loading = ref(false)
const saving = ref(false)
const rules = ref([])
const stats = reactive({})
const dialogVisible = ref(false)
const scopeIdsText = ref('')
const form = reactive(emptyForm())

const records = ref([])
const recordTotal = ref(0)
const recordPage = ref(1)
const recordSize = ref(20)
const recordLoading = ref(false)
const recordFilter = reactive({ orderNo: '', userId: '' })

const previewVisible = ref(false)
const previewing = ref(false)
const previewResult = ref(null)
const preview = reactive({ amount: 200 })

function emptyForm() {
  return {
    id: 0,
    ruleName: '',
    scopeType: 'ALL',
    thresholdAmount: 200,
    giftMode: 'FIXED',
    maxGiftKinds: 1,
    priority: 0,
    status: 1,
    remark: '',
    items: [{ goodsId: '', skuId: '', giftTitle: '', giftQty: 1, stockTotal: 0, stockUsed: 0 }]
  }
}

async function fetchAll() {
  loading.value = true
  try {
    const data = await loadRules()
    rules.value = data.list || []
    Object.assign(stats, data.stats || {})
  } catch (error) {
    ElMessage.error(error?.message || '加载满赠规则失败')
  } finally {
    loading.value = false
  }
}

function addItem() {
  form.items.push({ goodsId: '', skuId: '', giftTitle: '', giftQty: 1, stockTotal: 0, stockUsed: 0 })
}

function openCreate() {
  Object.assign(form, emptyForm())
  scopeIdsText.value = ''
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    ruleName: row.ruleName,
    scopeType: row.scopeType,
    thresholdAmount: Number(row.thresholdAmount),
    giftMode: row.giftMode,
    maxGiftKinds: row.maxGiftKinds,
    priority: row.priority,
    status: row.status,
    remark: row.remark || '',
    items: (row.items || []).map(item => ({
      goodsId: item.goodsId,
      skuId: item.skuId || '',
      giftTitle: item.giftTitle,
      giftQty: item.giftQty,
      stockTotal: item.stockTotal,
      stockUsed: item.stockUsed
    }))
  })
  scopeIdsText.value = (row.scopeIds || []).join(',')
  dialogVisible.value = true
}

async function submit() {
  saving.value = true
  try {
    const payload = {
      id: form.id,
      ruleName: form.ruleName,
      scopeType: form.scopeType,
      scopeIds: form.scopeType === 'ALL' ? [] : scopeIdsText.value.split(',').map(item => item.trim()).filter(Boolean),
      thresholdAmount: form.thresholdAmount,
      giftMode: form.giftMode,
      maxGiftKinds: form.maxGiftKinds,
      priority: form.priority,
      status: form.status,
      remark: form.remark,
      items: form.items
    }
    const data = await saveRule(payload)
    rules.value = data.list || []
    Object.assign(stats, data.stats || {})
    dialogVisible.value = false
    ElMessage.success('已保存')
  } catch (error) {
    ElMessage.error(error?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除规则「${row.ruleName}」？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    const data = await deleteRule(row.id)
    rules.value = data.list || []
    Object.assign(stats, data.stats || {})
    ElMessage.success('已删除')
  } catch (error) {
    ElMessage.error(error?.message || '删除失败')
  }
}

async function fetchRecords() {
  recordLoading.value = true
  try {
    const data = await loadRecords({
      page: recordPage.value,
      size: recordSize.value,
      orderNo: recordFilter.orderNo || undefined,
      userId: recordFilter.userId || undefined
    })
    records.value = data.list || []
    recordTotal.value = data.total || 0
  } catch (error) {
    ElMessage.error(error?.message || '加载赠出记录失败')
  } finally {
    recordLoading.value = false
  }
}

function onRecordPage(next) {
  recordPage.value = next
  fetchRecords()
}

function openPreview() {
  previewResult.value = null
  previewVisible.value = true
}

async function runPreview() {
  previewing.value = true
  try {
    previewResult.value = await evaluate({ amount: preview.amount })
  } catch (error) {
    ElMessage.error(error?.message || '预检失败')
  } finally {
    previewing.value = false
  }
}

onMounted(fetchAll)
</script>

<style scoped>
.gift-page { padding: 16px; }
.gift-masthead { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; margin-bottom: 16px; }
.gift-masthead h1 { margin: 0 0 4px; font-size: 20px; }
.gift-lead { margin: 0; color: var(--el-text-color-secondary); font-size: 13px; }
.gift-actions { display: flex; gap: 8px; }
.gift-stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 12px; margin-bottom: 16px; }
.gift-stat { background: var(--el-fill-color-blank); border: 1px solid var(--el-border-color-lighter); border-radius: 8px; padding: 12px 16px; display: flex; flex-direction: column; gap: 6px; }
.gift-stat span { font-size: 12px; color: var(--el-text-color-secondary); }
.gift-stat strong { font-size: 22px; line-height: 1; }
.gift-chip { display: inline-block; margin: 2px 6px 2px 0; padding: 1px 8px; border-radius: 10px; background: var(--el-fill-color-light); font-size: 12px; }
.gift-pager { margin-top: 16px; justify-content: flex-end; }
</style>
