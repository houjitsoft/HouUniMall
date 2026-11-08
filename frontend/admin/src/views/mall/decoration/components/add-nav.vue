<template>
    <div>
        <div>
            <del-wrap
                class="w-full"
                v-for="(item, index) in modelValue"
                :key="index"
                @close="handleDelete(index)"
            >
                <div class="flex items-center w-full p-4 mb-4 bg-fill-light">
                    <material-picker
                        v-model="item.image"
                        upload-class="bg-body"
                        size="60px"
                        exclude-domain
                    >
                        <template #upload>
                            <div class="upload-btn w-[60px] h-[60px]">
                                <icon name="el-icon-Plus" :size="20" />
                            </div>
                        </template>
                    </material-picker>
                    <div class="flex-1 ml-3">
                        <div class="flex">
                            <span class="flex-none mr-3 tx-regular">名称</span>
                            <el-input v-model="item.name" placeholder="请输入名称" />
                        </div>
                        <div class="flex mt-[18px]">
                            <span class="flex-none mr-3 text-tx-regular">链接</span>
                            <link-picker v-model="item.link" />
                        </div>
                    </div>
                </div>
            </del-wrap>
        </div>
        <div>
            <el-button type="primary" @click="handleAdd" class="w-full">添加</el-button>
        </div>
    </div>
</template>
<script setup>
import feedback from '@/utils/feedback'

const props = defineProps({
    modelValue: {
        type: Array,
        default: () => []
    },
    max: {
        type: Number,
        default: 10
    },
    min: {
        type: Number,
        default: 1
    }
})

const handleAdd = () => {
    if (props.modelValue?.length < props.max) {
        props.modelValue.push({
            image: '',
            name: '导航名称',
            link: {}
        })
    } else {
        feedback.msgError(`最多添加${props.max}个`)
    }
}
const handleDelete = (index) => {
    if (props.modelValue?.length <= props.min) {
        return feedback.msgError(`最少保留${props.min}个`)
    }
    props.modelValue.splice(index, 1)
}
</script>

<style lang="scss" scoped></style>
