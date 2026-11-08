<template>
  <div class="quick-page">
    <!-- 快捷功能区 -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="action in quickActions" :key="action.id">
        <el-card shadow="hover" class="quick-card" @click="handleAction(action)">
          <div class="quick-content">
            <div class="quick-icon" :style="{ background: action.color }">
              <el-icon :size="32"><component :is="action.icon" /></el-icon>
            </div>
            <div class="quick-info">
              <div class="quick-title">{{ action.title }}</div>
              <div class="quick-desc">{{ action.desc }}</div>
            </div>
            <div class="quick-badge" v-if="action.badge">
              <el-badge :value="action.badge" :max="99" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分类快捷操作 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <div class="section-header">
              <span class="section-title">订单管理</span>
            </div>
          </template>
          <div class="action-list">
            <div v-for="item in orderActions" :key="item.id" class="action-item" @click="handleAction(item)">
              <div class="action-icon" :style="{ background: item.color }">
                <el-icon><component :is="item.icon" /></el-icon>
              </div>
              <div class="action-info">
                <div class="action-name">{{ item.title }}</div>
                <div class="action-count" v-if="item.count">{{ item.count }}</div>
              </div>
              <el-icon class="action-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <div class="section-header">
              <span class="section-title">商品管理</span>
            </div>
          </template>
          <div class="action-list">
            <div v-for="item in goodsActions" :key="item.id" class="action-item" @click="handleAction(item)">
              <div class="action-icon" :style="{ background: item.color }">
                <el-icon><component :is="item.icon" /></el-icon>
              </div>
              <div class="action-info">
                <div class="action-name">{{ item.title }}</div>
                <div class="action-count" v-if="item.count">{{ item.count }}</div>
              </div>
              <el-icon class="action-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <div class="section-header">
              <span class="section-title">营销活动</span>
            </div>
          </template>
          <div class="action-list">
            <div v-for="item in marketingActions" :key="item.id" class="action-item" @click="handleAction(item)">
              <div class="action-icon" :style="{ background: item.color }">
                <el-icon><component :is="item.icon" /></el-icon>
              </div>
              <div class="action-info">
                <div class="action-name">{{ item.title }}</div>
                <div class="action-count" v-if="item.count">{{ item.count }}</div>
              </div>
              <el-icon class="action-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <div class="section-header">
              <span class="section-title">系统功能</span>
            </div>
          </template>
          <div class="action-list">
            <div v-for="item in systemActions" :key="item.id" class="action-item" @click="handleAction(item)">
              <div class="action-icon" :style="{ background: item.color }">
                <el-icon><component :is="item.icon" /></el-icon>
              </div>
              <div class="action-info">
                <div class="action-name">{{ item.title }}</div>
                <div class="action-count" v-if="item.count">{{ item.count }}</div>
              </div>
              <el-icon class="action-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Plus, Document, ShoppingCart, DataAnalysis, User, Shop,
  Tickets, Promotion, Setting, ArrowRight, Goods, Files,
  WarnTriangleFilled, PieChart
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

const quickActions = ref([
  { 
    id: 1, 
    title: '新增商品', 
    desc: '快速上架商品', 
    icon: Plus, 
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    path: '/mall/goods/list'
  },
  { 
    id: 2, 
    title: '订单查询', 
    desc: '查看所有订单', 
    icon: Document, 
    color: 'linear-gradient(135deg, #5ad8a6 0%, #3ab881 100%)',
    path: '/mall/order/list'
  },
  { 
    id: 3, 
    title: '待审核', 
    desc: '商品待审核', 
    icon: WarnTriangleFilled, 
    color: 'linear-gradient(135deg, #ff9845 0%, #ff7320 100%)',
    path: '/mall/goods/audit',
    badge: 125
  },
  { 
    id: 4, 
    title: '数据报表', 
    desc: '查看营业数据', 
    icon: PieChart, 
    color: 'linear-gradient(135deg, #5b8ff9 0%, #3a6fcc 100%)',
    path: '/mall/statistics/sales'
  },
  { 
    id: 5, 
    title: '用户管理', 
    desc: '管理客户信息', 
    icon: User, 
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    path: '/mall/customer/list'
  },
  { 
    id: 6, 
    title: '店铺管理', 
    desc: '管理店铺信息', 
    icon: Shop, 
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    path: '/mall/store/list'
  },
  { 
    id: 7, 
    title: '优惠券', 
    desc: '创建优惠券', 
    icon: Tickets, 
    color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    path: '/mall/marketing/coupon'
  },
  { 
    id: 8, 
    title: '系统设置', 
    desc: '配置系统参数', 
    icon: Setting, 
    color: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
    path: '/mall/setting/basic'
  }
])

const orderActions = ref([
  { id: 1, title: '待处理订单', icon: Document, color: '#ff9845', count: 23, path: '/mall/order/list' },
  { id: 2, title: '待发货订单', icon: ShoppingCart, color: '#5ad8a6', count: 15, path: '/mall/order/list' },
  { id: 3, title: '退款申请', icon: Files, color: '#5b8ff9', count: 5, path: '/mall/order/refund-manage' },
  { id: 4, title: '维权订单', icon: WarnTriangleFilled, color: '#f56c6c', count: 2, path: '/mall/order/rights' }
])

const goodsActions = ref([
  { id: 1, title: '待审核商品', icon: Goods, color: '#ff9845', count: 125, path: '/mall/goods/audit' },
  { id: 2, title: '库存预警', icon: WarnTriangleFilled, color: '#f56c6c', count: 18, path: '/mall/inventory/alert' },
  { id: 3, title: '商品分类', icon: Files, color: '#5ad8a6', path: '/mall/goods/category' },
  { id: 4, title: '商品统计', icon: DataAnalysis, color: '#5b8ff9', path: '/mall/goods/statistics' }
])

const marketingActions = ref([
  { id: 1, title: '优惠券管理', icon: Tickets, color: '#ff9845', path: '/mall/marketing/coupon' },
  { id: 2, title: '促销活动', icon: Promotion, color: '#5ad8a6', path: '/mall/marketing/promotion' },
  { id: 3, title: '积分管理', icon: PieChart, color: '#5b8ff9', path: '/mall/marketing/points' },
  { id: 4, title: '会员等级', icon: User, color: '#667eea', path: '/mall/marketing/member' }
])

const systemActions = ref([
  { id: 1, title: '设置中心', icon: Setting, color: '#5b8ff9', path: '/mall/setting/basic' },
  { id: 2, title: '支付设置', icon: WarnTriangleFilled, color: '#5ad8a6', path: '/mall/setting/payment' },
  { id: 3, title: '物流设置', icon: ShoppingCart, color: '#ff9845', path: '/mall/setting/logistics' },
  { id: 4, title: '通知设置', icon: Document, color: '#667eea', path: '/mall/setting/notification' }
])

const handleAction = (action) => {
  if (action.path) {
    router.push(action.path)
  } else {
    ElMessage.info(`打开: ${action.title}`)
  }
}
</script>

<style scoped>
.quick-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.quick-card {
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
}

.quick-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.quick-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  position: relative;
}

.quick-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.quick-info {
  flex: 1;
}

.quick-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.quick-desc {
  font-size: 13px;
  color: #999;
}

.quick-badge {
  position: absolute;
  top: 0;
  right: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-weight: 600;
  color: #333;
  font-size: 16px;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-item:hover {
  background: #e6f0ff;
  transform: translateX(4px);
}

.action-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.action-info {
  flex: 1;
}

.action-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.action-count {
  font-size: 12px;
  color: #f56c6c;
  font-weight: 600;
  margin-top: 2px;
}

.action-arrow {
  color: #999;
  font-size: 16px;
}

@media (max-width: 768px) {
  .quick-page {
    padding: 10px;
  }
  
  .quick-card {
    margin-bottom: 10px;
  }
}
</style>
