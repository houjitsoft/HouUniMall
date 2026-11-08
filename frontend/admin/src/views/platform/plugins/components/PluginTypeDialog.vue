<template>
  <el-dialog
    v-model="visible"
    title="选择插件类型"
    width="min(880px, calc(100vw - 24px))"
    :close-on-click-modal="false"
    destroy-on-close
    class="plugin-type-dialog"
    @closed="resetSelection"
  >
    <p class="dialog-intro">主要区别是业务代码放在哪里、如何交付更新。宿主就是下一步所选的已有业务服务；两种方式都不会新建服务、进程或端口。</p>

    <el-radio-group v-model="selectedRuntime" class="type-list" aria-label="插件类型">
      <el-radio
        v-for="type in pluginTypes"
        :key="type.value"
        :value="type.value"
        class="type-option"
        :class="{ 'is-selected': selectedRuntime === type.value }"
      >
        <div class="type-content">
          <div class="type-icon" :class="type.iconClass">
            <el-icon><component :is="type.icon" /></el-icon>
          </div>
          <div class="type-copy">
            <div class="type-title-row">
              <strong>{{ type.title }}</strong>
              <el-tag v-if="type.default" type="success" size="small" effect="plain">默认</el-tag>
            </div>
            <div class="type-manifest">plugin.yaml · runtime: {{ type.value }}</div>
            <p class="type-summary">{{ type.description }}</p>
            <dl class="type-details">
              <div><dt>实现位置</dt><dd><code>{{ type.directory }}</code><span>{{ type.implementation }}</span></dd></div>
              <div><dt>交付更新</dt><dd>{{ type.delivery }}</dd></div>
              <div><dt>适用场景</dt><dd>{{ type.scenario }}</dd></div>
            </dl>
          </div>
        </div>
      </el-radio>
    </el-radio-group>

    <p class="shared-note">共同边界：两者都在所选服务进程内运行，没有插件专属的进程隔离；同进程不等于沙箱。性能主要取决于业务实现，不能仅按类型判断快慢。更新是否需要重启取决于宿主装载与发布方式，不保证无重启热更新。</p>

    <el-alert
      class="type-alert"
      type="info"
      :closable="false"
      show-icon
      title="平台核心（CORE）不提供创建入口"
      description="认证、权限、用户、订单、支付等平台核心能力随框架发布，由平台维护，不在插件中心创建。"
    />

    <template #footer>
      <el-button @click="cancel">取消</el-button>
      <el-button type="primary" :disabled="!selectedRuntime" @click="confirmRuntime">下一步</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { Box, Connection } from '@element-plus/icons-vue'

const DEFAULT_RUNTIME = 'standalone'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 可创建的插件形态：取值与 plugin.yaml 的 runtime 字段一致（embedded / standalone）。
// plugin.yaml 里没有 type 字段，归属只由 runtime 表达；CORE 是平台归属，不在这里出现。
const pluginTypes = [
  {
    value: 'standalone',
    title: '服务产品（standalone）',
    description: '随服务发布：业务代码编入已有服务。',
    directory: 'backend/paas/{hostService}/',
    implementation: '在所选已有业务服务内实现；脚手架仅在 plugins/{hostService}/{code}/ 保存描述与前端，不生成独立后端。',
    delivery: '业务修改随所选服务一起编译、发布，不单独交付插件 JAR。',
    scenario: '适合由已有服务统一维护、随服务版本一起更新的业务产品。',
    icon: Connection,
    iconClass: 'is-service',
    default: true
  },
  {
    value: 'embedded',
    title: '内嵌扩展（embedded）',
    description: '独立 JAR 交付：由已有服务加载扩展。',
    directory: 'plugins/{hostService}/{code}/runtime/java/',
    implementation: '在插件目录内实现 Java 扩展代码，由所选宿主服务加载。',
    delivery: '单独构建扩展 JAR 并交付给宿主；更新需按宿主支持的装载流程执行。',
    scenario: '适合已有服务提供扩展点、需要单独维护和交付扩展包的功能。',
    icon: Box,
    iconClass: 'is-example',
    default: false
  }
]

const selectedRuntime = ref(DEFAULT_RUNTIME)

const resetSelection = () => {
  selectedRuntime.value = DEFAULT_RUNTIME
}

watch(
  () => props.modelValue,
  (isOpen) => {
    if (isOpen) resetSelection()
  }
)

const cancel = () => {
  visible.value = false
  emit('cancel')
}

const confirmRuntime = () => {
  if (!selectedRuntime.value) return
  visible.value = false
  emit('confirm', selectedRuntime.value)
}
</script>

<style scoped>
.dialog-intro { margin: 0 0 14px; color: #606266; font-size: 13px; line-height: 20px; }
.type-list { display: grid; width: 100%; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
.type-option { display: flex; align-items: stretch; height: auto; margin: 0; padding: 0; border: 1px solid #dcdfe6; border-radius: 6px; background: #fff; transition: border-color .2s, box-shadow .2s, background .2s; }
.type-option :deep(.el-radio__input) { align-self: flex-start; margin: 16px 0 0 14px; }
.type-option :deep(.el-radio__label) { display: block; width: 100%; padding: 0 12px 0 10px; white-space: normal; }
.type-option.is-selected { border-color: #409eff; background: #f4f9ff; box-shadow: 0 0 0 1px rgba(64, 158, 255, .12); }
.type-content { display: flex; min-height: 176px; flex-direction: column; padding: 16px 0 14px; }
.type-icon { display: flex; align-items: center; justify-content: center; width: 34px; height: 34px; margin-bottom: 12px; border-radius: 8px; font-size: 19px; }
.type-icon.is-service { background: #fff2db; color: #b86b00; }
.type-icon.is-example { background: #e8f3ff; color: #1677d2; }
.type-title-row { display: flex; align-items: center; gap: 6px; min-height: 24px; }
.type-title-row strong { color: #303133; font-size: 15px; line-height: 22px; }
.type-manifest { margin-top: 4px; color: #606266; font: 12px Consolas, monospace; }
.type-summary { margin: 10px 0; color: #303133; font-size: 13px; line-height: 20px; }
.type-details { margin: 0; color: #606266; font-size: 12px; line-height: 19px; }
.type-details > div + div { margin-top: 12px; }
.type-details dt { margin-bottom: 3px; color: #303133; font-weight: 600; }
.type-details dd { margin: 0; }
.type-details code, .type-details span { display: block; }
.type-details code { margin-bottom: 4px; font-size: 12px; }
.type-copy, .type-option, .type-option :deep(.el-radio__label) { min-width: 0; overflow-wrap: anywhere; }
.type-title-row { flex-wrap: wrap; }
.shared-note { margin: 14px 0 0; color: #606266; font-size: 12px; line-height: 20px; }
.type-alert { margin-top: 14px; }
@media (max-width: 720px) { .type-list { grid-template-columns: 1fr; } .type-content { min-height: 0; } }
</style>
