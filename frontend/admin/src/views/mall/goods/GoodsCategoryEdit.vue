<template>
  <el-dialog
    v-model="visible"
    :title="mode === 'edit' ? '编辑栏目' : '新增栏目'"
    width="550px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      label-width="90px"
      :rules="formRules"
      style="margin-right:12px"
    >
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入分类名称" clearable style="width:320px" />
      </el-form-item>

      <el-form-item label="上级分类" prop="hasParent">
        <el-radio-group v-model="formData.hasParent" style="margin-left:16px">
          <el-radio :label="0">无顶级分类</el-radio>
          <el-radio :label="1">有顶级分类</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="formData.hasParent" prop="pid">
        <el-cascader
          v-model="formData.pid"
          :options="categoryOptions"
          :props="cascaderProps"
          clearable
          style="width:320px"
          placeholder="请选择上级分类"
        />
      </el-form-item>

      <el-form-item label="分类图片" prop="image">
        <el-upload
          class="category-img-uploader"
          action="#"
          :show-file-list="false"
          :before-upload="beforeUpload"
          :http-request="handleUpload"
          accept="image/*"
        >
          <el-image
            v-if="formData.image"
            :src="formData.image"
            style="width:80px;height:80px;display:block"
            fit="cover"
          />
          <div v-else class="upload-placeholder">
            <el-icon class="upload-icon"><Plus /></el-icon>
            <span class="upload-text">添加</span>
          </div>
        </el-upload>
      </el-form-item>

      <el-form-item label="排序">
        <div>
          <el-input v-model="formData.sort" placeholder="请输入排序" clearable style="width:320px" />
          <div style="font-size:12px;color:#909399;margin-top:4px">默认为0，数值越大越排前</div>
        </div>
      </el-form-item>

      <el-form-item label="是否显示" prop="isShow">
        <el-switch v-model="formData.isShow" :active-value="1" :inactive-value="0" />
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
import {
  mallCategoryAdd, mallCategoryEdit, mallCategoryAll
} from '@/api/mall/goodsAdmin.js'

const emit = defineEmits(['success', 'close'])

const visible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const mode = ref('add')
const categoryOptions = ref([])

const formData = reactive({
  id: '',
  name: '',
  sort: 0,
  hasParent: 0,
  pid: 0,
  isShow: 1,
  image: ''
})

const cascaderProps = reactive({
  multiple: false,
  checkStrictly: true,
  label: 'name',
  value: 'id',
  children: 'sons',
  emitPath: false
})

const formRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: ['blur'] }]
}

/**
 * 获取分类列表（用于级联选择器）
 */
const fetchCategoryOptions = async () => {
  try {
    const res = await mallCategoryAll()
    if (res && res.code === 1) {
      categoryOptions.value = res.data || []
    }
  } catch (e) { /* ignore */ }
}

/**
 * 打开弹窗
 * @param {string} type - 'add' | 'edit'
 * @param {Object} row - 编辑时传入的行数据
 */
const open = (type, row) => {
  mode.value = type
  // 重置表单
  Object.assign(formData, {
    id: '',
    name: '',
    sort: 0,
    hasParent: 0,
    pid: 0,
    isShow: 1,
    image: ''
  })
  if (type === 'edit' && row) {
    formData.id = row.id
    formData.name = row.name
    formData.sort = row.sort ?? 0
    formData.isShow = row.isShow ?? 1
    formData.image = row.image ?? ''
    formData.pid = row.pid ?? 0
    formData.hasParent = row.pid ? 1 : 0
  }
  fetchCategoryOptions()
  visible.value = true
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  // 无上级分类时 pid 置 0
  const payload = {
    ...formData,
    pid: formData.hasParent ? formData.pid : 0
  }
  submitting.value = true
  try {
    const res = mode.value === 'edit'
      ? await mallCategoryEdit(payload)
      : await mallCategoryAdd(payload)
    if (res && res.code === 1) {
      ElMessage.success('保存成功')
      visible.value = false
      emit('success')
    } else {
      ElMessage.error(res?.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  visible.value = false
  emit('close')
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
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return true
}

/**
 * 自定义上传处理
 * @param {object} options - 上传选项
 */
const handleUpload = async (options) => {
  const fd = new FormData()
  fd.append('file', options.file)
  try {
    const res = await request.post('/web/thirdparty/oss/uploadFile', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res && res.code === 1 && res.data) {
      formData.image = res.data.url || res.data
    } else {
      ElMessage.error('上传失败')
    }
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

defineExpose({ open })
</script>

<style scoped>
/**
 * 分类图片上传器样式
 */
.category-img-uploader {
  .upload-placeholder {
    width: 80px;
    height: 80px;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #8c8c8c;
    transition: border-color 0.3s;

    &:hover {
      border-color: #409eff;
      color: #409eff;
    }

    .upload-icon {
      font-size: 20px;
      margin-bottom: 4px;
    }

    .upload-text {
      font-size: 12px;
    }
  }

  :deep(.el-upload) {
    display: block;
  }
}
</style>