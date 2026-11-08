<template>
  <div class="xj-panel" :class="{ 'is-mobile': isMobile }" :aria-busy="loadingDraft || recognizing || confirming">
    <header class="xj-hero">
      <div class="hero-icon-wrap"><img src="@/assets/images/icons/xiaojiang.gif" alt="" class="hero-icon" /></div>
      <div class="hero-text">
        <h2 class="hero-title">小匠帮你传</h2>
        <p class="hero-subtitle">上传业务资料，确认 AI 建议后直接入库</p>
      </div>
      <div class="step-track" aria-label="智能入库进度">
        <span class="step-node is-active"><b>1</b>上传资料</span>
        <span class="step-line" :class="{ 'is-active': step === 'preview' }" />
        <span class="step-node" :class="{ 'is-active': step === 'preview' }"><b>2</b>确认入库</span>
      </div>
    </header>

    <main v-loading="loadingDraft" class="xj-body">
      <section v-if="step === 'upload'" class="upload-step" aria-labelledby="upload-step-title">
        <div class="upload-card">
          <div class="section-heading">
            <div>
              <h3 id="upload-step-title">上传与识别</h3>
              <p>先选业务系统并写清资料用途，再上传文件；AI 会判断具体功能和入库对象。</p>
            </div>
            <span v-if="draftNo" class="draft-badge">草稿已保存</span>
          </div>

          <div class="form-stack">
            <div class="form-field system-field">
              <label for="xj-system-select">业务系统 <em aria-hidden="true">*</em></label>
              <el-select
                id="xj-system-select"
                :model-value="systemKey"
                placeholder="请选择商城、CRM、智能制造等系统"
                filterable
                @update:model-value="emit('update:system-key', $event)"
              >
                <el-option v-for="system in systems" :key="system.key" :label="system.name" :value="system.key" />
              </el-select>
            </div>
            <div class="form-field description-field">
              <label for="xj-description">资料描述 <em aria-hidden="true">*</em></label>
              <el-input
                id="xj-description"
                :model-value="description"
                type="textarea"
                :rows="isMobile ? 3 : 4"
                maxlength="500"
                show-word-limit
                resize="none"
                placeholder="例如：9 月客户拜访记录，请识别客户并录入 CRM"
                @update:model-value="emit('update:description', $event)"
              />
              <p v-if="!description.trim()" class="field-hint" role="status">请填写资料描述，帮助 AI 判断入库对象。</p>
            </div>
          </div>

          <el-upload
            ref="uploadRef"
            class="upload-area"
            drag
            :auto-upload="false"
            :on-change="(file, files) => emit('file-change', file, files)"
            :before-upload="beforeUpload"
            :accept="acceptedTypes"
            :show-file-list="true"
            :file-list="fileList"
            :limit="10"
            multiple
          >
            <div class="upload-content">
              <span class="upload-icon-wrap">
                <el-icon class="upload-icon" :size="isMobile ? 34 : 44"><UploadFilled /></el-icon>
              </span>
              <div>
                <div class="upload-main-text">拖拽业务资料到这里，或<span>点击选择</span></div>
                <div class="upload-sub-text">支持 PDF、Word、Excel、图片和压缩包，最多 10 个文件</div>
              </div>
            </div>
          </el-upload>

          <div class="privacy-note">
            <el-icon><Paperclip /></el-icon>
            <span>原始文件会随入库记录一并保存，方便后续追溯。</span>
          </div>
        </div>
      </section>

      <section v-else class="preview-step" aria-labelledby="preview-step-title">
        <div class="section-heading">
          <div><h3 id="preview-step-title">确认入库位置与字段</h3><p>AI 已给出建议。你可以修改目标功能和字段，确认后才会写入业务系统。</p></div>
          <span class="preview-count">{{ recognizeResults.length }} 项待确认</span>
        </div>

        <div class="target-path" aria-label="当前入库系统">
          <span>{{ currentSystem?.name || systemKey }}</span><el-icon><ArrowRight /></el-icon><strong>AI 建议目标可修改</strong>
        </div>

        <div class="result-list" aria-live="polite">
          <article v-for="(item, index) in recognizeResults" :key="item.itemId || item.fileName" class="result-card" :class="`status-${item.status}`">
            <header class="result-head">
              <div class="file-identity">
                <span class="file-icon"><el-icon><Document /></el-icon></span>
                <div><h4>{{ item.fileName || `资料 ${index + 1}` }}</h4><p>{{ formatFileSize(item.fileSize) }}<span v-if="item.aiModel"> · {{ item.aiProvider ? `${item.aiProvider} / ` : '' }}{{ item.aiModel }}</span><span v-if="item.summary"> · {{ item.summary }}</span></p></div>
              </div>
              <el-tag v-if="item.status === 'failed'" type="danger" effect="light">入库失败</el-tag>
              <el-tag v-else-if="item.status === 'needs_manual_target'" type="warning" effect="light">请选择目标</el-tag>
              <el-tag v-else type="success" effect="plain">AI 已识别</el-tag>
            </header>

            <div class="target-editor">
              <div class="form-field">
                <label :for="`xj-target-${item.itemId}`">目标功能与对象</label>
                <el-select :id="`xj-target-${item.itemId}`" :model-value="item.objectKey" placeholder="请选择入库对象" @update:model-value="emit('target-change', item, $event)">
                  <el-option v-for="target in currentSystem?.objects || []" :key="target.objectKey" :label="`${target.functionName} / ${target.objectName}`" :value="target.objectKey" />
                </el-select>
              </div>
              <div class="selected-route"><span>{{ currentSystem?.name || systemKey }}</span><el-icon><ArrowRight /></el-icon><span>{{ targetFor(item)?.functionName || item.functionKey || '待选择' }}</span><el-icon><ArrowRight /></el-icon><strong>{{ targetFor(item)?.objectName || item.objectKey || '待选择' }}</strong></div>
            </div>

            <div v-if="schemaFor(item).length" class="fields-grid">
              <div v-for="field in schemaFor(item)" :key="field.name" class="form-field" :class="{ 'field-wide': field.type === 'text' }">
                <label :for="`xj-${item.itemId}-${field.name}`">{{ field.label }} <em v-if="field.required" aria-hidden="true">*</em></label>
                <el-input-number v-if="['decimal', 'integer', 'long'].includes(field.type)" :id="`xj-${item.itemId}-${field.name}`" v-model="item.fields[field.name]" :precision="field.type === 'decimal' ? 2 : 0" controls-position="right" @change="emit('field-change', item)" />
                <el-date-picker v-else-if="field.type === 'datetime'" :id="`xj-${item.itemId}-${field.name}`" v-model="item.fields[field.name]" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择日期时间" @change="emit('field-change', item)" />
                <el-input v-else :id="`xj-${item.itemId}-${field.name}`" v-model="item.fields[field.name]" :type="field.type === 'text' ? 'textarea' : 'text'" :rows="2" :placeholder="`请输入${field.label}`" @input="emit('field-change', item)" />
                <p v-if="field.required && isBlank(item.fields[field.name])" class="field-error" role="alert">{{ field.label }}不能为空</p>
              </div>
            </div>

            <div class="attachment-row"><el-icon><Paperclip /></el-icon><span>原始附件：{{ item.fileName }}</span><a v-if="item.fileUrl" :href="item.fileUrl" target="_blank" rel="noopener">查看</a></div>
            <ul v-if="item.validationErrors?.length" class="validation-list" role="alert"><li v-for="message in item.validationErrors" :key="message">{{ message }}</li></ul>
            <p v-if="item.errorMessage" class="item-error" role="alert">{{ item.errorMessage }}</p>
            <div v-if="item.status === 'failed'" class="item-actions"><el-button type="primary" plain :loading="item.saving || confirming" @click="emit('retry', item)">重试此项</el-button></div>
          </article>
        </div>
      </section>
    </main>

    <footer class="xj-footer">
      <template v-if="step === 'upload'">
        <el-button class="touch-button" @click="emit('cancel')">稍后继续</el-button>
        <el-button type="primary" class="primary-action touch-button" :loading="recognizing" :disabled="!canRecognize" @click="emit('recognize')"><el-icon v-if="!recognizing"><MagicStick /></el-icon>{{ recognizing ? 'AI 识别中…' : '开始智能识别' }}</el-button>
      </template>
      <template v-else>
        <el-button class="touch-button" @click="emit('back')"><el-icon><ArrowLeft /></el-icon>返回修改</el-button>
        <el-button type="primary" class="primary-action touch-button" :loading="confirming" :disabled="!canConfirm" @click="emit('confirm')">{{ confirming ? '正在入库…' : '确认批量入库' }}</el-button>
      </template>
      <p class="footer-status" aria-live="polite">{{ footerStatus }}</p>
    </footer>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ArrowLeft, ArrowRight, Document, MagicStick, Paperclip, UploadFilled } from '@element-plus/icons-vue'
import { acceptedTypes, beforeUpload as validateUploadFile } from './xiaojiangUploadShared.js'

const props = defineProps({
  isMobile: { type: Boolean, default: false }, step: { type: String, default: 'upload' },
  systemKey: { type: String, default: '' }, systems: { type: Array, default: () => [] },
  fileList: { type: Array, default: () => [] }, description: { type: String, default: '' },
  recognizeResults: { type: Array, default: () => [] }, recognizing: { type: Boolean, default: false },
  confirming: { type: Boolean, default: false }, loadingDraft: { type: Boolean, default: false },
  draftNo: { type: String, default: '' }
})
const emit = defineEmits(['update:system-key', 'update:description', 'file-change', 'recognize', 'back', 'target-change', 'field-change', 'confirm', 'retry', 'cancel'])
const uploadRef = ref()
const currentSystem = computed(() => props.systems.find((item) => item.key === props.systemKey))
const canRecognize = computed(() => Boolean(props.systemKey && props.description.trim() && props.fileList.length && !props.recognizing))
const canConfirm = computed(() => props.recognizeResults.length > 0 && props.recognizeResults.every((item) => item.objectKey && schemaFor(item).every((field) => !field.required || !isBlank(item.fields?.[field.name]))))
const footerStatus = computed(() => props.step === 'upload' ? '描述为必填项；返回或关闭后，已识别草稿会保留。' : '仅写入当前预览内容；单项失败不会影响其他资料。')
const targetFor = (item) => currentSystem.value?.objects?.find((target) => target.objectKey === item.objectKey)
const schemaFor = (item) => targetFor(item)?.fields || item.fieldSchema || []
const isBlank = (value) => value === null || value === undefined || String(value).trim() === ''
const formatFileSize = (size) => !size ? '原始文件已保存' : size < 1024 * 1024 ? `${Math.max(1, Math.round(size / 1024))} KB` : `${(size / 1024 / 1024).toFixed(1)} MB`
const beforeUpload = (file) => { validateUploadFile(file); return false }
defineExpose({ clearFiles: () => uploadRef.value?.clearFiles?.() })
</script>

<style scoped lang="scss">
.xj-panel { display:flex; flex-direction:column; height:100%; min-height:0; color:#1f2552; background:#f8f9ff; font-family:Inter, "Helvetica Neue", Arial, sans-serif; }
.xj-hero { position:relative; display:flex; align-items:center; gap:14px; padding:22px 28px 24px; color:#fff; background:radial-gradient(circle at 76% 20%, rgba(0,240,255,.34), transparent 26%), linear-gradient(125deg,#3b52c4 0%,#7139c6 58%,#c237a0 100%); overflow:hidden; }
.xj-hero::after { content:""; position:absolute; inset:auto -10% -65px 38%; height:100px; background:rgba(255,255,255,.12); filter:blur(28px); transform:rotate(-8deg); }
.hero-icon-wrap { z-index:1; width:56px; height:56px; border-radius:18px; display:grid; place-items:center; flex:none; background:rgba(255,255,255,.16); border:1px solid rgba(255,255,255,.3); box-shadow:0 0 26px rgba(0,240,255,.2); }
.hero-icon { width:40px; height:40px; object-fit:contain; }.hero-text{z-index:1}.hero-title{margin:0;font-size:23px;line-height:1.2}.hero-subtitle{margin:5px 0 0;font-size:13px;color:rgba(255,255,255,.88)}
.step-track { z-index:1; margin-left:auto; display:flex; align-items:center; min-width:310px; }.step-node{display:flex;align-items:center;gap:7px;font-size:13px;color:rgba(255,255,255,.62);white-space:nowrap}.step-node b{width:27px;height:27px;display:grid;place-items:center;border-radius:50%;border:1px solid rgba(255,255,255,.4)}.step-node.is-active{color:#fff;font-weight:700}.step-node.is-active b{background:#fff;color:#5738bf;box-shadow:0 0 18px rgba(0,240,255,.5)}.step-line{height:2px;flex:1;margin:0 10px;background:rgba(255,255,255,.25)}.step-line.is-active{background:#fff;box-shadow:0 0 10px #00f0ff}
.xj-body { flex:1; min-height:0; overflow:auto; padding:24px 28px; }
.upload-card {
  padding: 22px 22px 18px;
  border: 1px solid #e8eaf5;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 10px 28px rgba(47, 39, 106, 0.06);
}
.section-heading{display:flex;align-items:flex-start;justify-content:space-between;gap:18px;margin-bottom:20px}
.section-heading h3{margin:0;font-size:19px;color:#23284f}
.section-heading p{margin:6px 0 0;color:#71779c;font-size:13px;line-height:1.55;max-width:52em}
.draft-badge,.preview-count{padding:7px 11px;border-radius:999px;background:#edeaff;color:#613fc3;font-size:12px;font-weight:700;white-space:nowrap}
.form-stack{display:flex;flex-direction:column;gap:16px;margin-bottom:18px}
.form-grid{display:grid;grid-template-columns:minmax(220px,.72fr) minmax(360px,1.4fr);gap:16px;margin-bottom:18px}
.form-field{min-width:0}
.system-field{max-width:420px}
.form-field label{display:block;margin-bottom:7px;font-size:13px;font-weight:700;color:#373c66}
.form-field label em{color:#ed3c72;font-style:normal}
.form-field :deep(.el-select),.form-field :deep(.el-input-number),.form-field :deep(.el-date-editor){width:100%}
.description-field :deep(.el-textarea__inner){
  min-height: 108px;
  padding: 12px 14px 28px;
  border-radius: 12px;
  line-height: 1.55;
  box-shadow: none;
}
.description-field :deep(.el-input__count){background: transparent; color: #9aa0bd;}
.field-error{margin:6px 0 0;color:#d93365;font-size:12px;line-height:1.35}
.field-hint{margin:8px 0 0;color:#858bab;font-size:12px;line-height:1.45}
.upload-area{width:100%}
.upload-area :deep(.el-upload),.upload-area :deep(.el-upload-dragger){width:100%}
.upload-area :deep(.el-upload-dragger){
  padding:0;
  border:1px dashed #c5cceb;
  border-radius:16px;
  background:#fafbff;
  transition:.25s;
}
.upload-area :deep(.el-upload-dragger:hover){border-color:#7046d7;background:#f7f5ff;box-shadow:0 10px 28px rgba(93,52,208,.1)}
.upload-content{display:flex;align-items:center;justify-content:center;gap:18px;padding:30px 20px;text-align:left}
.upload-icon-wrap{width:66px;height:66px;display:grid;place-items:center;border-radius:20px;background:linear-gradient(135deg,#dce8ff,#eadfff);color:#5d42cf}
.upload-main-text{font-size:16px;font-weight:700;color:#292e5b}
.upload-main-text span{color:#643fd1}
.upload-sub-text{margin-top:7px;color:#858bab;font-size:12px}
.privacy-note,.attachment-row{display:flex;align-items:center;gap:7px;color:#777e9d;font-size:12px}
.privacy-note{margin-top:14px;padding-top:12px;border-top:1px solid #eef0f8}
.privacy-note .el-icon,.attachment-row .el-icon{color:#6544cf}
.target-path{display:flex;align-items:center;gap:9px;padding:12px 15px;margin-bottom:16px;border:1px solid #cbd1ff;border-radius:13px;background:linear-gradient(90deg,#eff3ff,#f9edff);box-shadow:0 0 20px rgba(93,52,208,.08);font-size:13px}.target-path strong{color:#683dc8}.result-list{display:flex;flex-direction:column;gap:16px}.result-card{padding:18px;border:1px solid #e2e4f3;border-radius:16px;background:#fff;box-shadow:0 8px 26px rgba(47,39,106,.07)}.result-card.status-failed{border-color:#f2b3c6}.result-head{display:flex;align-items:flex-start;justify-content:space-between;gap:12px;padding-bottom:14px;border-bottom:1px solid #eceef7}.file-identity{display:flex;gap:11px;min-width:0}.file-icon{width:38px;height:38px;display:grid;place-items:center;flex:none;border-radius:11px;background:#eeebff;color:#6843ca}.file-identity h4{margin:0;color:#252a50;font-size:14px;word-break:break-all}.file-identity p{margin:5px 0 0;color:#858aa5;font-size:12px}.target-editor{display:grid;grid-template-columns:minmax(260px,.75fr) 1.25fr;gap:18px;align-items:end;padding:16px 0}.selected-route{min-height:40px;display:flex;align-items:center;gap:8px;padding:0 13px;border-radius:10px;background:#f5f3ff;color:#62698b;font-size:12px}.selected-route strong{color:#633bc5}.fields-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:15px;padding:16px;border-radius:13px;background:#f8f9fd}.field-wide{grid-column:span 2}.attachment-row{padding-top:14px}.attachment-row a{margin-left:auto;color:#5d3bc5;text-decoration:none}.item-error{margin:12px 0 0;padding:10px 12px;border-radius:9px;background:#fff0f4;color:#bc2452;font-size:12px}.item-actions{display:flex;justify-content:flex-end;margin-top:12px}
.validation-list{margin:12px 0 0;padding:10px 12px 10px 28px;border-radius:9px;background:#fff8e7;color:#966900;font-size:12px;line-height:1.55}
.xj-footer{flex:none;display:flex;align-items:center;gap:12px;padding:16px 28px 19px;border-top:1px solid #e5e6f2;background:#fff}.primary-action{background:linear-gradient(120deg,#4666df,#8b3dcc);border:0;box-shadow:0 8px 18px rgba(93,52,208,.22)}.touch-button{min-height:44px;padding-inline:20px}.footer-status{margin:0 0 0 auto;color:#858ba7;font-size:12px}
.is-mobile .xj-hero{padding:17px 64px 17px 18px;flex-wrap:wrap}
.is-mobile .hero-icon-wrap{width:48px;height:48px;border-radius:15px}
.is-mobile .hero-icon{width:34px;height:34px}
.is-mobile .hero-title{font-size:19px}
.is-mobile .step-track{order:3;width:100%;min-width:0;margin:4px 0 0}
.is-mobile .xj-body{padding:16px 14px}
.is-mobile .upload-card{padding:16px 14px 14px;border-radius:14px}
.is-mobile .system-field{max-width:none}
.is-mobile .form-grid,.is-mobile .target-editor,.is-mobile .fields-grid{grid-template-columns:1fr}
.is-mobile .field-wide{grid-column:auto}
.is-mobile .upload-content{padding:23px 12px;gap:12px;text-align:left}
.is-mobile .upload-icon-wrap{width:52px;height:52px;border-radius:16px}
.is-mobile .upload-main-text{font-size:14px}
.is-mobile .target-path{overflow:auto;white-space:nowrap}
.is-mobile .selected-route{overflow:auto;white-space:nowrap}
.is-mobile .xj-footer{padding:13px 16px calc(13px + env(safe-area-inset-bottom,0px));flex-wrap:wrap}
.is-mobile .touch-button{flex:1;margin:0}
.is-mobile .footer-status{width:100%;margin:0;line-height:1.4}
@media (max-width:736px){.section-heading{align-items:flex-start}.preview-count{max-width:120px;overflow:hidden;text-overflow:ellipsis}.result-card{padding:14px}.result-head{align-items:flex-start}.file-identity p{line-height:1.45}.target-editor{gap:12px}.fields-grid{padding:12px}}
@media (max-width:380px){.xj-hero{gap:9px}.hero-subtitle{display:none}.step-node{font-size:12px}.step-node b{width:24px;height:24px}.xj-body{padding-inline:12px}.xj-footer{padding-inline:12px}.touch-button{padding-inline:12px}}
</style>
