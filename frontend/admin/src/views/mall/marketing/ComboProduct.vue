<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">组合商品</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增组合
          </el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="组合名称">
          <el-input v-model="searchForm.combinationName" placeholder="请输入组合名称" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="组合类型">
          <el-select v-model="searchForm.combinationType" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="固定组合" :value="1" />
            <el-option label="搭配组合" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动状态">
          <el-select v-model="searchForm.combinationStatus" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
            <el-option label="已失效" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="combinationId" label="组合ID" width="80" />
        <el-table-column prop="combinationName" label="组合名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="组合类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.combinationType === 1 ? 'success' : 'primary'">
              {{ row.combinationType === 1 ? '固定组合' : '搭配组合' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="组合主图" width="80">
          <template #default="{ row }">
            <el-image
              v-if="row.combinationImage"
              :src="row.combinationImage"
              :preview-src-list="[row.combinationImage]"
              fit="cover"
              style="width: 50px; height: 50px;"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="combinationPrice" label="组合价(元)" width="110">
          <template #default="{ row }">
            <span class="price">¥{{ row.combinationPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="combinationOriginalPrice" label="原价(元)" width="100">
          <template #default="{ row }">
            <span class="original-price">¥{{ row.combinationOriginalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="活动时间" width="180">
          <template #default="{ row }">
            <div v-if="row.startTime && row.endTime">
              <div>{{ formatDate(row.startTime) }}</div>
              <div>{{ formatDate(row.endTime) }}</div>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="活动状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.combinationStatus)">
              {{ getStatusText(row.combinationStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleView(row)">
              <el-icon><View /></el-icon> 查看
            </el-button>
            <el-button text type="primary" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button
              v-if="row.combinationStatus === 0 || row.combinationStatus === 3"
              text
              type="success"
              size="small"
              @click="handleEnable(row)"
            >
              <el-icon><VideoPlay /></el-icon> 启用
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        class="pagination"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组合名称" prop="combinationName">
              <el-input v-model="form.combinationName" placeholder="请输入组合名称" maxlength="50" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组合类型" prop="combinationType">
              <el-radio-group v-model="form.combinationType">
                <el-radio :label="1">固定组合</el-radio>
                <el-radio :label="2">搭配组合</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组合价格" prop="combinationPrice">
              <el-input-number
                v-model="form.combinationPrice"
                :min="0.01"
                :precision="2"
                :step="0.01"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原价" prop="combinationOriginalPrice">
              <el-input-number
                v-model="form.combinationOriginalPrice"
                :min="0.01"
                :precision="2"
                :step="0.01"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="组合主图" prop="combinationImage">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="handleImageUpload"
          >
            <img v-if="form.combinationImage" :src="form.combinationImage" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="tips">建议尺寸：800*800px，支持JPG、PNG格式，大小不超过2MB</div>
        </el-form-item>

        <el-form-item label="组合商品" prop="goodsList">
          <el-button @click="handleSelectGoods">
            <el-icon><Plus /></el-icon> 添加商品
          </el-button>
          <el-table :data="form.goodsList" border class="goods-table">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="goodsName" label="商品名称" min-width="150" />
            <el-table-column prop="goodsPrice" label="商品价格(元)" width="120">
              <template #default="{ row }">
                ¥{{ row.goodsPrice }}
              </template>
            </el-table-column>
            <el-table-column label="是否主商品" width="100">
              <template #default="{ row }">
                <el-switch v-model="row.isMainProduct" :active-value="1" :inactive-value="0" />
              </template>
            </el-table-column>
            <el-table-column label="数量" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.goodsNum" :min="1" size="small" />
              </template>
            </el-table-column>
            <el-table-column v-if="form.combinationType === 2" label="搭配价格(元)" width="140">
              <template #default="{ row }">
                <el-input-number v-model="row.matchingPrices" :min="0.01" :precision="2" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="scope">
                <el-button text type="danger" size="small" @click="handleRemoveGoods(scope.$index)">
                  <el-icon><Delete /></el-icon> 移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 商品选择对话框 -->
    <el-dialog v-model="goodsDialogVisible" title="选择商品" width="800px">
      <el-table :data="goodsTableData" @selection-change="handleGoodsSelection" border>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="goodsId" label="商品ID" width="80" />
        <el-table-column prop="goodsName" label="商品名称" min-width="200" />
        <el-table-column prop="goodsPrice" label="价格(元)" width="100">
          <template #default="{ row }">
            ¥{{ row.goodsPrice }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="goodsDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSelectGoods">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, View, VideoPlay } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const goodsDialogVisible = ref(false)
const dialogTitle = ref('新增组合')
const formRef = ref(null)

const searchForm = reactive({
  combinationName: '',
  combinationType: null,
  combinationStatus: null
})

const form = reactive({
  combinationId: null,
  combinationName: '',
  combinationType: 1,
  combinationPrice: 0,
  combinationOriginalPrice: 0,
  startTime: '',
  endTime: '',
  combinationImage: '',
  goodsList: []
})

const rules = {
  combinationName: [{ required: true, message: '请输入组合名称', trigger: 'blur' }],
  combinationType: [{ required: true, message: '请选择组合类型', trigger: 'change' }],
  combinationPrice: [{ required: true, message: '请输入组合价格', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const goodsTableData = ref([
  { goodsId: 1, goodsName: 'iPhone 15 Pro Max', goodsPrice: 9999 },
  { goodsId: 2, goodsName: 'MacBook Pro 14', goodsPrice: 12999 },
  { goodsId: 3, goodsName: 'AirPods Pro 2', goodsPrice: 1899 }
])
const selectedGoods = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    const res = await request.post('/web/activity/combination/query', params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  Object.assign(searchForm, { combinationName: '', combinationType: null, combinationStatus: null })
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增组合'
  Object.assign(form, {
    combinationId: null,
    combinationName: '',
    combinationType: 1,
    combinationPrice: 0,
    combinationOriginalPrice: 0,
    startTime: '',
    endTime: '',
    combinationImage: '',
    goodsList: []
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑组合'
  Object.assign(form, { ...row, goodsList: row.goodsList || [] })
  dialogVisible.value = true
}

const handleView = (row) => {
  ElMessage.info('查看组合详情：' + row.combinationName)
}

const handleEnable = async (row) => {
  try {
    await ElMessageBox.confirm('确认启用该组合活动？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    const res = await request.post('/web/activity/combination/enable', { combinationId: row.combinationId })
    if (res.code === 200) {
      ElMessage.success('启用成功')
      fetchData()
    }
  } catch (error) {
    console.log('取消启用')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该组合活动？删除后不可恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await request.post('/web/activity/combination/delete', { combinationId: row.combinationId })
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    }
  } catch (error) {
    console.log('取消删除')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const url = form.combinationId ? '/web/activity/combination/update' : '/web/activity/combination/add'
        const res = await request.post(url, form)
        if (res.code === 200) {
          ElMessage.success('保存成功')
          dialogVisible.value = false
          fetchData()
        }
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleImageUpload = (file) => {
  // 这里应该调用文件上传接口，暂时模拟
  const reader = new FileReader()
  reader.onload = (e) => {
    form.combinationImage = e.target.result
  }
  reader.readAsDataURL(file)
  return false
}

const handleSelectGoods = () => {
  goodsDialogVisible.value = true
}

const handleGoodsSelection = (selection) => {
  selectedGoods.value = selection
}

const confirmSelectGoods = () => {
  selectedGoods.value.forEach((goods) => {
    if (!form.goodsList.find((g) => g.goodsId === goods.goodsId)) {
      form.goodsList.push({
        goodsId: goods.goodsId,
        goodsName: goods.goodsName,
        goodsPrice: goods.goodsPrice,
        goodsNum: 1,
        isMainProduct: 0,
        matchingPrices: goods.goodsPrice
      })
    }
  })
  goodsDialogVisible.value = false
}

const handleRemoveGoods = (index) => {
  form.goodsList.splice(index, 1)
}

const formatDate = (date) => {
  if (!date) return '-'
  return date.replace('T', ' ').substring(0, 16)
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'danger', 3: 'warning' }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = { 0: '未开始', 1: '进行中', 2: '已结束', 3: '已失效' }
  return map[status] || '-'
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: bold; font-size: 16px; }
.search-form { margin-bottom: 20px; }
.pagination { margin-top: 20px; justify-content: flex-end; }
.price { color: #f56c6c; font-weight: bold; }
.original-price { color: #909399; text-decoration: line-through; }
.avatar-uploader .avatar { width: 178px; height: 178px; display: block; }
.avatar-uploader .el-upload { border: 1px dashed var(--el-border-color); border-radius: 6px; cursor: pointer; position: relative; overflow: hidden; transition: var(--el-transition-duration-fast); }
.avatar-uploader .el-upload:hover { border-color: var(--el-color-primary); }
.el-icon.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 178px; height: 178px; text-align: center; line-height: 178px; }
.tips { color: #909399; font-size: 12px; margin-top: 5px; }
.goods-table { margin-top: 10px; }
</style>
