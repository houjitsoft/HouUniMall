<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="rules"
    label-width="100px"
    @submit.prevent
  >
    <el-form-item label="处理意见" prop="comment">
      <el-input
        v-model="formData.comment"
        type="textarea"
        :rows="4"
        placeholder="请输入处理意见"
      />
    </el-form-item>

    <el-form-item label="处理结果" prop="result">
      <el-radio-group v-model="formData.result">
        <el-radio label="approved">同意</el-radio>
        <el-radio label="rejected">拒绝</el-radio>
        <el-radio label="returned">退回</el-radio>
      </el-radio-group>
    </el-form-item>

    <el-form-item v-if="showDelegate" label="委派人">
      <el-select v-model="formData.delegateTo" placeholder="请选择委派人" clearable>
        <el-option
          v-for="user in delegateUsers"
          :key="user.id"
          :label="user.name"
          :value="user.id"
        />
      </el-select>
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="handleSubmit">提交</el-button>
      <el-button @click="handleCancel">取消</el-button>
      <el-button v-if="showDelegate" type="warning" @click="handleDelegate">
        委派
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, defineProps, defineEmits, computed } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  task: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['submit', 'cancel', 'delegate'])

const formRef = ref()

const formData = reactive({
  comment: '',
  result: 'approved',
  delegateTo: ''
})

const rules = {
  comment: [
    { required: true, message: '请输入处理意见', trigger: 'blur' }
  ],
  result: [
    { required: true, message: '请选择处理结果', trigger: 'change' }
  ]
}

// 模拟委派用户数据
const delegateUsers = [
  { id: 1, name: '张三' },
  { id: 2, name: '李四' },
  { id: 3, name: '王五' }
]

// 是否显示委派选项
const showDelegate = computed(() => {
  return props.task?.processKey === 'leave-request' || props.task?.processKey === 'reimbursement'
})

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

const handleDelegate = () => {
  if (!formData.delegateTo) {
    ElMessage.warning('请选择委派人')
    return
  }
  emit('delegate', { delegateTo: formData.delegateTo, comment: formData.comment })
}
</script>

<style scoped>
/* 样式可以根据需要添加 */
</style>
