<template>
  <main class="pintuan-page">
    <header class="pintuan-masthead">
      <div>
        <h1>拼团设置</h1>
        <p class="pintuan-lead">配置 H5 拼团首页模块标题与挂载商品；模块 1 为固定位不可删，模块 2 可新增/删除。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增模块</el-button>
    </header>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="rows" row-key="id">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="configName" label="模块名称" min-width="140" show-overflow-tooltip />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">{{ Number(row.configType) === 1 ? '固定模块' : '可编辑' }}</template>
        </el-table-column>
        <el-table-column prop="configTitleOne" label="一级标题" min-width="140" show-overflow-tooltip />
        <el-table-column prop="configTitleTwo" label="二级标题" min-width="140" show-overflow-tooltip />
        <el-table-column label="挂载商品" width="100" align="center">
          <template #default="{ row }">{{ (row.configData || []).length }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              link
              type="danger"
              :disabled="Number(row.configType) === 1"
              @click="remove(row)"
            >删除</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="还没有拼团首页模块" /></template>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑模块' : '新增模块'" width="720px" top="8vh">
      <el-form label-position="top">
        <el-form-item label="模块名称" required>
          <el-input v-model="form.configName" maxlength="40" placeholder="例如：热门拼团" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="一级标题">
              <el-input v-model="form.configTitleOne" maxlength="40" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="二级标题">
              <el-input v-model="form.configTitleTwo" maxlength="40" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="挂载商品 commonId">
          <div class="pintuan-goods-head">
            <el-button type="primary" plain @click="openPicker">选择商品</el-button>
            <span class="pintuan-hint">保存为 JSON 数组写入 configData</span>
          </div>
          <el-tag
            v-for="id in form.configData"
            :key="id"
            class="pintuan-tag"
            closable
            @close="removeGoodsId(id)"
          >{{ id }}</el-tag>
          <div v-if="!form.configData.length" class="pintuan-hint">尚未挂载商品</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="picker.visible" title="选择商品" width="780px" append-to-body>
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="商品名称">
          <el-input v-model="picker.keyword" clearable style="width: 220px" @keyup.enter="fetchPicker" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="picker.loading" @click="fetchPicker">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="picker.rows" v-loading="picker.loading" row-key="commonId">
        <el-table-column prop="commonId" label="ID" width="70" />
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">
            <div class="pintuan-pick-cell">
              <img v-if="row.commonImage" :src="row.commonImage" alt="" />
              <span>{{ row.commonName || `商品 ${row.commonId}` }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="commonPrice" label="原价" width="90" />
        <el-table-column prop="commonStock" label="库存" width="90" />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button link type="primary" @click="pick(row)">选择</el-button>
          </template>
        </el-table-column>
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
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteH5Config, loadGoodsOptions, loadH5Configs, saveH5Config } from '../api.js'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const dialogVisible = ref(false)
const form = reactive({
  id: null,
  configType: 2,
  configName: '',
  configTitleOne: '',
  configTitleTwo: '',
  configData: []
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

async function fetchList() {
  loading.value = true
  try {
    rows.value = await loadH5Configs()
  } catch (error) {
    ElMessage.error(error?.message || '加载拼团设置失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    id: null,
    configType: 2,
    configName: '',
    configTitleOne: '',
    configTitleTwo: '',
    configData: []
  })
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    configType: Number(row.configType) || 2,
    configName: row.configName || '',
    configTitleOne: row.configTitleOne || '',
    configTitleTwo: row.configTitleTwo || '',
    configData: [...(row.configData || [])]
  })
  dialogVisible.value = true
}

function removeGoodsId(id) {
  form.configData = form.configData.filter((item) => String(item) !== String(id))
}

async function openPicker() {
  picker.visible = true
  picker.page = 1
  await fetchPicker()
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

function onPickerPage(next) {
  picker.page = next
  fetchPicker()
}

function pick(row) {
  const id = String(row.commonId)
  if (!form.configData.includes(id)) form.configData.push(id)
  picker.visible = false
}

async function save() {
  if (!form.configName.trim()) {
    ElMessage.warning('请填写模块名称')
    return
  }
  saving.value = true
  try {
    await saveH5Config({
      id: form.id,
      configType: form.id ? form.configType : 2,
      configName: form.configName.trim(),
      configTitleOne: form.configTitleOne.trim(),
      configTitleTwo: form.configTitleTwo.trim(),
      configData: form.configData
    })
    ElMessage.success('已保存')
    dialogVisible.value = false
    await fetchList()
  } catch (error) {
    ElMessage.error(error?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  if (Number(row.configType) === 1) return
  try {
    await ElMessageBox.confirm(`确认删除模块「${row.configName}」？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await deleteH5Config(row.id)
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
.pintuan-pager { margin-top: 16px; justify-content: flex-end; }
.pintuan-hint { font-size: 12px; color: var(--el-text-color-secondary); }
.pintuan-goods-head { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.pintuan-tag { margin: 0 8px 8px 0; }
.pintuan-pick-cell { display: flex; align-items: center; gap: 8px; }
.pintuan-pick-cell img { width: 36px; height: 36px; object-fit: cover; border-radius: 4px; }
</style>
