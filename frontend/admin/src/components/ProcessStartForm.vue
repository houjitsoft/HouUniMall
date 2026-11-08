<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="rules"
    label-width="100px"
    @submit.prevent
  >
    <el-form-item label="申请人" prop="applicant">
      <el-input v-model="formData.applicant" placeholder="请输入申请人姓名" />
    </el-form-item>

    <el-form-item label="申请部门" prop="department">
      <el-input v-model="formData.department" placeholder="请输入申请部门" />
    </el-form-item>

    <el-form-item label="申请事由" prop="reason">
      <el-input
        v-model="formData.reason"
        type="textarea"
        :rows="3"
        placeholder="请输入申请事由"
      />
    </el-form-item>

    <el-form-item label="紧急程度" prop="urgency">
      <el-select v-model="formData.urgency" placeholder="请选择紧急程度">
        <el-option label="普通" value="normal" />
        <el-option label="紧急" value="urgent" />
        <el-option label="特急" value="veryUrgent" />
      </el-select>
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="handleSubmit">提交</el-button>
      <el-button @click="handleCancel">取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, defineProps, defineEmits } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  process: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['submit', 'cancel'])

const formRef = ref()

const formData = reactive({
  applicant: '',
  department: '',
  reason: '',
  urgency: 'normal'
})

const rules = {
  applicant: [
    { required: true, message: '请输入申请人姓名', trigger: 'blur' }
  ],
  department: [
    { required: true, message: '请输入申请部门', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入申请事由', trigger: 'blur' }
  ],
  urgency: [
    { required: true, message: '请选择紧急程度', trigger: 'change' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    emit('submit', { ...formData })
  } catch (error) {
    ElMessage.error('请完善表单信息')
  }
}

const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped>
/* 样式可以根据需要添加 */
</style>
