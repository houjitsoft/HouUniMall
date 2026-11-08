<template>
  <el-dialog
    v-model="visible"
    title="升级前风险确认"
    width="640px"
    :close-on-click-modal="false"
    destroy-on-close
    @open="loadRisk"
  >
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else-if="risk">
      <el-alert
        v-if="risk.blocking"
        type="error"
        :closable="false"
        show-icon
        title="兼容性预检未通过，无法升级"
        :description="risk.precheckDetail || '请先解决依赖或框架版本问题'"
        class="mb"
      />

      <el-alert
        v-if="maintenanceText"
        :type="maintenanceLevel === 'danger' ? 'error' : maintenanceLevel === 'warn' ? 'warning' : 'info'"
        :closable="false"
        show-icon
        :title="`官方维护：${maintenanceText}`"
        class="mb"
      />

      <div class="risk-block">
        <h4>将备份</h4>
        <p>{{ risk.willBackupDetail }}</p>
        <el-checkbox v-model="acks.backup" :disabled="risk.blocking">我已知晓：升级前会备份，失败时优先回滚制品</el-checkbox>
      </div>

      <div class="risk-block warn">
        <h4>可能不可逆迁移</h4>
        <p>{{ risk.irreversibleDetail }}</p>
        <el-checkbox v-model="acks.migration" :disabled="risk.blocking">我已知晓：Schema 变更可能无法自动回滚业务数据</el-checkbox>
      </div>

      <div class="risk-block">
        <h4>二开冲突报告（{{ risk.extensionConflictCount || 0 }}）</h4>
        <el-table v-if="(risk.extensionConflicts || []).length" :data="risk.extensionConflicts" size="small" max-height="180">
          <el-table-column prop="code" label="二开模块" min-width="120" />
          <el-table-column prop="hostService" label="宿主" width="100" />
          <el-table-column prop="version" label="版本" width="90" />
          <el-table-column prop="note" label="说明" min-width="160" show-overflow-tooltip />
        </el-table>
        <el-empty v-else description="未发现 classpath 上的 custom-* 二开描述" :image-size="48" />
        <el-checkbox v-model="acks.extension" :disabled="risk.blocking" class="mt">
          我已知晓：升级后需复核二开契约（可用 scripts/upgrade-diff-report.py）
        </el-checkbox>
      </div>

      <p class="meta">{{ pluginCode }} → {{ targetVersion || '最新版' }}</p>
    </template>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="confirming" :disabled="!canConfirm" @click="confirm">
        确认升级
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getUpgradeRisk } from '@/api/plugin-center/pluginUpdate'
import { formatMaintenanceCountdown } from '@/utils/pluginInstallFeedback'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  pluginCode: { type: String, required: true },
  targetVersion: { type: String, default: '' },
  hostService: { type: String, default: '' },
  maintenanceUntil: { type: [String, Date], default: '' },
  confirming: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const loading = ref(false)
const risk = ref(null)
const acks = reactive({ backup: false, migration: false, extension: false })

const maintenanceInfo = computed(() => formatMaintenanceCountdown(props.maintenanceUntil))
const maintenanceText = computed(() => (maintenanceInfo.value.level === 'none' ? '' : maintenanceInfo.value.text))
const maintenanceLevel = computed(() => maintenanceInfo.value.level)

const canConfirm = computed(() => {
  if (!risk.value || risk.value.blocking) return false
  return acks.backup && acks.migration && acks.extension
})

const resetAcks = () => {
  acks.backup = false
  acks.migration = false
  acks.extension = false
}

const loadRisk = async () => {
  loading.value = true
  risk.value = null
  resetAcks()
  try {
    risk.value = await getUpgradeRisk(props.pluginCode, props.targetVersion, props.hostService || props.pluginCode)
  } catch (error) {
    ElMessage.error(error?.message || '加载升级风险失败')
    visible.value = false
  } finally {
    loading.value = false
  }
}

watch(
  () => props.modelValue,
  (open) => {
    if (open) loadRisk()
  }
)

const confirm = () => {
  if (!canConfirm.value) return
  emit('confirm', { risk: risk.value, acks: { ...acks } })
}
</script>

<style scoped>
.mb{margin-bottom:12px}
.mt{margin-top:10px}
.risk-block{padding:12px 14px;margin-bottom:12px;border:1px solid #ebeef5;border-radius:6px;background:#fafafa}
.risk-block.warn{border-color:#f5dab1;background:#fdf6ec}
.risk-block h4{margin:0 0 6px;font-size:14px}
.risk-block p{margin:0 0 10px;color:#606266;font-size:13px;line-height:1.5}
.meta{margin:0;color:#909399;font-size:12px;font-family:Consolas,monospace}
</style>
