<template>
    <div class="edit-popup">
        <popup ref="popupRef" :title="popupTitle" :async="true" width="550px" @confirm="handleSubmit"
            @close="handleClose">
            <el-form ref="formRef" :model="formData" label-width="84px" :rules="formRules">
                <el-form-item label="分类图片" prop="image">
                    <material-picker v-model="formData.image" size="80px" :limit="1" />
                </el-form-item>
                <el-form-item label="分类名称" prop="name">
                    <div class="w-80">
                        <el-input v-model="formData.name" placeholder="请输入分类名称" maxlength="8" show-word-limit clearable />
                    </div>
                </el-form-item>
                <el-form-item label="排序">
                    <div>
                        <div class="w-80">
                            <el-input v-model="formData.sort" placeholder="请输入排序" clearable />
                        </div>
                        <div class="form-tips">默认为0， 数值越大越排前</div>
                    </div>
                </el-form-item>
                <el-form-item label="是否显示" prop="is_show">
                    <el-switch v-model="formData.is_show" :active-value="1" :inactive-value="0" />
                </el-form-item>
            </el-form>
        </popup>
    </div>
</template>
<script lang="ts" setup>
import type { CategoryForm } from "@/api/goods/category";
import { apiGoodsCategoryEdit, apiGoodsCategoryAdd, getGoodsCategoryDetail } from "@/api/goods/category";
import Popup from "@/components/popup/index.vue";
import type { FormInstance, FormRules } from "element-plus";
const emit = defineEmits(["success", "close"]);
const formRef = shallowRef<FormInstance>();
const popupRef = shallowRef<InstanceType<typeof Popup>>();
const mode = ref("add");

const popupTitle = computed(() => {
    return mode.value == "edit" ? "编辑经营类目" : "新增经营类目";
});

const formData = reactive<CategoryForm>({
    id: "",
    name: "",
    sort: 0,
    is_show: 1,
    image: ""
});

const formRules = reactive<FormRules>({
    image: [{ required: true, message: "请选择分类图片", trigger: ["blur"] }],
    name: [{ required: true, message: "请输入分类名称", trigger: ["blur"] }],
    is_show: [{ required: true, message: "请选择是否显示", trigger: ["blur"] }],
});

const handleSubmit = async () => {
    await formRef.value?.validate();
    mode.value == "edit"
        ? await apiGoodsCategoryEdit(formData)
        : await apiGoodsCategoryAdd(formData);
    popupRef.value?.close();
    emit("success");
};

const open = (type = "add") => {
    mode.value = type;
    popupRef.value?.open();
};

const setFormData = (data: CategoryForm) => {
    for (const key in formData) {
        if (data[key] != null && data[key] != undefined) {
            formData[key] = data[key];
        }
    }
};

const getDetail = async (row: Record<string, any>) => {
    const data: CategoryForm = await getGoodsCategoryDetail({
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
