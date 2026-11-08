<template>
  <div class="distributor-level">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>分销等级</span>
          <el-button type="primary" @click="openCreate">新增等级</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="等级名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="level1Rate" label="一级比例" width="100" align="right" />
        <el-table-column prop="level2Rate" label="二级比例" width="100" align="right" />
        <el-table-column prop="upgradeAmount" label="升级金额" width="110" align="right" />
        <el-table-column prop="upgradeOrderCount" label="升级订单数" width="110" align="right" />
        <el-table-column prop="upgradeSubCount" label="升级下级数" width="110" align="right" />
        <el-table-column prop="sort" label="排序" width="80" align="right" />
        <el-table-column prop="isDefault" label="默认" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault === 1" type="success">默认</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无分销等级" />
        </template>
      </el-table>
      <!-- 后端 /level/list 不分页（返回全量数组），因此这里没有分页栏 -->
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑等级' : '新增等级'" width="640px">
      <el-form :model="form" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="等级名称"><el-input v-model="form.name" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="图标"><el-input v-model="form.icon" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="一级比例"><el-input-number v-model="form.level1Rate" :min="0" :precision="2" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="二级比例"><el-input-number v-model="form.level2Rate" :min="0" :precision="2" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="升级金额"><el-input-number v-model="form.upgradeAmount" :min="0" :precision="2" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="升级订单数"><el-input-number v-model="form.upgradeOrderCount" :min="0" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="升级下级数"><el-input-number v-model="form.upgradeSubCount" :min="0" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { distributorLevelApi } from '../api/distribution'
import { runAction, useTable } from '../utils/useTable'

const { loading, list, load } = useTable(distributorLevelApi.list, { defaultQuery: {} })

const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)

function emptyForm() {
  return {
    id: null, name: '', icon: '', level1Rate: 0, level2Rate: 0,
    upgradeAmount: 0, upgradeOrderCount: 0, upgradeSubCount: 0, sort: 0
  }
}

const form = reactive(emptyForm())

function openCreate() {
  isEdit.value = false
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, emptyForm(), JSON.parse(JSON.stringify(row)))
  dialogVisible.value = true
}

async function submit() {
  if (!form.name) {
    ElMessage.warning('请填写等级名称')
    return
  }
  submitting.value = true
  const action = isEdit ? distributorLevelApi.update : distributorLevelApi.create
  await runAction(() => action({ ...form }), () => {}, isEdit ? '已保存' : '已创建')
  submitting.value = false
  dialogVisible.value = false
  await load()
}

async function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  await runAction(
    () => distributorLevelApi.update({ ...row, status: next }),
    load,
    next === 1 ? '已启用' : '已停用'
  )
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除等级「${row.name}」？`, '删除确认', { type: 'warning' })
  await runAction(() => distributorLevelApi.remove(row.id), load, '已删除')
}

onMounted(load)
</script>

<style scoped>
.card-header { display: flex; align-items: center; justify-content: space-between; }
.search-form { margin-bottom: 8px; }
</style>
