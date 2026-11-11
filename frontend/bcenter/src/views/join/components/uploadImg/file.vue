<template>
  <div>
    <el-upload
      v-if="ossType && ossType == 'minio'"
      class="upload-demo"
      :action="uploadUrl"
      :on-success="handleSuccess"
      :on-remove="handleRemove"
      multiple
      :file-list="fileList"
      ref="uploadRef"
      :before-upload="beforeAvatarUpload"
    >
      <el-button size="small" type="primary" round>点击上传</el-button>
    </el-upload>
    <el-upload
      v-else
      ref="uploadRef"
      :http-request="upload"
      :before-upload="beforeAvatarUpload"
      :on-preview="handlePictureCardPreview"
      :on-remove="handleRemove"
      :limit="1"
      :file-list="fileList"
      :on-success="handleSuccess"
      class="upload-demo fl"
      action="''"
      :on-change="handleChange"
      :accept="accept"
    >
      <el-button size="mini" type="primary" round>点击上传</el-button>
    </el-upload>

    <div class="clear" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getPresignedUrl } from '@/api/joinSet'

const props = defineProps({
  accept: String,
})

const emit = defineEmits(['changeSrc', 'changeRemove'])

const uploadRef = ref(null)
// bcenter 走 Vite，不再有 process.env；直传(minio)分支用的 OSS 上传地址按三方服务路由拼装。
// ossType 当前恒为空，此分支未启用，这里仅保证不引用未定义的 process 而崩溃。
const base = (import.meta.env.VITE_APP_BASE_URL || '').replace(/\/+$/, '')
const uploadUrl = `${base}/web/thirdparty/web/oss/uploadFile`
const ossType = ref('')
const fileList = ref([])
const dialogImageUrl = ref('')
const dialogVisible = ref(false)
const fileName = ref('')
const uploadParams = ref({ uploadPath: '', uploadSize: '' })

function generateNewFileName(originalName) {
  const timestamp = Date.now()
  const fileExtension = originalName.split('.').pop()
  return `image_${timestamp}.${fileExtension}`
}

function handleChange(file, list) {
  fileList.value = list.slice(-3)
}

function beforeAvatarUpload(file) {
  const format = /^application\/(pdf)$/.test(file.type)
  const xlsx =
    /^application\/(vnd.openxmlformats-officedocument.spreadsheetml.sheet)$/.test(
      file.type
    )
  const xls = /^application\/(vnd.ms-excel)$/.test(file.type)

  if (props.accept) {
    if (!(format || xlsx || xls)) {
      ElMessage.error('只能上传pdf,xlsx,xls文件!')
      return false
    }
  } else if (!format) {
    ElMessage.error('只能上传pdf文件!')
    return false
  }

  uploadParams.value.uploadSize = file.size
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('上传pdf大小不能超过 2MB!')
    return false
  }
  return true
}

async function upload(item) {
  try {
    const newFileName = generateNewFileName(item.file.name)
    const fileUrl = await getPresignedUrl(item.file, newFileName)
    fileName.value = newFileName
    fileList.value = []
    emit('changeSrc', fileUrl, fileUrl, fileName.value)
  } catch (e) {
    console.error('上传失败:', e)
    ElMessage.error((e && e.message) || '上传失败，请稍后再试')
  }
}

function handleSuccess() {}
function handlePictureCardPreview(file) {
  dialogImageUrl.value = file.url
  dialogVisible.value = true
}
function handleRemove() {
  emit('changeRemove')
}
</script>

<style lang="scss" rel="stylesheet/scss">
.el-upload {
  text-align: left !important;
}
.el-upload-list {
  display: block !important;
}
.detail-agreements {
  display: none;
}
.upload-demo {
  display: inline-block;
  line-height: 32px;
  margin-left: 0;
}
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  text-align: inherit;
}
.avatar-uploader .el-upload:hover {
  border-color: #2c9b69;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 60px;
  height: 60px;
  line-height: 60px;
  text-align: center;
}
.avatar {
  width: 140px;
  height: 140px;
  display: block;
}
.el-upload-list--picture-card .el-upload-list__item {
  width: 100px;
  height: 100px;
}
.el-upload--picture-card {
  width: 100px;
  height: 100px;
  line-height: 102px;
}
</style>
