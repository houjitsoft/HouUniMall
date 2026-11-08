<template>
  <div class="express-company">
    <div class="page-header">
      <el-alert
        :title="t('setting.express.company.tipTitle')"
        type="info"
        :closable="false"
        show-icon
      >
        <template #default>
          <div style="color: #e6a23c;">{{ t('setting.express.company.tipContent') }}</div>
        </template>
      </el-alert>
    </div>

    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="showAddDialog">{{ t('setting.express.company.add') }}</el-button>
    </div>

    <el-table :data="companyList" border style="width: 100%">
      <el-table-column :label="t('setting.express.company.action')" width="120" fixed="left">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="editCompany(row)">{{ t('setting.express.company.edit') }}</el-button>
          <el-button link type="danger" size="small" @click="deleteCompany(row)">{{ t('setting.express.company.delete') }}</el-button>
        </template>
      </el-table-column>
      
      <el-table-column prop="expressName" :label="t('setting.express.company.name')" min-width="150" />
      <el-table-column prop="expressPinyin" :label="t('setting.express.company.code')" min-width="120" />
      <el-table-column :label="t('setting.express.company.status')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.expressStatus === true ? 'success' : 'info'">
            {{ row.expressStatus === true ? t('setting.express.company.enabled') : t('setting.express.company.disabled') }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="companyForm" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item :label="t('setting.express.company.name')" prop="expressName">
          <el-input v-model="companyForm.expressName" :placeholder="t('setting.express.company.namePlaceholder')" />
        </el-form-item>
        
        <el-form-item :label="t('setting.express.company.code')" prop="expressPinyin">
          <el-input v-model="companyForm.expressPinyin" :placeholder="t('setting.express.company.codePlaceholder')" />
        </el-form-item>

        <el-form-item :label="t('setting.express.company.status')" prop="expressStatus">
          <el-radio-group v-model="companyForm.expressStatus">
            <el-radio :label="true">{{ t('setting.express.company.enabled') }}</el-radio>
            <el-radio :label="false">{{ t('setting.express.company.disabled') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('setting.express.common.cancel') }}</el-button>
        <el-button type="primary" @click="saveCompany" :loading="saveLoading">{{ t('setting.express.common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 快递公司列表
const companyList = ref([
  { id: 1, name: '顺丰快递', code: 'SF', status: 1 },
  { id: 2, name: '德邦', code: 'DBL', status: 1 },
  { id: 3, name: '中通快递', code: 'STO', status: 1 },
  { id: 4, name: '百世汇通', code: 'Bsht', status: 1 },
  { id: 5, name: '圆通速递', code: 'YTO', status: 1 },
  { id: 6, name: '中通快递', code: 'ZTO', status: 1 },
  { id: 7, name: '京东快递', code: 'JD', status: 1 },
  { id: 8, name: '自配送', code: 'zs', status: 1 },
  { id: 9, name: '速腾快递', code: 'st', status: 0 },
  { id: 10, name: '宅急送', code: 'KY', status: 1 },
  { id: 11, name: '韵达', code: 'CY', status: 1 },
  { id: 12, name: '极兔快递', code: 'JTSD', status: 1 },
  { id: 13, name: 'UPS国际快递', code: 'UPS', status: 1 },
  { id: 14, name: 'FedEx国际快递', code: 'FedEX', status: 1 },
  { id: 15, name: 'DHL国际快递', code: 'DHL', status: 1 }
])

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = computed(() => companyForm.expressId ? t('setting.express.company.dialogEdit') : t('setting.express.company.dialogAdd'))
const formRef = ref()
const saveLoading = ref(false)

const companyForm = reactive({
  expressId: null,
  expressName: '',
  expressPinyin: '',
  expressStatus: true
})

const formRules = {
  expressName: [{ required: true, message: t('setting.express.company.namePlaceholder'), trigger: 'blur' }],
  expressPinyin: [{ required: true, message: t('setting.express.company.codePlaceholder'), trigger: 'blur' }],
  expressStatus: [{ required: true, message: t('setting.express.company.statusPlaceholder'), trigger: 'change' }]
}

// 加载快递公司列表
const loadCompanyList = async () => {
  try {
    const res = await request.post('/web/base/express/company/list', {})
    if (res.code === 200) {
      companyList.value = res.data || []
    }
  } catch (error) {
    console.error('加载快递公司列表失败:', error)
  }
}

// 显示新增对话框
const showAddDialog = () => {
  resetForm()
  dialogVisible.value = true
}

// 编辑快递公司
const editCompany = (company) => {
  Object.assign(companyForm, {
    expressId: company.expressId,
    expressName: company.expressName,
    expressPinyin: company.expressPinyin,
    expressStatus: company.expressStatus
  })
  dialogVisible.value = true
}

// 保存快递公司
const saveCompany = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        saveLoading.value = true
        const res = await request.post('/web/base/express/company/save', companyForm)
        if (res.code === 200) {
          ElMessage.success(companyForm.id ? t('setting.express.company.saveSuccessEdit') : t('setting.express.company.saveSuccessAdd'))
          dialogVisible.value = false
          loadCompanyList()
        } else {
          ElMessage.error(res.message || t('setting.express.company.operateFailed'))
        }
      } catch (error) {
        console.error('保存快递公司失败:', error)
        ElMessage.error(t('setting.express.company.operateFailed'))
      } finally {
        saveLoading.value = false
      }
    }
  })
}

// 删除快递公司
const deleteCompany = (company) => {
  ElMessageBox.confirm(t('setting.express.company.deleteConfirm'), t('setting.express.common.tip'), {
    confirmButtonText: t('setting.express.common.confirm'),
    cancelButtonText: t('setting.express.common.cancel'),
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.post('/web/base/express/company/delete', { expressId: company.expressId })
      if (res.code === 200) {
        ElMessage.success(t('setting.express.company.deleteSuccess'))
        loadCompanyList()
      } else {
        ElMessage.error(res.message || t('setting.express.company.deleteFailed'))
      }
    } catch (error) {
      console.error('删除快递公司失败:', error)
      ElMessage.error(t('setting.express.company.deleteFailed'))
    }
  }).catch(() => {})
}

// 重置表单
const resetForm = () => {
  Object.assign(companyForm, {
    expressId: null,
    expressName: '',
    expressPinyin: '',
    expressStatus: true
  })
  formRef.value?.clearValidate()
}

// 初始化
onMounted(() => {
  loadCompanyList()
})
</script>

<style scoped lang="scss">
.express-company {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;
  }

  .toolbar {
    margin-bottom: 20px;
  }
}
</style>
