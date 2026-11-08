<template>
  <el-dialog
    v-model="visible"
    :title="mode === 'edit' ? '编辑菜单' : '新增菜单'"
    width="550px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="formData" label-width="80px" :rules="formRules">
      <el-form-item label="菜单类型" prop="type">
        <el-radio-group v-model="formData.type">
          <el-radio :label="0">目录</el-radio>
          <el-radio :label="1">菜单</el-radio>
          <el-radio :label="2">按钮</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="父级菜单" prop="pid">
        <el-tree-select
          class="w-full"
          v-model="formData.pid"
          :data="menuOptions"
          clearable
          node-key="id"
          :props="{ label: 'name' }"
          :default-expand-all="true"
          placeholder="请选择父级菜单"
          check-strictly
        />
      </el-form-item>
      <el-form-item label="菜单名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入菜单名称" clearable />
      </el-form-item>
      <el-form-item v-if="formData.type !== 2" label="菜单图标">
        <el-input v-model="formData.icon" placeholder="请输入图标名称" clearable />
      </el-form-item>
      <el-form-item v-if="formData.type !== 2" label="路由路径" prop="route">
        <div class="w-full">
          <el-input v-model="formData.route" placeholder="请输入路由路径" clearable />
          <div class="text-gray-400 text-xs mt-1">访问的路由地址，如：`user`</div>
        </div>
      </el-form-item>
      <el-form-item v-if="formData.type === 1" label="组件路径" prop="component">
        <div class="w-full">
          <el-input v-model="formData.component" placeholder="请输入组件路径" clearable />
          <div class="text-gray-400 text-xs mt-1">访问的组件路径，如：`user/setting`，默认在`views`目录下</div>
        </div>
      </el-form-item>
      <el-form-item v-if="formData.type !== 0" label="权限字符" prop="access">
        <div class="w-full">
          <el-input v-model="formData.access" placeholder="请输入权限字符" clearable />
          <div class="text-gray-400 text-xs mt-1">将作为server端API验权使用，如`auth.admin/user`</div>
        </div>
      </el-form-item>
      <el-form-item v-if="formData.type === 1" label="是否缓存">
        <el-radio-group v-model="formData.is_cache">
          <el-radio :label="1">缓存</el-radio>
          <el-radio :label="0">不缓存</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="formData.type !== 2" label="是否显示">
        <el-radio-group v-model="formData.show_status">
          <el-radio :label="1">显示</el-radio>
          <el-radio :label="0">隐藏</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="formData.type !== 2" label="菜单状态">
        <el-radio-group v-model="formData.status">
          <el-radio :label="1">正常</el-radio>
          <el-radio :label="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="菜单排序" prop="sort">
        <div>
          <el-input-number v-model="formData.sort" :min="0" :max="9999" />
          <div class="text-gray-400 text-xs mt-1">数值越大越排前</div>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const emit = defineEmits(['success', 'close'])

/** 弹窗可见 */
const visible = ref(false)
/** 模式：add/edit */
const mode = ref('add')
/** 提交中 */
const submitting = ref(false)
/** 表单ref */
const formRef = ref(null)
/** 菜单树选项 */
const menuOptions = ref([])

/** 表单数据 */
const formData = reactive({
  id: null,
  pid: 0,
  type: 0,
  icon: '',
  name: '',
  sort: 0,
  route: '',
  access: '',
  component: '',
  selected: '',
  route_params: '',
  is_cache: 0,
  status: 1,
  show_status: 1
})

/** 表单校验规则 */
const formRules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  pid: [{ required: true, message: '请选择父级菜单', trigger: ['blur', 'change'] }]
}

/**
 * 加载菜单树（用于父级菜单选择）
 */
const loadMenuOptions = async () => {
  try {
    const data = await request.get('/web/mall/admin/shop/menu/lists', { params: { page_type: 0 } })
    if (Array.isArray(data)) {
      const topMenu = { id: 0, name: '顶级', children: data.filter(item => item.type !== 2) }
      menuOptions.value = [topMenu]
    }
  } catch (e) {
    menuOptions.value = [{ id: 0, name: '顶级', children: [] }]
  }
}

/**
 * 打开弹窗
 * @param {string} type - 'add' 或 'edit'
 */
const open = (type = 'add') => {
  mode.value = type
  if (type === 'add') {
    formData.id = null
    formData.pid = 0
    formData.type = 0
    formData.icon = ''
    formData.name = ''
    formData.sort = 0
    formData.route = ''
    formData.access = ''
    formData.component = ''
    formData.status = 1
    formData.show_status = 1
    formData.is_cache = 0
  }
  visible.value = true
}

/**
 * 设置表单数据
 * @param {object} data - 菜单数据
 */
const setFormData = (data) => {
  for (const key in formData) {
    if (data[key] != null && data[key] !== undefined) {
      formData[key] = data[key]
    }
  }
}

/**
 * 获取详情并回填
 * @param {object} row - 列表行数据
 */
const getDetail = async (row) => {
  try {
    const res = await request.get('/web/mall/admin/shop/menu/detail', { params: { id: row.id } })
    if (res) {
      setFormData(res)
    }
  } catch (e) {
    setFormData(row)
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  await formRef.value?.validate()
  if (formData.type === 2) formData.show_status = 0
  submitting.value = true
  try {
    const api = mode.value === 'edit'
      ? '/web/mall/admin/shop/menu/edit'
      : '/web/mall/admin/shop/menu/add'
    const res = await request.post(api, formData)
    if (res && res.code === 1) {
      ElMessage.success(mode.value === 'edit' ? '编辑成功' : '新增成功')
      visible.value = false
      emit('success')
    } else {
      ElMessage.error(res?.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

/**
 * 关闭弹窗
 */
const handleClose = () => {
  visible.value = false
  emit('close')
}

onMounted(loadMenuOptions)

defineExpose({ open, setFormData, getDetail })
</script>
