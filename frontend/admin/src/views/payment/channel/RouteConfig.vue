<template>
  <div class="route-config-page">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="路由目标渠道须写 PayMethodEnum / Strategy 码：WECHAT_V3、ALIPAY、STRIPE（可真联调）。旧码 WX/ALI/UNION/YSF 不会命中收银台 Strategy。"
      style="margin-bottom: 12px;"
    />
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>路由规则配置</span>
          <el-button type="primary" size="small" @click="showCreate">新增规则</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="routeName" label="规则名称" min-width="140" />
        <el-table-column prop="channelCode" label="目标渠道" min-width="160">
          <template #default="{ row }">
            <span>{{ channelLabel(row.channelCode) }}</span>
            <el-tag v-if="isStub(row.channelCode)" type="warning" size="small" style="margin-left:6px;">骨架</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" align="center" />
        <el-table-column prop="bizType" label="业务类型" width="100" />
        <el-table-column prop="minAmount" label="最小金额" width="100" align="right" />
        <el-table-column prop="maxAmount" label="最大金额" width="100" align="right" />
        <el-table-column prop="weight" label="权重" width="70" align="center" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="handleToggle(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editRow ? '编辑路由规则' : '新增路由规则'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="规则名称"><el-input v-model="form.routeName" /></el-form-item>
        <el-form-item label="目标渠道">
          <el-select v-model="form.channelCode" style="width:100%" filterable>
            <el-option
              v-for="opt in channelOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="form.bizType" style="width:100%">
            <el-option label="支付" value="1" /><el-option label="退款" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级"><el-input-number v-model="form.priority" :min="1" :max="99" /></el-form-item>
        <el-form-item label="金额范围">
          <el-col :span="11"><el-input-number v-model="form.minAmount" :min="0" placeholder="最小" style="width:100%" /></el-col>
          <el-col :span="2" style="text-align:center">~</el-col>
          <el-col :span="11"><el-input-number v-model="form.maxAmount" :min="0" placeholder="最大" style="width:100%" /></el-col>
        </el-form-item>
        <el-form-item label="权重"><el-input-number v-model="form.weight" :min="1" :max="100" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'
import {
  channelSelectOptions,
  findChannel,
  CHANNEL_MATURITY
} from '@/views/payment/channel/channelCatalog'

const channelOptions = computed(() => channelSelectOptions({ includeStub: true }))
const channelLabel = (code) => {
  const c = findChannel(code)
  if (c) return c.name
  // 兼容历史错误缩写，仅展示提示
  const legacy = { WX: 'WX（请改为 WECHAT_V3）', ALI: 'ALI（请改为 ALIPAY）', UNION: 'UNION（无 Strategy）', YSF: 'YSF（请改为 YSF_PAY）' }
  return legacy[code] || code || '-'
}
const isStub = (code) => {
  const c = findChannel(code)
  return c ? c.maturity === CHANNEL_MATURITY.STUB : false
}

const loading = ref(false)
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const dialogVisible = ref(false)
const editRow = ref(null)
const form = reactive({ routeName:'', channelCode:'WECHAT_V3', bizType:'1', priority:10, minAmount:0, maxAmount:0, weight:50 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.post('/web/pay/channel/route/list', { page: page.current, size: page.size })
    if (res.code === 200 && res.data) { tableData.value = res.data.records || res.data.list || []; page.total = res.data.total || 0 }
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const showCreate = () => { editRow.value = null; Object.assign(form, { routeName:'', channelCode:'WECHAT_V3', bizType:'1', priority:10, minAmount:0, maxAmount:0, weight:50 }); dialogVisible.value = true }
const handleEdit = (row) => { editRow.value = row; Object.assign(form, row); dialogVisible.value = true }

const handleSave = async () => {
  const data = { ...form }; if (editRow.value) data.id = editRow.value.id
  const res = await request.post('/web/pay/channel/route/save', data)
  if (res.code === 200) { ElMessage.success('保存成功'); dialogVisible.value = false; loadData() } else { ElMessage.error(res.message || '操作失败') }
}

const handleToggle = async (row) => {
  const res = await request.post('/web/pay/channel/route/changeStatus', { id: row.id, status: row.status === 1 ? 0 : 1 })
  if (res.code === 200) { ElMessage.success('状态变更成功'); loadData() } else { ElMessage.error(res.message || '操作失败') }
}

const handleDelete = async (row) => {
  const res = await request.post('/web/pay/channel/route/delete', { id: row.id })
  if (res.code === 200) { ElMessage.success('删除成功'); loadData() } else { ElMessage.error(res.message || '操作失败') }
}

onMounted(() => loadData())
</script>

<style scoped>
.route-config-page { padding: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
