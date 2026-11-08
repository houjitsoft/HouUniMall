<template>
  <div class="menu-management">
    <PermissionTabs />
    <!-- 页面标题 -->
    <el-page-header @back="goBack" content="菜单管理">
      <template #extra>
        <el-space>
          <el-button type="primary" @click="addMenu">
            <el-icon><component :is="ElementPlusIconsVue.Plus" /></el-icon>
            新增菜单
          </el-button>
          <el-button @click="toggleExpand">
            <el-icon v-if="expanded"><component :is="ElementPlusIconsVue.ArrowDown" /></el-icon>
            <el-icon v-else><component :is="ElementPlusIconsVue.ArrowRight" /></el-icon>
            {{ expanded ? '折叠' : '展开' }}
          </el-button>
        </el-space>
      </template>
    </el-page-header>

    <el-tabs v-model="menuView" class="menu-view-tabs">
      <el-tab-pane label="核心菜单" name="core" />
      <el-tab-pane label="应用菜单" name="applications" />
      <el-tab-pane label="隐藏结构" name="hidden" />
    </el-tabs>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item v-if="menuView === 'applications'" label="应用分类">
          <el-select v-model="selectedCateId" clearable placeholder="全部分类" style="width: 180px">
            <el-option v-for="cate in cateOptions" :key="cate.id" :label="cate.name" :value="Number(cate.id)" />
            <el-option label="未分类" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜单名称">
          <el-input 
            v-model="searchForm.name" 
            placeholder="请输入菜单名称" 
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable style="width: 120px">
            <el-option label="目录" value="M" />
            <el-option label="菜单" value="C" />
            <el-option label="按钮" value="A" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><component :is="ElementPlusIconsVue.Search" /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><component :is="ElementPlusIconsVue.Refresh" /></el-icon>
            重置
          </el-button>
          
          
          
          
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 菜单表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :key="`${menuView}-${selectedCateId}`"
        :data="filteredMenuList"
        v-loading="loading"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="expanded"
        ref="menuTableRef"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="name" label="菜单名称" min-width="200">
          <template #default="{ row }">
            <div class="menu-name-cell">
              <span class="menu-name">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="icon" label="图标" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon" size="16" class="menu-icon-display">
              <component :is="ElementPlusIconsVue[row.icon]" />
            </el-icon>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="perms" label="权限标识" min-width="200">
          <template #default="{ row }">
            <el-text v-if="row.perms" type="info">
              {{ row.perms }}
            </el-text>
            <el-text v-else type="info">-</el-text>
          </template>
        </el-table-column>
        
        <el-table-column prop="paths" label="页面路径" min-width="150">
          <template #default="{ row }">
            <el-text v-if="row.paths" type="primary">
              {{ row.paths }}
            </el-text>
            <el-text v-else type="info">-</el-text>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.is_disable === 0 ? 'success' : 'danger'">
              {{ row.is_disable === 0 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="sort" label="排序" width="80" align="center">
          <template #default="{ row }">
            <el-text>{{ row.sort }}</el-text>
          </template>
        </el-table-column>
        
        <!-- 显示位置：决定一级菜单落在左侧竖栏、右上角插件分类，还是不展示 -->
        <el-table-column label="显示位置" width="150" align="center">
          <template #default="{ row }">
            <el-select
              v-if="isPositionConfigurable(row) && !isFixedCore(row)"
              :model-value="readDisplayPos(row)"
              size="small"
              style="width: 120px"
              @change="(value) => handleDisplayPosChange(row, value)"
            >
              <el-option
                v-for="opt in positionOptions(row)"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
            <el-tag v-else-if="isFixedCore(row)" type="info">左侧核心</el-tag>
            <el-text v-else type="info">随所属模块</el-text>
          </template>
        </el-table-column>

        <!-- 插件分类：仅在「右上角插件」位置生效，对应插件中心分类表 -->
        <el-table-column label="插件分类" width="160" align="center">
          <template #default="{ row }">
            <el-select
              v-if="isPositionConfigurable(row) && readDisplayPos(row) === 'plugin'"
              :model-value="readCateId(row)"
              size="small"
              clearable
              style="width: 132px"
              placeholder="未分类"
              @change="(value) => handleCateChange(row, value == null ? 0 : value)"
            >
              <el-option
                v-for="cate in cateOptions"
                :key="cate.id"
                :label="cate.name"
                :value="Number(cate.id)"
              />
            </el-select>
            <el-text v-else type="info">-</el-text>
          </template>
        </el-table-column>
        
        <el-table-column 
          label="操作" 
          width="200" 
          fixed="right" 
          align="center"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <el-space>
              <el-button 
                v-if="row.type === 'M' || row.type === 'C'" 
                type="primary" 
                link 
                @click="addSubMenu(row)"
              >
                新增
              </el-button>
              <el-button type="primary" link @click="editMenu(row)">
                编辑
              </el-button>
              <el-button v-if="!isFixedCore(row)" type="danger" link @click="deleteMenu(row)">
                删除
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增菜单弹窗 -->
    <el-dialog
      v-model="addMenuDialogVisible"
      :title="isEditMode ? '编辑菜单' : '新增菜单'"
      width="600px"
      :before-close="handleAddMenuCancel"
    >
      <el-form
        :model="addMenuForm"
        label-width="80px"
        label-position="right"
        :rules="addMenuRules"
        ref="addMenuFormRef"
      >
        <!-- 菜单类型 -->
        <el-form-item label="菜单类型" prop="type" required>
          <el-radio-group v-model="addMenuForm.type">
            <el-radio value="M">目录</el-radio>
            <el-radio value="C">菜单</el-radio>
            <el-radio value="A">按钮</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 父级菜单 -->
        <el-form-item label="父级菜单" prop="parentId" required>
          <el-select 
            v-model="addMenuForm.parentId" 
            placeholder="请选择父级菜单" 
            style="width: 100%"
            :key="`parent-select-${addMenuForm.id || 'new'}`"
            :disabled="parentLocked"
            clearable
          >
            <el-option v-if="parentLocked || menuView === 'hidden'" label="顶级" value="0"></el-option>
            <el-option 
              v-for="menu in flatMenuList" 
              :key="menu.id" 
              :label="menu.name" 
              :value="String(menu.id)"
            />
          </el-select>
        </el-form-item>

        <!-- 菜单名称 -->
        <el-form-item label="菜单名称" prop="name" required>
          <el-input v-model="addMenuForm.name" placeholder="请输入菜单名称" />
        </el-form-item>

        <!-- 菜单图标 - 目录和菜单类型显示 -->
        <el-form-item v-if="addMenuForm.type === 'M' || addMenuForm.type === 'C'" label="菜单图标">
          <div class="icon-input-wrapper">
            <el-input
              :value="iconDisplayName"
              placeholder="请选择图标"
              readonly
              @click="openIconSelector"
            >
              <template #prepend>
                <el-icon v-if="addMenuForm.icon && addMenuForm.icon !== ''" size="16">
                  <component :is="ElementPlusIconsVue[addMenuForm.icon]" />
                </el-icon>
                <span v-else>无</span>
              </template>
            </el-input>
            <el-button @click="openIconSelector" class="icon-search-btn">搜索图标</el-button>
            <el-button @click="clearIcon" class="icon-clear-btn">
              <el-icon><component :is="ElementPlusIconsVue.Close" /></el-icon>
            </el-button>
          </div>
        </el-form-item>

        <!-- 路由路径 - 目录和菜单类型显示 -->
        <el-form-item v-if="addMenuForm.type === 'M' || addMenuForm.type === 'C'" label="路由路径" prop="paths" required>
          <el-input v-model="addMenuForm.paths" placeholder="请输入路由路径" />
          <div class="form-tips">
            访问的路由地址，如：`user`，如外网地址需内链访问则以`http(s)://`开头
          </div>
        </el-form-item>

        <!-- 组件路径 - 菜单类型显示 -->
        <el-form-item v-if="addMenuForm.type === 'C'" label="组件路径" prop="component" required>
          <el-input v-model="addMenuForm.component" placeholder="请输入组件路径" />
          <div class="form-tips">
            访问的组件路径，如：`user/setting`，默认在`views`目录下
          </div>
        </el-form-item>

        <!-- 选中菜单 - 菜单类型显示 -->
        <el-form-item v-if="addMenuForm.type === 'C'" label="选中菜单">
          <el-input v-model="addMenuForm.selected" placeholder="请输入路由路径" />
          <div class="form-tips">
            访问详情页面，编辑页面时，菜单高亮显示，如`/consumer/lists`
          </div>
        </el-form-item>

        <!-- 权限字符 - 所有类型都显示 -->
        <el-form-item label="权限字符">
          <el-input v-model="addMenuForm.perms" placeholder="请输入权限字符" />
          <div class="form-tips">
            将作为server端API验权使用，如`auth.admin/user`，请谨慎修改
          </div>
        </el-form-item>

        <!-- 路由参数 - 菜单类型显示 -->
        <el-form-item v-if="addMenuForm.type === 'C'" label="路由参数">
          <el-input v-model="addMenuForm.params" placeholder="请输入路由参数" />
          <div class="form-tips">
            访问路由的默认传递参数，如：`{"id": 1, "name": "admin"}`或`id=1&name=admin`
          </div>
        </el-form-item>

        <!-- 是否缓存 - 菜单类型显示 -->
        <el-form-item v-if="addMenuForm.type === 'C'" label="是否缓存" prop="is_cache" required>
          <el-radio-group v-model="addMenuForm.is_cache">
            <el-radio :value="1">缓存</el-radio>
            <el-radio :value="0">不缓存</el-radio>
          </el-radio-group>
          <div class="form-tips">
            选择缓存则会被`keep-alive`缓存
          </div>
        </el-form-item>

        <!-- 是否显示 - 目录和菜单类型显示 -->
        <el-form-item v-if="addMenuForm.type === 'M' || addMenuForm.type === 'C'" label="是否显示" prop="is_show" required>
          <el-radio-group v-model="addMenuForm.is_show">
            <el-radio :value="1">显示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
          <div class="form-tips">
            选择隐藏则路由将不会出现在侧边栏，但仍然可以访问
          </div>
        </el-form-item>

        <!-- 菜单状态 - 所有类型都显示 -->
        <el-form-item label="菜单状态" prop="is_disable" required>
          <el-radio-group v-model="addMenuForm.is_disable">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">停用</el-radio>
          </el-radio-group>
          <div class="form-tips">
            选择停用则路由将不会出现在侧边栏，也不能被访问
          </div>
        </el-form-item>

        <!-- 菜单排序 - 所有类型都显示 -->
        <el-form-item label="菜单排序">
          <el-input-number 
            v-model="addMenuForm.sort" 
            :min="0" 
            :max="9999" 
            controls-position="right"
            style="width: 100%"
          />
          <div class="form-tips">数值越大越排前</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleAddMenuCancel">取消</el-button>
          <el-button type="primary" @click="handleAddMenuConfirm">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 图标选择器弹窗 -->
    <el-dialog
      v-model="showIconSelector"
      title="选择图标"
      width="600px"
      :before-close="closeIconSelector"
    >
      <div class="icon-selector">
        <!-- 图标类型切换 -->
        <el-tabs v-model="activeIconTab" class="icon-tabs">
          <el-tab-pane label="Element图标" name="element">
            <div class="icon-search">
              <el-input
                v-model="iconSearchKeyword"
                placeholder="搜索图标"
                clearable
                @input="filterIcons"
              >
                <template #prefix>
                  <el-icon><component :is="ElementPlusIconsVue.Search" /></el-icon>
                </template>
              </el-input>
            </div>
            <div class="icon-grid">
              <div
                v-for="icon in filteredElementIcons"
                :key="icon"
                class="icon-item"
                :class="{ active: selectedIcon === icon }"
                @click="selectIcon(icon)"
              >
                <el-icon size="20">
                  <component :is="ElementPlusIconsVue[icon]" />
                </el-icon>
                <span class="icon-name">{{ icon.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase()).trim() }}</span>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="本地图标" name="local">
            <div class="local-icons">
              <el-empty description="暂无本地图标" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeIconSelector">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, onMounted, computed, watch, getCurrentInstance, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// 使用封装好的request工具
import request from '../../utils/request.js'
import { refreshMenusAfterPermissionChange, clearMenuCacheAndRefresh } from '@/utils/userMenusCache'
import PermissionTabs from '@/components/setting/PermissionTabs.vue'
import { updateMenuDisplay } from '@/api/permission/menuDisplay'
import { getPluginCategories } from '@/api/plugin-center/pluginCenter'
import { selectManagedMenus, menuPluginCode } from '@/utils/pluginApplicationNav'
import { FIXED_CORE_MENU_IDS, getMenuDisplayPos } from '@/utils/menuDisplay'
import { useAppStore } from '@/stores/app'

/**
 * 菜单管理页面
 * 
 * @author Amos
 * @since 2025-01-09
 */

// 响应式数据
const loading = ref(false)
const clearCacheLoading = ref(false)
const expanded = ref(false)
const menuView = ref('core')
const selectedCateId = ref(null)
const editRoot = ref(null)
const appStore = useAppStore()
const menuTableRef = ref(null)

// ---------- 菜单显示位置（左侧竖栏 / 右上角插件分类 / 隐藏） ----------
// 一级菜单的落点由 hou_permission_menu.display_pos 决定；
// plugin 位置再通过 cate_id 归到插件中心分类（hou_addon_cate）。
const DISPLAY_POS_OPTIONS = [
  { value: 'left', label: '左侧竖栏' },
  { value: 'plugin', label: '右上角插件' },
  { value: 'hidden', label: '隐藏' }
]
const cateOptions = ref([])
const posSaving = ref(false)

/** 只有目录（M 型）才是导航一级项，才需要配显示位置 */
const isPositionConfigurable = row => Number(row?.pid || 0) === 0 && row?.type !== 'A'
const isFixedCore = row => FIXED_CORE_MENU_IDS.includes(Number(row?.id))
const positionOptions = row => DISPLAY_POS_OPTIONS.filter(option => option.value === 'hidden' || (menuPluginCode(row) ? option.value === 'plugin' : option.value === 'left'))

const readDisplayPos = getMenuDisplayPos

const readCateId = (row) => {
  const n = Number(row?.cate_id ?? row?.cateId)
  return Number.isFinite(n) && n > 0 ? Math.trunc(n) : 0
}

const loadCateOptions = async () => {
  try {
    cateOptions.value = await getPluginCategories()
  } catch (e) {
    console.warn('[菜单管理] 插件分类加载失败:', e?.message || e)
    cateOptions.value = []
  }
}

/**
 * 保存显示位置 / 插件分类。
 * 后端会清 Redis 里的用户菜单缓存，这里再刷一遍本地与内存缓存，改完立刻见效。
 */
const saveMenuDisplay = async (row, { displayPos, cateId }) => {
  if (!row?.id) return
  const prevPos = readDisplayPos(row)
  const prevCate = readCateId(row)
  const nextPos = displayPos ?? prevPos
  const nextCate = menuPluginCode(row) ? (cateId ?? prevCate) : 0
  row.display_pos = nextPos
  row.cate_id = nextCate
  posSaving.value = true
  try {
    const res = await updateMenuDisplay({ id: row.id, displayPos: nextPos, cateId: nextCate })
    if (res && res.code !== undefined && res.code !== 1 && res.code !== 200) {
      throw new Error(res.message || '保存失败')
    }
    ElMessage.success(`「${row.name}」显示位置已更新`)
    await clearMenuCacheAndRefresh({ clearServerCache: true })
    await appStore.loadPluginCategories()
  } catch (e) {
    row.display_pos = prevPos
    row.cate_id = prevCate
    ElMessage.error(`保存失败：${e?.message || '未知错误'}`)
  } finally {
    posSaving.value = false
  }
}

const handleDisplayPosChange = (row, value) => saveMenuDisplay(row, { displayPos: value })
const handleCateChange = (row, value) => saveMenuDisplay(row, { displayPos: 'plugin', cateId: value })

// 搜索表单image.png
const searchForm = ref({
  name: '',
  type: ''
})

// 菜单数据
const menuList = ref([])

const filteredMenuList = computed(() => selectManagedMenus(menuList.value, menuView.value, {
  cateId: selectedCateId.value, query: searchForm.value.name, type: searchForm.value.type
}))
const findRoot = id => menuList.value.find(root => {
  const contains = node => String(node.id) === String(id) || (node.children || []).some(contains)
  return contains(root)
})
const parentLocked = computed(() => isEditMode.value && String(addMenuForm.value.id) === String(editRoot.value?.id))
const flatMenuList = computed(() => {
  const source = editRoot.value ? [editRoot.value] : selectManagedMenus(menuList.value, menuView.value, { cateId: selectedCateId.value })
  const flatten = nodes => nodes.flatMap(node => String(node.id) === String(addMenuForm.value.id) ? [] : [node, ...flatten(node.children || [])])
  return flatten(source).filter(node => node.type !== 'A')
})
watch(menuView, () => { selectedCateId.value = null })

// 辅助函数
const getTypeTagType = (type) => {
  const typeMap = {
    'M': 'primary',    // 目录
    'C': 'success',    // 菜单
    'A': 'warning'     // 按钮
  }
  return typeMap[type] || 'info'
}

const getTypeText = (type) => {
  const textMap = {
    'M': '目录',
    'C': '菜单',
    'A': '按钮'
  }
  return textMap[type] || type
}

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


// 事件处理
const goBack = () => {
  // 返回上一页
  window.history.back()
}

// 新增菜单弹窗
const addMenuDialogVisible = ref(false)
const addMenuFormRef = ref(null) // 用于表单验证
const isEditMode = ref(false) // 是否为编辑模式
const addMenuForm = ref({
  type: 'M',
  parentId: '',
  name: '',
  icon: '',
  paths: '',
  component: '',
  selected: '',
  perms: '',
  params: '',
  is_cache: 0,
  is_show: 1,
  is_disable: 0,
  sort: 0
})

const addMenu = () => {
  editRoot.value = null
  isEditMode.value = false
  addMenuDialogVisible.value = true
  // 重置表单
  addMenuForm.value = {
    type: 'M',
    parentId: '',
    name: '',
    icon: '',
    paths: '',
    component: '',
    selected: '',
    perms: '',
    params: '',
    is_cache: 0,
    is_show: 1,
    is_disable: 0,
    sort: 0
  }
}

const handleAddMenuConfirm = async () => {
  try {
    const valid = await addMenuFormRef.value.validate()
    if (valid) {
      loading.value = true
      
      // 准备请求数据
      const requestData = {
        type: addMenuForm.value.type,
        parentId: addMenuForm.value.parentId || 0,
        name: addMenuForm.value.name,
        icon: addMenuForm.value.icon,
        paths: addMenuForm.value.paths,
        component: addMenuForm.value.component,
        selected: addMenuForm.value.selected,
        perms: addMenuForm.value.perms,
        params: addMenuForm.value.params,
        is_cache: addMenuForm.value.is_cache,
        is_show: addMenuForm.value.is_show,
        is_disable: addMenuForm.value.is_disable,
        sort: addMenuForm.value.sort
      }
      
      let response
      if (isEditMode.value) {
        // 编辑菜单
        requestData.id = addMenuForm.value.id
        response = await request.post('/web/permission/menu/edit', requestData)
      } else {
        // 新增菜单
        response = await request.post('/web/permission/menu/add', requestData)
      }
      
      if (response.code === 1) {
        ElMessage.success(isEditMode.value ? '编辑菜单成功' : '新增菜单成功')
        addMenuDialogVisible.value = false
        getMenuList() // 刷新列表
        
        // 清除菜单缓存，确保新菜单立即生效
        try {
          await request.post('/web/permission/menu/clear-cache', {})
          console.log('菜单缓存已清除')
        } catch (error) {
          console.warn('清除菜单缓存失败:', error)
          // 不清除缓存也不影响主流程，只记录警告
        }
        refreshMenusAfterPermissionChange().catch(() => {})
      } else {
        ElMessage.error(response.message || '操作失败')
      }
    }
  } catch (error) {
    console.error('保存菜单失败:', error)
    ElMessage.error('保存菜单失败')
  } finally {
    loading.value = false
  }
}

const handleAddMenuCancel = () => {
  addMenuDialogVisible.value = false
  isEditMode.value = false
}

// 编辑菜单
const editMenu = (row) => {
  editRoot.value = findRoot(row.id)
  isEditMode.value = true
  addMenuDialogVisible.value = true
  
  // 先重置表单
  addMenuForm.value = {
    id: '',
    type: 'M',
    parentId: '0',
    name: '',
    icon: '',
    paths: '',
    component: '',
    selected: '',
    perms: '',
    params: '',
    is_cache: 0,
    is_show: 1,
    is_disable: 0,
    sort: 0
  }
  
  // 使用 setTimeout 延迟设置值，确保 el-select 完全渲染
  setTimeout(() => {
    addMenuForm.value = {
      id: row.id, // 添加ID用于编辑
      type: row.type,
      parentId: row.pid !== undefined && row.pid !== null && row.pid !== 0 ? String(row.pid) : '0',
      name: row.name,
      icon: row.icon || '', // 确保图标字段不为undefined
      paths: row.paths || '',
      component: row.component || '',
      selected: row.selected || '',
      perms: row.perms || '',
      params: row.params || '',
      is_cache: row.is_cache !== undefined ? row.is_cache : 0,
      is_show: row.is_show !== undefined ? row.is_show : 1,
      is_disable: row.is_disable !== undefined ? row.is_disable : 0,
      sort: row.sort !== undefined ? row.sort : 0
    }
  }, 100)
}

// 删除菜单
const deleteMenu = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除菜单"${row.name}"吗？`,
      '温馨提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    loading.value = true
    
    // 调用后端API删除菜单
    const response = await request.post('/web/permission/menu/delete', {
      id: row.id
    })
    
    if (response.code === 1) {
      ElMessage.success('删除成功')
      getMenuList() // 刷新列表
      
      // 清除菜单缓存，确保菜单删除立即生效
      try {
        await request.post('/web/permission/menu/clear-cache', {})
        console.log('菜单缓存已清除')
      } catch (error) {
        console.warn('清除菜单缓存失败:', error)
      }
      refreshMenusAfterPermissionChange().catch(() => {})
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error === 'cancel') {
      ElMessage.info('已取消删除')
    } else {
      console.error('删除菜单失败:', error)
      ElMessage.error('删除菜单失败')
    }
  } finally {
    loading.value = false
  }
}

// 新增子菜单
const addSubMenu = (row) => {
  editRoot.value = findRoot(row.id)
  isEditMode.value = false
  addMenuDialogVisible.value = true
  // 重置表单并设置父级菜单
  addMenuForm.value = {
    type: 'C', // 默认为菜单类型
    parentId: String(row.id),
    name: '',
    icon: '',
    paths: '',
    component: '',
    selected: '',
    perms: '',
    params: '',
    is_cache: 0,
    is_show: 1,
    is_disable: 0,
    sort: 0
  }
}

// 表单验证规则 - 动态验证
const addMenuRules = computed(() => {
  const rules = {
    type: [
      { required: true, message: '请选择菜单类型', trigger: 'change' }
    ],
    parentId: [
      { required: true, message: '请选择父级菜单', trigger: 'change' },
      { validator: (rule, value, callback) => {
          if (value === '' || value === null || value === undefined) {
            callback(new Error('请选择父级菜单'))
          } else {
            callback()
          }
        }, trigger: 'change' }
    ],
    name: [
      { required: true, message: '请输入菜单名称', trigger: 'blur' },
      { min: 2, max: 20, message: '菜单名称长度在 2 到 20 个字符', trigger: 'blur' }
    ],
    is_disable: [
      { required: true, message: '请选择菜单状态', trigger: 'change' }
    ]
  }
  
  // 根据菜单类型添加不同的验证规则
  if (addMenuForm.value.type === 'M' || addMenuForm.value.type === 'C') {
    rules.paths = [
      { required: true, message: '请输入路由路径', trigger: 'blur' }
    ]
    rules.is_show = [
      { required: true, message: '请选择是否显示', trigger: 'change' }
    ]
  }
  
  if (addMenuForm.value.type === 'C') {
    rules.component = [
      { required: true, message: '请输入组件路径', trigger: 'blur' }
    ]
    rules.is_cache = [
      { required: true, message: '请选择是否缓存', trigger: 'change' }
    ]
  }
  
  return rules
})

// 图标选择器
const showIconSelector = ref(false)
const activeIconTab = ref('element')
const iconSearchKeyword = ref('')
const selectedIcon = ref('')

// Element UI 图标列表 - 从全局获取
const elementIcons = ref([])

// 过滤后的图标列表
const filteredElementIcons = computed(() => {
  if (!iconSearchKeyword.value) {
    return elementIcons.value
  }
  return elementIcons.value.filter(icon => 
    icon.toLowerCase().includes(iconSearchKeyword.value.toLowerCase())
  )
})

// 图标显示名称
const iconDisplayName = computed(() => {
  if (!addMenuForm.value.icon) return ''
  // 将驼峰命名转换为友好显示名称
  return addMenuForm.value.icon
    .replace(/([A-Z])/g, ' $1')
    .replace(/^./, str => str.toUpperCase())
    .trim()
})

// 打开图标选择器
const openIconSelector = () => {
  selectedIcon.value = addMenuForm.value.icon || ''
  showIconSelector.value = true
}

// 选择图标 - 直接选中并关闭对话框
const selectIcon = (icon) => {
  addMenuForm.value.icon = icon
  showIconSelector.value = false
  selectedIcon.value = ''
  iconSearchKeyword.value = ''
}

// 关闭图标选择器
const closeIconSelector = () => {
  showIconSelector.value = false
  selectedIcon.value = ''
  iconSearchKeyword.value = ''
}

// 清空图标
const clearIcon = () => {
  addMenuForm.value.icon = ''
  selectedIcon.value = ''
}

// 过滤图标
const filterIcons = () => {
  // 搜索逻辑已在计算属性中处理
}


const toggleExpand = () => {
  expanded.value = !expanded.value
  
  // 动态控制表格展开状态
  if (menuTableRef.value) {
    if (expanded.value) {
      // 展开所有行
      menuTableRef.value.toggleAllSelection()
      // 手动展开所有有子节点的行
      const toggleRowExpansion = (rows) => {
        rows.forEach(row => {
          if (row.children && row.children.length > 0) {
            menuTableRef.value.toggleRowExpansion(row, true)
            toggleRowExpansion(row.children)
          }
        })
      }
      toggleRowExpansion(menuList.value)
    } else {
      // 折叠所有行
      const toggleRowExpansion = (rows) => {
        rows.forEach(row => {
          if (row.children && row.children.length > 0) {
            menuTableRef.value.toggleRowExpansion(row, false)
            toggleRowExpansion(row.children)
          }
        })
      }
      toggleRowExpansion(menuList.value)
    }
  }
  
}

// 搜索功能
const handleSearch = async () => {
  expanded.value = true
  await nextTick()
  const expandMatches = rows => rows.forEach(row => {
    if (row.children?.length) {
      menuTableRef.value?.toggleRowExpansion(row, true)
      expandMatches(row.children)
    }
  })
  expandMatches(filteredMenuList.value)
}
const handleReset = () => {
  searchForm.value = { name: '', type: '' }
  selectedCateId.value = null
  expanded.value = false
}

// 清除菜单缓存（含服务端 Redis + 本地）
const clearMenuCache = async () => {
  try {
    clearCacheLoading.value = true
    await clearMenuCacheAndRefresh({ clearServerCache: true })
    ElMessage.success('菜单缓存已清除，侧栏已刷新')
  } catch (error) {
    console.error('清除菜单缓存失败:', error)
    ElMessage.error('清除菜单缓存失败')
  } finally {
    clearCacheLoading.value = false
  }
}

// 获取菜单列表
const getMenuList = async () => {
  try {
    loading.value = true
    
    // 调用后端接口获取菜单数据
    const response = await request.post('/web/permission/menu/list')
    
    if (response.code === 1) {
      menuList.value = response.data || []
      
      // 数据加载完成后，根据展开状态设置表格
      setTimeout(() => {
        if (menuTableRef.value) {
          if (expanded.value) {
            // 展开所有行
            const toggleRowExpansion = (rows) => {
              rows.forEach(row => {
                if (row.children && row.children.length > 0) {
                  menuTableRef.value.toggleRowExpansion(row, true)
                  toggleRowExpansion(row.children)
                }
              })
            }
            toggleRowExpansion(menuList.value)
          }
        }
      }, 100)
    } else {
      ElMessage.error(response.message || '获取菜单列表失败')
      menuList.value = []
    }
  } catch (error) {
    console.error('获取菜单列表失败:', error)
    ElMessage.error('获取菜单列表失败')
    menuList.value = []
  } finally {
    loading.value = false
  }
}





// 监听菜单类型变化，清空相关字段
watch(() => addMenuForm.value.type, (newType, oldType) => {
  if (newType !== oldType) {
    // 清空相关字段
    addMenuForm.value.icon = ''
    addMenuForm.value.paths = ''
    addMenuForm.value.component = ''
    addMenuForm.value.selected = ''
    addMenuForm.value.params = ''
    addMenuForm.value.is_cache = 0
    addMenuForm.value.is_show = 1
    
    // 根据类型设置默认值
    if (newType === 'A') {
      // 按钮类型，隐藏显示选项
      addMenuForm.value.is_show = 0
    }
  }
})

onMounted(() => {
getMenuList()
  loadCateOptions()
  
  // 初始化图标列表 - 直接使用ElementPlusIconsVue的键名
  elementIcons.value = Object.keys(ElementPlusIconsVue)
  console.log('已注册图标数量:', elementIcons.value.length)
})
</script>

<style scoped>
.menu-management {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.menu-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  line-height: 1.5;
}

.menu-icon {
  font-size: 16px;
  color: #409eff;
  vertical-align: middle;
  display: inline-flex;
  align-items: center;
}

.menu-icon-display {
  color: #666;
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 6px;
  overflow: hidden;
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

/* 固定列样式 */
:deep(.el-table__fixed-right) {
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
}

:deep(.el-table__fixed-right-patch) {
  background-color: #fafafa;
}

:deep(.el-table__fixed-right .el-table__fixed-body-wrapper) {
  background-color: #fff;
}

:deep(.el-table__fixed-right .el-table__fixed-body-wrapper tr:hover > td) {
  background-color: #f5f7fa;
}

/* 树形表格样式 */
:deep(.el-table__expand-icon) {
  color: #999;
  font-size: 14px;
  vertical-align: middle;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
}

:deep(.el-table__expand-icon--expanded) {
  color: #409eff;
}

/* 表格单元格内容对齐 */
:deep(.el-table .cell) {
  display: flex;
  align-items: center;
  line-height: 1.5;
}

/* 按钮样式 */

/* 标签样式 */
:deep(.el-tag) {
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

/* 文本样式 */
:deep(.el-text) {
  font-size: 12px;
}

/* 页面头部样式 */
:deep(.el-page-header) {
  margin-bottom: 20px;
  background-color: #fff;
  padding: 16px 20px;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 卡片样式 */
:deep(.el-card) {
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 表单样式 */
:deep(.el-form--inline .el-form-item) {
  margin-right: 16px;
  margin-bottom: 0;
}

/* 图标样式 */
:deep(.el-icon) {
  vertical-align: middle;
}

/* 表单提示样式 */
.form-tips {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  line-height: 1.4;
  display: block;
  width: 100%;
}

/* 弹窗样式 */
:deep(.el-dialog) {
  border-radius: 8px;
}

:deep(.el-dialog__header) {
  padding: 20px 20px 10px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  padding: 10px 20px 20px;
  border-top: 1px solid #f0f0f0;
}

/* 表单样式 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

:deep(.el-radio-group) {
  display: flex;
  gap: 16px;
}

/* 图标选择器样式 */

/* 图标选择器样式 */
.icon-selector {
  max-height: 500px;
  overflow-y: auto;
}

.icon-tabs {
  margin-bottom: 16px;
}

.icon-search {
  margin-bottom: 16px;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
  padding: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background-color: #fafafa;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background-color: #fff;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
}

.icon-item:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.2);
}

.icon-item.active {
  border-color: #409eff;
  background-color: #e6f7ff;
  color: #409eff;
}

.icon-name {
  font-size: 10px;
  margin-top: 4px;
  word-break: break-all;
  line-height: 1.2;
  color: #666;
}

.icon-item.active .icon-name {
  color: #409eff;
  font-weight: 500;
}

.local-icons {
  text-align: center;
  padding: 40px 0;
}

/* 图标输入框样式 */
.icon-input-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.icon-input-wrapper .el-input {
  flex: 1;
}

.icon-search-btn {
  white-space: nowrap;
}

.icon-clear-btn {
  padding: 8px;
  min-width: auto;
}

/* 图标选择器弹窗样式 */
:deep(.el-dialog__body) {
  padding: 16px 20px;
}

:deep(.el-tabs__header) {
  margin-bottom: 16px;
}

:deep(.el-tabs__nav-wrap) {
  padding: 0 0 8px 0;
}

:deep(.el-tabs__item) {
  padding: 0 16px;
  font-weight: 500;
}

:deep(.el-tabs__item.is-active) {
  color: #409eff;
}

:deep(.el-tabs__active-bar) {
  background-color: #409eff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .menu-management {
    padding: 10px;
  }
  
  :deep(.el-form--inline .el-form-item) {
    margin-right: 8px;
  }
  
  :deep(.el-dialog) {
    width: 90% !important;
    margin: 0 auto;
  }
}
</style>
