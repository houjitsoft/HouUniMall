<template>
  <div class="setting-seo-page" v-loading="loading">
    <header class="seo-hero">
      <div class="seo-hero__text">
        <h1 class="seo-hero__title">SEO 配置</h1>
        <p class="seo-hero__desc">优化搜索引擎收录与社交分享展示，提升品牌曝光</p>
      </div>
      <div class="seo-hero__badge">
        <el-icon><Search /></el-icon>
        <span>GEO / SEO</span>
      </div>
    </header>

    <el-row :gutter="12" class="seo-stats">
      <el-col :xs="12" :sm="6">
        <div class="stat-chip" :class="{ warn: titleLen > 60 }">
          <span class="stat-chip__value">{{ titleLen }}</span>
          <span class="stat-chip__label">标题字数</span>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-chip" :class="{ warn: descLen > 160 }">
          <span class="stat-chip__value">{{ descLen }}</span>
          <span class="stat-chip__label">描述字数</span>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-chip">
          <span class="stat-chip__value">{{ keywordCount }}</span>
          <span class="stat-chip__label">关键词</span>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-chip" :class="{ ok: form.sitemapEnabled }">
          <span class="stat-chip__value">{{ form.sitemapEnabled ? '开' : '关' }}</span>
          <span class="stat-chip__label">站点地图</span>
        </div>
      </el-col>
    </el-row>

    <section class="serp-preview">
      <div class="section-head">
        <h2>搜索结果预览</h2>
        <span class="section-tag">SERP</span>
      </div>
      <div class="serp-card">
        <div class="serp-card__url">unimall.houjit.com › setting</div>
        <div class="serp-card__title">{{ previewTitle }}</div>
        <div class="serp-card__desc">{{ previewDesc }}</div>
      </div>
    </section>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="seo-form">
      <section class="form-section">
        <div class="section-head">
          <h2>基础 Meta</h2>
          <p>页面 title / keywords / description</p>
        </div>
        <el-form-item label="网站标题" prop="title">
          <el-input v-model="form.title" maxlength="80" show-word-limit placeholder="建议 30–60 字" />
        </el-form-item>
        <el-form-item label="网站关键词" prop="keywords">
          <el-input
            v-model="form.keywords"
            type="textarea"
            :rows="2"
            maxlength="200"
            show-word-limit
            placeholder="多个关键词用逗号分隔"
          />
        </el-form-item>
        <el-form-item label="网站描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            maxlength="300"
            show-word-limit
            placeholder="建议 80–160 字，概括站点价值"
          />
        </el-form-item>
        <el-form-item label="Canonical 地址">
          <el-input v-model="form.canonical" placeholder="https://unimall.houjit.com/" />
        </el-form-item>
      </section>

      <section class="form-section">
        <div class="section-head">
          <h2>品牌资产</h2>
          <p>Logo、Favicon 与分享图</p>
        </div>
        <div class="upload-grid">
          <div class="upload-tile">
            <span class="upload-tile__label">网站 Logo</span>
            <el-upload
              class="brand-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="(res) => onUploadSuccess(res, 'logo')"
              :before-upload="beforeImageUpload"
            >
              <img v-if="form.logo" :src="form.logo" class="brand-preview logo" alt="logo" />
              <div v-else class="brand-placeholder">
                <el-icon><Picture /></el-icon>
                <span>上传 Logo</span>
              </div>
            </el-upload>
          </div>
          <div class="upload-tile">
            <span class="upload-tile__label">Favicon</span>
            <el-upload
              class="brand-uploader favicon"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="(res) => onUploadSuccess(res, 'favicon')"
              :before-upload="beforeFaviconUpload"
            >
              <img v-if="form.favicon" :src="form.favicon" class="brand-preview favicon" alt="favicon" />
              <div v-else class="brand-placeholder small">
                <el-icon><Picture /></el-icon>
              </div>
            </el-upload>
          </div>
          <div class="upload-tile full">
            <span class="upload-tile__label">Open Graph 分享图</span>
            <el-upload
              class="brand-uploader og"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="(res) => onUploadSuccess(res, 'ogImage')"
              :before-upload="beforeImageUpload"
            >
              <img v-if="form.ogImage" :src="form.ogImage" class="brand-preview og" alt="og" />
              <div v-else class="brand-placeholder wide">
                <el-icon><Picture /></el-icon>
                <span>1200×630 推荐</span>
              </div>
            </el-upload>
          </div>
        </div>
      </section>

      <section class="form-section">
        <div class="section-head">
          <h2>爬虫与索引</h2>
          <p>robots.txt 与站点地图</p>
        </div>
        <el-form-item label="robots.txt">
          <el-input v-model="form.robots" type="textarea" :rows="6" placeholder="爬虫规则" />
        </el-form-item>
        <el-form-item label="自动生成站点地图">
          <div class="switch-row">
            <el-switch v-model="form.sitemapEnabled" />
            <span class="switch-hint">开启后定时更新 sitemap.xml</span>
          </div>
        </el-form-item>
      </section>
    </el-form>

    <div class="seo-action-bar">
      <el-button size="large" @click="handleReset">重置</el-button>
      <el-button type="primary" size="large" :loading="saving" @click="handleSave">保存配置</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Search } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'
import { seoSettingApi } from '@/api/setting/seo'

const loading = ref(false)
const saving = ref(false)
const formRef = ref()

const form = reactive(seoSettingApi.getDefaults())

const rules = {
  title: [{ required: true, message: '请输入网站标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入网站描述', trigger: 'blur' }]
}

const uploadUrl = `${import.meta.env.VITE_APP_BASE_API}/web/common/upload`
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${getToken()}` }))

const titleLen = computed(() => (form.title || '').length)
const descLen = computed(() => (form.description || '').length)
const keywordCount = computed(() => {
  const raw = (form.keywords || '').trim()
  if (!raw) return 0
  return raw.split(/[,，]/).filter(Boolean).length
})
const previewTitle = computed(() => form.title || '网站标题预览')
const previewDesc = computed(() => {
  const d = form.description || '网站描述将显示在搜索结果摘要中…'
  return d.length > 160 ? `${d.slice(0, 157)}…` : d
})

async function loadData() {
  loading.value = true
  try {
    Object.assign(form, await seoSettingApi.load())
  } catch (e) {
    ElMessage.error('加载 SEO 配置失败')
  } finally {
    loading.value = false
  }
}

function onUploadSuccess(response, field) {
  if (response?.code === 200 || response?.code === 1) {
    const url = response.data?.url || response.data?.fileUrl || response.data
    if (url) {
      form[field] = url
      ElMessage.success('上传成功')
    }
  } else {
    ElMessage.error(response?.message || '上传失败')
  }
}

function beforeImageUpload(file) {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片')
    return false
  }
  if (file.size / 1024 / 1024 > 2) {
    ElMessage.error('图片不能超过 2MB')
    return false
  }
  return true
}

function beforeFaviconUpload(file) {
  if (!beforeImageUpload(file)) return false
  if (file.size / 1024 > 500) {
    ElMessage.error('图标不能超过 500KB')
    return false
  }
  return true
}

async function handleSave() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const res = await seoSettingApi.save({ ...form })
    if (res?.code === 1) {
      ElMessage.success('SEO 配置已保存')
    } else {
      ElMessage.warning(res?.message || '保存异常')
    }
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function handleReset() {
  await ElMessageBox.confirm('确定恢复默认 SEO 配置？', '重置确认', { type: 'warning' })
  Object.assign(form, seoSettingApi.getDefaults())
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.setting-seo-page {
  padding: 16px;
  padding-bottom: calc(88px + env(safe-area-inset-bottom, 0px));
  max-width: 960px;
  margin: 0 auto;
}

.seo-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 20px;
  border-radius: 16px;
  background: linear-gradient(135deg, #1d4ed8 0%, #6366f1 55%, #8b5cf6 100%);
  color: #fff;
  margin-bottom: 16px;
  box-shadow: 0 8px 24px rgba(29, 78, 216, 0.25);

  &__title {
    margin: 0 0 6px;
    font-size: 22px;
    font-weight: 700;
    letter-spacing: -0.02em;
  }

  &__desc {
    margin: 0;
    font-size: 13px;
    opacity: 0.9;
    line-height: 1.5;
  }

  &__badge {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 12px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.18);
    font-size: 12px;
    font-weight: 600;
    flex-shrink: 0;
  }
}

.seo-stats {
  margin-bottom: 16px;
}

.stat-chip {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 12px;
  text-align: center;
  margin-bottom: 8px;
  transition: box-shadow 0.2s;

  &.warn {
    border-color: #fde2a0;
    background: #fffbeb;
  }

  &.ok {
    border-color: #b3e19d;
    background: #f0f9eb;
  }

  &__value {
    display: block;
    font-size: 20px;
    font-weight: 700;
    color: #303133;
    line-height: 1.2;
  }

  &__label {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
    display: block;
  }
}

.section-head {
  margin-bottom: 14px;

  h2 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  p {
    margin: 4px 0 0;
    font-size: 12px;
    color: #909399;
  }

  .section-tag {
    float: right;
    font-size: 11px;
    font-weight: 600;
    color: #6366f1;
    background: #eef2ff;
    padding: 2px 8px;
    border-radius: 6px;
  }
}

.serp-preview {
  margin-bottom: 16px;
}

.serp-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  &__url {
    font-size: 12px;
    color: #059669;
    margin-bottom: 4px;
  }

  &__title {
    font-size: 18px;
    color: #1a0dab;
    font-weight: 500;
    line-height: 1.35;
    margin-bottom: 6px;
    word-break: break-word;
  }

  &__desc {
    font-size: 13px;
    color: #545454;
    line-height: 1.5;
    word-break: break-word;
  }
}

.form-section {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 12px;
}

.upload-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;

  .full {
    grid-column: 1 / -1;
  }
}

.upload-tile__label {
  display: block;
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.brand-uploader {
  width: 100%;
}

.brand-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 100px;
  border: 1px dashed #dcdfe6;
  border-radius: 12px;
  color: #909399;
  font-size: 12px;
  background: #fafafa;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;

  &:hover {
    border-color: #409eff;
    background: #f0f7ff;
  }

  &.small {
    min-height: 72px;
  }

  &.wide {
    min-height: 120px;
  }
}

.brand-preview {
  width: 100%;
  border-radius: 12px;
  object-fit: contain;
  background: #f5f7fa;
  border: 1px solid #ebeef5;

  &.logo {
    height: 80px;
  }

  &.favicon {
    width: 48px;
    height: 48px;
  }

  &.og {
    width: 100%;
    max-height: 160px;
  }
}

.switch-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.switch-hint {
  font-size: 13px;
  color: #909399;
}

.seo-action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom, 0px));
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-top: 1px solid #ebeef5;
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.06);

  .el-button {
    flex: 1;
    min-height: 44px;
    border-radius: 10px;
    font-weight: 600;
  }
}

@media (min-width: 992px) {
  .setting-seo-page {
    padding: 20px 24px 24px;
    padding-bottom: 24px;
  }

  .seo-action-bar {
    position: static;
    margin-top: 8px;
    padding: 0;
    background: transparent;
    backdrop-filter: none;
    border: none;
    box-shadow: none;
    justify-content: flex-end;

    .el-button {
      flex: 0 0 auto;
      min-width: 120px;
    }
  }
}
</style>
