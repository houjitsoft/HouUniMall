<template>
    <div class="edit-popup">
        <popup
            ref="popupRef"
            title="公告详情"
            :async="true"
            width="1000px"
            @confirm="handleSubmit"
            @close="handleClose"
        >
            <el-scrollbar height="800px">
                <div class="rich-text" v-html="formData.content"></div>
            </el-scrollbar>
        </popup>
    </div>
</template>
<script lang="ts" setup>
import type { CategoryForm } from '@/api/goods/category'
import { NoticeDetial } from '@/api/notice'
import Popup from '@/components/popup/index.vue'
import type { FormInstance, FormRules } from 'element-plus'
const emit = defineEmits(['success', 'close'])
const formRef = shallowRef<FormInstance>()
const popupRef = shallowRef<InstanceType<typeof Popup>>()
const mode = ref('add')

const formData = reactive<any>({
    id: '',
    title: '',
    content: '',
    priority: 0
})

const handleSubmit = async () => {
    popupRef.value?.close()
    emit('success')
}

const open = (type = 'add') => {
    mode.value = type
    popupRef.value?.open()
}

const setFormData = (data: CategoryForm) => {
    for (const key in formData) {
        if (data[key] != null && data[key] != undefined) {
            formData[key] = data[key]
        }
    }
}

const getDetail = async (row: Record<string, any>) => {
    const data: CategoryForm = await NoticeDetial({
        id: row.id
    })
    setFormData(data)
}

const handleClose = () => {
    emit('close')
}

defineExpose({
    open,
    setFormData,
    getDetail
})
</script>
