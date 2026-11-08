<template>
  <div class="developer-page">
    <header class="page-header">
      <div>
        <h1>开发者中心</h1>
        <p>登记插件发布主体、维护收款账户；审核通过后即可在「插件发布」上传并提交版本。</p>
      </div>
      <el-button :loading="loading" @click="loadAll">刷新</el-button>
    </header>

    <el-alert v-if="!developer.id" type="info" :closable="false" class="tip">
      <template #title>
        尚未登记开发者身份。填写下面的资料提交审核，审核通过后即可作为独立开发者发布插件。
      </template>
    </el-alert>
    <el-alert v-else-if="developer.status !== 1" :type="developer.status === 3 ? 'error' : 'warning'" :closable="false" class="tip">
      <template #title>
        当前状态：<strong>{{ statusMeta.text }}</strong>
        <span v-if="developer.reviewNote">　原因：{{ developer.reviewNote }}</span>
        <span v-if="developer.status === 3">　修改资料后重新提交即可再次审核。</span>
      </template>
    </el-alert>

    <el-card shadow="never" class="block">
      <template #header>开发者资料</template>
      <el-form :model="form" label-width="120px">
        <el-form-item label="开发者名称" required>
          <el-input v-model="form.displayName" maxlength="64" show-word-limit placeholder="对外展示的工作室/个人名称" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="form.contactEmail" placeholder="用于审核通知与结算沟通" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="公司主体">
          <el-input v-model="form.company" placeholder="个人开发者可留空" />
        </el-form-item>
        <el-form-item label="纳税人识别号">
          <el-input v-model="form.taxNo" placeholder="开票用，个人可留空" />
        </el-form-item>
        <el-form-item label="开发者简介">
          <el-input v-model="form.intro" type="textarea" :rows="3" maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveProfile">
            {{ developer.id ? '更新资料' : '提交申请' }}
          </el-button>
          <span v-if="developer.id" class="meta">
            配额：可上架 {{ developer.quotaPlugins ?? '—' }} 个插件／每月 {{ developer.quotaVersionsMonthly ?? '—' }} 个版本
          </span>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="block">
      <template #header>收款账户</template>
      <el-form :model="account" label-width="120px">
        <el-form-item label="结算方式">
          <el-select v-model="account.settleType" style="width: 220px">
            <el-option label="对公银行账户" value="BANK" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="微信" value="WECHAT" />
          </el-select>
        </el-form-item>
        <el-form-item label="账户名称">
          <el-input v-model="account.accountName" placeholder="与银行/支付账户一致" />
        </el-form-item>
        <el-form-item label="收款账号">
          <el-input v-model="account.accountNo" :placeholder="account.accountNoMasked ? `已保存 ${account.accountNoMasked}，留空表示不修改` : '请输入收款账号'" />
        </el-form-item>
        <el-form-item label="开户行">
          <el-input v-model="account.bankName" placeholder="对公账户填写开户支行" />
        </el-form-item>
        <el-form-item>
          <el-button :disabled="!developer.id" :loading="savingAccount" @click="saveAccount">保存账户</el-button>
          <span v-if="!developer.id" class="meta">请先提交开发者申请</span>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="block">
      <template #header>
        开发者审核（运营）
        <el-select v-model="statusFilter" size="small" style="width: 140px; margin-left: 12px" @change="loadDevelopers">
          <el-option label="全部状态" :value="null" />
          <el-option v-for="(meta, key) in DEVELOPER_STATUS" :key="key" :label="meta.text" :value="Number(key)" />
        </el-select>
      </template>
      <el-table :data="developers" size="small" empty-text="暂无开发者">
        <el-table-column prop="displayName" label="开发者" min-width="150" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="developerStatusOf(row.status).type" size="small">{{ developerStatusOf(row.status).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contactEmail" label="邮箱" min-width="160" />
        <el-table-column prop="reviewNote" label="审核意见" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="primary" @click="review(row, true)">通过</el-button>
            <el-button v-if="row.status === 0" link type="danger" @click="review(row, false)">驳回</el-button>
            <el-button v-if="row.status === 1" link type="warning" @click="toggleFreeze(row, true)">冻结</el-button>
            <el-button v-if="row.status === 2" link type="success" @click="toggleFreeze(row, false)">解冻</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  applyDeveloper,
  developerStatusOf,
  freezeDeveloper,
  getMyDeveloper,
  listDevelopers,
  reviewDeveloper,
  saveSellerAccount,
  DEVELOPER_STATUS
} from '@/api/plugin-center/pluginDeveloper'

const loading = ref(false)
const saving = ref(false)
const savingAccount = ref(false)
const developer = ref({})
const developers = ref([])
const statusFilter = ref(null)

const form = ref({
  displayName: '',
  contactEmail: '',
  contactPhone: '',
  company: '',
  taxNo: '',
  intro: ''
})
const account = ref({ settleType: 'BANK', accountName: '', accountNo: '', bankName: '', accountNoMasked: '' })

const statusMeta = computed(() => developerStatusOf(developer.value.status))

const loadDevelopers = async () => {
  try {
    const data = await listDevelopers(statusFilter.value)
    developers.value = data?.list ?? []
  } catch {
    // 无插件管理权限时该接口 403，属预期：普通开发者只看自己的资料
    developers.value = []
  }
}

const loadAll = async () => {
  loading.value = true
  try {
    const data = await getMyDeveloper()
    developer.value = data?.developer || {}
    const acc = data?.account || {}
    account.value = {
      settleType: acc.settleType || 'BANK',
      accountName: acc.accountName || '',
      accountNo: '',
      bankName: acc.bankName || '',
      accountNoMasked: acc.accountNoMasked || ''
    }
    if (developer.value.id) {
      form.value = {
        displayName: developer.value.displayName || '',
        contactEmail: developer.value.contactEmail || '',
        contactPhone: developer.value.contactPhone || '',
        company: developer.value.company || '',
        taxNo: developer.value.taxNo || '',
        intro: developer.value.intro || ''
      }
    }
    await loadDevelopers()
  } catch (error) {
    ElMessage.error(error?.message || '加载开发者信息失败')
  } finally {
    loading.value = false
  }
}

const saveProfile = async () => {
  if (!form.value.displayName.trim()) {
    ElMessage.warning('请填写开发者名称')
    return
  }
  saving.value = true
  try {
    await applyDeveloper({ ...form.value })
    ElMessage.success('已提交，等待审核')
    await loadAll()
  } catch (error) {
    ElMessage.error(error?.message || '提交失败')
  } finally {
    saving.value = false
  }
}

const saveAccount = async () => {
  if (!account.value.accountNo.trim() && !account.value.accountNoMasked) {
    ElMessage.warning('请填写收款账号')
    return
  }
  if (!account.value.accountNo.trim()) {
    ElMessage.info('账号未修改')
    return
  }
  savingAccount.value = true
  try {
    await saveSellerAccount({
      settleType: account.value.settleType,
      accountName: account.value.accountName,
      accountNo: account.value.accountNo,
      bankName: account.value.bankName
    })
    ElMessage.success('收款账户已保存')
    account.value.accountNo = ''
    await loadAll()
  } catch (error) {
    ElMessage.error(error?.message || '保存失败')
  } finally {
    savingAccount.value = false
  }
}

const review = async (row, approve) => {
  let note = ''
  if (!approve) {
    const input = await ElMessageBox.prompt('请填写驳回原因（开发者按此修改后重新提交）', '驳回开发者', {
      inputValidator: (value) => (value && value.trim().length >= 4 ? true : '至少 4 个字')
    }).catch(() => null)
    if (!input) return
    note = input.value
  }
  try {
    await reviewDeveloper({ id: row.id, approve, note })
    ElMessage.success(approve ? '已通过审核' : '已驳回')
    await loadDevelopers()
  } catch (error) {
    ElMessage.error(error?.message || '审核失败')
  }
}

const toggleFreeze = async (row, frozen) => {
  try {
    await freezeDeveloper({ id: row.id, frozen, note: frozen ? '运营冻结' : '运营解冻' })
    ElMessage.success(frozen ? '已冻结' : '已解冻')
    await loadDevelopers()
  } catch (error) {
    ElMessage.error(error?.message || '操作失败')
  }
}

onMounted(loadAll)
</script>

<style scoped>
.developer-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.page-header h1 { margin: 0 0 6px; font-size: 20px; }
.page-header p { margin: 0; color: var(--el-text-color-secondary); font-size: 13px; }
.tip { margin-bottom: 16px; }
.block { margin-bottom: 16px; }
.meta { margin-left: 12px; color: var(--el-text-color-secondary); font-size: 12px; }
</style>
