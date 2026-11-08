<template>
  <el-dialog
    v-model="visible"
    :title="isReAudit ? '违规重审' : '审核'"
    width="500px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      label-width="100px"
      :rules="formRules"
      style="margin-right:12px"
    >
      <!-- 非违规重审时显示审核状态单选 -->
      <el-form-item v-if="!isReAudit" label="审核状态">
        <el-radio-group v-model="formData.auditAgree" style="margin-left:16px">
          <el-radio :label="1" size="large">审核通过</el-radio>
          <el-radio :label="0" size="large">审核不通过</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 审核不通过或违规重审时显示说明输入框 -->
      <el-form-item
        v-if="!formData.auditAgree || isReAudit"
        label="审核说明"
        prop="auditContent"
      >
        <el-input
          v-model="formData.auditContent"
          type="textarea"
          placeholder="请输入审核说明"
          :autosize="{ minRows: 3, maxRows: 5 }"
          show-word-limit
          clearable
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { mallGoodsAudit, mallGoodsReAudit } from '@/api/mall/goodsAdmin.js'

const emit = defineEmits(['success', 'close'])

const visible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const isReAudit = ref(false)

const formData = reactive({
  id: 0,
  auditAgree: 1,
  auditContent: ''
})

const formRules = {
  auditContent: [{ required: true, message: '请输入说明', trigger: ['blur'] }]
}

/**
 * 打开审核弹窗
 * @param {boolean} reAudit - 是否为违规重审
 * @param {Object} data - 商品数据 {id, auditContent}
 */
const open = (reAudit, data) => {
  isReAudit.value = reAudit
  formData.id = data.id
  formData.auditAgree = 1
  formData.auditContent = ''
  visible.value = true
}

/**
 * 提交审核/违规重审
 */
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    let res
    if (isReAudit.value) {
      res = await mallGoodsReAudit({ id: formData.id, auditContent: formData.auditContent })
    } else {
      res = await mallGoodsAudit({
        id: formData.id,
        auditAgree: formData.auditAgree,
        auditContent: formData.auditContent
      })
    }
    if (res && res.code === 1) {
      ElMessage.success('操作成功')
      visible.value = false
      emit('success')
    } else {
      ElMessage.error(res?.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  visible.value = false
  emit('close')
}

defineExpose({ open })
</script>
