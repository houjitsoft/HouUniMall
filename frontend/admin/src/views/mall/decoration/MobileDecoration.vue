<template>
  <div class="mobile-decoration">
    <el-row :gutter="0" class="decoration-layout">
      <!-- 左侧菜单 -->
      <el-col :span="4" class="menu-sidebar">
        <div class="menu-header">装修管理</div>
        <el-menu
          :default-active="activeMenu"
          class="decoration-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="pcStyle">
            <el-icon><Monitor /></el-icon>
            <span>PC装修</span>
          </el-menu-item>
          <el-menu-item index="theme">
            <el-icon><Brush /></el-icon>
            <span>移动装修</span>
          </el-menu-item>

          <el-sub-menu index="pages">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>页面装修</span>
            </template>
            <el-menu-item index="pageList">首页装修</el-menu-item>
            <el-menu-item index="productDetail">商品详情</el-menu-item>
            <el-menu-item index="userCenter">个人中心</el-menu-item>
            <el-menu-item index="dataConfig">数据配置</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="tabbar">
            <el-icon><Grid /></el-icon>
            <span>底部导航</span>
          </el-menu-item>
        </el-menu>
      </el-col>

      <!-- 右侧内容区 -->
      <el-col :span="20" class="content-area">
        <component
          :is="currentComponent"
          :pageId="editPageId"
          @editPage="handleEditPage"
          @back="handleBack"
        />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Brush, Document, Grid, Monitor } from '@element-plus/icons-vue'

// 懒加载子页面组件，避免一次性加载所有组件
import ThemeStyle from './pages/ThemeStyle.vue'
import PageList from './pages/PageList.vue'
import PageEditor from './pages/PageEditor.vue'
import ProductDetail from './pages/ProductDetail.vue'
import UserCenter from './pages/UserCenter.vue'
import DataConfig from './pages/DataConfig.vue'
import TabBar from './pages/TabBar.vue'

const router = useRouter()

/**
 * 当前激活的菜单项
 * @type {import('vue').Ref<string>}
 */
const activeMenu = ref('theme')

/**
 * 当前编辑的页面ID
 * @type {import('vue').Ref<string|number>}
 */
const editPageId = ref('')

/**
 * 菜单项与组件的映射关系
 * @type {Record<string, import('vue').Component>}
 */
const menuComponents = {
  theme: ThemeStyle,
  pageList: PageList,
  pageEditor: PageEditor,
  productDetail: ProductDetail,
  userCenter: UserCenter,
  dataConfig: DataConfig,
  tabbar: TabBar
}

/**
 * 根据当前激活菜单计算要渲染的组件
 * @type {import('vue').ComputedRef<import('vue').Component>}
 */
const currentComponent = computed(() => {
  return menuComponents[activeMenu.value] || ThemeStyle
})

/**
 * 菜单选择事件处理
 * @param {string} index - 选中的菜单项索引
 */
const handleMenuSelect = (index) => {
  if (index === 'pcStyle') {
    router.push('/mall/decoration/pc_style')
    return
  }
  if (index === 'theme') {
    router.push('/mall/decoration/mobile_style')
    return
  }
  activeMenu.value = index
  // 切换菜单时清空编辑状态
  editPageId.value = ''
}

/**
 * 处理页面编辑事件
 * @param {number} id - 要编辑的页面ID
 */
const handleEditPage = (id) => {
  editPageId.value = id
  activeMenu.value = 'pageEditor'
}

/**
 * 处理返回事件（从编辑器返回列表）
 */
const handleBack = () => {
  editPageId.value = ''
  activeMenu.value = 'pageList'
}
</script>

<style scoped>
.mobile-decoration {
  height: calc(100vh - 84px);
  background-color: #f0f2f5;
}

.decoration-layout {
  height: 100%;
}

.menu-sidebar {
  height: 100%;
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
}

.menu-header {
  height: 50px;
  line-height: 50px;
  padding: 0 20px;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  border-bottom: 1px solid #e4e7ed;
}

.decoration-menu {
  border-right: none;
}

.content-area {
  height: 100%;
  padding: 16px;
  overflow-y: auto;
}
</style>
