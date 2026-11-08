<template>
  <div class="physical-store-page">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="商家是经营主体，门店是商家下属的经营与履约节点"
      description="每个门店独立维护营业状态、骑手配送和收银系统（POS）。"
    />

    <el-card class="!border-none mt-4" shadow="never">
      <el-form :model="query" :inline="true" class="mb-[-16px]">
        <el-form-item label="门店名称">
          <el-input v-model="query.storeName" clearable placeholder="请输入门店名称" @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="所属商家">
          <el-select v-model="query.merchantId" clearable placeholder="全部商家" filterable class="w-[220px]">
            <el-option v-for="item in merchants" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="营业状态">
          <el-select v-model="query.businessStatus" class="w-[130px]">
            <el-option label="全部" value="" />
            <el-option label="营业中" :value="1" />
            <el-option label="已停业" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="!border-none mt-4" shadow="never">
      <div class="store-toolbar">
        <div>
          <div class="toolbar-title">门店列表</div>
          <div class="toolbar-subtitle">共 {{ pagination.total }} 家门店</div>
        </div>
        <el-button type="primary" @click="createVisible = true">+ 新增门店</el-button>
      </div>

      <el-table v-loading="loading" :data="list" size="large" class="mt-4">
        <el-table-column label="门店" min-width="210">
          <template #default="{ row }">
            <div class="store-name">{{ row.storeName }}</div>
            <div class="store-no">{{ row.storeNo }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="merchantName" label="所属商家" min-width="150" />
        <el-table-column label="联系方式" min-width="150">
          <template #default="{ row }">
            <div>{{ row.contactPerson || '-' }}</div>
            <div class="minor">{{ row.contactPhone || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="门店地址" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">{{ fullAddress(row) || '-' }}</template>
        </el-table-column>
        <el-table-column label="营业状态" width="110" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.businessStatus"
              :active-value="1"
              :inactive-value="0"
              @change="toggle(row, 'business_status')"
            />
          </template>
        </el-table-column>
        <el-table-column label="骑手配送" width="120" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.riderDeliveryEnabled"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="已启用"
              inactive-text="未启用"
              @change="toggle(row, 'rider_delivery_enabled')"
            />
          </template>
        </el-table-column>
        <el-table-column label="收银系统" width="120" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.posEnabled"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="已启用"
              inactive-text="未启用"
              @change="toggle(row, 'pos_enabled')"
            />
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !list.length" description="暂无门店，请先在所属商家下新增门店" />
      <div class="flex justify-end mt-4">
        <el-pagination
          v-model:current-page="pagination.pageNo"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 15, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="load"
          @current-change="load"
        />
      </div>
    </el-card>

    <el-dialog v-model="createVisible" title="新增门店" width="640px" destroy-on-close @open="loadAccounts" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="商家账号" prop="accountMode">
          <el-radio-group v-model="form.accountMode" @change="formRef?.clearValidate(['platformUserId', 'accountIdentifier', 'accountPassword', 'accountPasswordAgain'])">
            <el-radio value="existing">选择已有账号</el-radio>
            <el-radio value="new">注册新账号</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.accountMode === 'existing'" label="统一账号" prop="platformUserId">
          <el-select v-model="form.platformUserId" filterable remote clearable class="w-full" placeholder="搜索手机号或邮箱" :remote-method="searchAccounts" :loading="accountLoading">
            <el-option v-for="item in accounts" :key="item.userId" :label="`${item.displayName} (${item.loginAccount})`" :value="item.userId">
              <div class="account-option">
                <span>{{ item.displayName }}</span>
                <span class="minor">{{ item.loginAccount }}<template v-if="item.grantedMerchantName"> · 已授权：{{ item.grantedMerchantName }}</template></span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <template v-else>
          <el-form-item label="手机号或邮箱" prop="accountIdentifier">
            <el-input v-model="form.accountIdentifier" autocomplete="off" placeholder="注册后可用手机号或邮箱登录" />
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="登录密码" prop="accountPassword">
                <el-input v-model="form.accountPassword" type="password" show-password autocomplete="new-password" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="确认密码" prop="accountPasswordAgain">
                <el-input v-model="form.accountPasswordAgain" type="password" show-password autocomplete="new-password" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-alert class="mb-4" type="info" :closable="false" title="新账号会注册为平台账号，并自动获得所选商家的管理权限" />
        </template>
        <el-form-item label="所属商家" prop="merchantId">
          <el-select v-model="form.merchantId" filterable class="w-full" placeholder="请选择商家">
            <el-option v-for="item in merchants" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="门店名称" prop="storeName">
              <el-input v-model="form.storeName" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="门店编号">
              <el-input v-model="form.storeNo" maxlength="32" placeholder="不填则自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人">
              <el-input v-model="form.contactPerson" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.contactPhone" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="8"><el-form-item label="省"><el-input v-model="form.province" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="市"><el-input v-model="form.city" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="区县"><el-input v-model="form.district" /></el-form-item></el-col>
          <el-col :span="24">
            <el-form-item label="详细地址"><el-input v-model="form.address" maxlength="255" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="营业中"><el-switch v-model="form.businessStatus" :active-value="1" :inactive-value="0" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="骑手配送"><el-switch v-model="form.riderDeliveryEnabled" :active-value="1" :inactive-value="0" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="收银 POS"><el-switch v-model="form.posEnabled" :active-value="1" :inactive-value="0" /></el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="createStore">确认新增</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  physicalStoreAccountOptions,
  physicalStoreCreate,
  physicalStoreList,
  physicalStoreMerchantOptions,
  physicalStoreToggle
} from '@/api/mall/physicalStore'
import {
  buildPhysicalStoreListParams,
  buildPhysicalStorePayload,
  normalizePhysicalStorePage
} from './physicalStoreModel'

const loading = ref(false)
const creating = ref(false)
const accountLoading = ref(false)
const createVisible = ref(false)
const formRef = ref()
const list = ref([])
const merchants = ref([])
const accounts = ref([])
const pagination = reactive({ pageNo: 1, pageSize: 15, total: 0 })
const query = reactive({ storeName: '', merchantId: '', businessStatus: '', riderDeliveryEnabled: '', posEnabled: '' })

const defaults = () => ({
  merchantId: '', accountMode: 'existing', platformUserId: '', accountIdentifier: '',
  accountPassword: '', accountPasswordAgain: '', storeNo: '', storeName: '', contactPerson: '', contactPhone: '',
  province: '', city: '', district: '', address: '', businessStatus: 1,
  riderDeliveryEnabled: 0, posEnabled: 0
})
const form = reactive(defaults())
const rules = {
  merchantId: [{ required: true, message: '请选择所属商家', trigger: 'change' }],
  accountMode: [{ required: true, message: '请选择账号来源', trigger: 'change' }],
  platformUserId: [{ required: true, message: '请选择已有账号', trigger: 'change' }],
  accountIdentifier: [
    { required: true, message: '请输入手机号或邮箱', trigger: 'blur' },
    { pattern: /^(1\d{10}|[^\s@]+@[^\s@]+\.[^\s@]+)$/, message: '请输入正确的手机号或邮箱', trigger: 'blur' }
  ],
  accountPassword: [{ required: true, min: 6, max: 32, message: '密码长度为6至32个字符', trigger: 'blur' }],
  accountPasswordAgain: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (_rule, value, callback) => value === form.accountPassword ? callback() : callback(new Error('两次密码不一致')), trigger: 'blur' }
  ],
  storeName: [{ required: true, message: '请输入门店名称', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const res = await physicalStoreList(buildPhysicalStoreListParams(query, pagination))
    if (res?.code !== 1) throw new Error(res?.msg || res?.message || '门店列表加载失败')
    const page = normalizePhysicalStorePage(res.data)
    list.value = page.list
    pagination.total = page.total
  } catch (error) {
    list.value = []
    pagination.total = 0
    ElMessage.error(error?.message || '门店列表加载失败')
  } finally {
    loading.value = false
  }
}

async function loadMerchants() {
  const res = await physicalStoreMerchantOptions()
  merchants.value = res?.code === 1 && Array.isArray(res.data) ? res.data : []
}

async function loadAccounts(keyword = '') {
  accountLoading.value = true
  try {
    const res = await physicalStoreAccountOptions({ keyword })
    accounts.value = res?.code === 1 && Array.isArray(res.data) ? res.data : []
  } finally {
    accountLoading.value = false
  }
}

function searchAccounts(keyword) {
  loadAccounts(keyword)
}

function search() {
  pagination.pageNo = 1
  load()
}

function reset() {
  Object.assign(query, { storeName: '', merchantId: '', businessStatus: '', riderDeliveryEnabled: '', posEnabled: '' })
  search()
}

function resetForm() {
  Object.assign(form, defaults())
  formRef.value?.clearValidate()
}

async function createStore() {
  await formRef.value?.validate()
  creating.value = true
  try {
    const res = await physicalStoreCreate(buildPhysicalStorePayload(form))
    if (res?.code !== 1) throw new Error(res?.msg || res?.message || '新增门店失败')
    ElMessage.success('门店新增成功')
    createVisible.value = false
    pagination.pageNo = 1
    await load()
  } catch (error) {
    ElMessage.error(error?.message || '新增门店失败')
  } finally {
    creating.value = false
  }
}

async function toggle(row, field) {
  const property = {
    business_status: 'businessStatus',
    rider_delivery_enabled: 'riderDeliveryEnabled',
    pos_enabled: 'posEnabled'
  }[field]
  try {
    const res = await physicalStoreToggle({ id: row.id, field, enabled: row[property] })
    if (res?.code !== 1 || res.data !== true) throw new Error(res?.msg || res?.message || '更新失败')
    ElMessage.success('门店配置已更新')
  } catch (error) {
    row[property] = row[property] === 1 ? 0 : 1
    ElMessage.error(error?.message || '更新失败')
  }
}

function fullAddress(row) {
  return [row.province, row.city, row.district, row.address].filter(Boolean).join(' ')
}

onMounted(() => {
  load()
  loadMerchants()
})
</script>

<style scoped>
.store-toolbar { display: flex; align-items: center; justify-content: space-between; }
.toolbar-title { font-size: 17px; font-weight: 600; color: var(--el-text-color-primary); }
.toolbar-subtitle, .store-no, .minor { margin-top: 4px; color: var(--el-text-color-secondary); font-size: 12px; }
.store-name { font-weight: 600; color: var(--el-text-color-primary); }
</style>
