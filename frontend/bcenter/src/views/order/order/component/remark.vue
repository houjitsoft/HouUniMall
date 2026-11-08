<template>
    <div class="edit-popup">
        <popup
            ref="popupRef"
            title="商家备注"
            :async="true"
            width="550px"
            @confirm="handleSubmit"
            @close="handleClose"
        >
            <el-form
                ref="formRef"
                :model="formData"
                label-width="84px"
                :rules="formRules"
            >
                <el-form-item label="备注" prop="content">
                    <!-- <div> -->
                    <el-input
                        v-model="formData.content"
                        placeholder="请输入备注"
                        type="textarea"
                        class="mr-8"
                        :autosize="{ minRows: 10, maxRows: 10 }"
                        show-word-limit
                        clearable
                    />
                    <!-- </div> -->
                </el-form-item>
            </el-form>
        </popup>
    </div>
</template>
<script lang="ts" setup>
import { apiOrderRemark } from "@/api/order/order";
import Popup from "@/components/popup/index.vue";
import type { FormInstance, FormRules } from "element-plus";
const emit = defineEmits(["success", "close"]);
const formRef = shallowRef<FormInstance>();
const popupRef = shallowRef<InstanceType<typeof Popup>>();

type RemarkType = {
    id: string | number;
    content: string;
}

const formData = reactive<RemarkType>({
    id: '',
    content: ''
});

const formRules = reactive<FormRules>({
    content: [
        {
            required: true,
            message: "请输入备注",
            trigger: ["blur"],
        },
    ],
});

const handleSubmit = async () => {
    await formRef.value?.validate();
    await apiOrderRemark({ 
        id: formData.id as number,
        shop_remark: formData.content
  })
    popupRef.value?.close();
    emit("success");
};

const open = (id: number, content: string) => {
    console.log(id, content)
    formData.id = id;
    formData.content = content;
    popupRef.value?.open();
};

const handleClose = () => {
    emit("close");
};

defineExpose({
    open
});
</script>
