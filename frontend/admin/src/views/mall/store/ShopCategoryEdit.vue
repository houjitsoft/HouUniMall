<template>
  <el-dialog
    v-model="visible"
    :title="mode === 'edit' ? '编辑经营类目' : '新增经营类目'"
    width="550px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="formData" label-width="84px" :rules="formRules">
      <el-form-item label="分类图片">
        <el-upload
          class="avatar-uploader"
          action="#"
          :show-file-list="false"
          :before-upload="beforeUpload"
          :http-request="handleUpload"
        >
          <el-image
            v-if="formData.image"
            :src="formData.image"
            style="width: 80px; height: 80px; display: block"
            fit="cover"
          />
          <el-icon v-else style="width: 80px; height: 80px; font-size: 24px; border: 1px dashed #d9d9d9; border-radius: 4px"><Plus /></el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入分类名称" clearable class="w-[320px]" />
      </el-form-item>
      <el-form-item label="排序">
        <div>
          <el-input v-model="formData.sort" placeholder="请输入排序" clearable class="w-[320px]" />
          <div class="text-gray-400 text-xs mt-1">默认为0，数值越大越排前</div>
        </div>
      </el-form-item>
      <el-form-item label="是否显示" prop="status">
        <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
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
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const emit = defineEmits(['success', 'close'])

/** 弹窗可见 */
const visible = ref(false)
/** 模式：add/edit */
const mode = ref('add')
/** 提交中 */
const submitting = ref(false)
/** 表单ref */
const formRef = ref(null)

/** 表单数据 */
const formData = reactive({
  id: '',
  name: '',
  sort: 0,
  status: 1,
  image: ''
})

/** 表单校验规则 */
const formRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: ['blur'] }],
  status: [{ required: true, message: '请选择是否显示', trigger: ['blur'] }]
}

/**
 * 打开弹窗
 * @param {string} type - 'add' 或 'edit'
 */
const open = (type = 'add') => {
  mode.value = type
  if (type === 'add') {
    formData.id = ''
    formData.name = ''
    formData.sort = 0
    formData.status = 1
    formData.image = ''
  }
  visible.value = true
}

/**
 * 设置表单数据（用于编辑回填）
 * @param {object} data - 分类数据
 */
const setFormData = (data) => {
  for (const key in formData) {
    if (data[key] != null && data[key] !== undefined) {
      formData[key] = data[key]
    }
  }
}

/**
 * 获取详情并回填
 * @param {object} row - 列表行数据
 */
const getDetail = async (row) => {
  try {
    const res = await request.get('/web/mall/store/shop-category/detail', { params: { id: row.id } })
    if (res && res.code === 1 && res.data) {
      setFormData(res.data)
    }
  } catch (e) {
    console.error('获取详情失败', e)
  }
}

/**
 * 上传前校验
 * @param {File} file - 文件对象
 * @returns {boolean}
 */
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('请上传图片文件')
    return false
  }
  return true
}

/**
 * 自定义上传处理
 * @param {object} options - 上传选项
 */
const handleUpload = async (options) => {
  const formDataUpload = new FormData()
  formDataUpload.append('file', options.file)
  try {
    const res = await request.post('/web/common/upload', formDataUpload, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res && res.code === 1 && res.data) {
      formData.image = res.data.url || res.data
    }
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const api = mode.value === 'edit'
      ? '/web/mall/store/shop-category/update'
      : '/web/mall/store/shop-category/create'
    const res = await request.post(api, formData)
    if (res && res.code === 1) {
      ElMessage.success(mode.value === 'edit' ? '编辑成功' : '新增成功')
      visible.value = false
      emit('success')
    } else {
      ElMessage.error(res?.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

/**
 * 关闭弹窗
 */
const handleClose = () => {
  visible.value = false
  emit('close')
}

defineExpose({ open, setFormData, getDetail })
</script>
