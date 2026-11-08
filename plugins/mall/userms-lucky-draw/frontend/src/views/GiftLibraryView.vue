<template>
  <main class="lucky-page">
    <header class="lucky-masthead">
      <div>
        <h1>礼品库</h1>
        <p class="lucky-lead">
          维护活动可发放的礼品池。礼品可为实物（图片 + 名称）或红包（绑定红包模板），在「奖项设置」中与奖项关联。
        </p>
      </div>
      <el-button type="primary" @click="openCreate">新增礼品</el-button>
    </header>

    <el-card shadow="never">
      <el-form :inline="true" class="lucky-filter" @submit.prevent>
        <el-form-item label="礼品名称">
          <el-input
            v-model="filter.giftName"
            clearable
            placeholder="搜索礼品名称"
            style="width: 200px"
            @keyup.enter="search"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="search">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rows" v-loading="loading" row-key="giftId">
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <el-image
              v-if="row.giftImage"
              :src="row.giftImage"
              :preview-src-list="[row.giftImage]"
              fit="cover"
              preview-teleported
              class="lucky-thumb"
            />
            <span v-else class="lucky-thumb lucky-thumb--empty">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="giftId" label="ID" width="70" />
        <el-table-column prop="giftName" label="礼品名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="Number(row.giftIsRedpacket) === 1 ? 'danger' : 'info'">
              {{ Number(row.giftIsRedpacket) === 1 ? '红包' : '实物/权益' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="红包模板" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ Number(row.giftIsRedpacket) === 1 ? (row.redpacketTTitle || `模板 #${row.redpacketTId}`) : '—' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="Number(row.giftIsEnable) === 1 ? 'success' : 'info'">
              {{ Number(row.giftIsEnable) === 1 ? '可用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="170">
          <template #default="{ row }">{{ fmt(row.updateDate) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="还没有礼品，点击右上角「新增礼品」开始" />
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

    <el-dialog v-model="dialogVisible" :title="form.giftId ? '编辑礼品' : '新增礼品'" width="560px">
      <el-form label-position="top">
        <el-form-item label="礼品名称" required>
          <el-input v-model="form.giftName" maxlength="60" placeholder="例如：10 元无门槛红包 / 定制马克杯" />
        </el-form-item>
        <el-form-item label="礼品类型">
          <el-radio-group v-model="form.giftIsRedpacket">
            <el-radio :value="2">实物 / 权益</el-radio>
            <el-radio :value="1">红包</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.giftIsRedpacket === 1" label="红包模板 ID" required>
          <el-input-number v-model="form.redpacketTId" :min="1" controls-position="right" style="width: 200px" />
          <span class="lucky-form-hint">填写红包插件中已创建的模板编号</span>
        </el-form-item>
        <el-form-item label="礼品图片地址">
          <el-input v-model="form.giftImage" placeholder="https://..." clearable />
          <div v-if="form.giftImage" class="lucky-preview">
            <el-image :src="form.giftImage" fit="cover" class="lucky-thumb" />
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.giftIsEnable">
            <el-radio :value="1">可用</el-radio>
            <el-radio :value="2">停用</el-radio>
          </el-radio-group>
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteGift, loadGifts, saveGift } from '../api.js'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 20
const dialogVisible = ref(false)

const filter = reactive({ giftName: '' })

function emptyForm() {
  return {
    giftId: '',
    giftName: '',
    giftIsRedpacket: 2,
    redpacketTId: 0,
    giftImage: '',
    giftIsEnable: 1
  }
}

const form = reactive(emptyForm())

function fmt(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

async function fetchList() {
  loading.value = true
  try {
    const data = await loadGifts({
      page: page.value,
      limit: pageSize,
      giftName: filter.giftName.trim() || undefined
    })
    rows.value = data.list
    total.value = data.totalCount
  } catch (error) {
    ElMessage.error(error?.message || '加载礼品失败')
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
  filter.giftName = ''
  search()
}

function openCreate() {
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    giftId: row.giftId,
    giftName: row.giftName || '',
    giftIsRedpacket: Number(row.giftIsRedpacket) === 1 ? 1 : 2,
    redpacketTId: Number(row.redpacketTId) || 0,
    giftImage: row.giftImage || '',
    giftIsEnable: Number(row.giftIsEnable) === 2 ? 2 : 1
  })
  dialogVisible.value = true
}

async function submit() {
  if (!form.giftName.trim()) {
    ElMessage.warning('请填写礼品名称')
    return
  }
  if (form.giftIsRedpacket === 1 && !Number(form.redpacketTId)) {
    ElMessage.warning('红包礼品必须填写红包模板 ID')
    return
  }
  saving.value = true
  try {
    await saveGift({
      giftId: form.giftId || '',
      giftName: form.giftName.trim(),
      giftIsRedpacket: form.giftIsRedpacket,
      redpacketTId: form.giftIsRedpacket === 1 ? form.redpacketTId : 0,
      giftImage: form.giftImage || '',
      giftIsEnable: form.giftIsEnable
    })
    dialogVisible.value = false
    ElMessage.success('礼品已保存')
    await fetchList()
  } catch (error) {
    ElMessage.error(error?.message || '保存礼品失败')
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除礼品「${row.giftName}」？已被活动奖项关联的礼品不能删除。`,
      '删除礼品',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await deleteGift(row.giftId)
    ElMessage.success('礼品已删除')
    await fetchList()
  } catch (error) {
    ElMessage.error(error?.message || '删除失败')
  }
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
.lucky-filter {
  margin-bottom: 8px;
}
.lucky-thumb {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  background: var(--el-fill-color-light);
}
.lucky-preview {
  margin-top: 8px;
}
.lucky-form-hint {
  margin-left: 12px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.lucky-pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
