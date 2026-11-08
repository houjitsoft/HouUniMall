<template>
  <main class="lucky-page">
    <header class="lucky-masthead">
      <div>
        <h1>奖项设置</h1>
        <p class="lucky-lead">
          配置转盘上的奖项：等级、展示名称、关联礼品与库存。请至少保留一个「未中奖」项用于兜底，避免用户抽到空指针。
        </p>
      </div>
      <div class="lucky-actions">
        <ActivitySelect v-model="activityId" placeholder="选择活动" width="240px" :clearable="false" />
        <el-button type="primary" :disabled="!activityId" @click="openCreate">新增奖项</el-button>
      </div>
    </header>

    <el-empty v-if="!loading && !activityId" description="还没有抽奖活动，请先在「抽奖活动」中新建">
      <el-button type="primary" @click="goActivityList">去新建活动</el-button>
    </el-empty>

    <template v-else>
      <section class="lucky-stats">
        <article v-for="item in statCards" :key="item.key" class="lucky-stat">
          <p class="lucky-stat__label">{{ item.label }}</p>
          <p class="lucky-stat__value">{{ item.value }}</p>
          <p class="lucky-stat__hint">{{ item.hint }}</p>
        </article>
      </section>

      <el-card shadow="never">
        <el-table :data="rows" v-loading="loading" row-key="prizeId">
          <el-table-column prop="prizeId" label="ID" width="70" />
          <el-table-column prop="prizeGrade" label="奖项等级" width="130" show-overflow-tooltip />
          <el-table-column prop="prizeName" label="奖项名称" min-width="160" show-overflow-tooltip />
          <el-table-column label="关联礼品" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">{{ giftNameOf(row.giftId) }}</template>
          </el-table-column>
          <el-table-column prop="giftNum" label="礼品总数" width="100" align="right" />
          <el-table-column prop="giveOutGiftNum" label="已发放" width="90" align="right" />
          <el-table-column label="剩余" width="90" align="right">
            <template #default="{ row }">{{ Math.max((Number(row.giftNum) || 0) - (Number(row.giveOutGiftNum) || 0), 0) }}</template>
          </el-table-column>
          <el-table-column label="是否中奖" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="String(row.isWin) === '2' ? 'info' : 'success'">
                {{ String(row.isWin) === '2' ? '未中奖' : '中奖' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="displayOrder" label="排序" width="80" align="right" />
          <el-table-column label="操作" width="130" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="该活动还没有奖项，点击右上角「新增奖项」配置转盘" />
          </template>
        </el-table>
      </el-card>
    </template>

    <el-dialog v-model="dialogVisible" :title="form.prizeId ? '编辑奖项' : '新增奖项'" width="560px">
      <el-form label-position="top">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="奖项等级" required>
              <el-input v-model="form.prizeGrade" maxlength="40" placeholder="例如：一等奖 / 谢谢参与" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="奖项名称" required>
              <el-input v-model="form.prizeName" maxlength="80" placeholder="转盘上展示的名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="关联礼品">
          <el-select v-model="form.giftId" clearable filterable placeholder="不关联礼品（如谢谢参与）" style="width: 100%">
            <el-option
              v-for="gift in gifts"
              :key="gift.giftId"
              :label="`${gift.giftName}${Number(gift.giftIsRedpacket) === 1 ? '（红包）' : ''}`"
              :value="gift.giftId"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="礼品数量">
              <el-input-number v-model="form.giftNum" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否中奖">
              <el-select v-model="form.isWin" style="width: 100%">
                <el-option label="中奖" value="1" />
                <el-option label="未中奖" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序">
              <el-input-number v-model="form.displayOrder" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <p class="lucky-tip">礼品库存为 0 时该奖项不会再被抽出；「未中奖」项不受库存限制。</p>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import ActivitySelect from '../components/ActivitySelect.vue'
import {
  deletePrize,
  loadAllGifts,
  loadPrizes,
  savePrize
} from '../api.js'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const activityId = ref(route.query.activityId ? Number(route.query.activityId) : '')
const rows = ref([])
const gifts = ref([])
const dialogVisible = ref(false)

function emptyForm() {
  return {
    prizeId: '',
    prizeGrade: '',
    prizeName: '',
    giftId: '',
    giftNum: 0,
    displayOrder: 0,
    isWin: '1'
  }
}

const form = reactive(emptyForm())

const statCards = computed(() => {
  const total = rows.value.reduce((sum, item) => sum + (Number(item.giftNum) || 0), 0)
  const given = rows.value.reduce((sum, item) => sum + (Number(item.giveOutGiftNum) || 0), 0)
  const winCount = rows.value.filter((item) => String(item.isWin) !== '2').length
  const loseCount = rows.value.filter((item) => String(item.isWin) === '2').length
  return [
    { key: 'prize', label: '奖项总数', value: rows.value.length, hint: `中奖 ${winCount} / 未中奖 ${loseCount}` },
    { key: 'stock', label: '礼品总数', value: total, hint: '所有奖项配置的礼品数量合计' },
    { key: 'given', label: '已发放', value: given, hint: '累计已抽出的礼品数' },
    { key: 'left', label: '剩余库存', value: Math.max(total - given, 0), hint: '库存为 0 的奖项不再中奖' }
  ]
})

function giftNameOf(giftId) {
  if (!giftId || Number(giftId) === 0) return '—'
  const gift = gifts.value.find((item) => Number(item.giftId) === Number(giftId))
  return gift ? gift.giftName : `礼品 #${giftId}`
}

async function fetchPrizes() {
  if (!activityId.value) {
    rows.value = []
    return
  }
  loading.value = true
  try {
    rows.value = await loadPrizes(activityId.value)
  } catch (error) {
    ElMessage.error(error?.message || '加载奖项失败')
  } finally {
    loading.value = false
  }
}

async function fetchGifts() {
  try {
    gifts.value = await loadAllGifts()
  } catch {
    gifts.value = []
  }
}

function openCreate() {
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    prizeId: row.prizeId,
    prizeGrade: row.prizeGrade || '',
    prizeName: row.prizeName || '',
    giftId: Number(row.giftId) || '',
    giftNum: Number(row.giftNum) || 0,
    displayOrder: Number(row.displayOrder) || 0,
    isWin: String(row.isWin) === '2' ? '2' : '1'
  })
  dialogVisible.value = true
}

async function submit() {
  if (!activityId.value) return
  if (!form.prizeGrade.trim() || !form.prizeName.trim()) {
    ElMessage.warning('请填写奖项等级与奖项名称')
    return
  }
  saving.value = true
  try {
    await savePrize({
      activityId: activityId.value,
      prizeId: form.prizeId || '',
      prizeGrade: form.prizeGrade.trim(),
      prizeName: form.prizeName.trim(),
      giftId: form.giftId || 0,
      giftNum: form.giftNum,
      displayOrder: form.displayOrder,
      isWin: form.isWin
    })
    dialogVisible.value = false
    ElMessage.success('奖项已保存')
    await fetchPrizes()
  } catch (error) {
    ElMessage.error(error?.message || '保存奖项失败')
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除奖项「${row.prizeName}」？转盘将立即移除该奖项。`, '删除奖项', {
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deletePrize(row.prizeId)
    ElMessage.success('奖项已删除')
    await fetchPrizes()
  } catch (error) {
    ElMessage.error(error?.message || '删除失败')
  }
}

function goActivityList() {
  router.push('/plugin/userms-lucky-draw/index')
}

watch(activityId, (id) => {
  if (!id) {
    rows.value = []
    return
  }
  router.replace({ path: route.path, query: { ...route.query, activityId: String(id) } })
  fetchPrizes()
})

onMounted(async () => {
  await Promise.all([fetchGifts(), fetchPrizes()])
})
</script>

<style scoped>
.lucky-page {
  padding: 8px 16px 16px;
}
.lucky-masthead {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.lucky-masthead h1 {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 600;
}
.lucky-lead {
  margin: 0;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
  max-width: 760px;
}
.lucky-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.lucky-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}
.lucky-stat {
  padding: 14px 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-bg-color);
}
.lucky-stat__label {
  margin: 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.lucky-stat__value {
  margin: 6px 0 2px;
  font-size: 24px;
  font-weight: 600;
  line-height: 1.1;
}
.lucky-stat__hint {
  margin: 0;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}
.lucky-tip {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.6;
}
</style>
