<template>
  <div class="page-container">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" type="card">
        <!-- Tab 1: 店铺设置 -->
        <el-tab-pane label="店铺设置" name="basic">
          <el-form :model="shopForm" label-width="120px">
            <el-form-item label="店铺名称">
              <el-input v-model="shopForm.shopName" placeholder="请输入店铺名称" />
            </el-form-item>
            <el-form-item label="店铺Logo">
              <el-input v-model="shopForm.shopLogo" placeholder="请输入Logo URL" />
            </el-form-item>
            <el-form-item label="联系人">
              <el-input v-model="shopForm.contactPerson" placeholder="请输入联系人" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="shopForm.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="店铺简介">
              <el-input v-model="shopForm.description" type="textarea" :rows="4" placeholder="请输入店铺简介" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveBasic">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- Tab 2: 审核资质信息 -->
        <el-tab-pane label="审核资质信息" name="qualification">
          <el-form :model="qualificationForm" label-width="140px">
            <el-form-item label="营业执照号">
              <el-input v-model="qualificationForm.licenseNo" placeholder="请输入营业执照号" />
            </el-form-item>
            <el-form-item label="营业执照">
              <el-input v-model="qualificationForm.businessLicense" placeholder="请输入营业执照图片URL" />
            </el-form-item>
            <el-form-item label="法人代表">
              <el-input v-model="qualificationForm.legalPerson" placeholder="请输入法人代表" />
            </el-form-item>
            <el-form-item label="身份证号">
              <el-input v-model="qualificationForm.idCardNo" placeholder="请输入身份证号" />
            </el-form-item>
            <el-form-item label="身份证正面">
              <el-input v-model="qualificationForm.idCardFront" placeholder="请输入身份证正面图片URL" />
            </el-form-item>
            <el-form-item label="身份证反面">
              <el-input v-model="qualificationForm.idCardBack" placeholder="请输入身份证反面图片URL" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveQualification">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- Tab 3: 结算管理 -->
        <el-tab-pane label="结算管理" name="settlement">
          <el-form :model="settlementForm" label-width="120px">
            <el-form-item label="开户行">
              <el-input v-model="settlementForm.bankName" placeholder="请输入开户行" />
            </el-form-item>
            <el-form-item label="银行账户">
              <el-input v-model="settlementForm.bankAccount" placeholder="请输入银行账户" />
            </el-form-item>
            <el-form-item label="账户名">
              <el-input v-model="settlementForm.accountName" placeholder="请输入账户名" />
            </el-form-item>
            <el-form-item label="结算费率(%)">
              <el-input-number v-model="settlementForm.settlementRate" :min="0" :max="100" :precision="2" />
            </el-form-item>
            <el-form-item label="质保金金额">
              <el-input-number v-model="settlementForm.qualityDeposit" :min="0" :precision="2" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveSettlement">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- Tab 4: 审核经营类目 -->
        <el-tab-pane label="审核经营类目" name="categories">
          <el-form label-width="120px">
            <el-form-item label="经营类目">
              <el-select v-model="selectedCategories" multiple placeholder="请选择经营类目" style="width: 100%">
                <el-option label="电子产品" value="electronics" />
                <el-option label="服装鞋帽" value="clothing" />
                <el-option label="食品饮料" value="food" />
                <el-option label="家居用品" value="home" />
                <el-option label="图书音像" value="books" />
                <el-option label="母婴用品" value="baby" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveCategories">保存</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="[]" style="margin-top: 20px">
            <el-table-column prop="categoryName" label="类目名称" />
            <el-table-column prop="status" label="审核状态" width="120">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'warning'">{{ row.status === 1 ? '已审核' : '待审核' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="auditTime" label="审核时间" width="180" />
            <el-table-column label="操作" width="100">
              <template #default>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div style="margin-top: 16px; text-align: center; color: #909399">暂无数据</div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('basic')

// 店铺基本信息
const shopForm = ref({
  shopName: '',
  shopLogo: '',
  contactPerson: '',
  contactPhone: '',
  description: ''
})

// 资质信息
const qualificationForm = ref({
  licenseNo: '',
  businessLicense: '',
  legalPerson: '',
  idCardNo: '',
  idCardFront: '',
  idCardBack: ''
})

// 结算信息
const settlementForm = ref({
  bankName: '',
  bankAccount: '',
  accountName: '',
  settlementRate: 0,
  qualityDeposit: 0
})

// 经营类目
const selectedCategories = ref([])

const saveBasic = () => {
  ElMessage.success('店铺基本信息保存成功')
}

const saveQualification = () => {
  ElMessage.success('资质信息保存成功')
}

const saveSettlement = () => {
  ElMessage.success('结算信息保存成功')
}

const saveCategories = () => {
  ElMessage.success('经营类目保存成功')
}
</script>

<style scoped>
.page-container { 
  padding: 20px; 
}
</style>
