<template>
  <div class="modify-enterprise-page">
    <el-card shadow="never">
      <template #header><span>会员企业信息修改</span></template>
      <el-form :inline="true" style="margin-bottom:16px">
        <el-form-item label="会员号"><el-input v-model="memberNo" placeholder="输入会员号" style="width:220px" /></el-form-item>
        <el-form-item><el-button type="primary" @click="loadInfo">加载</el-button></el-form-item>
      </el-form>
      <el-form v-if="loaded" :model="form" label-width="120px" style="max-width:600px">
        <el-form-item label="企业名称"><el-input v-model="form.enterpriseName" /></el-form-item>
        <el-form-item label="统一社会信用代码"><el-input v-model="form.creditCode" /></el-form-item>
        <el-form-item label="法人姓名"><el-input v-model="form.legalPerson" /></el-form-item>
        <el-form-item label="法人身份证号"><el-input v-model="form.legalIdCard" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item>
        <el-form-item label="经营地址"><el-input v-model="form.address" type="textarea" :rows="2" /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSave">保存修改</el-button></el-form-item>
      </el-form>
      <el-empty v-else description="请输入会员号加载企业信息" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

const memberNo = ref('')
const loaded = ref(false)
const form = reactive({ enterpriseName:'', creditCode:'', legalPerson:'', legalIdCard:'', contactPhone:'', address:'' })

const loadInfo = async () => {
  if (!memberNo.value) { ElMessage.warning('请输入会员号'); return }
  try {
    const res = await request.post('/web/pay/member/biz/info/query', { memberNo: memberNo.value })
    if (res.code === 200 && res.data) {
      const d = res.data
      form.enterpriseName = d.enterpriseName || ''; form.creditCode = d.creditCode || ''
      form.legalPerson = d.legalPerson || ''; form.legalIdCard = d.legalIdCard || ''
      form.contactPhone = d.contactPhone || ''; form.address = d.address || ''
      loaded.value = true
    } else { ElMessage.warning(res.message || '未找到会员') }
  } catch (e) { ElMessage.error('加载失败') }
}

const handleSave = async () => {
  const res = await request.post('/web/pay/member/biz/enterprise/modify', { memberNo: memberNo.value, ...form })
  if (res.code === 200) { ElMessage.success('修改成功') } else { ElMessage.error(res.message || '修改失败') }
}
</script>

<style scoped>
.modify-enterprise-page { padding: 16px; }
</style>
