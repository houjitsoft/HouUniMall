<template>
    <div class="edit-popup">
        <popup
            ref="popupRef"
            title="同意退款"
            :async="true"
            width="550px"
            @confirm="handleSubmit"
            @close="handleClose"
        >
            <el-alert title="您同意退款后，退款将自动原路退回至买家付款账户" type="warning" :closable="false" />
            <el-form >
                <el-form-item label="售后类型">
                    {{ formData.type_text }}
                </el-form-item>
                <el-form-item label="申请退款金额">
                    ¥{{ formData.refund_money }}
                </el-form-item>
                <el-form-item label="退款金额">
                    <el-input
                        v-model="formData.refund_money"
                        class="w-[140px]"
                        disabled
                    >
                        <template #append>
                            元
                        </template>
                    </el-input>
                </el-form-item>
            </el-form>
        </popup>
    </div>
</template>
<script lang="ts" setup>
import { apiAfterSaleRefund } from "@/api/order/after_sale";
import Popup from "@/components/popup/index.vue";
import type { FormInstance, FormRules } from "element-plus";
import feedback from "@/utils/feedback";

const emit = defineEmits(["success", "close"]);
const formRef = shallowRef<FormInstance>();
const popupRef = shallowRef<InstanceType<typeof Popup>>();

type RefundType = {
    id: string | number;
    type_text: string
    refund_money: string;
    [index: number|string]: number|string
}

const formData = reactive<RefundType>({
    id: '',
    type_text: '',
    refund_money: ''
});

const handleSubmit = async () => {
    await apiAfterSaleRefund({
        id: formData.id as number
    })
    popupRef.value?.close();
    emit("success");
};

const open = (data: RefundType) => {
    Reflect.ownKeys(data).map((item: any) => {
        formData[item] = data[item];
    });
    popupRef.value?.open();
};

const handleClose = () => {
    emit("close");
};

defineExpose({
    open
});
</script>
