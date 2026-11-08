<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">会员等级</span>
          <el-button type="primary" @click="handleAdd">新增等级</el-button>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="等级ID" width="80" />
        <el-table-column prop="name" label="等级名称" min-width="120" />
        <el-table-column prop="level" label="等级" width="80" sortable />
        <el-table-column label="等级图标" width="100">
          <template #default="{ row }">
            <el-avatar :size="40" :style="{ background: row.color }">
              {{ row.name.substring(0, 2) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="minPoints" label="所需积分" width="120">
          <template #default="{ row }">
            {{ row.minPoints }} - {{ row.maxPoints > 0 ? row.maxPoints : '∞' }}
          </template>
        </el-table-column>
        <el-table-column prop="discount" label="会员折扣" width="100">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.discount }}折</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="会员权益" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="(benefit, index) in row.benefits" :key="index" class="benefit-tag">
              {{ benefit }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="memberCount" label="会员数" width="100" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button text type="primary" size="small" @click="handleViewMembers(row)">查看会员</el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="等级名称">
          <el-input v-model="form.name" placeholder="请输入等级名称" />
        </el-form-item>
        <el-form-item label="等级">
          <el-input-number v-model="form.level" :min="1" />
        </el-form-item>
        <el-form-item label="等级颜色">
          <el-color-picker v-model="form.color" />
        </el-form-item>
        <el-form-item label="所需积分范围">
          <el-input-number v-model="form.minPoints" :min="0" placeholder="最小积分" />
          <span style="margin: 0 10px;">-</span>
          <el-input-number v-model="form.maxPoints" :min="0" placeholder="最大积分（0表示无上限）" />
        </el-form-item>
        <el-form-item label="会员折扣">
          <el-input-number v-model="form.discount" :min="1" :max="10" :step="0.1" />
          <span style="margin-left: 10px;">折</span>
        </el-form-item>
        <el-form-item label="会员权益">
          <el-select v-model="form.benefits" multiple placeholder="请选择权益">
            <el-option label="免运费" value="freeShipping" />
            <el-option label="专属客服" value="vipService" />
            <el-option label="生日礼包" value="birthdayGift" />
            <el-option label="积分翻倍" value="doublePoints" />
            <el-option label="优先发货" value="priorityShip" />
            <el-option label="专属折扣" value="specialDiscount" />
          </el-select>
        </el-form-item>
        <el-form-item label="等级说明">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增等级')

const form = reactive({
  id: null,
  name: '',
  level: 1,
  color: '#409EFF',
  minPoints: 0,
  maxPoints: 0,
  discount: 10,
  benefits: [],
  description: ''
})

const tableData = ref([
  { 
    id: 1, 
    name: '普通会员', 
    level: 1, 
    color: '#909399',
    minPoints: 0, 
    maxPoints: 999, 
    discount: 10, 
    benefits: ['免运费'],
    memberCount: 1256
  },
  { 
    id: 2, 
    name: 'VIP会员', 
    level: 2, 
    color: '#67c23a',
    minPoints: 1000, 
    maxPoints: 4999, 
    discount: 9.5, 
    benefits: ['免运费', '专属客服', '生日礼包'],
    memberCount: 856
  },
  { 
    id: 3, 
    name: 'SVIP会员', 
    level: 3, 
    color: '#e6a23c',
    minPoints: 5000, 
    maxPoints: 9999, 
    discount: 9, 
    benefits: ['免运费', '专属客服', '生日礼包', '积分翻倍'],
    memberCount: 345
  },
  { 
    id: 4, 
    name: '钻石会员', 
    level: 4, 
    color: '#f56c6c',
    minPoints: 10000, 
    maxPoints: 0, 
    discount: 8.5, 
    benefits: ['免运费', '专属客服', '生日礼包', '积分翻倍', '优先发货', '专属折扣'],
    memberCount: 128
  }
])

const fetchData = async () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

const handleAdd = () => {
  dialogTitle.value = '新增等级'
  Object.assign(form, { 
    id: null, 
    name: '', 
    level: 1, 
    color: '#409EFF',
    minPoints: 0, 
    maxPoints: 0, 
    discount: 10, 
    benefits: [],
    description: '' 
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑等级'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleViewMembers = (row) => {
  ElMessage.info('查看会员列表：' + row.name)
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该等级？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleSubmit = () => {
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: bold; font-size: 16px; }
.benefit-tag { margin-right: 8px; margin-bottom: 5px; }
</style>
