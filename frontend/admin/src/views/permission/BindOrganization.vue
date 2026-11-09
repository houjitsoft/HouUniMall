<template>
  <div class="bind-org-page">
    <div class="bind-org-shell">
      <!-- 左侧说明 -->
      <aside v-if="!isMobile" class="bind-org-brand">
        <div class="bind-org-brand__inner">
          <div class="bind-org-brand__logo">
            <el-icon><OfficeBuilding /></el-icon>
            <span>HouUniMall</span>
          </div>
          <h1 class="bind-org-brand__title">创建您的组织空间</h1>
          <p class="bind-org-brand__desc">
            组织是权限、菜单与业务数据的隔离边界。填写组织名称即可快速开通，其余信息可稍后在设置中补充。
          </p>
          <ul class="bind-org-brand__tips">
            <li><el-icon><CircleCheck /></el-icon> 仅需 1 项必填，约 10 秒完成</li>
            <li><el-icon><CircleCheck /></el-icon> 自动绑定当前登录账号为管理员</li>
            <li><el-icon><CircleCheck /></el-icon> 支持后续在「组织管理」中完善资料</li>
          </ul>
        </div>
      </aside>

      <!-- 右侧表单 -->
      <main class="bind-org-main">
        <div class="bind-org-card">
          <header class="bind-org-header">
            <h2>绑定组织</h2>
            <p>填写组织名称后即可进入管理后台</p>
          </header>

          <!-- 检查中 -->
          <div v-if="checking" class="bind-org-checking">
            <el-icon class="is-loading bind-org-checking__icon"><Loading /></el-icon>
            <p>{{ checkingText }}</p>
          </div>

          <template v-else>
            <!-- 查询失败提示 + 重试 -->
            <el-alert
              v-if="checkFailed"
              type="warning"
              show-icon
              :closable="false"
              class="bind-org-alert"
              title="未能确认您的组织绑定状态"
              description="若您已有组织，可点击下方重新检测；新用户请直接创建组织。"
            />

            <el-form
              ref="createFormRef"
              :model="createForm"
              :rules="createRules"
              class="bind-org-form"
              label-position="top"
              @submit.prevent="handleCreateOrg"
            >
              <!-- 快捷名称 -->
              <div v-if="nameSuggestions.length" class="bind-org-suggestions">
                <span class="bind-org-suggestions__label">快捷填入</span>
                <div class="bind-org-suggestions__list">
                  <button
                    v-for="item in nameSuggestions"
                    :key="item"
                    type="button"
                    class="bind-org-chip"
                    @click="applySuggestion(item)"
                  >
                    {{ item }}
                  </button>
                </div>
              </div>

              <el-form-item label="组织名称" prop="orgName" required>
                <el-input
                  ref="orgNameInputRef"
                  v-model="createForm.orgName"
                  placeholder="例如：上海厚匠科技有限公司"
                  maxlength="100"
                  show-word-limit
                  clearable
                  size="large"
                  @blur="maybeAutoOrgCode"
                />
              </el-form-item>

              <!-- 可选信息折叠 -->
              <el-collapse v-model="optionalOpen" class="bind-org-optional">
                <el-collapse-item name="optional">
                  <template #title>
                    <span class="bind-org-optional__title">
                      补充信息
                      <el-tag size="small" type="info" effect="plain">可选</el-tag>
                    </span>
                  </template>
                  <div class="bind-org-optional__grid">
                    <el-form-item label="组织编码">
                      <el-input
                        v-model="createForm.orgCode"
                        placeholder="留空将自动生成"
                        maxlength="50"
                        clearable
                      >
                        <template #append>
                          <el-button @click="generateOrgCode">生成</el-button>
                        </template>
                      </el-input>
                    </el-form-item>
                    <el-form-item label="联系电话">
                      <el-input
                        v-model="createForm.contactPhone"
                        placeholder="用于接收通知"
                        maxlength="20"
                        clearable
                      />
                    </el-form-item>
                    <el-form-item label="联系邮箱">
                      <el-input
                        v-model="createForm.contactEmail"
                        placeholder="用于接收通知"
                        type="email"
                        clearable
                      />
                    </el-form-item>
                    <el-form-item label="注册地址" class="bind-org-optional__full">
                      <el-input
                        v-model="createForm.address"
                        placeholder="公司注册地址"
                        type="textarea"
                        :rows="2"
                        maxlength="200"
                        show-word-limit
                      />
                    </el-form-item>
                  </div>
                </el-collapse-item>
              </el-collapse>

              <div class="bind-org-actions">
                <el-button
                  v-if="checkFailed"
                  :loading="rechecking"
                  plain
                  @click="handleRecheck"
                >
                  重新检测绑定
                </el-button>
                <el-button plain @click="handleCancel">退出登录</el-button>
                <el-button
                  type="primary"
                  size="large"
                  class="bind-org-submit"
                  :loading="creatingLoading"
                  native-type="submit"
                >
                  创建并进入系统
                </el-button>
              </div>
            </el-form>
          </template>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useWindowSize } from '@vueuse/core'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, OfficeBuilding, CircleCheck } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { organizationService } from '@/api/setting/organization'
import { toAppPath } from '@/utils/appBase'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const { width } = useWindowSize()

const isMobile = computed(() => width.value <= 768)

const creatingLoading = ref(false)
const rechecking = ref(false)
const createFormRef = ref(null)
const orgNameInputRef = ref(null)
const checking = ref(true)
const checkFailed = ref(false)
const checkingText = ref('正在检查组织绑定状态...')
const optionalOpen = ref([])

const createForm = reactive({
  orgName: '',
  orgCode: '',
  contactPhone: '',
  contactEmail: '',
  address: ''
})

const createRules = {
  orgName: [
    { required: true, message: '请输入组织名称', trigger: 'blur' },
    { min: 2, max: 100, message: '组织名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  contactEmail: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const displayName = computed(() => {
  const u = authStore.userInfo || {}
  return u.userNickname || u.userName || u.userMobile || '我的'
})

const nameSuggestions = computed(() => {
  const base = String(displayName.value).trim()
  if (!base || base === '我的') return ['我的工作室', '我的团队']
  return [`${base}的工作室`, `${base}的团队`, `${base}的公司`]
})

const applySuggestion = (name) => {
  createForm.orgName = name
  maybeAutoOrgCode()
  nextTick(() => orgNameInputRef.value?.focus?.())
}

const generateOrgCode = () => {
  const seed = createForm.orgName.trim().replace(/\s+/g, '').slice(0, 8).toUpperCase()
  const suffix = Date.now().toString(36).toUpperCase().slice(-4)
  createForm.orgCode = seed ? `${seed}_${suffix}` : `ORG_${suffix}`
}

const maybeAutoOrgCode = () => {
  if (!createForm.orgCode.trim() && createForm.orgName.trim().length >= 2) {
    generateOrgCode()
  }
}

const prefillFromUserInfo = (userInfo) => {
  if (!userInfo) return
  const phone = userInfo.userMobile || userInfo.phone || ''
  const email = userInfo.userEmail || userInfo.email || ''
  if (phone && !createForm.contactPhone) createForm.contactPhone = String(phone)
  if (email && !createForm.contactEmail) createForm.contactEmail = String(email)
}

const focusOrgName = () => {
  nextTick(() => {
    orgNameInputRef.value?.focus?.()
  })
}

const redirectAfterBind = () => {
  // 归一化：redirect 可能带部署基路径前缀（子路径部署下由外部跳转带入）
  const raw = typeof route.query.redirect === 'string' ? toAppPath(route.query.redirect) : '/dashboard'
  router.replace(raw.startsWith('/') ? raw : '/dashboard')
}

const checkOrgBindingWithRetry = async () => {
  checking.value = true
  checkFailed.value = false
  checkingText.value = '正在检查组织绑定状态...'
  const maxRetries = 3
  const retryInterval = 2000

  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      checkingText.value = attempt > 1
        ? `正在检查组织绑定状态（第 ${attempt} 次）...`
        : '正在检查组织绑定状态...'

      const response = await authStore.getUserInfo()

      if (response && (response.code === 1 || response.code === 200) && response.data) {
        prefillFromUserInfo(response.data)
        if (authStore.hasBoundOrganization(response.data)) {
          redirectAfterBind()
          return
        }
        checking.value = false
        focusOrgName()
        return
      }
    } catch (error) {
      const errorMsg = error.message || String(error)
      const isAuthError = (
        errorMsg.includes('用户未登录') ||
        errorMsg.includes('未登录') ||
        errorMsg.includes('token失效') ||
        errorMsg.includes('Token失效') ||
        errorMsg.includes('认证失败') ||
        errorMsg.includes('认证过期') ||
        errorMsg.includes('token非法')
      ) || (error.response && error.response.status === 401)

      if (isAuthError) {
        checking.value = false
        authStore.logout()
        router.replace('/login')
        return
      }

      if (attempt < maxRetries) {
        checkingText.value = `查询失败，${retryInterval / 1000} 秒后重试...`
        await new Promise((resolve) => setTimeout(resolve, retryInterval))
      } else {
        checking.value = false
        checkFailed.value = true
        prefillFromUserInfo(authStore.userInfo)
        focusOrgName()
        ElMessage.warning('查询用户信息失败，您仍可创建新组织，或尝试重新检测')
      }
    }
  }
}

const handleRecheck = async () => {
  rechecking.value = true
  try {
    const response = await authStore.getUserInfo()
    if (response?.data && authStore.hasBoundOrganization(response.data)) {
      ElMessage.success('已检测到组织绑定，正在进入系统')
      redirectAfterBind()
      return
    }
    ElMessage.info('仍未检测到组织，请创建新组织或联系管理员邀请加入')
    checkFailed.value = true
  } catch {
    ElMessage.error('检测失败，请稍后重试')
  } finally {
    rechecking.value = false
  }
}

onMounted(() => {
  prefillFromUserInfo(authStore.userInfo)
  checkOrgBindingWithRetry()
})

const handleCreateOrg = async () => {
  if (!createFormRef.value) return

  try {
    await createFormRef.value.validate()
    creatingLoading.value = true

    if (!createForm.orgCode.trim()) {
      generateOrgCode()
    }

    const userInfo = authStore.userInfo
    if (!userInfo?.userId && !userInfo?.id) {
      ElMessage.error('用户信息不存在，请重新登录')
      router.push('/login')
      return
    }

    const companyData = {
      companyName: createForm.orgName.trim(),
      orgCode: createForm.orgCode.trim() || undefined,
      contactPhone: createForm.contactPhone.trim() || undefined,
      contactEmail: createForm.contactEmail.trim() || undefined,
      address: createForm.address.trim() || undefined
    }

    const response = await organizationService.submitCompanyRegistration(companyData)

    if (response.code === 1) {
      ElMessage.success('组织创建成功，正在进入系统...')
      await authStore.getUserInfo()
      redirectAfterBind()
    } else {
      ElMessage.error(response.message || '组织创建失败')
    }
  } catch (error) {
    if (error !== false) {
      console.error('创建组织失败:', error)
      ElMessage.error('创建组织失败，请稍后重试')
    }
  } finally {
    creatingLoading.value = false
  }
}

const handleCancel = () => {
  ElMessageBox.confirm('确定退出登录吗？未绑定组织将无法使用系统功能。', '提示', {
    confirmButtonText: '退出',
    cancelButtonText: '继续填写',
    type: 'warning'
  }).then(() => {
    authStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.bind-org-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
}

.bind-org-shell {
  width: 100%;
  max-width: 960px;
  min-height: 520px;
  display: grid;
  grid-template-columns: 1fr 1.1fr;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 24px 64px rgba(15, 23, 42, 0.18);
  background: #fff;
}

.bind-org-brand {
  background: linear-gradient(160deg, #4f46e5 0%, #6366f1 45%, #818cf8 100%);
  color: #fff;
  padding: 48px 40px;
  display: flex;
  align-items: center;
}

.bind-org-brand__logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 28px;
  opacity: 0.95;
}

.bind-org-brand__logo .el-icon {
  font-size: 28px;
}

.bind-org-brand__title {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.35;
  margin: 0 0 14px;
}

.bind-org-brand__desc {
  font-size: 14px;
  line-height: 1.75;
  margin: 0 0 28px;
  color: rgba(255, 255, 255, 0.88);
}

.bind-org-brand__tips {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bind-org-brand__tips li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.92);
}

.bind-org-main {
  padding: 40px 36px;
  display: flex;
  align-items: center;
}

.bind-org-card {
  width: 100%;
}

.bind-org-header {
  margin-bottom: 24px;
}

.bind-org-header h2 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.bind-org-header p {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

.bind-org-alert {
  margin-bottom: 20px;
}

.bind-org-suggestions {
  margin-bottom: 16px;
}

.bind-org-suggestions__label {
  display: block;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 8px;
}

.bind-org-suggestions__list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.bind-org-chip {
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #334155;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.bind-org-chip:hover {
  border-color: #6366f1;
  color: #4f46e5;
  background: #eef2ff;
}

.bind-org-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #334155;
  padding-bottom: 6px;
}

.bind-org-optional {
  margin-bottom: 8px;
  border: none;
}

.bind-org-optional :deep(.el-collapse-item__header) {
  border: none;
  height: 44px;
  font-size: 14px;
  color: #475569;
  background: transparent;
}

.bind-org-optional :deep(.el-collapse-item__wrap) {
  border: none;
}

.bind-org-optional__title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.bind-org-optional__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}

.bind-org-optional__full {
  grid-column: 1 / -1;
}

.bind-org-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #f1f5f9;
}

.bind-org-submit {
  min-width: 160px;
  font-weight: 600;
}

.bind-org-checking {
  text-align: center;
  padding: 48px 16px;
  color: #64748b;
}

.bind-org-checking__icon {
  font-size: 36px;
  color: #6366f1;
  margin-bottom: 12px;
}

@media (max-width: 768px) {
  .bind-org-shell {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .bind-org-main {
    padding: 28px 20px;
  }

  .bind-org-optional__grid {
    grid-template-columns: 1fr;
  }

  .bind-org-actions {
    flex-direction: column-reverse;
    align-items: stretch;
  }

  .bind-org-submit {
    width: 100%;
    min-width: 0;
  }
}
</style>
