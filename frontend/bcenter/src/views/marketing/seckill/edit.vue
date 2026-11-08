<template>
    <div class="edit-popup">
        <popup
            ref="popupRef"
            :title="popupTitle"
            :async="true"
            width="880px"
            @confirm="handleSubmit"
            @close="handleClose"
        >
            <el-scrollbar height="50vh">
                <el-form ref="formRef" :model="formData" label-width="110px" :disabled="disabled">
                    <el-form-item label="商品信息">
                        <div>
                            <goods-select
                                v-model="selectData"
                                v-model:sku="formData.ms_sku_list"
                                v-model:goods_id="formData.goods_id"
                                :disabled="disabled"
                                :extend="{
                                    name: '秒杀',
                                    showTable: true
                                }"
                            >
                            </goods-select>
                        </div>
                    </el-form-item>
                    <el-form-item label="秒杀时间">
                        <div class="w-100">
                            <daterange-picker
                                v-model:startTime="formData.start_time"
                                v-model:endTime="formData.end_time"
                            />
                            <div class="form-tips">
                                设置活动开启结束时间，用户可以在设置时间内享受秒杀优惠
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item label="每单限制">
                        <div>
                            <div>
                                <el-radio-group v-model="orderLimit" class="ml-4">
                                    <el-radio :label="0">不限制</el-radio>
                                    <el-radio :label="1">限制</el-radio>
                                </el-radio-group>
                            </div>
                            <template v-if="orderLimit !== 0">
                                <el-input
                                    class="w-[380px]"
                                    v-model="formData.order_limit"
                                    placeholder="请输入限制单数"
                                    :clearable="true"
                                >
                                    <template #append> 件 </template>
                                </el-input>
                                <div class="form-tips">每件商品单笔订单最多购买的件数</div>
                            </template>
                        </div>
                    </el-form-item>
                    <el-form-item label="审核状态" v-if="formData.audit_status_text">
                        <div>
                            <div>{{ formData.audit_status_text }}</div>
                            <div class="mt-2" v-if="formData.audit_content">
                                原因：{{ formData.audit_content }}
                            </div>
                        </div>
                    </el-form-item>
                </el-form>
            </el-scrollbar>
        </popup>
    </div>
</template>
<script lang="ts" setup>
import type { SeckillFormType } from '@/api/marketing/seckill.d'
import { apiSeckillAdd, apiSeckillEdit, getSeckillDetail } from '@/api/marketing/seckill'

import Popup from '@/components/popup/index.vue'
import type { FormInstance } from 'element-plus'

const emit = defineEmits(['success'])
const formRef = shallowRef<FormInstance>()
const popupRef = shallowRef<InstanceType<typeof Popup>>()

const popupTitle = computed(() => {
    switch (mode.value) {
        case 'add':
            return '添加秒杀商品'
        case 'edit':
            return '编辑秒杀商品'
        case 'detail':
            return '查看秒杀商品'
    }
})

const disabled = computed(() => mode.value === 'detail')

const mode = ref('add')
const orderLimit = shallowRef(0)
const selectData = ref<any>()
const formData = reactive<SeckillFormType>({
    id: '',
    goods_id: '',
    start_time: '',
    end_time: '',
    audit_status_text: '',
    audit_content: '',
    order_limit: 0,
    ms_sku_list: []
})

watch(
    () => orderLimit.value,
    (value) => {
        if (value === 0) {
            formData.order_limit = 0
        }
    }
)

const handleSubmit = async () => {
    mode.value == 'edit' ? await apiSeckillEdit(formData) : await apiSeckillAdd(formData)
    popupRef.value?.close()
    emit('success')
}

const open = (type = 'add', id: number) => {
    if (type == 'edit' || type == 'detail') {
        getDetail({ id: id })
    }
    mode.value = type
    popupRef.value?.open()
}

const handleClose = () => {}

const setFormData = (data: SeckillFormType | any) => {
    // 将数据转换
    data.ms_sku_list = data?.ms_sku_list?.map((item: any) => {
        return {
            id: item.goods_sku_id,
            sku_value_arr: item.sku_info.sku_value_arr,
            price: item.sku_info.price,
            stock: item.sku_info.stock,
            current_price: item.price,
            current_stock: item.stock
        }
    })
    selectData.value = {
        ...data.goods,
        sku: data.ms_sku_list
    }
    // @ts-ignore
    for (const key in formData) {
        // @ts-ignore
        if (data[key] != null && data[key] != undefined) {
            // @ts-ignore
            formData[key] = data[key]
        }
    }
}

const getDetail = async (row: Record<string, any>) => {
    const data: SeckillFormType = await getSeckillDetail({
        id: row.id
    })
    setFormData(data)
    if (data.order_limit !== 0) orderLimit.value = 1
}

defineExpose({ open })
</script>
