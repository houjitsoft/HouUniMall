<template>
  <div class="website-theme">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>主题设置</span>
        </div>
      </template>

      <el-form :model="themeForm" label-width="120px">
        <el-form-item label="主题模式">
          <el-radio-group v-model="themeForm.mode" @change="liveApply">
            <el-radio value="light">浅色模式</el-radio>
            <el-radio value="dark">深色模式</el-radio>
            <el-radio value="auto">跟随系统</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="主色调">
          <el-color-picker v-model="themeForm.primaryColor" @change="liveApply" />
          <span class="form-tip">选择系统主色调</span>
        </el-form-item>

        <el-form-item label="侧边栏主题">
          <el-select v-model="themeForm.sidebarTheme" placeholder="请选择侧边栏主题" @change="liveApply">
            <el-option label="深色" value="dark" />
            <el-option label="浅色" value="light" />
            <el-option label="蓝色" value="blue" />
            <el-option label="绿色" value="green" />
          </el-select>
        </el-form-item>

        <el-form-item label="顶部导航">
          <el-switch v-model="themeForm.showTopNav" @change="liveApply" />
          <span class="form-tip">显示顶部导航栏</span>
        </el-form-item>

        <el-form-item label="侧边栏折叠">
          <el-switch v-model="themeForm.sidebarCollapse" @change="onSidebarCollapseChange" />
          <span class="form-tip">默认折叠侧边栏</span>
        </el-form-item>

        <el-form-item label="页面动画">
          <el-switch v-model="themeForm.pageAnimation" @change="liveApply" />
          <span class="form-tip">启用页面切换动画</span>
        </el-form-item>

        <el-form-item label="自定义CSS">
          <el-input
            v-model="themeForm.customCSS"
            type="textarea"
            :rows="6"
            placeholder="请输入自定义CSS代码"
            @change="liveApply"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveTheme">保存设置</el-button>
          <el-button @click="resetTheme">重置</el-button>
          <el-button type="success" @click="previewTheme">预览效果</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog v-model="previewVisible" title="主题预览" width="80%">
      <div class="theme-preview">
        <div class="preview-header" :style="{ backgroundColor: themeForm.primaryColor }">
          <h3>预览效果</h3>
        </div>
        <div class="preview-content">
          <div class="preview-sidebar" :class="themeForm.sidebarTheme">
            <div class="sidebar-item">菜单项 1</div>
            <div class="sidebar-item">菜单项 2</div>
            <div class="sidebar-item">菜单项 3</div>
          </div>
          <div class="preview-main" :class="{ dark: themeForm.mode === 'dark' }">
            <div class="preview-card">
              <h4>内容区域</h4>
              <p>这里是主要内容区域的预览效果（后台已同步应用当前选择）</p>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/stores/app'
import {
  DEFAULT_ADMIN_THEME,
  applyAdminTheme,
  loadAdminTheme,
  normalizeAdminTheme
} from '@/utils/adminTheme'

const appStore = useAppStore()
const saving = ref(false)
const previewVisible = ref(false)

const themeForm = reactive({
  ...DEFAULT_ADMIN_THEME
})

/**
 * 将表单字段同步为规范化主题对象
 * @returns {ReturnType<typeof normalizeAdminTheme>}
 */
const currentTheme = () => normalizeAdminTheme(themeForm)

/**
 * 应用主题到整个后台
 * @param {boolean} persist 是否写入 localStorage
 * @param {{ syncCollapse?: boolean }} [opts] syncCollapse 为 true 时同步侧栏折叠
 */
const applyCurrent = (persist, opts = {}) => {
  const theme = applyAdminTheme(currentTheme(), { persist, emit: true })
  if (opts.syncCollapse) {
    appStore.sidebar.opened = !!theme.sidebarCollapse
  }
  return theme
}

const liveApply = () => {
  applyCurrent(false)
}

const onSidebarCollapseChange = () => {
  applyCurrent(false, { syncCollapse: true })
}

const saveTheme = () => {
  saving.value = true
  try {
    applyCurrent(true, { syncCollapse: true })
    ElMessage.success('主题设置已保存，后台颜色已统一生效')
  } finally {
    saving.value = false
  }
}

const resetTheme = () => {
  Object.assign(themeForm, { ...DEFAULT_ADMIN_THEME })
  applyCurrent(true, { syncCollapse: true })
  ElMessage.info('已恢复默认主题')
}

const previewTheme = () => {
  applyCurrent(false, { syncCollapse: true })
  previewVisible.value = true
}

onMounted(() => {
  Object.assign(themeForm, loadAdminTheme())
  applyCurrent(false)
})
</script>

<style scoped>
.website-theme {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  margin-left: 10px;
  color: var(--hc-text-secondary, #909399);
  font-size: 14px;
}

.theme-preview {
  border: 1px solid var(--hc-border, #e4e7ed);
  border-radius: 4px;
  overflow: hidden;
}

.preview-header {
  padding: 15px 20px;
  color: white;
}

.preview-header h3 {
  margin: 0;
}

.preview-content {
  display: flex;
  height: 300px;
}

.preview-sidebar {
  width: 200px;
  padding: 20px;
}

.preview-sidebar.dark {
  background-color: #304156;
  color: white;
}

.preview-sidebar.light {
  background-color: #f5f5f5;
  color: #333;
}

.preview-sidebar.blue {
  background-color: #1d39c4;
  color: white;
}

.preview-sidebar.green {
  background-color: #237804;
  color: white;
}

.sidebar-item {
  padding: 10px;
  margin: 5px 0;
  border-radius: 4px;
  background-color: rgba(255, 255, 255, 0.1);
}

.preview-main {
  flex: 1;
  padding: 20px;
  background-color: #fafafa;
}

.preview-main.dark {
  background-color: #0f1419;
}

.preview-card {
  background: var(--hc-header-bg, white);
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.preview-card h4 {
  margin: 0 0 10px 0;
  color: var(--hc-text-primary, #303133);
}

.preview-card p {
  margin: 0;
  color: var(--hc-text-secondary, #606266);
}
</style>
