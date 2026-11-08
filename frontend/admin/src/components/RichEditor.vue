<template>
  <div class="rich-editor-wrapper" :class="{ 'rich-editor-embedded': embedded }">
    <!-- 工具栏 -->
    <div class="editor-toolbar">
      <el-button-group>
        <el-button 
          size="small" 
          @click="formatText('bold')"
          :type="isActive('bold') ? 'primary' : 'default'"
          title="粗体 (Ctrl+B)"
        >
          <strong>B</strong>
        </el-button>
        <el-button 
          size="small" 
          @click="formatText('italic')"
          :type="isActive('italic') ? 'primary' : 'default'"
          title="斜体 (Ctrl+I)"
        >
          <em>I</em>
        </el-button>
        <el-button 
          size="small" 
          @click="formatText('underline')"
          :type="isActive('underline') ? 'primary' : 'default'"
          title="下划线 (Ctrl+U)"
        >
          <u>U</u>
        </el-button>
      </el-button-group>
      
      <el-divider direction="vertical" />
      
      <el-button-group>
        <el-button 
          size="small" 
          @click="formatText('justifyLeft')"
          title="左对齐"
        >
          <el-icon><AlignLeft /></el-icon>
        </el-button>
        <el-button 
          size="small" 
          @click="formatText('justifyCenter')"
          title="居中"
        >
          <el-icon><AlignCenter /></el-icon>
        </el-button>
        <el-button 
          size="small" 
          @click="formatText('justifyRight')"
          title="右对齐"
        >
          <el-icon><AlignRight /></el-icon>
        </el-button>
      </el-button-group>
      
      <el-divider direction="vertical" />
      
      <el-button-group>
        <el-button 
          size="small" 
          @click="formatText('insertUnorderedList')"
          title="无序列表"
        >
          <el-icon><List /></el-icon>
        </el-button>
        <el-button 
          size="small" 
          @click="formatText('insertOrderedList')"
          title="有序列表"
        >
          <el-icon><Sort /></el-icon>
        </el-button>
      </el-button-group>
      
      <el-divider direction="vertical" />
      
      <el-select 
        v-model="fontSize" 
        @change="formatText('fontSize')" 
        size="small"
        style="width: 80px; margin-right: 8px;"
      >
        <el-option label="12px" value="12" />
        <el-option label="14px" value="14" />
        <el-option label="16px" value="16" />
        <el-option label="18px" value="18" />
        <el-option label="20px" value="20" />
        <el-option label="24px" value="24" />
      </el-select>
      
      <el-color-picker 
        v-model="textColor" 
        @change="formatText('foreColor')" 
        size="small"
        show-alpha
      />
      
      <el-divider direction="vertical" />
      
      <el-button 
        size="small" 
        @click="formatText('removeFormat')"
        title="清除格式"
      >
        清除格式
      </el-button>
    </div>
    
    <!-- 编辑器内容区：勿用 v-html，会与 contenteditable 输入冲突导致中文反序/乱序 -->
    <div 
      ref="editorRef"
      class="editor-content"
      dir="ltr"
      contenteditable="true"
      @input="handleInput"
      @paste="handlePaste"
      @keydown="handleKeydown"
      @compositionstart="isComposing = true"
      @compositionend="handleCompositionEnd"
      :style="{ minHeight: height + 'px' }"
    ></div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted } from 'vue'
import { 
  ArrowLeft as AlignLeft, 
  ArrowRight as AlignRight, 
  CircleCheck as AlignCenter,
  List,
  Sort 
} from '@element-plus/icons-vue'
import { formatRichTextContent } from '@/utils/richText'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  height: {
    type: Number,
    default: 300
  },
  /** 嵌入合同内容区域时使用，移除外层边框 */
  embedded: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const editorRef = ref(null)
const fontSize = ref('16')
const textColor = ref('#000000')
const isComposing = ref(false)

const setEditorHtml = (html) => {
  if (!editorRef.value) return
  const safe = formatRichTextContent(html)
  if (editorRef.value.innerHTML !== safe) {
    editorRef.value.innerHTML = safe
  }
}

// 外部赋值时同步到编辑器（弹窗异步回填描述等场景）
watch(() => props.modelValue, (newVal) => {
  if (!editorRef.value || isComposing.value) return
  if (document.activeElement === editorRef.value) return
  setEditorHtml(newVal)
}, { flush: 'post', immediate: true })

onMounted(() => {
  nextTick(() => setEditorHtml(props.modelValue))
})

defineExpose({
  reload: () => setEditorHtml(props.modelValue)
})

const handleCompositionEnd = () => {
  isComposing.value = false
  handleInput()
}

// 处理输入
const handleInput = () => {
  if (!editorRef.value || isComposing.value) return
  emit('update:modelValue', editorRef.value.innerHTML)
}

// 处理粘贴（清理格式）
const handlePaste = (event) => {
  event.preventDefault()
  const text = event.clipboardData.getData('text/plain')
  try {
    document.execCommand('insertText', false, text)
  } catch (e) {
    // 降级方案：直接插入文本
    const selection = window.getSelection()
    if (selection.rangeCount > 0) {
      const range = selection.getRangeAt(0)
      range.deleteContents()
      const textNode = document.createTextNode(text)
      range.insertNode(textNode)
      range.setStartAfter(textNode)
      range.collapse(true)
      selection.removeAllRanges()
      selection.addRange(range)
    }
  }
  handleInput()
}

// 处理快捷键
const handleKeydown = (event) => {
  if (event.ctrlKey || event.metaKey) {
    switch (event.key) {
      case 'b':
        event.preventDefault()
        formatText('bold')
        break
      case 'i':
        event.preventDefault()
        formatText('italic')
        break
      case 'u':
        event.preventDefault()
        formatText('underline')
        break
    }
  }
}

// 格式化文本
const formatText = (command, value) => {
  if (!editorRef.value) return
  
  // 确保编辑器获得焦点
  editorRef.value.focus()
  
  switch (command) {
    case 'bold':
      document.execCommand('bold', false, null)
      break
    case 'italic':
      document.execCommand('italic', false, null)
      break
    case 'underline':
      document.execCommand('underline', false, null)
      break
    case 'justifyLeft':
      document.execCommand('justifyLeft', false, null)
      break
    case 'justifyCenter':
      document.execCommand('justifyCenter', false, null)
      break
    case 'justifyRight':
      document.execCommand('justifyRight', false, null)
      break
    case 'insertUnorderedList':
      document.execCommand('insertUnorderedList', false, null)
      break
    case 'insertOrderedList':
      document.execCommand('insertOrderedList', false, null)
      break
    case 'fontSize':
      // 设置字体大小
      document.execCommand('fontSize', false, '7') // 使用7作为基准
      // 然后通过CSS设置实际大小
      const selection = window.getSelection()
      if (selection.rangeCount > 0) {
        const range = selection.getRangeAt(0)
        const span = document.createElement('span')
        span.style.fontSize = fontSize.value + 'px'
        try {
          range.surroundContents(span)
        } catch (e) {
          // 如果surroundContents失败，使用其他方法
          const contents = range.extractContents()
          span.appendChild(contents)
          range.insertNode(span)
        }
      }
      break
    case 'foreColor':
      document.execCommand('foreColor', false, textColor.value)
      break
    case 'removeFormat':
      document.execCommand('removeFormat', false, null)
      break
  }
  
  // 触发更新
  nextTick(() => {
    handleInput()
  })
}

// 检查当前格式是否激活
const isActive = (command) => {
  if (!editorRef.value) return false
  try {
    return document.queryCommandState(command)
  } catch (e) {
    return false
  }
}
</script>

<style scoped>
.rich-editor-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
}

.rich-editor-wrapper.rich-editor-embedded {
  border: none;
  border-radius: 0;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  padding: 8px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
  flex-wrap: wrap;
  gap: 4px;
}

.editor-toolbar .el-divider {
  margin: 0 8px;
  height: 20px;
}

.editor-content {
  padding: 12px;
  min-height: 300px;
  max-height: 600px;
  overflow-y: auto;
  outline: none;
  line-height: 1.6;
  font-size: 14px;
  color: #303133;
  direction: ltr;
  unicode-bidi: plaintext;
  text-align: left;
  word-break: break-word;
}

.editor-content:focus {
  outline: none;
}

.editor-content p {
  margin: 8px 0;
}

.editor-content ul,
.editor-content ol {
  margin: 8px 0;
  padding-left: 24px;
}

.editor-content h1,
.editor-content h2,
.editor-content h3 {
  margin: 16px 0 8px 0;
  font-weight: bold;
}

.editor-content h1 {
  font-size: 24px;
}

.editor-content h2 {
  font-size: 20px;
}

.editor-content h3 {
  font-size: 18px;
}

.editor-content img {
  max-width: 100%;
  height: auto;
}
</style>

