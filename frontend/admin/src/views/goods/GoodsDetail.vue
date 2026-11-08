<template>
  <div class="goods-detail">
    <div class="page-header">
      <el-button @click="goBack" type="primary" plain>
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
      <h1>{{ isEdit ? '编辑商品' : '商品详情' }}</h1>
    </div>

    <el-card v-loading="loading">
      <el-form 
        ref="goodsFormRef"
        :model="goodsForm" 
        :rules="formRules"
        label-width="120px"
        :disabled="!isEdit"
      >
        <el-tabs v-model="activeTab">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="商品名称" prop="commonName">
                  <el-input v-model="goodsForm.commonName" placeholder="请输入商品名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="商品分类" prop="catId">
                  <el-select v-model="goodsForm.catId" placeholder="请选择分类" style="width: 100%">
                    <el-option 
                      v-for="category in categoryList" 
                      :key="category.catId"
                      :label="category.catName" 
                      :value="category.catId" 
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="商品品牌" prop="brandId">
                  <el-select v-model="goodsForm.brandId" placeholder="请选择品牌" style="width: 100%">
                    <el-option 
                      v-for="brand in brandList" 
                      :key="brand.brandId"
                      :label="brand.brandName" 
                      :value="brand.brandId" 
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="商品状态" prop="commonState">
                  <el-radio-group v-model="goodsForm.commonState">
                    <el-radio :value="1">正常</el-radio>
                    <el-radio :value="0">下架</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="商品广告词" prop="commonPromotionTips">
                  <el-input 
                    v-model="goodsForm.commonPromotionTips" 
                    placeholder="请输入商品广告词"
                    type="textarea"
                    :rows="2"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>

          <!-- 价格库存 -->
          <el-tab-pane label="价格库存" name="price">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="商品价格" prop="commonPrice">
                  <el-input-number 
                    v-model="goodsForm.commonPrice" 
                    :precision="2"
                    :step="0.01"
                    :min="0"
                    style="width: 100%"
                  >
                    <template #prepend>¥</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="市场价" prop="commonMarketPrice">
                  <el-input-number 
                    v-model="goodsForm.commonMarketPrice" 
                    :precision="2"
                    :step="0.01"
                    :min="0"
                    style="width: 100%"
                  >
                    <template #prepend>¥</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="成本价" prop="commonCostPrice">
                  <el-input-number 
                    v-model="goodsForm.commonCostPrice" 
                    :precision="2"
                    :step="0.01"
                    :min="0"
                    style="width: 100%"
                  >
                    <template #prepend>¥</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="商品库存" prop="commonStock">
                  <el-input-number 
                    v-model="goodsForm.commonStock" 
                    :min="0"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="库存预警" prop="commonAlarm">
                  <el-input-number 
                    v-model="goodsForm.commonAlarm" 
                    :min="0"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="商品重量" prop="commonCubage">
                  <el-input-number 
                    v-model="goodsForm.commonCubage" 
                    :precision="2"
                    :step="0.01"
                    :min="0"
                    style="width: 100%"
                  >
                    <template #append>kg</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>

          <!-- 商品图片 -->
          <el-tab-pane label="商品图片" name="images">
            <el-form-item label="商品主图" prop="commonImage">
              <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :before-upload="beforeImageUpload"
                action="#"
                :http-request="handleImageUpload"
              >
                <img v-if="goodsForm.commonImage" :src="goodsForm.commonImage" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">
                只能上传jpg/png文件，且不超过2MB
              </div>
            </el-form-item>
          </el-tab-pane>

          <!-- 其他信息 -->
          <el-tab-pane label="其他信息" name="other">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="是否推荐">
                  <el-switch 
                    v-model="goodsForm.commonIsRecommend" 
                    :active-value="1"
                    :inactive-value="0"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="审核状态" v-if="!isEdit">
                  <el-tag :type="getVerifyStatusType(goodsForm.commonVerify)">
                    {{ getVerifyStatusText(goodsForm.commonVerify) }}
                  </el-tag>
                </el-form-item>
              </el-col>
              <el-col :span="12" v-if="!isEdit">
                <el-form-item label="销售数量">
                  <span>{{ goodsForm.commonSalenum || 0 }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="12" v-if="!isEdit">
                <el-form-item label="收藏数量">
                  <span>{{ goodsForm.commonCollect || 0 }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="12" v-if="!isEdit">
                <el-form-item label="创建时间">
                  <span>{{ goodsForm.commonAddTime }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="12" v-if="!isEdit">
                <el-form-item label="上架时间">
                  <span>{{ goodsForm.commonSellTime }}</span>
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>
        </el-tabs>

        <!-- 操作按钮 -->
        <div class="form-actions" v-if="isEdit">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            保存
          </el-button>
        </div>
        <div class="form-actions" v-else>
          <el-button type="primary" @click="toggleEditMode">编辑</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { goodsApi, categoryApi, brandApi } from '@/api/goods/goods'

// 路由信息
const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const activeTab = ref('basic')
const goodsFormRef = ref()

// 获取商品ID
const goodsId = route.params.id

// 数据列表
const categoryList = ref([])
const brandList = ref([])

// 商品表单数据
const goodsForm = reactive({
  commonId: null,
  commonName: '',
  commonPromotionTips: '',
  catId: null,
  brandId: null,
  commonImage: '',
  commonState: 1,
  commonVerify: 1,
  commonPrice: 0,
  commonMarketPrice: 0,
  commonCostPrice: 0,
  commonStock: 0,
  commonAlarm: 0,
  commonCubage: 0,
  commonIsRecommend: 0,
  commonSalenum: 0,
  commonCollect: 0,
  commonAddTime: '',
  commonSellTime: ''
})

// 表单验证规则
const formRules = {
  commonName: [
    { required: true, message: '请输入商品名称', trigger: 'blur' }
  ],
  catId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  commonPrice: [
    { required: true, message: '请输入商品价格', trigger: 'blur' }
  ],
  commonStock: [
    { required: true, message: '请输入商品库存', trigger: 'blur' }
  ]
}

// 生命周期
onMounted(async () => {
  await Promise.all([
    fetchCategoryList(),
    fetchBrandList()
  ])
  
  if (goodsId && goodsId !== 'create') {
    await fetchGoodsDetail()
  } else {
    isEdit.value = true
  }
})

// 获取分类列表
const fetchCategoryList = async () => {
  try {
    const response = await categoryApi.getCategoryList({})
    if (response.code === 200) {
      categoryList.value = response.data || []
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取品牌列表
const fetchBrandList = async () => {
  try {
    const response = await brandApi.getBrandList({})
    if (response.code === 200) {
      brandList.value = response.data || []
    }
  } catch (error) {
    console.error('获取品牌列表失败:', error)
  }
}

// 获取商品详情
const fetchGoodsDetail = async () => {
  loading.value = true
  try {
    const response = await goodsApi.getGoodsDetail(goodsId)
    if (response.code === 200) {
      Object.assign(goodsForm, response.data)
    } else {
      ElMessage.error(response.message || '获取商品详情失败')
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

// 切换编辑模式
const toggleEditMode = () => {
  isEdit.value = !isEdit.value
}

// 返回列表
const goBack = () => {
  router.push('/list')
}

// 图片上传前验证
const beforeImageUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传头像图片只能是 JPG 或 PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

// 处理图片上传
const handleImageUpload = (param) => {
  // 这里应该调用实际的文件上传接口
  // 临时使用 FileReader 生成 base64
  const reader = new FileReader()
  reader.onload = () => {
    goodsForm.commonImage = reader.result
  }
  reader.readAsDataURL(param.file)
}

// 提交表单
const handleSubmit = async () => {
  if (!goodsFormRef.value) return
  
  try {
    await goodsFormRef.value.validate()
    
    submitting.value = true
    let response
    
    if (goodsForm.commonId) {
      // 编辑商品
      response = await goodsApi.updateGoods(goodsForm)
    } else {
      // 创建商品
      response = await goodsApi.createGoods(goodsForm)
    }
    
    if (response.code === 200) {
      ElMessage.success(goodsForm.commonId ? '更新成功' : '创建成功')
      goBack()
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    if (error.message) {
      console.error('保存失败:', error)
      ElMessage.error('保存失败')
    }
  } finally {
    submitting.value = false
  }
}

// 获取审核状态类型
const getVerifyStatusType = (status) => {
  const typeMap = {
    0: 'danger',
    1: 'success',
    10: 'warning'
  }
  return typeMap[status] || 'info'
}

// 获取审核状态文本
const getVerifyStatusText = (status) => {
  const textMap = {
    0: '审核未通过',
    1: '审核通过',
    10: '审核中'
  }
  return textMap[status] || '未知'
}
</script>

<style scoped>
.goods-detail {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.form-actions {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.form-actions .el-button {
  margin: 0 10px;
  min-width: 100px;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: 0.2s;
  width: 148px;
  height: 148px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.avatar {
  width: 148px;
  height: 148px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #999;
}
</style>
