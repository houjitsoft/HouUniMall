<template>
  <main class="lucky-page">
    <header class="lucky-masthead">
      <div>
        <h1>抽奖活动</h1>
        <p class="lucky-lead">
          管理幸运大转盘活动：新建活动后进入「活动配置」设置时间与抽奖规则，再到「奖项设置」配置转盘奖项与礼品库存。
        </p>
      </div>
      <el-button type="primary" @click="openCreate">新增活动</el-button>
    </header>

    <section class="lucky-stats">
      <article v-for="item in statCards" :key="item.key" class="lucky-stat">
        <p class="lucky-stat__label">{{ item.label }}</p>
        <p class="lucky-stat__value">{{ item.value }}</p>
        <p class="lucky-stat__hint">{{ item.hint }}</p>
      </article>
    </section>

    <el-card shadow="never">
      <el-form :inline="true" class="lucky-filter" @submit.prevent>
        <el-form-item label="活动标题">
          <el-input
            v-model="filter.activityTitle"
            clearable
            placeholder="搜索活动标题"
            style="width: 200px"
            @keyup.enter="search"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.activityIsEnable" clearable placeholder="全部" style="width: 130px">
            <el-option label="可用" :value="1" />
            <el-option label="不可用" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="search">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rows" v-loading="loading" row-key="activityId">
        <el-table-column prop="activityId" label="ID" width="70" />
        <el-table-column label="活动标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="lucky-title">{{ row.activityTitle }}</span>
          </template>
        </el-table-column>
        <el-table-column label="活动时间" width="330">
          <template #default="{ row }">
            <span class="lucky-time">{{ fmt(row.activityStartTime) }}</span>
            <span class="lucky-time-sep">~</span>
            <span class="lucky-time">{{ fmt(row.activityEndTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="参与人数" width="100" align="right">
          <template #default="{ row }">{{ row.partakeNum ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="进行状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="runState(row).type" effect="light">{{ runState(row).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="启用" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="Number(row.activityIsEnable) === 1 ? 'success' : 'info'">
              {{ Number(row.activityIsEnable) === 1 ? '可用' : '不可用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goConfig(row)">活动配置</el-button>
            <el-button link type="primary" @click="goPrizes(row)">奖项设置</el-button>
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, row)">
              <el-button link type="primary">更多<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">编辑基础信息</el-dropdown-item>
                  <el-dropdown-item command="limit">抽奖次数限制</el-dropdown-item>
                  <el-dropdown-item command="records">查看抽奖记录</el-dropdown-item>
                  <el-dropdown-item command="duplicate">复制活动</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除活动</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="还没有抽奖活动，点击右上角「新增活动」开始" />
        </template>
      </el-table>

      <el-pagination
        class="lucky-pager"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="onPage"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.activityId ? '编辑活动' : '新增活动'" width="660px">
      <el-form label-position="top">
        <el-form-item label="活动标题" required>
          <el-input v-model="form.activityTitle" maxlength="80" show-word-limit placeholder="例如：双十一幸运大转盘" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="开始时间" required>
              <el-date-picker
                v-model="form.activityStartTime"
                type="datetime"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" required>
              <el-date-picker
                v-model="form.activityEndTime"
                type="datetime"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="活动状态">
              <el-select v-model="form.activityIsEnable" style="width: 100%">
                <el-option label="可用" :value="1" />
                <el-option label="不可用" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="显示参与人数">
              <el-select v-model="form.partakeNumIsDisplay" style="width: 100%">
                <el-option label="显示" :value="1" />
                <el-option label="隐藏" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="轮播中奖信息">
              <el-select v-model="form.isRoundAwardInformation" style="width: 100%">
                <el-option label="开启" :value="1" />
                <el-option label="关闭" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="虚拟参与人数">
          <el-input-number v-model="form.partakeNum" :min="0" :max="10000" controls-position="right" style="width: 200px" />
          <span class="lucky-form-hint">仅用于前台展示，可营造活动热度，不影响真实抽奖数据</span>
        </el-form-item>
        <el-form-item label="图文说明">
          <el-input v-model="form.graphicExplanation" type="textarea" :rows="3" maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="limitVisible" title="抽奖次数限制" width="520px">
      <p class="lucky-dialog-tip">保存后立即对该活动生效；活动配置页可查看并调整完整规则。</p>
      <el-form label-position="top">
        <el-form-item label="总抽奖机会是否限制">
          <el-select v-model="limitForm.totalSweepstakesIsLimit" style="width: 100%">
            <el-option label="不限" :value="1" />
            <el-option label="限制" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="总抽奖机会">
          <el-input-number v-model="limitForm.totalSweepstakesLimit" :min="0" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="每人每日抽奖次数">
          <el-input-number v-model="limitForm.winningTimesPerPerson" :min="0" controls-position="right" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="limitVisible = false">取消</el-button>
        <el-button type="primary" :loading="limitSaving" @click="submitLimit">保存</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import {
  deleteActivity,
  loadActivities,
  loadActivityDetail,
  loadActivityLimit,
  loadDrawRecords,
  saveActivity,
  saveActivityLimit
} from '../api.js'

const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const limitSaving = ref(false)
const rows = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 20
const dialogVisible = ref(false)
const limitVisible = ref(false)
const recordTotal = ref(0)

const filter = reactive({ activityTitle: '', activityIsEnable: null })

function emptyForm() {
  return {
    activityId: undefined,
    activityTitle: '',
    activityStartTime: '',
    activityEndTime: '',
    activityIsEnable: 1,
    partakeNumIsDisplay: 1,
    isRoundAwardInformation: 1,
    partakeNum: 0,
    graphicExplanation: ''
  }
}

const form = reactive(emptyForm())
const limitForm = reactive({
  activityId: undefined,
  totalSweepstakesIsLimit: 1,
  totalSweepstakesLimit: 0,
  winningTimesPerPerson: 1
})

const statCards = computed(() => {
  const available = rows.value.filter((item) => Number(item.activityIsEnable) === 1).length
  const partake = rows.value.reduce((sum, item) => sum + (Number(item.partakeNum) || 0), 0)
  return [
    { key: 'total', label: '活动总数', value: total.value, hint: '当前筛选条件下的活动数' },
    { key: 'available', label: '本页可用', value: available, hint: '启用中的活动（本页统计）' },
    { key: 'partake', label: '参与人次', value: partake, hint: '本页活动参与人数合计' },
    { key: 'records', label: '抽奖记录', value: recordTotal.value, hint: '全部活动的抽奖记录数' }
  ]
})

function fmt(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

function parseTime(value) {
  if (!value) return NaN
  return new Date(String(value).replace(' ', 'T')).getTime()
}

function runState(row) {
  if (Number(row.activityIsEnable) !== 1) return { text: '已停用', type: 'info' }
  const now = Date.now()
  const start = parseTime(row.activityStartTime)
  const end = parseTime(row.activityEndTime)
  if (Number.isFinite(start) && now < start) return { text: '未开始', type: 'warning' }
  if (Number.isFinite(end) && now > end) return { text: '已结束', type: 'info' }
  return { text: '进行中', type: 'success' }
}

async function fetchList() {
  loading.value = true
  try {
    const data = await loadActivities({
      page: page.value,
      limit: pageSize,
      activityTitle: filter.activityTitle.trim() || undefined,
      activityIsEnable: filter.activityIsEnable ?? undefined
    })
    rows.value = data.list
    total.value = data.totalCount
  } catch (error) {
    ElMessage.error(error?.message || '加载抽奖活动失败')
  } finally {
    loading.value = false
  }
}

async function fetchRecordTotal() {
  try {
    const data = await loadDrawRecords({ page: 1, limit: 1 })
    recordTotal.value = data.totalCount
  } catch {
    recordTotal.value = 0
  }
}

function search() {
  page.value = 1
  fetchList()
}

function onPage(next) {
  page.value = next
  fetchList()
}

function resetFilter() {
  filter.activityTitle = ''
  filter.activityIsEnable = null
  search()
}

function openCreate() {
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

async function openEdit(row) {
  Object.assign(form, emptyForm(), {
    activityId: row.activityId,
    activityTitle: row.activityTitle || '',
    activityStartTime: fmt(row.activityStartTime) === '-' ? '' : fmt(row.activityStartTime),
    activityEndTime: fmt(row.activityEndTime) === '-' ? '' : fmt(row.activityEndTime),
    activityIsEnable: Number(row.activityIsEnable) === 2 ? 2 : 1,
    partakeNumIsDisplay: Number(row.partakeNumIsDisplay) === 2 ? 2 : 1,
    isRoundAwardInformation: Number(row.isRoundAwardInformation) === 2 ? 2 : 1,
    partakeNum: Number(row.partakeNum) || 0,
    graphicExplanation: row.graphicExplanation || ''
  })
  dialogVisible.value = true
  try {
    const detail = await loadActivityDetail(row.activityId)
    if (detail && detail.activityId) {
      Object.assign(form, {
        activityIsEnable: Number(detail.activityIsEnable) === 2 ? 2 : 1,
        partakeNumIsDisplay: Number(detail.partakeNumIsDisplay) === 2 ? 2 : 1,
        isRoundAwardInformation: Number(detail.isRoundAwardInformation) === 2 ? 2 : 1,
        partakeNum: Number(detail.partakeNum) || 0,
        graphicExplanation: detail.graphicExplanation || ''
      })
    }
  } catch {
    // 列表字段已足够，详情失败不阻断编辑
  }
}

function validate() {
  if (!form.activityTitle.trim()) {
    ElMessage.warning('请填写活动标题')
    return false
  }
  if (!form.activityStartTime || !form.activityEndTime) {
    ElMessage.warning('请选择活动开始与结束时间')
    return false
  }
  if (parseTime(form.activityEndTime) <= parseTime(form.activityStartTime)) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return false
  }
  return true
}

async function submit() {
  if (!validate()) return
  saving.value = true
  try {
    await saveActivity({
      activityId: form.activityId || undefined,
      activityTitle: form.activityTitle.trim(),
      activityStartTime: form.activityStartTime,
      activityEndTime: form.activityEndTime,
      activityIsEnable: form.activityIsEnable,
      partakeNumIsDisplay: form.partakeNumIsDisplay,
      isRoundAwardInformation: form.isRoundAwardInformation,
      partakeNum: form.partakeNum,
      graphicExplanation: form.graphicExplanation || ''
    })
    dialogVisible.value = false
    ElMessage.success(form.activityId ? '活动已更新' : '活动已创建，请继续配置活动规则与奖项')
    await fetchList()
  } catch (error) {
    ElMessage.error(error?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function openLimit(row) {
  Object.assign(limitForm, {
    activityId: row.activityId,
    totalSweepstakesIsLimit: Number(row.totalSweepstakesIsLimit) === 2 ? 2 : 1,
    totalSweepstakesLimit: Number(row.totalSweepstakesLimit) || 0,
    winningTimesPerPerson: Number(row.winningTimesPerPerson) || 0
  })
  limitVisible.value = true
  try {
    const detail = await loadActivityLimit(row.activityId)
    if (detail && (detail.activityId || detail.totalSweepstakesIsLimit != null)) {
      Object.assign(limitForm, {
        totalSweepstakesIsLimit: Number(detail.totalSweepstakesIsLimit) === 2 ? 2 : 1,
        totalSweepstakesLimit: Number(detail.totalSweepstakesLimit) || 0,
        winningTimesPerPerson: Number(detail.winningTimesPerPerson) || 0
      })
    }
  } catch {
    // 列表字段兜底
  }
}

async function submitLimit() {
  if (!limitForm.activityId) return
  limitSaving.value = true
  try {
    await saveActivityLimit({ ...limitForm })
    limitVisible.value = false
    ElMessage.success('抽奖次数限制已保存')
    await fetchList()
  } catch (error) {
    ElMessage.error(error?.message || '保存失败')
  } finally {
    limitSaving.value = false
  }
}

async function duplicate(row) {
  try {
    await ElMessageBox.confirm(
      `将复制活动「${row.activityTitle}」的基础配置与全部奖项（不含抽奖记录），确认继续？`,
      '复制活动',
      { type: 'info' }
    )
  } catch {
    return
  }
  try {
    const detail = await loadActivityDetail(row.activityId)
    await saveActivity({
      activityTitle: `${row.activityTitle}（副本）`.slice(0, 80),
      activityStartTime: fmt(detail.activityStartTime || row.activityStartTime),
      activityEndTime: fmt(detail.activityEndTime || row.activityEndTime),
      activityIsEnable: 2,
      partakeNumIsDisplay: Number(detail.partakeNumIsDisplay) === 2 ? 2 : 1,
      isRoundAwardInformation: Number(detail.isRoundAwardInformation) === 2 ? 2 : 1,
      partakeNum: 0,
      graphicExplanation: detail.graphicExplanation || ''
    })
    ElMessage.success('活动已复制（默认不可用，确认无误后再启用）')
    await fetchList()
  } catch (error) {
    ElMessage.error(error?.message || '复制活动失败')
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除活动「${row.activityTitle}」？删除后前台立即不可见。`, '删除活动', {
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteActivity(row.activityId)
    ElMessage.success('活动已删除')
    await fetchList()
  } catch (error) {
    ElMessage.error(error?.message || '删除失败')
  }
}

function goConfig(row) {
  router.push({ path: '/plugin/userms-lucky-draw/config', query: { activityId: row.activityId } })
}

function goPrizes(row) {
  router.push({ path: '/plugin/userms-lucky-draw/prizes', query: { activityId: row.activityId } })
}

function handleCommand(command, row) {
  if (command === 'edit') openEdit(row)
  else if (command === 'limit') openLimit(row)
  else if (command === 'records') {
    router.push({ path: '/plugin/userms-lucky-draw/records', query: { activityId: row.activityId } })
  } else if (command === 'duplicate') duplicate(row)
  else if (command === 'delete') remove(row)
}

onMounted(async () => {
  await Promise.all([fetchList(), fetchRecordTotal()])
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
.lucky-filter {
  margin-bottom: 8px;
}
.lucky-title {
  font-weight: 500;
}
.lucky-time {
  color: var(--el-text-color-regular);
}
.lucky-time-sep {
  margin: 0 6px;
  color: var(--el-text-color-placeholder);
}
.lucky-pager {
  margin-top: 12px;
  justify-content: flex-end;
}
.lucky-form-hint {
  margin-left: 12px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.lucky-dialog-tip {
  margin: 0 0 12px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
