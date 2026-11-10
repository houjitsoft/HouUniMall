<template>
  <div class="join-upload-cover">
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
    >
      <span class="join-upload-hit" aria-hidden="true"></span>
    </el-upload>
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

<!--
  入驻页（step3 证件 / step6 付款凭证）给本组件透传的是
  `wp100 hp100 absolute top0 zIndex2`：absolute / top0 生效，但 wp100 / hp100
  两个原子类在 bcenter 全仓都没有定义，覆盖层宽度恒为 0，
  el-upload 的点击区也是 0 宽，点「上传图片」自然没有任何反应。
  这里让组件自带可点尺寸与层级，不依赖页面的原子类。
-->
<style lang="scss" scoped>
/* el-upload 组件根是 .upload-demo 这层包装 div，内层才是 .el-upload 触发区，
   两层都得撑满；只写内层时父宽收缩为 0，内层 100% 仍等于 0。 */
.join-upload-cover,
.join-upload-cover > :deep(.upload-demo),
.join-upload-cover :deep(.el-upload),
.join-upload-hit {
  width: 100%;
  height: 100%;
  min-width: 120px;
  min-height: 28px;
}

.join-upload-cover {
  z-index: 2;
}

.join-upload-cover > :deep(.upload-demo),
.join-upload-cover :deep(.el-upload) {
  display: block;
  line-height: normal;
  cursor: pointer;
}

.join-upload-hit {
  display: block;
}
</style>
