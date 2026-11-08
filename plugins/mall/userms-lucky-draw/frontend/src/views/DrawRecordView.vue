<template>
  <main class="lucky-page">
    <header class="lucky-masthead">
      <div>
        <h1>抽奖记录</h1>
        <p class="lucky-lead">
          查看每一条中奖/抽奖记录（用户、奖项、时间）。记录按抽奖时间倒序，可按活动与用户筛选并导出。
        </p>
      </div>
    </header>

    <el-card shadow="never">
      <el-form :inline="true" class="lucky-filter" @submit.prevent>
        <el-form-item label="活动">
          <ActivitySelect
            v-model="filter.activityId"
            placeholder="全部活动"
            width="220px"
            @loaded="onActivitiesLoaded"
          />
        </el-form-item>
        <el-form-item label="用户">
          <el-input
            v-model="filter.userName"
            clearable
            placeholder="手机号 / 用户名"
            style="width: 180px"
            @keyup.enter="search"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="search">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
          <el-button :loading="exporting" @click="exportCsv">导出</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rows" v-loading="loading" row-key="id">
        <el-table-column prop="id" label="记录ID" width="90" />
        <el-table-column prop="activityTitle" label="活动" min-width="160" show-overflow-tooltip />
        <el-table-column label="用户" min-width="180">
          <template #default="{ row }">
            <div class="lucky-user">
              <el-avatar v-if="row.userAvatar" :src="row.userAvatar" :size="24" />
              <div class="lucky-user__meta">
                <span class="lucky-user__name">{{ row.userName || '-' }}</span>
                <span class="lucky-user__id">ID {{ row.userId }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="prizeGrade" label="奖项等级" width="120" show-overflow-tooltip />
        <el-table-column prop="prizeName" label="奖项名称" min-width="140" show-overflow-tooltip />
        <el-table-column label="抽奖时间" width="170">
          <template #default="{ row }">{{ fmt(row.updateDate) }}</template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无抽奖记录" />
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
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import ActivitySelect from '../components/ActivitySelect.vue'
import { loadDrawRecords } from '../api.js'

const route = useRoute()

const loading = ref(false)
const exporting = ref(false)
const rows = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 20
const allActivities = ref([])

const filter = reactive({
  activityId: route.query.activityId ? Number(route.query.activityId) : '',
  userName: ''
})

function currentActivityTitle() {
  if (!filter.activityId) return undefined
  const matched = allActivities.value.find((item) => Number(item.activityId) === Number(filter.activityId))
  return matched?.activityTitle
}

function fmt(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

async function fetchList() {
  loading.value = true
  try {
    const data = await loadDrawRecords({
      page: page.value,
      limit: pageSize,
      userName: filter.userName.trim() || undefined,
      activityTitle: currentActivityTitle()
    })
    rows.value = data.list
    total.value = data.totalCount
  } catch (error) {
    ElMessage.error(error?.message || '加载抽奖记录失败')
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
  filter.userName = ''
  search()
}

function onActivitiesLoaded(list) {
  allActivities.value = list
  if (filter.activityId && !currentActivityTitle()) {
    filter.activityId = ''
  }
  fetchList()
}

function exportCsv() {
  exporting.value = true
  loadDrawRecords({
    page: 1,
    limit: 1000,
    userName: filter.userName.trim() || undefined,
    activityTitle: currentActivityTitle()
  })
    .then((data) => {
      if (!data.list.length) {
        ElMessage.warning('当前条件下没有可导出的记录')
        return
      }
      const header = ['记录ID', '活动', '用户ID', '用户名/手机号', '奖项等级', '奖项名称', '抽奖时间']
      const lines = data.list.map((row) =>
        [row.id, row.activityTitle, row.userId, row.userName, row.prizeGrade, row.prizeName, fmt(row.updateDate)]
          .map((cell) => `"${String(cell ?? '').replace(/"/g, '""')}"`)
          .join(',')
      )
      const csv = `\ufeff${[header.join(','), ...lines].join('\n')}`
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `抽奖记录-${new Date().toISOString().slice(0, 10)}.csv`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
      ElMessage.success(`已导出 ${data.list.length} 条记录`)
    })
    .catch((error) => ElMessage.error(error?.message || '导出失败'))
    .finally(() => {
      exporting.value = false
    })
}

onMounted(() => {
  // 活动下拉加载完成后再查（需要活动标题做后端过滤）
  if (!filter.activityId) fetchList()
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
.lucky-filter {
  margin-bottom: 8px;
}
.lucky-user {
  display: flex;
  align-items: center;
  gap: 8px;
}
.lucky-user__meta {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}
.lucky-user__name {
  font-weight: 500;
}
.lucky-user__id {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}
.lucky-pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
