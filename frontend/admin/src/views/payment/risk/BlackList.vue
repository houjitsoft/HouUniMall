<template>
  <div class="blacklist-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>黑白名单管理</span>
          <el-button type="primary" size="small" @click="showAdd">新增名单</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="listType" label="名单类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.listType === 'black' ? 'danger' : 'success'" size="small">{{ row.listType === 'black' ? '黑名单' : '白名单' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="对象类型" width="100" />
        <el-table-column prop="targetValue" label="对象值" min-width="160" />
        <el-table-column prop="reason" label="原因" min-width="180" />
        <el-table-column prop="expireTime" label="过期时间" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确认移除?" @confirm="handleRemove(row)">
              <template #reference><el-button link type="danger" size="small">移除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:12px;display:flex;justify-content:flex-end;">
        <el-pagination v-model:current-page="page.current" v-model:page-size="page.size" :total="page.total"
          :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" background @change="loadData" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增黑白名单" width="450px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名单类型">
          <el-select v-model="form.listType" style="width:100%">
            <el-option label="黑名单" value="black" /><el-option label="白名单" value="white" />
          </el-select>
        </el-form-item>
        <el-form-item label="对象类型">
          <el-select v-model="form.targetType" style="width:100%">
            <el-option label="用户ID" value="userId" /><el-option label="商户号" value="merchantNo" />
            <el-option label="IP地址" value="ip" /><el-option label="设备ID" value="deviceId" />
          </el-select>
        </el-form-item>
        <el-form-item label="对象值"><el-input v-model="form.targetValue" /></el-form-item>
        <el-form-item label="原因"><el-input v-model="form.reason" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="过期时间"><el-date-picker v-model="form.expireTime" type="datetime" style="width:100%" /></el-form-item>
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
const form = reactive({ listType: 'black', targetType: 'userId', targetValue: '', reason: '', expireTime: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.post('/web/pay/risk/blacklist/list', { page: page.current, size: page.size })
    if (res.code === 200 && res.data) {
      tableData.value = res.data.records || res.data.list || []
      page.total = res.data.total || 0
    }
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const showAdd = () => { Object.assign(form, { listType:'black', targetType:'userId', targetValue:'', reason:'', expireTime:'' }); dialogVisible.value = true }

const handleSave = async () => {
  const res = await request.post('/web/pay/risk/blacklist/add', { ...form })
  if (res.code === 200) { ElMessage.success('添加成功'); dialogVisible.value = false; loadData() } else { ElMessage.error(res.message || '操作失败') }
}

const handleRemove = async (row) => {
  const res = await request.post('/web/pay/risk/blacklist/remove', { id: row.id })
  if (res.code === 200) { ElMessage.success('移除成功'); loadData() } else { ElMessage.error(res.message || '操作失败') }
}

onMounted(() => loadData())
</script>

<style scoped>
.blacklist-page { padding: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
