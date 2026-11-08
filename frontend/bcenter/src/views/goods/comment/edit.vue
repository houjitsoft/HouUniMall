<template>
    <div class="edit-popup">
        <popup
            ref="popupRef"
            title="回复评价"
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
                <el-form-item label="回复内容" prop="reply_content">
                    <!-- <div> -->
                    <el-input
                        v-model="formData.reply_content"
                        placeholder="请输入回复内容"
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
import { apiCommentReply } from "@/api/goods/comment";
import Popup from "@/components/popup/index.vue";
import type { FormInstance, FormRules } from "element-plus";
const emit = defineEmits(["success", "close"]);
const formRef = shallowRef<FormInstance>();
const popupRef = shallowRef<InstanceType<typeof Popup>>();

interface ReplyType {
    id: number;
    reply_content: string;
}

const formData = reactive<ReplyType>({
    id: -1,
    reply_content: "",
});

const formRules = reactive<FormRules>({
    reply_content: [
        {
            required: true,
            message: "请输入回复内容",
            trigger: ["blur"],
        },
    ],
});

const handleSubmit = async () => {
    await formRef.value?.validate();
    await apiCommentReply(formData)
    popupRef.value?.close();
    emit("success");
};

const open = (id: number, content: string) => {
    formData.id = id;
    formData.reply_content = content;
    popupRef.value?.open();
};

const handleClose = () => {
    emit("close");
};

defineExpose({ open });
</script>
