<template>
  <div class="intl-page" v-loading="pageLoading">
    <header class="intl-hero">
      <div class="intl-hero__main">
        <h1 class="intl-hero__title">国际化设置</h1>
        <p class="intl-hero__desc">货币、语言包与国家/区号统一管理</p>
      </div>
      <div class="intl-hero__switch">
        <span>多语言开关</span>
        <el-switch
          v-model="i18nEnabled"
          :loading="i18nSwitchLoading"
          @change="saveI18nSwitch"
        />
      </div>
    </header>

    <nav class="intl-tabs" role="tablist">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        type="button"
        class="intl-tab"
        :class="{ active: activeTab === tab.key }"
        @click="switchTab(tab.key)"
      >
        <el-icon><component :is="tab.icon" /></el-icon>
        <span>{{ tab.label }}</span>
        <em v-if="tab.count != null">{{ tab.count }}</em>
      </button>
    </nav>

    <!-- 货币 -->
    <section v-show="activeTab === 'currency'" class="intl-panel">
      <div class="panel-toolbar">
        <el-input
          v-model="currencySearchName"
          clearable
          placeholder="搜索货币名称"
          :prefix-icon="Search"
          @keyup.enter="searchCurrency"
        />
        <el-button type="primary" :icon="Search" @click="searchCurrency">查询</el-button>
        <el-button :icon="Refresh" circle @click="loadCurrencyList" />
        <el-button type="primary" :icon="Plus" class="toolbar-add" @click="showAddCurrencyDialog">新增</el-button>
      </div>

      <el-empty v-if="!currencyList.length && !currencyLoading" description="暂无货币数据" />

      <div v-else class="data-cards" v-loading="currencyLoading">
        <article v-for="item in currencyList" :key="item.id" class="data-card">
          <div class="data-card__head">
            <div class="currency-badge">{{ item.currencySymbol || '¤' }}</div>
            <div class="data-card__title">
              <h3>{{ item.remark || item.currencyCode }}</h3>
              <span>{{ item.currencyCode }}</span>
            </div>
            <strong class="rate-value">{{ formatRate(item.exchangeRate) }}</strong>
          </div>
          <div class="data-card__actions">
            <el-button size="small" type="primary" plain @click="editCurrency(item)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="deleteCurrency(item)">删除</el-button>
          </div>
        </article>
      </div>

      <el-pagination
        v-if="currencyPage.total > currencyPage.size"
        v-model:current-page="currencyPage.current"
        v-model:page-size="currencyPage.size"
        :total="currencyPage.total"
        layout="prev, pager, next"
        class="intl-pager"
        @current-change="loadCurrencyList"
      />
    </section>

    <!-- 语言包 -->
    <section v-show="activeTab === 'language'" class="intl-panel">
      <div class="lang-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>系统采用 AI 翻译 + 语言包组合；译文有误可在此人工校正。</span>
      </div>

      <div class="panel-toolbar">
        <el-input
          v-model="languageSearchKeyword"
          clearable
          placeholder="中文关键词"
          :prefix-icon="Search"
          @keyup.enter="searchLanguage"
        />
        <el-select v-model="selectedLanguage" class="lang-select" @change="searchLanguage">
          <el-option
            v-for="opt in LANGUAGE_OPTIONS"
            :key="opt.value"
            :label="`${opt.flag} ${opt.label}`"
            :value="opt.value"
          />
        </el-select>
        <el-button type="primary" :icon="Search" @click="searchLanguage">查询</el-button>
        <el-button :icon="Refresh" circle @click="loadLanguageList" />
      </div>

      <el-empty v-if="!languageList.length && !languageLoading" description="暂无翻译条目" />

      <div v-else class="lang-cards" v-loading="languageLoading">
        <article
          v-for="item in languageList"
          :key="item.id"
          class="lang-card"
          @dblclick="editTranslation(item)"
        >
          <div class="lang-card__cn">{{ item.cnText }}</div>
          <div class="lang-card__arrow">→</div>
          <div class="lang-card__trans">
            {{ item.translation }}
            <el-tag v-if="item.isCorrected" size="small" type="success" effect="light">已校正</el-tag>
          </div>
          <el-button type="primary" link size="small" @click="editTranslation(item)">校正</el-button>
        </article>
      </div>

      <el-pagination
        v-if="languagePage.total > languagePage.size"
        v-model:current-page="languagePage.current"
        v-model:page-size="languagePage.size"
        :total="languagePage.total"
        layout="prev, pager, next"
        class="intl-pager"
        @current-change="loadLanguageList"
      />
    </section>

    <!-- 国家 -->
    <section v-show="activeTab === 'country'" class="intl-panel">
      <div class="panel-toolbar">
        <el-input
          v-model="countrySearchName"
          clearable
          placeholder="搜索国家名称"
          :prefix-icon="Search"
          @keyup.enter="searchCountry"
        />
        <el-button type="primary" :icon="Search" @click="searchCountry">查询</el-button>
        <el-button :icon="Refresh" circle @click="loadCountryList" />
        <el-button type="primary" :icon="Plus" class="toolbar-add" @click="showAddCountryDialog">新增</el-button>
      </div>

      <el-empty v-if="!countryList.length && !countryLoading" description="暂无国家数据" />

      <div v-else class="data-cards country-grid" v-loading="countryLoading">
        <article v-for="item in countryList" :key="item.id" class="data-card country-card">
          <div class="data-card__head">
            <div class="country-avatar">{{ (item.name || '?').charAt(0) }}</div>
            <div class="data-card__title">
              <h3>{{ item.name }}</h3>
              <span>{{ item.phoneCode }}</span>
            </div>
            <el-tag :type="item.isCommon ? 'success' : 'info'" size="small" effect="light">
              {{ item.isCommon ? '常用' : '非常用' }}
            </el-tag>
          </div>
          <div class="data-card__actions">
            <el-button size="small" type="primary" plain @click="editCountry(item)">编辑</el-button>
            <el-button
              v-if="item.deleteType !== 2"
              size="small"
              type="danger"
              plain
              @click="deleteCountry(item)"
            >
              删除
            </el-button>
          </div>
        </article>
      </div>
    </section>

    <!-- 货币弹窗 -->
    <el-dialog
      v-model="currencyDialogVisible"
      :title="currencyForm.id ? '编辑货币' : '新增货币'"
      width="92%"
      class="intl-dialog"
      destroy-on-close
      @close="resetCurrencyForm"
    >
      <el-form ref="currencyFormRef" :model="currencyForm" :rules="currencyRules" label-position="top">
        <el-form-item label="货币名称" prop="remark">
          <el-input v-model="currencyForm.remark" placeholder="如：人民币" />
        </el-form-item>
        <el-form-item label="货币编码" prop="currencyCode">
          <el-input v-model="currencyForm.currencyCode" placeholder="CNY" maxlength="8" />
        </el-form-item>
        <el-form-item label="货币符号" prop="currencySymbol">
          <el-input v-model="currencyForm.currencySymbol" placeholder="¥" maxlength="8" />
        </el-form-item>
        <el-form-item label="汇率" prop="exchangeRate">
          <el-input-number v-model="currencyForm.exchangeRate" :min="0" :precision="4" :step="0.0001" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="currencyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="currencySaveLoading" @click="saveCurrency">保存</el-button>
      </template>
    </el-dialog>

    <!-- 译文弹窗 -->
    <el-dialog v-model="translationDialogVisible" title="校正译文" width="92%" class="intl-dialog" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="中文原文">
          <el-input v-model="translationForm.cnText" disabled />
        </el-form-item>
        <el-form-item label="当前译文">
          <el-input v-model="translationForm.originalText" disabled type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="修正译文" required>
          <el-input v-model="translationForm.correctedText" type="textarea" :rows="3" placeholder="输入校正后的译文" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="translationDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="translationSaveLoading" @click="saveTranslation">提交</el-button>
      </template>
    </el-dialog>

    <!-- 国家弹窗 -->
    <el-dialog
      v-model="countryDialogVisible"
      :title="countryForm.id ? '编辑国家' : '新增国家'"
      width="92%"
      class="intl-dialog"
      destroy-on-close
      @close="resetCountryForm"
    >
      <el-form ref="countryFormRef" :model="countryForm" :rules="countryRules" label-position="top">
        <el-form-item label="国家名称" prop="name">
          <el-input v-model="countryForm.name" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="电话区号" prop="phoneCode">
          <el-input v-model="countryForm.phoneCode" placeholder="+86" />
        </el-form-item>
        <el-form-item label="设为常用">
          <el-switch v-model="countryForm.isCommon" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="countryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="countrySaveLoading" @click="saveCountry">保存</el-button>
      </template>
    </el-dialog>

    <button
      v-if="activeTab !== 'language'"
      type="button"
      class="mobile-fab"
      aria-label="新增"
      @click="activeTab === 'currency' ? showAddCurrencyDialog() : showAddCountryDialog()"
    >
      <el-icon><Plus /></el-icon>
    </button>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Coin,
  InfoFilled,
  Location,
  Notebook,
  Plus,
  Refresh,
  Search
} from '@element-plus/icons-vue'
import { websiteSettingApi } from '@/api/setting/website'
import {
  internationalApi,
  isApiSuccess,
  LANGUAGE_OPTIONS
} from '@/api/setting/international'

const tabs = computed(() => [
  { key: 'currency', label: '货币', icon: Coin, count: currencyPage.total || currencyList.value.length },
  { key: 'language', label: '语言包', icon: Notebook, count: languagePage.total || languageList.value.length },
  { key: 'country', label: '国家', icon: Location, count: countryList.value.length }
])

const activeTab = ref('currency')
const pageLoading = ref(false)
const i18nEnabled = ref(localStorage.getItem('internationalizationEnabled') !== '0')
const i18nSwitchLoading = ref(false)

function switchTab(key) {
  activeTab.value = key
}

async function saveI18nSwitch(val) {
  i18nSwitchLoading.value = true
  try {
    const res = await websiteSettingApi.saveWebsiteSwitches({
      internationalizationEnabled: !!val,
      multiOrganizationEnabled: localStorage.getItem('multiOrganizationEnabled') !== '0'
    })
    if (isApiSuccess(res)) {
      localStorage.setItem('internationalizationEnabled', val ? '1' : '0')
      ElMessage.success(val ? '已开启多语言' : '已关闭多语言')
    } else {
      i18nEnabled.value = !val
      ElMessage.error(res?.message || '保存失败')
    }
  } catch {
    i18nEnabled.value = !val
    ElMessage.error('保存失败')
  } finally {
    i18nSwitchLoading.value = false
  }
}

function formatRate(rate) {
  const n = Number(rate)
  return Number.isFinite(n) ? n.toFixed(4) : '—'
}

// ==================== 货币 ====================
const currencySearchName = ref('')
const currencyList = ref([])
const currencyLoading = ref(false)
const currencyPage = reactive({ current: 1, size: 10, total: 0 })
const currencyDialogVisible = ref(false)
const currencyFormRef = ref()
const currencySaveLoading = ref(false)
const currencyForm = reactive({
  id: null,
  remark: '',
  currencyCode: '',
  currencySymbol: '',
  exchangeRate: 1
})
const currencyRules = {
  remark: [{ required: true, message: '请输入货币名称', trigger: 'blur' }],
  currencyCode: [{ required: true, message: '请输入货币编码', trigger: 'blur' }],
  currencySymbol: [{ required: true, message: '请输入货币符号', trigger: 'blur' }],
  exchangeRate: [{ required: true, message: '请输入汇率', trigger: 'blur' }]
}

async function loadCurrencyList() {
  currencyLoading.value = true
  try {
    const { list, total } = await internationalApi.listCurrency({
      remark: currencySearchName.value,
      page: currencyPage.current,
      size: currencyPage.size
    })
    currencyList.value = list
    currencyPage.total = total
  } catch {
    ElMessage.error('加载货币列表失败')
  } finally {
    currencyLoading.value = false
  }
}

function searchCurrency() {
  currencyPage.current = 1
  loadCurrencyList()
}

function showAddCurrencyDialog() {
  resetCurrencyForm()
  currencyDialogVisible.value = true
}

function editCurrency(row) {
  Object.assign(currencyForm, {
    id: row.id,
    remark: row.remark || '',
    currencyCode: row.currencyCode || '',
    currencySymbol: row.currencySymbol || '',
    exchangeRate: row.exchangeRate ?? 1
  })
  currencyDialogVisible.value = true
}

async function saveCurrency() {
  const valid = await currencyFormRef.value?.validate().catch(() => false)
  if (!valid) return
  currencySaveLoading.value = true
  try {
    const res = await internationalApi.saveCurrency({ ...currencyForm })
    if (isApiSuccess(res)) {
      ElMessage.success(currencyForm.id ? '编辑成功' : '添加成功')
      currencyDialogVisible.value = false
      loadCurrencyList()
    } else {
      ElMessage.error(res?.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    currencySaveLoading.value = false
  }
}

function deleteCurrency(row) {
  ElMessageBox.confirm(`确定删除「${row.remark || row.currencyCode}」？`, '删除确认', { type: 'warning' })
    .then(async () => {
      const res = await internationalApi.deleteCurrency(row.id)
      if (isApiSuccess(res)) {
        ElMessage.success('删除成功')
        loadCurrencyList()
      } else {
        ElMessage.error(res?.message || '删除失败')
      }
    })
    .catch(() => {})
}

function resetCurrencyForm() {
  Object.assign(currencyForm, { id: null, remark: '', currencyCode: '', currencySymbol: '', exchangeRate: 1 })
  currencyFormRef.value?.clearValidate()
}

// ==================== 语言包 ====================
const languageSearchKeyword = ref('')
const selectedLanguage = ref('en')
const languageList = ref([])
const languageLoading = ref(false)
const languagePage = reactive({ current: 1, size: 20, total: 0 })
const translationDialogVisible = ref(false)
const translationSaveLoading = ref(false)
const translationForm = reactive({ id: null, cnText: '', originalText: '', correctedText: '' })

async function loadLanguageList() {
  languageLoading.value = true
  try {
    const { list, total } = await internationalApi.listLanguage({
      keyword: languageSearchKeyword.value,
      languageCode: selectedLanguage.value,
      page: languagePage.current,
      size: languagePage.size
    })
    languageList.value = list
    languagePage.total = total
  } catch {
    ElMessage.error('加载语言包失败')
  } finally {
    languageLoading.value = false
  }
}

function searchLanguage() {
  languagePage.current = 1
  loadLanguageList()
}

function editTranslation(item) {
  Object.assign(translationForm, {
    id: item.id,
    cnText: item.cnText,
    originalText: item.originalTranslation || item.translation,
    correctedText: item.correctedTranslation || item.translation
  })
  translationDialogVisible.value = true
}

async function saveTranslation() {
  if (!translationForm.correctedText?.trim()) {
    ElMessage.warning('请输入修正后的译文')
    return
  }
  translationSaveLoading.value = true
  try {
    const res = await internationalApi.correctTranslation({
      id: translationForm.id,
      languageCode: selectedLanguage.value,
      correctedText: translationForm.correctedText.trim()
    })
    if (isApiSuccess(res)) {
      ElMessage.success('校正成功')
      translationDialogVisible.value = false
      loadLanguageList()
    } else {
      ElMessage.error(res?.message || '校正失败')
    }
  } catch {
    ElMessage.error('校正失败')
  } finally {
    translationSaveLoading.value = false
  }
}

// ==================== 国家 ====================
const countrySearchName = ref('')
const countryList = ref([])
const countryLoading = ref(false)
const countryDialogVisible = ref(false)
const countryFormRef = ref()
const countrySaveLoading = ref(false)
const countryForm = reactive({ id: null, name: '', phoneCode: '', isCommon: true })
const countryRules = {
  name: [
    { required: true, message: '请输入国家名称', trigger: 'blur' },
    { max: 30, message: '不超过 30 字', trigger: 'blur' }
  ],
  phoneCode: [{ required: true, message: '请输入电话区号', trigger: 'blur' }]
}

async function loadCountryList() {
  countryLoading.value = true
  try {
    const { list } = await internationalApi.listCountry({ name: countrySearchName.value, size: 200 })
    countryList.value = list
  } catch {
    ElMessage.error('加载国家列表失败')
  } finally {
    countryLoading.value = false
  }
}

function searchCountry() {
  loadCountryList()
}

function showAddCountryDialog() {
  resetCountryForm()
  countryDialogVisible.value = true
}

function editCountry(row) {
  Object.assign(countryForm, {
    id: row.id,
    name: row.name,
    phoneCode: row.phoneCode,
    isCommon: row.isCommon
  })
  countryDialogVisible.value = true
}

async function saveCountry() {
  const valid = await countryFormRef.value?.validate().catch(() => false)
  if (!valid) return
  countrySaveLoading.value = true
  try {
    const res = await internationalApi.saveCountry({ ...countryForm })
    if (isApiSuccess(res)) {
      ElMessage.success(countryForm.id ? '编辑成功' : '添加成功')
      countryDialogVisible.value = false
      loadCountryList()
    } else {
      ElMessage.error(res?.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    countrySaveLoading.value = false
  }
}

function deleteCountry(row) {
  ElMessageBox.confirm(`确定删除「${row.name}」？`, '删除确认', { type: 'warning' })
    .then(async () => {
      const res = await internationalApi.deleteCountry(row.id)
      if (isApiSuccess(res)) {
        ElMessage.success('删除成功')
        loadCountryList()
      } else {
        ElMessage.error(res?.message || '删除失败')
      }
    })
    .catch(() => {})
}

function resetCountryForm() {
  Object.assign(countryForm, { id: null, name: '', phoneCode: '', isCommon: true })
  countryFormRef.value?.clearValidate()
}

watch(activeTab, (tab) => {
  if (tab === 'currency' && !currencyList.value.length) loadCurrencyList()
  if (tab === 'language' && !languageList.value.length) loadLanguageList()
  if (tab === 'country' && !countryList.value.length) loadCountryList()
})

onMounted(async () => {
  pageLoading.value = true
  try {
    const res = await websiteSettingApi.getWebsiteSwitches()
    if (isApiSuccess(res)) {
      i18nEnabled.value = res.data?.internationalizationEnabled !== false
    }
    await loadCurrencyList()
  } finally {
    pageLoading.value = false
  }
})
</script>

<style scoped lang="scss">
.intl-page {
  padding: 16px;
  padding-bottom: calc(80px + env(safe-area-inset-bottom, 0px));
  max-width: 980px;
  margin: 0 auto;
}

.intl-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 20px;
  border-radius: 16px;
  background: linear-gradient(135deg, #7c3aed 0%, #6366f1 45%, #2563eb 100%);
  color: #fff;
  margin-bottom: 16px;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.28);

  &__title {
    margin: 0 0 6px;
    font-size: 22px;
    font-weight: 700;
  }

  &__desc {
    margin: 0;
    font-size: 13px;
    opacity: 0.92;
  }

  &__switch {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 8px;
    font-size: 12px;
    flex-shrink: 0;
  }
}

.intl-tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 4px;
  margin-bottom: 16px;

  &::-webkit-scrollbar {
    display: none;
  }
}

.intl-tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  border: 1px solid #e4e7ed;
  border-radius: 999px;
  background: #fff;
  color: #606266;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;

  em {
    font-style: normal;
    font-size: 11px;
    background: #f2f3f5;
    padding: 1px 6px;
    border-radius: 999px;
  }

  &.active {
    background: #6366f1;
    border-color: #6366f1;
    color: #fff;

    em {
      background: rgba(255, 255, 255, 0.2);
      color: #fff;
    }
  }
}

.intl-panel {
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: none; }
}

.panel-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
  align-items: center;

  .el-input {
    flex: 1;
    min-width: 140px;
  }

  .lang-select {
    width: 140px;
  }

  .toolbar-add {
    margin-left: auto;
  }
}

.lang-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 14px;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 12px;
  font-size: 13px;
  color: #92400e;
  margin-bottom: 14px;
}

.data-cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.data-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 14px;
  padding: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);

  &__head {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
  }

  &__title {
    flex: 1;
    min-width: 0;

    h3 {
      margin: 0 0 2px;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }

    span {
      font-size: 12px;
      color: #909399;
    }
  }

  &__actions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}

.currency-badge {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #eef2ff, #e0e7ff);
  color: #4f46e5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  flex-shrink: 0;
}

.rate-value {
  font-size: 15px;
  color: #303133;
  font-variant-numeric: tabular-nums;
}

.country-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #f0f9ff;
  color: #0284c7;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  flex-shrink: 0;
}

.lang-cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.lang-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 14px;
  padding: 14px;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px 12px;
  align-items: start;

  &__cn {
    grid-column: 1 / -1;
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    line-height: 1.4;
  }

  &__arrow {
    display: none;
  }

  &__trans {
    grid-column: 1;
    font-size: 14px;
    color: #606266;
    line-height: 1.5;
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
  }
}

.intl-pager {
  margin-top: 16px;
  justify-content: center;
}

.mobile-fab {
  display: flex;
  position: fixed;
  right: 20px;
  bottom: calc(20px + env(safe-area-inset-bottom, 0px));
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #6366f1, #2563eb);
  color: #fff;
  font-size: 24px;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.35);
  z-index: 90;
  cursor: pointer;
}

@media (min-width: 992px) {
  .intl-page {
    padding: 20px 24px;
    padding-bottom: 24px;
  }

  .mobile-fab {
    display: none;
  }

  .panel-toolbar .toolbar-add {
    margin-left: 0;
  }

  .country-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .lang-card {
    grid-template-columns: 1fr auto 1fr auto;
    align-items: center;

    &__cn {
      grid-column: 1;
    }

    &__arrow {
      display: block;
      color: #c0c4cc;
    }

    &__trans {
      grid-column: 3;
    }
  }
}

@media (max-width: 991px) {
  .panel-toolbar .toolbar-add {
    display: none;
  }

  .intl-hero {
    flex-direction: column;
  }

  .intl-hero__switch {
    flex-direction: row;
    align-items: center;
    width: 100%;
    justify-content: space-between;
    padding-top: 8px;
    border-top: 1px solid rgba(255, 255, 255, 0.2);
  }
}
</style>
