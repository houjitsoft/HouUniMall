<template>
    <div class="settlement-setup">
        <el-card shadow="never" class="!border-none mb-4">
            <el-alert type="warning" :closable="false" show-icon>
                <template #default>
                    <div>1.每日凌晨0点结算今日之前未结算的已完成订单;</div>
                    <div>2.在设定的时间内，结算本周之前未结算的已完成订单;</div>
                    <div>3.在设定的时间内，结算本月之前未结算的已完成订单;</div>
                </template>
            </el-alert>
        </el-card>

        <el-form ref="formRef" :model="formData" label-width="140px" disabled>
            <el-card shadow="never" class="!border-none">
                <div class="font-medium mb-7">订单设置</div>

                <el-form-item label="结算周期">
                    <div>
                        <el-radio-group v-model="formData.settlement.settle_cycle" class="ml-4">
                            <el-radio :label="CycleEnum.DAY">日结</el-radio>
                            <el-radio :label="CycleEnum.WEEK">周结</el-radio>
                            <el-radio :label="CycleEnum.MONTH">月结</el-radio>
                        </el-radio-group>

                        <!-- 日结 -->
                        <template v-if="CycleEnum.DAY === formData.settlement.settle_cycle">
                            <div class="form-tips">
                                每日凌晨0点结算今日之前未结算的已过售后期订单
                            </div>
                        </template>

                        <!-- 周结 -->
                        <template v-if="CycleEnum.WEEK === formData.settlement.settle_cycle">
                            <div class="form-tips">
                                在设定的时间内，结算本周之前未结算的已支付订单
                            </div>

                            <el-select
                                class="w-[320px]"
                                v-model="formData.settlement.settle_cycle_week_day"
                            >
                                <el-option
                                    v-for="(itme, index) in formData.enum_week"
                                    :key="index"
                                    :label="itme"
                                    :value="index * 1"
                                ></el-option>
                            </el-select>
                        </template>

                        <!-- 余额结 -->
                        <template v-if="CycleEnum.MONTH === formData.settlement.settle_cycle">
                            <div class="form-tips">
                                在设定的时间内，结算本月之前未结算的已支付订单
                            </div>

                            <el-select
                                class="w-[320px]"
                                v-model="formData.settlement.settle_cycle_month_day"
                            >
                                <el-option
                                    v-for="(itme, index) in formData.enum_month"
                                    :key="index"
                                    :label="index + '号'"
                                    :value="index * 1"
                                    >{{ index }}号</el-option
                                >
                            </el-select>
                        </template>
                    </div>
                </el-form-item>
            </el-card>
        </el-form>
    </div>
</template>

<script lang="ts" setup name="SettlementSetup">
import type { SettlementForm } from '@/api/setting/settlement'
import { getSettlementConfig } from '@/api/setting/settlement'
import type { FormInstance, FormRules } from 'element-plus'
const formRef = ref<FormInstance>()

enum CycleEnum {
    DAY = 0,
    WEEK = 1,
    MONTH = 2
}

// 表单数据
const formData = reactive<SettlementForm>({
    settlement: {
        settle_cycle: CycleEnum.DAY,
        settle_cycle_week_day: 0,
        settle_cycle_month_day: 0
    },

    enum_week: {},
    enum_month: {}
})

// 获取登录注册数据
const getData = async () => {
    try {
        const data = await getSettlementConfig()
        for (const key in formData) {
            formData[key] = data[key]
        }
    } catch (error) {
        console.log('获取=>', error)
    }
}

getData()
</script>
