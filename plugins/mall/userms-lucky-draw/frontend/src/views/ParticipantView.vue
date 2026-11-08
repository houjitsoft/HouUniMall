<template>
  <main class="lucky-page">
    <header class="lucky-masthead">
      <div>
        <h1>参与用户</h1>
        <p class="lucky-lead">
          查看每个用户在各活动中的抽奖次数、剩余次数与中奖次数。次数由前台抽奖流程写入，用于核对异常与客诉。
        </p>
      </div>
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
        <el-form-item label="活动">
          <ActivitySelect v-model="filter.activityId" placeholder="全部活动" width="240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="search">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rows" v-loading="loading" :row-key="rowKeyOf">
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column label="用户名称" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.userName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="activityId" label="活动ID" width="90" align="right" />
        <el-table-column prop="luckyDrawTimes" label="总抽奖次数" width="110" align="right" />
        <el-table-column label="剩余次数" width="100" align="right">
          <template #default="{ row }">
            <span :class="{ 'lucky-zero': Number(row.surplusDrawTimes) <= 0 }">{{ row.surplusDrawTimes ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="winningTimes" label="中奖次数" width="100" align="right" />
        <el-table-column prop="numberOfDailyLottery" label="每日上限" width="100" align="right" />
        <el-table-column prop="numSweepstakesToday" label="今日已抽" width="100" align="right" />
        <el-table-column label="更新时间" width="170">
          <template #default="{ row }">{{ fmt(row.updateDate) }}</template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无参与记录，用户在前台抽奖后这里会出现明细" />
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
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ActivitySelect from '../components/ActivitySelect.vue'
import { loadDrawTimes } from '../api.js'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 20

const filter = reactive({ activityId: '' })

const statCards = computed(() => {
  const totalTimes = rows.value.reduce((sum, item) => sum + (Number(item.luckyDrawTimes) || 0), 0)
  const winning = rows.value.reduce((sum, item) => sum + (Number(item.winningTimes) || 0), 0)
  const surplus = rows.value.reduce((sum, item) => sum + (Number(item.surplusDrawTimes) || 0), 0)
  return [
    { key: 'users', label: '参与用户', value: total.value, hint: '当前筛选下产生过抽奖的用户数' },
    { key: 'times', label: '总抽奖次数', value: totalTimes, hint: '本页用户累计抽奖次数' },
    { key: 'winning', label: '中奖次数', value: winning, hint: '本页用户累计中奖次数' },
    { key: 'surplus', label: '剩余次数', value: surplus, hint: '本页用户剩余可抽次数' }
  ]
})

function fmt(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

/** 次数列表接口不返回 time_id，用活动 + 用户做稳定行键 */
function rowKeyOf(row) {
  return `${row.activityId}-${row.userId}`
}

async function fetchList() {
  loading.value = true
  try {
    const data = await loadDrawTimes({
      page: page.value,
      limit: pageSize,
      activityId: filter.activityId || undefined
    })
    rows.value = data.list
    total.value = data.totalCount
  } catch (error) {
    ElMessage.error(error?.message || '加载参与用户失败')
  } finally {
    loading.value = false
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
  filter.activityId = ''
  search()
}

onMounted(fetchList)
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
.lucky-zero {
  color: var(--el-color-danger);
}
.lucky-pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
