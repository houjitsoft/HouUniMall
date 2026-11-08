<template>
  <div class="sensitive-word-container">
    <div class="page-header">
      <h2>敏感词</h2>
      <p>管理系统敏感词词库、分类、命中记录和配置</p>
    </div>

    <el-card class="sensitive-card">
      <el-tabs v-model="activeTab" type="border-card" class="sensitive-tabs">
        <!-- 敏感词词库 -->
        <el-tab-pane label="敏感词词库" name="words">
          <div class="tab-content">
            <!-- 搜索区域 -->
            <div class="search-bar">
              <div class="search-left">
                <el-input
                  v-model="wordSearch.sensitiveName"
                  placeholder="请输入敏感词"
                  clearable
                  style="width: 240px; margin-right: 12px"
                />
                <el-select
                  v-model="wordSearch.sensitiveClassificationId"
                  placeholder="请选择分类"
                  clearable
                  style="width: 200px; margin-right: 12px"
                >
                  <el-option
                    v-for="item in classificationList"
                    :key="item.classificationId"
                    :label="item.classificationName"
                    :value="item.classificationId"
                  />
                </el-select>
                <el-button type="primary" @click="loadWords">查询</el-button>
                <el-button @click="resetWordSearch">重置</el-button>
              </div>
              <div class="search-right">
                <el-button type="primary" icon="Plus" @click="showAddWordDialog">新增敏感词</el-button>
                <el-button type="primary" icon="Upload" @click="showBatchAddDialog">批量新增</el-button>
                <el-button type="danger" icon="Delete" @click="batchDeleteWords">批量删除</el-button>
                <el-button type="info" icon="Refresh" @click="refreshWords">刷新</el-button>
              </div>
            </div>

            <!-- 表格 -->
            <el-table
              :data="wordList"
              style="width: 100%; margin-top: 20px"
              v-loading="wordLoading"
              @selection-change="handleWordSelectionChange"
            >
              <el-table-column type="selection" width="55" />
              <el-table-column prop="sensitiveName" label="敏感词" min-width="150" />
              <el-table-column prop="classificationName" label="分类" width="120">
                <template #default="{ row }">
                  <el-tag>{{ row.classificationName || '-' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="sensitiveAddTime" label="添加时间" width="180" />
              <el-table-column prop="sensitiveState" label="启用状态" width="100">
                <template #default="{ row }">
                  <el-switch
                    v-model="row.sensitiveState"
                    :active-value="1"
                    :inactive-value="0"
                    @change="toggleWordState(row)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="editWord(row)">编辑</el-button>
                  <el-button link type="danger" @click="deleteWord(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <el-pagination
              v-model:current-page="wordSearch.page"
              v-model:page-size="wordSearch.limit"
              :page-sizes="[10, 20, 50, 100]"
              :total="wordTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadWords"
              @current-change="loadWords"
              style="margin-top: 20px; justify-content: flex-end"
            />
          </div>
        </el-tab-pane>

        <!-- 分类管理 -->
        <el-tab-pane label="分类管理" name="classification">
          <div class="tab-content">
            <!-- 搜索区域 -->
            <div class="search-bar">
              <div class="search-left">
                <el-input
                  v-model="classSearch.classificationName"
                  placeholder="请输入分类名称"
                  clearable
                  style="width: 240px; margin-right: 12px"
                />
                <el-button type="primary" @click="loadClassifications">查询</el-button>
                <el-button @click="resetClassSearch">重置</el-button>
              </div>
              <div class="search-right">
                <el-button type="primary" icon="Plus" @click="showAddClassDialog">新增分类</el-button>
                <el-button type="info" icon="Refresh" @click="refreshClassifications">刷新</el-button>
              </div>
            </div>

            <!-- 表格 -->
            <el-table
              :data="classList"
              style="width: 100%; margin-top: 20px"
              v-loading="classLoading"
            >
              <el-table-column prop="classificationName" label="分类名称" min-width="200" />
              <el-table-column prop="classificationAddTime" label="添加时间" width="180" />
              <el-table-column prop="classificationState" label="启用状态" width="100">
                <template #default="{ row }">
                  <el-switch
                    v-model="row.classificationState"
                    :active-value="1"
                    :inactive-value="0"
                    @change="toggleClassState(row)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="editClass(row)">编辑</el-button>
                  <el-button link type="danger" @click="deleteClass(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <el-pagination
              v-model:current-page="classSearch.page"
              v-model:page-size="classSearch.limit"
              :page-sizes="[10, 20, 50, 100]"
              :total="classTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadClassifications"
              @current-change="loadClassifications"
              style="margin-top: 20px; justify-content: flex-end"
            />
          </div>
        </el-tab-pane>

        <!-- 敏感词命中记录 -->
        <el-tab-pane label="敏感词命中记录" name="record">
          <div class="tab-content">
            <!-- 搜索区域 -->
            <div class="search-bar">
              <div class="search-left">
                <el-input
                  v-model="recordSearch.sensitiveName"
                  placeholder="请输入敏感词"
                  clearable
                  style="width: 240px; margin-right: 12px"
                />
                <el-button type="primary" @click="loadRecords">查询</el-button>
                <el-button @click="resetRecordSearch">重置</el-button>
              </div>
              <div class="search-right">
                <el-button type="info" icon="Refresh" @click="refreshRecords">刷新</el-button>
              </div>
            </div>

            <!-- 表格 -->
            <el-table
              :data="recordList"
              style="width: 100%; margin-top: 20px"
              v-loading="recordLoading"
            >
              <el-table-column prop="sensitiveName" label="敏感词" width="150" />
              <el-table-column prop="content" label="命中内容" min-width="250" />
              <el-table-column prop="source" label="来源" width="120" />
              <el-table-column prop="createTime" label="命中时间" width="180" />
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="viewRecordDetail(row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <el-pagination
              v-model:current-page="recordSearch.page"
              v-model:page-size="recordSearch.limit"
              :page-sizes="[10, 20, 50, 100]"
              :total="recordTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadRecords"
              @current-change="loadRecords"
              style="margin-top: 20px; justify-content: flex-end"
            />
          </div>
        </el-tab-pane>

        <!-- 敏感词配置 -->
        <el-tab-pane label="敏感词配置" name="config">
          <div class="tab-content">
            <el-form :model="configForm" label-width="150px" style="max-width: 800px">
              <el-form-item label="启用敏感词过滤">
                <el-switch v-model="configForm.enabled" />
                <span style="margin-left: 12px; color: #909399; font-size: 13px">
                  关闭后将不再进行敏感词检测
                </span>
              </el-form-item>
              
              <el-form-item label="过滤模式">
                <el-radio-group v-model="configForm.filterMode">
                  <el-radio :label="1">拦截模式（禁止发布）</el-radio>
                  <el-radio :label="2">替换模式（自动替换为***）</el-radio>
                  <el-radio :label="3">标记模式（仅记录不拦截）</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="敏感词替换字符">
                <el-input
                  v-model="configForm.replaceChar"
                  placeholder="请输入替换字符"
                  style="width: 200px"
                />
                <span style="margin-left: 12px; color: #909399; font-size: 13px">
                  替换模式下使用的字符，默认为 ***
                </span>
              </el-form-item>

              <el-form-item label="检测范围">
                <el-checkbox-group v-model="configForm.detectScopes">
                  <el-checkbox label="商品标题" value="goodsTitle" />
                  <el-checkbox label="商品描述" value="goodsDesc" />
                  <el-checkbox label="用户评论" value="comment" />
                  <el-checkbox label="用户昵称" value="nickname" />
                </el-checkbox-group>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveConfig">保存配置</el-button>
                <el-button @click="loadConfig">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 添加/编辑敏感词对话框 -->
    <el-dialog
      v-model="wordDialogVisible"
      :title="wordForm.id ? '编辑敏感词' : '新增敏感词'"
      width="500px"
    >
      <el-form :model="wordForm" label-width="100px">
        <el-form-item label="敏感词" required>
          <el-input v-model="wordForm.sensitiveName" placeholder="请输入敏感词" />
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="wordForm.sensitiveClassificationId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="item in classificationList"
              :key="item.classificationId"
              :label="item.classificationName"
              :value="item.classificationId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="wordForm.sensitiveState" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="wordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveWord">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量添加敏感词对话框 -->
    <el-dialog v-model="batchAddDialogVisible" title="批量新增敏感词" width="600px">
      <el-form :model="batchAddForm" label-width="100px">
        <el-form-item label="分类" required>
          <el-select v-model="batchAddForm.sensitiveClassificationId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="item in classificationList"
              :key="item.classificationId"
              :label="item.classificationName"
              :value="item.classificationId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="敏感词" required>
          <el-input
            v-model="batchAddForm.sensitiveNameStr"
            type="textarea"
            :rows="8"
            placeholder="请输入敏感词，多个敏感词用英文逗号分隔"
          />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="batchAddForm.sensitiveState" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchAddDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="batchAddWords">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      v-model="classDialogVisible"
      :title="classForm.classificationId ? '编辑分类' : '新增分类'"
      width="500px"
    >
      <el-form :model="classForm" label-width="100px">
        <el-form-item label="分类名称" required>
          <el-input v-model="classForm.classificationName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="classForm.classificationState" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="classDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveClass">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// Tab激活状态
const activeTab = ref('words')

// ==================== 敏感词词库 ====================
const wordList = ref([])
const wordTotal = ref(0)
const wordLoading = ref(false)
const wordSearch = reactive({
  page: 1,
  limit: 10,
  sensitiveName: '',
  sensitiveClassificationId: null
})
const selectedWords = ref([])

const wordDialogVisible = ref(false)
const wordForm = reactive({
  id: null,
  sensitiveName: '',
  sensitiveClassificationId: null,
  sensitiveState: 1
})

const batchAddDialogVisible = ref(false)
const batchAddForm = reactive({
  sensitiveClassificationId: null,
  sensitiveNameStr: '',
  sensitiveState: 1
})

// 加载敏感词列表
const loadWords = async () => {
  wordLoading.value = true
  try {
    const res = await request.post('/web/base/sensitive/word/list', wordSearch)
    if (res.code === 200) {
      wordList.value = res.data.list || []
      wordTotal.value = res.data.totalCount || 0
    }
  } catch (error) {
    console.error('加载敏感词失败:', error)
    ElMessage.error('加载敏感词失败')
  } finally {
    wordLoading.value = false
  }
}

// 重置搜索
const resetWordSearch = () => {
  wordSearch.sensitiveName = ''
  wordSearch.sensitiveClassificationId = null
  wordSearch.page = 1
  loadWords()
}

// 刷新
const refreshWords = () => {
  wordSearch.page = 1
  loadWords()
}

// 显示添加对话框
const showAddWordDialog = () => {
  Object.assign(wordForm, {
    id: null,
    sensitiveName: '',
    sensitiveClassificationId: null,
    sensitiveState: 1
  })
  wordDialogVisible.value = true
}

// 编辑敏感词
const editWord = (row) => {
  Object.assign(wordForm, {
    id: row.id,
    sensitiveName: row.sensitiveName,
    sensitiveClassificationId: row.sensitiveClassificationId,
    sensitiveState: row.sensitiveState
  })
  wordDialogVisible.value = true
}

// 保存敏感词
const saveWord = async () => {
  if (!wordForm.sensitiveName) {
    ElMessage.warning('请输入敏感词')
    return
  }
  if (!wordForm.sensitiveClassificationId) {
    ElMessage.warning('请选择分类')
    return
  }

  try {
    const res = await request.post('/web/base/sensitive/word/add', wordForm)
    if (res.code === 200) {
      ElMessage.success(wordForm.id ? '编辑成功' : '添加成功')
      wordDialogVisible.value = false
      loadWords()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('保存敏感词失败:', error)
    ElMessage.error('操作失败')
  }
}

// 删除敏感词
const deleteWord = (row) => {
  ElMessageBox.confirm('确定要删除该敏感词吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.post('/web/base/sensitive/word/delete', { id: row.id })
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadWords()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除敏感词失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 切换状态
const toggleWordState = async (row) => {
  try {
    const res = await request.post('/web/base/sensitive/word/add', {
      id: row.id,
      sensitiveState: row.sensitiveState
    })
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
    } else {
      ElMessage.error(res.message || '状态更新失败')
      row.sensitiveState = row.sensitiveState === 1 ? 0 : 1
    }
  } catch (error) {
    console.error('切换状态失败:', error)
    ElMessage.error('状态更新失败')
    row.sensitiveState = row.sensitiveState === 1 ? 0 : 1
  }
}

// 选择变化
const handleWordSelectionChange = (selection) => {
  selectedWords.value = selection
}

// 显示批量添加对话框
const showBatchAddDialog = () => {
  Object.assign(batchAddForm, {
    sensitiveClassificationId: null,
    sensitiveNameStr: '',
    sensitiveState: 1
  })
  batchAddDialogVisible.value = true
}

// 批量添加
const batchAddWords = async () => {
  if (!batchAddForm.sensitiveClassificationId) {
    ElMessage.warning('请选择分类')
    return
  }
  if (!batchAddForm.sensitiveNameStr) {
    ElMessage.warning('请输入敏感词')
    return
  }

  try {
    const res = await request.post('/web/base/sensitive/word/batch-add', batchAddForm)
    if (res.code === 200) {
      ElMessage.success('批量添加成功')
      batchAddDialogVisible.value = false
      loadWords()
    } else {
      ElMessage.error(res.message || '批量添加失败')
    }
  } catch (error) {
    console.error('批量添加失败:', error)
    ElMessage.error('批量添加失败')
  }
}

// 批量删除
const batchDeleteWords = () => {
  if (selectedWords.value.length === 0) {
    ElMessage.warning('请选择要删除的敏感词')
    return
  }

  ElMessageBox.confirm(`确定要删除选中的 ${selectedWords.value.length} 条敏感词吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const idList = selectedWords.value.map(item => item.id)
      const res = await request.post('/web/base/sensitive/word/delete', { idList })
      if (res.code === 200) {
        ElMessage.success('批量删除成功')
        loadWords()
      } else {
        ElMessage.error(res.message || '批量删除失败')
      }
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }).catch(() => {})
}

// ==================== 分类管理 ====================
const classList = ref([])
const classTotal = ref(0)
const classLoading = ref(false)
const classSearch = reactive({
  page: 1,
  limit: 10,
  classificationName: ''
})
const classificationList = ref([]) // 所有分类列表（用于下拉选择）

const classDialogVisible = ref(false)
const classForm = reactive({
  classificationId: null,
  classificationName: '',
  classificationState: 1
})

// 加载分类列表
const loadClassifications = async () => {
  classLoading.value = true
  try {
    const res = await request.post('/web/base/sensitive/classification/list', classSearch)
    if (res.code === 200) {
      classList.value = res.data.list || []
      classTotal.value = res.data.totalCount || 0
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
  } finally {
    classLoading.value = false
  }
}

// 加载所有分类（用于下拉选择）
const loadAllClassifications = async () => {
  try {
    const res = await request.post('/web/base/sensitive/classification/all', {})
    if (res.code === 200) {
      classificationList.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 重置搜索
const resetClassSearch = () => {
  classSearch.classificationName = ''
  classSearch.page = 1
  loadClassifications()
}

// 刷新
const refreshClassifications = () => {
  classSearch.page = 1
  loadClassifications()
}

// 显示添加对话框
const showAddClassDialog = () => {
  Object.assign(classForm, {
    classificationId: null,
    classificationName: '',
    classificationState: 1
  })
  classDialogVisible.value = true
}

// 编辑分类
const editClass = (row) => {
  Object.assign(classForm, {
    classificationId: row.classificationId,
    classificationName: row.classificationName,
    classificationState: row.classificationState
  })
  classDialogVisible.value = true
}

// 保存分类
const saveClass = async () => {
  if (!classForm.classificationName) {
    ElMessage.warning('请输入分类名称')
    return
  }

  try {
    const res = await request.post('/web/base/sensitive/classification/add', classForm)
    if (res.code === 200) {
      ElMessage.success(classForm.classificationId ? '编辑成功' : '添加成功')
      classDialogVisible.value = false
      loadClassifications()
      loadAllClassifications() // 刷新下拉列表
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('保存分类失败:', error)
    ElMessage.error('操作失败')
  }
}

// 删除分类
const deleteClass = (row) => {
  ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.post('/web/base/sensitive/classification/delete', { classificationId: row.classificationId })
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadClassifications()
        loadAllClassifications() // 刷新下拉列表
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除分类失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 切换状态
const toggleClassState = async (row) => {
  try {
    const res = await request.post('/web/base/sensitive/classification/add', {
      classificationId: row.classificationId,
      classificationState: row.classificationState
    })
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
    } else {
      ElMessage.error(res.message || '状态更新失败')
      row.classificationState = row.classificationState === 1 ? 0 : 1
    }
  } catch (error) {
    console.error('切换状态失败:', error)
    ElMessage.error('状态更新失败')
    row.classificationState = row.classificationState === 1 ? 0 : 1
  }
}

// ==================== 命中记录 ====================
const recordList = ref([])
const recordTotal = ref(0)
const recordLoading = ref(false)
const recordSearch = reactive({
  page: 1,
  limit: 10,
  sensitiveName: ''
})

// 加载命中记录
const loadRecords = async () => {
  recordLoading.value = true
  try {
    const res = await request.post('/web/base/sensitive/record/list', recordSearch)
    if (res.code === 200) {
      recordList.value = res.data.list || []
      recordTotal.value = res.data.totalCount || 0
    }
  } catch (error) {
    console.error('加载命中记录失败:', error)
    ElMessage.error('加载命中记录失败')
  } finally {
    recordLoading.value = false
  }
}

// 重置搜索
const resetRecordSearch = () => {
  recordSearch.sensitiveName = ''
  recordSearch.page = 1
  loadRecords()
}

// 刷新
const refreshRecords = () => {
  recordSearch.page = 1
  loadRecords()
}

// 查看详情
const viewRecordDetail = (row) => {
  ElMessageBox.alert(`
    <div style="line-height: 1.8;">
      <p><strong>敏感词：</strong>${row.sensitiveName}</p>
      <p><strong>命中内容：</strong>${row.content}</p>
      <p><strong>来源：</strong>${row.source}</p>
      <p><strong>命中时间：</strong>${row.createTime}</p>
    </div>
  `, '命中记录详情', {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '关闭'
  })
}

// ==================== 敏感词配置 ====================
const configForm = reactive({
  enabled: true,
  filterMode: 1,
  replaceChar: '***',
  detectScopes: ['goodsTitle', 'goodsDesc', 'comment', 'nickname']
})

// 加载配置
const loadConfig = async () => {
  try {
    const res = await request.post('/web/base/sensitive/config/query', {})
    if (res.code === 200 && res.data) {
      Object.assign(configForm, res.data)
    }
  } catch (error) {
    console.error('加载配置失败:', error)
  }
}

// 保存配置
const saveConfig = async () => {
  try {
    const res = await request.post('/web/base/sensitive/config/update', configForm)
    if (res.code === 200) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存失败')
  }
}

// 初始化
onMounted(() => {
  loadWords()
  loadAllClassifications()
  loadConfig()
})
</script>

<style scoped lang="scss">
.sensitive-word-container {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;

    h2 {
      font-size: 24px;
      font-weight: 500;
      margin: 0 0 8px 0;
      color: #303133;
    }

    p {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }

  .sensitive-card {
    :deep(.el-card__body) {
      padding: 0;
    }
  }

  .sensitive-tabs {
    :deep(.el-tabs__content) {
      padding: 20px;
    }
  }

  .tab-content {
    .search-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 12px;

      .search-left {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 12px;
      }

      .search-right {
        display: flex;
        align-items: center;
        gap: 12px;
      }
    }
  }
}
</style>
