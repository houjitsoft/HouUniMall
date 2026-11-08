<template>
  <div class="ums-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>银联企业订单 · 渠道配置</span>
          <el-tag type="warning" effect="plain" size="small">UMS 商户资金自主管理</el-tag>
        </div>
      </template>
      <el-form :model="form" label-width="140px" v-loading="loading" style="max-width: 780px">
        <el-form-item label="环境">
          <el-radio-group v-model="form.env">
            <el-radio-button value="test">测试</el-radio-button>
            <el-radio-button value="prod">生产</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <el-form-item label="集团号 groupId" required>
          <el-input v-model="form.groupId" placeholder="银商分配的 6 位集团号" />
        </el-form-item>
        <el-form-item label="企业用户号 merNo" required>
          <el-input v-model="form.merNo" placeholder="一般以 10001 开头" />
        </el-form-item>
        <el-form-item label="渠道号">
          <el-input v-model="form.channelId" placeholder="固定 043" />
        </el-form-item>
        <el-form-item label="版本号">
          <el-input v-model="form.verNo" placeholder="固定 100" />
        </el-form-item>
        <el-form-item label="接口基址">
          <el-input v-model="form.baseUrl" placeholder=".../UnifyMulti（不含 transCode）" />
        </el-form-item>
        <el-form-item label="回调 notifyUrl">
          <el-input v-model="form.notifyUrl" placeholder="公网 HTTPS 回调地址" />
        </el-form-item>
        <el-form-item label="商户私钥 PEM">
          <el-input
            v-model="form.mchPrivateKey"
            type="textarea"
            :rows="4"
            :placeholder="form.hasMchPrivateKey ? '已配置（留空表示不修改）' : 'PKCS8 PEM'"
          />
        </el-form-item>
        <el-form-item label="银商公钥 PEM">
          <el-input
            v-model="form.umsPublicKey"
            type="textarea"
            :rows="4"
            :placeholder="form.hasUmsPublicKey ? '已配置（留空表示不修改）' : '用于验签'"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
          <el-button @click="load">刷新</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { umsCorpApi } from '@/api/ums-corp-order'

const loading = ref(false)
const saving = ref(false)
const form = reactive({
  env: 'test',
  enabled: true,
  groupId: '',
  merNo: '',
  channelId: '043',
  verNo: '100',
  baseUrl: 'https://mobl-test.chinaums.com/channel/Business/UnifyMulti',
  notifyUrl: '',
  mchPrivateKey: '',
  umsPublicKey: '',
  hasMchPrivateKey: false,
  hasUmsPublicKey: false
})

async function load() {
  loading.value = true
  try {
    const res = await umsCorpApi.getConfig({})
    const data = res?.data || res || {}
    Object.assign(form, {
      env: data.env || 'test',
      enabled: !!data.enabled,
      groupId: data.groupId || '',
      merNo: data.merNo || '',
      channelId: data.channelId || '043',
      verNo: data.verNo || '100',
      baseUrl: data.baseUrl || form.baseUrl,
      notifyUrl: data.notifyUrl || '',
      mchPrivateKey: '',
      umsPublicKey: '',
      hasMchPrivateKey: !!data.hasMchPrivateKey,
      hasUmsPublicKey: !!data.hasUmsPublicKey
    })
  } catch (e) {
    ElMessage.error(e?.message || '读取配置失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    const payload = { ...form }
    if (!payload.mchPrivateKey) delete payload.mchPrivateKey
    if (!payload.umsPublicKey) delete payload.umsPublicKey
    delete payload.hasMchPrivateKey
    delete payload.hasUmsPublicKey
    const res = await umsCorpApi.saveConfig(payload)
    if (res?.code != null && ![0,1,200].includes(Number(res.code))) {
      ElMessage.error(res.message || '保存失败')
      return
    }
    ElMessage.success('配置已保存')
    await load()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.ums-page { padding: 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; font-weight: 600; }
</style>
