<template>
  <div>
    <el-card class="!border-none" shadow="never">
      <el-alert type="warning" :closable="false" show-icon>
        <template #title>
          <div>1、会员商城下单列表</div>
          <div>2、订单状态有待付款，待发货，待收货，已完成，已关闭</div>
          <div>3、待付款订单取消后则为已关闭。待付款订单支付后则为待发货。待发货订单发货后则为待收货。待收货订单收货后则为已完成。</div>
        </template>
      </el-alert>

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
                <el-option label="商品名称" value="1" />
                <el-option label="商品货号" value="2" />
                <el-option label="订单编号" value="3" />
              </el-select>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="买家信息">
          <el-input
            v-model="queryParams.buyer_key_word"
            placeholder="请输入"
            class="w-[320px]"
            @keyup.enter="handleSearch"
          >
            <template #prepend>
              <el-select v-model="queryParams.buyer_key_word_type" placeholder="Select" style="width: 115px">
                <el-option label="用户昵称" value="1" />
                <el-option label="用户编号" value="2" />
                <el-option label="收货人姓名" value="3" />
                <el-option label="收货人手机" value="4" />
              </el-select>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="订单来源">
          <el-select class="w-[280px]" v-model="queryParams.from">
            <el-option label="全部" value="" />
            <el-option label="微信小程序" value="wechat" />
            <el-option label="H5" value="h5" />
            <el-option label="PC" value="pc" />
            <el-option label="APP" value="app" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select class="w-[280px]" v-model="queryParams.pay_way">
            <el-option label="全部" value="" />
            <el-option label="微信支付" value="wechat" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="余额支付" value="balance" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间类型">
          <el-select v-model="queryParams.time_type" placeholder="全部" style="width: 115px">
            <el-option label="下单时间" value="create" />
            <el-option label="支付时间" value="pay" />
          </el-select>
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
        <el-tab-pane :label="`全部(${tabCount.all_count || 0})`" name="" />
        <el-tab-pane :label="`待支付(${tabCount.pay_count || 0})`" :name="0" />
        <el-tab-pane :label="`待发货(${tabCount.delivery_count || 0})`" :name="1" />
        <el-tab-pane :label="`待收货(${tabCount.receive_count || 0})`" :name="2" />
        <el-tab-pane :label="`已完成(${tabCount.finish_count || 0})`" :name="3" />
        <el-tab-pane :label="`已取消(${tabCount.close_count || 0})`" :name="4" />
      </el-tabs>

      <!-- 表格 -->
      <el-table size="large" v-loading="loading" :data="list">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="sn" label="订单编号" width="180" />
        <el-table-column prop="type_text" label="订单类型" width="160">
          <template #default="{ row }">
            <span>{{ row.type_text }}</span>
            <span v-if="row?.pt_open?.status_text">({{ row?.pt_open?.status_text }})</span>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="买家昵称" width="130" />
        <el-table-column label="商品信息" min-width="220">
          <template #default="{ row }">
            <div
              v-for="(item, index) in (row.goods || [])"
              :key="index"
              v-show="index <= 2"
              class="flex mb-[5px]"
            >
              <el-image
                :src="item.goods_image"
                fit="contain"
                style="width: 70px; height: 70px; flex-shrink: 0"
              />
              <div class="ml-[6px] text-xs overflow-hidden">
                <el-tooltip effect="dark" :content="item.goods_name" placement="top">
                  <div class="text-base truncate" style="max-width: 120px">{{ item.goods_name }}</div>
                </el-tooltip>
                <div class="text-gray-400">{{ Array.isArray(item.goods_sku_value) ? item.goods_sku_value.join(',') : '' }}</div>
                <div>¥{{ item.goods_price }} &nbsp;x {{ item.goods_num }}</div>
              </div>
            </div>
            <el-button
              type="primary"
              link
              v-if="(row.goods || []).length > 2"
              @click="handleViewAllGoods(row)"
            >查看更多</el-button>
          </template>
        </el-table-column>
        <el-table-column label="实付金额" min-width="160">
          <template #default="{ row }">
            <div>¥{{ row.need_pay_money }}</div>
            <div v-if="row.express_money != '0.00'" class="text-gray-400 mt-2">
              (含运费：¥{{ row.express_money }})
            </div>
          </template>
        </el-table-column>
        <el-table-column label="收货人" min-width="90">
          <template #default="{ row }">
            <el-popover placement="top" width="300px" trigger="hover">
              <div>收件人: {{ row.address_contact }}</div>
              <div class="mt-[20px]">手机号: {{ row.address_mobile }}</div>
              <div class="mt-[20px]">地&nbsp;&nbsp;&nbsp;址: {{ row.address_content }}</div>
              <template #reference>
                <el-tag>{{ row.address_contact }}</el-tag>
              </template>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column label="支付状态" min-width="100">
          <template #default="{ row }">
            <div v-if="row.pay_is">已支付</div>
            <div v-else class="text-red-500">未支付</div>
          </template>
        </el-table-column>
        <el-table-column prop="status_text" label="订单状态" min-width="100" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleViewDetail(row)"
            >
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
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()

/** 加载状态 */
const loading = ref(false)
/** 列表数据 */
const list = ref([])
/** tab 数量统计 */
const tabCount = reactive({
  all_count: 0,
  pay_count: 0,
  delivery_count: 0,
  receive_count: 0,
  finish_count: 0,
  close_count: 0
})
/** 日期范围 */
const dateRange = ref([])

/** 查询参数 */
const queryParams = reactive({
  status: '',
  key_word: '',
  key_word_type: '1',
  buyer_key_word: '',
  buyer_key_word_type: '1',
  from: '',
  pay_way: '',
  time_type: '',
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
 * 获取订单列表数据
 */
const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.post('/web/mall/order/manage/list', {
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
  queryParams.key_word = ''
  queryParams.key_word_type = '1'
  queryParams.buyer_key_word = ''
  queryParams.buyer_key_word_type = '1'
  queryParams.from = ''
  queryParams.pay_way = ''
  queryParams.time_type = ''
  queryParams.start_time = ''
  queryParams.end_time = ''
  dateRange.value = []
  handleSearch()
}

/**
 * 切换 Tab（订单状态）
 * @param {string|number} name - tab name
 */
const handleTabChange = (name) => {
  queryParams.status = name
  pagination.pageNo = 1
  fetchList()
}

/**
 * 跳转订单详情
 * @param {object} row - 当前订单行
 */
const handleViewDetail = (row) => {
  router.push({ path: '/mall/order/detail', query: { id: row.id } })
}

/**
 * 查看全部商品
 * @param {object} row - 当前订单行
 */
const handleViewAllGoods = (row) => {
  ElMessage.info('查看全部商品')
}

onMounted(fetchList)
</script>
