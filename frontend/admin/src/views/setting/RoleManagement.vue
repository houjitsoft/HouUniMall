<template>
  <div class="role-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>角色</h2>
      </div>
      <div class="header-right">
        <el-icon class="header-icon"><FullScreen /></el-icon>
        <div class="user-info">
          <el-avatar size="small">admin</el-avatar>
          <span>admin</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <el-icon class="header-icon"><Setting /></el-icon>
      </div>
    </div>

    <PermissionTabs />

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="addRole">
        <el-icon><Plus /></el-icon>
        新增
      </el-button>
    </div>

    <!-- 角色表格 -->
    <div class="role-table">
      <div class="debug-info" style="margin-bottom: 10px; padding: 10px; background: #f0f0f0; font-size: 12px;">
        调试信息: 角色数量: {{ roleList.length }}, 总数量: {{ totalCount }}, 加载状态: {{ loading }}
      </div>
      <el-table
        :data="roleList"
        v-loading="loading"
        stripe
        empty-text="暂无数据"
      >
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="name" label="名称" min-width="150">
          <template #default="{ row }">
            <span class="role-name">{{ row.name }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="desc" label="备注" min-width="200">
          <template #default="{ row }">
            <span class="role-desc">{{ row.desc || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="sort" label="排序" width="100">
          <template #default="{ row }">
            <span class="sort-number">{{ row.sort }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="num" label="管理员人数" width="120">
          <template #default="{ row }">
            <span class="admin-count">{{ row.num || 0 }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="create_time" label="创建时间" width="180">
          <template #default="{ row }">
            <span class="create-time">{{ formatTime(row.create_time) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" link size="small" @click="editRole(row)">
                编辑
              </el-button>
              <el-button type="primary" link size="small" @click="setPermission(row)">
                权限
              </el-button>
              <el-button type="danger" link size="small" @click="deleteRole(row)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <div class="pagination-info">
        共{{ totalCount }}条
      </div>
      <div class="pagination-controls">
        <el-select v-model="pageSize" @change="handleSizeChange" size="small">
          <el-option label="15条/页" :value="15" />
          <el-option label="20条/页" :value="20" />
          <el-option label="50条/页" :value="50" />
          <el-option label="100条/页" :value="100" />
        </el-select>
        
        <div class="pagination-nav">
          <el-button 
            :disabled="currentPage <= 1" 
            @click="handlePrevPage" 
            size="small"
            icon="ArrowLeft"
          />
          <span class="page-number">{{ currentPage }}</span>
          <el-button 
            :disabled="currentPage >= totalPages" 
            @click="handleNextPage" 
            size="small"
            icon="ArrowRight"
          />
        </div>
        
        <div class="page-jump">
          <span>前往</span>
          <el-input 
            v-model="jumpPage" 
            @keyup.enter="handleJumpPage"
            size="small"
            style="width: 60px; margin: 0 4px;"
          />
          <span>页</span>
        </div>
      </div>
    </div>

    <!-- 新增角色对话框 -->
    <el-dialog 
      v-model="addDialogVisible" 
      title="新增角色" 
      width="600px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form 
        ref="addFormRef"
        :model="roleForm" 
        :rules="roleFormRules" 
        label-width="100px"
      >
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色描述" prop="desc">
          <el-input 
            v-model="roleForm.desc" 
            type="textarea" 
            :rows="3"
            placeholder="请输入角色描述" 
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number 
            v-model="roleForm.sort" 
            :min="0" 
            :max="99999"
            placeholder="请输入排序值" 
          />
        </el-form-item>
        <el-form-item label="菜单权限">
          <el-tree
            ref="addMenuTreeRef"
            :key="`add-menu-tree-${addTreeKey}`"
            class="role-menu-tree"
            :data="addTreeData"
            show-checkbox
            node-key="id"
            check-strictly
            :props="{ children: 'children', label: 'name' }"
            :expand-on-click-node="false"
            :check-on-click-node="true"
            @check="() => syncSelectedFromTree(addMenuTreeRef.value)"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑角色对话框 -->
    <el-dialog 
      v-model="editDialogVisible" 
      title="编辑角色" 
      width="800px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form 
        ref="editFormRef"
        :model="roleForm" 
        :rules="roleFormRules" 
        label-width="100px"
      >
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色描述" prop="desc">
          <el-input 
            v-model="roleForm.desc" 
            type="textarea" 
            :rows="3"
            placeholder="请输入角色描述" 
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number 
            v-model="roleForm.sort" 
            :min="0" 
            :max="99999"
            placeholder="请输入排序值" 
          />
        </el-form-item>
        <el-form-item label="菜单权限">
          <div class="menu-permission-container">
            <div class="permission-actions">
              <el-button type="primary" link size="small" @click="selectAllMenus('edit')">
                全选
              </el-button>
              <el-button type="primary" link size="small" @click="unselectAllMenus('edit')">
                全不选
              </el-button>
            </div>
            <el-tree
              ref="editMenuTreeRef"
              :key="`edit-menu-tree-${editTreeKey}`"
              class="role-menu-tree"
              :data="editTreeData"
              show-checkbox
              node-key="id"
              check-strictly
              :props="{ children: 'children', label: 'name' }"
              :expand-on-click-node="false"
              :check-on-click-node="true"
              @check="() => syncSelectedFromTree(editMenuTreeRef.value)"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限设置对话框 -->
    <el-dialog 
      v-model="permissionDialogVisible" 
      title="权限设置" 
      width="720px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="permission-content">
        <div class="role-info">
          <h4>角色信息</h4>
          <p><strong>角色名称：</strong>{{ currentRole.name }}</p>
          <p><strong>角色描述：</strong>{{ currentRole.desc || '-' }}</p>
        </div>
        
        <div class="permission-tree">
          <div class="permission-header">
            <h4>菜单权限</h4>
            <div class="permission-actions">
              <el-button type="primary" link size="small" @click="selectAllMenus('permission')">
                全选
              </el-button>
              <el-button type="primary" link size="small" @click="unselectAllMenus('permission')">
                全不选
              </el-button>
            </div>
          </div>
          <el-tree
            ref="permissionTreeRef"
            :key="`permission-tree-${permissionTreeKey}`"
            class="role-menu-tree"
            :data="permissionTreeData"
            show-checkbox
            node-key="id"
            check-strictly
            :props="{ children: 'children', label: 'name' }"
            :expand-on-click-node="false"
            :check-on-click-node="true"
            @check="() => syncSelectedFromTree(permissionTreeRef.value)"
          />
        </div>
      </div>
      <template #footer>
        <el-button :disabled="permissionSaving" @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permissionSaving" @click="savePermissions">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, onMounted, computed, nextTick } from 'vue'
import { 
  FullScreen, ArrowDown, Setting, Plus, ArrowLeft, ArrowRight
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { refreshMenusAfterPermissionChange } from '@/utils/userMenusCache'
import PermissionTabs from '@/components/setting/PermissionTabs.vue'

/**
 * 角色管理页面
 * 
 * @author Amos
 * @since 2025-01-09
 */

// 响应式数据
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(15)
const jumpPage = ref(1)

// 角色数据
const roleList = ref([])
const totalCount = ref(0)

// 计算属性
const totalPages = computed(() => Math.ceil(totalCount.value / pageSize.value))

// 辅助函数
const formatTime = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp * 1000)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 对话框状态
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const permissionSaving = ref(false)
const currentRole = ref({})

// 表单数据
const roleForm = ref({
  id: null,
  name: '',
  desc: '',
  sort: 0,
  menuIds: []
})

// 表单验证规则
const roleFormRules = ref({
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '角色名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' },
    { type: 'number', min: 0, max: 99999, message: '排序值必须在 0 到 99999 之间', trigger: 'blur' }
  ]
})

// 菜单数据（源数据）；每棵树必须用深拷贝，避免多棵 el-tree 共用同一对象被原地污染
const menuList = ref([])
const selectedMenuIds = ref([])
const addTreeData = ref([])
const editTreeData = ref([])
const permissionTreeData = ref([])
const addTreeKey = ref(0)
const editTreeKey = ref(0)
const permissionTreeKey = ref(0)

// 表单和树形组件引用
const addFormRef = ref(null)
const editFormRef = ref(null)
const addMenuTreeRef = ref(null)
const editMenuTreeRef = ref(null)
const permissionTreeRef = ref(null)

/** 深拷贝菜单树，切断与其它 Tree 实例的引用共享 */
const cloneMenuTree = (nodes) => {
  if (!Array.isArray(nodes)) return []
  return nodes.map((node) => {
    const cloned = {
      id: node.id,
      pid: node.pid,
      type: node.type,
      name: node.name,
      icon: node.icon,
      sort: node.sort,
      perms: node.perms
    }
    if (Array.isArray(node.children) && node.children.length > 0) {
      cloned.children = cloneMenuTree(node.children)
    }
    return cloned
  })
}

/** 勾选子节点时补全祖先目录 ID，保证侧栏能展示 */
const expandWithAncestors = (ids, tree) => {
  const selected = new Set((ids || []).filter((id) => id != null && id !== ''))
  const parentOf = new Map()
  const walk = (nodes, parentId = 0) => {
    ;(nodes || []).forEach((node) => {
      parentOf.set(node.id, node.pid != null ? node.pid : parentId)
      if (node.children?.length) walk(node.children, node.id)
    })
  }
  walk(tree)
  for (const id of [...selected]) {
    let pid = parentOf.get(id)
    while (pid != null && pid !== 0 && !selected.has(pid)) {
      selected.add(pid)
      pid = parentOf.get(pid)
    }
  }
  return [...selected]
}

// API调用
const getRoleList = async () => {
  try {
    loading.value = true
    console.log('开始获取角色列表，参数:', { page: currentPage.value, limit: pageSize.value })

    const response = await request.post('/web/permission/role/list', {
      page: currentPage.value,
      limit: pageSize.value
    })

    console.log('角色列表响应:', response)

    if (response.code === 1) {
      roleList.value = response.data.records || []
      totalCount.value = response.data.total || 0
      console.log('角色列表数据已设置:', { roleList: roleList.value, totalCount: totalCount.value })

      // 强制刷新Vue组件
      await nextTick()
      console.log('Vue组件已刷新，当前roleList:', roleList.value)
    } else {
      console.warn('角色列表响应码不正确:', response.code, response.message)
      ElMessage.error(response.message || '获取角色列表失败')
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

const getMenuList = async () => {
  try {
    console.log('开始获取菜单权限树...')
    const response = await request.post('/web/permission/menu/tree')
    console.log('菜单权限树响应:', response)

    if (response.code === 1) {
      menuList.value = response.data || []
      console.log('菜单权限树数据已设置:', menuList.value)

      // 强制刷新Vue组件
      await nextTick()
      console.log('菜单权限树Vue组件已刷新，当前menuList:', menuList.value)
    } else {
      console.warn('菜单权限树响应码不正确:', response.code)
    }
  } catch (error) {
    console.error('获取菜单权限树失败:', error)
    // 如果是网络错误，尝试重新获取token
    if (error.response?.status === 401) {
      console.log('Token可能过期，尝试重新获取...')
      // 在单一应用架构中，直接刷新页面重新获取token
      window.location.reload()
    }
  }
}

// 事件处理
const addRole = async () => {
  roleForm.value = {
    id: null,
    name: '',
    desc: '',
    sort: 0,
    menuIds: []
  }
  selectedMenuIds.value = []
  addTreeData.value = cloneMenuTree(menuList.value)
  addTreeKey.value += 1
  addDialogVisible.value = true
  const tree = await awaitTreeReady(() => addMenuTreeRef.value)
  applyCheckedMenus(tree, [])
}

const editRole = async (row) => {
  const menuIds = [...(row.menusId || [])]
  roleForm.value = {
    id: row.id,
    name: row.name,
    desc: row.desc || '',
    sort: row.sort,
    menuIds
  }
  selectedMenuIds.value = menuIds
  editTreeData.value = cloneMenuTree(menuList.value)
  editTreeKey.value += 1
  editDialogVisible.value = true
  const tree = await awaitTreeReady(() => editMenuTreeRef.value)
  applyCheckedMenus(tree, menuIds)
}

const setPermission = async (row) => {
  currentRole.value = row
  try {
    const response = await request.get(`/web/permission/role/detail/${row.id}`)
    if (response.code === 1) {
      const menuIds = response.data.menusId || []
      selectedMenuIds.value = menuIds
      permissionTreeData.value = cloneMenuTree(menuList.value)
      permissionTreeKey.value += 1
      permissionDialogVisible.value = true
      const tree = await awaitTreeReady(() => permissionTreeRef.value)
      applyCheckedMenus(tree, menuIds)
    } else {
      ElMessage.error(response.message || '获取角色详情失败')
    }
  } catch (error) {
    console.error('获取角色详情失败:', error)
    ElMessage.error('获取角色详情失败')
  }
}

const deleteRole = (row) => {
  ElMessageBox.confirm(
    `确定要删除角色 "${row.name}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await request.post('/web/permission/role/delete', {
        id: row.id
      })
      
      if (response.code === 1) {
        ElMessage.success('删除成功')
        getRoleList()
        refreshMenusAfterPermissionChange().catch(() => {})
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      console.error('删除角色失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

const saveRole = async () => {
  try {
    const treeRef = roleForm.value.id ? editMenuTreeRef.value : addMenuTreeRef.value
    const treeData = roleForm.value.id ? editTreeData.value : addTreeData.value
    const menuIds = expandWithAncestors(collectMenuIds(treeRef), treeData)
    selectedMenuIds.value = menuIds
    const formData = {
      ...roleForm.value,
      menuIds
    }
    
    const response = formData.id 
      ? await request.post('/web/permission/role/edit', formData)
      : await request.post('/web/permission/role/add', formData)
    
    if (response.code === 1) {
      ElMessage.success(formData.id ? '更新成功' : '创建成功')
      addDialogVisible.value = false
      editDialogVisible.value = false
      getRoleList()
      refreshMenusAfterPermissionChange().catch(() => {})
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存角色失败:', error)
    ElMessage.error('保存失败')
  }
}

const savePermissions = async () => {
  if (permissionSaving.value) return
  permissionSaving.value = true
  try {
    const menuIds = expandWithAncestors(
      collectMenuIds(permissionTreeRef.value),
      permissionTreeData.value
    )
    selectedMenuIds.value = menuIds
    const response = await request.post('/web/permission/role/edit', {
      id: currentRole.value.id,
      name: currentRole.value.name,
      desc: currentRole.value.desc,
      sort: currentRole.value.sort,
      menuIds
    })
    
    if (response.code === 1 || response.code === 200) {
      ElMessage.success('权限设置成功')
      permissionDialogVisible.value = false
      getRoleList()
      refreshMenusAfterPermissionChange().catch(() => {})
    } else {
      ElMessage.error(response.message || '权限设置失败')
    }
  } catch (error) {
    console.error('设置权限失败:', error)
    const msg = error?.message || ''
    if (/timeout|超时|Network Error/i.test(msg) || error?.code === 'ECONNABORTED') {
      ElMessage.error('权限保存超时，请确认已重启 permission + bff-web（含重新打包）后再试')
    } else {
      ElMessage.error(msg || '权限设置失败')
    }
  } finally {
    permissionSaving.value = false
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  getRoleList()
}

const handlePrevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    getRoleList()
  }
}

const handleNextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    getRoleList()
  }
}

const handleJumpPage = () => {
  const page = parseInt(jumpPage.value)
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    getRoleList()
  } else {
    ElMessage.warning('请输入有效的页码')
  }
}

/**
 * check-strictly 模式下只取显式勾选的节点；祖先目录在保存时再补全。
 * 禁止再用「联动勾选」回显：父级 ID 会触发 setChecked(true, true) 把整支二级菜单勾上。
 */
const collectMenuIds = (treeRef) => {
  const tree = treeRef?.value ?? treeRef
  if (!tree?.getCheckedKeys) return [...selectedMenuIds.value]
  return [...new Set(tree.getCheckedKeys(false) || [])]
}

const syncSelectedFromTree = (treeRef) => {
  selectedMenuIds.value = collectMenuIds(treeRef)
}

const applyCheckedMenus = (treeRef, menuIds) => {
  const tree = treeRef?.value ?? treeRef
  if (!tree?.setCheckedKeys) return
  // check-strictly：只勾给定 ID，绝不级联
  tree.setCheckedKeys(menuIds || [])
  syncSelectedFromTree(tree)
}

const awaitTreeReady = async (getTree, retries = 8) => {
  for (let i = 0; i < retries; i++) {
    await nextTick()
    const tree = getTree()
    if (tree?.setCheckedKeys) return tree
    await new Promise((r) => setTimeout(r, 16))
  }
  return getTree()
}

const resolveTreeRef = (type) => {
  if (type === 'edit') return editMenuTreeRef.value
  if (type === 'add') return addMenuTreeRef.value
  return permissionTreeRef.value
}

const resolveTreeData = (type) => {
  if (type === 'edit') return editTreeData.value
  if (type === 'add') return addTreeData.value
  return permissionTreeData.value
}

const selectAllMenus = (type) => {
  const treeRef = resolveTreeRef(type)
  const data = resolveTreeData(type)
  if (!treeRef || !data?.length) return
  treeRef.setCheckedKeys(getAllMenuIds(data))
  syncSelectedFromTree(treeRef)
}

const unselectAllMenus = (type) => {
  const treeRef = resolveTreeRef(type)
  if (!treeRef) return
  treeRef.setCheckedKeys([])
  selectedMenuIds.value = []
}

const getAllMenuIds = (menus) => {
  const ids = []
  menus.forEach((menu) => {
    ids.push(menu.id)
    if (menu.children?.length) {
      ids.push(...getAllMenuIds(menu.children))
    }
  })
  return ids
}

onMounted(() => {
getRoleList()
  getMenuList()
})
</script>

<style scoped>
.role-management {
  background-color: #f5f5f5;
  min-height: 100vh;
  padding: 0;
}

.page-header {
  background-color: #fff;
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  font-size: 18px;
  color: #666;
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #333;
  cursor: pointer;
}

.tab-container {
  background-color: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.tabs {
  display: flex;
  align-items: center;
  padding: 0 24px;
  position: relative;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  cursor: pointer;
  color: #666;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.tab-item:hover {
  color: #409eff;
}

.tab-item.active {
  color: #409eff;
  border-bottom-color: #409eff;
}

.tab-dot {
  width: 6px;
  height: 6px;
  background-color: #409eff;
  border-radius: 50%;
}

.close-icon {
  font-size: 12px;
  opacity: 0.6;
}

.close-icon:hover {
  opacity: 1;
  color: #f56c6c;
}

.more-icon {
  margin-left: 16px;
  color: #999;
  cursor: pointer;
}

.action-bar {
  background-color: #fff;
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  gap: 12px;
}

.role-table {
  background-color: #fff;
  margin: 0;
  padding: 0;
  min-height: 400px;
}

.role-name {
  font-weight: 500;
  color: #333;
}

.role-desc {
  color: #666;
  font-size: 14px;
}

.sort-number {
  font-weight: 500;
  color: #333;
}

.admin-count {
  color: #409eff;
  font-weight: 500;
}

.create-time {
  font-size: 12px;
  color: #999;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.pagination-container {
  background-color: #fff;
  padding: 16px 24px;
  border-top: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-info {
  color: #666;
  font-size: 14px;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.pagination-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-number {
  padding: 0 12px;
  color: #333;
  font-weight: 500;
}

.page-jump {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  font-size: 14px;
}

/* 表格样式优化 */
:deep(.el-table) {
  border: none;
}

:deep(.el-table th) {
  background-color: #fafafa;
  color: #333;
  font-weight: 500;
  border-bottom: 1px solid #e8e8e8;
}

:deep(.el-table td) {
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-table tr:hover > td) {
  background-color: #f5f7fa;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #fafafa;
}

/* 按钮样式 */
:deep(.el-button--primary.is-link) {
  color: #409eff;
  padding: 0;
  font-size: 12px;
}

:deep(.el-button--danger.is-link) {
  color: #f56c6c;
  padding: 0;
  font-size: 12px;
}

/* 选择器样式 */
:deep(.el-select) {
  width: 100px;
}

:deep(.el-input__inner) {
  text-align: center;
}

/* 分页按钮样式 */
:deep(.el-button--small) {
  padding: 5px 8px;
  font-size: 12px;
}

/* 菜单权限容器样式 */
.menu-permission-container {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  background-color: #fafafa;
  width: 100%;
}

.permission-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.permission-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.permission-header h4 {
  margin: 0;
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

:deep(.el-button:disabled) {
  color: #c0c4cc;
  background-color: #fff;
  border-color: #ebeef5;
}

/* 对话框样式 */
.permission-content {
  display: flex;
  gap: 30px;
}

.role-info {
  flex: 1;
}

.role-info h4 {
  margin-bottom: 15px;
  color: #303133;
}

.role-info p {
  margin: 8px 0;
  color: #606266;
}

.permission-tree {
  flex: 2;
}

.permission-tree h4 {
  margin-bottom: 15px;
  color: #303133;
}

/* 树形控件样式：加大行高/复选框点击区，减少「点不中」 */
:deep(.role-menu-tree) {
  max-height: 400px;
  overflow-y: auto;
  padding: 4px 0;
}

:deep(.role-menu-tree .el-tree-node__content) {
  height: 36px;
  line-height: 36px;
  cursor: pointer;
}

:deep(.role-menu-tree .el-tree-node__label) {
  font-size: 14px;
  flex: 1;
  user-select: none;
}

:deep(.role-menu-tree .el-checkbox) {
  margin-right: 8px;
  height: 36px;
  display: inline-flex;
  align-items: center;
}

:deep(.role-menu-tree .el-checkbox__inner) {
  width: 16px;
  height: 16px;
}
</style>
