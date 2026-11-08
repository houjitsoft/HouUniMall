<template>
  <div>
    <el-card class="!border-none" shadow="never">
      <el-alert
        type="warning"
        title="1、审核用户申请的售后订单；2、申请售后需要用户确认收货之后才能申请，平台同意退款后处于等待退款的状态，平台会自动进行原路退款操作"
        :closable="false"
        show-icon
      />

      <el-form ref="formRef" class="mb-[-16px] mt-[16px]" :model="queryParams" :inline="true">
        <el-form-item label="订单搜索">
          <el-input
            v-model="queryParams.key_word"
            placeholder="请输入"
            class="w-[320px]"
            @keyup.enter="handleSearch"
          >
            <template #prepend>
              <el-select v-model="queryParams.key_word_type" placeholder="选择" style="width: 100px">
                <el-option label="订单编号" value="1" />
                <el-option label="用户编号" value="2" />
                <el-option label="用户昵称" value="3" />
                <el-option label="收货人姓名" value="4" />
                <el-option label="收货人手机" value="5" />
                <el-option label="售后单号" value="6" />
              </el-select>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input
            v-model="queryParams.goods_name"
            placeholder="请输入"
            class="w-[280px]"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="退款方式">
          <el-select class="w-[280px]" v-model="queryParams.type">
            <el-option label="全部" value="" />
            <el-option label="仅退款" value="0" />
            <el-option label="退款退货" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px"
            @change="onDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="!border-none mt-4" :body-style="{ 'padding-top': '10px' }" shadow="never">
      <!-- 选项卡 -->
      <el-tabs v-model="queryParams.status" @tab-change="handleTabChange">
        <el-tab-pane :label="`全部(${tabCount.all || 0})`" name="" />
        <el-tab-pane :label="`申请中(${tabCount.apply || 0})`" :name="0" />
        <el-tab-pane :label="`等待退款(${tabCount.wait_refund || 0})`" :name="10" />
        <el-tab-pane :label="`售后成功(${tabCount.success || 0})`" :name="20" />
        <el-tab-pane :label="`售后失败(${tabCount.fail || 0})`" :name="30" />
      </el-tabs>

      <!-- 表格 -->
      <el-table size="large" v-loading="loading" :data="list">
        <el-table-column prop="sn" label="售后单号" width="180" />
        <el-table-column label="买家信息" min-width="140">
          <template #default="{ row }">
            <el-popover placement="top" width="300px" trigger="hover">
              <div class="flex items-center">
                <span class="mr-4">头像: </span>
                <el-avatar :size="50" :src="row?.user?.avatar" />
              </div>
              <div class="mt-[20px]">
                <span class="mr-4">昵称: </span>
                <span>{{ row?.user?.nickname }}</span>
              </div>
              <div class="mt-[20px]">
                <span class="mr-4">编号: </span>
                <span>{{ row?.user?.sn }}</span>
              </div>
              <template #reference>
                {{ row?.user?.nickname }}
              </template>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="order_sn" label="订单编号" width="180" />
        <el-table-column label="商品信息" min-width="200">
          <template #default="{ row }">
            <div
              v-for="(item, index) in (row.order_goods || [])"
              :key="index"
              class="flex mb-[5px]"
            >
              <el-image
                :src="item?.goods_image"
                fit="cover"
                style="width: 70px; height: 70px; flex-shrink: 0"
              />
              <div class="ml-[6px] text-xs overflow-hidden">
                <el-tooltip effect="dark" :content="item?.goods_name" placement="top">
                  <div class="text-base truncate" style="max-width: 120px">{{ item?.goods_name }}</div>
                </el-tooltip>
                <div class="text-gray-400">{{ Array.isArray(item?.goods_sku_value) ? item.goods_sku_value.join(',') : '' }}</div>
                <div>¥{{ item?.goods_price }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="数量" min-width="100">
          <template #default="{ row }">
            {{ row.order_goods?.goods_num }}
          </template>
        </el-table-column>
        <el-table-column label="实付金额" min-width="100">
          <template #default="{ row }">¥{{ row.order?.need_pay_money }}</template>
        </el-table-column>
        <el-table-column label="退款金额" min-width="90">
          <template #default="{ row }">¥{{ row.refund_money }}</template>
        </el-table-column>
        <el-table-column prop="type_text" label="退款方式" min-width="90" />
        <el-table-column label="售后状态" min-width="100">
          <template #default="{ row }">
            <div v-if="row.status === 0">申请中</div>
            <div v-if="row.status === 10" class="text-yellow-500">等待退款</div>
            <div v-if="row.status === 20" class="text-green-500">售后成功</div>
            <div v-if="row.status === 30" class="text-red-500">售后失败</div>
            <div>{{ row.sub_status_text }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="create_time" label="申请时间" min-width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">
              订单详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex justify-end mt-4">
        <el-pagination
          v-model:current-page="pagination.pageNo"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 15, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()

/** 加载状态 */
const loading = ref(false)
/** 列表数据 */
const list = ref([])
/** tab 数量统计 */
const tabCount = reactive({ all: 0, apply: 0, wait_refund: 0, success: 0, fail: 0 })
/** 日期范围 */
const dateRange = ref([])

/** 查询参数 */
const queryParams = reactive({
  status: '',
  goods_name: '',
  key_word: '',
  key_word_type: '1',
  type: '',
  start_time: '',
  end_time: ''
})

/** 分页信息 */
const pagination = reactive({ pageNo: 1, pageSize: 15, total: 0 })

/**
 * 日期范围变化处理
 * @param {Array} val - 日期范围数组
 */
const onDateChange = (val) => {
  queryParams.start_time = val ? val[0] : ''
  queryParams.end_time = val ? val[1] : ''
}

/**
 * 获取售后订单列表数据
 */
const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.post('/web/mall/order/manage/aftersale/list', {
      ...queryParams,
      page_no: pagination.pageNo,
      page_size: pagination.pageSize
    })
    if (res && res.code === 1 && res.data) {
      list.value = res.data.lists || []
      pagination.total = res.data.count || 0
      if (res.data.extend) {
        Object.assign(tabCount, res.data.extend)
      }
    } else {
      list.value = []
      pagination.total = 0
    }
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 查询（重置到第一页）
 */
const handleSearch = () => {
  pagination.pageNo = 1
  fetchList()
}

/**
 * 重置查询参数
 */
const handleReset = () => {
  queryParams.status = ''
  queryParams.goods_name = ''
  queryParams.key_word = ''
  queryParams.key_word_type = '1'
  queryParams.type = ''
  queryParams.start_time = ''
  queryParams.end_time = ''
  dateRange.value = []
  handleSearch()
}

/**
 * 切换 Tab
 * @param {string|number} name - tab name
 */
const handleTabChange = (name) => {
  queryParams.status = name
  pagination.pageNo = 1
  fetchList()
}

/**
 * 跳转售后订单详情
 * @param {object} row - 当前售后订单行
 */
const handleViewDetail = (row) => {
  router.push({ path: '/mall/order/after-sale-detail', query: { id: row.id } })
}

onMounted(fetchList)
</script>
