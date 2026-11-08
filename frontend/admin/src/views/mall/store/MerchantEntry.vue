<template>
  <div class="goods-lists">
    <el-card class="!border-none" shadow="never">
      <el-alert
        type="warning"
        title="温馨提示：审核商家入驻申请，审核通过后会自动创建商家；审核通过的商家初始营业状态为：暂停营业"
        :closable="false"
        show-icon
      />

      <el-form ref="formRef" class="mb-[-16px] mt-[16px]" :model="queryParams" :inline="true">
        <el-form-item label="主营类目">
          <el-select class="w-[280px]" v-model="queryParams.cid">
            <el-option label="全部" value="" />
            <el-option
              v-for="item in shopCategoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人姓名">
          <el-input
            class="w-[280px]"
            v-model="queryParams.contact"
            placeholder="请输入联系人姓名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="手机号码">
          <el-input
            class="w-[280px]"
            v-model="queryParams.mobile"
            placeholder="请输入手机号码"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="!border-none mt-4" :body-style="{ 'padding-top': '10px' }" shadow="never">
      <!-- 表格 -->
      <el-table size="large" v-loading="loading" :data="list" class="mt-4">
        <el-table-column label="用户信息" min-width="140">
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
                <span class="mr-4">账号: </span>
                <span>{{ row?.user?.account }}</span>
              </div>
              <template #reference>
                {{ row?.user?.nickname }}
              </template>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column label="经营类目" prop="shop_category.name" min-width="120" />
        <el-table-column label="联系人姓名" prop="contact" min-width="120" />
        <el-table-column label="联系电话" prop="mobile" min-width="120" />
        <el-table-column label="所在地区" min-width="180">
          <template #default="{ row }">
            {{ row.province_name }} {{ row.city_name }} {{ row.district_name }}
          </template>
        </el-table-column>
        <el-table-column label="详细地址" prop="address_info" min-width="120" />
        <el-table-column label="申请说明" prop="apply_content" min-width="120" />
        <el-table-column label="跟进状态" prop="follow_text" min-width="120" />
        <el-table-column label="申请时间" prop="create_time" min-width="120" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.follow == 0"
              type="primary"
              link
              @click="handleFollow(row.id)"
            >
              去跟进
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
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

/** 加载状态 */
const loading = ref(false)
/** 列表数据 */
const list = ref([])
/** 经营类目列表 */
const shopCategoryList = ref([])

/** 查询参数 */
const queryParams = reactive({
  cid: '',
  contact: '',
  mobile: ''
})

/** 分页信息 */
const pagination = reactive({ pageNo: 1, pageSize: 15, total: 0 })

/**
 * 获取入驻申请列表数据
 */
const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.post('/web/mall/admin/shop/apply/lists', {
      ...queryParams,
      page_no: pagination.pageNo,
      page_size: pagination.pageSize
    })
    if (res && res.code === 1 && res.data) {
      list.value = res.data.lists || []
      pagination.total = res.data.count || 0
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
 * 获取经营类目列表
 */
const fetchShopCategoryList = async () => {
  try {
    const res = await request.get('/web/mall/admin/shop/category/lists', { params: { page_no: 1, page_size: 999 } })
    if (res && res.code === 1 && res.data) {
      shopCategoryList.value = res.data.lists || []
    }
  } catch (e) {
    console.error('获取类目失败', e)
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
  queryParams.cid = ''
  queryParams.contact = ''
  queryParams.mobile = ''
  handleSearch()
}

/**
 * 跟进入驻申请
 * @param {number} id - 申请ID
 */
const handleFollow = async (id) => {
  try {
    await ElMessageBox.confirm('确定要跟进此入驻信息吗？', '提示', { type: 'warning' })
    const res = await request.post('/web/mall/admin/shop/apply/follow', { id })
    if (res && res.code === 1) {
      ElMessage.success('操作成功')
      fetchList()
    } else {
      ElMessage.error(res?.message || '操作失败')
    }
  } catch (e) {
    // 取消操作
  }
}

onMounted(() => {
  fetchList()
  fetchShopCategoryList()
})
</script>
