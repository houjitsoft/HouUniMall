<template>
  <div class="goods-detail-page">
    <!-- 顶部返回 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <el-page-header content="商品详情" @back="$router.back()" />
    </el-card>

    <el-card shadow="never" v-loading="loading">
      <el-tabs v-model="activeTab">
        <!-- 基础信息 -->
        <el-tab-pane label="基础设置" name="base">
          <el-descriptions :column="2" border style="margin-top:16px">
            <el-descriptions-item label="商品名称">{{ detail.name || '-' }}</el-descriptions-item>
            <el-descriptions-item label="商品货号">{{ detail.code || '-' }}</el-descriptions-item>
            <el-descriptions-item label="商家名称">{{ detail.shopName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="规格类型">{{ detail.skuType === 1 ? '多规格' : '单规格' }}</el-descriptions-item>
            <el-descriptions-item label="排序">{{ detail.sort }}</el-descriptions-item>
            <el-descriptions-item label="发布时间">{{ detail.createTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="商品主图" :span="2">
              <el-image
                v-if="detail.image"
                :src="detail.image"
                fit="cover"
                style="width:100px;height:100px"
                :preview-src-list="[detail.image]"
              />
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="商品描述" :span="2">
              {{ detail.descript || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <!-- 价格/库存 -->
        <el-tab-pane label="价格/库存" name="price">
          <el-table :data="detail.skuList || []" border style="margin-top:16px">
            <el-table-column label="规格" prop="skuValueStr" min-width="160" />
            <el-table-column label="价格" prop="price" min-width="100">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
            <el-table-column label="划线价" prop="linePrice" min-width="100">
              <template #default="{ row }">¥{{ row.linePrice }}</template>
            </el-table-column>
            <el-table-column label="库存" prop="stock" min-width="80" />
            <el-table-column label="商品编码" prop="code" min-width="120" />
          </el-table>

          <el-descriptions :column="2" border style="margin-top:16px">
            <el-descriptions-item label="总库存">{{ detail.totalStock }}</el-descriptions-item>
            <el-descriptions-item label="总销量">{{ detail.allSaleNum }}</el-descriptions-item>
            <el-descriptions-item label="虚拟销量">{{ detail.virtualSaleNum }}</el-descriptions-item>
            <el-descriptions-item label="虚拟浏览量">{{ detail.virtualVisitNum }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <!-- 审核信息 -->
        <el-tab-pane label="审核信息" name="audit">
          <el-descriptions :column="2" border style="margin-top:16px">
            <el-descriptions-item label="审核状态">
              <el-tag :type="auditTagType(detail.auditStatus)">{{ detail.auditStatusText || '-' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="销售状态">
              <el-tag :type="detail.status === 1 ? 'success' : 'info'">{{ detail.statusText || '-' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="审核说明" :span="2">
              {{ detail.auditContent || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <div style="margin-top:16px">
      <el-button @click="$router.back()">返回</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { mallGoodsDetail } from '@/api/mall/goodsAdmin.js'

const route = useRoute()
const loading = ref(false)
const activeTab = ref('base')

const detail = reactive({})

/**
 * 获取商品详情
 */
const fetchDetail = async () => {
  const id = route.query.id
  if (!id) return
  loading.value = true
  try {
    const res = await mallGoodsDetail(id)
    if (res && res.code === 1 && res.data) {
      const data = res.data.goods || res.data
      Object.assign(detail, data)
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

/**
 * 获取审核状态对应的 Tag 类型
 * @param {number} status - 审核状态
 * @returns {string} - Element Plus Tag 类型
 */
const auditTagType = (status) => {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.goods-detail-page { padding: 20px; }
</style>
