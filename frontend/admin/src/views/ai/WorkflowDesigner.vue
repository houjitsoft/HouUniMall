<template>
  <div class="wf-designer" v-loading="pageLoading">
    <!-- 顶部条 -->
    <div class="wf-topbar">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <span class="wf-topbar__title">{{ form.name || '新建工作流' }}</span>
      <el-tag v-if="form.code" size="small" type="info" effect="plain">{{ form.code }}</el-tag>
      <el-tag v-if="form.version" size="small" effect="plain">v{{ form.version }}</el-tag>
      <el-tag v-if="form.status === 1" size="small" type="success" effect="plain">已启用</el-tag>
      <div class="wf-topbar__spacer" />
      <el-button :loading="validating" @click="onValidate">
        <el-icon><CircleCheck /></el-icon>
        校验
      </el-button>
      <el-button @click="openRun">
        <el-icon><VideoPlay /></el-icon>
        试运行
      </el-button>
      <el-button type="primary" :loading="saving" @click="onSave">
        <el-icon><Check /></el-icon>
        保存
      </el-button>
    </div>

    <div class="wf-body">
      <!-- 左侧：节点面板 -->
      <div class="wf-palette">
        <div class="wf-palette__title">节点</div>
        <div
          v-for="t in NODE_TYPES"
          :key="t.type"
          class="wf-palette__item"
          :class="{ 'wf-palette__item--disabled': t.type === 'start' && hasStart }"
          @click="addNode(t.type)"
        >
          <span class="wf-palette__dot" :style="{ background: t.color }" />
          <div class="wf-palette__text">
            <div class="wf-palette__label">{{ t.label }}</div>
            <div class="wf-palette__desc">{{ t.desc }}</div>
          </div>
        </div>
        <div class="wf-palette__title wf-palette__title--mt">基本信息</div>
        <el-form label-position="top" size="small" class="wf-palette__form">
          <el-form-item label="编码">
            <el-input v-model="form.code" :disabled="!isCreate" placeholder="如 im_reply_flow" />
          </el-form-item>
          <el-form-item label="名称">
            <el-input v-model="form.name" placeholder="如 IM 客服应答工作流" />
          </el-form-item>
          <el-form-item label="所属端">
            <el-select v-model="form.terminal" style="width: 100%">
              <el-option v-for="t in TERMINALS" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="默认模型">
            <el-select v-model="form.modelCode" clearable placeholder="留空则用系统默认" style="width: 100%">
              <el-option v-for="m in models" :key="m.code || m.name" :label="modelLabel(m)" :value="m.code || m.name" />
            </el-select>
          </el-form-item>
          <el-form-item label="说明">
            <el-input v-model="form.description" type="textarea" :rows="2" placeholder="一句话说明这条流程做什么" />
          </el-form-item>
        </el-form>
        <div class="wf-palette__hint">
          提示：提示词里用 <code>&#123;&#123;变量名&#125;&#125;</code> 引用上游产出；选中节点后按 Delete 可删除，点右侧端口拖到另一个节点即可连线。
        </div>
      </div>

      <!-- 中间：画布 -->
      <div ref="canvasRef" class="wf-canvas" @click="onCanvasClick">
        <svg class="wf-edges" :width="canvasSize.w" :height="canvasSize.h">
          <path
            v-for="e in edges"
            :key="e.id"
            :d="edgePath(e)"
            :stroke="e.id === selectedEdgeId ? '#f56c6c' : '#c0c4cc'"
            :stroke-width="e.id === selectedEdgeId ? 3 : 2"
            fill="none"
            marker-end="url(#wf-arrow)"
          />
          <path
            v-for="e in edges"
            :key="e.id + '-hit'"
            :d="edgePath(e)"
            stroke="transparent"
            stroke-width="12"
            fill="none"
            class="wf-edges__hit"
            @click.stop="selectedEdgeId = e.id"
          />
          <path v-if="linking" :d="linkingPath()" stroke="#409eff" stroke-width="2" fill="none" stroke-dasharray="5 4" />
          <defs>
            <marker id="wf-arrow" markerWidth="8" markerHeight="8" refX="7" refY="3" orient="auto">
              <path d="M0,0 L0,6 L7,3 z" fill="#c0c4cc" />
            </marker>
          </defs>
        </svg>

        <div
          v-for="n in nodes"
          :key="n.id"
          class="wf-node"
          :class="{ 'wf-node--active': n.id === selectedId }"
          :style="{ left: n.x + 'px', top: n.y + 'px', borderColor: typeMeta(n.type).color }"
          @mousedown.stop="startDragNode(n, $event)"
          @click.stop="selectedId = n.id"
          @mouseenter="hoverNodeId = n.id"
          @mouseleave="hoverNodeId = null"
        >
          <div class="wf-node__head">
            <span class="wf-node__dot" :style="{ background: typeMeta(n.type).color }" />
            <span class="wf-node__name">{{ n.name }}</span>
            <el-tag size="small" effect="plain" type="info">{{ typeMeta(n.type).label }}</el-tag>
          </div>
          <div class="wf-node__brief">{{ nodeBrief(n) }}</div>

          <!-- 输入端口 -->
          <span v-if="n.type !== 'start'" class="wf-port wf-port--in" />
          <!-- 输出端口 -->
          <template v-if="n.type === 'condition'">
            <span
              class="wf-port wf-port--out wf-port--true"
              @mousedown.stop="startLink(n, 'true', $event)"
              title="命中分支"
            />
            <span
              class="wf-port wf-port--out wf-port--false"
              @mousedown.stop="startLink(n, 'false', $event)"
              title="未命中分支"
            />
            <span class="wf-port__tag wf-port__tag--true">是</span>
            <span class="wf-port__tag wf-port__tag--false">否</span>
          </template>
          <span v-else-if="n.type !== 'end'" class="wf-port wf-port--out" @mousedown.stop="startLink(n, 'out', $event)" />
        </div>
      </div>

      <!-- 右侧：属性面板 -->
      <div class="wf-inspector">
        <div class="wf-inspector__title">属性</div>

        <template v-if="selectedEdgeId">
          <el-alert type="info" :closable="false" show-icon title="已选中一条连线" />
          <el-button type="danger" plain class="wf-inspector__block" @click="removeEdge">删除这条连线</el-button>
        </template>

        <template v-else-if="selectedNode">
          <el-form label-position="top" size="small">
            <el-form-item label="节点名称">
              <el-input v-model="selectedNode.name" />
            </el-form-item>

            <!-- 开始：入参变量 -->
            <template v-if="selectedNode.type === 'start'">
              <div class="wf-inspector__sub">入参变量</div>
              <div v-for="(v, i) in selectedNode.config.inputs" :key="i" class="wf-var-row">
                <el-input v-model="v.name" placeholder="变量名" style="width: 88px" />
                <el-input v-model="v.label" placeholder="显示名" style="width: 84px" />
                <el-input v-model="v.default" placeholder="默认值" />
                <el-button link type="danger" @click="selectedNode.config.inputs.splice(i, 1)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
              <el-button size="small" plain @click="addInputVar">+ 添加变量</el-button>
            </template>

            <!-- 模型调用 -->
            <template v-else-if="selectedNode.type === 'llm'">
              <el-form-item label="模型（留空用工作流默认）">
                <el-select v-model="selectedNode.config.modelCode" clearable style="width: 100%">
                  <el-option v-for="m in models" :key="m.code || m.name" :label="modelLabel(m)" :value="m.code || m.name" />
                </el-select>
              </el-form-item>
              <div class="wf-two">
                <el-form-item label="温度">
                  <el-input-number v-model="selectedNode.config.temperature" :min="0" :max="2" :step="0.1" size="small" />
                </el-form-item>
                <el-form-item label="最大 Token">
                  <el-input-number v-model="selectedNode.config.maxTokens" :min="64" :max="8192" :step="64" size="small" />
                </el-form-item>
              </div>
              <el-form-item label="系统提示词">
                <el-input v-model="selectedNode.config.systemPrompt" type="textarea" :rows="4" placeholder="定义角色与约束" />
              </el-form-item>
              <el-form-item label="用户提示词">
                <el-input
                  v-model="selectedNode.config.userPrompt"
                  type="textarea"
                  :rows="5"
                  placeholder="可用 {{变量名}}，如：用户问题：{{question}}"
                />
              </el-form-item>
              <el-form-item label="输出变量名">
                <el-input v-model="selectedNode.config.outputVar" placeholder="如 reply，下游用 {{reply}} 引用" />
              </el-form-item>
              <div class="wf-inspector__vars">
                可引用：
                <el-tag v-for="v in varOptions" :key="v" size="small" effect="plain" class="wf-tag-var">{{ brace(v) }}</el-tag>
                <span v-if="!varOptions.length" class="wf-inspector__muted">（暂无上游变量）</span>
              </div>
            </template>

            <!-- 条件分支 -->
            <template v-else-if="selectedNode.type === 'condition'">
              <el-form-item label="判断变量">
                <el-select v-model="selectedNode.config.varName" filterable allow-create style="width: 100%">
                  <el-option v-for="v in varOptions" :key="v" :label="v" :value="v" />
                </el-select>
              </el-form-item>
              <el-form-item label="判断方式">
                <el-select v-model="selectedNode.config.operator" style="width: 100%">
                  <el-option label="包含" value="contains" />
                  <el-option label="不包含" value="notcontains" />
                  <el-option label="等于" value="eq" />
                  <el-option label="不等于" value="neq" />
                  <el-option label="大于" value="gt" />
                  <el-option label="大于等于" value="gte" />
                  <el-option label="小于" value="lt" />
                  <el-option label="小于等于" value="lte" />
                  <el-option label="为空" value="isempty" />
                  <el-option label="不为空" value="notempty" />
                </el-select>
              </el-form-item>
              <el-form-item v-if="!['isempty', 'notempty'].includes(selectedNode.config.operator)" label="比较值">
                <el-input v-model="selectedNode.config.value" placeholder="如 negative" />
              </el-form-item>
              <el-alert type="warning" :closable="false" show-icon title="右侧「是」端口接命中分支，「否」端口接未命中分支" />
            </template>

            <!-- 变量加工 -->
            <template v-else-if="selectedNode.type === 'transform'">
              <el-form-item label="模板">
                <el-input v-model="selectedNode.config.template" type="textarea" :rows="5" placeholder="如：订单 {{orderNo}} 已发货" />
              </el-form-item>
              <el-form-item label="输出变量名">
                <el-input v-model="selectedNode.config.outputVar" />
              </el-form-item>
              <div class="wf-inspector__vars">
                可引用：
                <el-tag v-for="v in varOptions" :key="v" size="small" effect="plain" class="wf-tag-var">{{ brace(v) }}</el-tag>
              </div>
            </template>

            <!-- 结束 -->
            <template v-else-if="selectedNode.type === 'end'">
              <el-form-item label="输出模板">
                <el-input v-model="selectedNode.config.outputTemplate" type="textarea" :rows="4" placeholder="如 {{reply}}" />
              </el-form-item>
              <div class="wf-inspector__vars">
                可引用：
                <el-tag v-for="v in varOptions" :key="v" size="small" effect="plain" class="wf-tag-var">{{ brace(v) }}</el-tag>
              </div>
            </template>
          </el-form>
          <el-button type="danger" plain class="wf-inspector__block" @click="removeNode(selectedNode)">删除节点</el-button>
        </template>

        <el-empty v-else description="选中一个节点或连线查看属性" :image-size="60" />
      </div>
    </div>

    <!-- 试运行 -->
    <el-dialog v-model="runVisible" title="试运行" width="760px" :close-on-click-modal="false">
      <el-form label-position="top" size="small">
        <el-form-item v-for="v in startInputs" :key="v.name" :label="v.label || v.name">
          <el-input v-model="runInputs[v.name]" type="textarea" :rows="2" :placeholder="v.default || ''" />
        </el-form-item>
        <el-empty v-if="!startInputs.length" description="「开始」节点没有声明入参变量" :image-size="50" />
      </el-form>
      <el-alert
        v-if="runResult"
        :type="runResult.status === 'success' ? 'success' : 'error'"
        :closable="false"
        :title="runResult.status === 'success' ? `运行成功 · 耗时 ${runResult.costMs} ms` : `运行失败：${runResult.errorMsg}`"
        class="wf-run__alert"
      />
      <div v-if="runResult?.status === 'success'" class="wf-run__output">
        <div class="wf-run__label">最终输出</div>
        <div class="wf-run__text">{{ runResult.outputs?.text }}</div>
      </div>
      <el-table v-if="runResult?.traces?.length" :data="runResult.traces" size="small" max-height="260">
        <el-table-column prop="name" label="节点" width="120" />
        <el-table-column prop="type" label="类型" width="90" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'failed' ? 'danger' : 'success'" effect="plain">
              {{ row.status === 'failed' ? '失败' : '成功' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costMs" label="耗时(ms)" width="90" />
        <el-table-column label="输出">
          <template #default="{ row }">
            <span class="wf-run__clip">{{ clip(row.output) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="runVisible = false">关闭</el-button>
        <el-button type="primary" :loading="running" @click="doRun">运行</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Check,
  CircleCheck,
  Delete,
  VideoPlay
} from '@element-plus/icons-vue'
import { workflowApi } from '@/api/ai/workflow'
import { aiApi } from '@/api/ai/ai'

const route = useRoute()
const router = useRouter()

const NODE_WIDTH = 208
const NODE_HEIGHT = 62

const NODE_TYPES = [
  { type: 'start', label: '开始', color: '#67c23a', desc: '声明入参变量' },
  { type: 'llm', label: '模型调用', color: '#409eff', desc: '调大模型产出变量' },
  { type: 'condition', label: '条件分支', color: '#e6a23c', desc: '按变量走是/否' },
  { type: 'transform', label: '变量加工', color: '#909399', desc: '纯文本拼装' },
  { type: 'end', label: '结束', color: '#f56c6c', desc: '定义最终输出' }
]

const TERMINALS = [
  { value: 'merchant', label: '商家端' },
  { value: 'frontend', label: '前端' },
  { value: 'admin', label: '管理后台' },
  { value: 'im', label: 'IM 客服' }
]

const pageLoading = ref(false)
const saving = ref(false)
const validating = ref(false)
const running = ref(false)
const canvasRef = ref(null)
const models = ref([])

const isCreate = ref(true)
const form = reactive({
  code: '',
  name: '',
  terminal: 'merchant',
  scene: '',
  description: '',
  status: 0,
  modelCode: '',
  version: null
})

const nodes = ref([])
const edges = ref([])
const selectedId = ref('')
const selectedEdgeId = ref('')
const hoverNodeId = ref('')
const linking = ref(null)
const canvasSize = reactive({ w: 2400, h: 1400 })

const runVisible = ref(false)
const runInputs = reactive({})
const runResult = ref(null)

const brace = (name) => '{{' + name + '}}'

const typeMeta = (type) => NODE_TYPES.find((t) => t.type === type) || NODE_TYPES[1]
const modelLabel = (m) => (m.name ? `${m.name}${m.code ? ' · ' + m.code : ''}` : String(m.code || m))
const hasStart = computed(() => nodes.value.some((n) => n.type === 'start'))
const selectedNode = computed(() => nodes.value.find((n) => n.id === selectedId.value) || null)
const startInputs = computed(() => {
  const start = nodes.value.find((n) => n.type === 'start')
  return start?.config?.inputs || []
})

/** 可引用变量：开始节点入参 + 所有模型/加工节点的输出变量 */
const varOptions = computed(() => {
  const vars = []
  startInputs.value.forEach((v) => v.name && vars.push(v.name))
  nodes.value.forEach((n) => {
    const out = n.config?.outputVar
    if (out && n.id !== selectedId.value) vars.push(out)
  })
  return Array.from(new Set(vars.filter(Boolean)))
})

const defaultConfig = (type) => {
  switch (type) {
    case 'start':
      return { inputs: [{ name: 'question', label: '用户输入', type: 'string', default: '' }] }
    case 'llm':
      return { modelCode: '', temperature: 0.7, maxTokens: 1024, systemPrompt: '', userPrompt: '', outputVar: 'reply' }
    case 'condition':
      return { varName: '', operator: 'contains', value: '' }
    case 'transform':
      return { template: '', outputVar: 'text' }
    case 'end':
      return { outputTemplate: '{{reply}}' }
    default:
      return {}
  }
}

let seq = 1
const nextId = (type) => `${type}_${Date.now().toString(36)}${(seq++).toString(36)}`

const nodeBrief = (n) => {
  const c = n.config || {}
  if (n.type === 'start') return `入参 ${(c.inputs || []).map((i) => i.name).join('、') || '（无）'}`
  if (n.type === 'llm') return c.userPrompt ? String(c.userPrompt).slice(0, 40) : '（未填提示词）'
  if (n.type === 'condition') return c.varName ? `${c.varName} ${c.operator} ${c.value || ''}` : '（未选变量）'
  if (n.type === 'transform') return c.template ? String(c.template).slice(0, 40) : '（未填模板）'
  if (n.type === 'end') return c.outputTemplate ? String(c.outputTemplate).slice(0, 40) : '（未填输出模板）'
  return ''
}

// ---------------- 端口坐标与连线 ----------------

const portOut = (node, handle) => {
  if (node.type === 'condition') {
    return { x: node.x + NODE_WIDTH, y: node.y + (handle === 'false' ? 46 : 24) }
  }
  return { x: node.x + NODE_WIDTH, y: node.y + NODE_HEIGHT / 2 }
}
const portIn = (node) => ({ x: node.x, y: node.y + NODE_HEIGHT / 2 })

const edgePath = (e) => {
  const from = nodes.value.find((n) => n.id === e.source)
  const to = nodes.value.find((n) => n.id === e.target)
  if (!from || !to) return ''
  const p1 = portOut(from, e.sourceHandle)
  const p2 = portIn(to)
  const dx = Math.max(40, Math.abs(p2.x - p1.x) / 2)
  return `M ${p1.x} ${p1.y} C ${p1.x + dx} ${p1.y}, ${p2.x - dx} ${p2.y}, ${p2.x} ${p2.y}`
}

const linkingPath = () => {
  if (!linking.value) return ''
  const { x, y, tx, ty } = linking.value
  const dx = Math.max(30, Math.abs(tx - x) / 2)
  return `M ${x} ${y} C ${x + dx} ${y}, ${tx - dx} ${ty}, ${tx} ${ty}`
}

// ---------------- 画布交互 ----------------

const canvasPoint = (ev) => {
  const rect = canvasRef.value.getBoundingClientRect()
  return {
    x: ev.clientX - rect.left + canvasRef.value.scrollLeft,
    y: ev.clientY - rect.top + canvasRef.value.scrollTop
  }
}

let dragOffset = null
const startDragNode = (node, ev) => {
  selectedId.value = node.id
  selectedEdgeId.value = ''
  const p = canvasPoint(ev)
  dragOffset = { node, dx: p.x - node.x, dy: p.y - node.y }
  window.addEventListener('mousemove', onDragMove)
  window.addEventListener('mouseup', onDragUp)
}
const onDragMove = (ev) => {
  if (!dragOffset) return
  const p = canvasPoint(ev)
  dragOffset.node.x = Math.max(8, p.x - dragOffset.dx)
  dragOffset.node.y = Math.max(8, p.y - dragOffset.dy)
  growCanvas(dragOffset.node)
}
const onDragUp = () => {
  dragOffset = null
  window.removeEventListener('mousemove', onDragMove)
  window.removeEventListener('mouseup', onDragUp)
}

const startLink = (node, handle, ev) => {
  const p = portOut(node, handle)
  linking.value = { source: node.id, handle, x: p.x, y: p.y, tx: p.x, ty: p.y }
  window.addEventListener('mousemove', onLinkMove)
  window.addEventListener('mouseup', onLinkUp)
}
const onLinkMove = (ev) => {
  if (!linking.value) return
  const p = canvasPoint(ev)
  linking.value.tx = p.x
  linking.value.ty = p.y
}
const onLinkUp = () => {
  const target = hoverNodeId.value
  const link = linking.value
  linking.value = null
  window.removeEventListener('mousemove', onLinkMove)
  window.removeEventListener('mouseup', onLinkUp)
  if (!link || !target || target === link.source) return
  if (nodes.value.find((n) => n.id === target)?.type === 'start') {
    ElMessage.warning('「开始」节点不能作为连线终点')
    return
  }
  if (edges.value.some((e) => e.source === link.source && e.sourceHandle === link.handle && e.target === target)) {
    return
  }
  edges.value.push({ id: `e_${Date.now().toString(36)}${(seq++).toString(36)}`, source: link.source, sourceHandle: link.handle, target })
}

const growCanvas = (node) => {
  canvasSize.w = Math.max(canvasSize.w, node.x + NODE_WIDTH + 320)
  canvasSize.h = Math.max(canvasSize.h, node.y + NODE_HEIGHT + 220)
}

const onCanvasClick = () => {
  selectedId.value = ''
  selectedEdgeId.value = ''
}

const addNode = (type) => {
  if (type === 'start' && hasStart.value) {
    ElMessage.warning('只能有一个「开始」节点')
    return
  }
  const node = {
    id: nextId(type),
    type,
    name: typeMeta(type).label,
    x: 60 + (nodes.value.length % 5) * 40,
    y: 40 + nodes.value.length * 96,
    config: defaultConfig(type)
  }
  nodes.value.push(node)
  selectedId.value = node.id
  selectedEdgeId.value = ''
  growCanvas(node)
}

const removeNode = (node) => {
  const idx = nodes.value.findIndex((n) => n.id === node.id)
  if (idx < 0) return
  nodes.value.splice(idx, 1)
  edges.value = edges.value.filter((e) => e.source !== node.id && e.target !== node.id)
  selectedId.value = ''
}

const removeEdge = () => {
  edges.value = edges.value.filter((e) => e.id !== selectedEdgeId.value)
  selectedEdgeId.value = ''
}

const addInputVar = () => {
  const start = nodes.value.find((n) => n.type === 'start')
  if (!start) return
  start.config.inputs = start.config.inputs || []
  start.config.inputs.push({ name: `var${start.config.inputs.length + 1}`, label: '新变量', type: 'string', default: '' })
}

const onKeyDown = (ev) => {
  if (['INPUT', 'TEXTAREA'].includes(ev.target?.tagName)) return
  if (ev.key !== 'Delete' && ev.key !== 'Backspace') return
  if (selectedEdgeId.value) removeEdge()
  else if (selectedNode.value) removeNode(selectedNode.value)
}

onMounted(() => window.addEventListener('keydown', onKeyDown))
onBeforeUnmount(() => window.removeEventListener('keydown', onKeyDown))

// ---------------- 读 / 存 / 校验 / 运行 ----------------

const localErrors = () => {
  const errors = []
  if (!form.code) errors.push('请先填写工作流编码')
  if (!form.name) errors.push('请先填写工作流名称')
  if (nodes.value.filter((n) => n.type === 'start').length !== 1) errors.push('必须有且仅有 1 个「开始」节点')
  if (!nodes.value.some((n) => n.type === 'end')) errors.push('至少要有 1 个「结束」节点')
  nodes.value.forEach((n) => {
    if (n.type === 'llm' && !n.config?.userPrompt) errors.push(`模型节点「${n.name}」未填用户提示词`)
    if (n.type === 'condition' && !n.config?.varName) errors.push(`条件节点「${n.name}」未选判断变量`)
  })
  return errors
}

const onValidate = async () => {
  const errors = localErrors()
  if (errors.length) {
    ElMessageBox.alert(errors.join('<br/>'), '校验未通过', { dangerouslyUseHTMLString: true, type: 'warning' })
    return
  }
  validating.value = true
  try {
    const res = await workflowApi.validate({ graph: JSON.stringify({ nodes: nodes.value, edges: edges.value }) })
    if (res.code === 1 && res.data?.valid) ElMessage.success('画布校验通过')
    else ElMessage.warning('校验未通过：' + (res.data?.errors || []).join('；'))
  } catch (e) {
    ElMessage.error(e.message || '校验失败')
  } finally {
    validating.value = false
  }
}

/** 从智能体页以新窗口打开时，保存后通知上一页刷新工作流下拉（独立打开时没有 opener，静默忽略） */
const notifyOpener = () => {
  try {
    window.opener?.postMessage(
      { type: 'ai-workflow-saved', code: form.code, name: form.name },
      window.location.origin
    )
  } catch (e) {
    /* 忽略：独立标签页打开时无 opener */
  }
}

const onSave = async () => {
  const errors = localErrors()
  if (errors.length) {
    ElMessage.warning(errors[0])
    return
  }
  saving.value = true
  try {
    const res = await workflowApi.save({
      code: form.code,
      name: form.name,
      terminal: form.terminal,
      scene: form.scene,
      description: form.description,
      status: form.status,
      modelCode: form.modelCode || '',
      graph: JSON.stringify({ nodes: nodes.value, edges: edges.value, viewport: { x: 0, y: 0, zoom: 1 } })
    })
    if (res.code === 1) {
      ElMessage.success('已保存')
      isCreate.value = false
      form.version = res.data?.version || form.version
      notifyOpener()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const openRun = () => {
  runResult.value = null
  Object.keys(runInputs).forEach((k) => delete runInputs[k])
  startInputs.value.forEach((v) => {
    runInputs[v.name] = v.default || ''
  })
  runVisible.value = true
}

const clip = (v) => {
  const s = v == null ? '' : typeof v === 'object' ? JSON.stringify(v) : String(v)
  return s.length > 160 ? s.slice(0, 160) + '…' : s
}

const doRun = async () => {
  if (!form.code) {
    ElMessage.warning('请先保存工作流再试运行')
    return
  }
  running.value = true
  runResult.value = null
  try {
    const res = await workflowApi.run({ code: form.code, inputs: { ...runInputs } })
    if (res.code === 1) runResult.value = res.data
    else ElMessage.error(res.msg || '运行失败')
  } catch (e) {
    ElMessage.error(e.message || '运行失败')
  } finally {
    running.value = false
  }
}

const loadModels = async () => {
  try {
    const res = await aiApi.getEnabledModels()
    if (res.code === 1 && Array.isArray(res.data)) models.value = res.data
  } catch (e) {
    /* 失败不阻塞 */
  }
}

const loadDetail = async (code) => {
  pageLoading.value = true
  try {
    const res = await workflowApi.getDetail(code)
    if (res.code !== 1 || !res.data || !res.data.code) {
      ElMessage.warning('工作流不存在：' + code)
      router.replace('/ai/workflows')
      return
    }
    const d = res.data
    form.code = d.code
    form.name = d.name || ''
    form.terminal = d.terminal || 'merchant'
    form.scene = d.scene || ''
    form.description = d.description || ''
    form.status = d.status ?? 0
    form.modelCode = d.modelCode || ''
    form.version = d.version
    const g = typeof d.graph === 'string' ? JSON.parse(d.graph || '{}') : d.graph || {}
    nodes.value = Array.isArray(g.nodes) ? g.nodes : []
    edges.value = Array.isArray(g.edges) ? g.edges : []
    nodes.value.forEach(growCanvas)
  } catch (e) {
    ElMessage.error(e.message || '加载工作流失败')
  } finally {
    pageLoading.value = false
  }
}

const goBack = () => router.push('/ai/workflows')

onMounted(async () => {
  await loadModels()
  const code = route.query.code
  if (code) {
    isCreate.value = false
    await loadDetail(code)
  } else {
    isCreate.value = true
    addNode('start')
    addNode('end')
  }
})
</script>

<style scoped>
.wf-designer {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  min-height: 620px;
}
.wf-topbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 10px;
}
.wf-topbar__title {
  font-size: 16px;
  font-weight: 600;
}
.wf-topbar__spacer {
  flex: 1;
}
.wf-body {
  flex: 1;
  display: flex;
  gap: 10px;
  min-height: 0;
}
.wf-palette {
  width: 250px;
  flex: 0 0 250px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  overflow-y: auto;
}
.wf-palette__title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
.wf-palette__title--mt {
  margin-top: 14px;
  border-top: 1px dashed #ebeef5;
  padding-top: 12px;
}
.wf-palette__item {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  padding: 8px 10px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.15s;
}
.wf-palette__item:hover {
  border-color: #409eff;
  background: #f5f9ff;
}
.wf-palette__item--disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
.wf-palette__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 5px;
}
.wf-palette__label {
  font-size: 13px;
  font-weight: 500;
}
.wf-palette__desc {
  font-size: 11px;
  color: #909399;
}
.wf-palette__form {
  margin-top: 4px;
}
.wf-palette__hint {
  margin-top: 12px;
  font-size: 11px;
  line-height: 1.6;
  color: #909399;
  background: #fafafa;
  padding: 8px;
  border-radius: 6px;
}
.wf-palette__hint code {
  background: #f0f2f5;
  padding: 0 3px;
  border-radius: 3px;
}
.wf-canvas {
  flex: 1;
  position: relative;
  overflow: auto;
  background-color: #fafafa;
  background-image: radial-gradient(#dcdfe6 1px, transparent 1px);
  background-size: 20px 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  min-height: 520px;
}
.wf-edges {
  position: absolute;
  left: 0;
  top: 0;
  pointer-events: none;
}
.wf-edges__hit {
  pointer-events: stroke;
  cursor: pointer;
}
.wf-node {
  position: absolute;
  width: 208px;
  min-height: 62px;
  background: #fff;
  border: 2px solid #409eff;
  border-radius: 8px;
  padding: 8px 10px;
  cursor: move;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  user-select: none;
}
.wf-node--active {
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.25);
}
.wf-node__head {
  display: flex;
  align-items: center;
  gap: 6px;
}
.wf-node__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.wf-node__name {
  font-size: 13px;
  font-weight: 600;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.wf-node__brief {
  margin-top: 4px;
  font-size: 11px;
  color: #909399;
  line-height: 1.5;
  max-height: 32px;
  overflow: hidden;
}
.wf-port {
  position: absolute;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #fff;
  border: 2px solid #409eff;
  cursor: crosshair;
}
.wf-port--in {
  left: -7px;
  top: 25px;
  cursor: default;
  border-color: #c0c4cc;
}
.wf-port--out {
  right: -7px;
  top: 25px;
}
.wf-port--true {
  right: -7px;
  top: 18px;
}
.wf-port--false {
  right: -7px;
  top: 40px;
}
.wf-port__tag {
  position: absolute;
  right: -30px;
  font-size: 10px;
  color: #909399;
}
.wf-port__tag--true {
  top: 15px;
}
.wf-port__tag--false {
  top: 37px;
}
.wf-inspector {
  width: 330px;
  flex: 0 0 330px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  overflow-y: auto;
}
.wf-inspector__title {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 10px;
}
.wf-inspector__sub {
  font-size: 12px;
  color: #606266;
  margin: 6px 0;
}
.wf-inspector__block {
  width: 100%;
  margin-top: 12px;
}
.wf-inspector__vars {
  font-size: 11px;
  color: #606266;
  line-height: 2;
}
.wf-inspector__muted {
  color: #c0c4cc;
}
.wf-tag-var {
  margin: 0 4px 4px 0;
}
.wf-two {
  display: flex;
  gap: 10px;
}
.wf-var-row {
  display: flex;
  gap: 6px;
  align-items: center;
  margin-bottom: 6px;
}
.wf-run__alert {
  margin: 10px 0;
}
.wf-run__output {
  margin-bottom: 12px;
}
.wf-run__label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.wf-run__text {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 10px;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre-wrap;
  max-height: 200px;
  overflow: auto;
}
.wf-run__clip {
  font-size: 12px;
  color: #606266;
}
</style>
