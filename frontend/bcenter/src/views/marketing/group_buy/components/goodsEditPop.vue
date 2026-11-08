<template>
    <Popup
        ref="popRef"
        title="拼团商品活动"
        width="1000px"
        :footer-center="true"
        @confirm="clickConfirm"
        async
    >
        <div>
            <el-form label-width="90px">
                <el-form-item label="商品信息">
                    <div>
                        <GoodsSelect
                            v-model="selectData"
                            v-model:sku="formData.pt_sku_list"
                            v-model:goods_id="formData.goods_id"
                            :extend="{ name: '拼团', isGroupBuy: true, showTable: true }"
                            :disabled="type == 'detail'"
                        ></GoodsSelect>
                    </div>
                </el-form-item>
                <el-form-item label="商品信息">
                    <div>
                        <el-input
                            v-model="formData.user_num"
                            class="w-[420px]"
                            :disabled="type == 'detail'"
                        >
                            <template #append>
                                <div>人</div>
                            </template>
                        </el-input>
                        <div class="form-tips">设置商品拼团人数，最少两人成团</div>
                    </div>
                </el-form-item>
                <el-form-item label="拼团失效">
                    <div>
                        <el-input
                            v-model="formData.time_limit"
                            class="w-[420px]"
                            :disabled="type == 'detail'"
                        >
                            <template #append>
                                <div>分钟</div>
                            </template>
                        </el-input>
                        <div class="form-tips">开团后的成团有效期，超出时间还未成团则拼团失败</div>
                    </div>
                </el-form-item>
                <el-form-item label="拼团时间">
                    <div>
                        <DaterangePicker
                            v-model:start-time="formData.start_time"
                            v-model:end-time="formData.end_time"
                            :disabled="type == 'detail'"
                        ></DaterangePicker>
                        <div class="form-tips">
                            设置活动开启结束时间，用户可以在设置时间内发起参与拼团
                        </div>
                    </div>
                </el-form-item>
                <el-form-item v-if="type != 'add'" label="审核状态">
                    <div>
                        <div>{{ formData.audit_status_text }}</div>
                        <div>{{ formData.audit_content }}</div>
                    </div>
                </el-form-item>
            </el-form>
        </div>
    </Popup>
</template>

<script setup lang="ts">
import { addPTGoods, editPTGoods, PTgoodsDetial } from '@/api/marketing/group_buy'

const emit = defineEmits(['close'])
const selectData = ref()
const id = ref()
const type = ref('')
//弹框ref
const popRef = shallowRef()

const formData = ref({
    goods_id: '', //商品id
    time_limit: '', //拼团时效
    user_num: '', //拼团人数
    start_time: '', //开始时间
    end_time: '', //结束时间
    pt_sku_list: {
        goods_sku_id: '', //商品规格id
        price: '' //商品秒杀价格
    },
    audit_status_text: '',
    audit_content: ''
})

//获取拼团商品
const getDetail = async () => {
    const data = await PTgoodsDetial({ id: id.value })
    setData(data)
}

const setData = (data: any) => {
    // 将数据转换
    data.ms_sku_list = data?.pt_sku_list?.map((item: any) => {
        return {
            id: item.goods_sku_id,
            sku_value_arr: item.sku_info.sku_value_arr,
            price: item.sku_info.price,
            stock: item.sku_info.stock,
            current_price: item.price
        }
    })
    selectData.value = {
        ...data.goods,
        sku: data.ms_sku_list
    }
    // @ts-ignore
    for (const key in formData.value) {
        // @ts-ignore
        if (data[key] != null && data[key] != undefined) {
            // @ts-ignore
            formData.value[key] = data[key]
        }
    }
}

//打开弹框
const open = (option: { type: string; id: any }) => {
    popRef.value.open()
    type.value = option.type
    id.value = option.id

    if (id.value) {
        getDetail()
    }
}

//确定
const clickConfirm = async () => {
    if (id.value) {
        await editPTGoods({ ...formData.value, id: id.value })
    } else {
        await addPTGoods({ ...formData.value })
    }
    formData.value = {
        goods_id: '', //商品id
        time_limit: '', //拼团时效
        user_num: '', //拼团人数
        start_time: '', //开始时间
        end_time: '', //结束时间
        pt_sku_list: {
            goods_sku_id: '', //商品规格id
            price: '' //商品秒杀价格
        },
        audit_status_text: '',
        audit_content: ''
    }
    emit('close')
    popRef.value.close()
}

defineExpose({ open })
</script>

<style lang="scss" scoped></style>
