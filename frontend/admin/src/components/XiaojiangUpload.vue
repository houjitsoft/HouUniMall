<template>
  <div class="xiaojiang-upload-wrapper">
    <button type="button" class="hc-header-chip hc-header-chip--brand xiaojiang-btn" aria-label="小匠帮你传" @click="openPanel">
      <img src="@/assets/images/icons/xiaojiang.gif" alt="" class="xiaojiang-icon" />
    </button>

    <el-dialog v-if="!isMobile" v-model="showUploadDialog" width="980px" :close-on-click-modal="false" class="xiaojiang-dialog" destroy-on-close>
      <template #header><span class="dialog-header-placeholder" /></template>
      <XiaojiangUploadPanel
        ref="panelRef" :is-mobile="false" :step="state.step" :draft-no="state.draftNo" :system-key="state.systemKey"
        :systems="systems" :file-list="state.fileList" :description="state.description"
        :recognize-results="state.items" :recognizing="recognizing" :confirming="confirming" :loading-draft="loadingDraft"
        @update:system-key="state.systemKey = $event" @update:description="state.description = $event"
        @file-change="handleFileChange" @recognize="handleRecognize" @back="state.step = 'upload'"
        @target-change="handleTargetChange" @field-change="handleFieldChange" @confirm="handleConfirm"
        @retry="handleRetry" @cancel="showUploadDialog = false"
      />
    </el-dialog>

    <el-drawer v-else v-model="showUploadDialog" direction="btt" size="94%" :with-header="false" append-to-body class="xiaojiang-mobile-drawer" destroy-on-close>
      <div class="mobile-drawer-shell">
        <button type="button" class="mobile-close-btn" aria-label="关闭并保留草稿" @click="showUploadDialog = false"><el-icon :size="20"><Close /></el-icon></button>
        <XiaojiangUploadPanel
          ref="panelRef" :is-mobile="true" :step="state.step" :draft-no="state.draftNo" :system-key="state.systemKey"
          :systems="systems" :file-list="state.fileList" :description="state.description"
          :recognize-results="state.items" :recognizing="recognizing" :confirming="confirming" :loading-draft="loadingDraft"
          @update:system-key="state.systemKey = $event" @update:description="state.description = $event"
          @file-change="handleFileChange" @recognize="handleRecognize" @back="state.step = 'upload'"
          @target-change="handleTargetChange" @field-change="handleFieldChange" @confirm="handleConfirm"
          @retry="handleRetry" @cancel="showUploadDialog = false"
        />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { fileRecognizeApi } from '@/api/common/file-recognize'
import XiaojiangUploadPanel from '@/components/XiaojiangUpload/XiaojiangUploadPanel.vue'
import { applyBatchResults, createImportState, derivePermittedSystems, restoreDraftState, switchItemTarget } from '@/components/XiaojiangUpload/xiaojiangImportState.js'

const MOBILE_BREAKPOINT = 992
const DRAFT_STORAGE_KEY = 'houcloud_xiaojiang_import_draft_no'
const appStore = useAppStore()
const showUploadDialog = ref(false)
const panelRef = ref()
const state = reactive(createImportState())
const catalogSystems = ref([])
const recognizing = ref(false)
const confirming = ref(false)
const loadingDraft = ref(false)
const isMobile = ref(false)
const saveTimers = new Map()

const systems = computed(() => derivePermittedSystems(appStore.dynamicMenus || [], catalogSystems.value))

const updateDeviceMode = () => { isMobile.value = window.innerWidth < MOBILE_BREAKPOINT }

/**
 * 恢复草稿只是「锦上添花」，失败不能阻断面板：
 * - 记忆的草稿号失效（已删除 / 换组织 / 无权限）→ 丢弃记忆，避免每次打开都撞一次 404
 * - 恢复接口报错（如账号未加入组织）→ 原样透出后端可操作文案，不再吞成「加载失败请重试」
 */
const restoreRememberedDraft = async () => {
  const rememberedDraft = localStorage.getItem(DRAFT_STORAGE_KEY)
  if (!rememberedDraft) return false
  try {
    const response = await fileRecognizeApi.getImportDraft(rememberedDraft)
    if (response?.code === 1 && response.data) {
      restoreDraftState(state, response.data)
      return true
    }
  } catch (error) {
    console.warn('记忆中的智能入库草稿已不可用:', error?.message || error)
  }
  localStorage.removeItem(DRAFT_STORAGE_KEY)
  return false
}

const restoreOpenDraft = async () => {
  try {
    const response = await fileRecognizeApi.resumeImportDraft()
    if (response?.code === 1 && response.data) {
      restoreDraftState(state, response.data)
      localStorage.setItem(DRAFT_STORAGE_KEY, state.draftNo)
      return true
    }
  } catch (error) {
    const reason = error?.message || ''
    console.warn('恢复智能入库草稿失败:', reason)
    if (reason) ElMessage.warning(reason)
  }
  return false
}

const openPanel = async () => {
  showUploadDialog.value = true
  loadingDraft.value = true
  try {
    await appStore.fetchMenus()
    const catalogResponse = await fileRecognizeApi.getImportCatalog()
    if (catalogResponse?.code === 1) catalogSystems.value = catalogResponse.data?.systems || []
    if (!state.draftNo) {
      const restored = await restoreRememberedDraft() || await restoreOpenDraft()
      if (restored) ElMessage.info('已恢复上次未完成的智能入库草稿')
    }
  } catch (error) {
    console.error('加载智能入库信息失败:', error)
    ElMessage.error('智能入库信息加载失败，请稍后重试')
  } finally { loadingDraft.value = false }
}

const handleFileChange = (_file, files) => { state.fileList = files }

const handleRecognize = async () => {
  if (!state.systemKey) return ElMessage.warning('请选择业务系统')
  if (!state.description.trim()) return ElMessage.warning('请填写资料描述')
  if (!state.fileList.length) return ElMessage.warning('请先选择文件')
  const localFiles = state.fileList.filter((file) => file.raw)
  if (!localFiles.length) return ElMessage.warning('刷新后重新识别需要再次选择原文件')
  recognizing.value = true
  try {
    for (const file of localFiles) {
      const formData = new FormData()
      formData.append('file', file.raw)
      formData.append('systemKey', state.systemKey)
      formData.append('description', state.description.trim())
      if (state.draftNo) formData.append('draftNo', state.draftNo)
      const response = await fileRecognizeApi.recognizeForImport(formData)
      if (response?.code !== 1) throw new Error(response?.message || `${file.name} 识别失败`)
      restoreDraftState(state, response.data)
      if (state.draftNo) localStorage.setItem(DRAFT_STORAGE_KEY, state.draftNo)
    }
    state.step = 'preview'
    ElMessage.success(`已识别 ${state.items.length} 份资料，请确认入库位置和字段`)
  } catch (error) {
    console.error('智能识别失败:', error)
    ElMessage.error(error?.message || '智能识别失败，请重试')
  } finally { recognizing.value = false }
}

const findTarget = (objectKey) => catalogSystems.value
  .find((entry) => entry.key === state.systemKey)?.objects?.find((entry) => entry.objectKey === objectKey)

const saveItem = async (item) => {
  item.saving = true
  try {
    const response = await fileRecognizeApi.updateImportItem(item.itemId, {
      functionKey: item.functionKey, objectKey: item.objectKey, fields: item.fields
    })
    if (response?.code !== 1) throw new Error(response?.message || '预览信息保存失败')
    item.validation = response.data?.validation
    item.errorMessage = ''
    return true
  } catch (error) {
    item.errorMessage = error?.message || '预览信息保存失败'
    ElMessage.error(`${item.fileName || '资料'}：${item.errorMessage}`)
    return false
  } finally { item.saving = false }
}

const handleTargetChange = async (item, objectKey) => {
  const target = findTarget(objectKey)
  if (!target) return
  switchItemTarget(item, target)
  await saveItem(item)
}

const handleFieldChange = (item) => {
  if (item.status === 'failed') item.status = 'edited'
  item.errorMessage = ''
  clearTimeout(saveTimers.get(item.itemId))
  saveTimers.set(item.itemId, setTimeout(() => {
    saveTimers.delete(item.itemId)
    saveItem(item)
  }, 500))
}

const flushDraftEdits = () => {
  for (const [itemId, timer] of saveTimers) {
    clearTimeout(timer)
    saveTimers.delete(itemId)
    const item = state.items.find((row) => row.itemId === itemId)
    if (item) saveItem(item)
  }
}

const handleVisibilityChange = () => {
  if (document.visibilityState === 'hidden') flushDraftEdits()
}

const confirmItems = async (items) => {
  if (!items.length) return
  for (const item of items) {
    clearTimeout(saveTimers.get(item.itemId))
    saveTimers.delete(item.itemId)
  }
  confirming.value = true
  try {
    const saved = await Promise.all(items.map(saveItem))
    const readyIds = items.filter((_, index) => saved[index]).map((item) => item.itemId)
    if (!readyIds.length) return
    const response = await fileRecognizeApi.confirmImport(readyIds)
    if (response?.code !== 1) throw new Error(response?.message || '批量入库失败')
    applyBatchResults(state.items, response.data?.results || [])
    const successCount = Number(response.data?.successCount || 0)
    const failedCount = Number(response.data?.failedCount || 0)
    if (successCount) ElMessage.success(`成功入库 ${successCount} 项`)
    if (failedCount) ElMessage.warning(`${failedCount} 项入库失败，可修改后重试`)
    if (!state.items.length) {
      localStorage.removeItem(DRAFT_STORAGE_KEY)
      resetAfterComplete()
      showUploadDialog.value = false
    }
  } catch (error) {
    console.error('批量入库失败:', error)
    ElMessage.error(error?.message || '批量入库失败，请重试')
  } finally { confirming.value = false }
}

const handleConfirm = () => confirmItems([...state.items])
const handleRetry = (item) => confirmItems([item])
const resetAfterComplete = () => { Object.assign(state, createImportState()); panelRef.value?.clearFiles?.() }

onMounted(() => {
  updateDeviceMode()
  window.addEventListener('resize', updateDeviceMode)
  window.addEventListener('beforeunload', flushDraftEdits)
  document.addEventListener('visibilitychange', handleVisibilityChange)
})
onBeforeUnmount(() => {
  flushDraftEdits()
  window.removeEventListener('resize', updateDeviceMode)
  window.removeEventListener('beforeunload', flushDraftEdits)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style scoped lang="scss">
.xiaojiang-upload-wrapper { display: inline-flex; align-items: center; }
/* 仅图标入口：正方形 chip，尺寸对齐顶栏其他控件；GIF 原始 400x400 必须约束 */
.xiaojiang-btn {
  gap: 0;
  width: var(--hc-header-control-height);
  min-width: var(--hc-header-control-height);
  padding: 0;
  justify-content: center;
}
.xiaojiang-icon { width: 18px; height: 18px; object-fit: contain; flex-shrink: 0; }
.dialog-header-placeholder { display: none; }
.mobile-drawer-shell { position: relative; height: 100%; display: flex; flex-direction: column; background: #fff; overflow: hidden; }
.mobile-close-btn { position: absolute; top: 16px; right: max(16px, env(safe-area-inset-right, 0px)); z-index: 4; width: 44px; height: 44px; border: 1px solid rgba(255,255,255,.28); border-radius: 50%; background: rgba(23,31,80,.28); color: #fff; display: inline-flex; align-items: center; justify-content: center; cursor: pointer; }
</style>

<style lang="scss">
.xiaojiang-dialog.el-dialog { left: calc(50% + 100px) !important; transform: translateX(-50%) !important; margin-left: 0 !important; padding: 0; overflow: hidden; border-radius: 18px; }
.xiaojiang-dialog .el-dialog__header { display: none; }
.xiaojiang-dialog .el-dialog__body { padding: 0; height: min(88vh, 860px); overflow: hidden; }
.xiaojiang-mobile-drawer .el-drawer__body { padding: 0; overflow: hidden; display: flex; flex-direction: column; }
.xiaojiang-mobile-drawer { border-radius: 18px 18px 0 0; }
.xiaojiang-mobile-drawer .el-drawer { max-width: 100%; }
@media (max-width: 991px) { .xiaojiang-dialog.el-dialog { left: 50% !important; width: calc(100vw - 24px) !important; } }
</style>
