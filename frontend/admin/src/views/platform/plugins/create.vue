<template>
  <div class="create-page">
    <header class="page-header">
      <div class="header-text">
        <h1>创建插件</h1>
        <p>选择归属业务服务，生成插件目录与前端入口，并登记到插件中心与市场目录</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Back" @click="goBack">返回列表</el-button>
      </div>
    </header>

    <el-steps class="steps" :active="step" align-center finish-status="success">
      <el-step title="基础信息" description="编码、名称与授权" />
      <el-step title="服务与契约" description="菜单与适用端" />
      <el-step title="确认生成" description="落盘与入库" />
    </el-steps>

    <!-- 步骤 1：基础信息 -->
    <section v-show="step === 0" class="panel">
      <el-form class="form" label-width="110px">
        <el-form-item label="插件形态">
          <el-radio-group v-model="pluginRuntime">
            <el-radio-button label="standalone">服务产品（standalone）</el-radio-button>
            <el-radio-button label="embedded">内嵌扩展（embedded）</el-radio-button>
          </el-radio-group>
          <div class="field-tip">
            宿主就是所选的已有业务服务。服务产品的业务代码位于 backend/paas/{hostService}/，随服务编译发布，脚手架只保存描述与前端；内嵌扩展位于 plugins/{hostService}/{code}/runtime/java/，单独构建 JAR 由宿主加载。两者都不新建服务、进程或端口。
          </div>
        </el-form-item>
        <el-form-item label="归属服务" required>
          <el-select v-model="form.hostService" class="host-service-select" filterable placeholder="选择业务服务">
            <el-option v-for="service in serviceOptions" :key="service.name" :value="service.name" :label="`${service.name} · ${service.description}`" />
          </el-select>
        </el-form-item>
        <el-form-item label="插件编码" required>
          <el-input v-model="form.code" class="code-input" :maxlength="32" placeholder="小写字母开头，如 visitor-log" @input="form.code = form.code.toLowerCase()" />
          <div class="field-tip">插件编码创建后不可修改，目录按归属服务组织</div>
        </el-form-item>
        <el-form-item label="插件名称" required>
          <el-input v-model="form.name" :maxlength="128" placeholder="如：访客管理" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.cateId" filterable placeholder="选择插件分类">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="Number(c.id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" :maxlength="500" show-word-limit placeholder="一句话说明插件的业务范围" />
        </el-form-item>
        <el-form-item label="版本号">
          <el-input v-model="form.version" class="version-input" placeholder="1.0.0" />
        </el-form-item>
        <el-form-item label="授权类型">
          <el-radio-group v-model="form.licenseType">
            <el-radio-button label="FREE">免费</el-radio-button>
            <el-radio-button label="PAID">付费</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.licenseType === 'PAID'" label="定价（元）">
          <el-input-number v-model="priceYuan" :min="0" :precision="2" :step="100" />
        </el-form-item>
      </el-form>
    </section>

    <!-- 步骤 2：服务与契约 -->
    <section v-show="step === 1" class="panel">
      <el-form class="form" label-width="110px">
        <el-form-item label="菜单路径">
          <el-input v-model="form.menuPath" placeholder="/plugin/xxx" />
          <div class="field-tip">默认 /plugin/{{ form.code || '{code}' }}，注册后出现在插件前端菜单</div>
        </el-form-item>
        <el-form-item label="菜单标题">
          <el-input v-model="form.menuTitle" :maxlength="64" placeholder="默认同插件名称" />
        </el-form-item>
        <el-form-item label="适用端">
          <el-checkbox-group v-model="form.terminals">
            <el-checkbox label="admin">平台端</el-checkbox>
            <el-checkbox label="cashier">收银端</el-checkbox>
            <el-checkbox label="seller">商家端</el-checkbox>
            <el-checkbox label="mobile">移动端</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="tagsText" placeholder="英文逗号分隔，如 official,trial" />
        </el-form-item>
      </el-form>
    </section>

    <!-- 步骤 3：确认生成 -->
    <section v-show="step === 2" class="panel preview-panel">
      <div class="preview-grid">
        <div class="preview-block">
          <h3>将创建的工程</h3>
          <p class="preview-path">{{ rootName }}/{{ artifactDir }}</p>
          <ul class="file-tree">
            <li v-for="f in previewFiles" :key="f"><span class="dot" />{{ f }}</li>
          </ul>
        </div>
        <div class="preview-block">
          <h3>关键契约</h3>
          <ul class="contract-list">
            <li><span>插件编码</span><b>{{ form.code || '—' }}</b></li>
            <li><span>插件形态</span><b>runtime: {{ pluginRuntime }}</b></li>
            <li><span>归属服务</span><b>{{ form.hostService || '待选择' }}</b></li>
            <li><span>业务实现</span><b>{{ isService ? `backend/paas/${form.hostService || '{hostService}'}` : `${artifactDir}runtime/java` }}</b></li>
            <li><span>表前缀</span><b>hou_plugin_{{ (form.code || '{code}').replaceAll('-', '_') }}_</b></li>
            <li><span>菜单</span><b>{{ form.menuPath || '/plugin/xxx' }}</b></li>
            <li><span>入库</span><b>插件中心目录（即刻可见）+ 市场草稿（待上架）</b></li>
          </ul>
          <el-alert class="preview-alert" type="info" :closable="false" show-icon
            title="生成即真实落盘" description="点击「创建插件」后，服务端会在插件工作区真实写出上述文件并写入插件中心目录与市场库；编码冲突、目录已存在都会被拒绝。" />

        </div>
      </div>
    </section>

    <!-- 成功结果 -->
    <section v-if="created" class="panel result-panel">
      <div class="result-head">
        <el-icon class="ok-icon"><CircleCheck /></el-icon>
        <div>
          <h3>插件 {{ created.code }} 已创建</h3>
          <p>工程目录：<code>{{ created.serviceDir }}</code>（共 {{ created.files.length }} 个文件）</p>
        </div>
      </div>
      <ul class="file-tree result-files">
        <li v-for="f in created.files" :key="f"><span class="dot" />{{ f }}</li>
      </ul>
      <div class="next-steps">
        <h4>下一步</h4>
        <ol><li v-for="(s, i) in created.nextSteps" :key="i">{{ s }}</li></ol>
      </div>
      <div class="result-actions">
        <el-button type="primary" @click="goBack">返回插件列表</el-button>
        <el-button @click="resetForm">再创建一个</el-button>
      </div>
    </section>

    <footer v-if="!created" class="wizard-footer">
      <el-button v-if="step > 0" @click="step--">上一步</el-button>
      <el-button v-if="step < 2" type="primary" @click="next">下一步</el-button>
      <el-button v-else type="primary" :loading="submitting" @click="submit">
        <el-icon><Box /></el-icon>&nbsp;创建插件
      </el-button>
    </footer>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, Box, CircleCheck } from '@element-plus/icons-vue'
import { createPlugin, getPluginCategories } from '@/api/plugin-center/pluginCenter'
import serviceArchitecture from '../../../../../../architecture/services.json'

const router = useRouter()
const route = useRoute()

const CODE_PATTERN = /^[a-z][a-z0-9-]{1,31}$/
const RESERVED_CODES = ['erp', 'webpos', 'oa', 'mes', 'finance', 'activity', 'cms', 'crm',
  'im', 'logging', 'geo', 'orderfood', 'pm', 'demo-coupon', 'supplier',
  'mall', 'auth', 'userms', 'payment', 'order', 'job', 'bff', 'admin']


/** 插件形态：只从新架构 query.runtime 读取，缺省使用 standalone。 */
const normalizeRuntime = (value) => (String(value || '').trim() === 'embedded' ? 'embedded' : 'standalone')
const pluginRuntime = ref(normalizeRuntime(route.query.runtime))

const step = ref(0)
const submitting = ref(false)
const created = ref(null)
const priceYuan = ref(0)
const tagsText = ref('official')

const form = reactive({
  code: '',
  hostService: '',
  name: '',
  cateId: null,
  description: '',
  version: '1.0.0',
  licenseType: 'FREE',
  menuPath: '',
  menuTitle: '',
  terminals: ['admin']
})

const categories = ref([])
onMounted(async () => {
  try { categories.value = await getPluginCategories() }
  catch (error) { ElMessage.error(error?.message || '获取分类失败，请刷新后重试') }
})
const serviceOptions = serviceArchitecture.services

const isService = computed(() => pluginRuntime.value === 'standalone')
const rootName = 'plugins'
const artifactDir = computed(() => `${form.hostService || '{hostService}'}/${form.code || '{code}'}/`)

const previewFiles = computed(() => {
  const code = form.code || '{code}'
  const pascal = code.split('-').filter(Boolean).map((p) => p.charAt(0).toUpperCase() + p.slice(1)).join('')
  const files = ['plugin.yaml', 'README.md', 'frontend/src/index.js', 'frontend/src/pages/Home.vue']
  // embedded 才有 pom 与运行时源码；源码目录是 runtime/java，不是 Maven 默认的 src/main/java
  if (!isService.value) files.push('pom.xml', `runtime/java/com/houjiang/plugin/${code.replaceAll('-', '_')}/${pascal}Plugin.java`)
  return files
})

const validateStep = (index) => {
  if (index === 0) {
    if (!categories.value.some(category => Number(category.id) === form.cateId)) {
      ElMessage.warning('请选择插件分类')
      return false
    }
    if (!serviceOptions.some((service) => service.name === form.hostService)) {
      ElMessage.warning('请选择归属业务服务')
      return false
    }
    const code = form.code.trim()
    if (!CODE_PATTERN.test(code)) {
      ElMessage.warning('插件编码需小写字母开头，仅含小写字母/数字/连字符（2-32 位）')
      return false
    }
    if (code.endsWith('-') || code.includes('--')) {
      ElMessage.warning('编码不能以连字符结尾或包含连续连字符')
      return false
    }
    if (RESERVED_CODES.includes(code)) {
      ElMessage.warning(`编码 ${code} 与现有插件或核心模块冲突，请换一个`)
      return false
    }
    if (!form.name.trim()) {
      ElMessage.warning('请填写插件名称')
      return false
    }
    if (!/^\d+\.\d+\.\d+$/.test(form.version.trim())) {
      ElMessage.warning('版本号格式应为 x.y.z，如 1.0.0')
      return false
    }
  }
  if (index === 1) {
    if (form.menuPath && !form.menuPath.startsWith('/')) {
      ElMessage.warning('菜单路径须以 / 开头')
      return false
    }
    if (!form.terminals.length) {
      ElMessage.warning('请至少选择一个适用端')
      return false
    }
  }
  return true
}

const next = () => {
  if (!validateStep(step.value)) return
  step.value += 1
}

const submit = async () => {
  if (!validateStep(0) || !validateStep(1)) return
  submitting.value = true
  try {
    const payload = {
      runtime: pluginRuntime.value,
      hostService: form.hostService,
      code: form.code.trim(),
      name: form.name.trim(),
      version: form.version.trim() || '1.0.0',
      cateId: form.cateId,
      category: categories.value.find(category => Number(category.id) === form.cateId)?.name,
      description: form.description.trim() || form.name.trim(),
      licenseType: form.licenseType,
      priceCents: form.licenseType === 'PAID' ? Math.round((priceYuan.value || 0) * 100) : 0,
      menuPath: form.menuPath.trim() || `/plugin/${form.code.trim()}`,
      menuTitle: form.menuTitle.trim() || form.name.trim(),
      terminals: form.terminals,
      tags: tagsText.value.split(',').map((t) => t.trim()).filter(Boolean)
    }
    // request 拦截器：业务失败直接 reject（error.response.data.message 为后端文案）
    const body = await createPlugin(payload)
    created.value = body?.data ?? body
    ElMessage.success(`插件 ${created.value.code} 创建成功`)
  } catch (e) {
    const msg = e?.response?.data?.message || e?.response?.data?.msg || e?.message || '创建失败'
    ElMessage.error(msg)
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    code: '', hostService: '', name: '', description: '', version: '1.0.0',
    licenseType: 'FREE', menuPath: '', menuTitle: '', terminals: ['admin']
  })
  priceYuan.value = 0
  tagsText.value = 'official'
  created.value = null
  step.value = 0
}

const goBack = () => router.push('/platform/plugins')
</script>

<style scoped>
.create-page {
  --pl-primary: var(--hc-primary, #409eff);
  --pl-text: #303133;
  --pl-text-sub: #8a919f;
  --pl-line: #eef1f5;
  --pl-radius: 12px;
  min-height: 100%;
  padding: 24px 28px;
  box-sizing: border-box;
  color: var(--pl-text);
  background: var(--hc-content-bg, #f4f6f9);
}
.page-header { display: flex; align-items: flex-end; justify-content: space-between; }
.page-header h1 { margin: 0; font-size: 22px; font-weight: 700; }
.page-header p { margin: 5px 0 0; color: var(--pl-text-sub); font-size: 13px; }
.steps { margin: 22px 0; background: #fff; border: 1px solid var(--pl-line); border-radius: var(--pl-radius); padding: 18px 8px 10px; }
.panel {
  background: #fff;
  border: 1px solid var(--pl-line);
  border-radius: var(--pl-radius);
  padding: 28px 32px;
}
.form { max-width: 640px; }
.code-input, .version-input { width: 320px; }
.field-tip { margin-top: 4px; font-size: 12px; color: var(--pl-text-sub); line-height: 1.5; }
.preview-grid { display: grid; grid-template-columns: 1.2fr 1fr; gap: 28px; }
.preview-block h3 { margin: 0 0 10px; font-size: 15px; }
.preview-path { margin: 0 0 10px; font-size: 12px; color: var(--pl-primary); font-weight: 600; }
.file-tree { margin: 0; padding: 0; list-style: none; max-height: 320px; overflow: auto; }
.file-tree li { display: flex; align-items: center; gap: 8px; padding: 3px 0; font-size: 12.5px; color: #4b5563; font-family: 'SF Mono', Menlo, Consolas, monospace; }
.dot { width: 5px; height: 5px; border-radius: 50%; background: #c4ccd6; flex: none; }
.contract-list { margin: 0 0 14px; padding: 0; list-style: none; }
.contract-list li { display: flex; justify-content: space-between; gap: 16px; padding: 7px 0; border-bottom: 1px dashed var(--pl-line); font-size: 13px; }
.contract-list span { color: var(--pl-text-sub); }
.contract-list b { font-weight: 600; text-align: right; }
.preview-alert { margin-top: 4px; }
.result-head { display: flex; align-items: center; gap: 14px; margin-bottom: 16px; }
.result-head h3 { margin: 0; font-size: 17px; }
.result-head p { margin: 4px 0 0; font-size: 13px; color: var(--pl-text-sub); }
.result-head code { background: #f1f4f8; padding: 2px 6px; border-radius: 6px; font-size: 12px; }
.ok-icon { font-size: 34px; color: #10b981; }
.result-files { max-height: 260px; margin-bottom: 16px; }
.next-steps h4 { margin: 0 0 8px; font-size: 14px; }
.next-steps ol { margin: 0; padding-left: 20px; color: #4b5563; font-size: 13px; line-height: 2; }
.result-actions { margin-top: 18px; }
.wizard-footer { display: flex; justify-content: center; gap: 12px; margin-top: 24px; }
@media (max-width: 960px) {
  .preview-grid { grid-template-columns: 1fr; }
}
</style>
