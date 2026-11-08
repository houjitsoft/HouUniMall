<template>
  <div class="rule-config-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>风控规则配置</span>
          <el-button type="primary" size="small" @click="showCreate">新增规则</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="ruleName" label="规则名称" min-width="140" />
        <el-table-column prop="ruleCode" label="规则编码" width="140" />
        <el-table-column prop="ruleType" label="规则类型" width="110" />
        <el-table-column prop="threshold" label="阈值" width="100" />
        <el-table-column prop="riskLevel" label="风险等级" width="90">
          <template #default="{ row }">
            <el-tag :type="row.riskLevel === 'high' ? 'danger' : row.riskLevel === 'medium' ? 'warning' : 'info'" size="small">
              {{ row.riskLevel === 'high' ? '高' : row.riskLevel === 'medium' ? '中' : '低' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="handleToggle(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(row)">
              <template #reference><el-button link type="danger" size="small">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:12px;display:flex;justify-content:flex-end;">
        <el-pagination v-model:current-page="page.current" v-model:page-size="page.size" :total="page.total"
          :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" background @change="loadData" />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editRow ? '编辑规则' : '新增规则'" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="规则名称"><el-input v-model="form.ruleName" /></el-form-item>
        <el-form-item label="规则编码"><el-input v-model="form.ruleCode" /></el-form-item>
        <el-form-item label="规则类型">
          <el-select v-model="form.ruleType" style="width:100%">
            <el-option label="交易金额" value="amount" /><el-option label="交易频率" value="frequency" />
            <el-option label="设备指纹" value="device" /><el-option label="地域限制" value="region" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值"><el-input v-model="form.threshold" /></el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="form.riskLevel" style="width:100%">
            <el-option label="低" value="low" /><el-option label="中" value="medium" /><el-option label="高" value="high" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

const loading = ref(false)
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const dialogVisible = ref(false)
const editRow = ref(null)
const form = reactive({ ruleName: '', ruleCode: '', ruleType: 'amount', threshold: '', riskLevel: 'medium', description: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.post('/web/pay/risk/rule/list', { page: page.current, size: page.size })
    if (res.code === 200 && res.data) {
      tableData.value = res.data.records || res.data.list || []
      page.total = res.data.total || 0
    }
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const showCreate = () => { editRow.value = null; Object.assign(form, { ruleName:'', ruleCode:'', ruleType:'amount', threshold:'', riskLevel:'medium', description:'' }); dialogVisible.value = true }
const handleEdit = (row) => { editRow.value = row; Object.assign(form, row); dialogVisible.value = true }

const handleSave = async () => {
  const api = editRow.value ? '/web/pay/risk/rule/update' : '/web/pay/risk/rule/create'
  const data = { ...form }; if (editRow.value) data.id = editRow.value.id
  const res = await request.post(api, data)
  if (res.code === 200) { ElMessage.success('保存成功'); dialogVisible.value = false; loadData() } else { ElMessage.error(res.message || '操作失败') }
}

const handleToggle = async (row) => {
  const res = await request.post('/web/pay/risk/rule/changeStatus', { id: row.id, status: row.status === 1 ? 0 : 1 })
  if (res.code === 200) { ElMessage.success('状态变更成功'); loadData() } else { ElMessage.error(res.message || '操作失败') }
}

const handleDelete = async (row) => {
  const res = await request.post('/web/pay/risk/rule/delete', { id: row.id })
  if (res.code === 200) { ElMessage.success('删除成功'); loadData() } else { ElMessage.error(res.message || '操作失败') }
}

onMounted(() => loadData())
</script>

<style scoped>
.rule-config-page { padding: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
