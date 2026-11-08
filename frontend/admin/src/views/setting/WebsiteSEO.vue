<template>
  <div class="website-seo">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>SEO设置</span>
        </div>
      </template>
      
      <el-form :model="seoForm" label-width="120px">
        <el-form-item label="网站标题">
          <el-input v-model="seoForm.title" placeholder="请输入网站标题"></el-input>
        </el-form-item>
        
        <el-form-item label="网站关键词">
          <el-input 
            v-model="seoForm.keywords" 
            type="textarea" 
            :rows="3"
            placeholder="请输入网站关键词，多个关键词用逗号分隔"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="网站描述">
          <el-input 
            v-model="seoForm.description" 
            type="textarea" 
            :rows="4"
            placeholder="请输入网站描述"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="网站Logo">
          <el-upload
            class="logo-uploader"
            action="#"
            :show-file-list="false"
            :on-success="handleLogoSuccess"
            :before-upload="beforeLogoUpload"
          >
            <img v-if="seoForm.logo" :src="seoForm.logo" class="logo" />
            <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="网站图标">
          <el-upload
            class="favicon-uploader"
            action="#"
            :show-file-list="false"
            :on-success="handleFaviconSuccess"
            :before-upload="beforeFaviconUpload"
          >
            <img v-if="seoForm.favicon" :src="seoForm.favicon" class="favicon" />
            <el-icon v-else class="favicon-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="robots.txt">
          <el-input 
            v-model="seoForm.robots" 
            type="textarea" 
            :rows="6"
            placeholder="请输入robots.txt内容"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="网站地图">
          <el-switch v-model="seoForm.sitemapEnabled"></el-switch>
          <span class="form-tip">启用自动生成网站地图</span>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveSeo">保存设置</el-button>
          <el-button @click="resetSeo">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const seoForm = reactive({
  title: 'UniMall 管理系统',
  keywords: '管理系统,企业应用,业务管理',
  description: 'UniMall是一个功能强大的企业级管理系统，提供完整的业务管理解决方案。',
  logo: '',
  favicon: '',
  robots: `User-agent: *
Allow: /
Disallow: /admin/
Disallow: /api/`,
  sitemapEnabled: true
})

const handleLogoSuccess = (response, file) => {
  seoForm.logo = URL.createObjectURL(file.raw)
}

const handleFaviconSuccess = (response, file) => {
  seoForm.favicon = URL.createObjectURL(file.raw)
}

const beforeLogoUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const beforeFaviconUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt500K = file.size / 1024 < 500

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt500K) {
    ElMessage.error('图标大小不能超过 500KB!')
    return false
  }
  return true
}

const saveSeo = () => {
  ElMessage.success('SEO设置保存成功')
}

const resetSeo = () => {
  seoForm.title = 'UniMall 管理系统'
  seoForm.keywords = '管理系统,企业应用,业务管理'
  seoForm.description = 'UniMall是一个功能强大的企业级管理系统，提供完整的业务管理解决方案。'
  seoForm.logo = ''
  seoForm.favicon = ''
  seoForm.robots = `User-agent: *
Allow: /
Disallow: /admin/
Disallow: /api/`
  seoForm.sitemapEnabled = true
  ElMessage.info('设置已重置')
}
</script>

<style scoped>
.website-seo {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-uploader .logo {
  width: 178px;
  height: 60px;
  display: block;
  object-fit: contain;
}

.favicon-uploader .favicon {
  width: 32px;
  height: 32px;
  display: block;
}

.logo-uploader-icon,
.favicon-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 60px;
  line-height: 60px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
}

.favicon-uploader-icon {
  width: 32px;
  height: 32px;
  line-height: 32px;
  font-size: 16px;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}
</style>
