<template>
  <div class="goods-list-container">
    <!-- 提示说明 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <el-alert type="warning" :closable="false" show-icon>
        <div>1、平台审核商家发布的商品，当商品审核通过并且处于销售中状态时，商家可以销售该商品。</div>
        <div>2、平台可通过"违规重审"，下架违规商品并标记为审核未通过。</div>
      </el-alert>

      <!-- 搜索栏 -->
      <el-form :model="searchForm" inline style="margin-top:16px;margin-bottom:-16px">
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入名称"
            clearable
            style="width:280px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="商家名称">
          <el-input
            v-model="searchForm.shopName"
            placeholder="请输入名称"
            clearable
            style="width:280px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="平台分类">
          <el-cascader
            v-model="searchForm.categoryId"
            :options="categoryTree"
            :props="cascaderProps"
            clearable
            filterable
            style="width:280px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="never" :body-style="{ 'padding-top': '10px' }">
      <!-- Tab 含数量 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane
          v-for="tab in tabsConfig"
          :key="tab.name"
          :label="`${tab.label}(${tabCount[tab.countKey] || 0})`"
          :name="tab.name"
        />
      </el-tabs>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="goodsList" size="large" style="margin-top:16px">
        <el-table-column label="商品货号" prop="code" width="140" show-overflow-tooltip />
        <el-table-column label="商品信息" min-width="280">
          <template #default="{ row }">
            <div style="display:flex;align-items:flex-start;gap:12px">
              <el-image
                :src="row.image"
                fit="cover"
                style="width:58px;height:58px;flex-shrink:0"
              />
              <div style="flex:1;overflow:hidden">
                <el-tooltip effect="dark" :content="row.name" placement="top">
                  <div style="font-size:14px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;margin-bottom:4px">
                    {{ row.name }}
                  </div>
                </el-tooltip>
                <el-tag v-if="row.skuType" size="small" style="margin-right:6px">多规格</el-tag>
                <el-tag v-if="row.ms" size="small" type="danger" style="margin-right:6px">秒杀</el-tag>
                <el-tag v-if="row.pt" size="small" type="success" style="margin-right:6px">拼团</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="商家名称" prop="shopName" min-width="120" />
        <el-table-column label="价格" min-width="120">
          <template #default="{ row }">
            <span style="color:#f56c6c;font-weight:bold">¥{{ row.priceText || row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存" prop="totalStock" min-width="80" />
        <el-table-column label="销量" prop="allSaleNum" min-width="80" />
        <el-table-column label="排序" prop="sort" min-width="80" />
        <el-table-column label="审核说明" min-width="140">
          <template #default="{ row }">
            {{ row.auditContent || row.audit_content || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="发布日期" prop="createTime" min-width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary" link
              @click="handleViewDetail(row)"
            >查看</el-button>

            <el-button
              type="primary" link
              @click="handleSet(row)"
            >设置</el-button>

            <el-button
              v-if="row.canAudit"
              type="primary" link
              @click="handleAudit(false, row)"
            >审核</el-button>

            <el-button
              v-if="row.canReAudit"
              type="primary" link
              @click="handleAudit(true, row)"
            >违规重审</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display:flex;justify-content:flex-end;margin-top:16px">
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

    <!-- 商品设置弹窗 -->
    <goods-set-popup
      v-if="showSet"
      ref="setRef"
      @close="showSet = false"
      @success="fetchList"
    />

    <!-- 审核弹窗 -->
    <goods-audit-popup
      v-if="showAudit"
      ref="auditRef"
      @close="showAudit = false"
      @success="fetchList"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import {
  mallGoodsList,
  mallGoodsGoodsSet,
  mallGoodsAudit,
  mallGoodsReAudit,
  mallCategoryTree
} from '@/api/mall/goodsAdmin.js'
import GoodsSetPopup from './GoodsSetPopup.vue'
import GoodsAuditPopup from './GoodsAuditPopup.vue'

const router = useRouter()

// Tab 配置
const tabsConfig = [
  { label: '销售中商品', name: 'sale', countKey: 'count_sale' },
  { label: '仓库中商品', name: 'not_sale', countKey: 'count_not_sale' },
  { label: '待审核商品', name: 'audit', countKey: 'count_audit' },
  { label: '审核未通过商品', name: 'audit_refuse', countKey: 'count_audit_refuse' }
]

const activeTab = ref('sale')
const loading = ref(false)
const goodsList = ref([])
const tabCount = ref({})
const categoryTree = ref([])

const cascaderProps = reactive({
  multiple: false,
  checkStrictly: true,
  label: 'name',
  value: 'id',
  children: 'sons',
  emitPath: false
})

const searchForm = reactive({ name: '', shopName: '', categoryId: '' })
const pagination = reactive({ pageNo: 1, pageSize: 15, total: 0 })

/**
 * 获取商品列表
 */
const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize,
      type: activeTab.value,
      name: searchForm.name || undefined,
      shopName: searchForm.shopName || undefined,
      categoryId: searchForm.categoryId || undefined
    }
    const res = await mallGoodsList(params)
    if (res && res.code === 1 && res.data) {
      goodsList.value = res.data.lists || []
      pagination.total = res.data.count || 0
      // 更新各 tab 数量
      if (res.data.extend) {
        tabCount.value = res.data.extend
      }
    } else {
      goodsList.value = []
      pagination.total = 0
    }
  } catch (e) {
    console.error(e)
    goodsList.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 获取分类树（用于级联选择器）
 */
const fetchCategoryTree = async () => {
  try {
    const res = await mallCategoryTree()
    if (res && res.code === 1) {
      categoryTree.value = res.data || []
    }
  } catch (e) { /* ignore */ }
}

const handleSearch = () => { pagination.pageNo = 1; fetchList() }
const handleReset = () => {
  searchForm.name = ''
  searchForm.shopName = ''
  searchForm.categoryId = ''
  pagination.pageNo = 1
  fetchList()
}
const handleTabChange = () => { pagination.pageNo = 1; fetchList() }

/* ============== 查看详情 ============== */
const handleViewDetail = (row) => {
  router.push({ path: '/mall/goods/detail', query: { id: row.id } })
}

/* ============== 设置弹窗 ============== */
const showSet = ref(false)
const setRef = ref(null)

const handleSet = async (row) => {
  showSet.value = true
  await nextTick()
  setRef.value?.open({
    id: row.id,
    virtualSaleNum: row.virtualSaleNum ?? row.virtual_sale_num ?? 0,
    visitNum: row.visitNum ?? row.visit_num ?? 0,
    sort: row.sort ?? 0,
    virtualVisitNum: row.virtualVisitNum ?? row.virtual_visit_num ?? 0
  })
}

/* ============== 审核弹窗 ============== */
const showAudit = ref(false)
const auditRef = ref(null)

const handleAudit = async (isReAudit, row) => {
  showAudit.value = true
  await nextTick()
  auditRef.value?.open(isReAudit, {
    id: row.id,
    auditContent: row.auditContent ?? row.audit_content ?? ''
  })
}

onMounted(() => {
  fetchCategoryTree()
  fetchList()
})
</script>

<style scoped>
.goods-list-container { padding: 20px; }
</style>
