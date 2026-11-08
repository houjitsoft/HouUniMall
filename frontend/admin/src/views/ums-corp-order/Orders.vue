<template>
  <div class="ums-page">
    <el-card shadow="never" class="mb">
      <el-form :inline="true" :model="search">
        <el-form-item label="商户订单号">
          <el-input v-model="search.orderNo" clearable placeholder="orderNo" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="search.orderState" clearable placeholder="全部" style="width: 140px">
            <el-option v-for="(t, k) in STATE_TEXT" :key="k" :label="t" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">搜索</el-button>
          <el-button type="success" @click="createVisible = true">创建订单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe empty-text="暂无数据">
        <el-table-column prop="order_no" label="商户订单号" min-width="160" />
        <el-table-column prop="src_req_id" label="请求流水" min-width="180" show-overflow-tooltip />
        <el-table-column prop="order_amt" label="金额(分)" width="100" />
        <el-table-column prop="order_state" label="状态" width="110">
          <template #default="{ row }">{{ STATE_TEXT[row.order_state] || row.order_state }}</template>
        </el-table-column>
        <el-table-column prop="payer_name" label="买家" width="120" />
        <el-table-column prop="mark_no" label="收款账号" min-width="140" show-overflow-tooltip />
        <el-table-column prop="create_time" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="queryRemote(row)">查银商</el-button>
            <el-button link type="warning" @click="closeRemote(row)">关闭</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="page.pageNum"
          v-model:page-size="page.pageSize"
          :total="page.total"
          layout="total, prev, pager, next"
          @current-change="load"
        />
      </div>
    </el-card>

    <el-dialog v-model="createVisible" title="创建企业订单 (202101)" width="560px">
      <el-form :model="createForm" label-width="110px">
        <el-form-item label="订单号" required><el-input v-model="createForm.orderNo" /></el-form-item>
        <el-form-item label="金额(元)" required>
          <el-input-number v-model="createForm.orderAmtYuan" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="买家用户号" required><el-input v-model="createForm.payerNo" /></el-form-item>
        <el-form-item label="买家名称" required><el-input v-model="createForm.payerName" /></el-form-item>
        <el-form-item label="商品名称" required><el-input v-model="createForm.goodsName" /></el-form-item>
        <el-form-item label="有效天数"><el-input v-model="createForm.rsv1" placeholder="可选，如 30" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="doCreate">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { umsCorpApi } from '@/api/ums-corp-order'

const STATE_TEXT = {
  '0': '待支付', '1': '已支付', '2': '已失效', '3': '已关闭',
  '4': '已退货', '5': '部分退货', '6': '退货失败', '7': '部分支付',
  '8': '退货中', '9': '退货已撤销'
}

const loading = ref(false)
const creating = ref(false)
const createVisible = ref(false)
const list = ref([])
const search = reactive({ orderNo: '', orderState: '' })
const page = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const createForm = reactive({
  orderNo: '', orderAmtYuan: 0.01, payerNo: '', payerName: '', goodsName: '', rsv1: ''
})

async function load() {
  loading.value = true
  try {
    const res = await umsCorpApi.orderPage({ ...search, pageNum: page.pageNum, pageSize: page.pageSize })
    const data = res?.data || res || {}
    list.value = data.records || []
    page.total = data.total || 0
  } catch (e) {
    ElMessage.error(e?.message || '加载订单失败')
    list.value = []
  } finally {
    loading.value = false
  }
}

async function doCreate() {
  creating.value = true
  try {
    const payload = {
      orderNo: createForm.orderNo,
      orderAmt: Math.round(Number(createForm.orderAmtYuan) * 100),
      payerNo: createForm.payerNo,
      payerName: createForm.payerName,
      goodsName: createForm.goodsName
    }
    if (createForm.rsv1) payload.rsv1 = createForm.rsv1
    const res = await umsCorpApi.orderCreate(payload)
    if (res?.code != null && ![0,1,200].includes(Number(res.code))) {
      ElMessage.error(res.message || '创建失败')
      return
    }
    ElMessage.success('已提交企业订单')
    createVisible.value = false
    await load()
  } catch (e) {
    ElMessage.error(e?.message || '创建失败')
  } finally {
    creating.value = false
  }
}

async function queryRemote(row) {
  try {
    const res = await umsCorpApi.orderQuery({ oriReqId: row.src_req_id })
    ElMessage.success(`查询完成：状态 ${res?.data?.orderState ?? res?.orderState ?? '-'}`)
    await load()
  } catch (e) {
    ElMessage.error(e?.message || '查询失败')
  }
}

async function closeRemote(row) {
  await ElMessageBox.confirm(`确认关闭订单 ${row.order_no}？`, '关闭订单')
  try {
    await umsCorpApi.orderClose({ oriReqId: row.src_req_id })
    ElMessage.success('已发起关闭')
    await load()
  } catch (e) {
    ElMessage.error(e?.message || '关闭失败')
  }
}

onMounted(load)
</script>

<style scoped>
.ums-page { padding: 16px; }
.mb { margin-bottom: 12px; }
.pager { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
