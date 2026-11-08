<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header><span class="card-title">营销设置</span></template>

      <el-tabs v-model="activeTab" type="card">
        <!-- 基础设置 -->
        <el-tab-pane label="基础设置" name="basic">
          <el-form :model="basicForm" label-width="150px">
            <el-form-item label="积分抵扣比例">
              <el-input-number v-model="basicForm.pointsRatio" :min="0" :max="100" />
              <span style="margin-left: 10px;">积分抵扣 1 元（0表示不可抵扣）</span>
            </el-form-item>
            <el-form-item label="积分有效期">
              <el-input-number v-model="basicForm.pointsExpireDays" :min="0" />
              <span style="margin-left: 10px;">天（0表示永久有效）</span>
            </el-form-item>
            <el-form-item label="优惠券叠加使用">
              <el-switch v-model="basicForm.couponStack" />
            </el-form-item>
            <el-form-item label="会员折扣叠加">
              <el-switch v-model="basicForm.memberDiscountStack" />
            </el-form-item>
            <el-form-item label="最低消费限制">
              <el-input-number v-model="basicForm.minOrderAmount" :min="0" />
              <span style="margin-left: 10px;">元</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveBasic">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 优惠券设置 -->
        <el-tab-pane label="优惠券设置" name="coupon">
          <el-form :model="couponForm" label-width="150px">
            <el-form-item label="优惠券有效期">
              <el-input-number v-model="couponForm.defaultExpireDays" :min="1" />
              <span style="margin-left: 10px;">天</span>
            </el-form-item>
            <el-form-item label="领取数量限制">
              <el-input-number v-model="couponForm.receiveLimit" :min="1" />
              <span style="margin-left: 10px;">张/人</span>
            </el-form-item>
            <el-form-item label="自动发放优惠券">
              <el-switch v-model="couponForm.autoSend" />
            </el-form-item>
            <el-form-item v-if="couponForm.autoSend" label="发放时机">
              <el-checkbox-group v-model="couponForm.sendTiming">
                <el-checkbox label="register">注册成功</el-checkbox>
                <el-checkbox label="firstOrder">首次下单</el-checkbox>
                <el-checkbox label="birthday">生日当天</el-checkbox>
                <el-checkbox label="levelUp">等级升级</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveCoupon">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 会员设置 -->
        <el-tab-pane label="会员设置" name="member">
          <el-form :model="memberForm" label-width="150px">
            <el-form-item label="会员升级方式">
              <el-radio-group v-model="memberForm.upgradeType">
                <el-radio value="points">按积分升级</el-radio>
                <el-radio value="amount">按消费金额升级</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="会员保级期">
              <el-input-number v-model="memberForm.protectionDays" :min="0" />
              <span style="margin-left: 10px;">天（0表示永久保级）</span>
            </el-form-item>
            <el-form-item label="会员降级规则">
              <el-switch v-model="memberForm.autoDowngrade" />
            </el-form-item>
            <el-form-item v-if="memberForm.autoDowngrade" label="降级检查周期">
              <el-input-number v-model="memberForm.checkDays" :min="30" :step="30" />
              <span style="margin-left: 10px;">天</span>
            </el-form-item>
            <el-form-item label="生日特权">
              <el-switch v-model="memberForm.birthdayPrivilege" />
            </el-form-item>
            <el-form-item v-if="memberForm.birthdayPrivilege" label="生日优惠">
              <el-input-number v-model="memberForm.birthdayDiscount" :min="1" :max="10" :step="0.1" />
              <span style="margin-left: 10px;">折</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveMember">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 促销设置 -->
        <el-tab-pane label="促销设置" name="promotion">
          <el-form :model="promotionForm" label-width="150px">
            <el-form-item label="活动审核">
              <el-switch v-model="promotionForm.needAudit" />
            </el-form-item>
            <el-form-item label="最大折扣限制">
              <el-input-number v-model="promotionForm.maxDiscount" :min="1" :max="10" :step="0.1" />
              <span style="margin-left: 10px;">折</span>
            </el-form-item>
            <el-form-item label="最大满减金额">
              <el-input-number v-model="promotionForm.maxCutAmount" :min="0" />
              <span style="margin-left: 10px;">元</span>
            </el-form-item>
            <el-form-item label="活动提前结束">
              <el-switch v-model="promotionForm.allowEarlyEnd" />
            </el-form-item>
            <el-form-item label="库存不足处理">
              <el-radio-group v-model="promotionForm.stockOutHandle">
                <el-radio value="end">自动结束活动</el-radio>
                <el-radio value="continue">继续接受订单</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSavePromotion">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('basic')

const basicForm = reactive({
  pointsRatio: 100,
  pointsExpireDays: 365,
  couponStack: false,
  memberDiscountStack: true,
  minOrderAmount: 0
})

const couponForm = reactive({
  defaultExpireDays: 30,
  receiveLimit: 5,
  autoSend: true,
  sendTiming: ['register', 'firstOrder']
})

const memberForm = reactive({
  upgradeType: 'points',
  protectionDays: 90,
  autoDowngrade: true,
  checkDays: 90,
  birthdayPrivilege: true,
  birthdayDiscount: 9
})

const promotionForm = reactive({
  needAudit: false,
  maxDiscount: 5,
  maxCutAmount: 500,
  allowEarlyEnd: true,
  stockOutHandle: 'end'
})

const handleSaveBasic = () => {
  ElMessage.success('基础设置保存成功')
}

const handleSaveCoupon = () => {
  ElMessage.success('优惠券设置保存成功')
}

const handleSaveMember = () => {
  ElMessage.success('会员设置保存成功')
}

const handleSavePromotion = () => {
  ElMessage.success('促销设置保存成功')
}
</script>

<style scoped>
.page-container { padding: 20px; }
.card-title { font-weight: bold; font-size: 16px; }
</style>
