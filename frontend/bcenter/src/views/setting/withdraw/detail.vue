<template>
    <div class="edit-popup">
        <popup ref="popupRef" title="银行卡详情" :async="false" width="550px"
            @close="handleClose">
            <el-form ref="formRef" :model="formData" label-width="110px">
                <el-form-item label="银行名称" prop="name">
                    {{ formData.name }}
                </el-form-item>
                <el-form-item label="银行支行" prop="branch">
                    {{ formData.branch }}
                </el-form-item>
                <el-form-item label="银行账号" prop="card_number">
                    {{ formData.card_number }}
                </el-form-item>
                <el-form-item label="开户人姓名" prop="username">
                    {{ formData.username }}
                </el-form-item>
                <el-form-item label="设为默认" prop="is_default">
                    <el-tag v-if="formData.is_default">默认</el-tag>
                    <el-tag v-else type="danger">-</el-tag>
                </el-form-item>
            </el-form>
        </popup>
    </div>
</template>
<script lang="ts" setup>
import type { WithDrawModal } from "@/api/setting/withdraw.d";
import { shopBankDetail } from "@/api/setting/withdraw";
import Popup from "@/components/popup/index.vue";
const emit = defineEmits(["success", "close"]);
const popupRef = shallowRef<InstanceType<typeof Popup>>();
const mode = ref("add");

const formData = reactive<WithDrawModal>({
    id: "",
    name: "",
    branch: "",
    username: "",
    card_number: "",
    is_default: 0
});

const open = () => {
    popupRef.value?.open();
};

const setFormData = (data: WithDrawModal) => {
    for (const key in formData) {
        if (data[key] != null && data[key] != undefined) {
            formData[key] = data[key];
        }
    }
};

const getDetail = async (row: Record<string, any>) => {
    const data: WithDrawModal = await shopBankDetail({
        id: row.id,
    });
    setFormData(data);
};

const handleClose = () => {
    emit("close");
};

defineExpose({ open, getDetail });
</script>
