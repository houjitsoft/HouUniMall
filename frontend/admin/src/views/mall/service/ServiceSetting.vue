<template>
  <div class="page-container">
    <!-- 售后政策配置 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <span class="card-title">售后政策配置</span>
      </template>
      <el-form :model="policyForm" label-width="140px" style="max-width: 800px">
        <el-form-item label="退货期限">
          <el-input-number
            v-model="policyForm.returnDays"
            :min="0"
            :max="30"
            style="width: 150px"
          />
          <span class="form-tip">天（0表示不支持退货）</span>
        </el-form-item>
        <el-form-item label="换货期限">
          <el-input-number
            v-model="policyForm.exchangeDays"
            :min="0"
            :max="30"
            style="width: 150px"
          />
          <span class="form-tip">天（0表示不支持换货）</span>
        </el-form-item>
        <el-form-item label="退款期限">
          <el-input-number
            v-model="policyForm.refundDays"
            :min="1"
            :max="15"
            style="width: 150px"
          />
          <span class="form-tip">天（审核通过后退款时间）</span>
        </el-form-item>
        <el-form-item label="自动审核">
          <el-switch v-model="policyForm.autoApprove" />
          <span class="form-tip">开启后售后申请将自动审核通过</span>
        </el-form-item>
        <el-form-item label="需要审核凭证">
          <el-switch v-model="policyForm.requireProof" />
          <span class="form-tip">要求用户上传退换货凭证</span>
        </el-form-item>
        <el-form-item label="退货地址" required>
          <el-input
            v-model="policyForm.returnAddress"
            type="textarea"
            :rows="2"
            placeholder="请输入退货收货地址"
          />
        </el-form-item>
        <el-form-item label="联系人" required>
          <el-input v-model="policyForm.contactName" placeholder="请输入联系人姓名" style="width: 200px" />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="policyForm.contactPhone" placeholder="请输入联系电话" style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSavePolicy">
            <el-icon><Select /></el-icon>保存配置
          </el-button>
          <el-button @click="handleResetPolicy">
            <el-icon><RefreshRight /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 售后原因配置 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">售后原因配置</span>
          <el-button type="primary" @click="handleAddReason">
            <el-icon><Plus /></el-icon>添加原因
          </el-button>
        </div>
      </template>
      <el-table :data="reasonList" border stripe>
        <el-table-column prop="type" label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getReasonTypeTag(row.type)">
              {{ getReasonTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="200" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="handleToggleReason(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEditReason(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDeleteReason(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 服务评价配置 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <span class="card-title">服务评价配置</span>
      </template>
      <el-form :model="reviewForm" label-width="140px" style="max-width: 800px">
        <el-form-item label="启用评价">
          <el-switch v-model="reviewForm.enabled" />
          <span class="form-tip">开启后用户可对售后服务进行评价</span>
        </el-form-item>
        <el-form-item label="匿名评价">
          <el-switch v-model="reviewForm.anonymous" />
          <span class="form-tip">允许用户匿名评价</span>
        </el-form-item>
        <el-form-item label="评价奖励">
          <el-input-number
            v-model="reviewForm.rewardPoints"
            :min="0"
            :max="100"
            style="width: 150px"
          />
          <span class="form-tip">积分（评价后赠送积分）</span>
        </el-form-item>
        <el-form-item label="评价标签">
          <el-tag
            v-for="tag in reviewForm.tags"
            :key="tag"
            closable
            @close="handleRemoveTag(tag)"
            style="margin-right: 10px"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="tagInputVisible"
            ref="tagInputRef"
            v-model="tagInputValue"
            size="small"
            style="width: 100px"
            @keyup.enter="handleTagConfirm"
            @blur="handleTagConfirm"
          />
          <el-button v-else size="small" @click="showTagInput">
            <el-icon><Plus /></el-icon>添加标签
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSaveReview">
            <el-icon><Select /></el-icon>保存配置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 通知设置 -->
    <el-card shadow="never">
      <template #header>
        <span class="card-title">通知设置</span>
      </template>
      <el-form :model="notifyForm" label-width="140px" style="max-width: 800px">
        <el-form-item label="用户通知">
          <el-checkbox-group v-model="notifyForm.userNotify">
            <el-checkbox value="sms">短信通知</el-checkbox>
            <el-checkbox value="email">邮件通知</el-checkbox>
            <el-checkbox value="system">站内通知</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="商家通知">
          <el-checkbox-group v-model="notifyForm.merchantNotify">
            <el-checkbox value="sms">短信通知</el-checkbox>
            <el-checkbox value="email">邮件通知</el-checkbox>
            <el-checkbox value="system">站内通知</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="通知时机">
          <el-checkbox-group v-model="notifyForm.notifyTiming">
            <el-checkbox value="apply">用户申请时</el-checkbox>
            <el-checkbox value="approve">审核通过时</el-checkbox>
            <el-checkbox value="reject">审核拒绝时</el-checkbox>
            <el-checkbox value="complete">服务完成时</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSaveNotify">
            <el-icon><Select /></el-icon>保存配置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 添加/编辑原因对话框 -->
    <el-dialog
      v-model="reasonDialogVisible"
      :title="reasonDialogTitle"
      width="500px"
    >
      <el-form :model="reasonForm" label-width="80px">
        <el-form-item label="类型" required>
          <el-select v-model="reasonForm.type" placeholder="请选择">
            <el-option label="退货" value="return" />
            <el-option label="换货" value="exchange" />
            <el-option label="维修" value="repair" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因" required>
          <el-input v-model="reasonForm.reason" placeholder="请输入售后原因" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="reasonForm.sort" :min="0" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="reasonForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reasonDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveReason">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Select, RefreshRight, Plus, Edit, Delete
} from '@element-plus/icons-vue'

// 售后政策表单
const policyForm = reactive({
  returnDays: 7,
  exchangeDays: 15,
  refundDays: 7,
  autoApprove: false,
  requireProof: true,
  returnAddress: '广东省深圳市南山区科技园南区某某大厦101',
  contactName: '售后客服',
  contactPhone: '400-123-4567'
})

// 售后原因列表
const reasonList = ref([
  { id: 1, type: 'return', reason: '商品质量问题', enabled: true, sort: 1 },
  { id: 2, type: 'return', reason: '商品描述不符', enabled: true, sort: 2 },
  { id: 3, type: 'return', reason: '不想要了', enabled: true, sort: 3 },
  { id: 4, type: 'exchange', reason: '尺码不合适', enabled: true, sort: 1 },
  { id: 5, type: 'exchange', reason: '颜色不喜欢', enabled: true, sort: 2 },
  { id: 6, type: 'repair', reason: '商品损坏', enabled: true, sort: 1 }
])

// 售后原因对话框
const reasonDialogVisible = ref(false)
const reasonDialogTitle = ref('添加原因')
const reasonForm = reactive({
  id: null,
  type: 'return',
  reason: '',
  sort: 0,
  enabled: true
})

// 评价配置表单
const reviewForm = reactive({
  enabled: true,
  anonymous: true,
  rewardPoints: 10,
  tags: ['服务态度好', '处理及时', '解决问题专业', '响应速度快', '沟通顺畅']
})

// 标签输入
const tagInputVisible = ref(false)
const tagInputValue = ref('')
const tagInputRef = ref(null)

// 通知设置表单
const notifyForm = reactive({
  userNotify: ['sms', 'system'],
  merchantNotify: ['system'],
  notifyTiming: ['apply', 'approve', 'complete']
})

// 保存售后政策
const handleSavePolicy = () => {
  ElMessage.success('售后政策已保存')
}

// 重置售后政策
const handleResetPolicy = () => {
  Object.assign(policyForm, {
    returnDays: 7,
    exchangeDays: 15,
    refundDays: 7,
    autoApprove: false,
    requireProof: true,
    returnAddress: '广东省深圳市南山区科技园南区某某大厦101',
    contactName: '售后客服',
    contactPhone: '400-123-4567'
  })
  ElMessage.success('已重置为默认配置')
}

// 添加原因
const handleAddReason = () => {
  reasonDialogTitle.value = '添加原因'
  Object.assign(reasonForm, {
    id: null,
    type: 'return',
    reason: '',
    sort: 0,
    enabled: true
  })
  reasonDialogVisible.value = true
}

// 编辑原因
const handleEditReason = (row) => {
  reasonDialogTitle.value = '编辑原因'
  Object.assign(reasonForm, { ...row })
  reasonDialogVisible.value = true
}

// 保存原因
const handleSaveReason = () => {
  ElMessage.success('保存成功')
  reasonDialogVisible.value = false
}

// 切换原因状态
const handleToggleReason = (row) => {
  ElMessage.success(row.enabled ? '已启用' : '已禁用')
}

// 删除原因
const handleDeleteReason = (row) => {
  ElMessageBox.confirm(`确定要删除原因"${row.reason}"吗？`, '确认删除', {
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  })
}

// 获取原因类型标签
const getReasonTypeTag = (type) => {
  const typeMap = {
    return: 'danger',
    exchange: 'warning',
    repair: 'success'
  }
  return typeMap[type] || 'info'
}

// 获取原因类型文本
const getReasonTypeText = (type) => {
  const textMap = {
    return: '退货',
    exchange: '换货',
    repair: '维修'
  }
  return textMap[type] || '未知'
}

// 保存评价配置
const handleSaveReview = () => {
  ElMessage.success('评价配置已保存')
}

// 显示标签输入框
const showTagInput = () => {
  tagInputVisible.value = true
  nextTick(() => {
    tagInputRef.value?.focus()
  })
}

// 确认添加标签
const handleTagConfirm = () => {
  if (tagInputValue.value) {
    reviewForm.tags.push(tagInputValue.value)
    tagInputValue.value = ''
  }
  tagInputVisible.value = false
}

// 移除标签
const handleRemoveTag = (tag) => {
  reviewForm.tags.splice(reviewForm.tags.indexOf(tag), 1)
}

// 保存通知设置
const handleSaveNotify = () => {
  ElMessage.success('通知设置已保存')
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.section-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}
</style>
