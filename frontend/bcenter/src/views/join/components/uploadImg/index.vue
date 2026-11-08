<template>
  <div>
    <el-upload
      ref="uploadRef"
      :http-request="upload"
      :before-upload="beforeAvatarUpload"
      :on-preview="handlePictureCardPreview"
      :on-remove="handleRemove"
      :limit="1"
      :file-list="fileList"
      :auto-upload="true"
      :on-success="handleSuccess"
      class="upload-demo wp100 hp100 iblock"
      action="''"
      multiple
      accept
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getPresignedUrl } from '@/api/joinSet'
import { compressImage } from '@/utils/compressImage'

const emit = defineEmits(['changeSrc'])

const uploadRef = ref(null)
const fileList = ref([])
const dialogImageUrl = ref('')
const dialogVisible = ref(false)
const uploadParams = ref({ uploadPath: '', uploadSize: '' })

function generateNewFileName(originalName) {
  const timestamp = Date.now()
  const fileExtension = originalName.split('.').pop()
  return `image_${timestamp}.${fileExtension}`
}

function beforeAvatarUpload(file) {
  const format = /^image\/(jpeg|png|jpg)$/.test(file.type)
  if (!format) {
    ElMessage.error('上传图片只能是 JPEG，JPG，PNG格式!')
  }
  return format
}

async function upload(item) {
  const compressed = await compressImage(item.file)
  const isLt2M = compressed.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
    return
  }
  uploadParams.value.uploadSize = compressed.size
  const newFileName = generateNewFileName(compressed.name)
  const fileUrl = await getPresignedUrl(compressed, newFileName)
  fileList.value = []
  emit('changeSrc', fileUrl, fileUrl)
}

function handleSuccess() {}
function handlePictureCardPreview(file) {
  dialogImageUrl.value = file.url
  dialogVisible.value = true
}
function handleRemove() {}
</script>

<style lang="scss" rel="stylesheet/scss">
.upload-demo {
  display: inline-block;
  line-height: 32px;
}
.upload-demotow {
  display: inline-block;
  line-height: 160px;
}
.el-upload {
  width: 100%;
  height: 40px;
}
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader .el-upload:hover {
  border-color: #409eff;
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
