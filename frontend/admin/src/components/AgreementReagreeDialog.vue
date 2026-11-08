<template>
  <el-dialog
    v-model="visible"
    title="协议确认"
    width="620px"
    :close-on-click-modal="false"
    :close-on-press-escape="!forceRequired"
    :show-close="!forceRequired"
    append-to-body
    class="agreement-reagree-dialog"
  >
    <div class="reagree-tip">
      以下协议已更新，请逐份<span class="reagree-strong">点开阅读至文末</span>后勾选同意：
    </div>

    <div class="reagree-list">
      <div v-for="item in pending" :key="item.code" class="reagree-item" :class="{ 'is-read': isRead(item) }">
        <el-checkbox v-model="checked[item.code]" :disabled="!isRead(item)">
          <span class="reagree-name" @click.prevent="open(item)">
            我已阅读并同意
            <el-link type="primary" :underline="false">《{{ item.name }}》</el-link>
            <span class="reagree-version">（{{ item.versionNo }}）</span>
            <el-tag v-if="isRequired(item)" size="small" type="danger" effect="plain">必选</el-tag>
          </span>
        </el-checkbox>
        <div v-if="!isRead(item)" class="reagree-read-hint">
          请点击《{{ item.name }}》阅读全文，滚动至文末后即可勾选
        </div>
        <div v-if="item.agreedVersionNo" class="reagree-history">
          你此前同意的版本：{{ item.agreedVersionNo }}
        </div>
        <div v-if="item.changeNote" class="reagree-history">变更说明：{{ item.changeNote }}</div>
      </div>
    </div>

    <div v-if="error" class="reagree-error">{{ error }}</div>

    <template #footer>
      <el-button v-if="!forceRequired" @click="handleLater">稍后再说</el-button>
      <el-button type="primary" :loading="submitting" @click="handleConfirm">确认并继续</el-button>
    </template>
  </el-dialog>

  <!-- 协议正文阅读弹窗：滚动到底部才允许确认已读 -->
  <el-dialog
    v-model="readerVisible"
    :title="readerTitle"
    width="720px"
    top="6vh"
    :close-on-click-modal="false"
    append-to-body
    class="agreement-reader-dialog"
    @closed="onReaderClosed"
  >
    <div ref="readerBodyRef" class="reader-body" @scroll="onReaderScroll">{{ readerContent }}</div>
    <div class="reader-footer-hint" :class="{ 'is-bottom': reachedBottom }">
      <template v-if="reachedBottom">已读至文末 ✓</template>
      <template v-else>阅读进度 {{ readerProgress }}%，请滚动至文末阅读完整内容</template>
    </div>
    <template #footer>
      <el-button @click="readerVisible = false">稍后再读</el-button>
      <el-button type="primary" :disabled="!reachedBottom" @click="finishRead">我已阅读完毕</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 协议版本更新后的补签弹窗。
 *
 * 挂载在 Layout 上，登录进入后台后自动检查「当前生效版本」与「用户已同意版本」的差异；
 * 存在差异时弹窗要求重新阅读并勾选。含必选协议时弹窗不可关闭，避免绕过。
 *
 * 阅读强制（2026-09-18）：勾选框在「点开正文并滚动到底部」之前禁用，
 * 防止不读条款直接勾选确认。滚动到底判定：scrollTop + clientHeight ≥ scrollHeight - 4；
 * 正文不足一屏时视为已读完。
 */
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { agreementApi } from '@/api/setting/agreement'
import { resolveAgreementContent, resetAgreementCache } from '@/utils/showLegalAgreement'

const visible = ref(false)
const pending = ref([])
const checked = reactive({})
/** 已读状态：code -> true（点开正文并读到底后置位，置位后勾选框才可用） */
const readMap = reactive({})
const error = ref('')
const submitting = ref(false)

// ================= 阅读弹窗状态 =================
const readerVisible = ref(false)
const readerTitle = ref('')
const readerContent = ref('')
const readerBodyRef = ref(null)
const reachedBottom = ref(false)
const readerProgress = ref(0)
/** 当前正在阅读的协议编码 */
let readingCode = ''

/** 存在未同意的必选协议时不允许关闭弹窗 */
const forceRequired = computed(() => pending.value.some((item) => isRequired(item) && !checked[item.code]))
const isRequired = (item) => Number(item?.required) === 1

const isInstallerAgreement = (item) =>
  item?.code === 'OPEN_SOURCE_EULA' || item?.scene === 'install'

const isRead = (item) => readMap[item.code] === true

const open = async (item) => {
  readingCode = item.code
  readerTitle.value = item.versionNo ? `${item.name}（${item.versionNo}）` : item.name
  readerContent.value = ''
  reachedBottom.value = false
  readerProgress.value = 0
  readerVisible.value = true
  try {
    readerContent.value = await resolveAgreementContent(item)
  } catch (e) {
    readerContent.value = `《${item.name}》正文加载失败，请关闭后重试`
  }
  // 正文不足一屏（无滚动条）时视为已读完
  nextTick(checkNoOverflow)
}

const checkNoOverflow = () => {
  const el = readerBodyRef.value
  if (!el) return
  if (el.scrollHeight <= el.clientHeight + 4) {
    reachedBottom.value = true
    readerProgress.value = 100
  }
}

const onReaderScroll = (event) => {
  const el = event.target
  if (!el) return
  const maxScroll = el.scrollHeight - el.clientHeight
  if (maxScroll <= 0) {
    reachedBottom.value = true
    readerProgress.value = 100
    return
  }
  const ratio = Math.min(100, Math.round((el.scrollTop / maxScroll) * 100))
  readerProgress.value = ratio
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - 4) {
    reachedBottom.value = true
  }
}

const finishRead = () => {
  if (!reachedBottom.value || !readingCode) return
  readMap[readingCode] = true
  checked[readingCode] = true
  readerVisible.value = false
}

const onReaderClosed = () => {
  // 关闭后把滚动复位，下次打开重新判定
  reachedBottom.value = false
  readerProgress.value = 0
  nextTick(() => {
    const el = readerBodyRef.value
    if (el) el.scrollTop = 0
  })
}

const load = async () => {
  try {
    const res = await agreementApi.pending()
    if (!res || res.code !== 1 || !res.data) {
      return
    }
    // 安装 EULA 只由安装者在部署阶段确认。即使旧后端误返回，也不得展示给站内用户。
    const list = Array.isArray(res.data.pending)
      ? res.data.pending.filter((item) => !isInstallerAgreement(item))
      : []
    if (list.length === 0) {
      return
    }
    pending.value = list
    Object.keys(checked).forEach((key) => {
      delete checked[key]
    })
    Object.keys(readMap).forEach((key) => {
      delete readMap[key]
    })
    list.forEach((item) => {
      checked[item.code] = false
      readMap[item.code] = false
    })
    visible.value = true
  } catch (e) {
    // 协议服务不可用时不打扰用户，仅记录日志
    console.warn('[AgreementReagreeDialog] 查询待同意协议失败', e?.message || e)
  }
}

const handleLater = () => {
  visible.value = false
}

const handleConfirm = async () => {
  const unread = pending.value.filter((item) => !isRead(item))
  if (unread.length > 0) {
    error.value = `请先点开阅读《${unread.map((item) => item.name).join('》《')}》并读至文末`
    return
  }
  const missing = pending.value.filter(isRequired).filter((item) => !checked[item.code])
  if (missing.length > 0) {
    error.value = `请阅读并勾选同意《${missing.map((item) => item.name).join('》《')}》`
    return
  }
  const consents = pending.value
    .filter((item) => checked[item.code])
    .map((item) => ({ code: item.code, versionNo: item.versionNo }))
  if (consents.length === 0) {
    error.value = '请至少勾选一项协议'
    return
  }
  submitting.value = true
  error.value = ''
  try {
    const res = await agreementApi.reagree(consents)
    if (res && res.code === 1) {
      // 落库成功后立刻再查一次：防止后端假成功（未写库仍返回成功）导致下次登录再弹
      try {
        const verify = await agreementApi.pending()
        const still = Array.isArray(verify?.data?.pending)
          ? verify.data.pending.filter((item) => !isInstallerAgreement(item))
          : []
        if (verify?.code === 1 && still.length > 0) {
          error.value = '同意未生效，请刷新页面后重试'
          pending.value = still
          return
        }
      } catch (_) {
        // 复核失败不阻断：以 reagree 成功为准，下次进页会再查
      }
      ElMessage.success('已记录你的同意')
      visible.value = false
      pending.value = []
      // 协议清单已变化，清缓存以便下次重新拉取
      resetAgreementCache()
    } else {
      error.value = res?.message || '保存失败，请稍后重试'
    }
  } catch (e) {
    error.value = e?.message || '保存失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.reagree-tip {
  font-size: 14px;
  color: #606266;
  line-height: 1.7;
  margin-bottom: 12px;
}

.reagree-strong {
  color: #409eff;
  font-weight: 600;
}

.reagree-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 46vh;
  overflow: auto;
}

.reagree-item {
  padding: 10px 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fafafa;
}

.reagree-item.is-read {
  background: #f5faff;
  border-color: #d9ecff;
}

.reagree-name {
  font-size: 13px;
  color: #303133;
}

.reagree-version {
  color: #909399;
  font-size: 12px;
}

.reagree-read-hint {
  margin-top: 4px;
  margin-left: 24px;
  font-size: 12px;
  color: #e6a23c;
  line-height: 1.6;
}

.reagree-history {
  margin-top: 4px;
  margin-left: 24px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}

.reagree-error {
  margin-top: 10px;
  font-size: 12px;
  color: #f56c6c;
}

.reader-body {
  max-height: 62vh;
  overflow: auto;
  white-space: pre-wrap;
  line-height: 1.8;
  font-size: 14px;
  color: #303133;
  padding: 4px 8px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fafafa;
}

.reader-footer-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #e6a23c;
  text-align: right;
}

.reader-footer-hint.is-bottom {
  color: #67c23a;
}
</style>
