<template>
  <div class="distribution-setting">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>分销设置</span>
          <el-button type="primary" :loading="saving" @click="handleSave">保存设置</el-button>
        </div>
      </template>

      <el-form :model="form" label-width="150px" v-loading="loading" class="setting-form">
        <el-divider content-position="left">基础开关</el-divider>

        <el-form-item label="分销功能">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" active-text="开启" inactive-text="关闭" />
          <div class="form-tip">关闭后，C 端分销中心入口隐藏，分销商品与推广链接全部失效；已有佣金记录保留。</div>
        </el-form-item>

        <el-form-item label="分佣层级">
          <el-radio-group v-model="form.maxLevel">
            <el-radio :value="1">一级分销（仅直接推荐人得佣金）</el-radio>
            <el-radio :value="2">二级分销（推荐人 + 推荐人的上级）</el-radio>
          </el-radio-group>
          <div class="form-tip">合规上限两级，系统已在服务端强制收敛，超出配置无效。</div>
        </el-form-item>

        <el-divider content-position="left">成为分销员</el-divider>

        <el-form-item label="加入方式">
          <el-radio-group v-model="form.applyMode">
            <el-radio :value="1">无门槛申请（申请即生效）</el-radio>
            <el-radio :value="2">需平台审核</el-radio>
            <el-radio :value="3">仅邀请（凭邀请码加入）</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="等级自动升级">
          <el-switch v-model="form.autoUpgrade" :active-value="1" :inactive-value="0" active-text="自动" inactive-text="手动" />
          <div class="form-tip">自动：按分销等级的升级金额 / 订单数 / 下级数规则自动升降；手动：仅管理员在分销商管理中调整。</div>
        </el-form-item>

        <el-divider content-position="left">关系与结算</el-divider>

        <el-form-item label="上下级绑定">
          <el-radio-group v-model="form.bindMode">
            <el-radio :value="1">永久绑定（首次绑定后不变）</el-radio>
            <el-radio :value="2">保护期后可换绑</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="绑定保护期（天）" v-if="form.bindMode === 2">
          <el-input-number v-model="form.bindProtectDays" :min="0" :max="365" />
          <div class="form-tip">0 表示不设保护，客户可随时改绑其它分销员。</div>
        </el-form-item>

        <el-form-item label="佣金结算周期（天）">
          <el-input-number v-model="form.settleDays" :min="0" :max="90" />
          <div class="form-tip">订单完成后 T+N 天，待结算佣金转为可提现；建议不小于售后期，防止退款后追佣。</div>
        </el-form-item>

        <el-form-item label="单笔最低提现（元）">
          <el-input-number v-model="form.minWithdrawAmount" :min="0" :precision="2" :step="1" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { distributionSettingApi } from '../api/distribution'

const loading = ref(false)
const saving = ref(false)
const form = reactive({
  enabled: 0,
  maxLevel: 2,
  applyMode: 2,
  bindMode: 1,
  bindProtectDays: 15,
  settleDays: 7,
  minWithdrawAmount: 10,
  autoUpgrade: 1
})

const loadSetting = async () => {
  loading.value = true
  try {
    const res = await distributionSettingApi.detail()
    const data = res?.data || res
    Object.keys(form).forEach((key) => {
      if (data && data[key] !== undefined && data[key] !== null) form[key] = data[key]
    })
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    await distributionSettingApi.save({ ...form })
    ElMessage.success('分销设置已保存')
    await loadSetting()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadSetting)
</script>

<style scoped>
.distribution-setting {
  padding: 12px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.setting-form {
  max-width: 640px;
}
.form-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
  margin-top: 4px;
}
</style>
