<template>
    <div class="edit-popup">
        <popup ref="popupRef" :title="popupTitle" :async="true" width="550px" @confirm="handleSubmit"
            @close="handleClose">
            <el-form ref="formRef" :model="formData" label-width="110px" :rules="formRules">
                <el-form-item label="银行名称" prop="name">
                    <div class="w-80">
                        <el-input v-model="formData.name" placeholder="请输入银行名称" clearable />
                    </div>
                </el-form-item>
                <el-form-item label="银行支行" prop="branch">
                    <div class="w-80">
                        <el-input v-model="formData.branch" placeholder="请输入银行支行" clearable />
                    </div>
                </el-form-item>
                <el-form-item label="银行账号" prop="card_number">
                    <div class="w-80">
                        <el-input v-model="formData.card_number" placeholder="请输入银行账号" clearable />
                    </div>
                </el-form-item>
                <el-form-item label="开户人姓名" prop="username">
                    <div class="w-80">
                        <el-input v-model="formData.username" placeholder="请输入开户人姓名" clearable />
                    </div>
                </el-form-item>
                <el-form-item label="设为默认" prop="is_default">
                    <el-switch v-model="formData.is_default" :active-value="1" :inactive-value="0" />
                </el-form-item>
            </el-form>
        </popup>
    </div>
</template>
<script lang="ts" setup>
import type { WithDrawModal } from "@/api/setting/withdraw.d";
import { shopBankEdit, shopBankAdd, shopBankDetail } from "@/api/setting/withdraw";
import Popup from "@/components/popup/index.vue";
import type { FormInstance, FormRules } from "element-plus";
const emit = defineEmits(["success", "close"]);
const formRef = shallowRef<FormInstance>();
const popupRef = shallowRef<InstanceType<typeof Popup>>();
const mode = ref("add");

const popupTitle = computed(() => {
    return mode.value == "edit" ? "编辑银行卡" : "添加银行卡";
});

const formData = reactive<WithDrawModal>({
    id: "",
    name: "",
    branch: "",
    username: "",
    card_number: "",
    is_default: 0
});

const formRules = reactive<FormRules>({
    name: [{ required: true, message: "请输入银行名称", trigger: ["blur"] }],
    branch: [{ required: true, message: "请输入支行", trigger: ["blur"] }],
    card_number: [{ required: true, message: "请输入银行账号", trigger: ["blur"] }],
    username: [{ required: true, message: "请输入开户人姓名", trigger: ["blur"] }],
});

const handleSubmit = async () => {
    await formRef.value?.validate();
    mode.value == "edit"
        ? await shopBankEdit(formData)
        : await shopBankAdd(formData);
    popupRef.value?.close();
    emit("success");
};

const open = (type = "add") => {
    mode.value = type;
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

defineExpose({
    open,
    setFormData,
    getDetail,
});
</script>
