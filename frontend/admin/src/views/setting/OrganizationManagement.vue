<template>
  <div class="organization-management">
    <div class="page-header">
      <h2>组织架构管理</h2>
      <div class="header-actions">
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加组织
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon company">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.totalCompanies }}</div>
              <div class="stats-label">总公司</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon branch">
              <el-icon><Shop /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.totalBranches }}</div>
              <div class="stats-label">分公司</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon dept">
              <el-icon><User /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.totalDepartments }}</div>
              <div class="stats-label">部门总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon job">
              <el-icon><Briefcase /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.totalJobs }}</div>
              <div class="stats-label">岗位总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索条件 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="组织名称">
          <el-input v-model="searchForm.orgName" placeholder="请输入组织名称" clearable />
        </el-form-item>
        <el-form-item label="组织类型">
          <el-select v-model="searchForm.orgType" placeholder="请选择组织类型" clearable>
            <el-option label="公司" value="company" />
            <el-option label="分公司" value="branch" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 组织架构树 -->
    <el-card>
      <div class="tree-header">
        <h3>组织架构树</h3>
        <el-button size="small" @click="expandAll">
          <el-icon><Expand /></el-icon>
          展开全部
        </el-button>
        <el-button size="small" @click="collapseAll">
          <el-icon><Fold /></el-icon>
          收起全部
        </el-button>
      </div>
      
      <el-tree
        ref="treeRef"
        :data="orgTree"
        :props="treeProps"
        node-key="id"
        :expand-on-click-node="false"
        :default-expand-all="false"
        class="org-tree"
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <div class="node-content">
              <el-icon class="node-icon">
                <OfficeBuilding v-if="data.orgType === 'company'" />
                <Shop v-else-if="data.orgType === 'branch'" />
                <User v-else-if="data.type === 'department'" />
                <Briefcase v-else />
              </el-icon>
              <span class="node-label">{{ data.orgName || data.deptName || data.jobName }}</span>
              <el-tag v-if="data.orgType" :type="getOrgTypeTagType(data.orgType)" size="small">
                {{ getOrgTypeText(data.orgType) }}
              </el-tag>
              <el-tag v-else-if="data.deptType" :type="getDeptTypeTagType(data.deptType)" size="small">
                {{ getDeptTypeText(data.deptType) }}
              </el-tag>
              <el-tag v-else-if="data.jobLevel" :type="getJobLevelTagType(data.jobLevel)" size="small">
                {{ getJobLevelText(data.jobLevel) }}
              </el-tag>
            </div>
            <div class="node-actions">
              <el-button size="small" type="primary" @click="handleEdit(data)">
                编辑
              </el-button>
              <el-button size="small" type="success" @click="handleViewDetail(data)">
                详情
              </el-button>
              <el-button 
                size="small" 
                type="danger" 
                @click="handleDelete(data)"
                v-if="data.orgType"
              >
                删除
              </el-button>
            </div>
          </div>
        </template>
      </el-tree>
    </el-card>

    <!-- 组织详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="组织详情"
      width="800px"
    >
      <div v-if="currentOrg" class="org-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="组织编码">
            {{ currentOrg.orgCode }}
          </el-descriptions-item>
          <el-descriptions-item label="组织名称">
            {{ currentOrg.orgName }}
          </el-descriptions-item>
          <el-descriptions-item label="组织类型">
            <el-tag :type="currentOrg.orgType === 'company' ? 'primary' : 'success'">
              {{ currentOrg.orgType === 'company' ? '公司' : '分公司' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="法人代表">
            {{ currentOrg.legalPerson || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="营业执照号">
            {{ currentOrg.businessLicense || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="税号">
            {{ currentOrg.taxNumber || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="注册资本">
            {{ currentOrg.registeredCapital ? formatCurrency(currentOrg.registeredCapital) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="成立日期">
            {{ currentOrg.establishDate || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="经营范围" :span="2">
            {{ currentOrg.businessScope || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="注册地址" :span="2">
            {{ currentOrg.address || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ currentOrg.contactPhone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系邮箱">
            {{ currentOrg.contactEmail || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="官网地址">
            {{ currentOrg.website || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="Logo地址">
            {{ currentOrg.logoUrl || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentOrg.status === 1 ? 'success' : 'danger'">
              {{ currentOrg.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="排序">
            {{ currentOrg.sortOrder }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ currentOrg.remark || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ currentOrg.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ currentOrg.updateTime }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, Refresh, OfficeBuilding, Shop, User, Briefcase, 
  Expand, Fold 
} from '@element-plus/icons-vue'
import { organizationApi, departmentApi, positionApi } from '../../api/setting/organization'

// 数据
const loading = ref(false)
const showDetailDialog = ref(false)
const currentOrg = ref(null)
const orgTree = ref([])
const treeRef = ref(null)

const searchForm = reactive({
  orgName: '',
  orgType: '',
  status: ''
})

const stats = reactive({
  totalCompanies: 0,
  totalBranches: 0,
  totalDepartments: 0,
  totalJobs: 0
})

const treeProps = {
  children: 'children',
  label: 'orgName'
}

// 方法
const loadOrgTree = async () => {
  try {
    loading.value = true
    // 模拟数据，实际应该调用API
    orgTree.value = [
      {
        id: 1,
        orgCode: 'ROOT',
        orgName: '总公司',
        orgType: 'company',
        legalPerson: '张三',
        businessLicense: '91110000000000000X',
        taxNumber: '91110000000000000X',
        registeredCapital: 10000000.00,
        establishDate: '2020-01-01',
        businessScope: '软件开发、技术服务、技术咨询',
        address: '北京市朝阳区xxx路xxx号',
        contactPhone: '010-12345678',
        contactEmail: 'contact@company.com',
        website: 'https://www.company.com',
        logoUrl: '',
        status: 1,
        sortOrder: 1,
        remark: '总公司',
        createTime: '2024-01-01 10:00:00',
        updateTime: '2024-01-01 10:00:00',
        children: [
          {
            id: 2,
            orgCode: 'BJ001',
            orgName: '北京分公司',
            orgType: 'branch',
            legalPerson: '李四',
            businessLicense: '91110000000000001X',
            taxNumber: '91110000000000001X',
            registeredCapital: 5000000.00,
            establishDate: '2021-01-01',
            businessScope: '软件开发、技术服务',
            address: '北京市海淀区xxx路xxx号',
            contactPhone: '010-87654321',
            contactEmail: 'bj@company.com',
            website: 'https://bj.company.com',
            logoUrl: '',
            status: 1,
            sortOrder: 2,
            remark: '北京分公司',
            createTime: '2024-01-01 10:00:00',
            updateTime: '2024-01-01 10:00:00',
            children: [
              {
                id: 101,
                deptName: '技术部',
                deptType: 'department',
                managerName: '技术经理',
                level: 1,
                sortOrder: 1,
                status: 1,
                remark: '北京技术部',
                createTime: '2024-01-01 10:00:00',
                children: [
                  {
                    id: 201,
                    jobName: '前端开发工程师',
                    jobLevel: 'middle',
                    jobCategory: 'technical',
                    workLocation: '北京',
                    workType: 'fulltime',
                    headcount: 3,
                    currentCount: 2,
                    status: 1,
                    createTime: '2024-01-01 10:00:00'
                  },
                  {
                    id: 202,
                    jobName: '后端开发工程师',
                    jobLevel: 'middle',
                    jobCategory: 'technical',
                    workLocation: '北京',
                    workType: 'fulltime',
                    headcount: 3,
                    currentCount: 1,
                    status: 1,
                    createTime: '2024-01-01 10:00:00'
                  }
                ]
              }
            ]
          },
          {
            id: 3,
            orgCode: 'SH001',
            orgName: '上海分公司',
            orgType: 'branch',
            legalPerson: '王五',
            businessLicense: '91310000000000002X',
            taxNumber: '91310000000000002X',
            registeredCapital: 3000000.00,
            establishDate: '2021-06-01',
            businessScope: '软件开发、技术服务',
            address: '上海市浦东新区xxx路xxx号',
            contactPhone: '021-12345678',
            contactEmail: 'sh@company.com',
            website: 'https://sh.company.com',
            logoUrl: '',
            status: 1,
            sortOrder: 3,
            remark: '上海分公司',
            createTime: '2024-01-01 10:00:00',
            updateTime: '2024-01-01 10:00:00',
            children: [
              {
                id: 102,
                deptName: '技术部',
                deptType: 'department',
                managerName: '技术经理',
                level: 1,
                sortOrder: 1,
                status: 1,
                remark: '上海技术部',
                createTime: '2024-01-01 10:00:00'
              }
            ]
          }
        ]
      }
    ]
    
    // 计算统计数据
    calculateStats()
  } catch (error) {
    console.error('加载组织架构失败:', error)
    ElMessage.error('加载组织架构失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const calculateStats = () => {
  let companies = 0
  let branches = 0
  let departments = 0
  let jobs = 0
  
  const traverse = (nodes) => {
    nodes.forEach(node => {
      if (node.orgType === 'company') {
        companies++
      } else if (node.orgType === 'branch') {
        branches++
      }
      
      if (node.children) {
        traverse(node.children)
      }
      
      if (node.deptName) {
        departments++
      }
      
      if (node.jobName) {
        jobs++
      }
    })
  }
  
  traverse(orgTree.value)
  
  stats.totalCompanies = companies
  stats.totalBranches = branches
  stats.totalDepartments = departments
  stats.totalJobs = jobs
}

const handleSearch = () => {
  // 实现搜索逻辑
  ElMessage.info('搜索功能待实现')
}

const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

const handleRefresh = () => {
  loadOrgTree()
}

const handleAdd = () => {
  // 跳转到公司信息管理页面添加
  ElMessage.info('请在公司信息管理页面添加新公司')
  // 这里可以添加路由跳转逻辑
  // router.push('/setting/permission/org/structure')
}

const handleEdit = (data) => {
  if (data.orgType) {
    // 跳转到公司信息管理页面编辑
    ElMessage.info('请在公司信息管理页面编辑公司信息')
  } else if (data.deptName) {
    // 跳转到部门管理页面编辑
    ElMessage.info('请在部门管理页面编辑部门信息')
  } else if (data.jobName) {
    // 跳转到岗位管理页面编辑
    ElMessage.info('请在岗位管理页面编辑岗位信息')
  }
}

const handleViewDetail = (data) => {
  if (data.orgType) {
    currentOrg.value = data
    showDetailDialog.value = true
  } else {
    ElMessage.info('部门和岗位详情请到对应管理页面查看')
  }
}

const handleDelete = async (data) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除组织 "${data.orgName}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用删除API
    // await orgAPI.deleteOrganization(data.id)
    ElMessage.success('删除成功')
    loadOrgTree()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除组织失败:', error)
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

const expandAll = () => {
  const expandKeys = []
  const traverse = (nodes) => {
    nodes.forEach(node => {
      expandKeys.push(node.id)
      if (node.children) {
        traverse(node.children)
      }
    })
  }
  traverse(orgTree.value)
  treeRef.value?.setExpandedKeys(expandKeys)
}

const collapseAll = () => {
  treeRef.value?.setExpandedKeys([])
}

const getOrgTypeTagType = (type) => {
  const typeMap = {
    'company': 'primary',
    'branch': 'success'
  }
  return typeMap[type] || 'info'
}

const getOrgTypeText = (type) => {
  const typeMap = {
    'company': '公司',
    'branch': '分公司'
  }
  return typeMap[type] || '未知'
}

const getDeptTypeTagType = (type) => {
  const typeMap = {
    'department': 'primary',
    'team': 'success'
  }
  return typeMap[type] || 'info'
}

const getDeptTypeText = (type) => {
  const typeMap = {
    'department': '部门',
    'team': '小组'
  }
  return typeMap[type] || '未知'
}

const getJobLevelTagType = (level) => {
  const typeMap = {
    'junior': 'info',
    'middle': 'primary',
    'senior': 'warning',
    'expert': 'danger'
  }
  return typeMap[level] || 'info'
}

const getJobLevelText = (level) => {
  const typeMap = {
    'junior': '初级',
    'middle': '中级',
    'senior': '高级',
    'expert': '专家'
  }
  return typeMap[level] || '未知'
}

const formatCurrency = (amount) => {
  if (!amount) return '-'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount)
}

onMounted(async () => {
  await loadOrgTree()
})
</script>

<style scoped>
.organization-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stats-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.stats-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.stats-icon.company {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stats-icon.branch {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stats-icon.dept {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stats-icon.job {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stats-info {
  flex: 1;
}

.stats-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 5px;
}

.stats-label {
  font-size: 14px;
  color: #909399;
}

.search-card {
  margin-bottom: 20px;
}

.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.tree-header h3 {
  margin: 0;
  color: #303133;
}

.org-tree {
  padding: 20px;
}

.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 8px 0;
}

.node-content {
  display: flex;
  align-items: center;
  flex: 1;
}

.node-icon {
  margin-right: 8px;
  color: #409eff;
}

.node-label {
  margin-right: 8px;
  font-weight: 500;
}

.node-actions {
  display: flex;
  gap: 8px;
}

.node-actions .el-button {
  padding: 4px 8px;
  font-size: 12px;
}

.org-detail {
  max-height: 500px;
  overflow-y: auto;
}
</style>
