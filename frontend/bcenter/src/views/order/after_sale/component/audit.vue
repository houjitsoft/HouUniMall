<template>
    <div class="edit-popup">
        <popup
            ref="popupRef"
            title="拒绝售后"
            :async="true"
            width="550px"
            @confirm="handleSubmit"
            @close="handleClose"
        >
            <el-input
                v-model="formData.refuse_content"
                placeholder="请输入拒绝理由"
                type="textarea"
                class="mr-8"
                :autosize="{ minRows: 10, maxRows: 10 }"
                show-word-limit
                clearable
            />
        </popup>
    </div>
</template>
<script lang="ts" setup>
import { apiAfterSaleAudit } from "@/api/order/after_sale";
import Popup from "@/components/popup/index.vue";
import type { FormInstance, FormRules } from "element-plus";
import feedback from "@/utils/feedback";

const emit = defineEmits(["success", "close"]);
const formRef = shallowRef<FormInstance>();
const popupRef = shallowRef<InstanceType<typeof Popup>>();

type AuditType = {
    id: string | number;
    refuse_content: string;
    [index: number|string]: number|string
}

const formData = reactive<AuditType>({
    id: '',
    refuse_content: ''
});

const handleSubmit = async () => {
    if(!formData.refuse_content) return feedback.msgError('请输入拒绝理由')
    await apiAfterSaleAudit({
        id: formData.id as number,
        agree: 0,
        refuse_content: formData.refuse_content
  })
    popupRef.value?.close();
    emit("success");
};

const open = (data: AuditType) => {
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
