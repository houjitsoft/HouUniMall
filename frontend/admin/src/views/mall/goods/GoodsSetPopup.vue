<template>
  <el-dialog
    v-model="visible"
    title="商品设置"
    width="500px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      label-width="100px"
      style="margin-right:12px"
    >
      <el-form-item label="虚拟销量">
        <el-input
          v-model="formData.virtualSaleNum"
          placeholder="请输入虚拟销量"
          clearable
        />
      </el-form-item>
      <el-form-item label="虚拟浏览量">
        <el-input
          v-model="formData.virtualVisitNum"
          placeholder="请输入虚拟浏览量"
          clearable
        />
      </el-form-item>
      <el-form-item label="排序">
        <el-input
          v-model="formData.sort"
          placeholder="请输入排序"
          clearable
        />
        <div style="font-size:12px;color:#909399;margin-top:4px">默认为0，数值越大越排前</div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { mallGoodsGoodsSet } from '@/api/mall/goodsAdmin.js'

const emit = defineEmits(['success', 'close'])

const visible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const formData = reactive({
  id: 0,
  virtualSaleNum: 0,
  visitNum: 0,
  sort: 0,
  virtualVisitNum: 0
})

/**
 * 打开弹窗并填充数据
 * @param {Object} data - 商品设置数据 {id, virtualSaleNum, visitNum, sort, virtualVisitNum}
 */
const open = (data) => {
  formData.id = data.id
  formData.virtualSaleNum = data.virtualSaleNum ?? 0
  formData.visitNum = data.visitNum ?? 0
  formData.sort = data.sort ?? 0
  formData.virtualVisitNum = data.virtualVisitNum ?? 0
  visible.value = true
}

/**
 * 提交设置
 */
const handleSubmit = async () => {
  submitting.value = true
  try {
    const res = await mallGoodsGoodsSet(formData)
    if (res && res.code === 1) {
      ElMessage.success('设置成功')
      visible.value = false
      emit('success')
    } else {
      ElMessage.error(res?.message || '设置失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '设置失败')
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  visible.value = false
  emit('close')
}

defineExpose({ open })
</script>
