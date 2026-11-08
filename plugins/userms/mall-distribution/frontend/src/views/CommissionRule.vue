<template>
  <div class="commission-rule">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>佣金规则</span>
          <el-button type="primary" @click="openCreate">新增规则</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="规则名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="type" label="规则类型" width="100" />
        <el-table-column prop="relatedId" label="关联ID" width="100" />
        <el-table-column prop="level1Rate" label="一级比例" width="100" align="right" />
        <el-table-column prop="level2Rate" label="二级比例" width="100" align="right" />
        <el-table-column prop="level1Amount" label="一级金额" width="110" align="right" />
        <el-table-column prop="level2Amount" label="二级金额" width="110" align="right" />
        <el-table-column prop="calcType" label="计算方式" width="100" />
        <el-table-column prop="priority" label="优先级" width="90" align="right" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无佣金规则" />
        </template>
      </el-table>
      <!-- 后端 /rule/list 不分页，因此没有分页栏 -->
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑规则' : '新增规则'" width="640px">
      <el-form :model="form" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="规则名称"><el-input v-model="form.name" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规则类型"><el-input-number v-model="form.type" :min="0" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联ID"><el-input-number v-model="form.relatedId" :min="0" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计算方式"><el-input-number v-model="form.calcType" :min="0" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="一级比例"><el-input-number v-model="form.level1Rate" :min="0" :precision="2" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="二级比例"><el-input-number v-model="form.level2Rate" :min="0" :precision="2" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="一级金额"><el-input-number v-model="form.level1Amount" :min="0" :precision="2" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="二级金额"><el-input-number v-model="form.level2Amount" :min="0" :precision="2" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级"><el-input-number v-model="form.priority" :min="0" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
            </el-form-item>
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
import { commissionRuleApi } from '../api/distribution'
import { runAction, useTable } from '../utils/useTable'

const { loading, list, load } = useTable(commissionRuleApi.list, { defaultQuery: {} })

const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)

function emptyForm() {
  return {
    id: null, name: '', type: 0, relatedId: 0, calcType: 0,
    level1Rate: 0, level2Rate: 0, level1Amount: 0, level2Amount: 0,
    priority: 0, status: 1
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
    ElMessage.warning('请填写规则名称')
    return
  }
  submitting.value = true
  const action = isEdit ? commissionRuleApi.update : commissionRuleApi.create
  await runAction(() => action({ ...form }), () => {}, isEdit ? '已保存' : '已创建')
  submitting.value = false
  dialogVisible.value = false
  await load()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除佣金规则「${row.name}」？`, '删除确认', { type: 'warning' })
  await runAction(() => commissionRuleApi.remove(row.id), load, '已删除')
}

onMounted(load)
</script>

<style scoped>
.card-header { display: flex; align-items: center; justify-content: space-between; }
</style>
