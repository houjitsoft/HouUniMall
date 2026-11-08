<template>
  <div class="store-edit-page">
    <div class="page-heading">
      <div>
        <h2>编辑店铺</h2>
        <p>修改店铺基础资料、经营状态和平台展示信息</p>
      </div>
      <el-button @click="backToList">返回店铺列表</el-button>
    </div>

    <el-card v-loading="loading" class="!border-none" shadow="never">
      <el-alert
        v-if="loadError"
        type="error"
        :title="loadError"
        :closable="false"
        show-icon
      >
        <template #default>
          <el-button type="primary" link @click="loadPage">重新加载</el-button>
        </template>
      </el-alert>

      <el-form
        v-else
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="110px"
        class="store-form"
      >
        <el-divider content-position="left">基础资料</el-divider>
        <el-row :gutter="24">
          <el-col :xs="24" :md="12">
            <el-form-item label="店铺名称" prop="name">
              <el-input v-model="form.name" maxlength="100" show-word-limit placeholder="请输入店铺名称" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="统一账号">
              <el-input v-model="form.loginAccount" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="店铺类型" prop="type">
              <el-radio-group v-model="form.type">
                <el-radio :value="0">官方自营</el-radio>
                <el-radio :value="1">商家入驻</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="主营类目" prop="shopCategoryId">
              <el-select v-model="form.shopCategoryId" class="w-full" clearable placeholder="请选择主营类目">
                <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="联系人" prop="contact">
              <el-input v-model="form.contact" maxlength="30" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="联系手机" prop="mobile">
              <el-input v-model="form.mobile" maxlength="11" placeholder="请输入11位手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="详细地址" prop="addressInfo">
              <el-input v-model="form.addressInfo" maxlength="255" show-word-limit placeholder="请输入店铺详细地址" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">经营状态</el-divider>
        <el-row :gutter="24">
          <el-col :xs="24" :md="12">
            <el-form-item label="店铺状态" prop="shopStatus">
              <el-radio-group v-model="form.shopStatus">
                <el-radio :value="1">正常</el-radio>
                <el-radio :value="0">冻结</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="营业状态" prop="businessStatus">
              <el-radio-group v-model="form.businessStatus">
                <el-radio :value="1">营业中</el-radio>
                <el-radio :value="0">暂停营业</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="form-actions">
          <el-button @click="backToList">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submit">保存修改</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

import { mallShopDetail, mallShopProvisionOptions, mallShopSave } from '@/api/mall/shopAdmin'
import { buildStoreUpdatePayload, normalizeStoreDetail } from './storeEditModel'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const saving = ref(false)
const loadError = ref('')
const categories = ref([])
const form = reactive(normalizeStoreDetail())

const rules = {
  name: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择店铺类型', trigger: 'change' }],
  contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  mobile: [
    { required: true, message: '请输入联系手机', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  shopStatus: [{ required: true, message: '请选择店铺状态', trigger: 'change' }],
  businessStatus: [{ required: true, message: '请选择营业状态', trigger: 'change' }]
}

const backToList = () => router.push('/mall/store/list')

const loadPage = async () => {
  const id = Number(route.query.id)
  if (!Number.isInteger(id) || id <= 0) {
    loadError.value = '店铺ID不正确，请返回列表后重新选择'
    return
  }

  loading.value = true
  loadError.value = ''
  try {
    const [detailRes, optionsRes] = await Promise.all([
      mallShopDetail({ id }),
      mallShopProvisionOptions()
    ])
    if (detailRes?.code !== 1 || !detailRes.data) {
      throw new Error(detailRes?.msg || detailRes?.message || '店铺资料加载失败')
    }
    Object.assign(form, normalizeStoreDetail(detailRes.data))
    categories.value = optionsRes?.code === 1 && Array.isArray(optionsRes.data) ? optionsRes.data : []
  } catch (error) {
    loadError.value = error?.message || '店铺资料加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    const res = await mallShopSave(buildStoreUpdatePayload(form))
    if (res?.code !== 1) throw new Error(res?.msg || res?.message || '保存失败')
    ElMessage.success('店铺资料已更新')
    await backToList()
  } catch (error) {
    ElMessage.error(error?.message || '保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

onMounted(loadPage)
</script>

<style scoped>
.store-edit-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-heading h2 {
  margin: 0;
  color: var(--el-text-color-primary);
  font-size: 20px;
}

.page-heading p {
  margin: 6px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.store-form {
  max-width: 1080px;
  padding: 4px 12px 8px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 8px;
}

@media (max-width: 767px) {
  .page-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .store-form {
    padding-inline: 0;
  }
}
</style>
